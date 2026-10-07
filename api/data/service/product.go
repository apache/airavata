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
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/data/dto"
	model "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	sharingmodel "github.com/apache/airavata/api/sharing/model"
	sharingrepo "github.com/apache/airavata/api/sharing/repository"
	sharingsvc "github.com/apache/airavata/api/sharing/service"
)

// productAccess resolves what the calling principal may do with a product, by loading
// its shares and handing them to the shared resolver.
type productAccess struct {
	access
	products *repository.DataProductRepository
	sharing  *sharingrepo.Repository
}

func (a productAccess) withTx(tx *gorm.DB) productAccess {
	return productAccess{
		access:   a.access.WithTx(tx),
		products: a.products.WithTx(tx),
		sharing:  a.sharing.WithTx(tx),
	}
}

// requireProduct loads a product or reports 404.
func (a productAccess) requireProduct(ctx context.Context, id string) (*model.DataProduct, error) {
	product, err := a.products.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Data product not found: %s", id)
	}
	return product, nil
}

// permissionOf returns the caller's effective permission on product and whether they
// control it.
func (a productAccess) permissionOf(ctx context.Context, product *model.DataProduct) (sharingmodel.AccessPermission, bool, error) {
	shares, err := a.sharing.FindByResource(ctx, sharingmodel.ResourceTypeDataProduct, product.ID)
	if err != nil {
		return sharingmodel.AccessPermissionNone, false, err
	}
	return a.access.PermissionOf(ctx, product.OwnerID, shares)
}

// require checks that the caller holds at least want.
func (a productAccess) require(ctx context.Context, product *model.DataProduct, want sharingmodel.AccessPermission) (sharingmodel.AccessPermission, bool, error) {
	held, controls, err := a.permissionOf(ctx, product)
	if err != nil {
		return sharingmodel.AccessPermissionNone, false, err
	}
	if !held.Allows(want) {
		return sharingmodel.AccessPermissionNone, false, httpx.Forbidden(
			"Access denied: data product %s is not shared with you for %s", product.ID, want)
	}
	return held, controls, nil
}

// requireControl allows only the owner and platform admins.
func (a productAccess) requireControl(ctx context.Context, product *model.DataProduct) error {
	_, controls, err := a.permissionOf(ctx, product)
	if err != nil {
		return err
	}
	if !controls {
		return httpx.Forbidden("Access denied: only the owner of data product %s may do that", product.ID)
	}
	return nil
}

// DataProductService manages registered datasets.
//
// A product belongs to whoever registered it, and everyone else reaches it only
// through a sharing rule — there is no listing an ordinary caller can use to discover
// products that were never shared with them.
type DataProductService struct {
	productAccess
	db       *gorm.DB
	storages *repository.SCPDataStorageRepository
	users    *iamrepo.UserRepository
}

// NewDataProductService returns a data product service.
func NewDataProductService(
	db *gorm.DB,
	products *repository.DataProductRepository,
	sharing *sharingrepo.Repository,
	storages *repository.SCPDataStorageRepository,
	users *iamrepo.UserRepository,
	members *iamrepo.GroupMemberRepository,
) *DataProductService {
	return &DataProductService{
		productAccess: productAccess{
			access:   sharingsvc.NewAccess(members),
			products: products,
			sharing:  sharing,
		},
		db:       db,
		storages: storages,
		users:    users,
	}
}

// List returns every product across every owner. Admin only — it names who holds what
// data.
func (s *DataProductService) List(ctx context.Context) ([]dto.DataProductResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	products, err := s.products.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToDataProductResponses(products), nil
}

// ListMine returns the caller's own products.
func (s *DataProductService) ListMine(ctx context.Context) ([]dto.DataProductResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	products, err := s.products.FindByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	return dto.ToDataProductResponses(products), nil
}

// ListSharedWithMe returns the products other users have shared with the caller,
// directly or through a group, each carrying what it grants them.
func (s *DataProductService) ListSharedWithMe(ctx context.Context) ([]dto.DataProductResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	ids, err := s.sharing.ResourceIDsSharedWith(ctx, sharingmodel.ResourceTypeDataProduct, principal.Name)
	if err != nil {
		return nil, err
	}
	products, err := s.products.FindByIDsExcludingOwner(ctx, ids, principal.Name)
	if err != nil {
		return nil, err
	}

	out := make([]dto.DataProductResponse, 0, len(products))
	for i := range products {
		// Re-resolving per product keeps the reported permission honest when a user
		// share and a group share reach the same product with different grants.
		held, _, err := s.permissionOf(ctx, &products[i])
		if err != nil {
			return nil, err
		}
		if held == sharingmodel.AccessPermissionNone {
			continue
		}
		out = append(out, dto.ToDataProductResponseWith(&products[i], string(held)))
	}
	return out, nil
}

