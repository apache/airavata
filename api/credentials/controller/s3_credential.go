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

	dto "github.com/apache/airavata/api/credentials/dto"
	"github.com/apache/airavata/api/credentials/service"
)

// S3CredentialController serves /api/v1/s3-credentials.
type S3CredentialController struct{ svc *service.S3CredentialService }

// NewS3CredentialController returns a handler delegating to svc.
func NewS3CredentialController(svc *service.S3CredentialService) *S3CredentialController {
	return &S3CredentialController{svc: svc}
}

// Register mounts the S3 credential routes.
func (h *S3CredentialController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/s3-credentials", h.list)
	mux.HandleFunc("POST /api/v1/s3-credentials", h.create)
	mux.HandleFunc("GET /api/v1/s3-credentials/{s3CredentialId}", h.get)
	mux.HandleFunc("PUT /api/v1/s3-credentials/{s3CredentialId}", h.update)
	mux.HandleFunc("DELETE /api/v1/s3-credentials/{s3CredentialId}", h.delete)
}

func (h *S3CredentialController) list(w http.ResponseWriter, r *http.Request) {
	creds, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, creds)
}

func (h *S3CredentialController) get(w http.ResponseWriter, r *http.Request) {
	cred, err := h.svc.Get(r.Context(), r.PathValue("s3CredentialId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, cred)
}

func (h *S3CredentialController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.S3CredentialRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	cred, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, cred)
}

func (h *S3CredentialController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.S3CredentialRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	cred, err := h.svc.Update(r.Context(), r.PathValue("s3CredentialId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, cred)
}

func (h *S3CredentialController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("s3CredentialId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
