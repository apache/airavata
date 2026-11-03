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

package dto

import (
	model "github.com/apache/airavata/api/process/model"
)

// TaskPauseResponse is the read model for one pause of a process's workflow. A pause
// is recorded by the workflow itself and closed by continuing the process, so there is
// no request DTO alongside it.
type TaskPauseResponse struct {
	TaskPauseID        string               `json:"taskPauseId"`
	ProcessID          string               `json:"processId"`
	TaskID             string               `json:"taskId"`
	TaskType           model.PausedTaskType `json:"taskType"`
	WorkflowInstanceID string               `json:"workflowInstanceId"`
	Reason             *string              `json:"reason"`

	// Both in milliseconds since the epoch. ContinuedAt is null while the pause is open.
	PausedAt    int64  `json:"pausedAt"`
	ContinuedAt *int64 `json:"continuedAt"`
}

func ToTaskPauseResponse(p *model.TaskPause) TaskPauseResponse {
	return TaskPauseResponse{
		TaskPauseID:        p.ID,
		ProcessID:          p.ProcessID,
		TaskID:             p.TaskID,
		TaskType:           p.TaskType,
		WorkflowInstanceID: p.WorkflowInstanceID,
		Reason:             p.Reason,
		PausedAt:           p.PausedAt,
		ContinuedAt:        p.ContinuedAt,
	}
}

func ToTaskPauseResponses(in []model.TaskPause) []TaskPauseResponse {
	out := make([]TaskPauseResponse, 0, len(in))
	for i := range in {
		out = append(out, ToTaskPauseResponse(&in[i]))
	}
	return out
}
