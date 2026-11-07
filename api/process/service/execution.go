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

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/process/dto"
	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/api/process/repository"
)

// ExecutionService records and reads the workflow executions started for a process.
//
// There is no create endpoint, by design: an execution is recorded by the code that
// starts a workflow for the process, never accepted from a client request body. Reads
// are owner-scoped like TaskStatusService's, since an execution id is a handle on the
// workflow engine's own record of the run.
type ExecutionService struct {
	db         *gorm.DB
	executions *repository.ExecutionRepository
	processes  *repository.ProcessRepository
}

// NewExecutionService returns an execution service.
func NewExecutionService(db *gorm.DB, executions *repository.ExecutionRepository, processes *repository.ProcessRepository) *ExecutionService {
	return &ExecutionService{db: db, executions: executions, processes: processes}
}

// Record records where a workflow execution of a process stands, in its own
// transaction: the first call for an executionID creates the execution, and later
// calls for the same one move it to the new status — RUNNING when the workflow is
// started, SUCCEEDED or FAILED when it finishes.
//
// It does not check authorisation: recording an execution is an internal operation
// performed by trusted service code, never taken directly from a client request.
func (s *ExecutionService) Record(ctx context.Context, processID, executionID string, executionType model.ProcessExecutionType, status model.ProcessExecutionStatusType) (*dto.ExecutionResponse, error) {
	var out dto.ExecutionResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		recorded, err := s.RecordTx(ctx, tx, processID, executionID, executionType, status)
		if err != nil {
			return err
		}
		out = dto.ToExecutionResponse(recorded)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// RecordTx creates or updates an execution inside an already-open transaction, so a
// caller that is already inside one of its own commits or rolls back the execution
// together with the rest of its work.
//
// See ExecutionRepository.Record for what an update changes.
func (s *ExecutionService) RecordTx(ctx context.Context, tx *gorm.DB, processID, executionID string, executionType model.ProcessExecutionType, status model.ProcessExecutionStatusType) (*model.ProcessExecution, error) {
	if _, err := s.processes.WithTx(tx).FindByID(ctx, processID); err != nil {
		return nil, notFoundAs(err, "Process not found: %s", processID)
	}

	return s.executions.WithTx(tx).Record(ctx, processID, executionID, executionType, status)
}

// ListForProcess returns every execution recorded for a process, oldest first.
func (s *ExecutionService) ListForProcess(ctx context.Context, processID string) ([]dto.ExecutionResponse, error) {
	if err := s.requireProcess(ctx, processID); err != nil {
		return nil, err
	}
	found, err := s.executions.FindByProcessID(ctx, processID)
	if err != nil {
		return nil, err
	}
	return dto.ToExecutionResponses(found), nil
}

// Get returns one execution, scoped to the process it must belong to.
func (s *ExecutionService) Get(ctx context.Context, processID, executionID string) (*dto.ExecutionResponse, error) {
	if err := s.requireProcess(ctx, processID); err != nil {
		return nil, err
	}
	found, err := s.executions.FindByIDAndProcessID(ctx, executionID, processID)
	if err != nil {
		return nil, notFoundAs(err, "Execution not found: %s in process %s", executionID, processID)
	}
	out := dto.ToExecutionResponse(found)
	return &out, nil
}

// requireProcess checks the process exists and the caller may see its executions: its
// owner, or an admin.
func (s *ExecutionService) requireProcess(ctx context.Context, processID string) error {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return err
	}
	proc, err := s.processes.FindByID(ctx, processID)
	if err != nil {
		return notFoundAs(err, "Process not found: %s", processID)
	}
	if !proc.OwnedBy(principal.Name) && !principal.IsAdmin() {
		return httpx.Forbidden("Access denied: you may only work with executions of your own processes")
	}
	return nil
}
