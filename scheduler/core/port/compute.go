package ports

import (
	"context"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
)

// ComputePort defines the interface for compute resource operations
// This abstracts compute implementations from domain services
type ComputePort interface {
	// Worker management
	SpawnWorker(ctx context.Context, req *SpawnWorkerRequest) (*Worker, error)
	TerminateWorker(ctx context.Context, workerID string) error
	GetWorkerStatus(ctx context.Context, workerID string) (*WorkerStatus, error)
	ListWorkers(ctx context.Context) ([]*Worker, error)

	// Job management
	SubmitJob(ctx context.Context, req *SubmitJobRequest) (*Job, error)
	CancelJob(ctx context.Context, jobID string) error
	GetJobStatus(ctx context.Context, jobID string) (*JobStatus, error)
	ListJobs(ctx context.Context, filters *JobFilters) ([]*Job, error)

	// Resource information
	GetResourceInfo(ctx context.Context) (*ResourceInfo, error)
	GetNodeInfo(ctx context.Context, nodeID string) (*NodeInfo, error)
	ListNodes(ctx context.Context) ([]*NodeInfo, error)
	GetQueueInfo(ctx context.Context, queueName string) (*QueueInfo, error)
	ListQueues(ctx context.Context) ([]*QueueInfo, error)

	// Script and task management (merged from ComputeAdapter)
	GenerateScript(task domain.Task, outputDir string) (scriptPath string, err error)
	SubmitTask(ctx context.Context, scriptPath string) (jobID string, err error)
	SubmitTaskWithWorker(ctx context.Context, task *domain.Task, worker *domain.Worker) (string, error)
	GetWorkerMetrics(ctx context.Context, worker *domain.Worker) (*domain.WorkerMetrics, error)

	// Worker spawn script generation
	GenerateWorkerSpawnScript(ctx context.Context, experiment *domain.Experiment, walltime time.Duration) (string, error)

	// Connection management
	Connect(ctx context.Context) error
	Disconnect(ctx context.Context) error
	IsConnected() bool
	Ping(ctx context.Context) error

	// Configuration
	GetConfig() *ComputeConfig
	GetStats(ctx context.Context) (*ComputeStats, error)
	GetType() string
}

// SpawnWorkerRequest represents a request to spawn a worker
type SpawnWorkerRequest struct {
	WorkerID         string                 `json:"workerId"`
	ExperimentID     string                 `json:"experimentId"`
	Command          string                 `json:"command"`
	Walltime         time.Duration          `json:"walltime"`
	CPUCores         int                    `json:"cpuCores"`
	MemoryMB         int                    `json:"memoryMB"`
	DiskGB           int                    `json:"diskGB"`
	GPUs             int                    `json:"gpus"`
	Queue            string                 `json:"queue,omitempty"`
	Priority         int                    `json:"priority,omitempty"`
	Environment      map[string]string      `json:"environment,omitempty"`
	WorkingDirectory string                 `json:"workingDirectory,omitempty"`
	InputFiles       []string               `json:"inputFiles,omitempty"`
	OutputFiles      []string               `json:"outputFiles,omitempty"`
	Metadata         map[string]interface{} `json:"metadata,omitempty"`
}

// Worker represents a compute worker
type Worker struct {
	ID                string                 `json:"id"`
	JobID             string                 `json:"jobId"`
	Status            domain.WorkerStatus    `json:"status"`
	CPUCores          int                    `json:"cpuCores"`
	MemoryMB          int                    `json:"memoryMB"`
	DiskGB            int                    `json:"diskGB"`
	GPUs              int                    `json:"gpus"`
	Walltime          time.Duration          `json:"walltime"`
	WalltimeRemaining time.Duration          `json:"walltimeRemaining"`
	NodeID            string                 `json:"nodeId"`
	Queue             string                 `json:"queue"`
	Priority          int                    `json:"priority"`
	CreatedAt         time.Time              `json:"createdAt"`
	StartedAt         *time.Time             `json:"startedAt,omitempty"`
	CompletedAt       *time.Time             `json:"completedAt,omitempty"`
	Metadata          map[string]interface{} `json:"metadata,omitempty"`
}

// WorkerStatus represents worker status information
type WorkerStatus struct {
	WorkerID            string                 `json:"workerId"`
	Status              domain.WorkerStatus    `json:"status"`
	CPULoad             float64                `json:"cpuLoad"`
	MemoryUsage         float64                `json:"memoryUsage"`
	DiskUsage           float64                `json:"diskUsage"`
	GPUUsage            float64                `json:"gpuUsage,omitempty"`
	WalltimeRemaining   time.Duration          `json:"walltimeRemaining"`
	LastHeartbeat       time.Time              `json:"lastHeartbeat"`
	CurrentTaskID       string                 `json:"currentTaskId,omitempty"`
	TasksCompleted      int                    `json:"tasksCompleted"`
	TasksFailed         int                    `json:"tasksFailed"`
	AverageTaskDuration time.Duration          `json:"averageTaskDuration"`
	Metadata            map[string]interface{} `json:"metadata,omitempty"`
}

