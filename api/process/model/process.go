package model

import (
	application "github.com/apache/airavata/api/application/model"
	iammodel "github.com/apache/airavata/api/iam/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

type TemplateInputMapping struct {
	TemplateInputMappingID string `gorm:"column:template_input_mapping_id;primaryKey;type:varchar(36)" json:"templateInputMappingId"`

	// The template this mapping belongs to.
	TemplateInputID *string                    `gorm:"column:template_input_id;type:varchar(36);index" json:"templateInputId,omitempty"`
	TemplateInput   *application.TemplateInput `gorm:"references:TemplateInputID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"templateInput,omitempty"`

	ProcessID *string  `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"process,omitempty"`

	// Value is a JSON document, either {"value": "..."} for a single value or
	// {"values": [...]} for a list.
	Value *string `gorm:"column:value;type:text" json:"value,omitempty"`
}

type TemplateOutputMapping struct {
	TemplateOutputMappingID string `gorm:"column:template_output_mapping_id;primaryKey;type:varchar(36)" json:"templateOutputMappingId"`

	// The template this mapping belongs to.
	TemplateOutputID *string                     `gorm:"column:template_output_id;type:varchar(36);index" json:"templateOutputId,omitempty"`
	TemplateOutput   *application.TemplateOutput `gorm:"references:TemplateOutputID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"templateOutput,omitempty"`

	ProcessID *string  `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"process,omitempty"`
	// Value is a JSON document, either {"value": "..."} for a single value or
	// {"values": [...]} for a list.
	Value *string `gorm:"column:value;type:text" json:"value,omitempty"`
}

type ProcessType string

const (
	ProcessTypeBatchJob ProcessType = "BATCH_JOB"
	ProcessTypeTask     ProcessType = "CLOUD_JOB"
)

type Process struct {
	ID string `gorm:"column:process_id;primaryKey;type:varchar(36)" json:"processId"`

	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"userId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// The process type, e.g. "BATCH_JOB". This is a plain string rather than an enum
	// because the set of process kinds is expected to grow.
	ProcessType    *ProcessType              `gorm:"column:process_type;type:varchar(32)" json:"processType,omitempty"`
	InputMappings  []*TemplateInputMapping  `gorm:"foreignKey:ProcessID" json:"inputMappings,omitempty"`
	OutputMappings []*TemplateOutputMapping `gorm:"foreignKey:ProcessID" json:"outputMappings,omitempty"`
}

// TableName returns the table backing TemplateInputMapping.
func (TemplateInputMapping) TableName() string { return "process_template_input_mappings" }

// TableName returns the table backing TemplateOutputMapping.
func (TemplateOutputMapping) TableName() string { return "process_template_output_mappings" }

// BeforeCreate assigns a UUID when none was supplied.
func (m *TemplateInputMapping) BeforeCreate(*gorm.DB) error {
	if m.TemplateInputMappingID == "" {
		m.TemplateInputMappingID = uuid.NewString()
	}
	return nil
}

// BeforeCreate assigns a UUID when none was supplied.
func (m *TemplateOutputMapping) BeforeCreate(*gorm.DB) error {
	if m.TemplateOutputMappingID == "" {
		m.TemplateOutputMappingID = uuid.NewString()
	}
	return nil
}

type ProcessStatusType string

const (
	ProcessStatusTypeCreated   ProcessStatusType = "CREATED"
	ProcessStatusTypeSubmitted ProcessStatusType = "SUBMITTED"
	ProcessStatusTypeRunning   ProcessStatusType = "RUNNING"
	ProcessStatusTypeCompleted ProcessStatusType = "COMPLETED"
	ProcessStatusTypeFailed    ProcessStatusType = "FAILED"
)

type ProcessStatus struct {
	ID string `gorm:"column:process_status_id;primaryKey;type:varchar(36)" json:"processStatusId"`

	ProcessID *string          `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Status *ProcessStatusType `gorm:"column:status;type:varchar(255)" json:"status,omitempty"`
	Log    *string                 `gorm:"column:log;type:text" json:"log,omitempty"`

	// The time the status was recorded, in milliseconds since the epoch.
	Timestamp *int64 `gorm:"column:timestamp" json:"timestamp,omitempty"`
}

// TableName returns the table backing ProcessStatus.
func (ProcessStatus) TableName() string { return "process_statuses" }

func (s *ProcessStatus) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

func (s *ProcessStatus) AfterCreate(tx *gorm.DB) error {
	if s.ProcessID == nil || *s.ProcessID == "" {
		return nil
	}
	return tx.Model(&Process{}).
		Where("process_id = ?", *s.ProcessID).
		Update("last_status_id", s.ID).Error
}