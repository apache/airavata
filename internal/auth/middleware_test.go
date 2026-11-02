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
