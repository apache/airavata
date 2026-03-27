package services

import (
	"context"
	"fmt"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// WorkerService implements the WorkerLifecycle interface
type WorkerService struct {
	repo    ports.RepositoryPort
	compute ports.ComputePort
	events  ports.EventPort
}

// Compile-time interface verification
var _ domain.WorkerLifecycle = (*WorkerService)(nil)

// NewWorkerService creates a new WorkerLifecycle service
func NewWorkerService(repo ports.RepositoryPort, compute ports.ComputePort, events ports.EventPort) *WorkerService {
	return &WorkerService{
		repo:    repo,
		compute: compute,
		events:  events,
	}
}

// SpawnWorker implements domain.WorkerLifecycle.SpawnWorker
func (s *WorkerService) SpawnWorker(ctx context.Context, computeResourceID string, experimentID string, walltime time.Duration) (*domain.Worker, error) {
	// Get compute resource
	computeResource, err := s.repo.GetComputeResourceByID(ctx, computeResourceID)
	if err != nil {
		return nil, fmt.Errorf("compute resource not found: %w", err)
	}
	if computeResource == nil {
		return nil, domain.ErrResourceNotFound
	}

	// Generate worker ID
	workerID := s.generateWorkerID(computeResourceID, experimentID)

	// Create worker record
	worker := &domain.Worker{
		ID:                workerID,
		ComputeResourceID: computeResourceID,
		ExperimentID:      experimentID,
		Status:            domain.WorkerStatusIdle,
		Walltime:          walltime,
		WalltimeRemaining: walltime,
		LastHeartbeat:     time.Now(),
		CreatedAt:         time.Now(),
		UpdatedAt:         time.Now(),
		Metadata:          make(map[string]interface{}),
	}

	// Store worker in repository
	if err := s.repo.CreateWorker(ctx, worker); err != nil {
		return nil, fmt.Errorf("failed to create worker record: %w", err)
	}

	// Get experiment to extract resource requirements
	experiment, err := s.repo.GetExperimentByID(ctx, experimentID)
	if err != nil {
		return nil, fmt.Errorf("failed to get experiment: %w", err)
	}

	// Extract resource requirements from experiment
	spawnReq := &ports.SpawnWorkerRequest{
		WorkerID:         workerID,
		ExperimentID:     experimentID,
		Command:          "worker",
		Walltime:         walltime,
		CPUCores:         1, // Default values
		MemoryMB:         1024,
		DiskGB:           10,
		GPUs:             0,
		Queue:            "default",
		Priority:         5,
		Environment:      make(map[string]string),
		WorkingDirectory: "/tmp/worker",
		InputFiles:       []string{},
		OutputFiles:      []string{},
		Metadata:         make(map[string]interface{}),
	}

	// Extract resource requirements from experiment metadata
	if experiment.Metadata != nil {
		if cpu, ok := experiment.Metadata["cpu_cores"].(int); ok {
			spawnReq.CPUCores = cpu
		}
		if mem, ok := experiment.Metadata["memory_mb"].(int); ok {
			spawnReq.MemoryMB = mem
		}
		if disk, ok := experiment.Metadata["disk_gb"].(int); ok {
			spawnReq.DiskGB = disk
		}
		if gpus, ok := experiment.Metadata["gpus"].(int); ok {
			spawnReq.GPUs = gpus
		}
		if queue, ok := experiment.Metadata["queue"].(string); ok {
			spawnReq.Queue = queue
		}
		if priority, ok := experiment.Metadata["priority"].(int); ok {
			spawnReq.Priority = priority
		}
	}

	// Extract from experiment requirements if available
	if experiment.Requirements != nil {
		if experiment.Requirements.CPUCores > 0 {
			spawnReq.CPUCores = experiment.Requirements.CPUCores
		}
		if experiment.Requirements.MemoryMB > 0 {
			spawnReq.MemoryMB = experiment.Requirements.MemoryMB
		}
		if experiment.Requirements.DiskGB > 0 {
			spawnReq.DiskGB = experiment.Requirements.DiskGB
		}
		if experiment.Requirements.GPUs > 0 {
			spawnReq.GPUs = experiment.Requirements.GPUs
		}
	}

	spawnedWorker, err := s.compute.SpawnWorker(ctx, spawnReq)
	if err != nil {
		// Clean up worker record
		s.repo.DeleteWorker(ctx, workerID)
		return nil, fmt.Errorf("failed to spawn worker on compute resource: %w", err)
	}

	// Update worker with compute resource details
	worker.Status = domain.WorkerStatusIdle
	worker.UpdatedAt = time.Now()
	worker.Metadata["computeJobId"] = spawnedWorker.JobID
	worker.Metadata["nodeId"] = spawnedWorker.NodeID

	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		fmt.Printf("failed to update worker status: %v\n", err)
	}

	// Publish event
	event := domain.NewWorkerCreatedEvent(workerID, computeResourceID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish worker created event: %v\n", err)
	}

	return worker, nil
}

