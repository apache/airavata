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

// Package repository reads and writes registered SSH keys.
package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/credentials/model"
)

// SSHKeyRepository reads and writes registered SSH keypairs.
type SSHKeyRepository struct{ db *gorm.DB }

// NewSSHKeyRepository returns a repository backed by db.
func NewSSHKeyRepository(db *gorm.DB) *SSHKeyRepository { return &SSHKeyRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *SSHKeyRepository) WithTx(tx *gorm.DB) *SSHKeyRepository { return &SSHKeyRepository{db: tx} }

// FindAll returns every key.
func (r *SSHKeyRepository) FindAll(ctx context.Context) ([]model.SSHKey, error) {
	var out []model.SSHKey
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindByOwnerID returns every key registered by one user. There is no listing across
// owners: a key is private to whoever registered it.
func (r *SSHKeyRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.SSHKey, error) {
	var out []model.SSHKey
	err := r.db.WithContext(ctx).Where("owner_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByID returns one key, or gorm.ErrRecordNotFound.
func (r *SSHKeyRepository) FindByID(ctx context.Context, id string) (*model.SSHKey, error) {
	var out model.SSHKey
	if err := r.db.WithContext(ctx).First(&out, "ssh_key_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a key.
func (r *SSHKeyRepository) Save(ctx context.Context, k *model.SSHKey) error {
	return r.db.WithContext(ctx).Save(k).Error
}

// Delete removes a key.
func (r *SSHKeyRepository) Delete(ctx context.Context, k *model.SSHKey) error {
	return r.db.WithContext(ctx).Delete(k).Error
}

// ExistsByName reports whether a key of that name is already registered. Declared but
// never called in the Java service, and preserved here for the same reason.
func (r *SSHKeyRepository) ExistsByName(ctx context.Context, name string) (bool, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.SSHKey{}).Where("ssh_key_name = ?", name).Count(&n).Error
	return n > 0, err
}
