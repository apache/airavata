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
	ProcessStatusID string                        `json:"processStatusId"`
	ProcessID       *string                       `json:"processId"`
	Status          *model.BatchProcessStatusType `json:"status"`
	Log             *string                       `json:"log"`
	Timestamp       *int64                        `json:"timestamp"`
}

func ToStatusResponse(s *model.BatchJobProcessStatus) StatusResponse {
	return StatusResponse{
		ProcessStatusID: s.ID,
		ProcessID:       s.ProcessID,
		Status:          s.Status,
		Log:             s.Log,
		Timestamp:       s.Timestamp,
	}
}

func ToStatusResponses(in []model.BatchJobProcessStatus) []StatusResponse {
	out := make([]StatusResponse, 0, len(in))
	for i := range in {
		out = append(out, ToStatusResponse(&in[i]))
	}
	return out
}
