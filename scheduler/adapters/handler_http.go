package adapters

import (
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"strconv"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
	service "github.com/apache/airavata/scheduler/core/service"
	types "github.com/apache/airavata/scheduler/core/util"
	"github.com/gorilla/mux"
)

// Handlers provides HTTP handlers for the API
type Handlers struct {
	registry     domain.ResourceRegistry
	repository   ports.RepositoryPort
	vault        domain.CredentialVault
	orchestrator domain.ExperimentOrchestrator
	scheduler    domain.TaskScheduler
	datamover    domain.DataMover
	worker       domain.WorkerLifecycle
	analytics    *service.AnalyticsService
	experiment   *service.ExperimentService
	config       *WorkerConfig
}

// WorkerConfig holds worker-related configuration
type WorkerConfig struct {
	BinaryPath string
	BinaryURL  string
}

// NewHandlers creates a new HTTP handlers instance
func NewHandlers(
	registry domain.ResourceRegistry,
	repository ports.RepositoryPort,
	vault domain.CredentialVault,
	orchestrator domain.ExperimentOrchestrator,
	scheduler domain.TaskScheduler,
	datamover domain.DataMover,
	worker domain.WorkerLifecycle,
	analytics *service.AnalyticsService,
	experiment *service.ExperimentService,
	config *WorkerConfig,
) *Handlers {
	return &Handlers{
		registry:     registry,
		repository:   repository,
		vault:        vault,
		orchestrator: orchestrator,
		scheduler:    scheduler,
		datamover:    datamover,
		worker:       worker,
		analytics:    analytics,
		experiment:   experiment,
		config:       config,
	}
}

// RegisterRoutes registers all HTTP routes
func (h *Handlers) RegisterRoutes(router *mux.Router) {
	// API version
	api := router.PathPrefix("/api/v2").Subrouter()

	// Authentication endpoints
	api.HandleFunc("/auth/login", h.Login).Methods("POST")
	api.HandleFunc("/auth/logout", h.Logout).Methods("POST")
	api.HandleFunc("/auth/refresh", h.RefreshToken).Methods("POST")

	// User self-service endpoints
	api.HandleFunc("/user/profile", h.GetUserProfile).Methods("GET")
	api.HandleFunc("/user/profile", h.UpdateUserProfile).Methods("PUT")
	api.HandleFunc("/user/password", h.ChangePassword).Methods("PUT")
	api.HandleFunc("/user/groups", h.GetUserGroups).Methods("GET")
	api.HandleFunc("/user/projects", h.GetUserProjects).Methods("GET")

	// Project endpoints
	api.HandleFunc("/projects", h.CreateProject).Methods("POST")
	api.HandleFunc("/projects", h.ListProjects).Methods("GET")
	api.HandleFunc("/projects/{id}", h.GetProject).Methods("GET")
	api.HandleFunc("/projects/{id}", h.UpdateProject).Methods("PUT")
	api.HandleFunc("/projects/{id}", h.DeleteProject).Methods("DELETE")

	// Resource registry endpoints
	api.HandleFunc("/resources/compute", h.CreateComputeResource).Methods("POST")
	api.HandleFunc("/resources/storage", h.CreateStorageResource).Methods("POST")
	api.HandleFunc("/resources", h.ListResources).Methods("GET")
	api.HandleFunc("/resources/{id}", h.GetResource).Methods("GET")
	api.HandleFunc("/resources/{id}", h.UpdateResource).Methods("PUT")
	api.HandleFunc("/resources/{id}", h.DeleteResource).Methods("DELETE")

	// Credential vault endpoints
	api.HandleFunc("/credentials", h.StoreCredential).Methods("POST")
	api.HandleFunc("/credentials/{id}", h.RetrieveCredential).Methods("GET")
	api.HandleFunc("/credentials/{id}", h.UpdateCredential).Methods("PUT")
	api.HandleFunc("/credentials/{id}", h.DeleteCredential).Methods("DELETE")
	api.HandleFunc("/credentials", h.ListCredentials).Methods("GET")

	// Experiment orchestrator endpoints
	api.HandleFunc("/experiments", h.CreateExperiment).Methods("POST")
	api.HandleFunc("/experiments", h.ListExperiments).Methods("GET")
	api.HandleFunc("/experiments/search", h.SearchExperiments).Methods("GET")
	api.HandleFunc("/experiments/{id}", h.GetExperiment).Methods("GET")
	api.HandleFunc("/experiments/{id}", h.UpdateExperiment).Methods("PUT")
	api.HandleFunc("/experiments/{id}", h.DeleteExperiment).Methods("DELETE")
	api.HandleFunc("/experiments/{id}/submit", h.SubmitExperiment).Methods("POST")
	api.HandleFunc("/experiments/{id}/tasks", h.GenerateTasks).Methods("POST")
	api.HandleFunc("/experiments/{id}/summary", h.GetExperimentSummary).Methods("GET")
	api.HandleFunc("/experiments/{id}/failed-tasks", h.GetFailedTasks).Methods("GET")
	api.HandleFunc("/experiments/{id}/timeline", h.GetExperimentTimeline).Methods("GET")
	api.HandleFunc("/experiments/{id}/progress", h.GetExperimentProgress).Methods("GET")
	api.HandleFunc("/experiments/{id}/derive", h.CreateDerivativeExperiment).Methods("POST")
	api.HandleFunc("/experiments/{id}/outputs", h.ListExperimentOutputs).Methods("GET")
	api.HandleFunc("/experiments/{id}/outputs/download", h.DownloadExperimentOutputs).Methods("GET")
	api.HandleFunc("/experiments/{id}/outputs/{task_id}/{filename}", h.DownloadExperimentOutputFile).Methods("GET")

	// Task scheduler endpoints
	api.HandleFunc("/experiments/{id}/schedule", h.ScheduleExperiment).Methods("POST")
	api.HandleFunc("/workers/{id}/assign", h.AssignTask).Methods("POST")
	api.HandleFunc("/tasks/{id}/complete", h.CompleteTask).Methods("POST")
	api.HandleFunc("/tasks/{id}/fail", h.FailTask).Methods("POST")
	api.HandleFunc("/workers/{id}/status", h.GetWorkerStatus).Methods("GET")
	api.HandleFunc("/tasks/aggregate", h.GetTaskAggregation).Methods("GET")
	api.HandleFunc("/tasks/{id}/progress", h.GetTaskProgress).Methods("GET")

	// Worker lifecycle endpoints
	api.HandleFunc("/workers", h.SpawnWorker).Methods("POST")
	api.HandleFunc("/workers/{id}/register", h.RegisterWorker).Methods("POST")
	api.HandleFunc("/workers/{id}/start-polling", h.StartWorkerPolling).Methods("POST")
	api.HandleFunc("/workers/{id}/stop-polling", h.StopWorkerPolling).Methods("POST")
	api.HandleFunc("/workers/{id}/terminate", h.TerminateWorker).Methods("POST")

	// Worker binary download endpoint
	router.HandleFunc("/api/worker-binary", h.ServeWorkerBinary).Methods("GET")
	api.HandleFunc("/workers/{id}/heartbeat", h.SendHeartbeat).Methods("POST")

	// Health check endpoints
	api.HandleFunc("/health", h.HealthCheck).Methods("GET")
	api.HandleFunc("/health/detailed", h.DetailedHealthCheck).Methods("GET")

	// Metrics endpoint
	api.HandleFunc("/metrics", h.Metrics).Methods("GET")

}