// Get returns one product, to anyone holding READ on it.
func (s *DataProductService) Get(ctx context.Context, id string) (*dto.DataProductResponse, error) {
	product, err := s.requireProduct(ctx, id)
	if err != nil {
		return nil, err
	}
	held, _, err := s.require(ctx, product, sharingmodel.AccessPermissionRead)
	if err != nil {
		return nil, err
	}
	out := dto.ToDataProductResponseWith(product, string(held))
	return &out, nil
}

// Create registers a dataset owned by the calling user.
//
// The storage it names must be one the caller can already reach: registering data into
// a storage nobody shared with them would be a way to have the platform touch a host
// they have no standing on.
func (s *DataProductService) Create(ctx context.Context, req *dto.DataProductRequest) (*dto.DataProductResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}

	var out dto.DataProductResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		products, users := s.products.WithTx(tx), s.users.WithTx(tx)

		owner, err := users.FindByID(ctx, principal.Name)
		if err != nil {
			return notFoundAs(err, "No user record found for authenticated principal: %s", principal.Name)
		}
		if err := s.resolveReferences(ctx, tx, req); err != nil {
			return err
		}

		status := model.ProvisionStatusRegistered
		product := &model.DataProduct{
			OwnerID:         &owner.ID,
			ProvisionStatus: &status,
			CreatedAt:       time.Now().UnixMilli(),
		}
		dto.ApplyDataProductRequest(product, req)
		if err := products.Save(ctx, product); err != nil {
			return err
		}
		out = dto.ToDataProductResponseWith(product, string(sharingmodel.AccessPermissionWrite))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a product's own fields. It needs WRITE, which a share can confer.
//
// The owner, the provision status and the creation time are left alone: re-deriving
// the owner from the caller's token would hand the product to whichever admin — or
// grantee — happened to issue the request.
func (s *DataProductService) Update(ctx context.Context, id string, req *dto.DataProductRequest) (*dto.DataProductResponse, error) {
	var out dto.DataProductResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		products := s.products.WithTx(tx)

		product, err := products.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "Data product not found: %s", id)
		}
		held, _, err := s.productAccess.withTx(tx).require(ctx, product, sharingmodel.AccessPermissionWrite)
		if err != nil {
			return err
		}
		if err := s.resolveReferences(ctx, tx, req); err != nil {
			return err
		}

		dto.ApplyDataProductRequest(product, req)
		if err := products.Save(ctx, product); err != nil {
			return err
		}
		out = dto.ToDataProductResponseWith(product, string(held))
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a product and every share of it, for its owner or an admin.
//
// The shares go first and in the same transaction: their foreign keys are RESTRICT, so
// leaving them would turn an ordinary delete into a constraint violation.
func (s *DataProductService) Delete(ctx context.Context, id string) error {
	product, err := s.requireProduct(ctx, id)
	if err != nil {
		return err
	}
	if err := s.requireControl(ctx, product); err != nil {
		return err
	}

	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := s.sharing.WithTx(tx).DeleteByResources(ctx, sharingmodel.ResourceTypeDataProduct, []string{product.ID}); err != nil {
			return err
		}
		return s.products.WithTx(tx).Delete(ctx, product)
	})
}

// resolveReferences checks the storage a request names.
//
// It has to be reachable by the caller, and the reference carries no foreign key — the
// storage id is qualified by a storage *type* — so this is the only thing standing
// between a request and a dangling one. The host and the account the dataset is reached
// under come from the storage itself, so there is nothing else here to agree with.
func (s *DataProductService) resolveReferences(ctx context.Context, tx *gorm.DB, req *dto.DataProductRequest) error {
	storage, err := s.storages.WithTx(tx).FindByID(ctx, req.DataStorageID)
	if err != nil {
		return notFoundAs(err, "SCP data storage not found: %s", req.DataStorageID)
	}
	return requireStorageReadable(ctx, s.access.WithTx(tx), s.sharing.WithTx(tx), storage)
}
