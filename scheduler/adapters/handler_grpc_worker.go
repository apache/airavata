package adapters

import (
	"context"
	"fmt"
	"log"
	"sync"
	"time"

	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/durationpb"
	"google.golang.org/protobuf/types/known/timestamppb"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/core/dto"
	ports "github.com/apache/airavata/scheduler/core/port"
	services "github.com/apache/airavata/scheduler/core/service"
	types "github.com/apache/airavata/scheduler/core/util"
)

// WorkerGRPCService implements the WorkerService gRPC interface
type WorkerGRPCService struct {
	dto.UnimplementedWorkerServiceServer
	repo             ports.RepositoryPort
	scheduler        domain.TaskScheduler
	dataMover        domain.DataMover
	events           ports.EventPort
	websocketHandler *Hub
	connections      map[string]*WorkerConnection
	mu               sync.RWMutex
	healthTicker     *time.Ticker
	ctx              context.Context
	cancel           context.CancelFunc
	stateHooks       *domain.StateChangeHookRegistry
	stateManager     *services.StateManager
}

// WorkerConnection represents an active worker connection
type WorkerConnection struct {
	WorkerID          string
	ExperimentID      string
	ComputeResourceID string
	Stream            dto.WorkerService_PollForTaskServer
	LastHeartbeat     time.Time
	Status            dto.WorkerStatus
	CurrentTaskID     string
	Capabilities      *dto.WorkerCapabilities
	Metadata          map[string]string
	mu                sync.RWMutex
}

// NewWorkerGRPCService creates a new WorkerGRPCService
func NewWorkerGRPCService(
	repo ports.RepositoryPort,
	scheduler domain.TaskScheduler,
	dataMover domain.DataMover,
	events ports.EventPort,
	websocketHandler *Hub,
	stateManager *services.StateManager,
) *WorkerGRPCService {
	ctx, cancel := context.WithCancel(context.Background())
	service := &WorkerGRPCService{
		repo:             repo,
		scheduler:        scheduler,
		dataMover:        dataMover,
		events:           events,
		websocketHandler: websocketHandler,
		connections:      make(map[string]*WorkerConnection),
		ctx:              ctx,
		cancel:           cancel,
		stateHooks:       domain.NewStateChangeHookRegistry(),
		stateManager:     stateManager,
	}

	// Start health monitoring
	service.startHealthMonitor()

	return service
}

// SetScheduler updates the scheduler reference (for circular dependency resolution)
func (s *WorkerGRPCService) SetScheduler(scheduler domain.TaskScheduler) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.scheduler = scheduler
}

// RegisterStateChangeHook registers a state change hook
func (s *WorkerGRPCService) RegisterStateChangeHook(hook interface{}) {
	s.mu.Lock()
	defer s.mu.Unlock()

	if taskHook, ok := hook.(domain.TaskStateChangeHook); ok {
		s.stateHooks.RegisterTaskHook(taskHook)
	}
	if workerHook, ok := hook.(domain.WorkerStateChangeHook); ok {
		s.stateHooks.RegisterWorkerHook(workerHook)
	}
	if experimentHook, ok := hook.(domain.ExperimentStateChangeHook); ok {
		s.stateHooks.RegisterExperimentHook(experimentHook)
	}
}

// Note: AssignTask method removed - task assignment is now pull-based via handleTaskRequest

