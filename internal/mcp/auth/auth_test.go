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

package auth

import (
	"bytes"
	"context"
	"crypto/sha256"
	"encoding/base64"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"net/http/httptest"
	"net/url"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/zalando/go-keyring"
)

const rawSub = "http://cilogon.org/serverA/users/12345"

type fakeIdP struct {
	t          *testing.T
	srv        *httptest.Server
	mu         sync.Mutex
	challenges map[string]string
	redirects  map[string]string
	valid      map[string]bool
	refresh    map[string]bool
	n          int
	devicePoll int
	badState   bool
	expiresIn  int
}

func newFakeIdP(t *testing.T) *fakeIdP {
	f := &fakeIdP{t: t, challenges: map[string]string{}, redirects: map[string]string{}, valid: map[string]bool{}, refresh: map[string]bool{}, expiresIn: 3600}
	mux := http.NewServeMux()
	mux.HandleFunc("GET /.well-known/openid-configuration", func(w http.ResponseWriter, r *http.Request) {
		json.NewEncoder(w).Encode(map[string]string{
			"issuer":                        f.srv.URL,
			"authorization_endpoint":        f.srv.URL + "/authorize",
			"token_endpoint":                f.srv.URL + "/token",
			"device_authorization_endpoint": f.srv.URL + "/device",
			"userinfo_endpoint":             f.srv.URL + "/userinfo",
		})
	})
	mux.HandleFunc("GET /authorize", f.authorize)
	mux.HandleFunc("POST /token", f.token)
	mux.HandleFunc("POST /device", func(w http.ResponseWriter, r *http.Request) {
		json.NewEncoder(w).Encode(map[string]any{
			"device_code": "dev-code", "user_code": "ABCD-EFGH",
			"verification_uri": f.srv.URL + "/device/verify", "expires_in": 60, "interval": 1,
		})
	})
	mux.HandleFunc("GET /userinfo", func(w http.ResponseWriter, r *http.Request) {
		tok := strings.TrimPrefix(r.Header.Get("Authorization"), "Bearer ")
		f.mu.Lock()
		ok := f.valid[tok]
		f.mu.Unlock()
		if !ok {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}
		json.NewEncoder(w).Encode(map[string]string{"sub": rawSub, "name": "Alice Doe", "email": "alice@example.org"})
	})
	f.srv = httptest.NewServer(mux)
	t.Cleanup(f.srv.Close)
	return f
}

func (f *fakeIdP) authorize(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	if q.Get("code_challenge_method") != "S256" || q.Get("code_challenge") == "" || q.Get("response_type") != "code" {
		http.Error(w, "bad request", http.StatusBadRequest)
		return
	}
	f.mu.Lock()
	f.n++
	code := fmt.Sprintf("code-%d", f.n)
	f.challenges[code] = q.Get("code_challenge")
	f.redirects[code] = q.Get("redirect_uri")
	f.mu.Unlock()
	state := q.Get("state")
	if f.badState {
		state = "forged"
	}
	http.Redirect(w, r, q.Get("redirect_uri")+"?"+url.Values{"code": {code}, "state": {state}}.Encode(), http.StatusFound)
}

func (f *fakeIdP) issue(w http.ResponseWriter) {
	f.n++
	at, rt := fmt.Sprintf("at-%d", f.n), fmt.Sprintf("rt-%d", f.n)
	f.valid[at], f.refresh[rt] = true, true
	json.NewEncoder(w).Encode(map[string]any{"access_token": at, "refresh_token": rt, "token_type": "Bearer", "expires_in": f.expiresIn})
}

func oauthErr(w http.ResponseWriter, code string) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusBadRequest)
	json.NewEncoder(w).Encode(map[string]string{"error": code})
}

