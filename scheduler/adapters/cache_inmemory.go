package adapters

import (
	"bytes"
	"context"
	"fmt"
	"sync"
	"time"

	ports "github.com/apache/airavata/scheduler/core/port"
)

// CacheItem represents a cached item with expiration
type CacheItem struct {
	Value     []byte
	ExpiresAt time.Time
}

// InMemoryCacheAdapter implements ports.CachePort using in-memory storage
type InMemoryCacheAdapter struct {
	items map[string]*CacheItem
	mu    sync.RWMutex
}

// NewInMemoryCacheAdapter creates a new in-memory cache adapter
func NewInMemoryCacheAdapter() *InMemoryCacheAdapter {
	cache := &InMemoryCacheAdapter{
		items: make(map[string]*CacheItem),
	}

	// Start cleanup goroutine
	go cache.cleanupExpired()

	return cache
}

// Get retrieves a value from cache
func (c *InMemoryCacheAdapter) Get(ctx context.Context, key string) ([]byte, error) {
	c.mu.RLock()
	item, exists := c.items[key]
	c.mu.RUnlock()

	if !exists {
		return nil, ports.ErrCacheMiss
	}

	// Check if expired
	if time.Now().After(item.ExpiresAt) {
		c.mu.Lock()
		delete(c.items, key)
		c.mu.Unlock()
		return nil, ports.ErrCacheMiss
	}

	return item.Value, nil
}

// Set stores a value in cache
func (c *InMemoryCacheAdapter) Set(ctx context.Context, key string, value []byte, ttl time.Duration) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	c.items[key] = &CacheItem{
		Value:     value,
		ExpiresAt: time.Now().Add(ttl),
	}

	return nil
}

// Delete removes a value from cache
func (c *InMemoryCacheAdapter) Delete(ctx context.Context, key string) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	delete(c.items, key)
	return nil
}

// Exists checks if a key exists in cache
func (c *InMemoryCacheAdapter) Exists(ctx context.Context, key string) (bool, error) {
	c.mu.RLock()
	item, exists := c.items[key]
	c.mu.RUnlock()

	if !exists {
		return false, nil
	}

	// Check if expired
	if time.Now().After(item.ExpiresAt) {
		c.mu.Lock()
		delete(c.items, key)
		c.mu.Unlock()
		return false, nil
	}

	return true, nil
}

// GetMultiple retrieves multiple values from cache
func (c *InMemoryCacheAdapter) GetMultiple(ctx context.Context, keys []string) (map[string][]byte, error) {
	result := make(map[string][]byte)

	c.mu.RLock()
	for _, key := range keys {
		if item, exists := c.items[key]; exists {
			if time.Now().Before(item.ExpiresAt) {
				result[key] = item.Value
			}
		}
	}
	c.mu.RUnlock()

	return result, nil
}

// SetMultiple stores multiple values in cache
func (c *InMemoryCacheAdapter) SetMultiple(ctx context.Context, items map[string][]byte, ttl time.Duration) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	expiresAt := time.Now().Add(ttl)
	for key, value := range items {
		c.items[key] = &CacheItem{
			Value:     value,
			ExpiresAt: expiresAt,
		}
	}

	return nil
}

// DeleteMultiple removes multiple values from cache
func (c *InMemoryCacheAdapter) DeleteMultiple(ctx context.Context, keys []string) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	for _, key := range keys {
		delete(c.items, key)
	}

	return nil
}

// Keys returns all keys matching a pattern
func (c *InMemoryCacheAdapter) Keys(ctx context.Context, pattern string) ([]string, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	var keys []string
	for key := range c.items {
		if matchPattern(key, pattern) {
			keys = append(keys, key)
		}
	}

	return keys, nil
}

// DeletePattern removes all keys matching a pattern
func (c *InMemoryCacheAdapter) DeletePattern(ctx context.Context, pattern string) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	for key := range c.items {
		if matchPattern(key, pattern) {
			delete(c.items, key)
		}
	}

	return nil
}

// TTL returns the time to live for a key
func (c *InMemoryCacheAdapter) TTL(ctx context.Context, key string) (time.Duration, error) {
	c.mu.RLock()
	item, exists := c.items[key]
	c.mu.RUnlock()

	if !exists {
		return 0, ports.ErrCacheMiss
	}

	ttl := time.Until(item.ExpiresAt)
	if ttl <= 0 {
		return 0, ports.ErrCacheMiss
	}

	return ttl, nil
}

