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
	"github.com/apache/airavata/internal/ssh"

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
	mux.HandleFunc("GET /api/v1/ssh-interactive-auths/ws", h.answerAll)
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

// answerAll serves all of the caller's auths over one websocket, one at a time and
// oldest first: it sends a challenge, and only once that is answered (here or
// anywhere else) or abandoned does it send the next. With nothing pending it sends
// "idle" and waits for the next auth to arrive.
func (h *InteractiveAuthController) answerAll(w http.ResponseWriter, r *http.Request) {
	// Resolve the caller before the upgrade, so a missing token is a plain 401.
	if _, _, err := h.svc.Next(r.Context()); err != nil {
		httpx.WriteError(w, r, err)
		return
	}

	conn, err := websocket.Accept(w, r, nil)
	if err != nil {
		return
	}
	defer conn.CloseNow()
	ctx, cancel := context.WithCancel(r.Context())
	defer cancel()

	// Answers are read on their own goroutine so the loop below can also wake when
	// the caller's auths change. The channel closes when the client goes away.
	answers := make(chan dto.InteractiveAuthAnswer)
	go func() {
		defer close(answers)
		for {
			var answer dto.InteractiveAuthAnswer
			if err := wsjson.Read(ctx, conn, &answer); err != nil {
				return
			}
			select {
			case answers <- answer:
			case <-ctx.Done():
				return
			}
		}
	}()

	var current *ssh.SSHInteractiveAuth
	idleSent := false
	for {
		next, changed, err := h.svc.Next(ctx)
		if err != nil {
			return
		}
		if next != current || (next == nil && !idleSent) {
			current = next
			msg := dto.InteractiveAuthMessage{Type: "idle"}
			if current != nil {
				challenge := dto.ToInteractiveAuthResponse(current)
				msg = dto.InteractiveAuthMessage{Type: "challenge", Auth: &challenge}
			}
			if err := wsjson.Write(ctx, conn, msg); err != nil {
				return
			}
			idleSent = current == nil
		}

		select {
		case <-ctx.Done():
			return
		case <-changed:
			// Answered elsewhere, abandoned, or a new auth queued behind this one:
			// look again.
		case answer, ok := <-answers:
			if !ok {
				return
			}
			var err error
			switch {
			case current == nil:
				err = httpx.BadRequest("No pending interactive auth to answer")
			case answer.AuthID != "" && answer.AuthID != current.AuthId:
				err = httpx.Conflict("Interactive auth %s is not the current challenge (%s)", answer.AuthID, current.AuthId)
			default:
				err = h.svc.Answer(current, &answer)
			}
			if err == nil {
				// The answered auth is skipped from here on, so the next look moves on.
				err = wsjson.Write(ctx, conn, dto.InteractiveAuthMessage{Type: "accepted", AuthID: current.AuthId})
			} else {
				err = writeSocketError(ctx, conn, err)
			}
			if err != nil {
				return
			}
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
