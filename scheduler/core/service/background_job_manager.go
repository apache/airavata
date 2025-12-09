package services

import (
	"context"
	"fmt"
	"log"
	"sync"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// BackgroundJobManager manages background jobs and goroutines
type BackgroundJobManager struct {
	db     *gorm.DB
	events ports.EventPort
	jobs   map[string]*BackgroundJob
	mu     sync.RWMutex
}

// BackgroundJob represents a background job in the database
type BackgroundJob struct {
	ID             string                 `gorm:"primaryKey" json:"id"`
	JobType        string                 `gorm:"not null;index" json:"jobType"`
	Status         string                 `gorm:"not null;index" json:"status"`
	Payload        map[string]interface{} `gorm:"serializer:json" json:"payload,omitempty"`
	Priority       int                    `gorm:"default:5" json:"priority"`
	MaxRetries     int                    `gorm:"default:3" json:"maxRetries"`
	RetryCount     int                    `gorm:"default:0" json:"retryCount"`
	ErrorMessage   string                 `gorm:"type:text" json:"errorMessage,omitempty"`
	StartedAt      *time.Time             `json:"startedAt,omitempty"`
	CompletedAt    *time.Time             `json:"completedAt,omitempty"`
	LastHeartbeat  time.Time              `gorm:"default:CURRENT_TIMESTAMP" json:"lastHeartbeat"`
	TimeoutSeconds int                    `gorm:"default:300" json:"timeoutSeconds"`
	Metadata       map[string]interface{} `gorm:"serializer:json" json:"metadata,omitempty"`
	CreatedAt      time.Time              `gorm:"autoCreateTime" json:"createdAt"`
	UpdatedAt      time.Time              `gorm:"autoUpdateTime" json:"updatedAt"`

	// Runtime fields (not persisted)
	ctx    context.Context
	cancel context.CancelFunc
	done   chan struct{}
}

// BackgroundJobStatus represents the status of a background job
type BackgroundJobStatus string

const (
	JobStatusPending   BackgroundJobStatus = "PENDING"
	JobStatusRunning   BackgroundJobStatus = "RUNNING"
	JobStatusCompleted BackgroundJobStatus = "COMPLETED"
	JobStatusFailed    BackgroundJobStatus = "FAILED"
	JobStatusCancelled BackgroundJobStatus = "CANCELLED"
)

// JobType represents the type of background job
type JobType string

const (
	JobTypeStagingMonitor   JobType = "STAGING_MONITOR"
	JobTypeWorkerHealth     JobType = "WORKER_HEALTH"
	JobTypeEventProcessor   JobType = "EVENT_PROCESSOR"
	JobTypeCacheCleanup     JobType = "CACHE_CLEANUP"
	JobTypeMetricsCollector JobType = "METRICS_COLLECTOR"
	JobTypeTaskTimeout      JobType = "TASK_TIMEOUT"
	JobTypeWorkerTimeout    JobType = "WORKER_TIMEOUT"
)

// JobHandler represents a function that handles a background job
type JobHandler func(ctx context.Context, job *BackgroundJob) error

// NewBackgroundJobManager creates a new background job manager
func NewBackgroundJobManager(db *gorm.DB, events ports.EventPort) *BackgroundJobManager {
	manager := &BackgroundJobManager{
		db:     db,
		events: events,
		jobs:   make(map[string]*BackgroundJob),
	}

	// Auto-migrate the background_jobs table
	if err := db.AutoMigrate(&BackgroundJob{}); err != nil {
		log.Printf("Warning: failed to auto-migrate background_jobs table: %v", err)
	}

	// Start background monitoring
	go manager.startBackgroundMonitoring()

	return manager
}

// StartJob starts a new background job
func (m *BackgroundJobManager) StartJob(ctx context.Context, jobType JobType, payload map[string]interface{}, handler JobHandler) (*BackgroundJob, error) {
	job := &BackgroundJob{
		ID:             fmt.Sprintf("job_%s_%d", string(jobType), time.Now().UnixNano()),
		JobType:        string(jobType),
		Status:         string(JobStatusPending),
		Payload:        payload,
		Priority:       5, // Default priority
		MaxRetries:     3,
		RetryCount:     0,
		TimeoutSeconds: 300, // 5 minutes default
		LastHeartbeat:  time.Now(),
		Metadata:       make(map[string]interface{}),
		done:           make(chan struct{}),
	}

	// Create context with timeout
	job.ctx, job.cancel = context.WithTimeout(ctx, time.Duration(job.TimeoutSeconds)*time.Second)

	// Store in database
	if err := m.db.WithContext(ctx).Create(job).Error; err != nil {
		return nil, fmt.Errorf("failed to create background job: %w", err)
	}

	// Store in memory
	m.mu.Lock()
	m.jobs[job.ID] = job
	m.mu.Unlock()

	// Start the job
	go m.runJob(job, handler)

	// Publish event
	event := domain.NewAuditEvent("system", "background.job.started", "background_job", job.ID)
	if err := m.events.Publish(ctx, event); err != nil {
		log.Printf("failed to publish background job started event: %v", err)
	}

	return job, nil
}

// runJob runs a background job
func (m *BackgroundJobManager) runJob(job *BackgroundJob, handler JobHandler) {
	defer func() {
		// Clean up
		m.mu.Lock()
		delete(m.jobs, job.ID)
		m.mu.Unlock()
		close(job.done)
	}()

	// Mark as running
	now := time.Now()
	job.Status = string(JobStatusRunning)
	job.StartedAt = &now
	job.LastHeartbeat = now

	if err := m.db.WithContext(job.ctx).Save(job).Error; err != nil {
		log.Printf("Failed to update job status to running: %v", err)
		return
	}

	// Start heartbeat routine
	heartbeatDone := make(chan struct{})
	go m.startJobHeartbeat(job, heartbeatDone)
	defer close(heartbeatDone)

	// Execute the job
	err := handler(job.ctx, job)

	// Update job status
	now = time.Now()
	if err != nil {
		job.Status = string(JobStatusFailed)
		job.ErrorMessage = err.Error()
		job.RetryCount++
	} else {
		job.Status = string(JobStatusCompleted)
	}
	job.CompletedAt = &now
	job.LastHeartbeat = now

	// Save final status
	if err := m.db.WithContext(context.Background()).Save(job).Error; err != nil {
		log.Printf("Failed to update job final status: %v", err)
	}

	// Publish event
	eventType := "background.job.completed"
	if err != nil {
		eventType = "background.job.failed"
	}
	event := domain.NewAuditEvent("system", eventType, "background_job", job.ID)
	if err := m.events.Publish(context.Background(), event); err != nil {
		log.Printf("failed to publish background job event: %v", err)
	}
}

// startJobHeartbeat starts the heartbeat routine for a job
func (m *BackgroundJobManager) startJobHeartbeat(job *BackgroundJob, done chan struct{}) {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-done:
			return
		case <-ticker.C:
			// Update heartbeat
			job.LastHeartbeat = time.Now()
			if err := m.db.WithContext(context.Background()).Model(job).Update("last_heartbeat", job.LastHeartbeat).Error; err != nil {
				log.Printf("Failed to update job heartbeat: %v", err)
			}
		}
	}
}

