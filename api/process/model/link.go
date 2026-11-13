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

// ProcessLink is what lets a process's Linkspan link back to Airavata: the secrets its
// job is handed at submission and the loopback ports its servers listen on.
//
// Linkspan, running as the job, dials /api/v1/processes/{id}/link and offers the link
// token; Airavata then reaches the job's servers over that one WebSocket. Only the
// token's digest is kept, since Airavata only ever compares it. The Jupyter token is
// kept as issued: it is handed to the process owner so the dashboard can open the
// job's Jupyter Server through Airavata.
//
// A process has at most one row, replaced on each submission, so a resubmitted job's
// predecessor can no longer link. Nothing here is serialised: the row never leaves the
// server except as the owner's Jupyter access.
type ProcessLink struct {
	ProcessID string   `gorm:"column:process_id;primaryKey;type:varchar(36)" json:"-"`
	Process   *Process `gorm:"references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"-"`

	// LinkTokenHash is the hex SHA-256 of the link token.
	LinkTokenHash string `gorm:"column:link_token_hash;type:varchar(64);not null" json:"-"`
	JupyterToken  string `gorm:"column:jupyter_token;type:varchar(64);not null" json:"-"`

	// The loopback ports, on the job's node, of Linkspan's control API and of the
	// Jupyter Server the job starts. Chosen by Airavata so it knows where to dial.
	ControlPort int32 `gorm:"column:control_port;not null" json:"-"`
	JupyterPort int32 `gorm:"column:jupyter_port;not null" json:"-"`

	// When the tokens were issued, in milliseconds since the epoch.
	IssuedAt int64 `gorm:"column:issued_at;not null" json:"-"`
}

// TableName returns the table backing ProcessLink.
func (ProcessLink) TableName() string { return "process_links" }
