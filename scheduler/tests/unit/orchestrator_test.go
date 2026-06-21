package unit

import (
	"context"
	"fmt"
	"testing"

	"github.com/apache/airavata/scheduler/adapters"
	"github.com/apache/airavata/scheduler/core/domain"
	services "github.com/apache/airavata/scheduler/core/service"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestExperimentOrchestrator_CreateExperiment(t *testing.T) {
	db := testutil.SetupFreshPostgresTestDB(t, "")
	defer db.Cleanup()

	// Create services
	eventPort := adapters.NewInMemoryEventAdapter()
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	stateManager := services.NewStateManager(db.Repo, eventPort)
	orchestratorService := services.NewOrchestratorService(db.Repo, eventPort, securityPort, nil, stateManager)

	// Create test user and project
	builder := testutil.NewTestDataBuilder(db.DB)
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)

	project, err := builder.CreateProject("test-project", "Test Project", user.ID).Build()
	require.NoError(t, err)

	// Create experiment request
	req := &domain.CreateExperimentRequest{
		Name:            "test-experiment",
		Description:     "Test experiment for unit testing",
		ProjectID:       project.ID,
		CommandTemplate: "echo 'Hello World' && sleep 5",
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "value1",
					"param2": "value2",
				},
			},
		},
		Requirements: &domain.ResourceRequirements{
			CPUCores: 1,
			MemoryMB: 1024,
			DiskGB:   1,
			Walltime: "1:00:00",
		},
	}

	// Create experiment
	resp, err := orchestratorService.CreateExperiment(context.Background(), req, user.ID)
	require.NoError(t, err)
	assert.NotNil(t, resp.Experiment)
	assert.Equal(t, "test-experiment", resp.Experiment.Name)
	assert.Equal(t, project.ID, resp.Experiment.ProjectID)
	assert.Equal(t, user.ID, resp.Experiment.OwnerID)
	assert.Equal(t, domain.ExperimentStatusCreated, resp.Experiment.Status)
}

func TestExperimentOrchestrator_MultipleExperiments(t *testing.T) {
	db := testutil.SetupFreshPostgresTestDB(t, "")
	defer db.Cleanup()

	// Create services
	eventPort := adapters.NewInMemoryEventAdapter()
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	stateManager := services.NewStateManager(db.Repo, eventPort)
	orchestratorService := services.NewOrchestratorService(db.Repo, eventPort, securityPort, nil, stateManager)

	// Create test user and project
	builder := testutil.NewTestDataBuilder(db.DB)
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)

	project, err := builder.CreateProject("test-project", "Test Project", user.ID).Build()
	require.NoError(t, err)

	// Create multiple experiments
	for i := 0; i < 3; i++ {
		req := &domain.CreateExperimentRequest{
			Name:            fmt.Sprintf("test-experiment-%d", i),
			Description:     fmt.Sprintf("Test experiment %d", i),
			ProjectID:       project.ID,
			CommandTemplate: "echo 'Hello World'",
			Parameters: []domain.ParameterSet{
				{
					Values: map[string]string{
						"param1": "value1",
					},
				},
			},
		}

		resp, err := orchestratorService.CreateExperiment(context.Background(), req, user.ID)
		require.NoError(t, err)
		assert.NotNil(t, resp.Experiment)
		assert.Equal(t, fmt.Sprintf("test-experiment-%d", i), resp.Experiment.Name)
	}
}

func TestExperimentOrchestrator_InvalidProjectID(t *testing.T) {
	db := testutil.SetupFreshPostgresTestDB(t, "")
	defer db.Cleanup()

	// Create services
	eventPort := adapters.NewInMemoryEventAdapter()
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	stateManager := services.NewStateManager(db.Repo, eventPort)
	orchestratorService := services.NewOrchestratorService(db.Repo, eventPort, securityPort, nil, stateManager)

	// Create test user
	builder := testutil.NewTestDataBuilder(db.DB)
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)

	// Create experiment with invalid project ID
	req := &domain.CreateExperimentRequest{
		Name:            "test-experiment",
		Description:     "Test experiment with invalid project",
		ProjectID:       "invalid-project-id",
		CommandTemplate: "echo 'Hello World'",
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "value1",
				},
			},
		},
	}

	_, err = orchestratorService.CreateExperiment(context.Background(), req, user.ID)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "resource not found")
}

func TestExperimentOrchestrator_DuplicateExperimentName(t *testing.T) {
	db := testutil.SetupFreshPostgresTestDB(t, "")
	defer db.Cleanup()

	// Create services
	eventPort := adapters.NewInMemoryEventAdapter()
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	stateManager := services.NewStateManager(db.Repo, eventPort)
	orchestratorService := services.NewOrchestratorService(db.Repo, eventPort, securityPort, nil, stateManager)

	// Create test user and project
	builder := testutil.NewTestDataBuilder(db.DB)
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	require.NoError(t, err)

	project, err := builder.CreateProject("test-project", "Test Project", user.ID).Build()
	require.NoError(t, err)

	// Create first experiment
	req := &domain.CreateExperimentRequest{
		Name:            "duplicate-experiment",
		Description:     "First experiment",
		ProjectID:       project.ID,
		CommandTemplate: "echo 'Hello World'",
		Parameters: []domain.ParameterSet{
			{
				Values: map[string]string{
					"param1": "value1",
				},
			},
		},
	}

	_, err = orchestratorService.CreateExperiment(context.Background(), req, user.ID)
	require.NoError(t, err)

	// Try to create second experiment with same name
	req.Description = "Second experiment with same name"
	_, err = orchestratorService.CreateExperiment(context.Background(), req, user.ID)
	assert.Error(t, err)
	assert.Contains(t, err.Error(), "duplicate key value violates unique constraint")
}