// Expire sets expiration for a key
func (c *InMemoryCacheAdapter) Expire(ctx context.Context, key string, ttl time.Duration) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	item, exists := c.items[key]
	if !exists {
		return ports.ErrCacheMiss
	}

	item.ExpiresAt = time.Now().Add(ttl)
	return nil
}

// Increment increments a numeric value
func (c *InMemoryCacheAdapter) Increment(ctx context.Context, key string) (int64, error) {
	return c.IncrementBy(ctx, key, 1)
}

// Decrement decrements a numeric value
func (c *InMemoryCacheAdapter) Decrement(ctx context.Context, key string) (int64, error) {
	return c.IncrementBy(ctx, key, -1)
}

// IncrementBy increments a numeric value by delta
func (c *InMemoryCacheAdapter) IncrementBy(ctx context.Context, key string, delta int64) (int64, error) {
	c.mu.Lock()
	defer c.mu.Unlock()

	item, exists := c.items[key]
	if !exists {
		// Create new item with value 0
		item = &CacheItem{
			Value:     []byte("0"),
			ExpiresAt: time.Now().Add(24 * time.Hour), // Default TTL
		}
		c.items[key] = item
	}

	// Parse current value
	currentValue := int64(0)
	if len(item.Value) > 0 {
		// Simple parsing - in production, use proper number parsing
		for _, b := range item.Value {
			if b >= '0' && b <= '9' {
				currentValue = currentValue*10 + int64(b-'0')
			}
		}
	}

	newValue := currentValue + delta
	item.Value = []byte(fmt.Sprintf("%d", newValue))

	return newValue, nil
}

// ListPush adds values to a list
func (c *InMemoryCacheAdapter) ListPush(ctx context.Context, key string, values ...[]byte) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	item, exists := c.items[key]
	if !exists {
		item = &CacheItem{
			Value:     []byte{},
			ExpiresAt: time.Now().Add(24 * time.Hour),
		}
		c.items[key] = item
	}

	// Simple list implementation - append values with separator
	for _, value := range values {
		if len(item.Value) > 0 {
			item.Value = append(item.Value, '|')
		}
		item.Value = append(item.Value, value...)
	}

	return nil
}

// ListPop removes and returns the last value from a list
func (c *InMemoryCacheAdapter) ListPop(ctx context.Context, key string) ([]byte, error) {
	c.mu.Lock()
	defer c.mu.Unlock()

	item, exists := c.items[key]
	if !exists || len(item.Value) == 0 {
		return nil, ports.ErrCacheMiss
	}

	// Find last separator
	lastSep := -1
	for i := len(item.Value) - 1; i >= 0; i-- {
		if item.Value[i] == '|' {
			lastSep = i
			break
		}
	}

	var result []byte
	if lastSep == -1 {
		// Single item
		result = item.Value
		item.Value = []byte{}
	} else {
		// Multiple items
		result = item.Value[lastSep+1:]
		item.Value = item.Value[:lastSep]
	}

	return result, nil
}

// ListRange returns a range of values from a list
func (c *InMemoryCacheAdapter) ListRange(ctx context.Context, key string, start, stop int64) ([][]byte, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	item, exists := c.items[key]
	if !exists {
		return nil, ports.ErrCacheMiss
	}

	// Simple implementation - split by separator
	values := bytes.Split(item.Value, []byte{'|'})
	if len(values) == 0 {
		return [][]byte{}, nil
	}

	// Apply range
	if start < 0 {
		start = int64(len(values)) + start
	}
	if stop < 0 {
		stop = int64(len(values)) + stop
	}

	if start < 0 {
		start = 0
	}
	if stop >= int64(len(values)) {
		stop = int64(len(values)) - 1
	}

	if start > stop {
		return [][]byte{}, nil
	}

	return values[start : stop+1], nil
}

// ListLength returns the length of a list
func (c *InMemoryCacheAdapter) ListLength(ctx context.Context, key string) (int64, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	item, exists := c.items[key]
	if !exists {
		return 0, nil
	}

	// Count separators + 1
	count := int64(1)
	for _, b := range item.Value {
		if b == '|' {
			count++
		}
	}

	return count, nil
}

