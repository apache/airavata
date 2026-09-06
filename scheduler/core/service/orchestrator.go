package services

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"strings"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// OrchestratorService implements the ExperimentOrchestrator interface
type OrchestratorService struct {
	repo         ports.RepositoryPort
	events       ports.EventPort
	security     ports.SecurityPort
	scheduler    domain.TaskScheduler
	stateManager *StateManager
}

// Compile-time interface verification
var _ domain.ExperimentOrchestrator = (*OrchestratorService)(nil)

// NewOrchestratorService creates a new ExperimentOrchestrator service
func NewOrchestratorService(repo ports.RepositoryPort, events ports.EventPort, security ports.SecurityPort, scheduler domain.TaskScheduler, stateManager *StateManager) *OrchestratorService {
	return &OrchestratorService{
		repo:         repo,
		events:       events,
		security:     security,
		scheduler:    scheduler,
		stateManager: stateManager,
	}
}

// CreateExperiment implements domain.ExperimentOrchestrator.CreateExperiment
func (s *OrchestratorService) CreateExperiment(ctx context.Context, req *domain.CreateExperimentRequest, userID string) (*domain.CreateExperimentResponse, error) {
	// Validate the request
	if err := s.validateCreateExperimentRequest(req); err != nil {
		return &domain.CreateExperimentResponse{
			Success: false,
			Message: fmt.Sprintf("validation failed: %v", err),
		}, err
	}

	// Check if user exists
	user, err := s.repo.GetUserByID(ctx, userID)
	if err != nil {
		return &domain.CreateExperimentResponse{
			Success: false,
			Message: "user not found",
		}, domain.ErrUserNotFound
	}
	if user == nil {
		return &domain.CreateExperimentResponse{
			Success: false,
			Message: "user not found",
		}, domain.ErrUserNotFound
	}

	// Check if project exists
	project, err := s.repo.GetProjectByID(ctx, req.ProjectID)
	if err != nil {
		return &domain.CreateExperimentResponse{
			Success: false,
			Message: "project not found",
		}, domain.ErrResourceNotFound
	}
	if project == nil {
		return &domain.CreateExperimentResponse{
			Success: false,
			Message: "project not found",
		}, domain.ErrResourceNotFound
	}

	// Generate experiment ID
	experimentID := s.generateExperimentID(req.Name, userID)

	// Create the experiment
	experiment := &domain.Experiment{
		ID:              experimentID,
		Name:            req.Name,
		Description:     req.Description,
		ProjectID:       req.ProjectID,
		OwnerID:         userID,
		Status:          domain.ExperimentStatusCreated,
		CommandTemplate: req.CommandTemplate,
		OutputPattern:   req.OutputPattern,
		Parameters:      req.Parameters,
		Requirements:    req.Requirements,
		Constraints:     req.Constraints,
		CreatedAt:       time.Now(),
		UpdatedAt:       time.Now(),
		Metadata:        req.Metadata,
	}

	// Store the experiment
	if err := s.repo.CreateExperiment(ctx, experiment); err != nil {
		return &domain.CreateExperimentResponse{
			Success: false,
			Message: fmt.Sprintf("failed to create experiment: %v", err),
		}, err
	}

	// Publish event
	event := domain.NewExperimentCreatedEvent(experimentID, userID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish experiment created event: %v\n", err)
	}

	return &domain.CreateExperimentResponse{
		Experiment: experiment,
		Success:    true,
		Message:    "experiment created successfully",
	}, nil
}

// GetExperiment implements domain.ExperimentOrchestrator.GetExperiment
func (s *OrchestratorService) GetExperiment(ctx context.Context, req *domain.GetExperimentRequest) (*domain.GetExperimentResponse, error) {
	// Get experiment from repository
	experiment, err := s.repo.GetExperimentByID(ctx, req.ExperimentID)
	if err != nil {
		return &domain.GetExperimentResponse{
			Success: false,
			Message: "experiment not found",
		}, domain.ErrExperimentNotFound
	}
	if experiment == nil {
		return &domain.GetExperimentResponse{
			Success: false,
			Message: "experiment not found",
		}, domain.ErrExperimentNotFound
	}

	var tasks []*domain.Task
	if req.IncludeTasks {
		tasks, _, err = s.repo.ListTasksByExperiment(ctx, req.ExperimentID, 1000, 0)
		if err != nil {
			return &domain.GetExperimentResponse{
				Success: false,
				Message: fmt.Sprintf("failed to get tasks: %v", err),
			}, err
		}
	}

	return &domain.GetExperimentResponse{
		Experiment: experiment,
		Tasks:      tasks,
		Success:    true,
	}, nil
}

