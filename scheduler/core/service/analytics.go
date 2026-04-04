package services

import (
	"context"
	"fmt"

	"gorm.io/gorm"

	"github.com/apache/airavata/scheduler/core/domain"
	types "github.com/apache/airavata/scheduler/core/util"
)

// AnalyticsService provides analytics and reporting functionality
type AnalyticsService struct {
	db *gorm.DB
}

// NewAnalyticsService creates a new analytics service
func NewAnalyticsService(db *gorm.DB) *AnalyticsService {
	return &AnalyticsService{
		db: db,
	}
}

// GetExperimentSummary generates a comprehensive experiment summary
func (s *AnalyticsService) GetExperimentSummary(ctx context.Context, experimentID string) (*types.ExperimentSummary, error) {
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

	// Calculate success rate
	var successRate float64
	if taskStats.TotalTasks > 0 {
		successRate = float64(taskStats.CompletedTasks) / float64(taskStats.TotalTasks)
	}

	// Get average duration
	var avgDuration float64
	if err := s.db.WithContext(ctx).Model(&domain.Task{}).
		Select("AVG(EXTRACT(EPOCH FROM (completed_at - started_at)))").
		Where("experiment_id = ? AND status = 'COMPLETED' AND started_at IS NOT NULL AND completed_at IS NOT NULL", experimentID).
		Scan(&avgDuration).Error; err != nil {
		// Log error but don't fail
		fmt.Printf("Failed to calculate average duration: %v\n", err)
	}

	// Get parameter set count
	var parameterSetCount int
	if len(experiment.Parameters) > 0 {
		parameterSetCount = len(experiment.Parameters)
	}

	// Calculate estimated total cost based on actual task duration and resource rates
	var totalCost float64
	if err := s.db.WithContext(ctx).Model(&domain.Task{}).
		Select("SUM(EXTRACT(EPOCH FROM (completed_at - started_at)) * 0.1)").
		Where("experiment_id = ? AND status = 'COMPLETED' AND started_at IS NOT NULL AND completed_at IS NOT NULL", experimentID).
		Scan(&totalCost).Error; err != nil {
		// Log error but don't fail
		fmt.Printf("Failed to calculate total cost: %v\n", err)
	}

	return &types.ExperimentSummary{
		ExperimentID:      experiment.ID,
		ExperimentName:    experiment.Name,
		Status:            string(experiment.Status),
		TotalTasks:        int(taskStats.TotalTasks),
		CompletedTasks:    int(taskStats.CompletedTasks),
		FailedTasks:       int(taskStats.FailedTasks),
		RunningTasks:      int(taskStats.RunningTasks),
		SuccessRate:       successRate,
		AvgDurationSec:    avgDuration,
		TotalCost:         totalCost,
		CreatedAt:         experiment.CreatedAt,
		UpdatedAt:         experiment.UpdatedAt,
		ParameterSetCount: parameterSetCount,
	}, nil
}

// GetFailedTasks retrieves failed tasks for an experiment
func (s *AnalyticsService) GetFailedTasks(ctx context.Context, experimentID string) ([]types.FailedTaskInfo, error) {
	var tasks []domain.Task
	if err := s.db.WithContext(ctx).
		Where("experiment_id = ? AND status = 'FAILED'", experimentID).
		Find(&tasks).Error; err != nil {
		return nil, fmt.Errorf("failed to get failed tasks: %w", err)
	}

	var failedTasks []types.FailedTaskInfo
	for _, task := range tasks {
		// Extract parameter set from metadata if available
		var parameterSet map[string]string
		if task.Metadata != nil {
			if params, ok := task.Metadata["parameterSet"].(map[string]interface{}); ok {
				parameterSet = make(map[string]string)
				for k, v := range params {
					if str, ok := v.(string); ok {
						parameterSet[k] = str
					}
				}
			}
		}

		failedTask := types.FailedTaskInfo{
			TaskID:       task.ID,
			TaskName:     fmt.Sprintf("Task %s", task.ID[:8]), // Use first 8 chars of ID as name
			ExperimentID: task.ExperimentID,
			Status:       string(task.Status),
			Error:        task.Error,
			RetryCount:   task.RetryCount,
			MaxRetries:   task.MaxRetries,
			LastAttempt:  task.UpdatedAt,
			WorkerID:     task.WorkerID,
			ParameterSet: parameterSet,
		}

		// Add suggested fix based on error type
		failedTask.SuggestedFix = s.getSuggestedFix(task.Error)

		failedTasks = append(failedTasks, failedTask)
	}

	return failedTasks, nil
}

