package adapters

import (
	"context"
	"fmt"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// contextKey is used for context values
type contextKey string

// PostgresAdapter implements the RepositoryPort interface using PostgreSQL
type PostgresAdapter struct {
	db *gorm.DB
}

// NewPostgresAdapter creates a new PostgreSQL database adapter
func NewPostgresAdapter(dsn string) (*PostgresAdapter, error) {
	// Configure GORM
	config := &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
		NowFunc: func() time.Time {
			return time.Now().UTC()
		},
	}

	// Connect to database
	db, err := gorm.Open(postgres.Open(dsn), config)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to database: %w", err)
	}

	// Get underlying sql.DB for connection pool configuration
	sqlDB, err := db.DB()
	if err != nil {
		return nil, fmt.Errorf("failed to get underlying sql.DB: %w", err)
	}

	// Configure connection pool
	sqlDB.SetMaxOpenConns(25)
	sqlDB.SetMaxIdleConns(25)
	sqlDB.SetConnMaxLifetime(5 * time.Minute)

	// Note: Auto-migration is disabled in favor of custom schema management
	// The schema is managed through db/schema.sql

	return &PostgresAdapter{db: db}, nil
}

const txKey contextKey = "tx"

// WithTransaction implements ports.DatabasePort.WithTransaction
func (a *PostgresAdapter) WithTransaction(ctx context.Context, fn func(ctx context.Context) error) error {
	return a.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		// Create a new context with the transaction
		txCtx := context.WithValue(ctx, txKey, tx)
		return fn(txCtx)
	})
}

// WithRetry implements ports.DatabasePort.WithRetry
func (a *PostgresAdapter) WithRetry(ctx context.Context, fn func() error) error {
	maxRetries := 3
	baseDelay := 100 * time.Millisecond

	for i := 0; i < maxRetries; i++ {
		err := fn()
		if err == nil {
			return nil
		}

		// Check if it's a retryable error
		if !isRetryableError(err) {
			return err
		}

		// Don't sleep on the last attempt
		if i < maxRetries-1 {
			delay := time.Duration(i+1) * baseDelay
			select {
			case <-ctx.Done():
				return ctx.Err()
			case <-time.After(delay):
				continue
			}
		}
	}

	return fmt.Errorf("max retries exceeded")
}

// Create implements ports.DatabasePort.Create
func (a *PostgresAdapter) Create(ctx context.Context, entity interface{}) error {
	db := a.getDB(ctx)
	return db.Create(entity).Error
}

// GetByID implements ports.DatabasePort.GetByID
func (a *PostgresAdapter) GetByID(ctx context.Context, id string, entity interface{}) error {
	db := a.getDB(ctx)
	err := db.First(entity, "id = ?", id).Error
	if err == gorm.ErrRecordNotFound {
		return domain.ErrResourceNotFound
	}
	return err
}

// Update implements ports.DatabasePort.Update
func (a *PostgresAdapter) Update(ctx context.Context, entity interface{}) error {
	db := a.getDB(ctx)
	return db.Save(entity).Error
}

// Delete implements ports.DatabasePort.Delete
func (a *PostgresAdapter) Delete(ctx context.Context, id string, entity interface{}) error {
	db := a.getDB(ctx)
	return db.Delete(entity, "id = ?", id).Error
}

// GetByField retrieves records by a specific field
func (a *PostgresAdapter) GetByField(ctx context.Context, fieldName string, value interface{}, dest interface{}) error {
	db := a.getDB(ctx)
	return db.Where(fieldName+" = ?", value).Find(dest).Error
}

// List implements ports.DatabasePort.List
func (a *PostgresAdapter) List(ctx context.Context, entities interface{}, limit, offset int) error {
	db := a.getDB(ctx)
	query := db.Find(entities)
	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}
	return query.Error
}

