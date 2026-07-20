package domain

// StateMachine provides centralized state transition validation
type StateMachine struct {
	validTaskTransitions       map[TaskStatus][]TaskStatus
	validWorkerTransitions     map[WorkerStatus][]WorkerStatus
	validExperimentTransitions map[ExperimentStatus][]ExperimentStatus
}

// NewStateMachine creates a new state machine with predefined valid transitions
func NewStateMachine() *StateMachine {
	return &StateMachine{
		validTaskTransitions: map[TaskStatus][]TaskStatus{
			TaskStatusCreated:       {TaskStatusQueued, TaskStatusFailed, TaskStatusCanceled},
			TaskStatusQueued:        {TaskStatusDataStaging, TaskStatusFailed, TaskStatusCanceled, TaskStatusQueued}, // Allow retry
			TaskStatusDataStaging:   {TaskStatusEnvSetup, TaskStatusFailed, TaskStatusCanceled},
			TaskStatusEnvSetup:      {TaskStatusRunning, TaskStatusFailed, TaskStatusCanceled},
			TaskStatusRunning:       {TaskStatusOutputStaging, TaskStatusFailed, TaskStatusCanceled, TaskStatusQueued}, // Allow retry
			TaskStatusOutputStaging: {TaskStatusCompleted, TaskStatusFailed, TaskStatusCanceled},
			TaskStatusCompleted:     {},                 // Terminal state
			TaskStatusFailed:        {TaskStatusQueued}, // Allow retry from failed
			TaskStatusCanceled:      {},                 // Terminal state
		},
		validWorkerTransitions: map[WorkerStatus][]WorkerStatus{
			WorkerStatusIdle: {WorkerStatusBusy},
			WorkerStatusBusy: {WorkerStatusIdle},
		},
		validExperimentTransitions: map[ExperimentStatus][]ExperimentStatus{
			ExperimentStatusCreated:   {ExperimentStatusExecuting, ExperimentStatusCanceled},
			ExperimentStatusExecuting: {ExperimentStatusCompleted, ExperimentStatusCanceled},
			ExperimentStatusCompleted: {}, // Terminal state
			ExperimentStatusCanceled:  {}, // Terminal state
		},
	}
}

// IsValidTaskTransition checks if a task state transition is valid
func (sm *StateMachine) IsValidTaskTransition(from, to TaskStatus) bool {
	validTransitions, exists := sm.validTaskTransitions[from]
	if !exists {
		return false
	}

	for _, validTo := range validTransitions {
		if validTo == to {
			return true
		}
	}
	return false
}

// IsValidWorkerTransition checks if a worker state transition is valid
func (sm *StateMachine) IsValidWorkerTransition(from, to WorkerStatus) bool {
	validTransitions, exists := sm.validWorkerTransitions[from]
	if !exists {
		return false
	}

	for _, validTo := range validTransitions {
		if validTo == to {
			return true
		}
	}
	return false
}

// IsValidExperimentTransition checks if an experiment state transition is valid
func (sm *StateMachine) IsValidExperimentTransition(from, to ExperimentStatus) bool {
	validTransitions, exists := sm.validExperimentTransitions[from]
	if !exists {
		return false
	}

	for _, validTo := range validTransitions {
		if validTo == to {
			return true
		}
	}
	return false
}

// GetValidTaskTransitions returns all valid transitions from a given task status
func (sm *StateMachine) GetValidTaskTransitions(from TaskStatus) []TaskStatus {
	return sm.validTaskTransitions[from]
}

// GetValidWorkerTransitions returns all valid transitions from a given worker status
func (sm *StateMachine) GetValidWorkerTransitions(from WorkerStatus) []WorkerStatus {
	return sm.validWorkerTransitions[from]
}

// GetValidExperimentTransitions returns all valid transitions from a given experiment status
func (sm *StateMachine) GetValidExperimentTransitions(from ExperimentStatus) []ExperimentStatus {
	return sm.validExperimentTransitions[from]
}

// IsTerminalTaskStatus checks if a task status is terminal
func (sm *StateMachine) IsTerminalTaskStatus(status TaskStatus) bool {
	return status == TaskStatusCompleted || status == TaskStatusFailed || status == TaskStatusCanceled
}

// IsTerminalWorkerStatus checks if a worker status is terminal
func (sm *StateMachine) IsTerminalWorkerStatus(status WorkerStatus) bool {
	// Workers don't have terminal states in our model
	return false
}

// IsTerminalExperimentStatus checks if an experiment status is terminal
func (sm *StateMachine) IsTerminalExperimentStatus(status ExperimentStatus) bool {
	return status == ExperimentStatusCompleted || status == ExperimentStatusCanceled
}
