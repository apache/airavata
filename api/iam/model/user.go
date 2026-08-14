// Package iam holds the identity and access-management entities.
package model

import (
	"gorm.io/gorm"
)

// User is an Airavata account.
//
// ID is supplied by the caller rather than generated: CILogon subjects are normalised
// to "cilogon:12345" for consistent lookup, while system users get a UUID chosen by
// the registration path. This is the one entity with no ID hook — every other entity
// generates its key in BeforeCreate.
//
// Every service that resolves ownership assumes the authenticated principal's name is
// exactly this ID. That assumption is load-bearing in ClusterCredential and
// BatchJobProcess authorisation.
//
// Java: org.apache.airavata.iam.model.UserEntity (@Entity(name = "users"))
type User struct {
	ID string `gorm:"column:user_id;primaryKey;type:varchar(255)" json:"userId"`

	AuthMethod *AuthMethod `gorm:"column:auth_method;type:varchar(32)" json:"authMethod,omitempty"`

	Email     *string `gorm:"column:email;type:varchar(255)" json:"email,omitempty"`
	FirstName *string `gorm:"column:first_name;type:varchar(255)" json:"firstName,omitempty"`
	LastName  *string `gorm:"column:last_name;type:varchar(255)" json:"lastName,omitempty"`

	Status *UserStatus `gorm:"column:status;type:varchar(32)" json:"status,omitempty"`

	// CreatedAt is epoch milliseconds, matching the Java primitive long. It is set
	// imperatively at registration, not by a lifecycle callback.
	CreatedAt int64 `gorm:"column:created_at;not null" json:"createdAt"`

	// Roles are owned by the user: deleting a user deletes its role rows, rather than
	// relying on every caller to clean up user_roles separately.
	Roles []UserRole `gorm:"foreignKey:UserID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"roles,omitempty"`
}

// TableName pins the table name, matching the Java @Entity(name = "users").
func (User) TableName() string { return "users" }

// HasRole reports whether the user holds role r.
func (u *User) HasRole(r Role) bool {
	for _, ur := range u.Roles {
		if ur.Role == r {
			return true
		}
	}
	return false
}

// IsAdmin reports whether the user holds ADMIN or SUPER_ADMIN.
func (u *User) IsAdmin() bool {
	for _, ur := range u.Roles {
		if ur.Role.IsAdmin() {
			return true
		}
	}
	return false
}

// UserRole grants one role to one user. The primary key is the (user_id, role) pair:
// a user may hold several roles, but not the same role twice.
//
// The Java entity carried a read-only ManyToOne back to UserEntity purely so JPA's
// cascade could reach it. Here the cascade is a database-level constraint declared on
// User.Roles, so the back-reference would be redundant and is omitted.
//
// Java: org.apache.airavata.iam.model.UserRoleEntity (@IdClass(UserRoleId.class))
type UserRole struct {
	UserID string `gorm:"column:user_id;primaryKey;type:varchar(255)" json:"userId"`
	Role   Role   `gorm:"column:role;primaryKey;type:varchar(32)" json:"role"`
}

// TableName pins the table name, matching the Java @Entity(name = "user_roles").
func (UserRole) TableName() string { return "user_roles" }

// BeforeSave rejects unrecognised roles. Hibernate stored these by name and would
// fail on read for an unknown constant; validating on write is the closer analogue
// now that the column is a plain varchar.
func (r *UserRole) BeforeSave(*gorm.DB) error {
	if !r.Role.Valid() {
		return gorm.ErrInvalidValue
	}
	return nil
}
