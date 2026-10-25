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

// Package controller serves the SSH credential routes.
package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/credentials/dto"
	"github.com/apache/airavata/api/credentials/service"
)

// SSHCredentialController serves /api/v1/ssh-credentials.
type SSHCredentialController struct{ svc *service.SSHCredentialService }

// NewSSHCredentialController returns a handler delegating to svc.
func NewSSHCredentialController(svc *service.SSHCredentialService) *SSHCredentialController {
	return &SSHCredentialController{svc: svc}
}

// Register mounts the SSH credential routes.
func (h *SSHCredentialController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/ssh-credentials", h.list)
	mux.HandleFunc("POST /api/v1/ssh-credentials", h.create)
	mux.HandleFunc("GET /api/v1/ssh-credentials/{sshCredentialId}", h.get)
	mux.HandleFunc("PUT /api/v1/ssh-credentials/{sshCredentialId}", h.update)
	mux.HandleFunc("DELETE /api/v1/ssh-credentials/{sshCredentialId}", h.delete)
}

func (h *SSHCredentialController) list(w http.ResponseWriter, r *http.Request) {
	keys, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, keys)
}

func (h *SSHCredentialController) get(w http.ResponseWriter, r *http.Request) {
	key, err := h.svc.Get(r.Context(), r.PathValue("sshCredentialId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, key)
}

func (h *SSHCredentialController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHCredentialRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	key, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, key)
}

func (h *SSHCredentialController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHCredentialRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	key, err := h.svc.Update(r.Context(), r.PathValue("sshCredentialId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, key)
}

func (h *SSHCredentialController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("sshCredentialId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