// Count implements ports.DatabasePort.Count
func (a *PostgresAdapter) Count(ctx context.Context, entity interface{}, count *int64) error {
	db := a.getDB(ctx)
	return db.Model(entity).Count(count).Error
}

// Find implements ports.DatabasePort.Find
func (a *PostgresAdapter) Find(ctx context.Context, entities interface{}, conditions map[string]interface{}) error {
	db := a.getDB(ctx)
	query := db.Where(conditions).Find(entities)
	return query.Error
}

// FindOne implements ports.DatabasePort.FindOne
func (a *PostgresAdapter) FindOne(ctx context.Context, entity interface{}, conditions map[string]interface{}) error {
	db := a.getDB(ctx)
	err := db.Where(conditions).First(entity).Error
	if err == gorm.ErrRecordNotFound {
		return domain.ErrResourceNotFound
	}
	return err
}

// Exists implements ports.DatabasePort.Exists
func (a *PostgresAdapter) Exists(ctx context.Context, entity interface{}, conditions map[string]interface{}) (bool, error) {
	db := a.getDB(ctx)
	var count int64
	err := db.Model(entity).Where(conditions).Count(&count).Error
	return count > 0, err
}

// Raw implements ports.DatabasePort.Raw
func (a *PostgresAdapter) Raw(ctx context.Context, query string, args ...interface{}) ([]map[string]interface{}, error) {
	db := a.getDB(ctx)
	rows, err := db.Raw(query, args...).Rows()
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var results []map[string]interface{}
	columns, err := rows.Columns()
	if err != nil {
		return nil, err
	}

	for rows.Next() {
		values := make([]interface{}, len(columns))
		valuePtrs := make([]interface{}, len(columns))
		for i := range columns {
			valuePtrs[i] = &values[i]
		}

		if err := rows.Scan(valuePtrs...); err != nil {
			return nil, err
		}

		row := make(map[string]interface{})
		for i, col := range columns {
			val := values[i]
			if b, ok := val.([]byte); ok {
				row[col] = string(b)
			} else {
				row[col] = val
			}
		}
		results = append(results, row)
	}

	return results, nil
}

// Exec implements ports.DatabasePort.Exec
func (a *PostgresAdapter) Exec(ctx context.Context, query string, args ...interface{}) error {
	db := a.getDB(ctx)
	return db.Exec(query, args...).Error
}

// Ping implements ports.DatabasePort.Ping
func (a *PostgresAdapter) Ping(ctx context.Context) error {
	sqlDB, err := a.db.DB()
	if err != nil {
		return err
	}
	return sqlDB.PingContext(ctx)
}

// Close implements ports.DatabasePort.Close
func (a *PostgresAdapter) Close() error {
	sqlDB, err := a.db.DB()
	if err != nil {
		return err
	}
	return sqlDB.Close()
}

// CreateAuditLog creates an audit log entry
func (a *PostgresAdapter) CreateAuditLog(ctx context.Context, log *domain.AuditLog) error {
	return fmt.Errorf("CreateAuditLog not implemented")
}

// Helper methods

// GetDB returns the underlying GORM database instance
func (a *PostgresAdapter) GetDB() *gorm.DB {
	return a.db
}

func (a *PostgresAdapter) getDB(ctx context.Context) *gorm.DB {
	// Check if we're in a transaction
	if tx, ok := ctx.Value(txKey).(*gorm.DB); ok {
		return tx
	}
	return a.db.WithContext(ctx)
}

func isRetryableError(err error) bool {
	if err == nil {
		return false
	}
	// Add logic to determine if an error is retryable
	// For now, return false to avoid infinite retries
	return false
}

// Repository implements the RepositoryPort interface using PostgreSQL
type Repository struct {
	adapter *PostgresAdapter
}

// NewRepository creates a new PostgreSQL repository
func NewRepository(adapter *PostgresAdapter) *Repository {
	return &Repository{adapter: adapter}
}

