package db_test

import (
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/ptr"

	applicationmodel "github.com/apache/airavata/api/application/model"
	computemodel "github.com/apache/airavata/api/compute/model"
	credentialsmodel "github.com/apache/airavata/api/credentials/model"
	datamodel "github.com/apache/airavata/api/data/model"
	iammodel "github.com/apache/airavata/api/iam/model"
	processmodel "github.com/apache/airavata/api/process/model"
)

// newTestDB brings up an in-memory SQLite database with the full schema.
//
// Production runs on MariaDB, so this does not verify the exact DDL that dialect
// emits. What it does verify is everything above the dialect: that the struct tags
// parse, that every association resolves to a real column, that the hooks fire, and
// that the ownership cascades behave as the JPA model did.
func newTestDB(t *testing.T) *gorm.DB {
	t.Helper()

	gdb, err := gorm.Open(sqlite.Open("file::memory:?_pragma=foreign_keys(1)"), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatalf("automigrate: %v", err)
	}
	return gdb
}

func TestAutoMigrateCreatesEveryTable(t *testing.T) {
	gdb := newTestDB(t)

	want := []string{
		"users", "user_roles",
		"ssh_keys", "ssh_user_credentials",
		"clusters", "cluster_partitions", "cluster_credentials",
		"application_templates", "application_template_inputs", "application_template_outputs",
		"batch_application_deployments", "batch_job_configs",
		"scp_data", "batch_job_processes",
	}
	for _, table := range want {
		if !gdb.Migrator().HasTable(table) {
			t.Errorf("table %q was not created", table)
		}
	}
}

func TestBeforeCreateGeneratesUUID(t *testing.T) {
	gdb := newTestDB(t)

	cluster := &computemodel.Cluster{ClusterName: "expanse", HostName: "login.expanse.edu", SlurmHome: "/usr/bin"}
	if err := gdb.Create(cluster).Error; err != nil {
		t.Fatalf("create cluster: %v", err)
	}
	if len(cluster.ID) != 36 {
		t.Errorf("ClusterID = %q, want a generated UUID", cluster.ID)
	}
}

// A user's id is supplied by the caller — CILogon subjects and system UUIDs both
// arrive from outside — so it must survive unchanged rather than being regenerated.
func TestUserIDIsNotGenerated(t *testing.T) {
	gdb := newTestDB(t)

	user := &iammodel.User{ID: "cilogon:12345", CreatedAt: 1_700_000_000_000}
	if err := gdb.Create(user).Error; err != nil {
		t.Fatalf("create user: %v", err)
	}

	var got iammodel.User
	if err := gdb.First(&got, "user_id = ?", "cilogon:12345").Error; err != nil {
		t.Fatalf("reload user: %v", err)
	}
	if got.ID != "cilogon:12345" {
		t.Errorf("UserID = %q, want it preserved verbatim", got.ID)
	}
}

// Roles are owned by the user: deleting the user must take its role rows with it.
func TestDeletingUserCascadesToRoles(t *testing.T) {
	gdb := newTestDB(t)

	user := &iammodel.User{
		ID:        "cilogon:99",
		CreatedAt: 1,
		Roles: []iammodel.UserRole{
			{UserID: "cilogon:99", Role: iammodel.RoleAdmin},
			{UserID: "cilogon:99", Role: iammodel.RoleUser},
		},
	}
	if err := gdb.Create(user).Error; err != nil {
		t.Fatalf("create user with roles: %v", err)
	}

	if err := gdb.Delete(&iammodel.User{}, "user_id = ?", "cilogon:99").Error; err != nil {
		t.Fatalf("delete user: %v", err)
	}

	var remaining int64
	gdb.Model(&iammodel.UserRole{}).Where("user_id = ?", "cilogon:99").Count(&remaining)
	if remaining != 0 {
		t.Errorf("%d role rows survived the user delete, want 0", remaining)
	}
}

// Partitions are owned by their cluster and have no meaning without it.
func TestDeletingClusterCascadesToPartitions(t *testing.T) {
	gdb := newTestDB(t)

	cluster := &computemodel.Cluster{ClusterName: "anvil", HostName: "anvil.rcac.edu", SlurmHome: "/usr/bin"}
	if err := gdb.Create(cluster).Error; err != nil {
		t.Fatalf("create cluster: %v", err)
	}
	part := &computemodel.ClusterPartition{
		ClusterID: &cluster.ID,
		Name:      "wholenode",
		MaxNodes:  ptr.To(int32(16)),
	}
	if err := gdb.Create(part).Error; err != nil {
		t.Fatalf("create partition: %v", err)
	}

	if err := gdb.Delete(&computemodel.Cluster{}, "cluster_id = ?", cluster.ID).Error; err != nil {
		t.Fatalf("delete cluster: %v", err)
	}

	var remaining int64
	gdb.Model(&computemodel.ClusterPartition{}).Where("cluster_id = ?", cluster.ID).Count(&remaining)
	if remaining != 0 {
		t.Errorf("%d partitions survived the cluster delete, want 0", remaining)
	}
}

