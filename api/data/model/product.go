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

	iammodel "github.com/apache/airavata/api/iam/model"
)

// ProvisionStatus is the lifecycle state of a registered dataset.
type ProvisionStatus string

const (
	ProvisionStatusRegistered     ProvisionStatus = "REGISTERD"
	ProvisionStatusProvisioning   ProvisionStatus = "PROVISIONING"
	ProvisionStatusProvisioned    ProvisionStatus = "PROVISIONED"
	ProvisionStatusDeprovisioning ProvisionStatus = "DEPROVISIONING"
	ProvisionStatusDeprovisioned  ProvisionStatus = "DEPROVISIONED"
	ProvisionStatusFailed         ProvisionStatus = "FAILED"
)

// Valid reports whether s is a recognised ProvisionStatus.
func (s ProvisionStatus) Valid() bool {
	switch s {
	case ProvisionStatusRegistered, ProvisionStatusProvisioning, ProvisionStatusProvisioned,
		ProvisionStatusDeprovisioning, ProvisionStatusDeprovisioned, ProvisionStatusFailed:
		return true
	}
	return false
}

type DataProduct struct {
	ID string `gorm:"column:data_id;primaryKey;type:varchar(36)" json:"dataId"`

	DataName        *string `gorm:"column:data_name;type:varchar(255)" json:"dataName,omitempty"`
	DataDescription *string `gorm:"column:data_description;type:varchar(2048)" json:"dataDescription,omitempty"`

	IsFile bool `gorm:"column:is_file;not null" json:"isFile"`

	Path *string `gorm:"column:path;type:varchar(2048)" json:"path,omitempty"`

	ProvisionStatus *ProvisionStatus `gorm:"column:provision_status;type:varchar(32)" json:"provisionStatus,omitempty"`

	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"ownerId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	DataStorageID   *string         `gorm:"column:data_storage_id;type:varchar(36);index" json:"dataStorageId,omitempty"`
	DataStorageType DataStorageType `gorm:"column:data_storage_type;type:varchar(32)" json:"dataStorageType,omitempty"`

	CreatedAt int64 `gorm:"column:created_at;not null" json:"createdAt"`
}

// TableName returns the table backing DataProduct.
func (DataProduct) TableName() string { return "data_products" }

// BeforeCreate assigns a UUID when none was supplied.
func (p *DataProduct) BeforeCreate(*gorm.DB) error {
	if p.ID == "" {
		p.ID = uuid.NewString()
	}
	return nil
}

// OwnedBy reports whether userID owns this product. A product with no owner is owned
// by nobody, so it must not match the empty principal name.
func (p *DataProduct) OwnedBy(userID string) bool {
	return p.OwnerID != nil && *p.OwnerID == userID
}
