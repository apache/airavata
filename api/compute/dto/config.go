package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/compute/model"
	creddto "github.com/apache/airavata/api/credentials/dto"
)

// SlurmClusterConfigRequest is the create/update payload for a cluster login config.
//
// There is no owner field: ownership comes from the access token and is immutable, so
// a config can neither be registered on someone else's behalf nor handed over by
// editing it.
type SlurmClusterConfigRequest struct {
	Name        *string `json:"name"`
	Description *string `json:"description"`

	SlurmClusterID string `json:"slurmClusterId"`

	LoginUser string `json:"loginUser"`
	WorkRoot  string `json:"workRoot"`

	SSHKeyID string `json:"sshKeyId"`
}

// Validate implements httpx.Validator.
func (r *SlurmClusterConfigRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("slurmClusterId", "Slurm cluster id cannot be blank", r.SlurmClusterID)
	c.NotBlank("loginUser", "Login user cannot be blank", r.LoginUser)
	c.NotBlank("workRoot", "Work root cannot be blank", r.WorkRoot)
	c.NotBlank("sshKeyId", "SSH key id cannot be blank", r.SSHKeyID)
	return c.Fields()
}

// ApplySlurmClusterConfigRequest copies the mutable fields of a request onto an entity.
// The cluster and the key are resolved by the service, which is what turns an unknown
// id into a 404 rather than a dangling reference.
func ApplySlurmClusterConfigRequest(dst *model.SlurmClusterConfig, src *SlurmClusterConfigRequest) {
	dst.Name = src.Name
	dst.Description = src.Description
	dst.LoginUser = src.LoginUser
	dst.WorkRoot = src.WorkRoot
}

// SlurmClusterConfigResponse is the read model for a cluster login config.
//
// The cluster is inlined because a config is only meaningful together with the machine
// it logs in to. The key is inlined as its safe summary — name and public key, never
// the private material, which the credential response type has no field for at all.
type SlurmClusterConfigResponse struct {
	SlurmClusterConfigID string  `json:"slurmClusterConfigId"`
	Name                 *string `json:"name"`
	Description          *string `json:"description"`
	OwnerID              *string `json:"ownerId"`

	SlurmClusterID string                `json:"slurmClusterId"`
	SlurmCluster   *SlurmClusterResponse `json:"slurmCluster"`

	LoginUser string `json:"loginUser"`
	WorkRoot  string `json:"workRoot"`

	SSHKeyID *string                 `json:"sshKeyId"`
	SSHKey   *creddto.SSHKeyResponse `json:"sshKey"`

	Permission *string `json:"permission,omitempty"`
}

func ToSlurmClusterConfigResponse(c *model.SlurmClusterConfig) SlurmClusterConfigResponse {
	out := SlurmClusterConfigResponse{
		SlurmClusterConfigID: c.ID,
		Name:                 c.Name,
		Description:          c.Description,
		OwnerID:              c.OwnerID,
		SlurmClusterID:       c.SlurmClusterID,
		LoginUser:            c.LoginUser,
		WorkRoot:             c.WorkRoot,
		SSHKeyID:             c.SSHKeyID,
	}
	if c.SlurmCluster != nil {
		cluster := ToSlurmClusterResponse(c.SlurmCluster)
		out.SlurmCluster = &cluster
	}
	if c.SSHKey != nil {
		key := creddto.ToSSHKeyResponse(c.SSHKey)
		out.SSHKey = &key
	}
	return out
}

// ToSlurmClusterConfigResponseWith is ToSlurmClusterConfigResponse with the caller's
// effective permission attached.
func ToSlurmClusterConfigResponseWith(c *model.SlurmClusterConfig, permission string) SlurmClusterConfigResponse {
	out := ToSlurmClusterConfigResponse(c)
	out.Permission = &permission
	return out
}

func ToSlurmClusterConfigResponses(in []model.SlurmClusterConfig) []SlurmClusterConfigResponse {
	out := make([]SlurmClusterConfigResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSlurmClusterConfigResponse(&in[i]))
	}
	return out
}

