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
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"net/url"
	"strings"
	"sync"
	"time"

	"golang.org/x/oauth2"

	"github.com/apache/airavata/internal/httpx"
)

// SignInScopes are the scopes the dashboard asks CILogon for: the profile claims the
// introspector reads, and offline_access for a refresh token, since CILogon access
// tokens are short-lived.
var SignInScopes = []string{"openid", "email", "profile", "org.cilogon.userinfo", "offline_access"}

// DashboardCallbackPath is the only redirect URI path the exchange accepts: the
// dashboard's sign-in callback page.
const DashboardCallbackPath = "/dashboard/login/callback/"

// SignIn lets a browser sign in with CILogon through the authorization code flow
// with PKCE. The browser sends the user to CILogon itself; this server performs the
// code exchange and refreshes, because they need the client secret, which never
// leaves it, and because CILogon's token endpoint does not answer browsers.
//
// The client is the same one introspection uses, so the access tokens it returns are
// ones the introspector accepts. Its redirect URIs, registered with CILogon, must
// include <dashboard origin>/dashboard/login/callback/.
type SignIn struct {
	Issuer       string
	ClientID     string
	ClientSecret string
	HTTPClient   *http.Client

	mu        sync.Mutex
	endpoints *signInEndpoints
}

type signInEndpoints struct {
	Authorization string `json:"authorization_endpoint"`
	Token         string `json:"token_endpoint"`
}

// NewSignIn returns the sign-in routes for a CILogon client.
func NewSignIn(issuer, clientID, clientSecret string) *SignIn {
	return &SignIn{
		Issuer:       strings.TrimRight(issuer, "/"),
		ClientID:     clientID,
		ClientSecret: clientSecret,
		HTTPClient:   &http.Client{Timeout: 15 * time.Second},
	}
}

// Enabled reports whether a client is configured to sign in with.
func (s *SignIn) Enabled() bool {
	return s != nil && s.ClientID != "" && s.ClientSecret != ""
}

// Register mounts the sign-in routes. They take no bearer token.
func (s *SignIn) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/auth/cilogon", s.config)
	mux.HandleFunc("POST /api/v1/auth/cilogon/token", s.exchange)
	mux.HandleFunc("POST /api/v1/auth/cilogon/refresh", s.refresh)
}

// SignInConfig is what the browser needs to send a user to CILogon.
type SignInConfig struct {
	Enabled               bool   `json:"enabled"`
	AuthorizationEndpoint string `json:"authorizationEndpoint,omitempty"`
	ClientID              string `json:"clientId,omitempty"`
	Scope                 string `json:"scope,omitempty"`
}

// SignInTokens is the outcome of an exchange or refresh. ExpiresIn is in seconds; 0
// when CILogon did not say.
type SignInTokens struct {
	AccessToken  string `json:"accessToken"`
	RefreshToken string `json:"refreshToken,omitempty"`
	ExpiresIn    int64  `json:"expiresIn,omitempty"`
}

type exchangeRequest struct {
	Code         string `json:"code"`
	CodeVerifier string `json:"codeVerifier"`
	RedirectURI  string `json:"redirectUri"`
}

type refreshRequest struct {
	RefreshToken string `json:"refreshToken"`
}

func (s *SignIn) config(w http.ResponseWriter, r *http.Request) {
	if !s.Enabled() {
		httpx.WriteJSON(w, http.StatusOK, SignInConfig{Enabled: false})
		return
	}
	endpoints, err := s.discover(r.Context())
	if err != nil {
		httpx.WriteError(w, r, upstream(err))
		return
	}
	httpx.WriteJSON(w, http.StatusOK, SignInConfig{
		Enabled:               true,
		AuthorizationEndpoint: endpoints.Authorization,
		ClientID:              s.ClientID,
		Scope:                 strings.Join(SignInScopes, " "),
	})
}

