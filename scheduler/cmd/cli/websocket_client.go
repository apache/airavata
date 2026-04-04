package main

import (
	"fmt"
	"net/http"
	"net/url"
	"sync"
	"time"

	"github.com/gorilla/websocket"
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
	ResourceType string               `json:"resourceType,omitempty"`
	ResourceID   string               `json:"resourceId,omitempty"`
	UserID       string               `json:"userId,omitempty"`
	Data         interface{}          `json:"data,omitempty"`
	Error        string               `json:"error,omitempty"`
}

// TaskProgress represents task progress information
type TaskProgress struct {
	TaskID          string  `json:"taskId"`
	ExperimentID    string  `json:"experimentId"`
	Progress        float64 `json:"progress"`
	Status          string  `json:"status"`
	Message         string  `json:"message"`
	WorkerID        string  `json:"workerId,omitempty"`
	ComputeResource string  `json:"computeResource,omitempty"`
}

// ExperimentProgress represents experiment progress information
type ExperimentProgress struct {
	ExperimentID   string  `json:"experimentId"`
	TotalTasks     int     `json:"totalTasks"`
	CompletedTasks int     `json:"completedTasks"`
	FailedTasks    int     `json:"failedTasks"`
	RunningTasks   int     `json:"runningTasks"`
	PendingTasks   int     `json:"pendingTasks"`
	Progress       float64 `json:"progress"`
	Status         string  `json:"status"`
}

// WebSocketClient handles WebSocket connections for real-time updates
type WebSocketClient struct {
	conn              *websocket.Conn
	serverURL         string
	token             string
	subscribed        map[string]bool
	messageChan       chan WebSocketMessage
	errorChan         chan error
	done              chan struct{}
	mu                sync.RWMutex
	reconnect         bool
	reconnectInterval time.Duration
}

// NewWebSocketClient creates a new WebSocket client
func NewWebSocketClient(serverURL, token string) *WebSocketClient {
	return &WebSocketClient{
		serverURL:         serverURL,
		token:             token,
		subscribed:        make(map[string]bool),
		messageChan:       make(chan WebSocketMessage, 100),
		errorChan:         make(chan error, 10),
		done:              make(chan struct{}),
		reconnect:         true,
		reconnectInterval: 5 * time.Second,
	}
}

// Connect establishes a WebSocket connection
func (c *WebSocketClient) Connect() error {
	// Parse server URL
	u, err := url.Parse(c.serverURL)
	if err != nil {
		return fmt.Errorf("invalid server URL: %w", err)
	}

	// Convert to WebSocket URL
	if u.Scheme == "https" {
		u.Scheme = "wss"
	} else {
		u.Scheme = "ws"
	}
	u.Path = "/ws"

	// Add authentication header
	headers := http.Header{}
	headers.Set("Authorization", "Bearer "+c.token)

	// Connect to WebSocket
	dialer := websocket.DefaultDialer
	dialer.HandshakeTimeout = 10 * time.Second

	conn, _, err := dialer.Dial(u.String(), headers)
	if err != nil {
		return fmt.Errorf("failed to connect to WebSocket: %w", err)
	}

	c.mu.Lock()
	c.conn = conn
	c.mu.Unlock()

	// Start message handling
	go c.handleMessages()
	go c.keepAlive()

	return nil
}

// Subscribe subscribes to updates for a specific resource
func (c *WebSocketClient) Subscribe(resourceType, resourceID string) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	if c.conn == nil {
		return fmt.Errorf("not connected")
	}

	subscriptionKey := fmt.Sprintf("%s:%s", resourceType, resourceID)
	c.subscribed[subscriptionKey] = true

	// Send subscription message
	message := WebSocketMessage{
		Type:         WebSocketMessageTypeSystemStatus,
		ID:           fmt.Sprintf("sub_%d", time.Now().UnixNano()),
		Timestamp:    time.Now(),
		ResourceType: resourceType,
		ResourceID:   resourceID,
		Data: map[string]interface{}{
			"action": "subscribe",
		},
	}

	return c.conn.WriteJSON(message)
}

