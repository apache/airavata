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

// Package service resolves what a caller may do with a shareable record, and manages
// the grants that decide it.
//
// Both halves used to exist once per vertical. The resolver in particular was copied
// verbatim between data and compute, which meant a change to the rule — "an inactive
// group membership grants nothing", say — had to be made twice and could be made
// inconsistently. There is one copy here, and the verticals say what their resources
// are by implementing ResourceGuard.
package service

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	iamrepo "github.com/apache/airavata/api/iam/repository"
	dto "github.com/apache/airavata/api/sharing/dto"
	model "github.com/apache/airavata/api/sharing/model"
	"github.com/apache/airavata/api/sharing/repository"
)

// Access resolves what the calling principal may do with a shared record.
//
// Strongest of ownership, a share naming the caller, and a share naming a group they
// are an active member of. Platform admins are treated as owners. "Control" — deleting
// a record and managing its shares — is not reachable through a share, because
// deciding who else gets access stays with the owner.
type Access struct {
	members *iamrepo.GroupMemberRepository
}

// NewAccess returns a resolver over the membership lookup.
func NewAccess(members *iamrepo.GroupMemberRepository) Access {
	return Access{members: members}
}

// WithTx binds the membership lookup to tx, for checks made from inside a transaction.
func (a Access) WithTx(tx *gorm.DB) Access {
	return Access{members: a.members.WithTx(tx)}
}

// PermissionOf returns the caller's effective permission and whether they control the
// record.
//
// ownerID is nil for a record that has no owner at all, and for one whose ownership the
// caller has already decided — a virtual directory, whose owner is resolved over a
// whole lineage before the shares are pooled. In both cases only admins and the shares
// reach it.
func (a Access) PermissionOf(ctx context.Context, ownerID *string, shares []model.Sharing) (model.AccessPermission, bool, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return model.AccessPermissionNone, false, err
	}
	if principal.IsAdmin() || (ownerID != nil && *ownerID == principal.Name) {
		return model.AccessPermissionWrite, true, nil
	}

	best := model.AccessPermissionNone
	needsGroups := false
	for i := range shares {
		switch {
		case shares[i].GrantsTo(model.PrincipalTypeUser, principal.Name):
			best = Strongest(best, shares[i].Permission)
		case shares[i].PrincipalType == model.PrincipalTypeGroup:
			needsGroups = true
		}
	}
	if !needsGroups {
		return best, false, nil
	}

	// A group share reaches the caller only through an ACTIVE membership: a suspended
	// member keeps their place in the group without keeping access through it. The
	// lookup is made once, and only when some share actually names a group.
	memberships, err := a.members.FindByUserID(ctx, principal.Name)
	if err != nil {
		return model.AccessPermissionNone, false, err
	}
	active := make(map[string]bool, len(memberships))
	for i := range memberships {
		if memberships[i].IsActive() {
			active[memberships[i].GroupID] = true
		}
	}
	for i := range shares {
		if shares[i].PrincipalType == model.PrincipalTypeGroup && active[shares[i].PrincipalID] {
			best = Strongest(best, shares[i].Permission)
		}
	}
	return best, false, nil
}

// Strongest returns the stronger of two grants. WRITE beats READ; anything beats
// nothing.
func Strongest(have, candidate model.AccessPermission) model.AccessPermission {
	if candidate == model.AccessPermissionNone {
		return have
	}
	if candidate == model.AccessPermissionWrite || have == model.AccessPermissionNone {
		return candidate
	}
	return have
}

// ResourceGuard is everything the sharing service needs to know about the kind of
// record it is opening up: what to tag the rows with, what to call it when something
// goes wrong, and whether the caller may manage its shares at all.
//
// One implementation per shareable record, living in the vertical that owns it — which
// is what keeps this package from importing any of them.
type ResourceGuard interface {
	ResourceType() model.ResourceType

	// Label names the kind in error messages, as a caller would recognise it.
	Label() string

	// RequireControlled loads the resource and checks the caller may manage its
	// shares, returning the user ids that already own it. A share with one of those as
	// its subject would grant nothing, so the service refuses it.
	RequireControlled(ctx context.Context, id string) (owners []string, err error)
}

// Service manages who, besides the owner, may reach one kind of record.
//
// One service for every shareable record. What used to be a service of eight methods
// per record — four per subject kind, the two halves differing only in which table
// they wrote — is four methods here, with the subject kind a field on the row.
//
// Only an owner (or a platform admin) may read or change a share list: it names who
// holds a record, which is more than a grantee needs to know.
type Service struct {
	guard   ResourceGuard
	db      *gorm.DB
	sharing *repository.Repository
	groups  *iamrepo.GroupRepository
	users   *iamrepo.UserRepository
}

