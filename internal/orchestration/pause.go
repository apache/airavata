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

package orchestration

import (
	"context"
	"errors"
	"log/slog"
	"reflect"
	"time"

	workflow "github.com/cschleiden/go-workflows/workflow"

	model "github.com/apache/airavata/api/process/model"
	internalptr "github.com/apache/airavata/internal/ptr"
	ssh "github.com/apache/airavata/internal/ssh"
)

// continueSignal is the signal a paused workflow waits on. It carries the id of the
// pause being continued.
const continueSignal = "continue"

// interactiveAuthErrorType is how an InteractiveAuthRequiredError is named once it has
// crossed the workflow backend, which keeps an error's type name but not its type.
var interactiveAuthErrorType = reflect.TypeOf(ssh.InteractiveAuthRequiredError{}).Name()

// runTask executes one task activity, parking the workflow whenever the task stops for
// an interactive SSH login until the process is continued through the API.
//
// A continued task runs again from the start. If the login it asked for was answered
// in the meantime, its connection is waiting in the pool; if that connection has since
// been closed, the task dials afresh, which asks for a new login and parks it again.
func (w *ExecutionEngine) runTask(ctx workflow.Context, opts workflow.ActivityOptions, activity workflow.Activity,
	taskType model.PausedTaskType, executionContext *ExecutionContext, processID, taskID string) (*ExecutionContext, error) {
	resume := workflow.NewSignalChannel[string](ctx, continueSignal)
	for {
		out, err := workflow.ExecuteActivity[*ExecutionContext](ctx, opts, activity, executionContext, processID, taskID).Get(ctx)
		if err == nil || !isInteractiveAuthRequired(err) {
			return out, err
		}

		instanceID := workflow.WorkflowInstance(ctx).InstanceID
		if _, err := workflow.ExecuteActivity[any](
			ctx, workflow.ActivityOptions{RetryOptions: workflow.DefaultRetryOptions},
			w.pauseTask, processID, taskID, taskType, instanceID, err.Error()).Get(ctx); err != nil {
			workflow.Logger(ctx).Error("Failed to pause task for interactive login", "processId", processID, "taskId", taskID, "error", err)
			return nil, err
		}

		pauseID, _ := resume.Receive(ctx)
		workflow.Logger(ctx).Info("Continuing paused task", "processId", processID, "taskId", taskID, "pauseId", pauseID)
	}
}

// isInteractiveAuthRequired reports whether an activity failed because a host it dialed
// is waiting for its owner to log in, anywhere along the error's chain.
func isInteractiveAuthRequired(err error) bool {
	for ; err != nil; err = errors.Unwrap(err) {
		switch e := err.(type) {
		case *workflow.Error:
			if e.Type == interactiveAuthErrorType {
				return true
			}
		case *ssh.InteractiveAuthRequiredError:
			return true
		}
	}
	return false
}

// deferInteractiveAuth is deferred by a task activity over its named error. A login the
// task is waiting on is turned into a permanent error: retrying would only find the same
// login still unanswered, and runTask parks the workflow on it instead.
func deferInteractiveAuth(err *error) {
	var authErr *ssh.InteractiveAuthRequiredError
	if *err != nil && errors.As(*err, &authErr) {
		*err = workflow.NewPermanentError(*err)
	}
}

// pauseTask records that a task is waiting for its owner to log in, both as an open
// pause the API can continue and as the process's current status.
func (a *ExecutionEngine) pauseTask(ctx context.Context, processID, taskID string, taskType model.PausedTaskType, workflowInstanceID, reason string) error {
	now := time.Now().UnixMilli()
	if err := a.taskPauses.Create(ctx, &model.TaskPause{
		ProcessID:          processID,
		TaskID:             taskID,
		TaskType:           taskType,
		WorkflowInstanceID: workflowInstanceID,
		Reason:             &reason,
		PausedAt:           now,
	}); err != nil {
		return err
	}

	paused := model.ProcessStatusTypePaused
	if err := a.processStatus.Create(ctx, &model.ProcessStatus{
		ProcessID: &processID,
		Status:    &paused,
		Log:       internalptr.To("Waiting for an interactive SSH login: " + reason),
		Timestamp: &now,
	}); err != nil {
		return err
	}
	slog.Info("Paused task for interactive login", "processId", processID, "taskId", taskID, "reason", reason)
	return nil
}

// ContinueWorkflow wakes a workflow paused by runTask. The caller is responsible for
// having closed the pause first, so that one pause wakes the workflow once.
func (w *ExecutionEngine) ContinueWorkflow(ctx context.Context, workflowInstanceID, pauseID string) error {
	if w.orchestrator == nil {
		return ErrNoWorkflowBackend
	}
	return w.orchestrator.SignalWorkflow(ctx, workflowInstanceID, continueSignal, pauseID)
}
