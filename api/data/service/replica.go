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

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/data/dto"
	model "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	sharingmodel "github.com/apache/airavata/api/sharing/model"
	sharingrepo "github.com/apache/airavata/api/sharing/repository"
	sharingsvc "github.com/apache/airavata/api/sharing/service"
)

// DataProductReplicaService manages the secondary locations of data products.
//
// Reading them needs READ on the product and changing them needs WRITE. Adding one also
// needs READ on the storage it names, the same rule as registering a product there.
// The primary location is not managed here: it is the product's own storage and path.
type DataProductReplicaService struct {
	productAccess
	db          *gorm.DB
	replicas    *repository.DataProductReplicaRepository
	scpStorages *repository.SCPDataStorageRepository
	s3Storages  *repository.S3DataStorageRepository
}

// NewDataProductReplicaService returns a replica service.
func NewDataProductReplicaService(
	db *gorm.DB,
	products *repository.DataProductRepository,
	sharing *sharingrepo.Repository,
	replicas *repository.DataProductReplicaRepository,
	scpStorages *repository.SCPDataStorageRepository,
	s3Storages *repository.S3DataStorageRepository,
	members *iamrepo.GroupMemberRepository,
) *DataProductReplicaService {
	return &DataProductReplicaService{
		productAccess: productAccess{
			access:   sharingsvc.NewAccess(members),
			products: products,
			sharing:  sharing,
		},
		db:          db,
		replicas:    replicas,
		scpStorages: scpStorages,
		s3Storages:  s3Storages,
	}
}

// List returns every location of a product, primary first.
func (s *DataProductReplicaService) List(ctx context.Context, productID string) ([]dto.DataProductReplicaResponse, error) {
	product, err := s.requireProduct(ctx, productID)
	if err != nil {
		return nil, err
	}
	if _, _, err := s.require(ctx, product, sharingmodel.AccessPermissionRead); err != nil {
		return nil, err
	}
	return dto.ToDataProductReplicaResponses(product.Replicas), nil
}

// Add records one more location of a product. It does not copy any bytes: it states
// that the product's data is also at this location.
func (s *DataProductReplicaService) Add(ctx context.Context, productID string, req *dto.DataProductReplicaRequest) (*dto.DataProductReplicaResponse, error) {
	var out dto.DataProductReplicaResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		product, err := s.products.WithTx(tx).FindByID(ctx, productID)
		if err != nil {
			return notFoundAs(err, "Data product not found: %s", productID)
		}
		if _, _, err := s.productAccess.withTx(tx).require(ctx, product, sharingmodel.AccessPermissionWrite); err != nil {
			return err
		}
		storageType := req.StorageType()
		if err := s.requireStorageReachable(ctx, tx, storageType, req.DataStorageID); err != nil {
			return err
		}

		replicas := s.replicas.WithTx(tx)
		exists, err := replicas.ExistsAtLocation(ctx, productID, req.DataStorageID, storageType, req.Path)
		if err != nil {
			return err
		}
		if exists {
			return httpx.Conflict("Data product %s already has a replica at that location", productID)
		}

		replica := &model.DataProductReplica{
			DataProductID:   productID,
			DataStorageID:   req.DataStorageID,
			DataStorageType: storageType,
			Path:            req.Path,
			CreatedAt:       time.Now().UnixMilli(),
		}
		if err := replicas.Save(ctx, replica); err != nil {
			return err
		}
		out = dto.ToDataProductReplicaResponse(replica)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Remove deletes a secondary location. The primary cannot be removed here, because the
// product's own columns would then name a location with no replica behind it.
func (s *DataProductReplicaService) Remove(ctx context.Context, productID, replicaID string) error {
	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		product, err := s.products.WithTx(tx).FindByID(ctx, productID)
		if err != nil {
			return notFoundAs(err, "Data product not found: %s", productID)
		}
		if _, _, err := s.productAccess.withTx(tx).require(ctx, product, sharingmodel.AccessPermissionWrite); err != nil {
			return err
		}

		replicas := s.replicas.WithTx(tx)
		replica, err := replicas.FindByID(ctx, productID, replicaID)
		if err != nil {
			return notFoundAs(err, "Replica %s not found on data product %s", replicaID, productID)
		}
		if replica.IsPrimary {
			return httpx.Conflict("Replica %s is the primary location of data product %s: update the product to move it", replicaID, productID)
		}
		return replicas.Delete(ctx, replica)
	})
}

// requireStorageReachable checks that the storage exists and the caller holds READ on
// it. The id carries no foreign key, so this is what keeps a replica from dangling.
func (s *DataProductReplicaService) requireStorageReachable(ctx context.Context, tx *gorm.DB, storageType model.DataStorageType, storageID string) error {
	base, sharing := s.access.WithTx(tx), s.sharing.WithTx(tx)

	if storageType == model.DataStorageTypeS3 {
		storage, err := s.s3Storages.WithTx(tx).FindByID(ctx, storageID)
		if err != nil {
			return notFoundAs(err, "S3 data storage not found: %s", storageID)
		}
		a := s3StorageAccess{access: base, sharing: sharing}
		_, err = a.require(ctx, storage, sharingmodel.AccessPermissionRead)
		return err
	}

	storage, err := s.scpStorages.WithTx(tx).FindByID(ctx, storageID)
	if err != nil {
		return notFoundAs(err, "SCP data storage not found: %s", storageID)
	}
	return requireStorageReadable(ctx, base, sharing, storage)
}
