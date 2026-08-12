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
