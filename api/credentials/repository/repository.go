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

// Package repository reads and writes registered SSH credentials.
package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/credentials/model"
)

// SSHCredentialRepository reads and writes registered SSH credentials.
type SSHCredentialRepository struct{ db *gorm.DB }

// NewSSHCredentialRepository returns a repository backed by db.
func NewSSHCredentialRepository(db *gorm.DB) *SSHCredentialRepository {
	return &SSHCredentialRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SSHCredentialRepository) WithTx(tx *gorm.DB) *SSHCredentialRepository {
	return &SSHCredentialRepository{db: tx}
}

// FindAll returns every key.
func (r *SSHCredentialRepository) FindAll(ctx context.Context) ([]model.SSHCredential, error) {
	var out []model.SSHCredential
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindByOwnerID returns every key registered by one user. There is no listing across
// owners: a credential is private to whoever registered it.
func (r *SSHCredentialRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.SSHCredential, error) {
	var out []model.SSHCredential
	err := r.db.WithContext(ctx).Where("owner_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByID returns one key, or gorm.ErrRecordNotFound.
func (r *SSHCredentialRepository) FindByID(ctx context.Context, id string) (*model.SSHCredential, error) {
	var out model.SSHCredential
	if err := r.db.WithContext(ctx).First(&out, "ssh_credential_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a key.
func (r *SSHCredentialRepository) Save(ctx context.Context, k *model.SSHCredential) error {
	return r.db.WithContext(ctx).Save(k).Error
}

// Delete removes a key.
func (r *SSHCredentialRepository) Delete(ctx context.Context, k *model.SSHCredential) error {
	return r.db.WithContext(ctx).Delete(k).Error
}

// ExistsByName reports whether a key of that name is already registered. Declared but
// never called in the Java service, and preserved here for the same reason.
func (r *SSHCredentialRepository) ExistsByName(ctx context.Context, name string) (bool, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.SSHCredential{}).Where("ssh_credential_name = ?", name).Count(&n).Error
	return n > 0, err
}
