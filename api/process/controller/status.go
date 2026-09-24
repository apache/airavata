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

	"github.com/apache/airavata/api/process/service"
)

// StatusController serves read-only access to a process's status history, nested
// under /api/v1/processes/{processId}.
//
// There is deliberately no POST or PUT: statuses are recorded internally by
// service.StatusService — from process submission and from whatever submits and monitors the
// actual job — never accepted as a request body from a client.
type StatusController struct{ svc *service.StatusService }

// NewStatusController returns a handler delegating to svc.
func NewStatusController(svc *service.StatusService) *StatusController {
	return &StatusController{svc: svc}
}

// Register mounts the read-only status routes.
func (h *StatusController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/processes/{processId}/statuses", h.list)
	mux.HandleFunc("GET /api/v1/processes/{processId}/statuses/{statusId}", h.get)
}

func (h *StatusController) list(w http.ResponseWriter, r *http.Request) {
	statuses, err := h.svc.ListForProcess(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, statuses)
}

func (h *StatusController) get(w http.ResponseWriter, r *http.Request) {
	status, err := h.svc.Get(r.Context(), r.PathValue("processId"), r.PathValue("statusId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, status)
}
