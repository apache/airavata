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

// Package data holds the SCP data-registration entities.
package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	cred "github.com/apache/airavata/api/credentials/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

type DataStorageType string

const (
	DataStorageTypeSCP DataStorageType = "SCP"
	DataStorageTypeHPC DataStorageType = "HPC"
)

func (t DataStorageType) Valid() bool {
	switch t {
	case DataStorageTypeSCP, DataStorageTypeHPC:
		return true
	}
	return false
}

// SCPDataStorage is a host and account data products can be staged through.
//
// It names the host itself — the name and port to reach over SSH — and the account it
// is reached as: a login user and the key presented for it. The host and the account
// are spelled out here rather than pointed at catalogue entries, for the same reason a
// SlurmClusterConfig spells them out: a storage is self-service, and a catalogue only
// an admin can add to would mean asking an admin before registering one. Only the key
// stays a reference, because the private material has to live somewhere it is never
// read back.
//
// It belongs to whoever registered it, and everyone else reaches it through the
// sharing rows below. Ownership is not transferable through the API: products are
// registered against a storage by id, so handing one over would silently hand over
// the place every dataset on it lives.
type SCPDataStorage struct {
	ID   string  `gorm:"column:data_id;primaryKey;type:varchar(36)" json:"dataId"`
	Name *string `gorm:"column:data_name;type:varchar(255)" json:"dataName,omitempty"`

	HostName *string `gorm:"column:host_name;type:varchar(255)" json:"hostName,omitempty"`
	Port     *int    `gorm:"column:port;type:int" json:"port,omitempty"`

	LoginUser *string `gorm:"column:login_user;type:varchar(255)" json:"loginUser,omitempty"`

	SSHKeyID *string      `gorm:"column:ssh_key_id;type:varchar(36);index" json:"sshKeyId,omitempty"`
	SSHKey   *cred.SSHKey `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"sshKey,omitempty"`

	// OwnerID is named for its role because ownership, not mere reference, is what the
	// authorisation checks read. RESTRICT: a user who still owns storages cannot be
	// deleted out from under them.
	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"ownerId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
}

// TableName returns the table backing SCPDataStorage.
func (SCPDataStorage) TableName() string { return "scp_data_storages" }

// OwnedBy reports whether userID owns this storage. A storage with no owner is owned
// by nobody, so it must not match the empty principal name.
func (s *SCPDataStorage) OwnedBy(userID string) bool {
	return s.OwnerID != nil && *s.OwnerID == userID
}

// BeforeCreate assigns a UUID when none was supplied.
func (s *SCPDataStorage) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}
