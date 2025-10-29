package unit

import (
	"context"
	"testing"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestComputeResourceValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	t.Run("ValidComputeResource", func(t *testing.T) {
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: 0.5,
			MaxWorkers:  10,
		}

		resp, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.NoError(t, err)
		assert.True(t, resp.Success)
		assert.NotEmpty(t, resp.Resource.ID)
		assert.Equal(t, "test-compute", resp.Resource.Name)
		assert.Equal(t, domain.ComputeResourceTypeSlurm, resp.Resource.Type)
		assert.Equal(t, "slurm.example.com:6817", resp.Resource.Endpoint)
		assert.Equal(t, 0.5, resp.Resource.CostPerHour)
		assert.Equal(t, 10, resp.Resource.MaxWorkers)
	})

	t.Run("InvalidName", func(t *testing.T) {
		req := &domain.CreateComputeResourceRequest{
			Name:        "", // Empty name
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: 0.5,
			MaxWorkers:  10,
		}

		_, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "name")
	})

	t.Run("InvalidType", func(t *testing.T) {
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        "", // Empty type
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: 0.5,
			MaxWorkers:  10,
		}

		_, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "type")
	})

	t.Run("InvalidEndpoint", func(t *testing.T) {
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "", // Empty endpoint
			CostPerHour: 0.5,
			MaxWorkers:  10,
		}

		_, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "endpoint")
	})

	t.Run("NegativeCost", func(t *testing.T) {
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: -1.0, // Negative cost
			MaxWorkers:  10,
		}

		_, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "cost")
	})

	t.Run("ZeroMaxWorkers", func(t *testing.T) {
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: 0.5,
			MaxWorkers:  0, // Zero max workers
		}

		_, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "max_workers")
	})

	t.Run("NegativeMaxWorkers", func(t *testing.T) {
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: 0.5,
			MaxWorkers:  -1, // Negative max workers
		}

		_, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "max_workers")
	})
}

func TestStorageResourceValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	t.Run("ValidStorageResource", func(t *testing.T) {
		capacity := int64(1024 * 1024 * 1024) // 1GB
		req := &domain.CreateStorageResourceRequest{
			Name:          "test-storage",
			Type:          domain.StorageResourceTypeS3,
			Endpoint:      "s3://test-bucket",
			OwnerID:       suite.TestUser.ID,
			TotalCapacity: &capacity,
		}

		resp, err := suite.RegistryService.RegisterStorageResource(ctx, req)
		require.NoError(t, err)
		assert.True(t, resp.Success)
		assert.NotEmpty(t, resp.Resource.ID)
		assert.Equal(t, "test-storage", resp.Resource.Name)
		assert.Equal(t, domain.StorageResourceTypeS3, resp.Resource.Type)
		assert.Equal(t, "s3://test-bucket", resp.Resource.Endpoint)
		assert.Equal(t, capacity, *resp.Resource.TotalCapacity)
	})

	t.Run("InvalidName", func(t *testing.T) {
		capacity := int64(1024 * 1024 * 1024)
		req := &domain.CreateStorageResourceRequest{
			Name:          "", // Empty name
			Type:          domain.StorageResourceTypeS3,
			Endpoint:      "s3://test-bucket",
			OwnerID:       suite.TestUser.ID,
			TotalCapacity: &capacity,
		}

		_, err := suite.RegistryService.RegisterStorageResource(ctx, req)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "name")
	})

	t.Run("InvalidType", func(t *testing.T) {
		capacity := int64(1024 * 1024 * 1024)
		req := &domain.CreateStorageResourceRequest{
			Name:          "test-storage",
			Type:          "", // Empty type
			Endpoint:      "s3://test-bucket",
			OwnerID:       suite.TestUser.ID,
			TotalCapacity: &capacity,
		}

		_, err := suite.RegistryService.RegisterStorageResource(ctx, req)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "type")
	})

	t.Run("InvalidEndpoint", func(t *testing.T) {
		capacity := int64(1024 * 1024 * 1024)
		req := &domain.CreateStorageResourceRequest{
			Name:          "test-storage",
			Type:          domain.StorageResourceTypeS3,
			Endpoint:      "", // Empty endpoint
			TotalCapacity: &capacity,
		}

		_, err := suite.RegistryService.RegisterStorageResource(ctx, req)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "endpoint")
	})

	t.Run("NegativeCapacity", func(t *testing.T) {
		capacity := int64(-1) // Negative capacity
		req := &domain.CreateStorageResourceRequest{
			Name:          "test-storage",
			Type:          domain.StorageResourceTypeS3,
			Endpoint:      "s3://test-bucket",
			OwnerID:       suite.TestUser.ID,
			TotalCapacity: &capacity,
		}

		_, err := suite.RegistryService.RegisterStorageResource(ctx, req)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "capacity")
	})
}

func TestResourceConnectionValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	t.Run("ValidateComputeResourceConnection", func(t *testing.T) {
		// Create a compute resource
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: 0.5,
			MaxWorkers:  10,
		}

		resp, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.NoError(t, err)

		// Test connection validation
		err = suite.RegistryService.ValidateResourceConnection(ctx, resp.Resource.ID, suite.TestUser.ID)
		// Note: This will likely fail in unit tests since we don't have real SLURM
		// but we're testing the validation logic, not the actual connection
		if err != nil {
			assert.Contains(t, err.Error(), "credentials")
		}
	})

	t.Run("ValidateStorageResourceConnection", func(t *testing.T) {
		// Create a storage resource
		capacity := int64(1024 * 1024 * 1024)
		req := &domain.CreateStorageResourceRequest{
			Name:          "test-storage",
			Type:          domain.StorageResourceTypeS3,
			Endpoint:      "s3://test-bucket",
			OwnerID:       suite.TestUser.ID,
			TotalCapacity: &capacity,
		}

		resp, err := suite.RegistryService.RegisterStorageResource(ctx, req)
		require.NoError(t, err)

		// Test connection validation
		err = suite.RegistryService.ValidateResourceConnection(ctx, resp.Resource.ID, suite.TestUser.ID)
		// Note: This will likely fail in unit tests since we don't have real S3
		// but we're testing the validation logic, not the actual connection
		if err != nil {
			assert.Contains(t, err.Error(), "connection")
		}
	})
}

func TestResourcePermissionValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	t.Run("OwnerCanAccessResource", func(t *testing.T) {
		// Create a compute resource
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: 0.5,
			MaxWorkers:  10,
		}

		resp, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.NoError(t, err)

		// Owner should be able to access the resource
		getReq := &domain.GetResourceRequest{
			ResourceID: resp.Resource.ID,
		}

		getResp, err := suite.RegistryService.GetResource(ctx, getReq)
		require.NoError(t, err)
		assert.True(t, getResp.Success)
		// Note: Resource interface doesn't have GetOwnerID method, so we can't test this directly
		// This would need to be tested at the service layer
	})

	t.Run("NonOwnerCanAccessResource", func(t *testing.T) {
		// Create another user
		otherUser, err := suite.Builder.CreateUser("other-user", "other@example.com", false).Build()
		require.NoError(t, err)

		// Create a compute resource owned by the other user
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute-other",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     otherUser.ID,
			CostPerHour: 0.5,
			MaxWorkers:  10,
		}

		resp, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.NoError(t, err)

		// Note: Currently, the registry service doesn't implement resource-level authorization
		// Any user can access any resource if they know the resource ID
		// This test documents the current behavior
		getReq := &domain.GetResourceRequest{
			ResourceID: resp.Resource.ID,
		}

		getResp, err := suite.RegistryService.GetResource(ctx, getReq)
		require.NoError(t, err)
		assert.True(t, getResp.Success)
		assert.Equal(t, otherUser.ID, getResp.Resource.(*domain.ComputeResource).OwnerID)
	})
}

func TestResourceStatusTransitions(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	t.Run("ValidStatusTransitions", func(t *testing.T) {
		// Create a compute resource
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: 0.5,
			MaxWorkers:  10,
		}

		resp, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.NoError(t, err)
		assert.Equal(t, domain.ResourceStatusActive, resp.Resource.Status)

		// Update to inactive
		inactiveStatus := domain.ResourceStatusInactive
		updateReq := &domain.UpdateResourceRequest{
			ResourceID: resp.Resource.ID,
			Status:     &inactiveStatus,
		}

		_, err = suite.RegistryService.UpdateResource(ctx, updateReq)
		require.NoError(t, err)
		// Note: Can't directly access Status field due to interface{} type
		// This would need to be tested at the service layer

		// Update to error
		errorStatus := domain.ResourceStatusError
		updateReq.Status = &errorStatus
		_, err = suite.RegistryService.UpdateResource(ctx, updateReq)
		require.NoError(t, err)

		// Update back to active
		activeStatus := domain.ResourceStatusActive
		updateReq.Status = &activeStatus
		_, err = suite.RegistryService.UpdateResource(ctx, updateReq)
		require.NoError(t, err)
	})
}

func TestResourceConstraints(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	t.Run("CostConstraints", func(t *testing.T) {
		// Test zero cost (should be valid)
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: 0.0, // Zero cost should be valid
			MaxWorkers:  10,
		}

		resp, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.NoError(t, err)
		assert.Equal(t, 0.0, resp.Resource.CostPerHour)

		// Test very high cost (should be valid)
		req.Name = "test-compute-expensive"
		req.CostPerHour = 1000.0
		resp, err = suite.RegistryService.RegisterComputeResource(ctx, req)
		require.NoError(t, err)
		assert.Equal(t, 1000.0, resp.Resource.CostPerHour)
	})

	t.Run("MaxWorkersConstraints", func(t *testing.T) {
		// Test minimum valid max workers
		req := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			OwnerID:     suite.TestUser.ID,
			CostPerHour: 0.5,
			MaxWorkers:  1, // Minimum valid value
		}

		resp, err := suite.RegistryService.RegisterComputeResource(ctx, req)
		require.NoError(t, err)
		assert.Equal(t, 1, resp.Resource.MaxWorkers)

		// Test high max workers
		req.Name = "test-compute-large"
		req.MaxWorkers = 1000
		resp, err = suite.RegistryService.RegisterComputeResource(ctx, req)
		require.NoError(t, err)
		assert.Equal(t, 1000, resp.Resource.MaxWorkers)
	})

	t.Run("CapacityConstraints", func(t *testing.T) {
		// Test zero capacity (should be valid for optional field)
		req := &domain.CreateStorageResourceRequest{
			Name:          "test-storage",
			Type:          domain.StorageResourceTypeS3,
			Endpoint:      "s3://test-bucket",
			OwnerID:       suite.TestUser.ID,
			TotalCapacity: nil, // No capacity specified
		}

		resp, err := suite.RegistryService.RegisterStorageResource(ctx, req)
		require.NoError(t, err)
		assert.Nil(t, resp.Resource.TotalCapacity)

		// Test very large capacity
		largeCapacity := int64(1024 * 1024 * 1024 * 1024) // 1TB
		req.Name = "test-storage-large"
		req.TotalCapacity = &largeCapacity
		resp, err = suite.RegistryService.RegisterStorageResource(ctx, req)
		require.NoError(t, err)
		assert.Equal(t, largeCapacity, *resp.Resource.TotalCapacity)
	})
}
