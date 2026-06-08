package performance

import (
	"context"
	"fmt"
	"sync"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// TestConcurrentExperimentSubmissions tests the system's ability to handle concurrent experiment submissions
func TestConcurrentExperimentSubmissions(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Start all SLURM clusters
	err := suite.Compose.StartServices(t, "slurm-controller", "slurm-node-1", "slurm-node-2", "slurm-node-3")
	require.NoError(t, err)

	// Wait for clusters to be ready
	err = suite.Compose.WaitForServices(t, 3*time.Minute)
	require.NoError(t, err)

	// Inject SSH keys into all containers
	err = suite.InjectSSHKeys("slurm-controller", "slurm-node-1", "slurm-node-2", "slurm-node-3")
	require.NoError(t, err)

	// Register all SLURM clusters
	clusters, err := suite.RegisterAllSlurmClusters()
	require.NoError(t, err)
	require.Len(t, clusters, 3)

	// Test concurrent experiment submissions
	numExperiments := 20
	var wg sync.WaitGroup
	results := make(chan error, numExperiments)

	startTime := time.Now()

	for i := 0; i < numExperiments; i++ {
		wg.Add(1)
		go func(index int) {
			defer wg.Done()

			// Create experiment
			req := &domain.CreateExperimentRequest{
				Name:            fmt.Sprintf("concurrent-exp-%d", index),
				Description:     fmt.Sprintf("Concurrent experiment %d", index),
				ProjectID:       suite.TestProject.ID,
				CommandTemplate: "echo 'Hello from concurrent experiment' && sleep 2",
				Parameters: []domain.ParameterSet{
					{
						Values: map[string]string{
							"param1": fmt.Sprintf("value%d", index),
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
			if err != nil {
				results <- err
				return
			}

			// Submit to a random cluster
			cluster := clusters[index%len(clusters)]
			err = suite.SubmitToCluster(exp.Experiment, cluster)
			results <- err
		}(i)
	}

	wg.Wait()
	close(results)

	// Check results
	var errors []error
	for err := range results {
		if err != nil {
			errors = append(errors, err)
		}
	}

	duration := time.Since(startTime)
	t.Logf("Submitted %d experiments in %v", numExperiments, duration)
	t.Logf("Throughput: %.2f experiments/second", float64(numExperiments)/duration.Seconds())

	// Allow some failures but not too many
	assert.Less(t, len(errors), numExperiments/4, "Too many submission failures: %d", len(errors))
}

// TestConcurrentExperimentQueries tests concurrent experiment queries
func TestConcurrentExperimentQueries(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Create multiple experiments first
	numExperiments := 50
	for i := 0; i < numExperiments; i++ {
		req := &domain.CreateExperimentRequest{
			Name:            fmt.Sprintf("query-exp-%d", i),
			Description:     fmt.Sprintf("Query experiment %d", i),
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "echo 'Hello World'",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"param1": fmt.Sprintf("value%d", i),
					},
				},
			},
		}

		_, err := suite.OrchestratorSvc.CreateExperiment(context.Background(), req, suite.TestUser.ID)
		require.NoError(t, err)
	}

	// Test concurrent queries
	numQueries := 100
	var wg sync.WaitGroup
	results := make(chan error, numQueries)

	startTime := time.Now()

	for i := 0; i < numQueries; i++ {
		wg.Add(1)
		go func(index int) {
			defer wg.Done()

			// List experiments
			req := &domain.ListExperimentsRequest{
				ProjectID: suite.TestProject.ID,
				OwnerID:   suite.TestUser.ID,
				Limit:     10,
				Offset:    index % 5, // Vary offset to test different queries
			}

			_, err := suite.OrchestratorSvc.ListExperiments(context.Background(), req)
			results <- err
		}(i)
	}

	wg.Wait()
	close(results)

	// Check results
	var errors []error
	for err := range results {
		if err != nil {
			errors = append(errors, err)
		}
	}

	duration := time.Since(startTime)
	t.Logf("Executed %d queries in %v", numQueries, duration)
	t.Logf("Query throughput: %.2f queries/second", float64(numQueries)/duration.Seconds())

	// All queries should succeed
	assert.Empty(t, errors, "Query failures: %v", errors)
}

func TestDatabaseConnectionPooling(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Test concurrent database operations
	numOperations := 100
	var wg sync.WaitGroup
	results := make(chan error, numOperations)

	startTime := time.Now()

	for i := 0; i < numOperations; i++ {
		wg.Add(1)
		go func(index int) {
			defer wg.Done()

			// Create a user (database operation)
			user := &domain.User{
				ID:        fmt.Sprintf("test-user-%d", index),
				Username:  fmt.Sprintf("user%d", index),
				Email:     fmt.Sprintf("user%d@example.com", index),
				CreatedAt: time.Now(),
				UpdatedAt: time.Now(),
			}

			err := suite.DB.Repo.CreateUser(context.Background(), user)
			results <- err
		}(i)
	}

	wg.Wait()
	close(results)

	// Check results
	var errors []error
	for err := range results {
		if err != nil {
			errors = append(errors, err)
		}
	}

	duration := time.Since(startTime)
	t.Logf("Executed %d database operations in %v", numOperations, duration)
	t.Logf("Database throughput: %.2f operations/second", float64(numOperations)/duration.Seconds())

	// All operations should succeed
	assert.Empty(t, errors, "Database operation failures: %v", errors)
}

func TestHighLoadTaskScheduling(t *testing.T) {

	suite := testutil.SetupIntegrationTest(t)
	defer suite.Cleanup()

	// Start all SLURM clusters
	err := suite.Compose.StartServices(t, "slurm-controller", "slurm-node-1", "slurm-node-2", "slurm-node-3")
	require.NoError(t, err)

	// Wait for clusters to be ready
	err = suite.Compose.WaitForServices(t, 3*time.Minute)
	require.NoError(t, err)

	// Inject SSH keys into all containers
	err = suite.InjectSSHKeys("slurm-controller", "slurm-node-1", "slurm-node-2", "slurm-node-3")
	require.NoError(t, err)

	// Register all SLURM clusters
	clusters, err := suite.RegisterAllSlurmClusters()
	require.NoError(t, err)
	require.Len(t, clusters, 3)

	// Create many experiments with multiple tasks each
	numExperiments := 10
	tasksPerExperiment := 5
	totalTasks := numExperiments * tasksPerExperiment

	var experiments []*domain.Experiment
	for i := 0; i < numExperiments; i++ {
		// Create experiment with multiple parameter sets (tasks)
		var parameters []domain.ParameterSet
		for j := 0; j < tasksPerExperiment; j++ {
			parameters = append(parameters, domain.ParameterSet{
				Values: map[string]string{
					"param1": fmt.Sprintf("value%d-%d", i, j),
				},
			})
		}

		req := &domain.CreateExperimentRequest{
			Name:            fmt.Sprintf("load-exp-%d", i),
			Description:     fmt.Sprintf("Load test experiment %d", i),
			ProjectID:       suite.TestProject.ID,
			CommandTemplate: "echo 'Hello from load test' && sleep 1",
			Parameters:      parameters,
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

	// Submit all experiments concurrently
	var wg sync.WaitGroup
	results := make(chan error, numExperiments)

	startTime := time.Now()

	for i, exp := range experiments {
		wg.Add(1)
		go func(index int, experiment *domain.Experiment) {
			defer wg.Done()

			// Submit to a random cluster
			cluster := clusters[index%len(clusters)]
			err := suite.SubmitToCluster(experiment, cluster)
			results <- err
		}(i, exp)
	}

	wg.Wait()
	close(results)

	// Check results
	var errors []error
	for err := range results {
		if err != nil {
			errors = append(errors, err)
		}
	}

	duration := time.Since(startTime)
	t.Logf("Scheduled %d experiments (%d total tasks) in %v", numExperiments, totalTasks, duration)
	t.Logf("Scheduling throughput: %.2f experiments/second", float64(numExperiments)/duration.Seconds())
	t.Logf("Task throughput: %.2f tasks/second", float64(totalTasks)/duration.Seconds())

	// Allow some failures but not too many
	assert.Less(t, len(errors), numExperiments/4, "Too many scheduling failures: %d", len(errors))

	// Wait for some tasks to complete
	time.Sleep(10 * time.Second)

	// Check task distribution across clusters
	clusterTaskCounts := make(map[string]int)
	for _, exp := range experiments {
		tasks, _, err := suite.DB.Repo.ListTasksByExperiment(context.Background(), exp.ID, 100, 0)
		require.NoError(t, err)

		for _, task := range tasks {
			if task.ComputeResourceID != "" {
				clusterTaskCounts[task.ComputeResourceID]++
			}
		}
	}

	t.Logf("Task distribution across clusters: %v", clusterTaskCounts)

	// Verify tasks are distributed across multiple clusters
	assert.Greater(t, len(clusterTaskCounts), 1, "Tasks should be distributed across multiple clusters")
}
