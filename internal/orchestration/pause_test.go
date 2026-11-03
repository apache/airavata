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
	"context"
	"fmt"
	"sync/atomic"
	"testing"
	"time"

	"github.com/cschleiden/go-workflows/tester"
	workflow "github.com/cschleiden/go-workflows/workflow"
	"github.com/stretchr/testify/mock"

	model "github.com/apache/airavata/api/process/model"
	ssh "github.com/apache/airavata/internal/ssh"
)

// loginTaskRuns counts runs of loginTask, which asks for a login on its first run only,
// as a task does whose connection was answered while the workflow was parked.
var loginTaskRuns atomic.Int32

func loginTask(ctx context.Context, executionContext *ExecutionContext, processID, taskID string) (_ *ExecutionContext, err error) {
	defer deferInteractiveAuth(&err)
	if loginTaskRuns.Add(1) == 1 {
		return nil, fmt.Errorf("ssh mkdir: %w", &ssh.InteractiveAuthRequiredError{Host: "delta", Port: 22, LoginUser: "alice", AuthID: "auth-1"})
	}
	executionContext.SomeData = "done"
	return executionContext, nil
}

// A task that stops for a login parks the workflow, is not retried while parked, and
// runs again once the continue signal arrives.
func TestRunTaskPausesUntilContinued(t *testing.T) {
	loginTaskRuns.Store(0)
	engine := &ExecutionEngine{}

	wf := func(ctx workflow.Context) (*ExecutionContext, error) {
		return engine.runTask(ctx, workflow.ActivityOptions{RetryOptions: retryOptions(model.OnFailureActionRetry, 3)},
			loginTask, model.PausedTaskTypeJobSubmission, &ExecutionContext{}, "proc-1", "task-1")
	}

	wt := tester.NewWorkflowTester[*ExecutionContext](wf)
	wt.Registry().RegisterActivity(loginTask)
	var paused atomic.Bool
	wt.OnActivity(engine.pauseTask, mock.Anything, "proc-1", "task-1", model.PausedTaskTypeJobSubmission, mock.Anything, mock.Anything).
		Run(func(mock.Arguments) { paused.Store(true) }).
		Return(nil).Once()

	wt.ScheduleCallback(time.Minute, func() {
		if !paused.Load() {
			t.Error("workflow was continued before it paused")
		}
		if n := loginTaskRuns.Load(); n != 1 {
			t.Errorf("task ran %d times while parked, want 1", n)
		}
		wt.SignalWorkflow(continueSignal, "pause-1")
	})

	wt.Execute(context.Background())
	if !wt.WorkflowFinished() {
		t.Fatal("workflow did not finish")
	}
	out, err := wt.WorkflowResult()
	if err != nil {
		t.Fatal(err)
	}
	if out == nil || out.SomeData != "done" || loginTaskRuns.Load() != 2 {
		t.Fatalf("task was not run again after continuing: result %+v, runs %d", out, loginTaskRuns.Load())
	}
	wt.AssertExpectations(t)
}

// Only a login is a reason to park: any other failure still fails the task.
func TestIsInteractiveAuthRequired(t *testing.T) {
	login := fmt.Errorf("ssh mkdir: %w", &ssh.InteractiveAuthRequiredError{AuthID: "auth-1"})
	if !isInteractiveAuthRequired(login) {
		t.Error("a login was not recognised")
	}
	if !isInteractiveAuthRequired(workflow.NewPermanentError(login)) {
		t.Error("a login was not recognised once wrapped for the workflow backend")
	}
	if isInteractiveAuthRequired(workflow.NewError(fmt.Errorf("ssh mkdir: connection refused"))) {
		t.Error("an ordinary failure was taken for a login")
	}
}