// RegisterWorker implements domain.WorkerLifecycle.RegisterWorker
func (s *WorkerService) RegisterWorker(ctx context.Context, worker *domain.Worker) error {
	// Update worker status to running
	worker.Status = domain.WorkerStatusIdle
	worker.StartedAt = &time.Time{}
	*worker.StartedAt = time.Now()
	worker.UpdatedAt = time.Now()

	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		return fmt.Errorf("failed to register worker: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent(worker.ID, "worker.started", "worker", worker.ID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish worker started event: %v\n", err)
	}

	return nil
}

// StartWorkerPolling implements domain.WorkerLifecycle.StartWorkerPolling
func (s *WorkerService) StartWorkerPolling(ctx context.Context, workerID string) error {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return domain.ErrWorkerNotFound
	}

	// Update worker status
	worker.Status = domain.WorkerStatusIdle
	worker.UpdatedAt = time.Now()

	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		return fmt.Errorf("failed to start worker polling: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent(workerID, "worker.polling_started", "worker", workerID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish worker polling started event: %v\n", err)
	}

	return nil
}

// StopWorkerPolling implements domain.WorkerLifecycle.StopWorkerPolling
func (s *WorkerService) StopWorkerPolling(ctx context.Context, workerID string) error {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return domain.ErrWorkerNotFound
	}

	// Update worker status
	worker.Status = domain.WorkerStatusIdle
	worker.UpdatedAt = time.Now()

	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		return fmt.Errorf("failed to stop worker polling: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent(workerID, "worker.polling_stopped", "worker", workerID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish worker polling stopped event: %v\n", err)
	}

	return nil
}

// TerminateWorker implements domain.WorkerLifecycle.TerminateWorker
func (s *WorkerService) TerminateWorker(ctx context.Context, workerID string, reason string) error {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return domain.ErrWorkerNotFound
	}

	// Terminate worker on compute resource
	if err := s.compute.TerminateWorker(ctx, workerID); err != nil {
		fmt.Printf("failed to terminate worker on compute resource: %v\n", err)
	}

	// Update worker status
	worker.Status = domain.WorkerStatusIdle
	worker.TerminatedAt = &time.Time{}
	*worker.TerminatedAt = time.Now()
	worker.UpdatedAt = time.Now()
	worker.Metadata["terminationReason"] = reason

	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		return fmt.Errorf("failed to terminate worker: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent(workerID, "worker.terminated", "worker", workerID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish worker terminated event: %v\n", err)
	}

	return nil
}

// SendHeartbeat implements domain.WorkerLifecycle.SendHeartbeat
func (s *WorkerService) SendHeartbeat(ctx context.Context, workerID string, metrics *domain.WorkerMetrics) error {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return domain.ErrWorkerNotFound
	}

	// Update worker heartbeat
	worker.LastHeartbeat = time.Now()
	worker.UpdatedAt = time.Now()

	// Update walltime remaining
	if metrics != nil {
		// Calculate walltime remaining based on metrics and elapsed time
		worker.WalltimeRemaining = worker.Walltime - time.Since(worker.CreatedAt)
	}

	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		return fmt.Errorf("failed to update worker heartbeat: %w", err)
	}

	// Publish event
	event := domain.NewWorkerHeartbeatEvent(workerID, metrics)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish worker heartbeat event: %v\n", err)
	}

	return nil
}

