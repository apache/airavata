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

func TestBareMetal_ProcessTermination(t *testing.T) {

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

	// Register bare metal resource (corrected endpoint)
	resource, err := suite.RegisterBaremetalResource("process-termination-test", "localhost:2225")
	require.NoError(t, err)

	// Create experiment with long-running process
	req := &domain.CreateExperimentRequest{
		Name:            "process-termination-test",
		Description:     "Test bare metal process termination",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "sleep 30 & echo $! > /tmp/pid && wait",
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

	// Submit experiment to generate tasks
	err = suite.SubmitExperiment(exp.Experiment)
	require.NoError(t, err)

	// Submit experiment to bare metal
	err = suite.SubmitToCluster(exp.Experiment, resource)
	require.NoError(t, err)

	// Wait for task to start
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	time.Sleep(3 * time.Second)

	// Delete the experiment to test process termination
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

func TestBareMetal_ZombieProcess(t *testing.T) {

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

	// Register bare metal resource (corrected endpoint)
	resource, err := suite.RegisterBaremetalResource("zombie-process-test", "localhost:2225")
	require.NoError(t, err)

	// Create experiment that creates a zombie process
	req := &domain.CreateExperimentRequest{
		Name:            "zombie-process-test",
		Description:     "Test bare metal zombie process handling",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "python -c 'import os, time; pid = os.fork(); time.sleep(1) if pid == 0 else time.sleep(2)'",
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

	// Submit experiment to generate tasks
	err = suite.SubmitExperiment(exp.Experiment)
	require.NoError(t, err)

	// Submit experiment to bare metal
	err = suite.SubmitToCluster(exp.Experiment, resource)
	require.NoError(t, err)

	// Wait for task to complete
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	err = suite.WaitForTaskState(taskID, domain.TaskStatusCompleted, 1*time.Minute)
	require.NoError(t, err)

	// Verify task completed successfully
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, task.Status)
}

func TestBareMetal_SignalHandling(t *testing.T) {

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

	// Register bare metal resource (corrected endpoint)
	resource, err := suite.RegisterBaremetalResource("signal-handling-test", "localhost:2225")
	require.NoError(t, err)

	// Create experiment that handles signals
	req := &domain.CreateExperimentRequest{
		Name:            "signal-handling-test",
		Description:     "Test bare metal signal handling",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "trap 'echo Signal received' SIGTERM; sleep 10",
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

	// Submit experiment to generate tasks
	err = suite.SubmitExperiment(exp.Experiment)
	require.NoError(t, err)

	// Submit experiment to bare metal
	err = suite.SubmitToCluster(exp.Experiment, resource)
	require.NoError(t, err)

	// Wait for task to start
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	time.Sleep(2 * time.Second)

	// Delete the experiment to test signal handling
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

func TestBareMetal_BackgroundProcess(t *testing.T) {

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

	// Register bare metal resource (corrected endpoint)
	resource, err := suite.RegisterBaremetalResource("background-process-test", "localhost:2225")
	require.NoError(t, err)

	// Create experiment with background process
	req := &domain.CreateExperimentRequest{
		Name:            "background-process-test",
		Description:     "Test bare metal background process",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "nohup sleep 5 > /tmp/background.log 2>&1 & echo 'Background process started'",
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

	// Submit experiment to generate tasks
	err = suite.SubmitExperiment(exp.Experiment)
	require.NoError(t, err)

	// Submit experiment to bare metal
	err = suite.SubmitToCluster(exp.Experiment, resource)
	require.NoError(t, err)

	// Wait for task to complete
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	err = suite.WaitForTaskState(taskID, domain.TaskStatusCompleted, 1*time.Minute)
	require.NoError(t, err)

	// Verify task completed successfully
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, task.Status)
}

func TestBareMetal_ProcessCleanup(t *testing.T) {

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

	// Register bare metal resource (corrected endpoint)
	resource, err := suite.RegisterBaremetalResource("process-cleanup-test", "localhost:2225")
	require.NoError(t, err)

	// Create experiment that creates multiple processes
	req := &domain.CreateExperimentRequest{
		Name:            "process-cleanup-test",
		Description:     "Test bare metal process cleanup",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "for i in {1..5}; do sleep 2 & done; wait; echo 'All processes completed'",
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

	// Submit experiment to generate tasks
	err = suite.SubmitExperiment(exp.Experiment)
	require.NoError(t, err)

	// Submit experiment to bare metal
	err = suite.SubmitToCluster(exp.Experiment, resource)
	require.NoError(t, err)

	// Wait for task to complete
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	err = suite.WaitForTaskState(taskID, domain.TaskStatusCompleted, 1*time.Minute)
	require.NoError(t, err)

	// Verify task completed successfully
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, task.Status)
}

func TestBareMetal_ProcessResourceLimits(t *testing.T) {

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

	// Register bare metal resource (corrected endpoint)
	resource, err := suite.RegisterBaremetalResource("process-resource-limits-test", "localhost:2225")
	require.NoError(t, err)

	// Create experiment that tests resource limits
	req := &domain.CreateExperimentRequest{
		Name:            "process-resource-limits-test",
		Description:     "Test bare metal process resource limits",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "ulimit -v 1048576; python -c 'import time; data = [0] * 1000000; time.sleep(1)'", // 1MB memory limit
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

	// Submit experiment to generate tasks
	err = suite.SubmitExperiment(exp.Experiment)
	require.NoError(t, err)

	// Submit experiment to bare metal
	err = suite.SubmitToCluster(exp.Experiment, resource)
	require.NoError(t, err)

	// Wait for task to complete
	// Get the first task for this experiment
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)
	taskID := tasks[0].ID
	err = suite.WaitForTaskState(taskID, domain.TaskStatusCompleted, 1*time.Minute)
	require.NoError(t, err)

	// Verify task completed successfully
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, task.Status)
}
