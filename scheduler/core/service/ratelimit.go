package services

import (
	"context"
	"sync"
	"time"
)

// RateLimiter provides rate limiting functionality
type RateLimiter struct {
	// In-memory rate limiter (for single instance)
	userLimits map[string]*UserLimit
	ipLimits   map[string]*IPLimit
	mutex      sync.RWMutex

	// Configuration
	config *RateLimitConfig

	// Redis client for distributed rate limiting (optional)
	// redisClient interface{} // Commented out until RedisClient is defined
}

// RateLimitConfig represents rate limiting configuration
type RateLimitConfig struct {
	// Per-user limits
	UserRequestsPerMinute int `json:"userRequestsPerMinute"`
	UserBurstSize         int `json:"userBurstSize"`

	// Per-IP limits
	IPRequestsPerMinute int `json:"ipRequestsPerMinute"`
	IPBurstSize         int `json:"ipBurstSize"`

	// Global limits
	GlobalRequestsPerMinute int `json:"globalRequestsPerMinute"`
	GlobalBurstSize         int `json:"globalBurstSize"`

	// Cleanup interval
	CleanupInterval time.Duration `json:"cleanupInterval"`

	// Enable distributed rate limiting
	EnableDistributed bool `json:"enableDistributed"`
}

// GetDefaultRateLimitConfig returns default rate limiting configuration
func GetDefaultRateLimitConfig() *RateLimitConfig {
	return &RateLimitConfig{
		UserRequestsPerMinute:   100,
		UserBurstSize:           20,
		IPRequestsPerMinute:     200,
		IPBurstSize:             50,
		GlobalRequestsPerMinute: 1000,
		GlobalBurstSize:         200,
		CleanupInterval:         5 * time.Minute,
		EnableDistributed:       false,
	}
}

// UserLimit represents rate limiting for a user
type UserLimit struct {
	Requests   int       `json:"requests"`
	LastReset  time.Time `json:"lastReset"`
	BurstCount int       `json:"burstCount"`
	LastBurst  time.Time `json:"lastBurst"`
}

// IPLimit represents rate limiting for an IP address
type IPLimit struct {
	Requests   int       `json:"requests"`
	LastReset  time.Time `json:"lastReset"`
	BurstCount int       `json:"burstCount"`
	LastBurst  time.Time `json:"lastBurst"`
}

// RateLimitResult represents the result of a rate limit check
type RateLimitResult struct {
	Allowed    bool          `json:"allowed"`
	Remaining  int           `json:"remaining"`
	ResetTime  time.Time     `json:"resetTime"`
	RetryAfter time.Duration `json:"retryAfter,omitempty"`
	LimitType  string        `json:"limitType"`
	LimitValue string        `json:"limitValue"`
}

// NewRateLimiter creates a new rate limiter
func NewRateLimiter(config *RateLimitConfig) *RateLimiter {
	if config == nil {
		config = GetDefaultRateLimitConfig()
	}

	rl := &RateLimiter{
		userLimits: make(map[string]*UserLimit),
		ipLimits:   make(map[string]*IPLimit),
		config:     config,
		// redisClient: redisClient, // Commented out
	}

	// Start cleanup routine
	go rl.startCleanupRoutine()

	return rl
}

// CheckRateLimit checks if a request is allowed based on rate limits
func (rl *RateLimiter) CheckRateLimit(ctx context.Context, userID, ipAddress string) (*RateLimitResult, error) {
	// Use distributed rate limiting if enabled and Redis is available
	if rl.config.EnableDistributed {
		// Redis client implementation for distributed rate limiting
		// if rl.redisClient != nil {
		return rl.checkDistributedRateLimit(ctx, userID, ipAddress)
	}

	// Use in-memory rate limiting
	return rl.checkInMemoryRateLimit(ctx, userID, ipAddress)
}

