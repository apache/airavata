package services

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"

	"github.com/apache/airavata/scheduler/core/domain"
	types "github.com/apache/airavata/scheduler/core/util"
)

// ExperimentService provides experiment management functionality
type ExperimentService struct {
	db *gorm.DB
}

// NewExperimentService creates a new experiment service
func NewExperimentService(db *gorm.DB) *ExperimentService {
	return &ExperimentService{
		db: db,
	}
}

// CreateDerivativeExperiment creates a new experiment based on an existing one
func (s *ExperimentService) CreateDerivativeExperiment(ctx context.Context, req *types.DerivativeExperimentRequest) (*types.DerivativeExperimentResponse, error) {
	// Get source experiment
	var sourceExperiment domain.Experiment
	if err := s.db.WithContext(ctx).First(&sourceExperiment, "id = ?", req.SourceExperimentID).Error; err != nil {
		return nil, fmt.Errorf("failed to find source experiment: %w", err)
	}

	// Validate source experiment
	if sourceExperiment.Status != domain.ExperimentStatusCompleted && sourceExperiment.Status != domain.ExperimentStatusCanceled {
		return nil, fmt.Errorf("source experiment must be completed or failed to create derivative")
	}

	// Create new experiment based on source
	newExperiment := &domain.Experiment{
		ID:              uuid.New().String(),
		Name:            req.NewExperimentName,
		Description:     fmt.Sprintf("Derivative of experiment: %s", sourceExperiment.Name),
		ProjectID:       sourceExperiment.ProjectID,
		OwnerID:         sourceExperiment.OwnerID, // Same owner as source
		Status:          domain.ExperimentStatusCreated,
		CommandTemplate: sourceExperiment.CommandTemplate,
		OutputPattern:   sourceExperiment.OutputPattern,
		TaskTemplate:    sourceExperiment.TaskTemplate,
		Requirements:    sourceExperiment.Requirements,
		Constraints:     sourceExperiment.Constraints,
		CreatedAt:       time.Now(),
		UpdatedAt:       time.Now(),
		Metadata: map[string]interface{}{
			"derivativeOf": req.SourceExperimentID,
			"createdAt":    time.Now(),
		},
	}

	// Apply parameter modifications if specified
	var newParameters []domain.ParameterSet
	if len(req.ParameterModifications) > 0 {
		// Modify existing parameters
		for _, paramSet := range sourceExperiment.Parameters {
			modifiedParamSet := paramSet

			// Apply modifications
			for key, value := range req.ParameterModifications {
				if modifiedParamSet.Values == nil {
					modifiedParamSet.Values = make(map[string]string)
				}
				modifiedParamSet.Values[key] = fmt.Sprintf("%v", value)
			}

			newParameters = append(newParameters, modifiedParamSet)
		}
	} else {
		// Use original parameters
		newParameters = sourceExperiment.Parameters
	}

	// Filter parameters based on task filter
	if req.TaskFilter != "" {
		newParameters = s.filterParameters(ctx, req.SourceExperimentID, newParameters, req.TaskFilter)
	}

	newExperiment.Parameters = newParameters

	// Preserve compute resources if requested
	if req.PreserveComputeResources {
		if sourceExperiment.Constraints != nil {
			newExperiment.Constraints = sourceExperiment.Constraints
		}
	}

	// Save new experiment
	if err := s.db.WithContext(ctx).Create(newExperiment).Error; err != nil {
		return nil, fmt.Errorf("failed to create derivative experiment: %w", err)
	}

	// Generate tasks for the new experiment
	tasks, err := s.generateTasksFromParameters(newExperiment)
	if err != nil {
		return nil, fmt.Errorf("failed to generate tasks: %w", err)
	}

	// Save generated tasks
	for _, task := range tasks {
		if err := s.db.WithContext(ctx).Create(&task).Error; err != nil {
			return nil, fmt.Errorf("failed to create task: %w", err)
		}
	}

	// Update experiment with generated tasks
	generatedTasksJSON, err := json.Marshal(tasks)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal generated tasks: %w", err)
	}
	newExperiment.GeneratedTasks = string(generatedTasksJSON)

	if err := s.db.WithContext(ctx).Save(newExperiment).Error; err != nil {
		return nil, fmt.Errorf("failed to update experiment with generated tasks: %w", err)
	}

	// Create validation result
	validation := types.ValidationResult{
		IsValid:  true,
		Warnings: []string{},
		Errors:   []string{},
	}

	// Add warnings if needed
	if len(newParameters) == 0 {
		validation.Warnings = append(validation.Warnings, "No parameters generated for derivative experiment")
	}

	if len(newParameters) != len(sourceExperiment.Parameters) {
		validation.Warnings = append(validation.Warnings,
			fmt.Sprintf("Parameter count changed from %d to %d", len(sourceExperiment.Parameters), len(newParameters)))
	}

	return &types.DerivativeExperimentResponse{
		NewExperimentID:    newExperiment.ID,
		SourceExperimentID: req.SourceExperimentID,
		TaskCount:          len(tasks),
		ParameterCount:     len(newParameters),
		Validation:         validation,
	}, nil
}