// RegisterWorker handles worker registration
func (s *WorkerGRPCService) RegisterWorker(
	ctx context.Context,
	req *dto.WorkerRegistrationRequest,
) (*dto.WorkerRegistrationResponse, error) {
	log.Printf("Worker registration request: %s", req.WorkerId)

	// Validate request
	if req.WorkerId == "" {
		return &dto.WorkerRegistrationResponse{
			Success: false,
			Message: "Worker ID is required",
		}, status.Error(codes.InvalidArgument, "worker ID is required")
	}

	if req.ExperimentId == "" {
		return &dto.WorkerRegistrationResponse{
			Success: false,
			Message: "Experiment ID is required",
		}, status.Error(codes.InvalidArgument, "experiment ID is required")
	}

	if req.ComputeResourceId == "" {
		return &dto.WorkerRegistrationResponse{
			Success: false,
			Message: "Compute resource ID is required",
		}, status.Error(codes.InvalidArgument, "compute resource ID is required")
	}

	// Get worker from database
	workerRecord, err := s.repo.GetWorkerByID(ctx, req.WorkerId)
	if err != nil {
		return &dto.WorkerRegistrationResponse{
			Success: false,
			Message: "Worker not found",
		}, status.Error(codes.NotFound, "worker not found")
	}

	if workerRecord == nil {
		return &dto.WorkerRegistrationResponse{
			Success: false,
			Message: "Worker not found",
		}, status.Error(codes.NotFound, "worker not found")
	}

	// Validate experiment and compute resource match
	if workerRecord.ExperimentID != req.ExperimentId {
		return &dto.WorkerRegistrationResponse{
			Success: false,
			Message: "Experiment ID mismatch",
		}, status.Error(codes.InvalidArgument, "experiment ID mismatch")
	}

	if workerRecord.ComputeResourceID != req.ComputeResourceId {
		return &dto.WorkerRegistrationResponse{
			Success: false,
			Message: "Compute resource ID mismatch",
		}, status.Error(codes.InvalidArgument, "compute resource ID mismatch")
	}

	// Update worker status to idle and set connection state
	workerRecord.Status = domain.WorkerStatusIdle
	workerRecord.ConnectionState = "CONNECTED"
	workerRecord.LastHeartbeat = time.Now()
	now := time.Now()
	workerRecord.LastSeenAt = &now
	workerRecord.UpdatedAt = time.Now()

	if err := s.repo.UpdateWorker(ctx, workerRecord); err != nil {
		return &dto.WorkerRegistrationResponse{
			Success: false,
			Message: "Failed to update worker status",
		}, status.Error(codes.Internal, "failed to update worker status")
	}

	// Create worker configuration
	config := &dto.WorkerConfig{
		WorkerId:          req.WorkerId,
		HeartbeatInterval: &durationpb.Duration{Seconds: 30}, // 30 seconds
		TaskTimeout:       &durationpb.Duration{Seconds: int64(workerRecord.Walltime.Seconds())},
		WorkingDirectory:  "/tmp/worker",
		Environment: map[string]string{
			"WORKER_ID":           req.WorkerId,
			"EXPERIMENT_ID":       req.ExperimentId,
			"COMPUTE_RESOURCE_ID": req.ComputeResourceId,
		},
		Metadata: map[string]string{
			"registered_at": time.Now().Format(time.RFC3339),
		},
	}

	// Publish event
	event := domain.NewAuditEvent(req.WorkerId, "dto.registered", "worker", req.WorkerId)
	if err := s.events.Publish(ctx, event); err != nil {
		log.Printf("Failed to publish worker registered event: %v", err)
	}

	log.Printf("Worker %s registered successfully", req.WorkerId)

	return &dto.WorkerRegistrationResponse{
		Success: true,
		Message: "Worker registered successfully",
		Config:  config,
		Validation: &dto.ValidationResult{
			Valid:    true,
			Errors:   []*dto.Error{},
			Warnings: []string{},
		},
	}, nil
}

// PollForTask handles bidirectional streaming for task polling
// This implements a pull-based model where workers request tasks
func (s *WorkerGRPCService) PollForTask(stream dto.WorkerService_PollForTaskServer) error {
	var workerConn *WorkerConnection
	var workerID string

	// Handle incoming messages from worker
	for {
		msg, err := stream.Recv()
		if err != nil {
			if workerConn != nil {
				log.Printf("Worker %s disconnected: %v", workerID, err)
				s.removeWorkerConnection(workerID)
			}
			return err
		}

		// Handle different message types
		switch m := msg.Message.(type) {
		case *dto.WorkerMessage_Heartbeat:
			// Heartbeat is ONLY for health monitoring
			workerConn = s.handleHeartbeat(stream, m.Heartbeat)
			if workerConn != nil {
				workerID = workerConn.WorkerID
			}
		case *dto.WorkerMessage_TaskRequest:
			// TaskRequest is ONLY for requesting tasks
			s.handleTaskRequest(stream, m.TaskRequest)
		case *dto.WorkerMessage_TaskStatus:
			s.handleTaskStatus(context.Background(), m.TaskStatus)
		case *dto.WorkerMessage_TaskOutput:
			s.handleTaskOutput(context.Background(), m.TaskOutput)
		case *dto.WorkerMessage_WorkerMetrics:
			s.handleWorkerMetrics(context.Background(), m.WorkerMetrics)
		case *dto.WorkerMessage_StagingStatus:
			s.handleStagingStatus(context.Background(), m.StagingStatus)
		default:
			log.Printf("Unknown worker message type: %T", msg.Message)
		}
	}
}