// StopJob stops a background job
func (m *BackgroundJobManager) StopJob(ctx context.Context, jobID string) error {
	m.mu.RLock()
	job, exists := m.jobs[jobID]
	m.mu.RUnlock()

	if !exists {
		return fmt.Errorf("job not found: %s", jobID)
	}

	// Cancel the job context
	job.cancel()

	// Wait for job to complete
	select {
	case <-job.done:
		// Job completed
	case <-ctx.Done():
		return ctx.Err()
	}

	// Mark as cancelled
	now := time.Now()
	job.Status = string(JobStatusCancelled)
	job.CompletedAt = &now
	job.LastHeartbeat = now

	if err := m.db.WithContext(ctx).Save(job).Error; err != nil {
		return fmt.Errorf("failed to update job status to cancelled: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent("system", "background.job.cancelled", "background_job", jobID)
	if err := m.events.Publish(ctx, event); err != nil {
		log.Printf("failed to publish background job cancelled event: %v", err)
	}

	return nil
}

// GetJob retrieves a background job by ID
func (m *BackgroundJobManager) GetJob(ctx context.Context, jobID string) (*BackgroundJob, error) {
	var job BackgroundJob
	err := m.db.WithContext(ctx).Where("id = ?", jobID).First(&job).Error
	if err != nil {
		if err == gorm.ErrRecordNotFound {
			return nil, fmt.Errorf("job not found: %s", jobID)
		}
		return nil, fmt.Errorf("failed to get job: %w", err)
	}
	return &job, nil
}

// ListJobs lists background jobs with optional filtering
func (m *BackgroundJobManager) ListJobs(ctx context.Context, jobType *JobType, status *BackgroundJobStatus, limit, offset int) ([]*BackgroundJob, int64, error) {
	query := m.db.WithContext(ctx).Model(&BackgroundJob{})

	if jobType != nil {
		query = query.Where("job_type = ?", string(*jobType))
	}

	if status != nil {
		query = query.Where("status = ?", string(*status))
	}

	// Get total count
	var total int64
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, fmt.Errorf("failed to count jobs: %w", err)
	}

	// Get jobs
	var jobs []*BackgroundJob
	err := query.Order("created_at DESC").Limit(limit).Offset(offset).Find(&jobs).Error
	if err != nil {
		return nil, 0, fmt.Errorf("failed to list jobs: %w", err)
	}

	return jobs, total, nil
}

// WaitForCompletion waits for all jobs to complete
func (m *BackgroundJobManager) WaitForCompletion(ctx context.Context, timeout time.Duration) error {
	log.Printf("Waiting for background jobs to complete (timeout: %v)...", timeout)

	// Create timeout context
	timeoutCtx, cancel := context.WithTimeout(ctx, timeout)
	defer cancel()

	// Wait for all jobs to complete
	for {
		select {
		case <-timeoutCtx.Done():
			return fmt.Errorf("timeout waiting for background jobs to complete")
		default:
			m.mu.RLock()
			activeJobs := len(m.jobs)
			m.mu.RUnlock()

			if activeJobs == 0 {
				log.Println("All background jobs completed")
				return nil
			}

			log.Printf("Waiting for %d background jobs to complete...", activeJobs)
			time.Sleep(1 * time.Second)
		}
	}
}

// PersistState persists the current state of all jobs
func (m *BackgroundJobManager) PersistState(ctx context.Context) error {
	log.Println("Persisting background job state...")

	m.mu.RLock()
	jobs := make([]*BackgroundJob, 0, len(m.jobs))
	for _, job := range m.jobs {
		jobs = append(jobs, job)
	}
	m.mu.RUnlock()

	// Update heartbeat for all active jobs
	for _, job := range jobs {
		job.LastHeartbeat = time.Now()
		if err := m.db.WithContext(ctx).Model(job).Update("last_heartbeat", job.LastHeartbeat).Error; err != nil {
			log.Printf("Failed to update job heartbeat during persist: %v", err)
		}
	}

	log.Printf("Persisted state for %d background jobs", len(jobs))
	return nil
}

// ResumeJobs resumes jobs that were running before shutdown
func (m *BackgroundJobManager) ResumeJobs(ctx context.Context, handlers map[JobType]JobHandler) error {
	log.Println("Resuming background jobs...")

	// Get all running jobs
	var jobs []*BackgroundJob
	err := m.db.WithContext(ctx).Where("status = ?", JobStatusRunning).Find(&jobs).Error
	if err != nil {
		return fmt.Errorf("failed to get running jobs: %w", err)
	}

	log.Printf("Found %d running jobs to resume", len(jobs))

	for _, job := range jobs {
		// Check if job has timed out
		if job.StartedAt != nil {
			timeout := job.StartedAt.Add(time.Duration(job.TimeoutSeconds) * time.Second)
			if time.Now().After(timeout) {
				// Mark as failed due to timeout
				job.Status = string(JobStatusFailed)
				job.ErrorMessage = "Job timed out during scheduler restart"
				job.CompletedAt = &time.Time{}
				*job.CompletedAt = time.Now()
				m.db.WithContext(ctx).Save(job)
				continue
			}
		}

		// Get handler for this job type
		handler, exists := handlers[JobType(job.JobType)]
		if !exists {
			log.Printf("No handler found for job type: %s", job.JobType)
			// Mark as failed
			job.Status = string(JobStatusFailed)
			job.ErrorMessage = "No handler found for job type"
			job.CompletedAt = &time.Time{}
			*job.CompletedAt = time.Now()
			m.db.WithContext(ctx).Save(job)
			continue
		}

		// Resume the job
		job.ctx, job.cancel = context.WithTimeout(ctx, time.Duration(job.TimeoutSeconds)*time.Second)
		job.done = make(chan struct{})

		// Store in memory
		m.mu.Lock()
		m.jobs[job.ID] = job
		m.mu.Unlock()

		// Start the job
		go m.runJob(job, handler)

		log.Printf("Resumed job: %s (type: %s)", job.ID, job.JobType)
	}

	return nil
}

// startBackgroundMonitoring starts the background monitoring routine
func (m *BackgroundJobManager) startBackgroundMonitoring() {
	ticker := time.NewTicker(1 * time.Minute)
	defer ticker.Stop()

	for range ticker.C {
		ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)

		// Check for timed out jobs
		if err := m.checkTimedOutJobs(ctx); err != nil {
			log.Printf("Warning: failed to check for timed out jobs: %v", err)
		}

		// Clean up old completed jobs
		if err := m.cleanupOldJobs(ctx); err != nil {
			log.Printf("Warning: failed to cleanup old jobs: %v", err)
		}

		cancel()
	}
}

