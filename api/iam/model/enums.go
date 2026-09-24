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
	"github.com/apache/airavata/internal/role"
)

// AuthMethod identifies how a user authenticates.
//
// Java: org.apache.airavata.iam.model.enums.AuthMethod
type AuthMethod string

const (
	AuthMethodCILogon AuthMethod = "CILOGON"
	AuthMethodSystem  AuthMethod = "SYSTEM"
)

// Valid reports whether m is a recognised AuthMethod.
func (m AuthMethod) Valid() bool {
	switch m {
	case AuthMethodCILogon, AuthMethodSystem:
		return true
	}
	return false
}

// UserStatus is the account lifecycle state.
//
// Java: org.apache.airavata.iam.model.enums.UserStatus
type UserStatus string

const (
	UserStatusActive    UserStatus = "ACTIVE"
	UserStatusInactive  UserStatus = "INACTIVE"
	UserStatusSuspended UserStatus = "SUSPENDED"
)

// Valid reports whether s is a recognised UserStatus.
func (s UserStatus) Valid() bool {
	switch s {
	case UserStatusActive, UserStatusInactive, UserStatusSuspended:
		return true
	}
	return false
}

// Role is the granted authority stored on UserRole. It is an alias rather than a
// distinct type so that a role read from the database and a role compared by the auth
// guards are the same value, with no conversion between them.
type Role = role.Role

// The roles, re-exported so callers working with users do not need a second import.
const (
	RoleSuperAdmin = role.SuperAdmin
	RoleAdmin      = role.Admin
	RoleUser       = role.User
)
