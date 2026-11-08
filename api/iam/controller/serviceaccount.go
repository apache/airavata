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

// ServiceAccountController serves /api/v1/service-accounts.
type ServiceAccountController struct {
	svc *service.ServiceAccountService
}

// NewServiceAccountController returns a handler delegating to svc.
func NewServiceAccountController(svc *service.ServiceAccountService) *ServiceAccountController {
	return &ServiceAccountController{svc: svc}
}

// Register mounts the service account routes.
func (h *ServiceAccountController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/service-accounts", h.list)
	mux.HandleFunc("POST /api/v1/service-accounts", h.create)
	mux.HandleFunc("GET /api/v1/service-accounts/{serviceAccountId}", h.get)
	mux.HandleFunc("PUT /api/v1/service-accounts/{serviceAccountId}", h.update)
	mux.HandleFunc("DELETE /api/v1/service-accounts/{serviceAccountId}", h.delete)
	mux.HandleFunc("POST /api/v1/service-accounts/{serviceAccountId}/token/renew", h.renew)
	mux.HandleFunc("POST /api/v1/service-accounts/{serviceAccountId}/token/regenerate", h.regenerate)
}

func (h *ServiceAccountController) list(w http.ResponseWriter, r *http.Request) {
	out, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, out)
}

func (h *ServiceAccountController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.ServiceAccountCreate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	out, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	noStore(w)
	httpx.WriteJSON(w, http.StatusCreated, out)
}

func (h *ServiceAccountController) get(w http.ResponseWriter, r *http.Request) {
	out, err := h.svc.Get(r.Context(), r.PathValue("serviceAccountId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, out)
}

func (h *ServiceAccountController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.ServiceAccountUpdate
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	out, err := h.svc.Update(r.Context(), r.PathValue("serviceAccountId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, out)
}

func (h *ServiceAccountController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("serviceAccountId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// renew and regenerate accept an empty body, meaning the default validity.
func (h *ServiceAccountController) renew(w http.ResponseWriter, r *http.Request) {
	req, ok := bindTokenRequest(w, r)
	if !ok {
		return
	}
	out, err := h.svc.RenewToken(r.Context(), r.PathValue("serviceAccountId"), req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, out)
}

func (h *ServiceAccountController) regenerate(w http.ResponseWriter, r *http.Request) {
	req, ok := bindTokenRequest(w, r)
	if !ok {
		return
	}
	out, err := h.svc.RegenerateToken(r.Context(), r.PathValue("serviceAccountId"), req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	noStore(w)
	httpx.WriteJSON(w, http.StatusOK, out)
}

func bindTokenRequest(w http.ResponseWriter, r *http.Request) (*dto.ServiceAccountTokenRequest, bool) {
	req := &dto.ServiceAccountTokenRequest{}
	if r.ContentLength == 0 {
		return req, true
	}
	if err := httpx.Bind(r, req); err != nil {
		httpx.WriteError(w, r, err)
		return nil, false
	}
	return req, true
}

// noStore keeps a response carrying a freshly minted token out of every cache.
func noStore(w http.ResponseWriter) {
	w.Header().Set("Cache-Control", "no-store")
}
