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

package db_test

import (
	"context"
	"testing"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/role"

	applicationmodel "github.com/apache/airavata/api/application/model"
	computemodel "github.com/apache/airavata/api/compute/model"
	credentialsmodel "github.com/apache/airavata/api/credentials/model"
	datamodel "github.com/apache/airavata/api/data/model"
	iammodel "github.com/apache/airavata/api/iam/model"
	sharingmodel "github.com/apache/airavata/api/sharing/model"
)

func TestSeedPopulatesAllResources(t *testing.T) {
	gdb := newTestDB(t)
	ctx := context.Background()

	res, err := db.Seed(ctx, gdb)
	if err != nil {
		t.Fatalf("Seed failed: %v", err)
	}

	if len(res.Users) < 2 {
		t.Errorf("got %d users, want at least 2", len(res.Users))
	}
	if len(res.ServiceAccounts) != 1 {
		t.Errorf("got %d service accounts, want 1", len(res.ServiceAccounts))
	}
	if len(res.Credentials) != 1 {
		t.Errorf("got %d credentials, want 1", len(res.Credentials))
	}
	if len(res.Clusters) != 1 {
		t.Errorf("got %d clusters, want 1", len(res.Clusters))
	}
	if len(res.Partitions) != 2 {
		t.Errorf("got %d partitions, want 2", len(res.Partitions))
	}
	if len(res.Configs) != 1 {
		t.Errorf("got %d configs, want 1", len(res.Configs))
	}
	if len(res.Templates) != 1 {
		t.Errorf("got %d templates, want 1", len(res.Templates))
	}
	if len(res.Deployments) != 1 {
		t.Errorf("got %d deployments, want 1", len(res.Deployments))
	}
	if len(res.Storages) != 1 {
		t.Errorf("got %d storages, want 1", len(res.Storages))
	}

	// Verify User rows
	var rootUser iammodel.User
	if err := gdb.Where("user_id = ?", auth.RootUsername).First(&rootUser).Error; err != nil {
		t.Fatalf("find root user: %v", err)
	}
	var rootRole iammodel.UserRole
	if err := gdb.Where("user_id = ? AND role = ?", auth.RootUsername, role.SuperAdmin).First(&rootRole).Error; err != nil {
		t.Fatalf("find root role: %v", err)
	}

	var devUser iammodel.User
	if err := gdb.Where("user_id = ?", "dev-user").First(&devUser).Error; err != nil {
		t.Fatalf("find dev user: %v", err)
	}

	// Verify Service Account
	saID := auth.ServiceAccountIDPrefix + db.DefaultDevServiceAccountName
	var saUser iammodel.User
	if err := gdb.Where("user_id = ?", saID).First(&saUser).Error; err != nil {
		t.Fatalf("find service account user: %v", err)
	}
	if !saUser.IsServiceAccount() {
		t.Errorf("saUser.IsServiceAccount() = false, want true")
	}
	var sa iammodel.ServiceAccount
	if err := gdb.Where("user_id = ?", saID).First(&sa).Error; err != nil {
		t.Fatalf("find service account: %v", err)
	}
	if sa.TokenHash != auth.HashServiceAccountToken(db.DefaultDevServiceAccountToken) {
		t.Errorf("token hash mismatch for service account")
	}
	if sa.ExpiredAt(time.Now().UnixMilli()) {
		t.Errorf("service account token should not be expired")
	}
	var saPermCount int64
	if err := gdb.Model(&iammodel.ServiceAccountPermission{}).Where("user_id = ?", saID).Count(&saPermCount).Error; err != nil {
		t.Fatalf("count service account permissions: %v", err)
	}
	if saPermCount == 0 {
		t.Errorf("expected permissions to be seeded for service account %s", saID)
	}

	// Verify Slurm Cluster
	var cluster computemodel.SlurmCluster
	if err := gdb.Where("cluster_name = ?", db.DefaultClusterName).First(&cluster).Error; err != nil {
		t.Fatalf("find cluster: %v", err)
	}

	// Verify Partitions
	var partitions []computemodel.ClusterPartition
	if err := gdb.Where("cluster_id = ?", cluster.ID).Find(&partitions).Error; err != nil {
		t.Fatalf("find partitions: %v", err)
	}
	if len(partitions) != 2 {
		t.Fatalf("expected 2 partitions, got %d", len(partitions))
	}

	// Verify SSH Credential
	var cred credentialsmodel.SSHCredential
	if err := gdb.Where("ssh_credential_name = ?", db.DefaultDevSSHKeyName).First(&cred).Error; err != nil {
		t.Fatalf("find credential: %v", err)
	}
	if cred.OwnerID != auth.RootUsername {
		t.Errorf("cred.OwnerID = %q, want %q", cred.OwnerID, auth.RootUsername)
	}

	// Verify Cluster Config
	var clusterCfg computemodel.SlurmClusterConfig
	if err := gdb.Where("slurm_cluster_id = ?", cluster.ID).First(&clusterCfg).Error; err != nil {
		t.Fatalf("find cluster config: %v", err)
	}
	if clusterCfg.LoginUser != "slurmuser" {
		t.Errorf("login user = %q, want slurmuser", clusterCfg.LoginUser)
	}
	if clusterCfg.SSHCredentialID == nil || *clusterCfg.SSHCredentialID != cred.ID {
		t.Errorf("ssh credential id = %v, want %q", clusterCfg.SSHCredentialID, cred.ID)
	}

	// Verify Application Template
	var tmpl applicationmodel.Template
	if err := gdb.Where("template_name = ?", db.DefaultTemplateName).First(&tmpl).Error; err != nil {
		t.Fatalf("find template: %v", err)
	}
	var inputs []applicationmodel.TemplateInput
	if err := gdb.Where("template_id = ?", tmpl.ID).Find(&inputs).Error; err != nil {
		t.Fatalf("find template inputs: %v", err)
	}
	if len(inputs) != 1 {
		t.Errorf("expected 1 template input, got %d", len(inputs))
	}

	// Verify Deployment
	var dep applicationmodel.BatchDeployment
	if err := gdb.Where("template_id = ? AND cluster_id = ?", tmpl.ID, cluster.ID).First(&dep).Error; err != nil {
		t.Fatalf("find deployment: %v", err)
	}

	// Verify Data Storage
	var storage datamodel.SCPDataStorage
	if err := gdb.Where("data_name = ?", db.DefaultStorageName).First(&storage).Error; err != nil {
		t.Fatalf("find data storage: %v", err)
	}

	// Verify Sharing
	var shares []sharingmodel.Sharing
	if err := gdb.Where("principal_id = ?", "dev-user").Find(&shares).Error; err != nil {
		t.Fatalf("find shares: %v", err)
	}
	if len(shares) < 2 {
		t.Errorf("expected at least 2 shares for dev-user, got %d", len(shares))
	}
}

