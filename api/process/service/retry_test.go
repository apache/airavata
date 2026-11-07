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
	"errors"
	"net/http"
	"testing"

	"github.com/apache/airavata/internal/httpx"

	procmodel "github.com/apache/airavata/api/process/model"
)

func TestWorkflowToRetry(t *testing.T) {
	const (
		sub  = procmodel.ProcessWorkflowTypeBatchJobSubmission
		comp = procmodel.ProcessWorkflowTypeJobCompletion

		running   = procmodel.ProcessWorkflowStatusTypeRunning
		succeeded = procmodel.ProcessWorkflowStatusTypeSucceeded
		failed    = procmodel.ProcessWorkflowStatusTypeFailed
	)
	run := func(wt procmodel.ProcessWorkflowType, st procmodel.ProcessWorkflowStatusType) procmodel.ProcessWorkflow {
		return procmodel.ProcessWorkflow{WorkflowType: &wt, Status: &st}
	}

	for _, tc := range []struct {
		name     string
		recorded []procmodel.ProcessWorkflow
		want     procmodel.ProcessWorkflowType // empty: refused with a conflict
	}{
		{"never launched", nil, ""},
		{"submission failed", []procmodel.ProcessWorkflow{run(sub, failed)}, sub},
		{"submission still running", []procmodel.ProcessWorkflow{run(sub, running)}, ""},
		{"submission succeeded, nothing failed", []procmodel.ProcessWorkflow{run(sub, succeeded)}, ""},
		{"completion failed", []procmodel.ProcessWorkflow{run(sub, succeeded), run(comp, failed)}, comp},
		{"completion retried and running", []procmodel.ProcessWorkflow{run(sub, succeeded), run(comp, failed), run(comp, running)}, ""},
		{"earlier failure already got past", []procmodel.ProcessWorkflow{run(sub, failed), run(sub, succeeded)}, ""},
		{"submission retried and failed again", []procmodel.ProcessWorkflow{run(sub, failed), run(sub, failed)}, sub},
	} {
		t.Run(tc.name, func(t *testing.T) {
			got, err := workflowToRetry("proc-1", tc.recorded)
			if tc.want == "" {
				var he *httpx.Error
				if !errors.As(err, &he) || he.Status != http.StatusConflict {
					t.Fatalf("workflowToRetry = %q, %v; want a conflict", got, err)
				}
				return
			}
			if err != nil || got != tc.want {
				t.Fatalf("workflowToRetry = %q, %v; want %q", got, err, tc.want)
			}
		})
	}
}
