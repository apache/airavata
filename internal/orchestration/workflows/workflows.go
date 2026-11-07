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

// Package workflows defines the workflows a process is carried out by, each a sequence
// of the tasks in the tasks packages.
package workflows

import (
	"context"
	"fmt"
	"log/slog"

	"github.com/cschleiden/go-workflows/registry"
	workflow "github.com/cschleiden/go-workflows/workflow"

	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/orchestration/execution"
	"github.com/apache/airavata/internal/orchestration/tasks/batch"
	"github.com/apache/airavata/internal/orchestration/tasks/data"
	"github.com/apache/airavata/internal/orchestration/tasks/monitoring"
)

// Registrar is where the workflows and the activities they run are registered:
// worker.WorkflowOrchestrator in the server, a test registry elsewhere.
type Registrar interface {
	RegisterWorkflow(wf workflow.Workflow, opts ...registry.RegisterOption) error
	RegisterActivity(a workflow.Activity, opts ...registry.RegisterOption) error
}

// Workflows holds the workflows of a process and the tasks they run.
type Workflows struct {
	data       *data.Tasks
	batch      *batch.Tasks
	monitoring *monitoring.Tasks

	dataStagingTasks   *processrepo.DataStagingTaskRepository
	jobSubmissionTasks *processrepo.JobSubmissionTaskRepository
	jobMonitoringTasks *processrepo.JobMonitoringTaskRepository
	processStatus      *processrepo.StatusRepository
	processWorkflows   *processrepo.WorkflowRepository
}

// New returns the workflows over the given tasks and the repositories they are
// planned from.
func New(data *data.Tasks, batch *batch.Tasks, monitoring *monitoring.Tasks,
	dataStagingTasks *processrepo.DataStagingTaskRepository,
	jobSubmissionTasks *processrepo.JobSubmissionTaskRepository,
	jobMonitoringTasks *processrepo.JobMonitoringTaskRepository,
	processStatus *processrepo.StatusRepository,
	processWorkflows *processrepo.WorkflowRepository,
) *Workflows {
	return &Workflows{
		data:               data,
		batch:              batch,
		monitoring:         monitoring,
		dataStagingTasks:   dataStagingTasks,
		jobSubmissionTasks: jobSubmissionTasks,
		jobMonitoringTasks: jobMonitoringTasks,
		processStatus:      processStatus,
		processWorkflows:   processWorkflows,
	}
}

// Register registers every workflow and activity with r. A workflow or activity is
// registered under its function name, which is also how a running instance refers to
// it, so renaming one strands the instances already in the backend.
func (w *Workflows) Register(r Registrar) error {
	for _, wf := range []workflow.Workflow{w.HandleBatchJobSubmission, w.HandleBatchJobCompletion} {
		if err := r.RegisterWorkflow(wf); err != nil {
			return err
		}
	}
	for _, a := range []workflow.Activity{w.data.CopyData, w.batch.SubmitBatchJob, w.monitoring.MonitorBatchJob, w.CompleteProcess} {
		if err := r.RegisterActivity(a); err != nil {
			return err
		}
	}
	return nil
}

// HandleBatchJobCompletion runs once the scheduler reports a job finished: it checks
// the job's outcome, stages its outputs back and marks the process complete.
func (w *Workflows) HandleBatchJobCompletion(ctx workflow.Context, processID string) error {
	// Implement the logic for batch job completion workflow here

	ctxInt := context.Background()
	dsts, err := w.dataStagingTasks.FindByProcessID(ctxInt, processID)
	if err != nil {
		slog.Error("Failed to list data staging tasks", "processId", processID, "error", err)
		return err
	}

	jmts, err := w.jobMonitoringTasks.FindByProcessID(ctxInt, processID)
	if err != nil {
		slog.Error("Failed to list job monitoring tasks", "processId", processID, "error", err)
		return err
	}

	if len(jmts) == 0 {
		slog.Warn("No job monitoring tasks found for process", "processId", processID)
		return fmt.Errorf("No job monitoring tasks found for process %s", processID)
	}

	globalJobConfigs, err := execution.LoadGlobalJobConfigs()
	if err != nil {
		slog.Error("Failed to get global job config", "processId", processID, "error", err)
		return err
	}

	executionContext := &execution.Context{
		GlobalJobConfigs: globalJobConfigs,
		Data:             make(map[string]interface{}),
	}

	if len(jmts) == 0 {
		slog.Warn("No job monitoring tasks found for process", "processId", processID)
		return fmt.Errorf("No job monitoring tasks found for process %s", processID)
	}

	if len(jmts) > 1 {
		slog.Warn("Multiple job monitoring tasks found for process", "processId", processID)
		return fmt.Errorf("Multiple job monitoring tasks found for process %s", processID)
	}

	jmt := jmts[0]

	executionContext, err = workflow.ExecuteActivity[*execution.Context](
		ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*jmt.OnFailure, *jmt.RetryCount)},
		w.monitoring.MonitorBatchJob, executionContext, processID, jmt.ID).Get(ctx)
	if err != nil {
		slog.Error("Failed processing job monitoring task", "processId", processID, "taskId", jmt.ID, "error", err)
		return err
	}

	for _, dst := range dsts { // Attach output staging tasks
		if dst.TaskOrder != nil && *dst.TaskOrder > *jmt.TaskOrder {
			executionContext, err = workflow.ExecuteActivity[*execution.Context](
				ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*dst.OnFailure, *dst.RetryCount)},
				w.data.CopyData, executionContext, processID, dst.ID).Get(ctx)
			if err != nil {
				slog.Error("Failed processing data staging task", "processId", processID, "taskId", dst.ID, "error", err)
				return err
			}
		}
	}

	executionContext, err = workflow.ExecuteActivity[*execution.Context](
		ctx, workflow.ActivityOptions{RetryOptions: retryOptions(model.OnFailureActionExit, 0)},
		w.CompleteProcess, executionContext, processID).Get(ctx)
	if err != nil {
		slog.Error("Failed completing process", "processId", processID, "error", err)
		return err
	}

	// record workflow completion
	if _, err := w.processWorkflows.Record(ctxInt, processID, workflow.WorkflowInstance(ctx).InstanceID, model.ProcessWorkflowTypeJobCompletion, model.ProcessWorkflowStatusTypeSucceeded); err != nil {
		slog.Error("Failed to record process workflow", "processId", processID, "error", err)
		return err
	}

	return nil
}

