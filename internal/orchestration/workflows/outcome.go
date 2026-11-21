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

package workflows

import (
	"context"
	"log/slog"
	"time"

	workflow "github.com/cschleiden/go-workflows/workflow"

	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/internal/ptr"
)

// recordingOutcome runs a workflow body between two records of it in the process's
// workflow history: RUNNING before the body starts, then SUCCEEDED or FAILED by what
// the body returns, or STOPPED if a stop signal cancelled it. A workflow fails exactly when its
// function returns an error, so this is where every failure path of the body passes.
//
// RUNNING is recorded here rather than by whoever started the workflow: a caller
// writing it after CreateWorkflowInstance could land after a fast workflow's final
// status and leave the record RUNNING for good.
//
// A panic in the body is not seen here — the workflow engine recovers it above this
// function — so a panicking workflow stays RUNNING.
func (w *Workflows) recordingOutcome(ctx workflow.Context, processID string,
	workflowType model.ProcessWorkflowType, body func(workflow.Context, string) error) error {

	// Created first, so a stop signalled during the RUNNING write is buffered.
	stops := workflow.NewSignalChannel[struct{}](ctx, stopSignal)
	processStopped, err := w.recordWorkflowStatus(ctx, processID, workflowType, model.ProcessWorkflowStatusTypeRunning)
	if err != nil {
		return err
	}
	if processStopped {
		w.recordWorkflowStatus(ctx, processID, workflowType, model.ProcessWorkflowStatusTypeStopped)
		// Recorded so the stop stays the latest status after the launch or retry that started this.
		return w.recordProcessStatus(ctx, processID, model.ProcessStatusTypeStopped, ProcessStoppedLog)
	}

	bodyCtx, cancel := workflow.WithCancel(ctx)
	defer cancel()
	workflow.Go(bodyCtx, func(ctx workflow.Context) {
		workflow.Select(ctx,
			workflow.Receive(stops, func(workflow.Context, struct{}, bool) { cancel() }),
			workflow.Receive(ctx.Done(), func(workflow.Context, struct{}, bool) {}))
	})
	err = body(bodyCtx, processID)

	if bodyCtx.Err() != nil {
		w.recordWorkflowStatus(ctx, processID, workflowType, model.ProcessWorkflowStatusTypeStopped)
		// The task in flight when the stop came may have submitted a job after the teardown released.
		return w.releaseStoppedProcess(ctx, processID)
	}
	status := model.ProcessWorkflowStatusTypeSucceeded
	if err != nil {
		status = model.ProcessWorkflowStatusTypeFailed
	}
	w.recordWorkflowStatus(ctx, processID, workflowType, status)
	return err
}

// recordWorkflowStatus records the running workflow's status through RecordWorkflowStatus,
// logs a failure, and reports whether the process is stopped.
func (w *Workflows) recordWorkflowStatus(ctx workflow.Context, processID string,
	workflowType model.ProcessWorkflowType, status model.ProcessWorkflowStatusType) (bool, error) {

	workflowID := workflow.WorkflowInstance(ctx).InstanceID
	stopped, err := workflow.ExecuteActivity[bool](ctx, workflow.DefaultActivityOptions,
		w.RecordWorkflowStatus, processID, workflowID, workflowType, status).Get(ctx)
	if err != nil {
		slog.Error("Failed to record process workflow", "processId", processID,
			"workflowId", workflowID, "status", status, "error", err)
	}
	return stopped, err
}

// RecordWorkflowStatus is the activity behind recordWorkflowStatus. The write is an activity
// rather than workflow code so that a replay reads its result from history instead of
// writing again.
func (w *Workflows) RecordWorkflowStatus(ctx context.Context, processID, workflowID string,
	workflowType model.ProcessWorkflowType, status model.ProcessWorkflowStatusType) (bool, error) {

	if _, err := w.processWorkflows.Record(ctx, processID, workflowID, workflowType, status); err != nil {
		return false, err
	}
	return w.processStatus.IsProcessStopped(ctx, processID)
}

// recordProcessStatus records a status of the process through RecordProcessStatus.
func (w *Workflows) recordProcessStatus(ctx workflow.Context, processID string, status model.ProcessStatusType, log string) error {
	_, err := workflow.ExecuteActivity[any](ctx, workflow.DefaultActivityOptions, w.RecordProcessStatus, processID, status, log).Get(ctx)
	return err
}

// RecordProcessStatus is the activity behind recordProcessStatus, for the same reason as
// RecordWorkflowStatus.
func (w *Workflows) RecordProcessStatus(ctx context.Context, processID string, status model.ProcessStatusType, log string) error {
	return w.processStatus.Create(ctx, &model.ProcessStatus{ProcessID: &processID, Status: &status, Log: &log, Timestamp: ptr.To(time.Now().UnixMilli())})
}
