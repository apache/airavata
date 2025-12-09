package integration

import (
	"context"
	"fmt"
	"strings"
	"sync"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestWorkflow_ComputeAndStorage(t *testing.T) {

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

	// Setup: Register compute + storage
	_, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	minio, err := suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	// Services are already verified by service checks above

	// Step 1: Stage input data to MinIO
	inputData := []byte("input data for processing")
	err = suite.UploadFile(minio.ID, "input.txt", inputData)
	require.NoError(t, err)

	// Step 2: Create experiment that reads input, processes, writes output
	command := `
		echo "Starting workflow processing..."
		echo "Input data: $(cat input.txt)" > output.txt
		echo "Processing completed at $(date)" >> output.txt
		echo "System info: $(uname -a)" >> output.txt
		sleep 5
		echo "Workflow completed successfully"
	`

	exp, err := suite.CreateTestExperiment("workflow-test", command)
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Get task ID
	taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
	require.NoError(t, err)

	// Step 3: Wait for completion
	err = suite.WaitForTaskCompletion(taskID, 30*time.Second)
	require.NoError(t, err)

	// Step 4: Verify output was created
	output, err := suite.GetTaskOutput(taskID)
	require.NoError(t, err)
	assert.Contains(t, output, "Starting workflow processing...")
	assert.Contains(t, output, "Workflow completed successfully")
}

func TestWorkflow_MultiClusterDistribution(t *testing.T) {

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

	// Submit tasks to all clusters
	var experiments []*domain.Experiment
	for i := 0; i < 2; i++ {
		command := fmt.Sprintf(`
			echo "Task %d starting on cluster %d" 
			echo "Cluster ID: %s"
			echo "Timestamp: $(date)"
			sleep %d
			echo "Task %d completed on cluster %d"
		`, i+1, i+1, clusters[i].ID, i+2, i+1, i+1)

		exp, err := suite.CreateTestExperiment(
			fmt.Sprintf("multi-cluster-test-%d", i+1),
			command,
		)
		require.NoError(t, err)
		experiments = append(experiments, exp)
	}

	// Wait for all tasks to complete
	for i, exp := range experiments {
		// Get task ID
		taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
		require.NoError(t, err)

		err = suite.WaitForTaskCompletion(taskID, 3*time.Minute)
		require.NoError(t, err, "Task %d failed to complete", i+1)

		// Verify output
		output, err := suite.GetTaskOutput(taskID)
		require.NoError(t, err)
		assert.Contains(t, output, fmt.Sprintf("Task %d starting on cluster %d", i+1, i+1))
		assert.Contains(t, output, fmt.Sprintf("Task %d completed on cluster %d", i+1, i+1))
	}
}

func TestWorkflow_FailureRecovery(t *testing.T) {

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

	// Register cluster
	slurm, err := suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)
	assert.NotNil(t, slurm)

	// Create experiment that will fail
	command := `
		echo "Starting task that will fail..."
		sleep 2
		echo "About to fail..."
		exit 1
	`

	exp, err := suite.CreateTestExperiment("failure-test", command)
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Get task ID
	taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
	require.NoError(t, err)

	// Wait for task completion (should fail)
	err = suite.WaitForTaskCompletion(taskID, 1*time.Minute)
	require.NoError(t, err) // WaitForTaskCompletion should not error even if task fails

	// Verify task failed
	task, err := suite.DB.Repo.GetTaskByID(context.Background(), taskID)
	require.NoError(t, err)
	assert.Equal(t, domain.TaskStatusFailed, task.Status)
}

func TestWorkflow_DataPipeline(t *testing.T) {

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

	// Register compute and storage resources
	var err error
	_, err = suite.RegisterSlurmResource("cluster-1", "localhost:6817")
	require.NoError(t, err)

	_, err = suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	_, err = suite.RegisterSFTPResource("sftp", "localhost:2222")
	require.NoError(t, err)

	// Step 1: Upload input data to MinIO
	inputData := []byte("raw data for processing pipeline")
	err = suite.UploadFile("minio", "raw-data.txt", inputData)
	require.NoError(t, err)

	// Step 2: Create data processing pipeline
	command := `
		echo "=== Data Processing Pipeline ==="
		echo "Step 1: Download input data"
		# In real implementation, this would download from MinIO
		echo "raw data for processing pipeline" > input.txt
		
		echo "Step 2: Process data"
		cat input.txt | tr 'a-z' 'A-Z' > processed.txt
		wc -l processed.txt > stats.txt
		
		echo "Step 3: Generate report"
		echo "Processing Report" > report.txt
		echo "Input size: $(wc -c < input.txt) bytes" >> report.txt
		echo "Output size: $(wc -c < processed.txt) bytes" >> report.txt
		echo "Line count: $(cat stats.txt)" >> report.txt
		echo "Processing completed at: $(date)" >> report.txt
		
		echo "Step 4: Upload results"
		# In real implementation, this would upload to SFTP
		echo "Results uploaded to SFTP"
		
		sleep 3
		echo "Pipeline completed successfully"
	`

	exp, err := suite.CreateTestExperiment("data-pipeline", command)
	require.NoError(t, err)
	assert.NotNil(t, exp)

	// Get task ID
	taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
	require.NoError(t, err)

	// Step 3: Wait for completion
	err = suite.WaitForTaskCompletion(taskID, 30*time.Second)
	require.NoError(t, err)

	// Step 4: Verify pipeline output
	output, err := suite.GetTaskOutput(taskID)
	require.NoError(t, err)
	assert.Contains(t, output, "=== Data Processing Pipeline ===")
	assert.Contains(t, output, "Step 1: Download input data")
	assert.Contains(t, output, "Step 2: Process data")
	assert.Contains(t, output, "Step 3: Generate report")
	assert.Contains(t, output, "Step 4: Upload results")
	assert.Contains(t, output, "Pipeline completed successfully")
}

