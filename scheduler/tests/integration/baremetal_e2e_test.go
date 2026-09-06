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

func TestBareMetal_HelloWorld(t *testing.T) {

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

	// Start bare metal Ubuntu container
	err := suite.StartBareMetal(t)
	require.NoError(t, err)

	// Register bare metal resource with SSH (corrected endpoint)
	computeResource, err := suite.RegisterBaremetalResource("ubuntu-vm", "localhost:2225")
	require.NoError(t, err)
	assert.NotNil(t, computeResource)

	// Execute hello world via SSH
	exp, err := suite.CreateTestExperiment("baremetal-test", "echo 'Hello from Ubuntu Bare Metal' && sleep 3")
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Experiment is already submitted by CreateTestExperiment

	// Real task execution with worker binary staging
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)

	task := tasks[0]

	// Wait for task to be assigned to a compute resource
	assignedTask, err := suite.WaitForTaskAssignment(task.ID, 30*time.Second)
	require.NoError(t, err)
	require.NotEmpty(t, assignedTask.ComputeResourceID)
	task = assignedTask

	// Start gRPC server for worker communication
	_, grpcAddr := suite.StartGRPCServer(t)
	t.Logf("Started gRPC server at %s", grpcAddr)

	// Spawn worker for this experiment
	worker, workerCmd, err := suite.SpawnWorkerForExperiment(t, exp.ID, task.ComputeResourceID)
	require.NoError(t, err)
	defer suite.TerminateWorker(workerCmd)

	// Wait for worker to register and become idle
	err = suite.WaitForWorkerIdle(worker.ID, 20*time.Second)
	require.NoError(t, err)
	t.Logf("Worker %s is ready", worker.ID)

	// Wait for task to progress through all expected state transitions using hooks
	// Note: CREATED state transitions to QUEUED immediately during scheduling,
	// so we start observing from QUEUED
	expectedStates := []domain.TaskStatus{
		domain.TaskStatusQueued,
		domain.TaskStatusDataStaging,
		domain.TaskStatusEnvSetup,
		domain.TaskStatusRunning,
		domain.TaskStatusOutputStaging,
		domain.TaskStatusCompleted,
	}
	observedStates, err := suite.StateHook.WaitForTaskStateTransitions(task.ID, expectedStates, 90*time.Second)
	require.NoError(t, err, "Task %s should complete with proper state transitions", task.ID)
	t.Logf("Task %s completed with state transitions: %v", task.ID, observedStates)

	// Retrieve output from task directory
	output, err := suite.GetTaskOutputFromWorkDir(task.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "Hello from Ubuntu Bare Metal")
}

func TestBareMetal_FileOperations(t *testing.T) {

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

	// Start bare metal Ubuntu container
	err := suite.StartBareMetal(t)
	require.NoError(t, err)

	// Register bare metal resource (corrected endpoint)
	computeResource, err := suite.RegisterBaremetalResource("ubuntu-vm", "localhost:2225")
	require.NoError(t, err)
	assert.NotNil(t, computeResource)

	// Test file operations
	command := `
		echo "Creating test file" > /tmp/test_file.txt
		echo "File contents:" && cat /tmp/test_file.txt
		echo "File size:" && ls -la /tmp/test_file.txt
		echo "Directory listing:" && ls -la /tmp/
		sleep 2
	`

	exp, err := suite.CreateTestExperiment("baremetal-file-ops", command)
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

	// 4. Wait for actual task completion
	err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 1*time.Minute)
	require.NoError(t, err, "Task %s should complete", task.ID)

	// 5. Retrieve output from task directory
	output, err := suite.GetTaskOutputFromWorkDir(task.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "Creating test file")
	assert.Contains(t, output, "File contents:")
	assert.Contains(t, output, "File size:")
	assert.Contains(t, output, "Directory listing:")
}

func TestBareMetal_EnvironmentVariables(t *testing.T) {

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

	// Start bare metal Ubuntu container
	err := suite.StartBareMetal(t)
	require.NoError(t, err)

	// Register bare metal resource (corrected endpoint)
	computeResource, err := suite.RegisterBaremetalResource("ubuntu-vm", "localhost:2225")
	require.NoError(t, err)
	assert.NotNil(t, computeResource)

	// Test environment variables
	command := `
		export TEST_VAR="Hello from environment"
		export ANOTHER_VAR="Test value"
		echo "TEST_VAR: $TEST_VAR"
		echo "ANOTHER_VAR: $ANOTHER_VAR"
		echo "PATH: $PATH"
		echo "USER: $USER"
		echo "HOME: $HOME"
		sleep 2
	`

	exp, err := suite.CreateTestExperiment("baremetal-env-vars", command)
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

	// 4. Wait for actual task completion
	err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 1*time.Minute)
	require.NoError(t, err, "Task %s should complete", task.ID)

	// 5. Retrieve output from task directory
	output, err := suite.GetTaskOutputFromWorkDir(task.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "TEST_VAR: Hello from environment")
	assert.Contains(t, output, "ANOTHER_VAR: Test value")
	assert.Contains(t, output, "PATH:")
	assert.Contains(t, output, "USER:")
	assert.Contains(t, output, "HOME:")
}

