package services

import (
	"context"
	"time"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"

	"github.com/apache/airavata/scheduler/core/domain"
)

// MetricsService provides Prometheus metrics collection
type MetricsService struct {
	// Experiment metrics
	experimentsTotal   *prometheus.CounterVec
	experimentDuration *prometheus.HistogramVec
	experimentTasks    *prometheus.GaugeVec

	// Task metrics
	tasksTotal   *prometheus.CounterVec
	taskDuration *prometheus.HistogramVec
	taskRetries  *prometheus.CounterVec

	// Worker metrics
	workersActive        *prometheus.GaugeVec
	workerUptime         *prometheus.HistogramVec
	workerTasksCompleted *prometheus.CounterVec

	// API metrics
	apiRequestsTotal   *prometheus.CounterVec
	apiRequestDuration *prometheus.HistogramVec
	apiRequestSize     *prometheus.HistogramVec

	// Data transfer metrics
	dataTransferBytes    *prometheus.CounterVec
	dataTransferDuration *prometheus.HistogramVec

	// Cost metrics
	costTotal   *prometheus.CounterVec
	costPerHour *prometheus.GaugeVec

	// System metrics
	systemUptime      prometheus.Gauge
	systemMemoryUsage prometheus.Gauge
	systemCPUUsage    prometheus.Gauge
	systemDiskUsage   prometheus.Gauge

	// WebSocket metrics
	websocketConnections prometheus.Gauge
	websocketMessages    *prometheus.CounterVec
	websocketLatency     *prometheus.HistogramVec

	// Database metrics
	dbConnections   prometheus.Gauge
	dbQueryDuration *prometheus.HistogramVec
	dbQueryErrors   *prometheus.CounterVec

	startTime time.Time
}

