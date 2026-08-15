package repository

import (
	"context"

	"gorm.io/gorm"

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
