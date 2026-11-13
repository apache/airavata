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

// s3StorageAccess resolves what the calling principal may do with an S3 storage. The
// rule is the SCP storage rule: strongest of ownership, a direct share and a share to
// an active group membership, with platform admins treated as owners, and control
// unreachable through a share.
type s3StorageAccess struct {
	access
	storages *repository.S3DataStorageRepository
	sharing  *sharingrepo.Repository
}

func (a s3StorageAccess) withTx(tx *gorm.DB) s3StorageAccess {
	return s3StorageAccess{
		access:   a.access.WithTx(tx),
		storages: a.storages.WithTx(tx),
		sharing:  a.sharing.WithTx(tx),
	}
}

// requireStorage loads a storage or reports 404.
func (a s3StorageAccess) requireStorage(ctx context.Context, id string) (*model.S3DataStorage, error) {
	storage, err := a.storages.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "S3 data storage not found: %s", id)
	}
	return storage, nil
}

// permissionOf returns the caller's effective permission on storage and whether they
// control it.
func (a s3StorageAccess) permissionOf(ctx context.Context, storage *model.S3DataStorage) (sharingmodel.AccessPermission, bool, error) {
	shares, err := a.sharing.FindByResource(ctx, sharingmodel.ResourceTypeS3DataStorage, storage.ID)
	if err != nil {
		return sharingmodel.AccessPermissionNone, false, err
	}
	return a.access.PermissionOf(ctx, storage.OwnerID, shares)
}

// require checks that the caller holds at least want.
func (a s3StorageAccess) require(ctx context.Context, storage *model.S3DataStorage, want sharingmodel.AccessPermission) (sharingmodel.AccessPermission, error) {
	held, _, err := a.permissionOf(ctx, storage)
	if err != nil {
		return sharingmodel.AccessPermissionNone, err
	}
	if !held.Allows(want) {
		return sharingmodel.AccessPermissionNone, httpx.Forbidden(
			"Access denied: S3 data storage %s is not shared with you for %s", storage.ID, want)
	}
	return held, nil
}

// requireControl allows only the owner and platform admins.
func (a s3StorageAccess) requireControl(ctx context.Context, storage *model.S3DataStorage) error {
	_, controls, err := a.permissionOf(ctx, storage)
	if err != nil {
		return err
	}
	if !controls {
		return httpx.Forbidden("Access denied: only the owner of S3 data storage %s may do that", storage.ID)
	}
	return nil
}

// S3DataStorageService manages the S3 buckets data products can live in.
//
// Registering one is self-service: any authenticated caller may declare a bucket on an
// endpoint of their choosing, presenting one of their own S3 credentials, and it
// belongs to them. Everyone else reaches it through its sharing rules — which is how
// they use data in a bucket without ever holding the credential.
type S3DataStorageService struct {
	s3StorageAccess
	db       *gorm.DB
	creds    *credsvc.S3CredentialAccess
	products *repository.DataProductRepository
	users    *iamrepo.UserRepository
}

