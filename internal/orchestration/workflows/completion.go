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
	"log/slog"
	"time"

	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/internal/orchestration/execution"
	internalptr "github.com/apache/airavata/internal/ptr"
)

// CompleteProcess is the last activity of the completion workflow: it marks the process
// complete once its outputs are staged.
func (a *Workflows) CompleteProcess(ctx context.Context, executionContext *execution.Context, processID string) (*execution.Context, error) {
	if execution.Stopped(ctx, a.processes, processID) {
		return executionContext, nil
	}
	slog.Info("Completing process", "processID", processID)

	runningStatus := model.ProcessStatusTypeCompleted
	if err := a.processStatus.Create(ctx, &model.ProcessStatus{
		ProcessID: &processID,
		Status:    &runningStatus,
		Log:       internalptr.To("Process has completed"),
		Timestamp: internalptr.To(time.Now().UnixMilli()),
	}); err != nil {
		slog.Error("Failed to create process status", "processID", processID, "error", err)
		return nil, err
	}
	return executionContext, nil
}
