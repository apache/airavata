package services

import (
	"context"
	"fmt"
	"runtime"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/scheduler/core/domain"
)

// HealthChecker provides comprehensive health checking functionality
type HealthChecker struct {
	db *gorm.DB
}

// NewHealthChecker creates a new health checker
func NewHealthChecker(db *gorm.DB) *HealthChecker {
	return &HealthChecker{
		db: db,
	}
}

// HealthStatus represents the status of a health check
type HealthStatus string

const (
	HealthStatusHealthy   HealthStatus = "healthy"
	HealthStatusDegraded  HealthStatus = "degraded"
	HealthStatusUnhealthy HealthStatus = "unhealthy"
	HealthStatusUnknown   HealthStatus = "unknown"
)

// HealthCheckResult represents the result of a health check
type HealthCheckResult struct {
	Component   string                 `json:"component"`
	Status      HealthStatus           `json:"status"`
	Message     string                 `json:"message,omitempty"`
	Latency     time.Duration          `json:"latency,omitempty"`
	Details     map[string]interface{} `json:"details,omitempty"`
	LastChecked time.Time              `json:"lastChecked"`
}

// DetailedHealthResponse represents a detailed health check response
type DetailedHealthResponse struct {
	Status     HealthStatus        `json:"status"`
	Timestamp  time.Time           `json:"timestamp"`
	Uptime     time.Duration       `json:"uptime"`
	Version    string              `json:"version"`
	Components []HealthCheckResult `json:"components"`
	Summary    map[string]int      `json:"summary"`
}

// BasicHealthResponse represents a basic health check response
type BasicHealthResponse struct {
	Status    HealthStatus  `json:"status"`
	Timestamp time.Time     `json:"timestamp"`
	Uptime    time.Duration `json:"uptime"`
}

// CheckBasicHealth performs a basic health check
func (hc *HealthChecker) CheckBasicHealth(ctx context.Context) *BasicHealthResponse {
	startTime := time.Now()

	// Check database connectivity
	dbStatus := hc.checkDatabase(ctx)

	// Determine overall status
	var status HealthStatus
	if dbStatus.Status == HealthStatusHealthy {
		status = HealthStatusHealthy
	} else {
		status = HealthStatusUnhealthy
	}

	return &BasicHealthResponse{
		Status:    status,
		Timestamp: time.Now(),
		Uptime:    time.Since(startTime),
	}
}

// CheckDetailedHealth performs a comprehensive health check
func (hc *HealthChecker) CheckDetailedHealth(ctx context.Context) *DetailedHealthResponse {
	startTime := time.Now()
	components := []HealthCheckResult{}

	// Check database
	components = append(components, hc.checkDatabase(ctx))
	components = append(components, hc.checkDatabaseConnections(ctx))
	components = append(components, hc.checkDatabasePerformance(ctx))

	// Check scheduler daemon
	components = append(components, hc.checkSchedulerDaemon(ctx))

	// Check workers
	components = append(components, hc.checkWorkers(ctx))

	// Check storage resources
	components = append(components, hc.checkStorageResources(ctx))

	// Check compute resources
	components = append(components, hc.checkComputeResources(ctx))

	// Check system resources
	components = append(components, hc.checkSystemResources(ctx))

	// Check WebSocket connections
	components = append(components, hc.checkWebSocketConnections(ctx))

	// Determine overall status
	status := hc.determineOverallStatus(components)

	// Create summary
	summary := map[string]int{
		"healthy":   0,
		"degraded":  0,
		"unhealthy": 0,
		"unknown":   0,
	}

	for _, component := range components {
		summary[string(component.Status)]++
	}

	return &DetailedHealthResponse{
		Status:     status,
		Timestamp:  time.Now(),
		Uptime:     time.Since(startTime),
		Version:    "1.0.0", // This should come from build info
		Components: components,
		Summary:    summary,
	}
}

