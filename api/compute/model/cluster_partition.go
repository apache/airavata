package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// ClusterPartition is a Slurm partition (queue) on a Cluster.
//
// Every limit is nullable: an unset MaxNodes means "no declared limit", which is
// distinct from a limit of zero. Hence the pointers.
//
// Java: org.apache.airavata.compute.model.ClusterPartitionEntity
type ClusterPartition struct {
	ID string `gorm:"column:partition_id;primaryKey;type:varchar(36)" json:"partitionId"`

	// The owning side of this association is Cluster.Partitions, which declares the
	// cascade. Only the key is held here; a back-reference would produce a second,
	// redundant foreign key on the same column.
	ClusterID *string `gorm:"column:cluster_id;type:varchar(36);index" json:"clusterId,omitempty"`

	Name        string  `gorm:"column:name;type:varchar(255);not null" json:"name"`
	Description *string `gorm:"column:description;type:varchar(1024)" json:"description,omitempty"`

	MaxRunTime     *int32 `gorm:"column:max_run_time" json:"maxRunTime,omitempty"`
	MaxNodes       *int32 `gorm:"column:max_nodes" json:"maxNodes,omitempty"`
	MaxProcessors  *int32 `gorm:"column:max_processors" json:"maxProcessors,omitempty"`
	MaxJobsInQueue *int32 `gorm:"column:max_jobs_in_queue" json:"maxJobsInQueue,omitempty"`
	MaxMemory      *int64 `gorm:"column:max_memory" json:"maxMemory,omitempty"`

	CPUPerNode       *int32 `gorm:"column:cpu_per_node" json:"cpuPerNode,omitempty"`
	DefaultNodeCount *int32 `gorm:"column:default_node_count" json:"defaultNodeCount,omitempty"`
	DefaultCPUCount  *int32 `gorm:"column:default_cpu_count" json:"defaultCpuCount,omitempty"`
	DefaultWalltime  *int64 `gorm:"column:default_walltime" json:"defaultWalltime,omitempty"`

	// Gres and Nodes are comma-separated lists, stored verbatim as in the Java model.
	Gres  *string `gorm:"column:gres;type:varchar(1024)" json:"gres,omitempty"`
	Nodes *string `gorm:"column:nodes;type:varchar(4096)" json:"nodes,omitempty"`

	IsDefaultQueue   *bool `gorm:"column:is_default_queue" json:"isDefaultQueue,omitempty"`
	IsCheckpointable *bool `gorm:"column:is_checkpointable" json:"isCheckpointable,omitempty"`
}

// TableName returns the table backing ClusterPartition.
func (ClusterPartition) TableName() string { return "cluster_partitions" }

// BeforeCreate assigns a UUID when none was supplied.
func (p *ClusterPartition) BeforeCreate(*gorm.DB) error {
	if p.ID == "" {
		p.ID = uuid.NewString()
	}
	return nil
}
