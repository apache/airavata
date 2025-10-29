package unit

import (
	"context"
	"testing"

	"github.com/apache/airavata/scheduler/adapters"
	"github.com/apache/airavata/scheduler/core/domain"
	services "github.com/apache/airavata/scheduler/core/service"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestVault_StoreCredential(t *testing.T) {
	db := testutil.SetupFreshPostgresTestDB(t, "")
	defer db.Cleanup()

	// Create services
	eventPort := adapters.NewInMemoryEventAdapter()
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	mockVault := testutil.NewMockVaultPort()
	mockAuthz := testutil.NewMockAuthorizationPort()
	vaultService := services.NewVaultService(mockVault, mockAuthz, securityPort, eventPort)

	// Create test user
	builder := testutil.NewTestDataBuilder(db.DB)
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)

	// Store SSH credential
	sshKeys, err := testutil.GenerateSSHKeys()
	require.NoError(t, err)
	defer sshKeys.Cleanup()

	credResp, err := vaultService.StoreCredential(
		context.Background(),
		"test-ssh-key",
		domain.CredentialTypeSSHKey,
		sshKeys.GetPrivateKey(),
		user.ID,
	)
	require.NoError(t, err)
	assert.NotNil(t, credResp)
	assert.Equal(t, "test-ssh-key", credResp.Name)
	assert.Equal(t, domain.CredentialTypeSSHKey, credResp.Type)
	assert.Equal(t, user.ID, credResp.OwnerID)
}

func TestVault_RetrieveCredential(t *testing.T) {
	db := testutil.SetupFreshPostgresTestDB(t, "")
	defer db.Cleanup()

	// Create services
	eventPort := adapters.NewInMemoryEventAdapter()
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	mockVault := testutil.NewMockVaultPort()
	mockAuthz := testutil.NewMockAuthorizationPort()
	vaultService := services.NewVaultService(mockVault, mockAuthz, securityPort, eventPort)

	// Create test user
	builder := testutil.NewTestDataBuilder(db.DB)
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)

	// Store credential
	testData := []byte("test-credential-data")
	credResp, err := vaultService.StoreCredential(
		context.Background(),
		"test-cred",
		domain.CredentialTypePassword,
		testData,
		user.ID,
	)
	require.NoError(t, err)

	// Retrieve credential
	retrievedCred, decryptedData, err := vaultService.RetrieveCredential(context.Background(), credResp.ID, user.ID)
	require.NoError(t, err)
	assert.Equal(t, credResp.ID, retrievedCred.ID)
	assert.Equal(t, "test-cred", retrievedCred.Name)
	assert.Equal(t, domain.CredentialTypePassword, retrievedCred.Type)
	assert.Equal(t, user.ID, retrievedCred.OwnerID)
	assert.Equal(t, testData, decryptedData)
}

func TestVault_EncryptionDecryption(t *testing.T) {
	db := testutil.SetupFreshPostgresTestDB(t, "")
	defer db.Cleanup()

	// Create services
	eventPort := adapters.NewInMemoryEventAdapter()
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	mockVault := testutil.NewMockVaultPort()
	mockAuthz := testutil.NewMockAuthorizationPort()
	vaultService := services.NewVaultService(mockVault, mockAuthz, securityPort, eventPort)

	// Create test user
	builder := testutil.NewTestDataBuilder(db.DB)
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)

	// Store sensitive data
	sensitiveData := []byte("very-sensitive-password-123")
	credResp, err := vaultService.StoreCredential(
		context.Background(),
		"password-cred",
		domain.CredentialTypePassword,
		sensitiveData,
		user.ID,
	)
	require.NoError(t, err)

	// Verify credential was created successfully
	assert.NotEmpty(t, credResp.ID)
	assert.Equal(t, "password-cred", credResp.Name)
	assert.Equal(t, domain.CredentialTypePassword, credResp.Type)

	// Retrieve and verify decryption works
	retrievedCred, decryptedData, err := vaultService.RetrieveCredential(context.Background(), credResp.ID, user.ID)
	require.NoError(t, err)
	assert.Equal(t, sensitiveData, decryptedData)
	assert.NotNil(t, retrievedCred)
}

func TestVault_DeleteCredential(t *testing.T) {
	db := testutil.SetupFreshPostgresTestDB(t, "")
	defer db.Cleanup()

	// Create services
	eventPort := adapters.NewInMemoryEventAdapter()
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	mockVault := testutil.NewMockVaultPort()
	mockAuthz := testutil.NewMockAuthorizationPort()
	vaultService := services.NewVaultService(mockVault, mockAuthz, securityPort, eventPort)

	// Create test user
	builder := testutil.NewTestDataBuilder(db.DB)
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)

	// Store credential
	credResp, err := vaultService.StoreCredential(
		context.Background(),
		"temp-cred",
		domain.CredentialTypeAPIKey,
		[]byte("temp-data"),
		user.ID,
	)
	require.NoError(t, err)

	// Delete credential
	err = vaultService.DeleteCredential(context.Background(), credResp.ID, user.ID)
	require.NoError(t, err)

	// Verify credential is deleted
	_, _, err = vaultService.RetrieveCredential(context.Background(), credResp.ID, user.ID)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "credential not found")
}
