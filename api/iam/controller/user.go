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

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/iam/dto"
	"github.com/apache/airavata/api/iam/service"
)

// UserController serves /api/v1/users.
type UserController struct{ svc *service.UserService }

// NewUserController returns a handler delegating to svc.
func NewUserController(svc *service.UserService) *UserController { return &UserController{svc: svc} }

// Register mounts the user routes.
//
// Update is POST /{userId}, not PUT. That is what the Java controller declared, and
// changing it would break existing clients even though it reads oddly.
func (h *UserController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/users", h.list)
	mux.HandleFunc("POST /api/v1/users", h.create)
	mux.HandleFunc("GET /api/v1/users/{userId}", h.get)
	mux.HandleFunc("POST /api/v1/users/{userId}", h.update)
}

func (h *UserController) list(w http.ResponseWriter, r *http.Request) {
	users, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, users)
}

// create registers a user.
//
// The Java controller omitted @Valid, so the constraints declared on the registration
// DTO never ran and a blank user id would be accepted as a primary key. They are
// enforced here; that is a deliberate correction, not an oversight.
func (h *UserController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.UserRegistration
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	user, err := h.svc.Register(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, user)
}

func (h *UserController) get(w http.ResponseWriter, r *http.Request) {
	user, err := h.svc.Get(r.Context(), r.PathValue("userId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, user)
}

func (h *UserController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.UserRegistration
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	user, err := h.svc.Update(r.Context(), r.PathValue("userId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, user)
}