// WithTransaction implements ports.RepositoryPort.WithTransaction
func (r *Repository) WithTransaction(ctx context.Context, fn func(ctx context.Context) error) error {
	return r.adapter.WithTransaction(ctx, fn)
}

// Experiment repository operations

func (r *Repository) CreateExperiment(ctx context.Context, experiment *domain.Experiment) error {
	return r.adapter.Create(ctx, experiment)
}

func (r *Repository) GetExperimentByID(ctx context.Context, id string) (*domain.Experiment, error) {
	var experiment domain.Experiment
	err := r.adapter.GetByID(ctx, id, &experiment)
	if err != nil {
		return nil, err
	}
	return &experiment, nil
}

func (r *Repository) UpdateExperiment(ctx context.Context, experiment *domain.Experiment) error {
	return r.adapter.Update(ctx, experiment)
}

func (r *Repository) DeleteExperiment(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.Experiment{})
}

func (r *Repository) ListExperiments(ctx context.Context, filters *ports.ExperimentFilters, limit, offset int) ([]*domain.Experiment, int64, error) {
	var experiments []*domain.Experiment
	var total int64

	// Build query
	query := r.adapter.getDB(ctx).Model(&domain.Experiment{})

	if filters.ProjectID != nil {
		query = query.Where("project_id = ?", *filters.ProjectID)
	}
	if filters.OwnerID != nil {
		query = query.Where("owner_id = ?", *filters.OwnerID)
	}
	if filters.Status != nil {
		query = query.Where("status = ?", *filters.Status)
	}
	if filters.CreatedAfter != nil {
		query = query.Where("created_at >= ?", *filters.CreatedAfter)
	}
	if filters.CreatedBefore != nil {
		query = query.Where("created_at <= ?", *filters.CreatedBefore)
	}

	// Get total count
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// Get results
	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&experiments).Error
	return experiments, total, err
}

func (r *Repository) SearchExperiments(ctx context.Context, query *ports.ExperimentSearchQuery) ([]*domain.Experiment, int64, error) {
	var experiments []*domain.Experiment
	var total int64

	// Build search query
	dbQuery := r.adapter.getDB(ctx).Model(&domain.Experiment{})

	if query.Query != "" {
		dbQuery = dbQuery.Where("name ILIKE ? OR description ILIKE ?",
			"%"+query.Query+"%", "%"+query.Query+"%")
	}
	if query.ProjectID != nil {
		dbQuery = dbQuery.Where("project_id = ?", *query.ProjectID)
	}
	if query.OwnerID != nil {
		dbQuery = dbQuery.Where("owner_id = ?", *query.OwnerID)
	}
	if query.Status != nil {
		dbQuery = dbQuery.Where("status = ?", *query.Status)
	}
	if query.CreatedAfter != nil {
		dbQuery = dbQuery.Where("created_at >= ?", *query.CreatedAfter)
	}
	if query.CreatedBefore != nil {
		dbQuery = dbQuery.Where("created_at <= ?", *query.CreatedBefore)
	}

	// Handle tags
	if len(query.Tags) > 0 {
		dbQuery = dbQuery.Joins("JOIN experiment_tags ON experiments.id = experiment_tags.experiment_id").
			Where("experiment_tags.tag IN ?", query.Tags)
	}

	// Get total count
	if err := dbQuery.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// Apply sorting
	if query.SortBy != "" {
		order := query.SortBy
		if query.SortOrder == "desc" {
			order += " DESC"
		}
		dbQuery = dbQuery.Order(order)
	}

	// Get results
	if query.Limit > 0 {
		dbQuery = dbQuery.Limit(query.Limit)
	}
	if query.Offset > 0 {
		dbQuery = dbQuery.Offset(query.Offset)
	}

	err := dbQuery.Find(&experiments).Error
	return experiments, total, err
}

// Task repository operations

func (r *Repository) CreateTask(ctx context.Context, task *domain.Task) error {
	return r.adapter.Create(ctx, task)
}

