package services

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"math"
	"strings"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// SchedulerService implements the TaskScheduler interface
type SchedulerService struct {
	repo           ports.RepositoryPort
	events         ports.EventPort
	registry       domain.ResourceRegistry
	orchestrator   domain.ExperimentOrchestrator
	dataMover      domain.DataMover
	workerGRPC     domain.WorkerGRPCService
	stagingManager *StagingOperationManager
	vault          domain.CredentialVault
	stateManager   *StateManager
	// Background task assignment
	assignmentRunning bool
	assignmentStop    chan struct{}
}

// TaskQueuedHandler handles task.queued events for immediate task assignment
type TaskQueuedHandler struct {
	scheduler *SchedulerService
	handlerID string
}

// Handle processes task.queued events
func (h *TaskQueuedHandler) Handle(ctx context.Context, event *domain.DomainEvent) error {
	// Extract task ID from event data
	taskID, ok := event.Data["taskId"].(string)
	if !ok {
		return fmt.Errorf("invalid task ID in event")
	}

	// Process this specific task immediately
	return h.scheduler.processTask(ctx, taskID)
}

// GetEventType returns the event type this handler processes
func (h *TaskQueuedHandler) GetEventType() string {
	return domain.EventTypeTaskQueued
}

// GetHandlerID returns a unique handler ID
func (h *TaskQueuedHandler) GetHandlerID() string {
	if h.handlerID == "" {
		h.handlerID = "task-queued-handler"
	}
	return h.handlerID
}

// Compile-time interface verification
var _ domain.TaskScheduler = (*SchedulerService)(nil)

// NewSchedulerService creates a new TaskScheduler service
func NewSchedulerService(repo ports.RepositoryPort, events ports.EventPort, registry domain.ResourceRegistry, orchestrator domain.ExperimentOrchestrator, dataMover domain.DataMover, workerGRPC domain.WorkerGRPCService, stagingManager *StagingOperationManager, vault domain.CredentialVault, stateManager *StateManager) *SchedulerService {
	scheduler := &SchedulerService{
		repo:              repo,
		events:            events,
		registry:          registry,
		orchestrator:      orchestrator,
		dataMover:         dataMover,
		workerGRPC:        workerGRPC,
		stagingManager:    stagingManager,
		vault:             vault,
		stateManager:      stateManager,
		assignmentRunning: false,
		assignmentStop:    make(chan struct{}),
	}

	// Subscribe to task.queued events for immediate processing
	eventHandler := &TaskQueuedHandler{scheduler: scheduler}
	events.Subscribe(context.Background(), domain.EventTypeTaskQueued, eventHandler)

	return scheduler
}

// ScheduleExperiment implements domain.TaskScheduler.ScheduleExperiment
func (s *SchedulerService) ScheduleExperiment(ctx context.Context, experimentID string) (*domain.SchedulingPlan, error) {
	// Get experiment
	experiment, err := s.repo.GetExperimentByID(ctx, experimentID)
	if err != nil {
		return nil, fmt.Errorf("experiment not found: %w", err)
	}
	if experiment == nil {
		return nil, domain.ErrExperimentNotFound
	}

	// Get available compute resources
	computeResources, _, err := s.repo.ListComputeResources(ctx, &ports.ComputeResourceFilters{}, 1000, 0)
	if err != nil {
		return nil, fmt.Errorf("failed to get compute resources: %w", err)
	}

	if len(computeResources) == 0 {
		return nil, domain.ErrNoAvailableWorkers
	}

	// Calculate optimal distribution
	distribution, err := s.CalculateOptimalDistribution(ctx, experimentID)
	if err != nil {
		return nil, fmt.Errorf("failed to calculate optimal distribution: %w", err)
	}

	// Extract constraints from experiment metadata
	var constraints []string
	if experiment.Metadata != nil {
		if constraintsStr, exists := experiment.Metadata["constraints"]; exists {
			if constraintsList, ok := constraintsStr.([]string); ok {
				constraints = constraintsList
			} else if constraintsStr, ok := constraintsStr.(string); ok {
				// Parse comma-separated constraints
				constraints = strings.Split(constraintsStr, ",")
				for i, c := range constraints {
					constraints[i] = strings.TrimSpace(c)
				}
			}
		}

		// Add resource constraints
		if cpuReq, exists := experiment.Metadata["cpu_requirement"]; exists {
			if cpuStr, ok := cpuReq.(string); ok {
				constraints = append(constraints, fmt.Sprintf("cpu:%s", cpuStr))
			}
		}
		if memReq, exists := experiment.Metadata["memory_requirement"]; exists {
			if memStr, ok := memReq.(string); ok {
				constraints = append(constraints, fmt.Sprintf("memory:%s", memStr))
			}
		}
		if gpuReq, exists := experiment.Metadata["gpu_requirement"]; exists {
			if gpuStr, ok := gpuReq.(string); ok {
				constraints = append(constraints, fmt.Sprintf("gpu:%s", gpuStr))
			}
		}
	}

	// Create scheduling plan
	plan := &domain.SchedulingPlan{
		ExperimentID:       experimentID,
		WorkerDistribution: distribution.ResourceAllocation,
		EstimatedDuration:  distribution.EstimatedDuration,
		EstimatedCost:      distribution.EstimatedCost,
		Constraints:        constraints,
		Metadata:           make(map[string]interface{}),
	}

	// Get all tasks for this experiment and update their status to QUEUED
	tasks, _, err := s.repo.ListTasksByExperiment(ctx, experimentID, 1000, 0)
	if err != nil {
		return nil, fmt.Errorf("failed to get experiment tasks: %w", err)
	}

	// Update task statuses from CREATED to QUEUED and publish events
	fmt.Printf("ScheduleExperiment: found %d tasks for experiment %s\n", len(tasks), experimentID)
	for _, task := range tasks {
		fmt.Printf("ScheduleExperiment: task %s has status %s\n", task.ID, task.Status)
		if task.Status == domain.TaskStatusCreated {
			task.Status = domain.TaskStatusQueued
			task.UpdatedAt = time.Now()
			if err := s.repo.UpdateTask(ctx, task); err != nil {
				fmt.Printf("failed to update task %s status to QUEUED: %v\n", task.ID, err)
			} else {
				fmt.Printf("ScheduleExperiment: updated task %s status to QUEUED\n", task.ID)

				// Publish task.queued event for immediate processing
				event := domain.NewTaskQueuedEvent(task.ID, task.ExperimentID)
				if err := s.events.Publish(ctx, event); err != nil {
					fmt.Printf("Failed to publish task.queued event: %v\n", err)
				}

				// Verify the update was persisted by re-reading the task
				updatedTask, err := s.repo.GetTaskByID(ctx, task.ID)
				if err != nil {
					fmt.Printf("ScheduleExperiment: failed to verify task update: %v\n", err)
				} else {
					fmt.Printf("ScheduleExperiment: verified task %s has status %s in database\n", updatedTask.ID, updatedTask.Status)
				}
			}
		}
	}

	// Assign tasks to compute resources based on the scheduling plan
	fmt.Printf("ScheduleExperiment: assigning tasks to compute resources\n")
	for _, task := range tasks {
		if task.Status == domain.TaskStatusQueued {
			// Find the best compute resource for this task
			var bestResource *domain.ComputeResource
			bestScore := 0.0

			for _, resource := range computeResources {
				// Check if this resource has capacity in the plan
				if allocation, exists := distribution.ResourceAllocation[resource.ID]; exists && allocation > 0 {
					// Simple scoring based on resource type and availability
					score := 1.0
					if resource.Type == domain.ComputeResourceTypeSlurm {
						score = 0.8 // Prefer SLURM for compute-intensive tasks
					} else if resource.Type == domain.ComputeResourceTypeBareMetal {
						score = 1.0 // Bare metal is good for general tasks
					} else if resource.Type == domain.ComputeResourceTypeKubernetes {
						score = 1.2 // Kubernetes is more expensive
					}

					if score > bestScore {
						bestScore = score
						bestResource = resource
					}
				}
			}

			if bestResource != nil {
				// Assign task to compute resource
				if err := s.assignTaskToResource(ctx, task, bestResource); err != nil {
					fmt.Printf("ScheduleExperiment: failed to assign task %s to resource %s: %v\n", task.ID, bestResource.ID, err)
				} else {
					fmt.Printf("ScheduleExperiment: assigned task %s to compute resource %s\n", task.ID, bestResource.ID)
				}
			} else {
				fmt.Printf("ScheduleExperiment: no available compute resource for task %s\n", task.ID)
			}
		}
	}

	// Publish event
	event := domain.NewAuditEvent(experiment.OwnerID, "scheduling.plan.created", "experiment", experimentID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish scheduling plan created event: %v\n", err)
	}

	return plan, nil
}

