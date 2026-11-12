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

	workflow "github.com/cschleiden/go-workflows/workflow"

	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/internal/orchestration/execution"
)

// recordingOutcome runs a workflow body between two records of it in the process's
// workflow history: RUNNING before the body starts, then SUCCEEDED or FAILED by what
// the body returns, or STOPPED if the process was stopped. A workflow fails exactly when its function returns an error, so
// this is where every failure path of the body passes.
//
// RUNNING is recorded here rather than by whoever started the workflow: a caller
// writing it after CreateWorkflowInstance could land after a fast workflow's final
// status and leave the record RUNNING for good.
//
// A panic in the body is not seen here — the workflow engine recovers it above this
// function — so a panicking workflow stays RUNNING.
func (w *Workflows) recordingOutcome(ctx workflow.Context, processID string,
	workflowType model.ProcessWorkflowType, body func() error) error {

	w.recordStatus(ctx, processID, workflowType, model.ProcessWorkflowStatusTypeRunning)

	err := body()

	status := model.ProcessWorkflowStatusTypeSucceeded
	if err != nil {
		status = model.ProcessWorkflowStatusTypeFailed
	}
	// Disconnected, so the outcome is still recorded when the body ended because the
	// workflow was cancelled.
	disconnected := workflow.NewDisconnectedContext(ctx)
	if ctx.Err() != nil {
		// A stop cancels the workflow, and a job submitted while it arrived is only
		// known once the submission has returned.
		if _, err := workflow.ExecuteActivity[any](disconnected, workflow.DefaultActivityOptions,
			w.batch.CancelBatchJob, processID).Get(disconnected); err != nil {
			slog.Error("Failed to cancel the job of a stopped process", "processId", processID, "error", err)
		}
	}
	w.recordStatus(disconnected, processID, workflowType, status)
	return err
}

// recordStatus records the running workflow's status through RecordWorkflowStatus. A
// failure to record is logged and otherwise ignored: the history describes the work,
// and losing an entry of it is no reason to fail the work itself.
func (w *Workflows) recordStatus(ctx workflow.Context, processID string,
	workflowType model.ProcessWorkflowType, status model.ProcessWorkflowStatusType) {

	instance := workflow.WorkflowInstance(ctx)
	if _, err := workflow.ExecuteActivity[any](ctx, workflow.DefaultActivityOptions,
		w.RecordWorkflowStatus, processID, instance.InstanceID, instance.ExecutionID, workflowType, status).Get(ctx); err != nil {
		slog.Error("Failed to record process workflow", "processId", processID,
			"workflowId", instance.InstanceID, "status", status, "error", err)
	}
}

// RecordWorkflowStatus is the activity behind recordStatus. The write is an activity
// rather than workflow code so that a replay reads its result from history instead of
// writing again.
func (w *Workflows) RecordWorkflowStatus(ctx context.Context, processID, workflowID, executionID string,
	workflowType model.ProcessWorkflowType, status model.ProcessWorkflowStatusType) error {

	if status == model.ProcessWorkflowStatusTypeRunning {
		recorded, err := w.processWorkflows.Record(ctx, processID, workflowID, workflowType, status)
		if err != nil {
			return err
		}
		return w.processWorkflows.SetExecutionID(ctx, recorded.ID, executionID)
	}
	if execution.Stopped(ctx, w.processes, processID) {
		status = model.ProcessWorkflowStatusTypeStopped
	}
	_, err := w.processWorkflows.Record(ctx, processID, workflowID, workflowType, status)
	return err
}
