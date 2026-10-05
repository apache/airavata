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

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// maxTreeDepth bounds every walk up a dataset's ancestry.
//
// The schema cannot express "no cycles": a directory points at its parent, and only
// the one-step case of a node parenting itself is caught on write. A longer cycle
// would otherwise turn an access check into an endless loop, so every walk stops here
// and reports a conflict rather than hanging the request.
const maxTreeDepth = 64

// directoryAccess resolves what the calling principal may do with a directory node.
//
// Access is inherited down the tree: a share names a directory and opens the whole
// subtree under it, so resolving a node means asking about that node and every
// ancestor, and taking the strongest answer. Ownership works the same way — owning the
// root of a dataset is owning all of it.
type directoryAccess struct {
	access
	directories *repository.VirtualDataDirectoryRepository
	sharing     *repository.VirtualDataDirectorySharingRepository
}

func (a directoryAccess) withTx(tx *gorm.DB) directoryAccess {
	return directoryAccess{
		access:      a.access.withTx(tx),
		directories: a.directories.WithTx(tx),
		sharing:     a.sharing.WithTx(tx),
	}
}

func (a directoryAccess) requireDirectory(ctx context.Context, id string) (*model.VirtualDataDirectory, error) {
	dir, err := a.directories.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Virtual data directory not found: %s", id)
	}
	return dir, nil
}

// lineageOf returns dir followed by every ancestor, nearest first.
//
// A broken parent link is reported as a 404 against the missing parent rather than
// being skipped: a node whose ancestry cannot be read has no resolvable access, and
// silently treating it as a root would hand out the permissions of a dataset it is not
// part of.
func (a directoryAccess) lineageOf(ctx context.Context, dir *model.VirtualDataDirectory) ([]model.VirtualDataDirectory, error) {
	lineage := []model.VirtualDataDirectory{*dir}
	seen := map[string]bool{dir.ID: true}

	current := dir
	for current.ParentDirectoryID != nil {
		if len(lineage) >= maxTreeDepth {
			return nil, httpx.Conflict(
				"Virtual data directory %s is nested deeper than %d levels", dir.ID, maxTreeDepth)
		}
		parent, err := a.directories.FindByID(ctx, *current.ParentDirectoryID)
		if err != nil {
			return nil, notFoundAs(err, "Parent virtual data directory not found: %s", *current.ParentDirectoryID)
		}
		if seen[parent.ID] {
			return nil, httpx.Conflict(
				"Virtual data directory %s is part of a parent cycle through %s", dir.ID, parent.ID)
		}
		seen[parent.ID] = true
		lineage = append(lineage, *parent)
		current = parent
	}
	return lineage, nil
}

// permissionOf returns the caller's effective permission on dir and whether they control it.
// Permission: real permission held by the caller on the directory.
// Controls: whether the caller owns the directory.
func (a directoryAccess) permissionOf(ctx context.Context, dir *model.VirtualDataDirectory) (model.AccessPermission, bool, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return model.AccessPermissionNone, false, err
	}
	if principal.IsAdmin() {
		return model.AccessPermissionWrite, true, nil
	}

	userShare, err := a.sharing.FindUserShareByUserID(ctx, dir.ID, principal.Name)
	if err != nil && !errors.Is(err, gorm.ErrRecordNotFound) {
		return model.AccessPermissionNone, false, err
	}

	if userShare != nil {
		return *userShare.Permission, false, nil
	}

	lineage, err := a.lineageOf(ctx, dir)
	if err != nil {
		return model.AccessPermissionNone, false, err
	}

	ids := make([]string, 0, len(lineage))
	for i := range lineage {
		if ownsDirectory(&lineage[i], principal.Name) {
			return model.AccessPermissionWrite, true, nil
		}
		ids = append(ids, lineage[i].ID)
	}

	userShares, err := a.sharing.FindUserSharesByDirectoryIDs(ctx, ids)
	if err != nil {
		return model.AccessPermissionNone, false, err
	}
	groupShares, err := a.sharing.FindGroupSharesByDirectoryIDs(ctx, ids)
	if err != nil {
		return model.AccessPermissionNone, false, err
	}

	users := make([]share, 0, len(userShares))
	for i := range userShares {
		users = append(users, newShare(userShares[i].UserID, permissionString(userShares[i].Permission)))
	}
	groups := make([]share, 0, len(groupShares))
	for i := range groupShares {
		groups = append(groups, newShare(groupShares[i].GroupID, permissionString(groupShares[i].Permission)))
	}
	return a.access.permissionOf(ctx, nil, users, groups)
}

// require checks that the caller holds at least want on dir.
func (a directoryAccess) require(ctx context.Context, dir *model.VirtualDataDirectory, want model.AccessPermission) (model.AccessPermission, bool, error) {
	held, controls, err := a.permissionOf(ctx, dir)
	if err != nil {
		return model.AccessPermissionNone, false, err
	}
	if !held.Allows(want) {
		return model.AccessPermissionNone, false, httpx.Forbidden(
			"Access denied: virtual data directory %s is not shared with you for %s", dir.ID, want)
	}
	return held, controls, nil
}

// requireControl allows only an owner in the lineage and platform admins.
func (a directoryAccess) requireControl(ctx context.Context, dir *model.VirtualDataDirectory) error {
	_, controls, err := a.permissionOf(ctx, dir)
	if err != nil {
		return err
	}
	if !controls {
		return httpx.Forbidden(
			"Access denied: only the owner of virtual data directory %s may do that", dir.ID)
	}
	return nil
}

// virtualTree holds the structural rules a dataset obeys, shared by the directory and
// file services because both place nodes into the same tree and must agree on what a
// legal placement is.
type virtualTree struct {
	directoryAccess
	files    *repository.VirtualDataFileRepository
	products productAccess
}

