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

// replicaOrder lists the primary location first and the rest oldest first.
const replicaOrder = "is_primary DESC, created_at, replica_id"

// DataProductReplicaRepository reads and writes the locations of data products.
type DataProductReplicaRepository struct{ db *gorm.DB }

// NewDataProductReplicaRepository returns a repository backed by db.
func NewDataProductReplicaRepository(db *gorm.DB) *DataProductReplicaRepository {
	return &DataProductReplicaRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *DataProductReplicaRepository) WithTx(tx *gorm.DB) *DataProductReplicaRepository {
	return &DataProductReplicaRepository{db: tx}
}

// FindByProductID returns every location of one product, primary first.
func (r *DataProductReplicaRepository) FindByProductID(ctx context.Context, productID string) ([]model.DataProductReplica, error) {
	var out []model.DataProductReplica
	err := r.db.WithContext(ctx).Where("data_product_id = ?", productID).Order(replicaOrder).Find(&out).Error
	return out, err
}

// FindByID returns one replica of one product, or gorm.ErrRecordNotFound. Scoping by
// product means a replica cannot be reached through a product it does not belong to.
func (r *DataProductReplicaRepository) FindByID(ctx context.Context, productID, replicaID string) (*model.DataProductReplica, error) {
	var out model.DataProductReplica
	err := r.db.WithContext(ctx).
		First(&out, "data_product_id = ? AND replica_id = ?", productID, replicaID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// ExistsAtLocation reports whether the product already has a replica at this location.
//
// A unique index cannot enforce this: path is too long for a PostgreSQL btree entry. A
// duplicate that slips through a race is harmless, as both rows name the same bytes.
func (r *DataProductReplicaRepository) ExistsAtLocation(ctx context.Context, productID, storageID string, storageType model.DataStorageType, path string) (bool, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.DataProductReplica{}).
		Where("data_product_id = ? AND data_storage_id = ? AND data_storage_type = ? AND path = ?",
			productID, storageID, storageType, path).
		Count(&n).Error
	return n > 0, err
}

// DeleteSecondaryAtLocation removes any non-primary replica at this location, for when
// the primary location moves onto it.
func (r *DataProductReplicaRepository) DeleteSecondaryAtLocation(ctx context.Context, productID, storageID string, storageType model.DataStorageType, path string) error {
	return r.db.WithContext(ctx).
		Where("data_product_id = ? AND data_storage_id = ? AND data_storage_type = ? AND path = ? AND NOT is_primary",
			productID, storageID, storageType, path).
		Delete(&model.DataProductReplica{}).Error
}

// DeletePrimary removes a product's primary replica, if it has one.
func (r *DataProductReplicaRepository) DeletePrimary(ctx context.Context, productID string) error {
	return r.db.WithContext(ctx).
		Where("data_product_id = ? AND is_primary", productID).
		Delete(&model.DataProductReplica{}).Error
}

// Save inserts or updates a replica.
func (r *DataProductReplicaRepository) Save(ctx context.Context, replica *model.DataProductReplica) error {
	return r.db.WithContext(ctx).Save(replica).Error
}

// Delete removes a replica.
func (r *DataProductReplicaRepository) Delete(ctx context.Context, replica *model.DataProductReplica) error {
	return r.db.WithContext(ctx).Delete(replica).Error
}
