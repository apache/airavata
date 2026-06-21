package ports

import (
	"context"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
)

// EventPort defines the interface for event publishing and subscription
// This abstracts the event system implementation from domain services
type EventPort interface {
	// Publishing
	Publish(ctx context.Context, event *domain.DomainEvent) error
	PublishBatch(ctx context.Context, events []*domain.DomainEvent) error

	// Subscription
	Subscribe(ctx context.Context, eventType string, handler EventHandler) error
	Unsubscribe(ctx context.Context, eventType string, handler EventHandler) error

	// Connection management
	Connect(ctx context.Context) error
	Disconnect(ctx context.Context) error
	IsConnected() bool

	// Health and monitoring
	GetStats(ctx context.Context) (*EventStats, error)
	Ping(ctx context.Context) error
}

// EventHandler defines the interface for event handlers
type EventHandler interface {
	Handle(ctx context.Context, event *domain.DomainEvent) error
	GetEventType() string
	GetHandlerID() string
}

// EventSubscription represents an event subscription
type EventSubscription struct {
	ID        string    `json:"id"`
	EventType string    `json:"eventType"`
	HandlerID string    `json:"handlerId"`
	CreatedAt time.Time `json:"createdAt"`
	Active    bool      `json:"active"`
}

// EventStats represents event system statistics
type EventStats struct {
	PublishedEvents     int64         `json:"publishedEvents"`
	FailedPublishes     int64         `json:"failedPublishes"`
	ActiveSubscriptions int64         `json:"activeSubscriptions"`
	Uptime              time.Duration `json:"uptime"`
	LastEvent           time.Time     `json:"lastEvent"`
	QueueSize           int64         `json:"queueSize"`
	ErrorRate           float64       `json:"errorRate"`
}

// EventConfig represents event system configuration
type EventConfig struct {
	BrokerURL          string        `json:"brokerUrl"`
	TopicPrefix        string        `json:"topicPrefix"`
	RetryAttempts      int           `json:"retryAttempts"`
	RetryDelay         time.Duration `json:"retryDelay"`
	BatchSize          int           `json:"batchSize"`
	FlushInterval      time.Duration `json:"flushInterval"`
	MaxQueueSize       int64         `json:"maxQueueSize"`
	CompressionEnabled bool          `json:"compressionEnabled"`
}

// WebSocketPort defines the interface for WebSocket connections
// This is a specialized event port for real-time communication
type WebSocketPort interface {
	EventPort

	// WebSocket specific operations
	BroadcastToUser(ctx context.Context, userID string, event *domain.DomainEvent) error
	BroadcastToExperiment(ctx context.Context, experimentID string, event *domain.DomainEvent) error
	BroadcastToProject(ctx context.Context, projectID string, event *domain.DomainEvent) error
	BroadcastToAll(ctx context.Context, event *domain.DomainEvent) error

	// Connection management
	AddConnection(ctx context.Context, conn WebSocketConnection) error
	RemoveConnection(ctx context.Context, connID string) error
	GetConnection(ctx context.Context, connID string) (WebSocketConnection, error)
	GetConnectionsByUser(ctx context.Context, userID string) ([]WebSocketConnection, error)

	// Subscription management
	SubscribeUser(ctx context.Context, connID, userID string) error
	SubscribeExperiment(ctx context.Context, connID, experimentID string) error
	SubscribeProject(ctx context.Context, connID, projectID string) error
	UnsubscribeUser(ctx context.Context, connID, userID string) error
	UnsubscribeExperiment(ctx context.Context, connID, experimentID string) error
	UnsubscribeProject(ctx context.Context, connID, projectID string) error

	// Statistics
	GetConnectionCount(ctx context.Context) (int, error)
	GetUserConnectionCount(ctx context.Context, userID string) (int, error)
}

// WebSocketConnection represents a WebSocket connection
type WebSocketConnection interface {
	GetID() string
	GetUserID() string
	GetIPAddress() string
	GetUserAgent() string
	GetConnectedAt() time.Time
	GetLastActivity() time.Time
	IsAlive() bool
	Send(ctx context.Context, event *domain.DomainEvent) error
	Close(ctx context.Context) error
	Ping(ctx context.Context) error
}

// WebSocketConfig represents WebSocket configuration
type WebSocketConfig struct {
	ReadBufferSize    int           `json:"readBufferSize"`
	WriteBufferSize   int           `json:"writeBufferSize"`
	HandshakeTimeout  time.Duration `json:"handshakeTimeout"`
	PingPeriod        time.Duration `json:"pingPeriod"`
	PongWait          time.Duration `json:"pongWait"`
	WriteWait         time.Duration `json:"writeWait"`
	MaxMessageSize    int64         `json:"maxMessageSize"`
	MaxConnections    int           `json:"maxConnections"`
	EnableCompression bool          `json:"enableCompression"`
}

// EventMiddleware defines the interface for event processing middleware
type EventMiddleware interface {
	Process(ctx context.Context, event *domain.DomainEvent, next EventHandler) error
	GetName() string
	GetPriority() int
}

// EventFilter defines the interface for event filtering
type EventFilter interface {
	ShouldProcess(ctx context.Context, event *domain.DomainEvent) bool
	GetName() string
}

// EventTransformer defines the interface for event transformation
type EventTransformer interface {
	Transform(ctx context.Context, event *domain.DomainEvent) (*domain.DomainEvent, error)
	GetName() string
	GetEventTypes() []string
}

// EventValidator defines the interface for event validation
type EventValidator interface {
	Validate(ctx context.Context, event *domain.DomainEvent) error
	GetName() string
	GetEventTypes() []string
}
