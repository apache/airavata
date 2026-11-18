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

	"github.com/cschleiden/go-workflows/registry"
	"github.com/cschleiden/go-workflows/tester"
	workflow "github.com/cschleiden/go-workflows/workflow"
	"github.com/stretchr/testify/mock"

	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/internal/orchestration/tasks/batch"
)

// The backend refers to a workflow or activity by name alone, so each must register,
// under the name a running instance was started with.
func TestRegister(t *testing.T) {
	w := New(nil, nil, nil, nil, nil, nil, nil, nil, nil)
	r := registry.New()
	if err := w.Register(r); err != nil {
		t.Fatal(err)
	}
	for _, name := range []string{"HandleBatchJobSubmission", "HandleBatchJobCompletion", "TearDownStoppedProcess"} {
		if _, err := r.GetWorkflow(name); err != nil {
			t.Errorf("workflow %s: %v", name, err)
		}
	}
	for _, name := range []string{"CopyData", "SubmitBatchJob", "MonitorBatchJob", "RecordProcessStatus", "RecordWorkflowStatus", "SignalRunningWorkflowsToStop", "ReleaseBatchResources"} {
		if _, err := r.GetActivity(name); err != nil {
			t.Errorf("activity %s: %v", name, err)
		}
	}
}

// A teardown must release at once, even when signalling fails, and must leave STOPPED as
// the latest status, carrying the failure when the release fails.
func TestTearDownReleasesAtOnceAndRecordsStoppedLast(t *testing.T) {
	for name, tc := range map[string]struct {
		signalErr, releaseErr error
		log                   string
	}{
		"released":         {log: ProcessStoppedLog},
		"signalling fails": {signalErr: workflow.NewPermanentError(errors.New("backend down")), log: ProcessStoppedLog},
		"release fails":    {releaseErr: workflow.NewPermanentError(errors.New("cluster unreachable")), log: "Releasing the stopped process failed: cluster unreachable"},
	} {
		t.Run(name, func(t *testing.T) {
			w := New(nil, &batch.Tasks{}, nil, nil, nil, nil, nil, nil, nil)
			wft := tester.NewWorkflowTester[any](w.TearDownStoppedProcess)
			wft.OnActivity(w.SignalRunningWorkflowsToStop, mock.Anything, "p").Return(tc.signalErr)
			wft.OnActivity(w.batch.ReleaseBatchResources, mock.Anything, "p").Return(tc.releaseErr)
			wft.OnActivity(w.RecordProcessStatus, mock.Anything, "p", model.ProcessStatusTypeStopped, mock.MatchedBy(func(log string) bool {
				return strings.HasPrefix(log, tc.log)
			})).Return(nil).Once()

			wft.Execute(context.Background(), "p")
			_, err := wft.WorkflowResult()
			if (err != nil) != (tc.signalErr != nil || tc.releaseErr != nil) {
				t.Errorf("error = %v", err)
			}
			wft.AssertExpectations(t)
		})
	}
}
