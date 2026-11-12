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
	"net/http"
	"testing"

	"github.com/apache/airavata/internal/permission"
	"github.com/apache/airavata/internal/role"

	iammodel "github.com/apache/airavata/api/iam/model"
)

// createServiceAccountWith creates an account holding perms and returns its id and token.
func (h *harness) createServiceAccountWith(name string, perms ...string) (id, token string) {
	h.t.Helper()
	out := h.mustDo(http.MethodPost, "/api/v1/service-accounts", tokenAdmin,
		map[string]any{"name": name, "permissions": perms}, http.StatusCreated)
	sa := out["serviceAccount"].(map[string]any)
	return sa["serviceAccountId"].(string), out["token"].(string)
}

func (h *harness) status(method, path, token string, body any) int {
	h.t.Helper()
	return h.do(method, path, token, body).Code
}

func names(v any) []string {
	var out []string
	for _, p := range v.([]any) {
		out = append(out, p.(string))
	}
	return out
}

// Each service account holds its own permissions: one account can list every process
// while its neighbour, created the same way, cannot.
func TestServiceAccountsHoldTheirOwnPermissions(t *testing.T) {
	h := newServiceAccountHarness(t)
	_, lister := h.createServiceAccountWith("lister", "process:list")
	_, plain := h.createServiceAccountWith("plain")

	if got := h.status("GET", "/api/v1/processes", lister, nil); got != http.StatusOK {
		t.Errorf("account granted process:list: status = %d, want 200", got)
	}
	if got := h.status("GET", "/api/v1/processes", plain, nil); got != http.StatusForbidden {
		t.Errorf("account with no grants: status = %d, want 403", got)
	}
	// A grant opens exactly its own door.
	if got := h.status("GET", "/api/v1/users", lister, nil); got != http.StatusForbidden {
		t.Errorf("account granted only process:list reached user:list: status = %d, want 403", got)
	}

	me := h.mustDo("GET", "/api/v1/users/me", lister, nil, http.StatusOK)
	if got := names(me["permissions"]); len(got) != 1 || got[0] != "process:list" {
		t.Errorf("/users/me permissions = %v, want [process:list]", got)
	}
}

func TestServiceAccountPermissionsAreReadableAndReplaceable(t *testing.T) {
	h := newServiceAccountHarness(t)
	id, token := h.createServiceAccountWith("ci", "process:list", "storage:list")

	got := h.mustDo("GET", "/api/v1/service-accounts/"+id, tokenAdmin, nil, http.StatusOK)
	if p := names(got["permissions"]); len(p) != 2 || p[0] != "process:list" || p[1] != "storage:list" {
		t.Fatalf("permissions = %v, want [process:list storage:list]", p)
	}

	// Replacing takes effect on the account's very next request, with no waiting out a cache.
	h.mustDo("PUT", "/api/v1/service-accounts/"+id, tokenAdmin,
		map[string]any{"permissions": []string{"storage:list"}}, http.StatusOK)
	if got := h.status("GET", "/api/v1/processes", token, nil); got != http.StatusForbidden {
		t.Errorf("revoked process:list still works: status = %d, want 403", got)
	}
	if got := h.status("GET", "/api/v1/scp-data-storages", token, nil); got != http.StatusOK {
		t.Errorf("kept storage:list: status = %d, want 200", got)
	}

	// Leaving permissions out of an update leaves them as they are.
	h.mustDo("PUT", "/api/v1/service-accounts/"+id, tokenAdmin,
		map[string]any{"description": "renamed"}, http.StatusOK)
	if got := h.status("GET", "/api/v1/scp-data-storages", token, nil); got != http.StatusOK {
		t.Errorf("an unrelated update dropped the grants: status = %d", got)
	}

	// An empty list clears them.
	cleared := h.mustDo("PUT", "/api/v1/service-accounts/"+id, tokenAdmin,
		map[string]any{"permissions": []string{}}, http.StatusOK)
	if p := names(cleared["permissions"]); len(p) != 0 {
		t.Errorf("permissions after clearing = %v, want none", p)
	}
	if got := h.status("GET", "/api/v1/scp-data-storages", token, nil); got != http.StatusForbidden {
		t.Errorf("cleared account still has storage:list: status = %d, want 403", got)
	}
}

// Whatever is requested, a service account cannot be given the permissions that manage
// identities and privileges, nor ones that do not exist.
func TestServiceAccountsCannotBeGrantedPrivilegePermissions(t *testing.T) {
	h := newServiceAccountHarness(t)
	for _, p := range []string{
		"admin:manage", "user:update_any", "serviceaccount:manage",
		"no:such_permission",
	} {
		rec := h.do("POST", "/api/v1/service-accounts", tokenAdmin,
			map[string]any{"name": "bad", "permissions": []string{p}})
		if rec.Code != http.StatusBadRequest {
			t.Errorf("create with %s: status = %d, want 400\n%s", p, rec.Code, rec.Body.String())
		}
	}
	id, _ := h.createServiceAccountWith("fine")
	if got := h.status("PUT", "/api/v1/service-accounts/"+id, tokenAdmin,
		map[string]any{"permissions": []string{"admin:manage"}}); got != http.StatusBadRequest {
		t.Errorf("update with admin:manage: status = %d, want 400", got)
	}
}

