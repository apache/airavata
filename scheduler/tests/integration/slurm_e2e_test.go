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

func TestSlurmCluster1_HelloWorld(t *testing.T) {

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

	// Register SLURM cluster 1 with SSH credentials
	computeResource, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)
	assert.NotNil(t, computeResource)

	// Submit hello world + sleep task
	exp, err := suite.CreateTestExperiment("slurm-test-1", "echo 'Hello World from SLURM Cluster 1' && sleep 5")
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Experiment is already submitted by CreateTestExperiment

	// Real task execution with worker binary staging
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)

	task := tasks[0]

	// 1. Create task directory
	workDir, err := suite.CreateTaskDirectory(task.ID, task.ComputeResourceID)
	require.NoError(t, err)
	t.Logf("Created task directory: %s", workDir)

	// 2. Stage worker binary
	err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
	require.NoError(t, err)
	t.Logf("Staged worker binary for task %s", task.ID)

	// 3. Submit SLURM job (this will run the actual command, not the worker binary)
	err = suite.SubmitSlurmJob(task.ID)
	require.NoError(t, err)
	t.Logf("Submitted SLURM job for task %s", task.ID)

	// 6. Check current task status before starting monitoring
	currentTask, err := suite.DB.Repo.GetTaskByID(context.Background(), task.ID)
	require.NoError(t, err)
	t.Logf("Task %s current status: %s", task.ID, currentTask.Status)

	// 7. Start task monitoring for real status updates
	err = suite.StartTaskMonitoring(task.ID)
	require.NoError(t, err)
	t.Logf("Started task monitoring for %s", task.ID)

	// 8. Wait for task to progress through all expected state transitions
	// Note: In SLURM tests, the task may already be in RUNNING state when monitoring starts
	// because the scheduler sets it to RUNNING when the SLURM job is submitted
	var expectedStates []domain.TaskStatus
	if currentTask.Status == domain.TaskStatusRunning {
		// Task is already running, just wait for completion
		expectedStates = []domain.TaskStatus{
			domain.TaskStatusRunning,
			domain.TaskStatusOutputStaging,
			domain.TaskStatusCompleted,
		}
	} else {
		// Task is still queued, wait for full sequence
		expectedStates = []domain.TaskStatus{
			domain.TaskStatusQueued,
			domain.TaskStatusRunning,
			domain.TaskStatusOutputStaging,
			domain.TaskStatusCompleted,
		}
	}
	observedStates, err := suite.WaitForTaskStateTransitions(task.ID, expectedStates, 3*time.Minute)
	require.NoError(t, err, "Task %s should complete with proper state transitions", task.ID)
	t.Logf("Task %s completed with state transitions: %v", task.ID, observedStates)

	// 5. Retrieve output from task directory
	output, err := suite.GetTaskOutputFromWorkDir(task.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "Hello World from SLURM Cluster 1")
}

func TestSlurmCluster2_ParallelTasks(t *testing.T) {

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

	// Register SLURM cluster 2
	computeResource, err := suite.RegisterSlurmResource("cluster-2", "localhost:6819")
	require.NoError(t, err)
	assert.NotNil(t, computeResource)

	// Create multiple experiments to test parallel execution
	experiments := make([]*domain.Experiment, 3)
	for i := 0; i < 3; i++ {
		exp, err := suite.CreateTestExperiment(
			fmt.Sprintf("slurm-test-2-parallel-%d", i),
			fmt.Sprintf("echo 'Task %d from SLURM Cluster 2' && sleep %d", i, i+1),
		)
		require.NoError(t, err)
		assert.NotNil(t, exp)
		experiments[i] = exp
	}

	// Experiments are already submitted when created, so we can proceed with task execution
	for i, exp := range experiments {

		// Real task execution with worker binary staging
		tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 1, 0)
		require.NoError(t, err)
		require.Len(t, tasks, 1)

		task := tasks[0]

		// 1. Create task directory
		workDir, err := suite.CreateTaskDirectory(task.ID, task.ComputeResourceID)
		require.NoError(t, err)
		t.Logf("Created task directory: %s", workDir)

		// 2. Stage worker binary
		err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
		require.NoError(t, err)
		t.Logf("Staged worker binary for task %s", task.ID)

		// 3. Submit SLURM job (this will run the actual command, not the worker binary)
		err = suite.SubmitSlurmJob(task.ID)
		require.NoError(t, err)
		t.Logf("Submitted SLURM job for task %s", task.ID)

		// 4. Start task monitoring for real status updates
		err = suite.StartTaskMonitoring(task.ID)
		require.NoError(t, err)
		t.Logf("Started task monitoring for %s", task.ID)

		// 5. Wait for actual task completion
		err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 3*time.Minute)
		require.NoError(t, err, "Task %s should complete", task.ID)

		// 6. Retrieve output from task directory
		output, err := suite.GetTaskOutputFromWorkDir(task.ID)
		require.NoError(t, err)
		assert.Contains(t, output, fmt.Sprintf("Task %d from SLURM Cluster 2", i))
	}
}

