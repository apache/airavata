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
	"sort"

	"github.com/apache/airavata/internal/auth"
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
