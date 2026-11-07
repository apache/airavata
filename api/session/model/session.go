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

// Package model holds the session entity: an owner's handle on a series of runs, each
// an existing BATCH_JOB process.
package model

// Session owns the runs that carry its id. CurrentProcessID is a cache of the newest
// run, maintained by the session service; it carries no foreign key, like
// Process.LastStatusID.
type Session struct {
	ID               string `gorm:"column:session_id;primaryKey;type:varchar(36)" json:"sessionId"`
	OwnerID          string `gorm:"column:user_id;type:varchar(255);not null;index" json:"userId"`
	CreatedAt        int64  `gorm:"column:created_at;type:bigint;not null" json:"createdAt"`
	CurrentProcessID string `gorm:"column:current_process_id;type:varchar(36);not null" json:"currentProcessId"`
}

func (Session) TableName() string { return "sessions" }