func (s *SignIn) exchange(w http.ResponseWriter, r *http.Request) {
	var req exchangeRequest
	if err := httpx.DecodeJSON(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	if !s.Enabled() {
		httpx.WriteError(w, r, httpx.NotFound("CILogon sign-in is not configured"))
		return
	}
	if req.Code == "" || req.CodeVerifier == "" || !callbackURI(req.RedirectURI) {
		httpx.WriteError(w, r, httpx.BadRequest("code, codeVerifier and the dashboard's redirectUri are required"))
		return
	}
	cfg, err := s.oauth(r.Context(), req.RedirectURI)
	if err != nil {
		httpx.WriteError(w, r, upstream(err))
		return
	}
	token, err := cfg.Exchange(s.ctx(r.Context()), req.Code, oauth2.VerifierOption(req.CodeVerifier))
	s.writeTokens(w, r, token, err)
}

func (s *SignIn) refresh(w http.ResponseWriter, r *http.Request) {
	var req refreshRequest
	if err := httpx.DecodeJSON(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	if !s.Enabled() {
		httpx.WriteError(w, r, httpx.NotFound("CILogon sign-in is not configured"))
		return
	}
	if req.RefreshToken == "" {
		httpx.WriteError(w, r, httpx.BadRequest("refreshToken is required"))
		return
	}
	cfg, err := s.oauth(r.Context(), "")
	if err != nil {
		httpx.WriteError(w, r, upstream(err))
		return
	}
	token, err := cfg.TokenSource(s.ctx(r.Context()), &oauth2.Token{RefreshToken: req.RefreshToken}).Token()
	s.writeTokens(w, r, token, err)
}

func (s *SignIn) writeTokens(w http.ResponseWriter, r *http.Request, token *oauth2.Token, err error) {
	var rejected *oauth2.RetrieveError
	switch {
	case errors.As(err, &rejected):
		// CILogon's own message is not echoed: it can name the client.
		httpx.WriteError(w, r, &httpx.Error{Status: http.StatusBadRequest,
			Message: "CILogon rejected the authorization code or refresh token", Err: err})
		return
	case err != nil:
		httpx.WriteError(w, r, upstream(err))
		return
	case token.AccessToken == "":
		httpx.WriteError(w, r, upstream(errors.New("CILogon returned no access token")))
		return
	}
	out := SignInTokens{AccessToken: token.AccessToken, RefreshToken: token.RefreshToken}
	if !token.Expiry.IsZero() {
		out.ExpiresIn = max(int64(time.Until(token.Expiry).Seconds()), 0)
	}
	w.Header().Set("Cache-Control", "no-store")
	httpx.WriteJSON(w, http.StatusOK, out)
}

func (s *SignIn) ctx(ctx context.Context) context.Context {
	return context.WithValue(ctx, oauth2.HTTPClient, s.HTTPClient)
}

func (s *SignIn) oauth(ctx context.Context, redirectURI string) (*oauth2.Config, error) {
	endpoints, err := s.discover(ctx)
	if err != nil {
		return nil, err
	}
	return &oauth2.Config{
		ClientID:     s.ClientID,
		ClientSecret: s.ClientSecret,
		Endpoint:     oauth2.Endpoint{AuthURL: endpoints.Authorization, TokenURL: endpoints.Token},
		RedirectURL:  redirectURI,
		Scopes:       SignInScopes,
	}, nil
}

// discover reads the issuer's endpoints once and keeps them; a failure is retried on
// the next request.
func (s *SignIn) discover(ctx context.Context) (*signInEndpoints, error) {
	s.mu.Lock()
	defer s.mu.Unlock()
	if s.endpoints != nil {
		return s.endpoints, nil
	}
	u := s.Issuer + "/.well-known/openid-configuration"
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, u, nil)
	if err != nil {
		return nil, err
	}
	resp, err := s.HTTPClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("OIDC discovery at %s: %w", u, err)
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("OIDC discovery at %s: HTTP %d", u, resp.StatusCode)
	}
	var found signInEndpoints
	if err := json.NewDecoder(io.LimitReader(resp.Body, 1<<20)).Decode(&found); err != nil {
		return nil, fmt.Errorf("OIDC discovery at %s: %w", u, err)
	}
	if found.Authorization == "" || found.Token == "" {
		return nil, fmt.Errorf("OIDC discovery at %s: no authorization or token endpoint", u)
	}
	s.endpoints = &found
	return s.endpoints, nil
}

// callbackURI accepts only the dashboard's callback page, on any http(s) origin.
// CILogon itself holds the list of registered redirect URIs and refuses a code
// issued for one to be redeemed with another.
func callbackURI(raw string) bool {
	u, err := url.Parse(raw)
	return err == nil && (u.Scheme == "https" || u.Scheme == "http") && u.Host != "" &&
		u.User == nil && u.Path == DashboardCallbackPath && u.RawQuery == "" && u.Fragment == ""
}

func upstream(err error) error {
	return &httpx.Error{Status: http.StatusBadGateway, Message: "CILogon is unavailable", Err: err}
}
