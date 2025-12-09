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

func TestComputeResource_RegisterComputeResource(t *testing.T) {
	db := testutil.SetupFreshPostgresTestDB(t, "")
	defer db.Cleanup()

	// Create services
	eventPort := adapters.NewInMemoryEventAdapter()
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	// Create mock vault and authorization ports
	mockVault := testutil.NewMockVaultPort()
	mockAuthz := testutil.NewMockAuthorizationPort()
	vaultService := services.NewVaultService(mockVault, mockAuthz, securityPort, eventPort)
	registryService := services.NewRegistryService(db.Repo, eventPort, securityPort, vaultService)

	// Create test user
	builder := testutil.NewTestDataBuilder(db.DB)
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)

	// Create SSH credential using vault service
	sshKeys, err := testutil.GenerateSSHKeys()
	require.NoError(t, err)
	defer sshKeys.Cleanup()

	_, err = vaultService.StoreCredential(
		context.Background(),
		"test-ssh-key",
		domain.CredentialTypeSSHKey,
		sshKeys.GetPrivateKey(),
		user.ID,
	)
	require.NoError(t, err)

	// Register compute resource
	req := &domain.CreateComputeResourceRequest{
		Name:        "test-slurm-cluster",
		Type:        domain.ComputeResourceTypeSlurm,
		Endpoint:    "localhost:6817",
		OwnerID:     user.ID,
		MaxWorkers:  10,
		CostPerHour: 1.0,
		Metadata: map[string]interface{}{
			"partition": "default",
			"account":   "test",
		},
	}

	resp, err := registryService.RegisterComputeResource(context.Background(), req)
	require.NoError(t, err)
	assert.NotNil(t, resp.Resource)
	assert.Equal(t, "test-slurm-cluster", resp.Resource.Name)
	assert.Equal(t, domain.ComputeResourceTypeSlurm, resp.Resource.Type)
	assert.Equal(t, "localhost:6817", resp.Resource.Endpoint)
	assert.Equal(t, domain.ResourceStatusActive, resp.Resource.Status)
}

func TestComputeResource_RegisterStorageResource(t *testing.T) {
	db := testutil.SetupFreshPostgresTestDB(t, "")
	defer db.Cleanup()

	// Create services
	eventPort := adapters.NewInMemoryEventAdapter()
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	// Create mock vault and authorization ports
	mockVault := testutil.NewMockVaultPort()
	mockAuthz := testutil.NewMockAuthorizationPort()
	vaultService := services.NewVaultService(mockVault, mockAuthz, securityPort, eventPort)
	registryService := services.NewRegistryService(db.Repo, eventPort, securityPort, vaultService)

	// Create test user
	builder := testutil.NewTestDataBuilder(db.DB)
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)

	// Create S3 credential
	_, err = vaultService.StoreCredential(
		context.Background(),
		"test-s3-cred",
		domain.CredentialTypeAPIKey,
		[]byte("testadmin:testpass"),
		user.ID,
	)
	require.NoError(t, err)

	// Register storage resource
	capacity := int64(1000000000) // 1GB
	req := &domain.CreateStorageResourceRequest{
		Name:          "global-scratch",
		Type:          domain.StorageResourceTypeS3,
		Endpoint:      "localhost:9000",
		OwnerID:       user.ID,
		TotalCapacity: &capacity,
		Metadata: map[string]interface{}{
			"bucket": "global-scratch",
		},
	}

	resp, err := registryService.RegisterStorageResource(context.Background(), req)
	require.NoError(t, err)
	assert.NotNil(t, resp.Resource)
	assert.Equal(t, "global-scratch", resp.Resource.Name)
	assert.Equal(t, domain.StorageResourceTypeS3, resp.Resource.Type)
	assert.Equal(t, "localhost:9000", resp.Resource.Endpoint)
	assert.Equal(t, domain.ResourceStatusActive, resp.Resource.Status)
}
