package db_test

import (
	"context"
	"fmt"
	"os"
	"sort"
	"strings"
	"testing"

	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/db"
)

// These tests run against a real PostgreSQL server, which is the only way to check the
// two paths a schema can arrive by — AutoMigrate in development, versioned migrations
// in production — actually agree. Everything else in this package uses SQLite, which
// verifies the entity model above the dialect but not the DDL any one server emits.
//
// Set AIRAVATA_TEST_POSTGRES_DSN to run them; they skip otherwise, so the suite stays
// green on a machine with no database:
//
//	docker compose -f dev-tools/compose/compose.yml up -d postgres
//	AIRAVATA_TEST_POSTGRES_DSN='postgres://airavata:123456@localhost:15432/airavata?sslmode=disable' go test ./internal/db/
//
// Each test works in a schema of its own and drops it afterwards, so they neither
// disturb a development database nor each other.
func openPostgres(t *testing.T, schema string) *gorm.DB {
	t.Helper()

	dsn := os.Getenv("AIRAVATA_TEST_POSTGRES_DSN")
	if dsn == "" {
		t.Skip("AIRAVATA_TEST_POSTGRES_DSN not set")
	}

	gdb, err := db.Open(db.DefaultConfig(dsn), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		t.Fatalf("open postgres: %v", err)
	}
	for _, stmt := range []string{
		fmt.Sprintf("DROP SCHEMA IF EXISTS %s CASCADE", schema),
		fmt.Sprintf("CREATE SCHEMA %s", schema),
		fmt.Sprintf("SET search_path TO %s", schema),
	} {
		if err := gdb.Exec(stmt).Error; err != nil {
			t.Fatalf("%s: %v", stmt, err)
		}
	}
	t.Cleanup(func() {
		gdb.Exec(fmt.Sprintf("DROP SCHEMA IF EXISTS %s CASCADE", schema))
		if sqlDB, err := gdb.DB(); err == nil {
			sqlDB.Close()
		}
	})
	return gdb
}

// describe reads the schema back out of the catalog: every column with its type and
// nullability, every constraint with its columns and referenced table, every index.
// Comparing these strings is what makes "the same schema" a checkable claim.
func describe(t *testing.T, gdb *gorm.DB, schema string) []string {
	t.Helper()

	var out []string
	collect := func(query string, args ...any) {
		rows, err := gdb.Raw(query, args...).Rows()
		if err != nil {
			t.Fatalf("describe: %v", err)
		}
		defer rows.Close()
		cols, _ := rows.Columns()
		for rows.Next() {
			vals := make([]any, len(cols))
			ptrs := make([]any, len(cols))
			for i := range vals {
				ptrs[i] = &vals[i]
			}
			if err := rows.Scan(ptrs...); err != nil {
				t.Fatalf("scan: %v", err)
			}
			parts := make([]string, 0, len(cols))
			for _, v := range vals {
				parts = append(parts, fmt.Sprintf("%v", v))
			}
			out = append(out, strings.Join(parts, " "))
		}
	}

	collect(`SELECT 'column', table_name, column_name, data_type,
		coalesce(character_maximum_length, 0), is_nullable
		FROM information_schema.columns WHERE table_schema = ?`, schema)
	collect(`SELECT 'constraint', tc.table_name, tc.constraint_name, tc.constraint_type,
		coalesce(kcu.column_name, ''), coalesce(ccu.table_name, ''), coalesce(rc.delete_rule, ''), coalesce(rc.update_rule, '')
		FROM information_schema.table_constraints tc
		LEFT JOIN information_schema.key_column_usage kcu
			ON kcu.constraint_name = tc.constraint_name AND kcu.table_schema = tc.table_schema
		LEFT JOIN information_schema.referential_constraints rc
			ON rc.constraint_name = tc.constraint_name AND rc.constraint_schema = tc.table_schema
		LEFT JOIN information_schema.constraint_column_usage ccu
			ON ccu.constraint_name = tc.constraint_name AND ccu.table_schema = tc.table_schema
		WHERE tc.table_schema = ?
		-- PostgreSQL names the implicit CHECK behind every NOT NULL after the table's
		-- OID, so those names differ between any two schemas by construction. The
		-- nullability they express is already compared column by column above.
		AND tc.constraint_name NOT SIMILAR TO '[0-9]+_[0-9]+_[0-9]+_not_null'`, schema)
	collect(`SELECT 'index', tablename, indexname, indexdef FROM pg_indexes WHERE schemaname = ?`, schema)

	sort.Strings(out)
	return out
}

// The baseline migration and AutoMigrate must produce the same schema. If they drift, a
// production database migrated from files stops matching a development one, and every
// test that passes against SQLite says nothing about it.
func TestBaselineMigrationMatchesAutoMigrate(t *testing.T) {
	ctx := context.Background()

	migrated := openPostgres(t, "airavata_migrated")
	if _, err := db.NewMigrator(migrated, db.Migrations()).Up(ctx); err != nil {
		t.Fatalf("migrate up: %v", err)
	}
	// The migrator's own bookkeeping table is not part of the entity model.
	if err := migrated.Exec("DROP TABLE schema_migrations").Error; err != nil {
		t.Fatalf("drop schema_migrations: %v", err)
	}

	auto := openPostgres(t, "airavata_auto")
	if err := db.AutoMigrate(auto); err != nil {
		t.Fatalf("automigrate: %v", err)
	}

	fromFiles := describe(t, migrated, "airavata_migrated")
	fromModel := describe(t, auto, "airavata_auto")

	if len(fromFiles) != len(fromModel) {
		t.Errorf("migrated schema has %d catalog rows, entity model has %d", len(fromFiles), len(fromModel))
	}
	for i := 0; i < len(fromFiles) && i < len(fromModel); i++ {
		// The schema name appears inside index definitions, so compare with it removed.
		got := strings.ReplaceAll(fromFiles[i], "airavata_migrated", "")
		want := strings.ReplaceAll(fromModel[i], "airavata_auto", "")
		if got != want {
			t.Errorf("schema differs:\n from migrations: %s\n from entities:   %s", got, want)
		}
	}
}

// AutoMigrate has to work from nothing on PostgreSQL, which is not a given: the server
// validates that a referenced table exists when the referencing table is declared, so
// any cycle in the entity graph fails outright rather than being resolved by ordering.
func TestAutoMigrateSucceedsOnEmptyPostgres(t *testing.T) {
	gdb := openPostgres(t, "airavata_fresh")
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatalf("automigrate on an empty database: %v", err)
	}
	// Idempotent, the same way ddl-auto is: a second run adds nothing and fails on
	// nothing.
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatalf("second automigrate: %v", err)
	}
}