func TestSeedIsIdempotent(t *testing.T) {
	gdb := newTestDB(t)
	ctx := context.Background()

	// First run
	if _, err := db.Seed(ctx, gdb); err != nil {
		t.Fatalf("first seed failed: %v", err)
	}

	// Second run must succeed without error or duplicate keys
	res, err := db.Seed(ctx, gdb)
	if err != nil {
		t.Fatalf("second seed failed: %v", err)
	}

	if len(res.Clusters) != 1 {
		t.Errorf("second run clusters = %d, want 1", len(res.Clusters))
	}

	// Verify no duplicate cluster rows
	var clusterCount int64
	if err := gdb.Model(&computemodel.SlurmCluster{}).Count(&clusterCount).Error; err != nil {
		t.Fatalf("count clusters: %v", err)
	}
	if clusterCount != 1 {
		t.Errorf("cluster count = %d, want 1", clusterCount)
	}

	// Verify no duplicate credentials
	var credCount int64
	if err := gdb.Model(&credentialsmodel.SSHCredential{}).Count(&credCount).Error; err != nil {
		t.Fatalf("count credentials: %v", err)
	}
	if credCount != 1 {
		t.Errorf("credential count = %d, want 1", credCount)
	}

	// Verify no duplicate templates
	var tmplCount int64
	if err := gdb.Model(&applicationmodel.Template{}).Count(&tmplCount).Error; err != nil {
		t.Fatalf("count templates: %v", err)
	}
	if tmplCount != 1 {
		t.Errorf("template count = %d, want 1", tmplCount)
	}

	// Verify no duplicate service accounts
	var saCount int64
	if err := gdb.Model(&iammodel.ServiceAccount{}).Count(&saCount).Error; err != nil {
		t.Fatalf("count service accounts: %v", err)
	}
	if saCount != 1 {
		t.Errorf("service account count = %d, want 1", saCount)
	}

	saID := auth.ServiceAccountIDPrefix + db.DefaultDevServiceAccountName
	var saPermCount int64
	if err := gdb.Model(&iammodel.ServiceAccountPermission{}).Where("user_id = ?", saID).Count(&saPermCount).Error; err != nil {
		t.Fatalf("count service account permissions: %v", err)
	}
	if saPermCount == 0 {
		t.Errorf("expected permissions to be seeded for service account %s", saID)
	}
}

