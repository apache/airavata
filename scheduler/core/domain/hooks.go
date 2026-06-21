package domain

import (
	"context"
	"time"
)

// TaskStateChangeHook is called whenever a task state changes
type TaskStateChangeHook interface {
	OnTaskStateChange(ctx context.Context, taskID string, from, to TaskStatus, timestamp time.Time, message string)
}

// WorkerStateChangeHook is called whenever a worker state changes
type WorkerStateChangeHook interface {
	OnWorkerStateChange(ctx context.Context, workerID string, from, to WorkerStatus, timestamp time.Time, message string)
}

// ExperimentStateChangeHook is called whenever an experiment state changes
type ExperimentStateChangeHook interface {
	OnExperimentStateChange(ctx context.Context, experimentID string, from, to ExperimentStatus, timestamp time.Time, message string)
}

// StateChangeHookRegistry manages all state change hooks
type StateChangeHookRegistry struct {
	taskHooks       []TaskStateChangeHook
	workerHooks     []WorkerStateChangeHook
	experimentHooks []ExperimentStateChangeHook
}

// NewStateChangeHookRegistry creates a new hook registry
func NewStateChangeHookRegistry() *StateChangeHookRegistry {
	return &StateChangeHookRegistry{
		taskHooks:       make([]TaskStateChangeHook, 0),
		workerHooks:     make([]WorkerStateChangeHook, 0),
		experimentHooks: make([]ExperimentStateChangeHook, 0),
	}
}

// RegisterTaskHook registers a task state change hook
func (r *StateChangeHookRegistry) RegisterTaskHook(hook TaskStateChangeHook) {
	r.taskHooks = append(r.taskHooks, hook)
}

// RegisterWorkerHook registers a worker state change hook
func (r *StateChangeHookRegistry) RegisterWorkerHook(hook WorkerStateChangeHook) {
	r.workerHooks = append(r.workerHooks, hook)
}

// RegisterExperimentHook registers an experiment state change hook
func (r *StateChangeHookRegistry) RegisterExperimentHook(hook ExperimentStateChangeHook) {
	r.experimentHooks = append(r.experimentHooks, hook)
}

// NotifyTaskStateChange notifies all registered task hooks of a state change
func (r *StateChangeHookRegistry) NotifyTaskStateChange(ctx context.Context, taskID string, from, to TaskStatus, timestamp time.Time, message string) {
	for _, hook := range r.taskHooks {
		hook.OnTaskStateChange(ctx, taskID, from, to, timestamp, message)
	}
}

// NotifyWorkerStateChange notifies all registered worker hooks of a state change
func (r *StateChangeHookRegistry) NotifyWorkerStateChange(ctx context.Context, workerID string, from, to WorkerStatus, timestamp time.Time, message string) {
	for _, hook := range r.workerHooks {
		hook.OnWorkerStateChange(ctx, workerID, from, to, timestamp, message)
	}
}

// NotifyExperimentStateChange notifies all registered experiment hooks of a state change
func (r *StateChangeHookRegistry) NotifyExperimentStateChange(ctx context.Context, experimentID string, from, to ExperimentStatus, timestamp time.Time, message string) {
	for _, hook := range r.experimentHooks {
		hook.OnExperimentStateChange(ctx, experimentID, from, to, timestamp, message)
	}
}
