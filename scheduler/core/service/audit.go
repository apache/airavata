package services

import (
	"context"
	"encoding/json"
	"fmt"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"

	"github.com/apache/airavata/scheduler/core/domain"
)

// Local type definitions to replace undefined ports types
type AuditLogRequest struct {
	UserID       string                 `json:"userId"`
	Action       string                 `json:"action"`
	ResourceType string                 `json:"resourceType"`
	ResourceID   string                 `json:"resourceId"`
	Details      map[string]interface{} `json:"details"`
	IPAddress    string                 `json:"ipAddress"`
	UserAgent    string                 `json:"userAgent"`
	Metadata     map[string]interface{} `json:"metadata,omitempty"`
}

type AuditLogQueryRequest struct {
	UserID       string     `json:"userId,omitempty"`
	Action       string     `json:"action,omitempty"`
	ResourceType string     `json:"resourceType,omitempty"`
	ResourceID   string     `json:"resourceId,omitempty"`
	StartTime    *time.Time `json:"startTime,omitempty"`
	EndTime      *time.Time `json:"endTime,omitempty"`
	Limit        int        `json:"limit,omitempty"`
	Offset       int        `json:"offset,omitempty"`
	SortBy       string     `json:"sortBy,omitempty"`
	Order        string     `json:"order,omitempty"`
}

type AuditLogQueryResponse struct {
	Logs  []domain.AuditLog `json:"logs"`
	Total int               `json:"total"`
}

type AuditStats struct {
	TotalLogs      int               `json:"totalLogs"`
	LogsByAction   map[string]int    `json:"logsByAction"`
	LogsByUser     map[string]int    `json:"logsByUser"`
	LogsByResource map[string]int    `json:"logsByResource"`
	ActionCounts   map[string]int64  `json:"actionCounts"`
	ResourceCounts map[string]int64  `json:"resourceCounts"`
	RecentActivity []domain.AuditLog `json:"recentActivity"`
	GeneratedAt    time.Time         `json:"generatedAt"`
}

// AuditService provides audit logging functionality
type AuditService struct {
	db     *gorm.DB
	config *AuditConfig
}

// AuditConfig represents audit service configuration
type AuditConfig struct {
	Enabled          bool          `json:"enabled"`
	RetentionPeriod  time.Duration `json:"retentionPeriod"`
	BatchSize        int           `json:"batchSize"`
	FlushInterval    time.Duration `json:"flushInterval"`
	AsyncLogging     bool          `json:"asyncLogging"`
	IncludeUserAgent bool          `json:"includeUserAgent"`
	IncludeIPAddress bool          `json:"includeIPAddress"`
	IncludeMetadata  bool          `json:"includeMetadata"`
}

// GetDefaultAuditConfig returns default audit configuration
func GetDefaultAuditConfig() *AuditConfig {
	return &AuditConfig{
		Enabled:          true,
		RetentionPeriod:  7 * 24 * time.Hour, // 7 days
		BatchSize:        100,
		FlushInterval:    5 * time.Second,
		AsyncLogging:     true,
		IncludeUserAgent: true,
		IncludeIPAddress: true,
		IncludeMetadata:  true,
	}
}

// NewAuditService creates a new audit service
func NewAuditService(db *gorm.DB, config *AuditConfig) *AuditService {
	if config == nil {
		config = GetDefaultAuditConfig()
	}

	service := &AuditService{
		db:     db,
		config: config,
	}

	// Start background cleanup if enabled
	if config.Enabled {
		go service.startCleanupRoutine()
	}

	return service
}

// LogAction logs a user action to the audit trail
func (s *AuditService) LogAction(ctx context.Context, req *AuditLogRequest) error {
	if !s.config.Enabled {
		return nil
	}

	auditLog := &domain.AuditLog{
		ID:         uuid.New().String(),
		UserID:     req.UserID,
		Action:     req.Action,
		Resource:   req.ResourceType,
		ResourceID: req.ResourceID,
		IPAddress:  req.IPAddress,
		UserAgent:  req.UserAgent,
		Timestamp:  time.Now(),
		Metadata:   req.Metadata,
	}

	// Serialize details if provided
	if req.Details != nil {
		detailsJSON, err := json.Marshal(req.Details)
		if err != nil {
			return fmt.Errorf("failed to marshal audit details: %w", err)
		}
		auditLog.Details = string(detailsJSON)
	}

	// Log asynchronously if configured
	if s.config.AsyncLogging {
		go func() {
			if err := s.db.WithContext(ctx).Create(auditLog).Error; err != nil {
				// Log error but don't fail the operation
				fmt.Printf("Failed to log audit action: %v\n", err)
			}
		}()
		return nil
	}

	// Log synchronously
	return s.db.WithContext(ctx).Create(auditLog).Error
}

