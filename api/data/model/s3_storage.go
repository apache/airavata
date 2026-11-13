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

	cred "github.com/apache/airavata/api/credentials/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// S3DataStorage is a bucket on an S3-compatible object store (AWS S3, MinIO, Ceph and
// the like) that data products can live in.
//
// It is the object-store counterpart of SCPDataStorage and follows the same rules: the
// place itself — endpoint, region, bucket and addressing style — is spelled out here,
// and only the credential stays a reference, because its secret has to live somewhere
// it is never read back. It belongs to whoever registered it and everyone else reaches
// it through sharing rows.
//
// Clusters never see the credential: data on an S3 storage is staged by Airavata
// itself, which pulls the object and pushes it to the cluster over SCP.
type S3DataStorage struct {
	ID   string  `gorm:"column:data_id;primaryKey;type:varchar(36)" json:"dataId"`
	Name *string `gorm:"column:data_name;type:varchar(255)" json:"dataName,omitempty"`

	// EndpointURL is the service to talk to, such as https://minio.example.edu:9000.
	// Nil means the AWS S3 endpoint for Region.
	EndpointURL *string `gorm:"column:endpoint_url;type:varchar(2048)" json:"endpointUrl,omitempty"`
	Region      string  `gorm:"column:region;type:varchar(64);not null" json:"region"`
	BucketName  string  `gorm:"column:bucket_name;type:varchar(63);not null" json:"bucketName"`

	// PathStyle addresses the bucket as endpoint/bucket rather than bucket.endpoint.
	// Most self-hosted stores (MinIO, Ceph) need it.
	PathStyle bool `gorm:"column:path_style;not null;default:false" json:"pathStyle"`

	S3CredentialID *string            `gorm:"column:s3_credential_id;type:varchar(36);index" json:"s3CredentialId,omitempty"`
	S3Credential   *cred.S3Credential `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// RESTRICT: a user who still owns storages cannot be deleted out from under them.
	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"ownerId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
}

// TableName returns the table backing S3DataStorage.
func (S3DataStorage) TableName() string { return "s3_data_storages" }

// OwnedBy reports whether userID owns this storage. A storage with no owner is owned
// by nobody, so it must not match the empty principal name.
func (s *S3DataStorage) OwnedBy(userID string) bool {
	return s.OwnerID != nil && *s.OwnerID == userID
}

// BeforeCreate assigns a UUID when none was supplied.
func (s *S3DataStorage) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}
