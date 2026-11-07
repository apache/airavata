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
	"encoding/base64"
	"net/http"
	"strings"

	"github.com/apache/airavata/internal/httpx"
)

// Middleware resolves the bearer token, if any, onto the request context.
//
// It deliberately does not reject anonymous requests. The Java filter chain permits
// every request and leaves enforcement to the method-level guards, so an endpoint
// with no authority requirement stays reachable without a token. What it does reject
// is a token that is present but unusable: a caller who supplies a bad token gets 401
// rather than being silently downgraded to anonymous, which would otherwise turn a
// typo'd token into a confusing 403 further down.
func Middleware(introspector Introspector) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			r = r.WithContext(WithConnection(r.Context(), connection(r)))
			token := bearerToken(r)
			if token == "" {
				next.ServeHTTP(w, r)
				return
			}

			principal, err := introspector.Introspect(r.Context(), token)
			if err != nil {
				w.Header().Set("WWW-Authenticate", `Bearer error="invalid_token"`)
				httpx.WriteError(w, r, httpx.Unauthorized("Invalid bearer token"))
				return
			}

			next.ServeHTTP(w, r.WithContext(WithPrincipal(r.Context(), principal)))
		})
	}
}

// WebSocketProtocol is the subprotocol a browser offers alongside its token, and the one
// a websocket handler must accept: a browser fails the upgrade when it offered
// subprotocols and the server selected none.
const WebSocketProtocol = "airavata.v1"

// webSocketTokenPrefix marks the subprotocol carrying the bearer token, base64url
// encoded without padding since a subprotocol may not contain the ":" and "/" a CILogon
// token does.
const webSocketTokenPrefix = "base64url.bearer."

// ConnectionHeader names the client connection a request comes from, such as one editor
// window or one browser tab; a websocket offers it as a "connection." subprotocol instead.
const ConnectionHeader = "X-Connection-Id"

func connection(r *http.Request) string {
	if id := r.Header.Get(ConnectionHeader); id != "" {
		return id
	}
	return Subprotocol(r, "connection.")
}

func bearerToken(r *http.Request) string {
	header := r.Header.Get("Authorization")
	if header == "" {
		return webSocketToken(r)
	}
	scheme, token, found := strings.Cut(header, " ")
	if !found || !strings.EqualFold(scheme, "Bearer") {
		return ""
	}
	return strings.TrimSpace(token)
}

// webSocketToken reads the token a browser sends on a websocket upgrade. The browser
// WebSocket API cannot set an Authorization header, so the dashboard offers the token as
// a subprotocol instead — the one header it does control.
func webSocketToken(r *http.Request) string {
	token, err := base64.RawURLEncoding.DecodeString(Subprotocol(r, webSocketTokenPrefix))
	if err != nil {
		return ""
	}
	return strings.TrimSpace(string(token))
}

// Subprotocol returns the value of the websocket subprotocol r offers under prefix, or "".
func Subprotocol(r *http.Request, prefix string) string {
	if !strings.EqualFold(r.Header.Get("Upgrade"), "websocket") {
		return ""
	}
	for _, offered := range r.Header.Values("Sec-WebSocket-Protocol") {
		for _, p := range strings.Split(offered, ",") {
			if value, ok := strings.CutPrefix(strings.TrimSpace(p), prefix); ok {
				return value
			}
		}
	}
	return ""
}
