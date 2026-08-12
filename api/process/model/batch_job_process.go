// Package process holds the batch job process entities.
package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	applicationmodel "github.com/apache/airavata/api/application/model"
	iammodel "github.com/apache/airavata/api/iam/model"
)

// BatchJobProcess is one run of a BatchDeployment.
//
// The attached BatchJobConfig is taken from the create request rather than copied
// from the deployment's default, so a caller can request different resources for a
// particular run. It is a snapshot of what this process actually asked for, owned by
// the process and deleted with it.
//
// Java: org.apache.airavata.process.model.BatchJobProcess
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
