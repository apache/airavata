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
)

// ClusterPartition is a Slurm partition (queue) on a SlurmCluster.
//
// Every limit is nullable: an unset MaxNodes means "no declared limit", which is
// distinct from a limit of zero. Hence the pointers.
//
// Java: org.apache.airavata.compute.model.ClusterPartitionEntity
type ClusterPartition struct {
	ID string `gorm:"column:partition_id;primaryKey;type:varchar(36)" json:"partitionId"`

	// The owning side of this association is SlurmCluster.Partitions, which declares the
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
