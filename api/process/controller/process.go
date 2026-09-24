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

// Package controller serves the process routes: the process itself, its status
// history and its tasks.
//
// There are no routes for a batch process. What a BATCH_JOB run needs is configured
// in the batchProcess section of a process body and read back nested in the process
// response, so every operation on it goes through the process it belongs to.
package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/process/dto"
	"github.com/apache/airavata/api/process/service"
)

// ProcessController serves /api/v1/processes.
type ProcessController struct{ svc *service.ProcessService }

// NewProcessController returns a handler delegating to svc.
func NewProcessController(svc *service.ProcessService) *ProcessController {
	return &ProcessController{svc: svc}
}

// Register mounts the process routes.
func (h *ProcessController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/processes", h.list)
	mux.HandleFunc("POST /api/v1/processes", h.create)
	mux.HandleFunc("GET /api/v1/processes/{processId}", h.get)
	mux.HandleFunc("PUT /api/v1/processes/{processId}", h.update)
	mux.HandleFunc("DELETE /api/v1/processes/{processId}", h.delete)
}

// list returns every process, or only those run against a deployment when
// deploymentId is supplied. The two branches have different authorisation: the unfiltered listing is
// admin only, while the per-deployment one is open.
func (h *ProcessController) list(w http.ResponseWriter, r *http.Request) {
	var (
		found []dto.Response
		err   error
	)
	if deploymentID := r.URL.Query().Get("deploymentId"); deploymentID != "" {
		found, err = h.svc.ListByDeployment(r.Context(), deploymentID)
	} else {
		found, err = h.svc.List(r.Context())
	}
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, found)
}

func (h *ProcessController) get(w http.ResponseWriter, r *http.Request) {
	proc, err := h.svc.Get(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, proc)
}

func (h *ProcessController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.Request
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	proc, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, proc)
}

func (h *ProcessController) update(w http.ResponseWriter, r *http.Request) {
	var req dto.Request
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	proc, err := h.svc.Update(r.Context(), r.PathValue("processId"), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, proc)
}

func (h *ProcessController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("processId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