func (f *fakeIdP) token(w http.ResponseWriter, r *http.Request) {
	r.ParseForm()
	f.mu.Lock()
	defer f.mu.Unlock()
	w.Header().Set("Content-Type", "application/json")
	if r.PostForm.Get("client_id") != "test-client" {
		oauthErr(w, "invalid_client")
		return
	}
	switch r.PostForm.Get("grant_type") {
	case "authorization_code":
		code := r.PostForm.Get("code")
		sum := sha256.Sum256([]byte(r.PostForm.Get("code_verifier")))
		want, ok := f.challenges[code]
		if !ok || base64.RawURLEncoding.EncodeToString(sum[:]) != want || r.PostForm.Get("redirect_uri") != f.redirects[code] {
			oauthErr(w, "invalid_grant")
			return
		}
		delete(f.challenges, code)
		f.issue(w)
	case "refresh_token":
		rt := r.PostForm.Get("refresh_token")
		if !f.refresh[rt] {
			oauthErr(w, "invalid_grant")
			return
		}
		delete(f.refresh, rt)
		f.issue(w)
	case "urn:ietf:params:oauth:grant-type:device_code":
		f.devicePoll++
		if f.devicePoll == 1 {
			oauthErr(w, "authorization_pending")
			return
		}
		f.issue(w)
	default:
		oauthErr(w, "unsupported_grant_type")
	}
}

func (f *fakeIdP) config(out *bytes.Buffer) Config {
	return Config{
		Issuer: f.srv.URL, ClientID: "test-client", APIURL: "http://airavata.test",
		Out: out,
		OpenBrowser: func(u string) error {
			resp, err := http.Get(u)
			if err == nil {
				resp.Body.Close()
			}
			return err
		},
		LoginTimeout: 20 * time.Second,
	}
}

func setup(t *testing.T) (*fakeIdP, Config, *bytes.Buffer) {
	keyring.MockInit()
	t.Setenv(EnvToken, "")
	f := newFakeIdP(t)
	out := &bytes.Buffer{}
	return f, f.config(out), out
}

func TestBrowserLoginEndToEnd(t *testing.T) {
	_, cfg, out := setup(t)
	p, err := Login(context.Background(), cfg, false)
	if err != nil {
		t.Fatal(err)
	}
	if p.Subject != "cilogon:12345" || p.RawSubject != rawSub || p.Email != "alice@example.org" || p.Name != "Alice Doe" {
		t.Fatalf("principal = %+v", p)
	}
	if !strings.Contains(out.String(), "/authorize?") || strings.Contains(out.String(), "at-") {
		t.Fatalf("unexpected login output: %q", out.String())
	}
	e, err := loadEntry(cfg)
	if err != nil {
		t.Fatal(err)
	}
	if e.AccessToken == "" || e.RefreshToken == "" || e.Subject != rawSub || e.ClientID != "test-client" {
		t.Fatalf("stored entry = %+v", e)
	}
	src, p2, err := NewTokenSource(context.Background(), cfg)
	if err != nil {
		t.Fatal(err)
	}
	if p2.Subject != "cilogon:12345" {
		t.Fatalf("subject = %q", p2.Subject)
	}
	if at, err := src.Token(context.Background()); err != nil || at != e.AccessToken {
		t.Fatalf("token = %q, %v", at, err)
	}
}

func TestBrowserLoginStateMismatch(t *testing.T) {
	f, cfg, _ := setup(t)
	f.badState = true
	_, err := Login(context.Background(), cfg, false)
	if err == nil || !strings.Contains(err.Error(), "state mismatch") {
		t.Fatalf("err = %v", err)
	}
	if _, err := loadEntry(cfg); !errors.Is(err, ErrNotLoggedIn) {
		t.Fatalf("expected nothing stored, got %v", err)
	}
}

func TestBrowserLoginTimeout(t *testing.T) {
	_, cfg, _ := setup(t)
	cfg.OpenBrowser = func(string) error { return errors.New("no browser") }
	cfg.LoginTimeout = 200 * time.Millisecond
	if _, err := Login(context.Background(), cfg, false); err == nil || !strings.Contains(err.Error(), "timed out") {
		t.Fatalf("err = %v", err)
	}
}

