package ports

import (
	"context"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
)

// DatabasePort defines the interface for database operations
// This abstracts the database implementation from domain services
type DatabasePort interface {
	// Transaction management
	WithTransaction(ctx context.Context, fn func(ctx context.Context) error) error
	WithRetry(ctx context.Context, fn func() error) error

	// Generic CRUD operations
	Create(ctx context.Context, entity interface{}) error
	GetByID(ctx context.Context, id string, entity interface{}) error
	Update(ctx context.Context, entity interface{}) error
	Delete(ctx context.Context, id string, entity interface{}) error
	List(ctx context.Context, entities interface{}, limit, offset int) error
	Count(ctx context.Context, entity interface{}, count *int64) error

	// Query operations
	Find(ctx context.Context, entities interface{}, conditions map[string]interface{}) error
	FindOne(ctx context.Context, entity interface{}, conditions map[string]interface{}) error
	Exists(ctx context.Context, entity interface{}, conditions map[string]interface{}) (bool, error)

	// Raw query operations
	Raw(ctx context.Context, query string, args ...interface{}) ([]map[string]interface{}, error)
	Exec(ctx context.Context, query string, args ...interface{}) error

	// Connection management
	Ping(ctx context.Context) error
	Close() error
}

// RepositoryPort defines the interface for domain-specific repositories
type RepositoryPort interface {
	// Transaction management
	WithTransaction(ctx context.Context, fn func(ctx context.Context) error) error

	// Experiment repository operations
	CreateExperiment(ctx context.Context, experiment *domain.Experiment) error
	GetExperimentByID(ctx context.Context, id string) (*domain.Experiment, error)
	UpdateExperiment(ctx context.Context, experiment *domain.Experiment) error
	DeleteExperiment(ctx context.Context, id string) error
	ListExperiments(ctx context.Context, filters *ExperimentFilters, limit, offset int) ([]*domain.Experiment, int64, error)
	SearchExperiments(ctx context.Context, query *ExperimentSearchQuery) ([]*domain.Experiment, int64, error)

	// Task repository operations
	CreateTask(ctx context.Context, task *domain.Task) error
	GetTaskByID(ctx context.Context, id string) (*domain.Task, error)
	UpdateTask(ctx context.Context, task *domain.Task) error
	DeleteTask(ctx context.Context, id string) error
	ListTasksByExperiment(ctx context.Context, experimentID string, limit, offset int) ([]*domain.Task, int64, error)
	GetTasksByStatus(ctx context.Context, status domain.TaskStatus, limit, offset int) ([]*domain.Task, int64, error)
	GetTasksByWorker(ctx context.Context, workerID string, limit, offset int) ([]*domain.Task, int64, error)

	// Worker repository operations
	CreateWorker(ctx context.Context, worker *domain.Worker) error
	GetWorkerByID(ctx context.Context, id string) (*domain.Worker, error)
	UpdateWorker(ctx context.Context, worker *domain.Worker) error
	DeleteWorker(ctx context.Context, id string) error
	ListWorkersByComputeResource(ctx context.Context, computeResourceID string, limit, offset int) ([]*domain.Worker, int64, error)
	ListWorkersByExperiment(ctx context.Context, experimentID string, limit, offset int) ([]*domain.Worker, int64, error)
	GetWorkersByStatus(ctx context.Context, status domain.WorkerStatus, limit, offset int) ([]*domain.Worker, int64, error)
	GetIdleWorkers(ctx context.Context, limit int) ([]*domain.Worker, error)

	// Compute resource repository operations
	CreateComputeResource(ctx context.Context, resource *domain.ComputeResource) error
	GetComputeResourceByID(ctx context.Context, id string) (*domain.ComputeResource, error)
	UpdateComputeResource(ctx context.Context, resource *domain.ComputeResource) error
	DeleteComputeResource(ctx context.Context, id string) error
	ListComputeResources(ctx context.Context, filters *ComputeResourceFilters, limit, offset int) ([]*domain.ComputeResource, int64, error)

	// Storage resource repository operations
	CreateStorageResource(ctx context.Context, resource *domain.StorageResource) error
	GetStorageResourceByID(ctx context.Context, id string) (*domain.StorageResource, error)
	UpdateStorageResource(ctx context.Context, resource *domain.StorageResource) error
	DeleteStorageResource(ctx context.Context, id string) error
	ListStorageResources(ctx context.Context, filters *StorageResourceFilters, limit, offset int) ([]*domain.StorageResource, int64, error)

	// Note: Credential operations removed - now handled by OpenBao and SpiceDB

	// User repository operations
	CreateUser(ctx context.Context, user *domain.User) error
	GetUserByID(ctx context.Context, id string) (*domain.User, error)
	GetUserByUsername(ctx context.Context, username string) (*domain.User, error)
	GetUserByEmail(ctx context.Context, email string) (*domain.User, error)
	UpdateUser(ctx context.Context, user *domain.User) error
	DeleteUser(ctx context.Context, id string) error
	ListUsers(ctx context.Context, limit, offset int) ([]*domain.User, int64, error)

	// Group repository operations
	CreateGroup(ctx context.Context, group *domain.Group) error
	GetGroupByID(ctx context.Context, id string) (*domain.Group, error)
	GetGroupByName(ctx context.Context, name string) (*domain.Group, error)
	UpdateGroup(ctx context.Context, group *domain.Group) error
	DeleteGroup(ctx context.Context, id string) error
	ListGroups(ctx context.Context, limit, offset int) ([]*domain.Group, int64, error)

	// Project repository operations
	CreateProject(ctx context.Context, project *domain.Project) error
	GetProjectByID(ctx context.Context, id string) (*domain.Project, error)
	UpdateProject(ctx context.Context, project *domain.Project) error
	DeleteProject(ctx context.Context, id string) error
	ListProjectsByOwner(ctx context.Context, ownerID string, limit, offset int) ([]*domain.Project, int64, error)

	// Data cache repository operations
	CreateDataCache(ctx context.Context, cache *domain.DataCache) error
	GetDataCacheByPath(ctx context.Context, filePath, computeResourceID string) (*domain.DataCache, error)
	UpdateDataCache(ctx context.Context, cache *domain.DataCache) error
	DeleteDataCache(ctx context.Context, id string) error
	ListDataCacheByComputeResource(ctx context.Context, computeResourceID string, limit, offset int) ([]*domain.DataCache, int64, error)

	// Data lineage repository operations
	CreateDataLineage(ctx context.Context, lineage *domain.DataLineageRecord) error
	GetDataLineageByFileID(ctx context.Context, fileID string) ([]*domain.DataLineageRecord, error)
	UpdateDataLineage(ctx context.Context, lineage *domain.DataLineageRecord) error
	DeleteDataLineage(ctx context.Context, id string) error

	// Audit log repository operations
	CreateAuditLog(ctx context.Context, log *domain.AuditLog) error
	ListAuditLogs(ctx context.Context, filters *AuditLogFilters, limit, offset int) ([]*domain.AuditLog, int64, error)

	// Experiment tag repository operations
	CreateExperimentTag(ctx context.Context, tag *domain.ExperimentTag) error
	GetExperimentTags(ctx context.Context, experimentID string) ([]*domain.ExperimentTag, error)
	DeleteExperimentTag(ctx context.Context, id string) error
	DeleteExperimentTagsByExperiment(ctx context.Context, experimentID string) error

	// Task result aggregate repository operations
	CreateTaskResultAggregate(ctx context.Context, aggregate *domain.TaskResultAggregate) error
	GetTaskResultAggregates(ctx context.Context, experimentID string) ([]*domain.TaskResultAggregate, error)
	UpdateTaskResultAggregate(ctx context.Context, aggregate *domain.TaskResultAggregate) error
	DeleteTaskResultAggregate(ctx context.Context, id string) error

	// Note: ACL operations removed - now handled by SpiceDB

	// Task metrics operations
	CreateTaskMetrics(ctx context.Context, metrics *domain.TaskMetrics) error
	CreateWorkerMetrics(ctx context.Context, metrics *domain.WorkerMetrics) error
	GetLatestWorkerMetrics(ctx context.Context, workerID string) (*domain.WorkerMetrics, error)

	// Staging operation operations
	GetStagingOperationByID(ctx context.Context, id string) (*domain.StagingOperation, error)
	UpdateStagingOperation(ctx context.Context, operation *domain.StagingOperation) error

	// Registration token operations
	ValidateRegistrationToken(ctx context.Context, token string) (*RegistrationToken, error)
	MarkTokenAsUsed(ctx context.Context, token string) error
	UpdateComputeResourceStatus(ctx context.Context, resourceID string, status domain.ResourceStatus) error
	UpdateStorageResourceStatus(ctx context.Context, resourceID string, status domain.ResourceStatus) error
}

// Filter types for repository queries
type ExperimentFilters struct {
	ProjectID     *string
	OwnerID       *string
	Status        *domain.ExperimentStatus
	CreatedAfter  *time.Time
	CreatedBefore *time.Time
}

type ExperimentSearchQuery struct {
	Query         string
	ProjectID     *string
	OwnerID       *string
	Status        *domain.ExperimentStatus
	Tags          []string
	CreatedAfter  *time.Time
	CreatedBefore *time.Time
	Limit         int
	Offset        int
	SortBy        string
	SortOrder     string
}

type ComputeResourceFilters struct {
	Type    *domain.ComputeResourceType
	Status  *domain.ResourceStatus
	OwnerID *string
}

type StorageResourceFilters struct {
	Type    *domain.StorageResourceType
	Status  *domain.ResourceStatus
	OwnerID *string
}

type AuditLogFilters struct {
	UserID     *string
	Action     *string
	Resource   *string
	ResourceID *string
	After      *time.Time
	Before     *time.Time
}

// RegistrationToken represents a one-time registration token
type RegistrationToken struct {
	ID         string
	Token      string
	ResourceID string
	UserID     string
	ExpiresAt  time.Time
	UsedAt     *time.Time
	CreatedAt  time.Time
}
