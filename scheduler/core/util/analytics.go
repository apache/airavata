package types

import (
	"time"
)

// ExperimentSummary represents aggregated experiment statistics
type ExperimentSummary struct {
	ExperimentID      string    `json:"experimentId"`
	ExperimentName    string    `json:"experimentName"`
	Status            string    `json:"status"`
	TotalTasks        int       `json:"totalTasks"`
	CompletedTasks    int       `json:"completedTasks"`
	FailedTasks       int       `json:"failedTasks"`
	RunningTasks      int       `json:"runningTasks"`
	SuccessRate       float64   `json:"successRate"`
	AvgDurationSec    float64   `json:"avgDurationSec"`
	TotalCost         float64   `json:"totalCost"`
	CreatedAt         time.Time `json:"createdAt"`
	UpdatedAt         time.Time `json:"updatedAt"`
	ParameterSetCount int       `json:"parameterSetCount"`
}

// TaskAggregationRequest represents a request for task aggregation
type TaskAggregationRequest struct {
	ExperimentID string `json:"experimentId,omitempty"`
	GroupBy      string `json:"groupBy" validate:"required,oneof=status worker compute_resource parameter_value"`
	Filter       string `json:"filter,omitempty"`
}

// TaskAggregationResponse represents aggregated task statistics
type TaskAggregationResponse struct {
	Groups []TaskAggregationGroup `json:"groups"`
	Total  int                    `json:"total"`
}

// TaskAggregationGroup represents a group in task aggregation
type TaskAggregationGroup struct {
	GroupKey    string  `json:"groupKey"`
	GroupValue  string  `json:"groupValue"`
	Count       int     `json:"count"`
	Completed   int     `json:"completed"`
	Failed      int     `json:"failed"`
	Running     int     `json:"running"`
	SuccessRate float64 `json:"successRate"`
}

// ExperimentTimeline represents chronological task execution timeline
type ExperimentTimeline struct {
	ExperimentID string          `json:"experimentId"`
	Events       []TimelineEvent `json:"events"`
	TotalEvents  int             `json:"totalEvents"`
}

// TimelineEvent represents a single event in the timeline
type TimelineEvent struct {
	EventID     string                 `json:"eventId"`
	EventType   string                 `json:"eventType"` // TASK_CREATED, TASK_STARTED, TASK_COMPLETED, etc.
	TaskID      string                 `json:"taskId,omitempty"`
	WorkerID    string                 `json:"workerId,omitempty"`
	Timestamp   time.Time              `json:"timestamp"`
	Description string                 `json:"description"`
	Metadata    map[string]interface{} `json:"metadata,omitempty"`
}

// ExperimentSearchRequest represents advanced experiment search parameters
type ExperimentSearchRequest struct {
	ProjectID          string            `json:"projectId,omitempty"`
	OwnerID            string            `json:"ownerId,omitempty"`
	Status             string            `json:"status,omitempty"`
	ParameterFilter    string            `json:"parameterFilter,omitempty"` // JSONB query
	CreatedAfter       *time.Time        `json:"createdAfter,omitempty"`
	CreatedBefore      *time.Time        `json:"createdBefore,omitempty"`
	HasFailedTasks     *bool             `json:"hasFailedTasks,omitempty"`
	TaskSuccessRateMin *float64          `json:"taskSuccessRateMin,omitempty"`
	Tags               []string          `json:"tags,omitempty"`
	SortBy             string            `json:"sortBy,omitempty"` // created_at, updated_at, task_count, success_rate
	Order              string            `json:"order,omitempty"`  // asc, desc
	Pagination         PaginationRequest `json:"pagination" validate:"required"`
}

// ExperimentSearchResponse represents the response to experiment search
type ExperimentSearchResponse struct {
	Experiments []ExperimentSummary `json:"experiments"`
	Pagination  PaginationResponse  `json:"pagination"`
	TotalCount  int                 `json:"totalCount"`
}

// FailedTaskInfo represents information about a failed task
type FailedTaskInfo struct {
	TaskID       string            `json:"taskId"`
	TaskName     string            `json:"taskName"`
	ExperimentID string            `json:"experimentId"`
	Status       string            `json:"status"`
	Error        string            `json:"error"`
	RetryCount   int               `json:"retryCount"`
	MaxRetries   int               `json:"maxRetries"`
	LastAttempt  time.Time         `json:"lastAttempt"`
	WorkerID     string            `json:"workerId,omitempty"`
	ParameterSet map[string]string `json:"parameterSet,omitempty"`
	SuggestedFix string            `json:"suggestedFix,omitempty"`
}

// DerivativeExperimentRequest represents a request to create a derivative experiment
type DerivativeExperimentRequest struct {
	SourceExperimentID       string                 `json:"sourceExperimentId" validate:"required"`
	NewExperimentName        string                 `json:"newExperimentName" validate:"required"`
	ParameterModifications   map[string]interface{} `json:"parameterModifications,omitempty"`
	TaskFilter               string                 `json:"taskFilter,omitempty"` // "only_successful", "only_failed", "all"
	PreserveComputeResources bool                   `json:"preserveComputeResources,omitempty"`
	Options                  map[string]interface{} `json:"options,omitempty"`
}

// DerivativeExperimentResponse represents the response to creating a derivative experiment
type DerivativeExperimentResponse struct {
	NewExperimentID    string           `json:"newExperimentId"`
	SourceExperimentID string           `json:"sourceExperimentId"`
	TaskCount          int              `json:"taskCount"`
	ParameterCount     int              `json:"parameterCount"`
	Validation         ValidationResult `json:"validation"`
}

// ExperimentProgress represents real-time experiment progress
type ExperimentProgress struct {
	ExperimentID           string        `json:"experimentId"`
	TotalTasks             int           `json:"totalTasks"`
	CompletedTasks         int           `json:"completedTasks"`
	FailedTasks            int           `json:"failedTasks"`
	RunningTasks           int           `json:"runningTasks"`
	ProgressPercent        float64       `json:"progressPercent"`
	EstimatedTimeRemaining time.Duration `json:"estimatedTimeRemaining,omitempty"`
	LastUpdated            time.Time     `json:"lastUpdated"`
}

// TaskProgress represents real-time task progress
type TaskProgress struct {
	TaskID              string     `json:"taskId"`
	ExperimentID        string     `json:"experimentId"`
	Status              string     `json:"status"`
	ProgressPercent     float64    `json:"progressPercent,omitempty"`
	CurrentStage        string     `json:"currentStage,omitempty"` // STAGING, RUNNING, COMPLETING
	WorkerID            string     `json:"workerId,omitempty"`
	StartedAt           *time.Time `json:"startedAt,omitempty"`
	EstimatedCompletion *time.Time `json:"estimatedCompletion,omitempty"`
	LastUpdated         time.Time  `json:"lastUpdated"`
}
