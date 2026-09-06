package services

import (
	"context"
	"fmt"
	"strings"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// StagingOperationManager manages persistent staging operations
type StagingOperationManager struct {
	db           *gorm.DB
	events       ports.EventPort
	shutdownChan chan struct{}
}

// StagingOperation represents a staging operation in the database
type StagingOperation struct {
	ID                string                 `gorm:"primaryKey" json:"id"`
	TaskID            string                 `gorm:"not null;index" json:"taskId"`
	WorkerID          string                 `gorm:"not null;index" json:"workerId"`
	ComputeResourceID string                 `gorm:"not null;index" json:"computeResourceId"`
	Status            string                 `gorm:"not null;index" json:"status"`
	SourcePath        string                 `gorm:"size:1000" json:"sourcePath,omitempty"`
	DestinationPath   string                 `gorm:"size:1000" json:"destinationPath,omitempty"`
	TotalSize         *int64                 `json:"totalSize,omitempty"`
	TransferredSize   int64                  `gorm:"default:0" json:"transferredSize"`
	TransferRate      *float64               `json:"transferRate,omitempty"`
	ErrorMessage      string                 `gorm:"type:text" json:"errorMessage,omitempty"`
	TimeoutSeconds    int                    `gorm:"default:600" json:"timeoutSeconds"`
	StartedAt         *time.Time             `json:"startedAt,omitempty"`
	CompletedAt       *time.Time             `json:"completedAt,omitempty"`
	LastHeartbeat     time.Time              `gorm:"default:CURRENT_TIMESTAMP" json:"lastHeartbeat"`
	Metadata          map[string]interface{} `gorm:"serializer:json" json:"metadata,omitempty"`
	CreatedAt         time.Time              `gorm:"autoCreateTime" json:"createdAt"`
	UpdatedAt         time.Time              `gorm:"autoUpdateTime" json:"updatedAt"`
}

// StagingOperationStatus represents the status of a staging operation
type StagingOperationStatus string

const (
	StagingStatusPending   StagingOperationStatus = "PENDING"
	StagingStatusRunning   StagingOperationStatus = "RUNNING"
	StagingStatusCompleted StagingOperationStatus = "COMPLETED"
	StagingStatusFailed    StagingOperationStatus = "FAILED"
	StagingStatusTimeout   StagingOperationStatus = "TIMEOUT"
)

// NewStagingOperationManager creates a new staging operation manager
func NewStagingOperationManager(db *gorm.DB, events ports.EventPort) *StagingOperationManager {
	manager := &StagingOperationManager{
		db:           db,
		events:       events,
		shutdownChan: make(chan struct{}),
	}

	// Auto-migrate the staging_operations table
	if err := db.AutoMigrate(&StagingOperation{}); err != nil {
		fmt.Printf("Warning: failed to auto-migrate staging_operations table: %v\n", err)
	}

	// Start background monitoring
	go manager.startBackgroundMonitoring()

	return manager
}

// NewStagingOperationManagerForTesting creates a new staging operation manager for testing
// without starting background monitoring to avoid database connection issues during test cleanup
func NewStagingOperationManagerForTesting(db *gorm.DB, events ports.EventPort) *StagingOperationManager {
	manager := &StagingOperationManager{
		db:           db,
		events:       events,
		shutdownChan: make(chan struct{}),
	}

	// Auto-migrate the staging_operations table
	if err := db.AutoMigrate(&StagingOperation{}); err != nil {
		fmt.Printf("Warning: failed to auto-migrate staging_operations table: %v\n", err)
	}

	// Don't start background monitoring for tests
	return manager
}

// CreateStagingOperation creates a new staging operation
func (m *StagingOperationManager) CreateStagingOperation(ctx context.Context, taskID, workerID, computeResourceID string, sourcePath, destPath string, timeoutSeconds int) (*StagingOperation, error) {
	now := time.Now()
	operation := &StagingOperation{
		ID:                fmt.Sprintf("staging_%s_%d", taskID, now.UnixNano()),
		TaskID:            taskID,
		WorkerID:          workerID,
		ComputeResourceID: computeResourceID,
		Status:            string(StagingStatusPending),
		SourcePath:        sourcePath,
		DestinationPath:   destPath,
		// TotalSize is nil (NULL in database) - will be set when staging starts
		TransferredSize: 0,
		TimeoutSeconds:  timeoutSeconds,
		// LastHeartbeat will be set by database default CURRENT_TIMESTAMP
		Metadata: make(map[string]interface{}),
	}

	if err := m.db.WithContext(ctx).Create(operation).Error; err != nil {
		return nil, fmt.Errorf("failed to create staging operation: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent("system", "staging.operation.created", "staging_operation", operation.ID)
	if err := m.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish staging operation created event: %v\n", err)
	}

	return operation, nil
}

// StartStagingOperation marks a staging operation as running
func (m *StagingOperationManager) StartStagingOperation(ctx context.Context, operationID string) error {
	now := time.Now()
	result := m.db.WithContext(ctx).Model(&StagingOperation{}).
		Where("id = ? AND status = ?", operationID, StagingStatusPending).
		Updates(map[string]interface{}{
			"status":         StagingStatusRunning,
			"started_at":     now,
			"last_heartbeat": now,
			"updated_at":     now,
		})

	if result.Error != nil {
		return fmt.Errorf("failed to start staging operation: %w", result.Error)
	}

	if result.RowsAffected == 0 {
		return fmt.Errorf("staging operation not found or not in pending status: %s", operationID)
	}

	// Publish event
	event := domain.NewAuditEvent("system", "staging.operation.started", "staging_operation", operationID)
	if err := m.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish staging operation started event: %v\n", err)
	}

	return nil
}

// UpdateStagingProgress updates the progress of a staging operation
func (m *StagingOperationManager) UpdateStagingProgress(ctx context.Context, operationID string, transferredSize int64, transferRate float64) error {
	now := time.Now()
	result := m.db.WithContext(ctx).Model(&StagingOperation{}).
		Where("id = ? AND status = ?", operationID, StagingStatusRunning).
		Updates(map[string]interface{}{
			"transferred_size": transferredSize,
			"transfer_rate":    transferRate,
			"last_heartbeat":   now,
			"updated_at":       now,
		})

	if result.Error != nil {
		return fmt.Errorf("failed to update staging progress: %w", result.Error)
	}

	return nil
}

// CompleteStagingOperation marks a staging operation as completed
func (m *StagingOperationManager) CompleteStagingOperation(ctx context.Context, operationID string) error {
	now := time.Now()
	result := m.db.WithContext(ctx).Model(&StagingOperation{}).
		Where("id = ? AND status = ?", operationID, StagingStatusRunning).
		Updates(map[string]interface{}{
			"status":         StagingStatusCompleted,
			"completed_at":   now,
			"last_heartbeat": now,
			"updated_at":     now,
		})

	if result.Error != nil {
		return fmt.Errorf("failed to complete staging operation: %w", result.Error)
	}

	if result.RowsAffected == 0 {
		return fmt.Errorf("staging operation not found or not in running status: %s", operationID)
	}

	// Publish event
	event := domain.NewAuditEvent("system", "staging.operation.completed", "staging_operation", operationID)
	if err := m.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish staging operation completed event: %v\n", err)
	}

	return nil
}