// Unsubscribe unsubscribes from updates for a specific resource
func (c *WebSocketClient) Unsubscribe(resourceType, resourceID string) error {
	c.mu.Lock()
	defer c.mu.Unlock()

	if c.conn == nil {
		return fmt.Errorf("not connected")
	}

	subscriptionKey := fmt.Sprintf("%s:%s", resourceType, resourceID)
	delete(c.subscribed, subscriptionKey)

	// Send unsubscription message
	message := WebSocketMessage{
		Type:         WebSocketMessageTypeSystemStatus,
		ID:           fmt.Sprintf("unsub_%d", time.Now().UnixNano()),
		Timestamp:    time.Now(),
		ResourceType: resourceType,
		ResourceID:   resourceID,
		Data: map[string]interface{}{
			"action": "unsubscribe",
		},
	}

	return c.conn.WriteJSON(message)
}

// GetMessageChan returns the message channel
func (c *WebSocketClient) GetMessageChan() <-chan WebSocketMessage {
	return c.messageChan
}

// GetErrorChan returns the error channel
func (c *WebSocketClient) GetErrorChan() <-chan error {
	return c.errorChan
}

// Close closes the WebSocket connection
func (c *WebSocketClient) Close() error {
	c.mu.Lock()
	defer c.mu.Unlock()

	c.reconnect = false
	close(c.done)

	if c.conn != nil {
		return c.conn.Close()
	}
	return nil
}

// IsConnected returns whether the client is connected
func (c *WebSocketClient) IsConnected() bool {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.conn != nil
}

// handleMessages handles incoming WebSocket messages
func (c *WebSocketClient) handleMessages() {
	defer func() {
		c.mu.Lock()
		if c.conn != nil {
			c.conn.Close()
			c.conn = nil
		}
		c.mu.Unlock()
	}()

	for {
		select {
		case <-c.done:
			return
		default:
			c.mu.RLock()
			conn := c.conn
			c.mu.RUnlock()

			if conn == nil {
				if c.reconnect {
					c.reconnectWithBackoff()
					continue
				}
				return
			}

			var message WebSocketMessage
			err := conn.ReadJSON(&message)
			if err != nil {
				if websocket.IsUnexpectedCloseError(err, websocket.CloseGoingAway, websocket.CloseAbnormalClosure) {
					c.errorChan <- fmt.Errorf("WebSocket error: %w", err)
				}

				if c.reconnect {
					c.reconnectWithBackoff()
					continue
				}
				return
			}

			// Handle ping messages
			if message.Type == WebSocketMessageTypePing {
				pong := WebSocketMessage{
					Type:      WebSocketMessageTypePong,
					ID:        fmt.Sprintf("pong_%d", time.Now().UnixNano()),
					Timestamp: time.Now(),
				}
				conn.WriteJSON(pong)
				continue
			}

			// Send message to channel
			select {
			case c.messageChan <- message:
			case <-c.done:
				return
			default:
				// Channel is full, skip message
			}
		}
	}
}

// keepAlive sends periodic ping messages
func (c *WebSocketClient) keepAlive() {
	ticker := time.NewTicker(30 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case <-c.done:
			return
		case <-ticker.C:
			c.mu.RLock()
			conn := c.conn
			c.mu.RUnlock()

			if conn != nil {
				ping := WebSocketMessage{
					Type:      WebSocketMessageTypePing,
					ID:        fmt.Sprintf("ping_%d", time.Now().UnixNano()),
					Timestamp: time.Now(),
				}
				conn.WriteJSON(ping)
			}
		}
	}
}

// reconnectWithBackoff attempts to reconnect with exponential backoff
func (c *WebSocketClient) reconnectWithBackoff() {
	backoff := time.Second
	maxBackoff := 30 * time.Second

	for {
		select {
		case <-c.done:
			return
		case <-time.After(backoff):
			c.mu.Lock()
			if c.conn != nil {
				c.conn.Close()
				c.conn = nil
			}
			c.mu.Unlock()

			err := c.Connect()
			if err == nil {
				// Reconnected successfully, resubscribe to all resources
				c.resubscribe()
				return
			}

			c.errorChan <- fmt.Errorf("reconnection failed: %w", err)

			// Exponential backoff
			backoff *= 2
			if backoff > maxBackoff {
				backoff = maxBackoff
			}
		}
	}
}

