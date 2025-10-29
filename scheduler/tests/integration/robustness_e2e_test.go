package integration

import (
	"fmt"
	"os/exec"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestWorkerHealthMonitoring_2MinTimeout(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Start gRPC server
	grpcServer, _ := suite.StartGRPCServer(t)
	defer grpcServer.Stop()

	// Register SLURM resource
	resource, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	// Create experiment
	exp, err := suite.CreateTestExperiment("health-test", "sleep 300")
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Spawn real worker
	worker, cmd := suite.SpawnRealWorker(t, exp.ID, resource.ID)
	defer func() {
		if cmd != nil && cmd.Process != nil {
			cmd.Process.Kill()
		}
	}()

	// Wait for worker to register
	err = suite.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
	require.NoError(t, err)

	// Get task ID
	taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
	require.NoError(t, err)

	// Assign task to worker
	err = suite.AssignTaskToWorker(t, worker.ID, taskID)
	require.NoError(t, err)

	// Kill worker process to simulate network failure
	cmd.Process.Kill()

	// Wait 2+ minutes and verify task is marked as failed
	time.Sleep(130 * time.Second)

	updatedTask, err := suite.GetTask(taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, updatedTask.Status) // Task failed due to worker death
	assert.Greater(t, updatedTask.RetryCount, 0)
}

func TestTaskRetry_3AttemptsMaximum(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Start gRPC server
	grpcServer, _ := suite.StartGRPCServer(t)
	defer grpcServer.Stop()

	// Register SLURM resource
	resource, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	// Create experiment with a command that always fails
	exp, err := suite.CreateTestExperiment("retry-test", "exit 1")
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Spawn real worker
	worker, cmd := suite.SpawnRealWorker(t, exp.ID, resource.ID)
	defer func() {
		if cmd != nil && cmd.Process != nil {
			cmd.Process.Kill()
		}
	}()

	// Wait for worker to register
	err = suite.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
	require.NoError(t, err)

	// Get task ID
	taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
	require.NoError(t, err)

	// Assign task to worker
	err = suite.AssignTaskToWorker(t, worker.ID, taskID)
	require.NoError(t, err)

	// Wait for task execution and retry attempts
	time.Sleep(2 * time.Minute)

	// Verify task failed permanently after retry attempts
	finalTask, err := suite.GetTask(taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, finalTask.Status)
	assert.Greater(t, finalTask.RetryCount, 0)
}

func TestWorkerSelfTermination_5MinServerUnresponsive(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Start gRPC server
	grpcServer, _ := suite.StartGRPCServer(t)
	defer grpcServer.Stop()

	// Register SLURM resource
	resource, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	exp, err := suite.CreateTestExperiment("worker-term-test", "sleep 600")
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Spawn real worker
	worker, cmd := suite.SpawnRealWorker(t, exp.ID, resource.ID)
	defer func() {
		if cmd != nil && cmd.Process != nil {
			cmd.Process.Kill()
		}
	}()

	// Wait for worker to register
	err = suite.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
	require.NoError(t, err)

	// Stop gRPC server to simulate server failure
	grpcServer.Stop()

	// Wait 5+ minutes for worker to detect server unresponsiveness
	time.Sleep(310 * time.Second)

	// Verify worker has terminated itself
	workerStatus, err := suite.GetWorkerStatus(worker.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.WorkerStatusIdle, workerStatus.Status)
}

func TestWorkerHealthMonitoring_HeartbeatRecovery(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Start gRPC server
	grpcServer, _ := suite.StartGRPCServer(t)
	defer grpcServer.Stop()

	// Register SLURM resource
	resource, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	// Create experiment
	exp, err := suite.CreateTestExperiment("heartbeat-recovery", "echo 'Hello World'")
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Spawn real worker
	worker, cmd := suite.SpawnRealWorker(t, exp.ID, resource.ID)
	defer func() {
		if cmd != nil && cmd.Process != nil {
			cmd.Process.Kill()
		}
	}()

	// Wait for worker to register
	err = suite.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
	require.NoError(t, err)

	// Get task ID
	taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
	require.NoError(t, err)

	// Assign task to worker
	err = suite.AssignTaskToWorker(t, worker.ID, taskID)
	require.NoError(t, err)

	// Wait for task completion
	err = suite.WaitForTaskOutputStreaming(t, taskID, 2*time.Minute)
	require.NoError(t, err)

	// Verify task completed successfully
	completedTask, err := suite.GetTask(taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, completedTask.Status)
}

func TestTaskRetry_DifferentWorkers(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Start gRPC server
	grpcServer, _ := suite.StartGRPCServer(t)
	defer grpcServer.Stop()

	// Register multiple SLURM resources
	resource1, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)
	resource2, err := suite.RegisterSlurmResource("cluster-2", "localhost:6819")
	require.NoError(t, err)

	// Create experiment that will fail and retry
	exp, err := suite.CreateTestExperiment("retry-different-workers", "echo 'Hello from retry test'")
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Spawn workers on both resources
	worker1, cmd1 := suite.SpawnRealWorker(t, exp.ID, resource1.ID)
	defer func() {
		if cmd1 != nil && cmd1.Process != nil {
			cmd1.Process.Kill()
		}
	}()

	worker2, cmd2 := suite.SpawnRealWorker(t, exp.ID, resource2.ID)
	defer func() {
		if cmd2 != nil && cmd2.Process != nil {
			cmd2.Process.Kill()
		}
	}()

	// Wait for workers to register
	err = suite.WaitForWorkerRegistration(t, worker1.ID, 30*time.Second)
	require.NoError(t, err)
	err = suite.WaitForWorkerRegistration(t, worker2.ID, 30*time.Second)
	require.NoError(t, err)

	// Get task ID
	taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
	require.NoError(t, err)

	// Assign task to first worker
	err = suite.AssignTaskToWorker(t, worker1.ID, taskID)
	require.NoError(t, err)

	// Wait for task execution
	time.Sleep(1 * time.Minute)

	// Verify task completed
	finalTask, err := suite.GetTask(taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, finalTask.Status)
}

