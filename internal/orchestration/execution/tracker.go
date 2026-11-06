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
	"log/slog"
	"time"

	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	internalptr "github.com/apache/airavata/internal/ptr"
)

// TaskTracker records the status history of the tasks an activity carries out.
type TaskTracker struct {
	statuses *processrepo.TaskStatusRepository
}

// NewTaskTracker returns a tracker writing to statuses.
func NewTaskTracker(statuses *processrepo.TaskStatusRepository) *TaskTracker {
	return &TaskTracker{statuses: statuses}
}

// Track runs one attempt of a task between an EXECUTING status and the status its
// outcome earns: COMPLETED on success, FAILED with the error as the log otherwise.
//
// It is called from inside an activity, never from the workflow, since a workflow body
// is replayed and must not write. Each retry of the activity is an attempt of its own,
// so a task retried twice shows two FAILED entries before its last outcome.
//
// A status that cannot be written is logged and dropped rather than failing the task:
// the history describes the work, and losing an entry of it is no reason to redo work
// that succeeded.
func (t *TaskTracker) Track(ctx context.Context, processID, taskID string, taskType model.TaskType,
	run func() (*Context, error)) (*Context, error) {

	t.Record(ctx, processID, taskID, taskType, model.TaskStatusTypeExecuting, nil)

	out, err := run()
	if err != nil {
		t.Record(ctx, processID, taskID, taskType, model.TaskStatusTypeFailed, internalptr.To(err.Error()))
		return out, err
	}
	t.Record(ctx, processID, taskID, taskType, model.TaskStatusTypeCompleted, nil)
	return out, nil
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
