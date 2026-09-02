package dto

import (
	"github.com/apache/airavata/internal/httpx"

	data "github.com/apache/airavata/api/data/model"
	model "github.com/apache/airavata/api/process/model"
)

// The four task kinds share an identity — a task id, the process it belongs to, an
// on-failure action, a retry count and an execution order — and differ only in what
// they carry. These DTOs mirror that: the shared constraints are validated by one
// helper, and each kind adds its own.
//
// None of them carries processId: it comes from the path, so a task cannot be moved
// to a different process by editing it. Nor does any carry a process type — a task
// names its process through a real foreign key now, so there is nothing to
// discriminate.

// taskCommon validates the fields every task kind shares.
func taskCommon(c *httpx.Constraints, onFailure *model.OnFailureAction, retryCount, taskOrder *int) {
	if onFailure != nil && !onFailure.Valid() {
		c.Add("onFailure", "On failure action must be one of RETRY, SKIP, EXIT")
	}
	if retryCount != nil && *retryCount < 0 {
		c.Add("retryCount", "Retry count cannot be negative")
	}
	if taskOrder != nil && *taskOrder < 0 {
		c.Add("taskOrder", "Task order cannot be negative")
	}
}

func validateStorageType(c *httpx.Constraints, field string, t *data.DataStorageType) {
	if t != nil && !t.Valid() {
		c.Add(field, "Data storage type must be one of SCP, S3")
	}
}

// DataStagingTaskRequest is the create/update payload for a staging task.
type DataStagingTaskRequest struct {
	SourceDataStorageID   *string               `json:"sourceDataStorageId"`
	SourceCredentialID    *string               `json:"sourceCredentialId"`
	SourceDataStorageType *data.DataStorageType `json:"sourceDataStorageType"`

	DestinationDataStorageID   *string               `json:"destinationDataStorageId"`
	DestinationCredentialID    *string               `json:"destinationCredentialId"`
	DestinationDataStorageType *data.DataStorageType `json:"destinationDataStorageType"`

	SourcePath      *string `json:"sourcePath"`
	DestinationPath *string `json:"destinationPath"`

	OnFailure  *model.OnFailureAction `json:"onFailure"`
	RetryCount *int                   `json:"retryCount"`
	TaskOrder  *int                   `json:"taskOrder"`
}

// Validate implements httpx.Validator.
//
// Both paths are required: a staging task that names neither where data comes from nor
// where it goes describes no transfer at all.
func (r *DataStagingTaskRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlankPtr("sourcePath", "Source path cannot be blank", r.SourcePath)
	c.NotBlankPtr("destinationPath", "Destination path cannot be blank", r.DestinationPath)
	validateStorageType(&c, "sourceDataStorageType", r.SourceDataStorageType)
	validateStorageType(&c, "destinationDataStorageType", r.DestinationDataStorageType)
	taskCommon(&c, r.OnFailure, r.RetryCount, r.TaskOrder)
	return c.Fields()
}

func ApplyDataStagingTaskRequest(dst *model.DataStagingTask, src *DataStagingTaskRequest) {
	dst.SourceDataStorageID = src.SourceDataStorageID
	dst.SourceCredentialID = src.SourceCredentialID
	dst.SourceDataStorageType = src.SourceDataStorageType
	dst.DestinationDataStorageID = src.DestinationDataStorageID
	dst.DestinationCredentialID = src.DestinationCredentialID
	dst.DestinationDataStorageType = src.DestinationDataStorageType
	dst.SourcePath = src.SourcePath
	dst.DestinationPath = src.DestinationPath
	dst.OnFailure = src.OnFailure
	dst.RetryCount = src.RetryCount
	dst.TaskOrder = src.TaskOrder
}

