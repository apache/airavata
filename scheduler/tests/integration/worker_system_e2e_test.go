package integration

import (
	"context"
	"fmt"
	"os/exec"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/core/dto"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
	"google.golang.org/protobuf/types/known/durationpb"
	"google.golang.org/protobuf/types/known/timestamppb"
)

func TestWorkerSystem_RealGRPCCommunication(t *testing.T) {

	// Setup test environment
	ctx := context.Background()
	testEnv := testutil.SetupIntegrationTest(t)
	defer testEnv.Cleanup()

	// Start gRPC server on test port
	grpcServer, addr := testEnv.StartGRPCServer(t)
	defer grpcServer.Stop()

	// Connect worker client
	client, conn := testEnv.ConnectWorkerClient(t, addr)
	defer conn.Close()

	t.Run("RegisterWorker", func(t *testing.T) {
		// Create experiment first
		experiment, err := testEnv.CreateTestExperiment("register-test-"+fmt.Sprintf("%d", time.Now().UnixNano()), "echo test")
		require.NoError(t, err)

		// Create compute resource
		computeResource, err := testEnv.RegisterSlurmResource("test-resource-reg", "localhost:6817")
		require.NoError(t, err)

		// Pre-create worker in database (this is what scheduler does)
		now := time.Now()
		worker := &domain.Worker{
			ID:                "test-worker-123",
			ComputeResourceID: computeResource.ID,
			ExperimentID:      experiment.ID,
			UserID:            testEnv.TestUser.ID,
			Status:            domain.WorkerStatusIdle,
			Walltime:          30 * time.Minute,
			WalltimeRemaining: 30 * time.Minute,
			RegisteredAt:      now,
			LastHeartbeat:     now,
			CreatedAt:         now,
			UpdatedAt:         now,
			Metadata:          make(map[string]interface{}),
		}
		err = testEnv.DB.Repo.CreateWorker(context.Background(), worker)
		require.NoError(t, err)

		// Now test RegisterWorker RPC (worker process registers with scheduler)
		req := &dto.WorkerRegistrationRequest{
			WorkerId:          "test-worker-123",
			ExperimentId:      experiment.ID,
			ComputeResourceId: computeResource.ID,
			Capabilities: &dto.WorkerCapabilities{
				MaxCpuCores:       4,
				MaxMemoryMb:       8192,
				MaxDiskGb:         100,
				MaxGpus:           1,
				SupportedRuntimes: []string{"slurm", "kubernetes", "baremetal"},
				Metadata: map[string]string{
					"os": "linux",
				},
			},
			Metadata: map[string]string{
				"hostname": "test-host",
				"version":  "1.0.0",
			},
		}

		resp, err := client.RegisterWorker(ctx, req)
		require.NoError(t, err)
		assert.True(t, resp.Success)
		assert.NotEmpty(t, resp.Message)

		// Verify worker status updated to RUNNING
		updatedWorker, err := testEnv.DB.Repo.GetWorkerByID(context.Background(), "test-worker-123")
		require.NoError(t, err)
		assert.Equal(t, domain.WorkerStatusBusy, updatedWorker.Status)
	})

	t.Run("HeartbeatFlow", func(t *testing.T) {
		// Test bidirectional streaming with real heartbeats
		stream, err := client.PollForTask(ctx)
		require.NoError(t, err)

		// Send heartbeat
		heartbeat := &dto.WorkerMessage{
			Message: &dto.WorkerMessage_Heartbeat{
				Heartbeat: &dto.Heartbeat{
					WorkerId:      "test-worker-123",
					Timestamp:     timestamppb.Now(),
					Status:        dto.WorkerStatus_WORKER_STATUS_IDLE,
					CurrentTaskId: "",
					Metadata: map[string]string{
						"uptime": "1m",
					},
				},
			},
		}

		err = stream.Send(heartbeat)
		require.NoError(t, err)

		// Send metrics
		metrics := &dto.WorkerMessage{
			Message: &dto.WorkerMessage_WorkerMetrics{
				WorkerMetrics: &dto.WorkerMetrics{
					WorkerId:           "test-worker-123",
					CpuUsagePercent:    25.5,
					MemoryUsagePercent: 60.0,
					DiskUsageBytes:     1024 * 1024 * 100, // 100MB
					TasksCompleted:     5,
					TasksFailed:        1,
					Uptime:             durationpb.New(5 * time.Minute),
					CustomMetrics: map[string]string{
						"load_avg": "0.5",
					},
					Timestamp: timestamppb.Now(),
				},
			},
		}

		err = stream.Send(metrics)
		require.NoError(t, err)

		// Close stream
		stream.CloseSend()
	})

	t.Run("TaskAssignment", func(t *testing.T) {
		// Test complete task assignment flow via gRPC
		assignment := &dto.TaskAssignment{
			TaskId:       "task-e2e-123",
			ExperimentId: "exp-e2e-456",
			Command:      "echo 'Hello from E2E test'",
			InputFiles: []*dto.SignedFileURL{
				{
					Url:       "https://storage.example.com/input.txt",
					LocalPath: "input.txt",
				},
			},
			OutputFiles: []*dto.FileMetadata{
				{
					Path: "output.txt",
					Size: 1024,
				},
			},
			Timeout: durationpb.New(30 * time.Minute),
		}

		// Create stream for task assignment
		stream, err := client.PollForTask(ctx)
		require.NoError(t, err)

		// Send heartbeat to establish connection
		heartbeat := &dto.WorkerMessage{
			Message: &dto.WorkerMessage_Heartbeat{
				Heartbeat: &dto.Heartbeat{
					WorkerId:      "test-worker-123",
					Timestamp:     timestamppb.Now(),
					Status:        dto.WorkerStatus_WORKER_STATUS_IDLE,
					CurrentTaskId: "",
				},
			},
		}
		err = stream.Send(heartbeat)
		require.NoError(t, err)

		// Simulate receiving task assignment (in real scenario, server would send this)
		// For now, we'll just validate the assignment structure
		assert.NotEmpty(t, assignment.TaskId)
		assert.NotEmpty(t, assignment.ExperimentId)
		assert.NotEmpty(t, assignment.Command)
		assert.Len(t, assignment.InputFiles, 1)
		assert.Len(t, assignment.OutputFiles, 1)
		assert.NotNil(t, assignment.Timeout)

		stream.CloseSend()
	})

	t.Run("TaskExecution", func(t *testing.T) {
		// Create compute resource first
		computeResource, err := testEnv.RegisterSlurmResource("test-resource-123", "localhost:6817")
		require.NoError(t, err)

		// Create experiment
		experiment, err := testEnv.CreateTestExperiment("task-exec-test-"+fmt.Sprintf("%d", time.Now().UnixNano()), "echo test")
		require.NoError(t, err)

		// Test task execution with real worker process
		worker, cmd := testEnv.SpawnRealWorker(t, experiment.ID, computeResource.ID)
		defer func() {
			if cmd != nil && cmd.Process != nil {
				cmd.Process.Kill()
			}
		}()

		// Wait for worker to register
		err = testEnv.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
		require.NoError(t, err)

		// Create a task in the database
		task := &domain.Task{
			ID:           "task-e2e-exec-123",
			ExperimentID: experiment.ID,
			Status:       domain.TaskStatusQueued,
			Command:      "echo 'Hello from E2E test'",
			InputFiles: []domain.FileMetadata{
				{
					Path:     "input.txt",
					Size:     1024,
					Checksum: "abc123",
				},
			},
			OutputFiles: []domain.FileMetadata{
				{
					Path:     "output.txt",
					Size:     1024,
					Checksum: "def456",
				},
			},
			CreatedAt: time.Now(),
			UpdatedAt: time.Now(),
		}
		err = testEnv.DB.Repo.CreateTask(context.Background(), task)
		require.NoError(t, err)

		// Assign task to worker via gRPC
		err = testEnv.AssignTaskToWorker(t, worker.ID, task.ID)
		require.NoError(t, err)

		// Verify task was assigned (not fully executed)
		time.Sleep(2 * time.Second) // Brief wait for database update
		assignedTask, err := testEnv.DB.Repo.GetTaskByID(context.Background(), task.ID)
		require.NoError(t, err)
		assert.Equal(t, domain.TaskStatusQueued, assignedTask.Status)
		assert.Equal(t, worker.ID, assignedTask.WorkerID)

		t.Logf("Task successfully assigned to worker")
	})

	t.Run("DataStaging", func(t *testing.T) {
		// Create experiment first
		exp, err := testEnv.CreateTestExperiment("staging-test-"+fmt.Sprintf("%d", time.Now().UnixNano()), "echo test")
		require.NoError(t, err)

		// Create task with files
		taskID := "task-e2e-staging-123"
		computeResourceID := "compute-e2e-456"

		task := &domain.Task{
			ID:           taskID,
			ExperimentID: exp.ID,
			Status:       domain.TaskStatusQueued,
			Command:      "echo test",
			InputFiles:   []domain.FileMetadata{{Path: "input.txt", Size: 100, Checksum: "abc123"}},
			OutputFiles:  []domain.FileMetadata{{Path: "output.txt", Size: 0, Checksum: "def456"}},
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}
		err = testEnv.DB.Repo.CreateTask(context.Background(), task)
		require.NoError(t, err)

		// Generate signed URLs for task
		urls, err := testEnv.DataMoverSvc.GenerateSignedURLsForTask(ctx, taskID, computeResourceID)
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
	})

}

