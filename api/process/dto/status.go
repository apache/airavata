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