// NewMetricsService creates a new metrics service
func NewMetricsService() *MetricsService {
	ms := &MetricsService{
		startTime: time.Now(),
	}

	// Initialize experiment metrics
	ms.experimentsTotal = promauto.NewCounterVec(
		prometheus.CounterOpts{
			Name: "scheduler_experiments_total",
			Help: "Total number of experiments by status",
		},
		[]string{"status", "project_id"},
	)

	ms.experimentDuration = promauto.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "scheduler_experiment_duration_seconds",
			Help:    "Duration of experiments in seconds",
			Buckets: prometheus.ExponentialBuckets(1, 2, 10), // 1s, 2s, 4s, 8s, 16s, 32s, 64s, 128s, 256s, 512s
		},
		[]string{"status", "project_id"},
	)

	ms.experimentTasks = promauto.NewGaugeVec(
		prometheus.GaugeOpts{
			Name: "scheduler_experiment_tasks",
			Help: "Number of tasks per experiment",
		},
		[]string{"experiment_id", "status"},
	)

	// Initialize task metrics
	ms.tasksTotal = promauto.NewCounterVec(
		prometheus.CounterOpts{
			Name: "scheduler_tasks_total",
			Help: "Total number of tasks by status",
		},
		[]string{"status", "experiment_id", "compute_resource_id"},
	)

	ms.taskDuration = promauto.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "scheduler_task_duration_seconds",
			Help:    "Duration of tasks in seconds",
			Buckets: prometheus.ExponentialBuckets(0.1, 2, 15), // 0.1s to ~54 minutes
		},
		[]string{"status", "compute_resource_id"},
	)

	ms.taskRetries = promauto.NewCounterVec(
		prometheus.CounterOpts{
			Name: "scheduler_task_retries_total",
			Help: "Total number of task retries",
		},
		[]string{"experiment_id", "task_id"},
	)

	// Initialize worker metrics
	ms.workersActive = promauto.NewGaugeVec(
		prometheus.GaugeOpts{
			Name: "scheduler_workers_active",
			Help: "Number of active workers",
		},
		[]string{"compute_resource_id", "status"},
	)

	ms.workerUptime = promauto.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "scheduler_worker_uptime_seconds",
			Help:    "Worker uptime in seconds",
			Buckets: prometheus.ExponentialBuckets(60, 2, 12), // 1min to ~34 hours
		},
		[]string{"compute_resource_id"},
	)

	ms.workerTasksCompleted = promauto.NewCounterVec(
		prometheus.CounterOpts{
			Name: "scheduler_worker_tasks_completed_total",
			Help: "Total number of tasks completed by workers",
		},
		[]string{"worker_id", "compute_resource_id"},
	)

	// Initialize API metrics
	ms.apiRequestsTotal = promauto.NewCounterVec(
		prometheus.CounterOpts{
			Name: "scheduler_api_requests_total",
			Help: "Total number of API requests",
		},
		[]string{"method", "endpoint", "status_code"},
	)

	ms.apiRequestDuration = promauto.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "scheduler_api_request_duration_seconds",
			Help:    "API request duration in seconds",
			Buckets: prometheus.ExponentialBuckets(0.001, 2, 12), // 1ms to ~4 seconds
		},
		[]string{"method", "endpoint"},
	)

	ms.apiRequestSize = promauto.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "scheduler_api_request_size_bytes",
			Help:    "API request size in bytes",
			Buckets: prometheus.ExponentialBuckets(100, 2, 15), // 100B to ~3MB
		},
		[]string{"method", "endpoint"},
	)

	// Initialize data transfer metrics
	ms.dataTransferBytes = promauto.NewCounterVec(
		prometheus.CounterOpts{
			Name: "scheduler_data_transfer_bytes_total",
			Help: "Total bytes transferred",
		},
		[]string{"direction", "storage_type", "compute_resource_id"},
	)

	ms.dataTransferDuration = promauto.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "scheduler_data_transfer_duration_seconds",
			Help:    "Data transfer duration in seconds",
			Buckets: prometheus.ExponentialBuckets(0.1, 2, 15), // 0.1s to ~54 minutes
		},
		[]string{"direction", "storage_type"},
	)

	// Initialize cost metrics
	ms.costTotal = promauto.NewCounterVec(
		prometheus.CounterOpts{
			Name: "scheduler_cost_total",
			Help: "Total cost in currency units",
		},
		[]string{"compute_resource_id", "currency"},
	)

	ms.costPerHour = promauto.NewGaugeVec(
		prometheus.GaugeOpts{
			Name: "scheduler_cost_per_hour",
			Help: "Current cost per hour",
		},
		[]string{"compute_resource_id", "currency"},
	)

	// Initialize system metrics
	ms.systemUptime = promauto.NewGauge(
		prometheus.GaugeOpts{
			Name: "scheduler_system_uptime_seconds",
			Help: "System uptime in seconds",
		},
	)

	ms.systemMemoryUsage = promauto.NewGauge(
		prometheus.GaugeOpts{
			Name: "scheduler_system_memory_usage_bytes",
			Help: "System memory usage in bytes",
		},
	)

	ms.systemCPUUsage = promauto.NewGauge(
		prometheus.GaugeOpts{
			Name: "scheduler_system_cpu_usage_percent",
			Help: "System CPU usage percentage",
		},
	)

	ms.systemDiskUsage = promauto.NewGauge(
		prometheus.GaugeOpts{
			Name: "scheduler_system_disk_usage_bytes",
			Help: "System disk usage in bytes",
		},
	)

	// Initialize WebSocket metrics
	ms.websocketConnections = promauto.NewGauge(
		prometheus.GaugeOpts{
			Name: "scheduler_websocket_connections",
			Help: "Number of active WebSocket connections",
		},
	)

	ms.websocketMessages = promauto.NewCounterVec(
		prometheus.CounterOpts{
			Name: "scheduler_websocket_messages_total",
			Help: "Total number of WebSocket messages",
		},
		[]string{"message_type", "direction"},
	)

	ms.websocketLatency = promauto.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "scheduler_websocket_latency_seconds",
			Help:    "WebSocket message latency in seconds",
			Buckets: prometheus.ExponentialBuckets(0.001, 2, 10), // 1ms to ~1 second
		},
		[]string{"message_type"},
	)

	// Initialize database metrics
	ms.dbConnections = promauto.NewGauge(
		prometheus.GaugeOpts{
			Name: "scheduler_database_connections",
			Help: "Number of active database connections",
		},
	)

	ms.dbQueryDuration = promauto.NewHistogramVec(
		prometheus.HistogramOpts{
			Name:    "scheduler_database_query_duration_seconds",
			Help:    "Database query duration in seconds",
			Buckets: prometheus.ExponentialBuckets(0.001, 2, 12), // 1ms to ~4 seconds
		},
		[]string{"query_type", "table"},
	)

	ms.dbQueryErrors = promauto.NewCounterVec(
		prometheus.CounterOpts{
			Name: "scheduler_database_query_errors_total",
			Help: "Total number of database query errors",
		},
		[]string{"query_type", "table", "error_type"},
	)

	return ms
}

