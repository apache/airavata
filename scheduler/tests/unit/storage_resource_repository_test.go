package unit

import (
	"context"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestStorageResourceRepository(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	// Create test data
	user := suite.TestUser

	t.Run("CreateStorageResource", func(t *testing.T) {
		totalCapacity := int64(1000000000) // 1GB
		usedCapacity := int64(0)
		availableCapacity := int64(1000000000)

		storageResource := &domain.StorageResource{
			ID:                "test-storage-1",
			Name:              "test-storage",
			Type:              domain.StorageResourceTypeS3,
			Endpoint:          "localhost:9000",
			OwnerID:           user.ID,
			Status:            domain.ResourceStatusActive,
			TotalCapacity:     &totalCapacity,
			UsedCapacity:      &usedCapacity,
			AvailableCapacity: &availableCapacity,
			Region:            "us-east-1",
			Zone:              "us-east-1a",
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateStorageResource(ctx, storageResource)
		require.NoError(t, err)

		// Verify storage resource was created
		createdResource, err := suite.DB.Repo.GetStorageResourceByID(ctx, storageResource.ID)
		require.NoError(t, err)
		assert.Equal(t, storageResource.ID, createdResource.ID)
		assert.Equal(t, storageResource.Name, createdResource.Name)
		assert.Equal(t, storageResource.Type, createdResource.Type)
		assert.Equal(t, storageResource.Endpoint, createdResource.Endpoint)
		assert.Equal(t, storageResource.OwnerID, createdResource.OwnerID)
		assert.Equal(t, storageResource.Status, createdResource.Status)
		assert.Equal(t, *storageResource.TotalCapacity, *createdResource.TotalCapacity)
		assert.Equal(t, *storageResource.UsedCapacity, *createdResource.UsedCapacity)
		assert.Equal(t, *storageResource.AvailableCapacity, *createdResource.AvailableCapacity)
		assert.Equal(t, storageResource.Region, createdResource.Region)
		assert.Equal(t, storageResource.Zone, createdResource.Zone)
	})

	t.Run("GetStorageResourceByID", func(t *testing.T) {
		// Create a storage resource first
		totalCapacity := int64(2000000000)     // 2GB
		usedCapacity := int64(500000000)       // 500MB
		availableCapacity := int64(1500000000) // 1.5GB

		storageResource := &domain.StorageResource{
			ID:                "test-storage-2",
			Name:              "test-storage-2",
			Type:              domain.StorageResourceTypeSFTP,
			Endpoint:          "sftp.example.com:22",
			OwnerID:           user.ID,
			Status:            domain.ResourceStatusActive,
			TotalCapacity:     &totalCapacity,
			UsedCapacity:      &usedCapacity,
			AvailableCapacity: &availableCapacity,
			Region:            "us-west-2",
			Zone:              "us-west-2a",
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateStorageResource(ctx, storageResource)
		require.NoError(t, err)

		// Retrieve the storage resource
		retrievedResource, err := suite.DB.Repo.GetStorageResourceByID(ctx, storageResource.ID)
		require.NoError(t, err)
		assert.Equal(t, storageResource.ID, retrievedResource.ID)
		assert.Equal(t, storageResource.Name, retrievedResource.Name)
		assert.Equal(t, storageResource.Type, retrievedResource.Type)
		assert.Equal(t, storageResource.Endpoint, retrievedResource.Endpoint)
		assert.Equal(t, storageResource.OwnerID, retrievedResource.OwnerID)
		assert.Equal(t, storageResource.Status, retrievedResource.Status)
		assert.Equal(t, *storageResource.TotalCapacity, *retrievedResource.TotalCapacity)
		assert.Equal(t, *storageResource.UsedCapacity, *retrievedResource.UsedCapacity)
		assert.Equal(t, *storageResource.AvailableCapacity, *retrievedResource.AvailableCapacity)
		assert.Equal(t, storageResource.Region, retrievedResource.Region)
		assert.Equal(t, storageResource.Zone, retrievedResource.Zone)

		// Test non-existent storage resource
		_, err = suite.DB.Repo.GetStorageResourceByID(ctx, "non-existent-storage")
		require.Error(t, err)
		assert.Contains(t, err.Error(), "resource not found")
	})

	t.Run("UpdateStorageResource", func(t *testing.T) {
		// Create a storage resource first
		totalCapacity := int64(3000000000) // 3GB
		usedCapacity := int64(0)
		availableCapacity := int64(3000000000)

		storageResource := &domain.StorageResource{
			ID:                "test-storage-3",
			Name:              "test-storage-3",
			Type:              domain.StorageResourceTypeNFS,
			Endpoint:          "nfs.example.com:/data",
			OwnerID:           user.ID,
			Status:            domain.ResourceStatusActive,
			TotalCapacity:     &totalCapacity,
			UsedCapacity:      &usedCapacity,
			AvailableCapacity: &availableCapacity,
			Region:            "us-central-1",
			Zone:              "us-central-1a",
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateStorageResource(ctx, storageResource)
		require.NoError(t, err)

		// Update the storage resource
		storageResource.Status = domain.ResourceStatusInactive
		storageResource.Name = "updated-storage-3"
		storageResource.Region = "us-west-1"
		storageResource.Zone = "us-west-1a"
		newUsedCapacity := int64(1000000000) // 1GB
		storageResource.UsedCapacity = &newUsedCapacity
		newAvailableCapacity := int64(2000000000) // 2GB
		storageResource.AvailableCapacity = &newAvailableCapacity

		err = suite.DB.Repo.UpdateStorageResource(ctx, storageResource)
		require.NoError(t, err)

		// Verify the update
		updatedResource, err := suite.DB.Repo.GetStorageResourceByID(ctx, storageResource.ID)
		require.NoError(t, err)
		assert.Equal(t, domain.ResourceStatusInactive, updatedResource.Status)
		assert.Equal(t, "updated-storage-3", updatedResource.Name)
		assert.Equal(t, "us-west-1", updatedResource.Region)
		assert.Equal(t, "us-west-1a", updatedResource.Zone)
		assert.Equal(t, int64(1000000000), *updatedResource.UsedCapacity)
		assert.Equal(t, int64(2000000000), *updatedResource.AvailableCapacity)
	})

	t.Run("DeleteStorageResource", func(t *testing.T) {
		// Create a storage resource first
		totalCapacity := int64(4000000000) // 4GB
		usedCapacity := int64(0)
		availableCapacity := int64(4000000000)

		storageResource := &domain.StorageResource{
			ID:                "test-storage-4",
			Name:              "test-storage-4",
			Type:              domain.StorageResourceTypeS3,
			Endpoint:          "s3.amazonaws.com",
			OwnerID:           user.ID,
			Status:            domain.ResourceStatusActive,
			TotalCapacity:     &totalCapacity,
			UsedCapacity:      &usedCapacity,
			AvailableCapacity: &availableCapacity,
			Region:            "eu-west-1",
			Zone:              "eu-west-1a",
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateStorageResource(ctx, storageResource)
		require.NoError(t, err)

		// Verify storage resource exists
		_, err = suite.DB.Repo.GetStorageResourceByID(ctx, storageResource.ID)
		require.NoError(t, err)

		// Delete the storage resource
		err = suite.DB.Repo.DeleteStorageResource(ctx, storageResource.ID)
		require.NoError(t, err)

		// Verify storage resource is deleted
		_, err = suite.DB.Repo.GetStorageResourceByID(ctx, storageResource.ID)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "resource not found")
	})

	t.Run("ListStorageResources", func(t *testing.T) {
		// Create multiple storage resources with different types and statuses
		storageResources := []*domain.StorageResource{
			{
				ID:                "test-storage-list-1",
				Name:              "s3-storage",
				Type:              domain.StorageResourceTypeS3,
				Endpoint:          "s3.example.com",
				OwnerID:           user.ID,
				Status:            domain.ResourceStatusActive,
				TotalCapacity:     &[]int64{1000000000}[0],
				UsedCapacity:      &[]int64{0}[0],
				AvailableCapacity: &[]int64{1000000000}[0],
				Region:            "us-east-1",
				Zone:              "us-east-1a",
				CreatedAt:         time.Now(),
				UpdatedAt:         time.Now(),
			},
			{
				ID:                "test-storage-list-2",
				Name:              "sftp-storage",
				Type:              domain.StorageResourceTypeSFTP,
				Endpoint:          "sftp.example.com:22",
				OwnerID:           user.ID,
				Status:            domain.ResourceStatusInactive,
				TotalCapacity:     &[]int64{2000000000}[0],
				UsedCapacity:      &[]int64{500000000}[0],
				AvailableCapacity: &[]int64{1500000000}[0],
				Region:            "us-west-2",
				Zone:              "us-west-2a",
				CreatedAt:         time.Now(),
				UpdatedAt:         time.Now(),
			},
			{
				ID:                "test-storage-list-3",
				Name:              "nfs-storage",
				Type:              domain.StorageResourceTypeNFS,
				Endpoint:          "nfs.example.com:/data",
				OwnerID:           user.ID,
				Status:            domain.ResourceStatusError,
				TotalCapacity:     &[]int64{3000000000}[0],
				UsedCapacity:      &[]int64{1000000000}[0],
				AvailableCapacity: &[]int64{2000000000}[0],
				Region:            "us-central-1",
				Zone:              "us-central-1a",
				CreatedAt:         time.Now(),
				UpdatedAt:         time.Now(),
			},
		}

		for _, resource := range storageResources {
			err := suite.DB.Repo.CreateStorageResource(ctx, resource)
			require.NoError(t, err)
		}

		// Test listing all storage resources
		allResources, total, err := suite.DB.Repo.ListStorageResources(ctx, &ports.StorageResourceFilters{}, 10, 0)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, total, int64(3)) // At least the 3 resources we just created
		assert.GreaterOrEqual(t, len(allResources), 3)

		// Test filtering by type
		s3Filter := &ports.StorageResourceFilters{
			Type: &[]domain.StorageResourceType{domain.StorageResourceTypeS3}[0],
		}
		s3Resources, total, err := suite.DB.Repo.ListStorageResources(ctx, s3Filter, 10, 0)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, total, int64(1))
		assert.GreaterOrEqual(t, len(s3Resources), 1)
		for _, resource := range s3Resources {
			assert.Equal(t, domain.StorageResourceTypeS3, resource.Type)
		}

		// Test filtering by status
		activeFilter := &ports.StorageResourceFilters{
			Status: &[]domain.ResourceStatus{domain.ResourceStatusActive}[0],
		}
		activeResources, total, err := suite.DB.Repo.ListStorageResources(ctx, activeFilter, 10, 0)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, total, int64(1))
		assert.GreaterOrEqual(t, len(activeResources), 1)
		for _, resource := range activeResources {
			assert.Equal(t, domain.ResourceStatusActive, resource.Status)
		}

		// Test filtering by owner
		ownerFilter := &ports.StorageResourceFilters{
			OwnerID: &user.ID,
		}
		ownerResources, total, err := suite.DB.Repo.ListStorageResources(ctx, ownerFilter, 10, 0)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, total, int64(3))
		assert.GreaterOrEqual(t, len(ownerResources), 3)
		for _, resource := range ownerResources {
			assert.Equal(t, user.ID, resource.OwnerID)
		}

		// Test pagination
		firstPage, total, err := suite.DB.Repo.ListStorageResources(ctx, &ports.StorageResourceFilters{}, 2, 0)
		require.NoError(t, err)
		assert.Equal(t, 2, len(firstPage))
		assert.GreaterOrEqual(t, total, int64(3))

		secondPage, _, err := suite.DB.Repo.ListStorageResources(ctx, &ports.StorageResourceFilters{}, 2, 2)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, len(secondPage), 1)
	})

	t.Run("StorageResourceCapacityTracking", func(t *testing.T) {
		// Create a storage resource with capacity tracking
		totalCapacity := int64(5000000000) // 5GB
		usedCapacity := int64(0)
		availableCapacity := int64(5000000000)

		storageResource := &domain.StorageResource{
			ID:                "test-storage-capacity",
			Name:              "capacity-tracked-storage",
			Type:              domain.StorageResourceTypeS3,
			Endpoint:          "s3.capacity.com",
			OwnerID:           user.ID,
			Status:            domain.ResourceStatusActive,
			TotalCapacity:     &totalCapacity,
			UsedCapacity:      &usedCapacity,
			AvailableCapacity: &availableCapacity,
			Region:            "us-east-1",
			Zone:              "us-east-1a",
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateStorageResource(ctx, storageResource)
		require.NoError(t, err)

		// Update capacity usage
		newUsedCapacity := int64(2000000000)      // 2GB
		newAvailableCapacity := int64(3000000000) // 3GB
		storageResource.UsedCapacity = &newUsedCapacity
		storageResource.AvailableCapacity = &newAvailableCapacity

		err = suite.DB.Repo.UpdateStorageResource(ctx, storageResource)
		require.NoError(t, err)

		// Verify capacity update
		updatedResource, err := suite.DB.Repo.GetStorageResourceByID(ctx, storageResource.ID)
		require.NoError(t, err)
		assert.Equal(t, int64(5000000000), *updatedResource.TotalCapacity)
		assert.Equal(t, int64(2000000000), *updatedResource.UsedCapacity)
		assert.Equal(t, int64(3000000000), *updatedResource.AvailableCapacity)
	})

	t.Run("StorageResourceMetadata", func(t *testing.T) {
		// Create a storage resource with metadata
		metadata := map[string]interface{}{
			"encryption":     "AES-256",
			"replication":    3,
			"backup_enabled": true,
			"tags":           []string{"production", "critical"},
			"custom_field":   "custom_value",
		}

		totalCapacity := int64(6000000000) // 6GB
		usedCapacity := int64(0)
		availableCapacity := int64(6000000000)

		storageResource := &domain.StorageResource{
			ID:                "test-storage-metadata",
			Name:              "metadata-storage",
			Type:              domain.StorageResourceTypeS3,
			Endpoint:          "s3.metadata.com",
			OwnerID:           user.ID,
			Status:            domain.ResourceStatusActive,
			TotalCapacity:     &totalCapacity,
			UsedCapacity:      &usedCapacity,
			AvailableCapacity: &availableCapacity,
			Region:            "us-west-1",
			Zone:              "us-west-1a",
			Metadata:          metadata,
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateStorageResource(ctx, storageResource)
		require.NoError(t, err)

		// Verify metadata is stored and retrieved correctly
		retrievedResource, err := suite.DB.Repo.GetStorageResourceByID(ctx, storageResource.ID)
		require.NoError(t, err)
		assert.NotNil(t, retrievedResource.Metadata)
		assert.Equal(t, "AES-256", retrievedResource.Metadata["encryption"])
		assert.Equal(t, float64(3), retrievedResource.Metadata["replication"]) // JSON numbers are float64
		assert.Equal(t, true, retrievedResource.Metadata["backup_enabled"])
		assert.Equal(t, "custom_value", retrievedResource.Metadata["custom_field"])

		// Verify array metadata
		tags, ok := retrievedResource.Metadata["tags"].([]interface{})
		require.True(t, ok)
		assert.Contains(t, tags, "production")
		assert.Contains(t, tags, "critical")
	})
}
