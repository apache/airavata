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
	"encoding/json"
	"net/http"
	"testing"
	"time"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/role"

	iammodel "github.com/apache/airavata/api/iam/model"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// serviceAccountIntrospector routes service account tokens to the real database
// lookup and everything else to the harness's stub, as the CILogon introspector does.
type serviceAccountIntrospector struct {
	stub   auth.Introspector
	lookup auth.ServiceAccountResolver
}

func (s serviceAccountIntrospector) Introspect(ctx context.Context, token string) (*auth.Principal, error) {
	if auth.IsServiceAccountToken(token) {
		return s.lookup.ResolveServiceAccount(ctx, token)
	}
	return s.stub.Introspect(ctx, token)
}

func newServiceAccountHarness(t *testing.T) *harness {
	t.Helper()
	h := newHarness(t)
	h.withIntrospector(serviceAccountIntrospector{
		stub:   h.introspector,
		lookup: iamrepo.ServiceAccountTokenLookup{DB: h.db},
	})
	return h
}

// createServiceAccount creates one as the admin and returns its id and token.
func (h *harness) createServiceAccount(name string) (id, token string) {
	h.t.Helper()
	out := h.mustDo(http.MethodPost, "/api/v1/service-accounts", tokenAdmin,
		map[string]any{"name": name, "displayName": "CI pipeline", "description": "nightly runs"},
		http.StatusCreated)
	sa := out["serviceAccount"].(map[string]any)
	return sa["serviceAccountId"].(string), out["token"].(string)
}

