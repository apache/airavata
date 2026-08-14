package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	iammodel "github.com/apache/airavata/api/iam/model"
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

// ProvisionStatus is the lifecycle state of a registered dataset.
type ProvisionStatus string

const (
	ProvisionStatusRegistered     ProvisionStatus = "REGISTERD"
	ProvisionStatusProvisioning   ProvisionStatus = "PROVISIONING"
	ProvisionStatusProvisioned    ProvisionStatus = "PROVISIONED"
	ProvisionStatusDeprovisioning ProvisionStatus = "DEPROVISIONING"
	ProvisionStatusDeprovisioned  ProvisionStatus = "DEPROVISIONED"
	ProvisionStatusFailed         ProvisionStatus = "FAILED"
)

// Valid reports whether s is a recognised ProvisionStatus.
func (s ProvisionStatus) Valid() bool {
	switch s {
	case ProvisionStatusRegistered, ProvisionStatusProvisioning, ProvisionStatusProvisioned,
		ProvisionStatusDeprovisioning, ProvisionStatusDeprovisioned, ProvisionStatusFailed:
		return true
	}
	return false
}

type DataProduct struct {
	ID string `gorm:"column:data_id;primaryKey;type:varchar(36)" json:"dataId"`

	DataName        *string `gorm:"column:data_name;type:varchar(255)" json:"dataName,omitempty"`
	DataDescription *string `gorm:"column:data_description;type:varchar(2048)" json:"dataDescription,omitempty"`

	IsFile bool `gorm:"column:is_file;not null" json:"isFile"`

	Path *string `gorm:"column:path;type:varchar(2048)" json:"path,omitempty"`

	ProvisionStatus *ProvisionStatus `gorm:"column:provision_status;type:varchar(32)" json:"provisionStatus,omitempty"`

	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"ownerId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	DataStorageID   *string         `gorm:"column:data_storage_id;type:varchar(36);index" json:"dataStorageId,omitempty"`
	DataStorageType DataStorageType `gorm:"column:data_storage_type;type:varchar(32)" json:"dataStorageType,omitempty"`

	CredentialID *string `gorm:"column:credential_id;type:varchar(36);index" json:"credentialId,omitempty"`

	CreatedAt int64 `gorm:"column:created_at;not null" json:"createdAt"`
}

// TableName returns the table backing DataProduct.
func (DataProduct) TableName() string { return "data_products" }

// BeforeCreate assigns a UUID when none was supplied.
func (p *DataProduct) BeforeCreate(*gorm.DB) error {
	if p.ID == "" {
		p.ID = uuid.NewString()
	}
	return nil
}

// OwnedBy reports whether userID owns this product. A product with no owner is owned
// by nobody, so it must not match the empty principal name.
func (p *DataProduct) OwnedBy(userID string) bool {
	return p.OwnerID != nil && *p.OwnerID == userID
}

type DataProductPermission string

const (
	DataProductPermissionRead  DataProductPermission = "READ"
	DataProductPermissionWrite DataProductPermission = "WRITE"
)

// Valid reports whether p is a recognised permission.
func (p DataProductPermission) Valid() bool {
	switch p {
	case DataProductPermissionRead, DataProductPermissionWrite:
		return true
	}
	return false
}

// DataProductGroupSharing grants a group access to one product. Every active member of
// the group holds the permission it names.
type DataProductGroupSharing struct {
	ID string `gorm:"column:data_product_group_sharing_id;primaryKey;type:varchar(36)" json:"dataProductGroupSharingId"`

	DataProductID *string      `gorm:"column:data_product_id;type:varchar(36);index" json:"dataProductId,omitempty"`
	DataProduct   *DataProduct `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	GroupID *string         `gorm:"column:group_id;type:varchar(36);index" json:"groupId,omitempty"`
	Group   *iammodel.Group `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission *DataProductPermission `gorm:"column:permission;type:varchar(32)" json:"permission,omitempty"`
}

// TableName returns the table backing DataProductGroupSharing.
func (DataProductGroupSharing) TableName() string { return "data_product_group_sharings" }

// BeforeCreate assigns a UUID when none was supplied.
func (s *DataProductGroupSharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

// DataProductUserSharing grants one named user access to one product.
type DataProductUserSharing struct {
	ID string `gorm:"column:data_product_user_sharing_id;primaryKey;type:varchar(36)" json:"dataProductUserSharingId"`

	DataProductID *string      `gorm:"column:data_product_id;type:varchar(36);index" json:"dataProductId,omitempty"`
	DataProduct   *DataProduct `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	UserID *string        `gorm:"column:user_id;type:varchar(255);index" json:"userId,omitempty"`
	User   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission *DataProductPermission `gorm:"column:permission;type:varchar(32)" json:"permission,omitempty"`
}

// TableName returns the table backing DataProductUserSharing.
func (DataProductUserSharing) TableName() string { return "data_product_user_sharings" }

// BeforeCreate assigns a UUID when none was supplied.
func (s *DataProductUserSharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}
