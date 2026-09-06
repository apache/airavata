package testutil

import (
	"context"
	"fmt"
	"sync"
	"time"

	"github.com/apache/airavata/scheduler/core/domain"
)

// TestStateChangeHook captures state changes for test validation
type TestStateChangeHook struct {
	mu sync.RWMutex

	// Task state changes
	taskStateChanges []TaskStateChange

	// Worker state changes
	workerStateChanges []WorkerStateChange

	// Experiment state changes
	experimentStateChanges []ExperimentStateChange
}

// TaskStateChange represents a task state transition
type TaskStateChange struct {
	TaskID    string
	From      domain.TaskStatus
	To        domain.TaskStatus
	Timestamp time.Time
	Message   string
}

// WorkerStateChange represents a worker state transition
type WorkerStateChange struct {
	WorkerID  string
	From      domain.WorkerStatus
	To        domain.WorkerStatus
	Timestamp time.Time
	Message   string
}

// ExperimentStateChange represents an experiment state transition
type ExperimentStateChange struct {
	ExperimentID string
	From         domain.ExperimentStatus
	To           domain.ExperimentStatus
	Timestamp    time.Time
	Message      string
}

// NewTestStateChangeHook creates a new test state change hook
func NewTestStateChangeHook() *TestStateChangeHook {
	return &TestStateChangeHook{
		taskStateChanges:       make([]TaskStateChange, 0),
		workerStateChanges:     make([]WorkerStateChange, 0),
		experimentStateChanges: make([]ExperimentStateChange, 0),
	}
}

// OnTaskStateChange implements TaskStateChangeHook
func (h *TestStateChangeHook) OnTaskStateChange(ctx context.Context, taskID string, from, to domain.TaskStatus, timestamp time.Time, message string) {
	h.mu.Lock()
	defer h.mu.Unlock()

	change := TaskStateChange{
		TaskID:    taskID,
		From:      from,
		To:        to,
		Timestamp: timestamp,
		Message:   message,
	}

	h.taskStateChanges = append(h.taskStateChanges, change)
	fmt.Printf("HOOK: Task %s state change: %s -> %s (at %s) - %s\n", taskID, from, to, timestamp.Format("15:04:05.000"), message)
}

// OnWorkerStateChange implements WorkerStateChangeHook
func (h *TestStateChangeHook) OnWorkerStateChange(ctx context.Context, workerID string, from, to domain.WorkerStatus, timestamp time.Time, message string) {
	h.mu.Lock()
	defer h.mu.Unlock()

	change := WorkerStateChange{
		WorkerID:  workerID,
		From:      from,
		To:        to,
		Timestamp: timestamp,
		Message:   message,
	}

	h.workerStateChanges = append(h.workerStateChanges, change)
	fmt.Printf("HOOK: Worker %s state change: %s -> %s (at %s) - %s\n", workerID, from, to, timestamp.Format("15:04:05.000"), message)
}

// OnExperimentStateChange implements ExperimentStateChangeHook
func (h *TestStateChangeHook) OnExperimentStateChange(ctx context.Context, experimentID string, from, to domain.ExperimentStatus, timestamp time.Time, message string) {
	h.mu.Lock()
	defer h.mu.Unlock()

	change := ExperimentStateChange{
		ExperimentID: experimentID,
		From:         from,
		To:           to,
		Timestamp:    timestamp,
		Message:      message,
	}

	h.experimentStateChanges = append(h.experimentStateChanges, change)
	fmt.Printf("HOOK: Experiment %s state change: %s -> %s (at %s) - %s\n", experimentID, from, to, timestamp.Format("15:04:05.000"), message)
}

// GetTaskStateChanges returns all task state changes
func (h *TestStateChangeHook) GetTaskStateChanges() []TaskStateChange {
	h.mu.RLock()
	defer h.mu.RUnlock()

	// Return a copy to avoid race conditions
	result := make([]TaskStateChange, len(h.taskStateChanges))
	copy(result, h.taskStateChanges)
	return result
}

// GetWorkerStateChanges returns all worker state changes
func (h *TestStateChangeHook) GetWorkerStateChanges() []WorkerStateChange {
	h.mu.RLock()
	defer h.mu.RUnlock()

	// Return a copy to avoid race conditions
	result := make([]WorkerStateChange, len(h.workerStateChanges))
	copy(result, h.workerStateChanges)
	return result
}

// GetExperimentStateChanges returns all experiment state changes
func (h *TestStateChangeHook) GetExperimentStateChanges() []ExperimentStateChange {
	h.mu.RLock()
	defer h.mu.RUnlock()

	// Return a copy to avoid race conditions
	result := make([]ExperimentStateChange, len(h.experimentStateChanges))
	copy(result, h.experimentStateChanges)
	return result
}

// GetTaskStateChangesForTask returns state changes for a specific task
func (h *TestStateChangeHook) GetTaskStateChangesForTask(taskID string) []TaskStateChange {
	h.mu.RLock()
	defer h.mu.RUnlock()

	var result []TaskStateChange
	for _, change := range h.taskStateChanges {
		if change.TaskID == taskID {
			result = append(result, change)
		}
	}
	return result
}

