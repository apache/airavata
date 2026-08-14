package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"

	computemodel "github.com/apache/airavata/api/compute/model"
)

// BatchJobConfig is a batch-scheduler resource request.
//
// It is owned outright by whatever references it — a BatchDeployment as its default,
// or a BatchJobProcess as the snapshot of what that run actually asked for. It is
// created, replaced and deleted with its owner and has no life of its own, so it is
// never shared between two owners.
//
// Java: org.apache.airavata.application.model.deployment.BatchJobConfigs
type BatchJobConfig struct {
	ID string `gorm:"column:batch_job_config_id;primaryKey;type:varchar(36)" json:"batchJobConfigId"`

	// Slurm resource allocation parameters. All optional: an unset value means "let
	// the scheduler decide", which is distinct from an explicit zero.
	CPUs          *int32  `gorm:"column:cpus" json:"cpus,omitempty"`
	Mem           *string `gorm:"column:mem;type:varchar(64)" json:"mem,omitempty"`
	MemPerCPU     *string `gorm:"column:mem_per_cpu;type:varchar(64)" json:"memPerCpu,omitempty"`
	NtasksPerNode *int32  `gorm:"column:ntasks_per_node" json:"ntasksPerNode,omitempty"`
	CPUsPerTask   *int32  `gorm:"column:cpus_per_task" json:"cpusPerTask,omitempty"`
	Nodes         *int32  `gorm:"column:nodes" json:"nodes,omitempty"`
	Ntasks        *int32  `gorm:"column:ntasks" json:"ntasks,omitempty"`

	// GPU related fields.
	Gres        *string `gorm:"column:gres;type:varchar(255)" json:"gres,omitempty"`
	GPUs        *int32  `gorm:"column:gpus" json:"gpus,omitempty"`
	MemPerGPU   *string `gorm:"column:mem_per_gpu;type:varchar(64)" json:"memPerGpu,omitempty"`
	CPUsPerGPU  *string `gorm:"column:cpus_per_gpu;type:varchar(64)" json:"cpusPerGpu,omitempty"`
	GPUsPerNode *int32  `gorm:"column:gpus_per_node" json:"gpusPerNode,omitempty"`

	WallTimeMinutes int64 `gorm:"column:wall_time_minutes;not null" json:"wallTimeMinutes"`

	// Constraints is a Slurm feature constraint expression, e.g. "gpu", "highmem".
	Constraints *string `gorm:"column:constraints;type:varchar(255)" json:"constraints,omitempty"`

	Allocation string `gorm:"column:allocation;type:varchar(255);not null" json:"allocation"`
}

// TableName returns the table backing BatchJobConfig.
func (BatchJobConfig) TableName() string { return "batch_job_configs" }

// BeforeCreate assigns a UUID when none was supplied.
func (c *BatchJobConfig) BeforeCreate(*gorm.DB) error {
	if c.ID == "" {
		c.ID = uuid.NewString()
	}
	return nil
}

// BatchDeployment is a Template made runnable on a specific cluster.
//
// Java: org.apache.airavata.application.model.deployment.BatchApplicationDeploymentEntity
type BatchDeployment struct {
	ID string `gorm:"column:deployment_id;primaryKey;type:varchar(36)" json:"deploymentId"`

	// The cluster is genuinely optional: a null ClusterID is legitimate, while a
	// supplied-but-unknown id is an error the service reports as not-found.
	ClusterID *string               `gorm:"column:cluster_id;type:varchar(36);index" json:"clusterId,omitempty"`
	Cluster   *computemodel.Cluster `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	TemplateID *string   `gorm:"column:template_id;type:varchar(36);index" json:"templateId,omitempty"`
	Template   *Template `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// SlurmRunSection carries every execution command — module loads, the run command
	// itself, and cleanup. It is a Jinja template, parameterised at submission time.
	SlurmRunSection string `gorm:"column:slurm_run_section;type:longtext;not null" json:"slurmRunSection"`

	// Owned one-to-one. The foreign key lives on this side and is unique, so no two
	// deployments can share a config. Because the key points outward, the orphaned
	// config row is removed by AfterDelete rather than by a database cascade.
	BatchJobConfigID string          `gorm:"column:batch_job_config_id;type:varchar(36);not null;uniqueIndex" json:"batchJobConfigId"`
	BatchJobConfig   *BatchJobConfig `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"batchJobConfig,omitempty"`

	// The default submission credential is an SSH endpoint credential binding rather
	// than a bare SSH credential: it ties the submitting identity to a specific host
	// and owner, the same association a job submitted under it will run as.
	DefaultSubmissionCredentialID string                              `gorm:"column:default_submission_credential_id;type:varchar(36);not null;index" json:"defaultSubmissionCredentialId"`
	DefaultSubmissionCredential   *computemodel.SSHEndpointCredential `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// WorkDir is a parent directory; each execution gets a subdirectory beneath it.
	WorkDir   *string `gorm:"column:work_dir;type:varchar(1024)" json:"workDir,omitempty"`
	Partition *string `gorm:"column:partition;type:varchar(255)" json:"partition,omitempty"`
}

// TableName returns the table backing BatchDeployment.
func (BatchDeployment) TableName() string { return "batch_application_deployments" }

// BeforeCreate assigns a UUID when none was supplied.
func (d *BatchDeployment) BeforeCreate(*gorm.DB) error {
	if d.ID == "" {
		d.ID = uuid.NewString()
	}
	return nil
}

// AfterDelete removes the owned BatchJobConfig. This stands in for JPA's
// orphanRemoval: the foreign key points from the deployment to the config, so the
// database cannot cascade in this direction and the cleanup has to be explicit.
func (d *BatchDeployment) AfterDelete(tx *gorm.DB) error {
	if d.BatchJobConfigID == "" {
		return nil
	}
	return tx.Where("batch_job_config_id = ?", d.BatchJobConfigID).
		Delete(&BatchJobConfig{}).Error
}