// SetAdd adds members to a set
func (c *InMemoryCacheAdapter) SetAdd(ctx context.Context, key string, members ...[]byte) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	item, exists := c.items[key]
	if !exists {
		item = &CacheItem{
			Value:     []byte{},
			ExpiresAt: time.Now().Add(24 * time.Hour),
		}
		c.items[key] = item
	}

	// Simple set implementation - store as map
	setData := make(map[string]bool)
	if len(item.Value) > 0 {
		// Parse existing set
		existing := bytes.Split(item.Value, []byte{'|'})
		for _, member := range existing {
			setData[string(member)] = true
		}
	}

	// Add new members
	for _, member := range members {
		setData[string(member)] = true
	}

	// Serialize back
	var newValue []byte
	first := true
	for member := range setData {
		if !first {
			newValue = append(newValue, '|')
		}
		newValue = append(newValue, []byte(member)...)
		first = false
	}

	item.Value = newValue
	return nil
}

// SetRemove removes members from a set
func (c *InMemoryCacheAdapter) SetRemove(ctx context.Context, key string, members ...[]byte) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	item, exists := c.items[key]
	if !exists {
		return nil
	}

	// Parse existing set
	setData := make(map[string]bool)
	if len(item.Value) > 0 {
		existing := bytes.Split(item.Value, []byte{'|'})
		for _, member := range existing {
			setData[string(member)] = true
		}
	}

	// Remove members
	for _, member := range members {
		delete(setData, string(member))
	}

	// Serialize back
	var newValue []byte
	first := true
	for member := range setData {
		if !first {
			newValue = append(newValue, '|')
		}
		newValue = append(newValue, []byte(member)...)
		first = false
	}

	item.Value = newValue
	return nil
}

// SetMembers returns all members of a set
func (c *InMemoryCacheAdapter) SetMembers(ctx context.Context, key string) ([][]byte, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	item, exists := c.items[key]
	if !exists {
		return [][]byte{}, nil
	}

	if len(item.Value) == 0 {
		return [][]byte{}, nil
	}

	return bytes.Split(item.Value, []byte{'|'}), nil
}

// SetIsMember checks if a member exists in a set
func (c *InMemoryCacheAdapter) SetIsMember(ctx context.Context, key string, member []byte) (bool, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	item, exists := c.items[key]
	if !exists {
		return false, nil
	}

	members := bytes.Split(item.Value, []byte{'|'})
	for _, m := range members {
		if bytes.Equal(m, member) {
			return true, nil
		}
	}

	return false, nil
}

// HashSet sets a field in a hash
func (c *InMemoryCacheAdapter) HashSet(ctx context.Context, key, field string, value []byte) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	item, exists := c.items[key]
	if !exists {
		item = &CacheItem{
			Value:     []byte{},
			ExpiresAt: time.Now().Add(24 * time.Hour),
		}
		c.items[key] = item
	}

	// Simple hash implementation - store as key:value|key:value
	hashData := make(map[string][]byte)
	if len(item.Value) > 0 {
		// Parse existing hash
		pairs := bytes.Split(item.Value, []byte{'|'})
		for _, pair := range pairs {
			parts := bytes.SplitN(pair, []byte{':'}, 2)
			if len(parts) == 2 {
				hashData[string(parts[0])] = parts[1]
			}
		}
	}

	// Set field
	hashData[field] = value

	// Serialize back
	var newValue []byte
	first := true
	for k, v := range hashData {
		if !first {
			newValue = append(newValue, '|')
		}
		newValue = append(newValue, []byte(k)...)
		newValue = append(newValue, ':')
		newValue = append(newValue, v...)
		first = false
	}

	item.Value = newValue
	return nil
}

// HashGet gets a field from a hash
func (c *InMemoryCacheAdapter) HashGet(ctx context.Context, key, field string) ([]byte, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	item, exists := c.items[key]
	if !exists {
		return nil, ports.ErrCacheMiss
	}

	// Parse hash
	pairs := bytes.Split(item.Value, []byte{'|'})
	for _, pair := range pairs {
		parts := bytes.SplitN(pair, []byte{':'}, 2)
		if len(parts) == 2 && string(parts[0]) == field {
			return parts[1], nil
		}
	}

	return nil, ports.ErrCacheMiss
}