func TestSlurmCluster3_LongRunning(t *testing.T) {

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

	// Register SLURM cluster 3
	computeResource, err := suite.RegisterSlurmResource("cluster-2", "localhost:6819")
	require.NoError(t, err)
	assert.NotNil(t, computeResource)

	// Submit long-running task
	exp, err := suite.CreateTestExperiment("slurm-test-3-long", "echo 'Starting long task' && sleep 10 && echo 'Long task completed'")
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Experiment is already submitted by CreateTestExperiment

	// Real task execution with worker binary staging
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)

	task := tasks[0]

	// 1. Create task directory
	workDir, err := suite.CreateTaskDirectory(task.ID, task.ComputeResourceID)
	require.NoError(t, err)
	t.Logf("Created task directory: %s", workDir)

	// 2. Stage worker binary
	err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
	require.NoError(t, err)
	t.Logf("Staged worker binary for task %s", task.ID)

	// 3. Start task monitoring for real status updates
	err = suite.StartTaskMonitoring(task.ID)
	require.NoError(t, err)
	t.Logf("Started task monitoring for %s", task.ID)

	// 4. Wait for actual task completion with longer timeout
	err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 3*time.Minute)
	require.NoError(t, err, "Task %s should complete", task.ID)

	// 5. Retrieve output from task directory
	output, err := suite.GetTaskOutputFromWorkDir(task.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "Starting long task")
	assert.Contains(t, output, "Long task completed")
}

func TestSlurmAllClusters_ConcurrentExecution(t *testing.T) {

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

	// Start all SLURM clusters
	err := suite.StartSlurmClusters(t)
	require.NoError(t, err)

	// Register all clusters
	clusters, err := suite.RegisterAllSlurmClusters()
	require.NoError(t, err)
	assert.Len(t, clusters, 2)

	// Submit tasks to all clusters concurrently
	var experiments []*domain.Experiment
	for i := 0; i < 2; i++ {
		exp, err := suite.CreateTestExperiment(
			fmt.Sprintf("concurrent-test-cluster-%d", i+1),
			fmt.Sprintf("echo 'Concurrent task on cluster %d' && sleep 3", i+1),
		)
		require.NoError(t, err)
		experiments = append(experiments, exp)

		// Submit experiment to generate tasks
		err = suite.SubmitExperiment(exp)
		require.NoError(t, err)
	}

	// Wait for all tasks to complete
	for i, exp := range experiments {
		// Real task execution with worker binary staging
		tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 1, 0)
		require.NoError(t, err)
		require.Len(t, tasks, 1)

		task := tasks[0]

		// 1. Create task directory
		workDir, err := suite.CreateTaskDirectory(task.ID, task.ComputeResourceID)
		require.NoError(t, err)
		t.Logf("Created task directory: %s", workDir)

		// 2. Stage worker binary
		err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
		require.NoError(t, err)
		t.Logf("Staged worker binary for task %s", task.ID)

		// 3. Start task monitoring for real status updates
		err = suite.StartTaskMonitoring(task.ID)
		require.NoError(t, err)
		t.Logf("Started task monitoring for %s", task.ID)

		// 4. Wait for actual task completion
		err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 2*time.Minute)
		require.NoError(t, err, "Task %d failed to complete", i)

		// 5. Retrieve output from task directory
		output, err := suite.GetTaskOutputFromWorkDir(task.ID)
		require.NoError(t, err)
		assert.Contains(t, output, fmt.Sprintf("Concurrent task on cluster %d", i+1))
	}
}

func TestSlurmCluster_ResourceRequirements(t *testing.T) {

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

	// Register SLURM cluster
	computeResource, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)
	assert.NotNil(t, computeResource)

	// Create experiment with specific resource requirements
	req := &domain.CreateExperimentRequest{
		Name:            "resource-test",
		Description:     "Test resource requirements",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "echo 'Resource test' && nproc && free -h && sleep 2",
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "value1",
				},
			},
		},
		Requirements: &domain.ResourceRequirements{
			CPUCores: 2,
			MemoryMB: 2048,
			DiskGB:   5,
			Walltime: "0:05:00", // 5 minutes
		},
	}

	resp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)
	assert.NotNil(t, resp.Experiment)

	// Submit experiment to generate tasks
	err = suite.SubmitExperiment(resp.Experiment)
	require.NoError(t, err)

	// Real task execution with worker binary staging
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), resp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)

	task := tasks[0]

	// 1. Create task directory
	workDir, err := suite.CreateTaskDirectory(task.ID, task.ComputeResourceID)
	require.NoError(t, err)
	t.Logf("Created task directory: %s", workDir)

	// 2. Stage worker binary
	err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
	require.NoError(t, err)
	t.Logf("Staged worker binary for task %s", task.ID)

	// 3. Start task monitoring for real status updates
	err = suite.StartTaskMonitoring(task.ID)
	require.NoError(t, err)
	t.Logf("Started task monitoring for %s", task.ID)

	// 4. Wait for actual task completion
	err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 2*time.Minute)
	require.NoError(t, err, "Task %s should complete", task.ID)

	// 5. Retrieve output from task directory
	output, err := suite.GetTaskOutputFromWorkDir(task.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "Resource test")
}
