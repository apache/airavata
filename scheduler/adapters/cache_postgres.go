package adapters

import (
	"context"
	"encoding/json"
	"fmt"
	"strconv"
	"strings"
	"time"

	"gorm.io/gorm"

	ports "github.com/apache/airavata/scheduler/core/port"
)

// PostgresCacheAdapter implements ports.CachePort using PostgreSQL storage
type PostgresCacheAdapter struct {
	db *gorm.DB
}

// CacheEntry represents a cache entry in the database
type CacheEntry struct {
	Key          string    `gorm:"primaryKey;size:1000"`
	Value        []byte    `gorm:"type:bytea;not null"`
	ExpiresAt    time.Time `gorm:"not null;index"`
	CreatedAt    time.Time `gorm:"autoCreateTime"`
	UpdatedAt    time.Time `gorm:"autoUpdateTime"`
	AccessCount  int       `gorm:"default:0"`
	LastAccessed time.Time `gorm:"autoUpdateTime"`
}

// NewPostgresCacheAdapter creates a new PostgreSQL cache adapter
func NewPostgresCacheAdapter(db *gorm.DB) *PostgresCacheAdapter {
	adapter := &PostgresCacheAdapter{
		db: db,
	}

	// Auto-migrate the cache_entries table
	if err := db.AutoMigrate(&CacheEntry{}); err != nil {
		// Log error but don't fail startup
		fmt.Printf("Warning: failed to auto-migrate cache_entries table: %v\n", err)
	}

	// Start cleanup goroutine
	go adapter.startCleanupRoutine()

	return adapter
}

// Get retrieves a value from cache
func (c *PostgresCacheAdapter) Get(ctx context.Context, key string) ([]byte, error) {
	// Use raw SQL for better performance and to handle expiration
	query := `
		UPDATE cache_entries 
		SET access_count = access_count + 1, last_accessed = CURRENT_TIMESTAMP 
		WHERE key = $1 AND expires_at > CURRENT_TIMESTAMP
		RETURNING value
	`

	var value []byte
	err := c.db.WithContext(ctx).Raw(query, key).Scan(&value).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, ports.ErrCacheMiss
		}
		return nil, fmt.Errorf("failed to get cache entry: %w", err)
	}

	if len(value) == 0 {
		return nil, ports.ErrCacheMiss
	}

	return value, nil
}

// Set stores a value in cache
func (c *PostgresCacheAdapter) Set(ctx context.Context, key string, value []byte, ttl time.Duration) error {
	expiresAt := time.Now().Add(ttl)

	// Use upsert (INSERT ... ON CONFLICT)
	err := c.db.WithContext(ctx).Exec(`
		INSERT INTO cache_entries (key, value, expires_at, created_at, updated_at, access_count, last_accessed)
		VALUES ($1, $2, $3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP)
		ON CONFLICT (key) DO UPDATE SET
			value = EXCLUDED.value,
			expires_at = EXCLUDED.expires_at,
			updated_at = CURRENT_TIMESTAMP,
			access_count = 0,
			last_accessed = CURRENT_TIMESTAMP
	`, key, value, expiresAt).Error

	if err != nil {
		return fmt.Errorf("failed to set cache entry: %w", err)
	}

	return nil
}

// Delete removes a value from cache
func (c *PostgresCacheAdapter) Delete(ctx context.Context, key string) error {
	err := c.db.WithContext(ctx).Exec("DELETE FROM cache_entries WHERE key = $1", key).Error
	if err != nil {
		return fmt.Errorf("failed to delete cache entry: %w", err)
	}
	return nil
}

// Exists checks if a key exists in cache
func (c *PostgresCacheAdapter) Exists(ctx context.Context, key string) (bool, error) {
	var count int64
	err := c.db.WithContext(ctx).Raw(
		"SELECT COUNT(*) FROM cache_entries WHERE key = $1 AND expires_at > CURRENT_TIMESTAMP",
		key,
	).Scan(&count).Error

	if err != nil {
		return false, fmt.Errorf("failed to check cache entry existence: %w", err)
	}

	return count > 0, nil
}

