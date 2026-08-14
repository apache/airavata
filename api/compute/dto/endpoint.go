package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/compute/model"
)

// defaultSSHPort is used when a request omits the port, so the common case of a
// standard SSH host does not have to state it.
const defaultSSHPort = 22

// SSHEndpointRequest is the create/update payload for an SSH endpoint.
type SSHEndpointRequest struct {
	Name     string `json:"name"`
	HostName string `json:"hostName"`
	Port     *int   `json:"port"`
}

// Validate implements httpx.Validator.
func (r *SSHEndpointRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("name", "Endpoint name cannot be blank", r.Name)
	c.NotBlank("hostName", "Host name cannot be blank", r.HostName)
	if r.Port != nil && (*r.Port < 1 || *r.Port > 65535) {
		c.Add("port", "Port must be between 1 and 65535")
	}
	return c.Fields()
}

// SSHEndpointResponse is the read model for an SSH endpoint.
type SSHEndpointResponse struct {
	SSHEndpointID string `json:"sshEndpointId"`
	Name          string `json:"name"`
	HostName      string `json:"hostName"`
	Port          int    `json:"port"`
}

// ApplySSHEndpointRequest copies the mutable fields of a request onto an entity. An
// omitted port means 22 rather than 0 — the zero value would be a port nothing can
// connect to, which is worse than a default.
func ApplySSHEndpointRequest(dst *model.SSHEndpoint, src *SSHEndpointRequest) {
	dst.Name = src.Name
	dst.HostName = src.HostName
	if src.Port != nil {
		dst.Port = *src.Port
	} else {
		dst.Port = defaultSSHPort
	}
}

func ToSSHEndpointResponse(e *model.SSHEndpoint) SSHEndpointResponse {
	return SSHEndpointResponse{
		SSHEndpointID: e.ID,
		Name:          e.Name,
		HostName:      e.HostName,
		Port:          e.Port,
	}
}

func ToSSHEndpointResponses(in []model.SSHEndpoint) []SSHEndpointResponse {
	out := make([]SSHEndpointResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSSHEndpointResponse(&in[i]))
	}
	return out
}
