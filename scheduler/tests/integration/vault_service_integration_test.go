package integration

import (
	"context"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestVaultService_StoreAndRetrieveCredential(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	// Check if SpiceDB is properly configured
	testutil.CheckServiceAvailable(t, "spicedb", "localhost:50052")

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for services to be ready
	time.Sleep(5 * time.Second)

	ctx := context.Background()

	t.Run("StoreSSHKeyCredential", func(t *testing.T) {
		sshKeyData := []byte("-----BEGIN OPENSSH PRIVATE KEY-----\nb3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAlwAAAAdzc2gtcn\nNhAAAAAwEAAQAAAIEAv...\n-----END OPENSSH PRIVATE KEY-----")

		credential, err := suite.VaultService.StoreCredential(ctx, "test-ssh-key", domain.CredentialTypeSSHKey, sshKeyData, suite.TestUser.ID)
		require.NoError(t, err)
		assert.NotEmpty(t, credential.ID)
		assert.Equal(t, "test-ssh-key", credential.Name)
		assert.Equal(t, domain.CredentialTypeSSHKey, credential.Type)
		assert.Equal(t, suite.TestUser.ID, credential.OwnerID)

		// Wait for SpiceDB consistency
		testutil.WaitForSpiceDBConsistency(t, func() bool {
			time.Sleep(100 * time.Millisecond)
			return true
		}, 5*time.Second)

		// Retrieve the credential
		retrievedCredential, retrievedData, err := suite.VaultService.RetrieveCredential(ctx, credential.ID, suite.TestUser.ID)
		require.NoError(t, err)
		assert.Equal(t, credential.ID, retrievedCredential.ID)
		assert.Equal(t, credential.Name, retrievedCredential.Name)
		assert.Equal(t, credential.Type, retrievedCredential.Type)
		assert.Equal(t, sshKeyData, retrievedData)
	})

	t.Run("StoreAPITokenCredential", func(t *testing.T) {
		tokenData := []byte("sk-1234567890abcdef")

		credential, err := suite.VaultService.StoreCredential(ctx, "test-api-token", domain.CredentialTypeToken, tokenData, suite.TestUser.ID)
		require.NoError(t, err)
		assert.NotEmpty(t, credential.ID)
		assert.Equal(t, "test-api-token", credential.Name)
		assert.Equal(t, domain.CredentialTypeToken, credential.Type)
		assert.Equal(t, suite.TestUser.ID, credential.OwnerID)

		// Wait for SpiceDB consistency
		testutil.WaitForSpiceDBConsistency(t, func() bool {
			time.Sleep(100 * time.Millisecond)
			return true
		}, 5*time.Second)

		// Retrieve the credential
		retrievedCredential, retrievedData, err := suite.VaultService.RetrieveCredential(ctx, credential.ID, suite.TestUser.ID)
		require.NoError(t, err)
		assert.Equal(t, credential.ID, retrievedCredential.ID)
		assert.Equal(t, tokenData, retrievedData)
	})
}

func TestVaultService_UpdateCredential(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for services to be ready
	time.Sleep(5 * time.Second)

	ctx := context.Background()

	// Store initial credential
	initialData := []byte("initial-password")
	credential, err := suite.VaultService.StoreCredential(ctx, "test-update", domain.CredentialTypePassword, initialData, suite.TestUser.ID)
	require.NoError(t, err)

	// Wait for SpiceDB consistency
	testutil.WaitForSpiceDBConsistency(t, func() bool {
		time.Sleep(100 * time.Millisecond)
		return true
	}, 5*time.Second)

	// Update credential data
	updatedData := []byte("updated-password")
	updatedCredential, err := suite.VaultService.UpdateCredential(ctx, credential.ID, updatedData, suite.TestUser.ID)
	require.NoError(t, err)
	assert.Equal(t, credential.ID, updatedCredential.ID)
	assert.Equal(t, credential.Name, updatedCredential.Name)
	assert.True(t, updatedCredential.UpdatedAt.After(credential.UpdatedAt))

	// Wait for SpiceDB consistency
	testutil.WaitForSpiceDBConsistency(t, func() bool {
		time.Sleep(100 * time.Millisecond)
		return true
	}, 5*time.Second)

	// Verify updated data
	retrievedCredential, retrievedData, err := suite.VaultService.RetrieveCredential(ctx, credential.ID, suite.TestUser.ID)
	require.NoError(t, err)
	assert.Equal(t, updatedData, retrievedData)
	assert.True(t, retrievedCredential.UpdatedAt.After(credential.UpdatedAt))
}

func TestVaultService_DeleteCredential(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for services to be ready
	time.Sleep(5 * time.Second)

	ctx := context.Background()

	// Store credential
	credentialData := []byte("credential-to-delete")
	credential, err := suite.VaultService.StoreCredential(ctx, "test-delete", domain.CredentialTypePassword, credentialData, suite.TestUser.ID)
	require.NoError(t, err)

	// Verify credential exists
	_, _, err = suite.VaultService.RetrieveCredential(ctx, credential.ID, suite.TestUser.ID)
	require.NoError(t, err)

	// Delete credential
	err = suite.VaultService.DeleteCredential(ctx, credential.ID, suite.TestUser.ID)
	require.NoError(t, err)

	// Verify credential is deleted
	_, _, err = suite.VaultService.RetrieveCredential(ctx, credential.ID, suite.TestUser.ID)
	require.Error(t, err)
	assert.Contains(t, err.Error(), "not found")
}

func TestVaultService_ListCredentials(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for services to be ready
	time.Sleep(5 * time.Second)

	ctx := context.Background()

	// Store multiple credentials
	credential1, err := suite.VaultService.StoreCredential(ctx, "credential-1", domain.CredentialTypeSSHKey, []byte("data1"), suite.TestUser.ID)
	require.NoError(t, err)

	credential2, err := suite.VaultService.StoreCredential(ctx, "credential-2", domain.CredentialTypeToken, []byte("data2"), suite.TestUser.ID)
	require.NoError(t, err)

	credential3, err := suite.VaultService.StoreCredential(ctx, "credential-3", domain.CredentialTypePassword, []byte("data3"), suite.TestUser.ID)
	require.NoError(t, err)

	// Wait for SpiceDB consistency
	testutil.WaitForSpiceDBConsistency(t, func() bool {
		// Simple check - just wait a bit for relationships to propagate
		time.Sleep(100 * time.Millisecond)
		return true
	}, 5*time.Second)

	// List credentials
	credentials, err := suite.VaultService.ListCredentials(ctx, suite.TestUser.ID)
	require.NoError(t, err)
	assert.Len(t, credentials, 3)

	// Verify all credentials are present
	credentialIDs := make(map[string]bool)
	for _, cred := range credentials {
		credentialIDs[cred.ID] = true
	}
	assert.True(t, credentialIDs[credential1.ID])
	assert.True(t, credentialIDs[credential2.ID])
	assert.True(t, credentialIDs[credential3.ID])
}

func TestVaultService_ShareCredential(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for services to be ready
	time.Sleep(5 * time.Second)

	ctx := context.Background()

	// Create another user
	otherUser, err := suite.Builder.CreateUser("other-user", "other@example.com", false).Build()
	require.NoError(t, err)

	// Store credential
	credentialData := []byte("shared-credential")
	credential, err := suite.VaultService.StoreCredential(ctx, "test-share", domain.CredentialTypePassword, credentialData, suite.TestUser.ID)
	require.NoError(t, err)

	// Wait for SpiceDB consistency
	testutil.WaitForSpiceDBConsistency(t, func() bool {
		time.Sleep(100 * time.Millisecond)
		return true
	}, 5*time.Second)

	// Share credential with other user
	err = suite.VaultService.ShareCredential(ctx, credential.ID, otherUser.ID, "", "r", suite.TestUser.ID)
	require.NoError(t, err)

	// Wait for SpiceDB consistency
	testutil.WaitForSpiceDBConsistency(t, func() bool {
		// Simple check - just wait a bit for relationships to propagate
		time.Sleep(100 * time.Millisecond)
		return true
	}, 5*time.Second)

	// Verify other user can access the credential
	retrievedCredential, retrievedData, err := suite.VaultService.RetrieveCredential(ctx, credential.ID, otherUser.ID)
	require.NoError(t, err)
	assert.Equal(t, credential.ID, retrievedCredential.ID)
	assert.Equal(t, credentialData, retrievedData)
}

func TestVaultService_UnshareCredential(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for services to be ready
	time.Sleep(5 * time.Second)

	ctx := context.Background()

	// Create another user
	otherUser, err := suite.Builder.CreateUser("other-user", "other@example.com", false).Build()
	require.NoError(t, err)

	// Store credential
	credentialData := []byte("shared-credential")
	credential, err := suite.VaultService.StoreCredential(ctx, "test-unshare", domain.CredentialTypePassword, credentialData, suite.TestUser.ID)
	require.NoError(t, err)

	// Share credential with other user
	err = suite.VaultService.ShareCredential(ctx, credential.ID, otherUser.ID, "", "r", suite.TestUser.ID)
	require.NoError(t, err)

	// Verify other user can access the credential
	_, _, err = suite.VaultService.RetrieveCredential(ctx, credential.ID, otherUser.ID)
	require.NoError(t, err)

	// Revoke access
	err = suite.VaultService.RevokeCredentialAccess(ctx, credential.ID, otherUser.ID, "", suite.TestUser.ID)
	require.NoError(t, err)

	// Verify other user can no longer access the credential
	_, _, err = suite.VaultService.RetrieveCredential(ctx, credential.ID, otherUser.ID)
	require.Error(t, err)
	assert.Contains(t, err.Error(), "permission")
}

func TestVaultService_GetUsableCredentialForResource(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for services to be ready
	time.Sleep(5 * time.Second)

	ctx := context.Background()

	// Create a compute resource
	computeReq := &domain.CreateComputeResourceRequest{
		Name:        "test-compute",
		Type:        domain.ComputeResourceTypeSlurm,
		Endpoint:    "slurm.example.com:6817",
		CostPerHour: 0.5,
		MaxWorkers:  10,
		OwnerID:     suite.TestUser.ID,
	}

	computeResp, err := suite.RegistryService.RegisterComputeResource(ctx, computeReq)
	require.NoError(t, err)

	// Store credential
	credentialData := []byte("resource-credential")
	credential, err := suite.VaultService.StoreCredential(ctx, "test-bind", domain.CredentialTypeSSHKey, credentialData, suite.TestUser.ID)
	require.NoError(t, err)

	// Get usable credential for resource
	usableCredential, usableData, err := suite.VaultService.GetUsableCredentialForResource(ctx, computeResp.Resource.ID, "compute_resource", suite.TestUser.ID, map[string]interface{}{
		"credential_id": credential.ID,
	})
	require.NoError(t, err)
	assert.Equal(t, credential.ID, usableCredential.ID)
	assert.Equal(t, credentialData, usableData)
}

func TestVaultService_CredentialLifecycle(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for services to be ready
	time.Sleep(5 * time.Second)

	ctx := context.Background()

	// Create another user
	otherUser, err := suite.Builder.CreateUser("other-user", "other@example.com", false).Build()
	require.NoError(t, err)

	// Create a compute resource
	computeReq := &domain.CreateComputeResourceRequest{
		Name:        "test-compute",
		Type:        domain.ComputeResourceTypeSlurm,
		Endpoint:    "slurm.example.com:6817",
		CostPerHour: 0.5,
		MaxWorkers:  10,
		OwnerID:     suite.TestUser.ID,
	}

	computeResp, err := suite.RegistryService.RegisterComputeResource(ctx, computeReq)
	require.NoError(t, err)

	// 1. Store credential
	initialData := []byte("lifecycle-credential")
	credential, err := suite.VaultService.StoreCredential(ctx, "test-lifecycle", domain.CredentialTypeSSHKey, initialData, suite.TestUser.ID)
	require.NoError(t, err)
	assert.NotEmpty(t, credential.ID)

	// 2. Retrieve credential
	_, retrievedData, err := suite.VaultService.RetrieveCredential(ctx, credential.ID, suite.TestUser.ID)
	require.NoError(t, err)
	assert.Equal(t, initialData, retrievedData)

	// 3. Update credential
	updatedData := []byte("updated-lifecycle-credential")
	updatedCredential, err := suite.VaultService.UpdateCredential(ctx, credential.ID, updatedData, suite.TestUser.ID)
	require.NoError(t, err)
	assert.True(t, updatedCredential.UpdatedAt.After(credential.UpdatedAt))

	// 4. Share credential
	err = suite.VaultService.ShareCredential(ctx, credential.ID, otherUser.ID, "", "r", suite.TestUser.ID)
	require.NoError(t, err)

	// 5. Verify other user can access
	_, _, err = suite.VaultService.RetrieveCredential(ctx, credential.ID, otherUser.ID)
	require.NoError(t, err)

	// 6. Get usable credential for resource
	_, _, err = suite.VaultService.GetUsableCredentialForResource(ctx, computeResp.Resource.ID, "compute_resource", suite.TestUser.ID, map[string]interface{}{
		"credential_id": credential.ID,
	})
	require.NoError(t, err)

	// 7. Revoke access
	err = suite.VaultService.RevokeCredentialAccess(ctx, credential.ID, otherUser.ID, "", suite.TestUser.ID)
	require.NoError(t, err)

	// 8. Verify other user can no longer access
	_, _, err = suite.VaultService.RetrieveCredential(ctx, credential.ID, otherUser.ID)
	require.Error(t, err)

	// 9. Delete credential
	err = suite.VaultService.DeleteCredential(ctx, credential.ID, suite.TestUser.ID)
	require.NoError(t, err)

	// 10. Verify credential is deleted
	_, _, err = suite.VaultService.RetrieveCredential(ctx, credential.ID, suite.TestUser.ID)
	require.Error(t, err)
	assert.Contains(t, err.Error(), "not found")
}
