package dto

import (
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/iam/model"
)

// GroupRequest is the create/update payload for a group.
//
// There is no owner field: ownership comes from the access token and is immutable, so
// a group can neither be created on someone else's behalf nor handed over by editing
// it.
type GroupRequest struct {
	GroupName string `json:"groupName"`
}

// Validate implements httpx.Validator.
func (r *GroupRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("groupName", "Group name cannot be blank", r.GroupName)
	return c.Fields()
}

// GroupResponse is the read model for a group.
//
// Members are not inlined: a group's membership is read through
// /api/v1/groups/{groupId}/members, which keeps a large group's listing from being
// dragged into every response that merely names it.
type GroupResponse struct {
	GroupID   string  `json:"groupId"`
	GroupName *string `json:"groupName,omitempty"`
	OwnerID   *string `json:"ownerId,omitempty"`
	CreatedAt int64   `json:"createdAt"`
}

func ToGroupResponse(g *model.Group) GroupResponse {
	return GroupResponse{
		GroupID:   g.ID,
		GroupName: g.Name,
		OwnerID:   g.OwnerID,
		CreatedAt: g.CreatedAt,
	}
}

func ToGroupResponses(groups []model.Group) []GroupResponse {
	out := make([]GroupResponse, 0, len(groups))
	for i := range groups {
		out = append(out, ToGroupResponse(&groups[i]))
	}
	return out
}

// GroupMemberRequest adds a user to a group.
//
// Role and status are optional: an omitted role admits a plain MEMBER, and an omitted
// status admits them as ACTIVE. Both default rather than erroring, because the common
// call is "add this person to my group".
type GroupMemberRequest struct {
	UserID            string                   `json:"userId"`
	GroupRole         *model.GroupRole         `json:"groupRole"`
	GroupMemberStatus *model.GroupMemberStatus `json:"groupMemberStatus"`
}

// Validate implements httpx.Validator.
func (r *GroupMemberRequest) Validate() []httpx.FieldError {
	var c httpx.Constraints
	c.NotBlank("userId", "User ID cannot be blank", r.UserID)
	validateGroupRole(&c, r.GroupRole)
	validateGroupMemberStatus(&c, r.GroupMemberStatus)
	return c.Fields()
}

// Role returns the requested role, defaulting to MEMBER.
func (r *GroupMemberRequest) Role() model.GroupRole {
	if r.GroupRole == nil {
		return model.GroupRoleMember
	}
	return *r.GroupRole
}

// Status returns the requested status, defaulting to ACTIVE.
func (r *GroupMemberRequest) Status() model.GroupMemberStatus {
	if r.GroupMemberStatus == nil {
		return model.GroupMemberStatusActive
	}
	return *r.GroupMemberStatus
}

// GroupMemberUpdate changes an existing membership.
//
// Both fields are optional and an omitted one is left as it stands, so a caller can
// suspend a member without having to restate their role. The user is identified by the
// path, not the body: a membership cannot be moved to a different user by editing it.
type GroupMemberUpdate struct {
	GroupRole         *model.GroupRole         `json:"groupRole"`
	GroupMemberStatus *model.GroupMemberStatus `json:"groupMemberStatus"`
}

// Validate implements httpx.Validator.
func (r *GroupMemberUpdate) Validate() []httpx.FieldError {
	var c httpx.Constraints
	validateGroupRole(&c, r.GroupRole)
	validateGroupMemberStatus(&c, r.GroupMemberStatus)
	return c.Fields()
}

// Apply writes the fields the caller actually supplied onto m.
func (r *GroupMemberUpdate) Apply(m *model.GroupMember) {
	if r.GroupRole != nil {
		m.GroupRole = *r.GroupRole
	}
	if r.GroupMemberStatus != nil {
		m.GroupMemberStatus = *r.GroupMemberStatus
	}
}

// GroupMemberResponse is the read model for one membership.
type GroupMemberResponse struct {
	GroupID           string                  `json:"groupId"`
	UserID            string                  `json:"userId"`
	GroupRole         model.GroupRole         `json:"groupRole"`
	GroupMemberStatus model.GroupMemberStatus `json:"groupMemberStatus"`
}

func ToGroupMemberResponse(m *model.GroupMember) GroupMemberResponse {
	return GroupMemberResponse{
		GroupID:           m.GroupID,
		UserID:            m.UserID,
		GroupRole:         m.GroupRole,
		GroupMemberStatus: m.GroupMemberStatus,
	}
}

func ToGroupMemberResponses(members []model.GroupMember) []GroupMemberResponse {
	out := make([]GroupMemberResponse, 0, len(members))
	for i := range members {
		out = append(out, ToGroupMemberResponse(&members[i]))
	}
	return out
}

// validateGroupRole rejects an unrecognised role before it reaches the BeforeSave hook,
// which can only fail the write with an opaque error.
func validateGroupRole(c *httpx.Constraints, r *model.GroupRole) {
	if r != nil && !r.Valid() {
		c.Add("groupRole", "Group role must be one of ADMIN, MODERATOR, MEMBER")
	}
}

func validateGroupMemberStatus(c *httpx.Constraints, s *model.GroupMemberStatus) {
	if s != nil && !s.Valid() {
		c.Add("groupMemberStatus", "Group member status must be one of ACTIVE, INACTIVE")
	}
}
