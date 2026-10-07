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

type VirtualDataDirectoryRepository struct{ db *gorm.DB }

func NewVirtualDataDirectoryRepository(db *gorm.DB) *VirtualDataDirectoryRepository {
	return &VirtualDataDirectoryRepository{db: db}
}

func (r *VirtualDataDirectoryRepository) WithTx(tx *gorm.DB) *VirtualDataDirectoryRepository {
	return &VirtualDataDirectoryRepository{db: tx}
}

func (r *VirtualDataDirectoryRepository) FindAll(ctx context.Context) ([]model.VirtualDataDirectory, error) {
	var out []model.VirtualDataDirectory
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

func (r *VirtualDataDirectoryRepository) FindByID(ctx context.Context, id string) (*model.VirtualDataDirectory, error) {
	var out model.VirtualDataDirectory
	if err := r.db.WithContext(ctx).First(&out, "virtual_data_directory_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

func (r *VirtualDataDirectoryRepository) FindRootsByOwnerID(ctx context.Context, userID string) ([]model.VirtualDataDirectory, error) {
	var out []model.VirtualDataDirectory
	err := r.db.WithContext(ctx).
		Where("user_id = ? AND parent_directory_id IS NULL", userID).
		Find(&out).Error
	return out, err
}

func (r *VirtualDataDirectoryRepository) FindChildDirectories(ctx context.Context, parentID string) ([]model.VirtualDataDirectory, error) {
	var out []model.VirtualDataDirectory
	err := r.db.WithContext(ctx).Where("parent_directory_id = ?", parentID).Find(&out).Error
	return out, err
}

func (r *VirtualDataDirectoryRepository) FindByNameUnderParent(ctx context.Context, parentID *string, ownerID, name string) (*model.VirtualDataDirectory, error) {
	q := r.db.WithContext(ctx).Where("directory_name = ?", name)
	if parentID == nil {
		q = q.Where("parent_directory_id IS NULL AND user_id = ?", ownerID)
	} else {
		q = q.Where("parent_directory_id = ?", *parentID)
	}

	var out model.VirtualDataDirectory
	if err := q.First(&out).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByIDsExcludingOwner returns the directorys named by ids that userID does not own.
//
// The ids come from the sharing table, which no longer knows what kind of record it is
// opening up beyond its type tag; this turns them back into entities. Directories the
// caller owns are dropped, because ownership is not a share and the caller asking
// "what has been shared with me?" already has /me for their own.
func (r *VirtualDataDirectoryRepository) FindByIDsExcludingOwner(ctx context.Context, ids []string, userID string) ([]model.VirtualDataDirectory, error) {
	if len(ids) == 0 {
		return nil, nil
	}
	var out []model.VirtualDataDirectory
	err := r.db.WithContext(ctx).
		Where("virtual_data_directory_id IN ? AND (user_id IS NULL OR user_id <> ?)", ids, userID).
		Find(&out).Error
	return out, err
}

// CountByDataProductID reports how many directory nodes cite one product. It is what
// makes deleting a product still used by a dataset reportable as a conflict.
func (r *VirtualDataDirectoryRepository) CountByDataProductID(ctx context.Context, productID string) (int64, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.VirtualDataDirectory{}).
		Where("data_product_id = ?", productID).Count(&n).Error
	return n, err
}

func (r *VirtualDataDirectoryRepository) Save(ctx context.Context, d *model.VirtualDataDirectory) error {
	return r.db.WithContext(ctx).Save(d).Error
}

func (r *VirtualDataDirectoryRepository) Delete(ctx context.Context, d *model.VirtualDataDirectory) error {
	return r.db.WithContext(ctx).Delete(d).Error
}

type VirtualDataFileRepository struct{ db *gorm.DB }

func NewVirtualDataFileRepository(db *gorm.DB) *VirtualDataFileRepository {
	return &VirtualDataFileRepository{db: db}
}

func (r *VirtualDataFileRepository) WithTx(tx *gorm.DB) *VirtualDataFileRepository {
	return &VirtualDataFileRepository{db: tx}
}

func (r *VirtualDataFileRepository) FindByID(ctx context.Context, id string) (*model.VirtualDataFile, error) {
	var out model.VirtualDataFile
	if err := r.db.WithContext(ctx).First(&out, "virtual_data_file_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

func (r *VirtualDataFileRepository) FindByParentID(ctx context.Context, parentID string) ([]model.VirtualDataFile, error) {
	var out []model.VirtualDataFile
	err := r.db.WithContext(ctx).Where("parent_directory_id = ?", parentID).Find(&out).Error
	return out, err
}

func (r *VirtualDataFileRepository) FindByNameUnderParent(ctx context.Context, parentID, name string) (*model.VirtualDataFile, error) {
	var out model.VirtualDataFile
	err := r.db.WithContext(ctx).First(&out,
		"parent_directory_id = ? AND file_name = ?", parentID, name).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

func (r *VirtualDataFileRepository) CountByDataProductID(ctx context.Context, productID string) (int64, error) {
	var n int64
	err := r.db.WithContext(ctx).Model(&model.VirtualDataFile{}).
		Where("data_product_id = ?", productID).Count(&n).Error
	return n, err
}

func (r *VirtualDataFileRepository) Save(ctx context.Context, f *model.VirtualDataFile) error {
	return r.db.WithContext(ctx).Save(f).Error
}

func (r *VirtualDataFileRepository) Delete(ctx context.Context, f *model.VirtualDataFile) error {
	return r.db.WithContext(ctx).Delete(f).Error
}

// FindSubtreeIDs returns id and the ids of every directory beneath it, breadth first.
//
// Deleting a directory cascades the rows under it through the self-referencing foreign
// key, but the shares hanging off those rows point at nothing and so are not cascaded
// with them. The caller deletes them by id, which is what this is for.
//
// maxDepth bounds the walk: the schema cannot express "no cycles", and a cycle would
// otherwise make this loop forever. Nodes already seen are not revisited, so a cycle
// yields a finite set rather than a wrong one.
func (r *VirtualDataDirectoryRepository) FindSubtreeIDs(ctx context.Context, id string, maxDepth int) ([]string, error) {
	all := []string{id}
	seen := map[string]bool{id: true}

	frontier := []string{id}
	for depth := 0; depth < maxDepth && len(frontier) > 0; depth++ {
		var next []string
		if err := r.db.WithContext(ctx).Model(&model.VirtualDataDirectory{}).
			Where("parent_directory_id IN ?", frontier).
			Pluck("virtual_data_directory_id", &next).Error; err != nil {
			return nil, err
		}
		frontier = frontier[:0]
		for _, child := range next {
			if seen[child] {
				continue
			}
			seen[child] = true
			all = append(all, child)
			frontier = append(frontier, child)
		}
	}
	return all, nil
}
