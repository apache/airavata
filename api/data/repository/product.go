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

// Package repository reads and writes registered datasets, the storages they live on,
// and the sharing rows that open either up.
package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/data/model"
)

// DataProductRepository reads and writes registered datasets.
type DataProductRepository struct{ db *gorm.DB }

// NewDataProductRepository returns a repository backed by db.
func NewDataProductRepository(db *gorm.DB) *DataProductRepository {
	return &DataProductRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *DataProductRepository) WithTx(tx *gorm.DB) *DataProductRepository {
	return &DataProductRepository{db: tx}
}

// FindAll returns every product across every owner.
func (r *DataProductRepository) FindAll(ctx context.Context) ([]model.DataProduct, error) {
	var out []model.DataProduct
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one product, or gorm.ErrRecordNotFound.
func (r *DataProductRepository) FindByID(ctx context.Context, id string) (*model.DataProduct, error) {
	var out model.DataProduct
	if err := r.db.WithContext(ctx).First(&out, "data_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByOwnerID returns every product owned by one user.
func (r *DataProductRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.DataProduct, error) {
	var out []model.DataProduct
	err := r.db.WithContext(ctx).Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindByDataStorageID returns every product staged on one storage. It is what makes
// deleting a storage still in use reportable as a conflict.
func (r *DataProductRepository) FindByDataStorageID(ctx context.Context, storageID string) ([]model.DataProduct, error) {
	var out []model.DataProduct
	err := r.db.WithContext(ctx).Where("data_storage_id = ?", storageID).Find(&out).Error
	return out, err
}

// FindByIDsExcludingOwner returns the products named by ids that userID does not own.
//
// The ids come from the sharing table, which no longer knows what kind of record it is
// opening up beyond its type tag; this turns them back into entities. Data products the
// caller owns are dropped, because ownership is not a share and the caller asking
// "what has been shared with me?" already has /me for their own.
func (r *DataProductRepository) FindByIDsExcludingOwner(ctx context.Context, ids []string, userID string) ([]model.DataProduct, error) {
	if len(ids) == 0 {
		return nil, nil
	}
	var out []model.DataProduct
	err := r.db.WithContext(ctx).
		Where("data_id IN ? AND (user_id IS NULL OR user_id <> ?)", ids, userID).
		Find(&out).Error
	return out, err
}

// Save inserts or updates a product.
func (r *DataProductRepository) Save(ctx context.Context, p *model.DataProduct) error {
	return r.db.WithContext(ctx).Save(p).Error
}

// Delete removes a product.
func (r *DataProductRepository) Delete(ctx context.Context, p *model.DataProduct) error {
	return r.db.WithContext(ctx).Delete(p).Error
}
