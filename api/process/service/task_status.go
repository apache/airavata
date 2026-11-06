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

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/process/dto"
	"github.com/apache/airavata/api/process/repository"
)

// TaskStatusService reads the status history of a process's tasks.
//
// There is nothing to write here: the orchestration engine records task statuses as it
// runs each task. Reads are owner-scoped like TaskService's, since a failed task's log
// carries the error it failed with — remote paths and command output included.
type TaskStatusService struct {
	statuses  *repository.TaskStatusRepository
	processes *repository.ProcessRepository
}

// NewTaskStatusService returns a task status service.
func NewTaskStatusService(statuses *repository.TaskStatusRepository, processes *repository.ProcessRepository) *TaskStatusService {
	return &TaskStatusService{statuses: statuses, processes: processes}
}

// ListForProcess returns every status recorded for any task of a process, oldest first.
func (s *TaskStatusService) ListForProcess(ctx context.Context, processID string) ([]dto.TaskStatusResponse, error) {
	if err := s.requireProcess(ctx, processID); err != nil {
		return nil, err
	}
	found, err := s.statuses.FindByProcessID(ctx, processID)
	if err != nil {
		return nil, err
	}
	return dto.ToTaskStatusResponses(found), nil
}

// ListForTask returns every status recorded for one task, oldest first.
//
// The task is matched on both ids, so a task of another process yields an empty list
// rather than that process's history. An unknown task id is an empty list too, not a
// 404: the id may name a row in any of the four task tables, and a task the engine has
// not reached yet has no history to show.
func (s *TaskStatusService) ListForTask(ctx context.Context, processID, taskID string) ([]dto.TaskStatusResponse, error) {
	if err := s.requireProcess(ctx, processID); err != nil {
		return nil, err
	}
	found, err := s.statuses.FindByTaskIDAndProcessID(ctx, taskID, processID)
	if err != nil {
		return nil, err
	}
	return dto.ToTaskStatusResponses(found), nil
}

// requireProcess checks the process exists and the caller may see its tasks: its
// owner, or an admin.
func (s *TaskStatusService) requireProcess(ctx context.Context, processID string) error {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return err
	}
	proc, err := s.processes.FindByID(ctx, processID)
	if err != nil {
		return notFoundAs(err, "Process not found: %s", processID)
	}
	if !proc.OwnedBy(principal.Name) && !principal.IsAdmin() {
		return httpx.Forbidden("Access denied: you may only work with tasks of your own processes")
	}
	return nil
}
