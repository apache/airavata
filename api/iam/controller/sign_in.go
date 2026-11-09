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

package controller

import (
	"net/http"
	"time"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	"github.com/apache/airavata/api/iam/service"
)

type SignInController struct {
	signIn *auth.SignIn
	bearer auth.Introspector
	users  *service.UserService
}

func NewSignInController(signIn *auth.SignIn, bearer auth.Introspector, users *service.UserService) *SignInController {
	return &SignInController{signIn: signIn, bearer: bearer, users: users}
}

func (h *SignInController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/auth/config", h.config)
	mux.HandleFunc("POST /api/v1/auth/device", h.device)
	mux.HandleFunc("POST /api/v1/auth/token", h.token)
}

func (h *SignInController) config(w http.ResponseWriter, _ *http.Request) {
	httpx.WriteJSON(w, http.StatusOK, h.signIn.Config())
}

func (h *SignInController) device(w http.ResponseWriter, r *http.Request) {
	noStore(w)
	device, err := h.signIn.DeviceStart(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, device)
}

func (h *SignInController) token(w http.ResponseWriter, r *http.Request) {
	noStore(w)
	var grant auth.Grant
	if err := httpx.DecodeJSON(r, &grant); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	tokens, err := h.signIn.Redeem(r.Context(), grant)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	caller, err := h.bearer.Introspect(r.Context(), tokens.IDToken)
	if err != nil {
		httpx.WriteError(w, r, &httpx.Error{Status: http.StatusBadGateway, Message: "Unable to validate the issued ID token", Err: err})
		return
	}
	if err := h.users.Enroll(r.Context(), caller); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	if exp, ok := caller.Attributes["exp"].(float64); ok {
		tokens.ExpiresIn = int64(time.Until(time.Unix(int64(exp), 0)).Seconds())
	}
	httpx.WriteJSON(w, http.StatusOK, tokens)
}