// FailStagingOperation marks a staging operation as failed
func (m *StagingOperationManager) FailStagingOperation(ctx context.Context, operationID string, errorMessage string) error {
	now := time.Now()
	result := m.db.WithContext(ctx).Model(&StagingOperation{}).
		Where("id = ?", operationID).
		Updates(map[string]interface{}{
			"status":         StagingStatusFailed,
			"error_message":  errorMessage,
			"completed_at":   now,
			"last_heartbeat": now,
			"updated_at":     now,
		})

	if result.Error != nil {
		return fmt.Errorf("failed to fail staging operation: %w", result.Error)
	}

	if result.RowsAffected == 0 {
		return fmt.Errorf("staging operation not found: %s", operationID)
	}

	// Publish event
	event := domain.NewAuditEvent("system", "staging.operation.failed", "staging_operation", operationID)
	if err := m.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish staging operation failed event: %v\n", err)
	}

	return nil
}

// GetStagingOperation retrieves a staging operation by ID
func (m *StagingOperationManager) GetStagingOperation(ctx context.Context, operationID string) (*StagingOperation, error) {
	var operation StagingOperation
	err := m.db.WithContext(ctx).Where("id = ?", operationID).First(&operation).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, fmt.Errorf("staging operation not found: %s", operationID)
		}
		return nil, fmt.Errorf("failed to get staging operation: %w", err)
	}
	return &operation, nil
}

// GetStagingOperationByTaskID retrieves a staging operation by task ID
func (m *StagingOperationManager) GetStagingOperationByTaskID(ctx context.Context, taskID string) (*StagingOperation, error) {
	var operation StagingOperation
	err := m.db.WithContext(ctx).Where("task_id = ?", taskID).First(&operation).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, fmt.Errorf("staging operation not found for task: %s", taskID)
		}
		return nil, fmt.Errorf("failed to get staging operation by task ID: %w", err)
	}
	return &operation, nil
}

