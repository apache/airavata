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

// Package role defines the granted authorities.
//
// It is a leaf package on purpose. Roles are both a persisted column on iam/model.UserRole
// and the authority string the auth guards compare against; giving them their own
// package lets both sides share one definition instead of one importing the other.
package role

// Role is a granted authority.
//
// These are bare names with no "ROLE_" prefix — the Java @PreAuthorize expressions
// check hasAnyAuthority('ADMIN','SUPER_ADMIN'), not hasRole, so adding a prefix here
// would silently deny every administrative call.
//
// Java: org.apache.airavata.iam.model.enums.UserRole
type Role string

const (
	SuperAdmin Role = "SUPER_ADMIN"
	Admin      Role = "ADMIN"
	User       Role = "USER"
)

// Valid reports whether r is a recognised role.
func (r Role) Valid() bool {
	switch r {
	case SuperAdmin, Admin, User:
		return true
	}
	return false
}

// IsAdmin reports whether r grants administrative access. Every administrative check
// in the services treats ADMIN and SUPER_ADMIN alike.
func (r Role) IsAdmin() bool {
	return r == Admin || r == SuperAdmin
}
