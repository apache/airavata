package unit

import (
	"context"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestWorkerRepository(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping unit test in short mode")
	}

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres", "redis", "minio")
	require.NoError(t, err)

	ctx := context.Background()

	t.Run("CreateWorker", func(t *testing.T) {
		// Create a worker using the test suite helper
		worker := suite.CreateWorker()
		assert.NotNil(t, worker)
		assert.NotEmpty(t, worker.ID)
		assert.Equal(t, domain.WorkerStatusIdle, worker.Status)
	})

	t.Run("GetWorkerByID", func(t *testing.T) {
		// Create a worker
		worker := suite.CreateWorker()

		// Retrieve the worker
		retrievedWorker, err := suite.DB.Repo.GetWorkerByID(ctx, worker.ID)
		require.NoError(t, err)
		assert.NotNil(t, retrievedWorker)
		assert.Equal(t, worker.ID, retrievedWorker.ID)
		assert.Equal(t, worker.Status, retrievedWorker.Status)
	})

	t.Run("UpdateWorker", func(t *testing.T) {
		// Create a worker
		worker := suite.CreateWorker()

		// Update worker status
		worker.Status = domain.WorkerStatusBusy
		worker.LastHeartbeat = time.Now()

		err := suite.DB.Repo.UpdateWorker(ctx, worker)
		require.NoError(t, err)

		// Verify the update
		updatedWorker, err := suite.DB.Repo.GetWorkerByID(ctx, worker.ID)
		require.NoError(t, err)
		assert.Equal(t, domain.WorkerStatusBusy, updatedWorker.Status)
	})

	t.Run("GetWorkersByStatus", func(t *testing.T) {
		// Create a worker
		worker := suite.CreateWorker()

		// List workers by status
		workers, count, err := suite.DB.Repo.GetWorkersByStatus(ctx, domain.WorkerStatusIdle, 10, 0)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, count, int64(1))
		assert.NotEmpty(t, workers)

		// Find our worker in the list
		found := false
		for _, w := range workers {
			if w.ID == worker.ID {
				found = true
				break
			}
		}
		assert.True(t, found, "Created worker should be in the list")
	})

	t.Run("ListWorkersByComputeResource", func(t *testing.T) {
		// Create a worker
		worker := suite.CreateWorker()

		// List workers for the compute resource
		workers, count, err := suite.DB.Repo.ListWorkersByComputeResource(ctx, worker.ComputeResourceID, 10, 0)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, count, int64(1))
		assert.NotEmpty(t, workers)

		// Find our worker in the list
		found := false
		for _, w := range workers {
			if w.ID == worker.ID {
				found = true
				break
			}
		}
		assert.True(t, found, "Created worker should be in the list")
	})

	t.Run("DeleteWorker", func(t *testing.T) {
		// Create a worker
		worker := suite.CreateWorker()

		// Delete the worker
		err := suite.DB.Repo.DeleteWorker(ctx, worker.ID)
		require.NoError(t, err)

		// Verify the worker is deleted
		deletedWorker, err := suite.DB.Repo.GetWorkerByID(ctx, worker.ID)
		assert.Error(t, err)
		assert.Nil(t, deletedWorker)
	})
}