// SubmitJobRequest represents a request to submit a job
type SubmitJobRequest struct {
	Name             string                 `json:"name"`
	Command          string                 `json:"command"`
	Walltime         time.Duration          `json:"walltime"`
	CPUCores         int                    `json:"cpuCores"`
	MemoryMB         int                    `json:"memoryMB"`
	DiskGB           int                    `json:"diskGB"`
	GPUs             int                    `json:"gpus"`
	Queue            string                 `json:"queue,omitempty"`
	Priority         int                    `json:"priority,omitempty"`
	Environment      map[string]string      `json:"environment,omitempty"`
	WorkingDirectory string                 `json:"workingDirectory,omitempty"`
	InputFiles       []string               `json:"inputFiles,omitempty"`
	OutputFiles      []string               `json:"outputFiles,omitempty"`
	Dependencies     []string               `json:"dependencies,omitempty"`
	ArraySize        int                    `json:"arraySize,omitempty"`
	Metadata         map[string]interface{} `json:"metadata,omitempty"`
}

// Job represents a compute job
type Job struct {
	ID           string                 `json:"id"`
	Name         string                 `json:"name"`
	Status       JobStatus              `json:"status"`
	CPUCores     int                    `json:"cpuCores"`
	MemoryMB     int                    `json:"memoryMB"`
	DiskGB       int                    `json:"diskGB"`
	GPUs         int                    `json:"gpus"`
	Walltime     time.Duration          `json:"walltime"`
	WalltimeUsed time.Duration          `json:"walltimeUsed"`
	NodeID       string                 `json:"nodeId"`
	Queue        string                 `json:"queue"`
	Priority     int                    `json:"priority"`
	ArrayIndex   int                    `json:"arrayIndex,omitempty"`
	CreatedAt    time.Time              `json:"createdAt"`
	StartedAt    *time.Time             `json:"startedAt,omitempty"`
	CompletedAt  *time.Time             `json:"completedAt,omitempty"`
	ExitCode     int                    `json:"exitCode,omitempty"`
	Error        string                 `json:"error,omitempty"`
	Metadata     map[string]interface{} `json:"metadata,omitempty"`
}

// JobStatus represents job status
type JobStatus string

const (
	JobStatusPending   JobStatus = "PENDING"
	JobStatusRunning   JobStatus = "RUNNING"
	JobStatusCompleted JobStatus = "COMPLETED"
	JobStatusFailed    JobStatus = "FAILED"
	JobStatusCancelled JobStatus = "CANCELLED"
	JobStatusSuspended JobStatus = "SUSPENDED"
)

// JobFilters represents filters for job listing
type JobFilters struct {
	Status        *JobStatus `json:"status,omitempty"`
	Queue         *string    `json:"queue,omitempty"`
	NodeID        *string    `json:"nodeId,omitempty"`
	UserID        *string    `json:"userId,omitempty"`
	CreatedAfter  *time.Time `json:"createdAfter,omitempty"`
	CreatedBefore *time.Time `json:"createdBefore,omitempty"`
	Limit         int        `json:"limit,omitempty"`
	Offset        int        `json:"offset,omitempty"`
}

// ResourceInfo represents compute resource information
type ResourceInfo struct {
	Name              string                     `json:"name"`
	Type              domain.ComputeResourceType `json:"type"`
	Version           string                     `json:"version"`
	TotalNodes        int                        `json:"totalNodes"`
	ActiveNodes       int                        `json:"activeNodes"`
	TotalCPUCores     int                        `json:"totalCpuCores"`
	AvailableCPUCores int                        `json:"availableCpuCores"`
	TotalMemoryGB     int                        `json:"totalMemoryGb"`
	AvailableMemoryGB int                        `json:"availableMemoryGb"`
	TotalDiskGB       int                        `json:"totalDiskGb"`
	AvailableDiskGB   int                        `json:"availableDiskGb"`
	TotalGPUs         int                        `json:"totalGpus"`
	AvailableGPUs     int                        `json:"availableGpus"`
	Queues            []*QueueInfo               `json:"queues"`
	Metadata          map[string]interface{}     `json:"metadata,omitempty"`
}

// NodeInfo represents compute node information
type NodeInfo struct {
	ID          string                 `json:"id"`
	Name        string                 `json:"name"`
	Status      NodeStatus             `json:"status"`
	CPUCores    int                    `json:"cpuCores"`
	MemoryGB    int                    `json:"memoryGb"`
	DiskGB      int                    `json:"diskGb"`
	GPUs        int                    `json:"gpus"`
	CPULoad     float64                `json:"cpuLoad"`
	MemoryUsage float64                `json:"memoryUsage"`
	DiskUsage   float64                `json:"diskUsage"`
	GPUUsage    float64                `json:"gpuUsage,omitempty"`
	ActiveJobs  int                    `json:"activeJobs"`
	QueuedJobs  int                    `json:"queuedJobs"`
	LastUpdate  time.Time              `json:"lastUpdate"`
	Metadata    map[string]interface{} `json:"metadata,omitempty"`
}

