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
	iam "github.com/apache/airavata/api/iam/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// S3Credential is a registered access key for an S3-compatible object store (AWS S3,
// MinIO, Ceph and the like).
//
// SecretAccessKey and SessionToken are secrets: they are stored here but deliberately
// have no field on the read-side response DTO, so they cannot leak through a GET.
// Update paths must treat a blank incoming value as "unchanged" rather than "erase" —
// see ptr.NonBlank. AccessKeyID is an identifier rather than a secret and is returned.
//
// Like an SSHCredential it belongs to whoever registered it and there is no sharing
// model to reach it by. Which bucket it opens, and at which endpoint, is the business of
// the storage that presents it.
type S3Credential struct {
	ID string `gorm:"column:s3_credential_id;primaryKey;type:varchar(36)" json:"s3CredentialId"`

	Name        string `gorm:"column:name;type:varchar(255);not null" json:"name"`
	AccessKeyID string `gorm:"column:access_key_id;type:varchar(255);not null" json:"accessKeyId"`

	SecretAccessKey string `gorm:"column:secret_access_key;type:text;not null" json:"-"`

	// SessionToken is set only for temporary credentials (for example from STS).
	SessionToken *string `gorm:"column:session_token;type:text" json:"-"`

	// varchar(255) to match users.user_id. RESTRICT, so a user who still owns
	// credentials cannot be deleted out from under them.
	OwnerID string    `gorm:"column:owner_id;type:varchar(255);not null;index" json:"ownerId"`
	Owner   *iam.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
}

// OwnedBy reports whether userID owns this credential.
func (c *S3Credential) OwnedBy(userID string) bool {
	return c.OwnerID != "" && c.OwnerID == userID
}

// TableName returns the table backing S3Credential.
func (S3Credential) TableName() string { return "s3_credentials" }

// BeforeCreate assigns a UUID when none was supplied.
func (c *S3Credential) BeforeCreate(*gorm.DB) error {
	if c.ID == "" {
		c.ID = uuid.NewString()
	}
	return nil
}
