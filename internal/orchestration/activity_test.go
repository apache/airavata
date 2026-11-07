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

package orchestration

import (
	"errors"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	computerepo "github.com/apache/airavata/api/compute/repository"
	model "github.com/apache/airavata/api/process/model"
	processrepo "github.com/apache/airavata/api/process/repository"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/db"
)

// A run whose foreign keys are left unchecked, which is all an activity's bookkeeping reads.
func runEngine(t *testing.T, ids ...string) *ExecutionEngine {
	gdb, err := gorm.Open(sqlite.Open("file::memory:"), &gorm.Config{Logger: logger.Default.LogMode(logger.Silent)})
	if err != nil {
		t.Fatal(err)
	}
	sqlDB, _ := gdb.DB()
	sqlDB.SetMaxOpenConns(1)
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatal(err)
	}
	e := &ExecutionEngine{processes: processrepo.NewProcessRepository(gdb), processStatus: processrepo.NewStatusRepository(gdb), slurmClusterConfigs: computerepo.NewSlurmClusterConfigRepository(gdb)}
	for _, id := range ids {
		if err := e.processes.Save(t.Context(), &model.Process{ID: id}); err != nil {
			t.Fatal(err)
		}
		if err := e.processes.SaveBatchProcess(t.Context(), &model.BatchJobProcess{ID: "b-" + id, ProcessID: &id, SlurmClusterConfigID: "c", BatchJobConfigID: "j-" + id, Connection: "editor"}); err != nil {
			t.Fatal(err)
		}
	}
	return e
}

func (a *ExecutionEngine) state(t *testing.T, id string) model.ProcessStatusType {
	process, err := a.process(t.Context(), id)
	if err != nil {
		t.Fatal(err)
	}
	return process.State()
}

func TestStopAbandonsAnActivityAndStaysCancelled(t *testing.T) {
	e := runEngine(t, "stopped")
	ctx, done := e.activity(t.Context(), "stopped")
	if got := auth.Connection(ctx); got != "editor" {
		t.Errorf("the activity prompts %q, want the launching connection", got)
	}
	abandoned := make(chan struct{})
	go func() { <-ctx.Done(); done(); close(abandoned) }()
	process, _ := e.process(t.Context(), "stopped")
	if err := e.CancelBatchJob(t.Context(), process); err != nil {
		t.Fatal(err)
	}
	<-abandoned
	statuses, _ := e.processStatus.FindByProcessID(t.Context(), "stopped")
	for _, s := range statuses {
		if *s.Status == model.ProcessStatusTypeFailed {
			t.Error("the abandoned activity recorded FAILED over the Stop")
		}
	}
	if e.state(t, "stopped") != model.ProcessStatusTypeCancelled {
		t.Errorf("after a Stop the run is %s, want CANCELLED", e.state(t, "stopped"))
	}

	if ctx, _ := e.activity(t.Context(), "stopped"); ctx.Err() == nil {
		t.Error("an activity of a run already stopped was not abandoned at once")
	}
	if e.failed("stopped", errors.New("abandoned")); e.state(t, "stopped") != model.ProcessStatusTypeCancelled {
		t.Errorf("a failed workflow moved a stopped run to %s", e.state(t, "stopped"))
	}
}

func TestATransientFailureIsRetriedAndOnlyTheLastAttemptFailsTheRun(t *testing.T) {
	e := runEngine(t, "retried", "failed")
	_, done := e.activity(t.Context(), "retried")
	done()
	ctx, done := e.activity(t.Context(), "retried")
	if ctx.Err() != nil {
		t.Fatal("the retry was abandoned at once")
	}
	done()
	if e.state(t, "retried") != model.ProcessStatusTypeCreated {
		t.Errorf("a retried activity left the run %s, want it untouched", e.state(t, "retried"))
	}

	if e.failed("failed", errors.New("no answer")); e.state(t, "failed") != model.ProcessStatusTypeFailed {
		t.Errorf("a workflow whose activity gave up left the run %s, want FAILED", e.state(t, "failed"))
	}
}

// cancelled counts a run's CANCELLED rows, failing unless each says only "Cancelled".
func (a *ExecutionEngine) cancelled(t *testing.T, id string) int {
	statuses, _ := a.processStatus.FindByProcessID(t.Context(), id)
	n := 0
	for _, s := range statuses {
		if *s.Status == model.ProcessStatusTypeCancelled {
			n++
			if *s.Log != "Cancelled" {
				t.Errorf("CANCELLED log = %q, want Cancelled", *s.Log)
			}
		}
	}
	return n
}

// The cluster config "c" does not exist, so an error from Stop or the re-check means it went to scancel.
func TestAStopRacingSbatchCancelsTheJobOnce(t *testing.T) {
	e := runEngine(t, "stop-saw-job", "submit-saw-stop")
	submitted := func(id string) {
		process, _ := e.process(t.Context(), id)
		process.BatchProcess.JobID = new(string)
		if err := e.processes.SaveBatchProcess(t.Context(), process.BatchProcess); err != nil {
			t.Fatal(err)
		}
	}

	stale, _ := e.process(t.Context(), "stop-saw-job")
	submitted("stop-saw-job")
	if err := e.CancelBatchJob(t.Context(), stale); err == nil {
		t.Error("a Stop that read no job id did not cancel the job saved meanwhile")
	}
	if e.state(t, "stop-saw-job") != model.ProcessStatusTypeCancelled || e.cancelled(t, "stop-saw-job") != 1 {
		t.Errorf("the run is %s with %d CANCELLED rows, want CANCELLED once", e.state(t, "stop-saw-job"), e.cancelled(t, "stop-saw-job"))
	}

	early, _ := e.process(t.Context(), "submit-saw-stop")
	if err := e.CancelBatchJob(t.Context(), early); err != nil {
		t.Fatal(err)
	}
	submitted("submit-saw-stop")
	if err := e.cancelIfStopped(t.Context(), "submit-saw-stop"); err == nil {
		t.Error("a submission that saw the Stop did not cancel its job")
	}
	if n := e.cancelled(t, "submit-saw-stop"); n != 1 {
		t.Errorf("Stop and the re-check wrote %d CANCELLED rows, want 1", n)
	}
}

func TestJobGoneMatchesOnlyAJobThatHasEnded(t *testing.T) {
	for answer, gone := range map[string]bool{
		"scancel: error: Kill job error on job id 42: Invalid job id specified":                 true,
		"scancel: error: Kill job error on job id 42: Job/step already completing or completed": true,
		"scancel: error: Kill job error on job id 42: Access/permission denied":                 false,
	} {
		if jobGone.MatchString(answer) != gone {
			t.Errorf("jobGone(%q) = %v, want %v", answer, !gone, gone)
		}
	}
}