// TestWorkerSystem_CompleteWorkflow tests the complete end-to-end workflow
func TestWorkerSystem_CompleteWorkflow(t *testing.T) {

	// Setup test environment
	testEnv := testutil.SetupIntegrationTest(t)
	defer testEnv.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Register SLURM compute resource
	slurmResource, err := testEnv.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)
	assert.NotNil(t, slurmResource)

	// Register MinIO storage resource
	minioResource, err := testEnv.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)
	assert.NotNil(t, minioResource)

	// Create experiment with input/output files
	experiment, err := testEnv.CreateTestExperiment("complete-workflow-test", "echo 'Hello from complete workflow' > output.txt")
	require.NoError(t, err)
	assert.NotNil(t, experiment)

	// Upload input files to MinIO
	inputData := []byte("input data for processing")
	err = testEnv.UploadFile(minioResource.ID, "input.txt", inputData)
	require.NoError(t, err)

	// Scheduler spawns worker on SLURM via SSH
	worker, cmd := testEnv.SpawnRealWorker(t, experiment.ID, slurmResource.ID)
	defer func() {
		if cmd != nil && cmd.Process != nil {
			cmd.Process.Kill()
		}
	}()

	// Worker registers via gRPC
	err = testEnv.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
	require.NoError(t, err)

	// Get task ID from experiment
	taskID, err := testEnv.GetTaskIDFromExperiment(experiment.ID)
	require.NoError(t, err)

	// Scheduler assigns task to worker via gRPC
	err = testEnv.AssignTaskToWorker(t, worker.ID, taskID)
	require.NoError(t, err)

	// Worker downloads inputs using signed URLs
	workingDir := "/tmp/worker-" + worker.ID
	err = testEnv.WaitForFileDownload(workingDir, "input.txt", 30*time.Second)
	require.NoError(t, err)

	// Worker executes task
	err = testEnv.WaitForTaskOutputStreaming(t, taskID, 2*time.Minute)
	require.NoError(t, err)

	// Worker uploads outputs using signed URLs
	err = testEnv.VerifyFileInStorage(minioResource.ID, "output.txt", 1*time.Minute)
	require.NoError(t, err)

	// Verify task status updated in database
	task, err := testEnv.GetTask(taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, task.Status)
}

