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

// WorkflowController serves read-only access to the workflows started for a
// process, nested under /api/v1/processes/{processId}.
//
// There is deliberately no POST or PUT: workflows are recorded by
// service.WorkflowService when a workflow is started, never accepted as a request
// body from a client.
type WorkflowController struct{ svc *service.WorkflowService }

// NewWorkflowController returns a handler delegating to svc.
func NewWorkflowController(svc *service.WorkflowService) *WorkflowController {
	return &WorkflowController{svc: svc}
}

// Register mounts the read-only workflow routes.
func (h *WorkflowController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/processes/{processId}/workflows", h.list)
	mux.HandleFunc("GET /api/v1/processes/{processId}/workflows/{processWorkflowId}", h.get)
}

func (h *WorkflowController) list(w http.ResponseWriter, r *http.Request) {
	workflows, err := h.svc.ListForProcess(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, workflows)
}

func (h *WorkflowController) get(w http.ResponseWriter, r *http.Request) {
	workflow, err := h.svc.Get(r.Context(), r.PathValue("processId"), r.PathValue("processWorkflowId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, workflow)
}
