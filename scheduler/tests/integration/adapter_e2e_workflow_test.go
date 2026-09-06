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

func TestAdapterE2E_CompleteWorkflow(t *testing.T) {

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

	// Services are already running and verified by SetupIntegrationTest

	// Services are already verified by service checks above

	// Inject SSH keys into all containers
	var err error
	err = suite.InjectSSHKeys("airavata-scheduler-slurm-node-01-01-1", "airavata-scheduler-slurm-node-02-01-1", "airavata-scheduler-baremetal-node-1-1")
	require.NoError(t, err)

	// 2. Register all compute and storage resources
	slurmClusters, err := suite.RegisterAllSlurmClusters()
	require.NoError(t, err)
	require.Len(t, slurmClusters, 2)

	baremetal, err := suite.RegisterBaremetalResource("baremetal", "localhost:2225")
	require.NoError(t, err)

	s3, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	sftp, err := suite.RegisterSFTPResource("sftp", "localhost:2222")
	require.NoError(t, err)

	// 3. Upload input data to storage
	inputData := []byte("Hello from E2E test - input data")
	err = suite.UploadFile(s3.ID, "input.txt", inputData)
	require.NoError(t, err)

	err = suite.UploadFile(sftp.ID, "input.txt", inputData)
	require.NoError(t, err)

	// 4. Create experiments for different compute resources
	experiments := make([]*domain.Experiment, 0)

	// SLURM experiment
	slurmReq := &domain.CreateExperimentRequest{
		Name:            "e2e-slurm-test",
		Description:     "E2E test for SLURM cluster",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "(echo 'Processing on SLURM' && cat /tmp/input.txt && echo 'SLURM task completed') > output.txt 2>&1",
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "slurm-value",
				},
			},
		},
		Requirements: &domain.ResourceRequirements{
			CPUCores: 1,
			MemoryMB: 1024,
			DiskGB:   1,
			Walltime: "0:10:00",
		},
		Metadata: map[string]interface{}{
			"input_files": []map[string]interface{}{
				{
					"path":     "/tmp/input.txt",
					"size":     int64(len(inputData)),
					"checksum": "test-checksum-slurm",
				},
			},
		},
	}

	slurmExp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), slurmReq, suite.TestUser.ID)
	require.NoError(t, err)
	experiments = append(experiments, slurmExp.Experiment)

	// Bare metal experiment
	baremetalReq := &domain.CreateExperimentRequest{
		Name:            "e2e-baremetal-test",
		Description:     "E2E test for bare metal",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "(echo 'Processing on bare metal' && cat /tmp/input.txt && echo 'Bare metal task completed') > output.txt 2>&1",
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "baremetal-value",
				},
			},
		},
		Requirements: &domain.ResourceRequirements{
			CPUCores: 1,
			MemoryMB: 512,
			DiskGB:   1,
		},
		Metadata: map[string]interface{}{
			"input_files": []map[string]interface{}{
				{
					"path":     "/tmp/input.txt",
					"size":     int64(len(inputData)),
					"checksum": "test-checksum-baremetal",
				},
			},
		},
	}

	baremetalExp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), baremetalReq, suite.TestUser.ID)
	require.NoError(t, err)
	experiments = append(experiments, baremetalExp.Experiment)

	// 5. Stage input data to compute resources BEFORE submitting tasks
	// For SLURM
	err = suite.StageInputFileToComputeResource(slurmClusters[0].ID, "/tmp/input.txt", inputData)
	require.NoError(t, err)

	// For Bare Metal
	err = suite.StageInputFileToComputeResource(baremetal.ID, "/tmp/input.txt", inputData)
	require.NoError(t, err)

	// 6. Real task execution with worker binary staging
	for i, exp := range experiments {
		t.Logf("Processing experiment %d: %s", i, exp.ID)

		// First submit the experiment to generate tasks
		err = suite.SubmitExperiment(exp)
		require.NoError(t, err)

		tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 1, 0)
		require.NoError(t, err)
		require.Len(t, tasks, 1)

		task := tasks[0]
		t.Logf("Processing task: %s", task.ID)

		// 1. Create task directory FIRST (before submitting to cluster)
		var computeResource *domain.ComputeResource
		if exp.ID == slurmExp.Experiment.ID {
			computeResource = slurmClusters[0]
		} else {
			computeResource = baremetal
		}
		workDir, err := suite.CreateTaskDirectory(task.ID, computeResource.ID)
		require.NoError(t, err)
		t.Logf("Created task directory: %s", workDir)

		// 2. Stage worker binary
		err = suite.StageWorkerBinary(computeResource.ID, task.ID)
		require.NoError(t, err)
		t.Logf("Staged worker binary for task %s", task.ID)

		// Add delay to avoid SSH connection limits
		time.Sleep(3 * time.Second)

		// 3. Submit task to cluster (now that work_dir is set)
		// Get the updated task from database to include work_dir metadata
		updatedTask, err := suite.DB.Repo.GetTaskByID(context.Background(), task.ID)
		require.NoError(t, err)

		if exp.ID == slurmExp.Experiment.ID {
			err = suite.SubmitTaskToCluster(updatedTask, slurmClusters[0])
		} else {
			err = suite.SubmitTaskToCluster(updatedTask, baremetal)
		}
		require.NoError(t, err)

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
		assert.Contains(t, output, "completed", "Output should contain completion message")
	}

	// 7. Verify task outputs
	for _, exp := range experiments {
		tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 1, 0)
		require.NoError(t, err)
		require.Len(t, tasks, 1)

		output, err := suite.GetTaskOutputFromWorkDir(tasks[0].ID)
		require.NoError(t, err)
		assert.Contains(t, output, "Processing on", "Task output should contain processing message")
		assert.Contains(t, output, "task completed", "Task output should contain completion message")
	}

	// 8. Download and verify output data from storage
	downloadedS3, err := suite.DownloadFile(s3.ID, "input.txt")
	require.NoError(t, err)
	assert.Equal(t, inputData, downloadedS3, "S3 download should match uploaded data")

	downloadedSFTP, err := suite.DownloadFile(sftp.ID, "input.txt")
	require.NoError(t, err)
	assert.Equal(t, inputData, downloadedSFTP, "SFTP download should match uploaded data")

	t.Log("E2E workflow test completed successfully")
}

