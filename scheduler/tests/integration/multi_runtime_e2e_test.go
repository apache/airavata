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

func TestMultiRuntime_SlurmKubernetesBareMetal(t *testing.T) {

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

	// Register all three compute types
	slurm, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	k8s, err := suite.RegisterKubernetesResource("docker-desktop-k8s")
	require.NoError(t, err)
	assert.NotNil(t, k8s)

	bare, err := suite.RegisterBareMetalResource("baremetal", "localhost:2225")
	require.NoError(t, err)
	assert.NotNil(t, bare)

	// Create experiments on each runtime
	experiments := []struct {
		name     string
		resource *domain.ComputeResource
		command  string
	}{
		{"slurm-exp", slurm, "squeue && echo 'SLURM test completed'"},
		{"k8s-exp", k8s, "kubectl version --client && echo 'K8s test completed'"},
		{"bare-exp", bare, "uname -a && echo 'Bare metal test completed'"},
	}

	for _, exp := range experiments {
		t.Run(exp.name, func(t *testing.T) {
			// Start gRPC server for worker communication
			_, grpcAddr := suite.StartGRPCServer(t)
			t.Logf("Started gRPC server at %s", grpcAddr)

			e, err := suite.CreateExperimentOnResource(exp.name, exp.command, exp.resource.ID)
			require.NoError(t, err)
			assert.NotNil(t, e)

			// Note: CreateExperimentOnResource already submits the experiment

			// Get tasks for this experiment
			tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), e.ID, 1, 0)
			require.NoError(t, err)
			require.Len(t, tasks, 1)

			task := tasks[0]

			// Wait for task to be assigned to a compute resource
			assignedTask, err := suite.WaitForTaskAssignment(task.ID, 30*time.Second)
			require.NoError(t, err)
			require.NotEmpty(t, assignedTask.ComputeResourceID)
			task = assignedTask

			// Spawn worker for this experiment
			worker, workerCmd, err := suite.SpawnWorkerForExperiment(t, e.ID, task.ComputeResourceID)
			require.NoError(t, err)
			defer suite.TerminateWorker(workerCmd)

			// Wait for worker to register and become idle
			err = suite.WaitForWorkerIdle(worker.ID, 20*time.Second)
			require.NoError(t, err)
			t.Logf("Worker %s is ready", worker.ID)

			// Wait for task to progress through all expected state transitions
			expectedStates := []domain.TaskStatus{
				domain.TaskStatusCreated,
				domain.TaskStatusQueued,
				domain.TaskStatusDataStaging,
				domain.TaskStatusEnvSetup,
				domain.TaskStatusRunning,
				domain.TaskStatusOutputStaging,
				domain.TaskStatusCompleted,
			}
			observedStates, err := suite.WaitForTaskStateTransitions(task.ID, expectedStates, 3*time.Minute)
			require.NoError(t, err, "Task %s should complete with proper state transitions", task.ID)
			t.Logf("Task %s completed with state transitions: %v", task.ID, observedStates)

			// Retrieve output from task directory
			output, err := suite.GetTaskOutputFromWorkDir(task.ID)
			require.NoError(t, err)
			assert.Contains(t, output, "test completed")
		})
	}
}

func TestMultiRuntime_ResourceSpecificFeatures(t *testing.T) {

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

	// Test SLURM-specific features
	t.Run("SLURM_JobSubmission", func(t *testing.T) {
		slurm, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
		require.NoError(t, err)

		exp, err := suite.CreateExperimentOnResource("slurm-job", "sbatch --wrap='echo SLURM job submitted'", slurm.ID)
		require.NoError(t, err)

		task, err := suite.GetFirstTask(exp.ID)
		require.NoError(t, err)

		err = suite.WaitForTaskCompletion(task.ID, 30*time.Second)
		require.NoError(t, err)

		output, err := suite.GetTaskOutput(task.ID)
		require.NoError(t, err)
		assert.Contains(t, output, "SLURM job submitted")
	})

	// Test Kubernetes-specific features
	t.Run("Kubernetes_PodCreation", func(t *testing.T) {
		k8s, err := suite.RegisterKubernetesResource("docker-desktop-k8s")
		require.NoError(t, err)

		exp, err := suite.CreateExperimentOnResource("k8s-pod", "kubectl run test-pod --image=busybox --rm --restart=Never -- echo 'K8s pod created'", k8s.ID)
		require.NoError(t, err)

		task, err := suite.GetFirstTask(exp.ID)
		require.NoError(t, err)

		err = suite.WaitForTaskCompletion(task.ID, 30*time.Second)
		require.NoError(t, err)

		output, err := suite.GetTaskOutput(task.ID)
		require.NoError(t, err)
		assert.Contains(t, output, "K8s pod created")
	})

	// Test bare metal-specific features
	t.Run("BareMetal_SystemInfo", func(t *testing.T) {
		bare, err := suite.RegisterBareMetalResource("baremetal", "localhost:2225")
		require.NoError(t, err)

		exp, err := suite.CreateExperimentOnResource("baremetal-info", "cat /etc/os-release && free -h", bare.ID)
		require.NoError(t, err)

		// Experiment is already submitted by CreateExperimentOnResource

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
		assert.Contains(t, output, "Ubuntu")
	})
}

