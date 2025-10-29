package integration

import (
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestCredentialACL_UnixPermissions(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Create users with UID/GID
	owner, err := suite.CreateUser("owner", 1001, 1001)
	require.NoError(t, err)
	assert.NotNil(t, owner)

	groupMember, err := suite.CreateUser("member", 1002, 1001)
	require.NoError(t, err)
	assert.NotNil(t, groupMember)

	otherUser, err := suite.CreateUser("other", 1003, 1003)
	require.NoError(t, err)
	assert.NotNil(t, otherUser)

	// Create credential with Unix permissions: rw-r-----
	cred, err := suite.CreateCredential("test-cred", owner.ID)
	require.NoError(t, err)
	assert.NotNil(t, cred)

	cred.OwnerUID = 1001
	cred.GroupGID = 1001
	cred.Permissions = "rw-r-----"
	err = suite.UpdateCredential(cred)
	require.NoError(t, err)

	// Test owner can access
	accessible := suite.CheckCredentialAccess(cred.ID, owner.ID, "r")
	assert.True(t, accessible)

	accessible = suite.CheckCredentialAccess(cred.ID, owner.ID, "w")
	assert.True(t, accessible)

	// Test group member can read but not write
	accessible = suite.CheckCredentialAccess(cred.ID, groupMember.ID, "r")
	assert.True(t, accessible)

	accessible = suite.CheckCredentialAccess(cred.ID, groupMember.ID, "w")
	assert.False(t, accessible)

	// Test other user cannot access
	accessible = suite.CheckCredentialAccess(cred.ID, otherUser.ID, "r")
	assert.False(t, accessible)

	accessible = suite.CheckCredentialAccess(cred.ID, otherUser.ID, "w")
	assert.False(t, accessible)
}

func TestCredentialACL_HierarchicalGroups(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Create group hierarchy: parentGroup -> childGroup -> user
	parentGroup, err := suite.CreateGroup("parent-group")
	require.NoError(t, err)
	assert.NotNil(t, parentGroup)

	childGroup, err := suite.CreateGroup("child-group")
	require.NoError(t, err)
	assert.NotNil(t, childGroup)

	user, err := suite.CreateUser("test-user", 1001, 1001)
	require.NoError(t, err)
	assert.NotNil(t, user)

	// Add user to child group
	err = suite.AddUserToGroup(user.ID, childGroup.ID)
	require.NoError(t, err)

	// Add child group to parent group
	err = suite.AddGroupToGroup(childGroup.ID, parentGroup.ID)
	require.NoError(t, err)

	// Create credential with ACL for parent group
	cred, err := suite.CreateCredential("test-cred", "admin")
	require.NoError(t, err)
	assert.NotNil(t, cred)

	err = suite.AddCredentialACL(cred.ID, "GROUP", parentGroup.ID, "r--")
	require.NoError(t, err)

	// Verify user can access through hierarchy
	accessible := suite.CheckCredentialAccess(cred.ID, user.ID, "r")
	assert.True(t, accessible)

	// Verify user cannot write (only read permission)
	accessible = suite.CheckCredentialAccess(cred.ID, user.ID, "w")
	assert.False(t, accessible)
}

func TestCredentialResourceBinding_ExperimentExecution(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Create user and credential
	user, err := suite.CreateUser("exp-user", 1001, 1001)
	require.NoError(t, err)
	assert.NotNil(t, user)

	cred, err := suite.CreateCredential("slurm-cred", user.ID)
	require.NoError(t, err)
	assert.NotNil(t, cred)

	// Register compute and storage resources
	compute, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)
	assert.NotNil(t, compute)

	storage, err := suite.RegisterS3Storage("test-bucket", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, storage)

	// Bind credential to resources
	err = suite.BindCredentialToResource(cred.ID, "compute_resource", compute.ID)
	require.NoError(t, err)

	err = suite.BindCredentialToResource(cred.ID, "storage_resource", storage.ID)
	require.NoError(t, err)

	// Create experiment as user
	exp, err := suite.CreateExperimentAsUser(user.ID, "test-exp", "echo test")
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Verify credential is resolved and used
	task, err := suite.GetFirstTask(exp.ID)
	require.NoError(t, err)

	err = suite.WaitForTaskCompletion(task.ID, 30*time.Second)
	require.NoError(t, err)

	completedTask, err := suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, completedTask.Status)
}

func TestCredentialACL_ExplicitDeny(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Create user and group
	user, err := suite.CreateUser("test-user", 1001, 1001)
	require.NoError(t, err)
	assert.NotNil(t, user)

	group, err := suite.CreateGroup("test-group")
	require.NoError(t, err)
	assert.NotNil(t, group)

	// Add user to group
	err = suite.AddUserToGroup(user.ID, group.ID)
	require.NoError(t, err)

	// Create credential with group read permission
	cred, err := suite.CreateCredential("test-cred", "admin")
	require.NoError(t, err)
	assert.NotNil(t, cred)

	cred.GroupGID = 1001
	cred.Permissions = "rw-r-----"
	err = suite.UpdateCredential(cred)
	require.NoError(t, err)

	// Add explicit deny ACL for the user
	err = suite.AddCredentialACL(cred.ID, "USER", user.ID, "---")
	require.NoError(t, err)

	// Verify user cannot access despite group membership
	accessible := suite.CheckCredentialAccess(cred.ID, user.ID, "r")
	assert.False(t, accessible)

	accessible = suite.CheckCredentialAccess(cred.ID, user.ID, "w")
	assert.False(t, accessible)
}