// checkDatabase checks database connectivity
func (hc *HealthChecker) checkDatabase(ctx context.Context) HealthCheckResult {
	start := time.Now()

	var result HealthCheckResult
	result.Component = "database"
	result.LastChecked = time.Now()

	// Test basic connectivity
	sqlDB, err := hc.db.DB()
	if err != nil {
		result.Status = HealthStatusUnhealthy
		result.Message = "Failed to get database connection"
		return result
	}

	// Test ping
	if err := sqlDB.PingContext(ctx); err != nil {
		result.Status = HealthStatusUnhealthy
		result.Message = fmt.Sprintf("Database ping failed: %v", err)
		return result
	}

	result.Latency = time.Since(start)
	result.Status = HealthStatusHealthy
	result.Message = "Database connection healthy"

	return result
}

// checkDatabaseConnections checks database connection pool
func (hc *HealthChecker) checkDatabaseConnections(ctx context.Context) HealthCheckResult {
	var result HealthCheckResult
	result.Component = "database_connections"
	result.LastChecked = time.Now()

	sqlDB, err := hc.db.DB()
	if err != nil {
		result.Status = HealthStatusUnhealthy
		result.Message = "Failed to get database connection"
		return result
	}

	stats := sqlDB.Stats()
	result.Details = map[string]interface{}{
		"open_connections":     stats.OpenConnections,
		"in_use":               stats.InUse,
		"idle":                 stats.Idle,
		"wait_count":           stats.WaitCount,
		"wait_duration":        stats.WaitDuration.String(),
		"max_idle_closed":      stats.MaxIdleClosed,
		"max_idle_time_closed": stats.MaxIdleTimeClosed,
		"max_lifetime_closed":  stats.MaxLifetimeClosed,
	}

	// Check if connection pool is healthy
	if stats.OpenConnections > 0 {
		result.Status = HealthStatusHealthy
		result.Message = "Database connection pool healthy"
	} else {
		result.Status = HealthStatusDegraded
		result.Message = "No active database connections"
	}

	return result
}

// checkDatabasePerformance checks database performance
func (hc *HealthChecker) checkDatabasePerformance(ctx context.Context) HealthCheckResult {
	start := time.Now()

	var result HealthCheckResult
	result.Component = "database_performance"
	result.LastChecked = time.Now()

	// Test a simple query
	var count int64
	if err := hc.db.WithContext(ctx).Model(&domain.Experiment{}).Count(&count).Error; err != nil {
		result.Status = HealthStatusUnhealthy
		result.Message = fmt.Sprintf("Database query failed: %v", err)
		return result
	}

	result.Latency = time.Since(start)
	result.Details = map[string]interface{}{
		"experiment_count": count,
		"query_latency_ms": result.Latency.Milliseconds(),
	}

	// Determine status based on latency
	if result.Latency < 100*time.Millisecond {
		result.Status = HealthStatusHealthy
		result.Message = "Database performance good"
	} else if result.Latency < 500*time.Millisecond {
		result.Status = HealthStatusDegraded
		result.Message = "Database performance degraded"
	} else {
		result.Status = HealthStatusUnhealthy
		result.Message = "Database performance poor"
	}

	return result
}

// checkSchedulerDaemon checks scheduler daemon status
func (hc *HealthChecker) checkSchedulerDaemon(ctx context.Context) HealthCheckResult {
	var result HealthCheckResult
	result.Component = "scheduler_daemon"
	result.LastChecked = time.Now()

	// Check for recent scheduler activity
	var lastActivity time.Time
	if err := hc.db.WithContext(ctx).Model(&domain.Experiment{}).
		Select("MAX(updated_at)").
		Where("status IN ?", []domain.ExperimentStatus{
			domain.ExperimentStatusExecuting,
		}).
		Scan(&lastActivity).Error; err != nil {
		result.Status = HealthStatusUnknown
		result.Message = "Unable to check scheduler activity"
		return result
	}

	// Check for pending experiments
	var pendingCount int64
	if err := hc.db.WithContext(ctx).Model(&domain.Experiment{}).
		Where("status = ?", domain.ExperimentStatusExecuting).
		Count(&pendingCount).Error; err != nil {
		result.Status = HealthStatusUnknown
		result.Message = "Unable to check pending experiments"
		return result
	}

	result.Details = map[string]interface{}{
		"last_activity":       lastActivity,
		"pending_experiments": pendingCount,
	}

	// Determine status
	if time.Since(lastActivity) < 5*time.Minute {
		result.Status = HealthStatusHealthy
		result.Message = "Scheduler daemon active"
	} else if time.Since(lastActivity) < 15*time.Minute {
		result.Status = HealthStatusDegraded
		result.Message = "Scheduler daemon slow"
	} else {
		result.Status = HealthStatusUnhealthy
		result.Message = "Scheduler daemon inactive"
	}

	return result
}