// A row that got into the table around the API is still ignored when it is read, so the
// guarantee does not rest on every writer behaving.
func TestServiceAccountForbiddenGrantIsIgnoredOnRead(t *testing.T) {
	h := newServiceAccountHarness(t)
	id, token := h.createServiceAccountWith("sneaky")
	if err := h.db.Exec(
		"INSERT INTO service_account_permissions (user_id, permission) VALUES (?, ?)", id, "admin:manage",
	).Error; err != nil {
		t.Fatal(err)
	}
	h.svcs.Permissions.Invalidate()
	if got := h.status("POST", "/api/v1/users/bob/admin", token, map[string]any{"admin": true}); got != http.StatusForbidden {
		t.Errorf("a directly inserted admin:manage worked: status = %d, want 403", got)
	}
}

// Nobody can hand out a permission they do not hold themselves.
func TestServiceAccountGrantsRequireHoldingThePermission(t *testing.T) {
	h := newServiceAccountHarness(t)
	if err := h.db.Delete(&iammodel.RolePermission{}, "role = ? AND permission = ?", "ADMIN", "process:list").Error; err != nil {
		t.Fatal(err)
	}
	h.svcs.Permissions.Invalidate()

	rec := h.do("POST", "/api/v1/service-accounts", tokenAdmin,
		map[string]any{"name": "greedy", "permissions": []string{"process:list"}})
	if rec.Code != http.StatusForbidden {
		t.Fatalf("granting an unheld permission: status = %d, want 403\n%s", rec.Code, rec.Body.String())
	}
	h.mustDo("POST", "/api/v1/service-accounts", tokenAdmin,
		map[string]any{"name": "modest", "permissions": []string{"storage:list"}}, http.StatusCreated)
}

// A service account holds no role that grants anything: granting a permission to USER
// reaches people, not service accounts.
func TestRoleGrantsDoNotReachServiceAccounts(t *testing.T) {
	h := newServiceAccountHarness(t)
	_, token := h.createServiceAccountWith("plain")
	if err := h.db.Create(&iammodel.RolePermission{Role: role.User, Permission: permission.ProcessList}).Error; err != nil {
		t.Fatal(err)
	}
	h.svcs.Permissions.Invalidate()

	if got := h.status("GET", "/api/v1/processes", tokenAlice, nil); got != http.StatusOK {
		t.Fatalf("a person holding USER: status = %d, want 200", got)
	}
	if got := h.status("GET", "/api/v1/processes", token, nil); got != http.StatusForbidden {
		t.Errorf("a service account inherited USER's grant: status = %d, want 403", got)
	}
}

func TestDeletingAServiceAccountRemovesItsGrants(t *testing.T) {
	h := newServiceAccountHarness(t)
	id, _ := h.createServiceAccountWith("short-lived", "process:list")
	h.mustDo("DELETE", "/api/v1/service-accounts/"+id, tokenAdmin, nil, http.StatusNoContent)

	var n int64
	h.db.Model(&iammodel.ServiceAccountPermission{}).Where("user_id = ?", id).Count(&n)
	if n != 0 {
		t.Errorf("%d grants outlived the account", n)
	}
}

// An onboarding integration can register users and activate them, but that reaches no
// administrator: changing an administrator's status still needs admin:manage.
func TestServiceAccountMayHoldUserCreateAndSetStatus(t *testing.T) {
	h := newServiceAccountHarness(t)
	// user:create is held only by SUPER_ADMIN by default, so an ADMIN cannot hand it out.
	if rec := h.do("POST", "/api/v1/service-accounts", tokenAdmin,
		map[string]any{"name": "onboarding", "permissions": []string{"user:create"}}); rec.Code != http.StatusForbidden {
		t.Fatalf("an ADMIN granting user:create: status = %d, want 403", rec.Code)
	}
	if err := h.db.Create(&iammodel.RolePermission{Role: role.Admin, Permission: permission.UserCreate}).Error; err != nil {
		t.Fatal(err)
	}
	h.svcs.Permissions.Invalidate()

	_, token := h.createServiceAccountWith("onboarding", "user:create", "user:set_status")

	created := h.do("POST", "/api/v1/users", token, map[string]any{
		"userId": "cilogon:777", "email": "n@example.org", "firstName": "New", "lastName": "User"})
	if created.Code != http.StatusOK {
		t.Fatalf("create user: status = %d\n%s", created.Code, created.Body.String())
	}
	h.mustDo("POST", "/api/v1/users/cilogon:777/status", token, map[string]any{"status": "SUSPENDED"}, http.StatusOK)

	// It still cannot touch an administrator, appoint one, or edit a profile.
	if got := h.status("POST", "/api/v1/users/admin-user/status", token, map[string]any{"status": "SUSPENDED"}); got != http.StatusForbidden {
		t.Errorf("changing an administrator's status: status = %d, want 403", got)
	}
	if got := h.status("POST", "/api/v1/users/bob/admin", token, map[string]any{"admin": true}); got != http.StatusForbidden {
		t.Errorf("appointing an administrator: status = %d, want 403", got)
	}
}
