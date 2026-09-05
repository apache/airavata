// Package model holds the cluster and partition entities. The host a cluster is
// reached through, and the credentials for it, belong to the credentials vertical.
package model

import (
	cred "github.com/apache/airavata/api/credentials/model"
	iammodel "github.com/apache/airavata/api/iam/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// ClusterPermission is the access level a cluster-config sharing row grants.
type ClusterPermission string

const (
	ClusterPermissionRead  ClusterPermission = "READ"
	ClusterPermissionWrite ClusterPermission = "WRITE"
)

// Valid reports whether p is one of the declared permissions.
func (p ClusterPermission) Valid() bool {
	switch p {
	case ClusterPermissionRead, ClusterPermissionWrite:
		return true
	}
	return false
}

// SlurmCluster is a Slurm cluster Airavata can submit to.
//
// It describes the machine and nothing about who reaches it: the head node to submit
// through, an optional separate endpoint for data movement, and the partitions it is
// carved into. How a particular person logs in — under which account, with which key,
// beneath which work root — is a SlurmClusterConfig, so one registered cluster serves
// every user without the catalogue holding anyone's credentials.
type SlurmCluster struct {
	ID string `gorm:"column:slurm_cluster_id;primaryKey;type:varchar(36)" json:"slurmClusterId"`

	ClusterName        string  `gorm:"column:cluster_name;type:varchar(255);not null" json:"clusterName"`
	ClusterDescription *string `gorm:"column:cluster_description;type:varchar(1024)" json:"clusterDescription,omitempty"`

	HeadnodeHost string `gorm:"column:headnode_host;type:varchar(255);not null" json:"headnodeHost"`
	HeadnodePort int    `gorm:"column:headnode_port;type:int;not null" json:"headnodePort"`

	// optional endpoint to configure additional data movement path
	DataHost *string `gorm:"column:data_host;type:varchar(255)" json:"dataHost,omitempty"`
	DataPort *int    `gorm:"column:data_port;type:int" json:"dataPort,omitempty"`

	// Partitions are owned by the cluster: created, replaced and deleted with it, and
	// meaningless outside it. Note that the Java service deliberately never mutated
	// this collection — it wrote through the partition repository instead, because
	// touching an orphan-removing collection risks deleting unrelated rows. Go code
	// should keep that discipline and treat this field as a read-side projection.
	Partitions []ClusterPartition `gorm:"foreignKey:ClusterID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"partitions,omitempty"`
}

// TableName returns the table backing SlurmCluster.
func (SlurmCluster) TableName() string { return "slurm_clusters" }

// BeforeCreate assigns a UUID when none was supplied.
func (c *SlurmCluster) BeforeCreate(*gorm.DB) error {
	if c.ID == "" {
		c.ID = uuid.NewString()
	}
	return nil
}

// SlurmClusterConfig is one way of logging in to a SlurmCluster: an account, a key,
// and the directory work is done beneath.
//
// It belongs to whoever registered it, and everyone else reaches it through the
// sharing rows below — which is what lets a PI hand a team access to an allocation
// without handing over the key itself. Ownership is not transferable through the API:
// a run is launched against a config by id, so handing one over would silently hand
// over the identity every job on it submits as.
type SlurmClusterConfig struct {
	ID string `gorm:"column:slurm_cluster_config_id;primaryKey;type:varchar(36)" json:"slurmClusterConfigId"`

	Name        *string `gorm:"column:name;type:varchar(255)" json:"name,omitempty"`
	Description *string `gorm:"column:description;type:varchar(1024)" json:"description,omitempty"`

	SlurmClusterID string        `gorm:"column:slurm_cluster_id;type:varchar(36);index" json:"slurmClusterId"`
	SlurmCluster   *SlurmCluster `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"slurmCluster,omitempty"`

	LoginUser string `gorm:"column:login_user;type:varchar(255);not null" json:"loginUser"`

	WorkRoot string `gorm:"column:work_root;type:varchar(1024);not null" json:"workRoot"`

	SSHKeyID *string      `gorm:"column:ssh_key_id;type:varchar(36);index" json:"sshKeyId,omitempty"`
	SSHKey   *cred.SSHKey `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"sshKey,omitempty"`

	// OwnerID is named for its role because ownership, not mere reference, is what the
	// authorisation checks read. RESTRICT: a user who still owns configs cannot be
	// deleted out from under them.
	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"ownerId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
}

// TableName returns the table backing SlurmClusterConfig.
func (SlurmClusterConfig) TableName() string { return "slurm_cluster_configs" }

// OwnedBy reports whether userID owns this config. A config with no owner is owned by
// nobody, so it must not match the empty principal name.
func (c *SlurmClusterConfig) OwnedBy(userID string) bool {
	return c.OwnerID != nil && *c.OwnerID == userID
}

// BeforeCreate assigns a UUID when none was supplied.
func (c *SlurmClusterConfig) BeforeCreate(*gorm.DB) error {
	if c.ID == "" {
		c.ID = uuid.NewString()
	}
	return nil
}

// SlurmClusterConfigUserSharing grants one named user access to one cluster config.
type SlurmClusterConfigUserSharing struct {
	ID string `gorm:"column:slurm_cluster_config_user_sharing_id;primaryKey;type:varchar(36)" json:"slurmClusterConfigUserSharingId"`

	SlurmClusterConfigID string              `gorm:"column:slurm_cluster_config_id;type:varchar(36);index" json:"slurmClusterConfigId"`
	SlurmClusterConfig   *SlurmClusterConfig `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	UserID string         `gorm:"column:user_id;type:varchar(255);not null;index" json:"userId"`
	User   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission ClusterPermission `gorm:"column:permission;type:varchar(10);not null" json:"permission"`
}

// TableName returns the table backing SlurmClusterConfigUserSharing.
func (SlurmClusterConfigUserSharing) TableName() string {
	return "slurm_cluster_config_user_sharings"
}

// BeforeCreate assigns a UUID when none was supplied.
func (s *SlurmClusterConfigUserSharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

// SlurmClusterConfigGroupSharing grants a group access to one cluster config.
type SlurmClusterConfigGroupSharing struct {
	ID string `gorm:"column:slurm_cluster_config_group_sharing_id;primaryKey;type:varchar(36)" json:"slurmClusterConfigGroupSharingId"`

	SlurmClusterConfigID string              `gorm:"column:slurm_cluster_config_id;type:varchar(36);index" json:"slurmClusterConfigId"`
	SlurmClusterConfig   *SlurmClusterConfig `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	GroupID string          `gorm:"column:group_id;type:varchar(255);not null;index" json:"groupId"`
	Group   *iammodel.Group `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Permission ClusterPermission `gorm:"column:permission;type:varchar(10);not null" json:"permission"`
}

// TableName returns the table backing SlurmClusterConfigGroupSharing.
func (SlurmClusterConfigGroupSharing) TableName() string {
	return "slurm_cluster_config_group_sharings"
}

// BeforeCreate assigns a UUID when none was supplied.
func (s *SlurmClusterConfigGroupSharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}
