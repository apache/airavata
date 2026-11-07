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
	"encoding/base64"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"golang.org/x/oauth2"
)

type tokenIntrospector map[string]string

func (s tokenIntrospector) Introspect(_ context.Context, token string) (*auth.Principal, error) {
	if name, ok := s[token]; ok {
		return &auth.Principal{Name: name}, nil
	}
	return nil, auth.ErrInvalidToken
}

// caller runs req through the middleware and reports who it resolved, "" for anonymous.
func caller(t *testing.T, req *http.Request) (string, int) {
	t.Helper()
	var name string
	h := auth.Middleware(tokenIntrospector{"https://cilogon.org/oauth2/accessToken/abc": "alice"})(
		http.HandlerFunc(func(_ http.ResponseWriter, r *http.Request) {
			if p := auth.FromContext(r.Context()); p != nil {
				name = p.Name
			}
		}))
	rec := httptest.NewRecorder()
	h.ServeHTTP(rec, req)
	return name, rec.Code
}

func TestWebSocketTokenFromSubprotocol(t *testing.T) {
	token := base64.RawURLEncoding.EncodeToString([]byte("https://cilogon.org/oauth2/accessToken/abc"))

	req := httptest.NewRequest(http.MethodGet, "/api/v1/ssh-interactive-auths/ws", nil)
	req.Header.Set("Upgrade", "websocket")
	req.Header.Set("Sec-WebSocket-Protocol", auth.WebSocketProtocol+", base64url.bearer."+token)
	if name, _ := caller(t, req); name != "alice" {
		t.Errorf("resolved %q, want alice from the subprotocol token", name)
	}

	// Only an upgrade may carry it: a plain request offering the header stays anonymous.
	req = httptest.NewRequest(http.MethodGet, "/api/v1/ssh-interactive-auths", nil)
	req.Header.Set("Sec-WebSocket-Protocol", "base64url.bearer."+token)
	if name, _ := caller(t, req); name != "" {
		t.Errorf("resolved %q from a non-upgrade request, want anonymous", name)
	}

	// A token that is present but wrong is still a 401, not anonymous.
	req = httptest.NewRequest(http.MethodGet, "/api/v1/ssh-interactive-auths/ws", nil)
	req.Header.Set("Upgrade", "websocket")
	req.Header.Set("Sec-WebSocket-Protocol", "base64url.bearer."+base64.RawURLEncoding.EncodeToString([]byte("nope")))
	if _, code := caller(t, req); code != http.StatusUnauthorized {
		t.Errorf("bad subprotocol token = %d, want 401", code)
	}
}

func TestSignInGrants(t *testing.T) {
	approved := false
	issuer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_ = r.ParseForm()
		w.Header().Set("Content-Type", "application/json")
		switch {
		case r.URL.Path == "/device":
			_, _ = w.Write([]byte(`{"device_code":"dc","verification_uri":"https://issuer/device","verification_uri_complete":"https://issuer/device?user_code=ABCD-EFGH"}`))
		case r.Form.Get("grant_type") == "authorization_code":
			if r.Form.Get("code") != "the-code" || r.Form.Get("code_verifier") != "the-verifier" || r.Form.Get("redirect_uri") != "https://app.example.edu/callback" {
				t.Errorf("token request = %v", r.Form)
			}
			_, _ = w.Write([]byte(`{"access_token":"a","id_token":"header.payload.signature","refresh_token":"r1","expires_in":900}`))
		case r.Form.Get("grant_type") == "refresh_token":
			w.WriteHeader(http.StatusBadRequest)
			_, _ = w.Write([]byte(`{"error":"invalid_grant"}`))
		case !approved:
			w.WriteHeader(http.StatusBadRequest)
			_, _ = w.Write([]byte(`{"error":"authorization_pending"}`))
		default:
			if r.Form.Get("device_code") != "dc" || r.Form.Get("client_secret") != "the-secret" {
				t.Errorf("device poll = %v", r.Form)
			}
			_, _ = w.Write([]byte(`{"access_token":"a","id_token":"header.payload.signature"}`))
		}
	}))
	defer issuer.Close()
	signIn := auth.NewSignIn(oauth2.Endpoint{TokenURL: issuer.URL + "/token", DeviceAuthURL: issuer.URL + "/device"}, "the-client", "the-secret", []string{"https://app.example.edu"})
	tokens, err := signIn.Redeem(context.Background(), auth.Grant{Code: "the-code", CodeVerifier: "the-verifier", RedirectURI: "https://app.example.edu/callback"})
	if err != nil || tokens.IDToken != "header.payload.signature" || tokens.RefreshToken != "r1" || tokens.ExpiresIn != 900 {
		t.Fatalf("exchange = %+v %v", tokens, err)
	}
	if _, err := signIn.Redeem(context.Background(), auth.Grant{Code: "the-code", CodeVerifier: "the-verifier", RedirectURI: "https://evil.example/callback"}); httpx.StatusOf(err) != http.StatusBadRequest {
		t.Fatalf("foreign redirect = %v", err)
	}
	if _, err := signIn.Redeem(context.Background(), auth.Grant{RefreshToken: "old"}); httpx.StatusOf(err) != http.StatusBadRequest {
		t.Fatalf("rejected refresh = %v", err)
	}
	device, err := signIn.DeviceStart(context.Background())
	if err != nil || device.DeviceCode != "dc" || device.VerificationURI != "https://issuer/device?user_code=ABCD-EFGH" || device.Interval != 5 {
		t.Fatalf("device = %+v %v", device, err)
	}
	if _, err := signIn.Redeem(context.Background(), auth.Grant{DeviceCode: "dc"}); httpx.StatusOf(err) != http.StatusAccepted {
		t.Fatalf("pending poll = %v", err)
	}
	approved = true
	if _, err := signIn.Redeem(context.Background(), auth.Grant{DeviceCode: "dc"}); err != nil {
		t.Fatalf("complete poll = %v", err)
	}
}

func TestConnectionFromHeaderOrSubprotocol(t *testing.T) {
	var got string
	h := auth.Middleware(tokenIntrospector{})(http.HandlerFunc(func(_ http.ResponseWriter, r *http.Request) { got = auth.Connection(r.Context()) }))

	req := httptest.NewRequest(http.MethodGet, "/api/v1/ssh-interactive-auths", nil)
	req.Header.Set(auth.ConnectionHeader, "window-1")
	h.ServeHTTP(httptest.NewRecorder(), req)
	if got != "window-1" {
		t.Errorf("connection %q from the header, want window-1", got)
	}

	req = httptest.NewRequest(http.MethodGet, "/api/v1/ssh-interactive-auths/ws", nil)
	req.Header.Set("Upgrade", "websocket")
	req.Header.Set("Sec-WebSocket-Protocol", auth.WebSocketProtocol+", connection.tab-2")
	h.ServeHTTP(httptest.NewRecorder(), req)
	if got != "tab-2" {
		t.Errorf("connection %q from the subprotocol, want tab-2", got)
	}
}
