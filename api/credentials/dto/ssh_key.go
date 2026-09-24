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

// SSHKeyRequest is the create/update payload for a key.
//
// PrivateKey is optional on the type because the same payload serves both create and
// update: creation requires it, while an update that omits it keeps the stored secret.
// That split cannot be expressed as a field constraint, so the service enforces it.
//
// Java: org.apache.airavata.credentials.dto.SSHKeyRequestDto
type SSHKeyRequest struct {
	SSHKeyName string  `json:"sshKeyName"`
	PublicKey  string  `json:"publicKey"`
	PrivateKey *string `json:"privateKey"`
	Passphrase *string `json:"passphrase"`
}

// Validate implements httpx.Validator.
func (r *SSHKeyRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("sshKeyName", "SSH key name cannot be blank", r.SSHKeyName)
	c.NotBlank("publicKey", "Public key cannot be blank", r.PublicKey)
	return c.Fields()
}

// SSHKeyResponse is the read model for a key.
//
// It has no private key or passphrase field at all. That is the containment: the
// secrets cannot leak through this endpoint because there is nowhere for them to go.
//
// There is no owner field on the request side: ownership comes from the access token
// and is immutable, so a key can neither be registered on someone else's behalf nor
// handed over by editing it.
//
// Java: org.apache.airavata.credentials.dto.SSHKeyResponseDto
type SSHKeyResponse struct {
	SSHKeyID   string `json:"sshKeyId"`
	SSHKeyName string `json:"sshKeyName"`
	PublicKey  string `json:"publicKey"`
	OwnerID    string `json:"ownerId"`
}

func ToSSHKeyResponse(k *model.SSHKey) SSHKeyResponse {
	return SSHKeyResponse{
		SSHKeyID:   k.ID,
		SSHKeyName: k.SSHKeyName,
		PublicKey:  k.PublicKey,
		OwnerID:    k.OwnerID,
	}
}
