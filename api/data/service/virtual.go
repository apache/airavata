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
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"

	dto "github.com/apache/airavata/api/data/dto"
	model "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// VirtualDataDirectoryService manages the directory nodes of virtual datasets.
//
// A dataset is a tree of references to already-registered products. Creating a node
// never moves or copies data: it records that a product appears at a place in a tree,
// which is why placing one needs only READ on the product itself.
type VirtualDataDirectoryService struct {
	virtualTree
	db    *gorm.DB
	users *iamrepo.UserRepository
}

// NewVirtualDataDirectoryService returns a virtual data directory service.
func NewVirtualDataDirectoryService(
	db *gorm.DB,
	directories *repository.VirtualDataDirectoryRepository,
	files *repository.VirtualDataFileRepository,
	sharing *repository.VirtualDataDirectorySharingRepository,
	products *repository.DataProductRepository,
	productSharing *repository.DataProductSharingRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *VirtualDataDirectoryService {
	return &VirtualDataDirectoryService{
		virtualTree: newVirtualTree(directories, files, sharing, products, productSharing, members),
		db:          db,
		users:       users,
	}
}

// List returns every directory node across every dataset. Admin only — it names who
// holds what data.
func (s *VirtualDataDirectoryService) List(ctx context.Context) ([]dto.VirtualDataDirectoryResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	dirs, err := s.directories.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToVirtualDataDirectoryResponses(dirs), nil
}

// ListMine returns the roots of the caller's own datasets.
//
// Roots only: every other node is reached by walking down from one of these, and
// listing them all would bury the datasets under their own contents.
func (s *VirtualDataDirectoryService) ListMine(ctx context.Context) ([]dto.VirtualDataDirectoryResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	dirs, err := s.directories.FindRootsByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}

	out := make([]dto.VirtualDataDirectoryResponse, 0, len(dirs))
	for i := range dirs {
		out = append(out, dto.ToVirtualDataDirectoryResponseWith(&dirs[i], string(model.AccessPermissionWrite)))
	}
	return out, nil
}

// ListSharedWithMe returns the directories other users have shared with the caller,
// directly or through a group, each carrying what it grants them.
//
// The nodes returned are the ones the shares name, which may be interior: a share
// opens a subtree, and its top is as far up as the grantee can see.
func (s *VirtualDataDirectoryService) ListSharedWithMe(ctx context.Context) ([]dto.VirtualDataDirectoryResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	dirs, err := s.directories.FindSharedWith(ctx, principal.Name)
	if err != nil {
		return nil, err
	}

	out := make([]dto.VirtualDataDirectoryResponse, 0, len(dirs))
	for i := range dirs {
		// Re-resolving per directory keeps the reported permission honest when a user
		// share and a group share reach the same node with different grants.
		held, _, err := s.permissionOf(ctx, &dirs[i])
		if err != nil {
			return nil, err
		}
		if held == model.AccessPermissionNone {
			continue
		}
		out = append(out, dto.ToVirtualDataDirectoryResponseWith(&dirs[i], string(held)))
	}
	return out, nil
}

// Get returns one directory without its contents, to anyone holding READ on it.
func (s *VirtualDataDirectoryService) Get(ctx context.Context, id string) (*dto.VirtualDataDirectoryResponse, error) {
	dir, err := s.requireDirectory(ctx, id)
	if err != nil {
		return nil, err
	}
	held, _, err := s.require(ctx, dir, model.AccessPermissionRead)
	if err != nil {
		return nil, err
	}
	out := dto.ToVirtualDataDirectoryResponseWith(dir, string(held))
	return &out, nil
}

// GetContents returns one directory with the files and directories directly under it.
//
// One level only. A dataset can be arbitrarily deep, and returning all of it would
// make the cost of a request a property of someone else's data rather than of the
// request; callers walk down a level at a time.
func (s *VirtualDataDirectoryService) GetContents(ctx context.Context, id string) (*dto.VirtualDataDirectoryResponse, error) {
	dir, err := s.requireDirectory(ctx, id)
	if err != nil {
		return nil, err
	}
	held, _, err := s.require(ctx, dir, model.AccessPermissionRead)
	if err != nil {
		return nil, err
	}

	out := dto.ToVirtualDataDirectoryResponseWith(dir, string(held))

	// A product-backed directory stands for a registered directory; its contents are
	// whatever is on disk, which these tables do not describe.
	if productBacked(dir) {
		return &out, nil
	}

	files, err := s.files.FindByParentID(ctx, dir.ID)
	if err != nil {
		return nil, err
	}
	children, err := s.directories.FindChildDirectories(ctx, dir.ID)
	if err != nil {
		return nil, err
	}

	// Everything below inherits this node's permission, so it is reported rather than
	// re-resolved per entry: access is granted on subtrees, and nothing under a node
	// can grant less than the node itself.
	out.Files = dto.ToVirtualDataFileResponsesWith(files, string(held))
	out.Directories = make([]dto.VirtualDataDirectoryResponse, 0, len(children))
	for i := range children {
		out.Directories = append(out.Directories,
			dto.ToVirtualDataDirectoryResponseWith(&children[i], string(held)))
	}
	return &out, nil
}

