package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	credentialsmodel "github.com/apache/airavata/api/credentials/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// ClusterCredential binds a user's SSH credential to a cluster: it is what lets a
// given user act on a given cluster.
//
// The owner is always derived from the access token, never from the request body —
// the create DTO has no user field at all. Ownership is also immutable: updates
// re-resolve the cluster and the SSH credential but never touch the owner.
//
// There is deliberately no uniqueness on (owner, cluster); a user may hold several
// bindings to the same cluster with different SSH identities.
//
// Java: org.apache.airavata.compute.model.ClusterCredentialEntity
type ClusterCredential struct {
	ID string `gorm:"column:cluster_credential_id;primaryKey;type:varchar(36)" json:"clusterCredentialId"`

	ClusterID *string  `gorm:"column:cluster_id;type:varchar(36);index" json:"clusterId,omitempty"`
	Cluster   *Cluster `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	SSHCredentialID *string                             `gorm:"column:ssh_credential_id;type:varchar(36);index" json:"sshCredentialId,omitempty"`
	SSHCredential   *credentialsmodel.SSHUserCredential `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// OwnerID is the Java entity's "user" association. It is named for its role
	// because ownership, not mere reference, is what the authorisation checks read.
	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"userId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
}

// TableName returns the table backing ClusterCredential.
func (ClusterCredential) TableName() string { return "cluster_credentials" }

// BeforeCreate assigns a UUID when none was supplied.
func (c *ClusterCredential) BeforeCreate(*gorm.DB) error {
	if c.ID == "" {
		c.ID = uuid.NewString()
	}
	return nil
}

// OwnedBy reports whether userID is this credential's owner. Read, update and delete
// are gated on this or an admin authority, rather than on a role alone.
func (c *ClusterCredential) OwnedBy(userID string) bool {
	return c.OwnerID != nil && *c.OwnerID == userID
}
