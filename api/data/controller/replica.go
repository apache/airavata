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

// DataProductReplicaController serves /api/v1/data-products/{dataProductId}/replicas.
type DataProductReplicaController struct {
	svc *service.DataProductReplicaService
}

// NewDataProductReplicaController returns a handler delegating to svc.
func NewDataProductReplicaController(svc *service.DataProductReplicaService) *DataProductReplicaController {
	return &DataProductReplicaController{svc: svc}
}

// Register mounts the replica routes.
func (h *DataProductReplicaController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/data-products/{dataProductId}/replicas", h.list)
	mux.HandleFunc("POST /api/v1/data-products/{dataProductId}/replicas", h.add)
	mux.HandleFunc("DELETE /api/v1/data-products/{dataProductId}/replicas/{replicaId}", h.remove)
}

func (h *DataProductReplicaController) list(w http.ResponseWriter, r *http.Request) {
	replicas, err := h.svc.List(r.Context(), r.PathValue("dataProductId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, replicas)
}

func (h *DataProductReplicaController) add(w http.ResponseWriter, r *http.Request) {
	var req dto.DataProductReplicaRequest
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	replica, err := h.svc.Add(r.Context(), r.PathValue("dataProductId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, replica)
}

func (h *DataProductReplicaController) remove(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Remove(r.Context(), r.PathValue("dataProductId"), r.PathValue("replicaId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