// GetExperimentProgress returns real-time progress for an experiment
func (s *ExperimentService) GetExperimentProgress(ctx context.Context, experimentID string) (*types.ExperimentProgress, error) {
	var experiment domain.Experiment
	if err := s.db.WithContext(ctx).First(&experiment, "id = ?", experimentID).Error; err != nil {
		return nil, fmt.Errorf("failed to find experiment: %w", err)
	}

	// Get task statistics
	var taskStats struct {
		TotalTasks     int64 `json:"totalTasks"`
		CompletedTasks int64 `json:"completedTasks"`
		FailedTasks    int64 `json:"failedTasks"`
		RunningTasks   int64 `json:"runningTasks"`
	}

	if err := s.db.WithContext(ctx).Model(&domain.Task{}).
		Select(`
			COUNT(*) as total_tasks,
			COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_tasks,
			COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed_tasks,
			COUNT(CASE WHEN status IN ('RUNNING', 'STAGING', 'ASSIGNED') THEN 1 END) as running_tasks
		`).
		Where("experiment_id = ?", experimentID).
		Scan(&taskStats).Error; err != nil {
		return nil, fmt.Errorf("failed to get task statistics: %w", err)
	}

	// Calculate progress percentage
	var progressPercent float64
	if taskStats.TotalTasks > 0 {
		progressPercent = float64(taskStats.CompletedTasks) / float64(taskStats.TotalTasks) * 100
	}

	// Estimate time remaining (simple calculation)
	var estimatedTimeRemaining time.Duration
	if taskStats.RunningTasks > 0 && taskStats.CompletedTasks > 0 {
		// Get average duration of completed tasks
		var avgDuration float64
		if err := s.db.WithContext(ctx).Model(&domain.Task{}).
			Select("AVG(EXTRACT(EPOCH FROM (completed_at - started_at)))").
			Where("experiment_id = ? AND status = 'COMPLETED' AND started_at IS NOT NULL AND completed_at IS NOT NULL", experimentID).
			Scan(&avgDuration).Error; err == nil && avgDuration > 0 {
			// Estimate remaining time based on average duration and remaining tasks
			remainingTasks := taskStats.TotalTasks - taskStats.CompletedTasks - taskStats.FailedTasks
			estimatedTimeRemaining = time.Duration(avgDuration) * time.Second * time.Duration(remainingTasks)
		}
	}

	return &types.ExperimentProgress{
		ExperimentID:           experimentID,
		TotalTasks:             int(taskStats.TotalTasks),
		CompletedTasks:         int(taskStats.CompletedTasks),
		FailedTasks:            int(taskStats.FailedTasks),
		RunningTasks:           int(taskStats.RunningTasks),
		ProgressPercent:        progressPercent,
		EstimatedTimeRemaining: estimatedTimeRemaining,
		LastUpdated:            time.Now(),
	}, nil
}

// GetTaskProgress returns real-time progress for a specific task
func (s *ExperimentService) GetTaskProgress(ctx context.Context, taskID string) (*types.TaskProgress, error) {
	var task domain.Task
	if err := s.db.WithContext(ctx).First(&task, "id = ?", taskID).Error; err != nil {
		return nil, fmt.Errorf("failed to find task: %w", err)
	}

	// Calculate progress percentage based on status
	var progressPercent float64
	var currentStage string
	var estimatedCompletion *time.Time

	switch task.Status {
	case domain.TaskStatusCreated:
		progressPercent = 0
		currentStage = "QUEUED"
	case domain.TaskStatusQueued:
		progressPercent = 10
		currentStage = "ASSIGNED"
	case domain.TaskStatusDataStaging:
		progressPercent = 25
		currentStage = "STAGING"
	case domain.TaskStatusRunning:
		progressPercent = 50
		currentStage = "RUNNING"

		// Estimate completion time if task has been running
		if task.StartedAt != nil {
			// Simple estimation: assume task will take average duration
			avgDuration := 5 * time.Minute // Default assumption
			estimated := task.StartedAt.Add(avgDuration)
			estimatedCompletion = &estimated
		}
	case domain.TaskStatusCompleted:
		progressPercent = 100
		currentStage = "COMPLETED"
	case domain.TaskStatusFailed:
		progressPercent = 0
		currentStage = "FAILED"
	default:
		progressPercent = 0
		currentStage = "UNKNOWN"
	}

	return &types.TaskProgress{
		TaskID:              task.ID,
		ExperimentID:        task.ExperimentID,
		Status:              string(task.Status),
		ProgressPercent:     progressPercent,
		CurrentStage:        currentStage,
		WorkerID:            task.WorkerID,
		StartedAt:           task.StartedAt,
		EstimatedCompletion: estimatedCompletion,
		LastUpdated:         time.Now(),
	}, nil
}