// NodeStatus represents node status
type NodeStatus string

const (
	NodeStatusUp          NodeStatus = "UP"
	NodeStatusDown        NodeStatus = "DOWN"
	NodeStatusDraining    NodeStatus = "DRAINING"
	NodeStatusMaintenance NodeStatus = "MAINTENANCE"
)

// QueueInfo represents queue information
type QueueInfo struct {
	Name           string                 `json:"name"`
	Status         QueueStatus            `json:"status"`
	MaxWalltime    time.Duration          `json:"maxWalltime"`
	MaxCPUCores    int                    `json:"maxCpuCores"`
	MaxMemoryMB    int                    `json:"maxMemoryMb"`
	MaxDiskGB      int                    `json:"maxDiskGb"`
	MaxGPUs        int                    `json:"maxGpus"`
	MaxJobs        int                    `json:"maxJobs"`
	MaxJobsPerUser int                    `json:"maxJobsPerUser"`
	Priority       int                    `json:"priority"`
	ActiveJobs     int                    `json:"activeJobs"`
	QueuedJobs     int                    `json:"queuedJobs"`
	RunningJobs    int                    `json:"runningJobs"`
	PendingJobs    int                    `json:"pendingJobs"`
	Metadata       map[string]interface{} `json:"metadata,omitempty"`
}

// QueueStatus represents queue status
type QueueStatus string

const (
	QueueStatusActive   QueueStatus = "ACTIVE"
	QueueStatusInactive QueueStatus = "INACTIVE"
	QueueStatusDraining QueueStatus = "DRAINING"
)

// ComputeConfig represents compute resource configuration
type ComputeConfig struct {
	Type         string                 `json:"type"`
	Endpoint     string                 `json:"endpoint"`
	Credentials  map[string]string      `json:"credentials"`
	DefaultQueue string                 `json:"defaultQueue"`
	MaxRetries   int                    `json:"maxRetries"`
	Timeout      time.Duration          `json:"timeout"`
	PollInterval time.Duration          `json:"pollInterval"`
	Metadata     map[string]interface{} `json:"metadata,omitempty"`
}

// ComputeStats represents compute resource statistics
type ComputeStats struct {
	TotalJobs       int64         `json:"totalJobs"`
	ActiveJobs      int64         `json:"activeJobs"`
	CompletedJobs   int64         `json:"completedJobs"`
	FailedJobs      int64         `json:"failedJobs"`
	CancelledJobs   int64         `json:"cancelledJobs"`
	AverageJobTime  time.Duration `json:"averageJobTime"`
	TotalCPUTime    time.Duration `json:"totalCpuTime"`
	TotalWalltime   time.Duration `json:"totalWalltime"`
	UtilizationRate float64       `json:"utilizationRate"`
	ErrorRate       float64       `json:"errorRate"`
	Uptime          time.Duration `json:"uptime"`
	LastActivity    time.Time     `json:"lastActivity"`
}

// ComputeFactory defines the interface for creating compute instances
type ComputeFactory interface {
	CreateCompute(ctx context.Context, config *ComputeConfig) (ComputePort, error)
	GetSupportedTypes() []domain.ComputeResourceType
	ValidateConfig(config *ComputeConfig) error
}

// ComputeValidator defines the interface for compute validation
type ComputeValidator interface {
	ValidateConnection(ctx context.Context, compute ComputePort) error
	ValidatePermissions(ctx context.Context, compute ComputePort) error
	ValidatePerformance(ctx context.Context, compute ComputePort) error
}

// ComputeMonitor defines the interface for compute monitoring
type ComputeMonitor interface {
	StartMonitoring(ctx context.Context, compute ComputePort) error
	StopMonitoring(ctx context.Context, compute ComputePort) error
	GetMetrics(ctx context.Context, compute ComputePort) (*ComputeMetrics, error)
}

// ComputeMetrics represents detailed compute metrics
type ComputeMetrics struct {
	JobsSubmitted     int64         `json:"jobsSubmitted"`
	JobsCompleted     int64         `json:"jobsCompleted"`
	JobsFailed        int64         `json:"jobsFailed"`
	JobsCancelled     int64         `json:"jobsCancelled"`
	WorkersSpawned    int64         `json:"workersSpawned"`
	WorkersTerminated int64         `json:"workersTerminated"`
	AverageJobTime    time.Duration `json:"averageJobTime"`
	AverageQueueTime  time.Duration `json:"averageQueueTime"`
	TotalCPUTime      time.Duration `json:"totalCpuTime"`
	TotalWalltime     time.Duration `json:"totalWalltime"`
	ErrorCount        int64         `json:"errorCount"`
	LastError         time.Time     `json:"lastError"`
	LastErrorMsg      string        `json:"lastErrorMsg"`
}
