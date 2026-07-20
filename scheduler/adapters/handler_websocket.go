package adapters

import (
	"fmt"
	"net/http"
	"sync"
	"time"

	"github.com/google/uuid"
	"github.com/gorilla/websocket"

	types "github.com/apache/airavata/scheduler/core/util"
)

// WebSocketUpgrader handles WebSocket connection upgrades
type WebSocketUpgrader struct {
	upgrader websocket.Upgrader
	hub      *Hub
	config   *types.WebSocketConfig
}

// NewWebSocketUpgrader creates a new WebSocket upgrader
func NewWebSocketUpgrader(hub *Hub, config *types.WebSocketConfig) *WebSocketUpgrader {
	if config == nil {
		config = types.GetDefaultWebSocketConfig()
	}

	return &WebSocketUpgrader{
		upgrader: websocket.Upgrader{
			ReadBufferSize:  config.ReadBufferSize,
			WriteBufferSize: config.WriteBufferSize,
			CheckOrigin:     checkOrigin,
		},
		hub:    hub,
		config: config,
	}
}

// HandleWebSocket handles WebSocket connection upgrades
func (w *WebSocketUpgrader) HandleWebSocket(writer http.ResponseWriter, request *http.Request) {
	// Upgrade HTTP connection to WebSocket
	conn, err := w.upgrader.Upgrade(writer, request, nil)
	if err != nil {
		fmt.Printf("Failed to upgrade WebSocket connection: %v\n", err)
		return
	}
	defer conn.Close()

	// Extract user ID from request context (set by auth middleware) or headers (for testing)
	userID := getUserIDFromContext(request.Context())
	if userID == "" {
		// Check for test authentication header
		userID = request.Header.Get("X-User-ID")
	}
	if userID == "" {
		// Send error message and close connection
		errorMsg := types.WebSocketMessage{
			Type:      types.WebSocketMessageTypeError,
			ID:        uuid.New().String(),
			Timestamp: time.Now(),
			Error:     "Authentication required",
		}
		conn.WriteJSON(errorMsg)
		return
	}

	// Create client
	client := &Client{
		ID:            uuid.New().String(),
		UserID:        userID,
		Conn:          conn,
		Hub:           w.hub,
		Send:          make(chan types.WebSocketMessage, 256),
		Subscriptions: make(map[string]bool),
		LastPing:      time.Now(),
		ConnectedAt:   time.Now(),
	}

	// Register client with hub
	w.hub.register <- client

	// Start goroutines for reading and writing
	go client.writePump()
	go client.readPump()
}

// checkOrigin checks if the origin is allowed for WebSocket connections
func checkOrigin(r *http.Request) bool {
	// In production, you should implement proper origin checking
	// For now, allow all origins
	return true
}

// Client represents a WebSocket client connection
type Client struct {
	ID            string
	UserID        string
	Hub           *Hub
	Conn          *websocket.Conn
	Send          chan types.WebSocketMessage
	Subscriptions map[string]bool
	LastPing      time.Time
	ConnectedAt   time.Time
}

// readPump pumps messages from the WebSocket connection to the hub
func (c *Client) readPump() {
	defer func() {
		c.Hub.unregister <- c
		c.Conn.Close()
	}()

	c.Conn.SetReadLimit(512) // 512 bytes
	c.Conn.SetReadDeadline(time.Now().Add(60 * time.Second))
	c.Conn.SetPongHandler(func(string) error {
		c.Conn.SetReadDeadline(time.Now().Add(60 * time.Second))
		c.LastPing = time.Now()
		return nil
	})

	for {
		var msg types.WebSocketMessage
		err := c.Conn.ReadJSON(&msg)
		if err != nil {
			if websocket.IsUnexpectedCloseError(err, websocket.CloseGoingAway, websocket.CloseAbnormalClosure) {
				fmt.Printf("WebSocket error: %v\n", err)
			}
			break
		}

		// Handle incoming messages
		c.handleMessage(msg)
	}
}

