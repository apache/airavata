// Package model holds the process entities: a process, the batch-job specifics some
// processes carry, its status history and its tasks.
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
	TemplateInput   *application.TemplateInput `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"templateInput,omitempty"`

	// Owned by Process.InputMappings, which declares the cascade; only the key is
	// held here.
	ProcessID *string `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`

	// Value is a JSON document, either {"value": "..."} for a single value or
	// {"values": [...]} for a list.
	Value *string `gorm:"column:value;type:text" json:"value,omitempty"`
}

type TemplateOutputMapping struct {
	TemplateOutputMappingID string `gorm:"column:template_output_mapping_id;primaryKey;type:varchar(36)" json:"templateOutputMappingId"`

	// The template this mapping belongs to.
	TemplateOutputID *string                     `gorm:"column:template_output_id;type:varchar(36);index" json:"templateOutputId,omitempty"`
	TemplateOutput   *application.TemplateOutput `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"templateOutput,omitempty"`

	// Owned by Process.OutputMappings, which declares the cascade; only the key is
	// held here.
	ProcessID *string `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`

	// Value is a JSON document, either {"value": "..."} for a single value or
	// {"values": [...]} for a list.
	Value *string `gorm:"column:value;type:text" json:"value,omitempty"`
}

type ProcessType string

const (
	ProcessTypeBatchJob ProcessType = "BATCH_JOB"
	ProcessTypeTask     ProcessType = "CLOUD_JOB"
)

// Valid reports whether t is a recognised process type.
func (t ProcessType) Valid() bool {
	switch t {
	case ProcessTypeBatchJob, ProcessTypeTask:
		return true
	}
	return false
}

// Process is one run: the single entity every other row in this package hangs off.
//
// What a run needs beyond an owner and a type depends on the kind of run it is, and
// that lives in a per-kind child rather than in columns here — BatchProcess for a
// BATCH_JOB, and whatever a later kind requires. The children are not resources of
// their own: a caller creates, reads and deletes them through the process.
type Process struct {
	ID string `gorm:"column:process_id;primaryKey;type:varchar(36)" json:"processId"`

	OwnerID *string        `gorm:"column:user_id;type:varchar(255);index" json:"userId,omitempty"`
	Owner   *iammodel.User `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// The process type, e.g. "BATCH_JOB". This is a plain string rather than an enum
	// because the set of process kinds is expected to grow.
	ProcessType *ProcessType `gorm:"column:process_type;type:varchar(32)" json:"processType,omitempty"`

	// The last status recorded for this process, if any.
	//
	// It carries no foreign key, deliberately. A status already points at its process,
	// and a constraint in this direction as well would make the two tables mutually
	// dependent — a cycle PostgreSQL cannot create, since it validates a referenced
	// table exists when the referencing table is declared. This column is a cache of
	// the newest status row, maintained by ProcessStatus.AfterCreate.
	LastStatusID *string `gorm:"column:last_status_id;type:varchar(36);index" json:"lastStatusId,omitempty"`

	// BatchProcess is present exactly when ProcessType is BATCH_JOB. It is owned by
	// the process, not shared with it: deleting the process deletes it too.
	BatchProcess *BatchJobProcess `gorm:"foreignKey:ProcessID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"batchProcess,omitempty"`

	InputMappings  []*TemplateInputMapping  `gorm:"foreignKey:ProcessID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"inputMappings,omitempty"`
	OutputMappings []*TemplateOutputMapping `gorm:"foreignKey:ProcessID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"outputMappings,omitempty"`
}

// TableName returns the table backing Process.
func (Process) TableName() string { return "processes" }

// TableName returns the table backing TemplateInputMapping.
func (TemplateInputMapping) TableName() string { return "process_template_input_mappings" }

// TableName returns the table backing TemplateOutputMapping.
func (TemplateOutputMapping) TableName() string { return "process_template_output_mappings" }

// BeforeCreate assigns a UUID when none was supplied.
func (p *Process) BeforeCreate(*gorm.DB) error {
	if p.ID == "" {
		p.ID = uuid.NewString()
	}
	return nil
}

// BeforeDelete removes what the database will not.
//
// The statuses have to go first: their own ProcessID foreign key is RESTRICT, so the
// server would refuse to delete a process any status still points at. LastStatusID
// needs no clearing — it holds no constraint — and the row carrying it is about to be
// deleted in this same transaction.
//
// The batch process is deleted through GORM rather than left to its ON DELETE CASCADE
// so that its own AfterDelete fires and takes the owned BatchJobConfig with it. A
// cascade in the database removes the row without ever calling into Go, which would
// leave the config orphaned.
//
// The tasks and the template mappings need neither: their foreign keys cascade and
// they own nothing outside themselves.
func (p *Process) BeforeDelete(tx *gorm.DB) error {
	if err := tx.Where("process_id = ?", p.ID).Delete(&ProcessStatus{}).Error; err != nil {
		return err
	}

	var batch []BatchJobProcess
	if err := tx.Where("parent_process_id = ?", p.ID).Find(&batch).Error; err != nil {
		return err
	}
	for i := range batch {
		if err := tx.Delete(&batch[i]).Error; err != nil {
			return err
		}
	}
	return nil
}

// OwnedBy reports whether userID launched this process. A process with no owner is
// owned by nobody, not by the empty principal.
func (p *Process) OwnedBy(userID string) bool {
	return p.OwnerID != nil && *p.OwnerID == userID
}

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

	ProcessID *string  `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Status *ProcessStatusType `gorm:"column:status;type:varchar(255)" json:"status,omitempty"`
	Log    *string            `gorm:"column:log;type:text" json:"log,omitempty"`

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
