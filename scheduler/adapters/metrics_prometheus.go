package adapters

import (
	"context"
	"fmt"
	"time"

	ports "github.com/apache/airavata/scheduler/core/port"
	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
)

// PrometheusAdapter implements ports.MetricsPort using Prometheus
type PrometheusAdapter struct {
	// Counters
	experimentCreatedCounter   prometheus.Counter
	experimentCompletedCounter prometheus.Counter
	experimentFailedCounter    prometheus.Counter
	taskCreatedCounter         prometheus.Counter
	taskCompletedCounter       prometheus.Counter
	taskFailedCounter          prometheus.Counter
	workerSpawnedCounter       prometheus.Counter
	workerTerminatedCounter    prometheus.Counter
	dataTransferCounter        prometheus.Counter
	cacheHitCounter            prometheus.Counter
	cacheMissCounter           prometheus.Counter

	// Gauges
	activeExperimentsGauge prometheus.Gauge
	activeTasksGauge       prometheus.Gauge
	activeWorkersGauge     prometheus.Gauge
	queueLengthGauge       prometheus.Gauge
	cacheSizeGauge         prometheus.Gauge
	storageUsageGauge      prometheus.Gauge

	// Histograms
	experimentDurationHistogram   prometheus.Histogram
	taskDurationHistogram         prometheus.Histogram
	dataTransferSizeHistogram     prometheus.Histogram
	dataTransferDurationHistogram prometheus.Histogram
	apiRequestDurationHistogram   prometheus.Histogram

	// Summaries
	workerUtilizationSummary prometheus.Summary
	cpuUsageSummary          prometheus.Summary
	memoryUsageSummary       prometheus.Summary

	// Start time for uptime calculation
	startTime time.Time
}

