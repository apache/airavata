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

// Package orchestration runs the workflows that carry out a process. The workflows are
// in the workflows package and the tasks they are made of in the tasks packages; this
// package wires them to the workflow backend and starts them.
package orchestration

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"time"

	"github.com/cschleiden/go-workflows/backend"
	"github.com/cschleiden/go-workflows/client"
	"github.com/cschleiden/go-workflows/worker"
	"github.com/cschleiden/go-workflows/workflow"
	"github.com/google/uuid"

	applicationrepo "github.com/apache/airavata/api/application/repository"
	computerepo "github.com/apache/airavata/api/compute/repository"
	datastorerepo "github.com/apache/airavata/api/data/repository"
	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/orchestration/execution"
	"github.com/apache/airavata/internal/orchestration/tasks/batch"
	"github.com/apache/airavata/internal/orchestration/tasks/data"
	"github.com/apache/airavata/internal/orchestration/tasks/monitoring"
	"github.com/apache/airavata/internal/orchestration/workflows"
	internalptr "github.com/apache/airavata/internal/ptr"
	ssh "github.com/apache/airavata/internal/ssh"
)

// ExecutionEngine runs the workflows that carry out a process, and starts them when a
// process is launched or the scheduler reports its job has finished.
type ExecutionEngine struct {
	workflows     *workflows.Workflows
	orchestrator  *worker.WorkflowOrchestrator
	processes     *processrepo.ProcessRepository
	batchStatus   *processrepo.BatchJobStatusRepository
	processStatus *processrepo.StatusRepository
}

// NewExecutionEngine returns the workflow set scheduling acts.
func NewExecutionEngine(dataStagingTasks *processrepo.DataStagingTaskRepository,
	jobSubmissionTasks *processrepo.JobSubmissionTaskRepository,
	jobMonitoringTasks *processrepo.JobMonitoringTaskRepository,
	scpStorages *datastorerepo.SCPDataStorageRepository,
	slurmClusterConfigs *computerepo.SlurmClusterConfigRepository,
	processes *processrepo.ProcessRepository,
	batchDeployments *applicationrepo.BatchDeploymentRepository,
	templates *applicationrepo.TemplateRepository,
	batchStatus *processrepo.BatchJobStatusRepository,
	processStatus *processrepo.StatusRepository,
	taskStatus *processrepo.TaskStatusRepository,
	processWorkflows *processrepo.WorkflowRepository,
	workflowBackend backend.Backend,
	sshContext *ssh.GlobalSSHContext,
) *ExecutionEngine {

	// workflowBackend may be nil in tests to prevent sqlite driver collision. Only create
	// the orchestrator if a backend is provided. This has to be fixed later by intoducing a custom test backend.
	var orchestrator *worker.WorkflowOrchestrator
	if workflowBackend != nil {
		orchestrator = worker.NewWorkflowOrchestrator(workflowBackend, nil)
	}

	tracker := execution.NewTaskTracker(taskStatus)

	return &ExecutionEngine{
		workflows: workflows.New(
			data.New(processes, dataStagingTasks, scpStorages, slurmClusterConfigs, sshContext, tracker),
			batch.New(processes, jobSubmissionTasks, slurmClusterConfigs, batchDeployments, templates, batchStatus, sshContext, tracker),
			monitoring.New(processes, jobMonitoringTasks, tracker),
			dataStagingTasks, jobSubmissionTasks, jobMonitoringTasks, processStatus,
			processWorkflows,
		),
		orchestrator:  orchestrator,
		processes:     processes,
		batchStatus:   batchStatus,
		processStatus: processStatus,
	}
}

func (w *ExecutionEngine) StartEngine() {
	if w.orchestrator == nil {
		slog.Info("Execution engine built without a workflow backend; not starting the workflow worker")
		return
	}

	slog.Info("Starting execution engine...........")
	ctx := context.Background()
	if err := w.workflows.Register(w.orchestrator); err != nil {
		slog.Error("Failed to register workflows", "error", err)
		panic("Could not register workflows")
	}

	if err := w.orchestrator.Start(ctx); err != nil {
		slog.Error("Failed to start execution engine", "error", err)
		panic("Could not start execution engine")
	}
}