func TestDeviceLogin(t *testing.T) {
	f, cfg, out := setup(t)
	p, err := Login(context.Background(), cfg, true)
	if err != nil {
		t.Fatal(err)
	}
	if p.Subject != "cilogon:12345" {
		t.Fatalf("subject = %q", p.Subject)
	}
	if !strings.Contains(out.String(), "ABCD-EFGH") || !strings.Contains(out.String(), "/device/verify") {
		t.Fatalf("output = %q", out.String())
	}
	if f.devicePoll != 2 {
		t.Fatalf("device polls = %d, want 2", f.devicePoll)
	}
	if _, err := loadEntry(cfg); err != nil {
		t.Fatal(err)
	}
}

func TestRefreshOnExpiryWritesBackToKeyring(t *testing.T) {
	f, cfg, _ := setup(t)
	f.refresh["rt-old"] = true
	full, _ := cfg.withDefaults()
	if err := saveEntry(full, &entry{AccessToken: "at-old", RefreshToken: "rt-old", TokenType: "Bearer",
		Expiry: time.Now().Add(-time.Hour), Issuer: full.Issuer, ClientID: full.ClientID, APIURL: full.APIURL, Subject: rawSub}); err != nil {
		t.Fatal(err)
	}
	src, p, err := NewTokenSource(context.Background(), cfg)
	if err != nil {
		t.Fatal(err)
	}
	if p.Subject != "cilogon:12345" {
		t.Fatalf("subject = %q", p.Subject)
	}
	at, err := src.Token(context.Background())
	if err != nil || at == "at-old" {
		t.Fatalf("token = %q, %v", at, err)
	}
	e, err := loadEntry(cfg)
	if err != nil {
		t.Fatal(err)
	}
	if e.AccessToken != at || e.RefreshToken == "rt-old" || !f.refresh[e.RefreshToken] || !e.Expiry.After(time.Now()) {
		t.Fatalf("keyring not updated: %+v", e)
	}
	if f.refresh["rt-old"] {
		t.Fatal("old refresh token should have been consumed")
	}
}

func TestRefreshFailureAsksForLogin(t *testing.T) {
	_, cfg, _ := setup(t)
	full, _ := cfg.withDefaults()
	saveEntry(full, &entry{AccessToken: "at-old", RefreshToken: "rt-revoked", Expiry: time.Now().Add(-time.Hour),
		Issuer: full.Issuer, ClientID: full.ClientID, APIURL: full.APIURL, Subject: rawSub})
	_, _, err := NewTokenSource(context.Background(), cfg)
	if err == nil || !strings.Contains(err.Error(), "airavata-mcp login") || strings.Contains(err.Error(), "rt-revoked") {
		t.Fatalf("err = %v", err)
	}
}

func TestEnvTokenOverride(t *testing.T) {
	f, cfg, _ := setup(t)
	f.valid["env-token"] = true
	t.Setenv(EnvToken, "env-token")
	src, p, err := NewTokenSource(context.Background(), cfg)
	if err != nil {
		t.Fatal(err)
	}
	if p.Subject != "cilogon:12345" {
		t.Fatalf("subject = %q", p.Subject)
	}
	if at, _ := src.Token(context.Background()); at != "env-token" {
		t.Fatalf("token = %q", at)
	}
}

func TestRootTokenRefused(t *testing.T) {
	_, cfg, _ := setup(t)
	root := "8c6c3e1e-2f4f-4f39-9d0b-0b8b1c1f1a2b"
	t.Setenv(EnvToken, root)
	_, _, err := NewTokenSource(context.Background(), cfg)
	if err == nil || !strings.Contains(err.Error(), "root tokens are not supported") || strings.Contains(err.Error(), root) {
		t.Fatalf("err = %v", err)
	}
	if _, err := Identify(context.Background(), cfg, root); err == nil || !strings.Contains(err.Error(), "root tokens are not supported") {
		t.Fatalf("Identify err = %v", err)
	}
}

