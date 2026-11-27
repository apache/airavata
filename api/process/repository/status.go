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
	"time"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/internal/ptr"
)

// StatusRepository reads and writes process statuses.
//
// There is no Update: statuses are append-only history, never corrected in place.
type StatusRepository struct{ db *gorm.DB }

// NewStatusRepository returns a repository backed by db.
func NewStatusRepository(db *gorm.DB) *StatusRepository { return &StatusRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *StatusRepository) WithTx(tx *gorm.DB) *StatusRepository { return &StatusRepository{db: tx} }

// FindByProcessID returns every status recorded for one process, oldest first.
func (r *StatusRepository) FindByProcessID(ctx context.Context, processID string) ([]model.ProcessStatus, error) {
	var out []model.ProcessStatus
	err := r.db.WithContext(ctx).
		Where("process_id = ?", processID).
		Order("timestamp").
		Find(&out).Error
	return out, err
}

// FindByIDAndProcessID returns one status scoped to its process, or
// gorm.ErrRecordNotFound if the id does not belong to that process.
func (r *StatusRepository) FindByIDAndProcessID(ctx context.Context, id, processID string) (*model.ProcessStatus, error) {
	var out model.ProcessStatus
	err := r.db.WithContext(ctx).
		First(&out, "process_status_id = ? AND process_id = ?", id, processID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// MarkRunning records a SUBMITTED process as RUNNING, with log. Linkspan's dial and
// Slurm's begin mail both mark a run RUNNING; whichever comes second records nothing.
func (r *StatusRepository) MarkRunning(ctx context.Context, processID, log string) error {
	found, err := r.FindByProcessID(ctx, processID)
	if err != nil || len(found) == 0 || ptr.From(found[len(found)-1].Status) != model.ProcessStatusTypeSubmitted {
		return err
	}
	running := model.ProcessStatusTypeRunning
	return r.Create(ctx, &model.ProcessStatus{ProcessID: &processID, Status: &running, Log: &log, Timestamp: ptr.To(time.Now().UnixMilli())})
}

// Create inserts a status row. BeforeCreate assigns its id and AfterCreate repoints
// the owning process's LastStatusID at it.
func (r *StatusRepository) Create(ctx context.Context, s *model.ProcessStatus) error {
	return r.db.WithContext(ctx).Create(s).Error
}