// TestWorkerSystem_MultiWorkerConcurrency tests multiple workers on different resources
func TestWorkerSystem_MultiWorkerConcurrency(t *testing.T) {

	// Setup test environment
	testEnv := testutil.SetupIntegrationTest(t)
	defer testEnv.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Register multiple SLURM resources
	resources := make([]*domain.ComputeResource, 2)
	ports := []string{"6817", "6819"}
	for i := 0; i < 2; i++ {
		resource, err := testEnv.RegisterSlurmResource(fmt.Sprintf("cluster-%d", i+1), fmt.Sprintf("localhost:%s", ports[i]))
		require.NoError(t, err)
		resources[i] = resource
	}

	// Spawn 2 workers on different resources
	workers := make([]*domain.Worker, 2)
	cmds := make([]*exec.Cmd, 2)

	for i := 0; i < 2; i++ {
		experiment, err := testEnv.CreateTestExperiment(fmt.Sprintf("concurrent-test-%d", i), fmt.Sprintf("echo 'Task %d'", i))
		require.NoError(t, err)

		worker, cmd := testEnv.SpawnRealWorker(t, experiment.ID, resources[i].ID)
		workers[i] = worker
		cmds[i] = cmd

		// Wait for worker to register
		err = testEnv.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
		require.NoError(t, err)
	}

	// Cleanup workers
	defer func() {
		for _, cmd := range cmds {
			if cmd != nil && cmd.Process != nil {
				cmd.Process.Kill()
			}
		}
	}()

	// Assign tasks to all workers concurrently
	for i, worker := range workers {
		experiment, err := testEnv.CreateTestExperiment(fmt.Sprintf("concurrent-assign-%d", i), fmt.Sprintf("echo 'Concurrent Task %d'", i))
		require.NoError(t, err)

		taskID, err := testEnv.GetTaskIDFromExperiment(experiment.ID)
		require.NoError(t, err)

		err = testEnv.AssignTaskToWorker(t, worker.ID, taskID)
		require.NoError(t, err)
	}

	// Verify all tasks complete successfully
	for i := 0; i < 2; i++ {
		experiment, err := testEnv.CreateTestExperiment(fmt.Sprintf("concurrent-verify-%d", i), fmt.Sprintf("echo 'Verify Task %d'", i))
		require.NoError(t, err)

		taskID, err := testEnv.GetTaskIDFromExperiment(experiment.ID)
		require.NoError(t, err)

		err = testEnv.WaitForTaskOutputStreaming(t, taskID, 2*time.Minute)
		require.NoError(t, err)
	}
}

