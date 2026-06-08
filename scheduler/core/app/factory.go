package app

import (
	"context"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	services "github.com/apache/airavata/scheduler/core/service"
)

// DataMover Factory

// DataMoverFactory creates DataMover service instances
type DataMoverFactory struct {
	repo    ports.RepositoryPort
	storage ports.StoragePort
	cache   ports.CachePort
	events  ports.EventPort
}

// NewDataMoverFactory creates a new DataMover factory
func NewDataMoverFactory(repo ports.RepositoryPort, storage ports.StoragePort, cache ports.CachePort, events ports.EventPort) *DataMoverFactory {
	return &DataMoverFactory{
		repo:    repo,
		storage: storage,
		cache:   cache,
		events:  events,
	}
}

// CreateService creates a new DataMover service
func (f *DataMoverFactory) CreateService(ctx context.Context) (*services.DataMoverService, error) {
	return services.NewDataMoverService(f.repo, f.storage, f.cache, f.events), nil
}

// Orchestrator Factory

// OrchestratorFactory creates ExperimentOrchestrator service instances
type OrchestratorFactory struct {
	repo         ports.RepositoryPort
	events       ports.EventPort
	security     ports.SecurityPort
	scheduler    domain.TaskScheduler
	stateManager *services.StateManager
}

// NewOrchestratorFactory creates a new ExperimentOrchestrator factory
func NewOrchestratorFactory(repo ports.RepositoryPort, events ports.EventPort, security ports.SecurityPort, scheduler domain.TaskScheduler, stateManager *services.StateManager) *OrchestratorFactory {
	return &OrchestratorFactory{
		repo:         repo,
		events:       events,
		security:     security,
		scheduler:    scheduler,
		stateManager: stateManager,
	}
}

// CreateService creates a new ExperimentOrchestrator service
func (f *OrchestratorFactory) CreateService(ctx context.Context) (*services.OrchestratorService, error) {
	return services.NewOrchestratorService(f.repo, f.events, f.security, f.scheduler, f.stateManager), nil
}

// Registry Factory

// RegistryFactory creates ResourceRegistry service instances
type RegistryFactory struct {
	repo     ports.RepositoryPort
	events   ports.EventPort
	security ports.SecurityPort
	vault    domain.CredentialVault
}

// NewRegistryFactory creates a new ResourceRegistry factory
func NewRegistryFactory(repo ports.RepositoryPort, events ports.EventPort, security ports.SecurityPort, vault domain.CredentialVault) *RegistryFactory {
	return &RegistryFactory{
		repo:     repo,
		events:   events,
		security: security,
		vault:    vault,
	}
}

// CreateService creates a new ResourceRegistry service
func (f *RegistryFactory) CreateService(ctx context.Context) (*services.RegistryService, error) {
	return services.NewRegistryService(f.repo, f.events, f.security, f.vault), nil
}

// Scheduler Factory

// SchedulerFactory creates TaskScheduler service instances
type SchedulerFactory struct {
	repo           ports.RepositoryPort
	events         ports.EventPort
	registry       domain.ResourceRegistry
	orchestrator   domain.ExperimentOrchestrator
	dataMover      domain.DataMover
	workerGRPC     domain.WorkerGRPCService
	stagingManager *services.StagingOperationManager
	vault          domain.CredentialVault
	stateManager   *services.StateManager
}

// NewSchedulerFactory creates a new TaskScheduler factory
func NewSchedulerFactory(repo ports.RepositoryPort, events ports.EventPort, registry domain.ResourceRegistry, orchestrator domain.ExperimentOrchestrator, dataMover domain.DataMover, workerGRPC domain.WorkerGRPCService, stagingManager *services.StagingOperationManager, vault domain.CredentialVault, stateManager *services.StateManager) *SchedulerFactory {
	return &SchedulerFactory{
		repo:           repo,
		events:         events,
		registry:       registry,
		orchestrator:   orchestrator,
		dataMover:      dataMover,
		workerGRPC:     workerGRPC,
		stagingManager: stagingManager,
		vault:          vault,
		stateManager:   stateManager,
	}
}

// CreateService creates a new TaskScheduler service
func (f *SchedulerFactory) CreateService(ctx context.Context) (*services.SchedulerService, error) {
	// Handle nil workerGRPC (circular dependency resolution)
	var workerGRPC domain.WorkerGRPCService = nil
	if f.workerGRPC != nil {
		workerGRPC = f.workerGRPC
	}
	return services.NewSchedulerService(f.repo, f.events, f.registry, f.orchestrator, f.dataMover, workerGRPC, f.stagingManager, f.vault, f.stateManager), nil
}

// Vault Factory

// VaultFactory creates CredentialVault service instances
type VaultFactory struct {
	vault    ports.VaultPort
	authz    ports.AuthorizationPort
	security ports.SecurityPort
	events   ports.EventPort
}

// NewVaultFactory creates a new CredentialVault factory
func NewVaultFactory(vault ports.VaultPort, authz ports.AuthorizationPort, security ports.SecurityPort, events ports.EventPort) *VaultFactory {
	return &VaultFactory{
		vault:    vault,
		authz:    authz,
		security: security,
		events:   events,
	}
}

// CreateService creates a new CredentialVault service
func (f *VaultFactory) CreateService(ctx context.Context) (*services.VaultService, error) {
	return services.NewVaultService(f.vault, f.authz, f.security, f.events), nil
}

// Worker Factory

// WorkerFactory creates WorkerLifecycle service instances
type WorkerFactory struct {
	repo    ports.RepositoryPort
	compute ports.ComputePort
	events  ports.EventPort
}

// NewWorkerFactory creates a new WorkerLifecycle factory
func NewWorkerFactory(repo ports.RepositoryPort, compute ports.ComputePort, events ports.EventPort) *WorkerFactory {
	return &WorkerFactory{
		repo:    repo,
		compute: compute,
		events:  events,
	}
}

// CreateService creates a new WorkerLifecycle service
func (f *WorkerFactory) CreateService(ctx context.Context) (*services.WorkerService, error) {
	return services.NewWorkerService(f.repo, f.compute, f.events), nil
}
