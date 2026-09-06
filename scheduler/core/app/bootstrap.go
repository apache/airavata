package app

import (
	"context"
	"fmt"
	"log"
	"net"
	"net/http"
	"time"

	"github.com/apache/airavata/scheduler/adapters"
	"github.com/apache/airavata/scheduler/core/config"
	"github.com/apache/airavata/scheduler/core/domain"
	"github.com/apache/airavata/scheduler/core/dto"
	service "github.com/apache/airavata/scheduler/core/service"
	"github.com/gorilla/mux"
	"github.com/hashicorp/vault/api"
	"google.golang.org/grpc"
)

// Application represents the main application
type Application struct {
	Config            *config.Config
	Server            *http.Server
	GRPCServer        *grpc.Server
	Handlers          *adapters.Handlers
	Hub               *adapters.Hub
	WorkerGRPCService *adapters.WorkerGRPCService
	// Expose services for direct access
	Orchestrator domain.ExperimentOrchestrator
	Scheduler    domain.TaskScheduler
	Registry     domain.ResourceRegistry
	Vault        domain.CredentialVault
	DataMover    domain.DataMover
	Worker       domain.WorkerLifecycle
	// Recovery components
	RecoveryManager      *RecoveryManager
	BackgroundJobManager *service.BackgroundJobManager
	StagingManager       *service.StagingOperationManager
	ShutdownCoordinator  *ShutdownCoordinator
}

