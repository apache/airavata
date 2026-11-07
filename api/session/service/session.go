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

// Package service holds the session rules: a session is created and started by its
// owner, one run at a time, and removed with its runs once none is live.
//
// Each write is one transaction over the session row and its runs, and a launch
// follows the commit through the process launch path. A launch that fails ends its run
// FAILED, so nothing is unwound here: create and start still answer the session, the
// failure in its run's error, and the session can start again.
package service

import (
	"context"
	"errors"
	"time"

	"github.com/google/uuid"
	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	processdto "github.com/apache/airavata/api/process/dto"
	processmodel "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	processsvc "github.com/apache/airavata/api/process/service"
	"github.com/apache/airavata/api/session/dto"
	"github.com/apache/airavata/api/session/model"
	"github.com/apache/airavata/api/session/repository"
)

type SessionService struct {
	db        *gorm.DB
	sessions  *repository.SessionRepository
	processes *processrepo.ProcessRepository
	runs      *processsvc.ProcessService
	launch    *processsvc.LaunchService
}

func NewSessionService(
	db *gorm.DB,
	sessions *repository.SessionRepository,
	processes *processrepo.ProcessRepository,
	runs *processsvc.ProcessService,
	launch *processsvc.LaunchService,
) *SessionService {
	return &SessionService{db: db, sessions: sessions, processes: processes, runs: runs, launch: launch}
}

// ListMine returns the caller's sessions, newest first.
func (s *SessionService) ListMine(ctx context.Context) ([]dto.Response, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	found, err := s.sessions.FindByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	out := make([]dto.Response, 0, len(found))
	for i := range found {
		view, _, err := s.view(ctx, s.processes, &found[i])
		if err != nil {
			return nil, err
		}
		out = append(out, view)
	}
	return out, nil
}

// Get returns one session to its owner or an admin.
func (s *SessionService) Get(ctx context.Context, id string) (*dto.Response, error) {
	sess, err := s.find(ctx, s.sessions, id, true)
	if err != nil {
		return nil, err
	}
	view, _, err := s.view(ctx, s.processes, sess)
	return &view, err
}

// Create writes a session and its first run for the caller, then launches the run.
func (s *SessionService) Create(ctx context.Context, req *dto.Request) (*dto.Response, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	sess := &model.Session{ID: uuid.NewString(), OwnerID: principal.Name, CreatedAt: time.Now().UnixMilli(), CurrentProcessID: uuid.NewString()}
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := s.runs.CreateTx(ctx, tx, &processmodel.Process{ID: sess.CurrentProcessID, SessionID: &sess.ID}, req.RunRequest()); err != nil {
			return err
		}
		return s.sessions.WithTx(tx).Create(ctx, sess)
	})
	if err != nil {
		return nil, err
	}
	return s.launched(ctx, sess.ID, sess.CurrentProcessID)
}

// Start writes the next run from the current one's request, then launches it. Owner only.
func (s *SessionService) Start(ctx context.Context, id string, req *dto.StartRequest) (*dto.Response, error) {
	next := uuid.NewString()
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sessions := s.sessions.WithTx(tx)
		sess, err := s.find(ctx, sessions, id, false)
		if err != nil {
			return err
		}
		view, _, err := s.view(ctx, s.processes.WithTx(tx), sess)
		if err != nil {
			return err
		}
		if view.Live {
			return httpx.Conflict("Session %s has a live run", id)
		}
		// The swap is conditional so that of two concurrent starts only one passes; the
		// other sees no row changed and rolls back with nothing written.
		swapped, err := sessions.SwapCurrent(ctx, id, sess.CurrentProcessID, next)
		if err != nil {
			return err
		}
		if !swapped {
			return httpx.Conflict("Session %s was started concurrently", id)
		}
		run := view.Request
		if req.WallMinutes != nil {
			run.Resources.WallMinutes = *req.WallMinutes
		}
		return s.runs.CreateTx(ctx, tx, &processmodel.Process{ID: next, SessionID: &id}, run.RunRequest())
	})
	if err != nil {
		return nil, err
	}
	return s.launched(ctx, id, next)
}

// Delete removes a session and its runs once none is live. Owner or admin.
func (s *SessionService) Delete(ctx context.Context, id string) error {
	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sessions, processes := s.sessions.WithTx(tx), s.processes.WithTx(tx)
		sess, err := s.find(ctx, sessions, id, true)
		if err != nil {
			return err
		}
		view, runs, err := s.view(ctx, processes, sess)
		if err != nil {
			return err
		}
		if view.Live {
			return httpx.Conflict("Session %s has a live run", id)
		}
		// The row goes first and only while its current run is unchanged, so a start
		// that committed in between turns this into a 409, not an orphaned run.
		deleted, err := sessions.Delete(ctx, sess)
		if err != nil {
			return err
		}
		if !deleted {
			return httpx.Conflict("Session %s was started concurrently", id)
		}
		for i := range runs {
			if err := processes.Delete(ctx, &runs[i]); err != nil {
				return err
			}
		}
		return nil
	})
}

// find loads a session the caller owns, or any session for an admin when admin is set.
func (s *SessionService) find(ctx context.Context, sessions *repository.SessionRepository, id string, admin bool) (*model.Session, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	sess, err := sessions.FindByID(ctx, id)
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, httpx.NotFound("Session not found: %s", id)
	}
	if err != nil {
		return nil, err
	}
	if sess.OwnerID != principal.Name && !(admin && principal.IsAdmin()) {
		return nil, httpx.Forbidden("Access denied: the session belongs to another user")
	}
	return sess, nil
}

// view maps a session over its runs as of now, returning the runs alongside.
func (s *SessionService) view(ctx context.Context, processes *processrepo.ProcessRepository, sess *model.Session) (dto.Response, []processmodel.Process, error) {
	runs, err := processes.FindBySessionID(ctx, sess.ID)
	if err != nil {
		return dto.Response{}, nil, err
	}
	return dto.ToResponse(sess, processdto.ToResponses(runs), time.Now()), runs, nil
}

// launched answers the session after launching its run. A launch error that left the run live is the
// caller's; one that ended it FAILED is answered in the run's error.
func (s *SessionService) launched(ctx context.Context, id, runID string) (*dto.Response, error) {
	_, launchErr := s.launch.LaunchProcess(ctx, runID)
	view, err := s.Get(ctx, id)
	switch {
	case err != nil:
		return nil, errors.Join(launchErr, err)
	case launchErr != nil && view.Live:
		return nil, launchErr
	}
	return view, nil
}
