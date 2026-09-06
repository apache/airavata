package services

import (
	"context"
	"fmt"
	"sync"
	"time"
)

// CacheService provides caching functionality
type CacheService struct {
	// In-memory cache
	memoryCache map[string]*CacheEntry
	mutex       sync.RWMutex

	// Configuration
	config *CacheConfig

	// Redis client for distributed caching (optional)
	// redisClient interface{} // Commented out until RedisClient is defined

	// Statistics
	stats *CacheStats
}

// CacheConfig represents cache configuration
type CacheConfig struct {
	// Default TTL for cached items
	DefaultTTL time.Duration `json:"defaultTTL"`

	// Maximum number of items in memory cache
	MaxItems int `json:"maxItems"`

	// Cleanup interval
	CleanupInterval time.Duration `json:"cleanupInterval"`

	// Enable distributed caching
	EnableDistributed bool `json:"enableDistributed"`

	// Cache prefixes for different types
	Prefixes map[string]string `json:"prefixes"`
}

// GetDefaultCacheConfig returns default cache configuration
func GetDefaultCacheConfig() *CacheConfig {
	return &CacheConfig{
		DefaultTTL:        5 * time.Minute,
		MaxItems:          10000,
		CleanupInterval:   1 * time.Minute,
		EnableDistributed: false,
		Prefixes: map[string]string{
			"experiment": "exp:",
			"task":       "task:",
			"user":       "user:",
			"project":    "project:",
			"worker":     "worker:",
			"resource":   "resource:",
		},
	}
}

// CacheEntry represents a cached item
type CacheEntry struct {
	Value        interface{} `json:"value"`
	ExpiresAt    time.Time   `json:"expiresAt"`
	CreatedAt    time.Time   `json:"createdAt"`
	AccessCount  int         `json:"accessCount"`
	LastAccessed time.Time   `json:"lastAccessed"`
}

// CacheStats represents cache statistics
type CacheStats struct {
	Hits       int64 `json:"hits"`
	Misses     int64 `json:"misses"`
	Sets       int64 `json:"sets"`
	Deletes    int64 `json:"deletes"`
	Evictions  int64 `json:"evictions"`
	TotalItems int64 `json:"totalItems"`
}

// NewCacheService creates a new cache service
func NewCacheService(config *CacheConfig) *CacheService {
	if config == nil {
		config = GetDefaultCacheConfig()
	}

	cs := &CacheService{
		memoryCache: make(map[string]*CacheEntry),
		config:      config,
		// redisClient: redisClient, // Commented out
		stats: &CacheStats{},
	}

	// Start cleanup routine
	go cs.startCleanupRoutine()

	return cs
}

// Get retrieves a value from the cache
func (cs *CacheService) Get(ctx context.Context, key string) (interface{}, error) {
	// Use distributed cache if enabled and Redis is available
	if cs.config.EnableDistributed {
		// Redis client implementation for distributed caching
		// if cs.redisClient != nil {
		return cs.getFromDistributedCache(ctx, key)
	}

	// Use in-memory cache
	return cs.getFromMemoryCache(ctx, key)
}

// getFromMemoryCache retrieves a value from in-memory cache
func (cs *CacheService) getFromMemoryCache(ctx context.Context, key string) (interface{}, error) {
	cs.mutex.RLock()
	defer cs.mutex.RUnlock()

	entry, exists := cs.memoryCache[key]
	if !exists {
		cs.stats.Misses++
		return nil, fmt.Errorf("key not found: %s", key)
	}

	// Check if entry has expired
	if time.Now().After(entry.ExpiresAt) {
		cs.stats.Misses++
		return nil, fmt.Errorf("key expired: %s", key)
	}

	// Update access statistics
	entry.AccessCount++
	entry.LastAccessed = time.Now()
	cs.stats.Hits++

	return entry.Value, nil
}

// getFromDistributedCache retrieves a value from distributed cache
func (cs *CacheService) getFromDistributedCache(ctx context.Context, key string) (interface{}, error) {
	// This would implement distributed caching using Redis
	// For now, fall back to in-memory cache
	return cs.getFromMemoryCache(ctx, key)
}

// Set stores a value in the cache
func (cs *CacheService) Set(ctx context.Context, key string, value interface{}, ttl time.Duration) error {
	// Use distributed cache if enabled and Redis is available
	if cs.config.EnableDistributed {
		// Redis client implementation for distributed caching
		// if cs.redisClient != nil {
		return cs.setInDistributedCache(ctx, key, value, ttl)
	}

	// Use in-memory cache
	return cs.setInMemoryCache(ctx, key, value, ttl)
}

// setInMemoryCache stores a value in in-memory cache
func (cs *CacheService) setInMemoryCache(ctx context.Context, key string, value interface{}, ttl time.Duration) error {
	cs.mutex.Lock()
	defer cs.mutex.Unlock()

	// Use default TTL if not specified
	if ttl == 0 {
		ttl = cs.config.DefaultTTL
	}

	// Check if we need to evict items
	if len(cs.memoryCache) >= cs.config.MaxItems {
		cs.evictOldestItems()
	}

	// Create cache entry
	entry := &CacheEntry{
		Value:        value,
		ExpiresAt:    time.Now().Add(ttl),
		CreatedAt:    time.Now(),
		AccessCount:  0,
		LastAccessed: time.Now(),
	}

	cs.memoryCache[key] = entry
	cs.stats.Sets++

	return nil
}

// setInDistributedCache stores a value in distributed cache
func (cs *CacheService) setInDistributedCache(ctx context.Context, key string, value interface{}, ttl time.Duration) error {
	// This would implement distributed caching using Redis
	// For now, fall back to in-memory cache
	return cs.setInMemoryCache(ctx, key, value, ttl)
}

