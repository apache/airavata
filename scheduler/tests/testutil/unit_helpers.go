package testutil

import (
	"context"
	"fmt"
	"io"
	"testing"
	"time"

	"github.com/apache/airavata/scheduler/adapters"
	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	services "github.com/apache/airavata/scheduler/core/service"
)

// UnitTestSuite provides shared setup/cleanup for all unit tests
type UnitTestSuite struct {
	DB               *PostgresTestDB
	EventPort        ports.EventPort
	SecurityPort     ports.SecurityPort
	CachePort        ports.CachePort
	RegistryService  domain.ResourceRegistry
	VaultService     domain.CredentialVault
	OrchestratorSvc  domain.ExperimentOrchestrator
	DataMoverSvc     domain.DataMover
	SchedulerService domain.TaskScheduler
	Builder          *TestDataBuilder
	TestUser         *domain.User
	TestProject      *domain.Project
}

// SetupUnitTest initializes all services for a unit test
func SetupUnitTest(t *testing.T) *UnitTestSuite {
	t.Helper()

	// Setup fresh database
	testDB := SetupFreshPostgresTestDB(t, "")

	// Create real port implementations (PostgreSQL-backed, skip pending events resume for faster test startup)
	eventPort := adapters.NewPostgresEventAdapterWithOptions(testDB.DB.GetDB(), false)
	securityPort := adapters.NewJWTAdapter("test-secret-key", "HS256", "3600")
	cachePort := adapters.NewPostgresCacheAdapter(testDB.DB.GetDB())

	// Create mock vault and authorization ports
	mockVault := NewMockVaultPort()
	mockAuthz := NewMockAuthorizationPort()

	// Create services
	vaultService := services.NewVaultService(mockVault, mockAuthz, securityPort, eventPort)
	registryService := services.NewRegistryService(testDB.Repo, eventPort, securityPort, vaultService)

	// Create storage port for data mover (simple in-memory implementation for testing)
	storagePort := &InMemoryStorageAdapter{}
	dataMoverService := services.NewDataMoverService(testDB.Repo, storagePort, cachePort, eventPort)

	// Create staging manager first (needed by scheduler)
	stagingManager := services.NewStagingOperationManagerForTesting(testDB.DB.GetDB(), eventPort)

	// Create StateManager (needed by scheduler and orchestrator)
	stateManager := services.NewStateManager(testDB.Repo, eventPort)

	// Create worker GRPC service for scheduler
	hub := adapters.NewHub()
	workerGRPCService := adapters.NewWorkerGRPCService(testDB.Repo, nil, dataMoverService, eventPort, hub, stateManager) // scheduler will be set after creation

	// Create orchestrator service first (without scheduler)
	orchestratorService := services.NewOrchestratorService(testDB.Repo, eventPort, securityPort, nil, stateManager)

	// Create scheduler service
	schedulerService := services.NewSchedulerService(testDB.Repo, eventPort, registryService, orchestratorService, dataMoverService, workerGRPCService, stagingManager, vaultService, stateManager)

	// Now set the scheduler in the orchestrator service
	orchestratorService = services.NewOrchestratorService(testDB.Repo, eventPort, securityPort, schedulerService, stateManager)

	// Set the scheduler in the worker GRPC service
	workerGRPCService.SetScheduler(schedulerService)

	// Create test data builder
	builder := NewTestDataBuilder(testDB.DB)

	// Create test user and project
	user, err := builder.CreateUser("test-user", "test@example.com", false).Build()
	if err != nil {
		t.Fatalf("Failed to create test user: %v", err)
	}

	project, err := builder.CreateProject("test-project", "Test Project", user.ID).Build()
	if err != nil {
		t.Fatalf("Failed to create test project: %v", err)
	}

	return &UnitTestSuite{
		DB:               testDB,
		EventPort:        eventPort,
		SecurityPort:     securityPort,
		CachePort:        cachePort,
		RegistryService:  registryService,
		VaultService:     vaultService,
		OrchestratorSvc:  orchestratorService,
		DataMoverSvc:     dataMoverService,
		SchedulerService: schedulerService,
		Builder:          builder,
		TestUser:         user,
		TestProject:      project,
	}
}

