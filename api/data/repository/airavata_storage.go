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

	model "github.com/apache/airavata/api/data/model"
)

// AiravataDataStorageRepository reads and writes the per-user storages kept on the
// Airavata host.
type AiravataDataStorageRepository struct{ db *gorm.DB }

// NewAiravataDataStorageRepository returns a repository backed by db.
func NewAiravataDataStorageRepository(db *gorm.DB) *AiravataDataStorageRepository {
	return &AiravataDataStorageRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *AiravataDataStorageRepository) WithTx(tx *gorm.DB) *AiravataDataStorageRepository {
	return &AiravataDataStorageRepository{db: tx}
}

// FindByID returns one storage, or gorm.ErrRecordNotFound.
func (r *AiravataDataStorageRepository) FindByID(ctx context.Context, id string) (*model.AiravataDataStorage, error) {
	var out model.AiravataDataStorage
	if err := r.db.WithContext(ctx).First(&out, "data_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByOwnerID returns the one storage userID owns, or gorm.ErrRecordNotFound.
func (r *AiravataDataStorageRepository) FindByOwnerID(ctx context.Context, userID string) (*model.AiravataDataStorage, error) {
	var out model.AiravataDataStorage
	if err := r.db.WithContext(ctx).First(&out, "user_id = ?", userID).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Create inserts a storage. A second storage for the same owner fails on the unique
// index on user_id.
func (r *AiravataDataStorageRepository) Create(ctx context.Context, s *model.AiravataDataStorage) error {
	return r.db.WithContext(ctx).Create(s).Error
}

// Delete removes a storage.
func (r *AiravataDataStorageRepository) Delete(ctx context.Context, s *model.AiravataDataStorage) error {
	return r.db.WithContext(ctx).Delete(s).Error
}
