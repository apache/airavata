package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
)

type BatchProcessStatusType string

const (
	BatchProcessStatusTypeCreated   BatchProcessStatusType = "CREATED"
	BatchProcessStatusTypeSubmitted BatchProcessStatusType = "SUBMITTED"
	BatchProcessStatusTypeRunning   BatchProcessStatusType = "RUNNING"
	BatchProcessStatusTypeCompleted BatchProcessStatusType = "COMPLETED"
	BatchProcessStatusTypeFailed    BatchProcessStatusType = "FAILED"
)

type BatchJobProcessStatus struct {
	ID string `gorm:"column:process_status_id;primaryKey;type:varchar(36)" json:"processStatusId"`

	ProcessID *string          `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`
	Process   *BatchJobProcess `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	Status *BatchProcessStatusType `gorm:"column:status;type:varchar(255)" json:"status,omitempty"`
	Log    *string                 `gorm:"column:log;type:text" json:"log,omitempty"`

	// The time the status was recorded, in milliseconds since the epoch.
	Timestamp *int64 `gorm:"column:timestamp" json:"timestamp,omitempty"`
}

// TableName returns the table backing BatchJobProcessStatus.
func (BatchJobProcessStatus) TableName() string { return "batch_job_process_statuses" }

func (s *BatchJobProcessStatus) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

func (s *BatchJobProcessStatus) AfterCreate(tx *gorm.DB) error {
	if s.ProcessID == nil || *s.ProcessID == "" {
		return nil
	}
	return tx.Model(&BatchJobProcess{}).
		Where("process_id = ?", *s.ProcessID).
		Update("last_status_id", s.ID).Error
}
