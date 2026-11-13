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

package service

import (
	"context"
	"errors"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/permission"

	dto "github.com/apache/airavata/api/iam/dto"
	model "github.com/apache/airavata/api/iam/model"
	"github.com/apache/airavata/api/iam/repository"
)

// UserService is the user management API.
//
// Unlike the other services it predates the @PreAuthorize convention and does its
// authority checks inline. The checks are also asymmetric in ways worth preserving
// rather than tidying, because clients depend on them: registration is SUPER_ADMIN
// only, reading is ADMIN or self, and updating is SUPER_ADMIN or self.
type UserService struct {
	repo *repository.UserRepository
	db   *gorm.DB
}

// NewUserService returns a user service.
func NewUserService(db *gorm.DB, repo *repository.UserRepository) *UserService {
	return &UserService{repo: repo, db: db}
}

// Register creates a user. Only a Super Admin may do this.
//
// Status and creation time are set here rather than taken from the request, so a
// caller cannot register an account that is already suspended or backdated.
func (s *UserService) Register(ctx context.Context, req *dto.UserRegistration) (*dto.UserResponse, error) {
	if _, err := auth.RequirePermission(ctx, permission.UserCreate); err != nil {
		return nil, err
	}
	// Service accounts have their own API, which is what issues their token. One
	// registered here would be a service account with no way to authenticate, in a
	// namespace the introspector refuses to hand to any identity provider.
	if req.AuthMethod != nil && *req.AuthMethod == model.AuthMethodServiceAccount {
		return nil, httpx.Invalid([]httpx.FieldError{{Field: "authMethod",
			Message: "Service accounts are created through /api/v1/service-accounts"}})
	}
	if auth.IsServiceAccountID(req.UserID) {
		return nil, httpx.Invalid([]httpx.FieldError{{Field: "userId",
			Message: "User IDs starting with \"" + auth.ServiceAccountIDPrefix + "\" are reserved for service accounts"}})
	}

	status := model.UserStatusActive
	user := &model.User{
		ID:         req.UserID,
		Email:      req.Email,
		FirstName:  req.FirstName,
		LastName:   req.LastName,
		AuthMethod: req.AuthMethod,
		Status:     &status,
		CreatedAt:  time.Now().UnixMilli(),
	}
	if err := s.repo.Save(ctx, user); err != nil {
		return nil, err
	}

	out := dto.ToUserResponse(user)
	return &out, nil
}

// Get returns one user. Admins may read anyone; everyone else may read only
// themselves.
func (s *UserService) Get(ctx context.Context, userID string) (*dto.UserResponse, error) {
	if _, err := auth.RequireSelfOrPermission(ctx, userID, permission.UserReadAny,
		"Access denied: You can only access your own user information"); err != nil {
		return nil, err
	}

	user, err := s.repo.FindByID(ctx, userID)
	if err != nil {
		return nil, notFoundAs(err, "User not found with ID: %s", userID)
	}
	out := dto.ToUserResponse(user)
	return &out, nil
}

// List returns every user, with the email, auth method and roles an administrator
// needs to decide on them. Needs user:list.
func (s *UserService) List(ctx context.Context) ([]dto.UserAccountResponse, error) {
	if _, err := auth.RequirePermission(ctx, permission.UserList); err != nil {
		return nil, err
	}

	users, err := s.repo.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]dto.UserAccountResponse, 0, len(users))
	for i := range users {
		out = append(out, dto.ToUserAccountResponse(&users[i]))
	}
	return out, nil
}

// Me describes the caller. It is the one route open to an account that is not
// active, so the dashboard can tell a suspended user why nothing else works.
func (s *UserService) Me(ctx context.Context) (*dto.CurrentUserResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	out := &dto.CurrentUserResponse{
		UserID:      principal.Name,
		Status:      principal.Status,
		Authorities: principal.Authorities,
		Permissions: principal.Permissions.Sorted(),
		Admin:       principal.IsAdmin(),
	}
	if out.Status == "" {
		out.Status = auth.StatusActive
	}
	user, err := s.repo.FindByIDWithRoles(ctx, principal.Name)
	switch {
	case err == nil:
		account := dto.ToUserAccountResponse(user)
		out.User = &account
	case !errors.Is(err, gorm.ErrRecordNotFound):
		return nil, err
	}
	return out, nil
}

