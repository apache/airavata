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

package db

import (
	"context"
	"errors"
	"fmt"
	"os"
	"strconv"
	"time"

	"gorm.io/gorm"

	applicationmodel "github.com/apache/airavata/api/application/model"
	computemodel "github.com/apache/airavata/api/compute/model"
	credentialsmodel "github.com/apache/airavata/api/credentials/model"
	datamodel "github.com/apache/airavata/api/data/model"
	iammodel "github.com/apache/airavata/api/iam/model"
	sharingmodel "github.com/apache/airavata/api/sharing/model"
	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/ptr"
	"github.com/apache/airavata/internal/role"
)

const (
	// DefaultDevSSHKeyName is the default credential name for the mock cluster key.
	DefaultDevSSHKeyName = "mock-slurm-key"

	// DefaultDevSSHPubKey is the public key matching dev-tools/compose/mock-slurm/keys/id_ed25519.
	DefaultDevSSHPubKey = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIP4OwqMYkHHVjkaZKcrd7MMGFKBBdM+jLcLzDIjRT6eB airavata-mock-slurm-dev"

	// DefaultDevSSHPrivKey is the private key matching dev-tools/compose/mock-slurm/keys/id_ed25519.
	DefaultDevSSHPrivKey = `-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACD+DsKjGJBx1Y5GmSnK3ezDBhSgQXTPoy3C8wyI0U+ngQAAAKDspGFZ7KRh
WQAAAAtzc2gtZWQyNTUxOQAAACD+DsKjGJBx1Y5GmSnK3ezDBhSgQXTPoy3C8wyI0U+ngQ
AAAEAgZZ723g5kvTHTUSq/i5Q/F1EwhMrtOb8Gi+ANfIvxKf4OwqMYkHHVjkaZKcrd7MMG
FKBBdM+jLcLzDIjRT6eBAAAAF2FpcmF2YXRhLW1vY2stc2x1cm0tZGV2AQIDBAUG
-----END OPENSSH PRIVATE KEY-----`

	// DefaultClusterName is the local mock cluster name.
	DefaultClusterName = "local-mock-cluster"

	// DefaultTemplateName is the demo application template name.
	DefaultTemplateName = "Echo App"

	// DefaultStorageName is the local mock storage name.
	DefaultStorageName = "mock-slurm-storage"

	// DefaultDevServiceAccountName is the default service account name for development.
	DefaultDevServiceAccountName = "dev-agent"

	// DefaultDevServiceAccountToken is the static token for the development service account.
	DefaultDevServiceAccountToken = "airavata_sa_dev_secret_token_for_local_testing_only"
)

// SeedResult summarizes the records created or verified during seeding.
type SeedResult struct {
	Users           []string
	ServiceAccounts []string
	Groups          []string
	Credentials     []string
	Clusters        []string
	Partitions      []string
	Configs         []string
	Templates       []string
	Deployments     []string
	Storages        []string
}