// LogExperimentAction logs an experiment-related action
func (s *AuditService) LogExperimentAction(ctx context.Context, userID, action, experimentID string, details interface{}, req *AuditLogRequest) error {
	detailsMap, ok := details.(map[string]interface{})
	if !ok {
		detailsMap = map[string]interface{}{"details": details}
	}

	auditReq := &AuditLogRequest{
		UserID:       userID,
		Action:       action,
		ResourceType: "EXPERIMENT",
		ResourceID:   experimentID,
		Details:      detailsMap,
		IPAddress:    req.IPAddress,
		UserAgent:    req.UserAgent,
		Metadata:     req.Metadata,
	}
	return s.LogAction(ctx, auditReq)
}

// LogTaskAction logs a task-related action
func (s *AuditService) LogTaskAction(ctx context.Context, userID, action, taskID string, details interface{}, req *AuditLogRequest) error {
	detailsMap, ok := details.(map[string]interface{})
	if !ok {
		detailsMap = map[string]interface{}{"details": details}
	}

	auditReq := &AuditLogRequest{
		UserID:       userID,
		Action:       action,
		ResourceType: "TASK",
		ResourceID:   taskID,
		Details:      detailsMap,
		IPAddress:    req.IPAddress,
		UserAgent:    req.UserAgent,
		Metadata:     req.Metadata,
	}
	return s.LogAction(ctx, auditReq)
}

// LogWorkerAction logs a worker-related action
func (s *AuditService) LogWorkerAction(ctx context.Context, userID, action, workerID string, details interface{}, req *AuditLogRequest) error {
	detailsMap, ok := details.(map[string]interface{})
	if !ok {
		detailsMap = map[string]interface{}{"details": details}
	}

	auditReq := &AuditLogRequest{
		UserID:       userID,
		Action:       action,
		ResourceType: "WORKER",
		ResourceID:   workerID,
		Details:      detailsMap,
		IPAddress:    req.IPAddress,
		UserAgent:    req.UserAgent,
		Metadata:     req.Metadata,
	}
	return s.LogAction(ctx, auditReq)
}

// LogResourceAction logs a resource-related action
func (s *AuditService) LogResourceAction(ctx context.Context, userID, action, resourceType, resourceID string, details interface{}, req *AuditLogRequest) error {
	detailsMap, ok := details.(map[string]interface{})
	if !ok {
		detailsMap = map[string]interface{}{"details": details}
	}

	auditReq := &AuditLogRequest{
		UserID:       userID,
		Action:       action,
		ResourceType: resourceType,
		ResourceID:   resourceID,
		Details:      detailsMap,
		IPAddress:    req.IPAddress,
		UserAgent:    req.UserAgent,
		Metadata:     req.Metadata,
	}
	return s.LogAction(ctx, auditReq)
}

// LogAuthentication logs authentication events
func (s *AuditService) LogAuthentication(ctx context.Context, userID, action string, success bool, details interface{}, req *AuditLogRequest) error {
	metadata := map[string]interface{}{
		"success": success,
	}
	if req.Metadata != nil {
		for k, v := range req.Metadata {
			metadata[k] = v
		}
	}

	detailsMap, ok := details.(map[string]interface{})
	if !ok {
		detailsMap = map[string]interface{}{"details": details}
	}

	auditReq := &AuditLogRequest{
		UserID:       userID,
		Action:       action,
		ResourceType: "AUTHENTICATION",
		Details:      detailsMap,
		IPAddress:    req.IPAddress,
		UserAgent:    req.UserAgent,
		Metadata:     metadata,
	}
	return s.LogAction(ctx, auditReq)
}