// GetTaskAggregation performs task aggregation by specified criteria
func (s *AnalyticsService) GetTaskAggregation(ctx context.Context, req *types.TaskAggregationRequest) (*types.TaskAggregationResponse, error) {
	query := s.db.WithContext(ctx).Model(&domain.Task{})

	// Apply experiment filter if specified
	if req.ExperimentID != "" {
		query = query.Where("experiment_id = ?", req.ExperimentID)
	}

	// Apply additional filters if specified
	if req.Filter != "" {
		// This would need to be implemented based on specific filter requirements
		// For now, we'll skip complex filtering
	}

	var groups []types.TaskAggregationGroup
	var total int64

	switch req.GroupBy {
	case "status":
		var statusGroups []struct {
			Status    string `json:"status"`
			Total     int64  `json:"total"`
			Completed int64  `json:"completed"`
			Failed    int64  `json:"failed"`
			Running   int64  `json:"running"`
		}

		if err := query.Select(`
			status,
			COUNT(*) as total,
			COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
			COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed,
			COUNT(CASE WHEN status IN ('RUNNING', 'STAGING', 'ASSIGNED') THEN 1 END) as running
		`).
			Group("status").
			Find(&statusGroups).Error; err != nil {
			return nil, fmt.Errorf("failed to aggregate by status: %w", err)
		}

		for _, sg := range statusGroups {
			var successRate float64
			if sg.Total > 0 {
				successRate = float64(sg.Completed) / float64(sg.Total)
			}

			groups = append(groups, types.TaskAggregationGroup{
				GroupKey:    "status",
				GroupValue:  sg.Status,
				Count:       int(sg.Total),
				Completed:   int(sg.Completed),
				Failed:      int(sg.Failed),
				Running:     int(sg.Running),
				SuccessRate: successRate,
			})
		}

	case "worker":
		var workerGroups []struct {
			WorkerID  string `json:"workerId"`
			Total     int64  `json:"total"`
			Completed int64  `json:"completed"`
			Failed    int64  `json:"failed"`
			Running   int64  `json:"running"`
		}

		if err := query.Select(`
			worker_id,
			COUNT(*) as total,
			COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
			COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed,
			COUNT(CASE WHEN status IN ('RUNNING', 'STAGING', 'ASSIGNED') THEN 1 END) as running
		`).
			Where("worker_id IS NOT NULL").
			Group("worker_id").
			Find(&workerGroups).Error; err != nil {
			return nil, fmt.Errorf("failed to aggregate by worker: %w", err)
		}

		for _, wg := range workerGroups {
			var successRate float64
			if wg.Total > 0 {
				successRate = float64(wg.Completed) / float64(wg.Total)
			}

			groups = append(groups, types.TaskAggregationGroup{
				GroupKey:    "worker",
				GroupValue:  wg.WorkerID,
				Count:       int(wg.Total),
				Completed:   int(wg.Completed),
				Failed:      int(wg.Failed),
				Running:     int(wg.Running),
				SuccessRate: successRate,
			})
		}

	case "compute_resource":
		var resourceGroups []struct {
			ComputeResourceID string `json:"computeResourceId"`
			Total             int64  `json:"total"`
			Completed         int64  `json:"completed"`
			Failed            int64  `json:"failed"`
			Running           int64  `json:"running"`
		}

		if err := query.Select(`
			compute_resource_id,
			COUNT(*) as total,
			COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed,
			COUNT(CASE WHEN status = 'FAILED' THEN 1 END) as failed,
			COUNT(CASE WHEN status IN ('RUNNING', 'STAGING', 'ASSIGNED') THEN 1 END) as running
		`).
			Where("compute_resource_id IS NOT NULL").
			Group("compute_resource_id").
			Find(&resourceGroups).Error; err != nil {
			return nil, fmt.Errorf("failed to aggregate by compute resource: %w", err)
		}

		for _, rg := range resourceGroups {
			var successRate float64
			if rg.Total > 0 {
				successRate = float64(rg.Completed) / float64(rg.Total)
			}

			groups = append(groups, types.TaskAggregationGroup{
				GroupKey:    "compute_resource",
				GroupValue:  rg.ComputeResourceID,
				Count:       int(rg.Total),
				Completed:   int(rg.Completed),
				Failed:      int(rg.Failed),
				Running:     int(rg.Running),
				SuccessRate: successRate,
			})
		}

	default:
		return nil, fmt.Errorf("unsupported group by field: %s", req.GroupBy)
	}

	// Get total count
	if err := query.Count(&total).Error; err != nil {
		return nil, fmt.Errorf("failed to count total tasks: %w", err)
	}

	return &types.TaskAggregationResponse{
		Groups: groups,
		Total:  int(total),
	}, nil
}