// NewPrometheusAdapter creates a new Prometheus metrics adapter
func NewPrometheusAdapter() *PrometheusAdapter {
	return &PrometheusAdapter{
		// Counters
		experimentCreatedCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_experiments_created_total",
			Help: "Total number of experiments created",
		}),
		experimentCompletedCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_experiments_completed_total",
			Help: "Total number of experiments completed",
		}),
		experimentFailedCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_experiments_failed_total",
			Help: "Total number of experiments failed",
		}),
		taskCreatedCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_tasks_created_total",
			Help: "Total number of tasks created",
		}),
		taskCompletedCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_tasks_completed_total",
			Help: "Total number of tasks completed",
		}),
		taskFailedCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_tasks_failed_total",
			Help: "Total number of tasks failed",
		}),
		workerSpawnedCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_workers_spawned_total",
			Help: "Total number of workers spawned",
		}),
		workerTerminatedCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_workers_terminated_total",
			Help: "Total number of workers terminated",
		}),
		dataTransferCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_data_transfers_total",
			Help: "Total number of data transfers",
		}),
		cacheHitCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_cache_hits_total",
			Help: "Total number of cache hits",
		}),
		cacheMissCounter: promauto.NewCounter(prometheus.CounterOpts{
			Name: "airavata_cache_misses_total",
			Help: "Total number of cache misses",
		}),

		// Gauges
		activeExperimentsGauge: promauto.NewGauge(prometheus.GaugeOpts{
			Name: "airavata_active_experiments",
			Help: "Number of active experiments",
		}),
		activeTasksGauge: promauto.NewGauge(prometheus.GaugeOpts{
			Name: "airavata_active_tasks",
			Help: "Number of active tasks",
		}),
		activeWorkersGauge: promauto.NewGauge(prometheus.GaugeOpts{
			Name: "airavata_active_workers",
			Help: "Number of active workers",
		}),
		queueLengthGauge: promauto.NewGauge(prometheus.GaugeOpts{
			Name: "airavata_queue_length",
			Help: "Length of the task queue",
		}),
		cacheSizeGauge: promauto.NewGauge(prometheus.GaugeOpts{
			Name: "airavata_cache_size_bytes",
			Help: "Size of the cache in bytes",
		}),
		storageUsageGauge: promauto.NewGauge(prometheus.GaugeOpts{
			Name: "airavata_storage_usage_bytes",
			Help: "Storage usage in bytes",
		}),

		// Histograms
		experimentDurationHistogram: promauto.NewHistogram(prometheus.HistogramOpts{
			Name:    "airavata_experiment_duration_seconds",
			Help:    "Duration of experiments in seconds",
			Buckets: prometheus.ExponentialBuckets(1, 2, 10),
		}),
		taskDurationHistogram: promauto.NewHistogram(prometheus.HistogramOpts{
			Name:    "airavata_task_duration_seconds",
			Help:    "Duration of tasks in seconds",
			Buckets: prometheus.ExponentialBuckets(0.1, 2, 10),
		}),
		dataTransferSizeHistogram: promauto.NewHistogram(prometheus.HistogramOpts{
			Name:    "airavata_data_transfer_size_bytes",
			Help:    "Size of data transfers in bytes",
			Buckets: prometheus.ExponentialBuckets(1024, 2, 20),
		}),
		dataTransferDurationHistogram: promauto.NewHistogram(prometheus.HistogramOpts{
			Name:    "airavata_data_transfer_duration_seconds",
			Help:    "Duration of data transfers in seconds",
			Buckets: prometheus.ExponentialBuckets(0.1, 2, 10),
		}),
		apiRequestDurationHistogram: promauto.NewHistogram(prometheus.HistogramOpts{
			Name:    "airavata_api_request_duration_seconds",
			Help:    "Duration of API requests in seconds",
			Buckets: prometheus.ExponentialBuckets(0.001, 2, 10),
		}),

		// Summaries
		workerUtilizationSummary: promauto.NewSummary(prometheus.SummaryOpts{
			Name: "airavata_worker_utilization_ratio",
			Help: "Worker utilization ratio",
		}),
		cpuUsageSummary: promauto.NewSummary(prometheus.SummaryOpts{
			Name: "airavata_cpu_usage_percent",
			Help: "CPU usage percentage",
		}),
		memoryUsageSummary: promauto.NewSummary(prometheus.SummaryOpts{
			Name: "airavata_memory_usage_percent",
			Help: "Memory usage percentage",
		}),
		startTime: time.Now(),
	}
}

// Compile-time interface verification
var _ ports.MetricsPort = (*PrometheusAdapter)(nil)

// Connect connects to the metrics system
func (p *PrometheusAdapter) Connect(ctx context.Context) error {
	// Prometheus metrics don't require explicit connection
	return nil
}

// Disconnect disconnects from the metrics system
func (p *PrometheusAdapter) Disconnect(ctx context.Context) error {
	// Prometheus metrics don't require explicit disconnection
	return nil
}

// IsConnected checks if connected to the metrics system
func (p *PrometheusAdapter) IsConnected() bool {
	// Prometheus metrics are always available
	return true
}

// Ping pings the metrics system
func (p *PrometheusAdapter) Ping(ctx context.Context) error {
	// Prometheus metrics are always available
	return nil
}

// IncrementCounter increments a counter metric
func (p *PrometheusAdapter) IncrementCounter(ctx context.Context, name string, labels map[string]string) error {
	switch name {
	case "experiments_created":
		p.experimentCreatedCounter.Inc()
	case "experiments_completed":
		p.experimentCompletedCounter.Inc()
	case "experiments_failed":
		p.experimentFailedCounter.Inc()
	case "tasks_created":
		p.taskCreatedCounter.Inc()
	case "tasks_completed":
		p.taskCompletedCounter.Inc()
	case "tasks_failed":
		p.taskFailedCounter.Inc()
	case "workers_spawned":
		p.workerSpawnedCounter.Inc()
	case "workers_terminated":
		p.workerTerminatedCounter.Inc()
	case "data_transfers":
		p.dataTransferCounter.Inc()
	case "cache_hits":
		p.cacheHitCounter.Inc()
	case "cache_misses":
		p.cacheMissCounter.Inc()
	default:
		return fmt.Errorf("unknown counter metric: %s", name)
	}
	return nil
}

