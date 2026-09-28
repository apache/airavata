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

// Package model holds the SSH key entity.
package model

import (
	iam "github.com/apache/airavata/api/iam/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// SSHKey is a registered SSH keypair.
//
// PrivateKey and Passphrase are secrets: they are stored here but deliberately have
// no field on the read-side response DTO, so they cannot leak through a GET. Update
// paths must treat a blank incoming value as "unchanged" rather than "erase" — see
// ptr.NonBlank.
//
// It belongs to whoever registered it, and — unlike every other owned record here —
// there is no sharing model to reach it by. A key is the credential itself rather than
// something reached with one, so a cluster config or a data storage may only present a
// key its own owner registered. Sharing the config is how someone else submits under
// it; the key never leaves its owner.
//
// Java: org.apache.airavata.credentials.model.SSHKeyEntity
type SSHKey struct {
	ID string `gorm:"column:ssh_key_id;primaryKey;type:varchar(36)" json:"sshKeyId"`

	SSHKeyName string `gorm:"column:ssh_key_name;type:varchar(255);not null" json:"sshKeyName"`

	// @Lob in Java: keys are far longer than a default varchar.
	PublicKey  string `gorm:"column:public_key;type:text;not null" json:"publicKey"`
	PrivateKey string `gorm:"column:private_key;type:text;not null" json:"-"`

	Passphrase *string `gorm:"column:passphrase;type:varchar(255)" json:"-"`

	// OwnerID is varchar(255) to match users.user_id: a CILogon subject is far longer
	// than a UUID. RESTRICT, so a user who still owns keys cannot be deleted out from
	// under them.
	OwnerID string    `gorm:"column:owner_id;type:varchar(255);not null;index" json:"ownerId"`
	Owner   *iam.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
}

// OwnedBy reports whether userID owns this key. A key with no owner is owned by
// nobody, so it must not match the empty principal name.
func (k *SSHKey) OwnedBy(userID string) bool {
	return k.OwnerID != "" && k.OwnerID == userID
}

// TableName returns the table backing SSHKey.
func (SSHKey) TableName() string { return "ssh_keys" }

// BeforeCreate assigns a UUID when none was supplied, replacing Hibernate's
// @GeneratedValue(strategy = GenerationType.UUID).
func (k *SSHKey) BeforeCreate(*gorm.DB) error {
	if k.ID == "" {
		k.ID = uuid.NewString()
	}
	return nil
}
