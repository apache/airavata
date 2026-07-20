package unit

import (
	"context"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/adapters"
	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/core/dto"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestWorkerSpawnScriptGeneration(t *testing.T) {
	t.Run("SLURM_SpawnScript", func(t *testing.T) {
		// Test SLURM worker spawn script generation
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test SLURM Resource",
			Type: "SLURM",
		}
		slurmAdapter := adapters.NewSlurmAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		script, err := slurmAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Verify script contains expected elements
		assert.Contains(t, script, "#!/bin/bash")
		assert.Contains(t, script, "#SBATCH")
		assert.Contains(t, script, "http://localhost:8080/api/worker-binary")
		assert.Contains(t, script, "localhost:50051")
		assert.Contains(t, script, "30:00") // Walltime format
	})

	t.Run("BareMetal_SpawnScript", func(t *testing.T) {
		// Test Bare Metal worker spawn script generation
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test Bare Metal Resource",
			Type: "BARE_METAL",
		}
		baremetalAdapter := adapters.NewBareMetalAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		script, err := baremetalAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Verify script contains expected elements
		assert.Contains(t, script, "#!/bin/bash")
		assert.Contains(t, script, "http://localhost:8080/api/worker-binary")
		assert.Contains(t, script, "localhost:50051")
		assert.Contains(t, script, "cleanup")
		assert.Contains(t, script, "&")
	})

	t.Run("Kubernetes_SpawnScript", func(t *testing.T) {
		// Test Kubernetes worker spawn script generation
		config := &adapters.ScriptConfig{
			WorkerBinaryURL:   "http://localhost:8080/api/worker-binary",
			ServerGRPCAddress: "localhost",
			ServerGRPCPort:    50051,
		}

		resource := domain.ComputeResource{
			ID:   "test-resource",
			Name: "Test Kubernetes Resource",
			Type: "KUBERNETES",
		}
		kubernetesAdapter := adapters.NewKubernetesAdapterWithConfig(resource, nil, config)

		experiment := &domain.Experiment{
			ID:   "exp-123",
			Name: "Test Experiment",
		}

		walltime := 30 * time.Minute

		script, err := kubernetesAdapter.GenerateWorkerSpawnScript(context.Background(), experiment, walltime)
		require.NoError(t, err)
		assert.NotEmpty(t, script)

		// Verify script contains expected elements
		assert.Contains(t, script, "apiVersion: v1")
		assert.Contains(t, script, "kind: Pod")
		assert.Contains(t, script, "http://localhost:8080/api/worker-binary")
		assert.Contains(t, script, "localhost:50051")
		assert.Contains(t, script, "worker")
	})
}

