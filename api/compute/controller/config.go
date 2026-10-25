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

	dto "github.com/apache/airavata/api/compute/dto"
	"github.com/apache/airavata/api/compute/service"
)

// SlurmClusterConfigController serves /api/v1/slurm-cluster-configs.
type SlurmClusterConfigController struct {
	svc *service.SlurmClusterConfigService
}

// NewSlurmClusterConfigController returns a handler delegating to svc.
func NewSlurmClusterConfigController(svc *service.SlurmClusterConfigService) *SlurmClusterConfigController {
	return &SlurmClusterConfigController{svc: svc}
}

// Register mounts the cluster config routes.
//
// The literal /me and /shared-with-me patterns take precedence over
// /{slurmClusterConfigId}, so neither is ever mistaken for a config id.
func (h *SlurmClusterConfigController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs", h.list)
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs/me", h.listMine)
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs/shared-with-me", h.listSharedWithMe)
	mux.HandleFunc("POST /api/v1/slurm-cluster-configs", h.create)
	mux.HandleFunc("GET /api/v1/slurm-cluster-configs/{slurmClusterConfigId}", h.get)
	mux.HandleFunc("PUT /api/v1/slurm-cluster-configs/{slurmClusterConfigId}", h.update)
	mux.HandleFunc("DELETE /api/v1/slurm-cluster-configs/{slurmClusterConfigId}", h.delete)
}

func (h *SlurmClusterConfigController) list(w http.ResponseWriter, r *http.Request) {
	configs, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, configs)
}

func (h *SlurmClusterConfigController) listMine(w http.ResponseWriter, r *http.Request) {
	configs, err := h.svc.ListMine(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, configs)
}

func (h *SlurmClusterConfigController) listSharedWithMe(w http.ResponseWriter, r *http.Request) {
	configs, err := h.svc.ListSharedWithMe(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, configs)
}

func (h *SlurmClusterConfigController) get(w http.ResponseWriter, r *http.Request) {
	config, err := h.svc.Get(r.Context(), r.PathValue("slurmClusterConfigId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, config)
}

func (h *SlurmClusterConfigController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.SlurmClusterConfigRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	config, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, config)
}

func (h *SlurmClusterConfigController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.SlurmClusterConfigRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	config, err := h.svc.Update(r.Context(), r.PathValue("slurmClusterConfigId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, config)
}

func (h *SlurmClusterConfigController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("slurmClusterConfigId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