// GetMultiple retrieves multiple values from cache
func (c *PostgresCacheAdapter) GetMultiple(ctx context.Context, keys []string) (map[string][]byte, error) {
	if len(keys) == 0 {
		return make(map[string][]byte), nil
	}

	// Build placeholders for IN clause
	placeholders := make([]string, len(keys))
	args := make([]interface{}, len(keys))
	for i, key := range keys {
		placeholders[i] = fmt.Sprintf("$%d", i+1)
		args[i] = key
	}

	query := fmt.Sprintf(`
		UPDATE cache_entries 
		SET access_count = access_count + 1, last_accessed = CURRENT_TIMESTAMP 
		WHERE key IN (%s) AND expires_at > CURRENT_TIMESTAMP
		RETURNING key, value
	`, strings.Join(placeholders, ","))

	rows, err := c.db.WithContext(ctx).Raw(query, args...).Rows()
	if err != nil {
		return nil, fmt.Errorf("failed to get multiple cache entries: %w", err)
	}
	defer rows.Close()

	result := make(map[string][]byte)
	for rows.Next() {
		var key string
		var value []byte
		if err := rows.Scan(&key, &value); err != nil {
			return nil, fmt.Errorf("failed to scan cache entry: %w", err)
		}
		result[key] = value
	}

	return result, nil
}

// SetMultiple stores multiple values in cache
func (c *PostgresCacheAdapter) SetMultiple(ctx context.Context, items map[string][]byte, ttl time.Duration) error {
	if len(items) == 0 {
		return nil
	}

	expiresAt := time.Now().Add(ttl)

	// Use transaction for atomicity
	return c.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		for key, value := range items {
			err := tx.Exec(`
				INSERT INTO cache_entries (key, value, expires_at, created_at, updated_at, access_count, last_accessed)
				VALUES ($1, $2, $3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP)
				ON CONFLICT (key) DO UPDATE SET
					value = EXCLUDED.value,
					expires_at = EXCLUDED.expires_at,
					updated_at = CURRENT_TIMESTAMP,
					access_count = 0,
					last_accessed = CURRENT_TIMESTAMP
			`, key, value, expiresAt).Error

			if err != nil {
				return fmt.Errorf("failed to set cache entry %s: %w", key, err)
			}
		}
		return nil
	})
}

// DeleteMultiple removes multiple values from cache
func (c *PostgresCacheAdapter) DeleteMultiple(ctx context.Context, keys []string) error {
	if len(keys) == 0 {
		return nil
	}

	// Build placeholders for IN clause
	placeholders := make([]string, len(keys))
	args := make([]interface{}, len(keys))
	for i, key := range keys {
		placeholders[i] = fmt.Sprintf("$%d", i+1)
		args[i] = key
	}

	query := fmt.Sprintf("DELETE FROM cache_entries WHERE key IN (%s)", strings.Join(placeholders, ","))
	err := c.db.WithContext(ctx).Exec(query, args...).Error
	if err != nil {
		return fmt.Errorf("failed to delete multiple cache entries: %w", err)
	}

	return nil
}

// Keys returns all keys matching a pattern
func (c *PostgresCacheAdapter) Keys(ctx context.Context, pattern string) ([]string, error) {
	var keys []string

	// Convert Redis-style pattern to SQL LIKE pattern
	sqlPattern := strings.ReplaceAll(pattern, "*", "%")

	err := c.db.WithContext(ctx).Raw(
		"SELECT key FROM cache_entries WHERE key LIKE $1 AND expires_at > CURRENT_TIMESTAMP",
		sqlPattern,
	).Scan(&keys).Error

	if err != nil {
		return nil, fmt.Errorf("failed to get cache keys: %w", err)
	}

	return keys, nil
}

// DeletePattern removes all keys matching a pattern
func (c *PostgresCacheAdapter) DeletePattern(ctx context.Context, pattern string) error {
	// Convert Redis-style pattern to SQL LIKE pattern
	sqlPattern := strings.ReplaceAll(pattern, "*", "%")

	err := c.db.WithContext(ctx).Exec(
		"DELETE FROM cache_entries WHERE key LIKE $1",
		sqlPattern,
	).Error

	if err != nil {
		return fmt.Errorf("failed to delete cache pattern: %w", err)
	}

	return nil
}

