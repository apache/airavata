package app

import (
	"context"
	"fmt"
	"log"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	service "github.com/apache/airavata/scheduler/core/service"
)

// RecoveryManager handles recovery from unclean shutdowns
type RecoveryManager struct {
	db             *gorm.DB
	stagingManager *service.StagingOperationManager
	repo           ports.RepositoryPort
	events         ports.EventPort
	instanceID     string
}

// SchedulerState represents the scheduler state in the database
type SchedulerState struct {
	ID            string                 `gorm:"primaryKey" json:"id"`
	InstanceID    string                 `gorm:"not null;index" json:"instanceId"`
	Status        string                 `gorm:"not null;index" json:"status"`
	StartupTime   time.Time              `gorm:"autoCreateTime" json:"startupTime"`
	ShutdownTime  *time.Time             `json:"shutdownTime,omitempty"`
	CleanShutdown bool                   `gorm:"default:false" json:"cleanShutdown"`
	LastHeartbeat time.Time              `gorm:"autoUpdateTime" json:"lastHeartbeat"`
	Metadata      map[string]interface{} `gorm:"serializer:json" json:"metadata,omitempty"`
	CreatedAt     time.Time              `gorm:"autoCreateTime" json:"createdAt"`
	UpdatedAt     time.Time              `gorm:"autoUpdateTime" json:"updatedAt"`
}

// TableName overrides the table name used by SchedulerState to `scheduler_state`
func (SchedulerState) TableName() string {
	return "scheduler_state"
}

// SchedulerStatus represents the status of the scheduler
type SchedulerStatus string

const (
	SchedulerStatusStarting     SchedulerStatus = "STARTING"
	SchedulerStatusRunning      SchedulerStatus = "RUNNING"
	SchedulerStatusShuttingDown SchedulerStatus = "SHUTTING_DOWN"
	SchedulerStatusStopped      SchedulerStatus = "STOPPED"
)

// NewRecoveryManager creates a new recovery manager
func NewRecoveryManager(db *gorm.DB, stagingManager *service.StagingOperationManager, repo ports.RepositoryPort, events ports.EventPort) *RecoveryManager {
	manager := &RecoveryManager{
		db:             db,
		stagingManager: stagingManager,
		repo:           repo,
		events:         events,
		instanceID:     generateInstanceID(),
	}

	// Auto-migrate the scheduler_state table
	if err := db.AutoMigrate(&SchedulerState{}); err != nil {
		log.Printf("Warning: failed to auto-migrate scheduler_state table: %v", err)
	}

	return manager
}

// StartRecovery initiates the recovery process on scheduler startup
func (r *RecoveryManager) StartRecovery(ctx context.Context) error {
	log.Println("Starting scheduler recovery process...")

	// Check for unclean shutdown BEFORE updating status
	uncleanShutdown, err := r.detectUncleanShutdown(ctx)
	if err != nil {
		return fmt.Errorf("failed to detect unclean shutdown: %w", err)
	}

	// Mark scheduler as starting
	if err := r.markSchedulerStatus(ctx, SchedulerStatusStarting); err != nil {
		return fmt.Errorf("failed to mark scheduler as starting: %w", err)
	}

	if uncleanShutdown {
		log.Println("Detected unclean shutdown, initiating recovery...")
		if err := r.performRecovery(ctx); err != nil {
			return fmt.Errorf("failed to perform recovery: %w", err)
		}
	} else {
		log.Println("Clean shutdown detected, no recovery needed")
	}

	// Mark scheduler as running
	if err := r.markSchedulerStatus(ctx, SchedulerStatusRunning); err != nil {
		return fmt.Errorf("failed to mark scheduler as running: %w", err)
	}

	// Start heartbeat routine
	go r.startHeartbeatRoutine()

	log.Println("Scheduler recovery process completed successfully")
	return nil
}

