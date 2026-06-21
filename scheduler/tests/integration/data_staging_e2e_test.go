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

func TestDataStaging_InputStaging(t *testing.T) {
	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Register storage and compute resources
	storageResource, err := suite.RegisterS3Resource("test-s3", "localhost:9000")
	require.NoError(t, err)

	computeResource, err := suite.RegisterSlurmResource("test-slurm", "localhost:6817")
	require.NoError(t, err)

	// Create test user and project
	user, err := suite.Builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)
	suite.TestUser = user

	project, err := suite.Builder.CreateProject("test-project", user.ID, "Test project for staging").Build()
	require.NoError(t, err)
	suite.TestProject = project

	// Upload test files to central storage (S3/MinIO)
	testFiles := []testutil.TestInputFile{
		{Path: "/test/input1.txt", Content: "Hello World from input1", Checksum: "a1b2c3d4e5f6"},
		{Path: "/test/input2.txt", Content: "Hello World from input2", Checksum: "f6e5d4c3b2a1"},
	}

	for _, file := range testFiles {
		err := suite.UploadFileToStorage(storageResource.ID, file.Path, file.Content)
		require.NoError(t, err)
	}

	// Create experiment with input files
	exp, err := suite.CreateTestExperimentWithInputs("staging-test", "cat input1.txt input2.txt > output.txt", testFiles)
	require.NoError(t, err)

	// Submit experiment
	err = suite.SubmitExperiment(exp)
	require.NoError(t, err)

	// Get tasks
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 10, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)

	task := tasks[0]

	// Test 1: Input staging (central → compute node)
	t.Run("InputStaging", func(t *testing.T) {
		// Trigger staging to compute resource
		stagingOp, err := suite.DataMoverSvc.BeginProactiveStaging(context.Background(), task.ID, computeResource.ID, user.ID)
		require.NoError(t, err)
		assert.NotNil(t, stagingOp)

		// Wait for staging to complete
		err = suite.WaitForStagingCompletion(stagingOp.ID, 2*time.Minute)
		require.NoError(t, err)

		// Verify files arrive with correct checksums
		for _, file := range testFiles {
			destPath := fmt.Sprintf("/tmp/task_%s/%s", task.ID, file.Path)
			content, err := suite.GetFileFromComputeResource(computeResource.ID, destPath)
			require.NoError(t, err)
			assert.Equal(t, file.Content, content)

			// Verify checksum
			checksum, err := suite.CalculateFileChecksum(computeResource.ID, destPath)
			require.NoError(t, err)
			assert.Equal(t, file.Checksum, checksum)
		}
	})

	// Test 2: Task execution with staged inputs
	t.Run("TaskExecutionWithStagedInputs", func(t *testing.T) {
		// Create task directory
		_, err = suite.CreateTaskDirectory(task.ID, task.ComputeResourceID)
		require.NoError(t, err)

		// Stage worker binary
		err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
		require.NoError(t, err)

		// Submit SLURM job
		err = suite.SubmitSlurmJob(task.ID)
		require.NoError(t, err)

		// Start task monitoring
		err = suite.StartTaskMonitoring(task.ID)
		require.NoError(t, err)

		// Wait for task completion
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

		// Verify output file was created
		outputPath := fmt.Sprintf("/tmp/task_%s/output.txt", task.ID)
		output, err := suite.GetFileFromComputeResource(computeResource.ID, outputPath)
		require.NoError(t, err)
		assert.Contains(t, output, "Hello World from input1")
		assert.Contains(t, output, "Hello World from input2")
	})

	// Test 3: Output staging (compute node → central)
	t.Run("OutputStaging", func(t *testing.T) {
		// Stage outputs back to central storage
		outputFiles := []string{"/tmp/task_" + task.ID + "/output.txt"}
		err = suite.StageOutputsToCentral(task.ID, outputFiles)
		require.NoError(t, err)

		// Verify outputs in experiment output directory
		outputPath := fmt.Sprintf("/experiments/%s/outputs/%s/output.txt", exp.ID, task.ID)
		content, err := suite.GetFileFromCentralStorage(storageResource.ID, outputPath)
		require.NoError(t, err)
		assert.Contains(t, content, "Hello World from input1")
		assert.Contains(t, content, "Hello World from input2")

		// Check data lineage records
		lineage, err := suite.GetDataLineage(task.ID)
		require.NoError(t, err)
		assert.Len(t, lineage, 3) // 2 input files + 1 output file
	})
}