// ReportTaskStatus handles task status updates
func (s *WorkerGRPCService) ReportTaskStatus(
	ctx context.Context,
	req *dto.TaskStatusUpdateRequest,
) (*dto.TaskStatusUpdateResponse, error) {
	log.Printf("Task status update: %s - %s", req.TaskId, req.Status)

	// Get task from database
	task, err := s.repo.GetTaskByID(ctx, req.TaskId)
	if err != nil {
		return &dto.TaskStatusUpdateResponse{
			Success: false,
			Message: "Task not found",
		}, status.Error(codes.NotFound, "task not found")
	}

	if task == nil {
		return &dto.TaskStatusUpdateResponse{
			Success: false,
			Message: "Task not found",
		}, status.Error(codes.NotFound, "task not found")
	}

	// Validate worker assignment
	if task.WorkerID != req.WorkerId {
		return &dto.TaskStatusUpdateResponse{
			Success: false,
			Message: "Worker not assigned to this task",
		}, status.Error(codes.PermissionDenied, "worker not assigned to this task")
	}

	// Convert protobuf status to domain status
	var newStatus domain.TaskStatus
	switch req.Status {
	case dto.TaskStatus_TASK_STATUS_QUEUED:
		newStatus = domain.TaskStatusQueued
	case dto.TaskStatus_TASK_STATUS_DATA_STAGING:
		newStatus = domain.TaskStatusDataStaging
	case dto.TaskStatus_TASK_STATUS_ENV_SETUP:
		newStatus = domain.TaskStatusEnvSetup
	case dto.TaskStatus_TASK_STATUS_RUNNING:
		newStatus = domain.TaskStatusRunning
	case dto.TaskStatus_TASK_STATUS_OUTPUT_STAGING:
		newStatus = domain.TaskStatusOutputStaging
	case dto.TaskStatus_TASK_STATUS_COMPLETED:
		newStatus = domain.TaskStatusCompleted
	case dto.TaskStatus_TASK_STATUS_FAILED:
		newStatus = domain.TaskStatusFailed
	case dto.TaskStatus_TASK_STATUS_CANCELLED:
		newStatus = domain.TaskStatusCanceled
	default:
		return &dto.TaskStatusUpdateResponse{
			Success: false,
			Message: "Invalid task status",
		}, status.Error(codes.InvalidArgument, "invalid task status")
	}

	// Use StateManager for transactional state transition
	metadata := map[string]interface{}{
		"worker_id": req.WorkerId,
		"message":   req.Message,
		"errors":    req.Errors,
	}
	if req.Metrics != nil {
		metadata["metrics"] = req.Metrics
	}
	// Include request metadata (like work_dir) in the state transition
	for key, value := range req.Metadata {
		metadata[key] = value
	}

	if err := s.stateManager.TransitionTaskState(ctx, task.ID, task.Status, newStatus, metadata); err != nil {
		return &dto.TaskStatusUpdateResponse{
			Success: false,
			Message: fmt.Sprintf("Failed to transition task state: %v", err),
		}, status.Error(codes.Internal, "failed to transition task state")
	}

	// Handle special cases after successful state transition
	if req.Status == dto.TaskStatus_TASK_STATUS_FAILED {
		// Generate signed URLs for output upload
		uploadURLs, err := s.dataMover.GenerateUploadURLsForTask(ctx, task.ID)
		if err != nil {
			log.Printf("Failed to generate upload URLs for task %s: %v", task.ID, err)
		} else if len(uploadURLs) > 0 {
			// Send upload URLs to worker
			msg := &dto.ServerMessage{
				Message: &dto.ServerMessage_OutputUploadRequest{
					OutputUploadRequest: &dto.OutputUploadRequest{
						TaskId:     task.ID,
						UploadUrls: convertToProtoSignedURLs(uploadURLs),
					},
				},
			}

			workerConn := s.getWorkerConnection(req.WorkerId)
			if workerConn != nil {
				workerConn.mu.Lock()
				if err := workerConn.Stream.Send(msg); err != nil {
					log.Printf("Failed to send upload URLs to worker %s: %v", req.WorkerId, err)
				}
				workerConn.mu.Unlock()
			}
		}
	}

	// Store task result summary if provided
	if req.Metrics != nil {
		// Store task execution metrics in database
		metrics := &domain.TaskMetrics{
			TaskID:           req.TaskId,
			CPUUsagePercent:  float64(req.Metrics.CpuUsagePercent),
			MemoryUsageBytes: int64(req.Metrics.MemoryUsagePercent * float32(1024*1024)), // Convert % to bytes
			DiskUsageBytes:   req.Metrics.DiskUsageBytes,
			Timestamp:        time.Now(),
		}
		if err := s.repo.CreateTaskMetrics(ctx, metrics); err != nil {
			log.Printf("Failed to store task metrics: %v", err)
		}
	}

	// Update worker status in database and connection for terminal task states
	if req.Status == dto.TaskStatus_TASK_STATUS_COMPLETED ||
		req.Status == dto.TaskStatus_TASK_STATUS_FAILED ||
		req.Status == dto.TaskStatus_TASK_STATUS_CANCELLED {

		// Get current worker status
		worker, err := s.repo.GetWorkerByID(ctx, req.WorkerId)
		if err == nil && worker != nil {
			// Use StateManager for worker state transition
			workerMetadata := map[string]interface{}{
				"task_id": req.TaskId,
				"reason":  "task_completed",
			}
			if err := s.stateManager.TransitionWorkerState(ctx, req.WorkerId, worker.Status, domain.WorkerStatusIdle, workerMetadata); err != nil {
				log.Printf("Failed to transition worker %s to IDLE status: %v", req.WorkerId, err)
			} else {
				log.Printf("Updated worker %s to IDLE status after task %s completion", req.WorkerId, req.TaskId)
			}
		}

		// Update worker connection
		if workerConn := s.getWorkerConnection(req.WorkerId); workerConn != nil {
			workerConn.mu.Lock()
			workerConn.Status = dto.WorkerStatus_WORKER_STATUS_IDLE
			workerConn.CurrentTaskID = ""
			workerConn.mu.Unlock()
		}
	}

	// Publish event
	eventType := "task.status.updated"
	if req.Status == dto.TaskStatus_TASK_STATUS_COMPLETED {
		eventType = "task.completed"
	} else if req.Status == dto.TaskStatus_TASK_STATUS_FAILED {
		eventType = "task.failed"
	}

	event := domain.NewAuditEvent(req.WorkerId, eventType, "task", req.TaskId)
	if err := s.events.Publish(ctx, event); err != nil {
		log.Printf("Failed to publish task status event: %v", err)
	}

	// If task completed, trigger output data staging and check experiment completion
	if req.Status == dto.TaskStatus_TASK_STATUS_COMPLETED {
		go s.stageOutputData(ctx, task, req.WorkerId)

		// Check if experiment is complete and shutdown workers if needed
		if s.scheduler != nil {
			if err := s.scheduler.CompleteTask(ctx, req.TaskId, req.WorkerId, nil); err != nil {
				log.Printf("Failed to complete task in scheduler: %v", err)
			}
		}
	}

	return &dto.TaskStatusUpdateResponse{
		Success: true,
		Message: "Task status updated successfully",
	}, nil
}

