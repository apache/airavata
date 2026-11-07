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

// Package controller serves the session routes. Stopping a run stays on
// /api/v1/processes/{processId}/stop, so a session has no stop of its own.
package controller

import (
	"net/http"

	"github.com/apache/airavata/internal/httpx"

	"github.com/apache/airavata/api/session/dto"
	"github.com/apache/airavata/api/session/service"
)

type SessionController struct{ svc *service.SessionService }

func NewSessionController(svc *service.SessionService) *SessionController {
	return &SessionController{svc: svc}
}

func (h *SessionController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/sessions/me", h.listMine)
	mux.HandleFunc("GET /api/v1/sessions/{sessionId}", h.get)
	mux.HandleFunc("POST /api/v1/sessions", h.create)
	mux.HandleFunc("POST /api/v1/sessions/{sessionId}/start", h.start)
	mux.HandleFunc("DELETE /api/v1/sessions/{sessionId}", h.delete)
}

func (h *SessionController) listMine(w http.ResponseWriter, r *http.Request) {
	found, err := h.svc.ListMine(r.Context())
	httpx.Answer(w, r, found, err)
}

func (h *SessionController) get(w http.ResponseWriter, r *http.Request) {
	found, err := h.svc.Get(r.Context(), r.PathValue("sessionId"))
	httpx.Answer(w, r, found, err)
}

func (h *SessionController) create(w http.ResponseWriter, r *http.Request) {
	var req dto.Request
	if err := httpx.Bind(r, &req); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	created, err := h.svc.Create(r.Context(), &req)
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusCreated, created)
}

func (h *SessionController) start(w http.ResponseWriter, r *http.Request) {
	var req dto.StartRequest
	if r.ContentLength != 0 {
		if err := httpx.Bind(r, &req); err != nil {
			httpx.WriteError(w, r, err)
			return
		}
	}
	started, err := h.svc.Start(r.Context(), r.PathValue("sessionId"), &req)
	httpx.Answer(w, r, started, err)
}

func (h *SessionController) delete(w http.ResponseWriter, r *http.Request) {
	if err := h.svc.Delete(r.Context(), r.PathValue("sessionId")); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}
