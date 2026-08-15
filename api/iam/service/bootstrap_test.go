package service_test

import (
	"context"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/db"

	model "github.com/apache/airavata/api/iam/model"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	service "github.com/apache/airavata/api/iam/service"
)

func openTestDB(t *testing.T) *gorm.DB {
	t.Helper()
	gdb, err := gorm.Open(sqlite.Open("file::memory:?_pragma=foreign_keys(1)"), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatalf("automigrate: %v", err)
	}
	return gdb
}

func TestEnsureRootUserCreatesMissingRow(t *testing.T) {
	gdb := openTestDB(t)
	ctx := context.Background()

	if err := service.EnsureRootUser(ctx, gdb); err != nil {
		t.Fatalf("EnsureRootUser: %v", err)
	}

	repo := iamrepo.NewUserRepository(gdb)
	user, err := repo.FindByID(ctx, auth.RootUsername)
	if err != nil {
		t.Fatalf("FindByID: %v", err)
	}
	if user.Status == nil || *user.Status != model.UserStatusActive {
		t.Errorf("Status = %v, want ACTIVE", user.Status)
	}
	if user.AuthMethod == nil || *user.AuthMethod != model.AuthMethodSystem {
		t.Errorf("AuthMethod = %v, want SYSTEM", user.AuthMethod)
	}
}

func TestEnsureRootUserLeavesExistingRowAlone(t *testing.T) {
	gdb := openTestDB(t)
	ctx := context.Background()

	repo := iamrepo.NewUserRepository(gdb)
	email := "root@example.org"
	existing := &model.User{ID: auth.RootUsername, Email: &email, CreatedAt: 1}
	if err := repo.Save(ctx, existing); err != nil {
		t.Fatalf("seed root user: %v", err)
	}

	if err := service.EnsureRootUser(ctx, gdb); err != nil {
		t.Fatalf("EnsureRootUser: %v", err)
	}

	user, err := repo.FindByID(ctx, auth.RootUsername)
	if err != nil {
		t.Fatalf("FindByID: %v", err)
	}
	if user.Email == nil || *user.Email != email {
		t.Errorf("Email = %v, want %q (existing row should not be overwritten)", user.Email, email)
	}
}