// RecordExperimentCreated records an experiment creation
func (ms *MetricsService) RecordExperimentCreated(experiment *domain.Experiment) {
	ms.experimentsTotal.WithLabelValues(string(experiment.Status), experiment.ProjectID).Inc()
}

// RecordExperimentUpdated records an experiment update
func (ms *MetricsService) RecordExperimentUpdated(experiment *domain.Experiment) {
	ms.experimentsTotal.WithLabelValues(string(experiment.Status), experiment.ProjectID).Inc()
}

// RecordExperimentDuration records experiment duration
func (ms *MetricsService) RecordExperimentDuration(experiment *domain.Experiment, duration time.Duration) {
	ms.experimentDuration.WithLabelValues(string(experiment.Status), experiment.ProjectID).Observe(duration.Seconds())
}

// RecordExperimentTasks records the number of tasks in an experiment
func (ms *MetricsService) RecordExperimentTasks(experimentID string, status string, count float64) {
	ms.experimentTasks.WithLabelValues(experimentID, status).Set(count)
}

// RecordTaskCreated records a task creation
func (ms *MetricsService) RecordTaskCreated(task *domain.Task) {
	ms.tasksTotal.WithLabelValues(string(task.Status), task.ExperimentID, task.ComputeResourceID).Inc()
}

// RecordTaskUpdated records a task update
func (ms *MetricsService) RecordTaskUpdated(task *domain.Task) {
	ms.tasksTotal.WithLabelValues(string(task.Status), task.ExperimentID, task.ComputeResourceID).Inc()
}

// RecordTaskDuration records task duration
func (ms *MetricsService) RecordTaskDuration(task *domain.Task, duration time.Duration) {
	ms.taskDuration.WithLabelValues(string(task.Status), task.ComputeResourceID).Observe(duration.Seconds())
}

// RecordTaskRetry records a task retry
func (ms *MetricsService) RecordTaskRetry(experimentID, taskID string) {
	ms.taskRetries.WithLabelValues(experimentID, taskID).Inc()
}

// RecordWorkerRegistered records a worker registration
func (ms *MetricsService) RecordWorkerRegistered(worker *domain.Worker) {
	ms.workersActive.WithLabelValues(worker.ComputeResourceID, string(worker.Status)).Inc()
}

// RecordWorkerUpdated records a worker update
func (ms *MetricsService) RecordWorkerUpdated(worker *domain.Worker) {
	ms.workersActive.WithLabelValues(worker.ComputeResourceID, string(worker.Status)).Inc()
}

// RecordWorkerUptime records worker uptime
func (ms *MetricsService) RecordWorkerUptime(worker *domain.Worker, uptime time.Duration) {
	ms.workerUptime.WithLabelValues(worker.ComputeResourceID).Observe(uptime.Seconds())
}

// RecordWorkerTaskCompleted records a task completion by a worker
func (ms *MetricsService) RecordWorkerTaskCompleted(worker *domain.Worker) {
	ms.workerTasksCompleted.WithLabelValues(worker.ID, worker.ComputeResourceID).Inc()
}