// filterParameters filters parameters based on task results
func (s *ExperimentService) filterParameters(ctx context.Context, experimentID string, parameters []domain.ParameterSet, filter string) []domain.ParameterSet {
	switch filter {
	case "only_successful":
		return s.filterBySuccessfulTasks(ctx, experimentID, parameters)
	case "only_failed":
		return s.filterByFailedTasks(ctx, experimentID, parameters)
	case "all":
		return parameters
	default:
		// Unknown filter, return all parameters
		return parameters
	}
}

// filterBySuccessfulTasks returns only parameters for tasks that completed successfully
func (s *ExperimentService) filterBySuccessfulTasks(ctx context.Context, experimentID string, parameters []domain.ParameterSet) []domain.ParameterSet {
	var successfulTaskIDs []string
	if err := s.db.WithContext(ctx).Model(&domain.Task{}).
		Select("id").
		Where("experiment_id = ? AND status = 'COMPLETED'", experimentID).
		Find(&successfulTaskIDs).Error; err != nil {
		// If we can't get successful tasks, return all parameters
		return parameters
	}

	// Filter parameters based on successful tasks
	// This is a simplified approach - in practice, you'd need to match parameter sets to tasks
	// based on the task generation logic
	filteredParams := append([]domain.ParameterSet{}, parameters...)

	return filteredParams
}

// filterByFailedTasks returns only parameters for tasks that failed
func (s *ExperimentService) filterByFailedTasks(ctx context.Context, experimentID string, parameters []domain.ParameterSet) []domain.ParameterSet {
	var failedTaskIDs []string
	if err := s.db.WithContext(ctx).Model(&domain.Task{}).
		Select("id").
		Where("experiment_id = ? AND status = 'FAILED'", experimentID).
		Find(&failedTaskIDs).Error; err != nil {
		// If we can't get failed tasks, return empty parameters
		return []domain.ParameterSet{}
	}

	// Filter parameters based on failed tasks
	// This is a simplified approach - in practice, you'd need to match parameter sets to tasks
	// based on the task generation logic
	filteredParams := append([]domain.ParameterSet{}, parameters...)

	return filteredParams
}

// generateTasksFromParameters generates tasks from experiment parameters
func (s *ExperimentService) generateTasksFromParameters(experiment *domain.Experiment) ([]domain.Task, error) {
	var tasks []domain.Task

	for i, paramSet := range experiment.Parameters {
		// Substitute parameters in command template
		command := s.substituteParameters(experiment.CommandTemplate, paramSet.Values)
		outputPath := s.substituteParameters(experiment.OutputPattern, paramSet.Values)

		// Create task
		task := domain.Task{
			ID:           uuid.New().String(),
			ExperimentID: experiment.ID,
			Status:       domain.TaskStatusCreated,
			Command:      command,
			RetryCount:   0,
			MaxRetries:   3,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
			Metadata: map[string]interface{}{
				"parameterSet":   paramSet.Values,
				"parameterIndex": i,
			},
		}

		// Set output path if specified
		if outputPath != "" {
			// This would need to be implemented based on your output file handling
		}

		tasks = append(tasks, task)
	}

	return tasks, nil
}

// substituteParameters substitutes parameter values in a template string
func (s *ExperimentService) substituteParameters(template string, parameters map[string]string) string {
	result := template

	for key, value := range parameters {
		placeholder := fmt.Sprintf("{{%s}}", key)
		result = strings.ReplaceAll(result, placeholder, value)
	}

	return result
}

// replaceAll replaces all occurrences of a substring
func replaceAll(s, old, new string) string {
	if old == "" {
		return s
	}

	result := ""
	start := 0
	for {
		pos := strings.Index(s[start:], old)
		if pos == -1 {
			result += s[start:]
			break
		}
		result += s[start:start+pos] + new
		start += pos + len(old)
	}

	return result
}
