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
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/orchestration"
	"github.com/apache/airavata/internal/ptr"

	dto "github.com/apache/airavata/api/process/dto"
	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/api/process/repository"
)

// PauseService lists the pauses of a process's workflows and continues a paused one.
//
// A workflow pauses when one of its tasks reaches a host that asks the owner for an
// interactive login (see orchestration.ExecutionEngine.runTask). The owner answers the
// login through the interactive auth API, then continues the process here; the paused
// task runs again from the start, logging in afresh if the connection has gone.
type PauseService struct {
	db             *gorm.DB
	pauses         *repository.TaskPauseRepository
	statuses       *repository.StatusRepository
	processService *ProcessService
	statusService  *StatusService

	executionEngine *orchestration.ExecutionEngine
}

// NewPauseService returns a pause service.
func NewPauseService(db *gorm.DB, pauses *repository.TaskPauseRepository, statuses *repository.StatusRepository,
	processService *ProcessService, statusService *StatusService, executionEngine *orchestration.ExecutionEngine) *PauseService {
	return &PauseService{
		db:              db,
		pauses:          pauses,
		statuses:        statuses,
		processService:  processService,
		statusService:   statusService,
		executionEngine: executionEngine,
	}
}

// ListForProcess returns every pause of a process, oldest first. Not owner-scoped,
// matching the other reads of this package.
func (s *PauseService) ListForProcess(ctx context.Context, processID string) ([]dto.TaskPauseResponse, error) {
	if _, err := s.processService.Get(ctx, processID); err != nil {
		return nil, err
	}
	found, err := s.pauses.FindByProcessID(ctx, processID)
	if err != nil {
		return nil, err
	}
	return dto.ToTaskPauseResponses(found), nil
}

// Continue wakes the workflow waiting on a process's open pause, and returns the pause
// it closed.
//
// Owner-scoped like launching, and for the same reason: the task runs again against a
// host under the identity the run was submitted with. A process with nothing paused is
// a conflict. The pause is closed, the status recorded and the workflow signalled in one
// transaction, so a signal that cannot be delivered leaves the pause open to try again.
func (s *PauseService) Continue(ctx context.Context, processID string) (*dto.TaskPauseResponse, error) {
	proc, err := s.processService.Get(ctx, processID)
	if err != nil {
		return nil, err
	}
	if err := requireOwner(ctx, proc, "Access denied: you may only continue your own processes"); err != nil {
		return nil, err
	}

	var out dto.TaskPauseResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		pause, err := s.pauses.WithTx(tx).FindOpenByProcessID(ctx, processID)
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return httpx.Conflict("Process %s is not paused", processID)
		}
		if err != nil {
			return err
		}

		now := time.Now().UnixMilli()
		closed, err := s.pauses.WithTx(tx).MarkContinued(ctx, pause.ID, now)
		if err != nil {
			return err
		}
		if !closed {
			return httpx.Conflict("Process %s has already been continued", processID)
		}
		pause.ContinuedAt = &now

		resumed, err := s.statusBeforePause(ctx, tx, processID)
		if err != nil {
			return err
		}
		if _, err := s.statusService.RecordTx(ctx, tx, processID, resumed,
			ptr.To("Continued after an interactive SSH login")); err != nil {
			return err
		}

		if err := s.executionEngine.ContinueWorkflow(ctx, pause.WorkflowInstanceID, pause.ID); err != nil {
			return err
		}
		out = dto.ToTaskPauseResponse(pause)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// statusBeforePause is the status the process had before its workflow last paused,
// which is what it returns to once continued.
func (s *PauseService) statusBeforePause(ctx context.Context, tx *gorm.DB, processID string) (model.ProcessStatusType, error) {
	history, err := s.statuses.WithTx(tx).FindByProcessID(ctx, processID)
	if err != nil {
		return "", err
	}
	for i := len(history) - 1; i >= 0; i-- {
		if st := history[i].Status; st != nil && *st != model.ProcessStatusTypePaused {
			return *st, nil
		}
	}
	return model.ProcessStatusTypeSubmitted, nil
}
