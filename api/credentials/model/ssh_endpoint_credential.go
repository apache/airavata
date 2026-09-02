package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	iammodel "github.com/apache/airavata/api/iam/model"
)

// SSHEndpoint is a host reachable over SSH.
//
// It was split out of Cluster, which used to carry a bare host name. Separating it
// lets several clusters share one login host, and lets a credential be held against
// the host itself rather than against one cluster's view of it.
type SSHEndpoint struct {
	ID       string `gorm:"column:ssh_endpoint_id;primaryKey;type:varchar(36)" json:"sshEndpointId"`
	Name     string `gorm:"column:name;type:varchar(255);not null" json:"name"`
	HostName string `gorm:"column:host_name;type:varchar(255);not null" json:"hostName"`
	Port     int    `gorm:"column:port;not null" json:"port"`
}

// TableName returns the table backing SSHEndpoint.
func (SSHEndpoint) TableName() string { return "ssh_endpoints" }

// BeforeCreate assigns a UUID when none was supplied.
func (e *SSHEndpoint) BeforeCreate(*gorm.DB) error {
	if e.ID == "" {
		e.ID = uuid.NewString()
	}
	return nil
}

// SSHEndpointCredential binds a user's SSH credential to an SSH endpoint: it is what
// lets a given user act on a given host.
type SSHEndpointCredential struct {
	ID string `gorm:"column:ssh_endpoint_credential_id;primaryKey;type:varchar(36)" json:"sshEndpointCredentialId"`

	SSHEndpointID *string      `gorm:"column:ssh_endpoint_id;type:varchar(36);index" json:"sshEndpointId,omitempty"`
	SSHEndpoint   *SSHEndpoint `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"sshEndpoint,omitempty"`

	SSHCredentialID *string            `gorm:"column:ssh_credential_id;type:varchar(36);index" json:"sshCredentialId,omitempty"`
	SSHCredential   *SSHUserCredential `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

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