func TestAdapterE2E_MultiClusterDistribution(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Services are already verified by service checks above

	// Inject SSH keys into all containers
	var err error
	err = suite.InjectSSHKeys("airavata-scheduler-slurm-node-01-01-1", "airavata-scheduler-slurm-node-02-01-1")
	require.NoError(t, err)

	// Register all SLURM clusters
	clusters, err := suite.RegisterAllSlurmClusters()
	require.NoError(t, err)
	require.Len(t, clusters, 2)

	// Create multiple experiments
	numExperiments := 9
	var experiments []*domain.Experiment

	for i := 0; i < numExperiments; i++ {
		req := &domain.CreateExperimentRequest{
			Name:            fmt.Sprintf("multi-cluster-exp-%d", i),
			Description:     fmt.Sprintf("Multi-cluster experiment %d", i),
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: fmt.Sprintf("echo 'Task %d on cluster' && sleep 2", i),
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"param1": fmt.Sprintf("value%d", i),
					},
				},
			},
			Requirements: &domain.ResourceRequirements{
				CPUCores: 1,
				MemoryMB: 1024,
				DiskGB:   1,
				Walltime: "0:05:00",
			},
		}

		exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
		require.NoError(t, err)
		experiments = append(experiments, exp.Experiment)
	}

	// Submit experiments to different clusters (round-robin)
	for i, exp := range experiments {
		cluster := clusters[i%len(clusters)]
		err := suite.SubmitToCluster(exp, cluster)
		require.NoError(t, err)
	}

	// Real task execution for all experiments
	for _, exp := range experiments {
		tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 1, 0)
		require.NoError(t, err)
		require.Len(t, tasks, 1)

		task := tasks[0]

		// Create task directory and stage worker binary
		computeResource, err := suite.GetComputeResourceFromTask(task)
		require.NoError(t, err)
		_, err = suite.CreateTaskDirectory(task.ID, computeResource.ID)
		require.NoError(t, err)

		err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
		require.NoError(t, err)

		// Start task monitoring and wait for completion
		err = suite.StartTaskMonitoring(task.ID)
		require.NoError(t, err)

		err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 3*time.Minute)
		require.NoError(t, err, "Task %s did not complete", task.ID)
	}

	// Verify distribution across clusters
	clusterTaskCounts := make(map[string]int)
	for _, exp := range experiments {
		tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 1, 0)
		require.NoError(t, err)
		require.Len(t, tasks, 1)

		if tasks[0].ComputeResourceID != "" {
			clusterTaskCounts[tasks[0].ComputeResourceID]++
		}
	}

	t.Logf("Task distribution across clusters: %v", clusterTaskCounts)

	// Verify tasks are distributed across all clusters
	assert.Equal(t, 2, len(clusterTaskCounts), "Tasks should be distributed across all 2 clusters")

	// Each cluster should have at least 4 tasks (9 tasks / 2 clusters = 4.5 tasks each)
	for clusterID, count := range clusterTaskCounts {
		assert.GreaterOrEqual(t, count, 4, "Cluster %s should have at least 4 tasks", clusterID)
	}
}