// ShutdownRecovery initiates the shutdown process
func (r *RecoveryManager) ShutdownRecovery(ctx context.Context) error {
	log.Println("Starting scheduler shutdown process...")

	// Mark scheduler as shutting down
	if err := r.markSchedulerStatus(ctx, SchedulerStatusShuttingDown); err != nil {
		return fmt.Errorf("failed to mark scheduler as shutting down: %w", err)
	}

	// Perform cleanup operations
	if err := r.performShutdownCleanup(ctx); err != nil {
		log.Printf("Warning: failed to perform shutdown cleanup: %v", err)
	}

	// Mark as clean shutdown
	if err := r.markCleanShutdown(ctx); err != nil {
		return fmt.Errorf("failed to mark clean shutdown: %w", err)
	}

	log.Println("Scheduler shutdown process completed")
	return nil
}

// detectUncleanShutdown checks if the previous shutdown was unclean
func (r *RecoveryManager) detectUncleanShutdown(ctx context.Context) (bool, error) {
	var state SchedulerState
	err := r.db.WithContext(ctx).Where("id = ?", "scheduler").First(&state).Error

	if err != nil {
		if err == gorm.ErrRecordNotFound {
			// No previous state, assume clean shutdown
			return false, nil
		}
		return false, fmt.Errorf("failed to get scheduler state: %w", err)
	}

	// Check if last shutdown was clean
	if state.CleanShutdown {
		return false, nil
	}

	// Check if scheduler was in running state when it stopped
	if state.Status == string(SchedulerStatusRunning) || state.Status == string(SchedulerStatusShuttingDown) {
		return true, nil
	}

	return false, nil
}

// performRecovery performs the actual recovery operations
func (r *RecoveryManager) performRecovery(ctx context.Context) error {
	log.Println("Performing recovery operations...")

	// 1. Resume incomplete staging operations
	if err := r.resumeIncompleteStagingOperations(ctx); err != nil {
		log.Printf("Warning: failed to resume staging operations: %v", err)
	}

	// 2. Requeue tasks in ASSIGNED state back to QUEUED
	if err := r.requeueAssignedTasks(ctx); err != nil {
		log.Printf("Warning: failed to requeue assigned tasks: %v", err)
	}

	// 3. Mark all workers as DISCONNECTED
	if err := r.markWorkersDisconnected(ctx); err != nil {
		log.Printf("Warning: failed to mark workers as disconnected: %v", err)
	}

	// 4. Clean up expired task claims
	if err := r.cleanupExpiredTaskClaims(ctx); err != nil {
		log.Printf("Warning: failed to cleanup expired task claims: %v", err)
	}

	// 5. Process any pending events (if using persistent event queue)
	if err := r.processPendingEvents(ctx); err != nil {
		log.Printf("Warning: failed to process pending events: %v", err)
	}

	log.Println("Recovery operations completed")
	return nil
}

// resumeIncompleteStagingOperations resumes all incomplete staging operations
func (r *RecoveryManager) resumeIncompleteStagingOperations(ctx context.Context) error {
	log.Println("Resuming incomplete staging operations...")

	operations, err := r.stagingManager.ListIncompleteStagingOperations(ctx)
	if err != nil {
		return fmt.Errorf("failed to list incomplete staging operations: %w", err)
	}

	log.Printf("Found %d incomplete staging operations", len(operations))

	for _, operation := range operations {
		log.Printf("Resuming staging operation: %s (task: %s)", operation.ID, operation.TaskID)

		if err := r.stagingManager.ResumeStagingOperation(ctx, operation.ID); err != nil {
			log.Printf("Failed to resume staging operation %s: %v", operation.ID, err)
			// Continue with other operations
		}
	}

	return nil
}

