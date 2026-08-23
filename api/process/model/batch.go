package model

import (
	applicationmodel "github.com/apache/airavata/api/application/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// BatchJobProcess carries what a BATCH_JOB process needs beyond a Process: the
// deployment being run, the resources this run asked for, and the scheduler's
// identifiers for the job once it has one.
//
// It is not a resource of its own. A caller creates it as the batchProcess section of
// a process, reads it back nested in that process, and deletes it by deleting the
// process — which is why the row is keyed by its own id but reached only through
// parent_process_id, unique because a process has at most one of them.
type BatchJobProcess struct {
	ID string `gorm:"column:batch_process_id;primaryKey;type:varchar(36)" json:"batchProcessId"`

	// Owned by Process.BatchProcess, which declares the cascade; only the key is held
	// here. Unique because a process has at most one batch process.
	ProcessID *string `gorm:"column:parent_process_id;type:varchar(36);uniqueIndex" json:"parentProcessId,omitempty"`

	DeploymentID *string                           `gorm:"column:deployment_id;type:varchar(36);index" json:"deploymentId,omitempty"`
	Deployment   *applicationmodel.BatchDeployment `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// Owned one-to-one, as on BatchDeployment: unique foreign key on this side, with
	// the orphan removed by AfterDelete since the database cannot cascade outward.
	BatchJobConfigID string                           `gorm:"column:batch_job_config_id;type:varchar(36);not null;uniqueIndex" json:"batchJobConfigId"`
	BatchJobConfig   *applicationmodel.BatchJobConfig `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"batchJobConfig,omitempty"`

	JobID   *string `gorm:"column:job_id;type:varchar(255)" json:"jobId,omitempty"`
	JobName *string `gorm:"column:job_name;type:varchar(255)" json:"jobName,omitempty"`

	// The values this run supplies for the deployment template's declared inputs and
	// outputs. They hang off the batch process rather than off the process because the
	// declarations they name belong to the deployment, which only a BATCH_JOB has.
	InputMappings  []*TemplateInputMapping  `gorm:"foreignKey:BatchProcessID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"inputMappings,omitempty"`
	OutputMappings []*TemplateOutputMapping `gorm:"foreignKey:BatchProcessID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"outputMappings,omitempty"`
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
//
// It fires only when the row is deleted through GORM, which is why Process.BeforeDelete
// deletes it explicitly rather than letting the database cascade do it.
func (p *BatchJobProcess) AfterDelete(tx *gorm.DB) error {
	if p.BatchJobConfigID == "" {
		return nil
	}
	return tx.Where("batch_job_config_id = ?", p.BatchJobConfigID).
		Delete(&applicationmodel.BatchJobConfig{}).Error
}