// NewS3DataStorageService returns an S3 storage service.
func NewS3DataStorageService(
	db *gorm.DB,
	storages *repository.S3DataStorageRepository,
	sharing *sharingrepo.Repository,
	creds *credsvc.S3CredentialAccess,
	products *repository.DataProductRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *S3DataStorageService {
	return &S3DataStorageService{
		s3StorageAccess: s3StorageAccess{
			access:   sharingsvc.NewAccess(members),
			storages: storages,
			sharing:  sharing,
		},
		db:       db,
		creds:    creds,
		products: products,
		users:    users,
	}
}

// List returns every storage across every owner. Admin only.
func (s *S3DataStorageService) List(ctx context.Context) ([]dto.S3DataStorageResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	storages, err := s.storages.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToS3DataStorageResponses(storages), nil
}

// ListMine returns the caller's own storages.
func (s *S3DataStorageService) ListMine(ctx context.Context) ([]dto.S3DataStorageResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	storages, err := s.storages.FindByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	return dto.ToS3DataStorageResponses(storages), nil
}

// ListSharedWithMe returns the storages other users have shared with the caller,
// directly or through a group, each carrying what it grants them.
func (s *S3DataStorageService) ListSharedWithMe(ctx context.Context) ([]dto.S3DataStorageResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	ids, err := s.sharing.ResourceIDsSharedWith(ctx, sharingmodel.ResourceTypeS3DataStorage, principal.Name)
	if err != nil {
		return nil, err
	}
	storages, err := s.storages.FindByIDsExcludingOwner(ctx, ids, principal.Name)
	if err != nil {
		return nil, err
	}

	out := make([]dto.S3DataStorageResponse, 0, len(storages))
	for i := range storages {
		held, _, err := s.permissionOf(ctx, &storages[i])
		if err != nil {
			return nil, err
		}
		if held == sharingmodel.AccessPermissionNone {
			continue
		}
		out = append(out, dto.ToS3DataStorageResponseWith(&storages[i], string(held)))
	}
	return out, nil
}

// Get returns one storage, to an admin or to anyone a share reaches.
func (s *S3DataStorageService) Get(ctx context.Context, id string) (*dto.S3DataStorageResponse, error) {
	storage, err := s.requireStorage(ctx, id)
	if err != nil {
		return nil, err
	}
	held, err := s.require(ctx, storage, sharingmodel.AccessPermissionRead)
	if err != nil {
		return nil, err
	}
	out := dto.ToS3DataStorageResponseWith(storage, string(held))
	return &out, nil
}

// resolveCredential loads the credential a request names.
//
// held is the credential the storage already presents, or nil on create. Assigning a
// credential needs the caller to own it; keeping the one already there does not, so a
// WRITE grantee can rename a storage without owning its credential — and still cannot
// point it at somebody else's. This is the SSH credential rule on SCP storages.
func (s *S3DataStorageService) resolveCredential(ctx context.Context, tx *gorm.DB, req *dto.S3DataStorageRequest, held *string) (*credmodel.S3Credential, error) {
	creds := s.creds.WithTx(tx)
	if held != nil && *held == req.S3CredentialID {
		return creds.Find(ctx, req.S3CredentialID)
	}
	return creds.RequireOwned(ctx, req.S3CredentialID)
}

// Create registers a storage owned by the calling user, under an existing credential.
func (s *S3DataStorageService) Create(ctx context.Context, req *dto.S3DataStorageRequest) (*dto.S3DataStorageResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.S3DataStorageResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		owner, err := s.users.WithTx(tx).FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}
		cred, err := s.resolveCredential(ctx, tx, req, nil)
		if err != nil {
			return err
		}

		storage := &model.S3DataStorage{
			S3CredentialID: &cred.ID,
			S3Credential:   cred,
			OwnerID:        &owner.ID,
		}
		dto.ApplyS3DataStorageRequest(storage, req)
		if err := s.storages.WithTx(tx).Save(ctx, storage); err != nil {
			return err
		}
		out = dto.ToS3DataStorageResponseWith(storage, string(sharingmodel.AccessPermissionWrite))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a storage, including the bucket and the credential it presents. It
// needs WRITE, which a share can confer. The owner is left alone.
func (s *S3DataStorageService) Update(ctx context.Context, id string, req *dto.S3DataStorageRequest) (*dto.S3DataStorageResponse, error) {
	var out dto.S3DataStorageResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		storages := s.storages.WithTx(tx)

		storage, err := storages.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "S3 data storage not found: %s", id)
		}
		held, err := s.s3StorageAccess.withTx(tx).require(ctx, storage, sharingmodel.AccessPermissionWrite)
		if err != nil {
			return err
		}
		cred, err := s.resolveCredential(ctx, tx, req, storage.S3CredentialID)
		if err != nil {
			return err
		}

		dto.ApplyS3DataStorageRequest(storage, req)
		storage.S3CredentialID = &cred.ID
		storage.S3Credential = cred
		if err := storages.Save(ctx, storage); err != nil {
			return err
		}
		out = dto.ToS3DataStorageResponseWith(storage, string(held))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a storage nothing is registered on, together with its shares.
//
// Products are checked first, as for SCP storages: a product's storage id carries no
// foreign key, so nothing at the database level would stop this from orphaning them.
func (s *S3DataStorageService) Delete(ctx context.Context, id string) error {
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
		return httpx.Conflict("S3 data storage %s still holds %d data product(s)", storage.ID, len(products))
	}

	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := s.sharing.WithTx(tx).DeleteByResources(ctx, sharingmodel.ResourceTypeS3DataStorage, []string{storage.ID}); err != nil {
			return err
		}
		return s.storages.WithTx(tx).Delete(ctx, storage)
	})
}
