package unit

import (
	"context"
	"testing"

	"github.com/apache/airavata/scheduler/core/domain"
	services "github.com/apache/airavata/scheduler/core/service"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestVaultService_SpiceDBPermissions(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping unit test in short mode")
	}

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	vault := suite.GetVaultService()
	require.NotNil(t, vault)

	// Get direct access to the mock authorization port for setup
	mockAuthz := suite.VaultService.(*services.VaultService).GetAuthzPort().(*testutil.MockAuthorizationPort)
	mockVault := suite.VaultService.(*services.VaultService).GetVaultPort().(*testutil.MockVaultPort)

	// Test scenarios for SpiceDB-based permissions
	testCases := []struct {
		name       string
		ownerID    string
		userID     string
		shareWith  string
		permission string
		testPerm   string
		canAccess  bool
	}{
		{"owner can read", "owner1", "owner1", "", "read", "read", true},
		{"owner can write", "owner2", "owner2", "", "write", "write", true},
		{"owner can delete", "owner3", "owner3", "", "delete", "delete", true},
		{"non-owner cannot access", "owner4", "user4", "", "read", "read", false},
		{"shared user can read", "owner5", "user5", "user5", "read", "read", true},
		{"shared user can write", "owner6", "user6", "user6", "write", "write", true},
		{"shared user cannot delete", "owner7", "user7", "user7", "read", "delete", false},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			// Create test users
			owner := suite.CreateUser(tc.ownerID, tc.ownerID+"@example.com")
			require.NotNil(t, owner)

			var user *domain.User
			if tc.userID != tc.ownerID {
				user = suite.CreateUser(tc.userID, tc.userID+"@example.com")
				require.NotNil(t, user)
			} else {
				user = owner
			}

			// Create credential directly in mock
			credID := "cred-" + tc.name
			testData := []byte("test-credential-data")
			err := mockVault.StoreCredential(context.Background(), credID, map[string]interface{}{
				"data": testData,
				"type": domain.CredentialTypePassword,
			})
			require.NoError(t, err)

			// Set up ownership in mock
			err = mockAuthz.CreateCredentialOwner(context.Background(), credID, owner.ID)
			require.NoError(t, err)

			// Share credential if needed
			if tc.shareWith != "" {
				// Use the actual user ID, not the string from test case
				shareWithUserID := user.ID
				err = mockAuthz.ShareCredential(context.Background(), credID, shareWithUserID, "user", tc.permission)
				require.NoError(t, err)
			}

			// Test permission check
			canAccess, err := vault.CheckPermission(context.Background(), user.ID, credID, "credential", tc.testPerm)
			require.NoError(t, err)
			assert.Equal(t, tc.canAccess, canAccess, "Permission check failed for %s", tc.name)
		})
	}
}

func TestVaultService_GroupPermissions(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping unit test in short mode")
	}

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	vault := suite.GetVaultService()
	require.NotNil(t, vault)

	mockAuthz := suite.VaultService.(*services.VaultService).GetAuthzPort().(*testutil.MockAuthorizationPort)
	mockVault := suite.VaultService.(*services.VaultService).GetVaultPort().(*testutil.MockVaultPort)

	// Create owner user and group
	owner := suite.CreateUser("group-owner", "group-owner@example.com")
	require.NotNil(t, owner)
	group := suite.CreateGroupWithOwner("test-group", "Test Group", owner.ID)
	require.NotNil(t, group)

	// Create credential directly in mock
	credID := "group-cred"
	testData := []byte("test-credential-data")
	err := mockVault.StoreCredential(context.Background(), credID, map[string]interface{}{
		"data": testData,
		"type": domain.CredentialTypePassword,
	})
	require.NoError(t, err)

	// Set up ownership in mock
	err = mockAuthz.CreateCredentialOwner(context.Background(), credID, owner.ID)
	require.NoError(t, err)

	// Add user to group
	memberUser := suite.CreateUser("group-member", "group-member@example.com")
	require.NotNil(t, memberUser)
	err = mockAuthz.AddUserToGroup(context.Background(), memberUser.ID, group.ID)
	require.NoError(t, err)

	// Share credential with group for read access
	err = mockAuthz.ShareCredential(context.Background(), credID, group.ID, "group", "read")
	require.NoError(t, err)

	// Test cases
	testCases := []struct {
		name       string
		userID     string
		permission string
		expected   bool
	}{
		{"group member can read", memberUser.ID, "read", true},
		{"group member cannot write", memberUser.ID, "write", false},
		{"group member cannot delete", memberUser.ID, "delete", false},
		{"owner can read", owner.ID, "read", true},
		{"owner can write", owner.ID, "write", true},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			canAccess, err := vault.CheckPermission(context.Background(), tc.userID, credID, "credential", tc.permission)
			require.NoError(t, err)
			assert.Equal(t, tc.expected, canAccess)
		})
	}
}

func TestVaultService_ResourceBindings(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping unit test in short mode")
	}

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	vault := suite.GetVaultService()
	require.NotNil(t, vault)

	mockAuthz := suite.VaultService.(*services.VaultService).GetAuthzPort().(*testutil.MockAuthorizationPort)
	mockVault := suite.VaultService.(*services.VaultService).GetVaultPort().(*testutil.MockVaultPort)

	// Create user and resource
	user := suite.CreateUser("resource-binder", "resource-binder@example.com")
	require.NotNil(t, user)

	// Create compute resource
	computeResource := suite.CreateComputeResource("test-compute", "SLURM", user.ID)
	require.NotNil(t, computeResource)

	// Create credential directly in mock
	credID := "resource-cred"
	testData := []byte("test-credential-data")
	err := mockVault.StoreCredential(context.Background(), credID, map[string]interface{}{
		"data": testData,
		"type": domain.CredentialTypeSSHKey,
	})
	require.NoError(t, err)

	// Set up ownership in mock
	err = mockAuthz.CreateCredentialOwner(context.Background(), credID, user.ID)
	require.NoError(t, err)

	// Bind credential to resource
	err = mockAuthz.BindCredentialToResource(context.Background(), credID, computeResource.ID, string(domain.ComputeResourceTypeSlurm))
	require.NoError(t, err)

	// Test cases
	testCases := []struct {
		name          string
		userID        string
		resourceID    string
		resourceType  string
		permission    string
		expectedCreds int
	}{
		{"user can get usable credentials for resource", user.ID, computeResource.ID, string(domain.ComputeResourceTypeSlurm), "read", 1},
		{"another user cannot get usable credentials", "another-user", computeResource.ID, string(domain.ComputeResourceTypeSlurm), "read", 0},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			usableCreds, err := vault.GetUsableCredentialsForResource(context.Background(), tc.userID, tc.resourceID, tc.resourceType, tc.permission)
			require.NoError(t, err)
			assert.Len(t, usableCreds, tc.expectedCreds)
		})
	}
}
