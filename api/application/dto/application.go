package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/application/model"
)

// TemplateInputDTO is one declared input, used for both reads and writes.
//
// InputID is echoed back on reads but ignored on writes: an update replaces the
// declaration set wholesale, so child ids are regenerated rather than matched.
//
// Java: org.apache.airavata.application.dto.template.ApplicationTemplateInputDto
type TemplateInputDTO struct {
	InputID          string                   `json:"inputId"`
	InputName        string                   `json:"inputName"`
	DisplayName      *string                  `json:"displayName"`
	InputDescription *string                  `json:"inputDescription"`
	InputType        *model.TemplateInputType `json:"inputType"`

	// Required is "required" on the wire, not "isRequired" — the Java DTO field is
	// named required even though the entity column is is_required.
	Required bool `json:"required"`

	DefaultValue *string `json:"defaultValue"`
}

// Validate implements httpx.Validator.
func (d *TemplateInputDTO) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("inputName", "Input name cannot be blank", d.InputName)
	c.NotNil("inputType", "Input type cannot be null", d.InputType)
	if d.InputType != nil && !d.InputType.Valid() {
		c.Add("inputType", "Input type is not recognised")
	}
	return c.Fields()
}

// TemplateOutputDTO is one declared output, used for both reads and writes.
//
// Java: org.apache.airavata.application.dto.template.ApplicationTemplateOutputDto
type TemplateOutputDTO struct {
	OutputID          string                    `json:"outputId"`
	OutputName        string                    `json:"outputName"`
	DisplayName       *string                   `json:"displayName"`
	OutputDescription *string                   `json:"outputDescription"`
	OutputType        *model.TemplateOutputType `json:"outputType"`
}

// Validate implements httpx.Validator.
func (d *TemplateOutputDTO) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("outputName", "Output name cannot be blank", d.OutputName)
	c.NotNil("outputType", "Output type cannot be null", d.OutputType)
	if d.OutputType != nil && !d.OutputType.Valid() {
		c.Add("outputType", "Output type is not recognised")
	}
	return c.Fields()
}

// TemplateRequest is the create/update payload for a template.
//
// Java: org.apache.airavata.application.dto.template.ApplicationTemplateRequestDto
type TemplateRequest struct {
	TemplateName        string              `json:"templateName"`
	TemplateDescription *string             `json:"templateDescription"`
	Inputs              []TemplateInputDTO  `json:"inputs"`
	Outputs             []TemplateOutputDTO `json:"outputs"`
}

// Validate implements httpx.Validator.
//
// Duplicate input names are caught here rather than being left to the unique
// constraint. The database would reject them too, but as an opaque driver error that
// surfaces as a 500 — a caller's mistake reported as a server fault.
func (r *TemplateRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("templateName", "Template name cannot be blank", r.TemplateName)

	seen := make(map[string]bool, len(r.Inputs))
	for i := range r.Inputs {
		c.Nested(indexed("inputs", i), &r.Inputs[i])
		name := r.Inputs[i].InputName
		if name == "" {
			continue
		}
		if seen[name] {
			c.Add(indexed("inputs", i)+".inputName", "Input name must be unique within a template: "+name)
		}
		seen[name] = true
	}

	for i := range r.Outputs {
		c.Nested(indexed("outputs", i), &r.Outputs[i])
	}
	return c.Fields()
}

// TemplateResponse is the read model for a template.
//
// Java: org.apache.airavata.application.dto.template.ApplicationTemplateResponseDto
type TemplateResponse struct {
	TemplateID          string              `json:"templateId"`
	TemplateName        *string             `json:"templateName"`
	TemplateDescription *string             `json:"templateDescription"`
	Inputs              []TemplateInputDTO  `json:"inputs"`
	Outputs             []TemplateOutputDTO `json:"outputs"`
}

func ToTemplateResponse(t *model.Template) TemplateResponse {
	inputs := make([]TemplateInputDTO, 0, len(t.Inputs))
	for i := range t.Inputs {
		in := &t.Inputs[i]
		inputs = append(inputs, TemplateInputDTO{
			InputID:          in.ID,
			InputName:        derefString(in.InputName),
			DisplayName:      in.DisplayName,
			InputDescription: in.InputDescription,
			InputType:        in.InputType,
			Required:         in.IsRequired,
			DefaultValue:     in.DefaultValue,
		})
	}
	outputs := make([]TemplateOutputDTO, 0, len(t.Outputs))
	for i := range t.Outputs {
		out := &t.Outputs[i]
		outputs = append(outputs, TemplateOutputDTO{
			OutputID:          out.ID,
			OutputName:        derefString(out.OutputName),
			DisplayName:       out.DisplayName,
			OutputDescription: out.OutputDescription,
			OutputType:        out.OutputType,
		})
	}
	return TemplateResponse{
		TemplateID:          t.ID,
		TemplateName:        t.TemplateName,
		TemplateDescription: t.TemplateDescription,
		Inputs:              inputs,
		Outputs:             outputs,
	}
}