// AssignTask implements domain.TaskScheduler.AssignTask
func (s *SchedulerService) AssignTask(ctx context.Context, workerID string) (*domain.Task, error) {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return nil, fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return nil, domain.ErrWorkerNotFound
	}

	// Check if worker is available (must be idle and have no current task)
	if worker.Status != domain.WorkerStatusIdle {
		return nil, domain.ErrWorkerUnavailable
	}
	if worker.CurrentTaskID != "" {
		return nil, fmt.Errorf("worker %s already has a task assigned: %s", workerID, worker.CurrentTaskID)
	}

	// Get tasks that are assigned to this compute resource but not yet assigned to a worker
	// These are tasks in RUNNING status (assigned to resource) with empty WorkerID
	tasks, _, err := s.repo.GetTasksByStatus(ctx, domain.TaskStatusRunning, 1000, 0)
	if err != nil {
		return nil, fmt.Errorf("failed to get running tasks: %w", err)
	}

	// Filter tasks for this experiment and compute resource, not yet assigned to worker
	var candidateTasks []*domain.Task
	for _, task := range tasks {
		if task.ExperimentID == worker.ExperimentID &&
			task.ComputeResourceID == worker.ComputeResourceID &&
			task.WorkerID == "" {
			candidateTasks = append(candidateTasks, task)
		}
	}

	if len(candidateTasks) == 0 {
		return nil, nil // No tasks available for this worker
	}

	// Score tasks using cost function that considers worker metrics and data locality
	bestTask := s.selectBestTaskByCost(ctx, candidateTasks, worker)

	// Atomically assign task to worker
	bestTask.Status = domain.TaskStatusQueued
	bestTask.WorkerID = workerID
	bestTask.UpdatedAt = time.Now()

	// Update worker status to busy and set current task
	worker.Status = domain.WorkerStatusBusy
	worker.CurrentTaskID = bestTask.ID
	worker.UpdatedAt = time.Now()

	// Save changes in transaction
	if err := s.repo.UpdateTask(ctx, bestTask); err != nil {
		return nil, fmt.Errorf("failed to update task: %w", err)
	}

	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		return nil, fmt.Errorf("failed to update worker: %w", err)
	}

	// Publish event
	event := domain.NewTaskAssignedEvent(bestTask.ID, workerID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish task assigned event: %v\n", err)
	}

	log.Printf("Assigned task %s to worker %s (worker now has 1 task)", bestTask.ID, workerID)
	return bestTask, nil
}

