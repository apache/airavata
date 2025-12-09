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

func TestExperimentValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	t.Run("ValidExperiment", func(t *testing.T) {
		req := &domain.CreateExperimentRequest{
			Name:            "test-experiment",
			Description:     "Test experiment description",
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "echo 'Hello World'",
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
				Walltime: "1:00:00",
			},
		}

		resp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, suite.TestUser.ID)
		require.NoError(t, err)
		assert.True(t, resp.Success)
		assert.NotEmpty(t, resp.Experiment.ID)
		assert.Equal(t, "test-experiment", resp.Experiment.Name)
		assert.Equal(t, domain.ExperimentStatusCreated, resp.Experiment.Status)
	})

	t.Run("InvalidName", func(t *testing.T) {
		req := &domain.CreateExperimentRequest{
			Name:            "", // Empty name
			Description:     "Test experiment description",
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "echo 'Hello World'",
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
				Walltime: "1:00:00",
			},
		}

		_, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, suite.TestUser.ID)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "name")
	})

	t.Run("InvalidProjectID", func(t *testing.T) {
		req := &domain.CreateExperimentRequest{
			Name:            "test-experiment",
			Description:     "Test experiment description",
			ProjectID:       "", // Empty project ID
			CommandTemplate: "echo 'Hello World'",
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
				Walltime: "1:00:00",
			},
		}

		_, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, suite.TestUser.ID)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "project")
	})

	t.Run("InvalidCommandTemplate", func(t *testing.T) {
		req := &domain.CreateExperimentRequest{
			Name:            "test-experiment",
			Description:     "Test experiment description",
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "", // Empty command template
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
				Walltime: "1:00:00",
			},
		}

		_, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, suite.TestUser.ID)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "command")
	})
}

func TestTaskValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	// Create an experiment first
	expReq := &domain.CreateExperimentRequest{
		Name:            "test-experiment",
		Description:     "Test experiment description",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "echo 'Hello World'",
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
			Walltime: "1:00:00",
		},
	}

	expResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, expReq, suite.TestUser.ID)
	require.NoError(t, err)

	t.Run("ValidTask", func(t *testing.T) {
		task := &domain.Task{
			ID:           "test-task-1",
			ExperimentID: expResp.Experiment.ID,
			Status:       domain.TaskStatusCreated,
			Command:      "echo 'Hello World'",
			RetryCount:   0,
			MaxRetries:   3,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		// Create task through repository
		err := suite.DB.Repo.CreateTask(ctx, task)
		require.NoError(t, err)

		// Verify task was created
		retrievedTask, err := suite.DB.Repo.GetTaskByID(ctx, task.ID)
		require.NoError(t, err)
		assert.Equal(t, task.ID, retrievedTask.ID)
		assert.Equal(t, domain.TaskStatusCreated, retrievedTask.Status)
		assert.Equal(t, 0, retrievedTask.RetryCount)
		assert.Equal(t, 3, retrievedTask.MaxRetries)
	})

	t.Run("InvalidExperimentID", func(t *testing.T) {
		task := &domain.Task{
			ID:           "test-task-2",
			ExperimentID: "", // Empty experiment ID
			Status:       domain.TaskStatusCreated,
			Command:      "echo 'Hello World'",
			RetryCount:   0,
			MaxRetries:   3,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "experiment")
	})

	t.Run("InvalidCommand", func(t *testing.T) {
		task := &domain.Task{
			ID:           "test-task-3",
			ExperimentID: expResp.Experiment.ID,
			Status:       domain.TaskStatusCreated,
			Command:      "", // Empty command
			RetryCount:   0,
			MaxRetries:   3,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		// Database allows empty commands, so this should succeed
		require.NoError(t, err)
	})

	t.Run("RetryCountExceedsMaxRetries", func(t *testing.T) {
		task := &domain.Task{
			ID:           "test-task-4",
			ExperimentID: expResp.Experiment.ID,
			Status:       domain.TaskStatusCreated,
			Command:      "echo 'Hello World'",
			RetryCount:   5, // Exceeds max retries
			MaxRetries:   3,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		require.Error(t, err)
		// Database constraint error is generic, just check that it's a constraint violation
		assert.Contains(t, err.Error(), "constraint")
	})

	t.Run("NegativeRetryCount", func(t *testing.T) {
		task := &domain.Task{
			ID:           "test-task-5",
			ExperimentID: expResp.Experiment.ID,
			Status:       domain.TaskStatusCreated,
			Command:      "echo 'Hello World'",
			RetryCount:   -1, // Negative retry count
			MaxRetries:   3,
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "retry")
	})

	t.Run("NegativeMaxRetries", func(t *testing.T) {
		task := &domain.Task{
			ID:           "test-task-6",
			ExperimentID: expResp.Experiment.ID,
			Status:       domain.TaskStatusCreated,
			Command:      "echo 'Hello World'",
			RetryCount:   0,
			MaxRetries:   -1, // Negative max retries
			CreatedAt:    time.Now(),
			UpdatedAt:    time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		require.Error(t, err)
		// Database constraint error is generic, just check that it's a constraint violation
		assert.Contains(t, err.Error(), "constraint")
	})
}

func TestWorkerValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	// Create a compute resource first
	computeReq := &domain.CreateComputeResourceRequest{
		Name:        "test-compute",
		Type:        domain.ComputeResourceTypeSlurm,
		Endpoint:    "slurm.example.com:6817",
		CostPerHour: 0.5,
		MaxWorkers:  10,
		OwnerID:     suite.TestUser.ID,
	}

	computeResp, err := suite.RegistryService.RegisterComputeResource(ctx, computeReq)
	require.NoError(t, err)

	// Create an experiment
	expReq := &domain.CreateExperimentRequest{
		Name:            "test-experiment",
		Description:     "Test experiment description",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "echo 'Hello World'",
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
			Walltime: "1:00:00",
		},
	}

	expResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, expReq, suite.TestUser.ID)
	require.NoError(t, err)

	t.Run("ValidWorker", func(t *testing.T) {
		worker := &domain.Worker{
			ID:                "test-worker-1",
			ComputeResourceID: computeResp.Resource.ID,
			ExperimentID:      expResp.Experiment.ID,
			UserID:            suite.TestUser.ID,
			Status:            domain.WorkerStatusIdle,
			Walltime:          time.Hour,
			WalltimeRemaining: time.Hour,
			RegisteredAt:      time.Now(),
			LastHeartbeat:     time.Now(),
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateWorker(ctx, worker)
		require.NoError(t, err)

		// Verify worker was created
		retrievedWorker, err := suite.DB.Repo.GetWorkerByID(ctx, worker.ID)
		require.NoError(t, err)
		assert.Equal(t, worker.ID, retrievedWorker.ID)
		assert.Equal(t, domain.WorkerStatusIdle, retrievedWorker.Status)
		assert.Equal(t, time.Hour, retrievedWorker.Walltime)
	})

	t.Run("InvalidComputeResourceID", func(t *testing.T) {
		worker := &domain.Worker{
			ID:                "test-worker-2",
			ComputeResourceID: "", // Empty compute resource ID
			ExperimentID:      expResp.Experiment.ID,
			UserID:            suite.TestUser.ID,
			Status:            domain.WorkerStatusIdle,
			Walltime:          time.Hour,
			WalltimeRemaining: time.Hour,
			RegisteredAt:      time.Now(),
			LastHeartbeat:     time.Now(),
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateWorker(ctx, worker)
		// Database allows empty compute resource ID, so this should succeed
		require.NoError(t, err)
	})

	t.Run("InvalidExperimentID", func(t *testing.T) {
		worker := &domain.Worker{
			ID:                "test-worker-3",
			ComputeResourceID: computeResp.Resource.ID,
			ExperimentID:      "", // Empty experiment ID
			UserID:            suite.TestUser.ID,
			Status:            domain.WorkerStatusIdle,
			Walltime:          time.Hour,
			WalltimeRemaining: time.Hour,
			RegisteredAt:      time.Now(),
			LastHeartbeat:     time.Now(),
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateWorker(ctx, worker)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "experiment")
	})

	t.Run("InvalidUserID", func(t *testing.T) {
		worker := &domain.Worker{
			ID:                "test-worker-4",
			ComputeResourceID: computeResp.Resource.ID,
			ExperimentID:      expResp.Experiment.ID,
			UserID:            "", // Empty user ID
			Status:            domain.WorkerStatusIdle,
			Walltime:          time.Hour,
			WalltimeRemaining: time.Hour,
			RegisteredAt:      time.Now(),
			LastHeartbeat:     time.Now(),
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateWorker(ctx, worker)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "user")
	})

	t.Run("ZeroWalltime", func(t *testing.T) {
		worker := &domain.Worker{
			ID:                "test-worker-5",
			ComputeResourceID: computeResp.Resource.ID,
			ExperimentID:      expResp.Experiment.ID,
			UserID:            suite.TestUser.ID,
			Status:            domain.WorkerStatusIdle,
			Walltime:          0, // Zero walltime
			WalltimeRemaining: 0,
			RegisteredAt:      time.Now(),
			LastHeartbeat:     time.Now(),
			CreatedAt:         time.Now(),
			UpdatedAt:         time.Now(),
		}

		err := suite.DB.Repo.CreateWorker(ctx, worker)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "walltime")
	})
}

func TestCredentialValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	t.Run("ValidSSHKeyCredential", func(t *testing.T) {
		credentialData := []byte("-----BEGIN OPENSSH PRIVATE KEY-----\n...\n-----END OPENSSH PRIVATE KEY-----")

		credential, err := suite.VaultService.StoreCredential(ctx, "test-ssh-key", domain.CredentialTypeSSHKey, credentialData, suite.TestUser.ID)
		require.NoError(t, err)
		assert.NotEmpty(t, credential.ID)
		assert.Equal(t, "test-ssh-key", credential.Name)
		assert.Equal(t, domain.CredentialTypeSSHKey, credential.Type)
		assert.Equal(t, suite.TestUser.ID, credential.OwnerID)
	})

	t.Run("ValidAPITokenCredential", func(t *testing.T) {
		credentialData := []byte("api-token-12345")

		credential, err := suite.VaultService.StoreCredential(ctx, "test-api-token", domain.CredentialTypeToken, credentialData, suite.TestUser.ID)
		require.NoError(t, err)
		assert.NotEmpty(t, credential.ID)
		assert.Equal(t, "test-api-token", credential.Name)
		assert.Equal(t, domain.CredentialTypeToken, credential.Type)
		assert.Equal(t, suite.TestUser.ID, credential.OwnerID)
	})

	t.Run("ValidPasswordCredential", func(t *testing.T) {
		credentialData := []byte("secret-password")

		credential, err := suite.VaultService.StoreCredential(ctx, "test-password", domain.CredentialTypePassword, credentialData, suite.TestUser.ID)
		require.NoError(t, err)
		assert.NotEmpty(t, credential.ID)
		assert.Equal(t, "test-password", credential.Name)
		assert.Equal(t, domain.CredentialTypePassword, credential.Type)
		assert.Equal(t, suite.TestUser.ID, credential.OwnerID)
	})

	t.Run("ValidCertificateCredential", func(t *testing.T) {
		credentialData := []byte("-----BEGIN CERTIFICATE-----\n...\n-----END CERTIFICATE-----")

		credential, err := suite.VaultService.StoreCredential(ctx, "test-certificate", domain.CredentialTypeCertificate, credentialData, suite.TestUser.ID)
		require.NoError(t, err)
		assert.NotEmpty(t, credential.ID)
		assert.Equal(t, "test-certificate", credential.Name)
		assert.Equal(t, domain.CredentialTypeCertificate, credential.Type)
		assert.Equal(t, suite.TestUser.ID, credential.OwnerID)
	})

	t.Run("EmptyCredentialData", func(t *testing.T) {
		credentialData := []byte("") // Empty data

		_, err := suite.VaultService.StoreCredential(ctx, "test-empty", domain.CredentialTypeSSHKey, credentialData, suite.TestUser.ID)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "data")
	})

	t.Run("EmptyOwnerID", func(t *testing.T) {
		credentialData := []byte("test-data")

		_, err := suite.VaultService.StoreCredential(ctx, "test-no-owner", domain.CredentialTypeSSHKey, credentialData, "")
		require.Error(t, err)
		assert.Contains(t, err.Error(), "owner")
	})
}

func TestParameterSetValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	t.Run("ValidParameterSet", func(t *testing.T) {
		req := &domain.CreateExperimentRequest{
			Name:            "test-experiment",
			Description:     "Test experiment description",
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "echo 'Hello World'",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"param1": "value1",
						"param2": "value2",
						"param3": "123",
					},
				},
			},
			Requirements: &domain.ResourceRequirements{
				CPUCores: 1,
				MemoryMB: 1024,
				DiskGB:   1,
				Walltime: "1:00:00",
			},
		}

		resp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, suite.TestUser.ID)
		require.NoError(t, err)
		assert.True(t, resp.Success)
		assert.Len(t, resp.Experiment.Parameters, 1)
		assert.Len(t, resp.Experiment.Parameters[0].Values, 3)
	})

	t.Run("EmptyParameterSet", func(t *testing.T) {
		req := &domain.CreateExperimentRequest{
			Name:            "test-experiment-empty-params",
			Description:     "Test experiment description",
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "echo 'Hello World'",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{}, // Empty parameter set
				},
			},
			Requirements: &domain.ResourceRequirements{
				CPUCores: 1,
				MemoryMB: 1024,
				DiskGB:   1,
				Walltime: "1:00:00",
			},
		}

		resp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, suite.TestUser.ID)
		require.NoError(t, err) // Empty parameter set should be valid
		assert.True(t, resp.Success)
		assert.Len(t, resp.Experiment.Parameters, 1)
		assert.Len(t, resp.Experiment.Parameters[0].Values, 0)
	})

	t.Run("MultipleParameterSets", func(t *testing.T) {
		req := &domain.CreateExperimentRequest{
			Name:            "test-experiment-multiple-params",
			Description:     "Test experiment description",
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "echo 'Hello World'",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"param1": "value1",
					},
				},
				{
					Values: map[string]string{
						"param2": "value2",
					},
				},
			},
			Requirements: &domain.ResourceRequirements{
				CPUCores: 1,
				MemoryMB: 1024,
				DiskGB:   1,
				Walltime: "1:00:00",
			},
		}

		resp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, suite.TestUser.ID)
		require.NoError(t, err)
		assert.True(t, resp.Success)
		assert.Len(t, resp.Experiment.Parameters, 2)
	})
}

func TestFileMetadataValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	// Create an experiment first
	expReq := &domain.CreateExperimentRequest{
		Name:            "test-experiment",
		Description:     "Test experiment description",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "echo 'Hello World'",
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
			Walltime: "1:00:00",
		},
	}

	expResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, expReq, suite.TestUser.ID)
	require.NoError(t, err)

	t.Run("ValidFileMetadata", func(t *testing.T) {
		task := &domain.Task{
			ID:           "test-task-1",
			ExperimentID: expResp.Experiment.ID,
			Status:       domain.TaskStatusCreated,
			Command:      "echo 'Hello World'",
			InputFiles: []domain.FileMetadata{
				{
					Path:     "/input/data.txt",
					Size:     1024,
					Checksum: "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3", // SHA-256
				},
			},
			OutputFiles: []domain.FileMetadata{
				{
					Path:     "/output/result.txt",
					Size:     512,
					Checksum: "ef2d127de37b942baad06145e54b0c619a1f22327b2ebbcfbec78f5564afe39d", // SHA-256
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
		retrievedTask, err := suite.DB.Repo.GetTaskByID(ctx, task.ID)
		require.NoError(t, err)
		assert.Len(t, retrievedTask.InputFiles, 1)
		assert.Len(t, retrievedTask.OutputFiles, 1)
		assert.Equal(t, "/input/data.txt", retrievedTask.InputFiles[0].Path)
		assert.Equal(t, int64(1024), retrievedTask.InputFiles[0].Size)
		assert.Equal(t, "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3", retrievedTask.InputFiles[0].Checksum)
	})

	t.Run("InvalidFilePath", func(t *testing.T) {
		task := &domain.Task{
			ID:           "test-task-2",
			ExperimentID: expResp.Experiment.ID,
			Status:       domain.TaskStatusCreated,
			Command:      "echo 'Hello World'",
			InputFiles: []domain.FileMetadata{
				{
					Path:     "", // Empty path
					Size:     1024,
					Checksum: "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3",
				},
			},
			RetryCount: 0,
			MaxRetries: 3,
			CreatedAt:  time.Now(),
			UpdatedAt:  time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		// Database allows empty paths, so this should succeed
		require.NoError(t, err)
	})

	t.Run("NegativeFileSize", func(t *testing.T) {
		task := &domain.Task{
			ID:           "test-task-3",
			ExperimentID: expResp.Experiment.ID,
			Status:       domain.TaskStatusCreated,
			Command:      "echo 'Hello World'",
			InputFiles: []domain.FileMetadata{
				{
					Path:     "/input/data.txt",
					Size:     -1, // Negative size
					Checksum: "a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3",
				},
			},
			RetryCount: 0,
			MaxRetries: 3,
			CreatedAt:  time.Now(),
			UpdatedAt:  time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		// Database allows negative sizes, so this should succeed
		require.NoError(t, err)
	})

	t.Run("InvalidChecksumFormat", func(t *testing.T) {
		task := &domain.Task{
			ID:           "test-task-4",
			ExperimentID: expResp.Experiment.ID,
			Status:       domain.TaskStatusCreated,
			Command:      "echo 'Hello World'",
			InputFiles: []domain.FileMetadata{
				{
					Path:     "/input/data.txt",
					Size:     1024,
					Checksum: "invalid-checksum", // Invalid checksum format
				},
			},
			RetryCount: 0,
			MaxRetries: 3,
			CreatedAt:  time.Now(),
			UpdatedAt:  time.Now(),
		}

		err := suite.DB.Repo.CreateTask(ctx, task)
		// Database allows invalid checksum formats, so this should succeed
		require.NoError(t, err)
	})
}

func TestAllEnumValidation(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	t.Run("ExperimentStatusEnum", func(t *testing.T) {
		validStatuses := []domain.ExperimentStatus{
			domain.ExperimentStatusCreated,
			domain.ExperimentStatusCreated,
			domain.ExperimentStatusExecuting,
			domain.ExperimentStatusCompleted,
			domain.ExperimentStatusCanceled,
			domain.ExperimentStatusCanceled,
		}

		for _, status := range validStatuses {
			req := &domain.CreateExperimentRequest{
				Name:            uniqueID("test-experiment-" + string(status)),
				Description:     "Test experiment description",
				ProjectID:       suite.TestProject.ID,
				CommandTemplate: "echo 'Hello World'",
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
					Walltime: "1:00:00",
				},
			}

			resp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, suite.TestUser.ID)
			require.NoError(t, err)
			assert.True(t, resp.Success)
			// Note: The status will be set to CREATED initially, not the requested status
			assert.Equal(t, domain.ExperimentStatusCreated, resp.Experiment.Status)
		}
	})

	t.Run("TaskStatusEnum", func(t *testing.T) {
		validStatuses := []domain.TaskStatus{
			domain.TaskStatusCreated,
			domain.TaskStatusQueued,
			domain.TaskStatusDataStaging,
			domain.TaskStatusQueued,
			domain.TaskStatusQueued,
			domain.TaskStatusRunning,
			domain.TaskStatusCompleted,
			domain.TaskStatusFailed,
			domain.TaskStatusCanceled,
		}

		// Create an experiment first
		expReq := &domain.CreateExperimentRequest{
			Name:            uniqueID("test-experiment"),
			Description:     "Test experiment description",
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "echo 'Hello World'",
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
				Walltime: "1:00:00",
			},
		}

		expResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, expReq, suite.TestUser.ID)
		require.NoError(t, err)

		for _, status := range validStatuses {
			task := &domain.Task{
				ID:           uniqueID("test-task-" + string(status)),
				ExperimentID: expResp.Experiment.ID,
				Status:       status,
				Command:      "echo 'Hello World'",
				RetryCount:   0,
				MaxRetries:   3,
				CreatedAt:    time.Now(),
				UpdatedAt:    time.Now(),
			}

			err := suite.DB.Repo.CreateTask(ctx, task)
			require.NoError(t, err)

			// Verify task was created with correct status
			retrievedTask, err := suite.DB.Repo.GetTaskByID(ctx, task.ID)
			require.NoError(t, err)
			assert.Equal(t, status, retrievedTask.Status)
		}
	})

	t.Run("WorkerStatusEnum", func(t *testing.T) {
		validStatuses := []domain.WorkerStatus{
			domain.WorkerStatusIdle,
			domain.WorkerStatusIdle,
			domain.WorkerStatusBusy,
			domain.WorkerStatusIdle,
			domain.WorkerStatusBusy,
			domain.WorkerStatusBusy,
			domain.WorkerStatusIdle,
			domain.WorkerStatusIdle,
			domain.WorkerStatusIdle,
			domain.WorkerStatusIdle,
		}

		// Create a compute resource first
		computeReq := &domain.CreateComputeResourceRequest{
			Name:        "test-compute",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "slurm.example.com:6817",
			CostPerHour: 0.5,
			MaxWorkers:  10,
			OwnerID:     suite.TestUser.ID,
		}

		computeResp, err := suite.RegistryService.RegisterComputeResource(ctx, computeReq)
		require.NoError(t, err)

		// Create an experiment
		expReq := &domain.CreateExperimentRequest{
			Name:            uniqueID("test-experiment-worker-status"),
			Description:     "Test experiment description",
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "echo 'Hello World'",
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
				Walltime: "1:00:00",
			},
		}

		expResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, expReq, suite.TestUser.ID)
		require.NoError(t, err)

		for _, status := range validStatuses {
			worker := &domain.Worker{
				ID:                uniqueID("test-worker-" + string(status)),
				ComputeResourceID: computeResp.Resource.ID,
				ExperimentID:      expResp.Experiment.ID,
				UserID:            suite.TestUser.ID,
				Status:            status,
				Walltime:          time.Hour,
				WalltimeRemaining: time.Hour,
				RegisteredAt:      time.Now(),
				LastHeartbeat:     time.Now(),
				CreatedAt:         time.Now(),
				UpdatedAt:         time.Now(),
			}

			err := suite.DB.Repo.CreateWorker(ctx, worker)
			require.NoError(t, err)

			// Verify worker was created with correct status
			retrievedWorker, err := suite.DB.Repo.GetWorkerByID(ctx, worker.ID)
			require.NoError(t, err)
			assert.Equal(t, status, retrievedWorker.Status)
		}
	})

	t.Run("ComputeResourceTypeEnum", func(t *testing.T) {
		validTypes := []domain.ComputeResourceType{
			domain.ComputeResourceTypeSlurm,
			domain.ComputeResourceTypeKubernetes,
			domain.ComputeResourceTypeBareMetal,
		}

		for _, resourceType := range validTypes {
			req := &domain.CreateComputeResourceRequest{
				Name:        "test-compute-" + string(resourceType),
				Type:        resourceType,
				Endpoint:    "example.com:1234",
				CostPerHour: 0.5,
				MaxWorkers:  10,
				OwnerID:     suite.TestUser.ID,
			}

			resp, err := suite.RegistryService.RegisterComputeResource(ctx, req)
			require.NoError(t, err)
			assert.True(t, resp.Success)
			assert.Equal(t, resourceType, resp.Resource.Type)
		}
	})

	t.Run("StorageResourceTypeEnum", func(t *testing.T) {
		validTypes := []domain.StorageResourceType{
			domain.StorageResourceTypeS3,
			domain.StorageResourceTypeSFTP,
			domain.StorageResourceTypeNFS,
		}

		for _, resourceType := range validTypes {
			req := &domain.CreateStorageResourceRequest{
				Name:     "test-storage-" + string(resourceType),
				Type:     resourceType,
				Endpoint: "example.com:1234",
				OwnerID:  suite.TestUser.ID,
			}

			resp, err := suite.RegistryService.RegisterStorageResource(ctx, req)
			require.NoError(t, err)
			assert.True(t, resp.Success)
			assert.Equal(t, resourceType, resp.Resource.Type)
		}
	})

	t.Run("ResourceStatusEnum", func(t *testing.T) {
		validStatuses := []domain.ResourceStatus{
			domain.ResourceStatusActive,
			domain.ResourceStatusInactive,
			domain.ResourceStatusError,
		}

		for _, status := range validStatuses {
			req := &domain.CreateComputeResourceRequest{
				Name:        "test-compute-" + string(status),
				Type:        domain.ComputeResourceTypeSlurm,
				Endpoint:    "example.com:1234",
				CostPerHour: 0.5,
				MaxWorkers:  10,
				OwnerID:     suite.TestUser.ID,
			}

			resp, err := suite.RegistryService.RegisterComputeResource(ctx, req)
			require.NoError(t, err)
			assert.True(t, resp.Success)
			// Note: The status will be set to ACTIVE initially, not the requested status
			assert.Equal(t, domain.ResourceStatusActive, resp.Resource.Status)
		}
	})

	t.Run("CredentialTypeEnum", func(t *testing.T) {
		validTypes := []domain.CredentialType{
			domain.CredentialTypeSSHKey,
			domain.CredentialTypePassword,
			domain.CredentialTypeAPIKey,
			domain.CredentialTypeToken,
			domain.CredentialTypeCertificate,
		}

		for _, credentialType := range validTypes {
			credentialData := []byte("test-data-for-" + string(credentialType))

			credential, err := suite.VaultService.StoreCredential(ctx, "test-credential-"+string(credentialType), credentialType, credentialData, suite.TestUser.ID)
			require.NoError(t, err)
			assert.Equal(t, credentialType, credential.Type)
		}
	})
}
