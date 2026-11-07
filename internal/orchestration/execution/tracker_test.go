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

package execution_test

import (
	"context"
	"errors"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/orchestration/execution"
)

func newTracker(t *testing.T) (*execution.TaskTracker, *processrepo.TaskStatusRepository, *gorm.DB) {
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
	statuses := processrepo.NewTaskStatusRepository(gdb)
	return execution.NewTaskTracker(statuses), statuses, gdb
}

func statusesOf(t *testing.T, repo *processrepo.TaskStatusRepository, taskID string) []model.TaskStatusType {
	t.Helper()
	rows, err := repo.FindByTaskID(context.Background(), taskID)
	if err != nil {
		t.Fatalf("find statuses: %v", err)
	}
	out := make([]model.TaskStatusType, 0, len(rows))
	for _, r := range rows {
		out = append(out, *r.Status)
	}
	return out
}

func TestTrackRecordsExecutingThenCompleted(t *testing.T) {
	tracker, repo, gdb := newTracker(t)
	proc := &model.Process{}
	if err := gdb.Create(proc).Error; err != nil {
		t.Fatalf("create process: %v", err)
	}

	want := &execution.Context{SomeData: "carried"}
	got, err := tracker.Track(context.Background(), proc.ID, "task-1", model.TaskTypeDataStaging, nil,
		func() (*execution.Context, error) { return want, nil })
	if err != nil {
		t.Fatalf("track: %v", err)
	}
	if got != want {
		t.Errorf("Track returned %v, want the run's own context", got)
	}

	statuses := statusesOf(t, repo, "task-1")
	if len(statuses) != 2 || statuses[0] != model.TaskStatusTypeExecuting || statuses[1] != model.TaskStatusTypeCompleted {
		t.Errorf("statuses = %v, want [EXECUTING COMPLETED]", statuses)
	}
}

func TestTrackRecordsFailureWithItsError(t *testing.T) {
	tracker, repo, gdb := newTracker(t)
	proc := &model.Process{}
	if err := gdb.Create(proc).Error; err != nil {
		t.Fatalf("create process: %v", err)
	}

	boom := errors.New("scp refused")
	_, err := tracker.Track(context.Background(), proc.ID, "task-2", model.TaskTypeJobSubmission, nil,
		func() (*execution.Context, error) { return nil, boom })
	if !errors.Is(err, boom) {
		t.Fatalf("Track error = %v, want the run's own error", err)
	}

	rows, err := repo.FindByTaskID(context.Background(), "task-2")
	if err != nil {
		t.Fatalf("find statuses: %v", err)
	}
	if len(rows) != 2 || *rows[1].Status != model.TaskStatusTypeFailed {
		t.Fatalf("statuses = %v, want EXECUTING then FAILED", statusesOf(t, repo, "task-2"))
	}
	if rows[1].Log == nil || *rows[1].Log != boom.Error() {
		t.Errorf("FAILED log = %v, want %q", rows[1].Log, boom.Error())
	}
	if rows[1].TaskType != model.TaskTypeJobSubmission {
		t.Errorf("TaskType = %q, want %q", rows[1].TaskType, model.TaskTypeJobSubmission)
	}
}

// A status that cannot be written must not fail the task it describes.
func TestTrackSurvivesUnwritableStatus(t *testing.T) {
	tracker, repo, _ := newTracker(t)

	// No such process, so the foreign key rejects every status row.
	_, err := tracker.Track(context.Background(), "missing-process", "task-3", model.TaskTypeJobMonitoring, nil,
		func() (*execution.Context, error) { return &execution.Context{}, nil })
	if err != nil {
		t.Fatalf("Track error = %v, want the run's success to stand", err)
	}
	if statuses := statusesOf(t, repo, "task-3"); len(statuses) != 0 {
		t.Errorf("statuses = %v, want none written", statuses)
	}
}

// Deleting a task takes its history with it, standing in for the foreign key task_id
// cannot carry.
func TestDeletingTaskRemovesItsStatuses(t *testing.T) {
	tracker, repo, gdb := newTracker(t)
	proc := &model.Process{}
	if err := gdb.Create(proc).Error; err != nil {
		t.Fatalf("create process: %v", err)
	}
	task := &model.DataStagingTask{ProcessID: &proc.ID}
	if err := gdb.Create(task).Error; err != nil {
		t.Fatalf("create task: %v", err)
	}
	tracker.Record(context.Background(), proc.ID, task.ID, model.TaskTypeDataStaging, model.TaskStatusTypeCreated, nil)

	if err := processrepo.NewDataStagingTaskRepository(gdb).Delete(context.Background(), task); err != nil {
		t.Fatalf("delete task: %v", err)
	}
	if statuses := statusesOf(t, repo, task.ID); len(statuses) != 0 {
		t.Errorf("statuses = %v, want none after the task was deleted", statuses)
	}
}

// A task that has completed is not run again, so a retried workflow passes over the
// steps that already got through and hands their input on unchanged.
func TestTrackSkipsCompletedTask(t *testing.T) {
	tracker, repo, gdb := newTracker(t)
	proc := &model.Process{}
	if err := gdb.Create(proc).Error; err != nil {
		t.Fatalf("create process: %v", err)
	}
	ctx := context.Background()
	if _, err := tracker.Track(ctx, proc.ID, "task-4", model.TaskTypeDataStaging, nil,
		func() (*execution.Context, error) { return &execution.Context{}, nil }); err != nil {
		t.Fatalf("first run: %v", err)
	}

	in := &execution.Context{SomeData: "handed on"}
	ran := false
	got, err := tracker.Track(ctx, proc.ID, "task-4", model.TaskTypeDataStaging, in,
		func() (*execution.Context, error) { ran = true; return nil, errors.New("must not run") })
	if err != nil {
		t.Fatalf("second run: %v", err)
	}
	if ran {
		t.Error("a completed task ran again")
	}
	if got != in {
		t.Errorf("Track returned %v, want the input context", got)
	}
	if got := statusesOf(t, repo, "task-4"); len(got) != 2 {
		t.Errorf("statuses = %v, want only the first run's EXECUTING and COMPLETED", got)
	}

	// A failed task is not completed, so it runs again.
	if _, err := tracker.Track(ctx, proc.ID, "task-5", model.TaskTypeDataStaging, nil,
		func() (*execution.Context, error) { return nil, errors.New("boom") }); err == nil {
		t.Fatal("failing run returned no error")
	}
	ran = false
	if _, err := tracker.Track(ctx, proc.ID, "task-5", model.TaskTypeDataStaging, nil,
		func() (*execution.Context, error) { ran = true; return &execution.Context{}, nil }); err != nil {
		t.Fatalf("rerun of failed task: %v", err)
	}
	if !ran {
		t.Error("a failed task was skipped")
	}
}
