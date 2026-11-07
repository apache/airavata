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

// ExecutionResponse is the read model for one recorded process execution. Like
// StatusResponse it has no request counterpart: executions are recorded when a
// workflow is started for a process, never accepted from a client.
type ExecutionResponse struct {
	ProcessExecutionID string                            `json:"processExecutionId"`
	ProcessID          *string                           `json:"processId"`
	ExecutionID        string                            `json:"executionId"`
	ExecutionType      *model.ProcessExecutionType       `json:"executionType"`
	Status             *model.ProcessExecutionStatusType `json:"status"`
	Timestamp          *int64                            `json:"timestamp"`
}

func ToExecutionResponse(e *model.ProcessExecution) ExecutionResponse {
	return ExecutionResponse{
		ProcessExecutionID: e.ID,
		ProcessID:          e.ProcessID,
		ExecutionID:        e.ExecutionID,
		ExecutionType:      e.ExecutionType,
		Status:             e.Status,
		Timestamp:          e.Timestamp,
	}
}

func ToExecutionResponses(in []model.ProcessExecution) []ExecutionResponse {
	out := make([]ExecutionResponse, 0, len(in))
	for i := range in {
		out = append(out, ToExecutionResponse(&in[i]))
	}
	return out
}