// checkWorkers checks worker status
func (hc *HealthChecker) checkWorkers(ctx context.Context) HealthCheckResult {
	var result HealthCheckResult
	result.Component = "workers"
	result.LastChecked = time.Now()

	// Get worker statistics
	var stats struct {
		Total     int64 `json:"total"`
		Active    int64 `json:"active"`
		Idle      int64 `json:"idle"`
		Unhealthy int64 `json:"unhealthy"`
	}

	if err := hc.db.WithContext(ctx).Model(&domain.Worker{}).
		Select(`
			COUNT(*) as total,
			COUNT(CASE WHEN status = 'RUNNING' THEN 1 END) as active,
			COUNT(CASE WHEN status = 'IDLE' THEN 1 END) as idle,
			COUNT(CASE WHEN last_heartbeat < NOW() - INTERVAL '5 minutes' THEN 1 END) as unhealthy
		`).
		Scan(&stats).Error; err != nil {
		result.Status = HealthStatusUnknown
		result.Message = "Unable to check worker status"
		return result
	}

	result.Details = map[string]interface{}{
		"total":     stats.Total,
		"active":    stats.Active,
		"idle":      stats.Idle,
		"unhealthy": stats.Unhealthy,
	}

	// Determine status
	if stats.Unhealthy == 0 && stats.Active > 0 {
		result.Status = HealthStatusHealthy
		result.Message = "Workers healthy"
	} else if stats.Unhealthy < stats.Total/2 {
		result.Status = HealthStatusDegraded
		result.Message = "Some workers unhealthy"
	} else {
		result.Status = HealthStatusUnhealthy
		result.Message = "Many workers unhealthy"
	}

	return result
}

// checkStorageResources checks storage resource availability
func (hc *HealthChecker) checkStorageResources(ctx context.Context) HealthCheckResult {
	var result HealthCheckResult
	result.Component = "storage_resources"
	result.LastChecked = time.Now()

	// Get storage resource statistics
	var stats struct {
		Total      int64 `json:"total"`
		Accessible int64 `json:"accessible"`
	}

	if err := hc.db.WithContext(ctx).Model(&domain.StorageResource{}).
		Select(`
			COUNT(*) as total,
			COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as accessible
		`).
		Scan(&stats).Error; err != nil {
		result.Status = HealthStatusUnknown
		result.Message = "Unable to check storage resources"
		return result
	}

	result.Details = map[string]interface{}{
		"total":      stats.Total,
		"accessible": stats.Accessible,
	}

	// Determine status
	if stats.Accessible == stats.Total && stats.Total > 0 {
		result.Status = HealthStatusHealthy
		result.Message = "All storage resources accessible"
	} else if stats.Accessible > 0 {
		result.Status = HealthStatusDegraded
		result.Message = "Some storage resources inaccessible"
	} else {
		result.Status = HealthStatusUnhealthy
		result.Message = "No storage resources accessible"
	}

	return result
}

