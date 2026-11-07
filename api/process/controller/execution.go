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

// ExecutionController serves read-only access to the workflow executions started for a
// process, nested under /api/v1/processes/{processId}.
//
// There is deliberately no POST or PUT: executions are recorded by
// service.ExecutionService when a workflow is started, never accepted as a request
// body from a client.
type ExecutionController struct{ svc *service.ExecutionService }

// NewExecutionController returns a handler delegating to svc.
func NewExecutionController(svc *service.ExecutionService) *ExecutionController {
	return &ExecutionController{svc: svc}
}

// Register mounts the read-only execution routes.
func (h *ExecutionController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/processes/{processId}/executions", h.list)
	mux.HandleFunc("GET /api/v1/processes/{processId}/executions/{executionId}", h.get)
}

func (h *ExecutionController) list(w http.ResponseWriter, r *http.Request) {
	executions, err := h.svc.ListForProcess(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, executions)
}

func (h *ExecutionController) get(w http.ResponseWriter, r *http.Request) {
	execution, err := h.svc.Get(r.Context(), r.PathValue("processId"), r.PathValue("executionId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, execution)
}