// Bootstrap creates and configures the application
func Bootstrap(config *config.Config) (*Application, error) {
	ctx := context.Background()

	// Create database adapter
	dbAdapter, err := adapters.NewPostgresAdapter(config.Database.DSN)
	if err != nil {
		return nil, fmt.Errorf("failed to create database adapter: %w", err)
	}

	// Create repository
	repo := adapters.NewRepository(dbAdapter)

	// Create real port implementations
	eventsPort := adapters.NewPostgresEventAdapter(dbAdapter.GetDB())
	securityPort := adapters.NewJWTAdapter("", "airavata-scheduler", "airavata-users")
	cachePort := adapters.NewPostgresCacheAdapter(dbAdapter.GetDB())

	// Create SpiceDB client and adapter
	spicedbAdapter, err := adapters.NewSpiceDBAdapter(config.SpiceDB.Endpoint, config.SpiceDB.PresharedKey)
	if err != nil {
		return nil, fmt.Errorf("failed to create SpiceDB adapter: %w", err)
	}

	// Create OpenBao client and adapter
	vaultConfig := api.DefaultConfig()
	vaultConfig.Address = config.OpenBao.Address
	vaultClient, err := api.NewClient(vaultConfig)
	if err != nil {
		return nil, fmt.Errorf("failed to create OpenBao client: %w", err)
	}
	vaultClient.SetToken(config.OpenBao.Token)

	openbaoAdapter := adapters.NewOpenBaoAdapter(vaultClient, "secret")

	// Create storage and compute ports using factories
	// Create service factories - order matters for dependencies
	vaultFactory := NewVaultFactory(openbaoAdapter, spicedbAdapter, securityPort, eventsPort)

	// Create vault service first (no dependencies)
	vaultService, err := vaultFactory.CreateService(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to create vault service: %w", err)
	}

	// Create registry service with vault dependency
	registryFactory := NewRegistryFactory(repo, eventsPort, securityPort, vaultService)
	registryService, err := registryFactory.CreateService(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to create registry service: %w", err)
	}

	// Create storage and compute factories with vault service
	storageFactory := adapters.NewStorageFactory(repo, vaultService)
	storagePort, err := storageFactory.CreateDefaultStorage(ctx, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create storage port: %w", err)
	}

	computeFactory := adapters.NewComputeFactory(repo, eventsPort, vaultService)
	computePort, err := computeFactory.CreateDefaultCompute(ctx, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to create compute port: %w", err)
	}

	// Create data mover (no service dependencies)
	datamoverFactory := NewDataMoverFactory(repo, storagePort, cachePort, eventsPort)
	datamoverService, err := datamoverFactory.CreateService(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to create datamover service: %w", err)
	}

	// Create worker service (needs compute port)
	workerFactory := NewWorkerFactory(repo, computePort, eventsPort)
	workerService, err := workerFactory.CreateService(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to create worker service: %w", err)
	}

	// Create staging manager first (needed by scheduler)
	stagingManager := service.NewStagingOperationManager(dbAdapter.GetDB(), eventsPort)

	// Create StateManager (needed by scheduler and orchestrator)
	stateManager := service.NewStateManager(repo, eventsPort)

	// Create orchestrator service first (without scheduler)
	orchestratorFactory := NewOrchestratorFactory(repo, eventsPort, securityPort, nil, stateManager)
	orchestratorService, err := orchestratorFactory.CreateService(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to create orchestrator service: %w", err)
	}

	// Create scheduler with all dependencies (workerGRPCService will be created later)
	schedulerFactory := NewSchedulerFactory(
		repo,
		eventsPort,
		registryService,
		orchestratorService,
		datamoverService,
		nil,            // workerGRPCService - will be set after creation
		stagingManager, // stagingManager - pass the staging manager
		vaultService,   // vault - pass the vault service
		stateManager,   // stateManager - pass the state manager
	)

	schedulerService, err := schedulerFactory.CreateService(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to create scheduler service: %w", err)
	}

	// Now create the orchestrator service with the scheduler
	orchestratorFactory = NewOrchestratorFactory(repo, eventsPort, securityPort, schedulerService, stateManager)
	orchestratorService, err = orchestratorFactory.CreateService(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to create orchestrator service with scheduler: %w", err)
	}

	// Create WebSocket hub
	hub := adapters.NewHub()
	go hub.Run()

	// Create worker gRPC service (now with scheduler and state manager)
	workerGRPCService := adapters.NewWorkerGRPCService(
		repo,
		schedulerService,
		datamoverService,
		eventsPort,
		hub,
		stateManager,
	)

	// Create recovery and background job managers
	backgroundJobManager := service.NewBackgroundJobManager(dbAdapter.GetDB(), eventsPort)
	recoveryManager := NewRecoveryManager(dbAdapter.GetDB(), stagingManager, repo, eventsPort)

	// Create shutdown coordinator
	shutdownCoordinator := NewShutdownCoordinator(recoveryManager, backgroundJobManager)

	// Create analytics and experiment services
	analyticsService := service.NewAnalyticsService(dbAdapter.GetDB())
	experimentService := service.NewExperimentService(dbAdapter.GetDB())

	// Create worker config for handlers
	workerConfig := &adapters.WorkerConfig{
		BinaryPath: config.Worker.BinaryPath,
		BinaryURL:  config.Worker.BinaryURL,
	}

	// Create HTTP handlers
	handlers := adapters.NewHandlers(
		registryService,
		repo,
		vaultService,
		orchestratorService,
		schedulerService,
		datamoverService,
		workerService,
		analyticsService,
		experimentService,
		workerConfig,
	)

	// Create HTTP router
	router := mux.NewRouter()
	handlers.RegisterRoutes(router)

	// Add WebSocket routes
	wsUpgrader := adapters.NewWebSocketUpgrader(hub, nil)
	router.HandleFunc("/ws", wsUpgrader.HandleWebSocket).Methods("GET")
	router.HandleFunc("/ws/experiments/{experimentId}", wsUpgrader.HandleWebSocket).Methods("GET")
	router.HandleFunc("/ws/tasks/{taskId}", wsUpgrader.HandleWebSocket).Methods("GET")
	router.HandleFunc("/ws/projects/{projectId}", wsUpgrader.HandleWebSocket).Methods("GET")
	router.HandleFunc("/ws/user", wsUpgrader.HandleWebSocket).Methods("GET")

	// Create HTTP server
	server := &http.Server{
		Addr:         fmt.Sprintf("%s:%d", config.Server.Host, config.Server.Port),
		Handler:      router,
		ReadTimeout:  config.Server.ReadTimeout,
		WriteTimeout: config.Server.WriteTimeout,
		IdleTimeout:  config.Server.IdleTimeout,
	}

	// Create gRPC server for worker communication
	grpcServer := grpc.NewServer()

	// Register worker service with gRPC server
	dto.RegisterWorkerServiceServer(grpcServer, workerGRPCService)

	return &Application{
		Config:            config,
		Server:            server,
		GRPCServer:        grpcServer,
		Handlers:          handlers,
		Hub:               hub,
		WorkerGRPCService: workerGRPCService,
		// Expose services for direct access
		Orchestrator: orchestratorService,
		Scheduler:    schedulerService,
		Registry:     registryService,
		Vault:        vaultService,
		DataMover:    datamoverService,
		Worker:       workerService,
		// Recovery components
		RecoveryManager:      recoveryManager,
		BackgroundJobManager: backgroundJobManager,
		StagingManager:       stagingManager,
		ShutdownCoordinator:  shutdownCoordinator,
	}, nil
}