// AssignTaskWithStaging assigns a task to a worker with proactive data staging
func (s *SchedulerService) AssignTaskWithStaging(ctx context.Context, workerID string) (*domain.Task, error) {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return nil, fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return nil, domain.ErrWorkerNotFound
	}

	// Check if worker is available
	if worker.Status != domain.WorkerStatusIdle {
		return nil, domain.ErrWorkerUnavailable
	}

	// Get queued tasks for this worker's experiment
	tasks, _, err := s.repo.GetTasksByStatus(ctx, domain.TaskStatusQueued, 100, 0)
	if err != nil {
		return nil, fmt.Errorf("failed to get queued tasks: %w", err)
	}

	// Filter tasks for this experiment
	var availableTasks []*domain.Task
	for _, task := range tasks {
		if task.ExperimentID == worker.ExperimentID {
			availableTasks = append(availableTasks, task)
		}
	}

	if len(availableTasks) == 0 {
		return nil, nil // No tasks available
	}

	// Select the first available task (simple round-robin)
	task := availableTasks[0]

	// Update task status to staging
	task.Status = domain.TaskStatusDataStaging
	task.WorkerID = workerID
	task.ComputeResourceID = worker.ComputeResourceID
	task.UpdatedAt = time.Now()
	now := time.Now()
	task.StagingStartedAt = &now

	// Update worker status
	worker.Status = domain.WorkerStatusBusy
	worker.CurrentTaskID = task.ID
	worker.UpdatedAt = time.Now()

	// Save changes in transaction
	if err := s.repo.UpdateTask(ctx, task); err != nil {
		return nil, fmt.Errorf("failed to update task: %w", err)
	}

	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		return nil, fmt.Errorf("failed to update worker: %w", err)
	}

	// Begin proactive data staging
	stagingOp, err := s.dataMover.BeginProactiveStaging(ctx, task.ID, worker.ComputeResourceID, worker.UserID)
	if err != nil {
		// Rollback task status
		task.Status = domain.TaskStatusQueued
		task.WorkerID = ""
		task.ComputeResourceID = ""
		task.StagingStartedAt = nil
		s.repo.UpdateTask(ctx, task)

		worker.Status = domain.WorkerStatusIdle
		worker.CurrentTaskID = ""
		s.repo.UpdateWorker(ctx, worker)

		return nil, fmt.Errorf("failed to begin data staging: %w", err)
	}

	// Store staging operation ID in task metadata
	if task.Metadata == nil {
		task.Metadata = make(map[string]interface{})
	}
	task.Metadata["staging_operation_id"] = stagingOp.ID

	// Update task with staging operation ID
	if err := s.repo.UpdateTask(ctx, task); err != nil {
		return nil, fmt.Errorf("failed to update task with staging operation: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent(worker.UserID, "task.staging.started", "task", task.ID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish task staging started event: %v\n", err)
	}

	// Start monitoring staging progress using StagingOperationManager
	if s.stagingManager != nil {
		go s.stagingManager.MonitorStagingProgress(ctx, stagingOp.ID, func() error {
			return s.completeStagingAndAssignTask(ctx, task.ID, workerID)
		})
	} else {
		// Fallback to old method if staging manager is not available
		go s.monitorStagingProgress(ctx, task.ID, stagingOp.ID, workerID)
	}

	return task, nil
}

// monitorStagingProgress monitors the progress of data staging and assigns task when complete
func (s *SchedulerService) monitorStagingProgress(ctx context.Context, taskID, stagingOpID, workerID string) {
	// Poll staging operation status
	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()

	timeout := time.After(10 * time.Minute) // 10 minute timeout for staging

	for {
		select {
		case <-ctx.Done():
			return
		case <-timeout:
			// Staging timeout - mark task as failed
			s.handleStagingTimeout(ctx, taskID, workerID)
			return
		case <-ticker.C:
			// Check staging status
			// In a real implementation, this would check the staging operation status
			// For now, we'll simulate successful staging after a short delay
			time.Sleep(2 * time.Second) // Simulate staging time

			// Mark staging as complete and assign task to worker
			if err := s.completeStagingAndAssignTask(ctx, taskID, workerID); err != nil {
				fmt.Printf("Failed to complete staging and assign task: %v\n", err)
				return
			}
			return
		}
	}
}

// completeStagingAndAssignTask completes staging and sends task to worker via gRPC
func (s *SchedulerService) completeStagingAndAssignTask(ctx context.Context, taskID, workerID string) error {
	// Get task
	task, err := s.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		return fmt.Errorf("failed to get task: %w", err)
	}
	if task == nil {
		return fmt.Errorf("task not found: %s", taskID)
	}

	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("failed to get worker: %w", err)
	}
	if worker == nil {
		return fmt.Errorf("worker not found: %s", workerID)
	}

	// Use StateManager for task state transition
	now := time.Now()
	task.StagingCompletedAt = &now
	metadata := map[string]interface{}{
		"worker_id":            workerID,
		"staging_completed_at": now,
	}
	if err := s.stateManager.TransitionTaskState(ctx, taskID, task.Status, domain.TaskStatusQueued, metadata); err != nil {
		return fmt.Errorf("failed to transition task to queued: %w", err)
	}

	// Update task staging completion time
	task.UpdatedAt = now
	if err := s.repo.UpdateTask(ctx, task); err != nil {
		log.Printf("Failed to update task staging completion time: %v", err)
	}

	// Use StateManager for worker state transition
	workerMetadata := map[string]interface{}{
		"task_id": taskID,
		"reason":  "task_assigned",
	}
	if err := s.stateManager.TransitionWorkerState(ctx, workerID, worker.Status, domain.WorkerStatusBusy, workerMetadata); err != nil {
		log.Printf("Failed to transition worker to busy: %v", err)
	}

	// Note: In pull-based model, workers request tasks via heartbeat
	// No need to send task to worker - they will pull it when ready

	// Publish event
	event := domain.NewAuditEvent(worker.UserID, "task.assigned", "task", task.ID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish task assigned event: %v\n", err)
	}

	return nil
}

// handleStagingTimeout handles staging timeout
func (s *SchedulerService) handleStagingTimeout(ctx context.Context, taskID, workerID string) {
	// Get task
	task, err := s.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		fmt.Printf("Failed to get task for timeout handling: %v\n", err)
		return
	}
	if task == nil {
		return
	}

	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		fmt.Printf("Failed to get worker for timeout handling: %v\n", err)
		return
	}
	if worker == nil {
		return
	}

	// Mark task as failed
	task.Status = domain.TaskStatusFailed
	task.Error = "Data staging timeout"
	task.UpdatedAt = time.Now()

	// Reset worker status
	worker.Status = domain.WorkerStatusIdle
	worker.CurrentTaskID = ""
	worker.UpdatedAt = time.Now()

	// Save changes
	s.repo.UpdateTask(ctx, task)
	s.repo.UpdateWorker(ctx, worker)

	// Publish event
	event := domain.NewAuditEvent(worker.UserID, "task.staging.timeout", "task", task.ID)
	s.events.Publish(ctx, event)
}

