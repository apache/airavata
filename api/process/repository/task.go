package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/process/model"
)

// TaskRepository reads and writes one kind of process task.
//
// The four kinds differ in payload but not in identity: every one is keyed by task_id,
// scoped by process_id and ordered by task_order. That is the whole of the table
// access, so it is written once here and instantiated per kind rather than copied four
// times. GORM resolves the table from T's TableName.
type TaskRepository[T any] struct{ db *gorm.DB }

// NewTaskRepository returns a repository over the table backing T.
func NewTaskRepository[T any](db *gorm.DB) *TaskRepository[T] {
	return &TaskRepository[T]{db: db}
}

// WithTx returns a repository bound to tx.
func (r *TaskRepository[T]) WithTx(tx *gorm.DB) *TaskRepository[T] {
	return &TaskRepository[T]{db: tx}
}

// FindByProcessID returns every task of one process in execution order.
//
// task_order is nullable, so rows without one sort last rather than jumping ahead of
// an explicitly ordered step; task_id breaks ties so the order is stable between
// calls.
func (r *TaskRepository[T]) FindByProcessID(ctx context.Context, processID string) ([]T, error) {
	var out []T
	err := r.db.WithContext(ctx).
		Where("process_id = ?", processID).
		Order("task_order IS NULL, task_order, task_id").
		Find(&out).Error
	return out, err
}

// FindByIDAndProcessID returns one task scoped to its process, or
// gorm.ErrRecordNotFound if the id does not belong to that process.
func (r *TaskRepository[T]) FindByIDAndProcessID(ctx context.Context, id, processID string) (*T, error) {
	var out T
	err := r.db.WithContext(ctx).
		Where("task_id = ? AND process_id = ?", id, processID).
		First(&out).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a task.
func (r *TaskRepository[T]) Save(ctx context.Context, t *T) error {
	return r.db.WithContext(ctx).Save(t).Error
}

// Delete removes a task.
func (r *TaskRepository[T]) Delete(ctx context.Context, t *T) error {
	return r.db.WithContext(ctx).Delete(t).Error
}

// The four instantiations. They exist so the wiring reads in entity terms rather than
// in type parameters, and so a future kind-specific query has somewhere to live.
type (
	DataStagingTaskRepository        = TaskRepository[model.DataStagingTask]
	JobSubmissionTaskRepository      = TaskRepository[model.JobSubmissionTask]
	JobMonitoringTaskRepository      = TaskRepository[model.JobMonitoringTask]
	InteractiveCommandTaskRepository = TaskRepository[model.InteractiveCommandTask]
)

// NewDataStagingTaskRepository returns a repository over data_staging_tasks.
func NewDataStagingTaskRepository(db *gorm.DB) *DataStagingTaskRepository {
	return NewTaskRepository[model.DataStagingTask](db)
}

// NewJobSubmissionTaskRepository returns a repository over job_submission_tasks.
func NewJobSubmissionTaskRepository(db *gorm.DB) *JobSubmissionTaskRepository {
	return NewTaskRepository[model.JobSubmissionTask](db)
}

// NewJobMonitoringTaskRepository returns a repository over job_monitoring_tasks.
func NewJobMonitoringTaskRepository(db *gorm.DB) *JobMonitoringTaskRepository {
	return NewTaskRepository[model.JobMonitoringTask](db)
}

// NewInteractiveCommandTaskRepository returns a repository over
// interactive_command_tasks.
func NewInteractiveCommandTaskRepository(db *gorm.DB) *InteractiveCommandTaskRepository {
	return NewTaskRepository[model.InteractiveCommandTask](db)
}
