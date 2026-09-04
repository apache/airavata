package dto

import (
	"github.com/apache/airavata/internal/httpx"

	creddto "github.com/apache/airavata/api/credentials/dto"
	model "github.com/apache/airavata/api/data/model"
)

// SCPDataStorageRequest is the create/update payload for a storage.
//
// There is no owner field: ownership comes from the access token and is immutable, so
// a storage can neither be registered on someone else's behalf nor handed over by
// editing it.
type SCPDataStorageRequest struct {
	DataName        *string `json:"dataName"`
	SSHEndpointID   string  `json:"sshEndpointId"`
	SSHCredentialID string  `json:"sshCredentialId"`
}

// Validate implements httpx.Validator.
func (r *SCPDataStorageRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlankPtr("dataName", "Data name cannot be blank", r.DataName)
	c.NotBlank("sshEndpointId", "SSH endpoint id cannot be blank", r.SSHEndpointID)
	c.NotBlank("sshCredentialId", "SSH credential id cannot be blank", r.SSHCredentialID)
	return c.Fields()
}

// ApplySCPDataStorageRequest copies the mutable fields of a request onto an entity.
// The endpoint and the credential are resolved by the service, which is what turns an
// unknown id into a 404 rather than a dangling reference.
func ApplySCPDataStorageRequest(dst *model.SCPDataStorage, src *SCPDataStorageRequest) {
	dst.Name = src.DataName
}

// SCPDataStorageResponse is the read model for a storage.
//
// Both the endpoint and the credential are inlined — the latter with its key summary,
// never the private material — because a storage is only meaningful together with the
// host it stages through and the account it is reached as.
type SCPDataStorageResponse struct {
	DataID          string                             `json:"dataId"`
	DataName        *string                            `json:"dataName"`
	OwnerID         *string                            `json:"ownerId"`
	SSHEndpointID   *string                            `json:"sshEndpointId"`
	SSHEndpoint     *creddto.SSHEndpointResponse       `json:"sshEndpoint"`
	SSHCredentialID *string                            `json:"sshCredentialId"`
	SSHCredential   *creddto.SSHUserCredentialResponse `json:"sshCredential"`

	Permission *string `json:"permission,omitempty"`
}

func ToSCPDataStorageResponse(s *model.SCPDataStorage) SCPDataStorageResponse {
	out := SCPDataStorageResponse{
		DataID:          s.ID,
		DataName:        s.Name,
		OwnerID:         s.OwnerID,
		SSHEndpointID:   s.SSHEndpointID,
		SSHCredentialID: s.SSHUserCredentialID,
	}
	if s.SSHEndpoint != nil {
		endpoint := creddto.ToSSHEndpointResponse(s.SSHEndpoint)
		out.SSHEndpoint = &endpoint
	}
	if s.SSHUserCredential != nil {
		credential := creddto.ToSSHUserCredentialResponse(s.SSHUserCredential)
		out.SSHCredential = &credential
	}
	return out
}

// ToSCPDataStorageResponseWith is ToSCPDataStorageResponse with the caller's effective
// permission attached.
func ToSCPDataStorageResponseWith(s *model.SCPDataStorage, permission string) SCPDataStorageResponse {
	out := ToSCPDataStorageResponse(s)
	out.Permission = &permission
	return out
}

func ToSCPDataStorageResponses(in []model.SCPDataStorage) []SCPDataStorageResponse {
	out := make([]SCPDataStorageResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSCPDataStorageResponse(&in[i]))
	}
	return out
}

// SCPDataStorageGroupSharingRequest shares a storage with a group.
type SCPDataStorageGroupSharingRequest struct {
	GroupID    string                       `json:"groupId"`
	Permission *model.DataStoragePermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *SCPDataStorageGroupSharingRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("groupId", "Group id cannot be blank", r.GroupID)
	validateStoragePermission(&c, r.Permission)
	return c.Fields()
}

// Grant returns the permission to store, defaulting to READ.
func (r *SCPDataStorageGroupSharingRequest) Grant() model.DataStoragePermission {
	return storageGrantOrRead(r.Permission)
}

// SCPDataStorageUserSharingRequest shares a storage with one user.
type SCPDataStorageUserSharingRequest struct {
	UserID     string                       `json:"userId"`
	Permission *model.DataStoragePermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *SCPDataStorageUserSharingRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("userId", "User id cannot be blank", r.UserID)
	validateStoragePermission(&c, r.Permission)
	return c.Fields()
}

// Grant returns the permission to store, defaulting to READ.
func (r *SCPDataStorageUserSharingRequest) Grant() model.DataStoragePermission {
	return storageGrantOrRead(r.Permission)
}

// SCPDataStorageSharingUpdate changes what an existing storage share grants.
type SCPDataStorageSharingUpdate struct {
	Permission *model.DataStoragePermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *SCPDataStorageSharingUpdate) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotNil("permission", "Permission cannot be null", r.Permission)
	validateStoragePermission(&c, r.Permission)
	return c.Fields()
}

// SCPDataStorageGroupSharingResponse is the read model for a group share.
type SCPDataStorageGroupSharingResponse struct {
	SharingID     string                       `json:"dataStorageGroupSharingId"`
	DataStorageID *string                      `json:"dataStorageId"`
	GroupID       *string                      `json:"groupId"`
	Permission    *model.DataStoragePermission `json:"permission"`
}

func ToSCPDataStorageGroupSharingResponse(s *model.SCPDataStorageGroupSharing) SCPDataStorageGroupSharingResponse {
	return SCPDataStorageGroupSharingResponse{
		SharingID:     s.ID,
		DataStorageID: s.DataStorageID,
		GroupID:       s.GroupID,
		Permission:    s.Permission,
	}
}

func ToSCPDataStorageGroupSharingResponses(in []model.SCPDataStorageGroupSharing) []SCPDataStorageGroupSharingResponse {
	out := make([]SCPDataStorageGroupSharingResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSCPDataStorageGroupSharingResponse(&in[i]))
	}
	return out
}

// SCPDataStorageUserSharingResponse is the read model for a user share.
type SCPDataStorageUserSharingResponse struct {
	SharingID     string                       `json:"dataStorageUserSharingId"`
	DataStorageID *string                      `json:"dataStorageId"`
	UserID        *string                      `json:"userId"`
	Permission    *model.DataStoragePermission `json:"permission"`
}

func ToSCPDataStorageUserSharingResponse(s *model.SCPDataStorageUserSharing) SCPDataStorageUserSharingResponse {
	return SCPDataStorageUserSharingResponse{
		SharingID:     s.ID,
		DataStorageID: s.DataStorageID,
		UserID:        s.UserID,
		Permission:    s.Permission,
	}
}

func ToSCPDataStorageUserSharingResponses(in []model.SCPDataStorageUserSharing) []SCPDataStorageUserSharingResponse {
	out := make([]SCPDataStorageUserSharingResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSCPDataStorageUserSharingResponse(&in[i]))
	}
	return out
}

func validateStoragePermission(c *httpx.Constraints, p *model.DataStoragePermission) {
	if p != nil && !p.Valid() {
		c.Add("permission", "Permission must be one of READ, WRITE")
	}
}

func storageGrantOrRead(p *model.DataStoragePermission) model.DataStoragePermission {
	if p == nil {
		return model.DataStoragePermissionRead
	}
	return *p
}
