package iam

import (
	"context"
	"errors"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	dto "github.com/apache/airavata/api/iam/dto"
	model "github.com/apache/airavata/api/iam/model"
)

// groupAccess resolves what the calling principal may do within a group. Both the
// group service and the membership service authorise through it, so the three
// standings a caller can hold — reader, member manager, owner — are defined once.
//
// A platform admin satisfies all three. Everything else is decided by the group's
// own owner field and membership rows, not by platform roles: groups are user-owned,
// so being an ADMIN of one group says nothing about any other.
type groupAccess struct {
	groups  *GroupRepository
	members *GroupMemberRepository
}

// requireGroup loads a group or reports 404.
func (a groupAccess) requireGroup(ctx context.Context, groupID string) (*model.Group, error) {
	group, err := a.groups.FindByID(ctx, groupID)
	if err != nil {
		return nil, notFoundAs(err, "Group not found: %s", groupID)
	}
	return group, nil
}

// membership returns the caller's row in the group, or nil when they hold none.
func (a groupAccess) membership(ctx context.Context, groupID, userID string) (*model.GroupMember, error) {
	member, err := a.members.FindByGroupIDAndUserID(ctx, groupID, userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, nil
		}
		return nil, err
	}
	return member, nil
}

// requireReader allows the owner, any active member, and platform admins.
//
// The failure is 404 rather than 403 for a caller with no standing at all: group names
// are chosen by users and may say who is working with whom, so an outsider should not
// be able to confirm that a given group id exists.
func (a groupAccess) requireReader(ctx context.Context, group *model.Group) (*auth.Principal, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	if principal.IsAdmin() || group.OwnedBy(principal.Name) {
		return principal, nil
	}
	member, err := a.membership(ctx, group.ID, principal.Name)
	if err != nil {
		return nil, err
	}
	if member != nil && member.IsActive() {
		return principal, nil
	}
	return nil, httpx.NotFound("Group not found: %s", group.ID)
}

// requireManager allows the owner, members holding ADMIN or MODERATOR, and platform
// admins. It gates every write to the membership list.
func (a groupAccess) requireManager(ctx context.Context, group *model.Group) (*auth.Principal, error) {
	principal, err := a.requireReader(ctx, group)
	if err != nil {
		return nil, err
	}
	if principal.IsAdmin() || group.OwnedBy(principal.Name) {
		return principal, nil
	}
	member, err := a.membership(ctx, group.ID, principal.Name)
	if err != nil {
		return nil, err
	}
	if member != nil && member.CanManageMembers() {
		return principal, nil
	}
	return nil, httpx.Forbidden("Access denied: you may only manage members of a group you administer")
}

// requireOwner allows the owner and platform admins. Renaming and deleting a group are
// deliberately kept from group ADMINs and MODERATORs: deleting a group revokes every
// share made through it, which is the owner's call.
func (a groupAccess) requireOwner(ctx context.Context, group *model.Group) (*auth.Principal, error) {
	principal, err := a.requireReader(ctx, group)
	if err != nil {
		return nil, err
	}
	if principal.IsAdmin() || group.OwnedBy(principal.Name) {
		return principal, nil
	}
	return nil, httpx.Forbidden("Access denied: you may only modify a group you own")
}

// GroupService manages groups.
//
// Any authenticated user may create one, which is the difference between this and the
// administrative catalogs: a group belongs to the user who made it rather than to the
// deployment.
type GroupService struct {
	groupAccess
	db    *gorm.DB
	users *UserRepository
}

// NewGroupService returns a group service.
func NewGroupService(db *gorm.DB, groups *GroupRepository, members *GroupMemberRepository, users *UserRepository) *GroupService {
	return &GroupService{
		groupAccess: groupAccess{groups: groups, members: members},
		db:          db,
		users:       users,
	}
}

// List returns every group across every owner. Admin only — this exposes who is
// working with whom.
func (s *GroupService) List(ctx context.Context) ([]dto.GroupResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	groups, err := s.groups.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	return dto.ToGroupResponses(groups), nil
}