// CompleteTask implements domain.TaskScheduler.CompleteTask
func (s *SchedulerService) CompleteTask(ctx context.Context, taskID string, workerID string, result *domain.TaskResult) error {
	// Get task
	task, err := s.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		return fmt.Errorf("task not found: %w", err)
	}
	if task == nil {
		return domain.ErrTaskNotFound
	}

	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return domain.ErrWorkerNotFound
	}

	// Validate task assignment
	if task.WorkerID != workerID {
		return domain.ErrTaskNotAssigned
	}

	// Use StateManager for task state transition
	metadata := map[string]interface{}{
		"worker_id": workerID,
		"result":    result,
	}
	if err := s.stateManager.TransitionTaskState(ctx, taskID, task.Status, domain.TaskStatusCompleted, metadata); err != nil {
		return fmt.Errorf("failed to transition task to completed: %w", err)
	}

	// Store result summary if provided
	if result != nil {
		resultJSON, _ := json.Marshal(result)
		task.ResultSummary = string(resultJSON)
		if err := s.repo.UpdateTask(ctx, task); err != nil {
			log.Printf("Failed to update task result summary: %v", err)
		}
	}

	// Use StateManager for worker state transition
	workerMetadata := map[string]interface{}{
		"task_id": taskID,
		"reason":  "task_completed",
	}
	if err := s.stateManager.TransitionWorkerState(ctx, workerID, worker.Status, domain.WorkerStatusIdle, workerMetadata); err != nil {
		log.Printf("Failed to transition worker to idle: %v", err)
	}

	// Check if experiment is complete
	if err := s.checkExperimentCompletion(ctx, task.ExperimentID); err != nil {
		fmt.Printf("failed to check experiment completion: %v\n", err)
	}

	// Publish event
	event := domain.NewTaskCompletedEvent(taskID, workerID, *task.Duration)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish task completed event: %v\n", err)
	}

	// Note: No automatic task assignment - workers will request tasks via pull-based model

	return nil
}

// FailTask implements domain.TaskScheduler.FailTask
func (s *SchedulerService) FailTask(ctx context.Context, taskID string, workerID string, errorMsg string) error {
	// Get task
	task, err := s.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		return fmt.Errorf("task not found: %w", err)
	}
	if task == nil {
		return domain.ErrTaskNotFound
	}

	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return domain.ErrWorkerNotFound
	}

	// Validate task assignment
	if task.WorkerID != workerID {
		return domain.ErrTaskNotAssigned
	}

	// Check retry logic and determine next state
	var nextTaskStatus domain.TaskStatus
	if task.RetryCount < task.MaxRetries {
		// Can retry - increment retry count and queue for retry
		task.RetryCount++
		nextTaskStatus = domain.TaskStatusQueued
		task.WorkerID = ""
		task.ComputeResourceID = ""
		task.Error = errorMsg
		task.CompletedAt = nil // Clear completion time for retry
	} else {
		// Task has exhausted retries
		nextTaskStatus = domain.TaskStatusFailed
		task.Error = errorMsg
		// Set completion time for permanent failure
		now := time.Now()
		task.CompletedAt = &now
	}

	// Use StateManager for task state transition
	metadata := map[string]interface{}{
		"worker_id":   workerID,
		"error":       errorMsg,
		"retry_count": task.RetryCount,
	}
	if err := s.stateManager.TransitionTaskState(ctx, taskID, task.Status, nextTaskStatus, metadata); err != nil {
		return fmt.Errorf("failed to transition task state: %w", err)
	}

	// Update task fields that aren't handled by StateManager
	task.Status = nextTaskStatus
	task.UpdatedAt = time.Now()
	if err := s.repo.UpdateTask(ctx, task); err != nil {
		log.Printf("Failed to update task retry information: %v", err)
	}

	// Use StateManager for worker state transition
	workerMetadata := map[string]interface{}{
		"task_id": taskID,
		"reason":  "task_failed",
	}
	if err := s.stateManager.TransitionWorkerState(ctx, workerID, worker.Status, domain.WorkerStatusIdle, workerMetadata); err != nil {
		log.Printf("Failed to transition worker to idle: %v", err)
	}

	// Publish event
	event := domain.NewAuditEvent(workerID, "task.failed", "task", taskID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish task failed event: %v\n", err)
	}

	// Note: No automatic task assignment - workers will request tasks via pull-based model

	return nil
}

// GetWorkerStatus implements domain.TaskScheduler.GetWorkerStatus
func (s *SchedulerService) GetWorkerStatus(ctx context.Context, workerID string) (*domain.WorkerStatusInfo, error) {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return nil, fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return nil, domain.ErrWorkerNotFound
	}

	// Get worker metrics
	metrics, err := s.GetWorkerMetrics(ctx, workerID)
	if err != nil {
		// Use default metrics if not available
		metrics = &domain.WorkerMetrics{
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
		}
	}

	// Create worker status
	status := &domain.WorkerStatusInfo{
		WorkerID:            worker.ID,
		ComputeResourceID:   worker.ComputeResourceID,
		Status:              worker.Status,
		CurrentTaskID:       worker.CurrentTaskID,
		TasksCompleted:      metrics.TasksCompleted,
		TasksFailed:         metrics.TasksFailed,
		AverageTaskDuration: metrics.AverageTaskDuration,
		WalltimeRemaining:   worker.WalltimeRemaining,
		LastHeartbeat:       worker.LastHeartbeat,
		Capabilities:        make(map[string]interface{}),
		Metadata:            worker.Metadata,
	}

	return status, nil
}

// UpdateWorkerMetrics implements domain.TaskScheduler.UpdateWorkerMetrics
func (s *SchedulerService) UpdateWorkerMetrics(ctx context.Context, workerID string, metrics *domain.WorkerMetrics) error {
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

	// Save worker
	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		return fmt.Errorf("failed to update worker: %w", err)
	}

	// Publish event
	event := domain.NewWorkerHeartbeatEvent(workerID, metrics)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish worker heartbeat event: %v\n", err)
	}

	return nil
}

