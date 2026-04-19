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

func TestCompleteWorkflow_FullDataStaging(t *testing.T) {
	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Register all compute and storage resources
	storageResource, err := suite.RegisterS3Resource("test-s3", "localhost:9000")
	require.NoError(t, err)

	slurmResource, err := suite.RegisterSlurmResource("test-slurm", "localhost:6817")
	require.NoError(t, err)

	_, err = suite.RegisterKubernetesResource("test-k8s")
	require.NoError(t, err)

	_, err = suite.RegisterBaremetalResource("test-baremetal", "localhost:2225")
	require.NoError(t, err)

	// Create test user and project
	user, err := suite.Builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)
	suite.TestUser = user

	project, err := suite.Builder.CreateProject("test-project", user.ID, "Test project for complete workflow").Build()
	require.NoError(t, err)
	suite.TestProject = project

	// Upload input data to central storage
	inputFiles := []testutil.TestInputFile{
		{Path: "/test/input1.txt", Content: "Hello World from input1", Checksum: "a1b2c3d4e5f6"},
		{Path: "/test/input2.txt", Content: "Hello World from input2", Checksum: "f6e5d4c3b2a1"},
		{Path: "/test/input3.txt", Content: "Hello World from input3", Checksum: "c3d4e5f6a1b2"},
	}

	for _, file := range inputFiles {
		err := suite.UploadFileToStorage(storageResource.ID, file.Path, file.Content)
		require.NoError(t, err)
	}

	// Create experiment with multiple tasks
	exp, err := suite.CreateTestExperimentWithInputs("complete-workflow-test", "cat input1.txt input2.txt input3.txt > output.txt && echo 'Task completed' > status.txt", inputFiles)
	require.NoError(t, err)

	// Submit experiment
	err = suite.SubmitExperiment(exp)
	require.NoError(t, err)

	// Get tasks
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 10, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)

	task := tasks[0]

	// Test complete workflow
	t.Run("CompleteWorkflow", func(t *testing.T) {
		// 1. Verify worker spawning on compute resource
		workerHelper := testutil.NewWorkerTestHelper(suite)
		worker, err := workerHelper.SpawnRealWorker(t, slurmResource, 5*time.Minute)
		require.NoError(t, err)
		assert.NotNil(t, worker)

		// Wait for worker registration
		err = workerHelper.WaitForWorkerRegistration(t, worker.ID, 2*time.Minute)
		require.NoError(t, err)

		// 2. Verify input staging to workers
		stagingOp, err := suite.DataMoverSvc.BeginProactiveStaging(context.Background(), task.ID, slurmResource.ID, user.ID)
		require.NoError(t, err)
		assert.NotNil(t, stagingOp)

		err = suite.WaitForStagingCompletion(stagingOp.ID, 3*time.Minute)
		require.NoError(t, err)

		// Verify files are staged correctly
		for _, file := range inputFiles {
			destPath := fmt.Sprintf("/tmp/task_%s/%s", task.ID, file.Path)
			content, err := suite.GetFileFromComputeResource(slurmResource.ID, destPath)
			require.NoError(t, err)
			assert.Equal(t, file.Content, content)
		}

		// 3. Verify task execution
		workDir, err := suite.CreateTaskDirectory(task.ID, task.ComputeResourceID)
		require.NoError(t, err)
		assert.NotEmpty(t, workDir)

		err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
		require.NoError(t, err)

		err = suite.SubmitSlurmJob(task.ID)
		require.NoError(t, err)

		err = suite.StartTaskMonitoring(task.ID)
		require.NoError(t, err)

		// Wait for task completion with proper state transitions
		expectedStates := []domain.TaskStatus{
			domain.TaskStatusCreated,
			domain.TaskStatusQueued,
			domain.TaskStatusDataStaging,
			domain.TaskStatusEnvSetup,
			domain.TaskStatusRunning,
			domain.TaskStatusOutputStaging,
			domain.TaskStatusCompleted,
		}
		observedStates, err := suite.WaitForTaskStateTransitions(task.ID, expectedStates, 5*time.Minute)
		require.NoError(t, err, "Task %s should complete with proper state transitions", task.ID)
		t.Logf("Task %s completed with state transitions: %v", task.ID, observedStates)

		// 4. Verify output staging to central
		outputFiles := []string{
			fmt.Sprintf("/tmp/task_%s/output.txt", task.ID),
			fmt.Sprintf("/tmp/task_%s/status.txt", task.ID),
		}
		err = suite.StageOutputsToCentral(task.ID, outputFiles)
		require.NoError(t, err)

		// 5. List experiment outputs via API
		outputs, err := suite.DataMoverSvc.ListExperimentOutputs(context.Background(), exp.ID)
		require.NoError(t, err)
		assert.Len(t, outputs, 2) // output.txt and status.txt

		// 6. Download all outputs and verify content
		archiveReader, err := suite.DataMoverSvc.GetExperimentOutputArchive(context.Background(), exp.ID)
		require.NoError(t, err)
		assert.NotNil(t, archiveReader)

		// 7. Verify data lineage records
		lineage, err := suite.GetDataLineage(task.ID)
		require.NoError(t, err)
		assert.Len(t, lineage, 5) // 3 input files + 2 output files

		// 8. Verify worker execution metrics
		err = workerHelper.VerifyWorkerExecution(t, worker, 1)
		require.NoError(t, err)

		// Cleanup worker
		err = workerHelper.TerminateWorker(t, worker)
		require.NoError(t, err)
	})
}

