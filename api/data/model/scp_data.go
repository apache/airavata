// Package data holds the SCP data-registration entities.
package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	computemodel "github.com/apache/airavata/api/compute/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// ProvisionStatus is the lifecycle state of a registered dataset.
//
// Java stored this as an ORDINAL int; it is stored by name here for the same reason
// as the template type enums. REGISTERD is misspelled in the Java source and is kept
// verbatim — it is the value already serialised over the API, so correcting the
// spelling here would be a breaking change rather than a cleanup.
//
// Java: org.apache.airavata.data.model.DataProvisionStatus
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

// SCPData is a dataset reachable over SCP through a cluster credential.
//
// ProvisionStatus is owned by the service, not the client: creation forces it to
// REGISTERD and the update mapper ignores the field entirely. No provisioning
// workflow exists yet, so in practice nothing advances it past REGISTERD.
//
// Java: org.apache.airavata.data.model.SCPDataEntity
type SCPData struct {
	ID string `gorm:"column:data_id;primaryKey;type:varchar(36)" json:"dataId"`

	DataName        *string `gorm:"column:data_name;type:varchar(255)" json:"dataName,omitempty"`
	DataDescription *string `gorm:"column:data_description;type:varchar(2048)" json:"dataDescription,omitempty"`

	// IsFile is a string, not a bool, in the Java model, and the request DTO validates
	// it as a non-blank string. Kept as-is so the API contract is unchanged; tightening
	// it to a bool should be a deliberate, separately-versioned change.
	IsFile *string `gorm:"column:is_file;type:varchar(16)" json:"isFile,omitempty"`

	Path *string `gorm:"column:path;type:varchar(2048)" json:"path,omitempty"`

	ClusterCredentialID *string                         `gorm:"column:slurm_cluster_credential_id;type:varchar(36);index" json:"slurmClusterCredentialId,omitempty"`
	ClusterCredential   *computemodel.ClusterCredential `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	ProvisionStatus *ProvisionStatus `gorm:"column:provision_status;type:varchar(32)" json:"provisionStatus,omitempty"`

	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"ownerId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
}

// TableName returns the table backing SCPData.
func (SCPData) TableName() string { return "scp_data" }

// BeforeCreate assigns a UUID when none was supplied.
func (d *SCPData) BeforeCreate(*gorm.DB) error {
	if d.ID == "" {
		d.ID = uuid.NewString()
	}
	return nil
}

// OwnedBy reports whether userID owns this dataset. Read, update and delete are gated
// on this or an admin authority.
func (d *SCPData) OwnedBy(userID string) bool {
	return d.OwnerID != nil && *d.OwnerID == userID
}