// GetAuditLogs retrieves audit logs with filtering
func (s *AuditService) GetAuditLogs(ctx context.Context, req *AuditLogQueryRequest) (*AuditLogQueryResponse, error) {
	query := s.db.WithContext(ctx).Model(&domain.AuditLog{})

	// Apply filters
	if req.UserID != "" {
		query = query.Where("user_id = ?", req.UserID)
	}
	if req.Action != "" {
		query = query.Where("action = ?", req.Action)
	}
	if req.ResourceType != "" {
		query = query.Where("resource = ?", req.ResourceType)
	}
	if req.ResourceID != "" {
		query = query.Where("resource_id = ?", req.ResourceID)
	}
	if req.StartTime != nil {
		query = query.Where("timestamp >= ?", *req.StartTime)
	}
	if req.EndTime != nil {
		query = query.Where("timestamp <= ?", *req.EndTime)
	}

	// Get total count
	var total int64
	if err := query.Count(&total).Error; err != nil {
		return nil, fmt.Errorf("failed to count audit logs: %w", err)
	}

	// Apply sorting
	sortBy := req.SortBy
	if sortBy == "" {
		sortBy = "timestamp"
	}
	order := req.Order
	if order == "" {
		order = "DESC"
	}
	query = query.Order(fmt.Sprintf("%s %s", sortBy, order))

	// Apply pagination
	query = query.Limit(req.Limit).Offset(req.Offset)

	var logs []domain.AuditLog
	if err := query.Find(&logs).Error; err != nil {
		return nil, fmt.Errorf("failed to query audit logs: %w", err)
	}

	return &AuditLogQueryResponse{
		Logs:  logs,
		Total: int(total),
	}, nil
}

// startCleanupRoutine starts the background cleanup routine
func (s *AuditService) startCleanupRoutine() {
	ticker := time.NewTicker(24 * time.Hour) // Run daily
	defer ticker.Stop()

	for range ticker.C {
		if err := s.cleanupOldLogs(); err != nil {
			fmt.Printf("Failed to cleanup old audit logs: %v\n", err)
		}
	}
}

// cleanupOldLogs removes audit logs older than the retention period
func (s *AuditService) cleanupOldLogs() error {
	if s.config.RetentionPeriod <= 0 {
		return nil // No cleanup if retention period is 0 or negative
	}

	cutoffTime := time.Now().Add(-s.config.RetentionPeriod)

	result := s.db.Where("timestamp < ?", cutoffTime).Delete(&domain.AuditLog{})
	if result.Error != nil {
		return fmt.Errorf("failed to cleanup old audit logs: %w", result.Error)
	}

	if result.RowsAffected > 0 {
		fmt.Printf("Cleaned up %d old audit logs\n", result.RowsAffected)
	}

	return nil
}

// GetAuditStats returns audit log statistics
func (s *AuditService) GetAuditStats(ctx context.Context) (*AuditStats, error) {
	var stats AuditStats

	// Total audit logs
	var totalLogs int64
	if err := s.db.WithContext(ctx).Model(&domain.AuditLog{}).Count(&totalLogs).Error; err != nil {
		return nil, fmt.Errorf("failed to count total audit logs: %w", err)
	}
	stats.TotalLogs = int(totalLogs)

	// Logs by action type
	var actionCounts []struct {
		Action string `json:"action"`
		Count  int64  `json:"count"`
	}
	if err := s.db.WithContext(ctx).Model(&domain.AuditLog{}).
		Select("action, COUNT(*) as count").
		Group("action").
		Find(&actionCounts).Error; err != nil {
		return nil, fmt.Errorf("failed to get action counts: %w", err)
	}

	stats.ActionCounts = make(map[string]int64)
	for _, ac := range actionCounts {
		stats.ActionCounts[ac.Action] = ac.Count
	}

	// Logs by resource type
	var resourceCounts []struct {
		Resource string `json:"resource"`
		Count    int64  `json:"count"`
	}
	if err := s.db.WithContext(ctx).Model(&domain.AuditLog{}).
		Select("resource, COUNT(*) as count").
		Group("resource").
		Find(&resourceCounts).Error; err != nil {
		return nil, fmt.Errorf("failed to get resource counts: %w", err)
	}

	stats.ResourceCounts = make(map[string]int64)
	for _, rc := range resourceCounts {
		stats.ResourceCounts[rc.Resource] = rc.Count
	}

	// Recent activity (last 24 hours)
	recentCutoff := time.Now().Add(-24 * time.Hour)
	if err := s.db.WithContext(ctx).Model(&domain.AuditLog{}).
		Where("timestamp >= ?", recentCutoff).
		Order("timestamp DESC").
		Limit(100).
		Find(&stats.RecentActivity).Error; err != nil {
		return nil, fmt.Errorf("failed to get recent activity: %w", err)
	}

	return &stats, nil
}
