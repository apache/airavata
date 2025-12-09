package adapters

import (
	"context"
	"fmt"
	"log"
	"sync"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// PostgresEventAdapter implements ports.EventPort using PostgreSQL storage
type PostgresEventAdapter struct {
	db           *gorm.DB
	subscribers  map[string][]ports.EventHandler
	mu           sync.RWMutex
	eventQueue   chan *domain.DomainEvent
	workerDone   chan struct{}
	shutdownChan chan struct{}
	workers      int
	resumeDone   chan struct{}
}

// EventQueueEntry represents an event in the database queue
type EventQueueEntry struct {
	ID           string                 `gorm:"primaryKey" json:"id"`
	EventType    string                 `gorm:"not null;index" json:"eventType"`
	Payload      map[string]interface{} `gorm:"serializer:json" json:"payload"`
	Status       string                 `gorm:"not null;index" json:"status"`
	Priority     int                    `gorm:"default:5" json:"priority"`
	MaxRetries   int                    `gorm:"default:3" json:"maxRetries"`
	RetryCount   int                    `gorm:"default:0" json:"retryCount"`
	ErrorMessage string                 `gorm:"type:text" json:"errorMessage,omitempty"`
	ProcessedAt  *time.Time             `json:"processedAt,omitempty"`
	CreatedAt    time.Time              `gorm:"autoCreateTime" json:"createdAt"`
	UpdatedAt    time.Time              `gorm:"autoUpdateTime" json:"updatedAt"`
}

// EventStatus represents the status of an event
type EventStatus string

const (
	EventStatusPending    EventStatus = "PENDING"
	EventStatusProcessing EventStatus = "PROCESSING"
	EventStatusCompleted  EventStatus = "COMPLETED"
	EventStatusFailed     EventStatus = "FAILED"
)

// NewPostgresEventAdapter creates a new PostgreSQL event adapter
func NewPostgresEventAdapter(db *gorm.DB) *PostgresEventAdapter {
	return NewPostgresEventAdapterWithOptions(db, true)
}

// NewPostgresEventAdapterWithOptions creates a new PostgreSQL event adapter with options
func NewPostgresEventAdapterWithOptions(db *gorm.DB, resumePendingEvents bool) *PostgresEventAdapter {
	adapter := &PostgresEventAdapter{
		db:           db,
		subscribers:  make(map[string][]ports.EventHandler),
		eventQueue:   make(chan *domain.DomainEvent, 1000),
		workerDone:   make(chan struct{}),
		shutdownChan: make(chan struct{}),
		workers:      3, // Default number of worker goroutines
		resumeDone:   make(chan struct{}),
	}

	// Auto-migrate the event_queue table
	if err := db.AutoMigrate(&EventQueueEntry{}); err != nil {
		log.Printf("Warning: failed to auto-migrate event_queue table: %v", err)
	}

	// Start event processing workers
	adapter.startEventWorkers()

	// Resume pending events from previous run (only if requested)
	if resumePendingEvents {
		go func() {
			defer close(adapter.resumeDone)
			adapter.resumePendingEvents()
		}()
	} else {
		// Close resumeDone immediately if not resuming
		close(adapter.resumeDone)
	}

	return adapter
}

// Publish publishes an event to the queue
func (e *PostgresEventAdapter) Publish(ctx context.Context, event *domain.DomainEvent) error {
	// Create event queue entry
	entry := &EventQueueEntry{
		ID:         event.ID,
		EventType:  event.Type,
		Payload:    event.Data,
		Status:     string(EventStatusPending),
		Priority:   5, // Default priority
		MaxRetries: 3,
		RetryCount: 0,
	}

	// Store in database using UPSERT to handle duplicate event IDs
	if err := e.db.WithContext(ctx).Save(entry).Error; err != nil {
		return fmt.Errorf("failed to store event in queue: %w", err)
	}

	// Send to processing queue
	select {
	case e.eventQueue <- event:
		return nil
	case <-ctx.Done():
		return ctx.Err()
	default:
		// Queue is full, but event is stored in database
		// It will be processed when workers are available
		return nil
	}
}

// PublishBatch publishes multiple events to the queue
func (e *PostgresEventAdapter) PublishBatch(ctx context.Context, events []*domain.DomainEvent) error {
	if len(events) == 0 {
		return nil
	}

	// Create batch of event queue entries
	entries := make([]*EventQueueEntry, len(events))
	for i, event := range events {
		entries[i] = &EventQueueEntry{
			ID:         event.ID,
			EventType:  event.Type,
			Payload:    event.Data,
			Status:     string(EventStatusPending),
			Priority:   5,
			MaxRetries: 3,
			RetryCount: 0,
		}
	}

	// Store batch in database using UPSERT to handle duplicate event IDs
	if err := e.db.WithContext(ctx).Save(entries).Error; err != nil {
		return fmt.Errorf("failed to store event batch in queue: %w", err)
	}

	// Send to processing queue
	for _, event := range events {
		select {
		case e.eventQueue <- event:
		case <-ctx.Done():
			return ctx.Err()
		default:
			// Queue is full, but events are stored in database
			goto done
		}
	}

done:
	return nil
}

// Subscribe subscribes to events of a specific type
func (e *PostgresEventAdapter) Subscribe(ctx context.Context, eventType string, handler ports.EventHandler) error {
	e.mu.Lock()
	defer e.mu.Unlock()

	// Add handler to subscribers
	e.subscribers[eventType] = append(e.subscribers[eventType], handler)

	return nil
}

// Unsubscribe unsubscribes from events of a specific type
func (e *PostgresEventAdapter) Unsubscribe(ctx context.Context, eventType string, handler ports.EventHandler) error {
	e.mu.Lock()
	defer e.mu.Unlock()

	// Remove handler from subscribers
	handlers := e.subscribers[eventType]
	for i, h := range handlers {
		if h == handler {
			e.subscribers[eventType] = append(handlers[:i], handlers[i+1:]...)
			break
		}
	}

	return nil
}

// GetSubscriberCount returns the number of subscribers for an event type
func (e *PostgresEventAdapter) GetSubscriberCount(eventType string) int {
	e.mu.RLock()
	defer e.mu.RUnlock()

	return len(e.subscribers[eventType])
}

// GetEventTypes returns all event types with subscribers
func (e *PostgresEventAdapter) GetEventTypes() []string {
	e.mu.RLock()
	defer e.mu.RUnlock()

	types := make([]string, 0, len(e.subscribers))
	for eventType := range e.subscribers {
		types = append(types, eventType)
	}

	return types
}

// Connect connects to the event system
func (e *PostgresEventAdapter) Connect(ctx context.Context) error {
	// PostgreSQL event adapter is always connected
	return nil
}

// Disconnect disconnects from the event system
func (e *PostgresEventAdapter) Disconnect(ctx context.Context) error {
	return e.Shutdown(ctx)
}

// Shutdown gracefully shuts down the event adapter
func (e *PostgresEventAdapter) Shutdown(ctx context.Context) error {
	// Signal shutdown
	close(e.shutdownChan)

	// Wait for resume goroutine to finish
	select {
	case <-e.resumeDone:
		// Resume goroutine finished
	case <-ctx.Done():
		return ctx.Err()
	}

	// Wait for all workers to finish with timeout
	for i := 0; i < e.workers; i++ {
		select {
		case <-e.workerDone:
			// Worker finished
		case <-ctx.Done():
			return ctx.Err()
		}
	}

	return nil
}

// IsConnected returns true if connected
func (e *PostgresEventAdapter) IsConnected() bool {
	select {
	case <-e.shutdownChan:
		return false
	default:
		return true
	}
}

// GetStats returns event system statistics
func (e *PostgresEventAdapter) GetStats(ctx context.Context) (*ports.EventStats, error) {
	var stats struct {
		Total      int64 `json:"total"`
		Pending    int64 `json:"pending"`
		Processing int64 `json:"processing"`
		Completed  int64 `json:"completed"`
		Failed     int64 `json:"failed"`
	}

	// Get counts by status
	e.db.WithContext(ctx).Model(&EventQueueEntry{}).Count(&stats.Total)
	e.db.WithContext(ctx).Model(&EventQueueEntry{}).Where("status = ?", EventStatusPending).Count(&stats.Pending)
	e.db.WithContext(ctx).Model(&EventQueueEntry{}).Where("status = ?", EventStatusProcessing).Count(&stats.Processing)
	e.db.WithContext(ctx).Model(&EventQueueEntry{}).Where("status = ?", EventStatusCompleted).Count(&stats.Completed)
	e.db.WithContext(ctx).Model(&EventQueueEntry{}).Where("status = ?", EventStatusFailed).Count(&stats.Failed)

	// Get subscriber counts
	e.mu.RLock()
	subscriberCount := 0
	for _, handlers := range e.subscribers {
		subscriberCount += len(handlers)
	}
	e.mu.RUnlock()

	return &ports.EventStats{
		PublishedEvents:     stats.Total,
		FailedPublishes:     stats.Failed,
		ActiveSubscriptions: int64(subscriberCount),
		Uptime:              0,          // Not tracked in this implementation
		LastEvent:           time.Now(), // Not tracked in this implementation
		QueueSize:           stats.Pending + stats.Processing,
		ErrorRate:           float64(stats.Failed) / float64(stats.Total),
	}, nil
}

// Ping pings the event system
func (e *PostgresEventAdapter) Ping(ctx context.Context) error {
	var result int
	err := e.db.WithContext(ctx).Raw("SELECT 1").Scan(&result).Error
	if err != nil {
		return fmt.Errorf("event system ping failed: %w", err)
	}
	return nil
}

// Clear clears all events (for testing)
func (e *PostgresEventAdapter) Clear() {
	e.mu.Lock()
	defer e.mu.Unlock()

	// Clear subscribers
	e.subscribers = make(map[string][]ports.EventHandler)

	// Clear event queue
	for {
		select {
		case <-e.eventQueue:
		default:
			return
		}
	}
}

// startEventWorkers starts the event processing workers
func (e *PostgresEventAdapter) startEventWorkers() {
	for i := 0; i < e.workers; i++ {
		go e.eventWorker(i)
	}
}

// eventWorker processes events from the queue
func (e *PostgresEventAdapter) eventWorker(workerID int) {
	defer func() {
		e.workerDone <- struct{}{}
	}()

	for {
		select {
		case <-e.shutdownChan:
			return
		case event := <-e.eventQueue:
			e.processEvent(event)
		}
	}
}

// processEvent processes a single event
func (e *PostgresEventAdapter) processEvent(event *domain.DomainEvent) {
	// Mark as processing
	now := time.Now()
	err := e.db.Model(&EventQueueEntry{}).
		Where("id = ?", event.ID).
		Updates(map[string]interface{}{
			"status":     EventStatusProcessing,
			"updated_at": now,
		}).Error

	if err != nil {
		log.Printf("Failed to mark event as processing: %v", err)
		return
	}

	// Get subscribers for this event type
	e.mu.RLock()
	handlers := make([]ports.EventHandler, len(e.subscribers[event.Type]))
	copy(handlers, e.subscribers[event.Type])
	e.mu.RUnlock()

	// Process event with all subscribers
	var lastError error
	for _, handler := range handlers {
		if err := handler.Handle(context.Background(), event); err != nil {
			log.Printf("Event handler failed for event %s: %v", event.ID, err)
			lastError = err
		}
	}

	// Update event status
	status := EventStatusCompleted
	errorMessage := ""
	if lastError != nil {
		// Check if we should retry
		var entry EventQueueEntry
		if err := e.db.Where("id = ?", event.ID).First(&entry).Error; err == nil {
			if entry.RetryCount < entry.MaxRetries {
				// Retry
				status = EventStatusPending
				e.db.Model(&entry).Updates(map[string]interface{}{
					"status":      status,
					"retry_count": entry.RetryCount + 1,
					"updated_at":  time.Now(),
				})
				// Re-queue for retry
				select {
				case e.eventQueue <- event:
				default:
					// Queue is full, will be picked up by resumePendingEvents
				}
				return
			} else {
				// Max retries exceeded
				status = EventStatusFailed
				errorMessage = lastError.Error()
			}
		}
	}

	// Mark as completed or failed
	processedAt := time.Now()
	e.db.Model(&EventQueueEntry{}).
		Where("id = ?", event.ID).
		Updates(map[string]interface{}{
			"status":        status,
			"error_message": errorMessage,
			"processed_at":  processedAt,
			"updated_at":    processedAt,
		})
}

// resumePendingEvents resumes processing of pending events from previous run
func (e *PostgresEventAdapter) resumePendingEvents() {
	// Wait a short time for the system to start up (reduced from 2s to 100ms)
	select {
	case <-time.After(100 * time.Millisecond):
		// Continue
	case <-e.shutdownChan:
		return
	}

	// Get pending events count first (faster query)
	var count int64
	err := e.db.Model(&EventQueueEntry{}).Where("status = ?", EventStatusPending).Count(&count).Error
	if err != nil {
		log.Printf("Failed to count pending events: %v", err)
		return
	}

	// Only log if there are actually pending events
	if count > 0 {
		log.Printf("Resuming %d pending events", count)

		// Get pending events
		var entries []EventQueueEntry
		err := e.db.Where("status = ?", EventStatusPending).
			Order("priority DESC, created_at ASC").
			Find(&entries).Error

		if err != nil {
			log.Printf("Failed to get pending events: %v", err)
			return
		}

		// Re-queue pending events
		for _, entry := range entries {
			event := &domain.DomainEvent{
				ID:        entry.ID,
				Type:      entry.EventType,
				Data:      entry.Payload,
				Timestamp: entry.CreatedAt,
			}

			select {
			case e.eventQueue <- event:
			case <-e.shutdownChan:
				return
			default:
				// Queue is full, events will be processed when workers are available
				goto done
			}
		}
	}

done:
}

// Compile-time interface verification
var _ ports.EventPort = (*PostgresEventAdapter)(nil)
