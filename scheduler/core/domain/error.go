package domain

import "errors"

// Domain-specific errors

var (
	// Resource errors
	ErrResourceNotFound         = errors.New("resource not found")
	ErrResourceAlreadyExists    = errors.New("resource already exists")
	ErrResourceInUse            = errors.New("resource is currently in use")
	ErrResourceUnavailable      = errors.New("resource is unavailable")
	ErrInvalidResourceType      = errors.New("invalid resource type")
	ErrResourceValidationFailed = errors.New("resource validation failed")

	// Credential errors
	ErrCredentialNotFound         = errors.New("credential not found")
	ErrCredentialAccessDenied     = errors.New("credential access denied")
	ErrCredentialDecryptionFailed = errors.New("credential decryption failed")
	ErrCredentialEncryptionFailed = errors.New("credential encryption failed")
	ErrInvalidCredentialType      = errors.New("invalid credential type")

	// Experiment errors
	ErrExperimentNotFound         = errors.New("experiment not found")
	ErrExperimentAlreadyExists    = errors.New("experiment already exists")
	ErrExperimentInProgress       = errors.New("experiment is currently in progress")
	ErrExperimentCompleted        = errors.New("experiment is already completed")
	ErrExperimentCancelled        = errors.New("experiment is cancelled")
	ErrInvalidExperimentState     = errors.New("invalid experiment state")
	ErrExperimentValidationFailed = errors.New("experiment validation failed")

	// Task errors
	ErrTaskNotFound        = errors.New("task not found")
	ErrTaskAlreadyAssigned = errors.New("task is already assigned")
	ErrTaskNotAssigned     = errors.New("task is not assigned")
	ErrTaskInProgress      = errors.New("task is currently in progress")
	ErrTaskCompleted       = errors.New("task is already completed")
	ErrTaskFailed          = errors.New("task has failed")
	ErrTaskCancelled       = errors.New("task is cancelled")
	ErrInvalidTaskState    = errors.New("invalid task state")
	ErrTaskRetryExhausted  = errors.New("task retry limit exceeded")

	// Worker errors
	ErrWorkerNotFound      = errors.New("worker not found")
	ErrWorkerAlreadyExists = errors.New("worker already exists")
	ErrWorkerInUse         = errors.New("worker is currently in use")
	ErrWorkerUnavailable   = errors.New("worker is unavailable")
	ErrWorkerTimeout       = errors.New("worker timeout")
	ErrWorkerFailure       = errors.New("worker failure")
	ErrInvalidWorkerState  = errors.New("invalid worker state")

	// Scheduling errors
	ErrNoAvailableWorkers          = errors.New("no available workers")
	ErrSchedulingFailed            = errors.New("scheduling failed")
	ErrCostOptimizationFailed      = errors.New("cost optimization failed")
	ErrResourceConstraintsViolated = errors.New("resource constraints violated")

	// Data movement errors
	ErrDataTransferFailed    = errors.New("data transfer failed")
	ErrDataIntegrityFailed   = errors.New("data integrity check failed")
	ErrCacheOperationFailed  = errors.New("cache operation failed")
	ErrLineageTrackingFailed = errors.New("lineage tracking failed")
	ErrFileNotFound          = errors.New("file not found")
	ErrFileAccessDenied      = errors.New("file access denied")

	// Authentication/Authorization errors
	ErrUnauthorized       = errors.New("unauthorized access")
	ErrForbidden          = errors.New("forbidden access")
	ErrInvalidCredentials = errors.New("invalid credentials")
	ErrTokenExpired       = errors.New("token expired")
	ErrTokenInvalid       = errors.New("invalid token")
	ErrUserNotFound       = errors.New("user not found")
	ErrUserAlreadyExists  = errors.New("user already exists")
	ErrGroupNotFound      = errors.New("group not found")
	ErrGroupAlreadyExists = errors.New("group already exists")
	ErrPermissionDenied   = errors.New("permission denied")

	// Validation errors
	ErrValidationFailed = errors.New("validation failed")
	ErrInvalidParameter = errors.New("invalid parameter")
	ErrMissingParameter = errors.New("missing required parameter")
	ErrInvalidFormat    = errors.New("invalid format")
	ErrOutOfRange       = errors.New("value out of range")

	// System errors
	ErrInternalError       = errors.New("internal error")
	ErrServiceUnavailable  = errors.New("service unavailable")
	ErrTimeout             = errors.New("operation timeout")
	ErrConcurrencyConflict = errors.New("concurrency conflict")
	ErrDatabaseError       = errors.New("database error")
	ErrNetworkError        = errors.New("network error")
	ErrConfigurationError  = errors.New("configuration error")
)

// DomainError represents a domain-specific error with additional context
type DomainError struct {
	Code    string `json:"code"`
	Message string `json:"message"`
	Details string `json:"details,omitempty"`
	Cause   error  `json:"-"`
}

func (e *DomainError) Error() string {
	if e.Cause != nil {
		return e.Message + ": " + e.Cause.Error()
	}
	return e.Message
}

func (e *DomainError) Unwrap() error {
	return e.Cause
}

// NewDomainError creates a new domain error
func NewDomainError(code, message string, cause error) *DomainError {
	return &DomainError{
		Code:    code,
		Message: message,
		Cause:   cause,
	}
}

// NewDomainErrorWithDetails creates a new domain error with details
func NewDomainErrorWithDetails(code, message, details string, cause error) *DomainError {
	return &DomainError{
		Code:    code,
		Message: message,
		Details: details,
		Cause:   cause,
	}
}