// resubscribe resubscribes to all previously subscribed resources
func (c *WebSocketClient) resubscribe() {
	c.mu.RLock()
	subscribed := make(map[string]bool)
	for k, v := range c.subscribed {
		subscribed[k] = v
	}
	c.mu.RUnlock()

	for subscriptionKey := range subscribed {
		// Parse subscription key (format: "resourceType:resourceID")
		parts := splitSubscriptionKey(subscriptionKey)
		if len(parts) == 2 {
			c.Subscribe(parts[0], parts[1])
		}
	}
}

// splitSubscriptionKey splits a subscription key into resource type and ID
func splitSubscriptionKey(key string) []string {
	// Simple implementation - assumes no colons in resource type or ID
	for i := 0; i < len(key); i++ {
		if key[i] == ':' {
			return []string{key[:i], key[i+1:]}
		}
	}
	return []string{key}
}

// ParseTaskProgress parses task progress from WebSocket message
func ParseTaskProgress(message WebSocketMessage) (*TaskProgress, error) {
	if message.Type != WebSocketMessageTypeTaskProgress {
		return nil, fmt.Errorf("not a task progress message")
	}

	data, ok := message.Data.(map[string]interface{})
	if !ok {
		return nil, fmt.Errorf("invalid message data format")
	}

	progress := &TaskProgress{}

	if taskID, ok := data["taskId"].(string); ok {
		progress.TaskID = taskID
	}
	if experimentID, ok := data["experimentId"].(string); ok {
		progress.ExperimentID = experimentID
	}
	if progressVal, ok := data["progress"].(float64); ok {
		progress.Progress = progressVal
	}
	if status, ok := data["status"].(string); ok {
		progress.Status = status
	}
	if message, ok := data["message"].(string); ok {
		progress.Message = message
	}
	if workerID, ok := data["workerId"].(string); ok {
		progress.WorkerID = workerID
	}
	if computeResource, ok := data["computeResource"].(string); ok {
		progress.ComputeResource = computeResource
	}

	return progress, nil
}

// ParseExperimentProgress parses experiment progress from WebSocket message
func ParseExperimentProgress(message WebSocketMessage) (*ExperimentProgress, error) {
	if message.Type != WebSocketMessageTypeExperimentProgress {
		return nil, fmt.Errorf("not an experiment progress message")
	}

	data, ok := message.Data.(map[string]interface{})
	if !ok {
		return nil, fmt.Errorf("invalid message data format")
	}

	progress := &ExperimentProgress{}

	if experimentID, ok := data["experimentId"].(string); ok {
		progress.ExperimentID = experimentID
	}
	if totalTasks, ok := data["totalTasks"].(float64); ok {
		progress.TotalTasks = int(totalTasks)
	}
	if completedTasks, ok := data["completedTasks"].(float64); ok {
		progress.CompletedTasks = int(completedTasks)
	}
	if failedTasks, ok := data["failedTasks"].(float64); ok {
		progress.FailedTasks = int(failedTasks)
	}
	if runningTasks, ok := data["runningTasks"].(float64); ok {
		progress.RunningTasks = int(runningTasks)
	}
	if pendingTasks, ok := data["pendingTasks"].(float64); ok {
		progress.PendingTasks = int(pendingTasks)
	}
	if progressVal, ok := data["progress"].(float64); ok {
		progress.Progress = progressVal
	}
	if status, ok := data["status"].(string); ok {
		progress.Status = status
	}

	return progress, nil
}

// GetStatusColor returns a color code for a status
func GetStatusColor(status string) string {
	switch status {
	case "completed", "success":
		return "green"
	case "failed", "error":
		return "red"
	case "running", "executing":
		return "blue"
	case "pending", "queued":
		return "yellow"
	case "cancelled":
		return "magenta"
	default:
		return "white"
	}
}

// FormatProgressBar creates a text-based progress bar
func FormatProgressBar(progress float64, width int) string {
	if progress < 0 {
		progress = 0
	}
	if progress > 1 {
		progress = 1
	}

	filled := int(progress * float64(width))
	bar := make([]rune, width)

	for i := 0; i < width; i++ {
		if i < filled {
			bar[i] = '█'
		} else {
			bar[i] = '░'
		}
	}

	return fmt.Sprintf("[%s] %.1f%%", string(bar), progress*100)
}