func (r *Repository) GetTaskByID(ctx context.Context, id string) (*domain.Task, error) {
	var task domain.Task
	err := r.adapter.GetByID(ctx, id, &task)
	if err != nil {
		return nil, err
	}
	return &task, nil
}

func (r *Repository) UpdateTask(ctx context.Context, task *domain.Task) error {
	return r.adapter.Update(ctx, task)
}

func (r *Repository) DeleteTask(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.Task{})
}

func (r *Repository) ListTasksByExperiment(ctx context.Context, experimentID string, limit, offset int) ([]*domain.Task, int64, error) {
	var tasks []*domain.Task
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.Task{}).Where("experiment_id = ?", experimentID)

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&tasks).Error
	return tasks, total, err
}

func (r *Repository) GetTasksByStatus(ctx context.Context, status domain.TaskStatus, limit, offset int) ([]*domain.Task, int64, error) {
	var tasks []*domain.Task
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.Task{})

	// Only filter by status if status is not empty
	if status != "" {
		query = query.Where("status = ?", status)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&tasks).Error
	return tasks, total, err
}

func (r *Repository) GetTasksByWorker(ctx context.Context, workerID string, limit, offset int) ([]*domain.Task, int64, error) {
	var tasks []*domain.Task
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.Task{}).Where("worker_id = ?", workerID)

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&tasks).Error
	return tasks, total, err
}

// Worker repository operations

func (r *Repository) CreateWorker(ctx context.Context, worker *domain.Worker) error {
	return r.adapter.Create(ctx, worker)
}

func (r *Repository) GetWorkerByID(ctx context.Context, id string) (*domain.Worker, error) {
	var worker domain.Worker
	err := r.adapter.GetByID(ctx, id, &worker)
	if err != nil {
		return nil, err
	}
	return &worker, nil
}

func (r *Repository) UpdateWorker(ctx context.Context, worker *domain.Worker) error {
	return r.adapter.Update(ctx, worker)
}

func (r *Repository) DeleteWorker(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.Worker{})
}

func (r *Repository) ListWorkersByComputeResource(ctx context.Context, computeResourceID string, limit, offset int) ([]*domain.Worker, int64, error) {
	var workers []*domain.Worker
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.Worker{}).Where("compute_resource_id = ?", computeResourceID)

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&workers).Error
	return workers, total, err
}

func (r *Repository) ListWorkersByExperiment(ctx context.Context, experimentID string, limit, offset int) ([]*domain.Worker, int64, error) {
	var workers []*domain.Worker
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.Worker{}).Where("experiment_id = ?", experimentID)

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&workers).Error
	return workers, total, err
}

func (r *Repository) GetWorkersByStatus(ctx context.Context, status domain.WorkerStatus, limit, offset int) ([]*domain.Worker, int64, error) {
	var workers []*domain.Worker
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.Worker{}).Where("status = ?", status)

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&workers).Error
	return workers, total, err
}

func (r *Repository) GetIdleWorkers(ctx context.Context, limit int) ([]*domain.Worker, error) {
	var workers []*domain.Worker

	query := r.adapter.getDB(ctx).Model(&domain.Worker{}).
		Where("status = ?", domain.WorkerStatusIdle).
		Order("created_at ASC")

	if limit > 0 {
		query = query.Limit(limit)
	}

	err := query.Find(&workers).Error
	return workers, err
}

// Compute resource repository operations

func (r *Repository) CreateComputeResource(ctx context.Context, resource *domain.ComputeResource) error {
	return r.adapter.Create(ctx, resource)
}

func (r *Repository) GetComputeResourceByID(ctx context.Context, id string) (*domain.ComputeResource, error) {
	fmt.Printf("DEBUG: Getting compute resource by ID: %s\n", id)
	var resource domain.ComputeResource
	err := r.adapter.GetByID(ctx, id, &resource)
	if err != nil {
		fmt.Printf("DEBUG: Failed to get compute resource %s: %v\n", id, err)
		return nil, err
	}
	fmt.Printf("DEBUG: Retrieved compute resource %s with status: %s\n", id, resource.Status)
	return &resource, nil
}

