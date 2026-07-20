package services

import (
	"context"
	"fmt"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	types "github.com/apache/airavata/scheduler/core/util"
)

// WebSocketHub interface for broadcasting events
type WebSocketHub interface {
	BroadcastExperimentUpdate(experimentID string, messageType types.WebSocketMessageType, data map[string]interface{})
	BroadcastTaskUpdate(taskID string, experimentID string, messageType types.WebSocketMessageType, data map[string]interface{})
	BroadcastWorkerUpdate(workerID string, messageType types.WebSocketMessageType, data map[string]interface{})
	BroadcastToUser(userID string, messageType types.WebSocketMessageType, data map[string]interface{})
	BroadcastMessage(message types.WebSocketMessage)
}

// EventBroadcaster publishes real-time events to WebSocket clients
type EventBroadcaster struct {
	hub WebSocketHub
}

// NewEventBroadcaster creates a new event broadcaster
func NewEventBroadcaster(hub WebSocketHub) *EventBroadcaster {
	return &EventBroadcaster{
		hub: hub,
	}
}

// PublishExperimentEvent publishes an experiment-related event
func (eb *EventBroadcaster) PublishExperimentEvent(ctx context.Context, experimentID string, eventType types.WebSocketMessageType, data interface{}) error {
	if eb.hub == nil {
		return fmt.Errorf("WebSocket hub not available")
	}

	// Create event data
	eventData := map[string]interface{}{
		"experimentId": experimentID,
		"timestamp":    time.Now(),
		"data":         data,
	}

	// Broadcast to experiment subscribers
	eb.hub.BroadcastExperimentUpdate(experimentID, eventType, eventData)

	return nil
}

// PublishTaskEvent publishes a task-related event
func (eb *EventBroadcaster) PublishTaskEvent(ctx context.Context, taskID, experimentID string, eventType types.WebSocketMessageType, data interface{}) error {
	if eb.hub == nil {
		return fmt.Errorf("WebSocket hub not available")
	}

	// Create event data
	eventData := map[string]interface{}{
		"taskId":       taskID,
		"experimentId": experimentID,
		"timestamp":    time.Now(),
		"data":         data,
	}

	// Broadcast to task subscribers
	eb.hub.BroadcastTaskUpdate(taskID, experimentID, eventType, eventData)

	// Also broadcast to experiment subscribers
	eb.hub.BroadcastExperimentUpdate(experimentID, eventType, eventData)

	return nil
}

// PublishWorkerEvent publishes a worker-related event
func (eb *EventBroadcaster) PublishWorkerEvent(ctx context.Context, workerID string, eventType types.WebSocketMessageType, data interface{}) error {
	if eb.hub == nil {
		return fmt.Errorf("WebSocket hub not available")
	}

	// Create event data
	eventData := map[string]interface{}{
		"workerId":  workerID,
		"timestamp": time.Now(),
		"data":      data,
	}

	// Broadcast to worker subscribers
	eb.hub.BroadcastWorkerUpdate(workerID, eventType, eventData)

	return nil
}

// PublishUserEvent publishes a user-specific event
func (eb *EventBroadcaster) PublishUserEvent(ctx context.Context, userID string, eventType types.WebSocketMessageType, data interface{}) error {
	if eb.hub == nil {
		return fmt.Errorf("WebSocket hub not available")
	}

	// Create event data
	eventData := map[string]interface{}{
		"userId":    userID,
		"timestamp": time.Now(),
		"data":      data,
	}

	// Broadcast to user
	eb.hub.BroadcastToUser(userID, eventType, eventData)

	return nil
}

// PublishSystemEvent publishes a system-wide event
func (eb *EventBroadcaster) PublishSystemEvent(ctx context.Context, eventType types.WebSocketMessageType, data interface{}) error {
	if eb.hub == nil {
		return fmt.Errorf("WebSocket hub not available")
	}

	// Create event data
	eventData := map[string]interface{}{
		"timestamp": time.Now(),
		"data":      data,
	}

	// Broadcast to all clients
	message := types.WebSocketMessage{
		Type:      eventType,
		ID:        fmt.Sprintf("system_%d", time.Now().UnixNano()),
		Timestamp: time.Now(),
		Data:      eventData,
	}
	eb.hub.BroadcastMessage(message)

	return nil
}

// PublishExperimentCreated publishes an experiment creation event
func (eb *EventBroadcaster) PublishExperimentCreated(ctx context.Context, experiment *domain.Experiment) error {
	data := map[string]interface{}{
		"experiment": experiment,
		"summary": map[string]interface{}{
			"id":      experiment.ID,
			"name":    experiment.Name,
			"status":  experiment.Status,
			"ownerId": experiment.OwnerID,
		},
	}
	return eb.PublishExperimentEvent(ctx, experiment.ID, types.WebSocketMessageTypeExperimentCreated, data)
}

// PublishExperimentUpdated publishes an experiment update event
func (eb *EventBroadcaster) PublishExperimentUpdated(ctx context.Context, experiment *domain.Experiment) error {
	data := map[string]interface{}{
		"experiment": experiment,
		"summary": map[string]interface{}{
			"id":      experiment.ID,
			"name":    experiment.Name,
			"status":  experiment.Status,
			"ownerId": experiment.OwnerID,
		},
	}
	return eb.PublishExperimentEvent(ctx, experiment.ID, types.WebSocketMessageTypeExperimentUpdated, data)
}