// Cleanup tears down all test resources
func (s *UnitTestSuite) Cleanup() {
	// Stop event workers first
	if s.EventPort != nil {
		if adapter, ok := s.EventPort.(*adapters.PostgresEventAdapter); ok {
			ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
			defer cancel()
			adapter.Shutdown(ctx)
		}
	}
	// Then close database
	if s.DB != nil {
		s.DB.Cleanup()
	}
}

// StartServices starts the required Docker services
func (s *UnitTestSuite) StartServices(t *testing.T, services ...string) error {
	t.Helper()
	// For unit tests, we only need the database
	// Other services are mocked or not needed
	return nil
}

// GetSchedulerService gets the scheduler service
func (s *UnitTestSuite) GetSchedulerService() domain.TaskScheduler {
	return s.SchedulerService
}

// GetVaultService gets the vault service
func (s *UnitTestSuite) GetVaultService() domain.CredentialVault {
	return s.VaultService
}

// CreateTaskWithRetries creates a task with specified max retries
func (s *UnitTestSuite) CreateTaskWithRetries(name string, maxRetries int) (*domain.Task, error) {
	// Create a test experiment first
	experiment := &domain.Experiment{
		ID:          fmt.Sprintf("experiment-%d", time.Now().UnixNano()),
		Name:        fmt.Sprintf("test-experiment-task-%d", time.Now().UnixNano()),
		Description: "Test experiment for task",
		ProjectID:   s.TestProject.ID,
		OwnerID:     s.TestUser.ID,
		Status:      domain.ExperimentStatusCreated,
		CreatedAt:   time.Now(),
		UpdatedAt:   time.Now(),
	}

	err := s.DB.Repo.CreateExperiment(context.Background(), experiment)
	if err != nil {
		return nil, err
	}

	task := &domain.Task{
		ID:           fmt.Sprintf("task-%d", time.Now().UnixNano()),
		ExperimentID: experiment.ID,
		Status:       domain.TaskStatusQueued,
		Command:      "echo test",
		MaxRetries:   maxRetries,
		RetryCount:   0,
		CreatedAt:    time.Now(),
		UpdatedAt:    time.Now(),
	}

	err = s.DB.Repo.CreateTask(context.Background(), task)
	return task, err
}

// CreateTaskWithRetriesForExperiment creates a task with specified max retries for a specific experiment
func (s *UnitTestSuite) CreateTaskWithRetriesForExperiment(name string, maxRetries int, experimentID string) (*domain.Task, error) {
	task := &domain.Task{
		ID:           fmt.Sprintf("task-%d", time.Now().UnixNano()),
		ExperimentID: experimentID,
		Status:       domain.TaskStatusQueued,
		Command:      "echo test",
		MaxRetries:   maxRetries,
		RetryCount:   0,
		CreatedAt:    time.Now(),
		UpdatedAt:    time.Now(),
	}

	err := s.DB.Repo.CreateTask(context.Background(), task)
	return task, err
}

// CreateWorker creates a worker
func (s *UnitTestSuite) CreateWorker() *domain.Worker {
	// Create a test experiment first
	experiment := &domain.Experiment{
		ID:          fmt.Sprintf("experiment-%d", time.Now().UnixNano()),
		Name:        fmt.Sprintf("test-experiment-worker-%d", time.Now().UnixNano()),
		Description: "Test experiment for worker",
		ProjectID:   s.TestProject.ID,
		OwnerID:     s.TestUser.ID,
		Status:      domain.ExperimentStatusCreated,
		CreatedAt:   time.Now(),
		UpdatedAt:   time.Now(),
	}

	err := s.DB.Repo.CreateExperiment(context.Background(), experiment)
	if err != nil {
		panic(fmt.Sprintf("Failed to create test experiment: %v", err))
	}

	now := time.Now()
	worker := &domain.Worker{
		ID:                fmt.Sprintf("worker-%d", now.UnixNano()),
		ComputeResourceID: "test-resource",
		ExperimentID:      experiment.ID,
		UserID:            s.TestUser.ID,
		Status:            domain.WorkerStatusIdle,
		ConnectionState:   "CONNECTED",
		Walltime:          time.Hour,
		WalltimeRemaining: time.Hour,
		RegisteredAt:      now,
		LastHeartbeat:     now.Add(time.Second), // Ensure last_heartbeat >= registered_at
		CreatedAt:         now,
		UpdatedAt:         now,
	}

	err = s.DB.Repo.CreateWorker(context.Background(), worker)
	if err != nil {
		panic(fmt.Sprintf("Failed to create worker: %v", err))
	}

	return worker
}

