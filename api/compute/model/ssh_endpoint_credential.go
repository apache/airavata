package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	credentialsmodel "github.com/apache/airavata/api/credentials/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// SSHEndpointCredential binds a user's SSH credential to an SSH endpoint: it is what
// lets a given user act on a given host.
//
// The owner is always derived from the access token, never from the request body —
// the create DTO has no user field at all. Ownership is also immutable: updates
// re-resolve the endpoint and the SSH credential but never touch the owner.
//
// There is deliberately no uniqueness on (owner, endpoint); a user may hold several
// bindings to the same endpoint with different SSH identities.
//
// Access beyond the owner comes from the sharing tables below rather than from any
// role: SSHEndpointCredentialUserSharing names one user, SSHEndpointCredentialGroupSharing
// names a group, and each carries the permission it grants.
//
// Java: org.apache.airavata.compute.model.SSHEndpointCredentialEntity
type SSHEndpointCredential struct {
	ID string `gorm:"column:ssh_endpoint_credential_id;primaryKey;type:varchar(36)" json:"sshEndpointCredentialId"`

	SSHEndpointID *string      `gorm:"column:ssh_endpoint_id;type:varchar(36);index" json:"sshEndpointId,omitempty"`
	SSHEndpoint   *SSHEndpoint `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"sshEndpoint,omitempty"`

	SSHCredentialID *string                             `gorm:"column:ssh_credential_id;type:varchar(36);index" json:"sshCredentialId,omitempty"`
	SSHCredential   *credentialsmodel.SSHUserCredential `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// OwnerID is the Java entity's "user" association. It is named for its role
	// because ownership, not mere reference, is what the authorisation checks read.
	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"userId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
}

// TableName returns the table backing SSHEndpointCredential.
func (SSHEndpointCredential) TableName() string { return "ssh_endpoint_credentials" }

// BeforeCreate assigns a UUID when none was supplied.
func (c *SSHEndpointCredential) BeforeCreate(*gorm.DB) error {
	if c.ID == "" {
		c.ID = uuid.NewString()
	}
	return nil
}

// OwnedBy reports whether userID is this credential's owner. Read, update and delete
// are gated on this or an admin authority, rather than on a role alone.
func (c *SSHEndpointCredential) OwnedBy(userID string) bool {
	return c.OwnerID != nil && *c.OwnerID == userID
}

type SSHEndpointCredentialPermission string

const (
	SSHEndpointCredentialPermissionRead  SSHEndpointCredentialPermission = "READ"
	SSHEndpointCredentialPermissionWrite SSHEndpointCredentialPermission = "WRITE"
)

func (p SSHEndpointCredentialPermission) Valid() bool {
	switch p {
	case SSHEndpointCredentialPermissionRead, SSHEndpointCredentialPermissionWrite:
		return true
	}
	return false
}

// Allows reports whether holding p is enough to do something requiring want. WRITE
// implies READ; nothing implies WRITE.
func (p SSHEndpointCredentialPermission) Allows(want SSHEndpointCredentialPermission) bool {
	if !p.Valid() || !want.Valid() {
		return false
	}
	return p == SSHEndpointCredentialPermissionWrite || p == want
}

// SSHEndpointCredentialGroupSharing grants a group access to one credential. Every
// active member of the group holds the permission it names.
type SSHEndpointCredentialGroupSharing struct {
	ID string `gorm:"column:ssh_endpoint_credential_group_sharing_id;primaryKey;type:varchar(36)" json:"sshEndpointCredentialGroupSharingId"`

	SSHEndpointCredentialID *string                `gorm:"column:ssh_endpoint_credential_id;type:varchar(36);index" json:"sshEndpointCredentialId,omitempty"`
	SSHEndpointCredential   *SSHEndpointCredential `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	GroupID *string         `gorm:"column:group_id;type:varchar(36);index" json:"groupId,omitempty"`
	Group   *iammodel.Group `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission *SSHEndpointCredentialPermission `gorm:"column:permission;type:varchar(32)" json:"permission,omitempty"`
}

// TableName returns the table backing SSHEndpointCredentialGroupSharing.
func (SSHEndpointCredentialGroupSharing) TableName() string {
	return "ssh_endpoint_credential_group_sharings"
}

// BeforeCreate assigns a UUID when none was supplied.
func (s *SSHEndpointCredentialGroupSharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

// Grants reports whether this share confers want. A share with no permission grants
// nothing, so an unset column cannot be read as blanket access.
func (s *SSHEndpointCredentialGroupSharing) Grants(want SSHEndpointCredentialPermission) bool {
	return s.Permission != nil && s.Permission.Allows(want)
}

// SSHEndpointCredentialUserSharing grants one named user access to one credential.
type SSHEndpointCredentialUserSharing struct {
	ID string `gorm:"column:ssh_endpoint_credential_user_sharing_id;primaryKey;type:varchar(36)" json:"sshEndpointCredentialUserSharingId"`

	SSHEndpointCredentialID *string                `gorm:"column:ssh_endpoint_credential_id;type:varchar(36);index" json:"sshEndpointCredentialId,omitempty"`
	SSHEndpointCredential   *SSHEndpointCredential `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	UserID *string        `gorm:"column:user_id;type:varchar(255);index" json:"userId,omitempty"`
	User   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission *SSHEndpointCredentialPermission `gorm:"column:permission;type:varchar(32)" json:"permission,omitempty"`
}

// TableName returns the table backing SSHEndpointCredentialUserSharing.
func (SSHEndpointCredentialUserSharing) TableName() string {
	return "ssh_endpoint_credential_user_sharings"
}

// BeforeCreate assigns a UUID when none was supplied.
func (s *SSHEndpointCredentialUserSharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

// Grants reports whether this share confers want.
func (s *SSHEndpointCredentialUserSharing) Grants(want SSHEndpointCredentialPermission) bool {
	return s.Permission != nil && s.Permission.Allows(want)
}

// SharedWith reports whether this share names userID.
func (s *SSHEndpointCredentialUserSharing) SharedWith(userID string) bool {
	return s.UserID != nil && *s.UserID == userID
}
