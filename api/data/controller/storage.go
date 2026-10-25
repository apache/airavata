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

	dto "github.com/apache/airavata/api/data/dto"
	"github.com/apache/airavata/api/data/service"
)

// SCPDataStorageController serves /api/v1/scp-data-storages.
type SCPDataStorageController struct {
	svc *service.SCPDataStorageService
}

// NewSCPDataStorageController returns a handler delegating to svc.
func NewSCPDataStorageController(svc *service.SCPDataStorageService) *SCPDataStorageController {
	return &SCPDataStorageController{svc: svc}
}

// Register mounts the storage routes.
//
// The literal /me and /shared-with-me patterns take precedence over /{dataStorageId},
// so neither is ever mistaken for a storage id.
func (h *SCPDataStorageController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/scp-data-storages", h.list)
	mux.HandleFunc("GET /api/v1/scp-data-storages/me", h.listMine)
	mux.HandleFunc("GET /api/v1/scp-data-storages/shared-with-me", h.listSharedWithMe)
	mux.HandleFunc("POST /api/v1/scp-data-storages", h.create)
	mux.HandleFunc("GET /api/v1/scp-data-storages/{dataStorageId}", h.get)
	mux.HandleFunc("PUT /api/v1/scp-data-storages/{dataStorageId}", h.update)
	mux.HandleFunc("DELETE /api/v1/scp-data-storages/{dataStorageId}", h.delete)
}

func (h *SCPDataStorageController) list(w http.ResponseWriter, r *http.Request) {
	storages, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, storages)
}

func (h *SCPDataStorageController) listMine(w http.ResponseWriter, r *http.Request) {
	storages, err := h.svc.ListMine(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, storages)
}

func (h *SCPDataStorageController) listSharedWithMe(w http.ResponseWriter, r *http.Request) {
	storages, err := h.svc.ListSharedWithMe(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, storages)
}

func (h *SCPDataStorageController) get(w http.ResponseWriter, r *http.Request) {
	storage, err := h.svc.Get(r.Context(), r.PathValue("dataStorageId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, storage)
}

func (h *SCPDataStorageController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SCPDataStorageRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	storage, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, storage)
}

func (h *SCPDataStorageController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SCPDataStorageRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	storage, err := h.svc.Update(r.Context(), r.PathValue("dataStorageId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, storage)
}

func (h *SCPDataStorageController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("dataStorageId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
