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

package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// DataProductReplica is one place a data product's bytes can be found. A product can
// have several, on storages of different kinds.
//
// Exactly one is primary, and it mirrors the DataStorageID, DataStorageType and Path
// columns on the product itself, which job launches and the content endpoint still
// read. Its id is the product's id: that keeps a product to a single primary row
// without a partial index, and lets the backfill of existing products run repeatedly
// without generating ids.
//
// DataStorageID carries no foreign key, for the reason the product's does not: it is
// qualified by a storage type, so a column cannot reference both storage tables. The
// storage services refuse to delete a storage a replica still names.
type DataProductReplica struct {
	ID string `gorm:"column:replica_id;primaryKey;type:varchar(36)" json:"replicaId"`

	DataProductID string `gorm:"column:data_product_id;type:varchar(36);not null;index" json:"dataProductId"`

	DataStorageID   string          `gorm:"column:data_storage_id;type:varchar(36);not null;index" json:"dataStorageId"`
	DataStorageType DataStorageType `gorm:"column:data_storage_type;type:varchar(32);not null" json:"dataStorageType"`

	// Path is a file path on an SCP storage and an object key on an S3 one.
	Path string `gorm:"column:path;type:varchar(2048);not null" json:"path"`

	IsPrimary bool `gorm:"column:is_primary;not null" json:"isPrimary"`

	CreatedAt int64 `gorm:"column:created_at;not null" json:"createdAt"`
}

// PrimaryReplicaOf returns the primary replica that mirrors p's own location, or nil
// when p names no location. Whatever writes a product's location writes this row too.
func PrimaryReplicaOf(p *DataProduct) *DataProductReplica {
	if p.DataStorageID == nil || p.Path == nil {
		return nil
	}
	return &DataProductReplica{
		ID:              p.ID,
		DataProductID:   p.ID,
		DataStorageID:   *p.DataStorageID,
		DataStorageType: p.DataStorageType,
		Path:            *p.Path,
		IsPrimary:       true,
		CreatedAt:       p.CreatedAt,
	}
}

// TableName returns the table backing DataProductReplica.
func (DataProductReplica) TableName() string { return "data_product_replicas" }

// BeforeCreate assigns a UUID when none was supplied.
func (r *DataProductReplica) BeforeCreate(*gorm.DB) error {
	if r.ID == "" {
		r.ID = uuid.NewString()
	}
	return nil
}
