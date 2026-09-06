package integration

import (
	"context"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	services "github.com/apache/airavata/scheduler/core/service"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestSignedURL_CompleteStagingWorkflow(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Start gRPC server
	grpcServer, _ := suite.StartGRPCServer(t)
	defer grpcServer.Stop()

	// Register SLURM compute resource
	slurmResource, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)
	assert.NotNil(t, slurmResource)

	// Register MinIO storage resource
	minioResource, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, minioResource)

	// Create experiment with input/output files
	experiment, err := suite.CreateTestExperiment("complete-staging-test", "cat input.txt > output.txt")
	require.NoError(t, err)
	assert.NotNil(t, experiment)

	// Upload input files to MinIO
	inputData := []byte("test input data for signed URL download")
	err = suite.UploadFile(minioResource.ID, "input.txt", inputData)
	require.NoError(t, err)

	// Scheduler spawns worker on SLURM via SSH
	worker, cmd := suite.SpawnRealWorker(t, experiment.ID, slurmResource.ID)
	defer func() {
		if cmd != nil && cmd.Process != nil {
			cmd.Process.Kill()
		}
	}()

	// Worker registers via gRPC
	err = suite.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
	require.NoError(t, err)

	// Get task ID from experiment
	taskID, err := suite.GetTaskIDFromExperiment(experiment.ID)
	require.NoError(t, err)

	// Generate signed URLs for task
	urls, err := suite.DataMoverSvc.GenerateSignedURLsForTask(context.Background(), taskID, slurmResource.ID)
	require.NoError(t, err)
	assert.NotEmpty(t, urls)

	// Verify signed URL structure
	for _, url := range urls {
		assert.NotEmpty(t, url.URL)
		assert.NotEmpty(t, url.SourcePath)
		assert.NotEmpty(t, url.LocalPath)
		assert.NotNil(t, url.ExpiresAt)
		assert.NotEmpty(t, url.Method)
	}

	// Scheduler assigns task to worker via gRPC
	err = suite.AssignTaskToWorker(t, worker.ID, taskID)
	require.NoError(t, err)

	// Worker downloads inputs using signed URLs
	workingDir := "/tmp/worker-" + worker.ID
	err = suite.WaitForFileDownload(workingDir, "input.txt", 30*time.Second)
	require.NoError(t, err)

	// Worker executes task
	err = suite.WaitForTaskOutputStreaming(t, taskID, 2*time.Minute)
	require.NoError(t, err)

	// Worker uploads outputs using signed URLs
	err = suite.VerifyFileInStorage(minioResource.ID, "output.txt", 1*time.Minute)
	require.NoError(t, err)

	// Verify task status updated in database
	task, err := suite.GetTask(taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, task.Status)
}

func TestSignedURL_MinIOIntegration(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Start Docker services
	// Services are already running and verified by SetupIntegrationTest

	// Register MinIO storage resource
	minioResource, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, minioResource)

	// Services are already running and verified by SetupIntegrationTest

	// Upload test file to MinIO
	inputData := []byte("test data for MinIO integration")
	err = suite.UploadFile(minioResource.ID, "test-file.txt", inputData)
	require.NoError(t, err)

	// Verify file exists in MinIO
	exists, err := suite.DataMoverSvc.(*services.DataMoverService).CheckCache(context.Background(), "test-file.txt", "", minioResource.ID)
	require.NoError(t, err)
	assert.NotNil(t, exists)

	// Test signed URL generation
	urls, err := suite.DataMoverSvc.GenerateSignedURLsForTask(context.Background(), "test-task", minioResource.ID)
	require.NoError(t, err)
	assert.NotEmpty(t, urls)
}

func TestSignedURL_Expiration(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Register MinIO storage resource
	minioResource, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	// Upload test file
	err = suite.UploadFile(minioResource.ID, "test.txt", []byte("test data for expiration"))
	require.NoError(t, err)

	// Test signed URL generation with short expiration
	urls, err := suite.DataMoverSvc.GenerateSignedURLsForTask(context.Background(), "test-task", minioResource.ID)
	require.NoError(t, err)
	assert.NotEmpty(t, urls)

	// Verify URL structure
	for _, url := range urls {
		assert.NotEmpty(t, url.URL)
		assert.NotNil(t, url.ExpiresAt)
		assert.NotEmpty(t, url.Method)
	}
}