func TestNotLoggedIn(t *testing.T) {
	_, cfg, _ := setup(t)
	if _, _, err := NewTokenSource(context.Background(), cfg); !errors.Is(err, ErrNotLoggedIn) {
		t.Fatalf("err = %v", err)
	}
}

func TestLogoutIdempotent(t *testing.T) {
	_, cfg, _ := setup(t)
	if _, err := Login(context.Background(), cfg, false); err != nil {
		t.Fatal(err)
	}
	for i := 0; i < 2; i++ {
		if err := Logout(cfg); err != nil {
			t.Fatalf("logout %d: %v", i, err)
		}
	}
	if _, err := loadEntry(cfg); !errors.Is(err, ErrNotLoggedIn) {
		t.Fatalf("err = %v", err)
	}
}

func TestNormalizeSubject(t *testing.T) {
	if got := NormalizeSubject(rawSub); got != "cilogon:12345" {
		t.Fatalf("got %q", got)
	}
	if got := NormalizeSubject("someone"); got != "someone" {
		t.Fatalf("got %q", got)
	}
}

func TestServiceAccountIdentifiedViaAiravataServer(t *testing.T) {
	const saToken = "airavata_sa_abcdef1234567890abcdef1234567890"
	var receivedAuthHeader string
	mockServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path != "/api/v1/users/me" {
			http.NotFound(w, r)
			return
		}
		receivedAuthHeader = r.Header.Get("Authorization")
		if receivedAuthHeader != "Bearer "+saToken {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}
		firstName := "Pipeline Worker"
		json.NewEncoder(w).Encode(map[string]any{
			"userId":    "sa:pipeline-worker",
			"firstName": firstName,
			"status":    "ACTIVE",
		})
	}))
	defer mockServer.Close()

	cfg := Config{
		APIURL:     mockServer.URL,
		HTTPClient: mockServer.Client(),
	}

	p, err := Identify(context.Background(), cfg, saToken)
	if err != nil {
		t.Fatalf("Identify error: %v", err)
	}
	if p.Subject != "sa:pipeline-worker" {
		t.Errorf("p.Subject = %q, want sa:pipeline-worker", p.Subject)
	}
	if p.RawSubject != "sa:pipeline-worker" {
		t.Errorf("p.RawSubject = %q, want sa:pipeline-worker", p.RawSubject)
	}
	if p.Name != "Pipeline Worker" {
		t.Errorf("p.Name = %q, want Pipeline Worker", p.Name)
	}
	if receivedAuthHeader != "Bearer "+saToken {
		t.Errorf("Authorization header = %q, want Bearer %s", receivedAuthHeader, saToken)
	}
}

func TestServiceAccountIdentifiedViaCurrentUserResponse(t *testing.T) {
	const saToken = "airavata_sa_abcdef1234567890abcdef1234567890"
	mockServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path != "/api/v1/users/me" {
			http.NotFound(w, r)
			return
		}
		if r.Header.Get("Authorization") != "Bearer "+saToken {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}
		firstName := "Pipeline Worker"
		json.NewEncoder(w).Encode(map[string]any{
			"userId": "sa:pipeline-worker",
			"status": "ACTIVE",
			"user": map[string]any{
				"userId":    "sa:pipeline-worker",
				"firstName": firstName,
			},
		})
	}))
	defer mockServer.Close()

	cfg := Config{
		APIURL:     mockServer.URL,
		HTTPClient: mockServer.Client(),
	}

	p, err := Identify(context.Background(), cfg, saToken)
	if err != nil {
		t.Fatalf("Identify error: %v", err)
	}
	if p.Subject != "sa:pipeline-worker" {
		t.Errorf("p.Subject = %q, want sa:pipeline-worker", p.Subject)
	}
	if p.Name != "Pipeline Worker" {
		t.Errorf("p.Name = %q, want Pipeline Worker", p.Name)
	}
}