func (r *Repository) UpdateComputeResource(ctx context.Context, resource *domain.ComputeResource) error {
	return r.adapter.Update(ctx, resource)
}

func (r *Repository) DeleteComputeResource(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.ComputeResource{})
}

func (r *Repository) ListComputeResources(ctx context.Context, filters *ports.ComputeResourceFilters, limit, offset int) ([]*domain.ComputeResource, int64, error) {
	var resources []*domain.ComputeResource
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.ComputeResource{})

	if filters.Type != nil {
		query = query.Where("type = ?", *filters.Type)
	}
	if filters.Status != nil {
		query = query.Where("status = ?", *filters.Status)
	}
	if filters.OwnerID != nil {
		query = query.Where("owner_id = ?", *filters.OwnerID)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&resources).Error
	return resources, total, err
}

// Storage resource repository operations

func (r *Repository) CreateStorageResource(ctx context.Context, resource *domain.StorageResource) error {
	return r.adapter.Create(ctx, resource)
}

func (r *Repository) GetStorageResourceByID(ctx context.Context, id string) (*domain.StorageResource, error) {
	var resource domain.StorageResource
	err := r.adapter.GetByID(ctx, id, &resource)
	if err != nil {
		return nil, err
	}
	return &resource, nil
}

func (r *Repository) UpdateStorageResource(ctx context.Context, resource *domain.StorageResource) error {
	return r.adapter.Update(ctx, resource)
}

func (r *Repository) DeleteStorageResource(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.StorageResource{})
}

func (r *Repository) ListStorageResources(ctx context.Context, filters *ports.StorageResourceFilters, limit, offset int) ([]*domain.StorageResource, int64, error) {
	var resources []*domain.StorageResource
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.StorageResource{})

	if filters.Type != nil {
		query = query.Where("type = ?", *filters.Type)
	}
	if filters.Status != nil {
		query = query.Where("status = ?", *filters.Status)
	}
	if filters.OwnerID != nil {
		query = query.Where("owner_id = ?", *filters.OwnerID)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&resources).Error
	return resources, total, err
}

// Note: Credential operations removed - now handled by OpenBao and SpiceDB

func (r *Repository) CreateGroup(ctx context.Context, group *domain.Group) error {
	return r.adapter.Create(ctx, group)
}

// Note: Group membership operations removed - now handled by SpiceDB

func (r *Repository) CreateTaskMetrics(ctx context.Context, metrics *domain.TaskMetrics) error {
	return r.adapter.Create(ctx, metrics)
}

func (r *Repository) CreateWorkerMetrics(ctx context.Context, metrics *domain.WorkerMetrics) error {
	return r.adapter.Create(ctx, metrics)
}

func (r *Repository) GetLatestWorkerMetrics(ctx context.Context, workerID string) (*domain.WorkerMetrics, error) {
	var metrics domain.WorkerMetrics
	err := r.adapter.getDB(ctx).Where("worker_id = ?", workerID).Order("timestamp DESC").First(&metrics).Error
	if err != nil {
		return nil, err
	}
	return &metrics, nil
}

func (r *Repository) GetStagingOperationByID(ctx context.Context, id string) (*domain.StagingOperation, error) {
	var operation domain.StagingOperation
	err := r.adapter.GetByID(ctx, id, &operation)
	if err != nil {
		return nil, err
	}
	return &operation, nil
}

func (r *Repository) UpdateStagingOperation(ctx context.Context, operation *domain.StagingOperation) error {
	return r.adapter.Update(ctx, operation)
}

// Note: User group membership operations removed - now handled by SpiceDB