func TestWorkflow_ResourceScaling(t *testing.T) {

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

	// Register all resources
	clusters, err := suite.RegisterAllSlurmClusters()
	require.NoError(t, err)
	assert.Len(t, clusters, 2)

	_, err = suite.RegisterS3Resource("minio", "localhost:9000")
	require.NoError(t, err)

	// Create experiments with different resource requirements
	experiments := []struct {
		name         string
		command      string
		requirements *domain.ResourceRequirements
	}{
		{
			name:    "light-task",
			command: "echo 'Light task' && sleep 1",
			requirements: &domain.ResourceRequirements{
				CPUCores: 1,
				MemoryMB: 512,
				DiskGB:   1,
				Walltime: "0:02:00",
			},
		},
		{
			name:    "medium-task",
			command: "echo 'Medium task' && sleep 3",
			requirements: &domain.ResourceRequirements{
				CPUCores: 2,
				MemoryMB: 1024,
				DiskGB:   2,
				Walltime: "0:05:00",
			},
		},
		{
			name:    "heavy-task",
			command: "echo 'Heavy task' && sleep 5",
			requirements: &domain.ResourceRequirements{
				CPUCores: 4,
				MemoryMB: 2048,
				DiskGB:   5,
				Walltime: "0:10:00",
			},
		},
	}

	// Submit all experiments
	var expResults []*domain.Experiment
	for _, expSpec := range experiments {
		req := &domain.CreateExperimentRequest{
			Name:            expSpec.name,
			Description:     "Resource scaling test",
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: expSpec.command,
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"param1": "value1",
					},
				},
			},
			Requirements: expSpec.requirements,
		}

		resp, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
		require.NoError(t, err)
		expResults = append(expResults, resp.Experiment)
	}

	// Wait for all experiments to complete
	for i, exp := range expResults {
		// Get task ID
		taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
		require.NoError(t, err)

		err = suite.WaitForTaskCompletion(taskID, 30*time.Second)
		require.NoError(t, err, "Experiment %s failed to complete", experiments[i].name)

		// Verify output
		output, err := suite.GetTaskOutput(taskID)
		require.NoError(t, err)
		assert.Contains(t, output, experiments[i].name)
	}
}

func TestWorkflow_ConcurrentExperiments(t *testing.T) {

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

	// Submit 10 experiments distributed across clusters
	var wg sync.WaitGroup
	results := make(chan error, 10)

	for i := 0; i < 10; i++ {
		wg.Add(1)
		go func(idx int) {
			defer wg.Done()

			cluster := clusters[idx%3]
			command := fmt.Sprintf(`
				echo "Concurrent experiment %d starting on cluster %s"
				echo "Timestamp: $(date)"
				sleep %d
				echo "Concurrent experiment %d completed"
			`, idx, cluster.ID, idx%3+1, idx)

			exp, err := suite.CreateTestExperiment(
				fmt.Sprintf("concurrent-%d", idx),
				command,
			)
			if err != nil {
				results <- err
				return
			}

			// Submit to specific cluster
			err = suite.SubmitToCluster(exp, cluster)
			if err != nil {
				results <- err
				return
			}

			// Get task ID
			taskID, err := suite.GetTaskIDFromExperiment(exp.ID)
			if err != nil {
				results <- err
				return
			}

			// Wait for completion
			err = suite.WaitForTaskCompletion(taskID, 30*time.Second)
			if err != nil {
				results <- err
				return
			}

			// Verify output
			output, err := suite.GetTaskOutput(taskID)
			if err != nil {
				results <- err
				return
			}

			if !strings.Contains(output, fmt.Sprintf("Concurrent experiment %d starting", idx)) {
				results <- fmt.Errorf("output verification failed for experiment %d", idx)
				return
			}

			results <- nil
		}(i)
	}

	wg.Wait()
	close(results)

	// Check results
	successCount := 0
	for err := range results {
		if err == nil {
			successCount++
		} else {
			t.Errorf("Concurrent experiment failed: %v", err)
		}
	}

	assert.Equal(t, 10, successCount, "All concurrent experiments should succeed")
}
