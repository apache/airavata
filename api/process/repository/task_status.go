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

// TaskStatusRepository reads and writes task statuses.
//
// There is no Update: statuses are append-only history, never corrected in place.
type TaskStatusRepository struct{ db *gorm.DB }

// NewTaskStatusRepository returns a repository backed by db.
func NewTaskStatusRepository(db *gorm.DB) *TaskStatusRepository {
	return &TaskStatusRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *TaskStatusRepository) WithTx(tx *gorm.DB) *TaskStatusRepository {
	return &TaskStatusRepository{db: tx}
}

// FindByTaskID returns every status recorded for one task, oldest first.
func (r *TaskStatusRepository) FindByTaskID(ctx context.Context, taskID string) ([]model.TaskStatus, error) {
	var out []model.TaskStatus
	err := r.db.WithContext(ctx).
		Where("task_id = ?", taskID).
		Order("timestamp").
		Find(&out).Error
	return out, err
}

// FindByProcessID returns every status recorded for the tasks of one process, oldest
// first.
func (r *TaskStatusRepository) FindByProcessID(ctx context.Context, processID string) ([]model.TaskStatus, error) {
	var out []model.TaskStatus
	err := r.db.WithContext(ctx).
		Where("process_id = ?", processID).
		Order("timestamp").
		Find(&out).Error
	return out, err
}

// Create inserts a status row. BeforeCreate assigns its id.
func (r *TaskStatusRepository) Create(ctx context.Context, s *model.TaskStatus) error {
	return r.db.WithContext(ctx).Create(s).Error
}
