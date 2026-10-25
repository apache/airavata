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

	"gorm.io/gorm"

	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	sharingmodel "github.com/apache/airavata/api/sharing/model"
	sharingrepo "github.com/apache/airavata/api/sharing/repository"
	sharingsvc "github.com/apache/airavata/api/sharing/service"
)

// The guards below are this vertical's half of the sharing contract: the sharing
// service knows how to grant, revoke and resolve, and these say which record an id
// names and who is allowed to decide who else reaches it.
//
// Each wraps the access struct the vertical already had, so the rule for "may this
// caller manage the shares" is the same one every other operation on that record uses.

// productGuard shares data products.
type productGuard struct{ productAccess }

func (g productGuard) ResourceType() sharingmodel.ResourceType {
	return sharingmodel.ResourceTypeDataProduct
}

func (g productGuard) Label() string { return "data product" }

func (g productGuard) RequireControlled(ctx context.Context, id string) ([]string, error) {
	product, err := g.requireProduct(ctx, id)
	if err != nil {
		return nil, err
	}
	if err := g.requireControl(ctx, product); err != nil {
		return nil, err
	}
	if product.OwnerID == nil {
		return nil, nil
	}
	return []string{*product.OwnerID}, nil
}

// storageGuard shares SCP data storages.
type storageGuard struct{ storageAccess }

func (g storageGuard) ResourceType() sharingmodel.ResourceType {
	return sharingmodel.ResourceTypeSCPDataStorage
}

func (g storageGuard) Label() string { return "SCP data storage" }

func (g storageGuard) RequireControlled(ctx context.Context, id string) ([]string, error) {
	storage, err := g.requireStorage(ctx, id)
	if err != nil {
		return nil, err
	}
	if err := g.requireControl(ctx, storage); err != nil {
		return nil, err
	}
	if storage.OwnerID == nil {
		return nil, nil
	}
	return []string{*storage.OwnerID}, nil
}

// directoryGuard shares virtual data directories.
//
// A share names a directory and opens the subtree under it, so sharing an interior
// node hands out part of a dataset without handing out the whole of it. Ownership
// spans the lineage, which is why this returns more than one owner.
type directoryGuard struct{ directoryAccess }

func (g directoryGuard) ResourceType() sharingmodel.ResourceType {
	return sharingmodel.ResourceTypeVirtualDataDirectory
}

func (g directoryGuard) Label() string { return "virtual data directory" }

func (g directoryGuard) RequireControlled(ctx context.Context, id string) ([]string, error) {
	dir, err := g.requireDirectory(ctx, id)
	if err != nil {
		return nil, err
	}
	if err := g.requireControl(ctx, dir); err != nil {
		return nil, err
	}
	lineage, err := g.lineageOf(ctx, dir)
	if err != nil {
		return nil, err
	}
	owners := make([]string, 0, len(lineage))
	for i := range lineage {
		if lineage[i].OwnerID != nil {
			owners = append(owners, *lineage[i].OwnerID)
		}
	}
	return owners, nil
}

// NewDataProductSharingService returns the sharing service for data products.
func NewDataProductSharingService(
	db *gorm.DB,
	products *repository.DataProductRepository,
	sharing *sharingrepo.Repository,
	groups *iamrepo.GroupRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *sharingsvc.Service {
	guard := productGuard{productAccess{
		access:   sharingsvc.NewAccess(members),
		products: products,
		sharing:  sharing,
	}}
	return sharingsvc.NewService(guard, db, sharing, groups, users)
}

// NewSCPDataStorageSharingService returns the sharing service for SCP data storages.
func NewSCPDataStorageSharingService(
	db *gorm.DB,
	storages *repository.SCPDataStorageRepository,
	sharing *sharingrepo.Repository,
	groups *iamrepo.GroupRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *sharingsvc.Service {
	guard := storageGuard{storageAccess{
		access:   sharingsvc.NewAccess(members),
		storages: storages,
		sharing:  sharing,
	}}
	return sharingsvc.NewService(guard, db, sharing, groups, users)
}

// NewVirtualDataDirectorySharingService returns the sharing service for virtual data
// directories.
func NewVirtualDataDirectorySharingService(
	db *gorm.DB,
	directories *repository.VirtualDataDirectoryRepository,
	sharing *sharingrepo.Repository,
	groups *iamrepo.GroupRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *sharingsvc.Service {
	guard := directoryGuard{directoryAccess{
		access:      sharingsvc.NewAccess(members),
		directories: directories,
		sharing:     sharing,
	}}
	return sharingsvc.NewService(guard, db, sharing, groups, users)
}
