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
	"github.com/apache/airavata/internal/ptr"

	creddto "github.com/apache/airavata/api/credentials/dto"
	model "github.com/apache/airavata/api/data/model"
)

// defaultSSHPort is used when a request omits the port, so the common case of a
// standard SSH host does not have to state it.
const defaultSSHPort = 22

// SCPDataStorageRequest is the create/update payload for a storage.
//
// There is no owner field: ownership comes from the access token and is immutable, so
// a storage can neither be registered on someone else's behalf nor handed over by
// editing it.
type SCPDataStorageRequest struct {
	DataName *string `json:"dataName"`

	HostName string `json:"hostName"`
	Port     *int   `json:"port"`

	LoginUser string `json:"loginUser"`

	SSHKeyID string `json:"sshKeyId"`
}

// Validate implements httpx.Validator.
func (r *SCPDataStorageRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlankPtr("dataName", "Data name cannot be blank", r.DataName)
	c.NotBlank("hostName", "Host name cannot be blank", r.HostName)
	c.NotBlank("loginUser", "Login user cannot be blank", r.LoginUser)
	c.NotBlank("sshKeyId", "SSH key id cannot be blank", r.SSHKeyID)
	if r.Port != nil && (*r.Port < 1 || *r.Port > 65535) {
		c.Add("port", "Port must be between 1 and 65535")
	}
	return c.Fields()
}

// ApplySCPDataStorageRequest copies the mutable fields of a request onto an entity.
// The key is resolved by the service, which is what turns an unknown id into a 404
// rather than a dangling reference.
//
// An omitted port means 22 rather than 0 — the zero value would be a port nothing can
// connect to, which is worse than a default.
func ApplySCPDataStorageRequest(dst *model.SCPDataStorage, src *SCPDataStorageRequest) {
	dst.Name = src.DataName
	dst.HostName = ptr.To(src.HostName)
	dst.LoginUser = ptr.To(src.LoginUser)
	dst.Port = ptr.To(ptr.FromOr(src.Port, defaultSSHPort))
}

// SCPDataStorageResponse is the read model for a storage.
//
// The host and the account it is reached as are spelled out on the storage itself; the
// key is inlined as its safe summary — name and public key, never the private
// material, which the credential response type has no field for at all.
type SCPDataStorageResponse struct {
	DataID   string  `json:"dataId"`
	DataName *string `json:"dataName"`
	OwnerID  *string `json:"ownerId"`

	HostName *string `json:"hostName"`
	Port     *int    `json:"port"`

	LoginUser *string `json:"loginUser"`

	SSHKeyID *string                 `json:"sshKeyId"`
	SSHKey   *creddto.SSHKeyResponse `json:"sshKey"`

	Permission *string `json:"permission,omitempty"`
}

func ToSCPDataStorageResponse(s *model.SCPDataStorage) SCPDataStorageResponse {
	out := SCPDataStorageResponse{
		DataID:    s.ID,
		DataName:  s.Name,
		OwnerID:   s.OwnerID,
		HostName:  s.HostName,
		Port:      s.Port,
		LoginUser: s.LoginUser,
		SSHKeyID:  s.SSHKeyID,
	}
	if s.SSHKey != nil {
		key := creddto.ToSSHKeyResponse(s.SSHKey)
		out.SSHKey = &key
	}
	return out
}

// ToSCPDataStorageResponseWith is ToSCPDataStorageResponse with the caller's effective
// permission attached.
func ToSCPDataStorageResponseWith(s *model.SCPDataStorage, permission string) SCPDataStorageResponse {
	out := ToSCPDataStorageResponse(s)
	out.Permission = &permission
	return out
}

func ToSCPDataStorageResponses(in []model.SCPDataStorage) []SCPDataStorageResponse {
	out := make([]SCPDataStorageResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSCPDataStorageResponse(&in[i]))
	}
	return out
}
