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

// PauseController serves the pauses of a process's workflows, and the continue action
// that ends one, under /api/v1/processes/{processId}.
//
// Like launch, continue is a verb with no body: it wakes the workflow waiting on the
// process's open pause, after the owner has answered the interactive login it asked for.
type PauseController struct{ svc *service.PauseService }

// NewPauseController returns a handler delegating to svc.
func NewPauseController(svc *service.PauseService) *PauseController {
	return &PauseController{svc: svc}
}

// Register mounts the pause routes.
func (h *PauseController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/processes/{processId}/pauses", h.list)
	mux.HandleFunc("POST /api/v1/processes/{processId}/continue", h.continueProcess)
}

func (h *PauseController) list(w http.ResponseWriter, r *http.Request) {
	pauses, err := h.svc.ListForProcess(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, pauses)
}

// continueProcess answers 202, as launch does: the task runs again on the workflow's
// time, not the request's. The body is the pause it closed.
func (h *PauseController) continueProcess(w http.ResponseWriter, r *http.Request) {
	pause, err := h.svc.Continue(r.Context(), r.PathValue("processId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusAccepted, pause)
}
