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

package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/credentials/model"
)

// S3CredentialRequest is the create/update payload for an S3 access key.
//
// SecretAccessKey is optional on the type for the same reason
// SSHCredentialRequest.PrivateKey is: creation requires it, while an update that omits
// it keeps the stored secret. The service enforces that split.
type S3CredentialRequest struct {
	Name            string  `json:"name"`
	AccessKeyID     string  `json:"accessKeyId"`
	SecretAccessKey *string `json:"secretAccessKey"`
	SessionToken    *string `json:"sessionToken"`
}

// Validate implements httpx.Validator.
func (r *S3CredentialRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("name", "Name cannot be blank", r.Name)
	c.NotBlank("accessKeyId", "Access key ID cannot be blank", r.AccessKeyID)
	return c.Fields()
}

// S3CredentialResponse is the read model for an S3 access key.
//
// It has no secret access key or session token field at all, so neither can leak
// through a read. The access key ID is an identifier, not a secret, and is returned so
// a caller can tell their credentials apart.
type S3CredentialResponse struct {
	S3CredentialID string `json:"s3CredentialId"`
	Name           string `json:"name"`
	AccessKeyID    string `json:"accessKeyId"`
	OwnerID        string `json:"ownerId"`
}

func ToS3CredentialResponse(c *model.S3Credential) S3CredentialResponse {
	return S3CredentialResponse{
		S3CredentialID: c.ID,
		Name:           c.Name,
		AccessKeyID:    c.AccessKeyID,
		OwnerID:        c.OwnerID,
	}
}