// SendHeartbeat handles heartbeat messages
func (s *WorkerGRPCService) SendHeartbeat(
	ctx context.Context,
	req *dto.HeartbeatRequest,
) (*dto.HeartbeatResponse, error) {
	// Update worker connection heartbeat
	if workerConn := s.getWorkerConnection(req.WorkerId); workerConn != nil {
		workerConn.mu.Lock()
		workerConn.LastHeartbeat = time.Now()
		workerConn.Status = req.Status
		workerConn.CurrentTaskID = req.CurrentTaskId
		workerConn.mu.Unlock()
	}

	// Update worker in database
	workerRecord, err := s.repo.GetWorkerByID(ctx, req.WorkerId)
	if err == nil && workerRecord != nil {
		workerRecord.LastHeartbeat = time.Now()
		now := time.Now()
		workerRecord.LastSeenAt = &now
		workerRecord.UpdatedAt = time.Now()
		s.repo.UpdateWorker(ctx, workerRecord)
	}

	return &dto.HeartbeatResponse{
		Success:    true,
		Message:    "Heartbeat received",
		ServerTime: timestamppb.Now(),
		Metadata:   map[string]string{},
	}, nil
}

// RequestDataStaging handles data staging requests
func (s *WorkerGRPCService) RequestDataStaging(
	ctx context.Context,
	req *dto.WorkerDataStagingRequest,
) (*dto.WorkerDataStagingResponse, error) {
	log.Printf("Data staging request: %s", req.TaskId)

	// Get task from database
	task, err := s.repo.GetTaskByID(ctx, req.TaskId)
	if err != nil {
		return &dto.WorkerDataStagingResponse{
			Success: false,
			Message: "Task not found",
		}, status.Error(codes.NotFound, "task not found")
	}

	if task == nil {
		return &dto.WorkerDataStagingResponse{
			Success: false,
			Message: "Task not found",
		}, status.Error(codes.NotFound, "task not found")
	}

	// Begin proactive data staging
	stagingOp, err := s.dataMover.BeginProactiveStaging(ctx, req.TaskId, req.ComputeResourceId, req.WorkerId)
	if err != nil {
		return &dto.WorkerDataStagingResponse{
			Success: false,
			Message: fmt.Sprintf("Failed to begin data staging: %v", err),
		}, status.Error(codes.Internal, "failed to begin data staging")
	}

	return &dto.WorkerDataStagingResponse{
		StagingId:   stagingOp.ID,
		Success:     true,
		Message:     "Data staging started",
		StagedFiles: []string{},
		FailedFiles: []string{},
		Validation:  &dto.ValidationResult{Valid: true},
	}, nil
}

// Helper methods