// writePump pumps messages from the hub to the WebSocket connection
func (c *Client) writePump() {
	ticker := time.NewTicker(54 * time.Second)
	defer func() {
		ticker.Stop()
		c.Conn.Close()
	}()

	for {
		select {
		case message, ok := <-c.Send:
			c.Conn.SetWriteDeadline(time.Now().Add(10 * time.Second))
			if !ok {
				c.Conn.WriteMessage(websocket.CloseMessage, []byte{})
				return
			}

			if err := c.Conn.WriteJSON(message); err != nil {
				fmt.Printf("Failed to write WebSocket message: %v\n", err)
				return
			}

		case <-ticker.C:
			c.Conn.SetWriteDeadline(time.Now().Add(10 * time.Second))
			if err := c.Conn.WriteMessage(websocket.PingMessage, nil); err != nil {
				return
			}
		}
	}
}

// handleMessage handles incoming WebSocket messages
func (c *Client) handleMessage(msg types.WebSocketMessage) {
	switch msg.Type {
	case types.WebSocketMessageTypePing:
		// Respond with pong
		pongMsg := types.WebSocketMessage{
			Type:      types.WebSocketMessageTypePong,
			ID:        uuid.New().String(),
			Timestamp: time.Now(),
		}
		select {
		case c.Send <- pongMsg:
		default:
			close(c.Send)
		}

	case types.WebSocketMessageTypeSystemStatus:
		// Handle system status requests
		c.handleSystemStatusRequest()

	default:
		// Handle subscription requests
		if msg.Data != nil {
			if data, ok := msg.Data.(map[string]interface{}); ok {
				if action, ok := data["action"].(string); ok {
					switch action {
					case "subscribe":
						c.handleSubscribe(data)
					case "unsubscribe":
						c.handleUnsubscribe(data)
					}
				}
			}
		}
	}
}

// handleSubscribe handles subscription requests
func (c *Client) handleSubscribe(data map[string]interface{}) {
	if resourceType, ok := data["resourceType"].(string); ok {
		if resourceID, ok := data["resourceId"].(string); ok {
			subscriptionKey := fmt.Sprintf("%s:%s", resourceType, resourceID)
			c.Subscriptions[subscriptionKey] = true

			// Notify hub of subscription
			c.Hub.subscribe <- &SubscriptionRequest{
				ClientID:     c.ID,
				UserID:       c.UserID,
				ResourceType: resourceType,
				ResourceID:   resourceID,
			}
		}
	}
}

// handleUnsubscribe handles unsubscription requests
func (c *Client) handleUnsubscribe(data map[string]interface{}) {
	if resourceType, ok := data["resourceType"].(string); ok {
		if resourceID, ok := data["resourceId"].(string); ok {
			subscriptionKey := fmt.Sprintf("%s:%s", resourceType, resourceID)
			delete(c.Subscriptions, subscriptionKey)

			// Notify hub of unsubscription
			c.Hub.unsubscribe <- &SubscriptionRequest{
				ClientID:     c.ID,
				UserID:       c.UserID,
				ResourceType: resourceType,
				ResourceID:   resourceID,
			}
		}
	}
}

// handleSystemStatusRequest handles system status requests
func (c *Client) handleSystemStatusRequest() {
	// Get system status from hub
	status := c.Hub.GetSystemStatus()

	statusMsg := types.WebSocketMessage{
		Type:      types.WebSocketMessageTypeSystemStatus,
		ID:        uuid.New().String(),
		Timestamp: time.Now(),
		Data:      status,
	}

	select {
	case c.Send <- statusMsg:
	default:
		close(c.Send)
	}
}

// SubscriptionRequest represents a subscription request
type SubscriptionRequest struct {
	ClientID     string
	UserID       string
	ResourceType string
	ResourceID   string
}

// Hub maintains the set of active clients and broadcasts messages to them
type Hub struct {
	// Registered clients
	clients map[*Client]bool

	// Inbound messages from clients
	broadcast chan types.WebSocketMessage

	// Register requests from clients
	register chan *Client

	// Unregister requests from clients
	unregister chan *Client

	// Subscription requests
	subscribe chan *SubscriptionRequest

	// Unsubscription requests
	unsubscribe chan *SubscriptionRequest

	// Rooms for targeted broadcasting
	rooms map[string]map[*Client]bool

	// Client subscriptions by resource
	subscriptions map[string]map[*Client]bool

	// Statistics
	stats *types.WebSocketStats

	// Mutex for thread safety
	mutex sync.RWMutex

	// Start time for uptime calculation
	startTime time.Time
}