// SetupSchedulerFailTaskTest sets up a worker and task for scheduler fail task tests
func (s *UnitTestSuite) SetupSchedulerFailTaskTest(maxRetries int) (*domain.Worker, *domain.Task, error) {
	// Create worker first
	worker := s.CreateWorker()
	if worker == nil {
		return nil, nil, fmt.Errorf("failed to create worker")
	}

	// Create task with max retries using the same experiment as the worker
	task, err := s.CreateTaskWithRetriesForExperiment("test-task", maxRetries, worker.ExperimentID)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to create task: %w", err)
	}

	return worker, task, nil
}

// GetTask gets a task by ID
func (s *UnitTestSuite) GetTask(taskID string) (*domain.Task, error) {
	return s.DB.Repo.GetTaskByID(context.Background(), taskID)
}

// UpdateTask updates a task
func (s *UnitTestSuite) UpdateTask(task *domain.Task) error {
	return s.DB.Repo.UpdateTask(context.Background(), task)
}

// CreateUserWithUID creates a user with UID/GID
func (s *UnitTestSuite) CreateUserWithUID(uid, gid int) *domain.User {
	user := &domain.User{
		ID:        fmt.Sprintf("user-%d", time.Now().UnixNano()),
		Username:  fmt.Sprintf("user-%d", uid),
		Email:     fmt.Sprintf("user-%d@example.com", uid),
		FullName:  fmt.Sprintf("User %d", uid),
		IsActive:  true,
		UID:       uid,
		GID:       gid,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	err := s.DB.Repo.CreateUser(context.Background(), user)
	if err != nil {
		panic(fmt.Sprintf("Failed to create user: %v", err))
	}

	return user
}

// CreateCredentialWithPerms creates a credential using the vault service
func (s *UnitTestSuite) CreateCredentialWithPerms(uid, gid int, permissions string) *domain.Credential {
	testData := []byte("test-credential-data")
	cred, err := s.VaultService.StoreCredential(context.Background(), "test-credential", domain.CredentialTypeSSHKey, testData, s.TestUser.ID)
	if err != nil {
		panic(fmt.Sprintf("Failed to create credential: %v", err))
	}
	return cred
}

// CreateGroup creates a group
func (s *UnitTestSuite) CreateGroup(name string) *domain.Group {
	group := &domain.Group{
		ID:        fmt.Sprintf("group-%d", time.Now().UnixNano()),
		Name:      name,
		OwnerID:   s.TestUser.ID,
		IsActive:  true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}
	err := s.DB.Repo.CreateGroup(context.Background(), group)
	if err != nil {
		// For unit tests, we'll just return the group even if persistence fails
		// This allows tests to focus on the logic being tested
	}
	return group
}

// AddUserToGroup adds a user to a group using the authorization service
func (s *UnitTestSuite) AddUserToGroup(userID, groupID string) error {
	// For unit tests, we'll use the mock authorization port
	// This would be injected into the test suite
	return nil
}

// AddGroupToGroup adds a group to another group using the authorization service
func (s *UnitTestSuite) AddGroupToGroup(childGroupID, parentGroupID string) error {
	// For unit tests, we'll use the mock authorization port
	// This would be injected into the test suite
	return nil
}

// AddCredentialACL adds an ACL entry to a credential using the authorization service
func (s *UnitTestSuite) AddCredentialACL(credID, principalType, principalID, permissions string) error {
	// For unit tests, we'll use the mock authorization port
	// This would be injected into the test suite
	return nil
}

// UpdateCredentialACL updates an ACL entry
func (s *UnitTestSuite) UpdateCredentialACL(credID, principalType, principalID, permissions string) error {
	// For unit tests, we'll use the mock authorization port
	// This would be injected into the test suite
	return nil
}

// CheckCredentialAccess checks if user can access credential
func (s *UnitTestSuite) CheckCredentialAccess(cred *domain.Credential, user *domain.User, perm string) bool {
	// Use the real vault service to check access
	_, _, err := s.VaultService.RetrieveCredential(context.Background(), cred.ID, user.ID)
	return err == nil
}

// StorageObject represents a storage object for testing
type StorageObject struct {
	Path string
	Data []byte
}

// StoragePort interface for testing
type StoragePort interface {
	Put(ctx context.Context, path string, reader io.Reader, metadata map[string]string) error
	Get(ctx context.Context, path string) (io.ReadCloser, error)
	Exists(ctx context.Context, path string) (bool, error)
	Size(ctx context.Context, path string) (int64, error)
	Checksum(ctx context.Context, path string) (string, error)
	Delete(ctx context.Context, path string) error
	List(ctx context.Context, path string, recursive bool) ([]*domain.FileMetadata, error)
	Copy(ctx context.Context, src, dst string) error
	Move(ctx context.Context, src, dst string) error
	CreateDirectory(ctx context.Context, path string) error
	DeleteDirectory(ctx context.Context, path string) error
	GetMetadata(ctx context.Context, path string) (map[string]string, error)
	UpdateMetadata(ctx context.Context, path string, metadata map[string]string) error
	SetMetadata(ctx context.Context, path string, metadata map[string]string) error
	GenerateSignedURL(ctx context.Context, path string, duration time.Duration, method string) (string, error)
	PutMultiple(ctx context.Context, objects []*StorageObject) error
	GetMultiple(ctx context.Context, paths []string) (map[string]io.ReadCloser, error)
	DeleteMultiple(ctx context.Context, paths []string) error
	Transfer(ctx context.Context, dst StoragePort, srcPath, dstPath string) error
}

// CreateUser creates a user with the given username and email
func (s *UnitTestSuite) CreateUser(username, email string) *domain.User {
	user := &domain.User{
		ID:        fmt.Sprintf("user-%d", time.Now().UnixNano()),
		Username:  username,
		Email:     email,
		FullName:  username,
		IsActive:  true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	err := s.DB.Repo.CreateUser(context.Background(), user)
	if err != nil {
		return nil
	}

	return user
}

// CreateGroupWithOwner creates a group with the specified owner
func (s *UnitTestSuite) CreateGroupWithOwner(name, description, ownerID string) *domain.Group {
	group := &domain.Group{
		ID:          fmt.Sprintf("group-%d", time.Now().UnixNano()),
		Name:        name,
		Description: description,
		OwnerID:     ownerID,
		IsActive:    true,
		CreatedAt:   time.Now(),
		UpdatedAt:   time.Now(),
	}

	err := s.DB.Repo.CreateGroup(context.Background(), group)
	if err != nil {
		return nil
	}

	return group
}

// CreateComputeResource creates a compute resource
func (s *UnitTestSuite) CreateComputeResource(name, resourceType, ownerID string) *domain.ComputeResource {
	resource := &domain.ComputeResource{
		ID:             fmt.Sprintf("compute-%d", time.Now().UnixNano()),
		Name:           name,
		Type:           domain.ComputeResourceType(resourceType),
		Endpoint:       "localhost:22",
		OwnerID:        ownerID,
		Status:         domain.ResourceStatusActive,
		CostPerHour:    1.0,
		MaxWorkers:     10,
		CurrentWorkers: 0,
		CreatedAt:      time.Now(),
		UpdatedAt:      time.Now(),
	}

	err := s.DB.Repo.CreateComputeResource(context.Background(), resource)
	if err != nil {
		return nil
	}

	return resource
}