func (s *WorkerGRPCService) handleHeartbeat(stream dto.WorkerService_PollForTaskServer, heartbeat *dto.Heartbeat) *WorkerConnection {
	workerConn := s.getWorkerConnection(heartbeat.WorkerId)
	if workerConn == nil {
		// Create new connection
		workerConn = &WorkerConnection{
			WorkerID:      heartbeat.WorkerId,
			Stream:        stream,
			LastHeartbeat: time.Now(),
			Status:        heartbeat.Status,
			CurrentTaskID: heartbeat.CurrentTaskId,
			Metadata:      heartbeat.Metadata,
		}
		s.addWorkerConnection(workerConn)
	} else {
		// Update existing connection
		workerConn.mu.Lock()
		workerConn.Stream = stream
		workerConn.LastHeartbeat = time.Now()
		workerConn.Status = heartbeat.Status
		workerConn.CurrentTaskID = heartbeat.CurrentTaskId
		workerConn.mu.Unlock()
	}

	// Update worker status in database to match heartbeat
	worker, err := s.repo.GetWorkerByID(context.Background(), heartbeat.WorkerId)
	if err == nil && worker != nil {
		// Convert protobuf status to domain status
		var domainStatus domain.WorkerStatus
		switch heartbeat.Status {
		case dto.WorkerStatus_WORKER_STATUS_IDLE:
			domainStatus = domain.WorkerStatusIdle
		case dto.WorkerStatus_WORKER_STATUS_BUSY:
			domainStatus = domain.WorkerStatusBusy
		case dto.WorkerStatus_WORKER_STATUS_STAGING:
			domainStatus = domain.WorkerStatusBusy // Map staging to busy
		case dto.WorkerStatus_WORKER_STATUS_ERROR:
			domainStatus = domain.WorkerStatusIdle // Map error to idle for retry
		default:
			domainStatus = domain.WorkerStatusIdle
		}

		// Only update if status has changed
		if worker.Status != domainStatus {
			worker.Status = domainStatus
			worker.LastHeartbeat = time.Now()
			worker.UpdatedAt = time.Now()
			if err := s.repo.UpdateWorker(context.Background(), worker); err != nil {
				log.Printf("Failed to update worker status in database: %v", err)
			}
		}
	}

	// Heartbeat is ONLY for health monitoring - no task assignment logic here

	return workerConn
}

// handleTaskRequest handles a worker's explicit request for a task
func (s *WorkerGRPCService) handleTaskRequest(stream dto.WorkerService_PollForTaskServer, request *dto.TaskRequest) {
	log.Printf("Worker %s requesting a task for experiment %s", request.WorkerId, request.ExperimentId)

	// Get worker from database to verify status
	worker, err := s.repo.GetWorkerByID(context.Background(), request.WorkerId)
	if err != nil {
		log.Printf("Failed to get worker %s: %v", request.WorkerId, err)
		s.sendNoTaskAvailable(stream, "Worker not found")
		return
	}
	if worker == nil {
		log.Printf("Worker %s not found in database", request.WorkerId)
		s.sendNoTaskAvailable(stream, "Worker not found")
		return
	}

	// Check if worker is available (must be idle and have no current task)
	if worker.Status != domain.WorkerStatusIdle {
		log.Printf("Worker %s is not idle (status: %s)", request.WorkerId, worker.Status)
		s.sendNoTaskAvailable(stream, "Worker not idle")
		return
	}
	if worker.CurrentTaskID != "" {
		log.Printf("Worker %s already has a task assigned: %s", request.WorkerId, worker.CurrentTaskID)
		s.sendNoTaskAvailable(stream, "Worker already has a task")
		return
	}

	// Try to assign a task using the scheduler
	task, err := s.scheduler.AssignTask(context.Background(), request.WorkerId)
	if err != nil {
		log.Printf("Failed to assign task to worker %s: %v", request.WorkerId, err)
		s.sendNoTaskAvailable(stream, "Failed to assign task")
		return
	}
	if task == nil {
		// No tasks available - tell worker to self-destruct
		log.Printf("No tasks available for worker %s - requesting self-destruction", request.WorkerId)
		s.sendWorkerShutdown(stream, "No tasks available")
		return
	}

	// Send task assignment to worker
	assignment := &domain.TaskAssignment{
		TaskId:          task.ID,
		ExperimentId:    task.ExperimentID,
		Command:         task.Command,
		ExecutionScript: task.ExecutionScript,
		InputFiles:      task.InputFiles,
		OutputFiles:     task.OutputFiles,
		Metadata:        task.Metadata,
	}

	if err := s.sendTaskAssignment(stream, assignment); err != nil {
		log.Printf("Failed to send task assignment to worker %s: %v", request.WorkerId, err)
		return
	}

	log.Printf("Assigned task %s to worker %s", task.ID, request.WorkerId)
}