// ListMine returns the groups the caller owns or belongs to.
func (s *GroupService) ListMine(ctx context.Context) ([]dto.GroupResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	groups, err := s.groups.FindVisibleTo(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	return dto.ToGroupResponses(groups), nil
}

// Get returns one group, to its owner, an active member, or an admin.
func (s *GroupService) Get(ctx context.Context, groupID string) (*dto.GroupResponse, error) {
	group, err := s.requireGroup(ctx, groupID)
	if err != nil {
		return nil, err
	}
	if _, err := s.requireReader(ctx, group); err != nil {
		return nil, err
	}
	out := dto.ToGroupResponse(group)
	return &out, nil
}

// Create registers a group owned by the calling user.
//
// The owner is admitted as an ADMIN member in the same transaction, so a group is
// never left in a state where nobody can administer it. Creation time is set here
// rather than taken from the request, matching user registration.
func (s *GroupService) Create(ctx context.Context, req *dto.GroupRequest) (*dto.GroupResponse, error) {
	var out dto.GroupResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		groups, members := s.groups.WithTx(tx), s.members.WithTx(tx)

		owner, err := RequireCurrentUser(ctx, s.users.WithTx(tx))
		if err != nil {
			return err
		}

		group := &model.Group{
			Name:      &req.GroupName,
			OwnerID:   &owner.ID,
			CreatedAt: time.Now().UnixMilli(),
		}
		if err := groups.Save(ctx, group); err != nil {
			return err
		}

		membership := &model.GroupMember{
			GroupID:           group.ID,
			UserID:            owner.ID,
			GroupRole:         model.GroupRoleAdmin,
			GroupMemberStatus: model.GroupMemberStatusActive,
		}
		if err := members.Save(ctx, membership); err != nil {
			return err
		}

		out = dto.ToGroupResponse(group)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update renames a group. The owner is left alone: re-deriving it from the caller's
// token would hand the group to whichever admin happened to issue the request.
func (s *GroupService) Update(ctx context.Context, groupID string, req *dto.GroupRequest) (*dto.GroupResponse, error) {
	group, err := s.requireGroup(ctx, groupID)
	if err != nil {
		return nil, err
	}
	if _, err := s.requireOwner(ctx, group); err != nil {
		return nil, err
	}

	group.Name = &req.GroupName
	if err := s.groups.Save(ctx, group); err != nil {
		return nil, err
	}
	out := dto.ToGroupResponse(group)
	return &out, nil
}

// Delete removes a group, taking its membership rows with it.
func (s *GroupService) Delete(ctx context.Context, groupID string) error {
	group, err := s.requireGroup(ctx, groupID)
	if err != nil {
		return err
	}
	if _, err := s.requireOwner(ctx, group); err != nil {
		return err
	}
	return s.groups.Delete(ctx, group)
}

// GroupMemberService manages memberships as a sub-resource of their group.
//
// Every operation is scoped by the group id from the path, and the membership's user
// comes from the path too — so a membership can be created, changed or withdrawn, but
// never moved to a different user or a different group.
type GroupMemberService struct {
	groupAccess
	db    *gorm.DB
	users *UserRepository
}

// NewGroupMemberService returns a membership service.
func NewGroupMemberService(db *gorm.DB, groups *GroupRepository, members *GroupMemberRepository, users *UserRepository) *GroupMemberService {
	return &GroupMemberService{
		groupAccess: groupAccess{groups: groups, members: members},
		db:          db,
		users:       users,
	}
}

// List returns every membership of a group, to anyone who may read the group.
func (s *GroupMemberService) List(ctx context.Context, groupID string) ([]dto.GroupMemberResponse, error) {
	group, err := s.requireGroup(ctx, groupID)
	if err != nil {
		return nil, err
	}
	if _, err := s.requireReader(ctx, group); err != nil {
		return nil, err
	}
	members, err := s.members.FindByGroupID(ctx, group.ID)
	if err != nil {
		return nil, err
	}
	return dto.ToGroupMemberResponses(members), nil
}

// Get returns one membership of a group.
func (s *GroupMemberService) Get(ctx context.Context, groupID, userID string) (*dto.GroupMemberResponse, error) {
	group, err := s.requireGroup(ctx, groupID)
	if err != nil {
		return nil, err
	}
	if _, err := s.requireReader(ctx, group); err != nil {
		return nil, err
	}
	member, err := s.requireMember(ctx, group.ID, userID)
	if err != nil {
		return nil, err
	}
	out := dto.ToGroupMemberResponse(member)
	return &out, nil
}

// Add admits a user to a group.
//
// The user must already be registered: an unknown id would otherwise sit in the table
// as a membership that can never be exercised, and the foreign key would reject it
// with an opaque error anyway.
func (s *GroupMemberService) Add(ctx context.Context, groupID string, req *dto.GroupMemberRequest) (*dto.GroupMemberResponse, error) {
	group, err := s.requireGroup(ctx, groupID)
	if err != nil {
		return nil, err
	}
	if _, err := s.requireManager(ctx, group); err != nil {
		return nil, err
	}

	var out dto.GroupMemberResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		members, users := s.members.WithTx(tx), s.users.WithTx(tx)

		if _, err := users.FindByID(ctx, req.UserID); err != nil {
			return notFoundAs(err, "User not found with ID: %s", req.UserID)
		}
		if _, err := members.FindByGroupIDAndUserID(ctx, group.ID, req.UserID); err == nil {
			return httpx.Conflict("User %s is already a member of group %s", req.UserID, group.ID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		membership := &model.GroupMember{
			GroupID:           group.ID,
			UserID:            req.UserID,
			GroupRole:         req.Role(),
			GroupMemberStatus: req.Status(),
		}
		if err := members.Save(ctx, membership); err != nil {
			return err
		}
		out = dto.ToGroupMemberResponse(membership)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a member's role or status.
//
// The owner's own membership is reserved to the owner and to platform admins: without
// that, a moderator could suspend the owner out of their own group.
func (s *GroupMemberService) Update(ctx context.Context, groupID, userID string, req *dto.GroupMemberUpdate) (*dto.GroupMemberResponse, error) {
	group, err := s.requireGroup(ctx, groupID)
	if err != nil {
		return nil, err
	}
	principal, err := s.requireManager(ctx, group)
	if err != nil {
		return nil, err
	}
	if group.OwnedBy(userID) && !(principal.IsAdmin() || group.OwnedBy(principal.Name)) {
		return nil, httpx.Forbidden("Access denied: only the group owner may change their own membership")
	}

	member, err := s.requireMember(ctx, group.ID, userID)
	if err != nil {
		return nil, err
	}
	req.Apply(member)
	if err := s.members.Save(ctx, member); err != nil {
		return nil, err
	}
	out := dto.ToGroupMemberResponse(member)
	return &out, nil
}

// Remove withdraws a membership.
//
// A member may always remove themselves — leaving a group needs nobody's permission,
// and works from a suspended membership too, which is the one case where an inactive
// member can still act on the group. The owner's membership cannot be removed at all
// while they own the group: that would leave the group owned by someone outside it.
func (s *GroupMemberService) Remove(ctx context.Context, groupID, userID string) error {
	group, err := s.requireGroup(ctx, groupID)
	if err != nil {
		return err
	}
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return err
	}

	if principal.Name != userID {
		if _, err := s.requireManager(ctx, group); err != nil {
			return err
		}
	} else {
		// Self-removal is authorised by the membership itself, so a caller who holds
		// none is told what an outsider is told: the group is not theirs to see.
		member, err := s.membership(ctx, group.ID, userID)
		if err != nil {
			return err
		}
		if member == nil {
			return httpx.NotFound("Group not found: %s", group.ID)
		}
	}

	if group.OwnedBy(userID) {
		return httpx.Conflict("The owner's membership of group %s cannot be removed; delete the group instead", group.ID)
	}

	member, err := s.requireMember(ctx, group.ID, userID)
	if err != nil {
		return err
	}
	return s.members.Delete(ctx, member)
}

func (s *GroupMemberService) requireMember(ctx context.Context, groupID, userID string) (*model.GroupMember, error) {
	member, err := s.members.FindByGroupIDAndUserID(ctx, groupID, userID)
	if err != nil {
		return nil, notFoundAs(err, "User %s is not a member of group %s", userID, groupID)
	}
	return member, nil
}
