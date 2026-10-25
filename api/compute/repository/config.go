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

	model "github.com/apache/airavata/api/compute/model"
)

// SlurmClusterConfigRepository reads and writes cluster login configs.
//
// Reads preload the cluster and the key summary because the response DTO carries both:
// a config without the machine it logs in to, and without which key it presents, is not
// much use to a caller deciding whether to launch through it.
type SlurmClusterConfigRepository struct{ db *gorm.DB }

// NewSlurmClusterConfigRepository returns a repository backed by db.
func NewSlurmClusterConfigRepository(db *gorm.DB) *SlurmClusterConfigRepository {
	return &SlurmClusterConfigRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *SlurmClusterConfigRepository) WithTx(tx *gorm.DB) *SlurmClusterConfigRepository {
	return &SlurmClusterConfigRepository{db: tx}
}

// withReferences is the read scope every lookup that feeds a response DTO starts from.
func (r *SlurmClusterConfigRepository) withReferences(ctx context.Context) *gorm.DB {
	return r.db.WithContext(ctx).
		Preload("SlurmCluster").
		Preload("SlurmCluster.Partitions").
		Preload("SSHKey")
}

// FindAll returns every config.
func (r *SlurmClusterConfigRepository) FindAll(ctx context.Context) ([]model.SlurmClusterConfig, error) {
	var out []model.SlurmClusterConfig
	err := r.withReferences(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one config, or gorm.ErrRecordNotFound.
func (r *SlurmClusterConfigRepository) FindByID(ctx context.Context, id string) (*model.SlurmClusterConfig, error) {
	var out model.SlurmClusterConfig
	if err := r.withReferences(ctx).First(&out, "slurm_cluster_config_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByOwnerID returns every config owned by one user.
func (r *SlurmClusterConfigRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.SlurmClusterConfig, error) {
	var out []model.SlurmClusterConfig
	err := r.withReferences(ctx).Where("user_id = ?", userID).Find(&out).Error
	return out, err
}

// FindBySlurmClusterID returns every config registered against one cluster. It is what
// makes deleting a cluster still in use reportable as a conflict rather than a foreign
// key error.
func (r *SlurmClusterConfigRepository) FindBySlurmClusterID(ctx context.Context, clusterID string) ([]model.SlurmClusterConfig, error) {
	var out []model.SlurmClusterConfig
	err := r.db.WithContext(ctx).Where("slurm_cluster_id = ?", clusterID).Find(&out).Error
	return out, err
}

// FindByIDsExcludingOwner returns the configs named by ids that userID does not own.
//
// The ids come from the sharing table, which knows a resource only by its type tag;
// this turns them back into entities. Configs the caller owns are dropped, because
// ownership is not a share and the caller asking "what has been shared with me?"
// already has /me for their own.
func (r *SlurmClusterConfigRepository) FindByIDsExcludingOwner(ctx context.Context, ids []string, userID string) ([]model.SlurmClusterConfig, error) {
	if len(ids) == 0 {
		return nil, nil
	}
	var out []model.SlurmClusterConfig
	err := r.db.WithContext(ctx).
		Where("slurm_cluster_config_id IN ? AND (user_id IS NULL OR user_id <> ?)", ids, userID).
		Find(&out).Error
	return out, err
}

// CountByKeyID reports how many configs still present one SSH key. The credentials
// vertical asks before deleting a key: the foreign key is RESTRICT, so without this the
// delete would fail as an opaque constraint violation rather than a 409 naming what
// still holds it.
func (r *SlurmClusterConfigRepository) CountByKeyID(ctx context.Context, keyID string) (int64, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.SlurmClusterConfig{}).
		Where("ssh_key_id = ?", keyID).Count(&n).Error
	return n, err
}

// Save inserts or updates a config.
func (r *SlurmClusterConfigRepository) Save(ctx context.Context, c *model.SlurmClusterConfig) error {
	return r.db.WithContext(ctx).Save(c).Error
}

// Delete removes a config.
func (r *SlurmClusterConfigRepository) Delete(ctx context.Context, c *model.SlurmClusterConfig) error {
	return r.db.WithContext(ctx).Delete(c).Error
}
