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

// Package db wires the entity model to a database connection.
package db

import (
	"gorm.io/gorm"

	"github.com/apache/airavata/internal/permission"

	applicationmodel "github.com/apache/airavata/api/application/model"
	auditmodel "github.com/apache/airavata/api/audit/model"
	computemodel "github.com/apache/airavata/api/compute/model"
	credentialsmodel "github.com/apache/airavata/api/credentials/model"
	datamodel "github.com/apache/airavata/api/data/model"
	iammodel "github.com/apache/airavata/api/iam/model"
	processmodel "github.com/apache/airavata/api/process/model"
	sharingmodel "github.com/apache/airavata/api/sharing/model"
)

// Entities lists every persistent model, ordered so that a table is always created
// after everything its foreign keys point at. AutoMigrate resolves dependencies on
// its own, but keeping the order explicit makes the reference graph readable and the
// generated DDL deterministic.
func Entities() []any {
	return []any{
		// No outbound references.
		&iammodel.User{},
		&credentialsmodel.SSHCredential{},
		&credentialsmodel.S3Credential{},
		&applicationmodel.Template{},
		&applicationmodel.BatchJobConfig{},

		// One level in.
		&iammodel.UserRole{},
		&iammodel.RolePermission{},
		&iammodel.ServiceAccount{},
		&iammodel.ServiceAccountPermission{},
		&iammodel.Group{},
		&computemodel.SlurmCluster{},
		&datamodel.SCPDataStorage{},
		&datamodel.DataProduct{},
		&applicationmodel.TemplateInput{},
		&applicationmodel.TemplateOutput{},

		// Depend on the above.
		&iammodel.GroupMember{},
		&computemodel.ClusterPartition{},
		&computemodel.SlurmClusterConfig{},
		&applicationmodel.BatchDeployment{},

		// The nodes of a virtual dataset: a tree of references to registered products.
		// The directory table references itself, so it has to precede the files and the
		// sharing rows that hang off it.
		&datamodel.VirtualDataDirectory{},
		&datamodel.VirtualDataFile{},

		// A run. Everything below in this package hangs off it.
		&processmodel.Process{},

		// Every share in the platform, of any resource and either principal kind, is a
		// row in one table. It references nothing: a column cannot point at four
		// resource tables, nor at users and groups at once, so the services delete a
		// record's shares alongside the record, and the group service withdraws a
		// deleted group's grants.
		&sharingmodel.Sharing{},

		// What a BATCH_JOB run carries beyond a Process. Owned by the process rather
		// than addressable on its own, which is why there is no repository, service or
		// route for it — only a section of the process body.
		&processmodel.BatchJobProcess{},

		// What the scheduler reported about the submitted job, hanging off the section
		// that submitted it.
		&processmodel.BatchJobStatus{},

		// References Process, which in turn references it back through LastStatusID —
		// the one circular pair in the schema.
		&processmodel.ProcessStatus{},

		// The values this run supplies for its template's declared inputs and outputs.
		&processmodel.TemplateInputMapping{},
		&processmodel.TemplateOutputMapping{},

		// The steps of a process, in execution order.
		&processmodel.DataStagingTask{},
		&processmodel.JobSubmissionTask{},
		&processmodel.JobMonitoringTask{},
		&processmodel.InteractiveCommandTask{},

		// The status history of those steps. It references the process rather than a
		// task, since a task id may name a row in any of the four tables above.
		&processmodel.TaskStatus{},

		// The workflow runs started for a process. Like ProcessStatus it is referenced
		// back from Process, through LastWorkflowID, which carries no constraint.
		&processmodel.ProcessWorkflow{},

		// The secrets a process's job is handed so its Linkspan can link back.
		&processmodel.ProcessLink{},

		// The append-only record of what callers did. It references nothing, so it
		// outlives the users and resources it describes.
		&auditmodel.Event{},
	}
}

// AutoMigrate creates or updates the schema to match the entity model. It is the
// counterpart to the Java service's spring.jpa.hibernate.ddl-auto setting.
//
// Like ddl-auto, this is a development convenience: it adds tables, columns and
// indexes but never drops or narrows anything. Production schema changes belong in
// versioned migrations.
func AutoMigrate(gdb *gorm.DB) error {
	if err := gdb.AutoMigrate(Entities()...); err != nil {
		return err
	}
	return seedRolePermissions(gdb)
}

// seedRolePermissions gives a fresh schema the default grants, the way migration
// 0015_role_permissions.sql does for a versioned one.
//
// It seeds only an empty table. Once any grant exists the table is the administrators'
// to edit, and re-inserting the defaults on every start would quietly undo a revoked
// permission. A permission added by a later release reaches an existing database
// through a migration, not through here.
func seedRolePermissions(gdb *gorm.DB) error {
	var existing int64
	if err := gdb.Model(&iammodel.RolePermission{}).Count(&existing).Error; err != nil {
		return err
	}
	if existing > 0 {
		return nil
	}
	var rows []iammodel.RolePermission
	for r, perms := range permission.Defaults() {
		for _, p := range perms {
			rows = append(rows, iammodel.RolePermission{Role: r, Permission: p})
		}
	}
	if len(rows) == 0 {
		return nil
	}
	return gdb.Create(&rows).Error
}