// CalculateOptimalDistribution implements domain.TaskScheduler.CalculateOptimalDistribution
func (s *SchedulerService) CalculateOptimalDistribution(ctx context.Context, experimentID string) (*domain.WorkerDistribution, error) {
	// Get experiment
	experiment, err := s.repo.GetExperimentByID(ctx, experimentID)
	if err != nil {
		return nil, fmt.Errorf("experiment not found: %w", err)
	}
	if experiment == nil {
		return nil, domain.ErrExperimentNotFound
	}

	// Get available compute resources
	computeResources, _, err := s.repo.ListComputeResources(ctx, &ports.ComputeResourceFilters{}, 1000, 0)
	if err != nil {
		return nil, fmt.Errorf("failed to get compute resources: %w", err)
	}

	// Get tasks for this experiment
	tasks, _, err := s.repo.ListTasksByExperiment(ctx, experimentID, 1000, 0)
	if err != nil {
		return nil, fmt.Errorf("failed to get tasks: %w", err)
	}

	// Simple distribution algorithm (round-robin)
	resourceAllocation := make(map[string]int)
	totalWorkers := 0
	estimatedCost := 0.0
	estimatedDuration := time.Duration(0)

	if len(computeResources) > 0 && len(tasks) > 0 {
		// Distribute tasks evenly across available resources
		tasksPerResource := len(tasks) / len(computeResources)
		remainingTasks := len(tasks) % len(computeResources)

		for i, resource := range computeResources {
			workers := tasksPerResource
			if i < remainingTasks {
				workers++
			}
			if workers > resource.MaxWorkers {
				workers = resource.MaxWorkers
			}
			resourceAllocation[resource.ID] = workers
			totalWorkers += workers

			// Estimate cost and duration
			estimatedCost += float64(workers) * resource.CostPerHour * 1.0 // Assume 1 hour
			estimatedDuration = time.Hour                                  // Simple estimate
		}
	}

	distribution := &domain.WorkerDistribution{
		ExperimentID:       experimentID,
		ResourceAllocation: resourceAllocation,
		TotalWorkers:       totalWorkers,
		EstimatedCost:      estimatedCost,
		EstimatedDuration:  estimatedDuration,
		OptimizationWeights: &domain.CostWeights{
			TimeWeight:        0.5,
			CostWeight:        0.3,
			ReliabilityWeight: 0.2,
		},
		Metadata: make(map[string]interface{}),
	}

	return distribution, nil
}

// HandleWorkerFailure implements domain.TaskScheduler.HandleWorkerFailure
func (s *SchedulerService) HandleWorkerFailure(ctx context.Context, workerID string) error {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return domain.ErrWorkerNotFound
	}

	// If worker has a current task, reassign it
	if worker.CurrentTaskID != "" {
		task, err := s.repo.GetTaskByID(ctx, worker.CurrentTaskID)
		if err == nil && task != nil {
			// Requeue the task
			task.Status = domain.TaskStatusQueued
			task.WorkerID = ""
			task.ComputeResourceID = ""
			task.UpdatedAt = time.Now()

			if err := s.repo.UpdateTask(ctx, task); err != nil {
				fmt.Printf("failed to requeue task %s: %v\n", task.ID, err)
			}
		}
	}

	// Mark worker as failed
	worker.Status = domain.WorkerStatusIdle
	worker.CurrentTaskID = ""
	worker.UpdatedAt = time.Now()

	if err := s.repo.UpdateWorker(ctx, worker); err != nil {
		return fmt.Errorf("failed to update worker: %w", err)
	}

	// Publish event
	event := domain.NewAuditEvent(workerID, "worker.failed", "worker", workerID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish worker failed event: %v\n", err)
	}

	return nil
}

// GetWorkerMetrics implements domain.TaskScheduler.GetWorkerMetrics
func (s *SchedulerService) GetWorkerMetrics(ctx context.Context, workerID string) (*domain.WorkerMetrics, error) {
	// Get worker
	worker, err := s.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return nil, fmt.Errorf("worker not found: %w", err)
	}
	if worker == nil {
		return nil, domain.ErrWorkerNotFound
	}

	// Get tasks completed by this worker
	tasks, _, err := s.repo.GetTasksByWorker(ctx, workerID, 1000, 0)
	if err != nil {
		return nil, fmt.Errorf("failed to get worker tasks: %w", err)
	}

	// Calculate metrics
	completedTasks := 0
	failedTasks := 0
	var totalDuration time.Duration
	var lastTaskDuration time.Duration

	for _, task := range tasks {
		if task.Status == domain.TaskStatusCompleted {
			completedTasks++
			if task.Duration != nil {
				totalDuration += *task.Duration
				lastTaskDuration = *task.Duration
			}
		} else if task.Status == domain.TaskStatusFailed {
			failedTasks++
		}
	}

	var averageTaskDuration time.Duration
	if completedTasks > 0 {
		averageTaskDuration = totalDuration / time.Duration(completedTasks)
	}

	// Get latest worker metrics from database
	latestMetrics, err := s.repo.GetLatestWorkerMetrics(ctx, workerID)
	if err != nil {
		fmt.Printf("failed to get latest worker metrics: %v\n", err)
	}

	var cpuUsage, memoryUsage float64
	if latestMetrics != nil {
		cpuUsage = latestMetrics.CPUUsagePercent
		memoryUsage = latestMetrics.MemoryUsagePercent
	}

	metrics := &domain.WorkerMetrics{
		WorkerID:            workerID,
		CPUUsagePercent:     cpuUsage,
		MemoryUsagePercent:  memoryUsage,
		TasksCompleted:      completedTasks,
		TasksFailed:         failedTasks,
		AverageTaskDuration: averageTaskDuration,
		LastTaskDuration:    lastTaskDuration,
		Uptime:              time.Since(worker.CreatedAt),
		CustomMetrics:       make(map[string]string),
		Timestamp:           time.Now(),
	}

	return metrics, nil
}

// Helper methods