func (r *Repository) GetGroupByID(ctx context.Context, id string) (*domain.Group, error) {
	var group domain.Group
	err := r.adapter.GetByID(ctx, id, &group)
	if err != nil {
		return nil, err
	}
	return &group, nil
}

func (r *Repository) GetGroupByName(ctx context.Context, name string) (*domain.Group, error) {
	var group domain.Group
	err := r.adapter.GetByField(ctx, "name", name, &group)
	if err != nil {
		return nil, err
	}
	return &group, nil
}

func (r *Repository) DeleteGroup(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.Group{})
}

// Note: Resource credential binding and group membership operations removed - now handled by SpiceDB

func (r *Repository) ListGroups(ctx context.Context, limit, offset int) ([]*domain.Group, int64, error) {
	var groups []*domain.Group
	var total int64

	err := r.adapter.Count(ctx, &domain.Group{}, &total)
	if err != nil {
		return nil, 0, err
	}

	err = r.adapter.List(ctx, &groups, limit, offset)
	if err != nil {
		return nil, 0, err
	}

	return groups, total, nil
}

func (r *Repository) UpdateGroup(ctx context.Context, group *domain.Group) error {
	return r.adapter.Update(ctx, group)
}

// Note: Credential CRUD operations removed - now handled by OpenBao

// User repository operations

func (r *Repository) CreateUser(ctx context.Context, user *domain.User) error {
	return r.adapter.Create(ctx, user)
}

func (r *Repository) GetUserByID(ctx context.Context, id string) (*domain.User, error) {
	var user domain.User
	err := r.adapter.GetByID(ctx, id, &user)
	if err != nil {
		return nil, err
	}
	return &user, nil
}

func (r *Repository) GetUserByUsername(ctx context.Context, username string) (*domain.User, error) {
	var user domain.User
	err := r.adapter.FindOne(ctx, &user, map[string]interface{}{"username": username})
	if err != nil {
		return nil, err
	}
	return &user, nil
}

func (r *Repository) GetUserByEmail(ctx context.Context, email string) (*domain.User, error) {
	var user domain.User
	err := r.adapter.FindOne(ctx, &user, map[string]interface{}{"email": email})
	if err != nil {
		return nil, err
	}
	return &user, nil
}

func (r *Repository) UpdateUser(ctx context.Context, user *domain.User) error {
	return r.adapter.Update(ctx, user)
}

func (r *Repository) DeleteUser(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.User{})
}

func (r *Repository) ListUsers(ctx context.Context, limit, offset int) ([]*domain.User, int64, error) {
	var users []*domain.User
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.User{})

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&users).Error
	return users, total, err
}

// Project repository operations

func (r *Repository) CreateProject(ctx context.Context, project *domain.Project) error {
	return r.adapter.Create(ctx, project)
}

func (r *Repository) GetProjectByID(ctx context.Context, id string) (*domain.Project, error) {
	var project domain.Project
	err := r.adapter.GetByID(ctx, id, &project)
	if err != nil {
		return nil, err
	}
	return &project, nil
}

func (r *Repository) UpdateProject(ctx context.Context, project *domain.Project) error {
	return r.adapter.Update(ctx, project)
}

func (r *Repository) DeleteProject(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.Project{})
}

func (r *Repository) ListProjectsByOwner(ctx context.Context, ownerID string, limit, offset int) ([]*domain.Project, int64, error) {
	var projects []*domain.Project
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.Project{}).Where("owner_id = ?", ownerID)

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&projects).Error
	return projects, total, err
}

// Data cache repository operations

func (r *Repository) CreateDataCache(ctx context.Context, cache *domain.DataCache) error {
	return r.adapter.Create(ctx, cache)
}

func (r *Repository) GetDataCacheByPath(ctx context.Context, filePath, computeResourceID string) (*domain.DataCache, error) {
	var cache domain.DataCache
	err := r.adapter.FindOne(ctx, &cache, map[string]interface{}{
		"file_path":           filePath,
		"compute_resource_id": computeResourceID,
	})
	if err != nil {
		return nil, err
	}
	return &cache, nil
}