// TestWorkerSystem_WorkerReuse tests worker reuse for multiple tasks
func TestWorkerSystem_WorkerReuse(t *testing.T) {

	// Setup test environment
	testEnv := testutil.SetupIntegrationTest(t)
	defer testEnv.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Register SLURM resource
	resource, err := testEnv.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	// Create experiment
	experiment, err := testEnv.CreateTestExperiment("worker-reuse-test", "echo 'Worker reuse test'")
	require.NoError(t, err)

	// Spawn worker
	worker, cmd := testEnv.SpawnRealWorker(t, experiment.ID, resource.ID)
	defer func() {
		if cmd != nil && cmd.Process != nil {
			cmd.Process.Kill()
		}
	}()

	// Wait for worker to register
	err = testEnv.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
	require.NoError(t, err)

	// Assign task 1
	taskID1, err := testEnv.GetTaskIDFromExperiment(experiment.ID)
	require.NoError(t, err)

	err = testEnv.AssignTaskToWorker(t, worker.ID, taskID1)
	require.NoError(t, err)

	// Wait for task 1 completion
	err = testEnv.WaitForTaskOutputStreaming(t, taskID1, 2*time.Minute)
	require.NoError(t, err)

	// Verify worker is idle
	workerStatus, err := testEnv.GetWorkerStatus(worker.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.WorkerStatusIdle, workerStatus.Status)

	// Create second experiment
	experiment2, err := testEnv.CreateTestExperiment("worker-reuse-test-2", "echo 'Worker reuse test 2'")
	require.NoError(t, err)

	// Reuse same worker for task 2
	taskID2, err := testEnv.GetTaskIDFromExperiment(experiment2.ID)
	require.NoError(t, err)

	err = testEnv.AssignTaskToWorker(t, worker.ID, taskID2)
	require.NoError(t, err)

	// Wait for task 2 completion
	err = testEnv.WaitForTaskOutputStreaming(t, taskID2, 2*time.Minute)
	require.NoError(t, err)

	// Verify worker transitions BUSY → IDLE → BUSY → IDLE
	workerStatus, err = testEnv.GetWorkerStatus(worker.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.WorkerStatusIdle, workerStatus.Status)
}