func ToInputEntities(in []TemplateInputDTO) []model.TemplateInput {
	out := make([]model.TemplateInput, 0, len(in))
	for i := range in {
		d := &in[i]
		name := d.InputName
		out = append(out, model.TemplateInput{
			InputName:        &name,
			DisplayName:      d.DisplayName,
			InputDescription: d.InputDescription,
			InputType:        d.InputType,
			IsRequired:       d.Required,
			DefaultValue:     d.DefaultValue,
		})
	}
	return out
}

func ToOutputEntities(in []TemplateOutputDTO) []model.TemplateOutput {
	out := make([]model.TemplateOutput, 0, len(in))
	for i := range in {
		d := &in[i]
		name := d.OutputName
		out = append(out, model.TemplateOutput{
			OutputName:        &name,
			DisplayName:       d.DisplayName,
			OutputDescription: d.OutputDescription,
			OutputType:        d.OutputType,
		})
	}
	return out
}

// BatchJobConfigRequest is the resource request carried by a deployment or process.
//
// Java: org.apache.airavata.application.dto.deployment.BatchJobConfigRequestDto
type BatchJobConfigRequest struct {
	WallTimeMinutes *int64  `json:"wallTimeMinutes"`
	Allocation      string  `json:"allocation"`
	CPUs            *int32  `json:"cpus"`
	Mem             *string `json:"mem"`
	MemPerCPU       *string `json:"memPerCpu"`
	NtasksPerNode   *int32  `json:"ntasksPerNode"`
	CPUsPerTask     *int32  `json:"cpusPerTask"`
	Nodes           *int32  `json:"nodes"`
	Ntasks          *int32  `json:"ntasks"`
	Gres            *string `json:"gres"`
	GPUs            *int32  `json:"gpus"`
	MemPerGPU       *string `json:"memPerGpu"`
	CPUsPerGPU      *string `json:"cpusPerGpu"`
	GPUsPerNode     *int32  `json:"gpusPerNode"`
	Constraints     *string `json:"constraints"`
}

// Validate implements httpx.Validator.
func (r *BatchJobConfigRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.Positive("wallTimeMinutes", "Wall time must be positive", r.WallTimeMinutes)
	c.NotBlank("allocation", "Allocation cannot be blank", r.Allocation)
	return c.Fields()
}

// BatchJobConfigResponse is the read model for a resource request.
//
// Java: org.apache.airavata.application.dto.deployment.BatchJobConfigResponseDto
type BatchJobConfigResponse struct {
	BatchJobConfigID string  `json:"batchJobConfigId"`
	WallTimeMinutes  int64   `json:"wallTimeMinutes"`
	Allocation       string  `json:"allocation"`
	CPUs             *int32  `json:"cpus"`
	Mem              *string `json:"mem"`
	MemPerCPU        *string `json:"memPerCpu"`
	NtasksPerNode    *int32  `json:"ntasksPerNode"`
	CPUsPerTask      *int32  `json:"cpusPerTask"`
	Nodes            *int32  `json:"nodes"`
	Ntasks           *int32  `json:"ntasks"`
	Gres             *string `json:"gres"`
	GPUs             *int32  `json:"gpus"`
	MemPerGPU        *string `json:"memPerGpu"`
	CPUsPerGPU       *string `json:"cpusPerGpu"`
	GPUsPerNode      *int32  `json:"gpusPerNode"`
	Constraints      *string `json:"constraints"`
}

// ApplyBatchJobConfigRequest copies a request onto a config entity, preserving the
// entity's id so an update mutates the existing row instead of orphaning it.
func ApplyBatchJobConfigRequest(dst *model.BatchJobConfig, src *BatchJobConfigRequest) {
	if src.WallTimeMinutes != nil {
		dst.WallTimeMinutes = *src.WallTimeMinutes
	}
	dst.Allocation = src.Allocation
	dst.CPUs = src.CPUs
	dst.Mem = src.Mem
	dst.MemPerCPU = src.MemPerCPU
	dst.NtasksPerNode = src.NtasksPerNode
	dst.CPUsPerTask = src.CPUsPerTask
	dst.Nodes = src.Nodes
	dst.Ntasks = src.Ntasks
	dst.Gres = src.Gres
	dst.GPUs = src.GPUs
	dst.MemPerGPU = src.MemPerGPU
	dst.CPUsPerGPU = src.CPUsPerGPU
	dst.GPUsPerNode = src.GPUsPerNode
	dst.Constraints = src.Constraints
}