// ListExperiments implements domain.ExperimentOrchestrator.ListExperiments
func (s *OrchestratorService) ListExperiments(ctx context.Context, req *domain.ListExperimentsRequest) (*domain.ListExperimentsResponse, error) {
	// Build filters
	filters := &ports.ExperimentFilters{}
	if req.ProjectID != "" {
		filters.ProjectID = &req.ProjectID
	}
	if req.OwnerID != "" {
		filters.OwnerID = &req.OwnerID
	}
	if req.Status != "" {
		status := domain.ExperimentStatus(req.Status)
		filters.Status = &status
	}

	// Get experiments from repository
	experiments, total, err := s.repo.ListExperiments(ctx, filters, req.Limit, req.Offset)
	if err != nil {
		return &domain.ListExperimentsResponse{
			Total: 0,
		}, err
	}

	return &domain.ListExperimentsResponse{
		Experiments: experiments,
		Total:       int(total),
		Limit:       req.Limit,
		Offset:      req.Offset,
	}, nil
}

// UpdateExperiment implements domain.ExperimentOrchestrator.UpdateExperiment
func (s *OrchestratorService) UpdateExperiment(ctx context.Context, req *domain.UpdateExperimentRequest) (*domain.UpdateExperimentResponse, error) {
	// Get existing experiment
	experiment, err := s.repo.GetExperimentByID(ctx, req.ExperimentID)
	if err != nil {
		return &domain.UpdateExperimentResponse{
			Success: false,
			Message: "experiment not found",
		}, domain.ErrExperimentNotFound
	}
	if experiment == nil {
		return &domain.UpdateExperimentResponse{
			Success: false,
			Message: "experiment not found",
		}, domain.ErrExperimentNotFound
	}

	// Check if experiment can be updated
	if experiment.Status != domain.ExperimentStatusCreated {
		return &domain.UpdateExperimentResponse{
			Success: false,
			Message: "experiment cannot be updated in current state",
		}, domain.ErrInvalidExperimentState
	}

	// Update fields
	if req.Description != nil {
		experiment.Description = *req.Description
	}
	if req.Constraints != nil {
		experiment.Constraints = req.Constraints
	}
	if req.Metadata != nil {
		experiment.Metadata = req.Metadata
	}
	experiment.UpdatedAt = time.Now()

	// Save changes
	if err := s.repo.UpdateExperiment(ctx, experiment); err != nil {
		return &domain.UpdateExperimentResponse{
			Success: false,
			Message: fmt.Sprintf("failed to update experiment: %v", err),
		}, err
	}

	// Publish event
	event := domain.NewAuditEvent(experiment.OwnerID, "experiment.updated", "experiment", req.ExperimentID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish experiment updated event: %v\n", err)
	}

	return &domain.UpdateExperimentResponse{
		Experiment: experiment,
		Success:    true,
		Message:    "experiment updated successfully",
	}, nil
}

// DeleteExperiment implements domain.ExperimentOrchestrator.DeleteExperiment
func (s *OrchestratorService) DeleteExperiment(ctx context.Context, req *domain.DeleteExperimentRequest) (*domain.DeleteExperimentResponse, error) {
	// Get existing experiment
	experiment, err := s.repo.GetExperimentByID(ctx, req.ExperimentID)
	if err != nil {
		return &domain.DeleteExperimentResponse{
			Success: false,
			Message: "experiment not found",
		}, domain.ErrExperimentNotFound
	}
	if experiment == nil {
		return &domain.DeleteExperimentResponse{
			Success: false,
			Message: "experiment not found",
		}, domain.ErrExperimentNotFound
	}

	// Check if experiment can be deleted
	if experiment.Status == domain.ExperimentStatusExecuting && !req.Force {
		return &domain.DeleteExperimentResponse{
			Success: false,
			Message: "running experiment cannot be deleted without force=true",
		}, domain.ErrExperimentInProgress
	}

	// Delete experiment
	if err := s.repo.DeleteExperiment(ctx, req.ExperimentID); err != nil {
		return &domain.DeleteExperimentResponse{
			Success: false,
			Message: fmt.Sprintf("failed to delete experiment: %v", err),
		}, err
	}

	// Publish event
	event := domain.NewAuditEvent(experiment.OwnerID, "experiment.deleted", "experiment", req.ExperimentID)
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish experiment deleted event: %v\n", err)
	}

	return &domain.DeleteExperimentResponse{
		Success: true,
		Message: "experiment deleted successfully",
	}, nil
}