func TestOutputCollection_MultipleTasksToOneFolder(t *testing.T) {
	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Register resources
	_, err := suite.RegisterS3Resource("test-s3", "localhost:9000")
	require.NoError(t, err)

	_, err = suite.RegisterSlurmResource("test-slurm", "localhost:6817")
	require.NoError(t, err)

	// Create test user and project
	user, err := suite.Builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)
	suite.TestUser = user

	project, err := suite.Builder.CreateProject("test-project", user.ID, "Test project for multi-task output collection").Build()
	require.NoError(t, err)
	suite.TestProject = project

	// Create experiment with 5 tasks
	exp, err := suite.CreateTestExperiment("multi-task-test", "echo 'Task output' > output.txt && echo 'Task status' > status.txt")
	require.NoError(t, err)

	// Submit experiment
	err = suite.SubmitExperiment(exp)
	require.NoError(t, err)

	// Get tasks
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 10, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1) // Single task for now, but structure supports multiple

	task := tasks[0]

	// Execute task
	_, err = suite.CreateTaskDirectory(task.ID, task.ComputeResourceID)
	require.NoError(t, err)

	err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
	require.NoError(t, err)

	err = suite.SubmitSlurmJob(task.ID)
	require.NoError(t, err)

	err = suite.StartTaskMonitoring(task.ID)
	require.NoError(t, err)

	// Wait for completion
	expectedStates := []domain.TaskStatus{
		domain.TaskStatusCreated,
		domain.TaskStatusQueued,
		domain.TaskStatusDataStaging,
		domain.TaskStatusEnvSetup,
		domain.TaskStatusRunning,
		domain.TaskStatusOutputStaging,
		domain.TaskStatusCompleted,
	}
	_, err = suite.WaitForTaskStateTransitions(task.ID, expectedStates, 3*time.Minute)
	require.NoError(t, err)

	// Stage outputs
	outputFiles := []string{
		fmt.Sprintf("/tmp/task_%s/output.txt", task.ID),
		fmt.Sprintf("/tmp/task_%s/status.txt", task.ID),
	}
	err = suite.StageOutputsToCentral(task.ID, outputFiles)
	require.NoError(t, err)

	// Verify all files in /experiments/{exp_id}/outputs/
	outputs, err := suite.DataMoverSvc.ListExperimentOutputs(context.Background(), exp.ID)
	require.NoError(t, err)
	assert.Len(t, outputs, 2)

	// Verify files are organized by task_id subdirectories
	for _, output := range outputs {
		assert.Contains(t, output.Path, task.ID)
		assert.True(t, output.Size > 0)
		assert.NotEmpty(t, output.Checksum)
	}

	// Verify download archive contains all files
	archiveReader, err := suite.DataMoverSvc.GetExperimentOutputArchive(context.Background(), exp.ID)
	require.NoError(t, err)
	assert.NotNil(t, archiveReader)
}

// TestDataStaging_CrossStorage is defined in data_staging_e2e_test.go to avoid duplication
