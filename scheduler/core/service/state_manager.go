package services

import (
	"context"
	"fmt"
	"log"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
	ports "github.com/apache/airavata/scheduler/core/port"
)

// StateManager provides centralized state management with database persistence guarantees
type StateManager struct {
	repo         ports.RepositoryPort
	stateHooks   *domain.StateChangeHookRegistry
	stateMachine *domain.StateMachine
	eventPort    ports.EventPort
}

// NewStateManager creates a new StateManager instance
func NewStateManager(repo ports.RepositoryPort, eventPort ports.EventPort) *StateManager {
	return &StateManager{
		repo:         repo,
		stateHooks:   domain.NewStateChangeHookRegistry(),
		stateMachine: domain.NewStateMachine(),
		eventPort:    eventPort,
	}
}

// RegisterStateChangeHook registers a state change hook
func (sm *StateManager) RegisterStateChangeHook(hook interface{}) {
	if taskHook, ok := hook.(domain.TaskStateChangeHook); ok {
		sm.stateHooks.RegisterTaskHook(taskHook)
	}
	if workerHook, ok := hook.(domain.WorkerStateChangeHook); ok {
		sm.stateHooks.RegisterWorkerHook(workerHook)
	}
	if experimentHook, ok := hook.(domain.ExperimentStateChangeHook); ok {
		sm.stateHooks.RegisterExperimentHook(experimentHook)
	}
}

// TransitionTaskState performs a transactional task state transition
func (sm *StateManager) TransitionTaskState(ctx context.Context, taskID string, from, to domain.TaskStatus, metadata map[string]interface{}) error {
	// Validate transition is legal
	if !sm.stateMachine.IsValidTaskTransition(from, to) {
		return fmt.Errorf("invalid task state transition from %s to %s", from, to)
	}

	// Get current task
	task, err := sm.repo.GetTaskByID(ctx, taskID)
	if err != nil {
		return fmt.Errorf("failed to get task %s: %w", taskID, err)
	}

	if task == nil {
		return fmt.Errorf("task %s not found", taskID)
	}

	// Verify current state matches expected from state
	if task.Status != from {
		return fmt.Errorf("task %s current state %s does not match expected from state %s", taskID, task.Status, from)
	}

	// Perform state transition in database transaction
	err = sm.repo.WithTransaction(ctx, func(txCtx context.Context) error {
		// Update task status
		task.Status = to
		task.UpdatedAt = time.Now()

		// Merge metadata from request into task metadata
		if task.Metadata == nil {
			task.Metadata = make(map[string]interface{})
		}
		for key, value := range metadata {
			task.Metadata[key] = value
		}

		// Set timestamps based on state
		now := time.Now()
		switch to {
		case domain.TaskStatusRunning:
			if task.StartedAt == nil {
				task.StartedAt = &now
			}
		case domain.TaskStatusCompleted, domain.TaskStatusFailed, domain.TaskStatusCanceled:
			if task.CompletedAt == nil {
				task.CompletedAt = &now
			}
			if task.StartedAt != nil && task.Duration == nil {
				duration := now.Sub(*task.StartedAt)
				task.Duration = &duration
			}
		}

		// Update task in database
		if err := sm.repo.UpdateTask(txCtx, task); err != nil {
			return fmt.Errorf("failed to update task in database: %w", err)
		}

		return nil
	})

	if err != nil {
		return fmt.Errorf("failed to transition task state: %w", err)
	}

	// State change persisted successfully, now trigger hooks
	sm.stateHooks.NotifyTaskStateChange(ctx, taskID, from, to, time.Now(), fmt.Sprintf("State transition: %s -> %s", from, to))

	// Publish state change event for distributed coordination
	event := &domain.DomainEvent{
		ID:   fmt.Sprintf("task-state-change-%s-%d", taskID, time.Now().UnixNano()),
		Type: "task.state.changed",
		Data: map[string]interface{}{
			"taskId":     taskID,
			"fromStatus": string(from),
			"toStatus":   string(to),
			"timestamp":  time.Now(),
			"metadata":   metadata,
		},
	}

	if err := sm.eventPort.Publish(ctx, event); err != nil {
		log.Printf("Failed to publish task state change event: %v", err)
		// Don't fail the state transition if event publishing fails
	}

	log.Printf("Task %s state transitioned from %s to %s", taskID, from, to)
	return nil
}