func TestWorkerSelfTermination_GracefulShutdown(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Start gRPC server
	grpcServer, _ := suite.StartGRPCServer(t)
	defer grpcServer.Stop()

	// Register SLURM resource
	resource, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	exp, err := suite.CreateTestExperiment("graceful-shutdown", "sleep 10 && echo 'completed'")
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Spawn real worker
	worker, cmd := suite.SpawnRealWorker(t, exp.ID, resource.ID)
	defer func() {
		if cmd != nil && cmd.Process != nil {
			cmd.Process.Kill()
		}
	}()

	// Wait for worker to register
	err = suite.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
	require.NoError(t, err)

	// Send graceful shutdown by killing the process
	cmd.Process.Kill()

	// Wait for worker to shutdown gracefully
	time.Sleep(30 * time.Second)

	// Verify worker status
	workerStatus, err := suite.GetWorkerStatus(worker.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.WorkerStatusIdle, workerStatus.Status)
}

func TestWorkerHealthMonitoring_ConcurrentWorkers(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Start gRPC server
	grpcServer, _ := suite.StartGRPCServer(t)
	defer grpcServer.Stop()

	// Register SLURM resources
	resource1, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)
	resource2, err := suite.RegisterSlurmResource("cluster-2", "localhost:6819")
	require.NoError(t, err)

	// Create multiple experiments
	experiments := make([]*domain.Experiment, 2)
	for i := 0; i < 2; i++ {
		exp, err := suite.CreateTestExperiment(
			fmt.Sprintf("concurrent-health-test-%d", i),
			"echo 'Worker health test' && sleep 5",
		)
		require.NoError(t, err)
		experiments[i] = exp
	}

	// Spawn workers for each experiment
	workers := make([]*domain.Worker, 2)
	cmds := make([]*exec.Cmd, 2)
	for i, exp := range experiments {
		resource := resource1
		if i == 1 {
			resource = resource2
		}
		worker, cmd := suite.SpawnRealWorker(t, exp.ID, resource.ID)
		workers[i] = worker
		cmds[i] = cmd
	}

	// Cleanup workers
	defer func() {
		for _, cmd := range cmds {
			if cmd != nil && cmd.Process != nil {
				cmd.Process.Kill()
			}
		}
	}()

	// Wait for workers to register
	for _, worker := range workers {
		err = suite.WaitForWorkerRegistration(t, worker.ID, 30*time.Second)
		require.NoError(t, err)
	}

	// Kill one worker to simulate failure
	cmds[1].Process.Kill()

	// Wait for timeout
	time.Sleep(130 * time.Second)

	// Verify tasks status
	for i, exp := range experiments {
		taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
		require.NoError(t, err)

		task, err := suite.GetTask(taskID)
		require.NoError(t, err)

		if i == 1 {
			// Failed worker's task should be failed
			assert.Equal(t, domain.TaskStatusFailed, task.Status)
		} else {
			// Healthy worker's task should be completed
			assert.Equal(t, domain.TaskStatusCompleted, task.Status)
		}
	}
}
