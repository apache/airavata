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

// LinkRepository persists the link secrets issued to each process's job.
type LinkRepository struct{ db *gorm.DB }

// NewLinkRepository returns a repository over db.
func NewLinkRepository(db *gorm.DB) *LinkRepository { return &LinkRepository{db: db} }

// FindByProcessID returns the process's link, or gorm.ErrRecordNotFound.
func (r *LinkRepository) FindByProcessID(ctx context.Context, processID string) (*model.ProcessLink, error) {
	var out model.ProcessLink
	if err := r.db.WithContext(ctx).First(&out, "process_id = ?", processID).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts the link or replaces the process's existing one.
func (r *LinkRepository) Save(ctx context.Context, link *model.ProcessLink) error {
	return r.db.WithContext(ctx).Save(link).Error
}