// handleTaskRequestViaHeartbeat handles a worker's request for a task via heartbeat (pull-based assignment) - DEPRECATED
func (s *WorkerGRPCService) handleTaskRequestViaHeartbeat(workerID string, stream dto.WorkerService_PollForTaskServer) {
	// Get worker from database to verify status
	worker, err := s.repo.GetWorkerByID(context.Background(), workerID)
	if err != nil {
		log.Printf("Failed to get worker %s: %v", workerID, err)
		s.sendNoTaskAvailable(stream, "Worker not found")
		return
	}
	if worker == nil {
		log.Printf("Worker %s not found in database", workerID)
		s.sendNoTaskAvailable(stream, "Worker not found")
		return
	}

	// Check if worker is available (must be idle and have no current task)
	if worker.Status != domain.WorkerStatusIdle {
		log.Printf("Worker %s is not idle (status: %s)", workerID, worker.Status)
		s.sendNoTaskAvailable(stream, "Worker not idle")
		return
	}
	if worker.CurrentTaskID != "" {
		log.Printf("Worker %s already has a task assigned: %s", workerID, worker.CurrentTaskID)
		s.sendNoTaskAvailable(stream, "Worker already has a task")
		return
	}

	// Worker is actually requesting a task - log this
	log.Printf("Worker %s requesting a task via heartbeat", workerID)

	// Try to assign a task using the scheduler
	task, err := s.scheduler.AssignTask(context.Background(), workerID)
	if err != nil {
		log.Printf("Failed to assign task to worker %s: %v", workerID, err)
		s.sendNoTaskAvailable(stream, "Failed to assign task")
		return
	}
	if task == nil {
		// No tasks available - tell worker to self-destruct
		log.Printf("No tasks available for worker %s - requesting self-destruction", workerID)
		s.sendWorkerShutdown(stream, "No tasks available")
		return
	}

	log.Printf("Assigned task %s to worker %s (worker now has 1 task)", task.ID, workerID)

	// Create task assignment message
	assignment := &domain.TaskAssignment{
		TaskId:          task.ID,
		ExperimentId:    task.ExperimentID,
		Command:         task.Command,
		ExecutionScript: task.ExecutionScript,
		Dependencies:    []string{}, // TODO: Extract from task metadata
		InputFiles:      task.InputFiles,
		OutputFiles:     task.OutputFiles,
		Environment:     make(map[string]string),
		Timeout:         time.Hour, // Default timeout
		Metadata:        task.Metadata,
	}

	// Send task assignment to worker
	if err := s.sendTaskAssignment(stream, assignment); err != nil {
		log.Printf("Failed to send task %s to worker %s: %v", task.ID, workerID, err)
		// TODO: Rollback task assignment
	}
}

// sendTaskAssignment sends a task assignment to the worker
func (s *WorkerGRPCService) sendTaskAssignment(stream dto.WorkerService_PollForTaskServer, assignment *domain.TaskAssignment) error {
	// Convert to protobuf message
	protoAssignment := &dto.TaskAssignment{
		TaskId:          assignment.TaskId,
		ExperimentId:    assignment.ExperimentId,
		Command:         assignment.Command,
		ExecutionScript: assignment.ExecutionScript,
		Dependencies:    assignment.Dependencies,
		Environment:     assignment.Environment,
		Timeout:         &durationpb.Duration{Seconds: int64(assignment.Timeout.Seconds())},
		Metadata:        convertToStringMap(assignment.Metadata),
	}

	msg := &dto.ServerMessage{
		Message: &dto.ServerMessage_TaskAssignment{
			TaskAssignment: protoAssignment,
		},
	}

	return stream.Send(msg)
}

// sendNoTaskAvailable tells the worker that no tasks are available
// For now, we'll use a simple log message since NoTaskAvailable message type is not available
func (s *WorkerGRPCService) sendNoTaskAvailable(stream dto.WorkerService_PollForTaskServer, reason string) {
	log.Printf("No task available for worker: %s", reason)
	// In a real implementation, we would send a specific message type
}

// sendWorkerShutdown tells the worker to self-destruct because no tasks are available
func (s *WorkerGRPCService) sendWorkerShutdown(stream dto.WorkerService_PollForTaskServer, reason string) {
	msg := &dto.ServerMessage{
		Message: &dto.ServerMessage_WorkerShutdown{
			WorkerShutdown: &dto.WorkerShutdown{
				WorkerId: "", // Will be set by worker
				Reason:   reason,
				Graceful: true,
				Timeout:  &durationpb.Duration{Seconds: 30}, // 30 seconds grace period
			},
		},
	}
	stream.Send(msg)
}

// Note: tryAssignTaskToWorker method removed - task assignment is now pull-based

func (s *WorkerGRPCService) handleTaskStatus(ctx context.Context, status *dto.TaskStatusUpdateRequest) {
	// This is handled by the ReportTaskStatus method
	// We can add additional logic here if needed
}

func (s *WorkerGRPCService) handleTaskOutput(ctx context.Context, output *dto.TaskOutput) {
	// Stream task output to WebSocket clients
	// Broadcast task output to WebSocket clients subscribed to this task
	if s.websocketHandler != nil {
		s.websocketHandler.BroadcastTaskUpdate(output.TaskId, "", types.WebSocketMessageTypeTaskProgress, output.Data)
	}

	// Handle different output types with appropriate logging
	switch output.Type {
	case dto.OutputType_OUTPUT_TYPE_LOG:
		// Worker log messages - prefix with worker name
		log.Printf("[worker-%s] %s", output.WorkerId, string(output.Data))
	case dto.OutputType_OUTPUT_TYPE_STDOUT:
		// Task stdout output
		if output.TaskId != "" {
			log.Printf("Task output from %s: %s", output.TaskId, string(output.Data))
		} else {
			log.Printf("Worker %s stdout: %s", output.WorkerId, string(output.Data))
		}
	case dto.OutputType_OUTPUT_TYPE_STDERR:
		// Task stderr output
		if output.TaskId != "" {
			log.Printf("Task stderr from %s: %s", output.TaskId, string(output.Data))
		} else {
			log.Printf("Worker %s stderr: %s", output.WorkerId, string(output.Data))
		}
	default:
		// Fallback for unknown types
		log.Printf("Task output from %s: %s", output.TaskId, string(output.Data))
	}
}

