package domain

import (
	"time"
)

// Core domain entities

// Experiment represents a computational experiment with parameter sets
type Experiment struct {
	ID               string                 `json:"id" gorm:"primaryKey" validate:"required"`
	Name             string                 `json:"name" gorm:"index" validate:"required"`
	Description      string                 `json:"description"`
	ProjectID        string                 `json:"projectId" gorm:"index" validate:"required"`
	OwnerID          string                 `json:"ownerId" gorm:"index" validate:"required"`
	Status           ExperimentStatus       `json:"status" gorm:"index" validate:"required"`
	CommandTemplate  string                 `json:"commandTemplate" validate:"required"`
	OutputPattern    string                 `json:"outputPattern"`
	TaskTemplate     string                 `json:"taskTemplate"`     // JSONB: Dynamic task template
	GeneratedTasks   string                 `json:"generatedTasks"`   // JSONB: Generated task specifications
	ExecutionSummary string                 `json:"executionSummary"` // JSONB: Execution summary and metrics
	Parameters       []ParameterSet         `json:"parameters" gorm:"serializer:json"`
	Requirements     *ResourceRequirements  `json:"requirements" gorm:"serializer:json"`
	Constraints      *ExperimentConstraints `json:"constraints" gorm:"serializer:json"`
	CreatedAt        time.Time              `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
	UpdatedAt        time.Time              `json:"updatedAt" gorm:"autoUpdateTime" validate:"required"`
	StartedAt        *time.Time             `json:"startedAt,omitempty"`
	CompletedAt      *time.Time             `json:"completedAt,omitempty"`
	Metadata         map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// Task represents an individual computational task within an experiment
type Task struct {
	ID                      string                 `json:"id" gorm:"primaryKey" validate:"required"`
	ExperimentID            string                 `json:"experimentId" gorm:"index" validate:"required"`
	Status                  TaskStatus             `json:"status" gorm:"index" validate:"required"`
	Command                 string                 `json:"command" validate:"required"`
	ExecutionScript         string                 `json:"executionScript,omitempty" gorm:"column:execution_script"`
	InputFiles              []FileMetadata         `json:"inputFiles" gorm:"serializer:json"`
	OutputFiles             []FileMetadata         `json:"outputFiles" gorm:"serializer:json"`
	ResultSummary           string                 `json:"resultSummary"`           // JSONB: Task result summary
	ExecutionMetrics        string                 `json:"executionMetrics"`        // JSONB: Execution metrics
	WorkerAssignmentHistory string                 `json:"workerAssignmentHistory"` // JSONB: Worker assignment history
	WorkerID                string                 `json:"workerId,omitempty" gorm:"index"`
	ComputeResourceID       string                 `json:"computeResourceId,omitempty" gorm:"index"`
	RetryCount              int                    `json:"retryCount" validate:"min=0"`
	MaxRetries              int                    `json:"maxRetries" validate:"min=0"`
	CreatedAt               time.Time              `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
	UpdatedAt               time.Time              `json:"updatedAt" gorm:"autoUpdateTime" validate:"required"`
	StartedAt               *time.Time             `json:"startedAt,omitempty"`
	CompletedAt             *time.Time             `json:"completedAt,omitempty"`
	StagingStartedAt        *time.Time             `json:"stagingStartedAt,omitempty"`
	StagingCompletedAt      *time.Time             `json:"stagingCompletedAt,omitempty"`
	Duration                *time.Duration         `json:"duration,omitempty" gorm:"type:bigint"`
	Error                   string                 `json:"error,omitempty"`
	Metadata                map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// Worker represents a computational worker that executes tasks
type Worker struct {
	ID                string                 `json:"id" gorm:"primaryKey" validate:"required"`
	ComputeResourceID string                 `json:"computeResourceId" gorm:"index" validate:"required"`
	ExperimentID      string                 `json:"experimentId" gorm:"index" validate:"required"`
	UserID            string                 `json:"userId" gorm:"index" validate:"required"`
	Status            WorkerStatus           `json:"status" gorm:"index" validate:"required"`
	CurrentTaskID     string                 `json:"currentTaskId,omitempty" gorm:"index"`
	ConnectionState   string                 `json:"connectionState" gorm:"column:connection_state;default:DISCONNECTED"`
	LastSeenAt        *time.Time             `json:"lastSeenAt,omitempty" gorm:"column:last_seen_at"`
	Walltime          time.Duration          `json:"walltime" validate:"required"`
	WalltimeRemaining time.Duration          `json:"walltimeRemaining"`
	RegisteredAt      time.Time              `json:"registeredAt" gorm:"autoCreateTime" validate:"required"`
	LastHeartbeat     time.Time              `json:"lastHeartbeat" validate:"required"`
	CreatedAt         time.Time              `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
	UpdatedAt         time.Time              `json:"updatedAt" gorm:"autoUpdateTime" validate:"required"`
	StartedAt         *time.Time             `json:"startedAt,omitempty"`
	TerminatedAt      *time.Time             `json:"terminatedAt,omitempty"`
	Metadata          map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// ComputeResource represents a computational resource (SLURM, Kubernetes, etc.)
type ComputeResource struct {
	ID             string                 `json:"id" gorm:"primaryKey" validate:"required"`
	Name           string                 `json:"name" gorm:"index" validate:"required"`
	Type           ComputeResourceType    `json:"type" gorm:"index" validate:"required"`
	Endpoint       string                 `json:"endpoint" validate:"required"`
	OwnerID        string                 `json:"ownerId" gorm:"index" validate:"required"`
	Status         ResourceStatus         `json:"status" gorm:"index" validate:"required"`
	CostPerHour    float64                `json:"costPerHour" validate:"min=0"`
	MaxWorkers     int                    `json:"maxWorkers" validate:"min=1"`
	CurrentWorkers int                    `json:"currentWorkers" validate:"min=0"`
	SSHKeyPath     string                 `json:"sshKeyPath,omitempty"`
	Port           int                    `json:"port,omitempty"`
	Capabilities   map[string]interface{} `json:"capabilities,omitempty" gorm:"serializer:json"`
	CreatedAt      time.Time              `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
	UpdatedAt      time.Time              `json:"updatedAt" gorm:"autoUpdateTime" validate:"required"`
	Metadata       map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// StorageResource represents a storage resource (S3, SFTP, NFS, etc.)
type StorageResource struct {
	ID                string                 `json:"id" gorm:"primaryKey" validate:"required"`
	Name              string                 `json:"name" gorm:"index" validate:"required"`
	Type              StorageResourceType    `json:"type" gorm:"index" validate:"required"`
	Endpoint          string                 `json:"endpoint" validate:"required"`
	OwnerID           string                 `json:"ownerId" gorm:"index" validate:"required"`
	Status            ResourceStatus         `json:"status" gorm:"index" validate:"required"`
	TotalCapacity     *int64                 `json:"totalCapacity,omitempty" validate:"omitempty,min=0"`     // in bytes
	UsedCapacity      *int64                 `json:"usedCapacity,omitempty" validate:"omitempty,min=0"`      // in bytes
	AvailableCapacity *int64                 `json:"availableCapacity,omitempty" validate:"omitempty,min=0"` // in bytes
	Region            string                 `json:"region,omitempty"`
	Zone              string                 `json:"zone,omitempty"`
	CreatedAt         time.Time              `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
	UpdatedAt         time.Time              `json:"updatedAt" gorm:"autoUpdateTime" validate:"required"`
	Metadata          map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// Credential represents a stored credential with encryption
type Credential struct {
	ID          string         `json:"id" gorm:"primaryKey" validate:"required"`
	Name        string         `json:"name" gorm:"index" validate:"required"`
	Type        CredentialType `json:"type" gorm:"index" validate:"required"`
	OwnerID     string         `json:"ownerId" gorm:"index" validate:"required"`
	OwnerUID    int            `json:"ownerUid" gorm:"index"`
	GroupGID    int            `json:"groupGid" gorm:"index"`
	Permissions string         `json:"permissions"` // e.g., "rw-r-----"
	// Note: EncryptedData and ACL entries are now managed by OpenBao and SpiceDB
	CreatedAt time.Time              `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
	UpdatedAt time.Time              `json:"updatedAt" gorm:"autoUpdateTime" validate:"required"`
	Metadata  map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// User represents a system user
type User struct {
	ID           string                 `json:"id" gorm:"primaryKey" validate:"required"`
	Username     string                 `json:"username" gorm:"uniqueIndex" validate:"required"`
	Email        string                 `json:"email" gorm:"uniqueIndex" validate:"required,email"`
	PasswordHash string                 `json:"-" gorm:"column:password_hash"` // Hidden from JSON
	FullName     string                 `json:"fullName" validate:"required"`
	IsActive     bool                   `json:"isActive" gorm:"index" validate:"required"`
	UID          int                    `json:"uid" gorm:"index"`
	GID          int                    `json:"gid" gorm:"index"`
	CreatedAt    time.Time              `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
	UpdatedAt    time.Time              `json:"updatedAt" gorm:"autoUpdateTime" validate:"required"`
	Metadata     map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// Project represents a research project
type Project struct {
	ID          string                 `json:"id" gorm:"primaryKey" validate:"required"`
	Name        string                 `json:"name" gorm:"index" validate:"required"`
	Description string                 `json:"description"`
	OwnerID     string                 `json:"ownerId" gorm:"index" validate:"required"`
	IsActive    bool                   `json:"isActive" gorm:"index" validate:"required"`
	CreatedAt   time.Time              `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
	UpdatedAt   time.Time              `json:"updatedAt" gorm:"autoUpdateTime" validate:"required"`
	Metadata    map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// Group represents a user group for sharing resources
type Group struct {
	ID          string                 `json:"id" gorm:"primaryKey" validate:"required"`
	Name        string                 `json:"name" gorm:"index" validate:"required"`
	Description string                 `json:"description"`
	OwnerID     string                 `json:"ownerId" gorm:"index" validate:"required"`
	IsActive    bool                   `json:"isActive" gorm:"index" validate:"required"`
	CreatedAt   time.Time              `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
	UpdatedAt   time.Time              `json:"updatedAt" gorm:"autoUpdateTime" validate:"required"`
	Metadata    map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// GroupMembership represents membership in a group
type GroupMembership struct {
	ID         string    `json:"id" gorm:"primaryKey" validate:"required"`
	MemberType string    `json:"memberType" gorm:"index" validate:"required"` // USER, GROUP
	MemberID   string    `json:"memberId" gorm:"index" validate:"required"`   // UserID or GroupID
	GroupID    string    `json:"groupId" gorm:"index" validate:"required"`
	Role       string    `json:"role" gorm:"index" validate:"required"` // MEMBER, ADMIN
	JoinedAt   time.Time `json:"joinedAt" gorm:"autoCreateTime" validate:"required"`
}

// DataCache represents cached data at compute resources
type DataCache struct {
	ID                string    `json:"id" gorm:"primaryKey" validate:"required"`
	FilePath          string    `json:"filePath" gorm:"column:file_path;index" validate:"required"`
	Checksum          string    `json:"checksum" gorm:"index" validate:"required"`
	ComputeResourceID string    `json:"computeResourceId" gorm:"column:compute_resource_id;index" validate:"required"`
	StorageResourceID string    `json:"storageResourceId" gorm:"column:storage_resource_id;index" validate:"required"`
	LocationType      string    `json:"locationType" gorm:"column:location_type" validate:"required"`
	SizeBytes         int64     `json:"sizeBytes" gorm:"column:size_bytes" validate:"min=0"`
	CachedAt          time.Time `json:"cachedAt" gorm:"column:cached_at;autoCreateTime" validate:"required"`
	LastAccessed      time.Time `json:"lastAccessed" gorm:"column:last_verified;autoUpdateTime" validate:"required"`
}

// TableName returns the table name for DataCache
func (DataCache) TableName() string {
	return "data_cache"
}

// DataLineageRecord represents the movement history of a file
type DataLineageRecord struct {
	ID               string                 `json:"id" gorm:"primaryKey" validate:"required"`
	FileID           string                 `json:"fileId" gorm:"column:file_id;index" validate:"required"`
	SourcePath       string                 `json:"sourcePath" gorm:"column:source_location" validate:"required"`
	DestinationPath  string                 `json:"destinationPath" gorm:"column:destination_location" validate:"required"`
	SourceChecksum   string                 `json:"sourceChecksum" gorm:"column:source_checksum" validate:"required"`
	DestChecksum     string                 `json:"destChecksum" gorm:"column:destination_checksum" validate:"required"`
	TransferType     string                 `json:"transferType" gorm:"column:transfer_type" validate:"required"`
	TaskID           string                 `json:"taskId" gorm:"column:task_id"`
	WorkerID         string                 `json:"workerId" gorm:"column:worker_id"`
	TransferSize     int64                  `json:"transferSize" gorm:"column:size_bytes" validate:"min=0"`
	TransferDuration time.Duration          `json:"transferDuration" gorm:"column:duration_ms" validate:"min=0"`
	Success          bool                   `json:"success" gorm:"default:true"`
	ErrorMessage     string                 `json:"errorMessage" gorm:"column:error_message"`
	TransferredAt    time.Time              `json:"transferredAt" gorm:"column:transferred_at;autoCreateTime" validate:"required"`
	Metadata         map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// TableName returns the table name for DataLineageRecord
func (DataLineageRecord) TableName() string {
	return "data_lineage"
}

// AuditLog represents system audit events
type AuditLog struct {
	ID         string                 `json:"id" gorm:"primaryKey" validate:"required"`
	UserID     string                 `json:"userId" gorm:"index" validate:"required"`
	Action     string                 `json:"action" gorm:"index" validate:"required"`
	Resource   string                 `json:"resource" gorm:"index" validate:"required"`
	ResourceID string                 `json:"resourceId" gorm:"index"`
	Details    string                 `json:"details"` // JSONB: Action details
	IPAddress  string                 `json:"ipAddress"`
	UserAgent  string                 `json:"userAgent"`
	Timestamp  time.Time              `json:"timestamp" gorm:"autoCreateTime" validate:"required"`
	Metadata   map[string]interface{} `json:"metadata,omitempty" gorm:"serializer:json"`
}

// ExperimentTag represents tags for experiment categorization
type ExperimentTag struct {
	ID           string    `json:"id" gorm:"primaryKey" validate:"required"`
	ExperimentID string    `json:"experimentId" gorm:"index" validate:"required"`
	Tag          string    `json:"tag" gorm:"index" validate:"required"`
	CreatedAt    time.Time `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
}

// TaskResultAggregate represents pre-computed task result aggregates
type TaskResultAggregate struct {
	ID            string    `json:"id" gorm:"primaryKey" validate:"required"`
	ExperimentID  string    `json:"experimentId" gorm:"index" validate:"required"`
	AggregateType string    `json:"aggregateType" gorm:"index" validate:"required"` // SUCCESS_RATE, AVG_DURATION, etc.
	Value         float64   `json:"value" validate:"required"`
	Count         int       `json:"count" validate:"min=0"`
	ComputedAt    time.Time `json:"computedAt" gorm:"autoCreateTime" validate:"required"`
}

// Note: CredentialACL and CredentialResourceBinding are now managed by SpiceDB

// UserGroupMembership represents direct user membership in groups
type UserGroupMembership struct {
	ID        string    `json:"id" gorm:"primaryKey" validate:"required"`
	UserID    string    `json:"userId" gorm:"index" validate:"required"`
	GroupID   string    `json:"groupId" gorm:"index" validate:"required"`
	CreatedAt time.Time `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
}

// GroupGroupMembership represents nested group memberships
type GroupGroupMembership struct {
	ID            string    `json:"id" gorm:"primaryKey" validate:"required"`
	ParentGroupID string    `json:"parentGroupId" gorm:"index" validate:"required"`
	ChildGroupID  string    `json:"childGroupId" gorm:"index" validate:"required"`
	CreatedAt     time.Time `json:"createdAt" gorm:"autoCreateTime" validate:"required"`
}

// SignedURL represents a time-limited signed URL for data access
type SignedURL struct {
	SourcePath string    `json:"sourcePath"`
	URL        string    `json:"url"`
	LocalPath  string    `json:"localPath"`
	ExpiresAt  time.Time `json:"expiresAt"`
	Method     string    `json:"method"` // GET, PUT, etc
}

// StagingOperation represents an ongoing data staging operation
type StagingOperation struct {
	ID                string                 `json:"id"`
	TaskID            string                 `json:"taskId"`
	ComputeResourceID string                 `json:"computeResourceId"`
	Status            string                 `json:"status"`
	TotalFiles        int                    `json:"totalFiles"`
	CompletedFiles    int                    `json:"completedFiles"`
	FailedFiles       int                    `json:"failedFiles"`
	TotalBytes        int64                  `json:"totalBytes"`
	TransferredBytes  int64                  `json:"transferredBytes"`
	StartTime         time.Time              `json:"startTime"`
	EndTime           *time.Time             `json:"endTime,omitempty"`
	Error             string                 `json:"error,omitempty"`
	Metadata          map[string]interface{} `json:"metadata,omitempty"`
}

// TaskAssignment represents a task assignment to a worker
type TaskAssignment struct {
	TaskId          string                 `json:"taskId" validate:"required"`
	ExperimentId    string                 `json:"experimentId" validate:"required"`
	Command         string                 `json:"command" validate:"required"`
	ExecutionScript string                 `json:"executionScript" validate:"required"`
	Dependencies    []string               `json:"dependencies,omitempty"`
	InputFiles      []FileMetadata         `json:"inputFiles,omitempty"`
	OutputFiles     []FileMetadata         `json:"outputFiles,omitempty"`
	Environment     map[string]string      `json:"environment,omitempty"`
	Timeout         time.Duration          `json:"timeout" validate:"min=0"`
	Metadata        map[string]interface{} `json:"metadata,omitempty"`
}

// TaskMetrics represents metrics for a task execution
type TaskMetrics struct {
	TaskID           string    `json:"taskId" validate:"required"`
	CPUUsagePercent  float64   `json:"cpuUsagePercent"`
	MemoryUsageBytes int64     `json:"memoryUsageBytes"`
	DiskUsageBytes   int64     `json:"diskUsageBytes"`
	Timestamp        time.Time `json:"timestamp" validate:"required"`
}
