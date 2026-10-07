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

	dto "github.com/apache/airavata/api/data/dto"
	model "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	sharingmodel "github.com/apache/airavata/api/sharing/model"
	sharingrepo "github.com/apache/airavata/api/sharing/repository"
)

// VirtualDataFileService manages the leaves of virtual datasets.
//
// A file carries no owner and no shares of its own: it is reached through the
// directory it sits under, and everything about who may touch it is decided there.
type VirtualDataFileService struct {
	virtualTree
	db *gorm.DB
}

func NewVirtualDataFileService(
	db *gorm.DB,
	directories *repository.VirtualDataDirectoryRepository,
	files *repository.VirtualDataFileRepository,
	sharing *sharingrepo.Repository,
	products *repository.DataProductRepository,
	members *iamrepo.GroupMemberRepository,
) *VirtualDataFileService {
	return &VirtualDataFileService{
		virtualTree: newVirtualTree(directories, files, sharing, products, members),
		db:          db,
	}
}

// ListByDirectory returns the files directly under one directory, to anyone holding
// READ on it.
func (s *VirtualDataFileService) ListByDirectory(ctx context.Context, directoryID string) ([]dto.VirtualDataFileResponse, error) {
	dir, err := s.requireDirectory(ctx, directoryID)
	if err != nil {
		return nil, err
	}
	held, _, err := s.require(ctx, dir, sharingmodel.AccessPermissionRead)
	if err != nil {
		return nil, err
	}

	files, err := s.files.FindByParentID(ctx, dir.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToVirtualDataFileResponsesWith(files, string(held)), nil
}

// Get returns one file, to anyone holding READ on the directory it sits under.
func (s *VirtualDataFileService) Get(ctx context.Context, id string) (*dto.VirtualDataFileResponse, error) {
	file, err := s.requireFile(ctx, id)
	if err != nil {
		return nil, err
	}
	held, _, err := s.requireOnParent(ctx, file, sharingmodel.AccessPermissionRead)
	if err != nil {
		return nil, err
	}
	out := dto.ToVirtualDataFileResponseWith(file, string(held))
	return &out, nil
}

// Create places a file product into a directory, which needs WRITE there.
func (s *VirtualDataFileService) Create(ctx context.Context, req *dto.VirtualDataFileRequest) (*dto.VirtualDataFileResponse, error) {
	var out dto.VirtualDataFileResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		tree := s.virtualTree.withTx(tx)

		parent, err := tree.requireWritableParent(ctx, req.ParentDirectoryID)
		if err != nil {
			return err
		}
		if err := tree.requireNameFree(ctx, &parent.ID, "", req.Name(), ""); err != nil {
			return err
		}
		if err := tree.resolveProduct(ctx, &req.DataProductID, true); err != nil {
			return err
		}

		file := &model.VirtualDataFile{CreatedAt: time.Now().UnixMilli()}
		dto.ApplyVirtualDataFileRequest(file, req)
		if err := tree.files.Save(ctx, file); err != nil {
			return err
		}
		out = dto.ToVirtualDataFileResponseWith(file, string(sharingmodel.AccessPermissionWrite))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update renames a file, re-points the product it stands for, or moves it to another
// directory.
//
// A move needs WRITE on both ends: taking an entry out of a dataset is a write to the
// dataset losing it just as much as to the one gaining it.
func (s *VirtualDataFileService) Update(ctx context.Context, id string, req *dto.VirtualDataFileRequest) (*dto.VirtualDataFileResponse, error) {
	var out dto.VirtualDataFileResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		tree := s.virtualTree.withTx(tx)

		file, err := tree.files.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Virtual data file not found: %s", id)
		}
		held, _, err := tree.requireOnParent(ctx, file, sharingmodel.AccessPermissionWrite)
		if err != nil {
			return err
		}
		if movedParent(file.ParentDirectoryID, &req.ParentDirectoryID) {
			if _, err := tree.requireWritableParent(ctx, req.ParentDirectoryID); err != nil {
				return err
			}
		}
		if err := tree.requireNameFree(ctx, &req.ParentDirectoryID, "", req.Name(), file.ID); err != nil {
			return err
		}
		if err := tree.resolveProduct(ctx, &req.DataProductID, true); err != nil {
			return err
		}

		dto.ApplyVirtualDataFileRequest(file, req)
		if err := tree.files.Save(ctx, file); err != nil {
			return err
		}
		out = dto.ToVirtualDataFileResponseWith(file, string(held))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a file from its dataset, which needs WRITE on the directory holding
// it. The product it cited stays registered.
//
// WRITE rather than control, unlike deleting a directory: a leaf carries no shares, so
// removing one withdraws nobody's access.
func (s *VirtualDataFileService) Delete(ctx context.Context, id string) error {
	file, err := s.requireFile(ctx, id)
	if err != nil {
		return err
	}
	if _, _, err := s.requireOnParent(ctx, file, sharingmodel.AccessPermissionWrite); err != nil {
		return err
	}
	return s.files.Delete(ctx, file)
}

// requireFile loads a file or reports 404.
func (t virtualTree) requireFile(ctx context.Context, id string) (*model.VirtualDataFile, error) {
	file, err := t.files.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Virtual data file not found: %s", id)
	}
	return file, nil
}

// requireOnParent resolves the caller's permission on the directory holding file and
// checks it is at least want.
//
// A file with no parent cannot happen — the entity refuses to save one — but it is
// reported rather than assumed, because the alternative is a nil dereference on a row
// that predates that rule.
func (t virtualTree) requireOnParent(ctx context.Context, file *model.VirtualDataFile, want sharingmodel.AccessPermission) (sharingmodel.AccessPermission, bool, error) {
	if file.ParentDirectoryID == nil {
		return sharingmodel.AccessPermissionNone, false, notFoundAs(gorm.ErrRecordNotFound,
			"Virtual data file %s is not in any directory", file.ID)
	}
	parent, err := t.requireDirectory(ctx, *file.ParentDirectoryID)
	if err != nil {
		return sharingmodel.AccessPermissionNone, false, err
	}
	return t.require(ctx, parent, want)
}
