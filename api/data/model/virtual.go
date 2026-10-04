/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	iammodel "github.com/apache/airavata/api/iam/model"
)

type VirtualDataFile struct {
	ID                string       `gorm:"column:virtual_data_file_id;primaryKey;type:varchar(36)" json:"virtualDataFileId"`
	FileName          *string      `gorm:"column:file_name;type:varchar(255);uniqueIndex:uk_virtual_data_file_name" json:"fileName,omitempty"`
	ParentDirectoryID *string      `gorm:"column:parent_directory_id;type:varchar(36);index;uniqueIndex:uk_virtual_data_file_name" json:"parentDirectoryId,omitempty"`
	DataProductID     *string      `gorm:"column:data_product_id;type:varchar(36);index" json:"dataProductId,omitempty"`
	DataProduct       *DataProduct `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"dataProduct,omitempty"`
	CreatedAt         int64        `gorm:"column:created_at;not null" json:"createdAt"`
}

func (VirtualDataFile) TableName() string { return "virtual_data_files" }

func (f *VirtualDataFile) BeforeCreate(*gorm.DB) error {
	if f.ID == "" {
		f.ID = uuid.NewString()
	}
	return nil
}

func (f *VirtualDataFile) BeforeSave(*gorm.DB) error {
	if f.DataProductID == nil || *f.DataProductID == "" {
		return gorm.ErrInvalidValue
	}
	if f.ParentDirectoryID == nil || *f.ParentDirectoryID == "" {
		return gorm.ErrInvalidValue
	}
	return nil
}

// VirtualDirectory holds different types of virtual directories and virtual files.
// If there is a DataProductID, the directory represents a registered product and should not have children.
type VirtualDataDirectory struct {
	ID string `gorm:"column:virtual_data_directory_id;primaryKey;type:varchar(36)" json:"virtualDataDirectoryId"`

	// Unique among the entries of one parent, for the reason given on FileName.
	DirectoryName *string `gorm:"column:directory_name;type:varchar(255);uniqueIndex:uk_virtual_data_directory_name" json:"directoryName,omitempty"`

	// A nil parent is a root: the top of one virtual dataset.
	ParentDirectoryID *string `gorm:"column:parent_directory_id;type:varchar(36);index;uniqueIndex:uk_virtual_data_directory_name" json:"parentDirectoryId,omitempty"`

	Files       []VirtualDataFile      `gorm:"foreignKey:ParentDirectoryID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"virtualDataFiles,omitempty"`
	Directories []VirtualDataDirectory `gorm:"foreignKey:ParentDirectoryID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"virtualDataDirectories,omitempty"`

	// Set only on a directory that stands for a registered directory product. RESTRICT
	// for the same reason as on a file: a cited product cannot be deleted.
	DataProductID *string      `gorm:"column:data_product_id;type:varchar(36);index" json:"dataProductId,omitempty"`
	DataProduct   *DataProduct `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"dataProduct,omitempty"`

	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"ownerId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	CreatedAt int64 `gorm:"column:created_at;not null" json:"createdAt"`
}

func (VirtualDataDirectory) TableName() string { return "virtual_data_directories" }

func (d *VirtualDataDirectory) BeforeCreate(*gorm.DB) error {
	if d.ID == "" {
		d.ID = uuid.NewString()
	}
	return nil
}

func (d *VirtualDataDirectory) BeforeSave(*gorm.DB) error {
	if d.ParentDirectoryID != nil && *d.ParentDirectoryID == d.ID {
		return gorm.ErrInvalidValue
	}

	if d.DataProductID != nil && *d.DataProductID != "" && (len(d.Directories) > 0 || len(d.Files) > 0) {
		return gorm.ErrInvalidValue
	}
	return nil
}

type VirtualDataDirectoryGroupSharing struct {
	ID string `gorm:"column:virtual_data_directory_group_sharing_id;primaryKey;type:varchar(36)" json:"virtualDataDirectoryGroupSharingId"`

	VirtualDataDirectoryID *string               `gorm:"column:virtual_data_directory_id;type:varchar(36);index;uniqueIndex:uk_virtual_data_directory_group_sharing" json:"virtualDataDirectoryId,omitempty"`
	VirtualDataDirectory   *VirtualDataDirectory `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"-"`

	GroupID *string         `gorm:"column:group_id;type:varchar(36);index;uniqueIndex:uk_virtual_data_directory_group_sharing" json:"groupId,omitempty"`
	Group   *iammodel.Group `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission *DataProductPermission `gorm:"column:permission;type:varchar(32)" json:"permission,omitempty"`
}

func (VirtualDataDirectoryGroupSharing) TableName() string {
	return "virtual_data_directory_group_sharings"
}

func (s *VirtualDataDirectoryGroupSharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

func (s *VirtualDataDirectoryGroupSharing) BeforeSave(*gorm.DB) error {
	if s.Permission != nil && !s.Permission.Valid() {
		return gorm.ErrInvalidValue
	}
	return nil
}

type VirtualDataDirectoryUserSharing struct {
	ID string `gorm:"column:virtual_data_directory_user_sharing_id;primaryKey;type:varchar(36)" json:"virtualDataDirectoryUserSharingId"`

	VirtualDataDirectoryID *string               `gorm:"column:virtual_data_directory_id;type:varchar(36);index;uniqueIndex:uk_virtual_data_directory_user_sharing" json:"virtualDataDirectoryId,omitempty"`
	VirtualDataDirectory   *VirtualDataDirectory `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"-"`

	UserID *string        `gorm:"column:user_id;type:varchar(255);index;uniqueIndex:uk_virtual_data_directory_user_sharing" json:"userId,omitempty"`
	User   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission *DataProductPermission `gorm:"column:permission;type:varchar(32)" json:"permission,omitempty"`
}

func (VirtualDataDirectoryUserSharing) TableName() string {
	return "virtual_data_directory_user_sharings"
}

func (s *VirtualDataDirectoryUserSharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

func (s *VirtualDataDirectoryUserSharing) BeforeSave(*gorm.DB) error {
	if s.Permission != nil && !s.Permission.Valid() {
		return gorm.ErrInvalidValue
	}
	return nil
}
