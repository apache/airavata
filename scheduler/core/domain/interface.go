package domain

import (
	"context"
	"io"
	"time"
)

// ResourceRegistry defines the interface for managing compute and storage resources
// This is the foundation for resource discovery and management in the scheduler
type ResourceRegistry interface {
	// RegisterComputeResource adds a new compute resource to the system
	// Returns the registered resource with assigned ID and validation results
	RegisterComputeResource(ctx context.Context, req *CreateComputeResourceRequest) (*CreateComputeResourceResponse, error)

	// RegisterStorageResource adds a new storage resource to the system
	// Returns the registered resource with assigned ID and validation results
	RegisterStorageResource(ctx context.Context, req *CreateStorageResourceRequest) (*CreateStorageResourceResponse, error)

	// ListResources retrieves available resources with optional filtering
	// Supports filtering by type, status, and ownership
	ListResources(ctx context.Context, req *ListResourcesRequest) (*ListResourcesResponse, error)

	// GetResource retrieves a specific resource by ID
	// Returns both compute and storage resources based on ID lookup
	GetResource(ctx context.Context, req *GetResourceRequest) (*GetResourceResponse, error)

	// UpdateResource modifies an existing resource's configuration
	// Supports updating status, credentials, and metadata
	UpdateResource(ctx context.Context, req *UpdateResourceRequest) (*UpdateResourceResponse, error)

	// DeleteResource removes a resource from the system
	// Supports force deletion for resources with active tasks
	DeleteResource(ctx context.Context, req *DeleteResourceRequest) (*DeleteResourceResponse, error)

	// ValidateResourceConnection tests connectivity to a resource
	// Verifies credentials and basic functionality
	ValidateResourceConnection(ctx context.Context, resourceID string, userID string) error
}

// CredentialVault defines the interface for secure credential storage and permission management
// Implements Unix-style permissions (owner/group/other) for credential access
type CredentialVault interface {
	// StoreCredential securely stores a credential with encryption
	// Returns the credential ID and encryption metadata
	StoreCredential(ctx context.Context, name string, credentialType CredentialType, data []byte, ownerID string) (*Credential, error)

	// RetrieveCredential retrieves and decrypts a credential
	// Checks permissions before returning decrypted data
	RetrieveCredential(ctx context.Context, credentialID string, userID string) (*Credential, []byte, error)

	// UpdateCredential modifies an existing credential
	// Requires appropriate permissions and re-encrypts data
	UpdateCredential(ctx context.Context, credentialID string, data []byte, userID string) (*Credential, error)

	// DeleteCredential removes a credential from the vault
	// Requires owner permissions or admin access
	DeleteCredential(ctx context.Context, credentialID string, userID string) error

	// ListCredentials returns credentials accessible to a user
	// Respects Unix-style permissions (owner/group/other)
	ListCredentials(ctx context.Context, userID string) ([]*Credential, error)

	// ShareCredential grants access to a credential for a user or group
	// Implements Unix-style permission model
	ShareCredential(ctx context.Context, credentialID string, targetUserID, targetGroupID string, permissions string, userID string) error

	// RevokeCredentialAccess removes access to a credential
	// Supports revoking from specific users or groups
	RevokeCredentialAccess(ctx context.Context, credentialID string, targetUserID, targetGroupID string, userID string) error

	// RotateCredential generates a new encryption key for a credential
	// Re-encrypts existing data with new key
	RotateCredential(ctx context.Context, credentialID string, userID string) error

	// GetUsableCredentialForResource retrieves a usable credential for a specific resource
	// Returns credential data that can be used to access the resource
	GetUsableCredentialForResource(ctx context.Context, resourceID, resourceType, userID string, metadata map[string]interface{}) (*Credential, []byte, error)

	// CheckPermission checks if a user has a specific permission on an object
	CheckPermission(ctx context.Context, userID, objectID, objectType, permission string) (bool, error)

	// GetUsableCredentialsForResource returns credentials bound to a resource that the user can access
	GetUsableCredentialsForResource(ctx context.Context, userID, resourceID, resourceType, permission string) ([]string, error)

	// BindCredentialToResource binds a credential to a resource using SpiceDB
	BindCredentialToResource(ctx context.Context, credentialID, resourceID, resourceType string) error
}

