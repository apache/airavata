package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/credentials/model"
)

// SSHEndpointCredentialRequest is the create/update payload for a binding.
//
// There is no owner field, and that is the point: the owning user comes from the
// access token, so a caller cannot create a binding on someone else's behalf.
//
// Java: org.apache.airavata.compute.dto.SSHEndpointCredentialRequestDto
type SSHEndpointCredentialRequest struct {
	SSHEndpointID   string `json:"sshEndpointId"`
	SSHCredentialID string `json:"sshCredentialId"`
}

// Validate implements httpx.Validator.
func (r *SSHEndpointCredentialRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("sshEndpointId", "SSH endpoint id cannot be blank", r.SSHEndpointID)
	c.NotBlank("sshCredentialId", "SSH credential id cannot be blank", r.SSHCredentialID)
	return c.Fields()
}

// SSHEndpointCredentialResponse is the read model for a binding.
//
// Permission is what the calling principal may do with it — `WRITE` for the owner and
// for admins, otherwise whatever the strongest share reaching them grants. It is a
// property of the request rather than of the record, which is why it is not stored.
//
// Java: org.apache.airavata.compute.dto.SSHEndpointCredentialResponseDto
type SSHEndpointCredentialResponse struct {
	SSHEndpointCredentialID string                                 `json:"sshEndpointCredentialId"`
	SSHEndpointID           *string                                `json:"sshEndpointId"`
	SSHCredentialID         *string                                `json:"sshCredentialId"`
	UserID                  *string                                `json:"userId"`
	Permission              *model.SSHEndpointCredentialPermission `json:"permission,omitempty"`
}

func ToSSHEndpointCredentialResponse(c *model.SSHEndpointCredential) SSHEndpointCredentialResponse {
	return SSHEndpointCredentialResponse{
		SSHEndpointCredentialID: c.ID,
		SSHEndpointID:           c.SSHEndpointID,
		SSHCredentialID:         c.SSHCredentialID,
		UserID:                  c.OwnerID,
	}
}

// ToSSHEndpointCredentialResponseWith is ToSSHEndpointCredentialResponse with the
// caller's effective permission attached.
func ToSSHEndpointCredentialResponseWith(c *model.SSHEndpointCredential, permission model.SSHEndpointCredentialPermission) SSHEndpointCredentialResponse {
	out := ToSSHEndpointCredentialResponse(c)
	out.Permission = &permission
	return out
}

func ToSSHEndpointCredentialResponses(in []model.SSHEndpointCredential) []SSHEndpointCredentialResponse {
	out := make([]SSHEndpointCredentialResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSSHEndpointCredentialResponse(&in[i]))
	}
	return out
}

// SSHEndpointCredentialGroupSharingRequest shares a credential with a group.
//
// The credential comes from the path rather than the body, so a share cannot be moved
// to a different credential by editing it.
type SSHEndpointCredentialGroupSharingRequest struct {
	GroupID    string                                 `json:"groupId"`
	Permission *model.SSHEndpointCredentialPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *SSHEndpointCredentialGroupSharingRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("groupId", "Group id cannot be blank", r.GroupID)
	validatePermission(&c, r.Permission)
	return c.Fields()
}

// Grant returns the permission to store, defaulting to READ. Read-only is the safe
// default for a share: widening it is a deliberate act.
func (r *SSHEndpointCredentialGroupSharingRequest) Grant() model.SSHEndpointCredentialPermission {
	return grantOrRead(r.Permission)
}

// SSHEndpointCredentialUserSharingRequest shares a credential with one user.
type SSHEndpointCredentialUserSharingRequest struct {
	UserID     string                                 `json:"userId"`
	Permission *model.SSHEndpointCredentialPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *SSHEndpointCredentialUserSharingRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("userId", "User id cannot be blank", r.UserID)
	validatePermission(&c, r.Permission)
	return c.Fields()
}

// Grant returns the permission to store, defaulting to READ.
func (r *SSHEndpointCredentialUserSharingRequest) Grant() model.SSHEndpointCredentialPermission {
	return grantOrRead(r.Permission)
}

// SharingUpdate changes what an existing share grants. The subject — group or user —
// is fixed at creation; only the permission is editable.
type SharingUpdate struct {
	Permission *model.SSHEndpointCredentialPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *SharingUpdate) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotNil("permission", "Permission cannot be null", r.Permission)
	validatePermission(&c, r.Permission)
	return c.Fields()
}

// SSHEndpointCredentialGroupSharingResponse is the read model for a group share.
type SSHEndpointCredentialGroupSharingResponse struct {
	SharingID               string                                 `json:"sshEndpointCredentialGroupSharingId"`
	SSHEndpointCredentialID *string                                `json:"sshEndpointCredentialId"`
	GroupID                 *string                                `json:"groupId"`
	Permission              *model.SSHEndpointCredentialPermission `json:"permission"`
}

func ToGroupSharingResponse(s *model.SSHEndpointCredentialGroupSharing) SSHEndpointCredentialGroupSharingResponse {
	return SSHEndpointCredentialGroupSharingResponse{
		SharingID:               s.ID,
		SSHEndpointCredentialID: s.SSHEndpointCredentialID,
		GroupID:                 s.GroupID,
		Permission:              s.Permission,
	}
}

func ToGroupSharingResponses(in []model.SSHEndpointCredentialGroupSharing) []SSHEndpointCredentialGroupSharingResponse {
	out := make([]SSHEndpointCredentialGroupSharingResponse, 0, len(in))
	for i := range in {
		out = append(out, ToGroupSharingResponse(&in[i]))
	}
	return out
}

// SSHEndpointCredentialUserSharingResponse is the read model for a user share.
type SSHEndpointCredentialUserSharingResponse struct {
	SharingID               string                                 `json:"sshEndpointCredentialUserSharingId"`
	SSHEndpointCredentialID *string                                `json:"sshEndpointCredentialId"`
	UserID                  *string                                `json:"userId"`
	Permission              *model.SSHEndpointCredentialPermission `json:"permission"`
}

func ToUserSharingResponse(s *model.SSHEndpointCredentialUserSharing) SSHEndpointCredentialUserSharingResponse {
	return SSHEndpointCredentialUserSharingResponse{
		SharingID:               s.ID,
		SSHEndpointCredentialID: s.SSHEndpointCredentialID,
		UserID:                  s.UserID,
		Permission:              s.Permission,
	}
}

func ToUserSharingResponses(in []model.SSHEndpointCredentialUserSharing) []SSHEndpointCredentialUserSharingResponse {
	out := make([]SSHEndpointCredentialUserSharingResponse, 0, len(in))
	for i := range in {
		out = append(out, ToUserSharingResponse(&in[i]))
	}
	return out
}

// validatePermission rejects an unrecognised permission before it reaches the write,
// which could only fail with an opaque error.
func validatePermission(c *httpx.Constraints, p *model.SSHEndpointCredentialPermission) {
	if p != nil && !p.Valid() {
		c.Add("permission", "Permission must be one of READ, WRITE")
	}
}

func grantOrRead(p *model.SSHEndpointCredentialPermission) model.SSHEndpointCredentialPermission {
	if p == nil {
		return model.SSHEndpointCredentialPermissionRead
	}
	return *p
}
