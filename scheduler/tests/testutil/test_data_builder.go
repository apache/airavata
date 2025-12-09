package testutil

import (
	"context"
	"fmt"
	"time"

	"github.com/apache/airavata/scheduler/adapters"
	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// TestDataBuilder provides a fluent interface for creating test data
type TestDataBuilder struct {
	repo ports.RepositoryPort
	db   *adapters.PostgresAdapter
}

// NewTestDataBuilder creates a new test data builder
func NewTestDataBuilder(db *adapters.PostgresAdapter) *TestDataBuilder {
	repo := adapters.NewRepository(db)
	return &TestDataBuilder{
		repo: repo,
		db:   db,
	}
}

// UserBuilder builds user test data
type UserBuilder struct {
	builder *TestDataBuilder
	user    *domain.User
}

// ProjectBuilder builds project test data
type ProjectBuilder struct {
	builder *TestDataBuilder
	project *domain.Project
}

// ComputeResourceBuilder builds compute resource test data
type ComputeResourceBuilder struct {
	builder  *TestDataBuilder
	resource *domain.ComputeResource
}

// StorageResourceBuilder builds storage resource test data
type StorageResourceBuilder struct {
	builder  *TestDataBuilder
	resource *domain.StorageResource
}

// CredentialBuilder builds credential test data
type CredentialBuilder struct {
	builder    *TestDataBuilder
	credential *domain.Credential
}

// ExperimentBuilder builds experiment test data
type ExperimentBuilder struct {
	builder    *TestDataBuilder
	experiment *domain.Experiment
}

// TaskBuilder builds task test data
type TaskBuilder struct {
	builder *TestDataBuilder
	task    *domain.Task
}

// WorkerBuilder builds worker test data
type WorkerBuilder struct {
	builder *TestDataBuilder
	worker  *domain.Worker
}

// User methods
func (tdb *TestDataBuilder) CreateUser(username, email string, isAdmin bool) *UserBuilder {
	user := &domain.User{
		ID:        fmt.Sprintf("user-%d", time.Now().UnixNano()),
		Username:  username,
		Email:     email,
		FullName:  username,
		IsActive:  true,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	// Set admin status in metadata
	if user.Metadata == nil {
		user.Metadata = make(map[string]interface{})
	}
	user.Metadata["isAdmin"] = isAdmin

	return &UserBuilder{
		builder: tdb,
		user:    user,
	}
}

// ID returns the user ID
func (ub *UserBuilder) ID() string {
	return ub.user.ID
}

// WithID sets the user ID
func (ub *UserBuilder) WithID(id string) *UserBuilder {
	ub.user.ID = id
	return ub
}

// Build persists the user and returns it
func (ub *UserBuilder) Build() (*domain.User, error) {
	if err := ub.builder.repo.CreateUser(context.Background(), ub.user); err != nil {
		return nil, fmt.Errorf("failed to create user: %w", err)
	}
	return ub.user, nil
}

func (ub *UserBuilder) WithEmail(email string) *UserBuilder {
	ub.user.Email = email
	return ub
}

func (ub *UserBuilder) WithAdmin(isAdmin bool) *UserBuilder {
	// Note: User model doesn't have IsAdmin field, using metadata instead
	if ub.user.Metadata == nil {
		ub.user.Metadata = make(map[string]interface{})
	}
	ub.user.Metadata["isAdmin"] = isAdmin
	return ub
}

// Project methods
func (ub *UserBuilder) CreateProject(name, description string) *ProjectBuilder {
	project := &domain.Project{
		ID:          fmt.Sprintf("project-%d", time.Now().UnixNano()),
		Name:        name,
		Description: description,
		OwnerID:     ub.user.ID,
		IsActive:    true,
		CreatedAt:   time.Now(),
		UpdatedAt:   time.Now(),
	}

	return &ProjectBuilder{
		builder: ub.builder,
		project: project,
	}
}

// CreateProject creates a project with a specific user ID
func (tdb *TestDataBuilder) CreateProject(name, description, userID string) *ProjectBuilder {
	project := &domain.Project{
		ID:          fmt.Sprintf("project-%d", time.Now().UnixNano()),
		Name:        name,
		Description: description,
		OwnerID:     userID,
		IsActive:    true,
		CreatedAt:   time.Now(),
		UpdatedAt:   time.Now(),
	}

	return &ProjectBuilder{
		builder: tdb,
		project: project,
	}
}

func (pb *ProjectBuilder) WithID(id string) *ProjectBuilder {
	pb.project.ID = id
	return pb
}

func (pb *ProjectBuilder) WithName(name string) *ProjectBuilder {
	pb.project.Name = name
	return pb
}

func (pb *ProjectBuilder) Build() (*domain.Project, error) {
	if err := pb.builder.repo.CreateProject(context.Background(), pb.project); err != nil {
		return nil, fmt.Errorf("failed to create project: %w", err)
	}
	return pb.project, nil
}

func (pb *ProjectBuilder) WithDescription(description string) *ProjectBuilder {
	pb.project.Description = description
	return pb
}

// ComputeResource methods
func (ub *UserBuilder) CreateComputeResource(name, resourceType, endpoint string) (*ComputeResourceBuilder, error) {
	resource := &domain.ComputeResource{
		ID:             fmt.Sprintf("compute-%d", time.Now().UnixNano()),
		Name:           name,
		Type:           domain.ComputeResourceType(resourceType),
		Endpoint:       endpoint,
		Status:         domain.ResourceStatusActive,
		CostPerHour:    1.0,
		MaxWorkers:     10,
		CurrentWorkers: 0,
		CreatedAt:      time.Now(),
		UpdatedAt:      time.Now(),
		Metadata:       make(map[string]interface{}),
	}

	if err := ub.builder.repo.CreateComputeResource(context.Background(), resource); err != nil {
		return nil, fmt.Errorf("failed to create compute resource: %w", err)
	}

	return &ComputeResourceBuilder{
		builder:  ub.builder,
		resource: resource,
	}, nil
}

func (crb *ComputeResourceBuilder) WithID(id string) *ComputeResourceBuilder {
	crb.resource.ID = id
	return crb
}

func (crb *ComputeResourceBuilder) WithType(resourceType string) *ComputeResourceBuilder {
	crb.resource.Type = domain.ComputeResourceType(resourceType)
	return crb
}

func (crb *ComputeResourceBuilder) WithEndpoint(endpoint string) *ComputeResourceBuilder {
	crb.resource.Endpoint = endpoint
	return crb
}

func (crb *ComputeResourceBuilder) WithStatus(status string) *ComputeResourceBuilder {
	crb.resource.Status = domain.ResourceStatus(status)
	return crb
}

func (crb *ComputeResourceBuilder) WithMetadata(key string, value interface{}) *ComputeResourceBuilder {
	if crb.resource.Metadata == nil {
		crb.resource.Metadata = make(map[string]interface{})
	}
	crb.resource.Metadata[key] = value
	return crb
}

func (crb *ComputeResourceBuilder) Build() (*domain.ComputeResource, error) {
	if err := crb.builder.repo.UpdateComputeResource(context.Background(), crb.resource); err != nil {
		return nil, fmt.Errorf("failed to update compute resource: %w", err)
	}
	return crb.resource, nil
}

// StorageResource methods
func (ub *UserBuilder) CreateStorageResource(name, resourceType, endpoint string) (*StorageResourceBuilder, error) {
	capacity := int64(1000000000) // 1GB
	resource := &domain.StorageResource{
		ID:                fmt.Sprintf("storage-%d", time.Now().UnixNano()),
		Name:              name,
		Type:              domain.StorageResourceType(resourceType),
		Endpoint:          endpoint,
		OwnerID:           "test-user",
		Status:            domain.ResourceStatusActive,
		TotalCapacity:     &capacity,
		UsedCapacity:      nil,
		AvailableCapacity: &capacity,
		CreatedAt:         time.Now(),
		UpdatedAt:         time.Now(),
		Metadata:          make(map[string]interface{}),
	}

	if err := ub.builder.repo.CreateStorageResource(context.Background(), resource); err != nil {
		return nil, fmt.Errorf("failed to create storage resource: %w", err)
	}

	return &StorageResourceBuilder{
		builder:  ub.builder,
		resource: resource,
	}, nil
}

func (srb *StorageResourceBuilder) WithID(id string) *StorageResourceBuilder {
	srb.resource.ID = id
	return srb
}

func (srb *StorageResourceBuilder) WithType(resourceType string) *StorageResourceBuilder {
	srb.resource.Type = domain.StorageResourceType(resourceType)
	return srb
}

func (srb *StorageResourceBuilder) WithEndpoint(endpoint string) *StorageResourceBuilder {
	srb.resource.Endpoint = endpoint
	return srb
}

func (srb *StorageResourceBuilder) WithStatus(status string) *StorageResourceBuilder {
	srb.resource.Status = domain.ResourceStatus(status)
	return srb
}

func (srb *StorageResourceBuilder) WithMetadata(key string, value interface{}) *StorageResourceBuilder {
	if srb.resource.Metadata == nil {
		srb.resource.Metadata = make(map[string]interface{})
	}
	srb.resource.Metadata[key] = value
	return srb
}

func (srb *StorageResourceBuilder) Build() (*domain.StorageResource, error) {
	if err := srb.builder.repo.UpdateStorageResource(context.Background(), srb.resource); err != nil {
		return nil, fmt.Errorf("failed to update storage resource: %w", err)
	}
	return srb.resource, nil
}

// Credential methods
func (ub *UserBuilder) CreateCredential(name, credentialType string, data []byte) (*CredentialBuilder, error) {
	// For now, we'll create a simple credential object without persisting to database
	// since credentials are now stored in OpenBao
	credential := &domain.Credential{
		ID:        fmt.Sprintf("cred-%d", time.Now().UnixNano()),
		Name:      name,
		Type:      domain.CredentialType(credentialType),
		OwnerID:   ub.user.ID,
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}

	return &CredentialBuilder{
		builder:    ub.builder,
		credential: credential,
	}, nil
}

// CreateSSHCredential creates an SSH key credential in vault
func (ub *UserBuilder) CreateSSHCredential(name string, privateKey []byte) (*CredentialBuilder, error) {
	return ub.CreateCredential(name, string(domain.CredentialTypeSSHKey), privateKey)
}

func (cb *CredentialBuilder) WithID(id string) *CredentialBuilder {
	cb.credential.ID = id
	return cb
}

func (cb *CredentialBuilder) WithName(name string) *CredentialBuilder {
	cb.credential.Name = name
	return cb
}

func (cb *CredentialBuilder) WithType(credentialType string) *CredentialBuilder {
	cb.credential.Type = domain.CredentialType(credentialType)
	return cb
}

func (cb *CredentialBuilder) WithData(data []byte) *CredentialBuilder {
	// Note: Data is now stored in OpenBao, not in the credential object
	return cb
}

func (cb *CredentialBuilder) Build() (*domain.Credential, error) {
	// For now, we'll just return the credential object without persisting to database
	// since credentials are now stored in OpenBao
	return cb.credential, nil
}

// Experiment methods
func (pb *ProjectBuilder) CreateExperiment(name, description, commandTemplate string) *ExperimentBuilder {
	experiment := &domain.Experiment{
		ID:              fmt.Sprintf("exp-%d", time.Now().UnixNano()),
		Name:            name,
		Description:     description,
		ProjectID:       pb.project.ID,
		OwnerID:         pb.project.OwnerID,
		Status:          domain.ExperimentStatusCreated,
		CommandTemplate: commandTemplate,
		OutputPattern:   "output_{task_id}.txt",
		Parameters:      []domain.ParameterSet{},
		Requirements:    &domain.ResourceRequirements{},
		Constraints:     &domain.ExperimentConstraints{},
		CreatedAt:       time.Now(),
		UpdatedAt:       time.Now(),
		Metadata:        make(map[string]interface{}),
	}

	return &ExperimentBuilder{
		builder:    pb.builder,
		experiment: experiment,
	}
}

func (eb *ExperimentBuilder) WithID(id string) *ExperimentBuilder {
	eb.experiment.ID = id
	return eb
}

func (eb *ExperimentBuilder) WithName(name string) *ExperimentBuilder {
	eb.experiment.Name = name
	return eb
}

func (eb *ExperimentBuilder) WithStatus(status string) *ExperimentBuilder {
	eb.experiment.Status = domain.ExperimentStatus(status)
	return eb
}

func (eb *ExperimentBuilder) WithCommandTemplate(template string) *ExperimentBuilder {
	eb.experiment.CommandTemplate = template
	return eb
}

func (eb *ExperimentBuilder) WithParameters(parameters []domain.ParameterSet) *ExperimentBuilder {
	eb.experiment.Parameters = parameters
	return eb
}

func (eb *ExperimentBuilder) WithRequirements(requirements *domain.ResourceRequirements) *ExperimentBuilder {
	eb.experiment.Requirements = requirements
	return eb
}

func (eb *ExperimentBuilder) WithConstraints(constraints *domain.ExperimentConstraints) *ExperimentBuilder {
	eb.experiment.Constraints = constraints
	return eb
}

func (eb *ExperimentBuilder) WithMetadata(key string, value interface{}) *ExperimentBuilder {
	if eb.experiment.Metadata == nil {
		eb.experiment.Metadata = make(map[string]interface{})
	}
	eb.experiment.Metadata[key] = value
	return eb
}

func (eb *ExperimentBuilder) Build() (*domain.Experiment, error) {
	if err := eb.builder.repo.CreateExperiment(context.Background(), eb.experiment); err != nil {
		return nil, fmt.Errorf("failed to create experiment: %w", err)
	}
	return eb.experiment, nil
}

// Task methods
func (eb *ExperimentBuilder) CreateTask(command string) *TaskBuilder {
	task := &domain.Task{
		ID:           fmt.Sprintf("task-%d", time.Now().UnixNano()),
		ExperimentID: eb.experiment.ID,
		Status:       domain.TaskStatusQueued,
		Command:      command,
		InputFiles:   []domain.FileMetadata{},
		OutputFiles:  []domain.FileMetadata{},
		RetryCount:   0,
		MaxRetries:   3,
		CreatedAt:    time.Now(),
		UpdatedAt:    time.Now(),
	}

	return &TaskBuilder{
		builder: eb.builder,
		task:    task,
	}
}

func (tb *TaskBuilder) WithID(id string) *TaskBuilder {
	tb.task.ID = id
	return tb
}

func (tb *TaskBuilder) WithStatus(status string) *TaskBuilder {
	tb.task.Status = domain.TaskStatus(status)
	return tb
}

func (tb *TaskBuilder) WithCommand(command string) *TaskBuilder {
	tb.task.Command = command
	return tb
}

func (tb *TaskBuilder) WithWorkerID(workerID string) *TaskBuilder {
	tb.task.WorkerID = workerID
	return tb
}

func (tb *TaskBuilder) WithInputFiles(files []domain.FileMetadata) *TaskBuilder {
	tb.task.InputFiles = files
	return tb
}

func (tb *TaskBuilder) WithOutputFiles(files []domain.FileMetadata) *TaskBuilder {
	tb.task.OutputFiles = files
	return tb
}

func (tb *TaskBuilder) Build() (*domain.Task, error) {
	if err := tb.builder.repo.CreateTask(context.Background(), tb.task); err != nil {
		return nil, fmt.Errorf("failed to create task: %w", err)
	}
	return tb.task, nil
}

// Worker methods
func (crb *ComputeResourceBuilder) CreateWorker(experimentID string, walltime time.Duration) *WorkerBuilder {
	worker := &domain.Worker{
		ID:                fmt.Sprintf("worker-%d", time.Now().UnixNano()),
		ComputeResourceID: crb.resource.ID,
		ExperimentID:      experimentID,
		Status:            domain.WorkerStatusIdle,
		Walltime:          walltime,
		WalltimeRemaining: walltime,
		LastHeartbeat:     time.Now(),
		CreatedAt:         time.Now(),
		UpdatedAt:         time.Now(),
	}

	return &WorkerBuilder{
		builder: crb.builder,
		worker:  worker,
	}
}

func (wb *WorkerBuilder) WithID(id string) *WorkerBuilder {
	wb.worker.ID = id
	return wb
}

func (wb *WorkerBuilder) WithStatus(status string) *WorkerBuilder {
	wb.worker.Status = domain.WorkerStatus(status)
	return wb
}

func (wb *WorkerBuilder) WithWalltime(walltime time.Duration) *WorkerBuilder {
	wb.worker.Walltime = walltime
	wb.worker.WalltimeRemaining = walltime
	return wb
}

func (wb *WorkerBuilder) WithWalltimeRemaining(remaining time.Duration) *WorkerBuilder {
	wb.worker.WalltimeRemaining = remaining
	return wb
}

func (wb *WorkerBuilder) WithLastHeartbeat(heartbeat time.Time) *WorkerBuilder {
	wb.worker.LastHeartbeat = heartbeat
	return wb
}

func (wb *WorkerBuilder) Build() (*domain.Worker, error) {
	if err := wb.builder.repo.CreateWorker(context.Background(), wb.worker); err != nil {
		return nil, fmt.Errorf("failed to create worker: %w", err)
	}
	return wb.worker, nil
}

// Convenience methods for common test scenarios
func (tdb *TestDataBuilder) CreateUserWithProject(username, email, projectName string) (*domain.User, *domain.Project, error) {
	userBuilder := tdb.CreateUser(username, email, false)
	user, err := userBuilder.Build()
	if err != nil {
		return nil, nil, err
	}

	projectBuilder := userBuilder.CreateProject(projectName, "Test project")
	project, err := projectBuilder.Build()
	if err != nil {
		return nil, nil, err
	}

	return user, project, nil
}

func (tdb *TestDataBuilder) CreateExperimentWithTasks(userID, projectID, experimentName string, numTasks int) (*domain.Experiment, []*domain.Task, error) {
	// Get project
	project, err := tdb.repo.GetProjectByID(context.Background(), projectID)
	if err != nil {
		return nil, nil, err
	}

	// Create experiment
	experimentBuilder := (&ProjectBuilder{builder: tdb, project: project}).CreateExperiment(experimentName, "Test experiment", "echo test")
	experiment, err := experimentBuilder.Build()
	if err != nil {
		return nil, nil, err
	}

	// Create tasks
	var tasks []*domain.Task
	for i := 0; i < numTasks; i++ {
		taskBuilder := experimentBuilder.CreateTask(fmt.Sprintf("echo task_%d", i))
		task, err := taskBuilder.Build()
		if err != nil {
			return nil, nil, err
		}
		tasks = append(tasks, task)
	}

	return experiment, tasks, nil
}