// Seed idempotently populates the database with initial development resources:
// - Root and dev admin users with corresponding roles
// - Default development service account (sa:dev-agent) and static bearer token
// - Default development group with memberships
// - Mock Slurm SSH credential (ed25519)
// - Slurm cluster pointing to the mock Slurm container
// - Debug (default) and compute partitions
// - Slurm cluster configuration for slurmuser
// - Demonstration application template ("Echo App") with default input and output
// - Batch deployment for the template on the mock cluster
// - SCP data storage configuration
// - Resource sharing grants for the dev user and service account
func Seed(ctx context.Context, gdb *gorm.DB) (*SeedResult, error) {
	result := &SeedResult{}

	// 1. Users & Roles
	if err := seedUsers(ctx, gdb, result); err != nil {
		return nil, fmt.Errorf("seed users: %w", err)
	}

	// 2. Service Accounts
	if err := seedServiceAccount(ctx, gdb, result); err != nil {
		return nil, fmt.Errorf("seed service account: %w", err)
	}

	// 3. Groups & Memberships
	if err := seedGroups(ctx, gdb, result); err != nil {
		return nil, fmt.Errorf("seed groups: %w", err)
	}

	// 4. SSH Credential
	cred, err := seedSSHCredential(ctx, gdb, result)
	if err != nil {
		return nil, fmt.Errorf("seed ssh credential: %w", err)
	}

	// 5. Slurm Cluster
	cluster, err := seedSlurmCluster(ctx, gdb, result)
	if err != nil {
		return nil, fmt.Errorf("seed slurm cluster: %w", err)
	}

	// 6. Cluster Partitions
	if err := seedPartitions(ctx, gdb, cluster, result); err != nil {
		return nil, fmt.Errorf("seed cluster partitions: %w", err)
	}

	// 7. Cluster Configuration
	config, err := seedClusterConfig(ctx, gdb, cluster, cred, result)
	if err != nil {
		return nil, fmt.Errorf("seed cluster config: %w", err)
	}

	// 8. Application Template
	tmpl, err := seedTemplate(ctx, gdb, result)
	if err != nil {
		return nil, fmt.Errorf("seed template: %w", err)
	}

	// 9. Batch Deployment
	if _, err := seedDeployment(ctx, gdb, tmpl, cluster, result); err != nil {
		return nil, fmt.Errorf("seed deployment: %w", err)
	}

	// 10. Data Storage
	storage, err := seedDataStorage(ctx, gdb, cred, result)
	if err != nil {
		return nil, fmt.Errorf("seed data storage: %w", err)
	}

	// 11. Grant sharing permissions to dev-user and service account
	if err := seedSharing(ctx, gdb, config, storage); err != nil {
		return nil, fmt.Errorf("seed sharing: %w", err)
	}

	return result, nil
}

func seedUsers(ctx context.Context, gdb *gorm.DB, res *SeedResult) error {
	now := time.Now().UnixMilli()

	// Root account
	var root iammodel.User
	err := gdb.WithContext(ctx).Where("user_id = ?", auth.RootUsername).First(&root).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		root = iammodel.User{
			ID:         auth.RootUsername,
			AuthMethod: ptr.To(iammodel.AuthMethodSystem),
			FirstName:  ptr.To("Root"),
			LastName:   ptr.To("Account"),
			Status:     ptr.To(iammodel.UserStatusActive),
			CreatedAt:  now,
		}
		if err := gdb.WithContext(ctx).Create(&root).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	var rootRole iammodel.UserRole
	err = gdb.WithContext(ctx).Where("user_id = ? AND role = ?", auth.RootUsername, role.SuperAdmin).First(&rootRole).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		rootRole = iammodel.UserRole{
			UserID: auth.RootUsername,
			Role:   role.SuperAdmin,
		}
		if err := gdb.WithContext(ctx).Create(&rootRole).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}
	res.Users = append(res.Users, fmt.Sprintf("%s (%s)", auth.RootUsername, role.SuperAdmin))

	// Dev user for testing
	const devUserID = "dev-user"
	var devUser iammodel.User
	err = gdb.WithContext(ctx).Where("user_id = ?", devUserID).First(&devUser).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		devUser = iammodel.User{
			ID:         devUserID,
			Email:      ptr.To("dev@example.edu"),
			AuthMethod: ptr.To(iammodel.AuthMethodSystem),
			FirstName:  ptr.To("Dev"),
			LastName:   ptr.To("User"),
			Status:     ptr.To(iammodel.UserStatusActive),
			CreatedAt:  now,
		}
		if err := gdb.WithContext(ctx).Create(&devUser).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	var devRole iammodel.UserRole
	err = gdb.WithContext(ctx).Where("user_id = ? AND role = ?", devUserID, role.Admin).First(&devRole).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		devRole = iammodel.UserRole{
			UserID: devUserID,
			Role:   role.Admin,
		}
		if err := gdb.WithContext(ctx).Create(&devRole).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}
	res.Users = append(res.Users, fmt.Sprintf("%s (%s)", devUserID, role.Admin))

	return nil
}