// checkTimedOutJobs checks for jobs that have timed out
func (m *BackgroundJobManager) checkTimedOutJobs(ctx context.Context) error {
	// Get running jobs that have timed out
	var jobs []*BackgroundJob
	err := m.db.WithContext(ctx).Where(
		"status = ? AND started_at IS NOT NULL AND (started_at + INTERVAL '1 second' * timeout_seconds) < ?",
		JobStatusRunning, time.Now(),
	).Find(&jobs).Error

	if err != nil {
		return fmt.Errorf("failed to get timed out jobs: %w", err)
	}

	for _, job := range jobs {
		log.Printf("Job timed out: %s (type: %s)", job.ID, job.JobType)

		// Mark as failed
		job.Status = string(JobStatusFailed)
		job.ErrorMessage = "Job timed out"
		job.CompletedAt = &time.Time{}
		*job.CompletedAt = time.Now()
		job.LastHeartbeat = time.Now()

		if err := m.db.WithContext(ctx).Save(job).Error; err != nil {
			log.Printf("Failed to mark job as timed out: %v", err)
		}

		// Publish event
		event := domain.NewAuditEvent("system", "background.job.timed_out", "background_job", job.ID)
		if err := m.events.Publish(ctx, event); err != nil {
			log.Printf("Failed to publish job timed out event: %v", err)
		}
	}

	return nil
}

