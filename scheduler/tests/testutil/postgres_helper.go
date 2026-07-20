package testutil

import (
	"database/sql"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/adapters"
	ports "github.com/apache/airavata/scheduler/core/port"
	_ "github.com/lib/pq"
)

// PostgresTestDB wraps PostgreSQL database for integration tests
type PostgresTestDB struct {
	DB      *adapters.PostgresAdapter
	Repo    ports.RepositoryPort
	DSN     string
	cleanup func()
}

// SetupFreshPostgresTestDB creates a fresh PostgreSQL test database
func SetupFreshPostgresTestDB(t *testing.T, dsn string) *PostgresTestDB {
	// Use provided DSN or get from environment
	if dsn == "" {
		dsn = os.Getenv("TEST_DATABASE_DSN")
		if dsn == "" {
			// Use the same database as the API server for integration tests
			dsn = "postgres://user:password@localhost:5432/airavata?sslmode=disable"
		}
	}

	// Drop and recreate the database to ensure clean schema
	if err := recreateDatabase(dsn); err != nil {
		t.Fatalf("Failed to recreate database: %v", err)
	}

	// Create database adapter
	dbAdapter, err := adapters.NewPostgresAdapter(dsn)
	if err != nil {
		t.Fatalf("Failed to create database adapter: %v", err)
	}

	// Set connection pool limits to prevent leaks
	if sqlDB, err := dbAdapter.GetDB().DB(); err == nil {
		sqlDB.SetMaxOpenConns(10)
		sqlDB.SetMaxIdleConns(5)
		sqlDB.SetConnMaxLifetime(5 * time.Minute)
	}

	// Create repository
	repo := adapters.NewRepository(dbAdapter)

	// Run migrations
	if err := runMigrations(dbAdapter); err != nil {
		t.Fatalf("Failed to run migrations: %v", err)
	}

	return &PostgresTestDB{
		DB:   dbAdapter,
		Repo: repo,
		DSN:  dsn,
		cleanup: func() {
			dbAdapter.Close()
		},
	}
}

// Cleanup cleans up the test database
func (ptdb *PostgresTestDB) Cleanup() {
	if ptdb.cleanup != nil {
		ptdb.cleanup()
	}
}

// recreateDatabase drops and recreates the test database
func recreateDatabase(dsn string) error {
	// Parse DSN to extract database name
	// DSN format: postgres://user:password@localhost:5432/dbname?sslmode=disable
	parts := strings.Split(dsn, "/")
	if len(parts) < 4 {
		return fmt.Errorf("invalid DSN format: %s", dsn)
	}

	dbNamePart := strings.Split(parts[3], "?")[0]
	if dbNamePart == "" {
		return fmt.Errorf("no database name in DSN: %s", dsn)
	}

	// Connect to postgres database to manage the test database
	postgresDSN := "postgres://user:password@localhost:5432/postgres?sslmode=disable"

	db, err := sql.Open("postgres", postgresDSN)
	if err != nil {
		return fmt.Errorf("failed to connect to postgres: %w", err)
	}
	defer db.Close()

	// Terminate existing connections to the test database
	_, err = db.Exec(fmt.Sprintf("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '%s'", dbNamePart))
	if err != nil {
		// Ignore error if database doesn't exist
	}

	// Drop the test database if it exists
	_, err = db.Exec(fmt.Sprintf("DROP DATABASE IF EXISTS %s", dbNamePart))
	if err != nil {
		return fmt.Errorf("failed to drop test database: %w", err)
	}

	// Create the test database
	_, err = db.Exec(fmt.Sprintf("CREATE DATABASE %s", dbNamePart))
	if err != nil {
		return fmt.Errorf("failed to create test database: %w", err)
	}

	return nil
}

// runMigrations runs database migrations
func runMigrations(adapter *adapters.PostgresAdapter) error {
	// Get raw database connection
	db := adapter.GetDB()

	// Read the PostgreSQL schema file
	schemaPath := filepath.Join("..", "..", "db", "schema.sql")
	schemaSQL, err := os.ReadFile(schemaPath)
	if err != nil {
		return fmt.Errorf("failed to read schema file: %w", err)
	}

	err = db.Exec(string(schemaSQL)).Error
	return err
}

// cleanupTestDatabase removes all test data from the database
func cleanupTestDatabase(adapter *adapters.PostgresAdapter) error {
	db := adapter.GetDB()

	// Delete in reverse order of dependencies
	// Note: credentials table removed - credentials are now stored in OpenBao
	tables := []string{
		"audit_logs",
		"data_lineage",
		"data_cache",
		"workers",
		"tasks",
		"experiments",
		"storage_resources",
		"compute_resources",
		"projects",
		"users",
	}

	for _, table := range tables {
		if err := db.Exec(fmt.Sprintf("DELETE FROM %s", table)).Error; err != nil {
			return fmt.Errorf("failed to cleanup table %s: %w", table, err)
		}
	}

	return nil
}

// GetRawDB returns the raw database connection for direct SQL operations
func (ptdb *PostgresTestDB) GetRawDB() *sql.DB {
	sqlDB, _ := ptdb.DB.GetDB().DB()
	return sqlDB
}

// TruncateTable truncates a specific table
func (ptdb *PostgresTestDB) TruncateTable(tableName string) error {
	_, err := ptdb.GetRawDB().Exec(fmt.Sprintf("TRUNCATE TABLE %s CASCADE", tableName))
	return err
}

// CountRecords returns the number of records in a table
func (ptdb *PostgresTestDB) CountRecords(tableName string) (int, error) {
	var count int
	err := ptdb.GetRawDB().QueryRow(fmt.Sprintf("SELECT COUNT(*) FROM %s", tableName)).Scan(&count)
	return count, err
}

// TableExists checks if a table exists
func (ptdb *PostgresTestDB) TableExists(tableName string) (bool, error) {
	var exists bool
	query := `
		SELECT EXISTS (
			SELECT FROM information_schema.tables 
			WHERE table_schema = 'public' 
			AND table_name = $1
		)
	`
	err := ptdb.GetRawDB().QueryRow(query, tableName).Scan(&exists)
	return exists, err
}
