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
	iammodel "github.com/apache/airavata/api/iam/model"
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

func (r *VirtualDataDirectoryRepository) FindSharedWith(ctx context.Context, userID string) ([]model.VirtualDataDirectory, error) {
	sharedDirectly := r.db.Model(&model.VirtualDataDirectoryUserSharing{}).
		Select("virtual_data_directory_id").
		Where("user_id = ?", userID)

	sharedByGroup := r.db.Model(&model.VirtualDataDirectoryGroupSharing{}).
		Select("virtual_data_directory_id").
		Where("group_id IN (?)", r.db.Model(&iammodel.GroupMember{}).
			Select("group_id").
			Where("user_id = ? AND group_member_status = ?", userID, iammodel.GroupMemberStatusActive))

	var out []model.VirtualDataDirectory
	err := r.db.WithContext(ctx).
		Where("(virtual_data_directory_id IN (?) OR virtual_data_directory_id IN (?)) AND (user_id IS NULL OR user_id <> ?)",
			sharedDirectly, sharedByGroup, userID).
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

type VirtualDataDirectorySharingRepository struct{ db *gorm.DB }

func NewVirtualDataDirectorySharingRepository(db *gorm.DB) *VirtualDataDirectorySharingRepository {
	return &VirtualDataDirectorySharingRepository{db: db}
}

func (r *VirtualDataDirectorySharingRepository) WithTx(tx *gorm.DB) *VirtualDataDirectorySharingRepository {
	return &VirtualDataDirectorySharingRepository{db: tx}
}

// FindGroupSharesByDirectoryID returns every group share of one directory.
func (r *VirtualDataDirectorySharingRepository) FindGroupSharesByDirectoryID(ctx context.Context, directoryID string) ([]model.VirtualDataDirectoryGroupSharing, error) {
	var out []model.VirtualDataDirectoryGroupSharing
	err := r.db.WithContext(ctx).Where("virtual_data_directory_id = ?", directoryID).Find(&out).Error
	return out, err
}

// FindGroupSharesByDirectoryIDs returns every group share of any of directoryIDs.
//
// Resolving access to a node means asking about it and every ancestor at once, so the
// whole chain is fetched in one query rather than one per level.
func (r *VirtualDataDirectorySharingRepository) FindGroupSharesByDirectoryIDs(ctx context.Context, directoryIDs []string) ([]model.VirtualDataDirectoryGroupSharing, error) {
	if len(directoryIDs) == 0 {
		return nil, nil
	}
	var out []model.VirtualDataDirectoryGroupSharing
	err := r.db.WithContext(ctx).Where("virtual_data_directory_id IN ?", directoryIDs).Find(&out).Error
	return out, err
}

// FindGroupShare returns one group share scoped to its directory, or
// gorm.ErrRecordNotFound. Scoping is deliberate, for the reason the product sharing
// repository gives.
func (r *VirtualDataDirectorySharingRepository) FindGroupShare(ctx context.Context, directoryID, sharingID string) (*model.VirtualDataDirectoryGroupSharing, error) {
	var out model.VirtualDataDirectoryGroupSharing
	err := r.db.WithContext(ctx).First(&out,
		"virtual_data_directory_group_sharing_id = ? AND virtual_data_directory_id = ?", sharingID, directoryID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindGroupShareByGroupID returns the share of one directory with one group, or
// gorm.ErrRecordNotFound.
func (r *VirtualDataDirectorySharingRepository) FindGroupShareByGroupID(ctx context.Context, directoryID, groupID string) (*model.VirtualDataDirectoryGroupSharing, error) {
	var out model.VirtualDataDirectoryGroupSharing
	err := r.db.WithContext(ctx).First(&out,
		"virtual_data_directory_id = ? AND group_id = ?", directoryID, groupID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveGroupShare inserts or updates a group share.
func (r *VirtualDataDirectorySharingRepository) SaveGroupShare(ctx context.Context, s *model.VirtualDataDirectoryGroupSharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// DeleteGroupShare removes a group share.
func (r *VirtualDataDirectorySharingRepository) DeleteGroupShare(ctx context.Context, s *model.VirtualDataDirectoryGroupSharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// FindUserSharesByDirectoryID returns every user share of one directory.
func (r *VirtualDataDirectorySharingRepository) FindUserSharesByDirectoryID(ctx context.Context, directoryID string) ([]model.VirtualDataDirectoryUserSharing, error) {
	var out []model.VirtualDataDirectoryUserSharing
	err := r.db.WithContext(ctx).Where("virtual_data_directory_id = ?", directoryID).Find(&out).Error
	return out, err
}

// FindUserSharesByDirectoryIDs returns every user share of any of directoryIDs.
func (r *VirtualDataDirectorySharingRepository) FindUserSharesByDirectoryIDs(ctx context.Context, directoryIDs []string) ([]model.VirtualDataDirectoryUserSharing, error) {
	if len(directoryIDs) == 0 {
		return nil, nil
	}
	var out []model.VirtualDataDirectoryUserSharing
	err := r.db.WithContext(ctx).Where("virtual_data_directory_id IN ?", directoryIDs).Find(&out).Error
	return out, err
}

// FindUserShare returns one user share scoped to its directory, or
// gorm.ErrRecordNotFound.
func (r *VirtualDataDirectorySharingRepository) FindUserShare(ctx context.Context, directoryID, sharingID string) (*model.VirtualDataDirectoryUserSharing, error) {
	var out model.VirtualDataDirectoryUserSharing
	err := r.db.WithContext(ctx).First(&out,
		"virtual_data_directory_user_sharing_id = ? AND virtual_data_directory_id = ?", sharingID, directoryID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindUserShareByUserID returns the share of one directory with one user, or
// gorm.ErrRecordNotFound.
func (r *VirtualDataDirectorySharingRepository) FindUserShareByUserID(ctx context.Context, directoryID, userID string) (*model.VirtualDataDirectoryUserSharing, error) {
	var out model.VirtualDataDirectoryUserSharing
	err := r.db.WithContext(ctx).First(&out,
		"virtual_data_directory_id = ? AND user_id = ?", directoryID, userID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// SaveUserShare inserts or updates a user share.
func (r *VirtualDataDirectorySharingRepository) SaveUserShare(ctx context.Context, s *model.VirtualDataDirectoryUserSharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// DeleteUserShare removes a user share.
func (r *VirtualDataDirectorySharingRepository) DeleteUserShare(ctx context.Context, s *model.VirtualDataDirectoryUserSharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// DeleteByDirectoryIDs removes every share of any of directoryIDs.
//
// Deleting a directory takes its subtree with it through the cascade, but the shares
// hanging off those nodes cascade too, so this exists for the one case the cascade
// does not cover: revoking a whole subtree's shares without deleting it.
func (r *VirtualDataDirectorySharingRepository) DeleteByDirectoryIDs(ctx context.Context, directoryIDs []string) error {
	if len(directoryIDs) == 0 {
		return nil
	}
	if err := r.db.WithContext(ctx).Where("virtual_data_directory_id IN ?", directoryIDs).
		Delete(&model.VirtualDataDirectoryGroupSharing{}).Error; err != nil {
		return err
	}
	return r.db.WithContext(ctx).Where("virtual_data_directory_id IN ?", directoryIDs).
		Delete(&model.VirtualDataDirectoryUserSharing{}).Error
}
