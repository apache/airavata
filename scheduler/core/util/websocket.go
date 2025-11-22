package types

import (
	"time"
)

// WebSocketMessageType represents the type of WebSocket message
type WebSocketMessageType string

const (
	// Experiment-related message types
	WebSocketMessageTypeExperimentCreated   WebSocketMessageType = "experiment_created"
	WebSocketMessageTypeExperimentUpdated   WebSocketMessageType = "experiment_updated"
	WebSocketMessageTypeExperimentProgress  WebSocketMessageType = "experiment_progress"
	WebSocketMessageTypeExperimentCompleted WebSocketMessageType = "experiment_completed"
	WebSocketMessageTypeExperimentFailed    WebSocketMessageType = "experiment_failed"

	// Task-related message types
	WebSocketMessageTypeTaskCreated   WebSocketMessageType = "task_created"
	WebSocketMessageTypeTaskUpdated   WebSocketMessageType = "task_updated"
	WebSocketMessageTypeTaskProgress  WebSocketMessageType = "task_progress"
	WebSocketMessageTypeTaskCompleted WebSocketMessageType = "task_completed"
	WebSocketMessageTypeTaskFailed    WebSocketMessageType = "task_failed"

	// Worker-related message types
	WebSocketMessageTypeWorkerRegistered WebSocketMessageType = "worker_registered"
	WebSocketMessageTypeWorkerUpdated    WebSocketMessageType = "worker_updated"
	WebSocketMessageTypeWorkerOffline    WebSocketMessageType = "worker_offline"

	// System message types
	WebSocketMessageTypeSystemStatus WebSocketMessageType = "system_status"
	WebSocketMessageTypeError        WebSocketMessageType = "error"
	WebSocketMessageTypePing         WebSocketMessageType = "ping"
	WebSocketMessageTypePong         WebSocketMessageType = "pong"
)

// WebSocketMessage represents a WebSocket message
type WebSocketMessage struct {
	Type         WebSocketMessageType `json:"type"`
	ID           string               `json:"id"`
	Timestamp    time.Time            `json:"timestamp"`
	Data         interface{}          `json:"data,omitempty"`
	Error        string               `json:"error,omitempty"`
	ResourceType string               `json:"resourceType,omitempty"`
	ResourceID   string               `json:"resourceId,omitempty"`
	UserID       string               `json:"userId,omitempty"`
}

// WebSocketConnection represents a WebSocket connection
type WebSocketConnection struct {
	ID            string                 `json:"id"`
	UserID        string                 `json:"userId"`
	Subscriptions []string               `json:"subscriptions"` // experiment IDs, project IDs, etc.
	LastPing      time.Time              `json:"lastPing"`
	ConnectedAt   time.Time              `json:"connectedAt"`
	Metadata      map[string]interface{} `json:"metadata,omitempty"`
}

// WebSocketSubscription represents a subscription to specific events
type WebSocketSubscription struct {
	ConnectionID string                 `json:"connectionId"`
	UserID       string                 `json:"userId"`
	ResourceType string                 `json:"resourceType"` // experiment, project, user, system
	ResourceID   string                 `json:"resourceId"`
	EventTypes   []WebSocketMessageType `json:"eventTypes"`
	CreatedAt    time.Time              `json:"createdAt"`
}

// WebSocketRoom represents a room for broadcasting messages
type WebSocketRoom struct {
	ID           string                 `json:"id"`
	ResourceType string                 `json:"resourceType"`
	ResourceID   string                 `json:"resourceId"`
	Connections  map[string]bool        `json:"connections"` // connection ID -> active
	CreatedAt    time.Time              `json:"createdAt"`
	Metadata     map[string]interface{} `json:"metadata,omitempty"`
}

// WebSocketEvent represents an event to be broadcast
type WebSocketEvent struct {
	Type         WebSocketMessageType `json:"type"`
	ResourceType string               `json:"resourceType"`
	ResourceID   string               `json:"resourceId"`
	UserID       string               `json:"userId,omitempty"` // for user-specific events
	Data         interface{}          `json:"data"`
	Timestamp    time.Time            `json:"timestamp"`
	BroadcastTo  []string             `json:"broadcastTo,omitempty"` // specific user IDs or "all"
}

// WebSocketConfig represents WebSocket server configuration
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

// GetDefaultWebSocketConfig returns default WebSocket configuration
func GetDefaultWebSocketConfig() *WebSocketConfig {
	return &WebSocketConfig{
		ReadBufferSize:    1024,
		WriteBufferSize:   1024,
		HandshakeTimeout:  10 * time.Second,
		PingPeriod:        54 * time.Second,
		PongWait:          60 * time.Second,
		WriteWait:         10 * time.Second,
		MaxMessageSize:    512,
		MaxConnections:    1000,
		EnableCompression: true,
	}
}

// WebSocketStats represents WebSocket server statistics
type WebSocketStats struct {
	TotalConnections  int           `json:"totalConnections"`
	ActiveConnections int           `json:"activeConnections"`
	TotalMessages     int64         `json:"totalMessages"`
	MessagesPerSecond float64       `json:"messagesPerSecond"`
	AverageLatency    time.Duration `json:"averageLatency"`
	LastMessageAt     time.Time     `json:"lastMessageAt"`
	Uptime            time.Duration `json:"uptime"`
	ErrorCount        int64         `json:"errorCount"`
	DisconnectCount   int64         `json:"disconnectCount"`
}

// WebSocketClientInfo represents client information for WebSocket connections
type WebSocketClientInfo struct {
	UserAgent   string            `json:"userAgent"`
	IPAddress   string            `json:"ipAddress"`
	RemoteAddr  string            `json:"remoteAddr"`
	RequestURI  string            `json:"requestUri"`
	Headers     map[string]string `json:"headers"`
	ConnectedAt time.Time         `json:"connectedAt"`
}