// PublishExperimentProgress publishes an experiment progress event
func (eb *EventBroadcaster) PublishExperimentProgress(ctx context.Context, experimentID string, progress *types.ExperimentProgress) error {
	return eb.PublishExperimentEvent(ctx, experimentID, types.WebSocketMessageTypeExperimentProgress, progress)
}

// PublishExperimentCompleted publishes an experiment completion event
func (eb *EventBroadcaster) PublishExperimentCompleted(ctx context.Context, experiment *domain.Experiment) error {
	data := map[string]interface{}{
		"experiment": experiment,
		"summary": map[string]interface{}{
			"id":      experiment.ID,
			"name":    experiment.Name,
			"status":  experiment.Status,
			"ownerId": experiment.OwnerID,
		},
	}
	return eb.PublishExperimentEvent(ctx, experiment.ID, types.WebSocketMessageTypeExperimentCompleted, data)
}

// PublishExperimentFailed publishes an experiment failure event
func (eb *EventBroadcaster) PublishExperimentFailed(ctx context.Context, experiment *domain.Experiment) error {
	data := map[string]interface{}{
		"experiment": experiment,
		"summary": map[string]interface{}{
			"id":      experiment.ID,
			"name":    experiment.Name,
			"status":  experiment.Status,
			"ownerId": experiment.OwnerID,
		},
	}
	return eb.PublishExperimentEvent(ctx, experiment.ID, types.WebSocketMessageTypeExperimentFailed, data)
}

// PublishTaskCreated publishes a task creation event
func (eb *EventBroadcaster) PublishTaskCreated(ctx context.Context, task *domain.Task) error {
	data := map[string]interface{}{
		"task": task,
		"summary": map[string]interface{}{
			"id":           task.ID,
			"experimentId": task.ExperimentID,
			"status":       task.Status,
			"workerId":     task.WorkerID,
		},
	}
	return eb.PublishTaskEvent(ctx, task.ID, task.ExperimentID, types.WebSocketMessageTypeTaskCreated, data)
}

// PublishTaskUpdated publishes a task update event
func (eb *EventBroadcaster) PublishTaskUpdated(ctx context.Context, task *domain.Task) error {
	data := map[string]interface{}{
		"task": task,
		"summary": map[string]interface{}{
			"id":           task.ID,
			"experimentId": task.ExperimentID,
			"status":       task.Status,
			"workerId":     task.WorkerID,
		},
	}
	return eb.PublishTaskEvent(ctx, task.ID, task.ExperimentID, types.WebSocketMessageTypeTaskUpdated, data)
}

// PublishTaskProgress publishes a task progress event
func (eb *EventBroadcaster) PublishTaskProgress(ctx context.Context, taskID, experimentID string, progress *types.TaskProgress) error {
	return eb.PublishTaskEvent(ctx, taskID, experimentID, types.WebSocketMessageTypeTaskProgress, progress)
}

// PublishTaskCompleted publishes a task completion event
func (eb *EventBroadcaster) PublishTaskCompleted(ctx context.Context, task *domain.Task) error {
	data := map[string]interface{}{
		"task": task,
		"summary": map[string]interface{}{
			"id":           task.ID,
			"experimentId": task.ExperimentID,
			"status":       task.Status,
			"workerId":     task.WorkerID,
		},
	}
	return eb.PublishTaskEvent(ctx, task.ID, task.ExperimentID, types.WebSocketMessageTypeTaskCompleted, data)
}

// PublishTaskFailed publishes a task failure event
func (eb *EventBroadcaster) PublishTaskFailed(ctx context.Context, task *domain.Task) error {
	data := map[string]interface{}{
		"task": task,
		"summary": map[string]interface{}{
			"id":           task.ID,
			"experimentId": task.ExperimentID,
			"status":       task.Status,
			"workerId":     task.WorkerID,
			"error":        task.Error,
		},
	}
	return eb.PublishTaskEvent(ctx, task.ID, task.ExperimentID, types.WebSocketMessageTypeTaskFailed, data)
}

// PublishWorkerRegistered publishes a worker registration event
func (eb *EventBroadcaster) PublishWorkerRegistered(ctx context.Context, worker *domain.Worker) error {
	data := map[string]interface{}{
		"worker": worker,
		"summary": map[string]interface{}{
			"id":                worker.ID,
			"computeResourceId": worker.ComputeResourceID,
			"experimentId":      worker.ExperimentID,
			"status":            worker.Status,
		},
	}
	return eb.PublishWorkerEvent(ctx, worker.ID, types.WebSocketMessageTypeWorkerRegistered, data)
}

// PublishWorkerUpdated publishes a worker update event
func (eb *EventBroadcaster) PublishWorkerUpdated(ctx context.Context, worker *domain.Worker) error {
	data := map[string]interface{}{
		"worker": worker,
		"summary": map[string]interface{}{
			"id":                worker.ID,
			"computeResourceId": worker.ComputeResourceID,
			"experimentId":      worker.ExperimentID,
			"status":            worker.Status,
		},
	}
	return eb.PublishWorkerEvent(ctx, worker.ID, types.WebSocketMessageTypeWorkerUpdated, data)
}

// PublishWorkerOffline publishes a worker offline event
func (eb *EventBroadcaster) PublishWorkerOffline(ctx context.Context, worker *domain.Worker) error {
	data := map[string]interface{}{
		"worker": worker,
		"summary": map[string]interface{}{
			"id":                worker.ID,
			"computeResourceId": worker.ComputeResourceID,
			"experimentId":      worker.ExperimentID,
			"status":            worker.Status,
		},
	}
	return eb.PublishWorkerEvent(ctx, worker.ID, types.WebSocketMessageTypeWorkerOffline, data)
}
