package testutil

import (
	"context"
	"fmt"
	"os"

	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/scheduler/adapters"
)

// TestEnvironment represents a test environment with database and configuration
type TestEnvironment struct {
	DB      *PostgresTestDB
	Config  *TestConfig
	Cleanup func()
}

// TestConfig is defined in test_config.go

// SetupTestEnvironment creates a test environment with fresh database
func SetupTestEnvironment(ctx context.Context) (*TestEnvironment, error) {
	// Get database URL from environment or use default
	databaseURL := os.Getenv("TEST_DATABASE_URL")
	if databaseURL == "" {
		databaseURL = "postgres://user:password@localhost:5432/airavata_scheduler_test?sslmode=disable"
	}

	// Connect to test database
	db, err := gorm.Open(postgres.Open(databaseURL), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent), // Suppress SQL logs in tests
	})
	if err != nil {
		return nil, fmt.Errorf("failed to connect to test database: %w", err)
	}

	// Get database instance for raw SQL operations
	sqlDB, err := db.DB()
	if err != nil {
		return nil, fmt.Errorf("failed to get database instance: %w", err)
	}

	// Test connection
	if err := sqlDB.Ping(); err != nil {
		return nil, fmt.Errorf("failed to ping test database: %w", err)
	}

	// Create test database if it doesn't exist
	testDBName := "airavata_scheduler_test"
	if err := createTestDatabaseIfNotExists(databaseURL, testDBName); err != nil {
		return nil, fmt.Errorf("failed to create test database: %w", err)
	}

	// Connect to the test database
	testDatabaseURL := fmt.Sprintf("postgres://user:password@localhost:5432/%s?sslmode=disable", testDBName)
	_, err = gorm.Open(postgres.Open(testDatabaseURL), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		return nil, fmt.Errorf("failed to connect to test database: %w", err)
	}

	// Create PostgresAdapter
	postgresAdapter, err := adapters.NewPostgresAdapter(testDatabaseURL)
	if err != nil {
		return nil, fmt.Errorf("failed to create postgres adapter: %w", err)
	}

	// Run migrations
	if err := runMigrations(postgresAdapter); err != nil {
		return nil, fmt.Errorf("failed to run migrations: %w", err)
	}

	// Create repository
	repo := adapters.NewRepository(postgresAdapter)

	// Create cleanup function
	cleanup := func() {
		// Close database connection
		if sqlDB, err := postgresAdapter.GetDB().DB(); err == nil {
			sqlDB.Close()
		}

		// Drop test database
		dropTestDatabase(databaseURL, testDBName)
	}

	// Create PostgresTestDB
	postgresTestDB := &PostgresTestDB{
		DB:      postgresAdapter,
		Repo:    repo,
		DSN:     testDatabaseURL,
		cleanup: cleanup,
	}

	// Create test config
	config := &TestConfig{
		DatabaseURL: testDatabaseURL,
	}

	return &TestEnvironment{
		DB:      postgresTestDB,
		Config:  config,
		Cleanup: cleanup,
	}, nil
}

// createTestDatabaseIfNotExists creates a test database if it doesn't exist
func createTestDatabaseIfNotExists(databaseURL, dbName string) error {
	// Connect to postgres database to create test database
	postgresURL := "postgres://user:password@localhost:5432/postgres?sslmode=disable"
	db, err := gorm.Open(postgres.Open(postgresURL), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		return err
	}

	// Check if database exists
	var exists bool
	err = db.Raw("SELECT EXISTS(SELECT datname FROM pg_catalog.pg_database WHERE datname = ?)", dbName).Scan(&exists).Error
	if err != nil {
		return err
	}

	// Create database if it doesn't exist
	if !exists {
		err = db.Exec(fmt.Sprintf("CREATE DATABASE %s", dbName)).Error
		if err != nil {
			return err
		}
	}

	// Close connection
	if sqlDB, err := db.DB(); err == nil {
		sqlDB.Close()
	}

	return nil
}

// dropTestDatabase drops the test database
func dropTestDatabase(databaseURL, dbName string) {
	// Connect to postgres database to drop test database
	postgresURL := "postgres://postgres:password@localhost:5432/postgres?sslmode=disable"
	db, err := gorm.Open(postgres.Open(postgresURL), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		return
	}

	// Terminate connections to the test database
	db.Exec(fmt.Sprintf("SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '%s' AND pid <> pg_backend_pid()", dbName))

	// Drop database
	db.Exec(fmt.Sprintf("DROP DATABASE IF EXISTS %s", dbName))

	// Close connection
	if sqlDB, err := db.DB(); err == nil {
		sqlDB.Close()
	}
}