// ExperimentOrchestrator defines the interface for experiment lifecycle management
// Handles experiment creation, task generation, and submission to the scheduler
type ExperimentOrchestrator interface {
	// CreateExperiment creates a new experiment with parameter sets
	// Validates experiment specification and generates initial task set
	CreateExperiment(ctx context.Context, req *CreateExperimentRequest, userID string) (*CreateExperimentResponse, error)

	// GetExperiment retrieves experiment details and current status
	// Supports including tasks, metadata, and execution history
	GetExperiment(ctx context.Context, req *GetExperimentRequest) (*GetExperimentResponse, error)

	// ListExperiments returns experiments accessible to a user
	// Supports filtering by project, owner, and status
	ListExperiments(ctx context.Context, req *ListExperimentsRequest) (*ListExperimentsResponse, error)

	// UpdateExperiment modifies experiment configuration
	// Supports updating metadata, requirements, and constraints
	UpdateExperiment(ctx context.Context, req *UpdateExperimentRequest) (*UpdateExperimentResponse, error)

	// DeleteExperiment removes an experiment from the system
	// Supports force deletion for running experiments
	DeleteExperiment(ctx context.Context, req *DeleteExperimentRequest) (*DeleteExperimentResponse, error)

	// SubmitExperiment submits an experiment for execution
	// Generates tasks from parameter sets and queues for scheduling
	SubmitExperiment(ctx context.Context, req *SubmitExperimentRequest) (*SubmitExperimentResponse, error)

	// GenerateTasks creates individual tasks from experiment parameters
	// Applies command templates and output patterns to parameter sets
	GenerateTasks(ctx context.Context, experimentID string) ([]*Task, error)

	// ValidateExperiment checks experiment specification for errors
	// Validates parameters, resources, and constraints
	ValidateExperiment(ctx context.Context, experimentID string) (*ValidationResult, error)
}

// TaskScheduler defines the interface for cost-based task scheduling and worker management
// Implements the core scheduling algorithm with state machine and cost optimization
type TaskScheduler interface {
	// ScheduleExperiment determines optimal worker distribution for an experiment
	// Uses cost-based optimization considering compute resources, data location, and constraints
	ScheduleExperiment(ctx context.Context, experimentID string) (*SchedulingPlan, error)

	// AssignTask atomically assigns a task to an available worker
	// Implements distributed consistency to prevent duplicate execution
	AssignTask(ctx context.Context, workerID string) (*Task, error)

	// CompleteTask marks a task as completed and releases worker resources
	// Updates metrics and triggers next task assignment
	CompleteTask(ctx context.Context, taskID string, workerID string, result *TaskResult) error

	// FailTask marks a task as failed and handles retry logic
	// Implements exponential backoff and maximum retry limits
	FailTask(ctx context.Context, taskID string, workerID string, error string) error

	// GetWorkerStatus returns current status and metrics for a worker
	// Includes task queue, performance metrics, and health status
	GetWorkerStatus(ctx context.Context, workerID string) (*WorkerStatusInfo, error)

	// UpdateWorkerMetrics updates worker performance and health metrics
	// Used for cost calculation and load balancing
	UpdateWorkerMetrics(ctx context.Context, workerID string, metrics *WorkerMetrics) error

	// CalculateOptimalDistribution determines best worker allocation across compute resources
	// Uses multi-objective optimization with user-configurable weights
	CalculateOptimalDistribution(ctx context.Context, experimentID string) (*WorkerDistribution, error)

	// HandleWorkerFailure manages worker failure recovery and task reassignment
	// Implements automatic task reassignment and worker respawn logic
	HandleWorkerFailure(ctx context.Context, workerID string) error

	// OnStagingComplete handles completion of data staging for a task
	// Transitions task from staging to ready for execution
	OnStagingComplete(ctx context.Context, taskID string) error
}