// TTL returns the time to live for a key
func (c *PostgresCacheAdapter) TTL(ctx context.Context, key string) (time.Duration, error) {
	var expiresAt time.Time
	err := c.db.WithContext(ctx).Raw(
		"SELECT expires_at FROM cache_entries WHERE key = $1 AND expires_at > CURRENT_TIMESTAMP",
		key,
	).Scan(&expiresAt).Error

	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return 0, ports.ErrCacheMiss
		}
		return 0, fmt.Errorf("failed to get cache TTL: %w", err)
	}

	ttl := time.Until(expiresAt)
	if ttl <= 0 {
		return 0, ports.ErrCacheMiss
	}

	return ttl, nil
}

// Expire sets expiration for a key
func (c *PostgresCacheAdapter) Expire(ctx context.Context, key string, ttl time.Duration) error {
	expiresAt := time.Now().Add(ttl)

	result := c.db.WithContext(ctx).Exec(
		"UPDATE cache_entries SET expires_at = $1, updated_at = CURRENT_TIMESTAMP WHERE key = $2",
		expiresAt, key,
	)

	if result.Error != nil {
		return fmt.Errorf("failed to expire cache entry: %w", result.Error)
	}

	if result.RowsAffected == 0 {
		return ports.ErrCacheMiss
	}

	return nil
}

// Increment increments a numeric value
func (c *PostgresCacheAdapter) Increment(ctx context.Context, key string) (int64, error) {
	return c.IncrementBy(ctx, key, 1)
}

// Decrement decrements a numeric value
func (c *PostgresCacheAdapter) Decrement(ctx context.Context, key string) (int64, error) {
	return c.IncrementBy(ctx, key, -1)
}

// IncrementBy increments a numeric value by delta
func (c *PostgresCacheAdapter) IncrementBy(ctx context.Context, key string, delta int64) (int64, error) {
	var newValue int64

	// Use atomic increment with upsert
	err := c.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// First, try to get existing value
		var value []byte
		err := tx.Raw(
			"SELECT value FROM cache_entries WHERE key = $1 AND expires_at > CURRENT_TIMESTAMP",
			key,
		).Scan(&value).Error

		var currentValue int64
		if err == gorm.ErrRecordNotFound {
			// Key doesn't exist, create with delta value
			currentValue = 0
		} else if err != nil {
			return err
		} else {
			// Parse existing value
			if len(value) > 0 {
				if val, err := strconv.ParseInt(string(value), 10, 64); err == nil {
					currentValue = val
				}
			}
		}

		newValue = currentValue + delta
		newValueBytes := []byte(strconv.FormatInt(newValue, 10))

		// Upsert with new value
		return tx.Exec(`
			INSERT INTO cache_entries (key, value, expires_at, created_at, updated_at, access_count, last_accessed)
			VALUES ($1, $2, $3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP)
			ON CONFLICT (key) DO UPDATE SET
				value = EXCLUDED.value,
				updated_at = CURRENT_TIMESTAMP,
				last_accessed = CURRENT_TIMESTAMP
		`, key, newValueBytes, time.Now().Add(24*time.Hour)).Error
	})

	if err != nil {
		return 0, fmt.Errorf("failed to increment cache value: %w", err)
	}

	return newValue, nil
}

// ListPush adds values to a list
func (c *PostgresCacheAdapter) ListPush(ctx context.Context, key string, values ...[]byte) error {
	// Get existing list
	existing, err := c.Get(ctx, key)
	if err != nil && err != ports.ErrCacheMiss {
		return err
	}

	var list [][]byte
	if err == nil {
		// Deserialize existing list
		if err := json.Unmarshal(existing, &list); err != nil {
			// If deserialization fails, start with empty list
			list = [][]byte{}
		}
	}

	// Append new values
	list = append(list, values...)

	// Serialize and store
	listBytes, err := json.Marshal(list)
	if err != nil {
		return fmt.Errorf("failed to marshal list: %w", err)
	}

	return c.Set(ctx, key, listBytes, 24*time.Hour)
}

