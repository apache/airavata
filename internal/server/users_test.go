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
package server_test

import (
	"context"
	"net/http"
	"testing"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/role"

	iammodel "github.com/apache/airavata/api/iam/model"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// recordIntrospector resolves the stub principals, then reads each one's status from
// the users table the way the CILogon introspector does, so a status change takes
// effect on the next request.
type recordIntrospector struct {
	stub  stubIntrospector
	users *iamrepo.CILogonUserProvisioner
}

func (r recordIntrospector) Introspect(ctx context.Context, token string) (*auth.Principal, error) {
	p, err := r.stub.Introspect(ctx, token)
	if err != nil {
		return nil, err
	}
	copied := *p
	status, err := r.users.ProvisionUser(ctx, &copied)
	if err != nil {
		return nil, err
	}
	copied.Status = status
	return &copied, nil
}

const (
	tokenNewcomer = "token-newcomer"
	tokenSuper    = "token-super"
)

func (h *harness) withRecordedStatuses() {
	h.t.Helper()
	if err := h.db.Create(&iammodel.User{ID: "super-user", CreatedAt: 1}).Error; err != nil {
		h.t.Fatalf("seed super-user: %v", err)
	}
	if err := h.db.Create(&iammodel.UserRole{UserID: "super-user", Role: role.SuperAdmin}).Error; err != nil {
		h.t.Fatalf("seed super-user role: %v", err)
	}
	h.withIntrospector(recordIntrospector{
		stub: stubIntrospector{
			tokenAdmin:    {Name: "admin-user", Authorities: []string{string(role.Admin)}},
			tokenSuper:    {Name: "super-user", Authorities: []string{string(role.SuperAdmin)}},
			tokenAlice:    {Name: "alice", Authorities: []string{string(role.User)}},
			tokenNewcomer: {Name: "cilogon:777", Authorities: []string{string(role.User)}},
		},
		users: &iamrepo.CILogonUserProvisioner{DB: h.db},
	})
}

// A first sign-in creates a suspended account, which can only ask about itself until
// an administrator activates it.
func TestNewUserIsSuspendedUntilAnAdminActivatesThem(t *testing.T) {
	h := newHarness(t)
	h.withRecordedStatuses()

	me := h.mustDo(http.MethodGet, "/api/v1/users/me", tokenNewcomer, nil, http.StatusOK)
	if me["status"] != "SUSPENDED" || me["admin"] != false {
		t.Fatalf("me = %v, want a SUSPENDED non-admin", me)
	}
	if rec := h.do(http.MethodGet, "/api/v1/processes/me", tokenNewcomer, nil); rec.Code != http.StatusForbidden {
		t.Fatalf("suspended caller: status = %d, want 403\nbody: %s", rec.Code, rec.Body.String())
	}

	users := h.list("/api/v1/users", tokenAdmin)
	var found map[string]any
	for _, u := range users {
		if u["userId"] == "cilogon:777" {
			found = u
		}
	}
	if found == nil || found["status"] != "SUSPENDED" || found["authMethod"] != "CILOGON" {
		t.Fatalf("listing = %v, want the newcomer listed as a SUSPENDED CILogon user", users)
	}

	h.mustDo(http.MethodPost, "/api/v1/users/cilogon:777/status", tokenAdmin,
		map[string]any{"status": "ACTIVE"}, http.StatusOK)
	h.mustDo(http.MethodGet, "/api/v1/processes/me", tokenNewcomer, nil, http.StatusOK)

	h.mustDo(http.MethodPost, "/api/v1/users/cilogon:777/status", tokenAdmin,
		map[string]any{"status": "SUSPENDED"}, http.StatusOK)
	if rec := h.do(http.MethodGet, "/api/v1/processes/me", tokenNewcomer, nil); rec.Code != http.StatusForbidden {
		t.Fatalf("re-suspended caller: status = %d, want 403", rec.Code)
	}
}

func TestOnlyAdminsChangeUserStatus(t *testing.T) {
	h := newHarness(t)
	h.withRecordedStatuses()
	h.mustDo(http.MethodGet, "/api/v1/users/me", tokenNewcomer, nil, http.StatusOK)

	cases := []struct {
		name, token, user, status string
		want                      int
	}{
		{"a user cannot", tokenAlice, "cilogon:777", "ACTIVE", http.StatusForbidden},
		{"nobody changes their own", tokenAdmin, "admin-user", "SUSPENDED", http.StatusForbidden},
		{"an admin cannot touch a super admin", tokenAdmin, "super-user", "SUSPENDED", http.StatusForbidden},
		{"a super admin can touch an admin", tokenSuper, "admin-user", "INACTIVE", http.StatusOK},
		{"an unknown status is refused", tokenSuper, "alice", "BANNED", http.StatusBadRequest},
		{"an unknown user is not found", tokenSuper, "nobody", "ACTIVE", http.StatusNotFound},
	}
	for _, c := range cases {
		t.Run(c.name, func(t *testing.T) {
			rec := h.do(http.MethodPost, "/api/v1/users/"+c.user+"/status", c.token, map[string]any{"status": c.status})
			if rec.Code != c.want {
				t.Errorf("status = %d, want %d\nbody: %s", rec.Code, c.want, rec.Body.String())
			}
		})
	}
}
