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

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	credmodel "github.com/apache/airavata/api/credentials/model"
	credsvc "github.com/apache/airavata/api/credentials/service"
	dto "github.com/apache/airavata/api/data/dto"
	model "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	sharingmodel "github.com/apache/airavata/api/sharing/model"
	sharingrepo "github.com/apache/airavata/api/sharing/repository"
	sharingsvc "github.com/apache/airavata/api/sharing/service"
)

// storageAccess resolves what the calling principal may do with a storage.
//
// Same model as a data product: strongest of ownership, a share naming the caller, and
// a share naming a group they are an active member of, with platform admins treated as
// owners. Control — deleting a storage and managing its shares — is not reachable
// through a share.
type storageAccess struct {
	access
	storages *repository.SCPDataStorageRepository
	sharing  *sharingrepo.Repository
}

func (a storageAccess) withTx(tx *gorm.DB) storageAccess {
	return storageAccess{
		access:   a.access.WithTx(tx),
		storages: a.storages.WithTx(tx),
		sharing:  a.sharing.WithTx(tx),
	}
}

// requireStorage loads a storage or reports 404.
func (a storageAccess) requireStorage(ctx context.Context, id string) (*model.SCPDataStorage, error) {
	storage, err := a.storages.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "SCP data storage not found: %s", id)
	}
	return storage, nil
}

// permissionOf returns the caller's effective permission on storage and whether they
// control it.
func (a storageAccess) permissionOf(ctx context.Context, storage *model.SCPDataStorage) (sharingmodel.AccessPermission, bool, error) {
	shares, err := a.sharing.FindByResource(ctx, sharingmodel.ResourceTypeSCPDataStorage, storage.ID)
	if err != nil {
		return sharingmodel.AccessPermissionNone, false, err
	}
	return a.access.PermissionOf(ctx, storage.OwnerID, shares)
}

// require checks that the caller holds at least want.
func (a storageAccess) require(ctx context.Context, storage *model.SCPDataStorage, want sharingmodel.AccessPermission) (sharingmodel.AccessPermission, error) {
	held, _, err := a.permissionOf(ctx, storage)
	if err != nil {
		return sharingmodel.AccessPermissionNone, err
	}
	if !held.Allows(want) {
		return sharingmodel.AccessPermissionNone, httpx.Forbidden(
			"Access denied: SCP data storage %s is not shared with you for %s", storage.ID, want)
	}
	return held, nil
}

// requireControl allows only the owner and platform admins.
func (a storageAccess) requireControl(ctx context.Context, storage *model.SCPDataStorage) error {
	_, controls, err := a.permissionOf(ctx, storage)
	if err != nil {
		return err
	}
	if !controls {
		return httpx.Forbidden("Access denied: only the owner of SCP data storage %s may do that", storage.ID)
	}
	return nil
}

// requireStorageReadable is the check the product service runs before letting a
// dataset be registered into a storage. It lives here so both services read the same
// rule.
func requireStorageReadable(ctx context.Context, base access, sharing *sharingrepo.Repository, storage *model.SCPDataStorage) error {
	a := storageAccess{access: base, sharing: sharing}
	_, err := a.require(ctx, storage, sharingmodel.AccessPermissionRead)
	return err
}

// SCPDataStorageService manages the storages datasets are staged through.
//
// Registering one is self-service: any authenticated caller may declare a storage on a
// host of their choosing, under an account of their choosing, presenting one of their
// own SSH keys, and it belongs to them. Everyone else reaches it through its sharing
// rules — which is how they stage under a key they do not hold.
type SCPDataStorageService struct {
	storageAccess
	db       *gorm.DB
	keys     *credsvc.KeyAccess
	products *repository.DataProductRepository
	users    *iamrepo.UserRepository
}

