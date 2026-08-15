// Package service holds the identity vertical's business rules: user registration,
// group ownership and membership, and the root-account bootstrap.
package service

import (
	"context"
	"errors"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"

	model "github.com/apache/airavata/api/iam/model"
	"github.com/apache/airavata/api/iam/repository"
)

// EnsureRootUser inserts a users row for the bootstrap root account if none exists
// yet.
//
// The root token authenticates as a Super Admin without touching this table, so
// admin actions work from a fresh database with no row at all. Owning resources is
// different: SSH endpoint credentials, SCP data and batch job processes resolve the caller
// to a users row and refuse if none exists. This closes that gap on startup instead of
// requiring the manual INSERT documented in INSTALL.md.
func EnsureRootUser(ctx context.Context, db *gorm.DB) error {
	repo := repository.NewUserRepository(db)
	if _, err := repo.FindByID(ctx, auth.RootUsername); err == nil {
		return nil
	} else if !errors.Is(err, gorm.ErrRecordNotFound) {
		return err
	}

	authMethod := model.AuthMethodSystem
	status := model.UserStatusActive
	firstName, lastName := "Root", "Account"
	return repo.Save(ctx, &model.User{
		ID:         auth.RootUsername,
		AuthMethod: &authMethod,
		FirstName:  &firstName,
		LastName:   &lastName,
		Status:     &status,
		CreatedAt:  time.Now().UnixMilli(),
	})
}
