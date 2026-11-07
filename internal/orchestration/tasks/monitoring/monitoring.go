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

// Package monitoring follows a submitted batch job: the task that watches it, and the
// mailbox the scheduler reports its state changes to.
package monitoring

import (
	"context"
	"log/slog"

	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/orchestration/execution"
)

// Tasks carries out the monitoring tasks of a process. Its methods are workflow
// activities.
type Tasks struct {
	processes          *processrepo.ProcessRepository
	jobMonitoringTasks *processrepo.JobMonitoringTaskRepository
	tracker            *execution.TaskTracker
}

// New returns the monitoring tasks over the given repositories.
func New(processes *processrepo.ProcessRepository, jobMonitoringTasks *processrepo.JobMonitoringTaskRepository,
	tracker *execution.TaskTracker) *Tasks {
	return &Tasks{processes: processes, jobMonitoringTasks: jobMonitoringTasks, tracker: tracker}
}

// MonitorBatchJob checks the outcome of a finished batch job, recording the task's
// status as it goes.
func (a *Tasks) MonitorBatchJob(ctx context.Context, executionContext *execution.Context, processID string, taskID string) (*execution.Context, error) {
	return a.tracker.Track(ctx, processID, taskID, model.TaskTypeJobMonitoring, executionContext, func() (*execution.Context, error) {
		return a.monitorBatchJob(ctx, executionContext, processID, taskID)
	})
}

func (a *Tasks) monitorBatchJob(ctx context.Context, executionContext *execution.Context, processID string, taskID string) (*execution.Context, error) {
	slog.Info("Starting to monitor batch job ", "taskId", taskID, "processId", processID)
	process, err := execution.FindBatchProcess(ctx, a.processes, processID)
	if err != nil {
		slog.Error("Failed to retrieve process for monitoring batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	jmt, err := a.jobMonitoringTasks.FindByIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		slog.Error("Failed to retrieve job monitoring task for monitoring batch job", "taskId", taskID, "processId", processID, "error", err)
		return nil, err
	}
	slog.Info("Completed monitoring batch job", "taskId", taskID, "processId", processID, "jobId", process.BatchProcess.JobID, "JMT Id", jmt.ID)
	return executionContext, nil
}
