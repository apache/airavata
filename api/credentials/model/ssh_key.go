// Package model holds the SSH key, SSH user-credential, SSH endpoint and
// endpoint-credential entities.
package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// SSHKey is a registered SSH keypair.
//
// PrivateKey and Passphrase are secrets: they are stored here but deliberately have
// no field on the read-side response DTO, so they cannot leak through a GET. Update
// paths must treat a blank incoming value as "unchanged" rather than "erase" — see
// ptr.NonBlank.
//
// Java: org.apache.airavata.credentials.model.SSHKeyEntity
type SSHKey struct {
	ID string `gorm:"column:ssh_key_id;primaryKey;type:varchar(36)" json:"sshKeyId"`

	SSHKeyName string `gorm:"column:ssh_key_name;type:varchar(255);not null" json:"sshKeyName"`

	// @Lob in Java: keys are far longer than a default varchar.
	PublicKey  string `gorm:"column:public_key;type:text;not null" json:"publicKey"`
	PrivateKey string `gorm:"column:private_key;type:text;not null" json:"-"`

	Passphrase *string `gorm:"column:passphrase;type:varchar(255)" json:"-"`
}

// TableName returns the table backing SSHKey.
func (SSHKey) TableName() string { return "ssh_keys" }

// BeforeCreate assigns a UUID when none was supplied, replacing Hibernate's
// @GeneratedValue(strategy = GenerationType.UUID).
func (k *SSHKey) BeforeCreate(*gorm.DB) error {
	if k.ID == "" {
		k.ID = uuid.NewString()
	}
	return nil
}
