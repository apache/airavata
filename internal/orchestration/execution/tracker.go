/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package execution

import (
	"context"
	"errors"
	"log/slog"
	"sync"
	"time"

	"github.com/cschleiden/go-workflows/workflow"

	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	internalptr "github.com/apache/airavata/internal/ptr"
)

// TaskTracker records the status history of the tasks an activity carries out.
type TaskTracker struct {
	statuses  *processrepo.TaskStatusRepository
	processes *processrepo.ProcessRepository

	mu    sync.Mutex
	stops map[*context.CancelFunc]string // one per task in flight, to its process id
}

// NewTaskTracker returns a tracker writing to statuses.
func NewTaskTracker(statuses *processrepo.TaskStatusRepository, processes *processrepo.ProcessRepository) *TaskTracker {
	return &TaskTracker{statuses: statuses, processes: processes, stops: map[*context.CancelFunc]string{}}
}

var errStopped = errors.New("the process is stopped")

// untilStopped must be called before reading the recorded status, or an earlier Stop is missed.
func (t *TaskTracker) untilStopped(ctx context.Context, processID string) (context.Context, context.CancelFunc) {
	ctx, cancel := context.WithCancel(ctx)
	t.mu.Lock()
	t.stops[&cancel] = processID
	t.mu.Unlock()
	return ctx, func() {
		t.mu.Lock()
		delete(t.stops, &cancel)
		t.mu.Unlock()
		cancel()
	}
}

// Stop signals every task the process has in flight on this server. Cancelling a
// workflow only keeps its later tasks from starting; this ends the one running.
func (t *TaskTracker) Stop(processID string) {
	t.mu.Lock()
	defer t.mu.Unlock()
	for cancel, id := range t.stops {
		if id == processID {
			(*cancel)()
		}
	}
}

// Track runs one attempt of a task between an EXECUTING status and the status its
// outcome earns: COMPLETED on success, FAILED with the error as the log otherwise, or
// CANCELED when it failed because its process was stopped.
//
// It is called from inside an activity, never from the workflow, since a workflow body
// is replayed and must not write. Each retry of the activity is an attempt of its own,
// so a task retried twice shows two FAILED entries before its last outcome.
//
// A task that has already completed is not run again: Track hands in back unchanged
// and records nothing. That is what lets a retried process re-run a failed workflow
// without repeating the steps that got through — staging a file twice, or submitting
// the job again. A completed status is enough to go by, rather than the latest one,
// because two statuses of one attempt can share a timestamp.
//
// A status that cannot be written is logged and dropped rather than failing the task:
// the history describes the work, and losing an entry of it is no reason to redo work
// that succeeded.
func (t *TaskTracker) Track(ctx context.Context, processID, taskID string, taskType model.TaskType,
	in *Context, run func(context.Context) (*Context, error)) (*Context, error) {

	if t.completed(ctx, processID, taskID) {
		slog.Info("Skipping task that has already completed", "taskId", taskID, "processId", processID, "taskType", taskType)
		return in, nil
	}

	ctx, cancel := t.untilStopped(ctx, processID)
	defer cancel()

	// Permanent, so the workflow does not retry a task its process no longer wants.
	if Stopped(ctx, t.processes, processID) {
		return in, workflow.NewPermanentError(errStopped)
	}

	t.Record(ctx, processID, taskID, taskType, model.TaskStatusTypeExecuting, nil)

	out, err := run(ctx)
	if err != nil && Stopped(ctx, t.processes, processID) {
		t.Record(ctx, processID, taskID, taskType, model.TaskStatusTypeCanceled, nil)
		return out, workflow.NewPermanentError(errStopped)
	}
	if err != nil {
		t.Record(ctx, processID, taskID, taskType, model.TaskStatusTypeFailed, internalptr.To(err.Error()))
		return out, err
	}
	t.Record(ctx, processID, taskID, taskType, model.TaskStatusTypeCompleted, nil)
	return out, nil
}

// completed reports whether the task has already completed. A failure to read is
// logged and taken as not completed: running a step again is recoverable, and skipping
// one that never ran is not.
func (t *TaskTracker) completed(ctx context.Context, processID, taskID string) bool {
	if t == nil || t.statuses == nil {
		return false
	}
	done, err := t.statuses.HasStatus(ctx, taskID, processID, model.TaskStatusTypeCompleted)
	if err != nil {
		slog.Error("Failed to read task status history", "taskId", taskID, "processId", processID, "error", err)
		return false
	}
	return done
}

// Record appends one status to a task's history, logging rather than returning a
// failure to write it.
func (t *TaskTracker) Record(ctx context.Context, processID, taskID string, taskType model.TaskType,
	status model.TaskStatusType, log *string) {

	if t == nil || t.statuses == nil {
		return
	}
	// The task's own context may already be cancelled by the time its outcome is known,
	// and the outcome is exactly what most needs recording.
	ctx = context.WithoutCancel(ctx)
	if err := t.statuses.Create(ctx, &model.TaskStatus{
		TaskID:    taskID,
		TaskType:  taskType,
		ProcessID: &processID,
		Status:    &status,
		Log:       log,
		Timestamp: internalptr.To(time.Now().UnixMilli()),
	}); err != nil {
		slog.Error("Failed to record task status", "taskId", taskID, "processId", processID,
			"taskType", taskType, "status", status, "error", err)
	}
}