// checkInMemoryRateLimit checks rate limits using in-memory storage
func (rl *RateLimiter) checkInMemoryRateLimit(ctx context.Context, userID, ipAddress string) (*RateLimitResult, error) {
	rl.mutex.Lock()
	defer rl.mutex.Unlock()

	now := time.Now()

	// Check user limits first
	if userID != "" {
		if result := rl.checkUserLimit(userID, now); result != nil {
			return result, nil
		}
	}

	// Check IP limits
	if ipAddress != "" {
		if result := rl.checkIPLimit(ipAddress, now); result != nil {
			return result, nil
		}
	}

	// Check global limits
	if result := rl.checkGlobalLimit(now); result != nil {
		return result, nil
	}

	// All checks passed
	return &RateLimitResult{
		Allowed:   true,
		Remaining: rl.config.UserRequestsPerMinute - 1,
		ResetTime: now.Add(time.Minute),
	}, nil
}

// checkUserLimit checks user-specific rate limits
func (rl *RateLimiter) checkUserLimit(userID string, now time.Time) *RateLimitResult {
	limit, exists := rl.userLimits[userID]
	if !exists {
		limit = &UserLimit{
			Requests:   0,
			LastReset:  now,
			BurstCount: 0,
			LastBurst:  now,
		}
		rl.userLimits[userID] = limit
	}

	// Reset counter if minute has passed
	if now.Sub(limit.LastReset) >= time.Minute {
		limit.Requests = 0
		limit.LastReset = now
	}

	// Check burst limit
	if now.Sub(limit.LastBurst) >= time.Minute {
		limit.BurstCount = 0
		limit.LastBurst = now
	}

	// Check if request is allowed
	if limit.Requests >= rl.config.UserRequestsPerMinute {
		return &RateLimitResult{
			Allowed:    false,
			Remaining:  0,
			ResetTime:  limit.LastReset.Add(time.Minute),
			RetryAfter: time.Until(limit.LastReset.Add(time.Minute)),
			LimitType:  "user",
			LimitValue: userID,
		}
	}

	if limit.BurstCount >= rl.config.UserBurstSize {
		return &RateLimitResult{
			Allowed:    false,
			Remaining:  0,
			ResetTime:  limit.LastBurst.Add(time.Minute),
			RetryAfter: time.Until(limit.LastBurst.Add(time.Minute)),
			LimitType:  "user_burst",
			LimitValue: userID,
		}
	}

	// Allow request and increment counters
	limit.Requests++
	limit.BurstCount++

	return nil
}

// checkIPLimit checks IP-specific rate limits
func (rl *RateLimiter) checkIPLimit(ipAddress string, now time.Time) *RateLimitResult {
	limit, exists := rl.ipLimits[ipAddress]
	if !exists {
		limit = &IPLimit{
			Requests:   0,
			LastReset:  now,
			BurstCount: 0,
			LastBurst:  now,
		}
		rl.ipLimits[ipAddress] = limit
	}

	// Reset counter if minute has passed
	if now.Sub(limit.LastReset) >= time.Minute {
		limit.Requests = 0
		limit.LastReset = now
	}

	// Check burst limit
	if now.Sub(limit.LastBurst) >= time.Minute {
		limit.BurstCount = 0
		limit.LastBurst = now
	}

	// Check if request is allowed
	if limit.Requests >= rl.config.IPRequestsPerMinute {
		return &RateLimitResult{
			Allowed:    false,
			Remaining:  0,
			ResetTime:  limit.LastReset.Add(time.Minute),
			RetryAfter: time.Until(limit.LastReset.Add(time.Minute)),
			LimitType:  "ip",
			LimitValue: ipAddress,
		}
	}

	if limit.BurstCount >= rl.config.IPBurstSize {
		return &RateLimitResult{
			Allowed:    false,
			Remaining:  0,
			ResetTime:  limit.LastBurst.Add(time.Minute),
			RetryAfter: time.Until(limit.LastBurst.Add(time.Minute)),
			LimitType:  "ip_burst",
			LimitValue: ipAddress,
		}
	}

	// Allow request and increment counters
	limit.Requests++
	limit.BurstCount++

	return nil
}

