package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// SSHUserCredential binds a username to a registered SSH key.
//
// The link to SSHKey is many-to-one rather than one-to-one on purpose: the same key
// can back credentials for different usernames, and deleting a credential must not
// take the key with it. The reverse protection — refusing to delete a key that is
// still referenced — is enforced in the service layer as a 409, not by the schema.
//
// Java: org.apache.airavata.credentials.model.SSHUserCredential
type SSHUserCredential struct {
	ID string `gorm:"column:ssh_credential_id;primaryKey;type:varchar(36)" json:"sshCredentialId"`

	Username string `gorm:"column:username;type:varchar(255);not null" json:"username"`

	SSHKeyID *string `gorm:"column:ssh_key_id;type:varchar(36);index" json:"sshKeyId,omitempty"`
	SSHKey   *SSHKey `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"sshKey,omitempty"`
}

// TableName returns the table backing SSHUserCredential.
func (SSHUserCredential) TableName() string { return "ssh_user_credentials" }

// BeforeCreate assigns a UUID when none was supplied.
func (c *SSHUserCredential) BeforeCreate(*gorm.DB) error {
	if c.ID == "" {
		c.ID = uuid.NewString()
	}
	return nil
}
