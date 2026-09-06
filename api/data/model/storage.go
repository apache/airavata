// Package data holds the SCP data-registration entities.
package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	cred "github.com/apache/airavata/api/credentials/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

type DataStoragePermission string

const (
	DataStoragePermissionRead  DataStoragePermission = "READ"
	DataStoragePermissionWrite DataStoragePermission = "WRITE"
)

type DataStorageType string

const (
	DataStorageTypeSCP DataStorageType = "SCP"
)

func (t DataStorageType) Valid() bool {
	switch t {
	case DataStorageTypeSCP:
		return true
	}
	return false
}

func (p DataStoragePermission) Valid() bool {
	switch p {
	case DataStoragePermissionRead, DataStoragePermissionWrite:
		return true
	}
	return false
}

// SCPDataStorage is a host and account data products can be staged through.
//
// It names the host itself — the name and port to reach over SSH — and the account it
// is reached as: a login user and the key presented for it. The host is spelled out
// here rather than pointed at an SSHEndpoint, and the account rather than an
// SSHUserCredential, for the same reason a SlurmClusterConfig does: those catalogue
// entries are administrative, so registering a storage would otherwise mean an admin
// first entering the host, while a storage is self-service and belongs to whoever
// declares it. Only the key stays a reference, because the private material has to
// live somewhere it is never read back.
//
// It belongs to whoever registered it, and everyone else reaches it through the
// sharing rows below. Ownership is not transferable through the API: products are
// registered against a storage by id, so handing one over would silently hand over
// the place every dataset on it lives.
type SCPDataStorage struct {
	ID   string  `gorm:"column:data_id;primaryKey;type:varchar(36)" json:"dataId"`
	Name *string `gorm:"column:data_name;type:varchar(255)" json:"dataName,omitempty"`

	HostName *string `gorm:"column:host_name;type:varchar(255)" json:"hostName,omitempty"`
	Port     *int    `gorm:"column:port;type:int" json:"port,omitempty"`

	LoginUser *string `gorm:"column:login_user;type:varchar(255)" json:"loginUser,omitempty"`

	SSHKeyID *string      `gorm:"column:ssh_key_id;type:varchar(36);index" json:"sshKeyId,omitempty"`
	SSHKey   *cred.SSHKey `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"sshKey,omitempty"`

	// OwnerID is named for its role because ownership, not mere reference, is what the
	// authorisation checks read. RESTRICT: a user who still owns storages cannot be
	// deleted out from under them.
	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"ownerId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
}

// TableName returns the table backing SCPDataStorage.
func (SCPDataStorage) TableName() string { return "scp_data_storages" }

// OwnedBy reports whether userID owns this storage. A storage with no owner is owned
// by nobody, so it must not match the empty principal name.
func (s *SCPDataStorage) OwnedBy(userID string) bool {
	return s.OwnerID != nil && *s.OwnerID == userID
}

// BeforeCreate assigns a UUID when none was supplied.
func (s *SCPDataStorage) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

// SCPDataStorageGroupSharing grants a group access to one storage.
type SCPDataStorageGroupSharing struct {
	ID string `gorm:"column:data_storage_group_sharing_id;primaryKey;type:varchar(36)" json:"dataStorageGroupSharingId"`

	DataStorageID *string         `gorm:"column:data_storage_id;type:varchar(36);index" json:"dataStorageId,omitempty"`
	DataStorage   *SCPDataStorage `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	GroupID *string         `gorm:"column:group_id;type:varchar(36);index" json:"groupId,omitempty"`
	Group   *iammodel.Group `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission *DataStoragePermission `gorm:"column:permission;type:varchar(32)" json:"permission,omitempty"`
}

// TableName returns the table backing SCPDataStorageGroupSharing.
func (SCPDataStorageGroupSharing) TableName() string { return "scp_data_storage_group_sharings" }

// BeforeCreate assigns a UUID when none was supplied.
func (s *SCPDataStorageGroupSharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

// SCPDataStorageUserSharing grants one named user access to one storage.
type SCPDataStorageUserSharing struct {
	ID string `gorm:"column:data_storage_user_sharing_id;primaryKey;type:varchar(36)" json:"dataStorageUserSharingId"`

	DataStorageID *string         `gorm:"column:data_storage_id;type:varchar(36);index" json:"dataStorageId,omitempty"`
	DataStorage   *SCPDataStorage `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	UserID *string        `gorm:"column:user_id;type:varchar(255);index" json:"userId,omitempty"`
	User   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission *DataStoragePermission `gorm:"column:permission;type:varchar(32)" json:"permission,omitempty"`
}

// TableName returns the table backing SCPDataStorageUserSharing.
func (SCPDataStorageUserSharing) TableName() string { return "scp_data_storage_user_sharings" }

// BeforeCreate assigns a UUID when none was supplied.
func (s *SCPDataStorageUserSharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}
