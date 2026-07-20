package integration

import (
	"context"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestSLURM_QueuedState(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("queued-state-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment with long-running task
	req := &domain.CreateExperimentRequest{
		Name:            "queued-state-test",
		Description:     "Test SLURM queued state",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "sleep 10",
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
			Walltime: "0:01:00",
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
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusQueued, task.Status)
}

func TestSLURM_PendingState(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("pending-state-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment with resource requirements that might cause pending state
	req := &domain.CreateExperimentRequest{
		Name:            "pending-state-test",
		Description:     "Test SLURM pending state",
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
			Walltime: "0:01:00",
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
	time.Sleep(2 * time.Second)

	// Check task status (should be pending or running)
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.True(t, task.Status == domain.TaskStatusQueued || task.Status == domain.TaskStatusRunning)
}

func TestSLURM_RunningState(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("running-state-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment with task that runs for a while
	req := &domain.CreateExperimentRequest{
		Name:            "running-state-test",
		Description:     "Test SLURM running state",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "sleep 5 && echo 'Task completed'",
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
			Walltime: "0:01:00",
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
	time.Sleep(3 * time.Second)

	// Check that task is in running state
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusRunning, task.Status)
}

func TestSLURM_CompletedState(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("completed-state-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment with quick task
	req := &domain.CreateExperimentRequest{
		Name:            "completed-state-test",
		Description:     "Test SLURM completed state",
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
			Walltime: "0:01:00",
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Start gRPC server for worker communication
	grpcServer, _ := suite.StartGRPCServer(t)
	defer grpcServer.Stop()

	// Spawn a worker to monitor the task
	_, workerCmd, err := suite.SpawnWorkerForExperiment(t, exp.Experiment.ID, cluster.ID)
	require.NoError(t, err)
	defer func() {
		if workerCmd != nil && workerCmd.Process != nil {
			workerCmd.Process.Kill()
		}
	}()

	// Submit experiment through normal scheduler workflow
	err = suite.SubmitExperiment(exp.Experiment)
	require.NoError(t, err)

	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID

	// Wait for task to complete
	err = suite.WaitForTaskState(taskID, domain.TaskStatusCompleted, 1*time.Minute)
	require.NoError(t, err)

	// Verify task is completed
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, task.Status)
	assert.NotNil(t, task.CompletedAt)
}

func TestSLURM_FailedState(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("failed-state-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment with failing command
	req := &domain.CreateExperimentRequest{
		Name:            "failed-state-test",
		Description:     "Test SLURM failed state",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "exit 1", // This will fail
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
			Walltime: "0:01:00",
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Submit experiment to cluster
	err = suite.SubmitToCluster(exp.Experiment, cluster)
	require.NoError(t, err)

	// Wait for task to fail
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	err = suite.WaitForTaskState(taskID, domain.TaskStatusFailed, 1*time.Minute)
	require.NoError(t, err)

	// Verify task failed
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, task.Status)
	assert.NotEmpty(t, task.Error)
}

func TestSLURM_CancelledState(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("cancelled-state-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment with long-running task
	req := &domain.CreateExperimentRequest{
		Name:            "cancelled-state-test",
		Description:     "Test SLURM cancelled state",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "sleep 60", // Long running task
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

	// Submit experiment to cluster
	err = suite.SubmitToCluster(exp.Experiment, cluster)
	require.NoError(t, err)

	// Wait for task to start running
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	time.Sleep(3 * time.Second)

	// Delete the experiment (this will cancel running tasks)
	_, err = suite.OrchestratorSvc.DeleteExperiment(context.Background(), &domain.DeleteExperimentRequest{
		ExperimentID: exp.Experiment.ID,
	})
	require.NoError(t, err)

	// Wait for task to be cancelled
	err = suite.WaitForTaskState(taskID, domain.TaskStatusCanceled, 1*time.Minute)
	require.NoError(t, err)

	// Verify task is cancelled
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCanceled, task.Status)
}

func TestSLURM_TimeoutState(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("timeout-state-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment with task that exceeds time limit
	req := &domain.CreateExperimentRequest{
		Name:            "timeout-state-test",
		Description:     "Test SLURM timeout state",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "sleep 30", // Sleep for 30 seconds
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
			Walltime: "0:00:10", // 10 second time limit
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Submit experiment to cluster
	err = suite.SubmitToCluster(exp.Experiment, cluster)
	require.NoError(t, err)

	// Wait for task to timeout
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	err = suite.WaitForTaskState(taskID, domain.TaskStatusFailed, 1*time.Minute)
	require.NoError(t, err)

	// Verify task failed due to timeout
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, task.Status)
	assert.Contains(t, task.Error, "time")
}

func TestSLURM_OutOfMemoryState(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("oom-state-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment that tries to allocate excessive memory
	req := &domain.CreateExperimentRequest{
		Name:            "oom-state-test",
		Description:     "Test SLURM out of memory state",
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
			MemoryMB: 100, // Very small memory limit
			DiskGB:   1,
			Walltime: "0:01:00",
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Submit experiment to cluster
	err = suite.SubmitToCluster(exp.Experiment, cluster)
	require.NoError(t, err)

	// Wait for task to fail due to OOM
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	err = suite.WaitForTaskState(taskID, domain.TaskStatusFailed, 1*time.Minute)
	require.NoError(t, err)

	// Verify task failed due to memory
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, task.Status)
	assert.Contains(t, task.Error, "memory")
}

func TestSLURM_NodeFailState(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Wait for SLURM to be ready
	var err error
	err = suite.Compose.WaitForServices(t, 2*time.Minute)
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("node-fail-cluster", "localhost:6817")
	require.NoError(t, err)

	// Create experiment
	req := &domain.CreateExperimentRequest{
		Name:            "node-fail-test",
		Description:     "Test SLURM node failure state",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "sleep 10",
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
			Walltime: "0:01:00",
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Submit experiment to cluster
	err = suite.SubmitToCluster(exp.Experiment, cluster)
	require.NoError(t, err)

	// Wait for task to start
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	time.Sleep(2 * time.Second)

	// Stop the SLURM cluster to simulate node failure
	err = suite.Compose.StopServices(t)
	require.NoError(t, err)

	// Wait for task to fail due to node failure
	err = suite.WaitForTaskState(taskID, domain.TaskStatusFailed, 1*time.Minute)
	require.NoError(t, err)

	// Verify task failed due to node failure
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, task.Status)
	assert.Contains(t, task.Error, "node")
}