func seedServiceAccount(ctx context.Context, gdb *gorm.DB, res *SeedResult) error {
	now := time.Now().UnixMilli()
	saID := auth.ServiceAccountIDPrefix + DefaultDevServiceAccountName

	var user iammodel.User
	err := gdb.WithContext(ctx).Where("user_id = ?", saID).First(&user).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		user = iammodel.User{
			ID:         saID,
			Email:      ptr.To("dev-agent@example.edu"),
			AuthMethod: ptr.To(iammodel.AuthMethodServiceAccount),
			FirstName:  ptr.To("Dev"),
			LastName:   ptr.To("Agent"),
			Status:     ptr.To(iammodel.UserStatusActive),
			CreatedAt:  now,
		}
		if err := gdb.WithContext(ctx).Create(&user).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	tokenHash := auth.HashServiceAccountToken(DefaultDevServiceAccountToken)
	expiresAt := now + 365*24*time.Hour.Milliseconds()

	var sa iammodel.ServiceAccount
	err = gdb.WithContext(ctx).Where("user_id = ?", saID).First(&sa).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		sa = iammodel.ServiceAccount{
			UserID:         saID,
			Description:    ptr.To("Default development service account"),
			CreatedBy:      auth.RootUsername,
			TokenHash:      tokenHash,
			TokenIssuedAt:  now,
			TokenExpiresAt: expiresAt,
		}
		if err := gdb.WithContext(ctx).Create(&sa).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	res.ServiceAccounts = append(res.ServiceAccounts, fmt.Sprintf("%s (%s)", saID, DefaultDevServiceAccountToken))
	return nil
}

func seedGroups(ctx context.Context, gdb *gorm.DB, res *SeedResult) error {
	now := time.Now().UnixMilli()
	const defaultGroupName = "default-group"

	var group iammodel.Group
	err := gdb.WithContext(ctx).Where("group_name = ? AND user_id = ?", defaultGroupName, auth.RootUsername).First(&group).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		group = iammodel.Group{
			Name:      ptr.To(defaultGroupName),
			OwnerID:   ptr.To(auth.RootUsername),
			CreatedAt: now,
		}
		if err := gdb.WithContext(ctx).Create(&group).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	// Owner is admin of the group
	var ownerMember iammodel.GroupMember
	err = gdb.WithContext(ctx).Where("group_id = ? AND user_id = ?", group.ID, auth.RootUsername).First(&ownerMember).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		ownerMember = iammodel.GroupMember{
			GroupID:           group.ID,
			UserID:            auth.RootUsername,
			GroupRole:         iammodel.GroupRoleAdmin,
			GroupMemberStatus: iammodel.GroupMemberStatusActive,
		}
		if err := gdb.WithContext(ctx).Create(&ownerMember).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	// dev-user is a member of the group
	var devMember iammodel.GroupMember
	err = gdb.WithContext(ctx).Where("group_id = ? AND user_id = ?", group.ID, "dev-user").First(&devMember).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		devMember = iammodel.GroupMember{
			GroupID:           group.ID,
			UserID:            "dev-user",
			GroupRole:         iammodel.GroupRoleMember,
			GroupMemberStatus: iammodel.GroupMemberStatusActive,
		}
		if err := gdb.WithContext(ctx).Create(&devMember).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	// sa:dev-agent is a member of the group
	saID := auth.ServiceAccountIDPrefix + DefaultDevServiceAccountName
	var saMember iammodel.GroupMember
	err = gdb.WithContext(ctx).Where("group_id = ? AND user_id = ?", group.ID, saID).First(&saMember).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		saMember = iammodel.GroupMember{
			GroupID:           group.ID,
			UserID:            saID,
			GroupRole:         iammodel.GroupRoleMember,
			GroupMemberStatus: iammodel.GroupMemberStatusActive,
		}
		if err := gdb.WithContext(ctx).Create(&saMember).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	res.Groups = append(res.Groups, fmt.Sprintf("%s (%s)", *group.Name, group.ID))
	return nil
}