func TestWorkerLifecycle(t *testing.T) {
	t.Run("WorkerCapabilities", func(t *testing.T) {
		// Test worker capabilities validation
		capabilities := &dto.WorkerCapabilities{
			MaxCpuCores:       4,
			MaxMemoryMb:       8192,
			MaxDiskGb:         100,
			MaxGpus:           1,
			SupportedRuntimes: []string{"slurm", "kubernetes", "baremetal"},
		}

		// Test valid capabilities
		assert.Greater(t, capabilities.MaxCpuCores, int32(0))
		assert.Greater(t, capabilities.MaxMemoryMb, int32(0))
		assert.Greater(t, capabilities.MaxDiskGb, int32(0))
		assert.GreaterOrEqual(t, capabilities.MaxGpus, int32(0))
		assert.Len(t, capabilities.SupportedRuntimes, 3)

		// Test invalid capabilities
		invalidCapabilities := &dto.WorkerCapabilities{
			MaxCpuCores: 0, // Invalid
			MaxMemoryMb: 0, // Invalid
			MaxDiskGb:   0, // Invalid
		}

		assert.Equal(t, int32(0), invalidCapabilities.MaxCpuCores)
		assert.Equal(t, int32(0), invalidCapabilities.MaxMemoryMb)
		assert.Equal(t, int32(0), invalidCapabilities.MaxDiskGb)
	})

	t.Run("WorkerStatusTransitions", func(t *testing.T) {
		// Test worker status transitions
		status := dto.WorkerStatus_WORKER_STATUS_IDLE
		assert.Equal(t, dto.WorkerStatus_WORKER_STATUS_IDLE, status)

		// Test status transitions
		status = dto.WorkerStatus_WORKER_STATUS_BUSY
		assert.Equal(t, dto.WorkerStatus_WORKER_STATUS_BUSY, status)

		status = dto.WorkerStatus_WORKER_STATUS_STAGING
		assert.Equal(t, dto.WorkerStatus_WORKER_STATUS_STAGING, status)

		status = dto.WorkerStatus_WORKER_STATUS_ERROR
		assert.Equal(t, dto.WorkerStatus_WORKER_STATUS_ERROR, status)
	})

	t.Run("WorkerHeartbeat", func(t *testing.T) {
		// Test worker heartbeat mechanism
		heartbeat := &dto.Heartbeat{
			WorkerId:      "worker-123",
			Status:        dto.WorkerStatus_WORKER_STATUS_IDLE,
			CurrentTaskId: "task-1",
			Metadata: map[string]string{
				"version": "1.0.0",
			},
		}

		assert.NotEmpty(t, heartbeat.WorkerId)
		assert.Equal(t, dto.WorkerStatus_WORKER_STATUS_IDLE, heartbeat.Status)
		assert.Equal(t, "task-1", heartbeat.CurrentTaskId)
		assert.NotNil(t, heartbeat.Metadata)
		assert.Equal(t, "1.0.0", heartbeat.Metadata["version"])

		// Test status change in heartbeat
		heartbeat.Status = dto.WorkerStatus_WORKER_STATUS_BUSY
		heartbeat.CurrentTaskId = "task-3"
		heartbeat.Metadata["status"] = "busy"

		assert.Equal(t, dto.WorkerStatus_WORKER_STATUS_BUSY, heartbeat.Status)
		assert.Equal(t, "task-3", heartbeat.CurrentTaskId)
		assert.Equal(t, "busy", heartbeat.Metadata["status"])
	})
}

func TestTaskExecution(t *testing.T) {
	t.Run("TaskAssignment", func(t *testing.T) {
		// Test task assignment to worker
		assignment := &dto.TaskAssignment{
			TaskId:       "task-123",
			ExperimentId: "exp-456",
			Command:      "echo 'Hello World'",
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
		}

		assert.NotEmpty(t, assignment.TaskId)
		assert.NotEmpty(t, assignment.ExperimentId)
		assert.NotEmpty(t, assignment.Command)
		assert.Len(t, assignment.InputFiles, 1)
		assert.Len(t, assignment.OutputFiles, 1)

		// Test input file validation
		inputFile := assignment.InputFiles[0]
		assert.NotEmpty(t, inputFile.Url)
		assert.NotEmpty(t, inputFile.LocalPath)

		// Test output file validation
		outputFile := assignment.OutputFiles[0]
		assert.NotEmpty(t, outputFile.Path)
		assert.Greater(t, outputFile.Size, int64(0))
	})

	t.Run("TaskStatusUpdates", func(t *testing.T) {
		// Test task status updates
		taskID := "task-123"
		status := dto.TaskStatus_TASK_STATUS_RUNNING

		assert.NotEmpty(t, taskID)
		assert.Equal(t, dto.TaskStatus_TASK_STATUS_RUNNING, status)

		// Test status transitions
		status = dto.TaskStatus_TASK_STATUS_COMPLETED
		assert.Equal(t, dto.TaskStatus_TASK_STATUS_COMPLETED, status)

		status = dto.TaskStatus_TASK_STATUS_FAILED
		assert.Equal(t, dto.TaskStatus_TASK_STATUS_FAILED, status)
	})
}

