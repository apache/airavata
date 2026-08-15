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

// Service is the user management API.
//
// Unlike the other services it predates the @PreAuthorize convention and does its
// authority checks inline. The checks are also asymmetric in ways worth preserving
// rather than tidying, because clients depend on them: registration is SUPER_ADMIN
// only, reading is ADMIN or self, and updating is SUPER_ADMIN or self.
type Service struct {
	repo *UserRepository
	db   *gorm.DB
}

// NewService returns a user service.
func NewService(db *gorm.DB, repo *UserRepository) *Service {
	return &Service{repo: repo, db: db}
}

// Register creates a user. Only a Super Admin may do this.
//
// Status and creation time are set here rather than taken from the request, so a
// caller cannot register an account that is already suspended or backdated.
func (s *Service) Register(ctx context.Context, req *dto.UserRegistration) (*dto.UserResponse, error) {
	if _, err := auth.RequireSuperAdmin(ctx); err != nil {
		return nil, err
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
func (s *Service) Get(ctx context.Context, userID string) (*dto.UserResponse, error) {
	if _, err := auth.RequireSelfOrAdmin(ctx, userID,
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

// List returns every user. Admin only.
func (s *Service) List(ctx context.Context) ([]dto.UserResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	users, err := s.repo.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]dto.UserResponse, 0, len(users))
	for i := range users {
		out = append(out, dto.ToUserResponse(&users[i]))
	}
	return out, nil
}

// Update changes a user's profile. A Super Admin may update anyone; everyone else may
// update only themselves.
//
// Only the three profile fields are writable. Auth method, status and roles are
// deliberately not updatable through this API — letting a self-service update touch
// them would let any user change their own standing.
func (s *Service) Update(ctx context.Context, userID string, req *dto.UserRegistration) (*dto.UserResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	if principal.Name != userID && !principal.HasAnyAuthority(string(model.RoleSuperAdmin)) {
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
