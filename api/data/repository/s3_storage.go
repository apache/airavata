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

// S3DataStorageRepository reads and writes S3 data storages.
//
// Reads preload the credential because the response DTO carries its safe summary, as
// SCP storage reads preload the SSH credential.
type S3DataStorageRepository struct{ db *gorm.DB }

// NewS3DataStorageRepository returns a repository backed by db.
func NewS3DataStorageRepository(db *gorm.DB) *S3DataStorageRepository {
	return &S3DataStorageRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *S3DataStorageRepository) WithTx(tx *gorm.DB) *S3DataStorageRepository {
	return &S3DataStorageRepository{db: tx}
}

func (r *S3DataStorageRepository) withReferences(ctx context.Context) *gorm.DB {
	return r.db.WithContext(ctx).Preload("S3Credential")
}

// FindAll returns every storage.
func (r *S3DataStorageRepository) FindAll(ctx context.Context) ([]model.S3DataStorage, error) {
	var out []model.S3DataStorage
	err := r.withReferences(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one storage, or gorm.ErrRecordNotFound.
func (r *S3DataStorageRepository) FindByID(ctx context.Context, id string) (*model.S3DataStorage, error) {
	var out model.S3DataStorage
	if err := r.withReferences(ctx).First(&out, "data_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByOwnerID returns every storage owned by one user.
func (r *S3DataStorageRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.S3DataStorage, error) {
	var out []model.S3DataStorage
	err := r.withReferences(ctx).Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByIDsExcludingOwner returns the storages named by ids that userID does not own.
// See SCPDataStorageRepository.FindByIDsExcludingOwner.
func (r *S3DataStorageRepository) FindByIDsExcludingOwner(ctx context.Context, ids []string, userID string) ([]model.S3DataStorage, error) {
	if len(ids) == 0 {
		return nil, nil
	}
	var out []model.S3DataStorage
	err := r.withReferences(ctx).
		Where("data_id IN ? AND (user_id IS NULL OR user_id <> ?)", ids, userID).
		Find(&out).Error
	return out, err
}

// CountByS3CredentialID reports how many storages still present one credential, so
// deleting a credential in use is a 409 rather than a constraint violation.
func (r *S3DataStorageRepository) CountByS3CredentialID(ctx context.Context, credentialID string) (int64, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.S3DataStorage{}).
		Where("s3_credential_id = ?", credentialID).Count(&n).Error
	return n, err
}

// Save inserts or updates a storage.
func (r *S3DataStorageRepository) Save(ctx context.Context, s *model.S3DataStorage) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// Delete removes a storage.
func (r *S3DataStorageRepository) Delete(ctx context.Context, s *model.S3DataStorage) error {
	return r.db.WithContext(ctx).Delete(s).Error
}
