package unit

import (
	"fmt"
	"strings"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/adapters"
)

// setupTestDB creates a fresh, isolated database for each test
func setupTestDB(t *testing.T) *adapters.PostgresAdapter {
	// Use unique database name per test to ensure isolation
	// Sanitize test name to avoid special characters in DSN
	testName := strings.ReplaceAll(t.Name(), "/", "_")
	testName = strings.ReplaceAll(testName, " ", "_")

	dsn := fmt.Sprintf("file::memory:?cache=shared&_testid=%s_%d",
		testName, time.Now().UnixNano())

	db, err := adapters.NewPostgresAdapter(dsn)
	if err != nil {
		t.Fatalf("Failed to create test database: %v", err)
	}

	return db
}

// cleanupTestDB closes the database connection
func cleanupTestDB(t *testing.T, db *adapters.PostgresAdapter) {
	if db != nil {
		db.Close()
	}
}

// uniqueID generates a unique ID with the given prefix
func uniqueID(prefix string) string {
	return fmt.Sprintf("%s-%d", prefix, time.Now().UnixNano())
}