func (r *Repository) UpdateDataCache(ctx context.Context, cache *domain.DataCache) error {
	return r.adapter.Update(ctx, cache)
}

func (r *Repository) DeleteDataCache(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.DataCache{})
}

func (r *Repository) ListDataCacheByComputeResource(ctx context.Context, computeResourceID string, limit, offset int) ([]*domain.DataCache, int64, error) {
	var caches []*domain.DataCache
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.DataCache{}).Where("compute_resource_id = ?", computeResourceID)

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Find(&caches).Error
	return caches, total, err
}

// Data lineage repository operations

func (r *Repository) CreateDataLineage(ctx context.Context, lineage *domain.DataLineageRecord) error {
	return r.adapter.Create(ctx, lineage)
}

func (r *Repository) GetDataLineageByFileID(ctx context.Context, fileID string) ([]*domain.DataLineageRecord, error) {
	var lineage []*domain.DataLineageRecord
	err := r.adapter.Find(ctx, &lineage, map[string]interface{}{"file_id": fileID})
	if err != nil {
		return nil, err
	}
	return lineage, nil
}

func (r *Repository) UpdateDataLineage(ctx context.Context, lineage *domain.DataLineageRecord) error {
	return r.adapter.Update(ctx, lineage)
}

func (r *Repository) DeleteDataLineage(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.DataLineageRecord{})
}

// Audit log repository operations

func (r *Repository) CreateAuditLog(ctx context.Context, log *domain.AuditLog) error {
	return r.adapter.Create(ctx, log)
}

func (r *Repository) ListAuditLogs(ctx context.Context, filters *ports.AuditLogFilters, limit, offset int) ([]*domain.AuditLog, int64, error) {
	var logs []*domain.AuditLog
	var total int64

	query := r.adapter.getDB(ctx).Model(&domain.AuditLog{})

	if filters.UserID != nil {
		query = query.Where("user_id = ?", *filters.UserID)
	}
	if filters.Action != nil {
		query = query.Where("action = ?", *filters.Action)
	}
	if filters.Resource != nil {
		query = query.Where("resource = ?", *filters.Resource)
	}
	if filters.ResourceID != nil {
		query = query.Where("resource_id = ?", *filters.ResourceID)
	}
	if filters.After != nil {
		query = query.Where("timestamp >= ?", *filters.After)
	}
	if filters.Before != nil {
		query = query.Where("timestamp <= ?", *filters.Before)
	}

	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	if limit > 0 {
		query = query.Limit(limit)
	}
	if offset > 0 {
		query = query.Offset(offset)
	}

	err := query.Order("timestamp DESC").Find(&logs).Error
	return logs, total, err
}

// Experiment tag repository operations

func (r *Repository) CreateExperimentTag(ctx context.Context, tag *domain.ExperimentTag) error {
	return r.adapter.Create(ctx, tag)
}

func (r *Repository) GetExperimentTags(ctx context.Context, experimentID string) ([]*domain.ExperimentTag, error) {
	var tags []*domain.ExperimentTag
	err := r.adapter.Find(ctx, &tags, map[string]interface{}{"experiment_id": experimentID})
	if err != nil {
		return nil, err
	}
	return tags, nil
}

func (r *Repository) DeleteExperimentTag(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.ExperimentTag{})
}

func (r *Repository) DeleteExperimentTagsByExperiment(ctx context.Context, experimentID string) error {
	return r.adapter.getDB(ctx).Where("experiment_id = ?", experimentID).Delete(&domain.ExperimentTag{}).Error
}

// Task result aggregate repository operations

func (r *Repository) CreateTaskResultAggregate(ctx context.Context, aggregate *domain.TaskResultAggregate) error {
	return r.adapter.Create(ctx, aggregate)
}

