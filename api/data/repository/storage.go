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

// SCPDataStorageRepository reads and writes SCP data storages.
//
// Reads preload the SSH key because the response DTO carries its public summary: the
// host and the login user are columns on the storage itself, but the key it presents
// is a reference, and a storage without it is not much use to a caller deciding
// whether to put data there.
type SCPDataStorageRepository struct{ db *gorm.DB }

// NewSCPDataStorageRepository returns a repository backed by db.
func NewSCPDataStorageRepository(db *gorm.DB) *SCPDataStorageRepository {
	return &SCPDataStorageRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SCPDataStorageRepository) WithTx(tx *gorm.DB) *SCPDataStorageRepository {
	return &SCPDataStorageRepository{db: tx}
}

// withReferences is the read scope every lookup that feeds a response DTO starts from.
func (r *SCPDataStorageRepository) withReferences(ctx context.Context) *gorm.DB {
	return r.db.WithContext(ctx).Preload("SSHKey")
}

// FindAll returns every storage.
func (r *SCPDataStorageRepository) FindAll(ctx context.Context) ([]model.SCPDataStorage, error) {
	var out []model.SCPDataStorage
	err := r.withReferences(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one storage, or gorm.ErrRecordNotFound.
func (r *SCPDataStorageRepository) FindByID(ctx context.Context, id string) (*model.SCPDataStorage, error) {
	var out model.SCPDataStorage
	if err := r.withReferences(ctx).First(&out, "data_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByOwnerID returns every storage owned by one user.
func (r *SCPDataStorageRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.SCPDataStorage, error) {
	var out []model.SCPDataStorage
	err := r.withReferences(ctx).Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByIDsExcludingOwner returns the storages named by ids that userID does not own.
//
// The ids come from the sharing table, which no longer knows what kind of record it is
// opening up beyond its type tag; this turns them back into entities. Storages the
// caller owns are dropped, because ownership is not a share and the caller asking
// "what has been shared with me?" already has /me for their own.
func (r *SCPDataStorageRepository) FindByIDsExcludingOwner(ctx context.Context, ids []string, userID string) ([]model.SCPDataStorage, error) {
	if len(ids) == 0 {
		return nil, nil
	}
	var out []model.SCPDataStorage
	err := r.db.WithContext(ctx).
		Where("data_id IN ? AND (user_id IS NULL OR user_id <> ?)", ids, userID).
		Find(&out).Error
	return out, err
}

// CountByKeyID reports how many storages still present one SSH key, for the same
// reason the cluster configs do: the key's foreign key is RESTRICT, and a 409 naming
// what holds it beats a constraint violation.
func (r *SCPDataStorageRepository) CountByKeyID(ctx context.Context, keyID string) (int64, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.SCPDataStorage{}).
		Where("ssh_key_id = ?", keyID).Count(&n).Error
	return n, err
}

// Save inserts or updates a storage.
func (r *SCPDataStorageRepository) Save(ctx context.Context, s *model.SCPDataStorage) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// Delete removes a storage.
func (r *SCPDataStorageRepository) Delete(ctx context.Context, s *model.SCPDataStorage) error {
	return r.db.WithContext(ctx).Delete(s).Error
}
