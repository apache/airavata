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
	"github.com/apache/airavata/internal/permission"

	dto "github.com/apache/airavata/api/process/dto"
	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/api/process/repository"
)

// WorkflowService records and reads the workflows started for a process.
//
// There is no create endpoint, by design: a workflow is recorded by the code that
// starts a workflow for the process, never accepted from a client request body. Reads
// are owner-scoped like TaskStatusService's, since a workflow id is a handle on the
// workflow engine's own record of the run.
type WorkflowService struct {
	db        *gorm.DB
	workflows *repository.WorkflowRepository
	processes *repository.ProcessRepository
}

// NewWorkflowService returns a process workflow service.
func NewWorkflowService(db *gorm.DB, workflows *repository.WorkflowRepository, processes *repository.ProcessRepository) *WorkflowService {
	return &WorkflowService{db: db, workflows: workflows, processes: processes}
}

// Record records where a workflow of a process stands, in its own
// transaction: the first call for a workflowID creates the record, and later
// calls for the same one move it to the new status — RUNNING when the workflow is
// started, SUCCEEDED or FAILED when it finishes.
//
// It does not check authorisation: recording a workflow is an internal operation
// performed by trusted service code, never taken directly from a client request.
func (s *WorkflowService) Record(ctx context.Context, processID, workflowID string, workflowType model.ProcessWorkflowType, status model.ProcessWorkflowStatusType) (*dto.WorkflowResponse, error) {
	var out dto.WorkflowResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		recorded, err := s.RecordTx(ctx, tx, processID, workflowID, workflowType, status)
		if err != nil {
			return err
		}
		out = dto.ToWorkflowResponse(recorded)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// RecordTx creates or updates a process workflow inside an already-open transaction, so a
// caller that is already inside one of its own commits or rolls back the workflow
// together with the rest of its work.
//
// See WorkflowRepository.Record for what an update changes.
func (s *WorkflowService) RecordTx(ctx context.Context, tx *gorm.DB, processID, workflowID string, workflowType model.ProcessWorkflowType, status model.ProcessWorkflowStatusType) (*model.ProcessWorkflow, error) {
	if _, err := s.processes.WithTx(tx).FindByID(ctx, processID); err != nil {
		return nil, notFoundAs(err, "Process not found: %s", processID)
	}

	return s.workflows.WithTx(tx).Record(ctx, processID, workflowID, workflowType, status)
}

// ListForProcess returns every workflow recorded for a process, oldest first.
func (s *WorkflowService) ListForProcess(ctx context.Context, processID string) ([]dto.WorkflowResponse, error) {
	if err := s.requireProcess(ctx, processID); err != nil {
		return nil, err
	}
	found, err := s.workflows.FindByProcessID(ctx, processID)
	if err != nil {
		return nil, err
	}
	return dto.ToWorkflowResponses(found), nil
}

// Get returns one process workflow by its processWorkflowId, scoped to the process it must belong to.
func (s *WorkflowService) Get(ctx context.Context, processID, processWorkflowID string) (*dto.WorkflowResponse, error) {
	if err := s.requireProcess(ctx, processID); err != nil {
		return nil, err
	}
	found, err := s.workflows.FindByIDAndProcessID(ctx, processWorkflowID, processID)
	if err != nil {
		return nil, notFoundAs(err, "Workflow not found: %s in process %s", processWorkflowID, processID)
	}
	out := dto.ToWorkflowResponse(found)
	return &out, nil
}

// requireProcess checks the process exists and the caller may see its workflows: its
// owner, or an admin.
func (s *WorkflowService) requireProcess(ctx context.Context, processID string) error {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return err
	}
	proc, err := s.processes.FindByID(ctx, processID)
	if err != nil {
		return notFoundAs(err, "Process not found: %s", processID)
	}
	if !proc.OwnedBy(principal.Name) && !principal.Can(permission.ProcessManageAny) {
		return httpx.Forbidden("Access denied: you may only work with workflows of your own processes")
	}
	return nil
}
