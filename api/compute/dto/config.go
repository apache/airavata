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

	model "github.com/apache/airavata/api/compute/model"
	creddto "github.com/apache/airavata/api/credentials/dto"
)

// SlurmClusterConfigRequest is the create/update payload for a cluster login config.
//
// There is no owner field: ownership comes from the access token and is immutable, so
// a config can neither be registered on someone else's behalf nor handed over by
// editing it.
type SlurmClusterConfigRequest struct {
	Name        *string `json:"name"`
	Description *string `json:"description"`

	SlurmClusterID string `json:"slurmClusterId"`

	LoginUser string `json:"loginUser"`
	WorkRoot  string `json:"workRoot"`

	SSHKeyID string `json:"sshKeyId"`
}

// Validate implements httpx.Validator.
func (r *SlurmClusterConfigRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("slurmClusterId", "Slurm cluster id cannot be blank", r.SlurmClusterID)
	c.NotBlank("loginUser", "Login user cannot be blank", r.LoginUser)
	c.NotBlank("workRoot", "Work root cannot be blank", r.WorkRoot)
	c.NotBlank("sshKeyId", "SSH key id cannot be blank", r.SSHKeyID)
	return c.Fields()
}

// ApplySlurmClusterConfigRequest copies the mutable fields of a request onto an entity.
// The cluster and the key are resolved by the service, which is what turns an unknown
// id into a 404 rather than a dangling reference.
func ApplySlurmClusterConfigRequest(dst *model.SlurmClusterConfig, src *SlurmClusterConfigRequest) {
	dst.Name = src.Name
	dst.Description = src.Description
	dst.LoginUser = src.LoginUser
	dst.WorkRoot = src.WorkRoot
}

// SlurmClusterConfigResponse is the read model for a cluster login config.
//
// The cluster is inlined because a config is only meaningful together with the machine
// it logs in to. The key is inlined as its safe summary — name and public key, never
// the private material, which the credential response type has no field for at all.
type SlurmClusterConfigResponse struct {
	SlurmClusterConfigID string  `json:"slurmClusterConfigId"`
	Name                 *string `json:"name"`
	Description          *string `json:"description"`
	OwnerID              *string `json:"ownerId"`

	SlurmClusterID string                `json:"slurmClusterId"`
	SlurmCluster   *SlurmClusterResponse `json:"slurmCluster"`

	LoginUser string `json:"loginUser"`
	WorkRoot  string `json:"workRoot"`

	SSHKeyID *string                 `json:"sshKeyId"`
	SSHKey   *creddto.SSHKeyResponse `json:"sshKey"`

	Permission *string `json:"permission,omitempty"`
}

func ToSlurmClusterConfigResponse(c *model.SlurmClusterConfig) SlurmClusterConfigResponse {
	out := SlurmClusterConfigResponse{
		SlurmClusterConfigID: c.ID,
		Name:                 c.Name,
		Description:          c.Description,
		OwnerID:              c.OwnerID,
		SlurmClusterID:       c.SlurmClusterID,
		LoginUser:            c.LoginUser,
		WorkRoot:             c.WorkRoot,
		SSHKeyID:             c.SSHKeyID,
	}
	if c.SlurmCluster != nil {
		cluster := ToSlurmClusterResponse(c.SlurmCluster)
		out.SlurmCluster = &cluster
	}
	if c.SSHKey != nil {
		key := creddto.ToSSHKeyResponse(c.SSHKey)
		out.SSHKey = &key
	}
	return out
}

// ToSlurmClusterConfigResponseWith is ToSlurmClusterConfigResponse with the caller's
// effective permission attached.
func ToSlurmClusterConfigResponseWith(c *model.SlurmClusterConfig, permission string) SlurmClusterConfigResponse {
	out := ToSlurmClusterConfigResponse(c)
	out.Permission = &permission
	return out
}

func ToSlurmClusterConfigResponses(in []model.SlurmClusterConfig) []SlurmClusterConfigResponse {
	out := make([]SlurmClusterConfigResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSlurmClusterConfigResponse(&in[i]))
	}
	return out
}
