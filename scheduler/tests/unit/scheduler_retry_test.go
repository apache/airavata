package unit

import (
	"context"
	"fmt"
	"testing"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestSchedulerService_FailTask_RetryLogic(t *testing.T) {

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	scheduler := suite.GetSchedulerService()
	require.NotNil(t, scheduler)

	// Setup worker and task for the test
	worker, task, err := suite.SetupSchedulerFailTaskTest(3)
	require.NoError(t, err)
	require.NotNil(t, worker)
	require.NotNil(t, task)

	// Update task status to ASSIGNED and assign to worker so it can be failed
	task.Status = domain.TaskStatusQueued
	task.ComputeResourceID = worker.ComputeResourceID
	task.WorkerID = worker.ID
	err = suite.DB.Repo.UpdateTask(context.Background(), task)
	require.NoError(t, err)

	// Fail task first time
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, "test failure 1")
	require.NoError(t, err)

	updatedTask, err := suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusQueued, updatedTask.Status)
	assert.Equal(t, 1, updatedTask.RetryCount)
	assert.Contains(t, updatedTask.Error, "test failure 1")

	// Reassign task to worker for second failure
	updatedTask.WorkerID = worker.ID
	updatedTask.ComputeResourceID = worker.ComputeResourceID
	updatedTask.Status = domain.TaskStatusQueued
	err = suite.DB.Repo.UpdateTask(context.Background(), updatedTask)
	require.NoError(t, err)

	// Fail task second time
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, "test failure 2")
	require.NoError(t, err)

	updatedTask, err = suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusQueued, updatedTask.Status)
	assert.Equal(t, 2, updatedTask.RetryCount)
	assert.Contains(t, updatedTask.Error, "test failure 2")

	// Reassign task to worker for third failure
	updatedTask.WorkerID = worker.ID
	updatedTask.ComputeResourceID = worker.ComputeResourceID
	updatedTask.Status = domain.TaskStatusQueued
	err = suite.DB.Repo.UpdateTask(context.Background(), updatedTask)
	require.NoError(t, err)

	// Fail task third time (still retry)
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, "test failure 3")
	require.NoError(t, err)

	updatedTask, err = suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusQueued, updatedTask.Status)
	assert.Equal(t, 3, updatedTask.RetryCount)
	assert.Contains(t, updatedTask.Error, "test failure 3")

	// Reassign task to worker for fourth failure
	updatedTask.WorkerID = worker.ID
	updatedTask.ComputeResourceID = worker.ComputeResourceID
	updatedTask.Status = domain.TaskStatusQueued
	err = suite.DB.Repo.UpdateTask(context.Background(), updatedTask)
	require.NoError(t, err)

	// Fail task fourth time (permanent failure)
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, "test failure 4")
	require.NoError(t, err)

	updatedTask, err = suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, updatedTask.Status)
	assert.Equal(t, 3, updatedTask.RetryCount)
	assert.Contains(t, updatedTask.Error, "test failure 4")
}

func TestSchedulerService_FailTask_NoRetries(t *testing.T) {

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	scheduler := suite.GetSchedulerService()
	require.NotNil(t, scheduler)

	// Setup worker and task for the test
	worker, task, err := suite.SetupSchedulerFailTaskTest(0)
	require.NoError(t, err)
	require.NotNil(t, worker)
	require.NotNil(t, task)

	// Update task status to ASSIGNED and assign to worker so it can be failed
	task.Status = domain.TaskStatusQueued
	task.ComputeResourceID = worker.ComputeResourceID
	task.WorkerID = worker.ID
	err = suite.DB.Repo.UpdateTask(context.Background(), task)
	require.NoError(t, err)

	// Fail task - should be permanent failure immediately
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, "test failure")
	require.NoError(t, err)

	updatedTask, err := suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, updatedTask.Status)
	assert.Equal(t, 0, updatedTask.RetryCount)
	assert.Contains(t, updatedTask.Error, "test failure")
}

func TestSchedulerService_FailTask_SingleRetry(t *testing.T) {

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	scheduler := suite.GetSchedulerService()
	require.NotNil(t, scheduler)

	// Setup worker and task for the test
	worker, task, err := suite.SetupSchedulerFailTaskTest(1)
	require.NoError(t, err)
	require.NotNil(t, worker)
	require.NotNil(t, task)

	// Update task status to ASSIGNED and assign to worker so it can be failed
	task.Status = domain.TaskStatusQueued
	task.ComputeResourceID = worker.ComputeResourceID
	task.WorkerID = worker.ID
	err = suite.DB.Repo.UpdateTask(context.Background(), task)
	require.NoError(t, err)

	// Fail task first time - should be re-queued
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, "test failure 1")
	require.NoError(t, err)

	updatedTask, err := suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusQueued, updatedTask.Status)
	assert.Equal(t, 1, updatedTask.RetryCount)

	// Reassign task to worker for second failure
	updatedTask.WorkerID = worker.ID
	updatedTask.ComputeResourceID = worker.ComputeResourceID
	updatedTask.Status = domain.TaskStatusQueued
	err = suite.DB.Repo.UpdateTask(context.Background(), updatedTask)
	require.NoError(t, err)

	// Fail task second time - should be permanent failure
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, "test failure 2")
	require.NoError(t, err)

	updatedTask, err = suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, updatedTask.Status)
	assert.Equal(t, 1, updatedTask.RetryCount)
}