// ListPop removes and returns the last value from a list
func (c *PostgresCacheAdapter) ListPop(ctx context.Context, key string) ([]byte, error) {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return nil, err
	}

	var list [][]byte
	if err := json.Unmarshal(existing, &list); err != nil {
		return nil, fmt.Errorf("failed to unmarshal list: %w", err)
	}

	if len(list) == 0 {
		return nil, ports.ErrCacheMiss
	}

	// Remove last element
	lastValue := list[len(list)-1]
	list = list[:len(list)-1]

	// Update or delete
	if len(list) == 0 {
		if err := c.Delete(ctx, key); err != nil {
			return nil, fmt.Errorf("failed to delete list: %w", err)
		}
	} else {
		listBytes, err := json.Marshal(list)
		if err != nil {
			return nil, fmt.Errorf("failed to marshal list: %w", err)
		}
		if err := c.Set(ctx, key, listBytes, 24*time.Hour); err != nil {
			return nil, fmt.Errorf("failed to set list: %w", err)
		}
	}

	return lastValue, nil
}

// ListRange returns a range of values from a list
func (c *PostgresCacheAdapter) ListRange(ctx context.Context, key string, start, stop int64) ([][]byte, error) {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return nil, err
	}

	var list [][]byte
	if err := json.Unmarshal(existing, &list); err != nil {
		return nil, fmt.Errorf("failed to unmarshal list: %w", err)
	}

	// Handle negative indices
	if start < 0 {
		start = int64(len(list)) + start
	}
	if stop < 0 {
		stop = int64(len(list)) + stop
	}

	// Clamp indices
	if start < 0 {
		start = 0
	}
	if stop >= int64(len(list)) {
		stop = int64(len(list)) - 1
	}

	if start > stop {
		return [][]byte{}, nil
	}

	return list[start : stop+1], nil
}

// ListLength returns the length of a list
func (c *PostgresCacheAdapter) ListLength(ctx context.Context, key string) (int64, error) {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return 0, err
	}

	var list [][]byte
	if err := json.Unmarshal(existing, &list); err != nil {
		return 0, fmt.Errorf("failed to unmarshal list: %w", err)
	}

	return int64(len(list)), nil
}

// SetAdd adds members to a set
func (c *PostgresCacheAdapter) SetAdd(ctx context.Context, key string, members ...[]byte) error {
	existing, err := c.Get(ctx, key)
	if err != nil && err != ports.ErrCacheMiss {
		return err
	}

	set := make(map[string]bool)
	if err == nil {
		// Deserialize existing set
		var list [][]byte
		if err := json.Unmarshal(existing, &list); err == nil {
			for _, member := range list {
				set[string(member)] = true
			}
		}
	}

	// Add new members
	for _, member := range members {
		set[string(member)] = true
	}

	// Convert back to list
	var list [][]byte
	for member := range set {
		list = append(list, []byte(member))
	}

	// Serialize and store
	setBytes, err := json.Marshal(list)
	if err != nil {
		return fmt.Errorf("failed to marshal set: %w", err)
	}

	return c.Set(ctx, key, setBytes, 24*time.Hour)
}

// SetRemove removes members from a set
func (c *PostgresCacheAdapter) SetRemove(ctx context.Context, key string, members ...[]byte) error {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return err
	}

	var list [][]byte
	if err := json.Unmarshal(existing, &list); err != nil {
		return fmt.Errorf("failed to unmarshal set: %w", err)
	}

	// Convert to set for efficient removal
	set := make(map[string]bool)
	for _, member := range list {
		set[string(member)] = true
	}

	// Remove members
	for _, member := range members {
		delete(set, string(member))
	}

	// Convert back to list
	var newList [][]byte
	for member := range set {
		newList = append(newList, []byte(member))
	}

	// Update or delete
	if len(newList) == 0 {
		return c.Delete(ctx, key)
	}

	setBytes, err := json.Marshal(newList)
	if err != nil {
		return fmt.Errorf("failed to marshal set: %w", err)
	}

	return c.Set(ctx, key, setBytes, 24*time.Hour)
}

