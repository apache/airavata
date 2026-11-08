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
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/role"
)

type resolverFunc func(ctx context.Context, token string) (*auth.Principal, error)

func (f resolverFunc) ResolveServiceAccount(ctx context.Context, token string) (*auth.Principal, error) {
	return f(ctx, token)
}

// cilogonStub serves introspection and userinfo, counting calls and reporting
// username as the caller's identity.
func cilogonStub(t *testing.T, username string, calls *int) *httptest.Server {
	t.Helper()
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		*calls++
		w.Header().Set("Content-Type", "application/json")
		if strings.HasSuffix(r.URL.Path, "/introspect") {
			_, _ = w.Write([]byte(`{"active":true,"username":"` + username + `"}`))
			return
		}
		_, _ = w.Write([]byte(`{}`))
	}))
	t.Cleanup(srv.Close)
	return srv
}

func newIntrospector(srv *httptest.Server, sa auth.ServiceAccountResolver) *auth.CILogonIntrospector {
	c := auth.NewCILogonIntrospector(srv.URL+"/introspect", srv.URL+"/userinfo", "id", "secret",
		auth.MockRoleLookup{}, nil)
	c.ServiceAccounts = sa
	return c
}

// A service account token is an Airavata secret: it must be resolved locally and
// never reach CILogon, whether or not a resolver is configured.
func TestServiceAccountTokenNeverReachesCILogon(t *testing.T) {
	var calls int
	srv := cilogonStub(t, "alice", &calls)
	token, _, err := auth.GenerateServiceAccountToken()
	if err != nil {
		t.Fatal(err)
	}

	resolved := newIntrospector(srv, resolverFunc(func(_ context.Context, got string) (*auth.Principal, error) {
		if got != token {
			t.Errorf("resolver got %q, want the presented token", got)
		}
		return auth.ServiceAccountPrincipal("sa:ci"), nil
	}))
	p, err := resolved.Introspect(context.Background(), token)
	if err != nil {
		t.Fatalf("introspect: %v", err)
	}
	if p.Name != "sa:ci" || !p.IsServiceAccount() {
		t.Errorf("principal = %+v, want service account sa:ci", p)
	}

	if _, err := newIntrospector(srv, nil).Introspect(context.Background(), token); err != auth.ErrInvalidToken {
		t.Errorf("without a resolver: err = %v, want ErrInvalidToken", err)
	}
	if calls != 0 {
		t.Errorf("CILogon was called %d times for a service account token, want 0", calls)
	}
}

// The service account namespace is reserved, so an identity provider cannot be used
// to impersonate one by reporting such a username.
func TestCILogonCannotClaimAServiceAccountID(t *testing.T) {
	var calls int
	srv := cilogonStub(t, "sa:ci", &calls)
	if _, err := newIntrospector(srv, nil).Introspect(context.Background(), "opaque-cilogon-token"); err != auth.ErrInvalidToken {
		t.Errorf("err = %v, want ErrInvalidToken", err)
	}
}

func TestServiceAccountPrincipalIsNeverAdmin(t *testing.T) {
	p := auth.ServiceAccountPrincipal("sa:ci")
	if p.IsAdmin() {
		t.Error("service account principal is an admin")
	}
	if !p.HasAnyAuthority(string(role.User)) || len(p.Authorities) != 1 {
		t.Errorf("authorities = %v, want exactly USER", p.Authorities)
	}
}

func TestGeneratedTokensAreUniqueAndHashed(t *testing.T) {
	a, ha, err := auth.GenerateServiceAccountToken()
	if err != nil {
		t.Fatal(err)
	}
	b, hb, _ := auth.GenerateServiceAccountToken()
	if a == b || ha == hb {
		t.Error("two generated tokens collided")
	}
	if !auth.IsServiceAccountToken(a) {
		t.Errorf("token %q lacks the service account prefix", a)
	}
	if ha != auth.HashServiceAccountToken(a) || strings.Contains(ha, a) {
		t.Error("stored digest does not match the token's hash")
	}
}