func (s *SchedulerService) checkExperimentCompletion(ctx context.Context, experimentID string) error {
	// Get all tasks for the experiment
	tasks, _, err := s.repo.ListTasksByExperiment(ctx, experimentID, 1000, 0)
	if err != nil {
		return fmt.Errorf("failed to get experiment tasks: %w", err)
	}

	// Check if all tasks are completed or failed
	allCompleted := true
	hasFailures := false

	for _, task := range tasks {
		if task.Status != domain.TaskStatusCompleted && task.Status != domain.TaskStatusFailed {
			allCompleted = false
			break
		}
		if task.Status == domain.TaskStatusFailed {
			hasFailures = true
		}
	}

	if allCompleted {
		// Update experiment status
		experiment, err := s.repo.GetExperimentByID(ctx, experimentID)
		if err != nil {
			return fmt.Errorf("failed to get experiment: %w", err)
		}

		if experiment.Status == domain.ExperimentStatusExecuting {
			var nextStatus domain.ExperimentStatus
			if hasFailures {
				nextStatus = domain.ExperimentStatusCanceled
			} else {
				nextStatus = domain.ExperimentStatusCompleted
			}

			// Use StateManager for experiment state transition
			metadata := map[string]interface{}{
				"has_failures": hasFailures,
				"task_count":   len(tasks),
			}
			if err := s.stateManager.TransitionExperimentState(ctx, experimentID, experiment.Status, nextStatus, metadata); err != nil {
				return fmt.Errorf("failed to transition experiment to completed: %w", err)
			}

			// Send shutdown commands to all workers associated with this experiment
			if err := s.shutdownExperimentWorkers(ctx, experimentID, hasFailures); err != nil {
				fmt.Printf("failed to shutdown experiment workers: %v\n", err)
			}

			// Publish event
			eventType := domain.EventTypeExperimentCompleted
			if hasFailures {
				eventType = domain.EventTypeExperimentFailed
			}
			event := &domain.DomainEvent{
				ID:        fmt.Sprintf("evt_%s_%d", experimentID, time.Now().UnixNano()),
				Type:      eventType,
				Source:    "task-scheduler",
				Timestamp: time.Now(),
				Data: map[string]interface{}{
					"experimentId": experimentID,
					"hasFailures":  hasFailures,
				},
			}
			if err := s.events.Publish(ctx, event); err != nil {
				fmt.Printf("failed to publish experiment completion event: %v\n", err)
			}
		}
	}

	return nil
}

// shutdownExperimentWorkers sends shutdown commands to all workers associated with an experiment
func (s *SchedulerService) shutdownExperimentWorkers(ctx context.Context, experimentID string, hasFailures bool) error {
	// Get all workers for this experiment
	workers, _, err := s.repo.ListWorkersByExperiment(ctx, experimentID, 1000, 0)
	if err != nil {
		return fmt.Errorf("failed to get workers for experiment %s: %w", experimentID, err)
	}

	if len(workers) == 0 {
		fmt.Printf("No workers found for experiment %s\n", experimentID)
		return nil
	}

	// Determine shutdown reason
	reason := "Experiment completed successfully"
	if hasFailures {
		reason = "Experiment completed with failures"
	}

	// Send shutdown command to each worker
	shutdownCount := 0
	for _, worker := range workers {
		if s.workerGRPC != nil {
			if err := s.workerGRPC.ShutdownWorker(worker.ID, reason, true); err != nil {
				fmt.Printf("Failed to shutdown worker %s: %v\n", worker.ID, err)
				continue
			}
			shutdownCount++
			fmt.Printf("Sent shutdown command to worker %s for experiment %s\n", worker.ID, experimentID)
		}
	}

	fmt.Printf("Sent shutdown commands to %d/%d workers for experiment %s\n", shutdownCount, len(workers), experimentID)
	return nil
}

// OnStagingComplete handles completion of data staging for a task
func (s *SchedulerService) OnStagingComplete(ctx context.Context, taskID string) error {
	// Get task
	task, err := s.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		return fmt.Errorf("failed to get task: %w", err)
	}
	if task == nil {
		return domain.ErrTaskNotFound
	}

	// Update task status from staging to queued
	if task.Status == domain.TaskStatusDataStaging {
		task.Status = domain.TaskStatusQueued
		task.UpdatedAt = time.Now()

		if err := s.repo.UpdateTask(ctx, task); err != nil {
			return fmt.Errorf("failed to update task status: %w", err)
		}

		// Publish event
		event := domain.NewAuditEvent("system", "task.staging.completed", "task", taskID)
		if err := s.events.Publish(ctx, event); err != nil {
			fmt.Printf("failed to publish staging completed event: %v\n", err)
		}
	}

	return nil
}

// Shutdown stops all background operations
func (s *SchedulerService) Shutdown(ctx context.Context) error {
	// Stop background task assignment
	if s.assignmentRunning {
		close(s.assignmentStop)
		s.assignmentRunning = false
	}

	if s.stagingManager != nil {
		return s.stagingManager.Shutdown(ctx)
	}
	return nil
}

// findBestComputeResource finds the best compute resource for a given task
func (s *SchedulerService) findBestComputeResource(task *domain.Task, resources []*domain.ComputeResource) *domain.ComputeResource {
	// Get experiment to check for preferred resource
	experiment, err := s.repo.GetExperimentByID(context.Background(), task.ExperimentID)
	if err != nil {
		return nil
	}

	// Check for preferred resource in experiment metadata
	if experiment.Metadata != nil {
		if preferredID, ok := experiment.Metadata["preferred_resource_id"].(string); ok {
			for _, resource := range resources {
				if resource.ID == preferredID {
					return resource
				}
			}
		}
	}

	// For now, return the first available resource
	// TODO: Implement more sophisticated resource matching based on requirements
	if len(resources) > 0 {
		return resources[0]
	}

	return nil
}

