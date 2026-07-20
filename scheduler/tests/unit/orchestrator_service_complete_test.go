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

func TestOrchestratorServiceComplete(t *testing.T) {
	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	ctx := context.Background()

	// Create test data
	user := suite.TestUser
	project := suite.TestProject

	t.Run("CreateExperiment", func(t *testing.T) {
		req := &domain.CreateExperimentRequest{
			Name:            "test-experiment",
			Description:     "A test experiment for orchestrator service",
			ProjectID:       project.ID,
			CommandTemplate: "echo 'Hello {name}' > {output_file}",
			OutputPattern:   "output_{name}.txt",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"name":        "world",
						"output_file": "output_world.txt",
					},
				},
				{
					Values: map[string]string{
						"name":        "universe",
						"output_file": "output_universe.txt",
					},
				},
			},
			Requirements: &domain.ResourceRequirements{
				CPUCores: 2,
				MemoryMB: 1024,
				DiskGB:   10,
				GPUs:     0,
				Walltime: "1:00:00",
				Priority: 5,
			},
			Constraints: &domain.ExperimentConstraints{
				MaxCost:            100.0,
				Deadline:           time.Now().Add(24 * time.Hour),
				PreferredResources: []string{"slurm-cluster-1"},
				ExcludedResources:  []string{"test-cluster"},
			},
			Metadata: map[string]interface{}{
				"category":    "test",
				"environment": "development",
				"tags":        []string{"test", "orchestrator"},
			},
		}

		resp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, user.ID)
		require.NoError(t, err)
		require.True(t, resp.Success)
		assert.Equal(t, "experiment created successfully", resp.Message)
		assert.NotNil(t, resp.Experiment)
		assert.Equal(t, req.Name, resp.Experiment.Name)
		assert.Equal(t, req.Description, resp.Experiment.Description)
		assert.Equal(t, req.ProjectID, resp.Experiment.ProjectID)
		assert.Equal(t, user.ID, resp.Experiment.OwnerID)
		assert.Equal(t, domain.ExperimentStatusCreated, resp.Experiment.Status)
		assert.Equal(t, req.CommandTemplate, resp.Experiment.CommandTemplate)
		assert.Equal(t, req.OutputPattern, resp.Experiment.OutputPattern)
		assert.Equal(t, req.Parameters, resp.Experiment.Parameters)
		assert.Equal(t, req.Requirements, resp.Experiment.Requirements)
		assert.Equal(t, req.Constraints, resp.Experiment.Constraints)
		assert.Equal(t, req.Metadata, resp.Experiment.Metadata)
		assert.False(t, resp.Experiment.CreatedAt.IsZero())
		assert.False(t, resp.Experiment.UpdatedAt.IsZero())
	})

	t.Run("GetExperiment", func(t *testing.T) {
		// First create an experiment
		req := &domain.CreateExperimentRequest{
			Name:            "get-test-experiment",
			Description:     "Experiment for testing GetExperiment",
			ProjectID:       project.ID,
			CommandTemplate: "python script.py {param1} {param2}",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"param1": "value1",
						"param2": "value2",
					},
				},
			},
		}

		createResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, user.ID)
		require.NoError(t, err)
		require.True(t, createResp.Success)

		// Test GetExperiment without tasks
		getReq := &domain.GetExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
			IncludeTasks: false,
		}

		getResp, err := suite.OrchestratorSvc.GetExperiment(ctx, getReq)
		require.NoError(t, err)
		assert.True(t, getResp.Success)
		assert.NotNil(t, getResp.Experiment)
		assert.Equal(t, createResp.Experiment.ID, getResp.Experiment.ID)
		assert.Equal(t, req.Name, getResp.Experiment.Name)
		assert.Nil(t, getResp.Tasks) // Should be nil when IncludeTasks is false

		// Test GetExperiment with tasks
		getReqWithTasks := &domain.GetExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
			IncludeTasks: true,
		}

		getRespWithTasks, err := suite.OrchestratorSvc.GetExperiment(ctx, getReqWithTasks)
		require.NoError(t, err)
		assert.True(t, getRespWithTasks.Success)
		assert.NotNil(t, getRespWithTasks.Experiment)
		assert.NotNil(t, getRespWithTasks.Tasks)        // Should not be nil when IncludeTasks is true
		assert.Equal(t, 0, len(getRespWithTasks.Tasks)) // No tasks generated yet

		// Test GetExperiment with non-existent ID
		nonExistentReq := &domain.GetExperimentRequest{
			ExperimentID: "non-existent-experiment",
			IncludeTasks: false,
		}

		nonExistentResp, err := suite.OrchestratorSvc.GetExperiment(ctx, nonExistentReq)
		require.Error(t, err)
		assert.False(t, nonExistentResp.Success)
		assert.Contains(t, nonExistentResp.Message, "experiment not found")
	})

	t.Run("ListExperiments", func(t *testing.T) {
		// Create multiple experiments for testing
		experiments := []*domain.CreateExperimentRequest{
			{
				Name:            "list-test-1",
				Description:     "First list test experiment",
				ProjectID:       project.ID,
				CommandTemplate: "echo test1",
				Parameters: []domain.ParameterSet{
					{Values: map[string]string{"param": "value1"}},
				},
			},
			{
				Name:            "list-test-2",
				Description:     "Second list test experiment",
				ProjectID:       project.ID,
				CommandTemplate: "echo test2",
				Parameters: []domain.ParameterSet{
					{Values: map[string]string{"param": "value2"}},
				},
			},
			{
				Name:            "list-test-3",
				Description:     "Third list test experiment",
				ProjectID:       project.ID,
				CommandTemplate: "echo test3",
				Parameters: []domain.ParameterSet{
					{Values: map[string]string{"param": "value3"}},
				},
			},
		}

		var createdExperimentIDs []string
		for _, expReq := range experiments {
			resp, err := suite.OrchestratorSvc.CreateExperiment(ctx, expReq, user.ID)
			require.NoError(t, err)
			require.True(t, resp.Success)
			createdExperimentIDs = append(createdExperimentIDs, resp.Experiment.ID)
		}

		// Test listing all experiments
		listReq := &domain.ListExperimentsRequest{
			Limit:  10,
			Offset: 0,
		}

		listResp, err := suite.OrchestratorSvc.ListExperiments(ctx, listReq)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, listResp.Total, 3) // At least the 3 we just created
		assert.GreaterOrEqual(t, len(listResp.Experiments), 3)
		assert.Equal(t, 10, listResp.Limit)
		assert.Equal(t, 0, listResp.Offset)

		// Verify our created experiments are in the list
		foundIDs := make(map[string]bool)
		for _, exp := range listResp.Experiments {
			foundIDs[exp.ID] = true
		}
		for _, id := range createdExperimentIDs {
			assert.True(t, foundIDs[id], "Created experiment %s should be in the list", id)
		}

		// Test filtering by project
		projectListReq := &domain.ListExperimentsRequest{
			ProjectID: project.ID,
			Limit:     10,
			Offset:    0,
		}

		projectListResp, err := suite.OrchestratorSvc.ListExperiments(ctx, projectListReq)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, projectListResp.Total, 3)
		for _, exp := range projectListResp.Experiments {
			assert.Equal(t, project.ID, exp.ProjectID)
		}

		// Test filtering by owner
		ownerListReq := &domain.ListExperimentsRequest{
			OwnerID: user.ID,
			Limit:   10,
			Offset:  0,
		}

		ownerListResp, err := suite.OrchestratorSvc.ListExperiments(ctx, ownerListReq)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, ownerListResp.Total, 3)
		for _, exp := range ownerListResp.Experiments {
			assert.Equal(t, user.ID, exp.OwnerID)
		}

		// Test filtering by status
		statusListReq := &domain.ListExperimentsRequest{
			Status: string(domain.ExperimentStatusCreated),
			Limit:  10,
			Offset: 0,
		}

		statusListResp, err := suite.OrchestratorSvc.ListExperiments(ctx, statusListReq)
		require.NoError(t, err)
		assert.GreaterOrEqual(t, statusListResp.Total, 3)
		for _, exp := range statusListResp.Experiments {
			assert.Equal(t, domain.ExperimentStatusCreated, exp.Status)
		}

		// Test pagination
		paginationReq := &domain.ListExperimentsRequest{
			Limit:  2,
			Offset: 0,
		}

		paginationResp, err := suite.OrchestratorSvc.ListExperiments(ctx, paginationReq)
		require.NoError(t, err)
		assert.Equal(t, 2, len(paginationResp.Experiments))
		assert.Equal(t, 2, paginationResp.Limit)
		assert.Equal(t, 0, paginationResp.Offset)
	})

	t.Run("UpdateExperiment", func(t *testing.T) {
		// First create an experiment
		req := &domain.CreateExperimentRequest{
			Name:            "update-test-experiment",
			Description:     "Original description",
			ProjectID:       project.ID,
			CommandTemplate: "echo original",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{"param": "original"},
				},
			},
		}

		createResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, user.ID)
		require.NoError(t, err)
		require.True(t, createResp.Success)

		// Update the experiment
		newDescription := "Updated description"
		updateReq := &domain.UpdateExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
			Description:  &newDescription,
			Constraints: &domain.ExperimentConstraints{
				MaxCost:            200.0,
				Deadline:           time.Now().Add(48 * time.Hour),
				PreferredResources: []string{"updated-cluster"},
			},
			Metadata: map[string]interface{}{
				"updated":  true,
				"version":  "2.0",
				"category": "updated-test",
			},
		}

		updateResp, err := suite.OrchestratorSvc.UpdateExperiment(ctx, updateReq)
		require.NoError(t, err)
		assert.True(t, updateResp.Success)
		assert.Equal(t, "experiment updated successfully", updateResp.Message)
		assert.NotNil(t, updateResp.Experiment)
		assert.Equal(t, newDescription, updateResp.Experiment.Description)
		assert.Equal(t, 200.0, updateResp.Experiment.Constraints.MaxCost)
		assert.Equal(t, []string{"updated-cluster"}, updateResp.Experiment.Constraints.PreferredResources)
		assert.Equal(t, true, updateResp.Experiment.Metadata["updated"])
		assert.Equal(t, "2.0", updateResp.Experiment.Metadata["version"])

		// Test updating non-existent experiment
		nonExistentUpdateReq := &domain.UpdateExperimentRequest{
			ExperimentID: "non-existent-experiment",
			Description:  &newDescription,
		}

		nonExistentUpdateResp, err := suite.OrchestratorSvc.UpdateExperiment(ctx, nonExistentUpdateReq)
		require.Error(t, err)
		assert.False(t, nonExistentUpdateResp.Success)
		assert.Contains(t, nonExistentUpdateResp.Message, "experiment not found")
	})

	t.Run("SubmitExperiment", func(t *testing.T) {
		// Register a compute resource for testing
		computeResourceReq := &domain.CreateComputeResourceRequest{
			Name:        "test-cluster",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "localhost:6817",
			MaxWorkers:  10,
			CostPerHour: 1.0,
			OwnerID:     user.ID,
			Capabilities: map[string]interface{}{
				"cpu_cores": 8,
				"memory_gb": 32,
			},
		}
		_, err := suite.RegistryService.RegisterComputeResource(ctx, computeResourceReq)
		require.NoError(t, err)

		// First create an experiment
		req := &domain.CreateExperimentRequest{
			Name:            uniqueID("submit-test-experiment"),
			Description:     "Experiment for testing SubmitExperiment",
			ProjectID:       project.ID,
			CommandTemplate: "echo 'Hello {name}' > {output_file}",
			OutputPattern:   "output_{name}.txt",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"name":        "world",
						"output_file": "output_world.txt",
					},
				},
				{
					Values: map[string]string{
						"name":        "universe",
						"output_file": "output_universe.txt",
					},
				},
			},
		}

		createResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, user.ID)
		require.NoError(t, err)
		require.True(t, createResp.Success)

		// Submit the experiment
		submitReq := &domain.SubmitExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
			Priority:     7,
			DryRun:       false,
		}

		submitResp, err := suite.OrchestratorSvc.SubmitExperiment(ctx, submitReq)
		require.NoError(t, err)
		assert.True(t, submitResp.Success)
		assert.Equal(t, "experiment submitted successfully", submitResp.Message)
		assert.NotNil(t, submitResp.Experiment)
		assert.Equal(t, domain.ExperimentStatusExecuting, submitResp.Experiment.Status)
		assert.NotNil(t, submitResp.Tasks)
		assert.Equal(t, 2, len(submitResp.Tasks)) // Should have 2 tasks for 2 parameter sets

		// Verify tasks were created correctly
		for _, task := range submitResp.Tasks {
			assert.Equal(t, createResp.Experiment.ID, task.ExperimentID)
			assert.Equal(t, domain.TaskStatusCreated, task.Status)
			assert.Contains(t, task.Command, "Hello")
			assert.Equal(t, 0, task.RetryCount)
			assert.Equal(t, 3, task.MaxRetries) // Default max retries
			assert.NotNil(t, task.Metadata)
		}

		// Test submitting non-existent experiment
		nonExistentSubmitReq := &domain.SubmitExperimentRequest{
			ExperimentID: "non-existent-experiment",
		}

		nonExistentSubmitResp, err := suite.OrchestratorSvc.SubmitExperiment(ctx, nonExistentSubmitReq)
		require.Error(t, err)
		assert.False(t, nonExistentSubmitResp.Success)
		assert.Contains(t, nonExistentSubmitResp.Message, "experiment not found")

		// Test submitting already submitted experiment
		alreadySubmittedReq := &domain.SubmitExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
		}

		alreadySubmittedResp, err := suite.OrchestratorSvc.SubmitExperiment(ctx, alreadySubmittedReq)
		require.Error(t, err)
		assert.False(t, alreadySubmittedResp.Success)
		assert.Contains(t, alreadySubmittedResp.Message, "experiment cannot be submitted in current state")
	})

	t.Run("GenerateTasks", func(t *testing.T) {
		// First create an experiment
		req := &domain.CreateExperimentRequest{
			Name:            "generate-tasks-experiment",
			Description:     "Experiment for testing GenerateTasks",
			ProjectID:       project.ID,
			CommandTemplate: "python script.py --input {input_file} --output {output_file} --param {param}",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"input_file":  "input1.txt",
						"output_file": "output1.txt",
						"param":       "value1",
					},
				},
				{
					Values: map[string]string{
						"input_file":  "input2.txt",
						"output_file": "output2.txt",
						"param":       "value2",
					},
				},
				{
					Values: map[string]string{
						"input_file":  "input3.txt",
						"output_file": "output3.txt",
						"param":       "value3",
					},
				},
			},
		}

		createResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, user.ID)
		require.NoError(t, err)
		require.True(t, createResp.Success)

		// Generate tasks
		tasks, err := suite.OrchestratorSvc.GenerateTasks(ctx, createResp.Experiment.ID)
		require.NoError(t, err)
		assert.Equal(t, 3, len(tasks)) // Should have 3 tasks for 3 parameter sets

		// Verify each task
		for i, task := range tasks {
			assert.Equal(t, createResp.Experiment.ID, task.ExperimentID)
			assert.Equal(t, domain.TaskStatusCreated, task.Status)
			assert.Contains(t, task.Command, "python script.py")
			assert.Contains(t, task.Command, "input")
			assert.Contains(t, task.Command, "output")
			assert.Equal(t, 0, task.RetryCount)
			assert.Equal(t, 3, task.MaxRetries)
			assert.NotNil(t, task.Metadata)
			assert.False(t, task.CreatedAt.IsZero())
			assert.False(t, task.UpdatedAt.IsZero())

			// Verify parameter substitution
			expectedParam := req.Parameters[i].Values["param"]
			assert.Contains(t, task.Command, expectedParam)
		}

		// Test generating tasks for non-existent experiment
		_, err = suite.OrchestratorSvc.GenerateTasks(ctx, "non-existent-experiment")
		require.Error(t, err)
		assert.Contains(t, err.Error(), "experiment not found")
	})

	t.Run("ValidateExperiment", func(t *testing.T) {
		// Test with valid experiment
		validReq := &domain.CreateExperimentRequest{
			Name:            "valid-experiment",
			Description:     "A valid experiment",
			ProjectID:       project.ID,
			CommandTemplate: "echo 'Hello {name}'",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"name": "world",
					},
				},
			},
			Requirements: &domain.ResourceRequirements{
				CPUCores: 2,
				MemoryMB: 1024,
				DiskGB:   10,
				GPUs:     0,
				Walltime: "1:00:00",
				Priority: 5,
			},
			Constraints: &domain.ExperimentConstraints{
				MaxCost: 100.0,
			},
		}

		validCreateResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, validReq, user.ID)
		require.NoError(t, err)
		require.True(t, validCreateResp.Success)

		validResult, err := suite.OrchestratorSvc.ValidateExperiment(ctx, validCreateResp.Experiment.ID)
		require.NoError(t, err)
		assert.True(t, validResult.Valid)
		assert.Empty(t, validResult.Errors)
		assert.Empty(t, validResult.Warnings)

		// Test with invalid experiment (missing name)
		invalidReq := &domain.CreateExperimentRequest{
			Name:            "", // Invalid: empty name
			Description:     "Invalid experiment",
			ProjectID:       project.ID,
			CommandTemplate: "",                      // Invalid: empty command template
			Parameters:      []domain.ParameterSet{}, // Invalid: no parameters
		}

		invalidCreateResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, invalidReq, user.ID)
		// This should fail during creation due to validation
		require.Error(t, err)
		assert.False(t, invalidCreateResp.Success)

		// Test validation of non-existent experiment
		_, err = suite.OrchestratorSvc.ValidateExperiment(ctx, "non-existent-experiment")
		require.Error(t, err)
		assert.Contains(t, err.Error(), "resource not found")
	})

	t.Run("DeleteExperiment", func(t *testing.T) {
		// First create an experiment
		req := &domain.CreateExperimentRequest{
			Name:            "delete-test-experiment",
			Description:     "Experiment for testing DeleteExperiment",
			ProjectID:       project.ID,
			CommandTemplate: "echo delete test",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{"param": "delete"},
				},
			},
		}

		createResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, user.ID)
		require.NoError(t, err)
		require.True(t, createResp.Success)

		// Delete the experiment
		deleteReq := &domain.DeleteExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
			Force:        false,
		}

		deleteResp, err := suite.OrchestratorSvc.DeleteExperiment(ctx, deleteReq)
		require.NoError(t, err)
		assert.True(t, deleteResp.Success)
		assert.Equal(t, "experiment deleted successfully", deleteResp.Message)

		// Verify experiment is deleted
		getReq := &domain.GetExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
			IncludeTasks: false,
		}

		_, err = suite.OrchestratorSvc.GetExperiment(ctx, getReq)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "experiment not found")

		// Test deleting non-existent experiment
		nonExistentDeleteReq := &domain.DeleteExperimentRequest{
			ExperimentID: "non-existent-experiment",
			Force:        false,
		}

		nonExistentDeleteResp, err := suite.OrchestratorSvc.DeleteExperiment(ctx, nonExistentDeleteReq)
		require.Error(t, err)
		assert.False(t, nonExistentDeleteResp.Success)
		assert.Contains(t, nonExistentDeleteResp.Message, "experiment not found")
	})

	t.Run("ExperimentLifecycle", func(t *testing.T) {
		// Register a compute resource for testing
		computeResourceReq := &domain.CreateComputeResourceRequest{
			Name:        "test-cluster-lifecycle",
			Type:        domain.ComputeResourceTypeSlurm,
			Endpoint:    "localhost:6817",
			MaxWorkers:  10,
			CostPerHour: 1.0,
			OwnerID:     user.ID,
			Capabilities: map[string]interface{}{
				"cpu_cores": 8,
				"memory_gb": 32,
			},
		}
		_, err := suite.RegistryService.RegisterComputeResource(ctx, computeResourceReq)
		require.NoError(t, err)

		// Test complete experiment lifecycle: Create -> Submit -> Update -> Delete
		req := &domain.CreateExperimentRequest{
			Name:            "lifecycle-experiment",
			Description:     "Complete lifecycle test",
			ProjectID:       project.ID,
			CommandTemplate: "echo 'Lifecycle test {iteration}'",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"iteration": "1",
					},
				},
				{
					Values: map[string]string{
						"iteration": "2",
					},
				},
			},
		}

		// 1. Create experiment
		createResp, err := suite.OrchestratorSvc.CreateExperiment(ctx, req, user.ID)
		require.NoError(t, err)
		require.True(t, createResp.Success)
		assert.Equal(t, domain.ExperimentStatusCreated, createResp.Experiment.Status)

		// 2. Validate experiment
		validationResult, err := suite.OrchestratorSvc.ValidateExperiment(ctx, createResp.Experiment.ID)
		require.NoError(t, err)
		assert.True(t, validationResult.Valid)

		// 3. Update experiment
		newDescription := "Updated lifecycle description"
		updateReq := &domain.UpdateExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
			Description:  &newDescription,
		}

		updateResp, err := suite.OrchestratorSvc.UpdateExperiment(ctx, updateReq)
		require.NoError(t, err)
		assert.True(t, updateResp.Success)
		assert.Equal(t, newDescription, updateResp.Experiment.Description)

		// 4. Submit experiment
		submitReq := &domain.SubmitExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
		}

		submitResp, err := suite.OrchestratorSvc.SubmitExperiment(ctx, submitReq)
		require.NoError(t, err)
		assert.True(t, submitResp.Success)
		assert.Equal(t, domain.ExperimentStatusExecuting, submitResp.Experiment.Status)
		assert.Equal(t, 2, len(submitResp.Tasks))

		// 5. Verify experiment state after submission
		getReq := &domain.GetExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
			IncludeTasks: true,
		}

		getResp, err := suite.OrchestratorSvc.GetExperiment(ctx, getReq)
		require.NoError(t, err)
		assert.True(t, getResp.Success)
		assert.Equal(t, domain.ExperimentStatusExecuting, getResp.Experiment.Status)
		assert.Equal(t, 2, len(getResp.Tasks))

		// 6. Delete experiment (force delete since it's submitted)
		deleteReq := &domain.DeleteExperimentRequest{
			ExperimentID: createResp.Experiment.ID,
			Force:        true,
		}

		deleteResp, err := suite.OrchestratorSvc.DeleteExperiment(ctx, deleteReq)
		require.NoError(t, err)
		assert.True(t, deleteResp.Success)

		// 7. Verify experiment is deleted
		_, err = suite.OrchestratorSvc.GetExperiment(ctx, getReq)
		require.Error(t, err)
		assert.Contains(t, err.Error(), "experiment not found")
	})
}