// SubmitExperiment implements domain.ExperimentOrchestrator.SubmitExperiment
func (s *OrchestratorService) SubmitExperiment(ctx context.Context, req *domain.SubmitExperimentRequest) (*domain.SubmitExperimentResponse, error) {
	// Get existing experiment
	experiment, err := s.repo.GetExperimentByID(ctx, req.ExperimentID)
	if err != nil {
		return &domain.SubmitExperimentResponse{
			Success: false,
			Message: "experiment not found",
		}, domain.ErrExperimentNotFound
	}
	if experiment == nil {
		return &domain.SubmitExperimentResponse{
			Success: false,
			Message: "experiment not found",
		}, domain.ErrExperimentNotFound
	}

	// Check if experiment can be submitted
	if experiment.Status != domain.ExperimentStatusCreated {
		return &domain.SubmitExperimentResponse{
			Success: false,
			Message: "experiment cannot be submitted in current state",
		}, domain.ErrInvalidExperimentState
	}

	// Generate tasks from parameters
	tasks, err := s.GenerateTasks(ctx, req.ExperimentID)
	if err != nil {
		return &domain.SubmitExperimentResponse{
			Success: false,
			Message: fmt.Sprintf("failed to generate tasks: %v", err),
		}, err
	}

	// Store task template and generated tasks in experiment
	taskTemplateJSON, _ := json.Marshal(experiment.CommandTemplate)
	generatedTasksJSON, _ := json.Marshal(tasks)
	experiment.TaskTemplate = string(taskTemplateJSON)
	experiment.GeneratedTasks = string(generatedTasksJSON)

	// Use StateManager for experiment state transition
	metadata := map[string]interface{}{
		"task_count": len(tasks),
		"user_id":    experiment.OwnerID,
	}
	if err := s.stateManager.TransitionExperimentState(ctx, req.ExperimentID, domain.ExperimentStatusCreated, domain.ExperimentStatusExecuting, metadata); err != nil {
		return &domain.SubmitExperimentResponse{
			Success: false,
			Message: fmt.Sprintf("failed to transition experiment to executing: %v", err),
		}, err
	}

	// Update experiment fields that aren't handled by StateManager
	experiment.Status = domain.ExperimentStatusExecuting
	experiment.UpdatedAt = time.Now()
	if err := s.repo.UpdateExperiment(ctx, experiment); err != nil {
		log.Printf("Failed to update experiment fields: %v", err)
	}

	// NEW: Trigger scheduling workflow
	if s.scheduler != nil {
		fmt.Printf("Orchestrator: starting full scheduling workflow for experiment %s\n", req.ExperimentID)

		// Phase 1: Analyze compute needs
		analyzer := NewComputeAnalyzer(s.repo, nil, nil) // Simplified for now
		analysis, err := analyzer.AnalyzeExperiment(ctx, req.ExperimentID)
		if err != nil {
			return &domain.SubmitExperimentResponse{Success: false, Message: err.Error()}, err
		}
		fmt.Printf("Orchestrator: analyzed experiment - %d tasks, %d CPU cores per task\n", analysis.TotalTasks, analysis.CPUCoresPerTask)

		// Log detailed data locality analysis
		analyzer.LogDataLocalityAnalysis(analysis)

		// Phase 2: Resolve accessible resources (simplified - get all resources)
		allResources, _, err := s.repo.ListComputeResources(ctx, &ports.ComputeResourceFilters{}, 10000, 0)
		if err != nil {
			return &domain.SubmitExperimentResponse{Success: false, Message: err.Error()}, err
		}
		fmt.Printf("Orchestrator: found %d compute resources\n", len(allResources))

		// Phase 3: Calculate optimal worker pool
		optimizer := NewSchedulingOptimizer(s.repo)
		plan, err := optimizer.CalculateOptimalWorkerPool(ctx, analysis, allResources)
		if err != nil {
			return &domain.SubmitExperimentResponse{Success: false, Message: err.Error()}, err
		}
		fmt.Printf("Orchestrator: calculated worker pool - %d total workers across %d resources\n", plan.TotalWorkers, len(plan.WorkersPerResource))

		// Phase 4: Schedule tasks (queue them on resources)
		_, err = s.scheduler.ScheduleExperiment(ctx, req.ExperimentID)
		if err != nil {
			return &domain.SubmitExperimentResponse{Success: false, Message: err.Error()}, err
		}
		fmt.Printf("Orchestrator: scheduled tasks to compute resources\n")

		// Phase 5: Provision worker pool (simplified - just log for now)
		fmt.Printf("Orchestrator: would provision worker pool with plan: %+v\n", plan)
	} else {
		fmt.Printf("Orchestrator: scheduler is nil, cannot schedule experiment\n")
	}

	// Publish event
	event := domain.NewExperimentSubmittedEvent(req.ExperimentID, experiment.OwnerID, len(tasks))
	if err := s.events.Publish(ctx, event); err != nil {
		fmt.Printf("failed to publish experiment submitted event: %v\n", err)
	}

	// Fetch the updated experiment from database to get the current status
	updatedExperiment, err := s.repo.GetExperimentByID(ctx, req.ExperimentID)
	if err != nil {
		return &domain.SubmitExperimentResponse{
			Success: false,
			Message: fmt.Sprintf("failed to fetch updated experiment: %v", err),
		}, err
	}

	return &domain.SubmitExperimentResponse{
		Experiment: updatedExperiment,
		Tasks:      tasks,
		Success:    true,
		Message:    "experiment submitted successfully",
	}, nil
}