func TestServiceAccountRejectedByServer(t *testing.T) {
	const saToken = "airavata_sa_revokedorinvalid"
	mockServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusUnauthorized)
	}))
	defer mockServer.Close()

	cfg := Config{
		APIURL:     mockServer.URL,
		HTTPClient: mockServer.Client(),
	}

	_, err := Identify(context.Background(), cfg, saToken)
	if err == nil || !strings.Contains(err.Error(), "rejected by airavata-server (HTTP 401)") {
		t.Fatalf("expected 401 error, got: %v", err)
	}
}

func TestServiceAccountNonServiceAccountIDRejected(t *testing.T) {
	const saToken = "airavata_sa_impersonating"
	mockServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		json.NewEncoder(w).Encode(map[string]any{
			"userId":    "root",
			"firstName": "Root",
			"status":    "ACTIVE",
		})
	}))
	defer mockServer.Close()

	cfg := Config{
		APIURL:     mockServer.URL,
		HTTPClient: mockServer.Client(),
	}

	_, err := Identify(context.Background(), cfg, saToken)
	if err == nil || !strings.Contains(err.Error(), "not a service account") {
		t.Fatalf("expected non-service-account error, got: %v", err)
	}
}

func TestNewTokenSourceWithServiceAccountEnvToken(t *testing.T) {
	const saToken = "airavata_sa_fromenv"
	mockServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path == "/api/v1/users/me" && r.Header.Get("Authorization") == "Bearer "+saToken {
			json.NewEncoder(w).Encode(map[string]any{
				"userId":    "sa:ci-bot",
				"firstName": "CI Bot",
				"status":    "ACTIVE",
			})
			return
		}
		w.WriteHeader(http.StatusUnauthorized)
	}))
	defer mockServer.Close()

	t.Setenv(EnvToken, saToken)
	cfg := Config{
		APIURL:     mockServer.URL,
		HTTPClient: mockServer.Client(),
	}

	ts, p, err := NewTokenSource(context.Background(), cfg)
	if err != nil {
		t.Fatalf("NewTokenSource error: %v", err)
	}
	tok, err := ts.Token(context.Background())
	if err != nil {
		t.Fatalf("Token() error: %v", err)
	}
	if tok != saToken {
		t.Errorf("tok = %q, want %q", tok, saToken)
	}
	if p.Subject != "sa:ci-bot" {
		t.Errorf("p.Subject = %q, want sa:ci-bot", p.Subject)
	}
}

func TestServiceAccountNeverQueriesIdP(t *testing.T) {
	idpHits := 0
	fakeIdpSrv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		idpHits++
		http.Error(w, "should never be reached", http.StatusInternalServerError)
	}))
	defer fakeIdpSrv.Close()

	const saToken = "airavata_sa_secrettoken"
	mockServer := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if r.URL.Path == "/api/v1/users/me" {
			json.NewEncoder(w).Encode(map[string]any{
				"userId": "sa:bot",
				"status": "ACTIVE",
			})
			return
		}
		http.NotFound(w, r)
	}))
	defer mockServer.Close()

	cfg := Config{
		Issuer:     fakeIdpSrv.URL,
		APIURL:     mockServer.URL,
		HTTPClient: http.DefaultClient,
	}

	p, err := Identify(context.Background(), cfg, saToken)
	if err != nil {
		t.Fatalf("Identify error: %v", err)
	}
	if p.Subject != "sa:bot" {
		t.Errorf("Subject = %q, want sa:bot", p.Subject)
	}
	if idpHits != 0 {
		t.Errorf("IdP was contacted %d times; service account tokens must never be sent to IdP", idpHits)
	}
}
