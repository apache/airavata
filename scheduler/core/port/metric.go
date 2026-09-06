package ports

import (
	"context"
	"time"
)

// MetricsPort defines the interface for metrics collection and monitoring
// This abstracts metrics implementations from domain services
type MetricsPort interface {
	// Counter operations
	IncrementCounter(ctx context.Context, name string, labels map[string]string) error
	AddToCounter(ctx context.Context, name string, value float64, labels map[string]string) error
	GetCounter(ctx context.Context, name string, labels map[string]string) (float64, error)

	// Gauge operations
	SetGauge(ctx context.Context, name string, value float64, labels map[string]string) error
	AddToGauge(ctx context.Context, name string, value float64, labels map[string]string) error
	GetGauge(ctx context.Context, name string, labels map[string]string) (float64, error)

	// Histogram operations
	ObserveHistogram(ctx context.Context, name string, value float64, labels map[string]string) error
	GetHistogram(ctx context.Context, name string, labels map[string]string) (*HistogramStats, error)

	// Summary operations
	ObserveSummary(ctx context.Context, name string, value float64, labels map[string]string) error
	GetSummary(ctx context.Context, name string, labels map[string]string) (*SummaryStats, error)

	// Timer operations
	StartTimer(ctx context.Context, name string, labels map[string]string) Timer
	RecordDuration(ctx context.Context, name string, duration time.Duration, labels map[string]string) error

	// Custom metrics
	RecordCustomMetric(ctx context.Context, metric *CustomMetric) error
	GetCustomMetric(ctx context.Context, name string, labels map[string]string) (*CustomMetric, error)

	// Health checks
	RecordHealthCheck(ctx context.Context, name string, status HealthStatus, details map[string]interface{}) error
	GetHealthChecks(ctx context.Context) ([]*HealthCheck, error)

	// Connection management
	Connect(ctx context.Context) error
	Disconnect(ctx context.Context) error
	IsConnected() bool
	Ping(ctx context.Context) error

	// Configuration
	GetConfig() *MetricsConfig
	GetStats(ctx context.Context) (*MetricsStats, error)
}

// Timer defines the interface for timing operations
type Timer interface {
	Stop() time.Duration
	Record() error
}

// HistogramStats represents histogram statistics
type HistogramStats struct {
	Count   int64            `json:"count"`
	Sum     float64          `json:"sum"`
	Min     float64          `json:"min"`
	Max     float64          `json:"max"`
	Mean    float64          `json:"mean"`
	Median  float64          `json:"median"`
	P95     float64          `json:"p95"`
	P99     float64          `json:"p99"`
	Buckets map[string]int64 `json:"buckets"`
}

// SummaryStats represents summary statistics
type SummaryStats struct {
	Count  int64   `json:"count"`
	Sum    float64 `json:"sum"`
	Min    float64 `json:"min"`
	Max    float64 `json:"max"`
	Mean   float64 `json:"mean"`
	Median float64 `json:"median"`
	P95    float64 `json:"p95"`
	P99    float64 `json:"p99"`
}

// CustomMetric represents a custom metric
type CustomMetric struct {
	Name      string                 `json:"name"`
	Type      MetricType             `json:"type"`
	Value     float64                `json:"value"`
	Labels    map[string]string      `json:"labels"`
	Timestamp time.Time              `json:"timestamp"`
	Metadata  map[string]interface{} `json:"metadata,omitempty"`
}

// MetricType represents the type of metric
type MetricType string

const (
	MetricTypeCounter   MetricType = "counter"
	MetricTypeGauge     MetricType = "gauge"
	MetricTypeHistogram MetricType = "histogram"
	MetricTypeSummary   MetricType = "summary"
	MetricTypeTimer     MetricType = "timer"
)

// HealthStatus represents health status
type HealthStatus string

const (
	HealthStatusHealthy   HealthStatus = "healthy"
	HealthStatusUnhealthy HealthStatus = "unhealthy"
	HealthStatusDegraded  HealthStatus = "degraded"
	HealthStatusUnknown   HealthStatus = "unknown"
)

// HealthCheck represents a health check
type HealthCheck struct {
	Name      string                 `json:"name"`
	Status    HealthStatus           `json:"status"`
	Message   string                 `json:"message,omitempty"`
	Details   map[string]interface{} `json:"details,omitempty"`
	Timestamp time.Time              `json:"timestamp"`
	Duration  time.Duration          `json:"duration"`
}