// GenerateTasks implements domain.ExperimentOrchestrator.GenerateTasks
func (s *OrchestratorService) GenerateTasks(ctx context.Context, experimentID string) ([]*domain.Task, error) {
	// Get experiment
	experiment, err := s.repo.GetExperimentByID(ctx, experimentID)
	if err != nil {
		return nil, fmt.Errorf("experiment not found: %w", err)
	}
	if experiment == nil {
		return nil, domain.ErrExperimentNotFound
	}

	var tasks []*domain.Task

	// Generate tasks from parameter sets
	for i, paramSet := range experiment.Parameters {
		taskID := s.generateTaskID(experimentID, i)

		// Substitute parameters into command template
		command := experiment.CommandTemplate
		for key, value := range paramSet.Values {
			placeholder := fmt.Sprintf("{%s}", key)
			command = strings.ReplaceAll(command, placeholder, value)
		}

		// Create task
		task := &domain.Task{
			ID:              taskID,
			ExperimentID:    experimentID,
			Status:          domain.TaskStatusCreated,
			Command:         command,
			ExecutionScript: command, // Set ExecutionScript to the command for simple execution
			InputFiles:      s.extractInputFiles(experiment),
			OutputFiles:     s.extractOutputFiles(experiment),
			RetryCount:      0,
			MaxRetries:      3, // Default max retries
			CreatedAt:       time.Now(),
			UpdatedAt:       time.Now(),
			Metadata:        s.convertStringMapToInterfaceMap(paramSet.Values),
		}

		// Store task
		log.Printf("Creating task %s with ExecutionScript: %s", task.ID, task.ExecutionScript)
		if err := s.repo.CreateTask(ctx, task); err != nil {
			return nil, fmt.Errorf("failed to create task: %w", err)
		}

		// Publish event
		event := domain.NewTaskCreatedEvent(taskID, experimentID)
		if err := s.events.Publish(ctx, event); err != nil {
			fmt.Printf("failed to publish task created event: %v\n", err)
		}

		tasks = append(tasks, task)
	}

	return tasks, nil
}

