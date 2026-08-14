package model

type Group struct {
	ID        string  `gorm:"column:group_id;primaryKey;type:varchar(36)" json:"groupId"`
	Name      *string `gorm:"column:group_name;type:varchar(255)" json:"groupName,omitempty"`
	OwnerID   *string `gorm:"column:user_id;type:varchar(255);index" json:"ownerId,omitempty"`
	Owner     *User   `gorm:"references:ID;constraint:OnDelete:RESTRICT,OnUpdate:CASCADE" json:"-"`
	CreatedAt int64   `gorm:"column:created_at;not null" json:"createdAt"`
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

type GroupMember struct {
	GroupID           string            `gorm:"column:group_id;primaryKey;type:varchar(36)" json:"groupId"`
	UserID            string            `gorm:"column:user_id;primaryKey;type:varchar(255)" json:"userId"`
	GroupRole         GroupRole         `gorm:"column:group_role;type:varchar(32)" json:"groupRole"`
	GroupMemberStatus GroupMemberStatus `gorm:"column:group_member_status;type:varchar(32)" json:"groupMemberStatus"`
}