// Delete removes a value from the cache
func (cs *CacheService) Delete(ctx context.Context, key string) error {
	// Use distributed cache if enabled and Redis is available
	if cs.config.EnableDistributed {
		// Redis client implementation for distributed caching
		// if cs.redisClient != nil {
		return cs.deleteFromDistributedCache(ctx, key)
	}

	// Use in-memory cache
	return cs.deleteFromMemoryCache(ctx, key)
}

// deleteFromMemoryCache removes a value from in-memory cache
func (cs *CacheService) deleteFromMemoryCache(ctx context.Context, key string) error {
	cs.mutex.Lock()
	defer cs.mutex.Unlock()

	if _, exists := cs.memoryCache[key]; exists {
		delete(cs.memoryCache, key)
		cs.stats.Deletes++
	}

	return nil
}

// deleteFromDistributedCache removes a value from distributed cache
func (cs *CacheService) deleteFromDistributedCache(ctx context.Context, key string) error {
	// This would implement distributed cache deletion using Redis
	// For now, fall back to in-memory cache
	return cs.deleteFromMemoryCache(ctx, key)
}

// GetOrSet retrieves a value from cache or sets it if not found
func (cs *CacheService) GetOrSet(ctx context.Context, key string, setter func() (interface{}, error), ttl time.Duration) (interface{}, error) {
	// Try to get from cache first
	value, err := cs.Get(ctx, key)
	if err == nil {
		return value, nil
	}

	// Value not in cache, call setter function
	value, err = setter()
	if err != nil {
		return nil, err
	}

	// Store in cache
	if err := cs.Set(ctx, key, value, ttl); err != nil {
		// Log error but don't fail the operation
		fmt.Printf("Failed to cache value for key %s: %v\n", key, err)
	}

	return value, nil
}

// GetWithPrefix retrieves a value using a prefix
func (cs *CacheService) GetWithPrefix(ctx context.Context, prefix, key string) (interface{}, error) {
	fullKey := cs.getFullKey(prefix, key)
	return cs.Get(ctx, fullKey)
}

// SetWithPrefix stores a value using a prefix
func (cs *CacheService) SetWithPrefix(ctx context.Context, prefix, key string, value interface{}, ttl time.Duration) error {
	fullKey := cs.getFullKey(prefix, key)
	return cs.Set(ctx, fullKey, value, ttl)
}

// DeleteWithPrefix removes a value using a prefix
func (cs *CacheService) DeleteWithPrefix(ctx context.Context, prefix, key string) error {
	fullKey := cs.getFullKey(prefix, key)
	return cs.Delete(ctx, fullKey)
}

// getFullKey constructs a full cache key with prefix
func (cs *CacheService) getFullKey(prefix, key string) string {
	if prefixKey, exists := cs.config.Prefixes[prefix]; exists {
		return prefixKey + key
	}
	return prefix + ":" + key
}

// evictOldestItems evicts the oldest items from the cache
func (cs *CacheService) evictOldestItems() {
	// Simple LRU eviction - remove items with oldest last access time
	var oldestKey string
	var oldestTime time.Time

	for key, entry := range cs.memoryCache {
		if oldestKey == "" || entry.LastAccessed.Before(oldestTime) {
			oldestKey = key
			oldestTime = entry.LastAccessed
		}
	}

	if oldestKey != "" {
		delete(cs.memoryCache, oldestKey)
		cs.stats.Evictions++
	}
}

// startCleanupRoutine starts the cleanup routine for expired entries
func (cs *CacheService) startCleanupRoutine() {
	ticker := time.NewTicker(cs.config.CleanupInterval)
	defer ticker.Stop()

	for range ticker.C {
		cs.cleanupExpiredEntries()
	}
}

// cleanupExpiredEntries removes expired entries from the cache
func (cs *CacheService) cleanupExpiredEntries() {
	cs.mutex.Lock()
	defer cs.mutex.Unlock()

	now := time.Now()
	for key, entry := range cs.memoryCache {
		if now.After(entry.ExpiresAt) {
			delete(cs.memoryCache, key)
			cs.stats.Evictions++
		}
	}
}

// GetStats returns cache statistics
func (cs *CacheService) GetStats() *CacheStats {
	cs.mutex.RLock()
	defer cs.mutex.RUnlock()

	stats := *cs.stats
	stats.TotalItems = int64(len(cs.memoryCache))

	return &stats
}

// Clear clears all cache entries
func (cs *CacheService) Clear(ctx context.Context) error {
	cs.mutex.Lock()
	defer cs.mutex.Unlock()

	cs.memoryCache = make(map[string]*CacheEntry)
	cs.stats = &CacheStats{}

	return nil
}

// GetKeys returns all cache keys (for debugging)
func (cs *CacheService) GetKeys() []string {
	cs.mutex.RLock()
	defer cs.mutex.RUnlock()

	keys := make([]string, 0, len(cs.memoryCache))
	for key := range cs.memoryCache {
		keys = append(keys, key)
	}

	return keys
}

// GetEntryInfo returns information about a cache entry
func (cs *CacheService) GetEntryInfo(key string) (map[string]interface{}, error) {
	cs.mutex.RLock()
	defer cs.mutex.RUnlock()

	entry, exists := cs.memoryCache[key]
	if !exists {
		return nil, fmt.Errorf("key not found: %s", key)
	}

	return map[string]interface{}{
		"key":          key,
		"createdAt":    entry.CreatedAt,
		"expiresAt":    entry.ExpiresAt,
		"accessCount":  entry.AccessCount,
		"lastAccessed": entry.LastAccessed,
		"isExpired":    time.Now().After(entry.ExpiresAt),
	}, nil
}