// NewService returns a sharing service for the resource kind guard describes.
func NewService(
	guard ResourceGuard,
	db *gorm.DB,
	sharing *repository.Repository,
	groups *iamrepo.GroupRepository,
	users *iamrepo.UserRepository,
) *Service {
	return &Service{guard: guard, db: db, sharing: sharing, groups: groups, users: users}
}

// List returns every share of one resource, of either principal kind.
func (s *Service) List(ctx context.Context, resourceID string) ([]dto.ShareResponse, error) {
	if _, err := s.guard.RequireControlled(ctx, resourceID); err != nil {
		return nil, err
	}
	shares, err := s.sharing.FindByResource(ctx, s.guard.ResourceType(), resourceID)
	if err != nil {
		return nil, err
	}
	return dto.ToShareResponses(shares), nil
}

// Share grants one principal access to one resource.
//
// The principal has to exist, and has to not already hold the record: sharing with an
// owner is refused rather than stored, because it would grant nothing they do not
// already have.
func (s *Service) Share(ctx context.Context, resourceID string, req *dto.ShareRequest) (*dto.ShareResponse, error) {
	owners, err := s.guard.RequireControlled(ctx, resourceID)
	if err != nil {
		return nil, err
	}
	if req.Subject() == model.PrincipalTypeUser {
		for _, owner := range owners {
			if owner == req.PrincipalID {
				return nil, httpx.Conflict("User %s already owns %s %s",
					req.PrincipalID, s.guard.Label(), resourceID)
			}
		}
	}

	var out dto.ShareResponse
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		sharing := s.sharing.WithTx(tx)

		if err := s.requirePrincipal(ctx, tx, req.Subject(), req.PrincipalID); err != nil {
			return err
		}
		if _, err := sharing.FindByPrincipal(ctx, s.guard.ResourceType(), resourceID,
			req.Subject(), req.PrincipalID); err == nil {
			return httpx.Conflict("%s %s is already shared with %s %s",
				s.guard.Label(), resourceID, req.Subject(), req.PrincipalID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		share := &model.Sharing{
			ResourceType:  s.guard.ResourceType(),
			ResourceID:    resourceID,
			PrincipalType: req.Subject(),
			PrincipalID:   req.PrincipalID,
			Permission:    req.Grant(),
		}
		if err := sharing.Save(ctx, share); err != nil {
			return err
		}
		out = dto.ToShareResponse(share)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes what an existing share grants.
func (s *Service) Update(ctx context.Context, resourceID, sharingID string, req *dto.ShareUpdate) (*dto.ShareResponse, error) {
	share, err := s.requireShare(ctx, resourceID, sharingID)
	if err != nil {
		return nil, err
	}

	share.Permission = req.Grant()
	if err := s.sharing.Save(ctx, share); err != nil {
		return nil, err
	}
	out := dto.ToShareResponse(share)
	return &out, nil
}

// Revoke withdraws a principal's access.
func (s *Service) Revoke(ctx context.Context, resourceID, sharingID string) error {
	share, err := s.requireShare(ctx, resourceID, sharingID)
	if err != nil {
		return err
	}
	return s.sharing.Delete(ctx, share)
}

// requireShare loads one share of a resource the caller controls.
func (s *Service) requireShare(ctx context.Context, resourceID, sharingID string) (*model.Sharing, error) {
	if _, err := s.guard.RequireControlled(ctx, resourceID); err != nil {
		return nil, err
	}
	share, err := s.sharing.FindByID(ctx, s.guard.ResourceType(), resourceID, sharingID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, httpx.NotFound("Sharing not found: %s on %s %s", sharingID, s.guard.Label(), resourceID)
		}
		return nil, err
	}
	return share, nil
}

// requirePrincipal checks that the subject of a share exists. No foreign key does it:
// the column names a user or a group depending on the row.
func (s *Service) requirePrincipal(ctx context.Context, tx *gorm.DB, principalType model.PrincipalType, principalID string) error {
	switch principalType {
	case model.PrincipalTypeUser:
		if _, err := s.users.WithTx(tx).FindByID(ctx, principalID); err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return httpx.NotFound("User not found with ID: %s", principalID)
			}
			return err
		}
	case model.PrincipalTypeGroup:
		if _, err := s.groups.WithTx(tx).FindByID(ctx, principalID); err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return httpx.NotFound("Group not found: %s", principalID)
			}
			return err
		}
	default:
		return httpx.BadRequest("Principal type must be one of USER, GROUP")
	}
	return nil
}
