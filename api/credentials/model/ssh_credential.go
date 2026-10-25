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

// Package model holds the SSH credential entity.
package model

import (
	iam "github.com/apache/airavata/api/iam/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// SSHCredential is a registered SSH credential.
//
// PrivateKey and Passphrase are secrets: they are stored here but deliberately have
// no field on the read-side response DTO, so they cannot leak through a GET. Update
// paths must treat a blank incoming value as "unchanged" rather than "erase" — see
// ptr.NonBlank.
//
// It belongs to whoever registered it, and — unlike every other owned record here —
// there is no sharing model to reach it by. A credential is the secret material itself
// rather than something reached with it, so a cluster config or a data storage may only
// present a credential its own owner registered. Sharing the config is how someone else
// submits under it; the credential never leaves its owner.
type SSHCredential struct {
	ID string `gorm:"column:ssh_credential_id;primaryKey;type:varchar(36)" json:"sshCredentialId"`

	SSHCredentialName string `gorm:"column:ssh_credential_name;type:varchar(255);not null" json:"sshCredentialName"`

	PublicKey  string  `gorm:"column:public_key;type:text;not null" json:"publicKey"`
	PrivateKey string  `gorm:"column:private_key;type:text;not null" json:"-"`
	Passphrase *string `gorm:"column:passphrase;type:varchar(255)" json:"-"`

	// Certificate based authentication. This indicates whether the SSH credential uses a certificate for authentication.
	IsCertificate bool `gorm:"column:is_certificate;not null" json:"isCertificate"`

	// FullInteractive indicates whether the SSH requires full interactive authentication.
	FullInteractive bool `gorm:"column:full_interactive;not null" json:"fullInteractive"`

	// OwnerID is varchar(255) to match users.user_id: a CILogon subject is far longer
	// than a UUID. RESTRICT, so a user who still owns keys cannot be deleted out from
	// under them.
	OwnerID string    `gorm:"column:owner_id;type:varchar(255);not null;index" json:"ownerId"`
	Owner   *iam.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
}

func (k *SSHCredential) OwnedBy(userID string) bool {
	return k.OwnerID != "" && k.OwnerID == userID
}

// TableName returns the table backing SSHCredential.
func (SSHCredential) TableName() string { return "ssh_credentials" }

// BeforeCreate assigns a UUID when none was supplied, replacing Hibernate's
// @GeneratedValue(strategy = GenerationType.UUID).
func (k *SSHCredential) BeforeCreate(*gorm.DB) error {
	if k.ID == "" {
		k.ID = uuid.NewString()
	}
	return nil
}
