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
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/apache/airavata/internal/auth"
)

// fakeCILogon serves discovery and a token endpoint that grants one code, bound to
// one verifier and redirect URI, and one refresh token.
func fakeCILogon(t *testing.T) *httptest.Server {
	t.Helper()
	var srv *httptest.Server
	mux := http.NewServeMux()
	mux.HandleFunc("GET /.well-known/openid-configuration", func(w http.ResponseWriter, _ *http.Request) {
		_ = json.NewEncoder(w).Encode(map[string]string{
			"issuer": srv.URL, "authorization_endpoint": srv.URL + "/authorize", "token_endpoint": srv.URL + "/oauth2/token",
		})
	})
	mux.HandleFunc("POST /oauth2/token", func(w http.ResponseWriter, r *http.Request) {
		_ = r.ParseForm()
		id, secret, basic := r.BasicAuth()
		if !basic {
			id, secret = r.PostForm.Get("client_id"), r.PostForm.Get("client_secret")
		}
		w.Header().Set("Content-Type", "application/json")
		ok := id == "client-1" && secret == "secret-1"
		switch r.PostForm.Get("grant_type") {
		case "authorization_code":
			ok = ok && r.PostForm.Get("code") == "code-1" && r.PostForm.Get("code_verifier") == "verifier-1" &&
				r.PostForm.Get("redirect_uri") == "https://airavata.example.org/dashboard/login/callback/"
		case "refresh_token":
			ok = ok && r.PostForm.Get("refresh_token") == "refresh-1"
		default:
			ok = false
		}
		if !ok {
			w.WriteHeader(http.StatusBadRequest)
			_, _ = w.Write([]byte(`{"error":"invalid_grant"}`))
			return
		}
		_, _ = w.Write([]byte(`{"access_token":"access-1","token_type":"Bearer","refresh_token":"refresh-1","expires_in":900}`))
	})
	srv = httptest.NewServer(mux)
	t.Cleanup(srv.Close)
	return srv
}

func call(t *testing.T, mux *http.ServeMux, method, path string, body any) (int, map[string]any) {
	t.Helper()
	var buf bytes.Buffer
	if body != nil {
		_ = json.NewEncoder(&buf).Encode(body)
	}
	rec := httptest.NewRecorder()
	mux.ServeHTTP(rec, httptest.NewRequest(method, path, &buf))
	var out map[string]any
	_ = json.Unmarshal(rec.Body.Bytes(), &out)
	return rec.Code, out
}

func TestSignInIsDisabledWithoutAClient(t *testing.T) {
	mux := http.NewServeMux()
	auth.NewSignIn("https://cilogon.invalid", "", "").Register(mux)
	if code, body := call(t, mux, http.MethodGet, "/api/v1/auth/cilogon", nil); code != http.StatusOK || body["enabled"] != false {
		t.Errorf("config = %d %v, want 200 disabled", code, body)
	}
	if code, _ := call(t, mux, http.MethodPost, "/api/v1/auth/cilogon/token", map[string]string{"code": "c"}); code != http.StatusNotFound {
		t.Errorf("exchange without a client = %d, want 404", code)
	}
}

func TestSignInExchangesAndRefreshes(t *testing.T) {
	idp := fakeCILogon(t)
	mux := http.NewServeMux()
	auth.NewSignIn(idp.URL+"/", "client-1", "secret-1").Register(mux)

	code, cfg := call(t, mux, http.MethodGet, "/api/v1/auth/cilogon", nil)
	if code != http.StatusOK || cfg["enabled"] != true || cfg["clientId"] != "client-1" ||
		cfg["authorizationEndpoint"] != idp.URL+"/authorize" || cfg["clientSecret"] != nil {
		t.Fatalf("config = %d %v", code, cfg)
	}

	exchange := map[string]string{
		"code": "code-1", "codeVerifier": "verifier-1",
		"redirectUri": "https://airavata.example.org/dashboard/login/callback/",
	}
	code, tokens := call(t, mux, http.MethodPost, "/api/v1/auth/cilogon/token", exchange)
	if code != http.StatusOK || tokens["accessToken"] != "access-1" || tokens["refreshToken"] != "refresh-1" {
		t.Fatalf("exchange = %d %v", code, tokens)
	}
	if exp, _ := tokens["expiresIn"].(float64); exp < 890 || exp > 900 {
		t.Errorf("expiresIn = %v, want about 900", tokens["expiresIn"])
	}

	exchange["codeVerifier"] = "wrong"
	if code, _ := call(t, mux, http.MethodPost, "/api/v1/auth/cilogon/token", exchange); code != http.StatusBadRequest {
		t.Errorf("wrong verifier = %d, want 400", code)
	}
	exchange["codeVerifier"] = "verifier-1"
	for _, redirect := range []string{
		"https://elsewhere.example.org/steal",
		"https://airavata.example.org/dashboard/login/callback/?x=1",
		"javascript:alert(1)",
		"",
	} {
		exchange["redirectUri"] = redirect
		if code, _ := call(t, mux, http.MethodPost, "/api/v1/auth/cilogon/token", exchange); code != http.StatusBadRequest {
			t.Errorf("redirect %q = %d, want 400", redirect, code)
		}
	}

	code, refreshed := call(t, mux, http.MethodPost, "/api/v1/auth/cilogon/refresh", map[string]string{"refreshToken": "refresh-1"})
	if code != http.StatusOK || refreshed["accessToken"] != "access-1" {
		t.Errorf("refresh = %d %v", code, refreshed)
	}
	if code, _ := call(t, mux, http.MethodPost, "/api/v1/auth/cilogon/refresh", map[string]string{"refreshToken": "stale"}); code != http.StatusBadRequest {
		t.Errorf("stale refresh = %d, want 400", code)
	}
}
