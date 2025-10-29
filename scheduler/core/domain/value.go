package domain

import "time"

// Domain value objects

// ParameterSet represents a set of parameters for task generation
type ParameterSet struct {
	Values map[string]string `json:"values" validate:"required"`
}

// FileMetadata represents metadata about a file
type FileMetadata struct {
	Path     string `json:"path" validate:"required"`
	Size     int64  `json:"size" validate:"min=0"`
	Checksum string `json:"checksum" validate:"required"`
	Type     string `json:"type,omitempty"` // input, output, intermediate
}

// ResourceRequirements represents resource requirements for an experiment
type ResourceRequirements struct {
	CPUCores int    `json:"cpuCores" validate:"min=1"`
	MemoryMB int    `json:"memoryMB" validate:"min=1"`
	DiskGB   int    `json:"diskGB" validate:"min=0"`
	GPUs     int    `json:"gpus" validate:"min=0"`
	Walltime string `json:"walltime" validate:"required"` // e.g., "2:00:00"
	Priority int    `json:"priority" validate:"min=0,max=10"`
}

// ExperimentConstraints represents constraints for experiment execution
type ExperimentConstraints struct {
	MaxCost            float64   `json:"maxCost" validate:"min=0"`
	Deadline           time.Time `json:"deadline,omitempty"`
	PreferredResources []string  `json:"preferredResources,omitempty"`
	ExcludedResources  []string  `json:"excludedResources,omitempty"`
}

// SchedulingPlan represents the result of experiment scheduling
type SchedulingPlan struct {
	ExperimentID       string                 `json:"experimentId"`
	WorkerDistribution map[string]int         `json:"workerDistribution"` // computeResourceID -> worker count
	EstimatedDuration  time.Duration          `json:"estimatedDuration"`
	EstimatedCost      float64                `json:"estimatedCost"`
	Constraints        []string               `json:"constraints"`
	Metadata           map[string]interface{} `json:"metadata"`
}

// TaskResult represents the result of task execution
type TaskResult struct {
	TaskID        string                 `json:"taskId"`
	Success       bool                   `json:"success"`
	OutputFiles   []FileMetadata         `json:"outputFiles"`
	Duration      time.Duration          `json:"duration"`
	ResourceUsage *ResourceUsage         `json:"resourceUsage"`
	Error         string                 `json:"error,omitempty"`
	Metadata      map[string]interface{} `json:"metadata"`
}

// WorkerStatusInfo represents current worker status and capabilities
type WorkerStatusInfo struct {
	WorkerID            string                 `json:"workerId"`
	ComputeResourceID   string                 `json:"computeResourceId"`
	Status              WorkerStatus           `json:"status"`
	CurrentTaskID       string                 `json:"currentTaskId,omitempty"`
	TasksCompleted      int                    `json:"tasksCompleted"`
	TasksFailed         int                    `json:"tasksFailed"`
	AverageTaskDuration time.Duration          `json:"averageTaskDuration"`
	WalltimeRemaining   time.Duration          `json:"walltimeRemaining"`
	LastHeartbeat       time.Time              `json:"lastHeartbeat"`
	Capabilities        map[string]interface{} `json:"capabilities"`
	Metadata            map[string]interface{} `json:"metadata"`
}

// WorkerMetrics represents worker performance metrics
type WorkerMetrics struct {
	ID                  string            `json:"id" gorm:"primaryKey"`
	WorkerID            string            `json:"workerId" gorm:"column:worker_id;not null"`
	CPUUsagePercent     float64           `json:"cpuUsagePercent" gorm:"column:cpu_usage_percent"`
	MemoryUsagePercent  float64           `json:"memoryUsagePercent" gorm:"column:memory_usage_percent"`
	TasksCompleted      int               `json:"tasksCompleted" gorm:"column:tasks_completed;default:0"`
	TasksFailed         int               `json:"tasksFailed" gorm:"column:tasks_failed;default:0"`
	AverageTaskDuration time.Duration     `json:"averageTaskDuration" gorm:"column:average_task_duration"`
	LastTaskDuration    time.Duration     `json:"lastTaskDuration" gorm:"column:last_task_duration"`
	Uptime              time.Duration     `json:"uptime" gorm:"column:uptime"`
	CustomMetrics       map[string]string `json:"customMetrics" gorm:"column:custom_metrics;type:jsonb"`
	Timestamp           time.Time         `json:"timestamp" gorm:"column:timestamp;default:CURRENT_TIMESTAMP"`
	CreatedAt           time.Time         `json:"createdAt" gorm:"column:created_at;default:CURRENT_TIMESTAMP"`
}

// TableName returns the table name for WorkerMetrics
func (WorkerMetrics) TableName() string {
	return "worker_metrics"
}

// WorkerDistribution represents optimal worker allocation across compute resources
type WorkerDistribution struct {
	ExperimentID        string                 `json:"experimentId"`
	ResourceAllocation  map[string]int         `json:"resourceAllocation"` // computeResourceID -> worker count
	TotalWorkers        int                    `json:"totalWorkers"`
	EstimatedCost       float64                `json:"estimatedCost"`
	EstimatedDuration   time.Duration          `json:"estimatedDuration"`
	OptimizationWeights *CostWeights           `json:"optimizationWeights"`
	Metadata            map[string]interface{} `json:"metadata"`
}

// ResourceUsage represents resource consumption during task execution
type ResourceUsage struct {
	CPUSeconds      float64 `json:"cpuSeconds"`
	MemoryMB        float64 `json:"memoryMB"`
	DiskIOBytes     int64   `json:"diskIOBytes"`
	NetworkIOBytes  int64   `json:"networkIOBytes"`
	GPUSeconds      float64 `json:"gpuSeconds,omitempty"`
	WalltimeSeconds float64 `json:"walltimeSeconds"`
}

// CostWeights represents weights for cost optimization
type CostWeights struct {
	TimeWeight        float64 `json:"timeWeight" validate:"min=0,max=1"`
	CostWeight        float64 `json:"costWeight" validate:"min=0,max=1"`
	ReliabilityWeight float64 `json:"reliabilityWeight" validate:"min=0,max=1"`
}

// CacheEntry represents a cached data entry
type CacheEntry struct {
	FilePath          string    `json:"filePath"`
	Checksum          string    `json:"checksum"`
	ComputeResourceID string    `json:"computeResourceId"`
	SizeBytes         int64     `json:"sizeBytes"`
	CachedAt          time.Time `json:"cachedAt"`
	LastAccessed      time.Time `json:"lastAccessed"`
}

// DataLineageInfo represents the movement history of a file
type DataLineageInfo struct {
	FileID           string                 `json:"fileId"`
	SourcePath       string                 `json:"sourcePath"`
	DestinationPath  string                 `json:"destinationPath"`
	SourceChecksum   string                 `json:"sourceChecksum"`
	DestChecksum     string                 `json:"destChecksum"`
	TransferSize     int64                  `json:"transferSize"`
	TransferDuration time.Duration          `json:"transferDuration"`
	TransferredAt    time.Time              `json:"transferredAt"`
	Metadata         map[string]interface{} `json:"metadata"`
}

// ValidationResult represents the result of experiment validation
type ValidationResult struct {
	Valid    bool     `json:"valid"`
	Errors   []string `json:"errors,omitempty"`
	Warnings []string `json:"warnings,omitempty"`
}