// SetStatus activates, deactivates or suspends a user. Needs user:set_status.
//
// An administrator cannot change their own status, which would be a way to lock the
// last administrator out. Only a Super Admin may change another administrator's
// status, so an ADMIN cannot suspend the people who appoint admins. Service accounts
// have their own lifecycle under /api/v1/service-accounts.
func (s *UserService) SetStatus(ctx context.Context, userID string, req *dto.UserStatusUpdate) (*dto.UserAccountResponse, error) {
	principal, err := auth.RequirePermission(ctx, permission.UserSetStatus)
	if err != nil {
		return nil, err
	}
	if principal.Name == userID {
		return nil, httpx.Forbidden("You cannot change your own status")
	}

	user, err := s.repo.FindByIDWithRoles(ctx, userID)
	if err != nil {
		return nil, notFoundAs(err, "User not found with ID: %s", userID)
	}
	if user.IsServiceAccount() {
		return nil, httpx.Invalid([]httpx.FieldError{{Field: "userId",
			Message: "Service accounts are managed through /api/v1/service-accounts"}})
	}
	if user.IsAdmin() && !principal.Can(permission.AdminManage) {
		return nil, httpx.Forbidden("Only a Super Admin can change an administrator's status")
	}

	status := req.Status
	user.Status = &status
	if err := s.repo.Save(ctx, user); err != nil {
		return nil, err
	}
	out := dto.ToUserAccountResponse(user)
	return &out, nil
}

// SetAdmin grants or withdraws the ADMIN role. Needs admin:manage, which only SUPER_ADMIN holds by default.
//
// A Super Admin cannot change their own role, and a Super Admin's standing is not
// something this call touches: SUPER_ADMIN accounts are rejected. Service accounts
// never hold administrator rights.
func (s *UserService) SetAdmin(ctx context.Context, userID string, req *dto.UserAdminUpdate) (*dto.UserAccountResponse, error) {
	principal, err := auth.RequirePermission(ctx, permission.AdminManage)
	if err != nil {
		return nil, err
	}
	if principal.Name == userID {
		return nil, httpx.Forbidden("You cannot change your own role")
	}

	user, err := s.repo.FindByIDWithRoles(ctx, userID)
	if err != nil {
		return nil, notFoundAs(err, "User not found with ID: %s", userID)
	}
	if user.IsServiceAccount() {
		return nil, httpx.Invalid([]httpx.FieldError{{Field: "userId",
			Message: "Service accounts cannot be administrators"}})
	}
	if user.HasRole(model.RoleSuperAdmin) {
		return nil, httpx.Forbidden("A Super Admin's role cannot be changed here")
	}

	if req.Admin {
		err = s.repo.AddRole(ctx, userID, model.RoleAdmin)
	} else {
		err = s.repo.RemoveRole(ctx, userID, model.RoleAdmin)
	}
	if err != nil {
		return nil, err
	}
	user, err = s.repo.FindByIDWithRoles(ctx, userID)
	if err != nil {
		return nil, err
	}
	out := dto.ToUserAccountResponse(user)
	return &out, nil
}

// Update changes a user's profile. A Super Admin may update anyone; everyone else may
// update only themselves.
//
// Only the three profile fields are writable. Auth method, status and roles are
// deliberately not updatable through this API — letting a self-service update touch
// them would let any user change their own standing.
func (s *UserService) Update(ctx context.Context, userID string, req *dto.UserRegistration) (*dto.UserResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	if principal.Name != userID && !principal.Can(permission.UserUpdateAny) {
		return nil, httpx.Forbidden("Access denied: You can only update your own user information")
	}

	user, err := s.repo.FindByID(ctx, userID)
	if err != nil {
		return nil, notFoundAs(err, "User not found with ID: %s", userID)
	}

	user.Email = req.Email
	user.FirstName = req.FirstName
	user.LastName = req.LastName

	if err := s.repo.Save(ctx, user); err != nil {
		return nil, err
	}
	out := dto.ToUserResponse(user)
	return &out, nil
}

// notFoundAs converts a missing-row error into a 404 and leaves anything else alone.
func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}

// RequireCurrentUser resolves the authenticated caller to their stored user record.
//
// The principal name is used as the user id directly — that equivalence is what makes
// every ownership check work, and it is why a token identifying a caller who was
// never registered fails here rather than silently creating a record.
//
// Three services depend on this: cluster configs, SCP data and batch job processes
// all derive ownership from the token this way.
func RequireCurrentUser(ctx context.Context, repo *repository.UserRepository) (*model.User, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	user, err := repo.FindByID(ctx, principal.Name)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, httpx.NotFound("No user record found for authenticated principal: %s", principal.Name)
		}
		return nil, err
	}
	return user, nil
}
