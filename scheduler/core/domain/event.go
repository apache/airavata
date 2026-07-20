package domain

import (
	"crypto/rand"
	"encoding/hex"
	"fmt"
	"time"
)

// Domain events for event-driven architecture

// DomainEvent represents a domain event
type DomainEvent struct {
	ID        string                 `json:"id"`
	Type      string                 `json:"type"`
	Source    string                 `json:"source"`
	Timestamp time.Time              `json:"timestamp"`
	Data      map[string]interface{} `json:"data"`
	Metadata  map[string]interface{} `json:"metadata,omitempty"`
}

// Resource events
const (
	EventTypeResourceCreated   = "resource.created"
	EventTypeResourceUpdated   = "resource.updated"
	EventTypeResourceDeleted   = "resource.deleted"
	EventTypeResourceValidated = "resource.validated"
)

// Credential events
const (
	EventTypeCredentialCreated = "credential.created"
	EventTypeCredentialUpdated = "credential.updated"
	EventTypeCredentialDeleted = "credential.deleted"
	EventTypeCredentialShared  = "credential.shared"
	EventTypeCredentialRevoked = "credential.revoked"
)

// Experiment events
const (
	EventTypeExperimentCreated   = "experiment.created"
	EventTypeExperimentUpdated   = "experiment.updated"
	EventTypeExperimentDeleted   = "experiment.deleted"
	EventTypeExperimentSubmitted = "experiment.submitted"
	EventTypeExperimentStarted   = "experiment.started"
	EventTypeExperimentCompleted = "experiment.completed"
	EventTypeExperimentFailed    = "experiment.failed"
	EventTypeExperimentCancelled = "experiment.cancelled"
)

// Task events
const (
	EventTypeTaskCreated   = "task.created"
	EventTypeTaskQueued    = "task.queued"
	EventTypeTaskAssigned  = "task.assigned"
	EventTypeTaskStarted   = "task.started"
	EventTypeTaskCompleted = "task.completed"
	EventTypeTaskFailed    = "task.failed"
	EventTypeTaskCancelled = "task.cancelled"
	EventTypeTaskRetried   = "task.retried"
)

// Worker events
const (
	EventTypeWorkerCreated    = "worker.created"
	EventTypeWorkerStarted    = "worker.started"
	EventTypeWorkerIdle       = "worker.idle"
	EventTypeWorkerBusy       = "worker.busy"
	EventTypeWorkerStopped    = "worker.stopped"
	EventTypeWorkerFailed     = "worker.failed"
	EventTypeWorkerTerminated = "worker.terminated"
	EventTypeWorkerHeartbeat  = "worker.heartbeat"
)

// Data movement events
const (
	EventTypeDataStaged          = "data.staged"
	EventTypeDataTransferred     = "data.transferred"
	EventTypeDataCached          = "data.cached"
	EventTypeDataCleaned         = "data.cleaned"
	EventTypeDataLineageRecorded = "data.lineage.recorded"
)

// Scheduling events
const (
	EventTypeSchedulingPlanCreated  = "scheduling.plan.created"
	EventTypeSchedulingTaskAssigned = "scheduling.task.assigned"
	EventTypeWorkerAllocated        = "scheduling.worker.allocated"
	EventTypeCostOptimized          = "scheduling.cost.optimized"
)

// Audit events
const (
	EventTypeUserLogin     = "audit.user.login"
	EventTypeUserLogout    = "audit.user.logout"
	EventTypeUserAction    = "audit.user.action"
	EventTypeSystemAction  = "audit.system.action"
	EventTypeSecurityEvent = "audit.security.event"
)

// Event constructors

// NewResourceCreatedEvent creates a new resource created event
func NewResourceCreatedEvent(resourceID, resourceType, userID string) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeResourceCreated,
		Source:    "resource-registry",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"resourceId":   resourceID,
			"resourceType": resourceType,
			"userId":       userID,
		},
	}
}

