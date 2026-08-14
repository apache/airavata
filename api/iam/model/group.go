package model

import (
	"github.com/google/uuid"
	"gorm.io/gorm"
)

// Group is a named collection of users that resources can be shared with.
//
// The owner is the user who created it and is not transferable through the API: the
// data-sharing tables grant access by group id, so handing a group to someone else
// would silently hand over everything shared with it. The owner always holds an
// ADMIN membership row as well, created alongside the group.
type Group struct {
	ID   string  `gorm:"column:group_id;primaryKey;type:varchar(36)" json:"groupId"`
	Name *string `gorm:"column:group_name;type:varchar(255)" json:"groupName,omitempty"`

	// OwnerID is named for its role rather than for the column, because ownership —
	// not mere reference — is what the authorisation checks read. RESTRICT: a user who
	// still owns groups cannot be deleted out from under them.
	OwnerID *string `gorm:"column:user_id;type:varchar(255);index" json:"ownerId,omitempty"`
	Owner   *User   `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`

	// CreatedAt is epoch milliseconds, matching User.CreatedAt. It is set imperatively
	// at creation, not by a lifecycle callback.
	CreatedAt int64 `gorm:"column:created_at;not null" json:"createdAt"`

	// Members are owned by the group: deleting a group deletes its membership rows.
	// Like Cluster.Partitions this is a read-side projection — the services write
	// through the membership repository rather than mutating the collection.
	Members []GroupMember `gorm:"foreignKey:GroupID;references:ID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"members,omitempty"`
}

// TableName returns the table backing Group.
func (Group) TableName() string { return "groups" }

// BeforeCreate assigns a UUID when none was supplied.
func (g *Group) BeforeCreate(*gorm.DB) error {
	if g.ID == "" {
		g.ID = uuid.NewString()
	}
	return nil
}

// OwnedBy reports whether userID owns this group. A group with no owner is owned by
// nobody, so it must not match the empty principal name.
func (g *Group) OwnedBy(userID string) bool {
	return g.OwnerID != nil && *g.OwnerID == userID
}

type GroupRole string

const (
	GroupRoleAdmin     GroupRole = "ADMIN"
	GroupRoleMember    GroupRole = "MEMBER"
	GroupRoleModerator GroupRole = "MODERATOR"
)

func (r GroupRole) Valid() bool {
	switch r {
	case GroupRoleAdmin, GroupRoleMember, GroupRoleModerator:
		return true
	}
	return false
}

type GroupMemberStatus string

const (
	GroupMemberStatusActive   GroupMemberStatus = "ACTIVE"
	GroupMemberStatusInactive GroupMemberStatus = "INACTIVE"
)

func (s GroupMemberStatus) Valid() bool {
	switch s {
	case GroupMemberStatusActive, GroupMemberStatusInactive:
		return true
	}
	return false
}

// GroupMember places one user in one group. The primary key is the (group, user)
// pair: a user holds at most one role in a given group.
type GroupMember struct {
	GroupID           string            `gorm:"column:group_id;primaryKey;type:varchar(36)" json:"groupId"`
	UserID            string            `gorm:"column:user_id;primaryKey;type:varchar(255)" json:"userId"`
	GroupRole         GroupRole         `gorm:"column:group_role;type:varchar(32)" json:"groupRole"`
	GroupMemberStatus GroupMemberStatus `gorm:"column:group_member_status;type:varchar(32)" json:"groupMemberStatus"`
}

// TableName returns the table backing GroupMember.
func (GroupMember) TableName() string { return "group_members" }

// BeforeSave rejects unrecognised roles and statuses, the same way UserRole does: the
// columns are plain varchars, so validating on write is what keeps an unknown constant
// out of the table.
func (m *GroupMember) BeforeSave(*gorm.DB) error {
	if !m.GroupRole.Valid() || !m.GroupMemberStatus.Valid() {
		return gorm.ErrInvalidValue
	}
	return nil
}

// IsActive reports whether the membership currently grants anything. An INACTIVE row
// keeps the user's place in the group without letting them read through it.
func (m *GroupMember) IsActive() bool {
	return m.GroupMemberStatus == GroupMemberStatusActive
}

// CanManageMembers reports whether this membership may add, change and remove other
// memberships. Moderators can, so that a group does not depend on its owner being
// available to admit people.
func (m *GroupMember) CanManageMembers() bool {
	return m.IsActive() && (m.GroupRole == GroupRoleAdmin || m.GroupRole == GroupRoleModerator)
}