// GetWorkerStateChangesForWorker returns state changes for a specific worker
func (h *TestStateChangeHook) GetWorkerStateChangesForWorker(workerID string) []WorkerStateChange {
	h.mu.RLock()
	defer h.mu.RUnlock()

	var result []WorkerStateChange
	for _, change := range h.workerStateChanges {
		if change.WorkerID == workerID {
			result = append(result, change)
		}
	}
	return result
}

// GetExperimentStateChangesForExperiment returns state changes for a specific experiment
func (h *TestStateChangeHook) GetExperimentStateChangesForExperiment(experimentID string) []ExperimentStateChange {
	h.mu.RLock()
	defer h.mu.RUnlock()

	var result []ExperimentStateChange
	for _, change := range h.experimentStateChanges {
		if change.ExperimentID == experimentID {
			result = append(result, change)
		}
	}
	return result
}

// Clear clears all captured state changes
func (h *TestStateChangeHook) Clear() {
	h.mu.Lock()
	defer h.mu.Unlock()

	h.taskStateChanges = h.taskStateChanges[:0]
	h.workerStateChanges = h.workerStateChanges[:0]
	h.experimentStateChanges = h.experimentStateChanges[:0]
}

// WaitForTaskStateTransitions waits for a task to progress through expected states using hooks
func (h *TestStateChangeHook) WaitForTaskStateTransitions(taskID string, expectedStates []domain.TaskStatus, timeout time.Duration) ([]domain.TaskStatus, error) {
	ctx, cancel := context.WithTimeout(context.Background(), timeout)
	defer cancel()

	ticker := time.NewTicker(100 * time.Millisecond) // Check more frequently
	defer ticker.Stop()

	var observedStates []domain.TaskStatus
	stateIndex := 0

	fmt.Printf("Waiting for task %s to progress through states: %v\n", taskID, expectedStates)

	for {
		select {
		case <-ctx.Done():
			return observedStates, fmt.Errorf("timeout waiting for task %s state transitions; observed: %v, expected: %v",
				taskID, observedStates, expectedStates)
		case <-ticker.C:
			// Get state changes for this task
			changes := h.GetTaskStateChangesForTask(taskID)

			// Build observed states from changes
			observedStates = make([]domain.TaskStatus, 0, len(changes)+1)

			// Add initial state if we have changes
			if len(changes) > 0 {
				observedStates = append(observedStates, changes[0].From)
			}

			// Add all "to" states
			for _, change := range changes {
				observedStates = append(observedStates, change.To)
			}

			// Check if we've observed all expected states
			if len(observedStates) >= len(expectedStates) {
				// Check if observed states match expected states
				allMatch := true
				for i := 0; i < len(expectedStates); i++ {
					if i >= len(observedStates) || observedStates[i] != expectedStates[i] {
						allMatch = false
						break
					}
				}

				if allMatch {
					fmt.Printf("Task %s completed all expected state transitions: %v\n", taskID, observedStates)
					return observedStates, nil
				}
			}

			// Update progress counter for logging
			if stateIndex < len(expectedStates) && len(observedStates) > stateIndex {
				if observedStates[stateIndex] == expectedStates[stateIndex] {
					stateIndex++
					fmt.Printf("Task %s reached expected state %d/%d: %s\n", taskID, stateIndex, len(expectedStates), observedStates[stateIndex-1])
				}
			}

			// Check for invalid state transitions
			if len(observedStates) > 1 {
				lastState := observedStates[len(observedStates)-2]
				currentState := observedStates[len(observedStates)-1]

				// Validate state transition is logical
				if !isValidStateTransition(lastState, currentState) {
					return observedStates, fmt.Errorf("invalid state transition detected for task %s: %s -> %s (observed: %v, expected: %v)",
						taskID, lastState, currentState, observedStates, expectedStates)
				}
			}
		}
	}
}

// isValidStateTransition validates that a state transition is logical
func isValidStateTransition(from, to domain.TaskStatus) bool {
	validTransitions := map[domain.TaskStatus][]domain.TaskStatus{
		domain.TaskStatusCreated:       {domain.TaskStatusQueued, domain.TaskStatusFailed, domain.TaskStatusCanceled},
		domain.TaskStatusQueued:        {domain.TaskStatusDataStaging, domain.TaskStatusFailed, domain.TaskStatusCanceled},
		domain.TaskStatusDataStaging:   {domain.TaskStatusEnvSetup, domain.TaskStatusFailed, domain.TaskStatusCanceled},
		domain.TaskStatusEnvSetup:      {domain.TaskStatusRunning, domain.TaskStatusFailed, domain.TaskStatusCanceled},
		domain.TaskStatusRunning:       {domain.TaskStatusOutputStaging, domain.TaskStatusFailed, domain.TaskStatusCanceled},
		domain.TaskStatusOutputStaging: {domain.TaskStatusCompleted, domain.TaskStatusFailed, domain.TaskStatusCanceled},
		domain.TaskStatusCompleted:     {}, // Terminal state
		domain.TaskStatusFailed:        {}, // Terminal state
		domain.TaskStatusCanceled:      {}, // Terminal state
	}

	allowedTransitions, exists := validTransitions[from]
	if !exists {
		return false
	}

	for _, allowed := range allowedTransitions {
		if allowed == to {
			return true
		}
	}

	return false
}
