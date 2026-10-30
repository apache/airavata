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

// InteractiveAuthController serves /api/v1/ssh-interactive-auths.
type InteractiveAuthController struct {
	svc *service.InteractiveAuthService
}

// NewInteractiveAuthController returns a handler delegating to svc.
func NewInteractiveAuthController(svc *service.InteractiveAuthService) *InteractiveAuthController {
	return &InteractiveAuthController{svc: svc}
}

// Register mounts the interactive auth routes.
func (h *InteractiveAuthController) Register(mux *http.ServeMux) {
	mux.HandleFunc("GET /api/v1/ssh-interactive-auths", h.list)
	mux.HandleFunc("GET /api/v1/ssh-interactive-auths/{authId}", h.get)
	mux.HandleFunc("POST /api/v1/ssh-interactive-auths/{authId}/response", h.respond)
	mux.HandleFunc("GET /api/v1/ssh-interactive-auths/{authId}/ws", h.answer)
}

func (h *InteractiveAuthController) list(w http.ResponseWriter, r *http.Request) {
	auths, err := h.svc.List(r.Context())
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, auths)
}

func (h *InteractiveAuthController) get(w http.ResponseWriter, r *http.Request) {
	auth, err := h.svc.Get(r.Context(), r.PathValue("authId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusOK, dto.ToInteractiveAuthResponse(auth))
}

// respond is the websocket's answer step as one request: 204 once the answer is
// handed to the SSH handshake, 400 for the wrong number of responses, 409 when the
// auth was already answered.
func (h *InteractiveAuthController) respond(w http.ResponseWriter, r *http.Request) {
	auth, err := h.svc.Get(r.Context(), r.PathValue("authId"))
	if err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	var answer dto.InteractiveAuthAnswer
	if err := httpx.Bind(r, &answer); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	if err := h.svc.Answer(auth, &answer); err != nil {
		httpx.WriteError(w, r, err)
		return
	}
	httpx.WriteJSON(w, http.StatusNoContent, nil)
}

// answer serves one auth over a websocket: it sends the challenge, then reads
// answers until one is accepted.
//
// The auth is resolved before the upgrade, so a missing token or an unknown
// auth is an ordinary JSON 401 or 404 rather than a socket that opens and dies.
func (h *InteractiveAuthController) answer(w http.ResponseWriter, r *http.Request) {
	auth, err := h.svc.Get(r.Context(), r.PathValue("authId"))
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

	challenge := dto.ToInteractiveAuthResponse(auth)
	if err := wsjson.Write(ctx, conn, dto.InteractiveAuthMessage{Type: "challenge", Auth: &challenge}); err != nil {
		return
	}

	for {
		var answer dto.InteractiveAuthAnswer
		if err := wsjson.Read(ctx, conn, &answer); err != nil {
			// The client went away or sent something that is not JSON. The auth
			// stays pending, so it can be answered from a new socket.
			return
		}

		err := h.svc.Answer(auth, &answer)
		if err == nil {
			_ = wsjson.Write(ctx, conn, dto.InteractiveAuthMessage{Type: "accepted"})
			conn.Close(websocket.StatusNormalClosure, "")
			return
		}

		if writeErr := writeSocketError(ctx, conn, err); writeErr != nil {
			return
		}
		var he *httpx.Error
		if errors.As(err, &he) && he.Status == http.StatusConflict {
			// Answered elsewhere: nothing this socket sends can matter any more.
			conn.Close(websocket.StatusPolicyViolation, "auth already answered")
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
		slog.Error("answering interactive auth", "error", err)
	}
	return wsjson.Write(ctx, conn, dto.InteractiveAuthMessage{Type: "error", Message: msg})
}