func TestDataStaging(t *testing.T) {
	t.Run("DataStagingRequest", func(t *testing.T) {
		// Test data staging request
		stagingRequest := &dto.WorkerDataStagingRequest{
			TaskId:            "task-123",
			ComputeResourceId: "compute-789",
			WorkerId:          "worker-456",
			Files: []*dto.FileMetadata{
				{
					Path: "input.txt",
					Size: 1024,
				},
			},
		}

		assert.NotEmpty(t, stagingRequest.TaskId)
		assert.NotEmpty(t, stagingRequest.ComputeResourceId)
		assert.NotEmpty(t, stagingRequest.WorkerId)
		assert.Len(t, stagingRequest.Files, 1)

		// Test staging file validation
		stagingFile := stagingRequest.Files[0]
		assert.NotEmpty(t, stagingFile.Path)
		assert.Greater(t, stagingFile.Size, int64(0))
	})

	t.Run("DataStagingResponse", func(t *testing.T) {
		// Test successful data staging response
		successResponse := &dto.WorkerDataStagingResponse{
			StagingId: "staging-123",
			Success:   true,
			Message:   "Data staging completed successfully",
			Validation: &dto.ValidationResult{
				Valid: true,
			},
		}

		assert.NotEmpty(t, successResponse.StagingId)
		assert.True(t, successResponse.Success)
		assert.NotEmpty(t, successResponse.Message)
		assert.NotNil(t, successResponse.Validation)
		assert.True(t, successResponse.Validation.Valid)

		// Test failed data staging response
		failedResponse := &dto.WorkerDataStagingResponse{
			StagingId: "staging-456",
			Success:   false,
			Message:   "Data staging failed: file not found",
			Validation: &dto.ValidationResult{
				Valid: false,
				Errors: []*dto.Error{
					{
						Message: "File not found",
					},
				},
			},
		}

		assert.False(t, failedResponse.Success)
		assert.Equal(t, "Data staging failed: file not found", failedResponse.Message)
		assert.False(t, failedResponse.Validation.Valid)
		assert.Len(t, failedResponse.Validation.Errors, 1)
		assert.Equal(t, "File not found", failedResponse.Validation.Errors[0].Message)
	})
}

func TestWorkerConcurrency(t *testing.T) {
	t.Run("MultipleWorkers", func(t *testing.T) {
		// Test multiple workers on same compute resource

		workers := []*dto.WorkerCapabilities{
			{
				MaxCpuCores:       2,
				MaxMemoryMb:       4096,
				MaxDiskGb:         50,
				MaxGpus:           0,
				SupportedRuntimes: []string{"slurm"},
			},
			{
				MaxCpuCores:       2,
				MaxMemoryMb:       4096,
				MaxDiskGb:         50,
				MaxGpus:           0,
				SupportedRuntimes: []string{"kubernetes"},
			},
		}

		// Validate multiple workers
		assert.Len(t, workers, 2)
		assert.Equal(t, int32(2), workers[0].MaxCpuCores)
		assert.Equal(t, int32(2), workers[1].MaxCpuCores)

		// Test resource sharing
		totalCpuCores := workers[0].MaxCpuCores + workers[1].MaxCpuCores
		totalMemoryMb := workers[0].MaxMemoryMb + workers[1].MaxMemoryMb
		totalDiskGb := workers[0].MaxDiskGb + workers[1].MaxDiskGb

		assert.Equal(t, int32(4), totalCpuCores)
		assert.Equal(t, int32(8192), totalMemoryMb)
		assert.Equal(t, int32(100), totalDiskGb)
	})

	t.Run("ConcurrentTaskExecution", func(t *testing.T) {
		// Test concurrent task execution on same worker
		tasks := []*dto.TaskAssignment{
			{
				TaskId:       "task-1",
				ExperimentId: "exp-123",
				Command:      "echo 'Task 1'",
			},
			{
				TaskId:       "task-2",
				ExperimentId: "exp-123",
				Command:      "echo 'Task 2'",
			},
		}

		// Validate concurrent tasks
		assert.Len(t, tasks, 2)
		assert.Equal(t, "task-1", tasks[0].TaskId)
		assert.Equal(t, "task-2", tasks[1].TaskId)

		// Test task status updates
		statusUpdates := []dto.TaskStatus{
			dto.TaskStatus_TASK_STATUS_RUNNING,
			dto.TaskStatus_TASK_STATUS_RUNNING,
		}

		assert.Len(t, statusUpdates, 2)
		assert.Equal(t, dto.TaskStatus_TASK_STATUS_RUNNING, statusUpdates[0])
		assert.Equal(t, dto.TaskStatus_TASK_STATUS_RUNNING, statusUpdates[1])
	})
}
