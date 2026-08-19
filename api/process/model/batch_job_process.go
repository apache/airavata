// Package process holds the batch job process entities.
package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	applicationmodel "github.com/apache/airavata/api/application/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// BatchJobProcess is one run of a BatchDeployment.
type BatchJobProcess struct {
	ID string `gorm:"column:process_id;primaryKey;type:varchar(36)" json:"processId"`

	DeploymentID *string                           `gorm:"column:deployment_id;type:varchar(36);index" json:"deploymentId,omitempty"`
	Deployment   *applicationmodel.BatchDeployment `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"userId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// Owned one-to-one, as on BatchDeployment: unique foreign key on this side, with
	// the orphan removed by AfterDelete since the database cannot cascade outward.
	BatchJobConfigID string                           `gorm:"column:batch_job_config_id;type:varchar(36);not null;uniqueIndex" json:"batchJobConfigId"`
	BatchJobConfig   *applicationmodel.BatchJobConfig `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"batchJobConfig,omitempty"`

	// The last status recorded for this process, if any.
	//
	// It carries no foreign key, deliberately. A status already points at its process,
	// and a constraint in this direction as well would make the two tables mutually
	// dependent — a cycle PostgreSQL cannot create, since it validates a referenced
	// table exists when the referencing table is declared. This column is a cache of
	// the newest status row, maintained by BatchJobProcessStatus.AfterCreate.
	LastStatusID *string `gorm:"column:last_status_id;type:varchar(36);index" json:"lastStatusId,omitempty"`
}

// TableName returns the table backing BatchJobProcess.
func (BatchJobProcess) TableName() string { return "batch_job_processes" }

// BeforeCreate assigns a UUID when none was supplied.
func (p *BatchJobProcess) BeforeCreate(*gorm.DB) error {
	if p.ID == "" {
		p.ID = uuid.NewString()
	}
	return nil
}

// BeforeDelete removes every recorded status ahead of the process itself.
//
// The statuses have to go first: their own ProcessID foreign key is RESTRICT, so the
// database would refuse to delete a process any status still points at. LastStatusID
// needs no clearing — it holds no constraint — and the row carrying it is about to be
// deleted in this same transaction.
func (p *BatchJobProcess) BeforeDelete(tx *gorm.DB) error {
	return tx.Where("process_id = ?", p.ID).Delete(&BatchJobProcessStatus{}).Error
}

// AfterDelete removes the owned BatchJobConfig, standing in for JPA's orphanRemoval.
func (p *BatchJobProcess) AfterDelete(tx *gorm.DB) error {
	if p.BatchJobConfigID == "" {
		return nil
	}
	return tx.Where("batch_job_config_id = ?", p.BatchJobConfigID).
		Delete(&applicationmodel.BatchJobConfig{}).Error
}

// OwnedBy reports whether userID launched this process.
func (p *BatchJobProcess) OwnedBy(userID string) bool {
	return p.OwnerID != nil && *p.OwnerID == userID
}