// AddToCounter adds a value to a counter metric
func (p *PrometheusAdapter) AddToCounter(ctx context.Context, name string, value float64, labels map[string]string) error {
	switch name {
	case "experiments_created":
		p.experimentCreatedCounter.Add(value)
	case "experiments_completed":
		p.experimentCompletedCounter.Add(value)
	case "experiments_failed":
		p.experimentFailedCounter.Add(value)
	case "tasks_created":
		p.taskCreatedCounter.Add(value)
	case "tasks_completed":
		p.taskCompletedCounter.Add(value)
	case "tasks_failed":
		p.taskFailedCounter.Add(value)
	case "workers_spawned":
		p.workerSpawnedCounter.Add(value)
	case "workers_terminated":
		p.workerTerminatedCounter.Add(value)
	case "data_transfers":
		p.dataTransferCounter.Add(value)
	case "cache_hits":
		p.cacheHitCounter.Add(value)
	case "cache_misses":
		p.cacheMissCounter.Add(value)
	default:
		return fmt.Errorf("unknown counter metric: %s", name)
	}
	return nil
}

// SetGauge sets a gauge metric value
func (p *PrometheusAdapter) SetGauge(ctx context.Context, name string, value float64, labels map[string]string) error {
	switch name {
	case "active_experiments":
		p.activeExperimentsGauge.Set(value)
	case "active_tasks":
		p.activeTasksGauge.Set(value)
	case "active_workers":
		p.activeWorkersGauge.Set(value)
	case "queue_length":
		p.queueLengthGauge.Set(value)
	case "cache_size":
		p.cacheSizeGauge.Set(value)
	case "storage_usage":
		p.storageUsageGauge.Set(value)
	default:
		return fmt.Errorf("unknown gauge metric: %s", name)
	}
	return nil
}

// AddToGauge adds a value to a gauge metric
func (p *PrometheusAdapter) AddToGauge(ctx context.Context, name string, value float64, labels map[string]string) error {
	switch name {
	case "active_experiments":
		p.activeExperimentsGauge.Add(value)
	case "active_tasks":
		p.activeTasksGauge.Add(value)
	case "active_workers":
		p.activeWorkersGauge.Add(value)
	case "queue_length":
		p.queueLengthGauge.Add(value)
	case "cache_size":
		p.cacheSizeGauge.Add(value)
	case "storage_usage":
		p.storageUsageGauge.Add(value)
	default:
		return fmt.Errorf("unknown gauge metric: %s", name)
	}
	return nil
}

// ObserveHistogram observes a value in a histogram metric
func (p *PrometheusAdapter) ObserveHistogram(ctx context.Context, name string, value float64, labels map[string]string) error {
	switch name {
	case "experiment_duration":
		p.experimentDurationHistogram.Observe(value)
	case "task_duration":
		p.taskDurationHistogram.Observe(value)
	case "data_transfer_size":
		p.dataTransferSizeHistogram.Observe(value)
	case "data_transfer_duration":
		p.dataTransferDurationHistogram.Observe(value)
	case "api_request_duration":
		p.apiRequestDurationHistogram.Observe(value)
	default:
		return fmt.Errorf("unknown histogram metric: %s", name)
	}
	return nil
}

// ObserveSummary observes a value in a summary metric
func (p *PrometheusAdapter) ObserveSummary(ctx context.Context, name string, value float64, labels map[string]string) error {
	switch name {
	case "worker_utilization":
		p.workerUtilizationSummary.Observe(value)
	case "cpu_usage":
		p.cpuUsageSummary.Observe(value)
	case "memory_usage":
		p.memoryUsageSummary.Observe(value)
	default:
		return fmt.Errorf("unknown summary metric: %s", name)
	}
	return nil
}