// HashGetAll gets all fields from a hash
func (c *InMemoryCacheAdapter) HashGetAll(ctx context.Context, key string) (map[string][]byte, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	item, exists := c.items[key]
	if !exists {
		return map[string][]byte{}, nil
	}

	hashData := make(map[string][]byte)
	if len(item.Value) > 0 {
		pairs := bytes.Split(item.Value, []byte{'|'})
		for _, pair := range pairs {
			parts := bytes.SplitN(pair, []byte{':'}, 2)
			if len(parts) == 2 {
				hashData[string(parts[0])] = parts[1]
			}
		}
	}

	return hashData, nil
}

// HashDelete deletes fields from a hash
func (c *InMemoryCacheAdapter) HashDelete(ctx context.Context, key string, fields ...string) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	item, exists := c.items[key]
	if !exists {
		return nil
	}

	// Parse existing hash
	hashData := make(map[string][]byte)
	if len(item.Value) > 0 {
		pairs := bytes.Split(item.Value, []byte{'|'})
		for _, pair := range pairs {
			parts := bytes.SplitN(pair, []byte{':'}, 2)
			if len(parts) == 2 {
				hashData[string(parts[0])] = parts[1]
			}
		}
	}

	// Delete fields
	for _, field := range fields {
		delete(hashData, field)
	}

	// Serialize back
	var newValue []byte
	first := true
	for k, v := range hashData {
		if !first {
			newValue = append(newValue, '|')
		}
		newValue = append(newValue, []byte(k)...)
		newValue = append(newValue, ':')
		newValue = append(newValue, v...)
		first = false
	}

	item.Value = newValue
	return nil
}

// HashExists checks if a field exists in a hash
func (c *InMemoryCacheAdapter) HashExists(ctx context.Context, key, field string) (bool, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	item, exists := c.items[key]
	if !exists {
		return false, nil
	}

	pairs := bytes.Split(item.Value, []byte{'|'})
	for _, pair := range pairs {
		parts := bytes.SplitN(pair, []byte{':'}, 2)
		if len(parts) == 2 && string(parts[0]) == field {
			return true, nil
		}
	}

	return false, nil
}

// HashLength returns the number of fields in a hash
func (c *InMemoryCacheAdapter) HashLength(ctx context.Context, key string) (int64, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	item, exists := c.items[key]
	if !exists {
		return 0, nil
	}

	if len(item.Value) == 0 {
		return 0, nil
	}

	pairs := bytes.Split(item.Value, []byte{'|'})
	return int64(len(pairs)), nil
}

// Clear removes all items from cache
func (c *InMemoryCacheAdapter) Clear(ctx context.Context) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	c.items = make(map[string]*CacheItem)
	return nil
}

// GetStats returns cache statistics
func (c *InMemoryCacheAdapter) GetStats(ctx context.Context) (map[string]interface{}, error) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	now := time.Now()
	totalItems := len(c.items)
	expiredItems := 0

	for _, item := range c.items {
		if now.After(item.ExpiresAt) {
			expiredItems++
		}
	}

	return map[string]interface{}{
		"total_items":   totalItems,
		"expired_items": expiredItems,
		"active_items":  totalItems - expiredItems,
	}, nil
}

// Close closes the cache
func (c *InMemoryCacheAdapter) Close() error {
	c.mu.Lock()
	defer c.mu.Unlock()

	c.items = make(map[string]*CacheItem)
	return nil
}

// cleanupExpired removes expired items periodically
func (c *InMemoryCacheAdapter) cleanupExpired() {
	ticker := time.NewTicker(5 * time.Minute)
	defer ticker.Stop()

	for range ticker.C {
		c.mu.Lock()
		now := time.Now()
		for key, item := range c.items {
			if now.After(item.ExpiresAt) {
				delete(c.items, key)
			}
		}
		c.mu.Unlock()
	}
}

// matchPattern matches a key against a simple pattern
func matchPattern(key, pattern string) bool {
	if pattern == "*" {
		return true
	}
	if pattern == key {
		return true
	}
	// Simple prefix matching
	if len(pattern) > 0 && pattern[len(pattern)-1] == '*' {
		prefix := pattern[:len(pattern)-1]
		return len(key) >= len(prefix) && key[:len(prefix)] == prefix
	}
	return false
}

// Ping pings the cache
func (c *InMemoryCacheAdapter) Ping(ctx context.Context) error {
	return nil
}

// Compile-time interface verification
var _ ports.CachePort = (*InMemoryCacheAdapter)(nil)
