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

package auth_test

import (
	"context"
	"errors"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/permission"
)

func TestRequirePermission(t *testing.T) {
	granted := &auth.Principal{Name: "a", Permissions: permission.Set{permission.AuditRead: {}}}
	denied := &auth.Principal{Name: "b"}

	if _, err := auth.RequirePermission(auth.WithPrincipal(context.Background(), granted), permission.AuditRead); err != nil {
		t.Errorf("holder was refused: %v", err)
	}

	for name, tc := range map[string]struct {
		ctx    context.Context
		status int
	}{
		"anonymous":     {context.Background(), http.StatusUnauthorized},
		"without grant": {auth.WithPrincipal(context.Background(), denied), http.StatusForbidden},
	} {
		_, err := auth.RequirePermission(tc.ctx, permission.AuditRead)
		var he *httpx.Error
		if err == nil || !errors.As(err, &he) || he.Status != tc.status {
			t.Errorf("%s: err = %v, want status %d", name, err, tc.status)
		}
	}
}

// Authorities alone grant nothing: a principal that did not come through the
// middleware's resolver holds no permission, even one named ADMIN.
func TestAuthorityDoesNotImplyPermission(t *testing.T) {
	p := &auth.Principal{Name: "x", Authorities: []string{"SUPER_ADMIN"}}
	if p.Can(permission.AuditRead) {
		t.Error("an unresolved principal holds a permission")
	}
	var anon *auth.Principal
	if anon.Can(permission.AuditRead) {
		t.Error("a nil principal holds a permission")
	}
}

type fakeResolver map[string]permission.Set

func (f fakeResolver) Permissions(_ context.Context, authorities []string) permission.Set {
	out := permission.Set{}
	for _, a := range authorities {
		for p := range f[a] {
			out[p] = struct{}{}
		}
	}
	return out
}

type roleIntrospector struct{}

func (roleIntrospector) Introspect(context.Context, string) (*auth.Principal, error) {
	return &auth.Principal{Name: "alice", Authorities: []string{"AUDITOR"}}, nil
}

func TestMiddlewareResolvesPermissions(t *testing.T) {
	resolver := fakeResolver{"AUDITOR": {permission.AuditRead: {}}}
	var got *auth.Principal
	next := http.HandlerFunc(func(_ http.ResponseWriter, r *http.Request) { got = auth.FromContext(r.Context()) })

	req := httptest.NewRequest(http.MethodGet, "/x", nil)
	req.Header.Set("Authorization", "Bearer t")
	auth.Middleware(roleIntrospector{}, auth.WithPermissions(resolver))(next).ServeHTTP(httptest.NewRecorder(), req)
	if got == nil || !got.Can(permission.AuditRead) {
		t.Fatalf("principal = %+v, want audit:read resolved", got)
	}

	// Without a resolver configured nothing is granted.
	got = nil
	auth.Middleware(roleIntrospector{})(next).ServeHTTP(httptest.NewRecorder(), req)
	if got == nil || got.Can(permission.AuditRead) {
		t.Fatalf("principal = %+v, want no permissions", got)
	}
}