// RecordDuration records the duration of an operation
func (p *PrometheusAdapter) RecordDuration(ctx context.Context, name string, duration time.Duration, labels map[string]string) error {
	return p.ObserveHistogram(ctx, name, duration.Seconds(), labels)
}

// RecordSize records the size of data
func (p *PrometheusAdapter) RecordSize(ctx context.Context, name string, size int64, labels map[string]string) error {
	return p.ObserveHistogram(ctx, name, float64(size), labels)
}

// RecordRate records a rate value
func (p *PrometheusAdapter) RecordRate(ctx context.Context, name string, rate float64, labels map[string]string) error {
	return p.ObserveSummary(ctx, name, rate, labels)
}

// GetCounter gets the current value of a counter metric
func (p *PrometheusAdapter) GetCounter(ctx context.Context, name string, labels map[string]string) (float64, error) {
	// Prometheus doesn't provide a direct way to get counter values
	// This would typically be done by querying the Prometheus server
	return 0, fmt.Errorf("counter values must be queried from Prometheus server")
}

// GetGauge gets the current value of a gauge metric
func (p *PrometheusAdapter) GetGauge(ctx context.Context, name string, labels map[string]string) (float64, error) {
	// Prometheus doesn't provide a direct way to get gauge values
	// This would typically be done by querying the Prometheus server
	return 0, fmt.Errorf("gauge values must be queried from Prometheus server")
}

// GetHistogram gets statistics for a histogram metric
func (p *PrometheusAdapter) GetHistogram(ctx context.Context, name string, labels map[string]string) (*ports.HistogramStats, error) {
	// Prometheus doesn't provide a direct way to get histogram stats
	// This would typically be done by querying the Prometheus server
	return nil, fmt.Errorf("histogram stats must be queried from Prometheus server")
}

// GetSummary gets statistics for a summary metric
func (p *PrometheusAdapter) GetSummary(ctx context.Context, name string, labels map[string]string) (*ports.SummaryStats, error) {
	// Prometheus doesn't provide a direct way to get summary stats
	// This would typically be done by querying the Prometheus server
	return nil, fmt.Errorf("summary stats must be queried from Prometheus server")
}

// ListMetrics lists all available metrics
func (p *PrometheusAdapter) ListMetrics(ctx context.Context) ([]string, error) {
	return []string{
		"experiments_created",
		"experiments_completed",
		"experiments_failed",
		"tasks_created",
		"tasks_completed",
		"tasks_failed",
		"workers_spawned",
		"workers_terminated",
		"data_transfers",
		"cache_hits",
		"cache_misses",
		"active_experiments",
		"active_tasks",
		"active_workers",
		"queue_length",
		"cache_size",
		"storage_usage",
		"experiment_duration",
		"task_duration",
		"data_transfer_size",
		"data_transfer_duration",
		"api_request_duration",
		"worker_utilization",
		"cpu_usage",
		"memory_usage",
	}, nil
}

// StartTimer starts a timer for measuring duration
func (p *PrometheusAdapter) StartTimer(ctx context.Context, name string, labels map[string]string) ports.Timer {
	return &prometheusTimer{
		start: time.Now(),
		name:  name,
		port:  p,
	}
}

// RecordCustomMetric records a custom metric
func (p *PrometheusAdapter) RecordCustomMetric(ctx context.Context, metric *ports.CustomMetric) error {
	// Convert custom metric to appropriate Prometheus metric type
	switch metric.Type {
	case ports.MetricTypeCounter:
		return p.AddToCounter(ctx, metric.Name, metric.Value, metric.Labels)
	case ports.MetricTypeGauge:
		return p.SetGauge(ctx, metric.Name, metric.Value, metric.Labels)
	case ports.MetricTypeHistogram:
		return p.ObserveHistogram(ctx, metric.Name, metric.Value, metric.Labels)
	case ports.MetricTypeSummary:
		return p.ObserveSummary(ctx, metric.Name, metric.Value, metric.Labels)
	default:
		return fmt.Errorf("unsupported metric type: %s", metric.Type)
	}
}

