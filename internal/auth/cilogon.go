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
	"cmp"
	"context"
	"errors"
	"fmt"
	"net/http"
	"net/url"
	"slices"
	"strings"

	"github.com/apache/airavata/internal/httpx"
	"github.com/coreos/go-oidc/v3/oidc"
	"golang.org/x/oauth2"
)

// Introspector validates a bearer token and resolves it to a principal.
type Introspector interface {
	Introspect(ctx context.Context, token string) (*Principal, error)
}

// ErrInvalidToken reports a token that introspection rejected.
var ErrInvalidToken = fmt.Errorf("invalid bearer token")

type IDTokenVerifier struct {
	// Verifier has no keys when no OIDC client is configured, so only the root token is admitted.
	Verifier *oidc.IDTokenVerifier
	Roles    RoleLookup
	Root     *RootTokenProvider
}

// Introspect implements Introspector.
func (v *IDTokenVerifier) Introspect(ctx context.Context, token string) (*Principal, error) {
	if v.Root.Matches(token) {
		return v.Root.Principal(), nil
	}
	idToken, err := v.Verifier.Verify(ctx, token)
	if err != nil {
		return nil, fmt.Errorf("%w: %v", ErrInvalidToken, err)
	}
	var claims map[string]any
	if err := idToken.Claims(&claims); err != nil {
		return nil, fmt.Errorf("%w: %v", ErrInvalidToken, err)
	}
	username := NormalizeUsername(firstString(claims, "username", "preferred_username", "sub"))
	if username == "" {
		return nil, ErrInvalidToken
	}
	return &Principal{Name: username, Authorities: v.Roles.Roles(ctx, username), Attributes: claims}, nil
}

// NormalizeUsername reduces a CILogon identifier to the form stored as iam/model.User.ID.
//
// CILogon sometimes reports usernames as full URIs like
// "http://cilogon.org/serverE/users/12345"; those collapse to "cilogon:12345" so that
// a user is looked up the same way no matter which claim carried the identity.
func NormalizeUsername(username string) string {
	if !strings.HasPrefix(username, "http://cilogon.org") {
		return username
	}
	if i := strings.LastIndexByte(username, '/'); i >= 0 && i < len(username)-1 {
		return "cilogon:" + username[i+1:]
	}
	return username
}

func firstString(m map[string]any, keys ...string) string {
	for _, k := range keys {
		if s, ok := m[k].(string); ok && s != "" {
			return s
		}
	}
	return ""
}

const signInScope = "openid email profile org.cilogon.userinfo offline_access"

type SignIn struct {
	oauth   oauth2.Config
	origins []string
}

func NewSignIn(endpoint oauth2.Endpoint, clientID, clientSecret string, redirectOrigins []string) *SignIn {
	// One auth style, or oauth2 sends every refused grant to the issuer twice while it probes for one.
	endpoint.AuthStyle = oauth2.AuthStyleInParams
	return &SignIn{
		oauth:   oauth2.Config{ClientID: clientID, ClientSecret: clientSecret, Endpoint: endpoint, Scopes: strings.Fields(signInScope)},
		origins: redirectOrigins,
	}
}

func (s *SignIn) Config() map[string]string {
	return map[string]string{"authorization_endpoint": s.oauth.Endpoint.AuthURL, "client_id": s.oauth.ClientID, "scope": signInScope}
}

type Tokens struct {
	IDToken      string `json:"id_token"`
	RefreshToken string `json:"refresh_token,omitempty"`
	ExpiresIn    int64  `json:"expires_in"`
}

type Grant struct {
	Code         string `json:"code"`
	CodeVerifier string `json:"code_verifier"`
	RedirectURI  string `json:"redirect_uri"`
	RefreshToken string `json:"refresh_token"`
	DeviceCode   string `json:"device_code"`
}

func (s *SignIn) Redeem(ctx context.Context, grant Grant) (Tokens, error) {
	switch {
	case grant.RefreshToken != "":
		return tokens(s.oauth.TokenSource(ctx, &oauth2.Token{RefreshToken: grant.RefreshToken}).Token())
	case grant.DeviceCode != "":
		return tokens(s.oauth.Exchange(ctx, "", oauth2.SetAuthURLParam("grant_type", "urn:ietf:params:oauth:grant-type:device_code"), oauth2.SetAuthURLParam("device_code", grant.DeviceCode)))
	}
	if parsed, err := url.Parse(grant.RedirectURI); grant.CodeVerifier == "" || err != nil || !slices.Contains(s.origins, parsed.Scheme+"://"+parsed.Host) {
		return Tokens{}, httpx.BadRequest("A refresh_token, a device_code, or a code with its code_verifier and a redirect_uri on an allowed origin is required")
	}
	return tokens(s.oauth.Exchange(ctx, grant.Code, oauth2.VerifierOption(grant.CodeVerifier), oauth2.SetAuthURLParam("redirect_uri", grant.RedirectURI)))
}

type Device struct {
	DeviceCode      string `json:"device_code"`
	UserCode        string `json:"user_code"`
	VerificationURI string `json:"verification_uri_complete"`
	Interval        int64  `json:"interval"`
}

func (s *SignIn) DeviceStart(ctx context.Context) (Device, error) {
	started, err := s.oauth.DeviceAuth(ctx, oauth2.SetAuthURLParam("client_secret", s.oauth.ClientSecret))
	if err != nil {
		return Device{}, classify(err)
	}
	return Device{DeviceCode: started.DeviceCode, UserCode: started.UserCode, VerificationURI: cmp.Or(started.VerificationURIComplete, started.VerificationURI), Interval: max(started.Interval, 5)}, nil
}

func tokens(token *oauth2.Token, err error) (Tokens, error) {
	if err != nil {
		return Tokens{}, classify(err)
	}
	idToken, _ := token.Extra("id_token").(string)
	return Tokens{IDToken: idToken, RefreshToken: token.RefreshToken, ExpiresIn: token.ExpiresIn}, nil
}

func classify(err error) error {
	var refusal *oauth2.RetrieveError
	switch {
	case !errors.As(err, &refusal) || refusal.ErrorCode == "":
		return &httpx.Error{Status: http.StatusBadGateway, Message: "The identity provider is unavailable", Err: err}
	case refusal.ErrorCode == "authorization_pending" || refusal.ErrorCode == "slow_down":
		return &httpx.Error{Status: http.StatusAccepted, Message: "The device code is not approved yet"}
	}
	return httpx.BadRequest("The identity provider rejected the grant: %s", refusal.ErrorCode)
}
