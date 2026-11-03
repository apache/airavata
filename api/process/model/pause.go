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
	"github.com/google/uuid"
	"gorm.io/gorm"
)

type PausedTaskType string

const (
	PausedTaskTypeDataStaging   PausedTaskType = "DATA_STAGING"
	PausedTaskTypeJobSubmission PausedTaskType = "JOB_SUBMISSION"
)

// TaskPause records a workflow parked on one of a process's tasks until its owner has
// completed an interactive SSH login and asked for the run to continue.
//
// A row is open while ContinuedAt is unset. The workflow it names is the one to signal
// to continue it: a process runs a submission and later a completion workflow, and
// either can be the one waiting.
type TaskPause struct {
	ID string `gorm:"column:task_pause_id;primaryKey;type:varchar(36)" json:"taskPauseId"`

	ProcessID string   `gorm:"column:process_id;type:varchar(36);index" json:"processId"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"-"`

	TaskID             string         `gorm:"column:task_id;type:varchar(36)" json:"taskId"`
	TaskType           PausedTaskType `gorm:"column:task_type;type:varchar(32)" json:"taskType"`
	WorkflowInstanceID string         `gorm:"column:workflow_instance_id;type:varchar(64)" json:"workflowInstanceId"`

	// Why the task stopped, as the SSH layer put it: the host, login user and the
	// interactive auth the owner was asked to answer.
	Reason *string `gorm:"column:reason;type:text" json:"reason,omitempty"`

	// Both in milliseconds since the epoch.
	PausedAt    int64  `gorm:"column:paused_at" json:"pausedAt"`
	ContinuedAt *int64 `gorm:"column:continued_at" json:"continuedAt,omitempty"`
}

// TableName returns the table backing TaskPause.
func (TaskPause) TableName() string { return "task_pauses" }

// BeforeCreate assigns a UUID when none was supplied.
func (p *TaskPause) BeforeCreate(*gorm.DB) error {
	if p.ID == "" {
		p.ID = uuid.NewString()
	}
	return nil
}
