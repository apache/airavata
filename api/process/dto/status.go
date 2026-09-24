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

// StatusResponse is the read model for one recorded process status.
//
// There is deliberately no request DTO alongside it: statuses are never created or
// updated from a client request body, only recorded internally by StatusService and
// read back through it.
type StatusResponse struct {
	ProcessStatusID string                   `json:"processStatusId"`
	ProcessID       *string                  `json:"processId"`
	Status          *model.ProcessStatusType `json:"status"`
	Log             *string                  `json:"log"`
	Timestamp       *int64                   `json:"timestamp"`
}

func ToStatusResponse(s *model.ProcessStatus) StatusResponse {
	return StatusResponse{
		ProcessStatusID: s.ID,
		ProcessID:       s.ProcessID,
		Status:          s.Status,
		Log:             s.Log,
		Timestamp:       s.Timestamp,
	}
}

func ToStatusResponses(in []model.ProcessStatus) []StatusResponse {
	out := make([]StatusResponse, 0, len(in))
	for i := range in {
		out = append(out, ToStatusResponse(&in[i]))
	}
	return out
}

type BatchJobStatusResponse struct {
	BatchProcessStatusID string                   `json:"batchProcessStatusId"`
	BatchProcessID       string                   `json:"batchProcessId"`
	Status               model.BatchJobStatusType `json:"status"`

	// UpdatedAt is when the state was recorded, in milliseconds since the epoch — the
	// same wire shape as StatusResponse.Timestamp.
	UpdatedAt int64 `json:"updatedAt"`
}

func ToBatchJobStatusResponse(s *model.BatchJobStatus) BatchJobStatusResponse {
	return BatchJobStatusResponse{
		BatchProcessStatusID: s.ID,
		BatchProcessID:       s.BatchProcessID,
		Status:               s.Status,
		UpdatedAt:            s.UpdatedAt.UnixMilli(),
	}
}

func ToBatchJobStatusResponses(in []model.BatchJobStatus) []BatchJobStatusResponse {
	out := make([]BatchJobStatusResponse, 0, len(in))
	for i := range in {
		out = append(out, ToBatchJobStatusResponse(&in[i]))
	}
	return out
}

func ToBatchJobStatusResponsesFromPtrs(in []*model.BatchJobStatus) []BatchJobStatusResponse {
	out := make([]BatchJobStatusResponse, 0, len(in))
	for _, s := range in {
		if s == nil {
			continue
		}
		out = append(out, ToBatchJobStatusResponse(s))
	}
	return out
}
