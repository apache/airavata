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

	model "github.com/apache/airavata/api/iam/model"
)

// UserRepository reads and writes user records.
type UserRepository struct{ db *gorm.DB }

// NewUserRepository returns a repository backed by db.
func NewUserRepository(db *gorm.DB) *UserRepository { return &UserRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *UserRepository) WithTx(tx *gorm.DB) *UserRepository { return &UserRepository{db: tx} }

// FindAll returns every user.
func (r *UserRepository) FindAll(ctx context.Context) ([]model.User, error) {
	var out []model.User
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one user, or gorm.ErrRecordNotFound.
func (r *UserRepository) FindByID(ctx context.Context, id string) (*model.User, error) {
	var out model.User
	if err := r.db.WithContext(ctx).First(&out, "user_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a user.
func (r *UserRepository) Save(ctx context.Context, u *model.User) error {
	return r.db.WithContext(ctx).Save(u).Error
}