// GetWorkerMetrics implements domain.WorkerLifecycle.GetWorkerMetrics
func (s *WorkerService) GetWorkerMetrics(ctx context.Context, workerID string) (*domain.WorkerMetrics, error) {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return nil, fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return nil, domain.ErrWorkerNotFound
	}

	// Get worker status from compute resource
	status, err := s.compute.GetWorkerStatus(ctx, workerID)
	if err != nil {
		// Return default metrics if compute resource is unavailable
		return &domain.WorkerMetrics{
			WorkerID:            workerID,
			CPUUsagePercent:     0,
			MemoryUsagePercent:  0,
			TasksCompleted:      0,
			TasksFailed:         0,
			AverageTaskDuration: 0,
			LastTaskDuration:    0,
			Uptime:              time.Since(worker.CreatedAt),
			CustomMetrics:       make(map[string]string),
			Timestamp:           time.Now(),
		}, nil
	}

	// Convert to domain metrics
	metrics := &domain.WorkerMetrics{
		WorkerID:            workerID,
		CPUUsagePercent:     status.CPULoad,
		MemoryUsagePercent:  status.MemoryUsage,
		TasksCompleted:      status.TasksCompleted,
		TasksFailed:         status.TasksFailed,
		AverageTaskDuration: status.AverageTaskDuration,
		LastTaskDuration:    0, // Not available in WorkerStatus
		Uptime:              time.Since(worker.CreatedAt),
		CustomMetrics:       convertInterfaceMapToStringMap(status.Metadata),
		Timestamp:           time.Now(),
	}

	return metrics, nil
}

// CheckWalltimeRemaining implements domain.WorkerLifecycle.CheckWalltimeRemaining
func (s *WorkerService) CheckWalltimeRemaining(ctx context.Context, workerID string, estimatedDuration time.Duration) (bool, time.Duration, error) {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return false, 0, fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return false, 0, domain.ErrWorkerNotFound
	}

	// Calculate remaining walltime
	elapsed := time.Since(worker.CreatedAt)
	remaining := worker.Walltime - elapsed

	// Check if there's enough time for the estimated duration
	hasEnoughTime := remaining >= estimatedDuration

	return hasEnoughTime, remaining, nil
}

// ReuseWorker implements domain.WorkerLifecycle.ReuseWorker
func (s *WorkerService) ReuseWorker(ctx context.Context, workerID string, taskID string) error {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return domain.ErrWorkerNotFound
	}

	// Check if worker is idle
	if worker.Status != domain.WorkerStatusIdle {
		return domain.ErrWorkerUnavailable
	}

	// Update worker status
	worker.Status = domain.WorkerStatusBusy
	worker.CurrentTaskID = taskID
	worker.UpdatedAt = time.Now()

	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		return fmt.Errorf("failed to reuse worker: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent(workerID, "worker.reused", "worker", workerID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish worker reused event: %v\n", err)
	}

	return nil
}

// Helper methods

func (s *WorkerService) generateWorkerID(computeResourceID string, experimentID string) string {
	timestamp := time.Now().UnixNano()
	return fmt.Sprintf("worker_%s_%s_%d", computeResourceID, experimentID, timestamp)
}

// convertInterfaceMapToStringMap converts map[string]interface{} to map[string]string
func convertInterfaceMapToStringMap(interfaceMap map[string]interface{}) map[string]string {
	stringMap := make(map[string]string)
	for k, v := range interfaceMap {
		if str, ok := v.(string); ok {
			stringMap[k] = str
		} else {
			stringMap[k] = fmt.Sprintf("%v", v)
		}
	}
	return stringMap
}