// assignTaskToResource assigns a task to a specific compute resource
func (s *SchedulerService) assignTaskToResource(ctx context.Context, task *domain.Task, resource *domain.ComputeResource) error {
	// Retrieve the latest version of the task from the database to preserve any metadata
	latestTask, err := s.repo.GetTaskByID(ctx, task.ID)
	if err != nil {
		return fmt.Errorf("failed to get latest task: %w", err)
	}

	// Update task status and assign to resource
	latestTask.Status = domain.TaskStatusRunning
	latestTask.ComputeResourceID = resource.ID
	latestTask.WorkerID = "" // Ensure WorkerID is empty so it can be assigned to a worker
	latestTask.StartedAt = &time.Time{}
	*latestTask.StartedAt = time.Now()
	latestTask.UpdatedAt = time.Now()

	// Save task changes
	if err := s.repo.UpdateTask(ctx, latestTask); err != nil {
		return fmt.Errorf("failed to update task: %w", err)
	}

	// Note: Task execution will be handled by existing mechanisms
	// The task is now assigned to a compute resource and ready for execution

	// Publish task.assigned event
	event := &domain.DomainEvent{
		ID:        fmt.Sprintf("task-assigned-%s", task.ID),
		Type:      domain.EventTypeTaskAssigned,
		Source:    "scheduler",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"taskId":            task.ID,
			"experimentId":      task.ExperimentID,
			"computeResourceId": resource.ID,
		},
	}
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish task.assigned event: %v\n", err)
	}

	return nil
}

// processTask processes a single task for assignment to a compute resource
func (s *SchedulerService) processTask(ctx context.Context, taskID string) error {
	// Get task from database
	task, err := s.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		return fmt.Errorf("failed to get task: %w", err)
	}

	// Verify status is QUEUED
	if task.Status != domain.TaskStatusQueued {
		return nil // Task is not queued, skip processing
	}

	// Skip if task already has a compute resource assigned
	if task.ComputeResourceID != "" {
		return nil // Task already assigned
	}

	// Get all available compute resources
	computeResources, _, err := s.repo.ListComputeResources(ctx, &ports.ComputeResourceFilters{}, 1000, 0)
	if err != nil {
		return fmt.Errorf("failed to get compute resources: %w", err)
	}

	if len(computeResources) == 0 {
		return nil // No compute resources available
	}

	// Find best compute resource for this task
	bestResource := s.findBestComputeResource(task, computeResources)
	if bestResource == nil {
		return nil // No suitable resource found
	}

	// Assign task to compute resource
	if err := s.assignTaskToResource(ctx, task, bestResource); err != nil {
		return fmt.Errorf("failed to assign task to resource: %w", err)
	}

	return nil
}

// ProvisionWorkerPool requests worker pool provisioning for an experiment
func (s *SchedulerService) ProvisionWorkerPool(ctx context.Context, experimentID string, plan *WorkerPoolPlan) error {
	// For each resource in the plan, create worker records
	for resourceID, workerCount := range plan.WorkersPerResource {
		resource, err := s.repo.GetComputeResourceByID(ctx, resourceID)
		if err != nil {
			return fmt.Errorf("failed to get resource %s: %w", resourceID, err)
		}

		// Get experiment to determine userID
		experiment, err := s.repo.GetExperimentByID(ctx, experimentID)
		if err != nil {
			return fmt.Errorf("failed to get experiment: %w", err)
		}

		// Create worker records (actual spawning happens asynchronously)
		for i := 0; i < workerCount; i++ {
			workerID := fmt.Sprintf("worker-%s-%s-%d", experimentID, resourceID, i)

			worker := &domain.Worker{
				ID:                workerID,
				ComputeResourceID: resourceID,
				ExperimentID:      experimentID,
				UserID:            experiment.OwnerID,
				Status:            domain.WorkerStatusIdle,
				Walltime:          30 * time.Minute, // Configurable
				WalltimeRemaining: 30 * time.Minute,
				CreatedAt:         time.Now(),
				UpdatedAt:         time.Now(),
				Metadata:          make(map[string]interface{}),
			}

			if err := s.repo.CreateWorker(ctx, worker); err != nil {
				return fmt.Errorf("failed to create worker: %w", err)
			}

			// Trigger worker spawning (implementation depends on compute resource type)
			if err := s.spawnWorker(ctx, worker, resource); err != nil {
				return fmt.Errorf("failed to spawn worker: %w", err)
			}
		}
	}

	return nil
}

// spawnWorker triggers actual worker process creation on compute resource
func (s *SchedulerService) spawnWorker(ctx context.Context, worker *domain.Worker, resource *domain.ComputeResource) error {
	// Generate worker launch script
	// This would submit a job that runs the worker binary
	// Implementation varies by compute resource type (SLURM sbatch, k8s pod, etc.)
	// For now, this is a placeholder - actual worker spawning would be handled by the compute resource

	return nil // Async operation
}

// selectBestTaskByLocality selects task with best data locality for worker
func (s *SchedulerService) selectBestTaskByLocality(ctx context.Context, tasks []*domain.Task, worker *domain.Worker) *domain.Task {
	// Get worker's compute resource location
	resource, err := s.repo.GetComputeResourceByID(ctx, worker.ComputeResourceID)
	if err != nil {
		return tasks[0] // Fallback to first task
	}

	bestTask := tasks[0]
	bestScore := 0.0

	for _, task := range tasks {
		score := 0.0

		// Score based on input file locations
		for _, inputFile := range task.InputFiles {
			// Simple heuristic: if path contains "s3" or "minio", assume it's on S3 storage
			// if path contains "nfs", assume it's on NFS storage
			var storageLocation string
			if strings.Contains(inputFile.Path, "s3") || strings.Contains(inputFile.Path, "minio") {
				storageLocation = "s3-storage"
			} else if strings.Contains(inputFile.Path, "nfs") {
				storageLocation = "nfs-storage"
			} else {
				storageLocation = "local-storage"
			}

			// Check if storage resource is co-located with compute resource
			if s.isColocated(resource.ID, storageLocation) {
				score += 1.0
			} else {
				// Penalize remote data
				score += 0.1
			}
		}

		if score > bestScore {
			bestScore = score
			bestTask = task
		}
	}

	return bestTask
}

// isColocated checks if compute and storage resources are co-located
func (s *SchedulerService) isColocated(computeResourceID, storageResourceID string) bool {
	// Check resource metadata for location/datacenter/region
	// For now, simple name-based heuristic
	return strings.Contains(computeResourceID, storageResourceID) ||
		strings.Contains(storageResourceID, computeResourceID)
}