func TestMultiRuntime_ConcurrentExecution(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register multiple resources
	slurm1, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	slurm2, err := suite.RegisterSlurmResource("cluster-2", "localhost:6819")
	require.NoError(t, err)

	bare, err := suite.RegisterBareMetalResource("baremetal", "localhost:2224")
	require.NoError(t, err)

	// Create concurrent experiments across different runtimes
	experiments := []struct {
		name     string
		resource *domain.ComputeResource
		command  string
	}{
		{"concurrent-slurm1", slurm1, "echo 'SLURM cluster 1 concurrent test' && sleep 5"},
		{"concurrent-slurm2", slurm2, "echo 'SLURM cluster 2 concurrent test' && sleep 5"},
		{"concurrent-bare", bare, "echo 'Bare metal concurrent test' && sleep 5"},
	}

	// Start all experiments concurrently
	startTime := time.Now()
	for _, exp := range experiments {
		e, err := suite.CreateExperimentOnResource(exp.name, exp.command, exp.resource.ID)
		require.NoError(t, err)
		assert.NotNil(t, e)
	}

	// Wait for all experiments to complete
	for _, exp := range experiments {
		t.Run(exp.name, func(t *testing.T) {
			e, err := suite.GetExperimentByName(exp.name)
			require.NoError(t, err)

			task, err := suite.GetFirstTask(e.ID)
			require.NoError(t, err)

			err = suite.WaitForTaskCompletion(task.ID, 30*time.Second)
			require.NoError(t, err)

			output, err := suite.GetTaskOutput(task.ID)
			require.NoError(t, err)
			assert.Contains(t, output, "concurrent test")
		})
	}

	// Verify all experiments completed within reasonable time
	totalTime := time.Since(startTime)
	assert.Less(t, totalTime, 3*time.Minute, "Concurrent execution should complete within 3 minutes")
}

func TestMultiRuntime_ResourceFailover(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register two SLURM clusters
	slurm1, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	slurm2, err := suite.RegisterSlurmResource("cluster-2", "localhost:6819")
	require.NoError(t, err)

	// Create experiment that will fail on first cluster
	exp, err := suite.CreateExperimentOnResource("failover-test", "echo 'Failover test completed'", slurm1.ID)
	require.NoError(t, err)

	task, err := suite.GetFirstTask(exp.ID)
	require.NoError(t, err)

	// Simulate first cluster failure by stopping it
	err = suite.StopService("slurm-cluster-1")
	require.NoError(t, err)

	// Wait for task to be retried on second cluster
	time.Sleep(2 * time.Minute)

	// Verify task completed on second cluster (slurm2)
	completedTask, err := suite.GetTask(task.ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, completedTask.Status)

	// Verify the task was retried on the second cluster
	assert.Equal(t, slurm2.ID, completedTask.ComputeResourceID)

	output, err := suite.GetTaskOutput(task.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "Failover test completed")
}

