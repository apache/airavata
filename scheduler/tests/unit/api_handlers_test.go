package unit

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/apache/airavata/scheduler/adapters"
	"github.com/apache/airavata/scheduler/core/domain"
	types "github.com/apache/airavata/scheduler/core/util"
	"github.com/apache/airavata/scheduler/tests/testutil"
	"github.com/gorilla/mux"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestAPIHandlers_ListExperiments(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping unit test in short mode")
	}

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	// Create test data
	user := suite.TestUser
	project := suite.TestProject

	// Create a test experiment
	experiment := &domain.Experiment{
		ID:          "test-experiment-1",
		Name:        "Test Experiment 1",
		Description: "Test experiment for API testing",
		ProjectID:   project.ID,
		OwnerID:     user.ID,
		Status:      domain.ExperimentStatusCreated,
	}
	err = suite.DB.Repo.CreateExperiment(context.Background(), experiment)
	require.NoError(t, err)

	// Create handlers
	handlers := adapters.NewHandlers(
		suite.RegistryService,
		suite.DB.Repo,
		suite.VaultService,
		suite.OrchestratorSvc,
		suite.SchedulerService,
		suite.DataMoverSvc,
		nil,                      // worker lifecycle
		nil,                      // analytics
		nil,                      // experiment service
		&adapters.WorkerConfig{}, // config
	)

	// Create router and register routes
	router := mux.NewRouter()
	handlers.RegisterRoutes(router)

	// Test cases
	tests := []struct {
		name           string
		queryParams    string
		expectedStatus int
		expectedCount  int
	}{
		{
			name:           "List all experiments",
			queryParams:    "",
			expectedStatus: http.StatusOK,
			expectedCount:  1,
		},
		{
			name:           "List experiments by project",
			queryParams:    "?projectId=" + project.ID,
			expectedStatus: http.StatusOK,
			expectedCount:  1,
		},
		{
			name:           "List experiments by owner",
			queryParams:    "?ownerId=" + user.ID,
			expectedStatus: http.StatusOK,
			expectedCount:  1,
		},
		{
			name:           "List experiments by status",
			queryParams:    "?status=CREATED",
			expectedStatus: http.StatusOK,
			expectedCount:  1,
		},
		{
			name:           "List experiments with non-existent project",
			queryParams:    "?projectId=non-existent",
			expectedStatus: http.StatusOK,
			expectedCount:  0,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			// Create request
			req, err := http.NewRequest("GET", "/api/v2/experiments"+tt.queryParams, nil)
			require.NoError(t, err)

			// Create response recorder
			rr := httptest.NewRecorder()

			// Serve request
			router.ServeHTTP(rr, req)

			// Check status code
			assert.Equal(t, tt.expectedStatus, rr.Code)

			if tt.expectedStatus == http.StatusOK {
				// Parse response
				var resp domain.ListExperimentsResponse
				err = json.Unmarshal(rr.Body.Bytes(), &resp)
				require.NoError(t, err)

				// Check response
				assert.Len(t, resp.Experiments, tt.expectedCount)
				if tt.expectedCount > 0 {
					assert.Equal(t, experiment.ID, resp.Experiments[0].ID)
					assert.Equal(t, experiment.Name, resp.Experiments[0].Name)
				}
			}
		})
	}
}

func TestAPIHandlers_CreateExperiment(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping unit test in short mode")
	}

	suite := testutil.SetupUnitTest(t)
	defer suite.Cleanup()

	err := suite.StartServices(t, "postgres")
	require.NoError(t, err)

	// Create handlers
	handlers := adapters.NewHandlers(
		suite.RegistryService,
		suite.DB.Repo,
		suite.VaultService,
		suite.OrchestratorSvc,
		suite.SchedulerService,
		suite.DataMoverSvc,
		nil,                      // worker lifecycle
		nil,                      // analytics
		nil,                      // experiment service
		&adapters.WorkerConfig{}, // config
	)

	// Create router and register routes
	router := mux.NewRouter()
	handlers.RegisterRoutes(router)

	// Test cases
	tests := []struct {
		name           string
		requestBody    interface{}
		expectedStatus int
		expectError    bool
	}{
		{
			name: "Create valid experiment",
			requestBody: domain.CreateExperimentRequest{
				Name:            "API Test Experiment",
				Description:     "Test experiment created via API",
				ProjectID:       suite.TestProject.ID,
				CommandTemplate: "echo 'Hello World'",
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
					Walltime: "1:00:00",
				},
			},
			expectedStatus: http.StatusOK,
			expectError:    false,
		},
		{
			name:           "Create experiment with invalid JSON",
			requestBody:    "invalid json",
			expectedStatus: http.StatusBadRequest,
			expectError:    true,
		},
		{
			name: "Create experiment with missing required fields",
			requestBody: domain.CreateExperimentRequest{
				// Missing Name and ProjectID
				Description: "Test experiment",
			},
			expectedStatus: http.StatusInternalServerError,
			expectError:    true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			// Marshal request body
			var body []byte
			var err error
			if str, ok := tt.requestBody.(string); ok {
				body = []byte(str)
			} else {
				body, err = json.Marshal(tt.requestBody)
				require.NoError(t, err)
			}

			// Create request
			req, err := http.NewRequest("POST", "/api/v2/experiments", bytes.NewBuffer(body))
			require.NoError(t, err)
			req.Header.Set("Content-Type", "application/json")

			// Add user context to request using proper ContextKey type
			ctx := context.WithValue(req.Context(), types.UserIDKey, suite.TestUser.ID)
			req = req.WithContext(ctx)

			// Create response recorder
			rr := httptest.NewRecorder()

			// Serve request
			router.ServeHTTP(rr, req)

			// Check status code
			assert.Equal(t, tt.expectedStatus, rr.Code)

			if !tt.expectError && tt.expectedStatus == http.StatusOK {
				// Parse response
				var resp domain.CreateExperimentResponse
				err = json.Unmarshal(rr.Body.Bytes(), &resp)
				require.NoError(t, err)

				// Check response
				assert.NotEmpty(t, resp.Experiment.ID)
				assert.Equal(t, suite.TestProject.ID, resp.Experiment.ProjectID)
				assert.Equal(t, domain.ExperimentStatusCreated, resp.Experiment.Status)

				// Verify experiment was created in database
				createdExp, err := suite.DB.Repo.GetExperimentByID(context.Background(), resp.Experiment.ID)
				require.NoError(t, err)
				assert.NotNil(t, createdExp)
				assert.Equal(t, resp.Experiment.ID, createdExp.ID)
			}
		})
	}
}