func seedSSHCredential(ctx context.Context, gdb *gorm.DB, res *SeedResult) (*credentialsmodel.SSHCredential, error) {
	pubKey, privKey := resolveDevSSHKeys()

	var cred credentialsmodel.SSHCredential
	err := gdb.WithContext(ctx).Where("ssh_credential_name = ? AND owner_id = ?", DefaultDevSSHKeyName, auth.RootUsername).First(&cred).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		cred = credentialsmodel.SSHCredential{
			SSHCredentialName: DefaultDevSSHKeyName,
			PublicKey:         pubKey,
			PrivateKey:        privKey,
			OwnerID:           auth.RootUsername,
		}
		if err := gdb.WithContext(ctx).Create(&cred).Error; err != nil {
			return nil, err
		}
	} else if err != nil {
		return nil, err
	} else {
		cred.PublicKey = pubKey
		cred.PrivateKey = privKey
		if err := gdb.WithContext(ctx).Save(&cred).Error; err != nil {
			return nil, err
		}
	}

	res.Credentials = append(res.Credentials, fmt.Sprintf("%s (%s)", cred.SSHCredentialName, cred.ID))
	return &cred, nil
}

func seedSlurmCluster(ctx context.Context, gdb *gorm.DB, res *SeedResult) (*computemodel.SlurmCluster, error) {
	headnodeHost := getEnv("MOCK_SLURM_HOST", "localhost")
	headnodePort := getEnvInt("MOCK_SLURM_PORT", 12222)

	var cluster computemodel.SlurmCluster
	err := gdb.WithContext(ctx).Where("cluster_name = ?", DefaultClusterName).First(&cluster).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		cluster = computemodel.SlurmCluster{
			ClusterName:        DefaultClusterName,
			ClusterDescription: ptr.To("Local mock Slurm cluster for development and testing"),
			HeadnodeHost:       headnodeHost,
			HeadnodePort:       headnodePort,
			SlurmHome:          ptr.To("/opt/slurm/bin"),
			DataHost:           ptr.To(headnodeHost),
			DataPort:           ptr.To(headnodePort),
		}
		if err := gdb.WithContext(ctx).Create(&cluster).Error; err != nil {
			return nil, err
		}
	} else if err != nil {
		return nil, err
	} else {
		cluster.HeadnodeHost = headnodeHost
		cluster.HeadnodePort = headnodePort
		if err := gdb.WithContext(ctx).Save(&cluster).Error; err != nil {
			return nil, err
		}
	}

	res.Clusters = append(res.Clusters, fmt.Sprintf("%s (%s on %s:%d)", cluster.ClusterName, cluster.ID, headnodeHost, headnodePort))
	return &cluster, nil
}

func seedPartitions(ctx context.Context, gdb *gorm.DB, cluster *computemodel.SlurmCluster, res *SeedResult) error {
	partitions := []computemodel.ClusterPartition{
		{
			ClusterID:      &cluster.ID,
			Name:           "debug",
			Description:    ptr.To("Default debug queue for local testing"),
			MaxRunTime:     ptr.To(int32(60)),
			MaxNodes:       ptr.To(int32(1)),
			MaxProcessors:  ptr.To(int32(4)),
			CPUPerNode:     ptr.To(int32(4)),
			MaxMemory:      ptr.To(int64(4096)),
			Nodes:          ptr.To("mock-node01"),
			IsDefaultQueue: ptr.To(true),
		},
		{
			ClusterID:      &cluster.ID,
			Name:           "compute",
			Description:    ptr.To("Compute queue for multi-node/GPU workloads"),
			MaxRunTime:     ptr.To(int32(1440)),
			MaxNodes:       ptr.To(int32(4)),
			MaxProcessors:  ptr.To(int32(16)),
			CPUPerNode:     ptr.To(int32(4)),
			MaxMemory:      ptr.To(int64(16384)),
			Nodes:          ptr.To("mock-node[02-05]"),
			Gres:           ptr.To("gpu:1"),
			IsDefaultQueue: ptr.To(false),
		},
	}

	for _, p := range partitions {
		var existing computemodel.ClusterPartition
		err := gdb.WithContext(ctx).Where("cluster_id = ? AND name = ?", cluster.ID, p.Name).First(&existing).Error
		if errors.Is(err, gorm.ErrRecordNotFound) {
			if err := gdb.WithContext(ctx).Create(&p).Error; err != nil {
				return err
			}
			existing = p
		} else if err != nil {
			return err
		} else {
			p.ID = existing.ID
			if err := gdb.WithContext(ctx).Save(&p).Error; err != nil {
				return err
			}
		}
		defStr := ""
		if p.IsDefaultQueue != nil && *p.IsDefaultQueue {
			defStr = " [DEFAULT]"
		}
		res.Partitions = append(res.Partitions, fmt.Sprintf("%s%s (%s)", p.Name, defStr, existing.ID))
	}

	return nil
}

