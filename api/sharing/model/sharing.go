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

// Package model holds the one table every shareable record in the platform is reached
// through, and the vocabulary that table is written in.
//
// Sharing used to be a thing each vertical did for itself: a pair of tables per
// shareable record — one for user grants, one for group grants — plus its own
// permission type and its own copy of the rule for resolving them. Adding a shareable
// record meant two more tables and a third copy of the resolver.
//
// Here the resource and the subject are each a (type, id) pair in one table, so a new
// shareable record adds a constant and nothing else. The package sits below every
// vertical and imports none of them; a vertical says what its resources are by
// implementing the guard the sharing service asks for.
package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// AccessPermission is what a grant confers, on any shareable record.
//
// One type for every vertical: the storages, products, virtual directories and cluster
// configs all distinguished READ from WRITE and nothing else, so what used to be three
// identical string types is this one.
type AccessPermission string

const (
	AccessPermissionRead  AccessPermission = "READ"
	AccessPermissionWrite AccessPermission = "WRITE"
	AccessPermissionNone  AccessPermission = ""
)

// Valid reports whether p is a recognised permission. The empty string is recognised:
// it is what "grants nothing" resolves to.
func (p AccessPermission) Valid() bool {
	switch p {
	case AccessPermissionRead, AccessPermissionWrite, AccessPermissionNone:
		return true
	}
	return false
}

// Allows reports whether holding current is enough to do something requiring want.
// WRITE implies READ; nothing implies WRITE.
func (current AccessPermission) Allows(want AccessPermission) bool {
	if current == AccessPermissionNone || want == AccessPermissionNone {
		return false
	}
	return current == AccessPermissionWrite || current == want
}

// ResourceType names the kind of record a sharing row opens up.
//
// The id alone is not enough to identify a resource: the kinds have separate id
// spaces, so a row is addressed by the pair.
type ResourceType string

const (
	ResourceTypeDataProduct          ResourceType = "DATA_PRODUCT"
	ResourceTypeSCPDataStorage       ResourceType = "SCP_DATA_STORAGE"
	ResourceTypeVirtualDataDirectory ResourceType = "VIRTUAL_DATA_DIRECTORY"
	ResourceTypeSlurmClusterConfig   ResourceType = "SLURM_CLUSTER_CONFIG"
)

// Valid reports whether t is a recognised resource type.
func (t ResourceType) Valid() bool {
	switch t {
	case ResourceTypeDataProduct, ResourceTypeSCPDataStorage,
		ResourceTypeVirtualDataDirectory, ResourceTypeSlurmClusterConfig:
		return true
	}
	return false
}

// PrincipalType names what kind of subject a share is granted to.
//
// A USER share reaches exactly that account. A GROUP share reaches every active member
// of the group, which is resolved at access time rather than stored, so adding someone
// to a group grants them what the group already holds.
type PrincipalType string

const (
	PrincipalTypeUser  PrincipalType = "USER"
	PrincipalTypeGroup PrincipalType = "GROUP"
)

// Valid reports whether t is a recognised principal type.
func (t PrincipalType) Valid() bool {
	switch t {
	case PrincipalTypeUser, PrincipalTypeGroup:
		return true
	}
	return false
}

// Sharing grants one principal access to one record.
//
// Neither id can carry a foreign key: a column cannot reference four resource tables,
// nor users and groups at once. The database will therefore not refuse a share of a
// record that has been deleted, so whatever deletes a resource deletes its shares in
// the same transaction, and whatever deletes a group withdraws what it was granted.
// PrincipalID is sized for a user id, the longer of the two.
type Sharing struct {
	ID string `gorm:"column:resource_sharing_id;primaryKey;type:varchar(36)" json:"resourceSharingId"`

	ResourceType ResourceType `gorm:"column:resource_type;type:varchar(32);not null;index:idx_resource_sharings_resource;uniqueIndex:uk_resource_sharing" json:"resourceType"`
	ResourceID   string       `gorm:"column:resource_id;type:varchar(36);not null;index:idx_resource_sharings_resource;uniqueIndex:uk_resource_sharing" json:"resourceId"`

	PrincipalType PrincipalType `gorm:"column:principal_type;type:varchar(32);not null;uniqueIndex:uk_resource_sharing" json:"principalType"`
	PrincipalID   string        `gorm:"column:principal_id;type:varchar(255);not null;index;uniqueIndex:uk_resource_sharing" json:"principalId"`

	Permission AccessPermission `gorm:"column:permission;type:varchar(32)" json:"permission"`
}

// TableName returns the table backing Sharing.
func (Sharing) TableName() string { return "resource_sharings" }

// BeforeCreate assigns a UUID when none was supplied.
func (s *Sharing) BeforeCreate(*gorm.DB) error {
	if s.ID == "" {
		s.ID = uuid.NewString()
	}
	return nil
}

// BeforeSave rejects a row that names nothing, or names it with a constant the columns
// cannot enforce. The types are plain varchars, so this is what keeps an unknown value
// out of the table.
func (s *Sharing) BeforeSave(*gorm.DB) error {
	if !s.ResourceType.Valid() || !s.PrincipalType.Valid() || !s.Permission.Valid() {
		return gorm.ErrInvalidValue
	}
	if s.ResourceID == "" || s.PrincipalID == "" {
		return gorm.ErrInvalidValue
	}
	// AccessPermissionNone is a valid constant — it is what "grants nothing" resolves
	// to — but storing it would be a row that exists and does nothing.
	if s.Permission == AccessPermissionNone {
		return gorm.ErrInvalidValue
	}
	return nil
}

// GrantsTo reports whether this row grants to the named subject. Membership of a group
// is the caller's to establish; this only compares the subject it names.
func (s *Sharing) GrantsTo(principalType PrincipalType, principalID string) bool {
	return s.PrincipalType == principalType && s.PrincipalID == principalID
}
