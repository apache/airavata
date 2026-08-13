package db_test

import (
	"context"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/db"
)

// openMigratorTestDB opens a fresh in-memory database with no schema at all — the
// migrator must be able to create its own tracking table from nothing, the same as
// against a brand new production database.
func openMigratorTestDB(t *testing.T) *gorm.DB {
	t.Helper()
	gdb, err := gorm.Open(sqlite.Open("file::memory:?_pragma=foreign_keys(1)"), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	return gdb
}

func TestUpAppliesPendingMigrationsInOrder(t *testing.T) {
	gdb := openMigratorTestDB(t)
	ctx := context.Background()

	migrations := []db.Migration{
		{Version: 2, Description: "second", Filename: "0002_second.sql", SQL: "CREATE TABLE second (id INTEGER PRIMARY KEY);\n"},
		{Version: 1, Description: "first", Filename: "0001_first.sql", SQL: "CREATE TABLE first (id INTEGER PRIMARY KEY);\n"},
	}
	m := db.NewMigrator(gdb, migrations)

	applied, err := m.Up(ctx)
	if err != nil {
		t.Fatalf("Up: %v", err)
	}
	if len(applied) != 2 || applied[0].Version != 1 || applied[1].Version != 2 {
		t.Fatalf("applied = %+v, want version 1 then 2", applied)
	}

	for _, table := range []string{"first", "second"} {
		var count int64
		if err := gdb.Table(table).Count(&count).Error; err != nil {
			t.Errorf("table %s was not created: %v", table, err)
		}
	}
}

func TestUpIsIdempotent(t *testing.T) {
	gdb := openMigratorTestDB(t)
	ctx := context.Background()

	migrations := []db.Migration{
		{Version: 1, Description: "first", Filename: "0001_first.sql", SQL: "CREATE TABLE first (id INTEGER PRIMARY KEY);\n"},
	}
	m := db.NewMigrator(gdb, migrations)

	if _, err := m.Up(ctx); err != nil {
		t.Fatalf("first Up: %v", err)
	}

	// A second run must not try to re-apply and fail against the now-existing table.
	applied, err := m.Up(ctx)
	if err != nil {
		t.Fatalf("second Up: %v", err)
	}
	if len(applied) != 0 {
		t.Errorf("second Up applied = %+v, want none (already applied)", applied)
	}
}

func TestUpOnlyAppliesNewlyAddedMigrations(t *testing.T) {
	gdb := openMigratorTestDB(t)
	ctx := context.Background()

	first := []db.Migration{
		{Version: 1, Description: "first", Filename: "0001_first.sql", SQL: "CREATE TABLE first (id INTEGER PRIMARY KEY);\n"},
	}
	if _, err := db.NewMigrator(gdb, first).Up(ctx); err != nil {
		t.Fatalf("Up with one migration: %v", err)
	}

	// Simulates a later deploy that ships an additional migration on top of one
	// already applied against this database.
	both := append(first, db.Migration{
		Version: 2, Description: "second", Filename: "0002_second.sql", SQL: "CREATE TABLE second (id INTEGER PRIMARY KEY);\n",
	})
	applied, err := db.NewMigrator(gdb, both).Up(ctx)
	if err != nil {
		t.Fatalf("Up with two migrations: %v", err)
	}
	if len(applied) != 1 || applied[0].Version != 2 {
		t.Fatalf("applied = %+v, want only version 2", applied)
	}
}

func TestStatusReportsAppliedAndPending(t *testing.T) {
	gdb := openMigratorTestDB(t)
	ctx := context.Background()

	migrations := []db.Migration{
		{Version: 1, Description: "first", Filename: "0001_first.sql", SQL: "CREATE TABLE first (id INTEGER PRIMARY KEY);\n"},
		{Version: 2, Description: "second", Filename: "0002_second.sql", SQL: "CREATE TABLE second (id INTEGER PRIMARY KEY);\n"},
	}
	m := db.NewMigrator(gdb, migrations)

	if _, err := db.NewMigrator(gdb, migrations[:1]).Up(ctx); err != nil {
		t.Fatalf("Up: %v", err)
	}

	status, err := m.Status(ctx)
	if err != nil {
		t.Fatalf("Status: %v", err)
	}
	if len(status) != 2 {
		t.Fatalf("status = %+v, want 2 entries", status)
	}
	if !status[0].Applied || status[0].AppliedAt == nil {
		t.Errorf("version 1 = %+v, want applied with a timestamp", status[0])
	}
	if status[1].Applied {
		t.Errorf("version 2 = %+v, want pending", status[1])
	}
}

// A migration whose DDL fails must not be recorded as applied — a caller retrying
// after fixing the SQL should see it as still pending, not skip it.
func TestFailedMigrationIsNotRecordedAsApplied(t *testing.T) {
	gdb := openMigratorTestDB(t)
	ctx := context.Background()

	migrations := []db.Migration{
		{Version: 1, Description: "broken", Filename: "0001_broken.sql", SQL: "CREATE TABLE this is not valid sql;\n"},
	}
	m := db.NewMigrator(gdb, migrations)

	if _, err := m.Up(ctx); err == nil {
		t.Fatal("Up with invalid SQL: want an error")
	}

	pending, err := m.Pending(ctx)
	if err != nil {
		t.Fatalf("Pending: %v", err)
	}
	if len(pending) != 1 {
		t.Errorf("pending = %+v, want the broken migration still pending", pending)
	}
}

func TestMigrationsAreDiscoveredFromEmbeddedFiles(t *testing.T) {
	migrations := db.Migrations()
	if len(migrations) == 0 {
		t.Fatal("Migrations() = empty, want at least the baseline")
	}
	if migrations[0].Version != 1 || migrations[0].Filename != "0001_baseline.sql" {
		t.Errorf("first migration = %+v, want version 1 (0001_baseline.sql)", migrations[0])
	}
	for i := 1; i < len(migrations); i++ {
		if migrations[i-1].Version >= migrations[i].Version {
			t.Errorf("migrations not strictly ascending: %d then %d", migrations[i-1].Version, migrations[i].Version)
		}
	}
}
