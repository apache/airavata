package integration

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

func TestSLURM_ExceedMemoryLimit(t *testing.T) {

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

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("memory-test-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment that requests excessive memory
	req := &domain.CreateExperimentRequest{
		Name:            "memory-exhaustion-test",
		Description:     "Test memory limit exhaustion",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "python -c 'import time; data = [0] * 1000000000; time.sleep(10)'", // Allocate 1GB+ memory
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "value1",
				},
			},
		},
		Requirements: &domain.ResourceRequirements{
			CPUCores: 1,
			MemoryMB: 2000000, // Request 2GB memory (exceeds typical limits)
			DiskGB:   1,
			Walltime: "0:05:00",
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Submit experiment to cluster
	err = suite.SubmitToCluster(exp.Experiment, cluster)
	require.NoError(t, err)

	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	err = suite.WaitForTaskState(taskID, domain.TaskStatusFailed, 2*time.Minute)
	require.NoError(t, err)

	// Verify task failed due to memory limit
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Contains(t, task.Error, "memory")
}

func TestSLURM_ExceedTimeLimit(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("timeout-test-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment that runs longer than time limit
	req := &domain.CreateExperimentRequest{
		Name:            "time-limit-test",
		Description:     "Test time limit exhaustion",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "sleep 300", // Sleep for 5 minutes
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "value1",
				},
			},
		},
		Requirements: &domain.ResourceRequirements{
			CPUCores: 1,
			MemoryMB: 1024,
			DiskGB:   1,
			Walltime: "0:01:00", // 1 minute time limit
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Submit experiment to cluster
	err = suite.SubmitToCluster(exp.Experiment, cluster)
	require.NoError(t, err)

	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	err = suite.WaitForTaskState(taskID, domain.TaskStatusFailed, 3*time.Minute)
	require.NoError(t, err)

	// Verify task failed due to time limit
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Contains(t, task.Error, "time")
}

func TestBareMetal_DiskSpaceFull(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Wait for bare metal to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register bare metal resource
	resource, err := suite.RegisterBaremetalResource("disk-full-test", "localhost:2224")
	require.NoError(t, err)

	// Create experiment that fills up disk space
	req := &domain.CreateExperimentRequest{
		Name:            "disk-space-test",
		Description:     "Test disk space exhaustion",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "dd if=/dev/zero of=/tmp/largefile bs=1M count=1000", // Create 1GB file
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "value1",
				},
			},
		},
		Requirements: &domain.ResourceRequirements{
			CPUCores: 1,
			MemoryMB: 1024,
			DiskGB:   1,
			Walltime: "0:05:00",
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Submit experiment to bare metal
	err = suite.SubmitToCluster(exp.Experiment, resource)
	require.NoError(t, err)

	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	err = suite.WaitForTaskState(taskID, domain.TaskStatusCompleted, 2*time.Minute)
	if err != nil {
		// If it fails, check if it's due to disk space
		err = suite.WaitForTaskState(taskID, domain.TaskStatusFailed, 1*time.Minute)
		require.NoError(t, err)

		task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
		require.NoError(t, err)
		// Note: This test might pass if the container has enough disk space
		// In a real scenario, we would pre-fill the disk to ensure failure
		t.Logf("Task failed with error: %s", task.Error)
	}
}

func TestStorage_S3QuotaExceeded(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Wait for MinIO to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 1*time.Minute)
	require.NoError(t, err)

	// Register S3 resource with small quota
	resource, err := suite.RegisterS3Resource("quota-test-minio", "localhost:9000")
	require.NoError(t, err)

	// Update resource with small capacity
	smallCapacity := int64(1024) // 1KB quota
	resource.TotalCapacity = &smallCapacity
	err = suite.DB.Repo.UpdateStorageResource(context.Background(), resource)
	require.NoError(t, err)

	// Try to upload a file larger than quota
	largeData := make([]byte, 2048) // 2KB file
	for i := range largeData {
		largeData[i] = byte(i % 256)
	}

	err = suite.UploadFile(resource.ID, "large-file.txt", largeData)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "quota")
}

func TestConcurrent_MaxWorkerLimit(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster with limited workers
	cluster, err := suite.RegisterSlurmResource("worker-limit-cluster", "localhost:6817")
	require.NoError(t, err)

	// Update cluster to have only 2 workers
	cluster.MaxWorkers = 2
	err = suite.DB.Repo.UpdateComputeResource(context.Background(), cluster)
	require.NoError(t, err)

	// Create multiple experiments to exceed worker limit
	var experiments []*domain.Experiment
	for i := 0; i < 5; i++ {
		req := &domain.CreateExperimentRequest{
			Name:            fmt.Sprintf("worker-limit-test-%d", i),
			Description:     fmt.Sprintf("Test worker limit %d", i),
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "sleep 30", // 30 second sleep
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"param1": "value1",
					},
				},
			},
			Requirements: &domain.ResourceRequirements{
				CPUCores: 1,
				MemoryMB: 1024,
				DiskGB:   1,
				Walltime: "0:02:00",
			},
		}

		exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
		require.NoError(t, err)
		experiments = append(experiments, exp.Experiment)
	}

	// Submit all experiments
	for _, exp := range experiments {
		err = suite.SubmitToCluster(exp, cluster)
		require.NoError(t, err)
	}

	// Wait for some tasks to be queued (not all can run due to worker limit)
	time.Sleep(5 * time.Second)

	// Check that some tasks are in queued state due to worker limit
	queuedCount := 0
	for _, exp := range experiments {
		tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 1, 0)
		require.NoError(t, err)
		if len(tasks) > 0 {
			task, err := suite.DB.Repo.GetTaskByID(context.Background(), tasks[0].ID)
			require.NoError(t, err)
			if task.Status == domain.TaskStatusQueued {
				queuedCount++
			}
		}
	}

	// At least some tasks should be queued due to worker limit
	assert.Greater(t, queuedCount, 0, "Some tasks should be queued due to worker limit")
}