// TestSeedCoversAllConfigEntities verifies all schema tables are consciously evaluated for seeding.
func TestSeedCoversAllConfigEntities(t *testing.T) {
	gdb := newTestDB(t)
	ctx := context.Background()

	if _, err := db.Seed(ctx, gdb); err != nil {
		t.Fatalf("Seed failed: %v", err)
	}

	// Entities that represent runtime execution state or user-uploaded products
	// and should NOT be pre-seeded into a clean development database:
	unseededTables := map[string]string{
		"processes":                        "created when a computation is launched",
		"batch_processes":                  "created when a batch job process is created",
		"batch_process_statuses":           "recorded by scheduler status polling",
		"process_statuses":                 "lifecycle state transitions recorded during run",
		"process_template_input_mappings":  "input parameter values assigned for a specific run",
		"process_template_output_mappings": "output paths assigned for a specific run",
		"data_staging_tasks":               "individual transfer task steps created by workflow engine",
		"job_submission_tasks":             "submission task steps created by workflow engine",
		"job_monitoring_tasks":             "monitoring task steps created by workflow engine",
		"interactive_command_tasks":        "interactive SSH session tasks",
		"task_statuses":                    "history of task status updates",
		"process_workflows":                "temporal/sqlite workflow engine execution records",
		"data_products":                    "registered when user uploads or imports datasets",
		"virtual_data_directories":         "user-created virtual dataset hierarchy",
		"virtual_data_files":               "file nodes in user-created virtual datasets",
		"process_links":                    "runtime secrets for running jobs linking back via linkspan",
		"audit_events":                     "append-only audit log records created when actions happen",
	}

	for _, entity := range db.Entities() {
		stmt := &gorm.Statement{DB: gdb}
		if err := stmt.Parse(entity); err != nil {
			t.Fatalf("failed to parse schema for entity %T: %v", entity, err)
		}
		table := stmt.Schema.Table

		if _, ok := unseededTables[table]; ok {
			continue
		}

		var count int64
		if err := gdb.Table(table).Count(&count).Error; err != nil {
			t.Fatalf("count table %s: %v", table, err)
		}
		if count == 0 {
			t.Errorf("Schema table %q (%T) has 0 rows after db.Seed(). "+
				"When adding new catalogue/configuration models to internal/db/migrate.go, "+
				"accompany them with seeding logic in internal/db/seed.go, "+
				"or add them to unseededTables with an explanation of why they are runtime-only.",
				table, entity)
		}
	}
}

// TestSeedHasNoForeignKeyViolations ensures that the seeded records do not violate
// any foreign key constraint in the schema.
func TestSeedHasNoForeignKeyViolations(t *testing.T) {
	gdb := newTestDB(t)
	ctx := context.Background()

	if _, err := db.Seed(ctx, gdb); err != nil {
		t.Fatalf("Seed failed: %v", err)
	}

	var violations []struct {
		Table  string `gorm:"column:table"`
		RowID  int64  `gorm:"column:rowid"`
		Parent string `gorm:"column:parent"`
		FkID   int64  `gorm:"column:fkid"`
	}
	if err := gdb.Raw("PRAGMA foreign_key_check").Scan(&violations).Error; err != nil {
		t.Fatalf("pragma foreign_key_check failed: %v", err)
	}
	if len(violations) > 0 {
		t.Fatalf("foreign key violations after seed: %+v", violations)
	}
}