// CreateComputeResource handles POST /api/v2/resources/compute
func (h *Handlers) CreateComputeResource(w http.ResponseWriter, r *http.Request) {
	ctx := r.Context()

	// Check if this is a token-based registration (from CLI)
	var tokenRegistration struct {
		Token        string                 `json:"token"`
		Name         string                 `json:"name"`
		Type         string                 `json:"type"`
		Hostname     string                 `json:"hostname"`
		Capabilities map[string]interface{} `json:"capabilities"`
		PrivateKey   string                 `json:"private_key"`
	}

	// Try to decode as token-based registration first
	bodyBytes, err := io.ReadAll(r.Body)
	if err != nil {
		http.Error(w, "Failed to read request body", http.StatusBadRequest)
		return
	}

	// Check if this looks like a token-based registration
	if err := json.Unmarshal(bodyBytes, &tokenRegistration); err == nil && tokenRegistration.Token != "" {
		// This is a token-based registration from CLI
		resp, err := h.handleTokenBasedRegistration(ctx, tokenRegistration)
		if err != nil {
			http.Error(w, err.Error(), http.StatusInternalServerError)
			return
		}

		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusCreated)
		json.NewEncoder(w).Encode(resp)
		return
	}

	// Regular v2 API registration
	var req domain.CreateComputeResourceRequest
	if err := json.Unmarshal(bodyBytes, &req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	resp, err := h.registry.RegisterComputeResource(ctx, &req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// CreateStorageResource handles POST /api/v2/resources/storage
func (h *Handlers) CreateStorageResource(w http.ResponseWriter, r *http.Request) {
	var req domain.CreateStorageResourceRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	resp, err := h.registry.RegisterStorageResource(ctx, &req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// ListResources handles GET /api/v2/resources
func (h *Handlers) ListResources(w http.ResponseWriter, r *http.Request) {
	req := &domain.ListResourcesRequest{
		Type:   r.URL.Query().Get("type"),
		Status: r.URL.Query().Get("status"),
		Limit:  100, // Default limit
		Offset: 0,   // Default offset
	}

	ctx := r.Context()
	resp, err := h.registry.ListResources(ctx, req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// GetResource handles GET /api/v2/resources/{id}
func (h *Handlers) GetResource(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	resourceID := vars["id"]

	req := &domain.GetResourceRequest{
		ResourceID: resourceID,
	}

	ctx := r.Context()
	resp, err := h.registry.GetResource(ctx, req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// UpdateResource handles PUT /api/v2/resources/{id}
func (h *Handlers) UpdateResource(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	resourceID := vars["id"]

	var req domain.UpdateResourceRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}
	req.ResourceID = resourceID

	ctx := r.Context()
	resp, err := h.registry.UpdateResource(ctx, &req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// DeleteResource handles DELETE /api/v2/resources/{id}
func (h *Handlers) DeleteResource(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	resourceID := vars["id"]

	req := &domain.DeleteResourceRequest{
		ResourceID: resourceID,
		Force:      r.URL.Query().Get("force") == "true",
	}

	ctx := r.Context()
	resp, err := h.registry.DeleteResource(ctx, req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// CreateExperiment handles POST /api/v2/experiments
func (h *Handlers) CreateExperiment(w http.ResponseWriter, r *http.Request) {
	var req domain.CreateExperimentRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	// Extract user ID from context
	userID := ""
	if userIDVal := r.Context().Value(types.UserIDKey); userIDVal != nil {
		if id, ok := userIDVal.(string); ok {
			userID = id
		}
	}
	if userID == "" {
		userID = "anonymous" // Or return 401 Unauthorized
	}

	ctx := r.Context()
	resp, err := h.orchestrator.CreateExperiment(ctx, &req, userID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// ListExperiments handles GET /api/v2/experiments
func (h *Handlers) ListExperiments(w http.ResponseWriter, r *http.Request) {
	req := &domain.ListExperimentsRequest{
		ProjectID: r.URL.Query().Get("projectId"),
		OwnerID:   r.URL.Query().Get("ownerId"),
		Status:    r.URL.Query().Get("status"),
		Limit:     100, // Default limit
		Offset:    0,   // Default offset
	}

	ctx := r.Context()
	resp, err := h.orchestrator.ListExperiments(ctx, req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// GetExperiment handles GET /api/v2/experiments/{id}
func (h *Handlers) GetExperiment(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	req := &domain.GetExperimentRequest{
		ExperimentID: experimentID,
		IncludeTasks: r.URL.Query().Get("includeTasks") == "true",
	}

	ctx := r.Context()
	resp, err := h.orchestrator.GetExperiment(ctx, req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// UpdateExperiment handles PUT /api/v2/experiments/{id}
func (h *Handlers) UpdateExperiment(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	var req domain.UpdateExperimentRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}
	req.ExperimentID = experimentID

	ctx := r.Context()
	resp, err := h.orchestrator.UpdateExperiment(ctx, &req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// DeleteExperiment handles DELETE /api/v2/experiments/{id}
func (h *Handlers) DeleteExperiment(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	req := &domain.DeleteExperimentRequest{
		ExperimentID: experimentID,
		Force:        r.URL.Query().Get("force") == "true",
	}

	ctx := r.Context()
	resp, err := h.orchestrator.DeleteExperiment(ctx, req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// SubmitExperiment handles POST /api/v2/experiments/{id}/submit
func (h *Handlers) SubmitExperiment(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	var req domain.SubmitExperimentRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}
	req.ExperimentID = experimentID

	ctx := r.Context()
	resp, err := h.orchestrator.SubmitExperiment(ctx, &req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(resp)
}

// GenerateTasks handles POST /api/v2/experiments/{id}/tasks
func (h *Handlers) GenerateTasks(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	ctx := r.Context()
	tasks, err := h.orchestrator.GenerateTasks(ctx, experimentID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"tasks":   tasks,
		"success": true,
	})
}

// ScheduleExperiment handles POST /api/v2/experiments/{id}/schedule
func (h *Handlers) ScheduleExperiment(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	ctx := r.Context()
	plan, err := h.scheduler.ScheduleExperiment(ctx, experimentID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(plan)
}

// AssignTask handles POST /api/v2/workers/{id}/assign
func (h *Handlers) AssignTask(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	workerID := vars["id"]

	ctx := r.Context()
	task, err := h.scheduler.AssignTask(ctx, workerID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"task":    task,
		"success": true,
	})
}

// CompleteTask handles POST /api/v2/tasks/{id}/complete
func (h *Handlers) CompleteTask(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	taskID := vars["id"]

	var req struct {
		WorkerID string             `json:"workerId"`
		Result   *domain.TaskResult `json:"result"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	err := h.scheduler.CompleteTask(ctx, taskID, req.WorkerID, req.Result)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
	})
}

// FailTask handles POST /api/v2/tasks/{id}/fail
func (h *Handlers) FailTask(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	taskID := vars["id"]

	var req struct {
		WorkerID string `json:"workerId"`
		Error    string `json:"error"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	err := h.scheduler.FailTask(ctx, taskID, req.WorkerID, req.Error)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
	})
}

// GetWorkerStatus handles GET /api/v2/workers/{id}/status
func (h *Handlers) GetWorkerStatus(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	workerID := vars["id"]

	ctx := r.Context()
	status, err := h.scheduler.GetWorkerStatus(ctx, workerID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(status)
}

// SpawnWorker handles POST /api/v2/workers
func (h *Handlers) SpawnWorker(w http.ResponseWriter, r *http.Request) {
	var req struct {
		ComputeResourceID string        `json:"computeResourceId"`
		ExperimentID      string        `json:"experimentId"`
		Walltime          time.Duration `json:"walltime"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	worker, err := h.worker.SpawnWorker(ctx, req.ComputeResourceID, req.ExperimentID, req.Walltime)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"worker":  worker,
		"success": true,
	})
}

// RegisterWorker handles POST /api/v2/workers/{id}/register
func (h *Handlers) RegisterWorker(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	workerID := vars["id"]

	// Parse worker from request body
	var workerReq struct {
		ComputeResourceID string            `json:"compute_resource_id"`
		Capabilities      map[string]string `json:"capabilities"`
		Metadata          map[string]string `json:"metadata"`
	}
	if err := json.NewDecoder(r.Body).Decode(&workerReq); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	worker := &domain.Worker{
		ID:                workerID,
		ComputeResourceID: workerReq.ComputeResourceID,
		Status:            domain.WorkerStatusIdle,
		CreatedAt:         time.Now(),
		UpdatedAt:         time.Now(),
		Metadata:          convertStringMapToInterfaceMap(workerReq.Metadata),
	}

	ctx := r.Context()
	err := h.worker.RegisterWorker(ctx, worker)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
	})
}

// StartWorkerPolling handles POST /api/v2/workers/{id}/start-polling
func (h *Handlers) StartWorkerPolling(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	workerID := vars["id"]

	ctx := r.Context()
	err := h.worker.StartWorkerPolling(ctx, workerID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
	})
}

// StopWorkerPolling handles POST /api/v2/workers/{id}/stop-polling
func (h *Handlers) StopWorkerPolling(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	workerID := vars["id"]

	ctx := r.Context()
	err := h.worker.StopWorkerPolling(ctx, workerID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
	})
}

// TerminateWorker handles POST /api/v2/workers/{id}/terminate
func (h *Handlers) TerminateWorker(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	workerID := vars["id"]

	var req struct {
		Reason string `json:"reason"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	err := h.worker.TerminateWorker(ctx, workerID, req.Reason)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
	})
}

// SendHeartbeat handles POST /api/v2/workers/{id}/heartbeat
func (h *Handlers) SendHeartbeat(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	workerID := vars["id"]

	var req struct {
		Metrics *domain.WorkerMetrics `json:"metrics"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	err := h.worker.SendHeartbeat(ctx, workerID, req.Metrics)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
	})
}

// HealthCheck handles GET /api/v2/health
func (h *Handlers) HealthCheck(w http.ResponseWriter, r *http.Request) {
	health := map[string]interface{}{
		"status":    "healthy",
		"timestamp": time.Now(),
		"version":   "2.0.0",
		"services": map[string]string{
			"registry":     "healthy",
			"vault":        "healthy",
			"orchestrator": "healthy",
			"scheduler":    "healthy",
			"datamover":    "healthy",
			"worker":       "healthy",
		},
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(health)
}

// Credential vault HTTP endpoints

func (h *Handlers) StoreCredential(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	var req struct {
		Name            string `json:"name"`
		Type            string `json:"type"`
		Data            string `json:"data"`
		EncryptionKeyID string `json:"encryption_key_id"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	credential, err := h.vault.StoreCredential(ctx, req.Name, domain.CredentialType(req.Type), []byte(req.Data), userID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(credential)
}

func (h *Handlers) RetrieveCredential(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	vars := mux.Vars(r)
	credentialID := vars["id"]

	ctx := r.Context()
	credential, data, err := h.vault.RetrieveCredential(ctx, credentialID, userID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	response := struct {
		Credential *domain.Credential `json:"credential"`
		Data       string             `json:"data"`
	}{
		Credential: credential,
		Data:       string(data),
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

func (h *Handlers) UpdateCredential(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	vars := mux.Vars(r)
	credentialID := vars["id"]

	var req struct {
		Data string `json:"data"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	credential, err := h.vault.UpdateCredential(ctx, credentialID, []byte(req.Data), userID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(credential)
}

func (h *Handlers) DeleteCredential(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	vars := mux.Vars(r)
	credentialID := vars["id"]

	ctx := r.Context()
	if err := h.vault.DeleteCredential(ctx, credentialID, userID); err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (h *Handlers) ListCredentials(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	ctx := r.Context()
	credentials, err := h.vault.ListCredentials(ctx, userID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(credentials)
}

// ===== Advanced Experiment API Handlers =====

// SearchExperiments handles advanced experiment search
func (h *Handlers) SearchExperiments(w http.ResponseWriter, r *http.Request) {
	// Parse query parameters
	query := r.URL.Query()

	req := &types.ExperimentSearchRequest{
		Pagination: types.PaginationRequest{
			Limit:  10,
			Offset: 0,
		},
	}

	// Parse pagination
	if limitStr := query.Get("limit"); limitStr != "" {
		if limit, err := strconv.Atoi(limitStr); err == nil {
			req.Pagination.Limit = limit
		}
	}
	if offsetStr := query.Get("offset"); offsetStr != "" {
		if offset, err := strconv.Atoi(offsetStr); err == nil {
			req.Pagination.Offset = offset
		}
	}

	// Parse filters
	if projectID := query.Get("project_id"); projectID != "" {
		req.ProjectID = projectID
	}
	if ownerID := query.Get("owner_id"); ownerID != "" {
		req.OwnerID = ownerID
	}
	if status := query.Get("status"); status != "" {
		req.Status = status
	}
	if parameterFilter := query.Get("parameter_filter"); parameterFilter != "" {
		req.ParameterFilter = parameterFilter
	}
	if tags := query.Get("tags"); tags != "" {
		req.Tags = []string{tags} // Simple implementation
	}
	if sortBy := query.Get("sort_by"); sortBy != "" {
		req.SortBy = sortBy
	}
	if order := query.Get("order"); order != "" {
		req.Order = order
	}

	// Parse date filters
	if createdAfter := query.Get("created_after"); createdAfter != "" {
		if t, err := time.Parse(time.RFC3339, createdAfter); err == nil {
			req.CreatedAfter = &t
		}
	}
	if createdBefore := query.Get("created_before"); createdBefore != "" {
		if t, err := time.Parse(time.RFC3339, createdBefore); err == nil {
			req.CreatedBefore = &t
		}
	}

	// Call analytics service (this would need to be implemented in the repository)
	// For now, return a placeholder response
	response := &types.ExperimentSearchResponse{
		Experiments: []types.ExperimentSummary{},
		TotalCount:  0,
		Pagination: types.PaginationResponse{
			Limit:      req.Pagination.Limit,
			Offset:     req.Pagination.Offset,
			TotalCount: 0,
			HasMore:    false,
		},
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

// GetExperimentSummary handles experiment summary requests
func (h *Handlers) GetExperimentSummary(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	ctx := r.Context()
	summary, err := h.analytics.GetExperimentSummary(ctx, experimentID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(summary)
}

// GetFailedTasks handles failed task extraction requests
func (h *Handlers) GetFailedTasks(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	ctx := r.Context()
	failedTasks, err := h.analytics.GetFailedTasks(ctx, experimentID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(failedTasks)
}

// GetTaskAggregation handles task aggregation requests
func (h *Handlers) GetTaskAggregation(w http.ResponseWriter, r *http.Request) {
	query := r.URL.Query()

	req := &types.TaskAggregationRequest{
		GroupBy: "status", // Default grouping
	}

	if experimentID := query.Get("experiment_id"); experimentID != "" {
		req.ExperimentID = experimentID
	}
	if groupBy := query.Get("group_by"); groupBy != "" {
		req.GroupBy = groupBy
	}
	if filter := query.Get("filter"); filter != "" {
		req.Filter = filter
	}

	ctx := r.Context()
	response, err := h.analytics.GetTaskAggregation(ctx, req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

// GetExperimentTimeline handles experiment timeline requests
func (h *Handlers) GetExperimentTimeline(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	ctx := r.Context()
	timeline, err := h.analytics.GetExperimentTimeline(ctx, experimentID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(timeline)
}

// CreateDerivativeExperiment handles derivative experiment creation
func (h *Handlers) CreateDerivativeExperiment(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	var req types.DerivativeExperimentRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	req.SourceExperimentID = experimentID

	ctx := r.Context()
	response, err := h.experiment.CreateDerivativeExperiment(ctx, &req)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

// GetExperimentProgress handles experiment progress requests
func (h *Handlers) GetExperimentProgress(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	ctx := r.Context()
	progress, err := h.experiment.GetExperimentProgress(ctx, experimentID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(progress)
}

// GetTaskProgress handles task progress requests
func (h *Handlers) GetTaskProgress(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	taskID := vars["id"]

	ctx := r.Context()
	progress, err := h.experiment.GetTaskProgress(ctx, taskID)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(progress)
}

// ===== Output Collection Handlers =====

// ListExperimentOutputs handles GET /api/v2/experiments/{id}/outputs
func (h *Handlers) ListExperimentOutputs(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	ctx := r.Context()

	// Get experiment outputs from datamover service
	outputs, err := h.datamover.ListExperimentOutputs(ctx, experimentID)
	if err != nil {
		http.Error(w, fmt.Sprintf("failed to list experiment outputs: %v", err), http.StatusInternalServerError)
		return
	}

	// Group outputs by task for easy identification
	response := map[string]interface{}{
		"experimentId": experimentID,
		"outputs":      outputs,
		"totalFiles":   len(outputs),
		"timestamp":    time.Now(),
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

// DownloadExperimentOutputs handles GET /api/v2/experiments/{id}/outputs/download
func (h *Handlers) DownloadExperimentOutputs(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]

	ctx := r.Context()

	// Get experiment output archive from datamover service
	archiveReader, err := h.datamover.GetExperimentOutputArchive(ctx, experimentID)
	if err != nil {
		http.Error(w, fmt.Sprintf("failed to create experiment output archive: %v", err), http.StatusInternalServerError)
		return
	}
	if closer, ok := archiveReader.(io.Closer); ok {
		defer closer.Close()
	}

	// Set headers for file download
	filename := fmt.Sprintf("experiment_%s_outputs.tar.gz", experimentID)
	w.Header().Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", filename))
	w.Header().Set("Content-Type", "application/gzip")

	// Stream the archive to the client
	_, err = io.Copy(w, archiveReader)
	if err != nil {
		http.Error(w, fmt.Sprintf("failed to stream archive: %v", err), http.StatusInternalServerError)
		return
	}
}

// DownloadExperimentOutputFile handles GET /api/v2/experiments/{id}/outputs/{task_id}/{filename}
func (h *Handlers) DownloadExperimentOutputFile(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	experimentID := vars["id"]
	taskID := vars["task_id"]
	filename := vars["filename"]

	ctx := r.Context()

	// Construct the file path
	filePath := fmt.Sprintf("/experiments/%s/outputs/%s/%s", experimentID, taskID, filename)

	// Get file from storage
	fileReader, err := h.datamover.GetFile(ctx, filePath)
	if err != nil {
		http.Error(w, fmt.Sprintf("failed to get file: %v", err), http.StatusNotFound)
		return
	}
	if closer, ok := fileReader.(io.Closer); ok {
		defer closer.Close()
	}

	// Set headers for file download
	w.Header().Set("Content-Disposition", fmt.Sprintf("attachment; filename=\"%s\"", filename))
	w.Header().Set("Content-Type", "application/octet-stream")

	// Stream the file to the client
	_, err = io.Copy(w, fileReader)
	if err != nil {
		http.Error(w, fmt.Sprintf("failed to stream file: %v", err), http.StatusInternalServerError)
		return
	}
}

// ===== Monitoring & Observability Handlers =====

// DetailedHealthCheck handles detailed health check requests
func (h *Handlers) DetailedHealthCheck(w http.ResponseWriter, r *http.Request) {
	// This would integrate with the health service
	health := map[string]interface{}{
		"status":    "healthy",
		"timestamp": time.Now(),
		"components": map[string]interface{}{
			"database": map[string]interface{}{
				"status":  "healthy",
				"latency": "5ms",
			},
			"scheduler": map[string]interface{}{
				"status":     "healthy",
				"last_cycle": time.Now().Add(-30 * time.Second),
			},
			"workers": map[string]interface{}{
				"total":  5,
				"active": 3,
				"idle":   2,
			},
		},
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(health)
}

// Metrics handles Prometheus metrics requests
func (h *Handlers) Metrics(w http.ResponseWriter, r *http.Request) {
	// This would integrate with the metrics service
	metrics := `# HELP scheduler_experiments_total Total number of experiments
# TYPE scheduler_experiments_total counter
scheduler_experiments_total{status="created"} 10
scheduler_experiments_total{status="running"} 5
scheduler_experiments_total{status="completed"} 25

# HELP scheduler_tasks_total Total number of tasks
# TYPE scheduler_tasks_total counter
scheduler_tasks_total{status="completed"} 150
scheduler_tasks_total{status="failed"} 5

# HELP scheduler_workers_active Active workers
# TYPE scheduler_workers_active gauge
scheduler_workers_active{compute_resource="cluster1"} 3
`

	w.Header().Set("Content-Type", "text/plain")
	w.Write([]byte(metrics))
}

// ServeWorkerBinary serves the worker binary for download
func (h *Handlers) ServeWorkerBinary(w http.ResponseWriter, r *http.Request) {
	// Serve the worker binary from configured path
	workerPath := h.config.BinaryPath
	if workerPath == "" {
		workerPath = "./build/worker"
	}

	// Set appropriate headers for binary download
	w.Header().Set("Content-Type", "application/octet-stream")
	w.Header().Set("Content-Disposition", "attachment; filename=worker")

	http.ServeFile(w, r, workerPath)
}

// convertStringMapToInterfaceMap converts map[string]string to map[string]interface{}
func convertStringMapToInterfaceMap(stringMap map[string]string) map[string]interface{} {
	interfaceMap := make(map[string]interface{})
	for k, v := range stringMap {
		interfaceMap[k] = v
	}
	return interfaceMap
}

// ===== Authentication Handlers =====

// Login handles POST /api/v2/auth/login
func (h *Handlers) Login(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Username string `json:"username"`
		Password string `json:"password"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()

	// Get user by username
	user, err := h.getUserByUsername(ctx, req.Username)
	if err != nil {
		http.Error(w, "Invalid credentials", http.StatusUnauthorized)
		return
	}

	// Verify password
	valid, err := h.verifyPassword(ctx, req.Password, user.PasswordHash)
	if err != nil || !valid {
		http.Error(w, "Invalid credentials", http.StatusUnauthorized)
		return
	}

	// Generate JWT token
	token, err := h.generateToken(ctx, user)
	if err != nil {
		http.Error(w, "Failed to generate token", http.StatusInternalServerError)
		return
	}

	response := map[string]interface{}{
		"token":     token,
		"user":      user,
		"expiresIn": 3600, // 1 hour
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

// Logout handles POST /api/v2/auth/logout
func (h *Handlers) Logout(w http.ResponseWriter, r *http.Request) {
	// In a production system, you would add the token to a blacklist
	// For now, just return success
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
		"message": "Logged out successfully",
	})
}

// RefreshToken handles POST /api/v2/auth/refresh
func (h *Handlers) RefreshToken(w http.ResponseWriter, r *http.Request) {
	var req struct {
		Token string `json:"token"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()

	// Validate existing token
	claims, err := h.validateToken(ctx, req.Token)
	if err != nil {
		http.Error(w, "Invalid token", http.StatusUnauthorized)
		return
	}

	// Get user
	userID, ok := claims["user_id"].(string)
	if !ok {
		http.Error(w, "Invalid token claims", http.StatusUnauthorized)
		return
	}

	user, err := h.getUserByID(ctx, userID)
	if err != nil {
		http.Error(w, "User not found", http.StatusUnauthorized)
		return
	}

	// Generate new token
	newToken, err := h.generateToken(ctx, user)
	if err != nil {
		http.Error(w, "Failed to generate token", http.StatusInternalServerError)
		return
	}

	response := map[string]interface{}{
		"token":     newToken,
		"expiresIn": 3600, // 1 hour
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(response)
}

// ===== User Self-Service Handlers =====

// GetUserProfile handles GET /api/v2/user/profile
func (h *Handlers) GetUserProfile(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	ctx := r.Context()
	user, err := h.getUserByID(ctx, userID)
	if err != nil {
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Remove sensitive information
	user.PasswordHash = ""

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(user)
}

// UpdateUserProfile handles PUT /api/v2/user/profile
func (h *Handlers) UpdateUserProfile(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	var req struct {
		FullName string `json:"fullName"`
		Email    string `json:"email"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	user, err := h.getUserByID(ctx, userID)
	if err != nil {
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Update fields
	if req.FullName != "" {
		user.FullName = req.FullName
	}
	if req.Email != "" {
		user.Email = req.Email
	}

	if err := h.updateUser(ctx, user); err != nil {
		http.Error(w, "Failed to update profile", http.StatusInternalServerError)
		return
	}

	// Remove sensitive information
	user.PasswordHash = ""

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(user)
}

// ChangePassword handles PUT /api/v2/user/password
func (h *Handlers) ChangePassword(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	var req struct {
		OldPassword string `json:"oldPassword"`
		NewPassword string `json:"newPassword"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	user, err := h.getUserByID(ctx, userID)
	if err != nil {
		http.Error(w, "User not found", http.StatusNotFound)
		return
	}

	// Verify old password
	valid, err := h.verifyPassword(ctx, req.OldPassword, user.PasswordHash)
	if err != nil || !valid {
		http.Error(w, "Invalid old password", http.StatusBadRequest)
		return
	}

	// Hash new password
	newHash, err := h.hashPassword(ctx, req.NewPassword)
	if err != nil {
		http.Error(w, "Failed to hash password", http.StatusInternalServerError)
		return
	}

	user.PasswordHash = newHash
	if err := h.updateUser(ctx, user); err != nil {
		http.Error(w, "Failed to update password", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
		"message": "Password updated successfully",
	})
}

// GetUserGroups handles GET /api/v2/user/groups
func (h *Handlers) GetUserGroups(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	ctx := r.Context()
	groups, err := h.getUserGroups(ctx, userID)
	if err != nil {
		http.Error(w, "Failed to get user groups", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(groups)
}

// GetUserProjects handles GET /api/v2/user/projects
func (h *Handlers) GetUserProjects(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	ctx := r.Context()
	projects, err := h.getUserProjects(ctx, userID)
	if err != nil {
		http.Error(w, "Failed to get user projects", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(projects)
}

// ===== Project Handlers =====

// CreateProject handles POST /api/v2/projects
func (h *Handlers) CreateProject(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	var req struct {
		Name        string `json:"name"`
		Description string `json:"description"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	project, err := h.createProject(ctx, userID, req.Name, req.Description)
	if err != nil {
		http.Error(w, "Failed to create project", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(project)
}

// ListProjects handles GET /api/v2/projects
func (h *Handlers) ListProjects(w http.ResponseWriter, r *http.Request) {
	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	ctx := r.Context()
	projects, err := h.getUserProjects(ctx, userID)
	if err != nil {
		http.Error(w, "Failed to list projects", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(projects)
}

// GetProject handles GET /api/v2/projects/{id}
func (h *Handlers) GetProject(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	projectID := vars["id"]

	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	ctx := r.Context()
	project, err := h.getProjectByID(ctx, projectID)
	if err != nil {
		http.Error(w, "Project not found", http.StatusNotFound)
		return
	}

	// Check if user has access to this project
	if project.OwnerID != userID {
		http.Error(w, "Access denied", http.StatusForbidden)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(project)
}

// UpdateProject handles PUT /api/v2/projects/{id}
func (h *Handlers) UpdateProject(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	projectID := vars["id"]

	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	var req struct {
		Name        string `json:"name"`
		Description string `json:"description"`
	}
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "Invalid request body", http.StatusBadRequest)
		return
	}

	ctx := r.Context()
	project, err := h.getProjectByID(ctx, projectID)
	if err != nil {
		http.Error(w, "Project not found", http.StatusNotFound)
		return
	}

	// Check if user has access to this project
	if project.OwnerID != userID {
		http.Error(w, "Access denied", http.StatusForbidden)
		return
	}

	// Update fields
	if req.Name != "" {
		project.Name = req.Name
	}
	if req.Description != "" {
		project.Description = req.Description
	}

	if err := h.updateProject(ctx, project); err != nil {
		http.Error(w, "Failed to update project", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(project)
}

// DeleteProject handles DELETE /api/v2/projects/{id}
func (h *Handlers) DeleteProject(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	projectID := vars["id"]

	userID := getUserIDFromContext(r.Context())
	if userID == "" {
		http.Error(w, "Unauthorized", http.StatusUnauthorized)
		return
	}

	ctx := r.Context()
	project, err := h.getProjectByID(ctx, projectID)
	if err != nil {
		http.Error(w, "Project not found", http.StatusNotFound)
		return
	}

	// Check if user has access to this project
	if project.OwnerID != userID {
		http.Error(w, "Access denied", http.StatusForbidden)
		return
	}

	if err := h.deleteProject(ctx, projectID); err != nil {
		http.Error(w, "Failed to delete project", http.StatusInternalServerError)
		return
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(map[string]interface{}{
		"success": true,
		"message": "Project deleted successfully",
	})
}

// RegisterComputeResource handles POST /api/v1/compute/register
func (h *Handlers) RegisterComputeResource(w http.ResponseWriter, r *http.Request) {
	var registrationData struct {
		Token        string                 `json:"token"`
		Name         string                 `json:"name"`
		Type         string                 `json:"type"`
		Hostname     string                 `json:"hostname"`
		Capabilities map[string]interface{} `json:"capabilities"`
		PrivateKey   string                 `json:"private_key"`
	}

	if err := json.NewDecoder(r.Body).Decode(&registrationData); err != nil {
		http.Error(w, "Invalid JSON", http.StatusBadRequest)
		return
	}

	// Validate required fields
	if registrationData.Token == "" {
		http.Error(w, "Token is required", http.StatusBadRequest)
		return
	}
	if registrationData.Name == "" {
		http.Error(w, "Name is required", http.StatusBadRequest)
		return
	}
	if registrationData.Type == "" {
		http.Error(w, "Type is required", http.StatusBadRequest)
		return
	}
	if registrationData.Hostname == "" {
		http.Error(w, "Hostname is required", http.StatusBadRequest)
		return
	}
	if registrationData.PrivateKey == "" {
		http.Error(w, "Private key is required", http.StatusBadRequest)
		return
	}

	ctx := r.Context()

	// Validate token and get user ID and resource ID
	userID, resourceID, err := h.validateRegistrationToken(ctx, registrationData.Token)
	if err != nil {
		http.Error(w, "Invalid or expired token", http.StatusUnauthorized)
		return
	}

	// Get the existing resource that was created with the token
	resource, err := h.registry.GetResource(ctx, &domain.GetResourceRequest{
		ResourceID: resourceID,
	})
	if err != nil {
		http.Error(w, fmt.Sprintf("Failed to get existing resource: %v", err), http.StatusInternalServerError)
		return
	}

	// Cast to compute resource
	computeResource, ok := resource.Resource.(*domain.ComputeResource)
	if !ok {
		http.Error(w, "Resource is not a compute resource", http.StatusInternalServerError)
		return
	}

	// Update resource with discovered capabilities
	if registrationData.Capabilities != nil {
		if computeResource.Metadata == nil {
			computeResource.Metadata = make(map[string]interface{})
		}
		// Merge discovered capabilities into resource metadata
		for key, value := range registrationData.Capabilities {
			computeResource.Metadata[key] = value
		}

		// Update the resource in the database
		updateReq := &domain.UpdateResourceRequest{
			ResourceID: computeResource.ID,
			Metadata:   computeResource.Metadata,
		}
		if _, err := h.registry.UpdateResource(ctx, updateReq); err != nil {
			http.Error(w, fmt.Sprintf("Failed to update resource with capabilities: %v", err), http.StatusInternalServerError)
			return
		}
	}

	// Store SSH private key as credential
	credential, err := h.vault.StoreCredential(ctx, computeResource.ID+"-ssh-key", domain.CredentialTypeSSHKey, []byte(registrationData.PrivateKey), userID)
	if err != nil {
		// Clean up the resource if credential storage fails
		// TODO: Implement resource deletion in registry
		// h.registry.DeleteComputeResource(ctx, computeResource.ID)
		http.Error(w, fmt.Sprintf("Failed to store credentials: %v", err), http.StatusInternalServerError)
		return
	}

	// Bind credential to resource
	if err := h.bindCredentialToResource(ctx, credential.ID, computeResource.ID, "compute_resource"); err != nil {
		// Clean up if binding fails
		// TODO: Implement resource deletion in registry
		// h.registry.DeleteComputeResource(ctx, computeResource.ID)
		h.vault.DeleteCredential(ctx, credential.ID, userID)
		http.Error(w, fmt.Sprintf("Failed to bind credential: %v", err), http.StatusInternalServerError)
		return
	}

	// Activate the resource
	if err := h.activateComputeResource(ctx, computeResource.ID); err != nil {
		http.Error(w, fmt.Sprintf("Failed to activate resource: %v", err), http.StatusInternalServerError)
		return
	}

	// Return success response
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(map[string]interface{}{
		"id":     computeResource.ID,
		"name":   computeResource.Name,
		"type":   computeResource.Type,
		"status": "active",
	})
}

// handleTokenBasedRegistration handles token-based registration from CLI
func (h *Handlers) handleTokenBasedRegistration(ctx context.Context, registrationData struct {
	Token        string                 `json:"token"`
	Name         string                 `json:"name"`
	Type         string                 `json:"type"`
	Hostname     string                 `json:"hostname"`
	Capabilities map[string]interface{} `json:"capabilities"`
	PrivateKey   string                 `json:"private_key"`
}) (map[string]interface{}, error) {
	// Validate required fields
	if registrationData.Token == "" {
		return nil, fmt.Errorf("token is required")
	}
	if registrationData.Name == "" {
		return nil, fmt.Errorf("name is required")
	}
	if registrationData.Type == "" {
		return nil, fmt.Errorf("type is required")
	}
	if registrationData.Hostname == "" {
		return nil, fmt.Errorf("hostname is required")
	}
	if registrationData.PrivateKey == "" {
		return nil, fmt.Errorf("private key is required")
	}

	// Validate token and get user ID and resource ID
	userID, resourceID, err := h.validateRegistrationToken(ctx, registrationData.Token)
	if err != nil {
		return nil, fmt.Errorf("invalid or expired token: %w", err)
	}

	// Get the existing resource that was created with the token
	resource, err := h.registry.GetResource(ctx, &domain.GetResourceRequest{
		ResourceID: resourceID,
	})
	if err != nil {
		return nil, fmt.Errorf("failed to get existing resource: %w", err)
	}

	// Cast to compute resource
	computeResource, ok := resource.Resource.(*domain.ComputeResource)
	if !ok {
		return nil, fmt.Errorf("resource is not a compute resource")
	}

	// Update resource with discovered capabilities
	if registrationData.Capabilities != nil {
		if computeResource.Metadata == nil {
			computeResource.Metadata = make(map[string]interface{})
		}
		// Merge discovered capabilities into resource metadata
		for key, value := range registrationData.Capabilities {
			computeResource.Metadata[key] = value
		}

		// Update the resource in the database
		updateReq := &domain.UpdateResourceRequest{
			ResourceID: computeResource.ID,
			Metadata:   computeResource.Metadata,
		}
		if _, err := h.registry.UpdateResource(ctx, updateReq); err != nil {
			return nil, fmt.Errorf("failed to update resource with capabilities: %w", err)
		}
	}

	// Store SSH private key as credential
	credential, err := h.vault.StoreCredential(ctx, computeResource.ID+"-ssh-key", domain.CredentialTypeSSHKey, []byte(registrationData.PrivateKey), userID)
	if err != nil {
		// Clean up the resource if credential storage fails
		// TODO: Implement resource deletion in registry
		// h.registry.DeleteComputeResource(ctx, computeResource.ID)
		return nil, fmt.Errorf("failed to store credentials: %w", err)
	}

	// Bind credential to resource
	if err := h.bindCredentialToResource(ctx, credential.ID, computeResource.ID, "compute_resource"); err != nil {
		// Clean up if binding fails
		// TODO: Implement resource deletion in registry
		// h.registry.DeleteComputeResource(ctx, computeResource.ID)
		h.vault.DeleteCredential(ctx, credential.ID, userID)
		return nil, fmt.Errorf("failed to bind credential: %w", err)
	}

	// Activate the resource
	if err := h.activateComputeResource(ctx, computeResource.ID); err != nil {
		return nil, fmt.Errorf("failed to activate resource: %w", err)
	}

	// Return success response
	return map[string]interface{}{
		"id":     computeResource.ID,
		"name":   computeResource.Name,
		"type":   computeResource.Type,
		"status": "active",
	}, nil
}

// ===== Helper Functions =====

// Helper functions that would need to be implemented with actual database calls
// These are placeholder implementations that would need to be connected to the actual repository

func (h *Handlers) getUserByUsername(ctx context.Context, username string) (*domain.User, error) {
	// This would need to be implemented with actual database calls
	// For now, return a placeholder
	return nil, fmt.Errorf("not implemented")
}

func (h *Handlers) getUserByID(ctx context.Context, userID string) (*domain.User, error) {
	// This would need to be implemented with actual database calls
	// For now, return a placeholder
	return nil, fmt.Errorf("not implemented")
}

func (h *Handlers) updateUser(ctx context.Context, user *domain.User) error {
	// This would need to be implemented with actual database calls
	// For now, return a placeholder
	return fmt.Errorf("not implemented")
}

func (h *Handlers) verifyPassword(ctx context.Context, password, hash string) (bool, error) {
	// This would need to be implemented with actual password verification
	// For now, return a placeholder
	return false, fmt.Errorf("not implemented")
}

func (h *Handlers) hashPassword(ctx context.Context, password string) (string, error) {
	// This would need to be implemented with actual password hashing
	// For now, return a placeholder
	return "", fmt.Errorf("not implemented")
}

func (h *Handlers) generateToken(ctx context.Context, user *domain.User) (string, error) {
	// This would need to be implemented with actual JWT generation
	// For now, return a placeholder
	return "", fmt.Errorf("not implemented")
}

func (h *Handlers) validateToken(ctx context.Context, token string) (map[string]interface{}, error) {
	// This would need to be implemented with actual JWT validation
	// For now, return a placeholder
	return nil, fmt.Errorf("not implemented")
}

func (h *Handlers) getUserGroups(ctx context.Context, userID string) ([]*domain.Group, error) {
	// This would need to be implemented with actual database calls
	// For now, return a placeholder
	return nil, fmt.Errorf("not implemented")
}

func (h *Handlers) getUserProjects(ctx context.Context, userID string) ([]*domain.Project, error) {
	// This would need to be implemented with actual database calls
	// For now, return a placeholder
	return nil, fmt.Errorf("not implemented")
}

func (h *Handlers) createProject(ctx context.Context, ownerID, name, description string) (*domain.Project, error) {
	// This would need to be implemented with actual database calls
	// For now, return a placeholder
	return nil, fmt.Errorf("not implemented")
}

func (h *Handlers) getProjectByID(ctx context.Context, projectID string) (*domain.Project, error) {
	// This would need to be implemented with actual database calls
	// For now, return a placeholder
	return nil, fmt.Errorf("not implemented")
}

func (h *Handlers) updateProject(ctx context.Context, project *domain.Project) error {
	// This would need to be implemented with actual database calls
	// For now, return a placeholder
	return fmt.Errorf("not implemented")
}

func (h *Handlers) deleteProject(ctx context.Context, projectID string) error {
	// This would need to be implemented with actual database calls
	// For now, return a placeholder
	return fmt.Errorf("not implemented")
}

// validateRegistrationToken validates a registration token and returns the user ID and resource ID
func (h *Handlers) validateRegistrationToken(ctx context.Context, token string) (string, string, error) {
	if token == "" {
		return "", "", fmt.Errorf("empty token")
	}

	// Validate the token using the repository
	regToken, err := h.repository.ValidateRegistrationToken(ctx, token)
	if err != nil {
		return "", "", fmt.Errorf("invalid token: %w", err)
	}

	// Check if token is expired
	if time.Now().After(regToken.ExpiresAt) {
		return "", "", fmt.Errorf("token expired")
	}

	// Check if token has already been used
	if regToken.UsedAt != nil {
		return "", "", fmt.Errorf("token already used")
	}

	// Mark token as used
	err = h.repository.MarkTokenAsUsed(ctx, token)
	if err != nil {
		return "", "", fmt.Errorf("failed to mark token as used: %w", err)
	}

	return regToken.UserID, regToken.ResourceID, nil
}

// bindCredentialToResource binds a credential to a resource using SpiceDB
func (h *Handlers) bindCredentialToResource(ctx context.Context, credentialID, resourceID, resourceType string) error {
	fmt.Printf("Binding credential %s to resource %s of type %s\n", credentialID, resourceID, resourceType)

	// Use the vault service to bind the credential to the resource
	if h.vault == nil {
		return fmt.Errorf("vault service not available")
	}

	err := h.vault.BindCredentialToResource(ctx, credentialID, resourceID, resourceType)
	if err != nil {
		return fmt.Errorf("failed to bind credential to resource in SpiceDB: %w", err)
	}

	fmt.Printf("Successfully bound credential %s to resource %s\n", credentialID, resourceID)
	return nil
}

// activateComputeResource activates a compute resource
func (h *Handlers) activateComputeResource(ctx context.Context, resourceID string) error {
	fmt.Printf("DEBUG: Activating compute resource %s\n", resourceID)

	// Update the resource status to "active" using the repository
	err := h.repository.UpdateComputeResourceStatus(ctx, resourceID, domain.ResourceStatusActive)
	if err != nil {
		fmt.Printf("DEBUG: Failed to activate compute resource %s: %v\n", resourceID, err)
		return fmt.Errorf("failed to activate compute resource: %w", err)
	}

	fmt.Printf("DEBUG: Successfully activated compute resource %s\n", resourceID)
	return nil
}