func (s *WorkerGRPCService) handleWorkerMetrics(ctx context.Context, metrics *dto.WorkerMetrics) {
	// Update worker metrics in database
	// Store worker metrics for monitoring and optimization
	workerMetrics := &domain.WorkerMetrics{
		ID:                 fmt.Sprintf("metrics_%s_%d", metrics.WorkerId, time.Now().UnixNano()),
		WorkerID:           metrics.WorkerId,
		CPUUsagePercent:    float64(metrics.CpuUsagePercent),
		MemoryUsagePercent: float64(metrics.MemoryUsagePercent),
		Timestamp:          metrics.Timestamp.AsTime(),
		CreatedAt:          time.Now(),
	}
	if err := s.repo.CreateWorkerMetrics(ctx, workerMetrics); err != nil {
		log.Printf("Failed to store worker metrics: %v", err)
	}

	log.Printf("Worker metrics from %s: CPU=%.2f%%, Memory=%.2f%%",
		metrics.WorkerId, metrics.CpuUsagePercent, metrics.MemoryUsagePercent)
}

func (s *WorkerGRPCService) handleStagingStatus(ctx context.Context, status *dto.DataStagingStatus) {
	// Handle data staging status updates
	// Update staging operation progress in database
	stagingOp, err := s.repo.GetStagingOperationByID(ctx, status.StagingId)
	if err == nil && stagingOp != nil {
		stagingOp.Status = string(domain.StagingStatus(status.Status))
		stagingOp.CompletedFiles = int(status.CompletedFiles)
		if err := s.repo.UpdateStagingOperation(ctx, stagingOp); err != nil {
			log.Printf("Failed to update staging operation: %v", err)
		}
	}

	// Notify scheduler of staging completion
	if status.Status == dto.StagingStatus_STAGING_STATUS_COMPLETED {
		if s.scheduler != nil {
			s.scheduler.OnStagingComplete(ctx, status.TaskId)
		}
	}

	log.Printf("Staging status for %s: %s (%d/%d files)",
		status.TaskId, status.Status, status.CompletedFiles, status.TotalFiles)
}

func (s *WorkerGRPCService) stageOutputData(ctx context.Context, task *domain.Task, workerID string) {
	// Stage output data back to central storage
	if err := s.dataMover.StageOutputFromWorker(ctx, task, workerID, task.ExperimentID); err != nil {
		log.Printf("Failed to stage output data for task %s: %v", task.ID, err)
	}
}

// Connection management

func (s *WorkerGRPCService) addWorkerConnection(conn *WorkerConnection) {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.connections[conn.WorkerID] = conn
	log.Printf("Added worker connection: %s", conn.WorkerID)
}

func (s *WorkerGRPCService) removeWorkerConnection(workerID string) {
	s.mu.Lock()
	defer s.mu.Unlock()
	delete(s.connections, workerID)
	log.Printf("Removed worker connection: %s", workerID)
}

func (s *WorkerGRPCService) getWorkerConnection(workerID string) *WorkerConnection {
	s.mu.RLock()
	defer s.mu.RUnlock()
	return s.connections[workerID]
}

// GetActiveWorkerConnections returns all active worker connections
func (s *WorkerGRPCService) GetActiveWorkerConnections() map[string]*WorkerConnection {
	s.mu.RLock()
	defer s.mu.RUnlock()

	connections := make(map[string]*WorkerConnection)
	for id, conn := range s.connections {
		connections[id] = conn
	}
	return connections
}

// Note: SendTaskToWorker method removed - task assignment is now pull-based via handleTaskRequest

// CancelTaskForWorker sends a task cancellation to a specific worker
func (s *WorkerGRPCService) CancelTaskForWorker(workerID string, taskID string, reason string) error {
	workerConn := s.getWorkerConnection(workerID)
	if workerConn == nil {
		return fmt.Errorf("worker connection not found: %s", workerID)
	}

	cancellation := &dto.TaskCancellation{
		TaskId:      taskID,
		Reason:      reason,
		Force:       false,
		GracePeriod: &durationpb.Duration{Seconds: 30}, // 30 seconds grace period
	}

	msg := &dto.ServerMessage{
		Message: &dto.ServerMessage_TaskCancellation{
			TaskCancellation: cancellation,
		},
	}

	workerConn.mu.Lock()
	defer workerConn.mu.Unlock()

	if err := workerConn.Stream.Send(msg); err != nil {
		return fmt.Errorf("failed to send task cancellation: %w", err)
	}

	return nil
}