func TestOnlyAdminsCreateServiceAccounts(t *testing.T) {
	h := newServiceAccountHarness(t)
	body := map[string]any{"name": "ci"}

	if rec := h.do(http.MethodPost, "/api/v1/service-accounts", "", body); rec.Code != http.StatusUnauthorized {
		t.Errorf("anonymous create: status = %d, want 401", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/service-accounts", tokenAlice, body); rec.Code != http.StatusForbidden {
		t.Errorf("user create: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodGet, "/api/v1/service-accounts", tokenAlice, nil); rec.Code != http.StatusForbidden {
		t.Errorf("user list: status = %d, want 403", rec.Code)
	}

	rec := h.do(http.MethodPost, "/api/v1/service-accounts", tokenAdmin, map[string]any{"name": "ci", "validityDays": 30})
	if rec.Code != http.StatusCreated {
		t.Fatalf("admin create: status = %d, body %s", rec.Code, rec.Body)
	}
	if cc := rec.Header().Get("Cache-Control"); cc != "no-store" {
		t.Errorf("Cache-Control = %q, want no-store on a response carrying a token", cc)
	}
	var out struct {
		Token          string `json:"token"`
		ServiceAccount struct {
			ID             string `json:"serviceAccountId"`
			Name           string `json:"name"`
			CreatedBy      string `json:"createdBy"`
			TokenIssuedAt  int64  `json:"tokenIssuedAt"`
			TokenExpiresAt int64  `json:"tokenExpiresAt"`
		} `json:"serviceAccount"`
	}
	if err := json.Unmarshal(rec.Body.Bytes(), &out); err != nil {
		t.Fatal(err)
	}
	if out.ServiceAccount.ID != "sa:ci" || out.ServiceAccount.Name != "ci" || out.ServiceAccount.CreatedBy != "admin-user" {
		t.Errorf("service account = %+v", out.ServiceAccount)
	}
	if got := time.Duration(out.ServiceAccount.TokenExpiresAt-out.ServiceAccount.TokenIssuedAt) * time.Millisecond; got != 30*24*time.Hour {
		t.Errorf("token lifetime = %v, want 30 days", got)
	}
	if !auth.IsServiceAccountToken(out.Token) {
		t.Errorf("token %q is not a service account token", out.Token)
	}

	// The token is shown once: no read returns it, and it is not stored in the clear.
	got := h.mustDo(http.MethodGet, "/api/v1/service-accounts/sa:ci", tokenAdmin, nil, http.StatusOK)
	if _, leaked := got["token"]; leaked {
		t.Error("GET returned the token")
	}
	var row iammodel.ServiceAccount
	h.db.First(&row, "user_id = ?", "sa:ci")
	if row.TokenHash == out.Token || row.TokenHash != auth.HashServiceAccountToken(out.Token) {
		t.Error("stored token is not the token's digest")
	}

	if rec := h.do(http.MethodPost, "/api/v1/service-accounts", tokenAdmin, map[string]any{"name": "ci"}); rec.Code != http.StatusConflict {
		t.Errorf("duplicate name: status = %d, want 409", rec.Code)
	}
}

func TestServiceAccountRequestValidation(t *testing.T) {
	h := newServiceAccountHarness(t)
	for _, tc := range []struct {
		name  string
		body  map[string]any
		field string
	}{
		{"upper case", map[string]any{"name": "CI"}, "name"},
		{"too short", map[string]any{"name": "c"}, "name"},
		{"slash", map[string]any{"name": "ci/bot"}, "name"},
		{"zero days", map[string]any{"name": "ci", "validityDays": 0}, "validityDays"},
		{"over a year", map[string]any{"name": "ci", "validityDays": 366}, "validityDays"},
	} {
		rec := h.do(http.MethodPost, "/api/v1/service-accounts", tokenAdmin, tc.body)
		if rec.Code != http.StatusBadRequest {
			t.Errorf("%s: status = %d, want 400", tc.name, rec.Code)
			continue
		}
		if f := firstFieldError(t, rec); f != tc.field {
			t.Errorf("%s: field = %q, want %q", tc.name, f, tc.field)
		}
	}
}

// User registration cannot be used to mint a service account or squat its namespace.
func TestUserRegistrationRefusesServiceAccounts(t *testing.T) {
	h := newServiceAccountHarness(t)
	h.withIntrospector(serviceAccountIntrospector{
		stub: stubIntrospector{
			"token-super": {Name: "super", Authorities: []string{string(role.SuperAdmin)}},
		},
		lookup: iamrepo.ServiceAccountTokenLookup{DB: h.db},
	})
	base := map[string]any{"firstName": "A", "lastName": "B"}

	withMethod := map[string]any{"userId": "robot", "authMethod": "SERVICE_ACCOUNT"}
	for k, v := range base {
		withMethod[k] = v
	}
	if rec := h.do(http.MethodPost, "/api/v1/users", "token-super", withMethod); rec.Code != http.StatusBadRequest {
		t.Errorf("SERVICE_ACCOUNT auth method: status = %d, want 400", rec.Code)
	}

	reserved := map[string]any{"userId": "sa:robot", "authMethod": "CILOGON"}
	for k, v := range base {
		reserved[k] = v
	}
	if rec := h.do(http.MethodPost, "/api/v1/users", "token-super", reserved); rec.Code != http.StatusBadRequest {
		t.Errorf("reserved id: status = %d, want 400", rec.Code)
	}
}

// A service account is never an administrator: not by default, not with an ADMIN row
// in user_roles, and so it cannot create other service accounts.
func TestServiceAccountIsNeverAdmin(t *testing.T) {
	h := newServiceAccountHarness(t)
	id, token := h.createServiceAccount("ci")

	if err := h.db.Create(&iammodel.UserRole{UserID: id, Role: role.SuperAdmin}).Error; err != nil {
		t.Fatalf("seed role: %v", err)
	}

	if rec := h.do(http.MethodGet, "/api/v1/users", token, nil); rec.Code != http.StatusForbidden {
		t.Errorf("list users: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/service-accounts", token, map[string]any{"name": "ci2"}); rec.Code != http.StatusForbidden {
		t.Errorf("create service account: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/service-accounts/"+id+"/token/regenerate", token, nil); rec.Code != http.StatusForbidden {
		t.Errorf("regenerate own token: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/slurm-clusters", token, map[string]any{
		"clusterName": "x", "headnodeHost": "x.example.edu", "headnodePort": 22,
	}); rec.Code != http.StatusForbidden {
		t.Errorf("admin-only write: status = %d, want 403", rec.Code)
	}
}

// A service account does what a user does: reads itself, owns records, is shared
// with and joins groups.
func TestServiceAccountActsAsAUser(t *testing.T) {
	h := newServiceAccountHarness(t)
	id, token := h.createServiceAccount("ci")

	h.mustDo(http.MethodGet, "/api/v1/users/"+id, token, nil, http.StatusOK)

	// Owns what it creates.
	_, ownStorage := h.seedStorage("sa-owned", token)
	h.mustDo(http.MethodGet, "/api/v1/scp-data-storages/"+ownStorage, token, nil, http.StatusOK)
	if rec := h.do(http.MethodGet, "/api/v1/scp-data-storages/"+ownStorage, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("bob reading the service account's storage: status = %d, want 403", rec.Code)
	}

	// Shared with directly.
	_, aliceStorage := h.seedStorage("alice-direct", tokenAlice)
	if rec := h.do(http.MethodGet, "/api/v1/scp-data-storages/"+aliceStorage, token, nil); rec.Code != http.StatusForbidden {
		t.Errorf("before the share: status = %d, want 403", rec.Code)
	}
	h.shareStorageWithUser(aliceStorage, id, "READ", tokenAlice)
	got := h.mustDo(http.MethodGet, "/api/v1/scp-data-storages/"+aliceStorage, token, nil, http.StatusOK)
	if got["permission"] != "READ" {
		t.Errorf("permission = %v, want READ", got["permission"])
	}

	// Shared with through a group.
	_, groupStorage := h.seedStorage("alice-group", tokenAlice)
	groupID := h.seedGroup("pipelines", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": id}, http.StatusCreated)
	h.mustDo(http.MethodPost, "/api/v1/scp-data-storages/"+groupStorage+"/shares", tokenAlice,
		map[string]any{"principalType": "GROUP", "principalId": groupID, "permission": "WRITE"}, http.StatusCreated)
	got = h.mustDo(http.MethodGet, "/api/v1/scp-data-storages/"+groupStorage, token, nil, http.StatusOK)
	if got["permission"] != "WRITE" {
		t.Errorf("permission through the group = %v, want WRITE", got["permission"])
	}
}

func TestServiceAccountTokenLifecycle(t *testing.T) {
	h := newServiceAccountHarness(t)
	id, token := h.createServiceAccount("ci")
	self := "/api/v1/users/" + id
	h.mustDo(http.MethodGet, self, token, nil, http.StatusOK)

	// Expired: rejected as an invalid token.
	h.db.Model(&iammodel.ServiceAccount{}).Where("user_id = ?", id).
		Update("token_expires_at", time.Now().Add(-time.Minute).UnixMilli())
	if rec := h.do(http.MethodGet, self, token, nil); rec.Code != http.StatusUnauthorized {
		t.Errorf("expired token: status = %d, want 401", rec.Code)
	}

	// Renewed: the same token works again, for the requested period.
	out := h.mustDo(http.MethodPost, "/api/v1/service-accounts/"+id+"/token/renew", tokenAdmin,
		map[string]any{"validityDays": 7}, http.StatusOK)
	if _, leaked := out["token"]; leaked {
		t.Error("renew returned a token")
	}
	if left := time.Until(time.UnixMilli(int64(out["tokenExpiresAt"].(float64)))); left < 6*24*time.Hour || left > 7*24*time.Hour {
		t.Errorf("renewed expiry is %v away, want 7 days", left)
	}
	h.mustDo(http.MethodGet, self, token, nil, http.StatusOK)

	// Renew with no body takes the default.
	h.mustDo(http.MethodPost, "/api/v1/service-accounts/"+id+"/token/renew", tokenAdmin, nil, http.StatusOK)

	// Regenerated: the old token is revoked and the new one works.
	out = h.mustDo(http.MethodPost, "/api/v1/service-accounts/"+id+"/token/regenerate", tokenAdmin, nil, http.StatusOK)
	fresh := out["token"].(string)
	if fresh == token {
		t.Fatal("regenerate returned the same token")
	}
	if rec := h.do(http.MethodGet, self, token, nil); rec.Code != http.StatusUnauthorized {
		t.Errorf("old token after regenerate: status = %d, want 401", rec.Code)
	}
	h.mustDo(http.MethodGet, self, fresh, nil, http.StatusOK)

	// Disabled: the token stops working without being discarded, and comes back.
	h.mustDo(http.MethodPut, "/api/v1/service-accounts/"+id, tokenAdmin, map[string]any{"status": "INACTIVE"}, http.StatusOK)
	if rec := h.do(http.MethodGet, self, fresh, nil); rec.Code != http.StatusUnauthorized {
		t.Errorf("inactive account: status = %d, want 401", rec.Code)
	}
	got := h.mustDo(http.MethodPut, "/api/v1/service-accounts/"+id, tokenAdmin,
		map[string]any{"status": "ACTIVE", "description": "updated"}, http.StatusOK)
	if got["description"] != "updated" || got["displayName"] != "CI pipeline" {
		t.Errorf("update = %v, want description changed and display name kept", got)
	}
	h.mustDo(http.MethodGet, self, fresh, nil, http.StatusOK)

	// A made-up token with the right prefix is just invalid.
	if rec := h.do(http.MethodGet, self, auth.ServiceAccountTokenPrefix+"nope", nil); rec.Code != http.StatusUnauthorized {
		t.Errorf("unknown token: status = %d, want 401", rec.Code)
	}
}

func TestServiceAccountEndpointsIgnorePeople(t *testing.T) {
	h := newServiceAccountHarness(t)
	if rec := h.do(http.MethodGet, "/api/v1/service-accounts/alice", tokenAdmin, nil); rec.Code != http.StatusNotFound {
		t.Errorf("GET a person: status = %d, want 404", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/service-accounts/alice/token/regenerate", tokenAdmin, nil); rec.Code != http.StatusNotFound {
		t.Errorf("regenerate for a person: status = %d, want 404", rec.Code)
	}
	if rec := h.do(http.MethodDelete, "/api/v1/service-accounts/alice", tokenAdmin, nil); rec.Code != http.StatusNotFound {
		t.Errorf("delete a person: status = %d, want 404", rec.Code)
	}

	h.createServiceAccount("ci")
	var list []map[string]any
	rec := h.do(http.MethodGet, "/api/v1/service-accounts", tokenAdmin, nil)
	if err := json.Unmarshal(rec.Body.Bytes(), &list); err != nil {
		t.Fatal(err)
	}
	if len(list) != 1 || list[0]["serviceAccountId"] != "sa:ci" {
		t.Errorf("list = %v, want only sa:ci", list)
	}
}

func TestDeletingServiceAccount(t *testing.T) {
	h := newServiceAccountHarness(t)

	// One that owns records is refused, and keeps working.
	owner, ownerToken := h.createServiceAccount("owner")
	h.seedSSHCredential("sa-key", ownerToken)
	if rec := h.do(http.MethodDelete, "/api/v1/service-accounts/"+owner, tokenAdmin, nil); rec.Code != http.StatusConflict {
		t.Errorf("delete of an owner: status = %d, want 409\nbody: %s", rec.Code, rec.Body)
	}
	h.mustDo(http.MethodGet, "/api/v1/users/"+owner, ownerToken, nil, http.StatusOK)

	// One that only holds shares and memberships goes, and takes them with it.
	id, token := h.createServiceAccount("reader")
	_, storageID := h.seedStorage("alice-data", tokenAlice)
	h.shareStorageWithUser(storageID, id, "READ", tokenAlice)
	groupID := h.seedGroup("readers", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": id}, http.StatusCreated)

	h.mustDo(http.MethodDelete, "/api/v1/service-accounts/"+id, tokenAdmin, nil, http.StatusNoContent)

	if rec := h.do(http.MethodGet, "/api/v1/users/"+id, token, nil); rec.Code != http.StatusUnauthorized {
		t.Errorf("token after delete: status = %d, want 401", rec.Code)
	}
	var shares, members, tokens int64
	h.db.Table("resource_sharings").Where("principal_id = ?", id).Count(&shares)
	h.db.Table("group_members").Where("user_id = ?", id).Count(&members)
	h.db.Table("service_accounts").Where("user_id = ?", id).Count(&tokens)
	if shares+members+tokens != 0 {
		t.Errorf("left behind %d shares, %d memberships, %d token rows", shares, members, tokens)
	}
}
