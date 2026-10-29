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

	"github.com/apache/airavata/api/credentials/service"
)

// InteractiveSessionController serves /api/v1/ssh-interactive-sessions.
type InteractiveSessionController struct {
	svc *service.InteractiveSessionService
}

// NewInteractiveSessionController returns a handler delegating to svc.
func NewInteractiveSessionController(svc *service.InteractiveSessionService) *InteractiveSessionController {
	return &InteractiveSessionController{svc: svc}
}

// Register mounts the interactive session routes.
func (h *InteractiveSessionController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/ssh-interactive-sessions", h.list)
}

func (h *InteractiveSessionController) list(w http.ResponseWriter, r *http.Request) {
	sessions, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, sessions)
}
