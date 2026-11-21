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
	"strings"
	"testing"
	"time"

	"github.com/cschleiden/go-workflows/tester"
	workflow "github.com/cschleiden/go-workflows/workflow"
	"github.com/glebarez/sqlite"
	"github.com/stretchr/testify/mock"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/db"
)

// runRecorded runs a workflow whose body returns bodyErr under recordingOutcome, with
// the real RecordWorkflowStatus activity writing to an in-memory database, and returns
// the workflow's error and the record it left.
func runRecorded(t *testing.T, bodyErr error) (error, model.ProcessWorkflow) {
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

	w := New(nil, nil, nil, nil, nil, nil, processrepo.NewStatusRepository(gdb), processrepo.NewWorkflowRepository(gdb), nil)
	wf := func(ctx workflow.Context, processID string) error {
		return w.recordingOutcome(ctx, processID, model.ProcessWorkflowTypeBatchJobSubmission,
			func(workflow.Context, string) error { return bodyErr })
	}

	wft := tester.NewWorkflowTester[any](wf)
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
	wfErr, rec := runRecorded(t, nil)
	if wfErr != nil {
		t.Fatalf("workflow error = %v, want nil", wfErr)
	}
	if rec.Status == nil || *rec.Status != model.ProcessWorkflowStatusTypeSucceeded {
		t.Errorf("status = %v, want SUCCEEDED", rec.Status)
	}
	if rec.WorkflowID == "" {
		t.Error("record names no workflow instance")
	}
}

func TestRecordingOutcomeMarksFailure(t *testing.T) {
	wfErr, rec := runRecorded(t, errors.New("staging failed"))
	if wfErr == nil {
		t.Fatal("workflow error = nil, want the body's error passed through")
	}
	if rec.Status == nil || *rec.Status != model.ProcessWorkflowStatusTypeFailed {
		t.Errorf("status = %v, want FAILED", rec.Status)
	}
}

// A stopped workflow must release again, since its task in flight may have submitted a
// job after the teardown released.
func TestAStopSignalledWorkflowReleasesAgain(t *testing.T) {
	w := New(nil, nil, nil, nil, nil, nil, nil, nil, nil)
	var calls []string
	record := func(call string) func(mock.Arguments) { return func(mock.Arguments) { calls = append(calls, call) } }
	wft := tester.NewWorkflowTester[any](func(ctx workflow.Context, processID string) error {
		return w.recordingOutcome(ctx, processID, model.ProcessWorkflowTypeBatchJobSubmission,
			// A task in flight runs to its end whatever the stop, as an activity does.
			func(ctx workflow.Context, _ string) error {
				return workflow.Sleep(workflow.NewDisconnectedContext(ctx), time.Hour)
			})
	})
	wft.OnActivity(w.RecordWorkflowStatus, mock.Anything, "p", mock.Anything, mock.Anything, model.ProcessWorkflowStatusTypeRunning).Return(false, nil)
	wft.OnActivity(w.RecordWorkflowStatus, mock.Anything, "p", mock.Anything, mock.Anything, model.ProcessWorkflowStatusTypeStopped).Run(record("workflow stopped")).Return(true, nil).Once()
	wft.OnActivity(w.batch.ReleaseBatchResources, mock.Anything, "p").Run(record("released")).Return(nil).Once()
	wft.OnActivity(w.RecordProcessStatus, mock.Anything, "p", model.ProcessStatusTypeStopped, ProcessStoppedLog).Run(record("process stopped")).Return(nil).Once()
	wft.ScheduleCallback(time.Minute, func() { wft.SignalWorkflow(stopSignal, struct{}{}) })

	wft.Execute(context.Background(), "p")
	if _, err := wft.WorkflowResult(); err != nil {
		t.Fatal(err)
	}
	wft.AssertExpectations(t)
	if got := strings.Join(calls, ","); got != "workflow stopped,released,process stopped" {
		t.Errorf("calls = %s, want the workflow recorded STOPPED, then released, then the process STOPPED last", got)
	}
}

// A workflow that cannot tell whether its process is stopped, or learns it is, must not
// start work a teardown would not wait for.
func TestAWorkflowDoesNoWorkUnlessItRecordedRunningOnAnUnstoppedProcess(t *testing.T) {
	for name, tc := range map[string]struct {
		stopped bool
		err     error
	}{
		"process already stopped": {stopped: true},
		"RUNNING record failed":   {err: errors.New("database down")},
	} {
		t.Run(name, func(t *testing.T) {
			w := New(nil, nil, nil, nil, nil, nil, nil, nil, nil)
			ran := false
			wft := tester.NewWorkflowTester[any](func(ctx workflow.Context, processID string) error {
				return w.recordingOutcome(ctx, processID, model.ProcessWorkflowTypeBatchJobSubmission,
					func(workflow.Context, string) error { ran = true; return nil })
			})
			wft.OnActivity(w.RecordWorkflowStatus, mock.Anything, "p", mock.Anything, mock.Anything, model.ProcessWorkflowStatusTypeRunning).
				Return(tc.stopped, tc.err)
			if tc.stopped {
				wft.OnActivity(w.RecordWorkflowStatus, mock.Anything, "p", mock.Anything, mock.Anything, model.ProcessWorkflowStatusTypeStopped).
					Return(true, nil).Once()
				wft.OnActivity(w.RecordProcessStatus, mock.Anything, "p", model.ProcessStatusTypeStopped, ProcessStoppedLog).Return(nil).Once()
			}
			wft.Execute(context.Background(), "p")
			if _, err := wft.WorkflowResult(); ran || (err == nil) != (tc.err == nil) {
				t.Errorf("body ran = %v, error = %v; want no work and error %v", ran, err, tc.err != nil)
			}
			wft.AssertExpectations(t)
		})
	}
}
