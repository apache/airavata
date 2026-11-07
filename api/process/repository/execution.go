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

// ExecutionRepository reads and writes process executions.
//
// Only an execution's status is ever updated: the rest records a workflow run that was
// started, which is never corrected in place.
type ExecutionRepository struct{ db *gorm.DB }

// NewExecutionRepository returns a repository backed by db.
func NewExecutionRepository(db *gorm.DB) *ExecutionRepository {
	return &ExecutionRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *ExecutionRepository) WithTx(tx *gorm.DB) *ExecutionRepository {
	return &ExecutionRepository{db: tx}
}

// FindByProcessID returns every execution recorded for one process, oldest first.
func (r *ExecutionRepository) FindByProcessID(ctx context.Context, processID string) ([]model.ProcessExecution, error) {
	var out []model.ProcessExecution
	err := r.db.WithContext(ctx).
		Where("process_id = ?", processID).
		Order("timestamp").
		Find(&out).Error
	return out, err
}

// FindByIDAndProcessID returns one execution scoped to its process, or
// gorm.ErrRecordNotFound if the id does not belong to that process.
func (r *ExecutionRepository) FindByIDAndProcessID(ctx context.Context, id, processID string) (*model.ProcessExecution, error) {
	var out model.ProcessExecution
	err := r.db.WithContext(ctx).
		First(&out, "process_execution_id = ? AND process_id = ?", id, processID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByExecutionIDAndProcessID returns the execution recorded for one workflow
// instance of one process, or gorm.ErrRecordNotFound if none was.
func (r *ExecutionRepository) FindByExecutionIDAndProcessID(ctx context.Context, executionID, processID string) (*model.ProcessExecution, error) {
	var out model.ProcessExecution
	err := r.db.WithContext(ctx).
		First(&out, "execution_id = ? AND process_id = ?", executionID, processID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateStatus sets the status of one execution, or returns gorm.ErrRecordNotFound if
// no execution has that id.
func (r *ExecutionRepository) UpdateStatus(ctx context.Context, id string, status model.ProcessExecutionStatusType) error {
	res := r.db.WithContext(ctx).Model(&model.ProcessExecution{}).
		Where("process_execution_id = ?", id).
		Update("status", status)
	if res.Error != nil {
		return res.Error
	}
	if res.RowsAffected == 0 {
		return gorm.ErrRecordNotFound
	}
	return nil
}

// Record creates the execution of one workflow instance of a process, or moves an
// existing one to status. An update changes only the status: the type and timestamp
// stay as first recorded, so the timestamp keeps saying when the execution started.
//
// It is here rather than only in the service so that the orchestration activities,
// which cannot import the service package, record executions the same way.
func (r *ExecutionRepository) Record(ctx context.Context, processID, executionID string,
	executionType model.ProcessExecutionType, status model.ProcessExecutionStatusType) (*model.ProcessExecution, error) {

	existing, err := r.FindByExecutionIDAndProcessID(ctx, executionID, processID)
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

	created := &model.ProcessExecution{
		ProcessID:     &processID,
		ExecutionID:   executionID,
		ExecutionType: &executionType,
		Status:        &status,
		Timestamp:     ptr.To(time.Now().UnixMilli()),
	}
	if err := r.Create(ctx, created); err != nil {
		return nil, err
	}
	return created, nil
}

// Create inserts an execution row. BeforeCreate assigns its id and AfterCreate
// repoints the owning process's LastExecutionID at it.
func (r *ExecutionRepository) Create(ctx context.Context, e *model.ProcessExecution) error {
	return r.db.WithContext(ctx).Create(e).Error
}