// NewHub creates a new WebSocket hub
func NewHub() *Hub {
	return &Hub{
		clients:       make(map[*Client]bool),
		broadcast:     make(chan types.WebSocketMessage),
		register:      make(chan *Client),
		unregister:    make(chan *Client),
		subscribe:     make(chan *SubscriptionRequest),
		unsubscribe:   make(chan *SubscriptionRequest),
		rooms:         make(map[string]map[*Client]bool),
		subscriptions: make(map[string]map[*Client]bool),
		stats: &types.WebSocketStats{
			TotalConnections:  0,
			ActiveConnections: 0,
			TotalMessages:     0,
			MessagesPerSecond: 0,
			AverageLatency:    0,
			LastMessageAt:     time.Now(),
			Uptime:            0,
			ErrorCount:        0,
			DisconnectCount:   0,
		},
		startTime: time.Now(),
	}
}

// Run starts the hub's main loop
func (h *Hub) Run() {
	ticker := time.NewTicker(1 * time.Second)
	defer ticker.Stop()

	for {
		select {
		case client := <-h.register:
			h.registerClient(client)

		case client := <-h.unregister:
			h.unregisterClient(client)

		case subscription := <-h.subscribe:
			h.handleSubscribe(subscription)

		case subscription := <-h.unsubscribe:
			h.handleUnsubscribe(subscription)

		case message := <-h.broadcast:
			h.BroadcastMessage(message)

		case <-ticker.C:
			h.updateStats()
		}
	}
}

// registerClient registers a new client
func (h *Hub) registerClient(client *Client) {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	h.clients[client] = true
	h.stats.TotalConnections++
	h.stats.ActiveConnections++

	fmt.Printf("Client %s connected. Total clients: %d\n", client.ID, h.stats.ActiveConnections)
}

// unregisterClient unregisters a client
func (h *Hub) unregisterClient(client *Client) {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	if _, ok := h.clients[client]; ok {
		delete(h.clients, client)
		close(client.Send)
		h.stats.ActiveConnections--
		h.stats.DisconnectCount++

		// Remove from all rooms
		for roomID, room := range h.rooms {
			if _, exists := room[client]; exists {
				delete(room, client)
				if len(room) == 0 {
					delete(h.rooms, roomID)
				}
			}
		}

		// Remove from all subscriptions
		for resourceID, subscribers := range h.subscriptions {
			if _, exists := subscribers[client]; exists {
				delete(subscribers, client)
				if len(subscribers) == 0 {
					delete(h.subscriptions, resourceID)
				}
			}
		}

		fmt.Printf("Client %s disconnected. Active clients: %d\n", client.ID, h.stats.ActiveConnections)
	}
}

// handleSubscribe handles subscription requests
func (h *Hub) handleSubscribe(req *SubscriptionRequest) {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	resourceID := fmt.Sprintf("%s:%s", req.ResourceType, req.ResourceID)

	// Find client
	var client *Client
	for c := range h.clients {
		if c.ID == req.ClientID {
			client = c
			break
		}
	}

	if client == nil {
		return
	}

	// Add to subscription
	if h.subscriptions[resourceID] == nil {
		h.subscriptions[resourceID] = make(map[*Client]bool)
	}
	h.subscriptions[resourceID][client] = true

	// Add to room
	roomID := fmt.Sprintf("room:%s", resourceID)
	if h.rooms[roomID] == nil {
		h.rooms[roomID] = make(map[*Client]bool)
	}
	h.rooms[roomID][client] = true

	fmt.Printf("Client %s subscribed to %s\n", client.ID, resourceID)
}

// handleUnsubscribe handles unsubscription requests
func (h *Hub) handleUnsubscribe(req *SubscriptionRequest) {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	resourceID := fmt.Sprintf("%s:%s", req.ResourceType, req.ResourceID)

	// Find client
	var client *Client
	for c := range h.clients {
		if c.ID == req.ClientID {
			client = c
			break
		}
	}

	if client == nil {
		return
	}

	// Remove from subscription
	if subscribers, exists := h.subscriptions[resourceID]; exists {
		delete(subscribers, client)
		if len(subscribers) == 0 {
			delete(h.subscriptions, resourceID)
		}
	}

	// Remove from room
	roomID := fmt.Sprintf("room:%s", resourceID)
	if room, exists := h.rooms[roomID]; exists {
		delete(room, client)
		if len(room) == 0 {
			delete(h.rooms, roomID)
		}
	}

	fmt.Printf("Client %s unsubscribed from %s\n", client.ID, resourceID)
}