// RecordAPIRequest records an API request
func (ms *MetricsService) RecordAPIRequest(method, endpoint, statusCode string, duration time.Duration, size int64) {
	ms.apiRequestsTotal.WithLabelValues(method, endpoint, statusCode).Inc()
	ms.apiRequestDuration.WithLabelValues(method, endpoint).Observe(duration.Seconds())
	ms.apiRequestSize.WithLabelValues(method, endpoint).Observe(float64(size))
}

// RecordDataTransfer records a data transfer
func (ms *MetricsService) RecordDataTransfer(direction, storageType, computeResourceID string, bytes int64, duration time.Duration) {
	ms.dataTransferBytes.WithLabelValues(direction, storageType, computeResourceID).Add(float64(bytes))
	ms.dataTransferDuration.WithLabelValues(direction, storageType).Observe(duration.Seconds())
}

// RecordCost records cost information
func (ms *MetricsService) RecordCost(computeResourceID, currency string, cost float64) {
	ms.costTotal.WithLabelValues(computeResourceID, currency).Add(cost)
}

// UpdateCostPerHour updates the current cost per hour
func (ms *MetricsService) UpdateCostPerHour(computeResourceID, currency string, costPerHour float64) {
	ms.costPerHour.WithLabelValues(computeResourceID, currency).Set(costPerHour)
}

// UpdateSystemUptime updates system uptime
func (ms *MetricsService) UpdateSystemUptime() {
	ms.systemUptime.Set(time.Since(ms.startTime).Seconds())
}

// UpdateSystemMemoryUsage updates system memory usage
func (ms *MetricsService) UpdateSystemMemoryUsage(usageBytes int64) {
	ms.systemMemoryUsage.Set(float64(usageBytes))
}

// UpdateSystemCPUUsage updates system CPU usage
func (ms *MetricsService) UpdateSystemCPUUsage(usagePercent float64) {
	ms.systemCPUUsage.Set(usagePercent)
}

// UpdateSystemDiskUsage updates system disk usage
func (ms *MetricsService) UpdateSystemDiskUsage(usageBytes int64) {
	ms.systemDiskUsage.Set(float64(usageBytes))
}

// UpdateWebSocketConnections updates WebSocket connection count
func (ms *MetricsService) UpdateWebSocketConnections(count int) {
	ms.websocketConnections.Set(float64(count))
}

// RecordWebSocketMessage records a WebSocket message
func (ms *MetricsService) RecordWebSocketMessage(messageType, direction string, latency time.Duration) {
	ms.websocketMessages.WithLabelValues(messageType, direction).Inc()
	ms.websocketLatency.WithLabelValues(messageType).Observe(latency.Seconds())
}

// UpdateDatabaseConnections updates database connection count
func (ms *MetricsService) UpdateDatabaseConnections(count int) {
	ms.dbConnections.Set(float64(count))
}

// RecordDatabaseQuery records a database query
func (ms *MetricsService) RecordDatabaseQuery(queryType, table string, duration time.Duration) {
	ms.dbQueryDuration.WithLabelValues(queryType, table).Observe(duration.Seconds())
}

// RecordDatabaseQueryError records a database query error
func (ms *MetricsService) RecordDatabaseQueryError(queryType, table, errorType string) {
	ms.dbQueryErrors.WithLabelValues(queryType, table, errorType).Inc()
}

// GetMetrics returns current metrics summary
func (ms *MetricsService) GetMetrics(ctx context.Context) map[string]interface{} {
	ms.UpdateSystemUptime()

	return map[string]interface{}{
		"system": map[string]interface{}{
			"uptime_seconds": time.Since(ms.startTime).Seconds(),
		},
		"experiments": map[string]interface{}{
			"total": "See prometheus metrics",
		},
		"tasks": map[string]interface{}{
			"total": "See prometheus metrics",
		},
		"workers": map[string]interface{}{
			"active": "See prometheus metrics",
		},
		"api": map[string]interface{}{
			"requests_total": "See prometheus metrics",
		},
		"websocket": map[string]interface{}{
			"connections": "See prometheus metrics",
		},
		"database": map[string]interface{}{
			"connections": "See prometheus metrics",
		},
	}
}