// NewResourceUpdatedEvent creates a new resource updated event
func NewResourceUpdatedEvent(resourceID, resourceType, userID string) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeResourceUpdated,
		Source:    "resource-registry",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"resourceId":   resourceID,
			"resourceType": resourceType,
			"userId":       userID,
		},
	}
}

// NewResourceDeletedEvent creates a new resource deleted event
func NewResourceDeletedEvent(resourceID, resourceType, userID string) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeResourceDeleted,
		Source:    "resource-registry",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"resourceId":   resourceID,
			"resourceType": resourceType,
			"userId":       userID,
		},
	}
}

// NewExperimentCreatedEvent creates a new experiment created event
func NewExperimentCreatedEvent(experimentID, userID string) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeExperimentCreated,
		Source:    "experiment-orchestrator",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"experimentId": experimentID,
			"userId":       userID,
		},
	}
}

// NewExperimentSubmittedEvent creates a new experiment submitted event
func NewExperimentSubmittedEvent(experimentID, userID string, taskCount int) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeExperimentSubmitted,
		Source:    "experiment-orchestrator",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"experimentId": experimentID,
			"userId":       userID,
			"taskCount":    taskCount,
		},
	}
}

// NewTaskCreatedEvent creates a new task created event
func NewTaskCreatedEvent(taskID, experimentID string) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeTaskCreated,
		Source:    "experiment-orchestrator",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"taskId":       taskID,
			"experimentId": experimentID,
		},
	}
}

// NewTaskQueuedEvent creates a new task queued event
func NewTaskQueuedEvent(taskID, experimentID string) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeTaskQueued,
		Source:    "task-scheduler",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"taskId":       taskID,
			"experimentId": experimentID,
		},
	}
}

// NewTaskAssignedEvent creates a new task assigned event
func NewTaskAssignedEvent(taskID, workerID string) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeTaskAssigned,
		Source:    "task-scheduler",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"taskId":   taskID,
			"workerId": workerID,
		},
	}
}

// NewTaskCompletedEvent creates a new task completed event
func NewTaskCompletedEvent(taskID, workerID string, duration time.Duration) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeTaskCompleted,
		Source:    "task-scheduler",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"taskId":   taskID,
			"workerId": workerID,
			"duration": duration.String(),
		},
	}
}

// NewWorkerCreatedEvent creates a new worker created event
func NewWorkerCreatedEvent(workerID, computeResourceID string) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeWorkerCreated,
		Source:    "worker-lifecycle",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"workerId":          workerID,
			"computeResourceId": computeResourceID,
		},
	}
}

// NewWorkerHeartbeatEvent creates a new worker heartbeat event
func NewWorkerHeartbeatEvent(workerID string, metrics *WorkerMetrics) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeWorkerHeartbeat,
		Source:    "worker-lifecycle",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"workerId": workerID,
			"metrics":  metrics,
		},
	}
}

// NewDataStagedEvent creates a new data staged event
func NewDataStagedEvent(filePath, workerID string, sizeBytes int64) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeDataStaged,
		Source:    "data-mover",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"filePath":  filePath,
			"workerId":  workerID,
			"sizeBytes": sizeBytes,
		},
	}
}

// NewAuditEvent creates a new audit event
func NewAuditEvent(userID, action, resource, resourceID string) *DomainEvent {
	return &DomainEvent{
		ID:        generateEventID(),
		Type:      EventTypeUserAction,
		Source:    "audit-logger",
		Timestamp: time.Now(),
		Data: map[string]interface{}{
			"userId":     userID,
			"action":     action,
			"resource":   resource,
			"resourceId": resourceID,
		},
	}
}

// Helper function to generate event IDs
func generateEventID() string {
	// Use timestamp + random string for better uniqueness
	// In production, consider using crypto/rand or google/uuid
	return fmt.Sprintf("evt_%s_%d_%s", "default", time.Now().UnixNano(), randomString(8))
}

// Helper function to generate random strings
func randomString(length int) string {
	bytes := make([]byte, length/2+1)
	rand.Read(bytes)
	return hex.EncodeToString(bytes)[:length]
}