// SlurmClusterConfigGroupSharingRequest shares a config with a group.
type SlurmClusterConfigGroupSharingRequest struct {
	GroupID    string                   `json:"groupId"`
	Permission *model.ClusterPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *SlurmClusterConfigGroupSharingRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("groupId", "Group id cannot be blank", r.GroupID)
	validateClusterPermission(&c, r.Permission)
	return c.Fields()
}

// Grant returns the permission to store, defaulting to READ.
func (r *SlurmClusterConfigGroupSharingRequest) Grant() model.ClusterPermission {
	return clusterGrantOrRead(r.Permission)
}

// SlurmClusterConfigUserSharingRequest shares a config with one user.
type SlurmClusterConfigUserSharingRequest struct {
	UserID     string                   `json:"userId"`
	Permission *model.ClusterPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *SlurmClusterConfigUserSharingRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("userId", "User id cannot be blank", r.UserID)
	validateClusterPermission(&c, r.Permission)
	return c.Fields()
}

// Grant returns the permission to store, defaulting to READ.
func (r *SlurmClusterConfigUserSharingRequest) Grant() model.ClusterPermission {
	return clusterGrantOrRead(r.Permission)
}

// SlurmClusterConfigSharingUpdate changes what an existing config share grants.
type SlurmClusterConfigSharingUpdate struct {
	Permission *model.ClusterPermission `json:"permission"`
}

// Validate implements httpx.Validator.
func (r *SlurmClusterConfigSharingUpdate) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotNil("permission", "Permission cannot be null", r.Permission)
	validateClusterPermission(&c, r.Permission)
	return c.Fields()
}

// SlurmClusterConfigGroupSharingResponse is the read model for a group share.
type SlurmClusterConfigGroupSharingResponse struct {
	SharingID            string                  `json:"slurmClusterConfigGroupSharingId"`
	SlurmClusterConfigID string                  `json:"slurmClusterConfigId"`
	GroupID              string                  `json:"groupId"`
	Permission           model.ClusterPermission `json:"permission"`
}

func ToSlurmClusterConfigGroupSharingResponse(s *model.SlurmClusterConfigGroupSharing) SlurmClusterConfigGroupSharingResponse {
	return SlurmClusterConfigGroupSharingResponse{
		SharingID:            s.ID,
		SlurmClusterConfigID: s.SlurmClusterConfigID,
		GroupID:              s.GroupID,
		Permission:           s.Permission,
	}
}

func ToSlurmClusterConfigGroupSharingResponses(in []model.SlurmClusterConfigGroupSharing) []SlurmClusterConfigGroupSharingResponse {
	out := make([]SlurmClusterConfigGroupSharingResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSlurmClusterConfigGroupSharingResponse(&in[i]))
	}
	return out
}

// SlurmClusterConfigUserSharingResponse is the read model for a user share.
type SlurmClusterConfigUserSharingResponse struct {
	SharingID            string                  `json:"slurmClusterConfigUserSharingId"`
	SlurmClusterConfigID string                  `json:"slurmClusterConfigId"`
	UserID               string                  `json:"userId"`
	Permission           model.ClusterPermission `json:"permission"`
}

func ToSlurmClusterConfigUserSharingResponse(s *model.SlurmClusterConfigUserSharing) SlurmClusterConfigUserSharingResponse {
	return SlurmClusterConfigUserSharingResponse{
		SharingID:            s.ID,
		SlurmClusterConfigID: s.SlurmClusterConfigID,
		UserID:               s.UserID,
		Permission:           s.Permission,
	}
}

func ToSlurmClusterConfigUserSharingResponses(in []model.SlurmClusterConfigUserSharing) []SlurmClusterConfigUserSharingResponse {
	out := make([]SlurmClusterConfigUserSharingResponse, 0, len(in))
	for i := range in {
		out = append(out, ToSlurmClusterConfigUserSharingResponse(&in[i]))
	}
	return out
}

func validateClusterPermission(c *httpx.Constraints, p *model.ClusterPermission) {
	if p != nil && !p.Valid() {
		c.Add("permission", "Permission must be one of READ, WRITE")
	}
}

func clusterGrantOrRead(p *model.ClusterPermission) model.ClusterPermission {
	if p == nil {
		return model.ClusterPermissionRead
	}
	return *p
}
