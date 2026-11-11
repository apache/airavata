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

// Package model holds the audit log's entity.
package model

// Action is what was done to a record.
type Action string

const (
	ActionCreate Action = "CREATE"
	ActionUpdate Action = "UPDATE"
	ActionDelete Action = "DELETE"
	// ActionUpsert is a write that inserts the record or, if it already exists,
	// updates it; the database does not say which happened.
	ActionUpsert Action = "UPSERT"
)

// SystemActor is recorded for a change made with no caller behind it, such as the
// execution engine moving a process to its next status.
const SystemActor = "system"

// Event is one change to one record: who made it, what was done and to what.
//
// The table is append-only. Nothing in the server updates or deletes a row, and it
// deliberately has no foreign keys: an audit trail must outlive the users and records
// it describes, so Actor and EntityID are plain strings rather than references.
//
// An event names the record, never its contents. Values are left out on purpose: rows
// carry token digests, SSH passwords and job secrets, and a log that copied them would
// be a second place to protect them.
type Event struct {
	ID int64 `gorm:"column:id;primaryKey;autoIncrement" json:"id"`

	// OccurredAt is when the change was made, in milliseconds since the epoch.
	OccurredAt int64 `gorm:"column:occurred_at;not null;index:idx_audit_events_occurred_at;index:idx_audit_events_actor,priority:2;index:idx_audit_events_entity,priority:3" json:"occurredAt"`

	// Actor is the principal's name — a users.user_id, "root", or "sa:<name>" — or
	// SystemActor.
	Actor string `gorm:"column:actor;type:varchar(255);not null;index:idx_audit_events_actor,priority:1" json:"actor"`

	Action Action `gorm:"column:action;type:varchar(10);not null" json:"action"`

	// EntityType is the table the record lives in, such as "processes", and EntityID its
	// primary key; a composite key is joined with ":". EntityID is empty when the
	// change addressed rows by a condition instead of by key.
	EntityType string `gorm:"column:entity_type;type:varchar(64);not null;index:idx_audit_events_entity,priority:1" json:"entityType"`
	EntityID   string `gorm:"column:entity_id;type:varchar(255);not null;default:'';index:idx_audit_events_entity,priority:2" json:"entityId"`

	// ChangedFields lists the columns an update named, comma-separated, when the write
	// named them. Empty for a create, a delete, and an update that saved the whole record.
	ChangedFields string `gorm:"column:changed_fields;type:varchar(1024);not null;default:''" json:"changedFields"`
}

// TableName returns the table backing Event.
func (Event) TableName() string { return "audit_events" }