// GetCustomMetric gets a custom metric
func (p *PrometheusAdapter) GetCustomMetric(ctx context.Context, name string, labels map[string]string) (*ports.CustomMetric, error) {
	// Prometheus doesn't provide a direct way to get custom metrics
	// This would typically be done by querying the Prometheus server
	return nil, fmt.Errorf("custom metrics must be queried from Prometheus server")
}

// RecordHealthCheck records a health check
func (p *PrometheusAdapter) RecordHealthCheck(ctx context.Context, name string, status ports.HealthStatus, details map[string]interface{}) error {
	// Record health check as a gauge metric
	value := 0.0
	switch status {
	case ports.HealthStatusHealthy:
		value = 1.0
	case ports.HealthStatusUnhealthy:
		value = 0.0
	case ports.HealthStatusDegraded:
		value = 0.5
	case ports.HealthStatusUnknown:
		value = -1.0
	}

	labels := map[string]string{"name": name}
	return p.SetGauge(ctx, "health_check", value, labels)
}

// GetHealthChecks gets all health checks
func (p *PrometheusAdapter) GetHealthChecks(ctx context.Context) ([]*ports.HealthCheck, error) {
	// Prometheus doesn't provide a direct way to get health checks
	// This would typically be done by querying the Prometheus server
	return nil, fmt.Errorf("health checks must be queried from Prometheus server")
}

// GetConfig gets the metrics configuration
func (p *PrometheusAdapter) GetConfig() *ports.MetricsConfig {
	return &ports.MetricsConfig{
		Type:                 "prometheus",
		Endpoint:             "http://prometheus:9090", // Use service name for container-to-container communication
		PushGatewayURL:       "http://prometheus:9091", // Use service name for container-to-container communication
		JobName:              "airavata-scheduler",
		InstanceID:           "instance-1",
		PushInterval:         15 * time.Second,
		CollectInterval:      15 * time.Second,
		Timeout:              5 * time.Second,
		MaxRetries:           3,
		EnableGoMetrics:      true,
		EnableProcessMetrics: true,
		EnableRuntimeMetrics: true,
		CustomLabels: map[string]string{
			"service": "airavata-scheduler",
			"version": "1.0.0",
		},
	}
}

// GetStats gets metrics system statistics
func (p *PrometheusAdapter) GetStats(ctx context.Context) (*ports.MetricsStats, error) {
	return &ports.MetricsStats{
		TotalMetrics:  25, // Number of metrics we defined
		ActiveMetrics: 25,
		MetricsPushed: 0, // Prometheus doesn't push, it scrapes
		PushErrors:    0,
		LastPush:      time.Now(),
		Uptime:        time.Since(p.startTime), // Real uptime
		ErrorRate:     0.0,
		Throughput:    100.0,
	}, nil
}

// HealthCheck performs a health check on the metrics system
func (p *PrometheusAdapter) HealthCheck(ctx context.Context) error {
	// Prometheus metrics are always available
	return nil
}

// Close closes the metrics port
func (p *PrometheusAdapter) Close() error {
	// Prometheus metrics don't need explicit cleanup
	return nil
}

// prometheusTimer implements the ports.Timer interface
type prometheusTimer struct {
	start time.Time
	name  string
	port  *PrometheusAdapter
}

// Stop stops the timer and returns the duration
func (t *prometheusTimer) Stop() time.Duration {
	return time.Since(t.start)
}

// Record records the timer duration as a metric
func (t *prometheusTimer) Record() error {
	duration := t.Stop()
	return t.port.RecordDuration(context.Background(), t.name, duration, nil)
}
