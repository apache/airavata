// Package db wires the entity model to a database connection.
package db

import (
	"gorm.io/gorm"

	applicationmodel "github.com/apache/airavata/api/application/model"
	computemodel "github.com/apache/airavata/api/compute/model"
	credentialsmodel "github.com/apache/airavata/api/credentials/model"
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
		&computemodel.Cluster{},
		&applicationmodel.Template{},
		&applicationmodel.BatchJobConfig{},

		// One level in.
		&iammodel.UserRole{},
		&iammodel.Group{},
		&credentialsmodel.SSHUserCredential{},
		&computemodel.ClusterPartition{},
		&applicationmodel.TemplateInput{},
		&applicationmodel.TemplateOutput{},

		// Depend on the above.
		&iammodel.GroupMember{},
		&computemodel.ClusterCredential{},
		&applicationmodel.BatchDeployment{},
		&processmodel.BatchJobProcess{},

		// References BatchJobProcess, which in turn references it back through
		// LastStatusID — the one circular pair in the schema.
		&processmodel.BatchJobProcessStatus{},
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
