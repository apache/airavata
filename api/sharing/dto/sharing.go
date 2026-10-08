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

// Package dto is the wire shape of a grant: one request body for every shareable
// record, with the subject's kind a field rather than a choice of endpoint.
package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/sharing/model"
)

// ShareRequest grants one principal access to the resource named in the path.
//
// There is no resource field: the path says what is being shared, so a request cannot
// name one resource in the URL and open up another in the body. PrincipalType is what
// used to be the choice between two endpoints.
type ShareRequest struct {
	PrincipalType *model.PrincipalType    `json:"principalType"`
	PrincipalID   string                  `json:"principalId"`
	Permission    *model.AccessPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *ShareRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotNil("principalType", "Principal type cannot be null", r.PrincipalType)
	if r.PrincipalType != nil && !r.PrincipalType.Valid() {
		c.Add("principalType", "Principal type must be one of USER, GROUP")
	}
	c.NotBlank("principalId", "Principal id cannot be blank", r.PrincipalID)
	validateGrant(&c, r.Permission)
	return c.Fields()
}

// Subject returns the principal type being granted to. Validation has already rejected
// a nil or unrecognised one.
func (r *ShareRequest) Subject() model.PrincipalType {
	if r.PrincipalType == nil {
		return ""
	}
	return *r.PrincipalType
}

// Grant returns the permission to store, defaulting to READ. Read-only is the safe
// default for a share: widening it is a deliberate act.
func (r *ShareRequest) Grant() model.AccessPermission {
	return grantOrRead(r.Permission)
}

// ShareUpdate changes what an existing share grants. The subject is fixed at creation;
// only the permission is editable, because moving a share to a different principal
// would be a revoke and a grant wearing one id.
type ShareUpdate struct {
	Permission *model.AccessPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *ShareUpdate) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotNil("permission", "Permission cannot be null", r.Permission)
	validateGrant(&c, r.Permission)
	return c.Fields()
}

// Grant returns the permission to store.
func (r *ShareUpdate) Grant() model.AccessPermission {
	return grantOrRead(r.Permission)
}

// ShareResponse is the read model for one share.
//
// The resource is echoed back even though the caller named it in the path: a listing
// is a set of rows, and a row that did not say what it opened up would be ambiguous
// once it left the response it arrived in.
type ShareResponse struct {
	SharingID     string                 `json:"resourceSharingId"`
	ResourceType  model.ResourceType     `json:"resourceType"`
	ResourceID    string                 `json:"resourceId"`
	PrincipalType model.PrincipalType    `json:"principalType"`
	PrincipalID   string                 `json:"principalId"`
	Permission    model.AccessPermission `json:"permission"`
}

func ToShareResponse(s *model.Sharing) ShareResponse {
	return ShareResponse{
		SharingID:     s.ID,
		ResourceType:  s.ResourceType,
		ResourceID:    s.ResourceID,
		PrincipalType: s.PrincipalType,
		PrincipalID:   s.PrincipalID,
		Permission:    s.Permission,
	}
}

func ToShareResponses(in []model.Sharing) []ShareResponse {
	out := make([]ShareResponse, 0, len(in))
	for i := range in {
		out = append(out, ToShareResponse(&in[i]))
	}
	return out
}

// validateGrant rejects a permission the resolver would not understand. The empty
// string is a recognised constant — it is what "grants nothing" resolves to — but it
// is not something a caller may ask to store.
func validateGrant(c *httpx.Constraints, p *model.AccessPermission) {
	if p == nil {
		return
	}
	if !p.Valid() || *p == model.AccessPermissionNone {
		c.Add("permission", "Permission must be one of READ, WRITE")
	}
}

func grantOrRead(p *model.AccessPermission) model.AccessPermission {
	if p == nil {
		return model.AccessPermissionRead
	}
	return *p
}
