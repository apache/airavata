package domain

// TaskStatus represents the status of a task
type TaskStatus string

const (
	TaskStatusCreated       TaskStatus = "CREATED"
	TaskStatusQueued        TaskStatus = "QUEUED"
	TaskStatusDataStaging   TaskStatus = "DATA_STAGING"
	TaskStatusEnvSetup      TaskStatus = "ENV_SETUP"
	TaskStatusRunning       TaskStatus = "RUNNING"
	TaskStatusOutputStaging TaskStatus = "OUTPUT_STAGING"
	TaskStatusCompleted     TaskStatus = "COMPLETED"
	TaskStatusFailed        TaskStatus = "FAILED"
	TaskStatusCanceled      TaskStatus = "CANCELED"
)

// WorkerStatus represents the status of a worker
type WorkerStatus string

const (
	WorkerStatusIdle WorkerStatus = "IDLE"
	WorkerStatusBusy WorkerStatus = "BUSY"
)

// StagingStatus represents the status of a staging operation
type StagingStatus string

const (
	StagingStatusPending   StagingStatus = "PENDING"
	StagingStatusRunning   StagingStatus = "RUNNING"
	StagingStatusCompleted StagingStatus = "COMPLETED"
	StagingStatusFailed    StagingStatus = "FAILED"
)

// ExperimentStatus represents the status of an experiment
type ExperimentStatus string

const (
	ExperimentStatusCreated   ExperimentStatus = "CREATED"
	ExperimentStatusExecuting ExperimentStatus = "EXECUTING"
	ExperimentStatusCompleted ExperimentStatus = "COMPLETED"
	ExperimentStatusCanceled  ExperimentStatus = "CANCELED"
)

// ComputeResourceType represents the type of compute resource
type ComputeResourceType string

const (
	ComputeResourceTypeSlurm      ComputeResourceType = "SLURM"
	ComputeResourceTypeKubernetes ComputeResourceType = "KUBERNETES"
	ComputeResourceTypeBareMetal  ComputeResourceType = "BARE_METAL"
)

// StorageResourceType represents the type of storage resource
type StorageResourceType string

const (
	StorageResourceTypeS3   StorageResourceType = "S3"
	StorageResourceTypeSFTP StorageResourceType = "SFTP"
	StorageResourceTypeNFS  StorageResourceType = "NFS"
)

// ResourceStatus represents the status of a resource
type ResourceStatus string

const (
	ResourceStatusActive   ResourceStatus = "ACTIVE"
	ResourceStatusInactive ResourceStatus = "INACTIVE"
	ResourceStatusError    ResourceStatus = "ERROR"
)

// CredentialType represents the type of credential
type CredentialType string

const (
	CredentialTypeSSHKey      CredentialType = "SSH_KEY"
	CredentialTypePassword    CredentialType = "PASSWORD"
	CredentialTypeAPIKey      CredentialType = "API_KEY"
	CredentialTypeToken       CredentialType = "TOKEN"
	CredentialTypeCertificate CredentialType = "CERTIFICATE"
)
