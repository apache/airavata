// Package db wires the entity model to a database connection.
package db

import (
	"gorm.io/gorm"

	applicationmodel "github.com/apache/airavata/api/application/model"
	computemodel "github.com/apache/airavata/api/compute/model"
	credentialsmodel "github.com/apache/airavata/api/credentials/model"
	datamodel "github.com/apache/airavata/api/data/model"
	iammodel "github.com/apache/airavata/api/iam/model"
	processmodel "github.com/apache/airavata/api/process/model"
)

// Entities lists every persistent model, ordered so that a table is always created
// after everything its foreign keys point at. AutoMigrate resolves dependencies on
// its own, but keeping the order explicit makes the reference graph readable and the
// generated DDL deterministic.
func Entities() []any {
	return []any{
		// No outbound references.
		&iammodel.User{},
		&credentialsmodel.SSHKey{},
		&computemodel.SSHEndpoint{},
		&applicationmodel.Template{},
		&applicationmodel.BatchJobConfig{},

		// One level in.
		&iammodel.UserRole{},
		&iammodel.Group{},
		&credentialsmodel.SSHUserCredential{},
		&computemodel.Cluster{},
		&datamodel.SCPDataStorage{},
		&datamodel.DataProduct{},
		&applicationmodel.TemplateInput{},
		&applicationmodel.TemplateOutput{},

		// Depend on the above.
		&iammodel.GroupMember{},
		&computemodel.ClusterPartition{},
		&computemodel.SSHEndpointCredential{},
		&applicationmodel.BatchDeployment{},
		&processmodel.BatchJobProcess{},

		// Sharing rows reference the record they open up, and a group or a user.
		&computemodel.SSHEndpointCredentialGroupSharing{},
		&computemodel.SSHEndpointCredentialUserSharing{},
		&datamodel.SCPDataStorageGroupSharing{},
		&datamodel.SCPDataStorageUserSharing{},
		&datamodel.DataProductGroupSharing{},
		&datamodel.DataProductUserSharing{},

		// References BatchJobProcess, which in turn references it back through
		// LastStatusID — the one circular pair in the schema.
		&processmodel.BatchJobProcessStatus{},

		// The steps of a process. They name their process by id and type rather than
		// through a foreign key, so they are not tied to one kind of process.
		&processmodel.DataStagingTask{},
		&processmodel.JobSubmissionTask{},
		&processmodel.JobMonitoringTask{},
		&processmodel.InteractiveCommandTask{},
	}
}

// AutoMigrate creates or updates the schema to match the entity model. It is the
// counterpart to the Java service's spring.jpa.hibernate.ddl-auto setting.
//
// Like ddl-auto, this is a development convenience: it adds tables, columns and
// indexes but never drops or narrows anything. Production schema changes belong in
// versioned migrations.
func AutoMigrate(gdb *gorm.DB) error {
	return gdb.AutoMigrate(Entities()...)
}