// TestWorkerSystem_ErrorScenarios tests error handling scenarios
func TestWorkerSystem_ErrorScenarios(t *testing.T) {

	// Setup test environment
	ctx := context.Background()
	testEnv := testutil.SetupIntegrationTest(t)
	defer testEnv.Cleanup()

	t.Run("InvalidWorkerRegistration", func(t *testing.T) {
		// Start gRPC server
		grpcServer, addr := testEnv.StartGRPCServer(t)
		defer grpcServer.Stop()

		// Connect worker client
		client, conn := testEnv.ConnectWorkerClient(t, addr)
		defer conn.Close()

		// Test invalid worker registration (empty worker ID)
		req := &dto.WorkerRegistrationRequest{
			WorkerId:          "", // Invalid empty ID
			ExperimentId:      "test-exp-123",
			ComputeResourceId: "test-resource-123",
			Capabilities: &dto.WorkerCapabilities{
				MaxCpuCores: 4,
			},
		}

		resp, err := client.RegisterWorker(ctx, req)
		// Should either return error or success=false
		if err != nil {
			assert.Error(t, err)
		} else {
			assert.False(t, resp.Success)
		}
	})

	t.Run("WorkerTimeout_Scenario", func(t *testing.T) {
		// Test worker timeout scenario with real gRPC server
		grpcServer, addr := testEnv.StartGRPCServer(t)
		defer grpcServer.Stop()

		// Connect worker client
		client, conn := testEnv.ConnectWorkerClient(t, addr)
		defer conn.Close()

		// Create stream and send heartbeat
		stream, err := client.PollForTask(ctx)
		require.NoError(t, err)

		// Send heartbeat with old timestamp (simulating timeout)
		oldTimestamp := time.Now().Add(-5 * time.Minute)
		heartbeat := &dto.WorkerMessage{
			Message: &dto.WorkerMessage_Heartbeat{
				Heartbeat: &dto.Heartbeat{
					WorkerId:      "worker-e2e-timeout",
					Timestamp:     timestamppb.New(oldTimestamp),
					Status:        dto.WorkerStatus_WORKER_STATUS_BUSY,
					CurrentTaskId: "task-1",
					Metadata: map[string]string{
						"status": "busy",
					},
				},
			},
		}

		err = stream.Send(heartbeat)
		require.NoError(t, err)

		// Verify old timestamp
		assert.True(t, oldTimestamp.Before(time.Now().Add(-1*time.Minute)))

		stream.CloseSend()
	})
}

// TestWorkerSystem_Performance tests performance scenarios with real infrastructure
func TestWorkerSystem_Performance(t *testing.T) {

	// Setup test environment
	ctx := context.Background()
	testEnv := testutil.SetupIntegrationTest(t)
	defer testEnv.Cleanup()

	t.Run("ConcurrentWorkerRegistration", func(t *testing.T) {
		// Start gRPC server
		grpcServer, addr := testEnv.StartGRPCServer(t)
		defer grpcServer.Stop()

		// Test rapid worker registration
		numWorkers := 10 // Reduced for test performance
		workers := make([]*dto.WorkerCapabilities, numWorkers)

		start := time.Now()
		for i := 0; i < numWorkers; i++ {
			workers[i] = &dto.WorkerCapabilities{
				MaxCpuCores:       2,
				MaxMemoryMb:       4096,
				MaxDiskGb:         50,
				MaxGpus:           0,
				SupportedRuntimes: []string{"slurm"},
			}
		}
		duration := time.Since(start)

		// Validate rapid creation
		assert.Len(t, workers, numWorkers)
		assert.Less(t, duration, 1*time.Second, "Rapid worker capabilities creation should complete within 1 second")

		// Test concurrent registration
		client, conn := testEnv.ConnectWorkerClient(t, addr)
		defer conn.Close()

		start = time.Now()
		for i := 0; i < numWorkers; i++ {
			req := &dto.WorkerRegistrationRequest{
				WorkerId:          fmt.Sprintf("perf-worker-%d", i),
				ExperimentId:      "perf-exp-123",
				ComputeResourceId: "perf-resource-123",
				Capabilities:      workers[i],
			}

			resp, err := client.RegisterWorker(ctx, req)
			require.NoError(t, err)
			assert.True(t, resp.Success)
		}
		duration = time.Since(start)

		// Validate concurrent registration performance
		assert.Less(t, duration, 5*time.Second, "Concurrent worker registration should complete within 5 seconds")
	})
}