// checkComputeResources checks compute resource availability
func (hc *HealthChecker) checkComputeResources(ctx context.Context) HealthCheckResult {
	var result HealthCheckResult
	result.Component = "compute_resources"
	result.LastChecked = time.Now()

	// Get compute resource statistics
	var stats struct {
		Total      int64 `json:"total"`
		Accessible int64 `json:"accessible"`
	}

	if err := hc.db.WithContext(ctx).Model(&domain.ComputeResource{}).
		Select(`
			COUNT(*) as total,
			COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as accessible
		`).
		Scan(&stats).Error; err != nil {
		result.Status = HealthStatusUnknown
		result.Message = "Unable to check compute resources"
		return result
	}

	result.Details = map[string]interface{}{
		"total":      stats.Total,
		"accessible": stats.Accessible,
	}

	// Determine status
	if stats.Accessible == stats.Total && stats.Total > 0 {
		result.Status = HealthStatusHealthy
		result.Message = "All compute resources accessible"
	} else if stats.Accessible > 0 {
		result.Status = HealthStatusDegraded
		result.Message = "Some compute resources inaccessible"
	} else {
		result.Status = HealthStatusUnhealthy
		result.Message = "No compute resources accessible"
	}

	return result
}

// checkSystemResources checks system resource usage
func (hc *HealthChecker) checkSystemResources(ctx context.Context) HealthCheckResult {
	var result HealthCheckResult
	result.Component = "system_resources"
	result.LastChecked = time.Now()

	// Get system memory stats
	var m runtime.MemStats
	runtime.ReadMemStats(&m)

	result.Details = map[string]interface{}{
		"memory_alloc_mb":       bToMb(m.Alloc),
		"memory_total_alloc_mb": bToMb(m.TotalAlloc),
		"memory_sys_mb":         bToMb(m.Sys),
		"num_gc":                m.NumGC,
		"goroutines":            runtime.NumGoroutine(),
	}

	// Determine status based on memory usage
	memoryUsagePercent := float64(m.Alloc) / float64(m.Sys) * 100
	if memoryUsagePercent < 70 {
		result.Status = HealthStatusHealthy
		result.Message = "System resources healthy"
	} else if memoryUsagePercent < 90 {
		result.Status = HealthStatusDegraded
		result.Message = "System resources under pressure"
	} else {
		result.Status = HealthStatusUnhealthy
		result.Message = "System resources critical"
	}

	return result
}

// checkWebSocketConnections checks WebSocket connection status
func (hc *HealthChecker) checkWebSocketConnections(ctx context.Context) HealthCheckResult {
	var result HealthCheckResult
	result.Component = "websocket_connections"
	result.LastChecked = time.Now()

	// In a real implementation, this would integrate with the WebSocket hub
	// to get actual connection statistics. For now, we'll check if the
	// WebSocket endpoint is accessible by looking for recent activity.

	// Check for recent WebSocket-related activity in the database
	var recentActivity int64
	if err := hc.db.WithContext(ctx).Model(&domain.DomainEvent{}).
		Where("event_type LIKE ? AND created_at > ?", "%websocket%", time.Now().Add(-5*time.Minute)).
		Count(&recentActivity).Error; err != nil {
		result.Status = HealthStatusUnknown
		result.Message = "Unable to check WebSocket activity"
		return result
	}

	result.Details = map[string]interface{}{
		"recent_activity": recentActivity,
		"status":          "WebSocket service available",
	}

	// Determine status based on recent activity
	if recentActivity > 0 {
		result.Status = HealthStatusHealthy
		result.Message = "WebSocket connections active"
	} else {
		result.Status = HealthStatusDegraded
		result.Message = "No recent WebSocket activity"
	}

	return result
}

// determineOverallStatus determines the overall health status
func (hc *HealthChecker) determineOverallStatus(components []HealthCheckResult) HealthStatus {
	hasUnhealthy := false
	hasDegraded := false

	for _, component := range components {
		switch component.Status {
		case HealthStatusUnhealthy:
			hasUnhealthy = true
		case HealthStatusDegraded:
			hasDegraded = true
		}
	}

	if hasUnhealthy {
		return HealthStatusUnhealthy
	} else if hasDegraded {
		return HealthStatusDegraded
	} else {
		return HealthStatusHealthy
	}
}

// bToMb converts bytes to megabytes
func bToMb(b uint64) uint64 {
	return b / 1024 / 1024
}