func (t virtualTree) withTx(tx *gorm.DB) virtualTree {
	return virtualTree{
		directoryAccess: t.directoryAccess.withTx(tx),
		files:           t.files.WithTx(tx),
		products:        t.products.withTx(tx),
	}
}

// requireWritableParent loads the directory a node is being placed under and checks
// that the caller may write there, and that it can hold entries at all.
func (t virtualTree) requireWritableParent(ctx context.Context, parentID string) (*model.VirtualDataDirectory, error) {
	parent, err := t.requireDirectory(ctx, parentID)
	if err != nil {
		return nil, err
	}
	if _, _, err := t.require(ctx, parent, model.AccessPermissionWrite); err != nil {
		return nil, err
	}
	if productBacked(parent) {
		return nil, httpx.Conflict(
			"Virtual data directory %s stands for a data product and cannot hold entries", parent.ID)
	}
	return parent, nil
}

// requireNotDescendant refuses a move that would put a directory inside its own
// subtree, which would detach it from every root and make it unreachable.
func (t virtualTree) requireNotDescendant(ctx context.Context, movingID string, destination *model.VirtualDataDirectory) error {
	lineage, err := t.lineageOf(ctx, destination)
	if err != nil {
		return err
	}
	for i := range lineage {
		if lineage[i].ID == movingID {
			return httpx.Conflict("Virtual data directory %s cannot be moved inside itself", movingID)
		}
	}
	return nil
}

// requireNameFree refuses a name already taken by a sibling, of either kind: one
// directory listing holds both, so a file and a directory cannot share a name there.
//
// The unique indexes would catch the same-kind case, but a driver-specific constraint
// error is not an answer a caller can act on. excludeID keeps a node from colliding
// with itself when it is updated in place.
//
// A nil parent means a root, and the index does not constrain rows whose parent is
// NULL, so roots are scoped to one owner here instead.
func (t virtualTree) requireNameFree(ctx context.Context, parentID *string, ownerID, name, excludeID string) error {
	existingDir, err := t.directories.FindByNameUnderParent(ctx, parentID, ownerID, name)
	switch {
	case err == nil && existingDir.ID != excludeID:
		return httpx.Conflict("A virtual data directory named %q already exists here", name)
	case err != nil && !errors.Is(err, gorm.ErrRecordNotFound):
		return err
	}

	if parentID == nil {
		return nil
	}
	existingFile, err := t.files.FindByNameUnderParent(ctx, *parentID, name)
	switch {
	case err == nil && existingFile.ID != excludeID:
		return httpx.Conflict("A virtual data file named %q already exists here", name)
	case err != nil && !errors.Is(err, gorm.ErrRecordNotFound):
		return err
	}
	return nil
}

// requireEmpty refuses an operation on a directory that still has entries.
func (t virtualTree) requireEmpty(ctx context.Context, id string) error {
	files, err := t.files.FindByParentID(ctx, id)
	if err != nil {
		return err
	}
	children, err := t.directories.FindChildDirectories(ctx, id)
	if err != nil {
		return err
	}
	if len(files) > 0 || len(children) > 0 {
		return httpx.Conflict(
			"Virtual data directory %s still has entries and cannot stand for a data product", id)
	}
	return nil
}

// resolveProduct checks the product a node names: that it exists, that the caller can
// read it, and that it is the right kind for the node citing it.
//
// READ is the bar because placing a product in a tree neither moves nor changes it. A
// caller who could cite a product they cannot read would learn its name and shape from
// the dataset they built around it.
func (t virtualTree) resolveProduct(ctx context.Context, productID *string, wantFile bool) error {
	if productID == nil || *productID == "" {
		return nil
	}
	product, err := t.products.requireProduct(ctx, *productID)
	if err != nil {
		return err
	}
	if _, _, err := t.products.require(ctx, product, model.AccessPermissionRead); err != nil {
		return err
	}
	if product.IsFile != wantFile {
		if wantFile {
			return httpx.BadRequest("Data product %s is a directory, not a file", product.ID)
		}
		return httpx.BadRequest("Data product %s is a file, not a directory", product.ID)
	}
	return nil
}

// newVirtualTree assembles the shared structural rules from the repositories.
func newVirtualTree(
	directories *repository.VirtualDataDirectoryRepository,
	files *repository.VirtualDataFileRepository,
	sharing *repository.VirtualDataDirectorySharingRepository,
	products *repository.DataProductRepository,
	productSharing *repository.DataProductSharingRepository,
	members *iamrepo.GroupMemberRepository,
) virtualTree {
	return virtualTree{
		directoryAccess: directoryAccess{
			access:      access{members: members},
			directories: directories,
			sharing:     sharing,
		},
		files: files,
		products: productAccess{
			access:   access{members: members},
			products: products,
			sharing:  productSharing,
		},
	}
}

// productBacked reports whether a directory stands for a registered product rather
// than holding entries of its own.
func productBacked(d *model.VirtualDataDirectory) bool {
	return d.DataProductID != nil && *d.DataProductID != ""
}

// ownsDirectory reports whether userID owns d. A directory with no owner is owned by
// nobody, so it must not match the empty principal name.
func ownsDirectory(d *model.VirtualDataDirectory, userID string) bool {
	return d.OwnerID != nil && *d.OwnerID == userID
}

// movedParent reports whether a request changes a node's parent.
func movedParent(current, requested *string) bool {
	switch {
	case current == nil && requested == nil:
		return false
	case current == nil || requested == nil:
		return true
	default:
		return *current != *requested
	}
}