// NewSCPDataStorageService returns a storage service.
func NewSCPDataStorageService(
	db *gorm.DB,
	storages *repository.SCPDataStorageRepository,
	sharing *sharingrepo.Repository,
	keys *credsvc.KeyAccess,
	products *repository.DataProductRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *SCPDataStorageService {
	return &SCPDataStorageService{
		storageAccess: storageAccess{
			access:   sharingsvc.NewAccess(members),
			storages: storages,
			sharing:  sharing,
		},
		db:       db,
		keys:     keys,
		products: products,
		users:    users,
	}
}

// List returns every storage across every owner. Admin only — it names who stages what
// where.
func (s *SCPDataStorageService) List(ctx context.Context) ([]dto.SCPDataStorageResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	storages, err := s.storages.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToSCPDataStorageResponses(storages), nil
}

// ListMine returns the caller's own storages.
func (s *SCPDataStorageService) ListMine(ctx context.Context) ([]dto.SCPDataStorageResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	storages, err := s.storages.FindByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	return dto.ToSCPDataStorageResponses(storages), nil
}

// ListSharedWithMe returns the storages other users have shared with the caller,
// directly or through a group, each carrying what it grants them.
func (s *SCPDataStorageService) ListSharedWithMe(ctx context.Context) ([]dto.SCPDataStorageResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	ids, err := s.sharing.ResourceIDsSharedWith(ctx, sharingmodel.ResourceTypeSCPDataStorage, principal.Name)
	if err != nil {
		return nil, err
	}
	storages, err := s.storages.FindByIDsExcludingOwner(ctx, ids, principal.Name)
	if err != nil {
		return nil, err
	}

	out := make([]dto.SCPDataStorageResponse, 0, len(storages))
	for i := range storages {
		held, _, err := s.permissionOf(ctx, &storages[i])
		if err != nil {
			return nil, err
		}
		if held == sharingmodel.AccessPermissionNone {
			continue
		}
		out = append(out, dto.ToSCPDataStorageResponseWith(&storages[i], string(held)))
	}
	return out, nil
}

// Get returns one storage, to an admin or to anyone a share reaches.
func (s *SCPDataStorageService) Get(ctx context.Context, id string) (*dto.SCPDataStorageResponse, error) {
	storage, err := s.requireStorage(ctx, id)
	if err != nil {
		return nil, err
	}
	held, err := s.require(ctx, storage, sharingmodel.AccessPermissionRead)
	if err != nil {
		return nil, err
	}
	out := dto.ToSCPDataStorageResponseWith(storage, string(held))
	return &out, nil
}

// resolveKey loads the SSH key a request names. It is not created here, so an id that
// resolves to nothing is a 404 rather than a storage pointing at a key that does not
// exist.
//
// held is the key the storage already presents, or nil on create. Assigning a key needs
// the caller to own it; keeping the one already there does not, so a grantee with WRITE
// can rename a storage or move its host without owning the key it stages under — and
// still cannot point it at a key of their own.
func (s *SCPDataStorageService) resolveKey(ctx context.Context, tx *gorm.DB, req *dto.SCPDataStorageRequest, held *string) (*credmodel.SSHKey, error) {
	keys := s.keys.WithTx(tx)
	if held != nil && *held == req.SSHKeyID {
		return keys.Find(ctx, req.SSHKeyID)
	}
	return keys.RequireOwned(ctx, req.SSHKeyID)
}

// Create registers a storage owned by the calling user, on the host it names and under
// an existing SSH key.
//
// The owner is taken from the token, so there is no way to register a storage on
// someone else's behalf.
func (s *SCPDataStorageService) Create(ctx context.Context, req *dto.SCPDataStorageRequest) (*dto.SCPDataStorageResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.SCPDataStorageResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		storages := s.storages.WithTx(tx)

		owner, err := s.users.WithTx(tx).FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}
		key, err := s.resolveKey(ctx, tx, req, nil)
		if err != nil {
			return err
		}

		storage := &model.SCPDataStorage{
			SSHKeyID: &key.ID,
			SSHKey:   key,
			OwnerID:  &owner.ID,
		}
		dto.ApplySCPDataStorageRequest(storage, req)
		if err := storages.Save(ctx, storage); err != nil {
			return err
		}
		out = dto.ToSCPDataStorageResponseWith(storage, string(sharingmodel.AccessPermissionWrite))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a storage, including which host it stages through and which account
// and key it stages under. It needs WRITE, which a share can confer.
//
// The owner is deliberately left alone: re-deriving it from the caller's token would
// hand the storage to whichever admin — or grantee — happened to issue the request.
func (s *SCPDataStorageService) Update(ctx context.Context, id string, req *dto.SCPDataStorageRequest) (*dto.SCPDataStorageResponse, error) {
	var out dto.SCPDataStorageResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		storages := s.storages.WithTx(tx)

		storage, err := storages.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "SCP data storage not found: %s", id)
		}
		held, err := s.storageAccess.withTx(tx).require(ctx, storage, sharingmodel.AccessPermissionWrite)
		if err != nil {
			return err
		}
		key, err := s.resolveKey(ctx, tx, req, storage.SSHKeyID)
		if err != nil {
			return err
		}

		dto.ApplySCPDataStorageRequest(storage, req)
		storage.SSHKeyID = &key.ID
		storage.SSHKey = key
		if err := storages.Save(ctx, storage); err != nil {
			return err
		}
		out = dto.ToSCPDataStorageResponseWith(storage, string(held))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a storage nothing is staged on, together with its shares.
//
// Products are checked first: a product's storage id carries no foreign key — it is
// qualified by a storage *type* — so nothing at the database level would stop this
// from orphaning them.
func (s *SCPDataStorageService) Delete(ctx context.Context, id string) error {
	storage, err := s.requireStorage(ctx, id)
	if err != nil {
		return err
	}
	if err := s.requireControl(ctx, storage); err != nil {
		return err
	}
	products, err := s.products.FindByDataStorageID(ctx, storage.ID)
	if err != nil {
		return err
	}
	if len(products) > 0 {
		return httpx.Conflict("SCP data storage %s still holds %d data product(s)", storage.ID, len(products))
	}

	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := s.sharing.WithTx(tx).DeleteByResources(ctx, sharingmodel.ResourceTypeSCPDataStorage, []string{storage.ID}); err != nil {
			return err
		}
		return s.storages.WithTx(tx).Delete(ctx, storage)
	})
}
