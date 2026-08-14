// Package data holds the SCP data-registration entities.
package model

import (
	computemodel "github.com/apache/airavata/api/compute/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

type DataStoragePermission string

const (
	DataStoragePermissionRead  DataStoragePermission = "READ"
	DataStoragePermissionWrite DataStoragePermission = "WRITE"
)

func (p DataStoragePermission) Valid() bool {
	switch p {
	case DataStoragePermissionRead, DataStoragePermissionWrite:
		return true
	}
	return false
}

type SCPDataStorage struct {
	ID   string  `gorm:"column:data_id;primaryKey;type:varchar(36)" json:"dataId"`
	Name *string `gorm:"column:data_name;type:varchar(255)" json:"dataName,omitempty"`

	SSHEndpointID *string                   `gorm:"column:ssh_endpoint_id;type:varchar(36);index" json:"sshEndpointId,omitempty"`
	SSHEndpoint   *computemodel.SSHEndpoint `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"sshEndpoint,omitempty"`
}

type SCPDataStorageGroupSharing struct {
	ID string `gorm:"column:data_storage_group_sharing_id;primaryKey;type:varchar(36)" json:"dataStorageGroupSharingId"`

	DataStorageID *string         `gorm:"column:data_storage_id;type:varchar(36);index" json:"dataStorageId,omitempty"`
	DataStorage   *SCPDataStorage `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	GroupID *string         `gorm:"column:group_id;type:varchar(36);index" json:"groupId,omitempty"`
	Group   *iammodel.Group `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission *DataStoragePermission `gorm:"column:permission;type:varchar(32)" json:"permission,omitempty"`
}

type SCPDataStorageUserSharing struct {
	ID string `gorm:"column:data_storage_user_sharing_id;primaryKey;type:varchar(36)" json:"dataStorageUserSharingId"`

	DataStorageID *string         `gorm:"column:data_storage_id;type:varchar(36);index" json:"dataStorageId,omitempty"`
	DataStorage   *SCPDataStorage `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	UserID *string        `gorm:"column:user_id;type:varchar(255);index" json:"userId,omitempty"`
	User   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission *DataStoragePermission `gorm:"column:permission;type:varchar(32)" json:"permission,omitempty"`
}
