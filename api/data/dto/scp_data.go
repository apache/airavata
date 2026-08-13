package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/data/model"
)

// SCPDataRequest is the create/update payload for a dataset.
//
// There is no owner field and no provision status field: ownership comes from the
// access token, and the lifecycle state belongs to the service.
//
// Java: org.apache.airavata.data.dto.SCPDataRequestDto
type SCPDataRequest struct {
	DataName        string  `json:"dataName"`
	DataDescription *string `json:"dataDescription"`

	IsFile bool `json:"isFile"`

	Path                     string `json:"path"`
	SlurmClusterCredentialID string `json:"slurmClusterCredentialId"`
}

// Validate implements httpx.Validator.
func (r *SCPDataRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("dataName", "Data name cannot be blank", r.DataName)
	c.NotBlank("path", "Path cannot be blank", r.Path)
	c.NotBlank("slurmClusterCredentialId", "Slurm cluster credential id cannot be blank", r.SlurmClusterCredentialID)
	return c.Fields()
}

// SCPDataResponse is the read model for a dataset.
//
// Java: org.apache.airavata.data.dto.SCPDataResponseDto
type SCPDataResponse struct {
	DataID                   string                 `json:"dataId"`
	DataName                 *string                `json:"dataName"`
	DataDescription          *string                `json:"dataDescription"`
	IsFile                   bool                   `json:"isFile"`
	Path                     *string                `json:"path"`
	SlurmClusterCredentialID *string                `json:"slurmClusterCredentialId"`
	ProvisionStatus          *model.ProvisionStatus `json:"provisionStatus"`
	OwnerID                  *string                `json:"ownerId"`
}

// applySCPDataRequest copies the client-writable fields onto an entity. The owner and
// the provision status are deliberately absent: neither is ever taken from a request.
func ApplySCPDataRequest(dst *model.SCPData, src *SCPDataRequest) {
	name, path := src.DataName, src.Path
	dst.DataName = &name
	dst.DataDescription = src.DataDescription
	dst.IsFile = src.IsFile
	dst.Path = &path
}

func ToSCPDataResponse(d *model.SCPData) SCPDataResponse {
	return SCPDataResponse{
		DataID:                   d.ID,
		DataName:                 d.DataName,
		DataDescription:          d.DataDescription,
		IsFile:                   d.IsFile,
		Path:                     d.Path,
		SlurmClusterCredentialID: d.ClusterCredentialID,
		ProvisionStatus:          d.ProvisionStatus,
		OwnerID:                  d.OwnerID,
	}
}

func ToSCPDataResponses(in []model.SCPData) []SCPDataResponse {
	out := make([]SCPDataResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSCPDataResponse(&in[i]))
	}
	return out
}