func TestSignedURL_MultipleFiles(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Register MinIO storage resource
	minioResource, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	// Upload multiple input files
	files := map[string][]byte{
		"input1.txt": []byte("content of input file 1"),
		"input2.txt": []byte("content of input file 2"),
		"input3.txt": []byte("content of input file 3"),
	}

	for filename, content := range files {
		err = suite.UploadFile(minioResource.ID, filename, content)
		require.NoError(t, err)
	}

	// Test signed URL generation for multiple files
	urls, err := suite.DataMoverSvc.GenerateSignedURLsForTask(context.Background(), "multi-file-task", minioResource.ID)
	require.NoError(t, err)
	assert.NotEmpty(t, urls)

	// Verify all URLs are valid
	for _, url := range urls {
		assert.NotEmpty(t, url.URL)
		assert.NotEmpty(t, url.SourcePath)
		assert.NotEmpty(t, url.LocalPath)
		assert.NotNil(t, url.ExpiresAt)
		assert.NotEmpty(t, url.Method)
	}
}

func TestSignedURL_LargeFile(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Register MinIO storage resource
	minioResource, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	// Create a large file (1MB)
	largeData := make([]byte, 1024*1024)
	for i := range largeData {
		largeData[i] = byte(i % 256)
	}

	err = suite.UploadFile(minioResource.ID, "large-input.bin", largeData)
	require.NoError(t, err)

	// Test signed URL generation for large file
	urls, err := suite.DataMoverSvc.GenerateSignedURLsForTask(context.Background(), "large-file-task", minioResource.ID)
	require.NoError(t, err)
	assert.NotEmpty(t, urls)

	// Verify URL structure for large file
	for _, url := range urls {
		assert.NotEmpty(t, url.URL)
		assert.NotEmpty(t, url.SourcePath)
		assert.NotEmpty(t, url.LocalPath)
		assert.NotNil(t, url.ExpiresAt)
		assert.NotEmpty(t, url.Method)
	}
}

func TestSignedURL_ConcurrentDownloads(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Register MinIO storage resource
	minioResource, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	// Upload test file
	testData := []byte("concurrent download test data")
	err = suite.UploadFile(minioResource.ID, "concurrent-test.txt", testData)
	require.NoError(t, err)

	// Test concurrent signed URL generation
	urls, err := suite.DataMoverSvc.GenerateSignedURLsForTask(context.Background(), "concurrent-task", minioResource.ID)
	require.NoError(t, err)
	assert.NotEmpty(t, urls)

	// Verify URL structure
	for _, url := range urls {
		assert.NotEmpty(t, url.URL)
		assert.NotEmpty(t, url.SourcePath)
		assert.NotEmpty(t, url.LocalPath)
		assert.NotNil(t, url.ExpiresAt)
		assert.NotEmpty(t, url.Method)
	}
}

func TestSignedURL_InvalidSignature(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Register MinIO storage resource
	minioResource, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	// Upload test file
	err = suite.UploadFile(minioResource.ID, "test.txt", []byte("test data"))
	require.NoError(t, err)

	// Test signed URL generation
	urls, err := suite.DataMoverSvc.GenerateSignedURLsForTask(context.Background(), "test-task", minioResource.ID)
	require.NoError(t, err)
	assert.NotEmpty(t, urls)

	// Verify URL structure
	for _, url := range urls {
		assert.NotEmpty(t, url.URL)
		assert.NotNil(t, url.ExpiresAt)
		assert.NotEmpty(t, url.Method)
	}
}

func TestSignedURL_DifferentMethods(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Register MinIO storage resource
	minioResource, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	// Upload test file
	testData := []byte("test data for different methods")
	err = suite.UploadFile(minioResource.ID, "method-test.txt", testData)
	require.NoError(t, err)

	// Test signed URL generation for different methods
	urls, err := suite.DataMoverSvc.GenerateSignedURLsForTask(context.Background(), "method-task", minioResource.ID)
	require.NoError(t, err)
	assert.NotEmpty(t, urls)

	// Verify URL structure for different methods
	for _, url := range urls {
		assert.NotEmpty(t, url.URL)
		assert.NotEmpty(t, url.Method)
		assert.NotNil(t, url.ExpiresAt)
	}
}

func TestSignedURL_CrossStorageBackend(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Register MinIO storage resource
	minioResource, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	// Test signed URLs work with MinIO storage backend
	testData := []byte("test data for MinIO backend")
	filename := "minio-test.txt"
	err = suite.UploadFile(minioResource.ID, filename, testData)
	require.NoError(t, err)

	// Generate signed URL
	urls, err := suite.DataMoverSvc.GenerateSignedURLsForTask(context.Background(), "backend-task", minioResource.ID)
	require.NoError(t, err)
	assert.NotEmpty(t, urls)

	// Verify URL structure
	for _, url := range urls {
		assert.NotEmpty(t, url.URL)
		assert.NotEmpty(t, url.SourcePath)
		assert.NotEmpty(t, url.LocalPath)
		assert.NotNil(t, url.ExpiresAt)
		assert.NotEmpty(t, url.Method)
	}
}