// DataStagingTaskResponse is the read model for a staging task.
type DataStagingTaskResponse struct {
	TaskID    string  `json:"taskId"`
	ProcessID *string `json:"processId"`

	SourceDataStorageID   *string               `json:"sourceDataStorageId"`
	SourceCredentialID    *string               `json:"sourceCredentialId"`
	SourceDataStorageType *data.DataStorageType `json:"sourceDataStorageType"`

	DestinationDataStorageID   *string               `json:"destinationDataStorageId"`
	DestinationCredentialID    *string               `json:"destinationCredentialId"`
	DestinationDataStorageType *data.DataStorageType `json:"destinationDataStorageType"`

	SourcePath      *string `json:"sourcePath"`
	DestinationPath *string `json:"destinationPath"`

	OnFailure  *model.OnFailureAction `json:"onFailure"`
	RetryCount *int                   `json:"retryCount"`
	TaskOrder  *int                   `json:"taskOrder"`
}

func ToDataStagingTaskResponse(t *model.DataStagingTask) DataStagingTaskResponse {
	return DataStagingTaskResponse{
		TaskID:                     t.ID,
		ProcessID:                  t.ProcessID,
		SourceDataStorageID:        t.SourceDataStorageID,
		SourceCredentialID:         t.SourceCredentialID,
		SourceDataStorageType:      t.SourceDataStorageType,
		DestinationDataStorageID:   t.DestinationDataStorageID,
		DestinationCredentialID:    t.DestinationCredentialID,
		DestinationDataStorageType: t.DestinationDataStorageType,
		SourcePath:                 t.SourcePath,
		DestinationPath:            t.DestinationPath,
		OnFailure:                  t.OnFailure,
		RetryCount:                 t.RetryCount,
		TaskOrder:                  t.TaskOrder,
	}
}

func ToDataStagingTaskResponses(in []model.DataStagingTask) []DataStagingTaskResponse {
	out := make([]DataStagingTaskResponse, 0, len(in))
	for i := range in {
		out = append(out, ToDataStagingTaskResponse(&in[i]))
	}
	return out
}

// JobSubmissionTaskRequest is the create/update payload for a submission task.
//
// jobId is writable rather than server-generated: it is the scheduler's identifier for
// the submitted job, learned at submission time and recorded here afterwards.
type JobSubmissionTaskRequest struct {
	JobID      *string                `json:"jobId"`
	OnFailure  *model.OnFailureAction `json:"onFailure"`
	RetryCount *int                   `json:"retryCount"`
	TaskOrder  *int                   `json:"taskOrder"`
}

// Validate implements httpx.Validator.
func (r *JobSubmissionTaskRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	taskCommon(&c, r.OnFailure, r.RetryCount, r.TaskOrder)
	return c.Fields()
}

func ApplyJobSubmissionTaskRequest(dst *model.JobSubmissionTask, src *JobSubmissionTaskRequest) {
	dst.JobId = src.JobID
	dst.OnFailure = src.OnFailure
	dst.RetryCount = src.RetryCount
	dst.TaskOrder = src.TaskOrder
}

// JobSubmissionTaskResponse is the read model for a submission task.
type JobSubmissionTaskResponse struct {
	TaskID    string  `json:"taskId"`
	ProcessID *string `json:"processId"`

	JobID      *string                `json:"jobId"`
	OnFailure  *model.OnFailureAction `json:"onFailure"`
	RetryCount *int                   `json:"retryCount"`
	TaskOrder  *int                   `json:"taskOrder"`
}

func ToJobSubmissionTaskResponse(t *model.JobSubmissionTask) JobSubmissionTaskResponse {
	return JobSubmissionTaskResponse{
		TaskID:     t.ID,
		ProcessID:  t.ProcessID,
		JobID:      t.JobId,
		OnFailure:  t.OnFailure,
		RetryCount: t.RetryCount,
		TaskOrder:  t.TaskOrder,
	}
}

func ToJobSubmissionTaskResponses(in []model.JobSubmissionTask) []JobSubmissionTaskResponse {
	out := make([]JobSubmissionTaskResponse, 0, len(in))
	for i := range in {
		out = append(out, ToJobSubmissionTaskResponse(&in[i]))
	}
	return out
}

// JobMonitoringTaskRequest is the create/update payload for a monitoring task.
type JobMonitoringTaskRequest struct {
	JobID      *string                `json:"jobId"`
	OnFailure  *model.OnFailureAction `json:"onFailure"`
	RetryCount *int                   `json:"retryCount"`
	TaskOrder  *int                   `json:"taskOrder"`
}

