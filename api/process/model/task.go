/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

package model

import (
	data "github.com/apache/airavata/api/data/model"
	"github.com/google/uuid"
	"gorm.io/gorm"
)

type OnFailureAction string

const (
	OnFailureActionRetry OnFailureAction = "RETRY"
	OnFailureActionSkip  OnFailureAction = "SKIP"
	OnFailureActionExit  OnFailureAction = "EXIT"
)

// Valid reports whether a is a recognised action.
func (a OnFailureAction) Valid() bool {
	switch a {
	case OnFailureActionRetry, OnFailureActionSkip, OnFailureActionExit:
		return true
	}
	return false
}

type DataStagingTask struct {
	ID string `gorm:"column:task_id;primaryKey;type:varchar(36)" json:"taskId"`

	ProcessID *string  `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"process,omitempty"`

	// Each end is addressed by data-storage id alone: a storage carries the host, the
	// login user and the key it is reached under, so there is no separate credential to
	// name here.
	SourceDataStorageID   *string               `gorm:"column:source_data_storage_id;type:varchar(36);index" json:"sourceDataStorageId,omitempty"`
	SourceDataStorageType *data.DataStorageType `gorm:"column:source_data_storage_type;type:varchar(32)" json:"sourceDataStorageType,omitempty"`

	DestinationDataStorageID   *string               `gorm:"column:destination_data_storage_id;type:varchar(36);index" json:"destinationDataStorageId,omitempty"`
	DestinationDataStorageType *data.DataStorageType `gorm:"column:destination_data_storage_type;type:varchar(32)" json:"destinationDataStorageType,omitempty"`

	// This could be a json array of file paths, or a single file path
	SourcePath *string `gorm:"column:source_path;type:text" json:"sourcePath,omitempty"`

	// This could be a json array of file paths, or a single file path
	DestinationPath *string `gorm:"column:destination_path;type:text" json:"destinationPath,omitempty"`

	// The action to take if the task fails. This is a string that can be used to determine what to do next.
	OnFailure  *OnFailureAction `gorm:"column:on_failure;type:varchar(32)" json:"onFailure,omitempty"`
	RetryCount *int             `gorm:"column:retry_count;type:int" json:"retryCount,omitempty"`

	// Determines the order of execution of tasks within a process. Lower numbers are executed first. Tasks with the same order number are executed in parallel.
	TaskOrder *int `gorm:"column:task_order;type:int" json:"taskOrder,omitempty"`
}

type JobSubmissionTask struct {
	ID string `gorm:"column:task_id;primaryKey;type:varchar(36)" json:"taskId"`

	ProcessID *string  `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"process,omitempty"`

	JobId *string `gorm:"column:job_id;type:varchar(255)" json:"jobId,omitempty"`

	WorkingDir *string `gorm:"column:working_dir;type:varchar(1024)" json:"workingDir,omitempty"`

	OnFailure  *OnFailureAction `gorm:"column:on_failure;type:varchar(32)" json:"onFailure,omitempty"`
	RetryCount *int             `gorm:"column:retry_count;type:int" json:"retryCount,omitempty"`

	TaskOrder *int `gorm:"column:task_order;type:int" json:"taskOrder,omitempty"`
}

type JobMonitoringTask struct {
	ID string `gorm:"column:task_id;primaryKey;type:varchar(36)" json:"taskId"`

	ProcessID *string  `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"process,omitempty"`

	JobId      *string          `gorm:"column:job_id;type:varchar(255)" json:"jobId,omitempty"`
	OnFailure  *OnFailureAction `gorm:"column:on_failure;type:varchar(32)" json:"onFailure,omitempty"`
	RetryCount *int             `gorm:"column:retry_count;type:int" json:"retryCount,omitempty"`

	TaskOrder *int `gorm:"column:task_order;type:int" json:"taskOrder,omitempty"`
}

// Runs an interactive command on the remote cluster. Use this to fiter output of the running job
type InteractiveCommandTask struct {
	ID string `gorm:"column:task_id;primaryKey;type:varchar(36)" json:"taskId"`

	ProcessID *string  `gorm:"column:process_id;type:varchar(36);index" json:"processId,omitempty"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"process,omitempty"`

	Command    *string          `gorm:"column:command;type:text" json:"command,omitempty"`
	Output     *string          `gorm:"column:output;type:text" json:"output,omitempty"`
	OnFailure  *OnFailureAction `gorm:"column:on_failure;type:varchar(32)" json:"onFailure,omitempty"`
	RetryCount *int             `gorm:"column:retry_count;type:int" json:"retryCount,omitempty"`

	TaskOrder *int `gorm:"column:task_order;type:int" json:"taskOrder,omitempty"`
}

// TableName returns the table backing DataStagingTask.
func (DataStagingTask) TableName() string { return "data_staging_tasks" }

// BeforeCreate assigns a UUID when none was supplied.
func (t *DataStagingTask) BeforeCreate(*gorm.DB) error {
	if t.ID == "" {
		t.ID = uuid.NewString()
	}
	return nil
}

// BelongsTo reports whether this task is part of processID. The scoping is what stops
// a task id from one process being reached through another process's path.
func (t *DataStagingTask) BelongsTo(processID string) bool {
	return t.ProcessID != nil && *t.ProcessID == processID
}

// TableName returns the table backing JobSubmissionTask.
func (JobSubmissionTask) TableName() string { return "job_submission_tasks" }

// BeforeCreate assigns a UUID when none was supplied.
func (t *JobSubmissionTask) BeforeCreate(*gorm.DB) error {
	if t.ID == "" {
		t.ID = uuid.NewString()
	}
	return nil
}

// BelongsTo reports whether this task is part of processID. The scoping is what stops
// a task id from one process being reached through another process's path.
func (t *JobSubmissionTask) BelongsTo(processID string) bool {
	return t.ProcessID != nil && *t.ProcessID == processID
}

// TableName returns the table backing JobMonitoringTask.
func (JobMonitoringTask) TableName() string { return "job_monitoring_tasks" }

// BeforeCreate assigns a UUID when none was supplied.
func (t *JobMonitoringTask) BeforeCreate(*gorm.DB) error {
	if t.ID == "" {
		t.ID = uuid.NewString()
	}
	return nil
}

// BelongsTo reports whether this task is part of processID. The scoping is what stops
// a task id from one process being reached through another process's path.
func (t *JobMonitoringTask) BelongsTo(processID string) bool {
	return t.ProcessID != nil && *t.ProcessID == processID
}

// TableName returns the table backing InteractiveCommandTask.
func (InteractiveCommandTask) TableName() string { return "interactive_command_tasks" }

// BeforeCreate assigns a UUID when none was supplied.
func (t *InteractiveCommandTask) BeforeCreate(*gorm.DB) error {
	if t.ID == "" {
		t.ID = uuid.NewString()
	}
	return nil
}

// BelongsTo reports whether this task is part of processID. The scoping is what stops
// a task id from one process being reached through another process's path.
func (t *InteractiveCommandTask) BelongsTo(processID string) bool {
	return t.ProcessID != nil && *t.ProcessID == processID
}
