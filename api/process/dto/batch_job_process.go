package dto

import (
	"github.com/apache/airavata/internal/httpx"

	applicationdto "github.com/apache/airavata/api/application/dto"
	model "github.com/apache/airavata/api/process/model"
)

// Request is the create/update payload for a process.
//
// The resource request is carried here rather than copied from the deployment's
// default, which is what lets a caller ask for different resources for a particular
// run. There is no owner field: ownership comes from the access token.
//
// Java: org.apache.airavata.process.dto.BatchJobProcessRequestDto
type Request struct {
	DeploymentID   string                                `json:"deploymentId"`
	BatchJobConfig *applicationdto.BatchJobConfigRequest `json:"batchJobConfig"`
}

// Validate implements httpx.Validator.
func (r *Request) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("deploymentId", "Deployment id cannot be blank", r.DeploymentID)
	if r.BatchJobConfig == nil {
		c.Add("batchJobConfig", "Batch job config cannot be null")
	} else {
		c.Nested("batchJobConfig", r.BatchJobConfig)
	}
	return c.Fields()
}

// Response is the read model for a process. The nested config is a snapshot of what
// this run actually asked for.
//
// Java: org.apache.airavata.process.dto.BatchJobProcessResponseDto
type Response struct {
	ProcessID      string                                 `json:"processId"`
	DeploymentID   *string                                `json:"deploymentId"`
	UserID         *string                                `json:"userId"`
	BatchJobConfig *applicationdto.BatchJobConfigResponse `json:"batchJobConfig"`
}

func ToResponse(p *model.BatchJobProcess) Response {
	return Response{
		ProcessID:      p.ID,
		DeploymentID:   p.DeploymentID,
		UserID:         p.OwnerID,
		BatchJobConfig: applicationdto.ToBatchJobConfigResponse(p.BatchJobConfig),
	}
}

func ToResponses(in []model.BatchJobProcess) []Response {
	out := make([]Response, 0, len(in))
	for i := range in {
		out = append(out, ToResponse(&in[i]))
	}
	return out
}