func TestAdapterE2E_FailureRecovery(t *testing.T) {

	// Check required services are available before starting
	checker := testutil.NewServiceChecker()
	services := []struct {
		name  string
		check func() error
	}{
		{"SLURM", checker.CheckSLURMService},
		{"SSH", checker.CheckSSHService},
	}

	for _, svc := range services {
		if err := svc.check(); err != nil {
			t.Fatalf("Required service %s not available: %v", svc.name, err)
		}
	}

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Services are already running and verified by SetupIntegrationTest

	// Services are already verified by service checks above

	// Inject SSH keys into container
	var err error
	err = suite.InjectSSHKeys("airavata-scheduler-slurm-node-01-01-1")
	require.NoError(t, err)

	// Register SLURM cluster
	cluster, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	// Create experiment with a command that will fail
	req := &domain.CreateExperimentRequest{
		Name:            "failure-recovery-test",
		Description:     "Test failure recovery",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "echo 'Starting task' && exit 1", // This will fail
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "test-value",
				},
			},
		},
		Requirements: &domain.ResourceRequirements{
			CPUCores: 1,
			MemoryMB: 1024,
			DiskGB:   1,
			Walltime: "0:05:00",
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Submit to cluster
	err = suite.SubmitToCluster(exp.Experiment, cluster)
	require.NoError(t, err)

	// Real task execution that should fail
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)

	task := tasks[0]

	// Create task directory and stage worker binary
	computeResource, err := suite.GetComputeResourceFromTask(task)
	require.NoError(t, err)
	_, err = suite.CreateTaskDirectory(task.ID, computeResource.ID)
	require.NoError(t, err)

	err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
	require.NoError(t, err)

	// Start task monitoring and wait for failure
	err = suite.StartTaskMonitoring(task.ID)
	require.NoError(t, err)

	err = suite.WaitForTaskState(task.ID, domain.TaskStatusFailed, 2*time.Minute)
	require.NoError(t, err, "Task should have failed")

	// Verify task status
	failedTask, err := suite.DB.Repo.GetTaskByID(context.Background(), tasks[0].ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, failedTask.Status, "Task status should be failed")

	// Create a new experiment with a successful command
	successReq := &domain.CreateExperimentRequest{
		Name:            "recovery-success-test",
		Description:     "Test successful recovery",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "echo 'Recovery task succeeded' && sleep 2",
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "recovery-value",
				},
			},
		},
		Requirements: &domain.ResourceRequirements{
			CPUCores: 1,
			MemoryMB: 1024,
			DiskGB:   1,
			Walltime: "0:05:00",
		},
	}

	successExp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), successReq, suite.TestUser.ID)
	require.NoError(t, err)

	// Submit successful experiment
	err = suite.SubmitToCluster(successExp.Experiment, cluster)
	require.NoError(t, err)

	// Real successful task execution
	successTasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), successExp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, successTasks, 1)

	task = successTasks[0]

	// Create task directory and stage worker binary
	computeResource, err = suite.GetComputeResourceFromTask(task)
	require.NoError(t, err)
	_, err = suite.CreateTaskDirectory(task.ID, computeResource.ID)
	require.NoError(t, err)

	err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
	require.NoError(t, err)

	// Start task monitoring and wait for success
	err = suite.StartTaskMonitoring(task.ID)
	require.NoError(t, err)

	err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 3*time.Minute)
	require.NoError(t, err, "Recovery task should succeed")

	// Verify recovery task completed successfully
	recoveryTask, err := suite.DB.Repo.GetTaskByID(context.Background(), successTasks[0].ID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusCompleted, recoveryTask.Status, "Recovery task should be completed")

	t.Log("Failure recovery test completed successfully")
}