// cleanupOldJobs cleans up old completed jobs
func (m *BackgroundJobManager) cleanupOldJobs(ctx context.Context) error {
	// Delete jobs older than 7 days
	cutoff := time.Now().AddDate(0, 0, -7)
	result := m.db.WithContext(ctx).Where(
		"status IN ? AND completed_at < ?",
		[]string{string(JobStatusCompleted), string(JobStatusFailed), string(JobStatusCancelled)},
		cutoff,
	).Delete(&BackgroundJob{})

	if result.Error != nil {
		return fmt.Errorf("failed to cleanup old jobs: %w", result.Error)
	}

	if result.RowsAffected > 0 {
		log.Printf("Cleaned up %d old background jobs", result.RowsAffected)
	}

	return nil
}

// GetJobStats returns statistics about background jobs
func (m *BackgroundJobManager) GetJobStats(ctx context.Context) (map[string]interface{}, error) {
	var stats struct {
		Total     int64 `json:"total"`
		Pending   int64 `json:"pending"`
		Running   int64 `json:"running"`
		Completed int64 `json:"completed"`
		Failed    int64 `json:"failed"`
		Cancelled int64 `json:"cancelled"`
	}

	// Get counts by status
	m.db.WithContext(ctx).Model(&BackgroundJob{}).Count(&stats.Total)
	m.db.WithContext(ctx).Model(&BackgroundJob{}).Where("status = ?", JobStatusPending).Count(&stats.Pending)
	m.db.WithContext(ctx).Model(&BackgroundJob{}).Where("status = ?", JobStatusRunning).Count(&stats.Running)
	m.db.WithContext(ctx).Model(&BackgroundJob{}).Where("status = ?", JobStatusCompleted).Count(&stats.Completed)
	m.db.WithContext(ctx).Model(&BackgroundJob{}).Where("status = ?", JobStatusFailed).Count(&stats.Failed)
	m.db.WithContext(ctx).Model(&BackgroundJob{}).Where("status = ?", JobStatusCancelled).Count(&stats.Cancelled)

	// Get active job count from memory
	m.mu.RLock()
	activeJobs := len(m.jobs)
	m.mu.RUnlock()

	return map[string]interface{}{
		"total":       stats.Total,
		"pending":     stats.Pending,
		"running":     stats.Running,
		"completed":   stats.Completed,
		"failed":      stats.Failed,
		"cancelled":   stats.Cancelled,
		"active_jobs": activeJobs,
	}, nil
}