func TestDataStaging_CrossStorage(t *testing.T) {
	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Register multiple storage resources
	s3Resource, err := suite.RegisterS3Resource("test-s3", "localhost:9000")
	require.NoError(t, err)

	sftpResource, err := suite.RegisterSFTPResource("test-sftp", "localhost:2222")
	require.NoError(t, err)

	nfsResource, err := suite.RegisterS3Resource("test-nfs", "localhost:2049")
	require.NoError(t, err)

	// Register compute resources
	slurmResource, err := suite.RegisterSlurmResource("test-slurm", "localhost:6817")
	require.NoError(t, err)

	k8sResource, err := suite.RegisterKubernetesResource("test-k8s")
	require.NoError(t, err)

	baremetalResource, err := suite.RegisterBaremetalResource("test-baremetal", "localhost:2225")
	require.NoError(t, err)

	// Create test user and project
	user, err := suite.Builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)
	suite.TestUser = user

	project, err := suite.Builder.CreateProject("test-project", user.ID, "Test project for cross-storage staging").Build()
	require.NoError(t, err)
	suite.TestProject = project

	// Test combinations
	testCases := []struct {
		name            string
		inputStorage    *domain.StorageResource
		computeResource *domain.ComputeResource
		outputStorage   *domain.StorageResource
	}{
		{
			name:            "S3_to_SLURM_to_NFS",
			inputStorage:    s3Resource,
			computeResource: slurmResource,
			outputStorage:   nfsResource,
		},
		{
			name:            "SFTP_to_K8s_to_S3",
			inputStorage:    sftpResource,
			computeResource: k8sResource,
			outputStorage:   s3Resource,
		},
		{
			name:            "NFS_to_BareMetal_to_SFTP",
			inputStorage:    nfsResource,
			computeResource: baremetalResource,
			outputStorage:   sftpResource,
		},
	}

	for _, tc := range testCases {
		t.Run(tc.name, func(t *testing.T) {
			// Upload input file to input storage
			inputPath := "/test/input.txt"
			inputContent := fmt.Sprintf("Input for %s", tc.name)
			err := suite.UploadFileToStorage(tc.inputStorage.ID, inputPath, inputContent)
			require.NoError(t, err)

			// Create experiment
			testFiles := []testutil.TestInputFile{
				{Path: inputPath, Content: inputContent, Checksum: "test123"},
			}

			exp, err := suite.CreateTestExperimentWithInputs("cross-storage-test", "echo 'Processing input' > output.txt", testFiles)
			require.NoError(t, err)

			// Submit experiment
			err = suite.SubmitExperiment(exp)
			require.NoError(t, err)

			// Get tasks
			tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 10, 0)
			require.NoError(t, err)
			require.Len(t, tasks, 1)

			task := tasks[0]

			// Stage inputs
			stagingOp, err := suite.DataMoverSvc.BeginProactiveStaging(context.Background(), task.ID, tc.computeResource.ID, user.ID)
			require.NoError(t, err)

			err = suite.WaitForStagingCompletion(stagingOp.ID, 2*time.Minute)
			require.NoError(t, err)

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

			// Stage outputs to output storage
			outputFiles := []string{"/tmp/task_" + task.ID + "/output.txt"}
			err = suite.StageOutputsToCentral(task.ID, outputFiles)
			require.NoError(t, err)

			// Verify output in output storage
			outputPath := fmt.Sprintf("/experiments/%s/outputs/%s/output.txt", exp.ID, task.ID)
			content, err := suite.GetFileFromCentralStorage(tc.outputStorage.ID, outputPath)
			require.NoError(t, err)
			assert.Contains(t, content, "Processing input")
		})
	}
}

func TestDataStaging_RetryLogic(t *testing.T) {
	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Register resources
	storageResource, err := suite.RegisterS3Resource("test-s3", "localhost:9000")
	require.NoError(t, err)

	computeResource, err := suite.RegisterSlurmResource("test-slurm", "localhost:6817")
	require.NoError(t, err)

	// Create test user and project
	user, err := suite.Builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)
	suite.TestUser = user

	project, err := suite.Builder.CreateProject("test-project", user.ID, "Test project for staging retry").Build()
	require.NoError(t, err)
	suite.TestProject = project

	// Upload large file to test retry logic
	largeContent := make([]byte, 1024*1024) // 1MB
	for i := range largeContent {
		largeContent[i] = byte(i % 256)
	}

	err = suite.UploadFileToStorage(storageResource.ID, "/test/large_file.bin", string(largeContent))
	require.NoError(t, err)

	// Create experiment
	testFiles := []testutil.TestInputFile{
		{Path: "/test/large_file.bin", Content: string(largeContent), Checksum: "large123"},
	}

	exp, err := suite.CreateTestExperimentWithInputs("retry-test", "ls -la large_file.bin > output.txt", testFiles)
	require.NoError(t, err)

	// Submit experiment
	err = suite.SubmitExperiment(exp)
	require.NoError(t, err)

	// Get tasks
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 10, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)

	task := tasks[0]

	// Test staging with retry logic
	stagingOp, err := suite.DataMoverSvc.BeginProactiveStaging(context.Background(), task.ID, computeResource.ID, user.ID)
	require.NoError(t, err)

	// Wait for staging to complete (with retry logic)
	err = suite.WaitForStagingCompletion(stagingOp.ID, 5*time.Minute)
	require.NoError(t, err)

	// Verify file was staged correctly
	destPath := fmt.Sprintf("/tmp/task_%s/large_file.bin", task.ID)
	content, err := suite.GetFileFromComputeResource(computeResource.ID, destPath)
	require.NoError(t, err)
	assert.Equal(t, string(largeContent), content)
}