// Common error codes
const (
	ErrCodeResourceNotFound         = "RESOURCE_NOT_FOUND"
	ErrCodeResourceAlreadyExists    = "RESOURCE_ALREADY_EXISTS"
	ErrCodeResourceInUse            = "RESOURCE_IN_USE"
	ErrCodeResourceUnavailable      = "RESOURCE_UNAVAILABLE"
	ErrCodeInvalidResourceType      = "INVALID_RESOURCE_TYPE"
	ErrCodeResourceValidationFailed = "RESOURCE_VALIDATION_FAILED"

	ErrCodeCredentialNotFound         = "CREDENTIAL_NOT_FOUND"
	ErrCodeCredentialAccessDenied     = "CREDENTIAL_ACCESS_DENIED"
	ErrCodeCredentialDecryptionFailed = "CREDENTIAL_DECRYPTION_FAILED"
	ErrCodeCredentialEncryptionFailed = "CREDENTIAL_ENCRYPTION_FAILED"
	ErrCodeInvalidCredentialType      = "INVALID_CREDENTIAL_TYPE"

	ErrCodeExperimentNotFound         = "EXPERIMENT_NOT_FOUND"
	ErrCodeExperimentAlreadyExists    = "EXPERIMENT_ALREADY_EXISTS"
	ErrCodeExperimentInProgress       = "EXPERIMENT_IN_PROGRESS"
	ErrCodeExperimentCompleted        = "EXPERIMENT_COMPLETED"
	ErrCodeExperimentCancelled        = "EXPERIMENT_CANCELLED"
	ErrCodeInvalidExperimentState     = "INVALID_EXPERIMENT_STATE"
	ErrCodeExperimentValidationFailed = "EXPERIMENT_VALIDATION_FAILED"

	ErrCodeTaskNotFound        = "TASK_NOT_FOUND"
	ErrCodeTaskAlreadyAssigned = "TASK_ALREADY_ASSIGNED"
	ErrCodeTaskNotAssigned     = "TASK_NOT_ASSIGNED"
	ErrCodeTaskInProgress      = "TASK_IN_PROGRESS"
	ErrCodeTaskCompleted       = "TASK_COMPLETED"
	ErrCodeTaskFailed          = "TASK_FAILED"
	ErrCodeTaskCancelled       = "TASK_CANCELLED"
	ErrCodeInvalidTaskState    = "INVALID_TASK_STATE"
	ErrCodeTaskRetryExhausted  = "TASK_RETRY_EXHAUSTED"

	ErrCodeWorkerNotFound      = "WORKER_NOT_FOUND"
	ErrCodeWorkerAlreadyExists = "WORKER_ALREADY_EXISTS"
	ErrCodeWorkerInUse         = "WORKER_IN_USE"
	ErrCodeWorkerUnavailable   = "WORKER_UNAVAILABLE"
	ErrCodeWorkerTimeout       = "WORKER_TIMEOUT"
	ErrCodeWorkerFailure       = "WORKER_FAILURE"
	ErrCodeInvalidWorkerState  = "INVALID_WORKER_STATE"

	ErrCodeNoAvailableWorkers          = "NO_AVAILABLE_WORKERS"
	ErrCodeSchedulingFailed            = "SCHEDULING_FAILED"
	ErrCodeCostOptimizationFailed      = "COST_OPTIMIZATION_FAILED"
	ErrCodeResourceConstraintsViolated = "RESOURCE_CONSTRAINTS_VIOLATED"

	ErrCodeDataTransferFailed    = "DATA_TRANSFER_FAILED"
	ErrCodeDataIntegrityFailed   = "DATA_INTEGRITY_FAILED"
	ErrCodeCacheOperationFailed  = "CACHE_OPERATION_FAILED"
	ErrCodeLineageTrackingFailed = "LINEAGE_TRACKING_FAILED"
	ErrCodeFileNotFound          = "FILE_NOT_FOUND"
	ErrCodeFileAccessDenied      = "FILE_ACCESS_DENIED"

	ErrCodeUnauthorized       = "UNAUTHORIZED"
	ErrCodeForbidden          = "FORBIDDEN"
	ErrCodeInvalidCredentials = "INVALID_CREDENTIALS"
	ErrCodeTokenExpired       = "TOKEN_EXPIRED"
	ErrCodeTokenInvalid       = "TOKEN_INVALID"
	ErrCodeUserNotFound       = "USER_NOT_FOUND"
	ErrCodeUserAlreadyExists  = "USER_ALREADY_EXISTS"
	ErrCodeGroupNotFound      = "GROUP_NOT_FOUND"
	ErrCodeGroupAlreadyExists = "GROUP_ALREADY_EXISTS"
	ErrCodePermissionDenied   = "PERMISSION_DENIED"

	ErrCodeValidationFailed = "VALIDATION_FAILED"
	ErrCodeInvalidParameter = "INVALID_PARAMETER"
	ErrCodeMissingParameter = "MISSING_PARAMETER"
	ErrCodeInvalidFormat    = "INVALID_FORMAT"
	ErrCodeOutOfRange       = "OUT_OF_RANGE"

	ErrCodeInternalError       = "INTERNAL_ERROR"
	ErrCodeServiceUnavailable  = "SERVICE_UNAVAILABLE"
	ErrCodeTimeout             = "TIMEOUT"
	ErrCodeConcurrencyConflict = "CONCURRENCY_CONFLICT"
	ErrCodeDatabaseError       = "DATABASE_ERROR"
	ErrCodeNetworkError        = "NETWORK_ERROR"
	ErrCodeConfigurationError  = "CONFIGURATION_ERROR"
)