// ListIncompleteStagingOperations returns all incomplete staging operations
func (m *StagingOperationManager) ListIncompleteStagingOperations(ctx context.Context) ([]*StagingOperation, error) {
	var operations []*StagingOperation
	err := m.db.WithContext(ctx).Where("status IN ?", []string{
		string(StagingStatusPending),
		string(StagingStatusRunning),
	}).Find(&operations).Error

	if err != nil {
		return nil, fmt.Errorf("failed to list incomplete staging operations: %w", err)
	}

	return operations, nil
}

// ListTimedOutStagingOperations returns all timed out staging operations
func (m *StagingOperationManager) ListTimedOutStagingOperations(ctx context.Context) ([]*StagingOperation, error) {
	var operations []*StagingOperation
	err := m.db.WithContext(ctx).Where(
		"status = ? AND started_at IS NOT NULL AND (started_at + INTERVAL '1 second' * timeout_seconds) < ?",
		StagingStatusRunning, time.Now(),
	).Find(&operations).Error

	if err != nil {
		return nil, fmt.Errorf("failed to list timed out staging operations: %w", err)
	}

	return operations, nil
}

// ResumeStagingOperation resumes a staging operation after scheduler restart
func (m *StagingOperationManager) ResumeStagingOperation(ctx context.Context, operationID string) error {
	operation, err := m.GetStagingOperation(ctx, operationID)
	if err != nil {
		return err
	}

	// Check if operation is still valid
	if operation.Status == string(StagingStatusCompleted) {
		return nil // Already completed
	}

	if operation.Status == string(StagingStatusFailed) {
		return fmt.Errorf("staging operation is in failed state: %s", operationID)
	}

	// Check for timeout
	if operation.StartedAt != nil {
		timeout := operation.StartedAt.Add(time.Duration(operation.TimeoutSeconds) * time.Second)
		if time.Now().After(timeout) {
			// Mark as timed out
			return m.FailStagingOperation(ctx, operationID, "Operation timed out during scheduler restart")
		}
	}

	// Resume the operation
	if operation.Status == string(StagingStatusPending) {
		return m.StartStagingOperation(ctx, operationID)
	}

	// For running operations, just update heartbeat
	now := time.Now()
	result := m.db.WithContext(ctx).Model(&StagingOperation{}).
		Where("id = ?", operationID).
		Update("last_heartbeat", now)

	if result.Error != nil {
		return fmt.Errorf("failed to update staging operation heartbeat: %w", result.Error)
	}

	// Publish event
	event := domain.NewAuditEvent("system", "staging.operation.resumed", "staging_operation", operationID)
	if err := m.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish staging operation resumed event: %v\n", err)
	}

	return nil
}

// DeleteStagingOperation deletes a staging operation
func (m *StagingOperationManager) DeleteStagingOperation(ctx context.Context, operationID string) error {
	result := m.db.WithContext(ctx).Delete(&StagingOperation{}, "id = ?", operationID)
	if result.Error != nil {
		return fmt.Errorf("failed to delete staging operation: %w", result.Error)
	}

	if result.RowsAffected == 0 {
		return fmt.Errorf("staging operation not found: %s", operationID)
	}

	return nil
}

// Shutdown stops the background monitoring goroutine
func (m *StagingOperationManager) Shutdown(ctx context.Context) error {
	close(m.shutdownChan)
	return nil
}

// startBackgroundMonitoring starts the background monitoring routine
func (m *StagingOperationManager) startBackgroundMonitoring() {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-ticker.C:
			ctx, cancel := context.WithTimeout(context.Background(), 60*time.Second)

			// Check if database connection is still valid
			if m.db == nil {
				cancel()
				continue
			}

			// Test database connection before proceeding
			sqlDB, err := m.db.DB()
			if err != nil || sqlDB == nil {
				cancel()
				continue
			}

			if err := sqlDB.Ping(); err != nil {
				// Database connection is closed, skip this iteration
				cancel()
				continue
			}

			// Check for timed out operations
			timedOutOps, err := m.ListTimedOutStagingOperations(ctx)
			if err != nil {
				// Only log if it's not a connection issue
				if !isConnectionError(err) {
					fmt.Printf("Warning: failed to check for timed out staging operations: %v\n", err)
				}
			} else {
				for _, op := range timedOutOps {
					fmt.Printf("Staging operation timed out: %s (task: %s)\n", op.ID, op.TaskID)
					if err := m.FailStagingOperation(ctx, op.ID, "Operation timed out"); err != nil {
						fmt.Printf("Failed to mark staging operation as timed out: %v\n", err)
					}
				}
			}

			// Clean up old completed operations (older than 7 days)
			cutoff := time.Now().AddDate(0, 0, -7)
			result := m.db.WithContext(ctx).Where(
				"status IN ? AND completed_at < ?",
				[]string{string(StagingStatusCompleted), string(StagingStatusFailed)},
				cutoff,
			).Delete(&StagingOperation{})

			if result.Error != nil {
				// Only log if it's not a connection issue
				if !isConnectionError(result.Error) {
					fmt.Printf("Warning: failed to cleanup old staging operations: %v\n", result.Error)
				}
			} else if result.RowsAffected > 0 {
				fmt.Printf("Cleaned up %d old staging operations\n", result.RowsAffected)
			}

			cancel()
		case <-m.shutdownChan:
			return
		}
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

