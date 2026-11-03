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

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/process/model"
)

// TaskPauseRepository reads and writes the pauses of a process's workflows.
type TaskPauseRepository struct{ db *gorm.DB }

// NewTaskPauseRepository returns a repository backed by db.
func NewTaskPauseRepository(db *gorm.DB) *TaskPauseRepository { return &TaskPauseRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *TaskPauseRepository) WithTx(tx *gorm.DB) *TaskPauseRepository {
	return &TaskPauseRepository{db: tx}
}

// Create inserts a pause row. BeforeCreate assigns its id.
func (r *TaskPauseRepository) Create(ctx context.Context, p *model.TaskPause) error {
	return r.db.WithContext(ctx).Create(p).Error
}

// FindByProcessID returns every pause of one process, oldest first.
func (r *TaskPauseRepository) FindByProcessID(ctx context.Context, processID string) ([]model.TaskPause, error) {
	var out []model.TaskPause
	err := r.db.WithContext(ctx).
		Where("process_id = ?", processID).
		Order("paused_at").
		Find(&out).Error
	return out, err
}

// FindOpenByProcessID returns the latest pause of one process that has not been
// continued, or gorm.ErrRecordNotFound when its workflows are not waiting.
func (r *TaskPauseRepository) FindOpenByProcessID(ctx context.Context, processID string) (*model.TaskPause, error) {
	var out model.TaskPause
	err := r.db.WithContext(ctx).
		Where("process_id = ? AND continued_at IS NULL", processID).
		Order("paused_at DESC").
		First(&out).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// MarkContinued stamps an open pause as continued at continuedAt, reporting false when
// it was not open. The condition is in the update itself, so of two requests racing to
// continue one pause exactly one wins, and the workflow is signalled once.
func (r *TaskPauseRepository) MarkContinued(ctx context.Context, id string, continuedAt int64) (bool, error) {
	res := r.db.WithContext(ctx).
		Model(&model.TaskPause{}).
		Where("task_pause_id = ? AND continued_at IS NULL", id).
		Update("continued_at", continuedAt)
	return res.RowsAffected == 1, res.Error
}
