// Package model holds the cluster and partition entities. The host a cluster is
// reached through, and the credentials for it, belong to the credentials vertical.
package model

import (
	cred "github.com/apache/airavata/api/credentials/model"
	data "github.com/apache/airavata/api/data/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// Cluster is a Slurm cluster Airavata can submit to.
//
// ClusterName is not unique at the database level. The Java repository declares an
// existsByClusterName check, but no service calls it, so duplicate names are
// currently accepted; that behaviour is preserved here rather than silently tightened.
//
// Java: org.apache.airavata.compute.model.ClusterEntity
type Cluster struct {
	ID string `gorm:"column:cluster_id;primaryKey;type:varchar(36)" json:"clusterId"`

	ClusterName        string  `gorm:"column:cluster_name;type:varchar(255);not null" json:"clusterName"`
	ClusterDescription *string `gorm:"column:cluster_description;type:varchar(1024)" json:"clusterDescription,omitempty"`
	SlurmHome          string  `gorm:"column:slurm_home;type:varchar(1024);not null" json:"slurmHome"`

	SSHEndpointID *string           `gorm:"column:ssh_endpoint_id;type:varchar(36);index" json:"sshEndpointId,omitempty"`
	SSHEndpoint   *cred.SSHEndpoint `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"sshEndpoint,omitempty"`

	SCPDataStorageID *string              `gorm:"column:scp_data_storage_id;type:varchar(36);index" json:"scpDataStorageId,omitempty"`
	SCPDataStorage   *data.SCPDataStorage `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"scpDataStorage,omitempty"`
	// Partitions are owned by the cluster: created, replaced and deleted with it, and
	// meaningless outside it. Note that the Java service deliberately never mutates
	// this collection — it writes through the partition repository instead, because
	// touching an orphan-removing collection risks deleting unrelated rows. Go code
	// should keep that discipline and treat this field as a read-side projection.
	Partitions []ClusterPartition `gorm:"foreignKey:ClusterID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"partitions,omitempty"`
}

// TableName returns the table backing Cluster.
func (Cluster) TableName() string { return "clusters" }

// BeforeCreate assigns a UUID when none was supplied.
func (c *Cluster) BeforeCreate(*gorm.DB) error {
	if c.ID == "" {
		c.ID = uuid.NewString()
	}
	return nil
}