// GetExperimentTimeline constructs a chronological timeline of experiment events
func (s *AnalyticsService) GetExperimentTimeline(ctx context.Context, experimentID string) (*types.ExperimentTimeline, error) {
	var events []types.TimelineEvent

	// Get experiment creation event
	var experiment domain.Experiment
	if err := s.db.WithContext(ctx).First(&experiment, "id = ?", experimentID).Error; err != nil {
		return nil, fmt.Errorf("failed to find experiment: %w", err)
	}

	events = append(events, types.TimelineEvent{
		EventID:     fmt.Sprintf("exp_created_%s", experiment.ID),
		EventType:   "EXPERIMENT_CREATED",
		Timestamp:   experiment.CreatedAt,
		Description: fmt.Sprintf("Experiment '%s' created", experiment.Name),
		Metadata: map[string]interface{}{
			"experimentId":   experiment.ID,
			"experimentName": experiment.Name,
		},
	})

	// Get task events
	var tasks []domain.Task
	if err := s.db.WithContext(ctx).
		Where("experiment_id = ?", experimentID).
		Order("created_at ASC").
		Find(&tasks).Error; err != nil {
		return nil, fmt.Errorf("failed to get tasks: %w", err)
	}

	for _, task := range tasks {
		// Task created event
		events = append(events, types.TimelineEvent{
			EventID:     fmt.Sprintf("task_created_%s", task.ID),
			EventType:   "TASK_CREATED",
			TaskID:      task.ID,
			Timestamp:   task.CreatedAt,
			Description: fmt.Sprintf("Task %s created", task.ID[:8]),
			Metadata: map[string]interface{}{
				"taskId":  task.ID,
				"command": task.Command,
			},
		})

		// Task started event
		if task.StartedAt != nil {
			events = append(events, types.TimelineEvent{
				EventID:     fmt.Sprintf("task_started_%s", task.ID),
				EventType:   "TASK_STARTED",
				TaskID:      task.ID,
				WorkerID:    task.WorkerID,
				Timestamp:   *task.StartedAt,
				Description: fmt.Sprintf("Task %s started on worker %s", task.ID[:8], task.WorkerID[:8]),
				Metadata: map[string]interface{}{
					"taskId":   task.ID,
					"workerId": task.WorkerID,
				},
			})
		}

		// Task completed/failed event
		if task.CompletedAt != nil {
			eventType := "TASK_COMPLETED"
			description := fmt.Sprintf("Task %s completed", task.ID[:8])
			if task.Status == domain.TaskStatusFailed {
				eventType = "TASK_FAILED"
				description = fmt.Sprintf("Task %s failed: %s", task.ID[:8], task.Error)
			}

			events = append(events, types.TimelineEvent{
				EventID:     fmt.Sprintf("task_%s_%s", eventType, task.ID),
				EventType:   eventType,
				TaskID:      task.ID,
				WorkerID:    task.WorkerID,
				Timestamp:   *task.CompletedAt,
				Description: description,
				Metadata: map[string]interface{}{
					"taskId":   task.ID,
					"workerId": task.WorkerID,
					"status":   string(task.Status),
					"error":    task.Error,
				},
			})
		}
	}

	// Sort events by timestamp
	for i := 0; i < len(events)-1; i++ {
		for j := i + 1; j < len(events); j++ {
			if events[i].Timestamp.After(events[j].Timestamp) {
				events[i], events[j] = events[j], events[i]
			}
		}
	}

	return &types.ExperimentTimeline{
		ExperimentID: experimentID,
		Events:       events,
		TotalEvents:  len(events),
	}, nil
}

// getSuggestedFix provides suggested fixes based on error messages
func (s *AnalyticsService) getSuggestedFix(errorMsg string) string {
	if errorMsg == "" {
		return ""
	}

	// Simple error pattern matching for common issues
	switch {
	case contains(errorMsg, "timeout"):
		return "Consider increasing timeout or checking network connectivity"
	case contains(errorMsg, "permission denied"):
		return "Check file permissions and user access rights"
	case contains(errorMsg, "out of memory"):
		return "Consider reducing memory usage or requesting more resources"
	case contains(errorMsg, "disk full"):
		return "Free up disk space or request additional storage"
	case contains(errorMsg, "connection refused"):
		return "Check if the service is running and accessible"
	default:
		return "Review error details and check system logs"
	}
}

// contains checks if a string contains a substring (case-insensitive)
func contains(s, substr string) bool {
	return len(s) >= len(substr) &&
		(s == substr ||
			len(s) > len(substr) &&
				(s[:len(substr)] == substr ||
					s[len(s)-len(substr):] == substr ||
					indexOfAnalytics(s, substr) >= 0))
}

// indexOf finds the index of a substring in a string
func indexOfAnalytics(s, substr string) int {
	for i := 0; i <= len(s)-len(substr); i++ {
		if s[i:i+len(substr)] == substr {
			return i
		}
	}
	return -1
}
