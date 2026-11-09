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
	"encoding/json"
	"errors"
	"fmt"
	"log/slog"
	"net/http"
	"net/url"
	"slices"
	"strings"
	"time"

	"github.com/coreos/go-oidc/v3/oidc"
	"golang.org/x/oauth2"

	"github.com/apache/airavata/internal/httpx"
)

// Introspector validates a bearer token and resolves it to a principal.
type Introspector interface {
	Introspect(ctx context.Context, token string) (*Principal, error)
}

// ErrInvalidToken reports a token that introspection rejected.
var ErrInvalidToken = fmt.Errorf("invalid bearer token")

// CILogonIntrospector validates tokens against CILogon.
//
// CILogon access tokens for standard OAuth clients are opaque strings rather than
// self-contained JWTs, so they cannot be verified locally against a JWK set — each
// one is checked by calling CILogon's RFC 7662 introspection endpoint. Introspection
// returns only a small fixed claim set, so profile attributes come from a second call
// to userinfo with the same token.
type CILogonIntrospector struct {
	HTTPClient       *http.Client
	IntrospectionURI string
	UserInfoURI      string
	ClientID         string
	ClientSecret     string
	Roles            RoleLookup

	// Root, when set, short-circuits introspection for the bootstrap token.
	Root *RootTokenProvider

	// ServiceAccounts, when set, resolves service account tokens locally. A token
	// carrying the service account prefix is never forwarded to CILogon, whether or
	// not this is set.
	ServiceAccounts ServiceAccountResolver

	// IDTokens, when set, verifies ID tokens issued to this client locally, in place of
	// introspection and userinfo.
	IDTokens *oidc.IDTokenVerifier
}

// NewCILogonIntrospector builds an introspector with a bounded HTTP client, so a
// hanging identity provider cannot pin request goroutines indefinitely.
func NewCILogonIntrospector(introspectionURI, userInfoURI, clientID, clientSecret string, roles RoleLookup, root *RootTokenProvider) *CILogonIntrospector {
	return &CILogonIntrospector{
		HTTPClient:       &http.Client{Timeout: 10 * time.Second},
		IntrospectionURI: introspectionURI,
		UserInfoURI:      userInfoURI,
		ClientID:         clientID,
		ClientSecret:     clientSecret,
		Roles:            roles,
		Root:             root,
	}
}

// Introspect implements Introspector.
func (c *CILogonIntrospector) Introspect(ctx context.Context, token string) (*Principal, error) {
	if c.Root.Matches(token) {
		return c.Root.Principal(), nil
	}
	if IsServiceAccountToken(token) {
		if c.ServiceAccounts == nil {
			return nil, ErrInvalidToken
		}
		return c.ServiceAccounts.ResolveServiceAccount(ctx, token)
	}
	if c.IDTokens != nil {
		var claims map[string]any
		if idToken, err := c.IDTokens.Verify(ctx, token); err == nil && idToken.Subject != "" && idToken.Claims(&claims) == nil {
			// An ID token has no username claim, and preferred_username is not unique.
			claims["username"] = idToken.Subject
			return c.principal(ctx, claims)
		}
	}

	attributes, err := c.introspect(ctx, token)
	if err != nil {
		return nil, err
	}

	// Introspection claims win on conflict; userinfo only fills in gaps.
	for k, v := range c.userInfo(ctx, token) {
		if _, exists := attributes[k]; !exists {
			attributes[k] = v
		}
	}
	return c.principal(ctx, attributes)
}

func (c *CILogonIntrospector) principal(ctx context.Context, attributes map[string]any) (*Principal, error) {
	username := NormalizeUsername(firstString(attributes, "username", "preferred_username", "sub"))
	// The service account namespace is reserved: an identity provider that reported
	// such a username would otherwise authenticate as that service account.
	if username == "" || IsServiceAccountID(username) {
		return nil, ErrInvalidToken
	}

	return &Principal{
		Name:        username,
		Authorities: c.Roles.Roles(ctx, username),
		Attributes:  attributes,
	}, nil
}

func (c *CILogonIntrospector) introspect(ctx context.Context, token string) (map[string]any, error) {
	form := url.Values{"token": {token}}
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, c.IntrospectionURI, strings.NewReader(form.Encode()))
	if err != nil {
		return nil, fmt.Errorf("build introspection request: %w", err)
	}
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	req.Header.Set("Accept", "application/json")
	req.SetBasicAuth(c.ClientID, c.ClientSecret)

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("call introspection endpoint: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("%w: introspection returned %s", ErrInvalidToken, resp.Status)
	}

	var claims map[string]any
	if err := json.NewDecoder(resp.Body).Decode(&claims); err != nil {
		return nil, fmt.Errorf("decode introspection response: %w", err)
	}
	if active, _ := claims["active"].(bool); !active {
		return nil, ErrInvalidToken
	}
	return claims, nil
}

// userInfo fetches profile claims. A failure here is not fatal — the token is already
// known to be valid, and the profile only enriches the principal — so it degrades to
// no extra claims, as the Java client did.
func (c *CILogonIntrospector) userInfo(ctx context.Context, token string) map[string]any {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, c.UserInfoURI, nil)
	if err != nil {
		return nil
	}
	req.Header.Set("Authorization", "Bearer "+token)
	req.Header.Set("Accept", "application/json")

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		slog.Warn("failed to fetch CILogon userinfo; proceeding without profile claims", "error", err)
		return nil
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		slog.Warn("CILogon userinfo returned a non-OK status; proceeding without profile claims",
			"status", resp.Status)
		return nil
	}
	var claims map[string]any
	if err := json.NewDecoder(resp.Body).Decode(&claims); err != nil {
		slog.Warn("failed to decode CILogon userinfo; proceeding without profile claims", "error", err)
		return nil
	}
	return claims
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