// BroadcastMessage broadcasts a message to all clients or specific subscribers
func (h *Hub) BroadcastMessage(message types.WebSocketMessage) {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	h.stats.TotalMessages++
	h.stats.LastMessageAt = time.Now()

	// If message has specific resource targeting, send to subscribers only
	if message.ResourceType != "" && message.ResourceID != "" {
		resourceID := fmt.Sprintf("%s:%s", message.ResourceType, message.ResourceID)
		if subscribers, exists := h.subscriptions[resourceID]; exists {
			for client := range subscribers {
				select {
				case client.Send <- message:
				default:
					close(client.Send)
					delete(h.clients, client)
				}
			}
		}
		return
	}

	// If message has user targeting, send to specific user
	if message.UserID != "" {
		for client := range h.clients {
			if client.UserID == message.UserID {
				select {
				case client.Send <- message:
				default:
					close(client.Send)
					delete(h.clients, client)
				}
			}
		}
		return
	}

	// Broadcast to all clients
	for client := range h.clients {
		select {
		case client.Send <- message:
		default:
			close(client.Send)
			delete(h.clients, client)
		}
	}
}

// BroadcastExperimentUpdate broadcasts an experiment update
func (h *Hub) BroadcastExperimentUpdate(experimentID string, eventType types.WebSocketMessageType, data interface{}) {
	message := types.WebSocketMessage{
		Type:         eventType,
		ID:           fmt.Sprintf("exp_%s_%d", experimentID, time.Now().UnixNano()),
		Timestamp:    time.Now(),
		ResourceType: "experiment",
		ResourceID:   experimentID,
		Data:         data,
	}
	h.BroadcastMessage(message)
}

// BroadcastTaskUpdate broadcasts a task update
func (h *Hub) BroadcastTaskUpdate(taskID, experimentID string, eventType types.WebSocketMessageType, data interface{}) {
	message := types.WebSocketMessage{
		Type:         eventType,
		ID:           fmt.Sprintf("task_%s_%d", taskID, time.Now().UnixNano()),
		Timestamp:    time.Now(),
		ResourceType: "task",
		ResourceID:   taskID,
		Data:         data,
	}
	h.BroadcastMessage(message)
}

// BroadcastWorkerUpdate broadcasts a worker update
func (h *Hub) BroadcastWorkerUpdate(workerID string, eventType types.WebSocketMessageType, data interface{}) {
	message := types.WebSocketMessage{
		Type:         eventType,
		ID:           fmt.Sprintf("worker_%s_%d", workerID, time.Now().UnixNano()),
		Timestamp:    time.Now(),
		ResourceType: "worker",
		ResourceID:   workerID,
		Data:         data,
	}
	h.BroadcastMessage(message)
}

// BroadcastToUser broadcasts a message to a specific user
func (h *Hub) BroadcastToUser(userID string, eventType types.WebSocketMessageType, data interface{}) {
	message := types.WebSocketMessage{
		Type:      eventType,
		ID:        fmt.Sprintf("user_%s_%d", userID, time.Now().UnixNano()),
		Timestamp: time.Now(),
		UserID:    userID,
		Data:      data,
	}
	h.BroadcastMessage(message)
}

// GetSystemStatus returns current system status
func (h *Hub) GetSystemStatus() *types.WebSocketStats {
	h.mutex.RLock()
	defer h.mutex.RUnlock()

	// Update uptime
	h.stats.Uptime = time.Since(h.startTime)

	return &types.WebSocketStats{
		TotalConnections:  h.stats.TotalConnections,
		ActiveConnections: h.stats.ActiveConnections,
		TotalMessages:     h.stats.TotalMessages,
		MessagesPerSecond: h.stats.MessagesPerSecond,
		AverageLatency:    h.stats.AverageLatency,
		LastMessageAt:     h.stats.LastMessageAt,
		Uptime:            h.stats.Uptime,
		ErrorCount:        h.stats.ErrorCount,
		DisconnectCount:   h.stats.DisconnectCount,
	}
}

// updateStats updates hub statistics
func (h *Hub) updateStats() {
	h.mutex.Lock()
	defer h.mutex.Unlock()

	// Calculate messages per second (simple moving average)
	// This is a simplified calculation - in production you'd want a more sophisticated approach
	h.stats.MessagesPerSecond = float64(h.stats.TotalMessages) / time.Since(h.startTime).Seconds()
}