func TestSchedulerService_FailTask_RetryCountTracking(t *testing.T) {

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	scheduler := suite.GetSchedulerService()
	require.NotNil(t, scheduler)

	// Create task with multiple retries
	task, err := suite.CreateTaskWithRetries("test-task", 5)
	require.NoError(t, err)
	assert.NotNil(t, task)

	worker := suite.CreateWorker()
	require.NotNil(t, worker)

	// Assign task to worker initially
	task.WorkerID = worker.ID
	task.ComputeResourceID = worker.ComputeResourceID
	task.Status = domain.TaskStatusRunning
	err = suite.UpdateTask(task)
	require.NoError(t, err)

	// Fail task multiple times and verify retry count tracking
	for i := 1; i <= 6; i++ {
		err = scheduler.FailTask(context.Background(), task.ID, worker.ID, fmt.Sprintf("test failure %d", i))
		require.NoError(t, err)

		updatedTask, err := suite.GetTask(task.ID)
		require.NoError(t, err)

		if i <= 5 {
			assert.Equal(t, domain.TaskStatusQueued, updatedTask.Status)
			assert.Equal(t, i, updatedTask.RetryCount)

			// Reassign task to worker for next attempt
			updatedTask.WorkerID = worker.ID
			updatedTask.ComputeResourceID = worker.ComputeResourceID
			updatedTask.Status = domain.TaskStatusRunning
			err = suite.UpdateTask(updatedTask)
			require.NoError(t, err)
		} else {
			assert.Equal(t, domain.TaskStatusFailed, updatedTask.Status)
			assert.Equal(t, 5, updatedTask.RetryCount)
		}
	}
}

func TestSchedulerService_FailTask_WorkerAssignment(t *testing.T) {

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	scheduler := suite.GetSchedulerService()
	require.NotNil(t, scheduler)

	// Create task
	task, err := suite.CreateTaskWithRetries("test-task", 2)
	require.NoError(t, err)
	assert.NotNil(t, task)

	worker1 := suite.CreateWorker()
	require.NotNil(t, worker1)

	worker2 := suite.CreateWorker()
	require.NotNil(t, worker2)

	// Assign task to worker1
	task.WorkerID = worker1.ID
	task.ComputeResourceID = worker1.ComputeResourceID
	task.Status = domain.TaskStatusRunning
	err = suite.UpdateTask(task)
	require.NoError(t, err)

	// Fail task on worker1
	err = scheduler.FailTask(context.Background(), task.ID, worker1.ID, "worker1 failure")
	require.NoError(t, err)

	updatedTask, err := suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusQueued, updatedTask.Status)
	assert.Equal(t, 1, updatedTask.RetryCount)
	assert.Empty(t, updatedTask.WorkerID)          // Worker should be cleared for retry
	assert.Empty(t, updatedTask.ComputeResourceID) // Compute resource should be cleared for retry

	// Assign task to worker2
	updatedTask.WorkerID = worker2.ID
	updatedTask.ComputeResourceID = worker2.ComputeResourceID
	updatedTask.Status = domain.TaskStatusRunning
	err = suite.UpdateTask(updatedTask)
	require.NoError(t, err)

	// Fail task on worker2
	err = scheduler.FailTask(context.Background(), task.ID, worker2.ID, "worker2 failure")
	require.NoError(t, err)

	// Assign task to worker1 again for third failure
	updatedTask, err = suite.GetTask(task.ID)
	require.NoError(t, err)
	updatedTask.WorkerID = worker1.ID
	updatedTask.ComputeResourceID = worker1.ComputeResourceID
	updatedTask.Status = domain.TaskStatusRunning
	err = suite.UpdateTask(updatedTask)
	require.NoError(t, err)

	// Fail task on worker1 again (third failure)
	err = scheduler.FailTask(context.Background(), task.ID, worker1.ID, "worker1 failure 2")
	require.NoError(t, err)

	finalTask, err := suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, finalTask.Status)
	assert.Equal(t, 2, finalTask.RetryCount)
}