// CILogonIssuer is the default OIDC issuer.
const CILogonIssuer = "https://cilogon.org"

// CILogonScopes are the scopes every sign-in requests.
var CILogonScopes = []string{"openid", "email", "profile", "org.cilogon.userinfo", "offline_access"}

// SignIn redeems CILogon grants with the server's client secret.
type SignIn struct {
	oauth   oauth2.Config
	origins []string
	client  *http.Client
}

// NewSignIn returns nil without a client secret.
func NewSignIn(endpoint oauth2.Endpoint, clientID, clientSecret string, redirectOrigins []string, client *http.Client) *SignIn {
	if clientSecret == "" {
		return nil
	}
	return &SignIn{
		oauth:   OAuthConfig(endpoint, clientID, clientSecret, CILogonScopes),
		origins: redirectOrigins,
		client:  client,
	}
}

// OAuthConfig is a CILogon client's OAuth configuration.
func OAuthConfig(endpoint oauth2.Endpoint, clientID, clientSecret string, scopes []string) oauth2.Config {
	// One auth style, or oauth2 sends every refused grant to the issuer twice while it probes for one.
	endpoint.AuthStyle = oauth2.AuthStyleInParams
	return oauth2.Config{ClientID: clientID, ClientSecret: clientSecret, Endpoint: endpoint, Scopes: scopes}
}

type SignInConfig struct {
	AuthorizationEndpoint string `json:"authorizationEndpoint"`
	ClientID              string `json:"clientId"`
	Scope                 string `json:"scope"`
}

func (s *SignIn) Config() SignInConfig {
	return SignInConfig{AuthorizationEndpoint: s.oauth.Endpoint.AuthURL, ClientID: s.oauth.ClientID, Scope: strings.Join(s.oauth.Scopes, " ")}
}

type Tokens struct {
	IDToken      string `json:"idToken"`
	RefreshToken string `json:"refreshToken,omitempty"`
	ExpiresIn    int64  `json:"expiresIn"`
}

type Grant struct {
	Code         string `json:"code"`
	CodeVerifier string `json:"codeVerifier"`
	RedirectURI  string `json:"redirectUri"`
	RefreshToken string `json:"refreshToken"`
	DeviceCode   string `json:"deviceCode"`
}

func (s *SignIn) Redeem(ctx context.Context, grant Grant) (Tokens, error) {
	ctx = context.WithValue(ctx, oauth2.HTTPClient, s.client)
	switch {
	case grant.RefreshToken != "":
		return tokens(s.oauth.TokenSource(ctx, &oauth2.Token{RefreshToken: grant.RefreshToken}).Token())
	case grant.DeviceCode != "":
		return tokens(s.oauth.Exchange(ctx, "", oauth2.SetAuthURLParam("grant_type", "urn:ietf:params:oauth:grant-type:device_code"), oauth2.SetAuthURLParam("device_code", grant.DeviceCode)))
	}
	if parsed, err := url.Parse(grant.RedirectURI); grant.CodeVerifier == "" || err != nil || !slices.Contains(s.origins, parsed.Scheme+"://"+parsed.Host) {
		return Tokens{}, httpx.BadRequest("Send a refreshToken, a deviceCode, or a code with codeVerifier and an allowed redirectUri")
	}
	return tokens(s.oauth.Exchange(ctx, grant.Code, oauth2.VerifierOption(grant.CodeVerifier), oauth2.SetAuthURLParam("redirect_uri", grant.RedirectURI)))
}

type Device struct {
	DeviceCode      string `json:"deviceCode"`
	UserCode        string `json:"userCode"`
	VerificationURI string `json:"verificationUri"`
	Interval        int64  `json:"interval"`
}

func (s *SignIn) DeviceStart(ctx context.Context) (Device, error) {
	ctx = context.WithValue(ctx, oauth2.HTTPClient, s.client)
	if s.oauth.Endpoint.DeviceAuthURL == "" {
		return Device{}, &httpx.Error{Status: http.StatusNotImplemented, Message: "The identity provider offers no device flow"}
	}
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
	if idToken == "" {
		return Tokens{}, &httpx.Error{Status: http.StatusBadGateway, Message: "The identity provider returned no ID token"}
	}
	return Tokens{IDToken: idToken, RefreshToken: token.RefreshToken}, nil
}

func classify(err error) error {
	var refusal *oauth2.RetrieveError
	switch {
	case !errors.As(err, &refusal) || refusal.ErrorCode == "" || refusal.ErrorCode == "invalid_client":
		return &httpx.Error{Status: http.StatusBadGateway, Message: "The identity provider refused or did not answer", Err: err}
	case refusal.ErrorCode == "authorization_pending" || refusal.ErrorCode == "slow_down":
		return &httpx.Error{Status: http.StatusAccepted, Message: "The device code is not approved yet"}
	}
	return httpx.BadRequest("The identity provider rejected the grant: %s", refusal.ErrorCode)
}