// DataMover defines the interface for 3-hop data staging with persistent caching
// Implements intelligent data movement with checksum-based caching and lineage tracking
type DataMover interface {
	// StageInputToWorker stages input data to worker's local filesystem
	// Implements 3-hop architecture: Central → Compute Storage → Worker
	// Uses persistent cache to avoid re-transferring identical files
	StageInputToWorker(ctx context.Context, task *Task, workerID string, userID string) error

	// StageOutputFromWorker stages output data from worker back to central storage
	// Implements 3-hop architecture: Worker → Compute Storage → Central
	// Records lineage for complete data movement history
	StageOutputFromWorker(ctx context.Context, task *Task, workerID string, userID string) error

	// CheckCache verifies if data is already available at compute resource
	// Uses checksum-based comparison to determine cache hits
	CheckCache(ctx context.Context, filePath string, checksum string, computeResourceID string) (*CacheEntry, error)

	// RecordCacheEntry stores information about cached data location
	// Tracks file location, checksum, and access metadata
	RecordCacheEntry(ctx context.Context, entry *CacheEntry) error

	// RecordDataLineage tracks complete file movement history
	// Records every transfer from origin to final destination
	RecordDataLineage(ctx context.Context, lineage *DataLineageInfo) error

	// GetDataLineage retrieves complete movement history for a file
	// Returns chronological list of all transfers and locations
	GetDataLineage(ctx context.Context, fileID string) ([]*DataLineageInfo, error)

	// VerifyDataIntegrity validates file integrity using checksums
	// Compares source and destination checksums for data corruption detection
	VerifyDataIntegrity(ctx context.Context, filePath string, expectedChecksum string) (bool, error)

	// CleanupWorkerData removes temporary files from worker after task completion
	// Implements safe cleanup with verification of successful staging
	CleanupWorkerData(ctx context.Context, taskID string, workerID string) error

	// BeginProactiveStaging starts proactive data staging for a task
	// Returns a staging operation that can be monitored for progress
	BeginProactiveStaging(ctx context.Context, taskID string, computeResourceID string, userID string) (*StagingOperation, error)

	// GenerateSignedURLsForTask generates signed URLs for input files
	// Returns time-limited URLs for workers to download input data
	GenerateSignedURLsForTask(ctx context.Context, taskID string, computeResourceID string) ([]SignedURL, error)

	// GenerateUploadURLsForTask generates signed URLs for output file uploads
	// Returns time-limited URLs for workers to upload output data
	GenerateUploadURLsForTask(ctx context.Context, taskID string) ([]SignedURL, error)

	// ListExperimentOutputs lists all output files for an experiment
	// Returns list of output files with metadata (path, size, checksum, task_id)
	ListExperimentOutputs(ctx context.Context, experimentID string) ([]FileMetadata, error)

	// GetExperimentOutputArchive creates an archive of all experiment outputs
	// Returns a reader for the archive (zip/tar.gz) containing all output files
	GetExperimentOutputArchive(ctx context.Context, experimentID string) (io.Reader, error)

	// GetFile retrieves a file from storage
	// Returns a reader for the specified file path
	GetFile(ctx context.Context, filePath string) (io.Reader, error)
}

// WorkerGRPCService defines the interface for worker gRPC communication
// Note: Pull-based model - workers request tasks via heartbeat, no push-based assignment
type WorkerGRPCService interface {
	// SetScheduler sets the scheduler service (for dependency injection)
	SetScheduler(scheduler TaskScheduler)

	// ShutdownWorker sends a shutdown command to a specific worker
	ShutdownWorker(workerID string, reason string, graceful bool) error
}

// WorkerLifecycle defines the interface for worker spawning, polling, and lifecycle management
// Handles worker creation, health monitoring, and graceful termination
type WorkerLifecycle interface {
	// SpawnWorker creates a new worker on a compute resource
	// Uses appropriate adapter (SLURM, bare metal, Kubernetes) based on resource type
	SpawnWorker(ctx context.Context, computeResourceID string, experimentID string, walltime time.Duration) (*Worker, error)

	// RegisterWorker registers a worker with the scheduler
	// Establishes heartbeat mechanism and task polling loop
	RegisterWorker(ctx context.Context, worker *Worker) error

	// StartWorkerPolling begins the worker's task polling loop
	// Implements atomic task claiming and execution coordination
	StartWorkerPolling(ctx context.Context, workerID string) error

	// StopWorkerPolling gracefully stops worker polling and completes current task
	// Ensures no tasks are left in inconsistent state
	StopWorkerPolling(ctx context.Context, workerID string) error

	// TerminateWorker forcefully terminates a worker and reassigns tasks
	// Used for walltime expiration or failure scenarios
	TerminateWorker(ctx context.Context, workerID string, reason string) error

	// SendHeartbeat updates worker status and health metrics
	// Used for worker health monitoring and failure detection
	SendHeartbeat(ctx context.Context, workerID string, metrics *WorkerMetrics) error

	// GetWorkerMetrics retrieves current performance metrics for a worker
	// Includes task completion rates, resource usage, and health status
	GetWorkerMetrics(ctx context.Context, workerID string) (*WorkerMetrics, error)

	// CheckWalltimeRemaining verifies if worker has sufficient time for task execution
	// Used for task assignment decisions and graceful shutdown planning
	CheckWalltimeRemaining(ctx context.Context, workerID string, estimatedDuration time.Duration) (bool, time.Duration, error)

	// ReuseWorker assigns a new task to an existing idle worker
	// Optimizes resource utilization by avoiding unnecessary worker creation
	ReuseWorker(ctx context.Context, workerID string, taskID string) error
}
