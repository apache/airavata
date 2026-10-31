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

// Package auth obtains, stores and refreshes the user's own OIDC token for airavata-mcp.
package auth

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"time"

	srvauth "github.com/apache/airavata/internal/auth"
	"golang.org/x/oauth2"
)

const (
	DefaultIssuer       = "https://cilogon.org"
	EnvToken            = "AIRAVATA_MCP_TOKEN"
	keyringService      = "airavata-mcp"
	defaultLoginTimeout = 5 * time.Minute
)

var DefaultScopes = []string{"openid", "email", "profile", "org.cilogon.userinfo", "offline_access"}

var ErrNotLoggedIn = errors.New("not logged in: run `airavata-mcp login`")

type Config struct {
	Issuer       string
	ClientID     string
	Scopes       []string
	APIURL       string
	HTTPClient   *http.Client
	Out          io.Writer
	OpenBrowser  func(url string) error
	LoginTimeout time.Duration
}

type Principal struct {
	Subject    string
	RawSubject string
	Name       string
	Email      string
}

type TokenSource interface {
	Token(ctx context.Context) (string, error)
}

func NormalizeSubject(sub string) string { return srvauth.NormalizeUsername(sub) }

func (c Config) withDefaults() (Config, error) {
	if c.Issuer == "" {
		c.Issuer = DefaultIssuer
	}
	c.Issuer = strings.TrimRight(c.Issuer, "/")
	if len(c.Scopes) == 0 {
		c.Scopes = DefaultScopes
	}
	if c.HTTPClient == nil {
		c.HTTPClient = &http.Client{Timeout: 30 * time.Second}
	}
	if c.Out == nil {
		c.Out = os.Stderr
	}
	if c.OpenBrowser == nil {
		c.OpenBrowser = openBrowser
	}
	if c.LoginTimeout <= 0 {
		c.LoginTimeout = defaultLoginTimeout
	}
	if c.ClientID == "" {
		return c, errors.New("auth: OAuth client ID is not configured")
	}
	return c, nil
}

func (c Config) ctx(ctx context.Context) context.Context {
	return context.WithValue(ctx, oauth2.HTTPClient, c.HTTPClient)
}

type discovery struct {
	Issuer                      string `json:"issuer"`
	AuthorizationEndpoint       string `json:"authorization_endpoint"`
	TokenEndpoint               string `json:"token_endpoint"`
	DeviceAuthorizationEndpoint string `json:"device_authorization_endpoint"`
	UserinfoEndpoint            string `json:"userinfo_endpoint"`
}

func discover(ctx context.Context, cfg Config) (*discovery, error) {
	u := cfg.Issuer + "/.well-known/openid-configuration"
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, u, nil)
	if err != nil {
		return nil, err
	}
	resp, err := cfg.HTTPClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("auth: OIDC discovery at %s: %w", u, err)
	}
	defer resp.Body.Close()
	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("auth: OIDC discovery at %s: HTTP %d", u, resp.StatusCode)
	}
	var d discovery
	if err := json.NewDecoder(io.LimitReader(resp.Body, 1<<20)).Decode(&d); err != nil {
		return nil, fmt.Errorf("auth: OIDC discovery at %s: %w", u, err)
	}
	if d.TokenEndpoint == "" || d.UserinfoEndpoint == "" {
		return nil, fmt.Errorf("auth: OIDC discovery at %s: missing token or userinfo endpoint", u)
	}
	return &d, nil
}

func (d *discovery) oauth(cfg Config, redirect string) *oauth2.Config {
	return &oauth2.Config{
		ClientID: cfg.ClientID,
		Endpoint: oauth2.Endpoint{
			AuthURL:       d.AuthorizationEndpoint,
			TokenURL:      d.TokenEndpoint,
			DeviceAuthURL: d.DeviceAuthorizationEndpoint,
			AuthStyle:     oauth2.AuthStyleInParams,
		},
		RedirectURL: redirect,
		Scopes:      cfg.Scopes,
	}
}

func Identify(ctx context.Context, cfg Config, accessToken string) (*Principal, error) {
	cfg, err := cfg.withDefaults()
	if err != nil {
		return nil, err
	}
	d, err := discover(ctx, cfg)
	if err != nil {
		return nil, err
	}
	return identify(ctx, cfg, d, accessToken)
}

func identify(ctx context.Context, cfg Config, d *discovery, accessToken string) (*Principal, error) {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, d.UserinfoEndpoint, nil)
	if err != nil {
		return nil, err
	}
	req.Header.Set("Authorization", "Bearer "+accessToken)
	req.Header.Set("Accept", "application/json")
	resp, err := cfg.HTTPClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("auth: userinfo request: %w", err)
	}
	defer resp.Body.Close()
	switch {
	case resp.StatusCode == http.StatusUnauthorized || resp.StatusCode == http.StatusForbidden:
		return nil, fmt.Errorf("auth: the identity provider rejected the token (HTTP %d); only your own %s token is accepted — "+
			"Airavata root tokens are not supported by airavata-mcp; run `airavata-mcp login`", resp.StatusCode, cfg.Issuer)
	case resp.StatusCode != http.StatusOK:
		return nil, fmt.Errorf("auth: userinfo returned HTTP %d", resp.StatusCode)
	}
	var claims map[string]any
	if err := json.NewDecoder(io.LimitReader(resp.Body, 1<<20)).Decode(&claims); err != nil {
		return nil, fmt.Errorf("auth: decoding userinfo: %w", err)
	}
	sub, _ := claims["sub"].(string)
	if sub == "" {
		return nil, errors.New("auth: userinfo response has no subject")
	}
	p := &Principal{Subject: NormalizeSubject(sub), RawSubject: sub}
	p.Email, _ = claims["email"].(string)
	p.Name, _ = claims["name"].(string)
	if p.Name == "" {
		g, _ := claims["given_name"].(string)
		f, _ := claims["family_name"].(string)
		p.Name = strings.TrimSpace(g + " " + f)
	}
	return p, nil
}