func seedClusterConfig(
	ctx context.Context,
	gdb *gorm.DB,
	cluster *computemodel.SlurmCluster,
	cred *credentialsmodel.SSHCredential,
	res *SeedResult,
) (*computemodel.SlurmClusterConfig, error) {
	const loginUser = "slurmuser"
	const workRoot = "/home/slurmuser/scratch"

	var cfg computemodel.SlurmClusterConfig
	err := gdb.WithContext(ctx).Where("slurm_cluster_id = ? AND user_id = ? AND login_user = ?", cluster.ID, auth.RootUsername, loginUser).First(&cfg).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		cfg = computemodel.SlurmClusterConfig{
			Name:            ptr.To("mock-slurm-root-config"),
			Description:     ptr.To("Default Slurm cluster configuration for root on mock-slurm"),
			SlurmClusterID:  cluster.ID,
			LoginUser:       loginUser,
			WorkRoot:        workRoot,
			SSHCredentialID: &cred.ID,
			OwnerID:         ptr.To(auth.RootUsername),
		}
		if err := gdb.WithContext(ctx).Create(&cfg).Error; err != nil {
			return nil, err
		}
	} else if err != nil {
		return nil, err
	} else {
		cfg.SSHCredentialID = &cred.ID
		cfg.WorkRoot = workRoot
		if err := gdb.WithContext(ctx).Save(&cfg).Error; err != nil {
			return nil, err
		}
	}

	res.Configs = append(res.Configs, fmt.Sprintf("%s (%s for %s@%s)", *cfg.Name, cfg.ID, cfg.LoginUser, cluster.ClusterName))
	return &cfg, nil
}

func seedTemplate(ctx context.Context, gdb *gorm.DB, res *SeedResult) (*applicationmodel.Template, error) {
	var tmpl applicationmodel.Template
	err := gdb.WithContext(ctx).Where("template_name = ?", DefaultTemplateName).First(&tmpl).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		inType := applicationmodel.TemplateInputTypeString
		outType := applicationmodel.TemplateOutputTypeFile

		tmpl = applicationmodel.Template{
			TemplateName:        ptr.To(DefaultTemplateName),
			TemplateDescription: ptr.To("Demonstration echo application for local development"),
			Inputs: []applicationmodel.TemplateInput{
				{
					InputName:        ptr.To("message"),
					DisplayName:      ptr.To("Greeting Message"),
					InputDescription: ptr.To("Message printed by the echo application"),
					InputType:        &inType,
					IsRequired:       false,
					DefaultValue:     ptr.To(`{"value":"Hello from Airavata"}`),
				},
			},
			Outputs: []applicationmodel.TemplateOutput{
				{
					OutputName:        ptr.To("output.txt"),
					DisplayName:       ptr.To("Output File"),
					OutputDescription: ptr.To("Captured computation output"),
					OutputType:        &outType,
				},
			},
		}
		if err := gdb.WithContext(ctx).Create(&tmpl).Error; err != nil {
			return nil, err
		}
	} else if err != nil {
		return nil, err
	}

	res.Templates = append(res.Templates, fmt.Sprintf("%s (%s)", *tmpl.TemplateName, tmpl.ID))
	return &tmpl, nil
}