// checkGlobalLimit checks global rate limits
func (rl *RateLimiter) checkGlobalLimit(now time.Time) *RateLimitResult {
	// This is a simplified global limit check
	// In a real implementation, you'd track global requests across all users/IPs

	// For now, always allow (you'd implement proper global tracking)
	return nil
}

// checkDistributedRateLimit checks rate limits using Redis
func (rl *RateLimiter) checkDistributedRateLimit(ctx context.Context, userID, ipAddress string) (*RateLimitResult, error) {
	// This would implement distributed rate limiting using Redis
	// For now, fall back to in-memory rate limiting
	return rl.checkInMemoryRateLimit(ctx, userID, ipAddress)
}

// startCleanupRoutine starts the cleanup routine for old rate limit entries
func (rl *RateLimiter) startCleanupRoutine() {
	ticker := time.NewTicker(rl.config.CleanupInterval)
	defer ticker.Stop()

	for range ticker.C {
		rl.cleanupOldEntries()
	}
}

// cleanupOldEntries removes old rate limit entries
func (rl *RateLimiter) cleanupOldEntries() {
	rl.mutex.Lock()
	defer rl.mutex.Unlock()

	now := time.Now()
	cutoff := now.Add(-time.Hour) // Remove entries older than 1 hour

	// Cleanup user limits
	for userID, limit := range rl.userLimits {
		if limit.LastReset.Before(cutoff) {
			delete(rl.userLimits, userID)
		}
	}

	// Cleanup IP limits
	for ipAddress, limit := range rl.ipLimits {
		if limit.LastReset.Before(cutoff) {
			delete(rl.ipLimits, ipAddress)
		}
	}
}

// GetRateLimitStatus returns current rate limit status for a user/IP
func (rl *RateLimiter) GetRateLimitStatus(ctx context.Context, userID, ipAddress string) (map[string]interface{}, error) {
	rl.mutex.RLock()
	defer rl.mutex.RUnlock()

	status := make(map[string]interface{})

	// Get user status
	if userID != "" {
		if limit, exists := rl.userLimits[userID]; exists {
			status["user"] = map[string]interface{}{
				"requests":       limit.Requests,
				"remaining":      rl.config.UserRequestsPerMinute - limit.Requests,
				"resetTime":      limit.LastReset.Add(time.Minute),
				"burstCount":     limit.BurstCount,
				"burstRemaining": rl.config.UserBurstSize - limit.BurstCount,
			}
		}
	}

	// Get IP status
	if ipAddress != "" {
		if limit, exists := rl.ipLimits[ipAddress]; exists {
			status["ip"] = map[string]interface{}{
				"requests":       limit.Requests,
				"remaining":      rl.config.IPRequestsPerMinute - limit.Requests,
				"resetTime":      limit.LastReset.Add(time.Minute),
				"burstCount":     limit.BurstCount,
				"burstRemaining": rl.config.IPBurstSize - limit.BurstCount,
			}
		}
	}

	// Get configuration
	status["config"] = map[string]interface{}{
		"userRequestsPerMinute":   rl.config.UserRequestsPerMinute,
		"userBurstSize":           rl.config.UserBurstSize,
		"ipRequestsPerMinute":     rl.config.IPRequestsPerMinute,
		"ipBurstSize":             rl.config.IPBurstSize,
		"globalRequestsPerMinute": rl.config.GlobalRequestsPerMinute,
		"globalBurstSize":         rl.config.GlobalBurstSize,
	}

	return status, nil
}

// ResetRateLimit resets rate limits for a user or IP
func (rl *RateLimiter) ResetRateLimit(ctx context.Context, userID, ipAddress string) error {
	rl.mutex.Lock()
	defer rl.mutex.Unlock()

	if userID != "" {
		delete(rl.userLimits, userID)
	}

	if ipAddress != "" {
		delete(rl.ipLimits, ipAddress)
	}

	return nil
}

// UpdateConfig updates rate limiting configuration
func (rl *RateLimiter) UpdateConfig(config *RateLimitConfig) {
	rl.mutex.Lock()
	defer rl.mutex.Unlock()

	rl.config = config
}
