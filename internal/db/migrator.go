package db

import (
	"context"
	"embed"
	"fmt"
	"regexp"
	"sort"
	"strconv"
	"strings"
	"time"

	"gorm.io/gorm"
)

//go:embed migrations/*.sql
var migrationFiles embed.FS

// Migration is one versioned, embedded schema change.
type Migration struct {
	Version     int64
	Description string
	Filename    string
	SQL         string
}

var migrationFilenamePattern = regexp.MustCompile(`^(\d+)_([a-zA-Z0-9]+(?:_[a-zA-Z0-9]+)*)\.sql$`)

// Migrations returns every embedded migration under migrations/, ordered by version
// ascending.
//
// It panics on a malformed filename or a duplicate version rather than returning an
// error: both are authoring mistakes caught by any test that calls this, not
// conditions a production caller could sensibly recover from.
func Migrations() []Migration {
	entries, err := migrationFiles.ReadDir("migrations")
	if err != nil {
		panic(fmt.Errorf("read embedded migrations: %w", err))
	}

	out := make([]Migration, 0, len(entries))
	seenBy := make(map[int64]string, len(entries))
	for _, e := range entries {
		groups := migrationFilenamePattern.FindStringSubmatch(e.Name())
		if groups == nil {
			panic(fmt.Errorf("migration file %q does not match NNNN_description.sql", e.Name()))
		}
		version, err := strconv.ParseInt(groups[1], 10, 64)
		if err != nil {
			panic(fmt.Errorf("migration file %q: %w", e.Name(), err))
		}
		if prior, ok := seenBy[version]; ok {
			panic(fmt.Errorf("migration version %d used by both %q and %q", version, prior, e.Name()))
		}
		seenBy[version] = e.Name()

		raw, err := migrationFiles.ReadFile("migrations/" + e.Name())
		if err != nil {
			panic(fmt.Errorf("read migration %q: %w", e.Name(), err))
		}
		out = append(out, Migration{
			Version:     version,
			Description: groups[2],
			Filename:    e.Name(),
			SQL:         string(raw),
		})
	}

	sort.Slice(out, func(i, j int) bool { return out[i].Version < out[j].Version })
	return out
}

// statements splits a migration file into individual statements.
//
// A statement ends with a semicolon immediately followed by a newline; the file must
// end the same way, including after its last statement. This is a deliberately simple
// convention rather than a real SQL parser, so a migration's DDL must not contain a
// literal ";\n" — comment lines and string literals in these files never need one.
func statements(sqlText string) []string {
	raw := strings.Split(sqlText, ";\n")
	out := make([]string, 0, len(raw))
	for _, s := range raw {
		s = strings.TrimSpace(s)
		if s == "" {
			continue
		}
		out = append(out, s)
	}
	return out
}

// migrationsTableDDL creates the tracking table on first use. It is issued directly
// rather than through AutoMigrate, so the migration runner has no dependency on the
// entity model — it must still work once a future schema change has moved on from it.
const migrationsTableDDL = `
CREATE TABLE IF NOT EXISTS schema_migrations (
	version bigint NOT NULL PRIMARY KEY,
	description varchar(255) NOT NULL,
	applied_at timestamp NOT NULL
)`

// Status is one migration's applied state.
type Status struct {
	Version     int64
	Description string
	Applied     bool
	AppliedAt   *time.Time
}

// Migrator applies versioned migrations and tracks which have run, in the
// schema_migrations table.
//
// Migrations are forward-only, matching the AutoMigrate philosophy this framework
// exists to replace in production: a mistake is corrected with a new migration, not a
// down script that would have to reconstruct dropped data anyway.
type Migrator struct {
	db         *gorm.DB
	migrations []Migration
}

// NewMigrator returns a migrator over the given, explicit migration set — Migrations()
// for the real embedded set in production, or a small hand-built slice in a test, so
// the runner's mechanics can be exercised without depending on the committed SQL
// files or a PostgreSQL-specific dialect.
//
// The set is sorted by version here, so every method applies and reports migrations
// in order regardless of the order the caller happened to list them in.
func NewMigrator(gdb *gorm.DB, migrations []Migration) *Migrator {
	sorted := make([]Migration, len(migrations))
	copy(sorted, migrations)
	sort.Slice(sorted, func(i, j int) bool { return sorted[i].Version < sorted[j].Version })
	return &Migrator{db: gdb, migrations: sorted}
}

func (m *Migrator) ensureTable(ctx context.Context) error {
	return m.db.WithContext(ctx).Exec(migrationsTableDDL).Error
}

// Applied returns the versions already recorded as applied, ascending.
func (m *Migrator) Applied(ctx context.Context) ([]int64, error) {
	if err := m.ensureTable(ctx); err != nil {
		return nil, err
	}
	var versions []int64
	err := m.db.WithContext(ctx).Raw(
		"SELECT version FROM schema_migrations ORDER BY version").Scan(&versions).Error
	return versions, err
}

// Pending returns the embedded migrations not yet applied, ascending.
func (m *Migrator) Pending(ctx context.Context) ([]Migration, error) {
	applied, err := m.Applied(ctx)
	if err != nil {
		return nil, err
	}
	appliedSet := make(map[int64]bool, len(applied))
	for _, v := range applied {
		appliedSet[v] = true
	}

	var pending []Migration
	for _, mig := range m.migrations {
		if !appliedSet[mig.Version] {
			pending = append(pending, mig)
		}
	}
	return pending, nil
}

// Up applies every pending migration in order and returns the ones it applied.
//
// Each migration runs in its own transaction, and on PostgreSQL that covers the DDL as
// well as the bookkeeping insert: a file that fails partway through leaves the schema
// as it was, so a failed migration can be fixed and re-run rather than needing a
// repair migration for whatever it managed to apply. (This is one of the things that
// changed with the move off MariaDB, where InnoDB committed each DDL statement
// regardless of the surrounding transaction.)
func (m *Migrator) Up(ctx context.Context) ([]Migration, error) {
	pending, err := m.Pending(ctx)
	if err != nil {
		return nil, err
	}

	applied := make([]Migration, 0, len(pending))
	for _, mig := range pending {
		err := m.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
			for _, stmt := range statements(mig.SQL) {
				if err := tx.Exec(stmt).Error; err != nil {
					return fmt.Errorf("migration %d (%s): %w", mig.Version, mig.Filename, err)
				}
			}
			return tx.Exec(
				"INSERT INTO schema_migrations (version, description, applied_at) VALUES (?, ?, ?)",
				mig.Version, mig.Description, time.Now().UTC(),
			).Error
		})
		if err != nil {
			return applied, err
		}
		applied = append(applied, mig)
	}
	return applied, nil
}

// Status reports every known migration's applied state, ascending by version.
func (m *Migrator) Status(ctx context.Context) ([]Status, error) {
	if err := m.ensureTable(ctx); err != nil {
		return nil, err
	}

	var rows []struct {
		Version   int64
		AppliedAt time.Time
	}
	if err := m.db.WithContext(ctx).Raw(
		"SELECT version, applied_at FROM schema_migrations").Scan(&rows).Error; err != nil {
		return nil, err
	}
	appliedAt := make(map[int64]time.Time, len(rows))
	for _, r := range rows {
		appliedAt[r.Version] = r.AppliedAt
	}

	out := make([]Status, 0, len(m.migrations))
	for _, mig := range m.migrations {
		s := Status{Version: mig.Version, Description: mig.Description}
		if at, ok := appliedAt[mig.Version]; ok {
			s.Applied = true
			s.AppliedAt = &at
		}
		out = append(out, s)
	}
	return out, nil
}
