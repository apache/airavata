// Package process holds the batch job process entities.
package model

import (
	applicationmodel "github.com/apache/airavata/api/application/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// BatchJobProcess is one run of a BatchDeployment.
type BatchJobProcess struct {
	ID string `gorm:"column:batch_process_id;primaryKey;type:varchar(36)" json:"batchProcessId"`

	ProcessID *string  `gorm:"column:parent_process_id;type:varchar(36);index" json:"parentProcessId,omitempty"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"parentProcess,omitempty"`

	DeploymentID *string                           `gorm:"column:deployment_id;type:varchar(36);index" json:"deploymentId,omitempty"`
	Deployment   *applicationmodel.BatchDeployment `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// Owned one-to-one, as on BatchDeployment: unique foreign key on this side, with
	// the orphan removed by AfterDelete since the database cannot cascade outward.
	BatchJobConfigID string                           `gorm:"column:batch_job_config_id;type:varchar(36);not null;uniqueIndex" json:"batchJobConfigId"`
	BatchJobConfig   *applicationmodel.BatchJobConfig `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"batchJobConfig,omitempty"`

	JobID   *string `gorm:"column:job_id;type:varchar(255)" json:"jobId,omitempty"`
	JobName *string `gorm:"column:job_name;type:varchar(255)" json:"jobName,omitempty"`
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
func (BatchJobProcess) TableName() string { return "batch_processes" }

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