func seedDeployment(
	ctx context.Context,
	gdb *gorm.DB,
	tmpl *applicationmodel.Template,
	cluster *computemodel.SlurmCluster,
	res *SeedResult,
) (*applicationmodel.BatchDeployment, error) {
	var dep applicationmodel.BatchDeployment
	err := gdb.WithContext(ctx).Where("template_id = ? AND cluster_id = ?", tmpl.ID, cluster.ID).First(&dep).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		jobCfg := applicationmodel.BatchJobConfig{
			WallTimeMinutes: 10,
			Nodes:           ptr.To(int32(1)),
			CPUs:            ptr.To(int32(1)),
			Allocation:      "default",
		}
		if err := gdb.WithContext(ctx).Create(&jobCfg).Error; err != nil {
			return nil, fmt.Errorf("create batch job config: %w", err)
		}

		runSection := "echo \"{{ inputs.message[0] }} on process {{ process_id }}!\" && echo \"computation output\" > output.txt"
		dep = applicationmodel.BatchDeployment{
			TemplateID:              &tmpl.ID,
			ClusterID:               &cluster.ID,
			DefaultPartition:        ptr.To("debug"),
			SlurmRunSection:         runSection,
			DefaultBatchJobConfigID: jobCfg.ID,
		}
		if err := gdb.WithContext(ctx).Create(&dep).Error; err != nil {
			return nil, err
		}
	} else if err != nil {
		return nil, err
	}

	res.Deployments = append(res.Deployments, fmt.Sprintf("%s on %s (%s)", *tmpl.TemplateName, cluster.ClusterName, dep.ID))
	return &dep, nil
}

func seedDataStorage(
	ctx context.Context,
	gdb *gorm.DB,
	cred *credentialsmodel.SSHCredential,
	res *SeedResult,
) (*datamodel.SCPDataStorage, error) {
	headnodeHost := getEnv("MOCK_SLURM_HOST", "localhost")
	headnodePort := getEnvInt("MOCK_SLURM_PORT", 12222)
	loginUser := "slurmuser"

	var storage datamodel.SCPDataStorage
	err := gdb.WithContext(ctx).Where("data_name = ? AND user_id = ?", DefaultStorageName, auth.RootUsername).First(&storage).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		storage = datamodel.SCPDataStorage{
			Name:            ptr.To(DefaultStorageName),
			HostName:        ptr.To(headnodeHost),
			Port:            ptr.To(headnodePort),
			LoginUser:       ptr.To(loginUser),
			SSHCredentialID: &cred.ID,
			OwnerID:         ptr.To(auth.RootUsername),
		}
		if err := gdb.WithContext(ctx).Create(&storage).Error; err != nil {
			return nil, err
		}
	} else if err != nil {
		return nil, err
	} else {
		storage.HostName = ptr.To(headnodeHost)
		storage.Port = ptr.To(headnodePort)
		storage.SSHCredentialID = &cred.ID
		if err := gdb.WithContext(ctx).Save(&storage).Error; err != nil {
			return nil, err
		}
	}

	res.Storages = append(res.Storages, fmt.Sprintf("%s (%s on %s:%d)", *storage.Name, storage.ID, headnodeHost, headnodePort))
	return &storage, nil
}