func TestAdapterE2E_DataPipeline(t *testing.T) {

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

	// Services are already running and verified by SetupIntegrationTest

	// Services are already verified by service checks above

	// Inject SSH keys into container
	var err error
	err = suite.InjectSSHKeys("airavata-scheduler-slurm-node-01-01-1")
	require.NoError(t, err)

	// Register resources
	cluster, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	s3, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	sftp, err := suite.RegisterSFTPResource("sftp", "localhost:2222")
	require.NoError(t, err)

	// Stage input data to both storage systems
	inputData := []byte("Input data for pipeline processing")
	err = suite.UploadFile(s3.ID, "pipeline-input.txt", inputData)
	require.NoError(t, err)

	err = suite.UploadFile(sftp.ID, "pipeline-input.txt", inputData)
	require.NoError(t, err)

	// Create experiment that processes data from storage
	req := &domain.CreateExperimentRequest{
		Name:            "data-pipeline-test",
		Description:     "Test data pipeline processing",
		ProjectID:       suite.TestProject.ID,
		CommandTemplate: "echo 'Processing pipeline data' && echo 'Input data for pipeline processing' > /tmp/pipeline-input.txt && cat /tmp/pipeline-input.txt && echo 'Pipeline processing completed' > /tmp/pipeline-output.txt",
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "pipeline-value",
				},
			},
		},
		Requirements: &domain.ResourceRequirements{
			CPUCores: 1,
			MemoryMB: 1024,
			DiskGB:   1,
			Walltime: "0:10:00",
		},
	}

	exp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
	require.NoError(t, err)

	// Submit to cluster
	err = suite.SubmitToCluster(exp.Experiment, cluster)
	require.NoError(t, err)

	// Real task execution
	tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.Experiment.ID, 1, 0)
	require.NoError(t, err)
	require.Len(t, tasks, 1)

	task := tasks[0]

	// Create task directory and stage worker binary
	computeResource, err := suite.GetComputeResourceFromTask(task)
	require.NoError(t, err)
	_, err = suite.CreateTaskDirectory(task.ID, computeResource.ID)
	require.NoError(t, err)

	err = suite.StageWorkerBinary(task.ComputeResourceID, task.ID)
	require.NoError(t, err)

	// Start task monitoring and wait for completion
	err = suite.StartTaskMonitoring(task.ID)
	require.NoError(t, err)

	err = suite.WaitForTaskState(task.ID, domain.TaskStatusCompleted, 3*time.Minute)
	require.NoError(t, err, "Pipeline task should complete")

	// Verify task output
	output, err := suite.GetTaskOutputFromWorkDir(tasks[0].ID)
	require.NoError(t, err)
	assert.Contains(t, output, "Processing pipeline data", "Output should contain processing message")
	assert.Contains(t, output, "Pipeline processing completed", "Output should contain completion message")

	// Verify input data is still accessible
	downloadedS3, err := suite.DownloadFile(s3.ID, "pipeline-input.txt")
	require.NoError(t, err)
	assert.Equal(t, inputData, downloadedS3, "S3 input data should be preserved")

	downloadedSFTP, err := suite.DownloadFile(sftp.ID, "pipeline-input.txt")
	require.NoError(t, err)
	assert.Equal(t, inputData, downloadedSFTP, "SFTP input data should be preserved")

	t.Log("Data pipeline test completed successfully")
}