func TestBareMetal_SystemInfo(t *testing.T) {

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

	// Start bare metal Ubuntu container
	err := suite.StartBareMetal(t)
	require.NoError(t, err)

	// Register bare metal resource (corrected endpoint)
	computeResource, err := suite.RegisterBaremetalResource("ubuntu-vm", "localhost:2225")
	require.NoError(t, err)
	assert.NotNil(t, computeResource)

	// Test system information
	command := `
		echo "=== System Information ==="
		echo "Hostname: $(hostname)"
		echo "OS: $(cat /etc/os-release | head -1)"
		echo "Kernel: $(uname -r)"
		echo "Architecture: $(uname -m)"
		echo "CPU cores: $(nproc)"
		echo "Memory: $(free -h | head -2)"
		echo "Disk space: $(df -h / | tail -1)"
		echo "Uptime: $(uptime)"
		sleep 3
	`

	exp, err := suite.CreateTestExperiment("baremetal-system-info", command)
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

	// 3. Submit task to compute resource
	err = suite.SubmitTaskToCluster(task, computeResource)
	require.NoError(t, err)
	t.Logf("Submitted task %s to compute resource %s", task.ID, task.ComputeResourceID)

	// 4. Start task monitoring for real status updates
	err = suite.StartTaskMonitoring(task.ID)
	require.NoError(t, err)
	t.Logf("Started task monitoring for %s", task.ID)

	// 5. Wait for actual task completion
	err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 2*time.Minute)
	require.NoError(t, err, "Task %s should complete", task.ID)

	// 6. Retrieve output from task directory
	output, err := suite.GetTaskOutputFromWorkDir(task.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "=== System Information ===")
	assert.Contains(t, output, "Hostname:")
	assert.Contains(t, output, "OS:")
	assert.Contains(t, output, "Kernel:")
	assert.Contains(t, output, "Architecture:")
	assert.Contains(t, output, "CPU cores:")
	assert.Contains(t, output, "Memory:")
	assert.Contains(t, output, "Disk space:")
	assert.Contains(t, output, "Uptime:")
}

func TestBareMetal_NetworkConnectivity(t *testing.T) {

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

	// Start bare metal Ubuntu container
	err := suite.StartBareMetal(t)
	require.NoError(t, err)

	// Register bare metal resource (corrected endpoint)
	computeResource, err := suite.RegisterBaremetalResource("ubuntu-vm", "localhost:2225")
	require.NoError(t, err)
	assert.NotNil(t, computeResource)

	// Test network connectivity
	command := `
		echo "=== Network Connectivity Test ==="
		echo "Testing localhost connectivity..."
		ping -c 3 127.0.0.1
		echo "Testing DNS resolution..."
		nslookup google.com || echo "DNS test failed"
		echo "Testing HTTP connectivity..."
		curl -s --connect-timeout 5 http://httpbin.org/ip || echo "HTTP test failed"
		sleep 2
	`

	exp, err := suite.CreateTestExperiment("baremetal-network", command)
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

	// 4. Wait for actual task completion
	err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 2*time.Minute)
	require.NoError(t, err, "Task %s should complete", task.ID)

	// 5. Retrieve output from task directory
	output, err := suite.GetTaskOutputFromWorkDir(task.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "=== Network Connectivity Test ===")
	assert.Contains(t, output, "Testing localhost connectivity...")
	assert.Contains(t, output, "Testing DNS resolution...")
	assert.Contains(t, output, "Testing HTTP connectivity...")
}

func TestBareMetal_ProcessManagement(t *testing.T) {

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

	// Start bare metal Ubuntu container
	err := suite.StartBareMetal(t)
	require.NoError(t, err)

	// Register bare metal resource (corrected endpoint)
	computeResource, err := suite.RegisterBaremetalResource("ubuntu-vm", "localhost:2225")
	require.NoError(t, err)
	assert.NotNil(t, computeResource)

	// Test process management
	command := `
		echo "=== Process Management Test ==="
		echo "Current processes:"
		ps aux | head -10
		echo "Process tree:"
		pstree || echo "pstree not available"
		echo "System load:"
		top -bn1 | head -5
		echo "Memory usage:"
		free -h
		sleep 3
	`

	exp, err := suite.CreateTestExperiment("baremetal-processes", command)
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

	// 4. Wait for actual task completion
	err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 1*time.Minute)
	require.NoError(t, err, "Task %s should complete", task.ID)

	// 5. Retrieve output from task directory
	output, err := suite.GetTaskOutputFromWorkDir(task.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "=== Process Management Test ===")
	assert.Contains(t, output, "Current processes:")
	assert.Contains(t, output, "System load:")
	assert.Contains(t, output, "Memory usage:")
}