// ToBatchJobConfigResponse renders a config entity.
func ToBatchJobConfigResponse(c *model.BatchJobConfig) *BatchJobConfigResponse {
	if c == nil {
		return nil
	}
	return &BatchJobConfigResponse{
		BatchJobConfigID: c.ID,
		WallTimeMinutes:  c.WallTimeMinutes,
		Allocation:       c.Allocation,
		CPUs:             c.CPUs,
		Mem:              c.Mem,
		MemPerCPU:        c.MemPerCPU,
		NtasksPerNode:    c.NtasksPerNode,
		CPUsPerTask:      c.CPUsPerTask,
		Nodes:            c.Nodes,
		Ntasks:           c.Ntasks,
		Gres:             c.Gres,
		GPUs:             c.GPUs,
		MemPerGPU:        c.MemPerGPU,
		CPUsPerGPU:       c.CPUsPerGPU,
		GPUsPerNode:      c.GPUsPerNode,
		Constraints:      c.Constraints,
	}
}

// BatchDeploymentRequest is the create/update payload for a deployment.
//
// Java: org.apache.airavata.application.dto.deployment.BatchApplicationDeploymentRequestDto
type BatchDeploymentRequest struct {
	TemplateID      string                 `json:"templateId"`
	SlurmClusterID  *string                `json:"slurmClusterId"`
	SlurmRunSection string                 `json:"slurmRunSection"`
	BatchJobConfig  *BatchJobConfigRequest `json:"batchJobConfig"`

	// DefaultSubmissionCredentialID names an SSH endpoint credential binding (the id
	// returned by POST /api/v1/ssh-endpoint-credentials), not a bare SSH credential —
	// a binding ties the submitting identity to both a host and an owner.
	DefaultSubmissionCredentialID string `json:"defaultSubmissionCredentialId"`

	WorkDir   *string `json:"workDir"`
	Partition *string `json:"partition"`
}

// Validate implements httpx.Validator.
func (r *BatchDeploymentRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("templateId", "Template id cannot be blank", r.TemplateID)
	c.NotBlank("slurmRunSection", "Slurm run section cannot be blank", r.SlurmRunSection)
	c.NotBlank("defaultSubmissionCredentialId", "Default submission credential id cannot be blank", r.DefaultSubmissionCredentialID)
	if r.BatchJobConfig == nil {
		c.Add("batchJobConfig", "Batch job config cannot be null")
	} else {
		c.Nested("batchJobConfig", r.BatchJobConfig)
	}
	return c.Fields()
}

// BatchDeploymentResponse is the read model for a deployment.
//
// Java: org.apache.airavata.application.dto.deployment.BatchApplicationDeploymentResponseDto
type BatchDeploymentResponse struct {
	DeploymentID                  string                  `json:"deploymentId"`
	TemplateID                    *string                 `json:"templateId"`
	SlurmClusterID                *string                 `json:"slurmClusterId"`
	SlurmRunSection               string                  `json:"slurmRunSection"`
	BatchJobConfig                *BatchJobConfigResponse `json:"batchJobConfig"`
	DefaultSubmissionCredentialID string                  `json:"defaultSubmissionCredentialId"`
	WorkDir                       *string                 `json:"workDir"`
	Partition                     *string                 `json:"partition"`
}

func ToBatchDeploymentResponse(d *model.BatchDeployment) BatchDeploymentResponse {
	return BatchDeploymentResponse{
		DeploymentID:                  d.ID,
		TemplateID:                    d.TemplateID,
		SlurmClusterID:                d.ClusterID,
		SlurmRunSection:               d.SlurmRunSection,
		BatchJobConfig:                ToBatchJobConfigResponse(d.BatchJobConfig),
		DefaultSubmissionCredentialID: d.DefaultSubmissionCredentialID,
		WorkDir:                       d.WorkDir,
		Partition:                     d.Partition,
	}
}

func derefString(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

func indexed(field string, i int) string {
	return field + "[" + itoa(i) + "]"
}

func itoa(i int) string {
	if i == 0 {
		return "0"
	}
	var buf [20]byte
	pos := len(buf)
	for i > 0 {
		pos--
		buf[pos] = byte('0' + i%10)
		i /= 10
	}
	return string(buf[pos:])
}
