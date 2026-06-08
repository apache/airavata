package adapters

import (
	"context"
	"sync"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// InMemoryEventAdapter implements port.EventPort using in-memory channels
type InMemoryEventAdapter struct {
	subscribers map[string][]ports.EventHandler
	mu          sync.RWMutex
	startTime   time.Time
}

// NewInMemoryEventAdapter creates a new in-memory event adapter
func NewInMemoryEventAdapter() *InMemoryEventAdapter {
	return &InMemoryEventAdapter{
		subscribers: make(map[string][]ports.EventHandler),
		startTime:   time.Now(),
	}
}

// Publish publishes an event to all subscribers
func (e *InMemoryEventAdapter) Publish(ctx context.Context, event *domain.DomainEvent) error {
	e.mu.RLock()
	handlers, exists := e.subscribers[event.Type]
	e.mu.RUnlock()

	if !exists {
		return nil // No subscribers for this event type
	}

	// Call all handlers asynchronously
	for _, handler := range handlers {
		go func(h ports.EventHandler) {
			defer func() {
				if r := recover(); r != nil {
					// Log error but don't crash the system
					// In production, this would use proper logging
				}
			}()
			h.Handle(ctx, event)
		}(handler)
	}

	return nil
}

// PublishBatch publishes multiple events
func (e *InMemoryEventAdapter) PublishBatch(ctx context.Context, events []*domain.DomainEvent) error {
	for _, event := range events {
		if err := e.Publish(ctx, event); err != nil {
			return err
		}
	}
	return nil
}

// Subscribe subscribes to events of a specific type
func (e *InMemoryEventAdapter) Subscribe(ctx context.Context, eventType string, handler ports.EventHandler) error {
	e.mu.Lock()
	defer e.mu.Unlock()

	if e.subscribers[eventType] == nil {
		e.subscribers[eventType] = make([]ports.EventHandler, 0)
	}

	e.subscribers[eventType] = append(e.subscribers[eventType], handler)
	return nil
}

// Unsubscribe removes a handler for a specific event type
func (e *InMemoryEventAdapter) Unsubscribe(ctx context.Context, eventType string, handler ports.EventHandler) error {
	e.mu.Lock()
	defer e.mu.Unlock()

	handlers := e.subscribers[eventType]
	for i, h := range handlers {
		// Compare handler IDs
		if h.GetHandlerID() == handler.GetHandlerID() {
			e.subscribers[eventType] = append(handlers[:i], handlers[i+1:]...)
			break
		}
	}
	return nil
}

// GetSubscriberCount returns the number of subscribers for an event type
func (e *InMemoryEventAdapter) GetSubscriberCount(eventType string) int {
	e.mu.RLock()
	defer e.mu.RUnlock()

	return len(e.subscribers[eventType])
}

// GetEventTypes returns all event types that have subscribers
func (e *InMemoryEventAdapter) GetEventTypes() []string {
	e.mu.RLock()
	defer e.mu.RUnlock()

	types := make([]string, 0, len(e.subscribers))
	for eventType := range e.subscribers {
		types = append(types, eventType)
	}
	return types
}

// Connect connects to the event system
func (e *InMemoryEventAdapter) Connect(ctx context.Context) error {
	return nil
}

// Disconnect disconnects from the event system
func (e *InMemoryEventAdapter) Disconnect(ctx context.Context) error {
	return nil
}

// IsConnected checks if connected
func (e *InMemoryEventAdapter) IsConnected() bool {
	return true
}

// GetStats returns event system statistics
func (e *InMemoryEventAdapter) GetStats(ctx context.Context) (*ports.EventStats, error) {
	e.mu.RLock()
	defer e.mu.RUnlock()

	totalSubscribers := 0
	for _, handlers := range e.subscribers {
		totalSubscribers += len(handlers)
	}

	return &ports.EventStats{
		PublishedEvents:     0, // Would track in real implementation
		FailedPublishes:     0,
		ActiveSubscriptions: int64(totalSubscribers),
		Uptime:              time.Since(e.startTime),
		LastEvent:           time.Now(),
		QueueSize:           0,
		ErrorRate:           0.0,
	}, nil
}

// Ping pings the event system
func (e *InMemoryEventAdapter) Ping(ctx context.Context) error {
	return nil
}

// Clear removes all subscribers
func (e *InMemoryEventAdapter) Clear() {
	e.mu.Lock()
	defer e.mu.Unlock()

	e.subscribers = make(map[string][]ports.EventHandler)
}

// Compile-time interface verification
var _ ports.EventPort = (*InMemoryEventAdapter)(nil)
