// Package dto is the wire contract of the process vertical.
//
// There is one resource here — the process — and everything a run needs hangs off it.
// A batch job is not a resource of its own: what a BATCH_JOB needs beyond any other
// process is carried in the batchProcess section of a process body, created with the
// process, read back nested inside it, and removed with it. The same holds for the
// template input and output mappings.
package dto

import (
	"strconv"

	"github.com/apache/airavata/internal/httpx"

	applicationdto "github.com/apache/airavata/api/application/dto"
	model "github.com/apache/airavata/api/process/model"
)

// BatchProcessRequest is the batchProcess section of a process request.
//
// The resource request is carried here rather than copied from the deployment's
// default, which is what lets a caller ask for different resources for a particular
// run.
//
// jobId is writable rather than server-generated, for the same reason the submission
// task's is: it is the scheduler's identifier for the submitted job, learned at
// submission time and recorded afterwards.
type BatchProcessRequest struct {
	DeploymentID   string                                `json:"deploymentId"`
	JobID          *string                               `json:"jobId"`
	JobName        *string                               `json:"jobName"`
	BatchJobConfig *applicationdto.BatchJobConfigRequest `json:"batchJobConfig"`
}

// Validate implements httpx.Validator.
func (r *BatchProcessRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("deploymentId", "Deployment id cannot be blank", r.DeploymentID)
	if r.BatchJobConfig == nil {
		c.Add("batchJobConfig", "Batch job config cannot be null")
	} else {
		c.Nested("batchJobConfig", r.BatchJobConfig)
	}
	return c.Fields()
}

// BatchProcessResponse is the batchProcess section of a process response. The nested
// config is a snapshot of what this run actually asked for.
type BatchProcessResponse struct {
	BatchProcessID string                                 `json:"batchProcessId"`
	DeploymentID   *string                                `json:"deploymentId"`
	JobID          *string                                `json:"jobId"`
	JobName        *string                                `json:"jobName"`
	BatchJobConfig *applicationdto.BatchJobConfigResponse `json:"batchJobConfig"`
}

// InputMapping binds one of the template's declared inputs to a value for this run.
// It is used for both reads and writes.
//
// TemplateInputMappingID is echoed back on reads but ignored on writes: an update
// replaces the mapping set wholesale, the same way a template's declarations are
// replaced, so child ids are regenerated rather than matched.
type InputMapping struct {
	TemplateInputMappingID string `json:"templateInputMappingId"`
	TemplateInputID        string `json:"templateInputId"`

	// Value is a JSON document, either {"value": "..."} for a single value or
	// {"values": [...]} for a list.
	Value *string `json:"value"`
}

// Validate implements httpx.Validator.
func (m *InputMapping) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("templateInputId", "Template input id cannot be blank", m.TemplateInputID)
	return c.Fields()
}

// OutputMapping binds one of the template's declared outputs to a value for this run.
type OutputMapping struct {
	TemplateOutputMappingID string `json:"templateOutputMappingId"`
	TemplateOutputID        string `json:"templateOutputId"`

	// Value is a JSON document, either {"value": "..."} for a single value or
	// {"values": [...]} for a list.
	Value *string `json:"value"`
}

// Validate implements httpx.Validator.
func (m *OutputMapping) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("templateOutputId", "Template output id cannot be blank", m.TemplateOutputID)
	return c.Fields()
}

// Request is the create/update payload for a process.
//
// There is no owner field: ownership comes from the access token.
type Request struct {
	ProcessType *model.ProcessType `json:"processType"`

	// BatchProcess is required when ProcessType is BATCH_JOB and rejected otherwise.
	BatchProcess *BatchProcessRequest `json:"batchProcess"`

	InputMappings  []InputMapping  `json:"inputMappings"`
	OutputMappings []OutputMapping `json:"outputMappings"`
}

// Validate implements httpx.Validator.
//
// Which sections a body may carry follows from its process type, so the type is
// checked first and the sections against it: a BATCH_JOB without a batchProcess
// describes no job at all, and a batchProcess on any other kind of process is a
// caller's mistake rather than something to store and ignore.
func (r *Request) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotNil("processType", "Process type cannot be null", r.ProcessType)
	if r.ProcessType != nil && !r.ProcessType.Valid() {
		c.Add("processType", "Process type must be one of BATCH_JOB, CLOUD_JOB")
	}

	isBatch := r.ProcessType != nil && *r.ProcessType == model.ProcessTypeBatchJob
	switch {
	case isBatch && r.BatchProcess == nil:
		c.Add("batchProcess", "Batch process cannot be null for a BATCH_JOB process")
	case r.BatchProcess != nil && r.ProcessType != nil && !isBatch:
		c.Add("batchProcess", "Batch process is only accepted for a BATCH_JOB process")
	case r.BatchProcess != nil:
		c.Nested("batchProcess", r.BatchProcess)
	}

	for i := range r.InputMappings {
		c.Nested(indexed("inputMappings", i), &r.InputMappings[i])
	}
	for i := range r.OutputMappings {
		c.Nested(indexed("outputMappings", i), &r.OutputMappings[i])
	}
	return c.Fields()
}