// ShutdownWorker sends a shutdown request to a specific worker
func (s *WorkerGRPCService) ShutdownWorker(workerID string, reason string, graceful bool) error {
	workerConn := s.getWorkerConnection(workerID)
	if workerConn == nil {
		return fmt.Errorf("worker connection not found: %s", workerID)
	}

	shutdown := &dto.WorkerShutdown{
		WorkerId: workerID,
		Reason:   reason,
		Graceful: graceful,
		Timeout:  &durationpb.Duration{Seconds: 60}, // 60 seconds timeout
	}

	msg := &dto.ServerMessage{
		Message: &dto.ServerMessage_WorkerShutdown{
			WorkerShutdown: shutdown,
		},
	}

	workerConn.mu.Lock()
	defer workerConn.mu.Unlock()

	if err := workerConn.Stream.Send(msg); err != nil {
		return fmt.Errorf("failed to send worker shutdown: %w", err)
	}

	return nil
}

// Helper function to convert interface{} map to string map
func convertToStringMap(metadata map[string]interface{}) map[string]string {
	result := make(map[string]string)
	for k, v := range metadata {
		if str, ok := v.(string); ok {
			result[k] = str
		} else {
			result[k] = fmt.Sprintf("%v", v)
		}
	}
	return result
}

// startHealthMonitor starts the background health monitoring goroutine
func (s *WorkerGRPCService) startHealthMonitor() {
	s.healthTicker = time.NewTicker(30 * time.Second)
	go func() {
		for {
			select {
			case <-s.ctx.Done():
				s.healthTicker.Stop()
				return
			case <-s.healthTicker.C:
				s.checkWorkerHealth()
			}
		}
	}()
}

// checkWorkerHealth checks all worker connections for timeouts
func (s *WorkerGRPCService) checkWorkerHealth() {
	s.mu.RLock()
	connections := make([]*WorkerConnection, 0, len(s.connections))
	for _, conn := range s.connections {
		connections = append(connections, conn)
	}
	s.mu.RUnlock()

	threshold := time.Now().Add(-2 * time.Minute) // 2 minute timeout

	for _, conn := range connections {
		conn.mu.RLock()
		lastHeartbeat := conn.LastHeartbeat
		workerID := conn.WorkerID
		conn.mu.RUnlock()

		if lastHeartbeat.Before(threshold) {
			log.Printf("Worker %s timed out (last heartbeat: %v)", workerID, lastHeartbeat)
			s.handleWorkerTimeout(workerID)
		}
	}
}

// handleWorkerTimeout handles a worker that has timed out
func (s *WorkerGRPCService) handleWorkerTimeout(workerID string) {
	// Get worker from database
	worker, err := s.repo.GetWorkerByID(s.ctx, workerID)
	if err != nil {
		log.Printf("Failed to get worker %s for timeout handling: %v", workerID, err)
		return
	}
	if worker == nil {
		return
	}

	// If worker has a current task, mark it as failed
	if worker.CurrentTaskID != "" {
		task, err := s.repo.GetTaskByID(s.ctx, worker.CurrentTaskID)
		if err == nil && task != nil {
			// Use scheduler to handle task failure with retry logic
			if scheduler, ok := s.scheduler.(*services.SchedulerService); ok {
				scheduler.FailTask(s.ctx, task.ID, workerID, "Worker connection timeout")
			}
		}
	}

	// Update worker status to failed
	worker.Status = domain.WorkerStatusIdle
	worker.UpdatedAt = time.Now()
	if err := s.repo.UpdateWorker(s.ctx, worker); err != nil {
		log.Printf("Failed to update worker %s status to failed: %v", workerID, err)
	}

	// Remove worker connection
	s.removeWorkerConnection(workerID)

	// Publish worker timeout event
	event := domain.NewAuditEvent("system", "worker.timeout", "worker", workerID)
	if err := s.events.Publish(s.ctx, event); err != nil {
		log.Printf("Failed to publish worker timeout event: %v", err)
	}
}

// Stop stops the health monitoring
func (s *WorkerGRPCService) Stop() {
	s.cancel()
}

// convertToProtoSignedURLs converts domain SignedURLs to protobuf SignedFileURLs
func convertToProtoSignedURLs(urls []domain.SignedURL) []*dto.SignedFileURL {
	protoURLs := make([]*dto.SignedFileURL, len(urls))
	for i, url := range urls {
		protoURLs[i] = &dto.SignedFileURL{
			SourcePath: url.SourcePath,
			Url:        url.URL,
			LocalPath:  url.LocalPath,
			ExpiresAt:  url.ExpiresAt.Unix(),
		}
	}
	return protoURLs
}