// HandleBatchJobSubmission stages a process's inputs onto its cluster and submits the
// job. This is an async call and will not block the caller.
func (w *Workflows) HandleBatchJobSubmission(ctx workflow.Context, processID string) error {

	ctxInt := context.Background()

	// dsts are ordered by their task order
	dsts, err := w.dataStagingTasks.FindByProcessID(ctxInt, processID)

	if err != nil {
		slog.Error("Failed to list data staging tasks", "processId", processID, "error", err)
		return err
	}

	jsts, err := w.jobSubmissionTasks.FindByProcessID(ctxInt, processID)
	if err != nil {
		slog.Error("Failed to list job submission tasks", "processId", processID, "error", err)
		return err
	}

	if len(jsts) == 0 {
		slog.Error("No job submission tasks found for process", "processId", processID)
		return fmt.Errorf("No job submission tasks found for process %s", processID)
	}

	if len(jsts) > 1 {
		slog.Error("Multiple job submission tasks found for process", "processId", processID)
		return fmt.Errorf("Multiple job submission tasks found for process %s", processID)
	}
	// At this point, we are guaranteed to have exactly one job submission task.

	jst := jsts[0]

	globalJobConfigs, err := execution.LoadGlobalJobConfigs()
	if err != nil {
		slog.Error("Failed to get global job config", "processId", processID, "error", err)
		return err
	}

	executionContext := &execution.Context{
		Data:             make(map[string]interface{}),
		GlobalJobConfigs: globalJobConfigs,
		SomeData:         "Fooooo",
	}

	for _, dst := range dsts { // tasks are already sorted by their task order. Attach input staging tasks
		if dst.TaskOrder != nil && *dst.TaskOrder < *jst.TaskOrder {
			executionContext, err = workflow.ExecuteActivity[*execution.Context](
				ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*dst.OnFailure, *dst.RetryCount)},
				w.data.CopyData, executionContext, processID, dst.ID).Get(ctx)
			if err != nil {
				slog.Error("Failed processing data staging task", "processId", processID, "taskId", dst.ID, "error", err)
				return err
			}
		}
	}

	executionContext, err = workflow.ExecuteActivity[*execution.Context](
		ctx, workflow.ActivityOptions{RetryOptions: retryOptions(*jst.OnFailure, *jst.RetryCount)},
		w.batch.SubmitBatchJob, executionContext, processID, jst.ID).Get(ctx)
	if err != nil {
		slog.Error("Failed processing job submission task", "processId", processID, "taskId", jst.ID, "error", err)
		return err
	}

	if _, err := w.processWorkflows.Record(ctxInt, processID, workflow.WorkflowInstance(ctx).InstanceID, model.ProcessWorkflowTypeBatchJobSubmission, model.ProcessWorkflowStatusTypeSucceeded); err != nil {
		slog.Error("Failed to record process workflow", "processId", processID, "error", err)
		return err
	}

	return nil
}

func retryOptions(onFailure model.OnFailureAction, retryCount int) workflow.RetryOptions {
	opts := workflow.RetryOptions{MaxAttempts: 1}
	if onFailure != model.OnFailureActionRetry {
		return opts
	}
	// retryCount counts retries, so the first run is one attempt on top of it.
	opts.MaxAttempts = 1 + retryCount
	opts.BackoffCoefficient = workflow.DefaultRetryOptions.BackoffCoefficient
	return opts
}