// This is invoked when an email related to a batch job is received.
// It handles the email response accordingly.
func (w *ExecutionEngine) HandleBatchJobEmailResponse(ctx context.Context, email monitoring.Email) error {
	slog.Info("Handling batch job email response", "from", email.From, "subject", email.Subject)
	parsed, err := monitoring.ParseBatchJobSubject(email.Subject)
	if err != nil {
		slog.Error("Failed to parse batch job email subject", "subject", email.Subject, "error", err)
		// Do not return error. That will prevent the email from being read.
		return nil
	}
	slog.Info("Parsed batch job email subject", "jobID", parsed.JobID, "jobName", parsed.JobName, "status", parsed.Status)

	process, err := w.processes.FindByID(ctx, parsed.JobName)
	if err != nil {
		slog.Error("Processing batch email: Failed to find process by ID", "processID", parsed.JobName, "error", err)
		return nil
	}

	slog.Info("Found process", "processID", process.ID)

	if process == nil {
		slog.Error("Processing batch email: Process is nil", "processID", parsed.JobName)
		return nil
	}

	err = w.batchStatus.Create(ctx, &model.BatchJobStatus{
		BatchProcessID: process.BatchProcess.ID,
		Status:         parsed.Status,
		UpdatedAt:      time.Now(),
	})

	if err != nil {
		slog.Error("Failed to create batch job status", "processID", process.BatchProcess.ID, "error", err)
		return nil
	}

	triggeringStatus := map[model.BatchJobStatusType]struct{}{
		model.BatchJobStatusFailed: {},
		model.BatchJobStatusEnded:  {},
	}

	// Trigger actions for specific batch job statuses (failed or ended)
	if _, ok := triggeringStatus[parsed.Status]; ok {
		slog.Info("Triggering action for batch job status", "status", parsed.Status)

		if w.orchestrator == nil {
			slog.Error("Cannot run batch job completion workflow", "error", ErrNoWorkflowBackend)
			return nil
		}

		workflowId := uuid.NewString()

		_, err := w.orchestrator.CreateWorkflowInstance(ctx, client.WorkflowInstanceOptions{
			InstanceID: workflowId,
		}, w.workflows.HandleBatchJobCompletion, process.ID)
		if err != nil {
			slog.Error("Failed to create workflow instance for batch job completion", "error", err)
			return nil
		}

	}

	if parsed.Status == model.BatchJobStatusBegin {
		slog.Info("Handling begun batch job", "processID", process.ID)
		// Update the process status to indicate that the batch job has begun
		runningStatus := model.ProcessStatusTypeRunning
		if err := w.processStatus.Create(ctx, &model.ProcessStatus{
			ProcessID: &process.ID,
			Status:    &runningStatus,
			Log:       internalptr.To("Batch job has begun"),
			Timestamp: internalptr.To(time.Now().UnixMilli()),
		}); err != nil {
			slog.Error("Failed to create process status", "processID", process.ID, "error", err)
			return nil
		}
	}

	return nil
}

var ErrNoWorkflowBackend = errors.New("execution engine was built without a workflow backend")

func (w *ExecutionEngine) LaunchBatchJobSubmission(ctx context.Context, processID string) (string, error) {
	workflowID := uuid.NewString()
	return workflowID, w.LaunchWorkflow(ctx, workflowID, processID, model.ProcessWorkflowTypeBatchJobSubmission)
}

func (w *ExecutionEngine) LaunchBatchJobCompletion(ctx context.Context, processID string) (string, error) {
	workflowID := uuid.NewString()
	return workflowID, w.LaunchWorkflow(ctx, workflowID, processID, model.ProcessWorkflowTypeJobCompletion)
}

// LaunchWorkflow starts the workflow of the given type for a process under an instance
// id the caller chose, so a caller can record the workflow before it starts.
func (w *ExecutionEngine) LaunchWorkflow(ctx context.Context, workflowID, processID string,
	workflowType model.ProcessWorkflowType) error {

	if w.orchestrator == nil {
		return ErrNoWorkflowBackend
	}
	if client := auth.Client(ctx); client != "" {
		if err := w.processes.SetBatchProcessOrigin(ctx, processID, client); err != nil {
			return err
		}
	}
	var wf workflow.Workflow
	switch workflowType {
	case model.ProcessWorkflowTypeBatchJobSubmission:
		wf = w.workflows.HandleBatchJobSubmission
	case model.ProcessWorkflowTypeJobCompletion:
		wf = w.workflows.HandleBatchJobCompletion
	default:
		return fmt.Errorf("no workflow runs %s", workflowType)
	}
	_, err := w.orchestrator.CreateWorkflowInstance(ctx, client.WorkflowInstanceOptions{
		InstanceID: workflowID,
	}, wf, processID)
	return err
}