// requeueAssignedTasks requeues tasks that were in ASSIGNED state
func (r *RecoveryManager) requeueAssignedTasks(ctx context.Context) error {
	log.Println("Requeuing assigned tasks...")

	// Get all tasks in ASSIGNED state
	tasks, _, err := r.repo.GetTasksByStatus(ctx, domain.TaskStatusQueued, 1000, 0)
	if err != nil {
		return fmt.Errorf("failed to get assigned tasks: %w", err)
	}

	log.Printf("Found %d assigned tasks to requeue", len(tasks))

	for _, task := range tasks {
		log.Printf("Requeuing task: %s", task.ID)

		// Reset task to QUEUED state
		task.Status = domain.TaskStatusQueued
		task.WorkerID = ""
		task.ComputeResourceID = ""
		task.UpdatedAt = time.Now()

		if err := r.repo.UpdateTask(ctx, task); err != nil {
			log.Printf("Failed to requeue task %s: %v", task.ID, err)
			// Continue with other tasks
		}

		// Publish event
		event := domain.NewAuditEvent("system", "task.requeued.recovery", "task", task.ID)
		if err := r.events.Publish(ctx, event); err != nil {
			log.Printf("Failed to publish task requeued event: %v", err)
		}
	}

	return nil
}

// markWorkersDisconnected marks all workers as disconnected
func (r *RecoveryManager) markWorkersDisconnected(ctx context.Context) error {
	log.Println("Marking workers as disconnected...")

	// Update all workers to DISCONNECTED state
	result := r.db.WithContext(ctx).Model(&domain.Worker{}).
		Where("connection_state != ?", "DISCONNECTED").
		Updates(map[string]interface{}{
			"connection_state": "DISCONNECTED",
			"last_seen_at":     time.Now(),
			"updated_at":       time.Now(),
		})

	if result.Error != nil {
		return fmt.Errorf("failed to mark workers as disconnected: %w", result.Error)
	}

	log.Printf("Marked %d workers as disconnected", result.RowsAffected)

	// Publish event
	event := domain.NewAuditEvent("system", "workers.marked_disconnected", "system", "recovery")
	if err := r.events.Publish(ctx, event); err != nil {
		log.Printf("Failed to publish workers disconnected event: %v", err)
	}

	return nil
}

// cleanupExpiredTaskClaims cleans up expired task claims
func (r *RecoveryManager) cleanupExpiredTaskClaims(ctx context.Context) error {
	log.Println("Cleaning up expired task claims...")

	// Delete expired task claims
	result := r.db.WithContext(ctx).Exec(`
		DELETE FROM task_claims 
		WHERE expires_at < CURRENT_TIMESTAMP
	`)

	if result.Error != nil {
		return fmt.Errorf("failed to cleanup expired task claims: %w", result.Error)
	}

	log.Printf("Cleaned up %d expired task claims", result.RowsAffected)
	return nil
}

// processPendingEvents processes any pending events (placeholder for future event queue implementation)
func (r *RecoveryManager) processPendingEvents(ctx context.Context) error {
	log.Println("Processing pending events...")

	// This is a placeholder for when we implement the persistent event queue
	// For now, we'll just log that we would process events here

	log.Println("No pending events to process (event queue not yet implemented)")
	return nil
}

// performShutdownCleanup performs cleanup operations during shutdown
func (r *RecoveryManager) performShutdownCleanup(ctx context.Context) error {
	log.Println("Performing shutdown cleanup...")

	// 1. Mark all workers as disconnected
	if err := r.markWorkersDisconnected(ctx); err != nil {
		log.Printf("Warning: failed to mark workers as disconnected during shutdown: %v", err)
	}

	// 2. Clean up expired task claims
	if err := r.cleanupExpiredTaskClaims(ctx); err != nil {
		log.Printf("Warning: failed to cleanup expired task claims during shutdown: %v", err)
	}

	// 3. Update any running staging operations to failed state
	if err := r.failRunningStagingOperations(ctx); err != nil {
		log.Printf("Warning: failed to fail running staging operations: %v", err)
	}

	log.Println("Shutdown cleanup completed")
	return nil
}

