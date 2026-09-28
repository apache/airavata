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

// Package controller serves the SSH key routes.
package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/credentials/dto"
	"github.com/apache/airavata/api/credentials/service"
)

// SSHKeyController serves /api/v1/ssh-keys.
type SSHKeyController struct{ svc *service.SSHKeyService }

// NewSSHKeyController returns a handler delegating to svc.
func NewSSHKeyController(svc *service.SSHKeyService) *SSHKeyController {
	return &SSHKeyController{svc: svc}
}

// Register mounts the SSH key routes.
func (h *SSHKeyController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/ssh-keys", h.list)
	mux.HandleFunc("POST /api/v1/ssh-keys", h.create)
	mux.HandleFunc("GET /api/v1/ssh-keys/{sshKeyId}", h.get)
	mux.HandleFunc("PUT /api/v1/ssh-keys/{sshKeyId}", h.update)
	mux.HandleFunc("DELETE /api/v1/ssh-keys/{sshKeyId}", h.delete)
}

func (h *SSHKeyController) list(w http.ResponseWriter, r *http.Request) {
	keys, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, keys)
}

func (h *SSHKeyController) get(w http.ResponseWriter, r *http.Request) {
	key, err := h.svc.Get(r.Context(), r.PathValue("sshKeyId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, key)
}

func (h *SSHKeyController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHKeyRequest
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

func (h *SSHKeyController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SSHKeyRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	key, err := h.svc.Update(r.Context(), r.PathValue("sshKeyId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, key)
}

func (h *SSHKeyController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("sshKeyId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