// SetMembers returns all members of a set
func (c *PostgresCacheAdapter) SetMembers(ctx context.Context, key string) ([][]byte, error) {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return [][]byte{}, err
	}

	var list [][]byte
	if err := json.Unmarshal(existing, &list); err != nil {
		return nil, fmt.Errorf("failed to unmarshal set: %w", err)
	}

	return list, nil
}

// SetIsMember checks if a member exists in a set
func (c *PostgresCacheAdapter) SetIsMember(ctx context.Context, key string, member []byte) (bool, error) {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return false, err
	}

	var list [][]byte
	if err := json.Unmarshal(existing, &list); err != nil {
		return false, fmt.Errorf("failed to unmarshal set: %w", err)
	}

	for _, m := range list {
		if string(m) == string(member) {
			return true, nil
		}
	}

	return false, nil
}

// HashSet sets a field in a hash
func (c *PostgresCacheAdapter) HashSet(ctx context.Context, key, field string, value []byte) error {
	existing, err := c.Get(ctx, key)
	if err != nil && err != ports.ErrCacheMiss {
		return err
	}

	hash := make(map[string][]byte)
	if err == nil {
		// Deserialize existing hash
		if err := json.Unmarshal(existing, &hash); err != nil {
			// If deserialization fails, start with empty hash
			hash = make(map[string][]byte)
		}
	}

	// Set field
	hash[field] = value

	// Serialize and store
	hashBytes, err := json.Marshal(hash)
	if err != nil {
		return fmt.Errorf("failed to marshal hash: %w", err)
	}

	return c.Set(ctx, key, hashBytes, 24*time.Hour)
}

// HashGet gets a field from a hash
func (c *PostgresCacheAdapter) HashGet(ctx context.Context, key, field string) ([]byte, error) {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return nil, err
	}

	var hash map[string][]byte
	if err := json.Unmarshal(existing, &hash); err != nil {
		return nil, fmt.Errorf("failed to unmarshal hash: %w", err)
	}

	value, exists := hash[field]
	if !exists {
		return nil, ports.ErrCacheMiss
	}

	return value, nil
}

// HashGetAll gets all fields from a hash
func (c *PostgresCacheAdapter) HashGetAll(ctx context.Context, key string) (map[string][]byte, error) {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return map[string][]byte{}, err
	}

	var hash map[string][]byte
	if err := json.Unmarshal(existing, &hash); err != nil {
		return nil, fmt.Errorf("failed to unmarshal hash: %w", err)
	}

	return hash, nil
}

// HashDelete deletes fields from a hash
func (c *PostgresCacheAdapter) HashDelete(ctx context.Context, key string, fields ...string) error {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return err
	}

	var hash map[string][]byte
	if err := json.Unmarshal(existing, &hash); err != nil {
		return fmt.Errorf("failed to unmarshal hash: %w", err)
	}

	// Delete fields
	for _, field := range fields {
		delete(hash, field)
	}

	// Update or delete
	if len(hash) == 0 {
		return c.Delete(ctx, key)
	}

	hashBytes, err := json.Marshal(hash)
	if err != nil {
		return fmt.Errorf("failed to marshal hash: %w", err)
	}

	return c.Set(ctx, key, hashBytes, 24*time.Hour)
}

// HashExists checks if a field exists in a hash
func (c *PostgresCacheAdapter) HashExists(ctx context.Context, key, field string) (bool, error) {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return false, err
	}

	var hash map[string][]byte
	if err := json.Unmarshal(existing, &hash); err != nil {
		return false, fmt.Errorf("failed to unmarshal hash: %w", err)
	}

	_, exists := hash[field]
	return exists, nil
}

// HashLength returns the number of fields in a hash
func (c *PostgresCacheAdapter) HashLength(ctx context.Context, key string) (int64, error) {
	existing, err := c.Get(ctx, key)
	if err != nil {
		return 0, err
	}

	var hash map[string][]byte
	if err := json.Unmarshal(existing, &hash); err != nil {
		return 0, fmt.Errorf("failed to unmarshal hash: %w", err)
	}

	return int64(len(hash)), nil
}

