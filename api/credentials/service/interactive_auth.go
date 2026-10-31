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

// InteractiveAuthService exposes the keyboard-interactive challenges waiting on
// the calling user.
//
// An interactive auth belongs to the user whose request opened the connection, and only that
// user sees it — admins included, for the same reason a credential is private: the
// answer is a password or a one-time code.
type InteractiveAuthService struct {
	store *ssh.SSHInteractiveAuthStore
}

func NewInteractiveAuthService(sshContext *ssh.GlobalSSHContext) *InteractiveAuthService {
	return &InteractiveAuthService{store: sshContext.InteractiveAuthStore}
}

// List returns the caller's pending interactive auths, oldest first.
func (s *InteractiveAuthService) List(ctx context.Context) ([]dto.InteractiveAuthResponse, error) {
	p, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	auths := s.store.ListAuthsForUser(p.Name)
	sort.Slice(auths, func(i, j int) bool { return auths[i].CreatedAt.Before(auths[j].CreatedAt) })

	out := make([]dto.InteractiveAuthResponse, 0, len(auths))
	for _, pending := range auths {
		out = append(out, dto.ToInteractiveAuthResponse(pending))
	}
	return out, nil
}

// Get returns one of the caller's pending interactive auths. Another user's is a 404,
// not a 403: the store is keyed by owner, so it is simply not there to find.
func (s *InteractiveAuthService) Get(ctx context.Context, authID string) (*ssh.SSHInteractiveAuth, error) {
	p, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	pending, ok := s.store.GetAuth(p.Name, authID)
	if !ok {
		return nil, httpx.NotFound("No pending interactive auth: %s", authID)
	}
	return pending, nil
}

// Next returns the caller's oldest auth still waiting for an answer (nil if none),
// and a channel closed the next time the caller's auths change.
func (s *InteractiveAuthService) Next(ctx context.Context) (*ssh.SSHInteractiveAuth, <-chan struct{}, error) {
	p, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, nil, err
	}
	next, changed := s.store.NextForUser(p.Name)
	return next, changed, nil
}

// errLoginCancelled is what the SSH handshake fails with when the user cancels.
var errLoginCancelled = errors.New("interactive login cancelled by user")

// Answer hands the caller's responses to the handshake waiting on pending.
//
// The responses are never logged or kept beyond the interactive auth: they are passwords and
// one-time codes.
func (s *InteractiveAuthService) Answer(pending *ssh.SSHInteractiveAuth, answer *dto.InteractiveAuthAnswer) error {
	if answer.Cancel {
		return s.set(pending, nil, errLoginCancelled)
	}
	prompts, _ := pending.Read()
	if len(answer.Responses) != len(prompts) {
		return httpx.BadRequest("Expected %d responses, got %d", len(prompts), len(answer.Responses))
	}
	return s.set(pending, answer.Responses, nil)
}

func (s *InteractiveAuthService) set(pending *ssh.SSHInteractiveAuth, responses []string, err error) error {
	if errors.Is(pending.SetResponse(responses, err), ssh.ErrAuthAnswered) {
		return httpx.Conflict("Interactive auth %s has already been answered", pending.AuthId)
	}
	return nil
}