// MonitorStagingProgress monitors the progress of a staging operation and calls completion callback
func (m *StagingOperationManager) MonitorStagingProgress(ctx context.Context, operationID string, onComplete func() error) {
	// Start the operation
	if err := m.StartStagingOperation(ctx, operationID); err != nil {
		fmt.Printf("Failed to start staging operation %s: %v\n", operationID, err)
		return
	}

	// Simulate staging progress (in real implementation, this would monitor actual data transfer)
	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()

	timeout := time.After(10 * time.Minute) // 10 minute timeout

	for {
		select {
		case <-ctx.Done():
			// Context cancelled, mark as failed
			m.FailStagingOperation(context.Background(), operationID, "Context cancelled")
			return
		case <-timeout:
			// Timeout reached, mark as failed
			m.FailStagingOperation(context.Background(), operationID, "Staging timeout")
			return
		case <-ticker.C:
			// Update progress (simulate)
			operation, err := m.GetStagingOperation(ctx, operationID)
			if err != nil {
				fmt.Printf("Failed to get staging operation: %v\n", err)
				continue
			}

			// Simulate progress
			transferredSize := operation.TransferredSize + 1024*1024 // 1MB per update
			transferRate := 1.0                                      // 1 MB/s

			if err := m.UpdateStagingProgress(ctx, operationID, transferredSize, transferRate); err != nil {
				fmt.Printf("Failed to update staging progress: %v\n", err)
				continue
			}

			// Simulate completion after some progress
			if transferredSize >= 10*1024*1024 { // 10MB
				// Mark as completed
				if err := m.CompleteStagingOperation(ctx, operationID); err != nil {
					fmt.Printf("Failed to complete staging operation: %v\n", err)
					return
				}

				// Call completion callback
				if onComplete != nil {
					if err := onComplete(); err != nil {
						fmt.Printf("Staging completion callback failed: %v\n", err)
						m.FailStagingOperation(context.Background(), operationID, fmt.Sprintf("Completion callback failed: %v", err))
						return
					}
				}

				return
			}
		}
	}
}

// GetStagingOperationStats returns statistics about staging operations
func (m *StagingOperationManager) GetStagingOperationStats(ctx context.Context) (map[string]interface{}, error) {
	var stats struct {
		Total     int64 `json:"total"`
		Pending   int64 `json:"pending"`
		Running   int64 `json:"running"`
		Completed int64 `json:"completed"`
		Failed    int64 `json:"failed"`
		TimedOut  int64 `json:"timedOut"`
	}

	// Get counts by status
	m.db.WithContext(ctx).Model(&StagingOperation{}).Count(&stats.Total)
	m.db.WithContext(ctx).Model(&StagingOperation{}).Where("status = ?", StagingStatusPending).Count(&stats.Pending)
	m.db.WithContext(ctx).Model(&StagingOperation{}).Where("status = ?", StagingStatusRunning).Count(&stats.Running)
	m.db.WithContext(ctx).Model(&StagingOperation{}).Where("status = ?", StagingStatusCompleted).Count(&stats.Completed)
	m.db.WithContext(ctx).Model(&StagingOperation{}).Where("status = ?", StagingStatusFailed).Count(&stats.Failed)
	m.db.WithContext(ctx).Model(&StagingOperation{}).Where("status = ?", StagingStatusTimeout).Count(&stats.TimedOut)

	return map[string]interface{}{
		"total":     stats.Total,
		"pending":   stats.Pending,
		"running":   stats.Running,
		"completed": stats.Completed,
		"failed":    stats.Failed,
		"timed_out": stats.TimedOut,
	}, nil
}
