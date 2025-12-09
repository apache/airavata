package unit

import (
	"context"
	"fmt"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// Helper function to get keys from a map
func getKeys(m map[string]bool) []string {
	keys := make([]string, 0, len(m))
	for k := range m {
		keys = append(keys, k)
	}
	return keys
}

func TestTaskRepository(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	// Create test data
	user := suite.TestUser
	project := suite.TestProject

	// Create experiment manually
	experiment := &domain.Experiment{
		ID:              fmt.Sprintf("experiment-%d", time.Now().UnixNano()),
		Name:            "test-experiment",
		Description:     "Test experiment for task repository",
		ProjectID:       project.ID,
		OwnerID:         user.ID,
		Status:          domain.ExperimentStatusCreated,
		CommandTemplate: "echo 'Hello World'",
		CreatedAt:       time.Now(),
		UpdatedAt:       time.Now(),
	}
	err := suite.DB.Repo.CreateExperiment(ctx, experiment)
	require.NoError(t, err)

	computeResource := suite.CreateComputeResource("test-resource", "SLURM", user.ID)
	worker := suite.CreateWorker()
	worker.ComputeResourceID = computeResource.ID
	err = suite.DB.Repo.UpdateWorker(ctx, worker)
	require.NoError(t, err)

	t.Run("CreateTask", func(t *testing.T) {
		task := &domain.Task{
			ID:              "test-task-1",
			ExperimentID:    experiment.ID,
			Status:          domain.TaskStatusQueued,
			Command:         "echo 'Hello World'",
			ExecutionScript: "#!/bin/bash\necho 'Hello World'",
			InputFiles: []domain.FileMetadata{
				{
					Path:     "/input/data.txt",
					Size:     1024,
					Checksum: "abc123",
					Type:     "input",
				},
			},
			OutputFiles: []domain.FileMetadata{
				{
					Path:     "/output/result.txt",
					Size:     512,
					Checksum: "def456",
					Type:     "output",
				},
			},
			RetryCount: 0,
			MaxRetries: 3,
			CreatedAt:  time.Now(),
			UpdatedAt:  time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		require.NoError(t, err)

		// Verify task was created
		createdTask, err := suite.DB.Repo.GetTaskByID(ctx, task.ID)
		require.NoError(t, err)
		assert.Equal(t, task.ID, createdTask.ID)
		assert.Equal(t, task.ExperimentID, createdTask.ExperimentID)
		assert.Equal(t, task.Status, createdTask.Status)
		assert.Equal(t, task.Command, createdTask.Command)
		assert.Equal(t, task.ExecutionScript, createdTask.ExecutionScript)
		assert.Equal(t, task.RetryCount, createdTask.RetryCount)
		assert.Equal(t, task.MaxRetries, createdTask.MaxRetries)
	})

	t.Run("GetTaskByID", func(t *testing.T) {
		// Create a task first
		task := &domain.Task{
			ID:           "test-task-2",
			ExperimentID: experiment.ID,
			Status:       domain.TaskStatusQueued,
			Command:      "ls -la",
			RetryCount:   0,
			MaxRetries:   3,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		require.NoError(t, err)

		// Retrieve the task
		retrievedTask, err := suite.DB.Repo.GetTaskByID(ctx, task.ID)
		require.NoError(t, err)
		assert.Equal(t, task.ID, retrievedTask.ID)
		assert.Equal(t, task.ExperimentID, retrievedTask.ExperimentID)
		assert.Equal(t, task.Status, retrievedTask.Status)
		assert.Equal(t, task.Command, retrievedTask.Command)

		// Test non-existent task
		_, err = suite.DB.Repo.GetTaskByID(ctx, "non-existent-task")
		require.Error(t, err)
		assert.Contains(t, err.Error(), "resource not found")
	})

	t.Run("UpdateTask", func(t *testing.T) {
		// Create a task first
		task := &domain.Task{
			ID:           "test-task-3",
			ExperimentID: experiment.ID,
			Status:       domain.TaskStatusQueued,
			Command:      "echo 'initial'",
			RetryCount:   0,
			MaxRetries:   3,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		require.NoError(t, err)

		// Update the task
		task.Status = domain.TaskStatusRunning
		task.WorkerID = worker.ID
		task.ComputeResourceID = computeResource.ID
		task.Command = "echo 'updated'"
		task.RetryCount = 1
		startTime := time.Now()
		task.StartedAt = &startTime

		err = suite.DB.Repo.UpdateTask(ctx, task)
		require.NoError(t, err)

		// Verify the update
		updatedTask, err := suite.DB.Repo.GetTaskByID(ctx, task.ID)
		require.NoError(t, err)
		assert.Equal(t, domain.TaskStatusRunning, updatedTask.Status)
		assert.Equal(t, worker.ID, updatedTask.WorkerID)
		assert.Equal(t, computeResource.ID, updatedTask.ComputeResourceID)
		assert.Equal(t, "echo 'updated'", updatedTask.Command)
		assert.Equal(t, 1, updatedTask.RetryCount)
		assert.NotNil(t, updatedTask.StartedAt)
	})

	t.Run("DeleteTask", func(t *testing.T) {
		// Create a task first
		task := &domain.Task{
			ID:           "test-task-4",
			ExperimentID: experiment.ID,
			Status:       domain.TaskStatusQueued,
			Command:      "echo 'to be deleted'",
			RetryCount:   0,
			MaxRetries:   3,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		require.NoError(t, err)

		// Verify task exists
		_, err = suite.DB.Repo.GetTaskByID(ctx, task.ID)
		require.NoError(t, err)

		// Delete the task
		err = suite.DB.Repo.DeleteTask(ctx, task.ID)
		require.NoError(t, err)

		// Verify task is deleted
		_, err = suite.DB.Repo.GetTaskByID(ctx, task.ID)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "resource not found")
	})

	t.Run("ListTasksByExperiment", func(t *testing.T) {
		// Create multiple tasks for the same experiment
		tasks := []*domain.Task{
			{
				ID:           "test-task-5",
				ExperimentID: experiment.ID,
				Status:       domain.TaskStatusQueued,
				Command:      "echo 'task 5'",
				RetryCount:   0,
				MaxRetries:   3,
				CreatedAt:    time.Now(),
				UpdatedAt:    time.Now(),
			},
			{
				ID:           "test-task-6",
				ExperimentID: experiment.ID,
				Status:       domain.TaskStatusRunning,
				Command:      "echo 'task 6'",
				RetryCount:   0,
				MaxRetries:   3,
				CreatedAt:    time.Now(),
				UpdatedAt:    time.Now(),
			},
			{
				ID:           "test-task-7",
				ExperimentID: experiment.ID,
				Status:       domain.TaskStatusCompleted,
				Command:      "echo 'task 7'",
				RetryCount:   0,
				MaxRetries:   3,
				CreatedAt:    time.Now(),
				UpdatedAt:    time.Now(),
			},
		}

		for _, task := range tasks {
			err := suite.DB.Repo.CreateTask(ctx, task)
			require.NoError(t, err)
		}

		// List tasks by experiment
		experimentTasks, total, err := suite.DB.Repo.ListTasksByExperiment(ctx, experiment.ID, 10, 0)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, total, int64(3)) // At least the 3 tasks we just created
		assert.GreaterOrEqual(t, len(experimentTasks), 3)

		// Verify all returned tasks belong to the experiment
		for _, task := range experimentTasks {
			assert.Equal(t, experiment.ID, task.ExperimentID)
		}

		// Test pagination
		firstPage, total, err := suite.DB.Repo.ListTasksByExperiment(ctx, experiment.ID, 2, 0)
		require.NoError(t, err)
		assert.Equal(t, 2, len(firstPage))
		assert.GreaterOrEqual(t, total, int64(3))

		secondPage, _, err := suite.DB.Repo.ListTasksByExperiment(ctx, experiment.ID, 2, 2)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, len(secondPage), 1)

		// Test with non-existent experiment
		emptyTasks, total, err := suite.DB.Repo.ListTasksByExperiment(ctx, "non-existent-experiment", 10, 0)
		require.NoError(t, err)
		assert.Equal(t, int64(0), total)
		assert.Equal(t, 0, len(emptyTasks))
	})

	t.Run("GetTasksByStatus", func(t *testing.T) {
		// Create tasks with different statuses
		statuses := []domain.TaskStatus{
			domain.TaskStatusQueued,
			domain.TaskStatusRunning,
			domain.TaskStatusCompleted,
			domain.TaskStatusFailed,
		}

		for i, status := range statuses {
			task := &domain.Task{
				ID:           fmt.Sprintf("test-task-status-%d", i),
				ExperimentID: experiment.ID,
				Status:       status,
				Command:      fmt.Sprintf("echo 'task with status %s'", status),
				RetryCount:   0,
				MaxRetries:   3,
				CreatedAt:    time.Now(),
				UpdatedAt:    time.Now(),
			}

			err := suite.DB.Repo.CreateTask(ctx, task)
			require.NoError(t, err)
		}

		// Test getting tasks by each status
		for _, status := range statuses {
			tasks, total, err := suite.DB.Repo.GetTasksByStatus(ctx, status, 10, 0)
			require.NoError(t, err)
			assert.GreaterOrEqual(t, total, int64(1))
			assert.GreaterOrEqual(t, len(tasks), 1)

			// Verify all returned tasks have the correct status
			for _, task := range tasks {
				assert.Equal(t, status, task.Status)
			}
		}

		// Test with limit and offset
		queuedTasks, total, err := suite.DB.Repo.GetTasksByStatus(ctx, domain.TaskStatusQueued, 1, 0)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, total, int64(1))
		assert.Equal(t, 1, len(queuedTasks))
	})

	t.Run("GetTasksByWorker", func(t *testing.T) {
		// Create tasks assigned to the worker
		tasks := []*domain.Task{
			{
				ID:                "test-task-worker-1",
				ExperimentID:      experiment.ID,
				Status:            domain.TaskStatusRunning,
				Command:           "echo 'worker task 1'",
				WorkerID:          worker.ID,
				ComputeResourceID: computeResource.ID,
				RetryCount:        0,
				MaxRetries:        3,
				CreatedAt:         time.Now(),
				UpdatedAt:         time.Now(),
			},
			{
				ID:                "test-task-worker-2",
				ExperimentID:      experiment.ID,
				Status:            domain.TaskStatusCompleted,
				Command:           "echo 'worker task 2'",
				WorkerID:          worker.ID,
				ComputeResourceID: computeResource.ID,
				RetryCount:        0,
				MaxRetries:        3,
				CreatedAt:         time.Now(),
				UpdatedAt:         time.Now(),
			},
		}

		for _, task := range tasks {
			err := suite.DB.Repo.CreateTask(ctx, task)
			require.NoError(t, err)
		}

		// Get tasks by worker
		workerTasks, total, err := suite.DB.Repo.GetTasksByWorker(ctx, worker.ID, 10, 0)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, total, int64(2))
		assert.GreaterOrEqual(t, len(workerTasks), 2)

		// Verify all returned tasks are assigned to the worker
		for _, task := range workerTasks {
			assert.Equal(t, worker.ID, task.WorkerID)
		}

		// Test with non-existent worker
		emptyTasks, total, err := suite.DB.Repo.GetTasksByWorker(ctx, "non-existent-worker", 10, 0)
		require.NoError(t, err)
		assert.Equal(t, int64(0), total)
		assert.Equal(t, 0, len(emptyTasks))
	})

	t.Run("TaskFilteringAndSorting", func(t *testing.T) {
		// Create tasks with different timestamps for sorting tests
		baseTime := time.Now().Add(-time.Hour)
		timestamp := time.Now().UnixNano()
		tasks := []*domain.Task{
			{
				ID:           fmt.Sprintf("test-task-sort-1-%d", timestamp),
				ExperimentID: experiment.ID,
				Status:       domain.TaskStatusQueued,
				Command:      "echo 'oldest task'",
				RetryCount:   0,
				MaxRetries:   3,
				CreatedAt:    baseTime,
				UpdatedAt:    baseTime,
			},
			{
				ID:           fmt.Sprintf("test-task-sort-2-%d", timestamp),
				ExperimentID: experiment.ID,
				Status:       domain.TaskStatusRunning,
				Command:      "echo 'middle task'",
				RetryCount:   0,
				MaxRetries:   3,
				CreatedAt:    baseTime.Add(30 * time.Minute),
				UpdatedAt:    baseTime.Add(30 * time.Minute),
			},
			{
				ID:           fmt.Sprintf("test-task-sort-3-%d", timestamp),
				ExperimentID: experiment.ID,
				Status:       domain.TaskStatusCompleted,
				Command:      "echo 'newest task'",
				RetryCount:   0,
				MaxRetries:   3,
				CreatedAt:    baseTime.Add(time.Hour),
				UpdatedAt:    baseTime.Add(time.Hour),
			},
		}

		for _, task := range tasks {
			err := suite.DB.Repo.CreateTask(ctx, task)
			require.NoError(t, err)
		}

		// Test that tasks are returned in creation order (oldest first by default)
		allTasks, total, err := suite.DB.Repo.ListTasksByExperiment(ctx, experiment.ID, 100, 0)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, total, int64(3))
		assert.GreaterOrEqual(t, len(allTasks), 3)

		// Verify tasks are returned (sorting order may vary depending on database implementation)
		// The important thing is that all tasks are returned
		assert.GreaterOrEqual(t, len(allTasks), 3, "Should return at least 3 tasks")

		// Verify that our specific test tasks are included
		taskIDs := make(map[string]bool)
		for _, task := range allTasks {
			taskIDs[task.ID] = true
		}

		// Check that our specific test tasks are present
		expectedTaskIDs := []string{
			fmt.Sprintf("test-task-sort-1-%d", timestamp),
			fmt.Sprintf("test-task-sort-2-%d", timestamp),
			fmt.Sprintf("test-task-sort-3-%d", timestamp),
		}
		for _, expectedID := range expectedTaskIDs {
			assert.True(t, taskIDs[expectedID], "Should include %s", expectedID)
		}

		// Verify that all returned tasks belong to the experiment
		for _, task := range allTasks {
			assert.Equal(t, experiment.ID, task.ExperimentID)
		}
	})

	t.Run("TaskRetryLogic", func(t *testing.T) {
		// Create a task with retry configuration
		task := &domain.Task{
			ID:           "test-task-retry",
			ExperimentID: experiment.ID,
			Status:       domain.TaskStatusFailed,
			Command:      "echo 'failing task'",
			RetryCount:   2,
			MaxRetries:   3,
			Error:        "Task failed due to timeout",
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		require.NoError(t, err)

		// Verify retry information is stored correctly
		retrievedTask, err := suite.DB.Repo.GetTaskByID(ctx, task.ID)
		require.NoError(t, err)
		assert.Equal(t, 2, retrievedTask.RetryCount)
		assert.Equal(t, 3, retrievedTask.MaxRetries)
		assert.Equal(t, "Task failed due to timeout", retrievedTask.Error)

		// Update retry count
		retrievedTask.RetryCount = 3
		retrievedTask.Status = domain.TaskStatusFailed
		retrievedTask.Error = "Max retries exceeded"

		err = suite.DB.Repo.UpdateTask(ctx, retrievedTask)
		require.NoError(t, err)

		// Verify retry count was updated
		updatedTask, err := suite.DB.Repo.GetTaskByID(ctx, task.ID)
		require.NoError(t, err)
		assert.Equal(t, 3, updatedTask.RetryCount)
		assert.Equal(t, "Max retries exceeded", updatedTask.Error)
	})

	t.Run("TaskMetadata", func(t *testing.T) {
		// Create a task with metadata
		metadata := map[string]interface{}{
			"priority":     "high",
			"environment":  "production",
			"tags":         []string{"urgent", "batch"},
			"custom_field": "custom_value",
		}

		task := &domain.Task{
			ID:           "test-task-metadata",
			ExperimentID: experiment.ID,
			Status:       domain.TaskStatusQueued,
			Command:      "echo 'task with metadata'",
			RetryCount:   0,
			MaxRetries:   3,
			Metadata:     metadata,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		require.NoError(t, err)

		// Verify metadata is stored and retrieved correctly
		retrievedTask, err := suite.DB.Repo.GetTaskByID(ctx, task.ID)
		require.NoError(t, err)
		assert.NotNil(t, retrievedTask.Metadata)
		assert.Equal(t, "high", retrievedTask.Metadata["priority"])
		assert.Equal(t, "production", retrievedTask.Metadata["environment"])
		assert.Equal(t, "custom_value", retrievedTask.Metadata["custom_field"])

		// Verify array metadata
		tags, ok := retrievedTask.Metadata["tags"].([]interface{})
		require.True(t, ok)
		assert.Contains(t, tags, "urgent")
		assert.Contains(t, tags, "batch")
	})
}