// MetricsConfig represents metrics configuration
type MetricsConfig struct {
	Type                 string            `json:"type"`
	Endpoint             string            `json:"endpoint"`
	PushGatewayURL       string            `json:"pushGatewayUrl,omitempty"`
	JobName              string            `json:"jobName"`
	InstanceID           string            `json:"instanceId"`
	PushInterval         time.Duration     `json:"pushInterval"`
	CollectInterval      time.Duration     `json:"collectInterval"`
	Timeout              time.Duration     `json:"timeout"`
	MaxRetries           int               `json:"maxRetries"`
	EnableGoMetrics      bool              `json:"enableGoMetrics"`
	EnableProcessMetrics bool              `json:"enableProcessMetrics"`
	EnableRuntimeMetrics bool              `json:"enableRuntimeMetrics"`
	CustomLabels         map[string]string `json:"customLabels"`
}

// MetricsStats represents metrics system statistics
type MetricsStats struct {
	TotalMetrics  int64         `json:"totalMetrics"`
	ActiveMetrics int64         `json:"activeMetrics"`
	MetricsPushed int64         `json:"metricsPushed"`
	PushErrors    int64         `json:"pushErrors"`
	LastPush      time.Time     `json:"lastPush"`
	Uptime        time.Duration `json:"uptime"`
	ErrorRate     float64       `json:"errorRate"`
	Throughput    float64       `json:"throughput"`
}

// MetricsCollector defines the interface for collecting metrics
type MetricsCollector interface {
	Collect(ctx context.Context) ([]*CustomMetric, error)
	GetName() string
	GetInterval() time.Duration
	Start(ctx context.Context) error
	Stop(ctx context.Context) error
}

// MetricsExporter defines the interface for exporting metrics
type MetricsExporter interface {
	Export(ctx context.Context, metrics []*CustomMetric) error
	GetName() string
	GetFormat() string
	IsEnabled() bool
}

// MetricsAggregator defines the interface for aggregating metrics
type MetricsAggregator interface {
	Aggregate(ctx context.Context, metrics []*CustomMetric) ([]*CustomMetric, error)
	GetName() string
	GetAggregationRules() []*AggregationRule
}

// AggregationRule represents a metric aggregation rule
type AggregationRule struct {
	Name        string            `json:"name"`
	SourceNames []string          `json:"sourceNames"`
	Operation   AggregationOp     `json:"operation"`
	Labels      map[string]string `json:"labels"`
	Interval    time.Duration     `json:"interval"`
}

// AggregationOp represents aggregation operations
type AggregationOp string

const (
	AggregationOpSum   AggregationOp = "sum"
	AggregationOpAvg   AggregationOp = "avg"
	AggregationOpMin   AggregationOp = "min"
	AggregationOpMax   AggregationOp = "max"
	AggregationOpCount AggregationOp = "count"
	AggregationOpLast  AggregationOp = "last"
	AggregationOpFirst AggregationOp = "first"
)

// MetricsAlert defines the interface for metrics alerting
type MetricsAlert interface {
	Check(ctx context.Context, metric *CustomMetric) (*Alert, error)
	GetName() string
	GetConditions() []*AlertCondition
	IsEnabled() bool
}

// Alert represents a metrics alert
type Alert struct {
	Name       string                 `json:"name"`
	Severity   AlertSeverity          `json:"severity"`
	Status     AlertStatus            `json:"status"`
	Message    string                 `json:"message"`
	Metric     *CustomMetric          `json:"metric"`
	Condition  *AlertCondition        `json:"condition"`
	Details    map[string]interface{} `json:"details,omitempty"`
	Timestamp  time.Time              `json:"timestamp"`
	ResolvedAt *time.Time             `json:"resolvedAt,omitempty"`
}

// AlertSeverity represents alert severity levels
type AlertSeverity string

const (
	AlertSeverityInfo     AlertSeverity = "info"
	AlertSeverityWarning  AlertSeverity = "warning"
	AlertSeverityCritical AlertSeverity = "critical"
)

// AlertStatus represents alert status
type AlertStatus string

const (
	AlertStatusFiring   AlertStatus = "firing"
	AlertStatusResolved AlertStatus = "resolved"
	AlertStatusSilenced AlertStatus = "silenced"
)

// AlertCondition represents an alert condition
type AlertCondition struct {
	Name      string            `json:"name"`
	Metric    string            `json:"metric"`
	Operator  AlertOp           `json:"operator"`
	Threshold float64           `json:"threshold"`
	Duration  time.Duration     `json:"duration"`
	Labels    map[string]string `json:"labels"`
}

// AlertOp represents alert operators
type AlertOp string

const (
	AlertOpGreaterThan        AlertOp = ">"
	AlertOpGreaterThanOrEqual AlertOp = ">="
	AlertOpLessThan           AlertOp = "<"
	AlertOpLessThanOrEqual    AlertOp = "<="
	AlertOpEqual              AlertOp = "=="
	AlertOpNotEqual           AlertOp = "!="
)