// Validate implements httpx.Validator.
func (r *JobMonitoringTaskRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	taskCommon(&c, r.OnFailure, r.RetryCount, r.TaskOrder)
	return c.Fields()
}

func ApplyJobMonitoringTaskRequest(dst *model.JobMonitoringTask, src *JobMonitoringTaskRequest) {
	dst.JobId = src.JobID
	dst.OnFailure = src.OnFailure
	dst.RetryCount = src.RetryCount
	dst.TaskOrder = src.TaskOrder
}

// JobMonitoringTaskResponse is the read model for a monitoring task.
type JobMonitoringTaskResponse struct {
	TaskID    string  `json:"taskId"`
	ProcessID *string `json:"processId"`

	JobID      *string                `json:"jobId"`
	OnFailure  *model.OnFailureAction `json:"onFailure"`
	RetryCount *int                   `json:"retryCount"`
	TaskOrder  *int                   `json:"taskOrder"`
}

func ToJobMonitoringTaskResponse(t *model.JobMonitoringTask) JobMonitoringTaskResponse {
	return JobMonitoringTaskResponse{
		TaskID:     t.ID,
		ProcessID:  t.ProcessID,
		JobID:      t.JobId,
		OnFailure:  t.OnFailure,
		RetryCount: t.RetryCount,
		TaskOrder:  t.TaskOrder,
	}
}

func ToJobMonitoringTaskResponses(in []model.JobMonitoringTask) []JobMonitoringTaskResponse {
	out := make([]JobMonitoringTaskResponse, 0, len(in))
	for i := range in {
		out = append(out, ToJobMonitoringTaskResponse(&in[i]))
	}
	return out
}

// InteractiveCommandTaskRequest is the create/update payload for a command task.
//
// output is writable for the same reason jobId is: it is the result of running the
// command, recorded once whatever executed it knows the answer.
type InteractiveCommandTaskRequest struct {
	Command    *string                `json:"command"`
	Output     *string                `json:"output"`
	OnFailure  *model.OnFailureAction `json:"onFailure"`
	RetryCount *int                   `json:"retryCount"`
	TaskOrder  *int                   `json:"taskOrder"`
}

// Validate implements httpx.Validator.
func (r *InteractiveCommandTaskRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlankPtr("command", "Command cannot be blank", r.Command)
	taskCommon(&c, r.OnFailure, r.RetryCount, r.TaskOrder)
	return c.Fields()
}

func ApplyInteractiveCommandTaskRequest(dst *model.InteractiveCommandTask, src *InteractiveCommandTaskRequest) {
	dst.Command = src.Command
	dst.Output = src.Output
	dst.OnFailure = src.OnFailure
	dst.RetryCount = src.RetryCount
	dst.TaskOrder = src.TaskOrder
}

// InteractiveCommandTaskResponse is the read model for a command task.
type InteractiveCommandTaskResponse struct {
	TaskID    string  `json:"taskId"`
	ProcessID *string `json:"processId"`

	Command    *string                `json:"command"`
	Output     *string                `json:"output"`
	OnFailure  *model.OnFailureAction `json:"onFailure"`
	RetryCount *int                   `json:"retryCount"`
	TaskOrder  *int                   `json:"taskOrder"`
}

func ToInteractiveCommandTaskResponse(t *model.InteractiveCommandTask) InteractiveCommandTaskResponse {
	return InteractiveCommandTaskResponse{
		TaskID:     t.ID,
		ProcessID:  t.ProcessID,
		Command:    t.Command,
		Output:     t.Output,
		OnFailure:  t.OnFailure,
		RetryCount: t.RetryCount,
		TaskOrder:  t.TaskOrder,
	}
}

func ToInteractiveCommandTaskResponses(in []model.InteractiveCommandTask) []InteractiveCommandTaskResponse {
	out := make([]InteractiveCommandTaskResponse, 0, len(in))
	for i := range in {
		out = append(out, ToInteractiveCommandTaskResponse(&in[i]))
	}
	return out
}
