package service

import (
	"context"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/process/dto"
	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/api/process/repository"
)

// TaskService manages one kind of task as a sub-resource of its process.
//
// The four kinds are the same service with a different payload — scope by process,
// authorise against that process, order by task_order — so the rules are written once
// and instantiated per kind. What differs is supplied at construction: how a request
// is applied to a row, and what the kind is called in an error message.
//
// Unlike the rest of this package, reads here are owner-scoped. Process and status
// reads are open for compatibility with clients the Java service already had; these
// endpoints are new, and a task carries filesystem paths and shell commands, so there
// is no reason to expose them more widely than the run they belong to.
type TaskService[T any, Req any] struct {
	db        *gorm.DB
	tasks     *repository.TaskRepository[T]
	processes *repository.ProcessRepository

	// name is how the kind is named in a not-found message, e.g. "Data staging task".
	name string
	// apply copies a validated request onto a row.
	apply func(dst *T, src *Req)
	// stamp records which process a new row belongs to.
	stamp func(dst *T, processID string)
}

// ListForProcess returns every task of one process, in execution order.
func (s *TaskService[T, Req]) ListForProcess(ctx context.Context, processID string) ([]T, error) {
	if _, err := s.requireProcess(ctx, processID); err != nil {
		return nil, err
	}
	return s.tasks.FindByProcessID(ctx, processID)
}

// Get returns one task of one process.
func (s *TaskService[T, Req]) Get(ctx context.Context, processID, taskID string) (*T, error) {
	if _, err := s.requireProcess(ctx, processID); err != nil {
		return nil, err
	}
	return s.requireTask(ctx, s.tasks, processID, taskID)
}

// Create adds a task to a process.
func (s *TaskService[T, Req]) Create(ctx context.Context, processID string, req *Req) (*T, error) {
	var out *T
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if _, err := s.requireProcessTx(ctx, tx, processID); err != nil {
			return err
		}

		var task T
		s.stamp(&task, processID)
		s.apply(&task, req)
		if err := s.tasks.WithTx(tx).Save(ctx, &task); err != nil {
			return err
		}
		out = &task
		return nil
	})
	if err != nil {
		return nil, err
	}
	return out, nil
}

// Update changes a task of a process.
//
// The process it belongs to is left alone: both ids come from the path, so a task can
// be edited but never moved to a different run.
func (s *TaskService[T, Req]) Update(ctx context.Context, processID, taskID string, req *Req) (*T, error) {
	var out *T
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if _, err := s.requireProcessTx(ctx, tx, processID); err != nil {
			return err
		}
		tasks := s.tasks.WithTx(tx)
		task, err := s.requireTask(ctx, tasks, processID, taskID)
		if err != nil {
			return err
		}

		s.apply(task, req)
		if err := tasks.Save(ctx, task); err != nil {
			return err
		}
		out = task
		return nil
	})
	if err != nil {
		return nil, err
	}
	return out, nil
}

// Delete removes a task from a process.
func (s *TaskService[T, Req]) Delete(ctx context.Context, processID, taskID string) error {
	if _, err := s.requireProcess(ctx, processID); err != nil {
		return err
	}
	task, err := s.requireTask(ctx, s.tasks, processID, taskID)
	if err != nil {
		return err
	}
	return s.tasks.Delete(ctx, task)
}

// requireProcess loads the process and checks the caller may act on it: its owner, or
// an admin. A process nobody can name is a 404; one that is not theirs is a 403.
func (s *TaskService[T, Req]) requireProcess(ctx context.Context, processID string) (*model.Process, error) {
	return s.requireProcessWith(ctx, s.processes, processID)
}

func (s *TaskService[T, Req]) requireProcessTx(ctx context.Context, tx *gorm.DB, processID string) (*model.Process, error) {
	return s.requireProcessWith(ctx, s.processes.WithTx(tx), processID)
}

func (s *TaskService[T, Req]) requireProcessWith(ctx context.Context, processes *repository.ProcessRepository, processID string) (*model.Process, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	proc, err := processes.FindByID(ctx, processID)
	if err != nil {
		return nil, notFoundAs(err, "Process not found: %s", processID)
	}
	if !proc.OwnedBy(principal.Name) && !principal.IsAdmin() {
		return nil, httpx.Forbidden("Access denied: you may only work with tasks of your own processes")
	}
	return proc, nil
}

func (s *TaskService[T, Req]) requireTask(ctx context.Context, tasks *repository.TaskRepository[T], processID, taskID string) (*T, error) {
	task, err := tasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		return nil, notFoundAs(err, "%s not found: %s in process %s", s.name, taskID, processID)
	}
	return task, nil
}

// The four instantiations.
type (
	DataStagingTaskService        = TaskService[model.DataStagingTask, dto.DataStagingTaskRequest]
	JobSubmissionTaskService      = TaskService[model.JobSubmissionTask, dto.JobSubmissionTaskRequest]
	JobMonitoringTaskService      = TaskService[model.JobMonitoringTask, dto.JobMonitoringTaskRequest]
	InteractiveCommandTaskService = TaskService[model.InteractiveCommandTask, dto.InteractiveCommandTaskRequest]
)

// NewDataStagingTaskService returns a staging task service.
func NewDataStagingTaskService(db *gorm.DB, tasks *repository.DataStagingTaskRepository, processes *repository.ProcessRepository) *DataStagingTaskService {
	return &DataStagingTaskService{
		db: db, tasks: tasks, processes: processes,
		name:  "Data staging task",
		apply: dto.ApplyDataStagingTaskRequest,
		stamp: func(t *model.DataStagingTask, processID string) { t.ProcessID = &processID },
	}
}

// NewJobSubmissionTaskService returns a submission task service.
func NewJobSubmissionTaskService(db *gorm.DB, tasks *repository.JobSubmissionTaskRepository, processes *repository.ProcessRepository) *JobSubmissionTaskService {
	return &JobSubmissionTaskService{
		db: db, tasks: tasks, processes: processes,
		name:  "Job submission task",
		apply: dto.ApplyJobSubmissionTaskRequest,
		stamp: func(t *model.JobSubmissionTask, processID string) { t.ProcessID = &processID },
	}
}

// NewJobMonitoringTaskService returns a monitoring task service.
func NewJobMonitoringTaskService(db *gorm.DB, tasks *repository.JobMonitoringTaskRepository, processes *repository.ProcessRepository) *JobMonitoringTaskService {
	return &JobMonitoringTaskService{
		db: db, tasks: tasks, processes: processes,
		name:  "Job monitoring task",
		apply: dto.ApplyJobMonitoringTaskRequest,
		stamp: func(t *model.JobMonitoringTask, processID string) { t.ProcessID = &processID },
	}
}

// NewInteractiveCommandTaskService returns a command task service.
func NewInteractiveCommandTaskService(db *gorm.DB, tasks *repository.InteractiveCommandTaskRepository, processes *repository.ProcessRepository) *InteractiveCommandTaskService {
	return &InteractiveCommandTaskService{
		db: db, tasks: tasks, processes: processes,
		name:  "Interactive command task",
		apply: dto.ApplyInteractiveCommandTaskRequest,
		stamp: func(t *model.InteractiveCommandTask, processID string) { t.ProcessID = &processID },
	}
}
