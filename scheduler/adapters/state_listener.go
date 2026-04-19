package adapters

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgconn"
)

// StateChangeCallback defines a callback function for state changes
type StateChangeCallback func(ctx context.Context, notification *StateChangeNotification) error

// StateChangeNotification represents a state change notification from PostgreSQL
type StateChangeNotification struct {
	Table     string                 `json:"table"`
	ID        string                 `json:"id"`
	OldStatus string                 `json:"old_status"`
	NewStatus string                 `json:"new_status"`
	Timestamp time.Time              `json:"timestamp"`
	Metadata  map[string]interface{} `json:"metadata,omitempty"`
}

// StateChangeListener listens to PostgreSQL LISTEN/NOTIFY for distributed state coordination
type StateChangeListener struct {
	conn      *pgx.Conn
	listeners map[string][]StateChangeCallback
	mu        sync.RWMutex
	stopChan  chan struct{}
	running   bool
}

// NewStateChangeListener creates a new state change listener
func NewStateChangeListener(dsn string) (*StateChangeListener, error) {
	conn, err := pgx.Connect(context.Background(), dsn)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to PostgreSQL: %w", err)
	}

	return &StateChangeListener{
		conn:      conn,
		listeners: make(map[string][]StateChangeCallback),
		stopChan:  make(chan struct{}),
		running:   false,
	}, nil
}

// Start begins listening for state change notifications
func (sl *StateChangeListener) Start(ctx context.Context) error {
	sl.mu.Lock()
	defer sl.mu.Unlock()

	if sl.running {
		return fmt.Errorf("listener is already running")
	}

	// Start listening to the state_changes channel
	_, err := sl.conn.Exec(ctx, "LISTEN state_changes")
	if err != nil {
		return fmt.Errorf("failed to start listening: %w", err)
	}

	sl.running = true

	// Start the notification handler goroutine
	go sl.handleNotifications(ctx)

	log.Println("State change listener started")
	return nil
}

// Stop stops listening for notifications
func (sl *StateChangeListener) Stop() error {
	sl.mu.Lock()
	defer sl.mu.Unlock()

	if !sl.running {
		return nil
	}

	close(sl.stopChan)
	sl.running = false

	// Close the connection
	if err := sl.conn.Close(context.Background()); err != nil {
		return fmt.Errorf("failed to close connection: %w", err)
	}

	log.Println("State change listener stopped")
	return nil
}

// RegisterCallback registers a callback for a specific table
func (sl *StateChangeListener) RegisterCallback(table string, callback StateChangeCallback) {
	sl.mu.Lock()
	defer sl.mu.Unlock()

	sl.listeners[table] = append(sl.listeners[table], callback)
	log.Printf("Registered callback for table: %s", table)
}

// UnregisterCallback removes a callback for a specific table
// Note: This is a simplified implementation that removes all callbacks for a table
// In a production system, you might want to use a different approach like callback IDs
func (sl *StateChangeListener) UnregisterCallback(table string, callback StateChangeCallback) {
	sl.mu.Lock()
	defer sl.mu.Unlock()

	// For now, we'll just clear all callbacks for the table
	// This is sufficient for our current use case
	delete(sl.listeners, table)
}

// handleNotifications processes incoming PostgreSQL notifications
func (sl *StateChangeListener) handleNotifications(ctx context.Context) {
	for {
		select {
		case <-sl.stopChan:
			return
		case <-ctx.Done():
			return
		default:
			// Wait for notification with timeout
			notification, err := sl.conn.WaitForNotification(ctx)
			if err != nil {
				if err == context.Canceled || err == context.DeadlineExceeded {
					continue
				}
				log.Printf("Error waiting for notification: %v", err)
				time.Sleep(1 * time.Second)
				continue
			}

			// Process the notification
			sl.processNotification(ctx, notification)
		}
	}
}

// processNotification processes a single notification
func (sl *StateChangeListener) processNotification(ctx context.Context, notification *pgconn.Notification) {
	// Parse the notification payload
	var stateChange StateChangeNotification
	if err := json.Unmarshal([]byte(notification.Payload), &stateChange); err != nil {
		log.Printf("Failed to parse notification payload: %v", err)
		return
	}

	// Get callbacks for this table
	sl.mu.RLock()
	callbacks := make([]StateChangeCallback, len(sl.listeners[stateChange.Table]))
	copy(callbacks, sl.listeners[stateChange.Table])
	sl.mu.RUnlock()

	// Execute callbacks
	for _, callback := range callbacks {
		if err := callback(ctx, &stateChange); err != nil {
			log.Printf("Callback failed for table %s: %v", stateChange.Table, err)
		}
	}

	log.Printf("Processed state change notification: %s.%s %s -> %s",
		stateChange.Table, stateChange.ID, stateChange.OldStatus, stateChange.NewStatus)
}

// IsRunning returns whether the listener is currently running
func (sl *StateChangeListener) IsRunning() bool {
	sl.mu.RLock()
	defer sl.mu.RUnlock()
	return sl.running
}

// GetListenerCount returns the number of registered callbacks for a table
func (sl *StateChangeListener) GetListenerCount(table string) int {
	sl.mu.RLock()
	defer sl.mu.RUnlock()
	return len(sl.listeners[table])
}

// GetRegisteredTables returns all tables with registered callbacks
func (sl *StateChangeListener) GetRegisteredTables() []string {
	sl.mu.RLock()
	defer sl.mu.RUnlock()

	tables := make([]string, 0, len(sl.listeners))
	for table := range sl.listeners {
		tables = append(tables, table)
	}
	return tables
}
