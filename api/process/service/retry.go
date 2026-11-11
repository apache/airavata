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

package service

import (
	"context"
	"log/slog"

	"github.com/google/uuid"

	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/ptr"

	"github.com/apache/airavata/api/process/dto"
	procmodel "github.com/apache/airavata/api/process/model"
)

// RetryProcess starts again the workflow of a launched process that failed.
//
// Only the failed workflow is run again: a process whose job was submitted but whose
// completion failed runs its completion workflow, not a second submission. Inside it,
// each task that already completed is passed over rather than repeated (see
// execution.TaskTracker.Track), so a retry picks up at the step that failed.
//
// Owner-scoped like LaunchProcess, for the same reason. A process with a workflow still
// running, or none that failed, is refused with a conflict rather than started twice.
func (s *LaunchService) RetryProcess(ctx context.Context, processID string) (*dto.Response, error) {
	proc, err := s.processService.Get(ctx, processID)
	if err != nil {
		return nil, notFoundAs(err, "Process not found: %s", processID)
	}
	if err := s.requireOwnership(ctx, proc); err != nil {
		return nil, err
	}
	if err := s.requireNotStopped(ctx, processID); err != nil {
		return nil, err
	}

	recorded, err := s.processWorkflows.FindByProcessID(ctx, processID)
	if err != nil {
		return nil, err
	}
	workflowType, err := workflowToRetry(processID, recorded)
	if err != nil {
		return nil, err
	}

	// The new workflow is recorded RUNNING before it starts, so a second retry arriving
	// meanwhile sees it running and is refused. The workflow records RUNNING again when
	// it begins, which changes nothing, and then its outcome.
	workflowID := uuid.NewString()
	if _, err := s.processWorkflows.Record(ctx, processID, workflowID, workflowType,
		procmodel.ProcessWorkflowStatusTypeRunning); err != nil {
		return nil, err
	}
	if err := s.executionEngine.LaunchWorkflow(ctx, workflowID, processID, workflowType); err != nil {
		slog.Error("Failed to start retried workflow", "processId", processID, "workflowId", workflowID, "error", err)
		if _, recErr := s.processWorkflows.Record(ctx, processID, workflowID, workflowType,
			procmodel.ProcessWorkflowStatusTypeFailed); recErr != nil {
			slog.Error("Failed to record process workflow", "processId", processID, "workflowId", workflowID, "error", recErr)
		}
		return nil, err
	}
	slog.Info("Retried process workflow", "processId", processID, "workflowId", workflowID, "workflowType", workflowType)

	status, log := procmodel.ProcessStatusTypeSubmitted, "Retrying the failed job submission"
	if workflowType == procmodel.ProcessWorkflowTypeJobCompletion {
		status, log = procmodel.ProcessStatusTypeRunning, "Retrying the failed job completion"
	}
	if _, err := s.statusService.Record(ctx, processID, status, ptr.To(log)); err != nil {
		return nil, err
	}
	return proc, nil
}

// workflowToRetry picks the workflow a retry runs again from the workflows recorded for
// a process, oldest first. Each workflow type is judged by its latest run, since an
// earlier failure that a later run of the same type got past needs nothing.
//
// Submission comes first: until a job is submitted there is nothing to complete. A
// completion is retried only once the submission succeeded, so the job is never
// submitted twice.
// TODO: Closely review later to generalize for other workflow types beyond submission and completion.
func workflowToRetry(processID string, recorded []procmodel.ProcessWorkflow) (procmodel.ProcessWorkflowType, error) {
	latest := map[procmodel.ProcessWorkflowType]procmodel.ProcessWorkflowStatusType{}
	for _, w := range recorded {
		if w.WorkflowType == nil || w.Status == nil {
			continue
		}
		latest[*w.WorkflowType] = *w.Status
	}
	for _, status := range latest {
		if status == procmodel.ProcessWorkflowStatusTypeRunning {
			return "", httpx.Conflict("Process %s has a workflow still running", processID)
		}
	}

	submission, launched := latest[procmodel.ProcessWorkflowTypeBatchJobSubmission]
	switch {
	case !launched:
		return "", httpx.Conflict("Process %s has not been launched", processID)
	case submission == procmodel.ProcessWorkflowStatusTypeFailed:
		return procmodel.ProcessWorkflowTypeBatchJobSubmission, nil
	case latest[procmodel.ProcessWorkflowTypeJobCompletion] == procmodel.ProcessWorkflowStatusTypeFailed:
		return procmodel.ProcessWorkflowTypeJobCompletion, nil
	}
	return "", httpx.Conflict("Process %s has no failed workflow to retry", processID)
}

func (s *LaunchService) requireNotStopped(ctx context.Context, processID string) error {
	stopped, err := s.processService.processes.HasStatus(ctx, processID, procmodel.ProcessStatusTypeStopped)
	if stopped {
		return httpx.Conflict("Process %s is stopped", processID)
	}
	return err
}