// Clear removes all items from cache
func (c *PostgresCacheAdapter) Clear(ctx context.Context) error {
	err := c.db.WithContext(ctx).Exec("DELETE FROM cache_entries").Error
	if err != nil {
		return fmt.Errorf("failed to clear cache: %w", err)
	}
	return nil
}

// GetStats returns cache statistics
func (c *PostgresCacheAdapter) GetStats(ctx context.Context) (map[string]interface{}, error) {
	var stats struct {
		TotalEntries   int64 `json:"total_entries"`
		ExpiredEntries int64 `json:"expired_entries"`
		ActiveEntries  int64 `json:"active_entries"`
		TotalAccess    int64 `json:"total_access"`
	}

	// Get total entries
	c.db.WithContext(ctx).Raw("SELECT COUNT(*) FROM cache_entries").Scan(&stats.TotalEntries)

	// Get expired entries
	c.db.WithContext(ctx).Raw("SELECT COUNT(*) FROM cache_entries WHERE expires_at <= CURRENT_TIMESTAMP").Scan(&stats.ExpiredEntries)

	// Get active entries
	c.db.WithContext(ctx).Raw("SELECT COUNT(*) FROM cache_entries WHERE expires_at > CURRENT_TIMESTAMP").Scan(&stats.ActiveEntries)

	// Get total access count
	c.db.WithContext(ctx).Raw("SELECT COALESCE(SUM(access_count), 0) FROM cache_entries").Scan(&stats.TotalAccess)

	return map[string]interface{}{
		"total_entries":   stats.TotalEntries,
		"expired_entries": stats.ExpiredEntries,
		"active_entries":  stats.ActiveEntries,
		"total_access":    stats.TotalAccess,
	}, nil
}

// Close closes the cache
func (c *PostgresCacheAdapter) Close() error {
	// PostgreSQL cache doesn't need explicit closing
	return nil
}

// Ping pings the cache
func (c *PostgresCacheAdapter) Ping(ctx context.Context) error {
	var result int
	err := c.db.WithContext(ctx).Raw("SELECT 1").Scan(&result).Error
	if err != nil {
		return fmt.Errorf("cache ping failed: %w", err)
	}
	return nil
}

// startCleanupRoutine starts the background cleanup routine
func (c *PostgresCacheAdapter) startCleanupRoutine() {
	ticker := time.NewTicker(5 * time.Minute)
	defer ticker.Stop()

	for range ticker.C {
		ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)

		// Check if database connection is still valid
		if c.db == nil {
			cancel()
			continue
		}

		// Test database connection before proceeding
		sqlDB, err := c.db.DB()
		if err != nil || sqlDB == nil {
			cancel()
			continue
		}

		if err := sqlDB.Ping(); err != nil {
			// Database connection is closed, skip this iteration
			cancel()
			continue
		}

		// Clean up expired entries
		result := c.db.WithContext(ctx).Exec("DELETE FROM cache_entries WHERE expires_at <= CURRENT_TIMESTAMP")
		if result.Error != nil {
			// Only log if it's not a connection issue
			if !isConnectionError(result.Error) {
				fmt.Printf("Warning: failed to cleanup expired cache entries: %v\n", result.Error)
			}
		} else if result.RowsAffected > 0 {
			fmt.Printf("Cleaned up %d expired cache entries\n", result.RowsAffected)
		}

		cancel()
	}
}

// isConnectionError checks if the error is related to database connection issues
func isConnectionError(err error) bool {
	if err == nil {
		return false
	}
	errStr := err.Error()
	return strings.Contains(errStr, "database is closed") ||
		strings.Contains(errStr, "connection refused") ||
		strings.Contains(errStr, "broken pipe") ||
		strings.Contains(errStr, "connection reset") ||
		strings.Contains(errStr, "context canceled")
}

// Compile-time interface verification
var _ ports.CachePort = (*PostgresCacheAdapter)(nil)
