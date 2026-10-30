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
	"context"
	"errors"
	"log/slog"
	"net/http"

	"github.com/coder/websocket"
	"github.com/coder/websocket/wsjson"

	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/credentials/dto"
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
	mux.HandleFunc("GET /api/v1/ssh-interactive-sessions/{sessionId}", h.get)
	mux.HandleFunc("POST /api/v1/ssh-interactive-sessions/{sessionId}/response", h.respond)
	mux.HandleFunc("GET /api/v1/ssh-interactive-sessions/{sessionId}/ws", h.answer)
}

func (h *InteractiveSessionController) list(w http.ResponseWriter, r *http.Request) {
	sessions, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, sessions)
}

func (h *InteractiveSessionController) get(w http.ResponseWriter, r *http.Request) {
	session, err := h.svc.Get(r.Context(), r.PathValue("sessionId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, dto.ToInteractiveSessionResponse(session))
}

// respond is the websocket's answer step as one request: 204 once the answer is
// handed to the SSH handshake, 400 for the wrong number of responses, 409 when the
// session was already answered.
func (h *InteractiveSessionController) respond(w http.ResponseWriter, r *http.Request) {
	session, err := h.svc.Get(r.Context(), r.PathValue("sessionId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	var answer dto.InteractiveSessionAnswer
	if err := httpx.Bind(r, &answer); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	if err := h.svc.Answer(session, &answer); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// answer serves one session over a websocket: it sends the challenge, then reads
// answers until one is accepted.
//
// The session is resolved before the upgrade, so a missing token or an unknown
// session is an ordinary JSON 401 or 404 rather than a socket that opens and dies.
func (h *InteractiveSessionController) answer(w http.ResponseWriter, r *http.Request) {
	session, err := h.svc.Get(r.Context(), r.PathValue("sessionId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}

	// Accept writes its own response when the handshake is bad.
	conn, err := websocket.Accept(w, r, nil)
	if err != nil {
		return
	}
	defer conn.CloseNow()
	ctx := r.Context()

	challenge := dto.ToInteractiveSessionResponse(session)
	if err := wsjson.Write(ctx, conn, dto.InteractiveSessionMessage{Type: "challenge", Session: &challenge}); err != nil {
		return
	}

	for {
		var answer dto.InteractiveSessionAnswer
		if err := wsjson.Read(ctx, conn, &answer); err != nil {
			// The client went away or sent something that is not JSON. The session
			// stays pending, so it can be answered from a new socket.
			return
		}

		err := h.svc.Answer(session, &answer)
		if err == nil {
			_ = wsjson.Write(ctx, conn, dto.InteractiveSessionMessage{Type: "accepted"})
			conn.Close(websocket.StatusNormalClosure, "")
			return
		}

		if writeErr := writeSocketError(ctx, conn, err); writeErr != nil {
			return
		}
		var he *httpx.Error
		if errors.As(err, &he) && he.Status == http.StatusConflict {
			// Answered elsewhere: nothing this socket sends can matter any more.
			conn.Close(websocket.StatusPolicyViolation, "session already answered")
			return
		}
	}
}

func writeSocketError(ctx context.Context, conn *websocket.Conn, err error) error {
	msg := "Internal server error"
	var he *httpx.Error
	if errors.As(err, &he) {
		msg = he.Message
	} else {
		slog.Error("answering interactive session", "error", err)
	}
	return wsjson.Write(ctx, conn, dto.InteractiveSessionMessage{Type: "error", Message: msg})
}