// Start starts the application
func (app *Application) Start() error {
	ctx := context.Background()

	// Start recovery process
	if app.RecoveryManager != nil {
		if err := app.RecoveryManager.StartRecovery(ctx); err != nil {
			log.Printf("Warning: recovery process failed: %v", err)
		}
	}

	// Resume background jobs
	if app.BackgroundJobManager != nil {
		// Define job handlers
		handlers := map[service.JobType]service.JobHandler{
			service.JobTypeStagingMonitor:   app.handleStagingMonitorJob,
			service.JobTypeWorkerHealth:     app.handleWorkerHealthJob,
			service.JobTypeCacheCleanup:     app.handleCacheCleanupJob,
			service.JobTypeMetricsCollector: app.handleMetricsCollectorJob,
		}

		if err := app.BackgroundJobManager.ResumeJobs(ctx, handlers); err != nil {
			log.Printf("Warning: failed to resume background jobs: %v", err)
		}
	}

	// Start gRPC server in a goroutine
	go func() {
		grpcAddr := fmt.Sprintf("%s:%d", app.Config.GRPC.Host, app.Config.GRPC.Port)

		listener, err := net.Listen("tcp", grpcAddr)
		if err != nil {
			log.Fatalf("Failed to listen on gRPC port: %v", err)
		}

		log.Printf("Starting gRPC server on %s", grpcAddr)
		if err := app.GRPCServer.Serve(listener); err != nil {
			log.Fatalf("Failed to serve gRPC: %v", err)
		}
	}()

	log.Printf("Starting Airavata Scheduler on %s", app.Server.Addr)
	return app.Server.ListenAndServe()
}

// Stop stops the application
func (app *Application) Stop(ctx context.Context) error {
	log.Println("Stopping Airavata Scheduler...")

	// Use shutdown coordinator for graceful shutdown
	if app.ShutdownCoordinator != nil {
		if err := app.ShutdownCoordinator.StartShutdown(ctx); err != nil {
			log.Printf("Warning: graceful shutdown failed: %v", err)
		}
	}

	// Stop gRPC server gracefully
	app.GRPCServer.GracefulStop()

	// Stop HTTP server
	return app.Server.Shutdown(ctx)
}

// Job handler methods for background job manager

func (app *Application) handleStagingMonitorJob(ctx context.Context, job *service.BackgroundJob) error {
	// This would monitor staging operations and handle timeouts
	log.Printf("Handling staging monitor job: %s", job.ID)

	// Simulate work
	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-time.After(30 * time.Second):
		// Job completed
		return nil
	}
}

func (app *Application) handleWorkerHealthJob(ctx context.Context, job *service.BackgroundJob) error {
	// This would check worker health and handle failures
	log.Printf("Handling worker health job: %s", job.ID)

	// Simulate work
	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-time.After(30 * time.Second):
		// Job completed
		return nil
	}
}

func (app *Application) handleCacheCleanupJob(ctx context.Context, job *service.BackgroundJob) error {
	// This would clean up expired cache entries
	log.Printf("Handling cache cleanup job: %s", job.ID)

	// Simulate work
	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-time.After(30 * time.Second):
		// Job completed
		return nil
	}
}

func (app *Application) handleMetricsCollectorJob(ctx context.Context, job *service.BackgroundJob) error {
	// This would collect and process metrics
	log.Printf("Handling metrics collector job: %s", job.ID)

	// Simulate work
	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-time.After(30 * time.Second):
		// Job completed
		return nil
	}
}