func TestSchedulerService_FailTask_ErrorMessageTracking(t *testing.T) {

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	scheduler := suite.GetSchedulerService()
	require.NotNil(t, scheduler)

	// Create task
	task, err := suite.CreateTaskWithRetries("test-task", 2)
	require.NoError(t, err)
	assert.NotNil(t, task)

	worker := suite.CreateWorker()
	require.NotNil(t, worker)

	// Assign task to worker first
	task.WorkerID = worker.ID
	task.ComputeResourceID = worker.ComputeResourceID
	task.Status = domain.TaskStatusRunning
	err = suite.UpdateTask(task)
	require.NoError(t, err)

	// Fail task with specific error message
	errorMsg := "specific error message for testing"
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, errorMsg)
	require.NoError(t, err)

	updatedTask, err := suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, errorMsg, updatedTask.Error)

	// Assign task to worker again for second failure
	updatedTask.WorkerID = worker.ID
	updatedTask.ComputeResourceID = worker.ComputeResourceID
	updatedTask.Status = domain.TaskStatusRunning
	err = suite.UpdateTask(updatedTask)
	require.NoError(t, err)

	// Fail task again with different error message
	errorMsg2 := "different error message for testing"
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, errorMsg2)
	require.NoError(t, err)

	updatedTask, err = suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, errorMsg2, updatedTask.Error)
}

func TestSchedulerService_FailTask_CompletionTimeTracking(t *testing.T) {

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	scheduler := suite.GetSchedulerService()
	require.NotNil(t, scheduler)

	// Create task
	task, err := suite.CreateTaskWithRetries("test-task", 1)
	require.NoError(t, err)
	assert.NotNil(t, task)

	worker := suite.CreateWorker()
	require.NotNil(t, worker)

	// Assign task to worker first
	task.WorkerID = worker.ID
	task.ComputeResourceID = worker.ComputeResourceID
	task.Status = domain.TaskStatusRunning
	err = suite.UpdateTask(task)
	require.NoError(t, err)

	// Fail task first time
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, "test failure")
	require.NoError(t, err)

	updatedTask, err := suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Nil(t, updatedTask.CompletedAt) // Should not be set for retry

	// Assign task to worker again for second failure
	updatedTask.WorkerID = worker.ID
	updatedTask.ComputeResourceID = worker.ComputeResourceID
	updatedTask.Status = domain.TaskStatusRunning
	err = suite.UpdateTask(updatedTask)
	require.NoError(t, err)

	// Fail task second time (permanent failure)
	err = scheduler.FailTask(context.Background(), task.ID, worker.ID, "test failure 2")
	require.NoError(t, err)

	finalTask, err := suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.NotNil(t, finalTask.CompletedAt) // Should be set for permanent failure
	// Just verify that completed_at is set and is a reasonable time (not zero time)
	assert.False(t, finalTask.CompletedAt.IsZero(), "CompletedAt should not be zero time")
}

func TestSchedulerService_FailTask_ConcurrentFailures(t *testing.T) {

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	scheduler := suite.GetSchedulerService()
	require.NotNil(t, scheduler)

	// Create multiple tasks
	tasks := make([]*domain.Task, 5)
	for i := 0; i < 5; i++ {
		task, err := suite.CreateTaskWithRetries(fmt.Sprintf("test-task-%d", i), 2)
		require.NoError(t, err)
		tasks[i] = task
	}

	worker := suite.CreateWorker()
	require.NotNil(t, worker)

	// Assign all tasks to worker before concurrent failures
	for _, task := range tasks {
		task.WorkerID = worker.ID
		task.ComputeResourceID = worker.ComputeResourceID
		task.Status = domain.TaskStatusRunning
		err := suite.UpdateTask(task)
		require.NoError(t, err)
	}

	// Fail all tasks concurrently
	errors := make(chan error, 5)
	for i, task := range tasks {
		go func(t *domain.Task, index int) {
			err := scheduler.FailTask(context.Background(), t.ID, worker.ID, fmt.Sprintf("concurrent failure %d", index))
			errors <- err
		}(task, i)
	}

	// Wait for all failures to complete
	for i := 0; i < 5; i++ {
		err := <-errors
		require.NoError(t, err)
	}

	// Verify all tasks were handled correctly
	for _, task := range tasks {
		updatedTask, err := suite.GetTask(task.ID)
		require.NoError(t, err)
		assert.Equal(t, domain.TaskStatusQueued, updatedTask.Status)
		assert.Equal(t, 1, updatedTask.RetryCount)
	}
}

func TestSchedulerService_FailTask_InvalidTaskID(t *testing.T) {

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	scheduler := suite.GetSchedulerService()
	require.NotNil(t, scheduler)

	worker := suite.CreateWorker()
	require.NotNil(t, worker)

	// Try to fail non-existent task
	err = scheduler.FailTask(context.Background(), "non-existent-task-id", worker.ID, "test failure")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "task not found")
}

func TestSchedulerService_FailTask_InvalidWorkerID(t *testing.T) {

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	scheduler := suite.GetSchedulerService()
	require.NotNil(t, scheduler)

	// Create task
	task, err := suite.CreateTaskWithRetries("test-task", 1)
	require.NoError(t, err)
	assert.NotNil(t, task)

	// Try to fail task with non-existent worker
	err = scheduler.FailTask(context.Background(), task.ID, "non-existent-worker-id", "test failure")
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "worker not found")
}
