package ports

import (
	"context"
	"errors"
	"time"
)

// Cache errors
var (
	ErrCacheMiss = errors.New("cache miss")
)

// CachePort defines the interface for caching operations
// This abstracts the cache implementation from domain services
type CachePort interface {
	// Basic operations
	Get(ctx context.Context, key string) ([]byte, error)
	Set(ctx context.Context, key string, value []byte, ttl time.Duration) error
	Delete(ctx context.Context, key string) error
	Exists(ctx context.Context, key string) (bool, error)

	// Batch operations
	GetMultiple(ctx context.Context, keys []string) (map[string][]byte, error)
	SetMultiple(ctx context.Context, items map[string][]byte, ttl time.Duration) error
	DeleteMultiple(ctx context.Context, keys []string) error

	// Pattern operations
	Keys(ctx context.Context, pattern string) ([]string, error)
	DeletePattern(ctx context.Context, pattern string) error

	// TTL operations
	TTL(ctx context.Context, key string) (time.Duration, error)
	Expire(ctx context.Context, key string, ttl time.Duration) error

	// Atomic operations
	Increment(ctx context.Context, key string) (int64, error)
	Decrement(ctx context.Context, key string) (int64, error)
	IncrementBy(ctx context.Context, key string, delta int64) (int64, error)

	// List operations
	ListPush(ctx context.Context, key string, values ...[]byte) error
	ListPop(ctx context.Context, key string) ([]byte, error)
	ListRange(ctx context.Context, key string, start, stop int64) ([][]byte, error)
	ListLength(ctx context.Context, key string) (int64, error)

	// Set operations
	SetAdd(ctx context.Context, key string, members ...[]byte) error
	SetRemove(ctx context.Context, key string, members ...[]byte) error
	SetMembers(ctx context.Context, key string) ([][]byte, error)
	SetIsMember(ctx context.Context, key string, member []byte) (bool, error)

	// Hash operations
	HashSet(ctx context.Context, key, field string, value []byte) error
	HashGet(ctx context.Context, key, field string) ([]byte, error)
	HashGetAll(ctx context.Context, key string) (map[string][]byte, error)
	HashDelete(ctx context.Context, key string, fields ...string) error

	// Connection management
	Ping(ctx context.Context) error
	Close() error
}

// CacheKeyGenerator defines the interface for generating cache keys
type CacheKeyGenerator interface {
	// Resource keys
	ComputeResourceKey(id string) string
	StorageResourceKey(id string) string
	CredentialKey(id string) string

	// Experiment keys
	ExperimentKey(id string) string
	ExperimentTasksKey(experimentID string) string
	ExperimentStatusKey(experimentID string) string

	// Task keys
	TaskKey(id string) string
	TaskStatusKey(id string) string
	TaskQueueKey(computeResourceID string) string

	// Worker keys
	WorkerKey(id string) string
	WorkerStatusKey(id string) string
	WorkerMetricsKey(id string) string
	IdleWorkersKey(computeResourceID string) string

	// Data cache keys
	DataCacheKey(filePath, computeResourceID string) string
	DataCachePatternKey(computeResourceID string) string

	// User keys
	UserKey(id string) string
	UserByUsernameKey(username string) string
	UserByEmailKey(email string) string

	// Project keys
	ProjectKey(id string) string
	ProjectExperimentsKey(projectID string) string

	// Session keys
	SessionKey(sessionID string) string
	UserSessionsKey(userID string) string

	// Rate limiting keys
	RateLimitKey(userID, endpoint string) string
	RateLimitWindowKey(userID, endpoint string, window time.Time) string

	// Metrics keys
	MetricsKey(metricName string) string
	MetricsCounterKey(metricName string) string
	MetricsGaugeKey(metricName string) string
	MetricsHistogramKey(metricName string) string
}

// CacheConfig represents cache configuration
type CacheConfig struct {
	DefaultTTL      time.Duration `json:"defaultTTL"`
	MaxTTL          time.Duration `json:"maxTTL"`
	CleanupInterval time.Duration `json:"cleanupInterval"`
	MaxMemory       int64         `json:"maxMemory"`
	MaxKeys         int64         `json:"maxKeys"`
}

// CacheStatsInfo represents cache statistics
type CacheStatsInfo struct {
	Hits       int64         `json:"hits"`
	Misses     int64         `json:"misses"`
	Keys       int64         `json:"keys"`
	Memory     int64         `json:"memory"`
	Uptime     time.Duration `json:"uptime"`
	LastUpdate time.Time     `json:"lastUpdate"`
}
