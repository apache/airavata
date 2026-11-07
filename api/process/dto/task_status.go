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

// TaskStatusResponse is the read model for one recorded task status. Like
// StatusResponse it has no request counterpart: task statuses are recorded by the
// orchestration engine as it runs a task, never accepted from a client.
type TaskStatusResponse struct {
	TaskStatusID string                `json:"taskStatusId"`
	TaskID       string                `json:"taskId"`
	TaskType     model.TaskType        `json:"taskType"`
	ProcessID    *string               `json:"processId"`
	Status       *model.TaskStatusType `json:"status"`
	Log          *string               `json:"log"`
	Timestamp    *int64                `json:"timestamp"`
}

func ToTaskStatusResponse(s *model.TaskStatus) TaskStatusResponse {
	return TaskStatusResponse{
		TaskStatusID: s.ID,
		TaskID:       s.TaskID,
		TaskType:     s.TaskType,
		ProcessID:    s.ProcessID,
		Status:       s.Status,
		Log:          s.Log,
		Timestamp:    s.Timestamp,
	}
}

func ToTaskStatusResponses(in []model.TaskStatus) []TaskStatusResponse {
	out := make([]TaskStatusResponse, 0, len(in))
	for i := range in {
		out = append(out, ToTaskStatusResponse(&in[i]))
	}
	return out
}
