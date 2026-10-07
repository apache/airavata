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

// Package repository reads and writes the sharing table every vertical grants through.
package repository

import (
	"context"

	"gorm.io/gorm"

	iammodel "github.com/apache/airavata/api/iam/model"
	model "github.com/apache/airavata/api/sharing/model"
)

// Repository reads and writes every share in the platform.
//
// One repository for every resource kind, because the rows differ only in the
// resource type they name. Every method is scoped by that type: an id is unique within
// its own kind, not across them, so a lookup that omitted the type could return a
// product's share for a storage's id.
type Repository struct{ db *gorm.DB }

// NewRepository returns a repository backed by db.
func NewRepository(db *gorm.DB) *Repository {
	return &Repository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *Repository) WithTx(tx *gorm.DB) *Repository {
	return &Repository{db: tx}
}

// FindByResource returns every share of one resource, of either principal kind.
//
// Resolving access needs them together, which is the whole point of the single table:
// what used to be two queries against two tables is one.
func (r *Repository) FindByResource(ctx context.Context, resourceType model.ResourceType, resourceID string) ([]model.Sharing, error) {
	var out []model.Sharing
	err := r.db.WithContext(ctx).
		Where("resource_type = ? AND resource_id = ?", resourceType, resourceID).
		Find(&out).Error
	return out, err
}

// FindByResources returns every share of any of resourceIDs.
//
// A virtual directory inherits what its ancestors grant, so resolving one node asks
// about the whole chain at once rather than once per level.
func (r *Repository) FindByResources(ctx context.Context, resourceType model.ResourceType, resourceIDs []string) ([]model.Sharing, error) {
	if len(resourceIDs) == 0 {
		return nil, nil
	}
	var out []model.Sharing
	err := r.db.WithContext(ctx).
		Where("resource_type = ? AND resource_id IN ?", resourceType, resourceIDs).
		Find(&out).Error
	return out, err
}

// FindByID returns one share scoped to its resource, or gorm.ErrRecordNotFound.
//
// Scoping is deliberate: it stops a sharing id belonging to one resource from being
// reached through another resource's path.
func (r *Repository) FindByID(ctx context.Context, resourceType model.ResourceType, resourceID, sharingID string) (*model.Sharing, error) {
	var out model.Sharing
	err := r.db.WithContext(ctx).First(&out,
		"resource_sharing_id = ? AND resource_type = ? AND resource_id = ?",
		sharingID, resourceType, resourceID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// FindByPrincipal returns the share of one resource with one principal, or
// gorm.ErrRecordNotFound. It is what makes a duplicate share reportable as a conflict.
func (r *Repository) FindByPrincipal(ctx context.Context, resourceType model.ResourceType, resourceID string, principalType model.PrincipalType, principalID string) (*model.Sharing, error) {
	var out model.Sharing
	err := r.db.WithContext(ctx).First(&out,
		"resource_type = ? AND resource_id = ? AND principal_type = ? AND principal_id = ?",
		resourceType, resourceID, principalType, principalID).Error
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// ResourceIDsSharedWith returns the ids of one kind of resource reaching userID
// through a share: named directly, or through a group they are an active member of.
//
// It replaces the pair of correlated subqueries each resource used to build against
// its own two tables. Callers filter out what the user already owns; a share is not
// how an owner reaches their own record.
func (r *Repository) ResourceIDsSharedWith(ctx context.Context, resourceType model.ResourceType, userID string) ([]string, error) {
	activeGroups := r.db.Model(&iammodel.GroupMember{}).
		Select("group_id").
		Where("user_id = ? AND group_member_status = ?", userID, iammodel.GroupMemberStatusActive)

	var out []string
	err := r.db.WithContext(ctx).Model(&model.Sharing{}).
		Where("resource_type = ?", resourceType).
		Where(
			r.db.Where("principal_type = ? AND principal_id = ?", model.PrincipalTypeUser, userID).
				Or("principal_type = ? AND principal_id IN (?)", model.PrincipalTypeGroup, activeGroups),
		).
		Distinct().
		Pluck("resource_id", &out).Error
	return out, err
}

// Save inserts or updates a share.
func (r *Repository) Save(ctx context.Context, s *model.Sharing) error {
	return r.db.WithContext(ctx).Save(s).Error
}

// Delete removes a share.
func (r *Repository) Delete(ctx context.Context, s *model.Sharing) error {
	return r.db.WithContext(ctx).Delete(s).Error
}

// DeleteByResources removes every share of any of resourceIDs.
//
// No foreign key points at the resource, so nothing in the database removes these when
// the record they open up goes away: whatever deletes a resource calls this in the same
// transaction. For a virtual directory that means the whole subtree, since the
// directory rows below it are cascaded away by the database without their shares.
func (r *Repository) DeleteByResources(ctx context.Context, resourceType model.ResourceType, resourceIDs []string) error {
	if len(resourceIDs) == 0 {
		return nil
	}
	return r.db.WithContext(ctx).
		Where("resource_type = ? AND resource_id IN ?", resourceType, resourceIDs).
		Delete(&model.Sharing{}).Error
}

// DeleteByPrincipal removes every share granted to one principal, across every
// resource kind.
//
// It is what keeps a deleted group from leaving rows behind. Those rows would grant
// nothing — memberships are cascaded away with the group, so nobody resolves as an
// active member of it — but they would accumulate, and a listing of a resource's
// shares would name a group that no longer exists.
func (r *Repository) DeleteByPrincipal(ctx context.Context, principalType model.PrincipalType, principalID string) error {
	return r.db.WithContext(ctx).
		Where("principal_type = ? AND principal_id = ?", principalType, principalID).
		Delete(&model.Sharing{}).Error
}
