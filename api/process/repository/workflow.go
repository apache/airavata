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

package repository

import (
	"context"
	"errors"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/ptr"

	model "github.com/apache/airavata/api/process/model"
)

// WorkflowRepository reads and writes process workflows.
//
// Only a process workflow's status is ever updated: the rest records a workflow run
// that was started, which is never corrected in place.
type WorkflowRepository struct{ db *gorm.DB }

// NewWorkflowRepository returns a repository backed by db.
func NewWorkflowRepository(db *gorm.DB) *WorkflowRepository {
	return &WorkflowRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *WorkflowRepository) WithTx(tx *gorm.DB) *WorkflowRepository {
	return &WorkflowRepository{db: tx}
}

// FindByProcessID returns every workflow recorded for one process, oldest first.
func (r *WorkflowRepository) FindByProcessID(ctx context.Context, processID string) ([]model.ProcessWorkflow, error) {
	var out []model.ProcessWorkflow
	err := r.db.WithContext(ctx).
		Where("process_id = ?", processID).
		Order("timestamp").
		Find(&out).Error
	return out, err
}

// FindByIDAndProcessID returns one workflow scoped to its process, or
// gorm.ErrRecordNotFound if the id does not belong to that process.
func (r *WorkflowRepository) FindByIDAndProcessID(ctx context.Context, id, processID string) (*model.ProcessWorkflow, error) {
	var out model.ProcessWorkflow
	err := r.db.WithContext(ctx).
		First(&out, "process_workflow_id = ? AND process_id = ?", id, processID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByWorkflowIDAndProcessID returns the workflow recorded for one workflow
// instance of one process, or gorm.ErrRecordNotFound if none was.
func (r *WorkflowRepository) FindByWorkflowIDAndProcessID(ctx context.Context, workflowID, processID string) (*model.ProcessWorkflow, error) {
	var out model.ProcessWorkflow
	err := r.db.WithContext(ctx).
		First(&out, "workflow_id = ? AND process_id = ?", workflowID, processID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateStatus sets the status of one workflow, or returns gorm.ErrRecordNotFound if
// no workflow has that id.
func (r *WorkflowRepository) UpdateStatus(ctx context.Context, id string, status model.ProcessWorkflowStatusType) error {
	res := r.db.WithContext(ctx).Model(&model.ProcessWorkflow{}).
		Where("process_workflow_id = ?", id).
		Update("status", status)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

// Record creates the workflow of one workflow instance of a process, or moves an
// existing one to status. An update changes only the status: the type and timestamp
// stay as first recorded, so the timestamp keeps saying when the workflow started.
//
// It is here rather than only in the service so that the orchestration activities,
// which cannot import the service package, record workflows the same way.
func (r *WorkflowRepository) Record(ctx context.Context, processID, workflowID string,
	workflowType model.ProcessWorkflowType, status model.ProcessWorkflowStatusType) (*model.ProcessWorkflow, error) {

	existing, err := r.FindByWorkflowIDAndProcessID(ctx, workflowID, processID)
	switch {
	case err == nil:
		if err := r.UpdateStatus(ctx, existing.ID, status); err != nil {
			return nil, err
		}
		existing.Status = &status
		return existing, nil
	case !errors.Is(err, gorm.ErrRecordNotFound):
		return nil, err
	}

	created := &model.ProcessWorkflow{
		ProcessID:    &processID,
		WorkflowID:   workflowID,
		WorkflowType: &workflowType,
		Status:       &status,
		Timestamp:    ptr.To(time.Now().UnixMilli()),
	}
	if err := r.Create(ctx, created); err != nil {
		return nil, err
	}
	return created, nil
}

// Create inserts a process workflow row. BeforeCreate assigns its id and AfterCreate
// repoints the owning process's LastWorkflowID at it.
func (r *WorkflowRepository) Create(ctx context.Context, e *model.ProcessWorkflow) error {
	return r.db.WithContext(ctx).Create(e).Error
}