// Create adds a directory node.
//
// With no parent it is a new dataset, owned by the caller. With one it is placed
// inside an existing dataset, which needs WRITE there, and it inherits that dataset's
// owner rather than belonging to whoever added it — a grantee building out someone
// else's dataset does not come to own a piece of it.
func (s *VirtualDataDirectoryService) Create(ctx context.Context, req *dto.VirtualDataDirectoryRequest) (*dto.VirtualDataDirectoryResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.VirtualDataDirectoryResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		tree := s.virtualTree.withTx(tx)

		owner, err := s.users.WithTx(tx).FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}

		ownerID := owner.ID
		if req.ParentDirectoryID != nil {
			parent, err := tree.requireWritableParent(ctx, *req.ParentDirectoryID)
			if err != nil {
				return err
			}
			if parent.OwnerID != nil {
				ownerID = *parent.OwnerID
			}
		}
		if err := tree.requireNameFree(ctx, req.ParentDirectoryID, ownerID, req.Name(), ""); err != nil {
			return err
		}
		if err := tree.resolveProduct(ctx, req.DataProductID, false); err != nil {
			return err
		}

		dir := &model.VirtualDataDirectory{
			OwnerID:   &ownerID,
			CreatedAt: time.Now().UnixMilli(),
		}
		dto.ApplyVirtualDataDirectoryRequest(dir, req)
		if err := tree.directories.Save(ctx, dir); err != nil {
			return err
		}
		out = dto.ToVirtualDataDirectoryResponseWith(dir, string(model.AccessPermissionWrite))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update renames a directory, re-points the product it stands for, or moves it.
//
// It needs WRITE on the node. Moving it needs WRITE on the destination too, and is
// refused if the destination sits inside the subtree being moved. The owner and the
// creation time are left alone: re-deriving the owner from the caller's token would
// hand the dataset to whichever grantee happened to issue the request.
func (s *VirtualDataDirectoryService) Update(ctx context.Context, id string, req *dto.VirtualDataDirectoryRequest) (*dto.VirtualDataDirectoryResponse, error) {
	var out dto.VirtualDataDirectoryResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		tree := s.virtualTree.withTx(tx)

		dir, err := tree.directories.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Virtual data directory not found: %s", id)
		}
		held, _, err := tree.require(ctx, dir, model.AccessPermissionWrite)
		if err != nil {
			return err
		}

		ownerID := ""
		if dir.OwnerID != nil {
			ownerID = *dir.OwnerID
		}
		if movedParent(dir.ParentDirectoryID, req.ParentDirectoryID) && req.ParentDirectoryID != nil {
			parent, err := tree.requireWritableParent(ctx, *req.ParentDirectoryID)
			if err != nil {
				return err
			}
			if err := tree.requireNotDescendant(ctx, dir.ID, parent); err != nil {
				return err
			}
		}
		if err := tree.requireNameFree(ctx, req.ParentDirectoryID, ownerID, req.Name(), dir.ID); err != nil {
			return err
		}
		if err := tree.resolveProduct(ctx, req.DataProductID, false); err != nil {
			return err
		}
		// Standing for a product and holding entries are alternatives. A node that
		// already has contents cannot be turned into a reference without those
		// contents becoming unreachable.
		if req.DataProductID != nil && !productBacked(dir) {
			if err := tree.requireEmpty(ctx, dir.ID); err != nil {
				return err
			}
		}

		dto.ApplyVirtualDataDirectoryRequest(dir, req)
		if err := tree.directories.Save(ctx, dir); err != nil {
			return err
		}
		out = dto.ToVirtualDataDirectoryResponseWith(dir, string(held))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a directory and everything under it, for an owner in its lineage or
// an admin.
//
// Control rather than WRITE: the subtree can hold nodes other people were granted
// access to, and withdrawing that is the owner's decision. The entries below and their
// shares go through the cascading foreign keys.
func (s *VirtualDataDirectoryService) Delete(ctx context.Context, id string) error {
	dir, err := s.requireDirectory(ctx, id)
	if err != nil {
		return err
	}
	if err := s.requireControl(ctx, dir); err != nil {
		return err
	}
	return s.directories.Delete(ctx, dir)
}