// ValidateExperiment implements domain.ExperimentOrchestrator.ValidateExperiment
func (s *OrchestratorService) ValidateExperiment(ctx context.Context, experimentID string) (*domain.ValidationResult, error) {
	// Get experiment
	experiment, err := s.repo.GetExperimentByID(ctx, experimentID)
	if err != nil {
		return &domain.ValidationResult{
			Valid:  false,
			Errors: []string{"experiment not found"},
		}, err
	}
	if experiment == nil {
		return &domain.ValidationResult{
			Valid:  false,
			Errors: []string{"experiment not found"},
		}, domain.ErrExperimentNotFound
	}

	var errors []string
	var warnings []string

	// Validate experiment fields
	if experiment.Name == "" {
		errors = append(errors, "experiment name is required")
	}
	if experiment.CommandTemplate == "" {
		errors = append(errors, "command template is required")
	}
	if len(experiment.Parameters) == 0 {
		errors = append(errors, "at least one parameter set is required")
	}

	// Validate parameter sets
	for i, paramSet := range experiment.Parameters {
		if len(paramSet.Values) == 0 {
			errors = append(errors, fmt.Sprintf("parameter set %d has no values", i))
		}
	}

	// Validate requirements
	if experiment.Requirements != nil {
		if experiment.Requirements.CPUCores <= 0 {
			errors = append(errors, "CPU cores must be greater than 0")
		}
		if experiment.Requirements.MemoryMB <= 0 {
			errors = append(errors, "memory must be greater than 0")
		}
	}

	// Validate constraints
	if experiment.Constraints != nil {
		if experiment.Constraints.MaxCost < 0 {
			errors = append(errors, "max cost cannot be negative")
		}
	}

	return &domain.ValidationResult{
		Valid:    len(errors) == 0,
		Errors:   errors,
		Warnings: warnings,
	}, nil
}

// Helper methods

func (s *OrchestratorService) validateCreateExperimentRequest(req *domain.CreateExperimentRequest) error {
	if req.Name == "" {
		return fmt.Errorf("missing required parameter: name")
	}
	if req.ProjectID == "" {
		return fmt.Errorf("missing required parameter: project_id")
	}
	if req.CommandTemplate == "" {
		return fmt.Errorf("missing required parameter: command_template")
	}
	if len(req.Parameters) == 0 {
		return fmt.Errorf("missing required parameter: parameters")
	}
	return nil
}

func (s *OrchestratorService) generateExperimentID(name string, userID string) string {
	timestamp := time.Now().UnixNano()
	return fmt.Sprintf("exp_%s_%s_%d", name, userID, timestamp)
}

func (s *OrchestratorService) generateTaskID(experimentID string, index int) string {
	timestamp := time.Now().UnixNano()
	return fmt.Sprintf("task_%s_%d_%d", experimentID, index, timestamp)
}

func (s *OrchestratorService) convertStringMapToInterfaceMap(stringMap map[string]string) map[string]interface{} {
	interfaceMap := make(map[string]interface{})
	for k, v := range stringMap {
		interfaceMap[k] = v
	}
	return interfaceMap
}

// extractInputFiles extracts input file metadata from experiment
func (s *OrchestratorService) extractInputFiles(experiment *domain.Experiment) []domain.FileMetadata {
	var inputFiles []domain.FileMetadata

	// Extract from experiment metadata
	if experiment.Metadata != nil {
		if inputs, ok := experiment.Metadata["input_files"].([]interface{}); ok {
			for _, input := range inputs {
				if inputMap, ok := input.(map[string]interface{}); ok {
					file := domain.FileMetadata{
						Path:     getStringFromMap(inputMap, "path"),
						Size:     getInt64FromMap(inputMap, "size"),
						Checksum: getStringFromMap(inputMap, "checksum"),
					}
					inputFiles = append(inputFiles, file)
				}
			}
		}
	}

	return inputFiles
}

// extractOutputFiles extracts output file metadata from experiment
func (s *OrchestratorService) extractOutputFiles(experiment *domain.Experiment) []domain.FileMetadata {
	var outputFiles []domain.FileMetadata

	// Extract from experiment metadata
	if experiment.Metadata != nil {
		if outputs, ok := experiment.Metadata["output_files"].([]interface{}); ok {
			for _, output := range outputs {
				if outputMap, ok := output.(map[string]interface{}); ok {
					file := domain.FileMetadata{
						Path:     getStringFromMap(outputMap, "path"),
						Size:     getInt64FromMap(outputMap, "size"),
						Checksum: getStringFromMap(outputMap, "checksum"),
					}
					outputFiles = append(outputFiles, file)
				}
			}
		}
	}

	return outputFiles
}

// Helper functions for map extraction
func getStringFromMap(m map[string]interface{}, key string) string {
	if val, ok := m[key].(string); ok {
		return val
	}
	return ""
}

func getInt64FromMap(m map[string]interface{}, key string) int64 {
	if val, ok := m[key].(int64); ok {
		return val
	}
	if val, ok := m[key].(int); ok {
		return int64(val)
	}
	if val, ok := m[key].(float64); ok {
		return int64(val)
	}
	return 0
}
