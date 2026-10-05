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

package service

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/data/dto"
	model "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// VirtualDataDirectorySharingService manages who, besides the owner, may reach a
// dataset.
//
// A share names a directory and opens the subtree under it, so sharing an interior
// node hands out part of a dataset without handing out the whole of it. Only an owner
// in the node's lineage (or a platform admin) may read or change the share list: it
// names who holds a dataset, which is more than a grantee needs to know.
type VirtualDataDirectorySharingService struct {
	directoryAccess
	db     *gorm.DB
	groups *iamrepo.GroupRepository
	users  *iamrepo.UserRepository
}

// NewVirtualDataDirectorySharingService returns a directory sharing service.
func NewVirtualDataDirectorySharingService(
	db *gorm.DB,
	directories *repository.VirtualDataDirectoryRepository,
	sharing *repository.VirtualDataDirectorySharingRepository,
	groups *iamrepo.GroupRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *VirtualDataDirectorySharingService {
	return &VirtualDataDirectorySharingService{
		directoryAccess: directoryAccess{
			access:      access{members: members},
			directories: directories,
			sharing:     sharing,
		},
		db:     db,
		groups: groups,
		users:  users,
	}
}

// ListGroupShares returns every group a directory is shared with.
//
// Shares inherited from an ancestor are not included: this is the list of grants made
// at this node, which is what the caller can edit here.
func (s *VirtualDataDirectorySharingService) ListGroupShares(ctx context.Context, directoryID string) ([]dto.VirtualDataDirectoryGroupSharingResponse, error) {
	dir, err := s.requireControlledDirectory(ctx, directoryID)
	if err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindGroupSharesByDirectoryID(ctx, dir.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToVirtualDataDirectoryGroupSharingResponses(shares), nil
}

// ShareWithGroup grants a group access to a directory and the subtree below it.
func (s *VirtualDataDirectorySharingService) ShareWithGroup(ctx context.Context, directoryID string, req *dto.VirtualDataDirectoryGroupSharingRequest) (*dto.VirtualDataDirectoryGroupSharingResponse, error) {
	dir, err := s.requireControlledDirectory(ctx, directoryID)
	if err != nil {
		return nil, err
	}

	var out dto.VirtualDataDirectoryGroupSharingResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing, groups := s.sharing.WithTx(tx), s.groups.WithTx(tx)

		if _, err := groups.FindByID(ctx, req.GroupID); err != nil {
			return notFoundAs(err, "Group not found: %s", req.GroupID)
		}
		if _, err := sharing.FindGroupShareByGroupID(ctx, dir.ID, req.GroupID); err == nil {
			return httpx.Conflict("Virtual data directory %s is already shared with group %s", dir.ID, req.GroupID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		permission := req.Grant()
		share := &model.VirtualDataDirectoryGroupSharing{
			VirtualDataDirectoryID: &dir.ID,
			GroupID:                &req.GroupID,
			Permission:             &permission,
		}
		if err := sharing.SaveGroupShare(ctx, share); err != nil {
			return err
		}
		out = dto.ToVirtualDataDirectoryGroupSharingResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateGroupShare changes what a group share grants.
func (s *VirtualDataDirectorySharingService) UpdateGroupShare(ctx context.Context, directoryID, sharingID string, req *dto.DataProductSharingUpdate) (*dto.VirtualDataDirectoryGroupSharingResponse, error) {
	dir, err := s.requireControlledDirectory(ctx, directoryID)
	if err != nil {
		return nil, err
	}
	share, err := s.sharing.FindGroupShare(ctx, dir.ID, sharingID)
	if err != nil {
		return nil, notFoundAs(err, "Group sharing not found: %s on virtual data directory %s", sharingID, dir.ID)
	}

	share.Permission = req.Permission
	if err := s.sharing.SaveGroupShare(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToVirtualDataDirectoryGroupSharingResponse(share)
	return &out, nil
}

// RevokeGroupShare withdraws a group's access.
func (s *VirtualDataDirectorySharingService) RevokeGroupShare(ctx context.Context, directoryID, sharingID string) error {
	dir, err := s.requireControlledDirectory(ctx, directoryID)
	if err != nil {
		return err
	}
	share, err := s.sharing.FindGroupShare(ctx, dir.ID, sharingID)
	if err != nil {
		return notFoundAs(err, "Group sharing not found: %s on virtual data directory %s", sharingID, dir.ID)
	}
	return s.sharing.DeleteGroupShare(ctx, share)
}

// ListUserShares returns every user a directory is shared with.
func (s *VirtualDataDirectorySharingService) ListUserShares(ctx context.Context, directoryID string) ([]dto.VirtualDataDirectoryUserSharingResponse, error) {
	dir, err := s.requireControlledDirectory(ctx, directoryID)
	if err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindUserSharesByDirectoryID(ctx, dir.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToVirtualDataDirectoryUserSharingResponses(shares), nil
}

// ShareWithUser grants one user access to a directory and the subtree below it.
//
// Sharing with someone who already owns this node or an ancestor is refused rather
// than stored: it would grant nothing they do not already have.
func (s *VirtualDataDirectorySharingService) ShareWithUser(ctx context.Context, directoryID string, req *dto.VirtualDataDirectoryUserSharingRequest) (*dto.VirtualDataDirectoryUserSharingResponse, error) {
	dir, err := s.requireControlledDirectory(ctx, directoryID)
	if err != nil {
		return nil, err
	}
	lineage, err := s.lineageOf(ctx, dir)
	if err != nil {
		return nil, err
	}
	for i := range lineage {
		if ownsDirectory(&lineage[i], req.UserID) {
			return nil, httpx.Conflict("User %s already owns virtual data directory %s", req.UserID, lineage[i].ID)
		}
	}

	var out dto.VirtualDataDirectoryUserSharingResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing, users := s.sharing.WithTx(tx), s.users.WithTx(tx)

		if _, err := users.FindByID(ctx, req.UserID); err != nil {
			return notFoundAs(err, "User not found with ID: %s", req.UserID)
		}
		if _, err := sharing.FindUserShareByUserID(ctx, dir.ID, req.UserID); err == nil {
			return httpx.Conflict("Virtual data directory %s is already shared with user %s", dir.ID, req.UserID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		permission := req.Grant()
		share := &model.VirtualDataDirectoryUserSharing{
			VirtualDataDirectoryID: &dir.ID,
			UserID:                 &req.UserID,
			Permission:             &permission,
		}
		if err := sharing.SaveUserShare(ctx, share); err != nil {
			return err
		}
		out = dto.ToVirtualDataDirectoryUserSharingResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// UpdateUserShare changes what a user share grants.
func (s *VirtualDataDirectorySharingService) UpdateUserShare(ctx context.Context, directoryID, sharingID string, req *dto.DataProductSharingUpdate) (*dto.VirtualDataDirectoryUserSharingResponse, error) {
	dir, err := s.requireControlledDirectory(ctx, directoryID)
	if err != nil {
		return nil, err
	}
	share, err := s.sharing.FindUserShare(ctx, dir.ID, sharingID)
	if err != nil {
		return nil, notFoundAs(err, "User sharing not found: %s on virtual data directory %s", sharingID, dir.ID)
	}

	share.Permission = req.Permission
	if err := s.sharing.SaveUserShare(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToVirtualDataDirectoryUserSharingResponse(share)
	return &out, nil
}

// RevokeUserShare withdraws a user's access.
func (s *VirtualDataDirectorySharingService) RevokeUserShare(ctx context.Context, directoryID, sharingID string) error {
	dir, err := s.requireControlledDirectory(ctx, directoryID)
	if err != nil {
		return err
	}
	share, err := s.sharing.FindUserShare(ctx, dir.ID, sharingID)
	if err != nil {
		return notFoundAs(err, "User sharing not found: %s on virtual data directory %s", sharingID, dir.ID)
	}
	return s.sharing.DeleteUserShare(ctx, share)
}

func (s *VirtualDataDirectorySharingService) requireControlledDirectory(ctx context.Context, directoryID string) (*model.VirtualDataDirectory, error) {
	dir, err := s.requireDirectory(ctx, directoryID)
	if err != nil {
		return nil, err
	}
	if err := s.requireControl(ctx, dir); err != nil {
		return nil, err
	}
	return dir, nil
}
