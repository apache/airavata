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
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"errors"
	"fmt"
	"os"
	"strings"
	"sync"
	"time"

	"github.com/zalando/go-keyring"
	"golang.org/x/oauth2"
)

type entry struct {
	AccessToken  string    `json:"access_token"`
	RefreshToken string    `json:"refresh_token,omitempty"`
	TokenType    string    `json:"token_type,omitempty"`
	Expiry       time.Time `json:"expiry,omitzero"`
	Issuer       string    `json:"issuer"`
	ClientID     string    `json:"client_id"`
	APIURL       string    `json:"api_url,omitempty"`
	Subject      string    `json:"subject,omitempty"`
}

func (e *entry) token() *oauth2.Token {
	return &oauth2.Token{AccessToken: e.AccessToken, RefreshToken: e.RefreshToken, TokenType: e.TokenType, Expiry: e.Expiry}
}

func entryFromToken(cfg Config, t *oauth2.Token, subject string) *entry {
	return &entry{
		AccessToken: t.AccessToken, RefreshToken: t.RefreshToken, TokenType: t.TokenType, Expiry: t.Expiry,
		Issuer: cfg.Issuer, ClientID: cfg.ClientID, APIURL: cfg.APIURL, Subject: subject,
	}
}

func keyringAccount(cfg Config) string {
	sum := sha256.Sum256([]byte(cfg.Issuer + "\x00" + cfg.ClientID + "\x00" + strings.TrimRight(cfg.APIURL, "/")))
	return "v1:" + hex.EncodeToString(sum[:16])
}

func saveEntry(cfg Config, e *entry) error {
	b, err := json.Marshal(e)
	if err != nil {
		return err
	}
	if err := keyring.Set(keyringService, keyringAccount(cfg), string(b)); err != nil {
		return fmt.Errorf("auth: saving credentials to the OS keychain: %w", err)
	}
	return nil
}

func loadEntry(cfg Config) (*entry, error) {
	s, err := keyring.Get(keyringService, keyringAccount(cfg))
	if errors.Is(err, keyring.ErrNotFound) {
		return nil, ErrNotLoggedIn
	}
	if err != nil {
		return nil, fmt.Errorf("auth: reading credentials from the OS keychain: %w", err)
	}
	var e entry
	if err := json.Unmarshal([]byte(s), &e); err != nil || e.AccessToken == "" {
		return nil, fmt.Errorf("auth: stored credentials are unreadable: %w", ErrNotLoggedIn)
	}
	return &e, nil
}

func deleteEntry(cfg Config) error {
	err := keyring.Delete(keyringService, keyringAccount(cfg))
	if err != nil && !errors.Is(err, keyring.ErrNotFound) {
		return fmt.Errorf("auth: removing credentials from the OS keychain: %w", err)
	}
	return nil
}

type staticSource string

func (s staticSource) Token(ctx context.Context) (string, error) { return string(s), ctx.Err() }

type keyringSource struct {
	cfg  Config
	base oauth2.TokenSource
	mu   sync.Mutex
	last *entry
}

func (s *keyringSource) Token(ctx context.Context) (string, error) {
	if err := ctx.Err(); err != nil {
		return "", err
	}
	s.mu.Lock()
	defer s.mu.Unlock()
	t, err := s.base.Token()
	if err != nil {
		return "", fmt.Errorf("auth: refreshing the access token failed (%s); run `airavata-mcp login`", refreshFailure(err))
	}
	if t.AccessToken != s.last.AccessToken || t.RefreshToken != s.last.RefreshToken {
		next := entryFromToken(s.cfg, t, s.last.Subject)
		if err := saveEntry(s.cfg, next); err != nil {
			return "", err
		}
		s.last = next
	}
	return t.AccessToken, nil
}

func refreshFailure(err error) string {
	var re *oauth2.RetrieveError
	if errors.As(err, &re) {
		if re.ErrorCode != "" {
			return re.ErrorCode
		}
		if re.Response != nil {
			return re.Response.Status
		}
	}
	return err.Error()
}

func NewTokenSource(ctx context.Context, cfg Config) (TokenSource, *Principal, error) {
	cfg, err := cfg.withDefaults()
	if err != nil {
		return nil, nil, err
	}
	if tok := strings.TrimSpace(os.Getenv(EnvToken)); tok != "" {
		p, err := Identify(ctx, cfg, tok)
		if err != nil {
			return nil, nil, fmt.Errorf("%s: %w", EnvToken, err)
		}
		return staticSource(tok), p, nil
	}
	e, err := loadEntry(cfg)
	if err != nil {
		return nil, nil, err
	}
	d, err := discover(ctx, cfg)
	if err != nil {
		return nil, nil, err
	}
	base := d.oauth(cfg, "").TokenSource(cfg.ctx(context.WithoutCancel(ctx)), e.token())
	src := &keyringSource{cfg: cfg, base: oauth2.ReuseTokenSource(e.token(), base), last: e}
	at, err := src.Token(ctx)
	if err != nil {
		return nil, nil, err
	}
	p, err := identify(ctx, cfg, d, at)
	if err != nil {
		return nil, nil, err
	}
	if e.Subject != "" && p.RawSubject != e.Subject {
		return nil, nil, errors.New("auth: stored token belongs to a different identity than the one that logged in; run `airavata-mcp login`")
	}
	return src, p, nil
}