func seedSharing(
	ctx context.Context,
	gdb *gorm.DB,
	config *computemodel.SlurmClusterConfig,
	storage *datamodel.SCPDataStorage,
) error {
	const devUserID = "dev-user"

	// Share cluster config with dev-user
	var cfgShare sharingmodel.Sharing
	err := gdb.WithContext(ctx).Where(
		"resource_type = ? AND resource_id = ? AND principal_type = ? AND principal_id = ?",
		sharingmodel.ResourceTypeSlurmClusterConfig, config.ID, sharingmodel.PrincipalTypeUser, devUserID,
	).First(&cfgShare).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		cfgShare = sharingmodel.Sharing{
			ResourceType:  sharingmodel.ResourceTypeSlurmClusterConfig,
			ResourceID:    config.ID,
			PrincipalType: sharingmodel.PrincipalTypeUser,
			PrincipalID:   devUserID,
			Permission:    sharingmodel.AccessPermissionWrite,
		}
		if err := gdb.WithContext(ctx).Create(&cfgShare).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	// Share data storage with dev-user
	var storageShare sharingmodel.Sharing
	err = gdb.WithContext(ctx).Where(
		"resource_type = ? AND resource_id = ? AND principal_type = ? AND principal_id = ?",
		sharingmodel.ResourceTypeSCPDataStorage, storage.ID, sharingmodel.PrincipalTypeUser, devUserID,
	).First(&storageShare).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		storageShare = sharingmodel.Sharing{
			ResourceType:  sharingmodel.ResourceTypeSCPDataStorage,
			ResourceID:    storage.ID,
			PrincipalType: sharingmodel.PrincipalTypeUser,
			PrincipalID:   devUserID,
			Permission:    sharingmodel.AccessPermissionWrite,
		}
		if err := gdb.WithContext(ctx).Create(&storageShare).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	// Share cluster config with sa:dev-agent
	saID := auth.ServiceAccountIDPrefix + DefaultDevServiceAccountName
	var saCfgShare sharingmodel.Sharing
	err = gdb.WithContext(ctx).Where(
		"resource_type = ? AND resource_id = ? AND principal_type = ? AND principal_id = ?",
		sharingmodel.ResourceTypeSlurmClusterConfig, config.ID, sharingmodel.PrincipalTypeUser, saID,
	).First(&saCfgShare).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		saCfgShare = sharingmodel.Sharing{
			ResourceType:  sharingmodel.ResourceTypeSlurmClusterConfig,
			ResourceID:    config.ID,
			PrincipalType: sharingmodel.PrincipalTypeUser,
			PrincipalID:   saID,
			Permission:    sharingmodel.AccessPermissionWrite,
		}
		if err := gdb.WithContext(ctx).Create(&saCfgShare).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	// Share data storage with sa:dev-agent
	var saStorageShare sharingmodel.Sharing
	err = gdb.WithContext(ctx).Where(
		"resource_type = ? AND resource_id = ? AND principal_type = ? AND principal_id = ?",
		sharingmodel.ResourceTypeSCPDataStorage, storage.ID, sharingmodel.PrincipalTypeUser, saID,
	).First(&saStorageShare).Error
	if errors.Is(err, gorm.ErrRecordNotFound) {
		saStorageShare = sharingmodel.Sharing{
			ResourceType:  sharingmodel.ResourceTypeSCPDataStorage,
			ResourceID:    storage.ID,
			PrincipalType: sharingmodel.PrincipalTypeUser,
			PrincipalID:   saID,
			Permission:    sharingmodel.AccessPermissionWrite,
		}
		if err := gdb.WithContext(ctx).Create(&saStorageShare).Error; err != nil {
			return err
		}
	} else if err != nil {
		return err
	}

	return nil
}

func resolveDevSSHKeys() (pubKey, privKey string) {
	pubKey = DefaultDevSSHPubKey
	privKey = DefaultDevSSHPrivKey

	for _, privPath := range []string{
		os.Getenv("MOCK_SLURM_PRIVATE_KEY_PATH"),
		"dev-tools/compose/mock-slurm/keys/id_ed25519",
		"../dev-tools/compose/mock-slurm/keys/id_ed25519",
		"../../dev-tools/compose/mock-slurm/keys/id_ed25519",
	} {
		if privPath != "" {
			if data, err := os.ReadFile(privPath); err == nil && len(data) > 0 {
				privKey = string(data)
				break
			}
		}
	}

	for _, pubPath := range []string{
		os.Getenv("MOCK_SLURM_PUBLIC_KEY_PATH"),
		"dev-tools/compose/mock-slurm/keys/id_ed25519.pub",
		"../dev-tools/compose/mock-slurm/keys/id_ed25519.pub",
		"../../dev-tools/compose/mock-slurm/keys/id_ed25519.pub",
	} {
		if pubPath != "" {
			if data, err := os.ReadFile(pubPath); err == nil && len(data) > 0 {
				pubKey = string(data)
				break
			}
		}
	}

	return pubKey, privKey
}

func getEnv(key, fallback string) string {
	if val := os.Getenv(key); val != "" {
		return val
	}
	return fallback
}

func getEnvInt(key string, fallback int) int {
	if val := os.Getenv(key); val != "" {
		if i, err := strconv.Atoi(val); err == nil {
			return i
		}
	}
	return fallback
}
