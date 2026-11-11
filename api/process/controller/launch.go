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

// LaunchController serves the launch action on /api/v1/processes/{processId}.
//
// Launching is a verb rather than a resource: it has no body, nothing is addressable
// afterwards under /launch, and what it produces is read back through the process's
// task collections. Hence the one POST, and no CRUD alongside it.
type LaunchController struct{ svc *service.LaunchService }

// NewLaunchController returns a handler delegating to svc.
func NewLaunchController(svc *service.LaunchService) *LaunchController {
	return &LaunchController{svc: svc}
}

// Register mounts the launch and retry routes. Their literal segments cannot collide
// with the task collections beside them, so no ordering is implied.
func (h *LaunchController) Register(mux *http.ServeMux) {
	mux.HandleFunc("POST /api/v1/processes/{processId}/launch", h.launch)
	mux.HandleFunc("POST /api/v1/processes/{processId}/retry", h.retry)
	mux.HandleFunc("POST /api/v1/processes/{processId}/stop", h.stop)
}

// launch answers 202 rather than 200: the tasks are recorded here, and carrying them
// out is somebody else's later work. The body is the process itself, so a caller that
// launched by id learns nothing new but has the run to hand.
func (h *LaunchController) launch(w http.ResponseWriter, r *http.Request) {
	proc, err := h.svc.LaunchProcess(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusAccepted, proc)
}

// retry answers 202 for the same reason launch does: it starts the failed workflow
// again, and carrying it out is later work.
func (h *LaunchController) retry(w http.ResponseWriter, r *http.Request) {
	proc, err := h.svc.RetryProcess(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusAccepted, proc)
}

func (h *LaunchController) stop(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Stop(r.Context(), r.PathValue("processId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
