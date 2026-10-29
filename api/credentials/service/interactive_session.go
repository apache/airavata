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

package service

import (
	"context"
	"errors"
	"sort"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/ssh"

	dto "github.com/apache/airavata/api/credentials/dto"
)

// InteractiveSessionService exposes the keyboard-interactive challenges waiting on
// the calling user.
//
// A session belongs to the user whose request opened the connection, and only that
// user sees it — admins included, for the same reason a credential is private: the
// answer is a password or a one-time code.
type InteractiveSessionService struct {
	store *ssh.SSHInteractiveSessionStore
}

func NewInteractiveSessionService(sshContext *ssh.GlobalSSHContext) *InteractiveSessionService {
	return &InteractiveSessionService{store: sshContext.InteractiveSessionStore}
}

// List returns the caller's pending sessions, oldest first.
func (s *InteractiveSessionService) List(ctx context.Context) ([]dto.InteractiveSessionResponse, error) {
	p, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	sessions := s.store.ListSessionsForUser(p.Name)
	sort.Slice(sessions, func(i, j int) bool { return sessions[i].CreatedAt.Before(sessions[j].CreatedAt) })

	out := make([]dto.InteractiveSessionResponse, 0, len(sessions))
	for _, session := range sessions {
		out = append(out, dto.ToInteractiveSessionResponse(session))
	}
	return out, nil
}

// Get returns one of the caller's pending sessions. Another user's session is a 404,
// not a 403: the store is keyed by owner, so it is simply not there to find.
func (s *InteractiveSessionService) Get(ctx context.Context, sessionID string) (*ssh.SSHInteractiveSession, error) {
	p, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	session, ok := s.store.GetSession(p.Name, sessionID)
	if !ok {
		return nil, httpx.NotFound("No pending interactive session: %s", sessionID)
	}
	return session, nil
}

// errLoginCancelled is what the SSH handshake fails with when the user cancels.
var errLoginCancelled = errors.New("interactive login cancelled by user")

// Answer hands the caller's responses to the handshake waiting on session.
//
// The responses are never logged or kept beyond the session: they are passwords and
// one-time codes.
func (s *InteractiveSessionService) Answer(session *ssh.SSHInteractiveSession, answer *dto.InteractiveSessionAnswer) error {
	if answer.Cancel {
		return s.set(session, nil, errLoginCancelled)
	}
	prompts, _ := session.Read()
	if len(answer.Responses) != len(prompts) {
		return httpx.BadRequest("Expected %d responses, got %d", len(prompts), len(answer.Responses))
	}
	return s.set(session, answer.Responses, nil)
}

func (s *InteractiveSessionService) set(session *ssh.SSHInteractiveSession, responses []string, err error) error {
	if errors.Is(session.SetResponse(responses, err), ssh.ErrSessionAnswered) {
		return httpx.Conflict("Interactive session %s has already been answered", session.SessionId)
	}
	return nil
}
