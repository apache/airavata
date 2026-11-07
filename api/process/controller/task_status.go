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

// TaskStatusController serves read-only access to the status history of a process's
// tasks, nested under /api/v1/processes/{processId}.
//
// One route covers all four task kinds: a status names its kind, so a client reads the
// whole run's task history in one call rather than one per kind.
type TaskStatusController struct{ svc *service.TaskStatusService }

// NewTaskStatusController returns a handler delegating to svc.
func NewTaskStatusController(svc *service.TaskStatusService) *TaskStatusController {
	return &TaskStatusController{svc: svc}
}

// Register mounts the read-only task status routes.
func (h *TaskStatusController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/processes/{processId}/task-statuses", h.list)
	mux.HandleFunc("GET /api/v1/processes/{processId}/tasks/{taskId}/statuses", h.listForTask)
}

func (h *TaskStatusController) list(w http.ResponseWriter, r *http.Request) {
	statuses, err := h.svc.ListForProcess(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, statuses)
}

func (h *TaskStatusController) listForTask(w http.ResponseWriter, r *http.Request) {
	statuses, err := h.svc.ListForTask(r.Context(), r.PathValue("processId"), r.PathValue("taskId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, statuses)
}