// Response is the read model for a process, with every section it carries nested
// inside it.
type Response struct {
	ProcessID    string             `json:"processId"`
	UserID       *string            `json:"userId"`
	ProcessType  *model.ProcessType `json:"processType"`
	LastStatusID *string            `json:"lastStatusId"`

	BatchProcess *BatchProcessResponse `json:"batchProcess"`

	InputMappings  []InputMapping  `json:"inputMappings"`
	OutputMappings []OutputMapping `json:"outputMappings"`
}

func ToResponse(p *model.Process) Response {
	return Response{
		ProcessID:      p.ID,
		UserID:         p.OwnerID,
		ProcessType:    p.ProcessType,
		LastStatusID:   p.LastStatusID,
		BatchProcess:   ToBatchProcessResponse(p.BatchProcess),
		InputMappings:  ToInputMappings(p.InputMappings),
		OutputMappings: ToOutputMappings(p.OutputMappings),
	}
}

func ToResponses(in []model.Process) []Response {
	out := make([]Response, 0, len(in))
	for i := range in {
		out = append(out, ToResponse(&in[i]))
	}
	return out
}

// ToBatchProcessResponse renders the batchProcess section, or nil for a process that
// has none.
func ToBatchProcessResponse(b *model.BatchJobProcess) *BatchProcessResponse {
	if b == nil {
		return nil
	}
	return &BatchProcessResponse{
		BatchProcessID: b.ID,
		DeploymentID:   b.DeploymentID,
		JobID:          b.JobID,
		JobName:        b.JobName,
		BatchJobConfig: applicationdto.ToBatchJobConfigResponse(b.BatchJobConfig),
	}
}

// ApplyBatchProcessRequest copies a section onto a batch process row, preserving the
// row's own ids so an update mutates it instead of orphaning it.
func ApplyBatchProcessRequest(dst *model.BatchJobProcess, src *BatchProcessRequest) {
	dst.DeploymentID = &src.DeploymentID
	dst.JobID = src.JobID
	dst.JobName = src.JobName
}

func ToInputMappings(in []*model.TemplateInputMapping) []InputMapping {
	out := make([]InputMapping, 0, len(in))
	for _, m := range in {
		if m == nil {
			continue
		}
		out = append(out, InputMapping{
			TemplateInputMappingID: m.TemplateInputMappingID,
			TemplateInputID:        derefString(m.TemplateInputID),
			Value:                  m.Value,
		})
	}
	return out
}

func ToOutputMappings(in []*model.TemplateOutputMapping) []OutputMapping {
	out := make([]OutputMapping, 0, len(in))
	for _, m := range in {
		if m == nil {
			continue
		}
		out = append(out, OutputMapping{
			TemplateOutputMappingID: m.TemplateOutputMappingID,
			TemplateOutputID:        derefString(m.TemplateOutputID),
			Value:                   m.Value,
		})
	}
	return out
}

// ToInputMappingEntities builds the rows for a process's mapping set. Ids are left
// unset so BeforeCreate assigns them.
func ToInputMappingEntities(processID string, in []InputMapping) []*model.TemplateInputMapping {
	out := make([]*model.TemplateInputMapping, 0, len(in))
	for i := range in {
		m := &in[i]
		templateInputID := m.TemplateInputID
		out = append(out, &model.TemplateInputMapping{
			ProcessID:       &processID,
			TemplateInputID: &templateInputID,
			Value:           m.Value,
		})
	}
	return out
}

// ToOutputMappingEntities builds the rows for a process's output mapping set.
func ToOutputMappingEntities(processID string, in []OutputMapping) []*model.TemplateOutputMapping {
	out := make([]*model.TemplateOutputMapping, 0, len(in))
	for i := range in {
		m := &in[i]
		templateOutputID := m.TemplateOutputID
		out = append(out, &model.TemplateOutputMapping{
			ProcessID:        &processID,
			TemplateOutputID: &templateOutputID,
			Value:            m.Value,
		})
	}
	return out
}

func derefString(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

func indexed(field string, i int) string {
	return field + "[" + strconv.Itoa(i) + "]"
}
