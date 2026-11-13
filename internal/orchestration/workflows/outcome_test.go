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

package workflows

import (
	"context"
	"errors"
	"testing"
	"time"

	"github.com/cschleiden/go-workflows/registry"
	"github.com/cschleiden/go-workflows/tester"
	workflow "github.com/cschleiden/go-workflows/workflow"
	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/ptr"
)

// runRecorded runs a workflow whose body returns bodyErr under recordingOutcome, with
// the real RecordWorkflowStatus activity writing to an in-memory database, and returns
// the workflow's error and the record it left. With stop, the process is stopped and
// the workflow cancelled while its body waits.
func runRecorded(t *testing.T, bodyErr error, stop bool) (error, model.ProcessWorkflow) {
	t.Helper()
	gdb, err := gorm.Open(sqlite.Open("file::memory:?_pragma=foreign_keys(1)"), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatalf("automigrate: %v", err)
	}
	proc := &model.Process{}
	if err := gdb.Create(proc).Error; err != nil {
		t.Fatalf("create process: %v", err)
	}

	w := New(nil, nil, nil, nil, nil, nil, nil, processrepo.NewWorkflowRepository(gdb), processrepo.NewProcessRepository(gdb))
	wf := func(ctx workflow.Context, processID string) error {
		return w.recordingOutcome(ctx, processID, model.ProcessWorkflowTypeBatchJobSubmission,
			func() error {
				if stop {
					return workflow.Sleep(ctx, time.Hour)
				}
				return bodyErr
			})
	}

	wft := tester.NewWorkflowTester[any](wf)
	if stop {
		if err := gdb.Create(&model.ProcessStatus{ProcessID: &proc.ID, Status: ptr.To(model.ProcessStatusTypeStopped)}).Error; err != nil {
			t.Fatalf("stop process: %v", err)
		}
		cancelled := false
		if err := wft.Registry().RegisterActivity(func(context.Context, string) error { cancelled = true; return nil },
			registry.WithName("CancelBatchJob")); err != nil {
			t.Fatalf("register activity: %v", err)
		}
		wft.ScheduleCallback(time.Minute, wft.CancelWorkflow)
		defer func() {
			if !cancelled {
				t.Error("the job of the stopped process was not cancelled")
			}
		}()
	}
	if err := wft.Registry().RegisterActivity(w.RecordWorkflowStatus); err != nil {
		t.Fatalf("register activity: %v", err)
	}
	wft.Execute(context.Background(), proc.ID)
	if !wft.WorkflowFinished() {
		t.Fatal("workflow did not finish")
	}
	_, wfErr := wft.WorkflowResult()

	var rows []model.ProcessWorkflow
	if err := gdb.Where("process_id = ?", proc.ID).Find(&rows).Error; err != nil {
		t.Fatalf("find process workflows: %v", err)
	}
	if len(rows) != 1 {
		t.Fatalf("process workflows = %d, want one record updated in place", len(rows))
	}
	return wfErr, rows[0]
}

func TestRecordingOutcomeMarksSuccess(t *testing.T) {
	wfErr, rec := runRecorded(t, nil, false)
	if wfErr != nil {
		t.Fatalf("workflow error = %v, want nil", wfErr)
	}
	if rec.Status == nil || *rec.Status != model.ProcessWorkflowStatusTypeSucceeded {
		t.Errorf("status = %v, want SUCCEEDED", rec.Status)
	}
	if rec.WorkflowID == "" || rec.ExecutionID == nil || *rec.ExecutionID == "" {
		t.Error("record does not name the workflow instance and its execution")
	}
}

func TestRecordingOutcomeMarksFailure(t *testing.T) {
	wfErr, rec := runRecorded(t, errors.New("staging failed"), false)
	if wfErr == nil {
		t.Fatal("workflow error = nil, want the body's error passed through")
	}
	if rec.Status == nil || *rec.Status != model.ProcessWorkflowStatusTypeFailed {
		t.Errorf("status = %v, want FAILED", rec.Status)
	}
}

func TestACancelledWorkflowCancelsTheJobAndIsRecordedStopped(t *testing.T) {
	_, rec := runRecorded(t, nil, true)
	if rec.Status == nil || *rec.Status != model.ProcessWorkflowStatusTypeStopped {
		t.Errorf("status = %v, want STOPPED", rec.Status)
	}
}
