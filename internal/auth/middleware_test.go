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
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"fmt"
	"maps"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/coreos/go-oidc/v3/oidc"
	"golang.org/x/oauth2"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
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

// signIDToken signs an ID token for the-client from https://issuer.example, with claims over the defaults.
func signIDToken(key *rsa.PrivateKey, claims map[string]any) string {
	segment := func(v any) string { raw, _ := json.Marshal(v); return base64.RawURLEncoding.EncodeToString(raw) }
	all := map[string]any{"iss": "https://issuer.example", "aud": "the-client", "sub": "http://cilogon.org/serverE/users/42", "exp": time.Now().Add(time.Hour).Unix()}
	maps.Copy(all, claims)
	signed := segment(map[string]string{"alg": "RS256"}) + "." + segment(all)
	digest := sha256.Sum256([]byte(signed))
	signature, _ := rsa.SignPKCS1v15(rand.Reader, key, crypto.SHA256, digest[:])
	return signed + "." + base64.RawURLEncoding.EncodeToString(signature)
}

func TestIDTokensSkipIntrospection(t *testing.T) {
	key, _ := rsa.GenerateKey(rand.Reader, 2048)
	idToken := func(claims map[string]any) string { return signIDToken(key, claims) }

	var calls int
	c := newIntrospector(cilogonStub(t, "alice", &calls), nil)
	c.IDTokens = oidc.NewVerifier("https://issuer.example", &oidc.StaticKeySet{PublicKeys: []crypto.PublicKey{key.Public()}}, &oidc.Config{ClientID: "the-client"})
	if p, err := c.Introspect(context.Background(), idToken(map[string]any{"preferred_username": "root"})); err != nil || p.Name != "cilogon:42" || len(p.Authorities) != 1 {
		t.Errorf("ID token = %+v %v, want cilogon:42 from sub with its roles", p, err)
	}
	if _, err := c.Introspect(context.Background(), idToken(map[string]any{"sub": "sa:ci"})); err != auth.ErrInvalidToken {
		t.Errorf("ID token for sa:ci: err = %v, want ErrInvalidToken", err)
	}
	if calls != 0 {
		t.Errorf("CILogon was called %d times for ID tokens, want 0", calls)
	}
	if p, err := c.Introspect(context.Background(), "opaque-access-token"); err != nil || p.Name != "alice" {
		t.Errorf("access token = %+v %v, want alice from introspection", p, err)
	}

	var introspected int
	anonymous := cilogonStub(t, "", &introspected)
	c.IntrospectionURI, c.UserInfoURI = anonymous.URL+"/introspect", anonymous.URL+"/userinfo"
	for name, claims := range map[string]map[string]any{"expired": {"exp": time.Now().Add(-time.Hour).Unix()}, "wrong audience": {"aud": "another-client"}} {
		if _, err := c.Introspect(context.Background(), idToken(claims)); err != auth.ErrInvalidToken {
			t.Errorf("%s ID token: err = %v, want ErrInvalidToken", name, err)
		}
	}
	if introspected != 4 {
		t.Errorf("CILogon was called %d times for rejected ID tokens, want 4", introspected)
	}
}

// One user gets one id whether they present an access token or an ID token.
func TestIDTokensAndAccessTokensShareTheUserID(t *testing.T) {
	key, _ := rsa.GenerateKey(rand.Reader, 2048)
	var calls int
	c := newIntrospector(cilogonStub(t, "http://cilogon.org/serverE/users/42", &calls), nil)
	c.IDTokens = oidc.NewVerifier("https://issuer.example", &oidc.StaticKeySet{PublicKeys: []crypto.PublicKey{key.Public()}}, &oidc.Config{ClientID: "the-client"})
	fromAccess, err := c.Introspect(context.Background(), "opaque-access-token")
	if err != nil {
		t.Fatal(err)
	}
	fromID, err := c.Introspect(context.Background(), signIDToken(key, map[string]any{"preferred_username": "jdoe"}))
	if err != nil || fromID.Name != fromAccess.Name {
		t.Errorf("ID token user = %v %v, access token user = %q", fromID, err, fromAccess.Name)
	}
}

func TestSignInGrants(t *testing.T) {
	approved := false
	issuer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		_ = r.ParseForm()
		w.Header().Set("Content-Type", "application/json")
		switch {
		case r.URL.Path == "/device":
			if r.Form.Get("client_secret") != "the-secret" {
				t.Errorf("device start = %v", r.Form)
			}
			_, _ = w.Write([]byte(`{"device_code":"dc","verification_uri":"https://issuer/device","verification_uri_complete":"https://issuer/device?user_code=ABCD-EFGH"}`))
		case r.Form.Get("grant_type") == "authorization_code":
			if r.Form.Get("code") != "the-code" || r.Form.Get("code_verifier") != "the-verifier" || r.Form.Get("redirect_uri") != "https://app.example.edu/callback" {
				t.Errorf("token request = %v", r.Form)
			}
			_, _ = w.Write([]byte(`{"access_token":"a","id_token":"header.payload.signature","refresh_token":"r1","expires_in":900}`))
		case r.Form.Get("refresh_token") == "no-id-token":
			_, _ = w.Write([]byte(`{"access_token":"a"}`))
		case r.Form.Get("grant_type") == "refresh_token":
			w.WriteHeader(http.StatusBadRequest)
			_, _ = fmt.Fprintf(w, `{"error":%q}`, r.Form.Get("refresh_token"))
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
	signIn := auth.NewSignIn(oauth2.Endpoint{TokenURL: issuer.URL + "/token", DeviceAuthURL: issuer.URL + "/device"}, "the-client", "the-secret", []string{"https://app.example.edu"}, http.DefaultClient)
	tokens, err := signIn.Redeem(context.Background(), auth.Grant{Code: "the-code", CodeVerifier: "the-verifier", RedirectURI: "https://app.example.edu/callback"})
	if err != nil || tokens.IDToken != "header.payload.signature" || tokens.RefreshToken != "r1" {
		t.Fatalf("exchange = %+v %v", tokens, err)
	}
	if _, err := signIn.Redeem(context.Background(), auth.Grant{Code: "the-code", CodeVerifier: "the-verifier", RedirectURI: "https://evil.example/callback"}); httpx.StatusOf(err) != http.StatusBadRequest {
		t.Fatalf("foreign redirect = %v", err)
	}
	for refresh, status := range map[string]int{"invalid_grant": http.StatusBadRequest, "invalid_client": http.StatusBadGateway, "no-id-token": http.StatusBadGateway} {
		if _, err := signIn.Redeem(context.Background(), auth.Grant{RefreshToken: refresh}); httpx.StatusOf(err) != status {
			t.Errorf("refresh %s = %v, want %d", refresh, err, status)
		}
	}
	noDevice := auth.NewSignIn(oauth2.Endpoint{TokenURL: issuer.URL + "/token"}, "the-client", "the-secret", nil, http.DefaultClient)
	if _, err := noDevice.DeviceStart(context.Background()); httpx.StatusOf(err) != http.StatusNotImplemented {
		t.Errorf("device start without a device endpoint = %v, want 501", err)
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
