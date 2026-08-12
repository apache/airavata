package iam

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/iam/model"
)

// UserRepository reads and writes user records.
type UserRepository struct{ db *gorm.DB }

// NewUserRepository returns a repository backed by db.
func NewUserRepository(db *gorm.DB) *UserRepository { return &UserRepository{db: db} }

// WithTx returns a repository bound to tx.
func (r *UserRepository) WithTx(tx *gorm.DB) *UserRepository { return &UserRepository{db: tx} }

// FindAll returns every user.
func (r *UserRepository) FindAll(ctx context.Context) ([]model.User, error) {
	var out []model.User
	err := r.db.WithContext(ctx).Find(&out).Error
	return out, err
}

// FindByID returns one user, or gorm.ErrRecordNotFound.
func (r *UserRepository) FindByID(ctx context.Context, id string) (*model.User, error) {
	var out model.User
	if err := r.db.WithContext(ctx).First(&out, "user_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

// Save inserts or updates a user.
func (r *UserRepository) Save(ctx context.Context, u *model.User) error {
	return r.db.WithContext(ctx).Save(u).Error
}

// RequireCurrentUser resolves the authenticated caller to their stored user record.
//
// The principal name is used as the user id directly — that equivalence is what makes
// every ownership check work, and it is why a token identifying a caller who was
// never registered fails here rather than silently creating a record.
//
// Three services depend on this: cluster credentials, SCP data and batch job
// processes all derive ownership from the token this way.
func RequireCurrentUser(ctx context.Context, repo *UserRepository) (*model.User, error) {
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
