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
	"regexp"
	"strings"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/permission"

	model "github.com/apache/airavata/api/iam/model"
)

// Token validity bounds, in days. A token always expires; the longest an admin may
// make one live is a year, after which it has to be renewed or regenerated.
const (
	DefaultServiceAccountValidityDays = 90
	MaxServiceAccountValidityDays     = 365
)

// serviceAccountName is the shape of a service account name: lower-case, starting
// with a letter or digit, 2 to 64 characters. It becomes part of the user id, so it
// is kept to characters that are safe in a URL path segment.
var serviceAccountName = regexp.MustCompile(`^[a-z0-9][a-z0-9._-]{1,63}$`)

// ServiceAccountCreate is the payload for creating a service account.
type ServiceAccountCreate struct {
	// Name becomes the account's user id as "sa:<name>".
	Name        string  `json:"name"`
	DisplayName *string `json:"displayName"`
	Description *string `json:"description"`
	Email       *string `json:"email"`

	// ValidityDays is how long the first token lives. Defaults to 90.
	ValidityDays *int `json:"validityDays"`

	// Permissions is what the account may do. Empty means it can act only on its own
	// records and what is shared with it.
	Permissions []string `json:"permissions"`
}

// Validate implements httpx.Validator.
func (r *ServiceAccountCreate) Validate() []httpx.FieldError {
	var c httpx.Constraints
	if !serviceAccountName.MatchString(r.Name) {
		c.Add("name", "Name must be 2-64 characters of lower-case letters, digits, '.', '_' or '-', starting with a letter or digit")
	}
	c.Email("email", "Email should be valid", r.Email)
	validateDescription(&c, r.Description)
	validateValidity(&c, r.ValidityDays)
	validatePermissions(&c, r.Permissions)
	return c.Fields()
}

// ServiceAccountUpdate changes an account's profile, status or permissions. Absent
// fields are left as they are.
type ServiceAccountUpdate struct {
	DisplayName *string           `json:"displayName"`
	Description *string           `json:"description"`
	Email       *string           `json:"email"`
	Status      *model.UserStatus `json:"status"`

	// Permissions, when present, replaces the account's whole set; an empty list
	// removes every grant. Absent leaves the set as it is.
	Permissions *[]string `json:"permissions"`
}

// Validate implements httpx.Validator.
func (r *ServiceAccountUpdate) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.Email("email", "Email should be valid", r.Email)
	validateDescription(&c, r.Description)
	if r.Status != nil && !r.Status.Valid() {
		c.Add("status", "Status must be one of ACTIVE, INACTIVE, SUSPENDED")
	}
	if r.Permissions != nil {
		validatePermissions(&c, *r.Permissions)
	}
	return c.Fields()
}

// ServiceAccountTokenRequest is the payload for renewing or regenerating a token.
type ServiceAccountTokenRequest struct {
	// ValidityDays is how long the token lives from now. Defaults to 90.
	ValidityDays *int `json:"validityDays"`
}

// Validate implements httpx.Validator.
func (r *ServiceAccountTokenRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	validateValidity(&c, r.ValidityDays)
	return c.Fields()
}

// ValidityDaysOrDefault returns v, or the default when v is nil.
func ValidityDaysOrDefault(v *int) int {
	if v == nil {
		return DefaultServiceAccountValidityDays
	}
	return *v
}

func validateValidity(c *httpx.Constraints, v *int) {
	if v != nil && (*v < 1 || *v > MaxServiceAccountValidityDays) {
		c.Add("validityDays", "Validity must be between 1 and 365 days")
	}
}

// validatePermissions rejects a permission that does not exist, and one that a service
// account may never hold, naming it so the caller can see which.
func validatePermissions(c *httpx.Constraints, ps []string) {
	for _, p := range ps {
		perm := permission.Permission(p)
		switch {
		case !perm.Valid():
			c.Add("permissions", "Unknown permission: "+p)
		case !perm.GrantableToServiceAccount():
			c.Add("permissions", "Permission "+p+" cannot be granted to a service account")
		}
	}
}

// ToPermissions converts validated names to permissions, dropping duplicates.
func ToPermissions(ps []string) []permission.Permission {
	seen := make(map[permission.Permission]bool, len(ps))
	out := make([]permission.Permission, 0, len(ps))
	for _, p := range ps {
		perm := permission.Permission(p)
		if !seen[perm] {
			seen[perm] = true
			out = append(out, perm)
		}
	}
	return out
}

func validateDescription(c *httpx.Constraints, d *string) {
	if d != nil && len(*d) > 1024 {
		c.Add("description", "Description must be at most 1024 characters")
	}
}

// ServiceAccountResponse is the read model for a service account. It never carries
// the token.
type ServiceAccountResponse struct {
	ServiceAccountID string            `json:"serviceAccountId"`
	Name             string            `json:"name"`
	DisplayName      *string           `json:"displayName"`
	Description      *string           `json:"description"`
	Email            *string           `json:"email"`
	Status           *model.UserStatus `json:"status"`
	CreatedBy        string            `json:"createdBy"`
	CreatedAt        int64             `json:"createdAt"`
	TokenIssuedAt    int64             `json:"tokenIssuedAt"`
	TokenExpiresAt   int64             `json:"tokenExpiresAt"`

	// Permissions is what the account is allowed to do, sorted.
	Permissions []string `json:"permissions"`
}

// ServiceAccountTokenResponse is returned when a token is minted: on create and on
// regenerate. It is the only time the token is ever sent.
type ServiceAccountTokenResponse struct {
	ServiceAccount ServiceAccountResponse `json:"serviceAccount"`
	Token          string                 `json:"token"`
}

// ToServiceAccountResponse maps a service account user, which must have its
// ServiceAccount row loaded.
func ToServiceAccountResponse(u *model.User) ServiceAccountResponse {
	out := ServiceAccountResponse{
		ServiceAccountID: u.ID,
		Name:             strings.TrimPrefix(u.ID, auth.ServiceAccountIDPrefix),
		DisplayName:      u.FirstName,
		Email:            u.Email,
		Status:           u.Status,
		CreatedAt:        u.CreatedAt,
		Permissions:      []string{},
	}
	if sa := u.ServiceAccount; sa != nil {
		out.Description = sa.Description
		out.CreatedBy = sa.CreatedBy
		out.TokenIssuedAt = sa.TokenIssuedAt
		out.TokenExpiresAt = sa.TokenExpiresAt
		set := make(permission.Set, len(sa.Permissions))
		for _, p := range sa.Permissions {
			set[p.Permission] = struct{}{}
		}
		out.Permissions = set.Sorted()
	}
	return out
}