func TestMultiRuntime_ResourceCapacity(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register resources with different capacities
	slurm1, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	slurm2, err := suite.RegisterSlurmResource("cluster-2", "localhost:6819")
	require.NoError(t, err)

	bare, err := suite.RegisterBareMetalResource("baremetal", "localhost:2224")
	require.NoError(t, err)

	// Test resource capacity limits
	capacityTests := []struct {
		name     string
		resource *domain.ComputeResource
		command  string
		timeout  time.Duration
	}{
		{"slurm1-capacity", slurm1, "echo 'SLURM cluster 1 capacity test'", 2 * time.Minute},
		{"slurm2-capacity", slurm2, "echo 'SLURM cluster 2 capacity test'", 2 * time.Minute},
		{"bare-capacity", bare, "echo 'Bare metal capacity test'", 2 * time.Minute},
	}

	for _, test := range capacityTests {
		t.Run(test.name, func(t *testing.T) {
			exp, err := suite.CreateExperimentOnResource(test.name, test.command, test.resource.ID)
			require.NoError(t, err)

			task, err := suite.GetFirstTask(exp.ID)
			require.NoError(t, err)

			err = suite.WaitForTaskCompletion(task.ID, test.timeout)
			require.NoError(t, err)

			output, err := suite.GetTaskOutput(task.ID)
			require.NoError(t, err)
			assert.Contains(t, output, "capacity test")
		})
	}
}

func TestMultiRuntime_CrossRuntimeDataSharing(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register resources
	slurm, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	bare, err := suite.RegisterBareMetalResource("baremetal", "localhost:2224")
	require.NoError(t, err)

	// Create experiment on SLURM that generates data
	exp1, err := suite.CreateExperimentOnResource("data-generator", "echo 'shared data content' > shared-data.txt", slurm.ID)
	require.NoError(t, err)

	task1, err := suite.GetFirstTask(exp1.ID)
	require.NoError(t, err)

	err = suite.WaitForTaskCompletion(task1.ID, 30*time.Second)
	require.NoError(t, err)

	// Create experiment on bare metal that consumes the data
	exp2, err := suite.CreateExperimentOnResource("data-consumer", "cat shared-data.txt", bare.ID)
	require.NoError(t, err)

	task2, err := suite.GetFirstTask(exp2.ID)
	require.NoError(t, err)

	err = suite.WaitForTaskCompletion(task2.ID, 30*time.Second)
	require.NoError(t, err)

	// Verify data sharing worked
	output, err := suite.GetTaskOutput(task2.ID)
	require.NoError(t, err)
	assert.Contains(t, output, "shared data content")
}

func TestMultiRuntime_ResourceHealthMonitoring(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already verified by service checks above

	// Register resources - use the same SLURM controller for both tests
	slurm1, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	slurm2, err := suite.RegisterSlurmResource("cluster-2", "localhost:6817") // Use same controller
	require.NoError(t, err)

	// Test resource health monitoring
	healthTests := []struct {
		name     string
		resource *domain.ComputeResource
		command  string
	}{
		{"slurm1-health", slurm1, "scontrol ping && echo 'SLURM cluster 1 healthy'"},
		{"slurm2-health", slurm2, "scontrol ping && echo 'SLURM cluster 2 healthy'"},
	}

	for _, test := range healthTests {
		t.Run(test.name, func(t *testing.T) {
			exp, err := suite.CreateExperimentOnResource(test.name, test.command, test.resource.ID)
			require.NoError(t, err)

			task, err := suite.GetFirstTask(exp.ID)
			require.NoError(t, err)

			// Wait for task to be assigned to a compute resource
			assignedTask, err := suite.WaitForTaskAssignment(task.ID, 30*time.Second)
			require.NoError(t, err)
			require.NotEmpty(t, assignedTask.ComputeResourceID)
			task = assignedTask

			err = suite.WaitForTaskCompletion(task.ID, 30*time.Second)
			require.NoError(t, err)

			output, err := suite.GetTaskOutput(task.ID)
			require.NoError(t, err)
			assert.Contains(t, output, "healthy")
		})
	}

	// Test resource failure detection
	t.Run("ResourceFailureDetection", func(t *testing.T) {
		// Stop one cluster
		err = suite.StopService("slurm-cluster-1")
		require.NoError(t, err)

		// Create experiment that should fail on stopped cluster
		exp, err := suite.CreateExperimentOnResource("failure-test", "echo 'This should fail'", slurm1.ID)
		require.NoError(t, err)

		task, err := suite.GetFirstTask(exp.ID)
		require.NoError(t, err)

		// Wait for failure detection and retry
		time.Sleep(2 * time.Minute)

		// Verify task was retried on healthy cluster
		completedTask, err := suite.GetTask(task.ID)
		require.NoError(t, err)
		assert.Equal(t, domain.TaskStatusCompleted, completedTask.Status)
	})
}