func TestCredentialACL_ComplexHierarchy(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Create complex hierarchy: rootGroup -> midGroup -> leafGroup -> user
	rootGroup, err := suite.CreateGroup("root-group")
	require.NoError(t, err)
	assert.NotNil(t, rootGroup)

	midGroup, err := suite.CreateGroup("mid-group")
	require.NoError(t, err)
	assert.NotNil(t, midGroup)

	leafGroup, err := suite.CreateGroup("leaf-group")
	require.NoError(t, err)
	assert.NotNil(t, leafGroup)

	user, err := suite.CreateUser("test-user", 1001, 1001)
	require.NoError(t, err)
	assert.NotNil(t, user)

	// Build hierarchy: root -> mid -> leaf -> user
	err = suite.AddGroupToGroup(midGroup.ID, rootGroup.ID)
	require.NoError(t, err)

	err = suite.AddGroupToGroup(leafGroup.ID, midGroup.ID)
	require.NoError(t, err)

	err = suite.AddUserToGroup(user.ID, leafGroup.ID)
	require.NoError(t, err)

	// Create credential with ACL for root group
	cred, err := suite.CreateCredential("test-cred", "admin")
	require.NoError(t, err)
	assert.NotNil(t, cred)

	err = suite.AddCredentialACL(cred.ID, "GROUP", rootGroup.ID, "rw-")
	require.NoError(t, err)

	// Verify user can access through complex hierarchy
	accessible := suite.CheckCredentialAccess(cred.ID, user.ID, "r")
	assert.True(t, accessible)

	accessible = suite.CheckCredentialAccess(cred.ID, user.ID, "w")
	assert.True(t, accessible)
}

func TestCredentialACL_MultipleGroups(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Create user and multiple groups
	user, err := suite.CreateUser("test-user", 1001, 1001)
	require.NoError(t, err)
	assert.NotNil(t, user)

	group1, err := suite.CreateGroup("group-1")
	require.NoError(t, err)
	assert.NotNil(t, group1)

	group2, err := suite.CreateGroup("group-2")
	require.NoError(t, err)
	assert.NotNil(t, group2)

	// Add user to both groups
	err = suite.AddUserToGroup(user.ID, group1.ID)
	require.NoError(t, err)

	err = suite.AddUserToGroup(user.ID, group2.ID)
	require.NoError(t, err)

	// Create credential with ACL for group1 (read) and group2 (write)
	cred, err := suite.CreateCredential("test-cred", "admin")
	require.NoError(t, err)
	assert.NotNil(t, cred)

	err = suite.AddCredentialACL(cred.ID, "GROUP", group1.ID, "r--")
	require.NoError(t, err)

	err = suite.AddCredentialACL(cred.ID, "GROUP", group2.ID, "-w-")
	require.NoError(t, err)

	// Verify user has both read and write access
	accessible := suite.CheckCredentialAccess(cred.ID, user.ID, "r")
	assert.True(t, accessible)

	accessible = suite.CheckCredentialAccess(cred.ID, user.ID, "w")
	assert.True(t, accessible)
}

func TestCredentialACL_ResourceSpecificAccess(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
		{"SFTP", checker.CheckSFTPService},
		{"MinIO", checker.CheckMinIOService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Create user
	user, err := suite.CreateUser("test-user", 1001, 1001)
	require.NoError(t, err)
	assert.NotNil(t, user)

	// Create credentials for different resources
	slurmCred1, err := suite.CreateCredential("slurm-cred-1", user.ID)
	require.NoError(t, err)
	assert.NotNil(t, slurmCred1)

	slurmCred2, err := suite.CreateCredential("slurm-cred-2", user.ID)
	require.NoError(t, err)
	assert.NotNil(t, slurmCred2)

	storageCred, err := suite.CreateCredential("storage-cred", user.ID)
	require.NoError(t, err)
	assert.NotNil(t, storageCred)

	// Register resources
	compute1, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)
	assert.NotNil(t, compute1)

	compute2, err := suite.RegisterSlurmResource("cluster-2", "localhost:6819")
	require.NoError(t, err)
	assert.NotNil(t, compute2)

	storage, err := suite.RegisterS3Storage("test-bucket", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, storage)

	// Bind credentials to specific resources
	err = suite.BindCredentialToResource(slurmCred1.ID, "compute_resource", compute1.ID)
	require.NoError(t, err)

	err = suite.BindCredentialToResource(slurmCred2.ID, "compute_resource", compute2.ID)
	require.NoError(t, err)

	err = suite.BindCredentialToResource(storageCred.ID, "storage_resource", storage.ID)
	require.NoError(t, err)

	// Test credential resolution for specific resources
	cred1, err := suite.GetUsableCredentialForResource(compute1.ID, "compute_resource", user.ID, "r")
	require.NoError(t, err)
	assert.Equal(t, slurmCred1.ID, cred1.ID)

	cred2, err := suite.GetUsableCredentialForResource(compute2.ID, "compute_resource", user.ID, "r")
	require.NoError(t, err)
	assert.Equal(t, slurmCred2.ID, cred2.ID)

	cred3, err := suite.GetUsableCredentialForResource(storage.ID, "storage_resource", user.ID, "r")
	require.NoError(t, err)
	assert.Equal(t, storageCred.ID, cred3.ID)
}