func (r *Repository) GetTaskResultAggregates(ctx context.Context, experimentID string) ([]*domain.TaskResultAggregate, error) {
	var aggregates []*domain.TaskResultAggregate
	err := r.adapter.Find(ctx, &aggregates, map[string]interface{}{"experiment_id": experimentID})
	if err != nil {
		return nil, err
	}
	return aggregates, nil
}

func (r *Repository) UpdateTaskResultAggregate(ctx context.Context, aggregate *domain.TaskResultAggregate) error {
	return r.adapter.Update(ctx, aggregate)
}

func (r *Repository) DeleteTaskResultAggregate(ctx context.Context, id string) error {
	return r.adapter.Delete(ctx, id, &domain.TaskResultAggregate{})
}

// ValidateRegistrationToken validates a registration token and returns token info
func (r *Repository) ValidateRegistrationToken(ctx context.Context, token string) (*ports.RegistrationToken, error) {
	var regToken ports.RegistrationToken

	// Use raw SQL to query the registration_tokens table
	rows, err := r.adapter.db.Raw(`
		SELECT id, token, resource_id, user_id, expires_at, used_at, created_at 
		FROM registration_tokens 
		WHERE token = ?
	`, token).Rows()

	if err != nil {
		return nil, fmt.Errorf("failed to query registration token: %w", err)
	}
	defer rows.Close()

	if !rows.Next() {
		return nil, fmt.Errorf("token not found")
	}

	err = rows.Scan(&regToken.ID, &regToken.Token, &regToken.ResourceID, &regToken.UserID,
		&regToken.ExpiresAt, &regToken.UsedAt, &regToken.CreatedAt)
	if err != nil {
		return nil, fmt.Errorf("failed to scan registration token: %w", err)
	}

	return &regToken, nil
}

// MarkTokenAsUsed marks a registration token as used
func (r *Repository) MarkTokenAsUsed(ctx context.Context, token string) error {
	result := r.adapter.db.Exec(`
		UPDATE registration_tokens 
		SET used_at = ? 
		WHERE token = ?
	`, time.Now(), token)

	if result.Error != nil {
		return fmt.Errorf("failed to mark token as used: %w", result.Error)
	}

	if result.RowsAffected == 0 {
		return fmt.Errorf("token not found")
	}

	return nil
}

// UpdateComputeResourceStatus updates the status of a compute resource
func (r *Repository) UpdateComputeResourceStatus(ctx context.Context, resourceID string, status domain.ResourceStatus) error {
	fmt.Printf("DEBUG: Updating compute resource %s status to %s\n", resourceID, string(status))

	result := r.adapter.db.Exec(`
		UPDATE compute_resources 
		SET status = $1, updated_at = $2 
		WHERE id = $3
	`, string(status), time.Now(), resourceID)

	if result.Error != nil {
		fmt.Printf("DEBUG: Failed to update compute resource status: %v\n", result.Error)
		return fmt.Errorf("failed to update compute resource status: %w", result.Error)
	}

	fmt.Printf("DEBUG: Successfully updated compute resource %s status to %s (rows affected: %d)\n", resourceID, string(status), result.RowsAffected)

	if result.RowsAffected == 0 {
		return fmt.Errorf("compute resource not found")
	}

	return nil
}

// UpdateStorageResourceStatus updates the status of a storage resource
func (r *Repository) UpdateStorageResourceStatus(ctx context.Context, resourceID string, status domain.ResourceStatus) error {
	result := r.adapter.db.Exec(`
		UPDATE storage_resources 
		SET status = ?, updated_at = ? 
		WHERE id = ?
	`, string(status), time.Now(), resourceID)

	if result.Error != nil {
		return fmt.Errorf("failed to update storage resource status: %w", result.Error)
	}

	if result.RowsAffected == 0 {
		return fmt.Errorf("storage resource not found")
	}

	return nil
}