// The foreign key points from the deployment to its config, so the database cannot
// cascade in that direction; AfterDelete has to clean up the orphan.
func TestDeletingDeploymentRemovesOwnedBatchJobConfig(t *testing.T) {
	gdb := newTestDB(t)

	key := &credentialsmodel.SSHKey{SSHKeyName: "k", PublicKey: "ssh-ed25519 AAAA", PrivateKey: "secret"}
	if err := gdb.Create(key).Error; err != nil {
		t.Fatalf("create ssh key: %v", err)
	}
	cred := &credentialsmodel.SSHUserCredential{Username: "dimuthu", SSHKeyID: &key.ID}
	if err := gdb.Create(cred).Error; err != nil {
		t.Fatalf("create ssh credential: %v", err)
	}
	cluster := &computemodel.Cluster{ClusterName: "anvil", HostName: "anvil.rcac.edu", SlurmHome: "/usr/bin"}
	if err := gdb.Create(cluster).Error; err != nil {
		t.Fatalf("create cluster: %v", err)
	}
	binding := &computemodel.ClusterCredential{ClusterID: &cluster.ID, SSHCredentialID: &cred.ID}
	if err := gdb.Create(binding).Error; err != nil {
		t.Fatalf("create cluster credential: %v", err)
	}
	tmpl := &applicationmodel.Template{TemplateName: ptr.To("gromacs")}
	if err := gdb.Create(tmpl).Error; err != nil {
		t.Fatalf("create template: %v", err)
	}
	cfg := &applicationmodel.BatchJobConfig{WallTimeMinutes: 60, Allocation: "TG-ABC123"}
	if err := gdb.Create(cfg).Error; err != nil {
		t.Fatalf("create batch job config: %v", err)
	}

	dep := &applicationmodel.BatchDeployment{
		TemplateID:                    &tmpl.ID,
		SlurmRunSection:               "module load gromacs\ngmx mdrun",
		BatchJobConfigID:              cfg.ID,
		DefaultSubmissionCredentialID: binding.ID,
	}
	if err := gdb.Create(dep).Error; err != nil {
		t.Fatalf("create deployment: %v", err)
	}

	if err := gdb.Delete(dep).Error; err != nil {
		t.Fatalf("delete deployment: %v", err)
	}

	var remaining int64
	gdb.Model(&applicationmodel.BatchJobConfig{}).Where("batch_job_config_id = ?", cfg.ID).Count(&remaining)
	if remaining != 0 {
		t.Errorf("owned batch job config survived the deployment delete")
	}
}

// The Java @UniqueConstraint named a column that the naming strategy never produced,
// so duplicate input names were silently accepted. Under the normalised schema the
// constraint is live.
func TestTemplateInputNameIsUniquePerTemplate(t *testing.T) {
	gdb := newTestDB(t)

	tmpl := &applicationmodel.Template{TemplateName: ptr.To("namd")}
	if err := gdb.Create(tmpl).Error; err != nil {
		t.Fatalf("create template: %v", err)
	}

	first := &applicationmodel.TemplateInput{
		TemplateID: &tmpl.ID,
		InputName:  ptr.To("config"),
		InputType:  ptr.To(applicationmodel.TemplateInputTypeFile),
		IsRequired: true,
	}
	if err := gdb.Create(first).Error; err != nil {
		t.Fatalf("create first input: %v", err)
	}

	dup := &applicationmodel.TemplateInput{
		TemplateID: &tmpl.ID,
		InputName:  ptr.To("config"),
		InputType:  ptr.To(applicationmodel.TemplateInputTypeFile),
	}
	if err := gdb.Create(dup).Error; err == nil {
		t.Error("duplicate input name within a template was accepted, want a uniqueness error")
	}
}

// Nullable columns are pointers so that "unset" stays distinct from the zero value —
// an unset MaxNodes is not a limit of zero.
func TestNullableColumnsRoundTripAsNil(t *testing.T) {
	gdb := newTestDB(t)

	cluster := &computemodel.Cluster{ClusterName: "delta", HostName: "delta.ncsa.edu", SlurmHome: "/usr/bin"}
	if err := gdb.Create(cluster).Error; err != nil {
		t.Fatalf("create cluster: %v", err)
	}
	part := &computemodel.ClusterPartition{ClusterID: &cluster.ID, Name: "cpu"}
	if err := gdb.Create(part).Error; err != nil {
		t.Fatalf("create partition: %v", err)
	}

	var got computemodel.ClusterPartition
	if err := gdb.First(&got, "partition_id = ?", part.ID).Error; err != nil {
		t.Fatalf("reload partition: %v", err)
	}
	if got.MaxNodes != nil {
		t.Errorf("MaxNodes = %v, want nil for an undeclared limit", *got.MaxNodes)
	}
	if got.Description != nil {
		t.Errorf("Description = %q, want nil rather than an empty string", *got.Description)
	}
}

// Ownership drives authorisation on credentials, datasets and processes, so the
// helpers must not treat a missing owner as a match for the empty principal.
func TestOwnedByRejectsUnownedRows(t *testing.T) {
	cred := &computemodel.ClusterCredential{}
	if cred.OwnedBy("") {
		t.Error("a credential with no owner reported ownership by the empty user id")
	}
	cred.OwnerID = ptr.To("cilogon:1")
	if !cred.OwnedBy("cilogon:1") {
		t.Error("owner was not recognised")
	}
	if cred.OwnedBy("cilogon:2") {
		t.Error("a different user was reported as the owner")
	}

	dataset := &datamodel.SCPData{}
	if dataset.OwnedBy("") {
		t.Error("a dataset with no owner reported ownership by the empty user id")
	}

	proc := &processmodel.BatchJobProcess{}
	if proc.OwnedBy("") {
		t.Error("a process with no user reported ownership by the empty user id")
	}
}