// TransitionWorkerState performs a transactional worker state transition
func (sm *StateManager) TransitionWorkerState(ctx context.Context, workerID string, from, to domain.WorkerStatus, metadata map[string]interface{}) error {
	// Validate transition is legal
	if !sm.stateMachine.IsValidWorkerTransition(from, to) {
		return fmt.Errorf("invalid worker state transition from %s to %s", from, to)
	}

	// Get current worker
	worker, err := sm.repo.GetWorkerByID(ctx, workerID)
	if err != nil {
		return fmt.Errorf("failed to get worker %s: %w", workerID, err)
	}

	if worker == nil {
		return fmt.Errorf("worker %s not found", workerID)
	}

	// Verify current state matches expected from state
	if worker.Status != from {
		return fmt.Errorf("worker %s current state %s does not match expected from state %s", workerID, worker.Status, from)
	}

	// Perform state transition in database transaction
	err = sm.repo.WithTransaction(ctx, func(txCtx context.Context) error {
		// Update worker status
		worker.Status = to
		worker.UpdatedAt = time.Now()

		// Clear current task ID when transitioning to idle
		if to == domain.WorkerStatusIdle {
			worker.CurrentTaskID = ""
		}

		// Update worker in database
		if err := sm.repo.UpdateWorker(txCtx, worker); err != nil {
			return fmt.Errorf("failed to update worker in database: %w", err)
		}

		return nil
	})

	if err != nil {
		return fmt.Errorf("failed to transition worker state: %w", err)
	}

	// State change persisted successfully, now trigger hooks
	sm.stateHooks.NotifyWorkerStateChange(ctx, workerID, from, to, time.Now(), fmt.Sprintf("State transition: %s -> %s", from, to))

	// Publish state change event for distributed coordination
	event := &domain.DomainEvent{
		ID:   fmt.Sprintf("worker-state-change-%s-%d", workerID, time.Now().UnixNano()),
		Type: "worker.state.changed",
		Data: map[string]interface{}{
			"workerId":   workerID,
			"fromStatus": string(from),
			"toStatus":   string(to),
			"timestamp":  time.Now(),
			"metadata":   metadata,
		},
	}

	if err := sm.eventPort.Publish(ctx, event); err != nil {
		log.Printf("Failed to publish worker state change event: %v", err)
		// Don't fail the state transition if event publishing fails
	}

	log.Printf("Worker %s state transitioned from %s to %s", workerID, from, to)
	return nil
}

// TransitionExperimentState performs a transactional experiment state transition
func (sm *StateManager) TransitionExperimentState(ctx context.Context, experimentID string, from, to domain.ExperimentStatus, metadata map[string]interface{}) error {
	// Validate transition is legal
	if !sm.stateMachine.IsValidExperimentTransition(from, to) {
		return fmt.Errorf("invalid experiment state transition from %s to %s", from, to)
	}

	// Get current experiment
	experiment, err := sm.repo.GetExperimentByID(ctx, experimentID)
	if err != nil {
		return fmt.Errorf("failed to get experiment %s: %w", experimentID, err)
	}

	if experiment == nil {
		return fmt.Errorf("experiment %s not found", experimentID)
	}

	// Verify current state matches expected from state
	if experiment.Status != from {
		return fmt.Errorf("experiment %s current state %s does not match expected from state %s", experimentID, experiment.Status, from)
	}

	// Perform state transition in database transaction
	err = sm.repo.WithTransaction(ctx, func(txCtx context.Context) error {
		// Update experiment status
		experiment.Status = to
		experiment.UpdatedAt = time.Now()

		// Set timestamps based on state
		now := time.Now()
		switch to {
		case domain.ExperimentStatusExecuting:
			if experiment.StartedAt == nil {
				experiment.StartedAt = &now
			}
		case domain.ExperimentStatusCompleted, domain.ExperimentStatusCanceled:
			if experiment.CompletedAt == nil {
				experiment.CompletedAt = &now
			}
		}

		// Update experiment in database
		if err := sm.repo.UpdateExperiment(txCtx, experiment); err != nil {
			return fmt.Errorf("failed to update experiment in database: %w", err)
		}

		return nil
	})

	if err != nil {
		return fmt.Errorf("failed to transition experiment state: %w", err)
	}

	// State change persisted successfully, now trigger hooks
	sm.stateHooks.NotifyExperimentStateChange(ctx, experimentID, from, to, time.Now(), fmt.Sprintf("State transition: %s -> %s", from, to))

	// Publish state change event for distributed coordination
	event := &domain.DomainEvent{
		ID:   fmt.Sprintf("experiment-state-change-%s-%d", experimentID, time.Now().UnixNano()),
		Type: "experiment.state.changed",
		Data: map[string]interface{}{
			"experimentId": experimentID,
			"fromStatus":   string(from),
			"toStatus":     string(to),
			"timestamp":    time.Now(),
			"metadata":     metadata,
		},
	}

	if err := sm.eventPort.Publish(ctx, event); err != nil {
		log.Printf("Failed to publish experiment state change event: %v", err)
		// Don't fail the state transition if event publishing fails
	}

	log.Printf("Experiment %s state transitioned from %s to %s", experimentID, from, to)
	return nil
}

// GetStateMachine returns the state machine instance
func (sm *StateManager) GetStateMachine() *domain.StateMachine {
	return sm.stateMachine
}

// GetStateHooks returns the state hooks registry
func (sm *StateManager) GetStateHooks() *domain.StateChangeHookRegistry {
	return sm.stateHooks
}