// selectBestTaskByCost selects task using cost function that considers worker metrics and data locality
func (s *SchedulerService) selectBestTaskByCost(ctx context.Context, tasks []*domain.Task, worker *domain.Worker) *domain.Task {
	if len(tasks) == 0 {
		return nil
	}
	if len(tasks) == 1 {
		return tasks[0]
	}

	// Get worker metrics for performance-based scoring
	workerMetrics, err := s.GetWorkerMetrics(ctx, worker.ID)
	if err != nil {
		log.Printf("Failed to get worker metrics for %s: %v, falling back to locality-based selection", worker.ID, err)
		return s.selectBestTaskByLocality(ctx, tasks, worker)
	}

	// Get worker's compute resource for data locality scoring
	resource, err := s.repo.GetComputeResourceByID(ctx, worker.ComputeResourceID)
	if err != nil {
		log.Printf("Failed to get compute resource %s: %v", worker.ComputeResourceID, err)
		return tasks[0] // Fallback to first task
	}

	bestTask := tasks[0]
	bestScore := math.Inf(-1) // Start with negative infinity

	for _, task := range tasks {
		score := s.calculateTaskCost(ctx, task, worker, workerMetrics, resource)

		if score > bestScore {
			bestScore = score
			bestTask = task
		}
	}

	log.Printf("Selected task %s for worker %s with cost score %.3f", bestTask.ID, worker.ID, bestScore)
	return bestTask
}

// calculateTaskCost calculates the cost score for assigning a task to a worker
// Higher score = better assignment (lower cost)
func (s *SchedulerService) calculateTaskCost(ctx context.Context, task *domain.Task, worker *domain.Worker, metrics *domain.WorkerMetrics, resource *domain.ComputeResource) float64 {
	score := 0.0

	// 1. Data Locality Score (0.0 to 1.0)
	localityScore := s.calculateDataLocalityScore(task, resource)
	score += localityScore * 0.3 // 30% weight for data locality

	// 2. Worker Performance Score (0.0 to 1.0)
	performanceScore := s.calculateWorkerPerformanceScore(metrics)
	score += performanceScore * 0.4 // 40% weight for worker performance

	// 3. Resource Utilization Score (0.0 to 1.0)
	utilizationScore := s.calculateResourceUtilizationScore(metrics)
	score += utilizationScore * 0.2 // 20% weight for resource utilization

	// 4. Task Priority Score (0.0 to 1.0)
	priorityScore := s.calculateTaskPriorityScore(task)
	score += priorityScore * 0.1 // 10% weight for task priority

	return score
}

// calculateDataLocalityScore calculates score based on data locality
func (s *SchedulerService) calculateDataLocalityScore(task *domain.Task, resource *domain.ComputeResource) float64 {
	if len(task.InputFiles) == 0 {
		return 0.5 // Neutral score for tasks with no input files
	}

	score := 0.0
	totalFiles := len(task.InputFiles)

	for _, inputFile := range task.InputFiles {
		// Determine storage location from file path
		var storageLocation string
		if strings.Contains(inputFile.Path, "s3") || strings.Contains(inputFile.Path, "minio") {
			storageLocation = "s3-storage"
		} else if strings.Contains(inputFile.Path, "nfs") {
			storageLocation = "nfs-storage"
		} else {
			storageLocation = "local-storage"
		}

		// Check if storage resource is co-located with compute resource
		if s.isColocated(resource.ID, storageLocation) {
			score += 1.0
		} else {
			score += 0.3 // Partial score for remote storage
		}
	}

	return score / float64(totalFiles)
}

// calculateWorkerPerformanceScore calculates score based on worker's historical performance
func (s *SchedulerService) calculateWorkerPerformanceScore(metrics *domain.WorkerMetrics) float64 {
	if metrics.TasksCompleted == 0 && metrics.TasksFailed == 0 {
		return 0.5 // Neutral score for new workers
	}

	totalTasks := metrics.TasksCompleted + metrics.TasksFailed
	successRate := float64(metrics.TasksCompleted) / float64(totalTasks)

	// Consider average task duration (shorter is better)
	var durationScore float64 = 0.5 // Default neutral score
	if metrics.AverageTaskDuration > 0 {
		// Normalize duration score (assume 1 hour is average, scale accordingly)
		avgDurationHours := metrics.AverageTaskDuration.Hours()
		if avgDurationHours <= 0.5 {
			durationScore = 1.0 // Excellent
		} else if avgDurationHours <= 1.0 {
			durationScore = 0.8 // Good
		} else if avgDurationHours <= 2.0 {
			durationScore = 0.6 // Average
		} else {
			durationScore = 0.3 // Poor
		}
	}

	// Combine success rate (70%) and duration performance (30%)
	return (successRate * 0.7) + (durationScore * 0.3)
}

// calculateResourceUtilizationScore calculates score based on current resource utilization
func (s *SchedulerService) calculateResourceUtilizationScore(metrics *domain.WorkerMetrics) float64 {
	// Prefer workers with lower CPU and memory utilization
	cpuScore := 1.0 - (metrics.CPUUsagePercent / 100.0)
	memScore := 1.0 - (metrics.MemoryUsagePercent / 100.0)

	// Clamp scores to [0, 1] range
	if cpuScore < 0 {
		cpuScore = 0
	}
	if cpuScore > 1 {
		cpuScore = 1
	}
	if memScore < 0 {
		memScore = 0
	}
	if memScore > 1 {
		memScore = 1
	}

	// Average CPU and memory scores
	return (cpuScore + memScore) / 2.0
}

// calculateTaskPriorityScore calculates score based on task priority and age
func (s *SchedulerService) calculateTaskPriorityScore(task *domain.Task) float64 {
	// Base score from task priority (if available in metadata)
	baseScore := 0.5 // Default neutral score

	if priority, exists := task.Metadata["priority"]; exists {
		if priorityStr, ok := priority.(string); ok {
			switch strings.ToLower(priorityStr) {
			case "high", "urgent":
				baseScore = 1.0
			case "medium", "normal":
				baseScore = 0.7
			case "low":
				baseScore = 0.3
			}
		}
	}

	// Boost score for older tasks (starvation prevention)
	ageHours := time.Since(task.CreatedAt).Hours()
	ageBoost := math.Min(ageHours/24.0, 0.3) // Max 30% boost for tasks older than 24 hours

	return math.Min(baseScore+ageBoost, 1.0)
}
