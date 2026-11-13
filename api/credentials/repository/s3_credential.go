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

	model "github.com/apache/airavata/api/credentials/model"
)

// S3CredentialRepository reads and writes registered S3 access keys.
type S3CredentialRepository struct{ db *gorm.DB }

// NewS3CredentialRepository returns a repository backed by db.
func NewS3CredentialRepository(db *gorm.DB) *S3CredentialRepository {
	return &S3CredentialRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *S3CredentialRepository) WithTx(tx *gorm.DB) *S3CredentialRepository {
	return &S3CredentialRepository{db: tx}
}

// FindByOwnerID returns every credential registered by one user. As with SSH
// credentials there is no listing across owners.
func (r *S3CredentialRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.S3Credential, error) {
	var out []model.S3Credential
	err := r.db.WithContext(ctx).Where("owner_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByID returns one credential, or gorm.ErrRecordNotFound.
func (r *S3CredentialRepository) FindByID(ctx context.Context, id string) (*model.S3Credential, error) {
	var out model.S3Credential
	if err := r.db.WithContext(ctx).First(&out, "s3_credential_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a credential.
func (r *S3CredentialRepository) Save(ctx context.Context, c *model.S3Credential) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// Delete removes a credential.
func (r *S3CredentialRepository) Delete(ctx context.Context, c *model.S3Credential) error {
	return r.db.WithContext(ctx).Delete(c).Error
}