// failRunningStagingOperations marks all running staging operations as failed
func (r *RecoveryManager) failRunningStagingOperations(ctx context.Context) error {
	log.Println("Failing running staging operations...")

	operations, err := r.stagingManager.ListIncompleteStagingOperations(ctx)
	if err != nil {
		return fmt.Errorf("failed to list running staging operations: %w", err)
	}

	for _, operation := range operations {
		if operation.Status == "RUNNING" {
			log.Printf("Failing staging operation: %s (task: %s)", operation.ID, operation.TaskID)
			if err := r.stagingManager.FailStagingOperation(ctx, operation.ID, "Scheduler shutdown"); err != nil {
				log.Printf("Failed to fail staging operation %s: %v", operation.ID, err)
			}
		}
	}

	return nil
}

// markSchedulerStatus updates the scheduler status in the database
func (r *RecoveryManager) markSchedulerStatus(ctx context.Context, status SchedulerStatus) error {
	now := time.Now()

	// Upsert scheduler state
	err := r.db.WithContext(ctx).Exec(`
		INSERT INTO scheduler_state (id, instance_id, status, startup_time, last_heartbeat, created_at, updated_at)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		ON CONFLICT (id) DO UPDATE SET
			instance_id = EXCLUDED.instance_id,
			status = EXCLUDED.status,
			startup_time = CASE WHEN EXCLUDED.status = 'STARTING' THEN EXCLUDED.startup_time ELSE scheduler_state.startup_time END,
			last_heartbeat = EXCLUDED.last_heartbeat,
			updated_at = EXCLUDED.updated_at
	`, "scheduler", r.instanceID, string(status), now, now, now, now).Error

	if err != nil {
		return fmt.Errorf("failed to update scheduler status: %w", err)
	}

	return nil
}

// markCleanShutdown marks the shutdown as clean
func (r *RecoveryManager) markCleanShutdown(ctx context.Context) error {
	now := time.Now()

	err := r.db.WithContext(ctx).Exec(`
		UPDATE scheduler_state 
		SET status = $1, shutdown_time = $2, clean_shutdown = $3, updated_at = $4
		WHERE id = $5
	`, string(SchedulerStatusStopped), now, true, now, "scheduler").Error

	if err != nil {
		return fmt.Errorf("failed to mark clean shutdown: %w", err)
	}

	return nil
}

// startHeartbeatRoutine starts the heartbeat routine
func (r *RecoveryManager) startHeartbeatRoutine() {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()

	for range ticker.C {
		ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)

		// Update heartbeat
		err := r.db.WithContext(ctx).Exec(`
			UPDATE scheduler_state 
			SET last_heartbeat = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP
			WHERE id = $1
		`, "scheduler").Error

		if err != nil {
			log.Printf("Warning: failed to update scheduler heartbeat: %v", err)
		}

		cancel()
	}
}

// generateInstanceID generates a unique instance ID
func generateInstanceID() string {
	return fmt.Sprintf("scheduler_%d", time.Now().UnixNano())
}

// GetRecoveryStats returns recovery statistics
func (r *RecoveryManager) GetRecoveryStats(ctx context.Context) (map[string]interface{}, error) {
	var state SchedulerState
	err := r.db.WithContext(ctx).Where("id = ?", "scheduler").First(&state).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return map[string]interface{}{
				"status":         "UNKNOWN",
				"clean_shutdown": false,
				"uptime":         "0s",
			}, nil
		}
		return nil, fmt.Errorf("failed to get scheduler state: %w", err)
	}

	uptime := time.Since(state.StartupTime)
	if state.ShutdownTime != nil {
		uptime = state.ShutdownTime.Sub(state.StartupTime)
	}

	return map[string]interface{}{
		"status":         state.Status,
		"instance_id":    state.InstanceID,
		"clean_shutdown": state.CleanShutdown,
		"startup_time":   state.StartupTime,
		"shutdown_time":  state.ShutdownTime,
		"last_heartbeat": state.LastHeartbeat,
		"uptime":         uptime.String(),
	}, nil
}
