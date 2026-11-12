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
	sharingmodel "github.com/apache/airavata/api/sharing/model"
	sharingrepo "github.com/apache/airavata/api/sharing/repository"
)

// ServiceAccountService administers service accounts: non-human users that
// authenticate with an Airavata-issued, expiring token.
//
// Every operation needs serviceaccount:read or serviceaccount:manage, which ADMIN holds by default. A service account is otherwise an ordinary user —
// it owns what it creates, can be shared with and can join groups — but it can never
// administer anything: its token always resolves to USER authority, and since
// creating service accounts needs ADMIN, a service account cannot mint another.
type ServiceAccountService struct {
	db       *gorm.DB
	accounts *repository.ServiceAccountRepository
	users    *repository.UserRepository
	shares   *sharingrepo.Repository

	// now is the clock, replaceable in tests.
	now func() time.Time
}

// NewServiceAccountService returns a service account service.
func NewServiceAccountService(db *gorm.DB, accounts *repository.ServiceAccountRepository, users *repository.UserRepository, shares *sharingrepo.Repository) *ServiceAccountService {
	return &ServiceAccountService{db: db, accounts: accounts, users: users, shares: shares, now: time.Now}
}

// Create registers a service account and mints its first token. The token is in the
// response and nowhere else; it cannot be read back later.
func (s *ServiceAccountService) Create(ctx context.Context, req *dto.ServiceAccountCreate) (*dto.ServiceAccountTokenResponse, error) {
	admin, err := auth.RequirePermission(ctx, permission.ServiceAccountManage)
	if err != nil {
		return nil, err
	}

	id := auth.ServiceAccountIDPrefix + req.Name
	token, hash, err := auth.GenerateServiceAccountToken()
	if err != nil {
		return nil, err
	}

	now := s.now()
	authMethod := model.AuthMethodServiceAccount
	status := model.UserStatusActive
	user := &model.User{
		ID:         id,
		AuthMethod: &authMethod,
		FirstName:  req.DisplayName,
		Email:      req.Email,
		Status:     &status,
		CreatedAt:  now.UnixMilli(),
		ServiceAccount: &model.ServiceAccount{
			UserID:         id,
			Description:    req.Description,
			CreatedBy:      admin.Name,
			TokenHash:      hash,
			TokenIssuedAt:  now.UnixMilli(),
			TokenExpiresAt: expiry(now, dto.ValidityDaysOrDefault(req.ValidityDays)),
		},
	}

	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if _, err := s.users.WithTx(tx).FindByID(ctx, id); err == nil {
			return httpx.Conflict("A user with ID %s already exists", id)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}
		// Create, not Save: Save would quietly overwrite a row inserted between the
		// check above and here.
		return tx.Create(user).Error
	})
	if err != nil {
		return nil, err
	}

	return &dto.ServiceAccountTokenResponse{
		ServiceAccount: dto.ToServiceAccountResponse(user),
		Token:          token,
	}, nil
}

// List returns every service account.
func (s *ServiceAccountService) List(ctx context.Context) ([]dto.ServiceAccountResponse, error) {
	if _, err := auth.RequirePermission(ctx, permission.ServiceAccountRead); err != nil {
		return nil, err
	}
	users, err := s.accounts.FindAll(ctx)
	if err != nil {
		return nil, err
	}
	out := make([]dto.ServiceAccountResponse, 0, len(users))
	for i := range users {
		out = append(out, dto.ToServiceAccountResponse(&users[i]))
	}
	return out, nil
}

// Get returns one service account.
func (s *ServiceAccountService) Get(ctx context.Context, id string) (*dto.ServiceAccountResponse, error) {
	if _, err := auth.RequirePermission(ctx, permission.ServiceAccountRead); err != nil {
		return nil, err
	}
	user, err := s.require(ctx, s.accounts, id)
	if err != nil {
		return nil, err
	}
	out := dto.ToServiceAccountResponse(user)
	return &out, nil
}

// Update changes a service account's display name, description, email or status.
// Setting the status to anything but ACTIVE stops its token working immediately,
// without discarding it, so the account can be re-enabled.
func (s *ServiceAccountService) Update(ctx context.Context, id string, req *dto.ServiceAccountUpdate) (*dto.ServiceAccountResponse, error) {
	if _, err := auth.RequirePermission(ctx, permission.ServiceAccountManage); err != nil {
		return nil, err
	}

	var user *model.User
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var err error
		if user, err = s.require(ctx, s.accounts.WithTx(tx), id); err != nil {
			return err
		}
		if req.DisplayName != nil {
			user.FirstName = req.DisplayName
		}
		if req.Email != nil {
			user.Email = req.Email
		}
		if req.Status != nil {
			user.Status = req.Status
		}
		if req.Description != nil {
			user.ServiceAccount.Description = req.Description
		}
		if err := tx.Omit("ServiceAccount", "Roles", "Memberships").Save(user).Error; err != nil {
			return err
		}
		return s.accounts.WithTx(tx).Save(ctx, user.ServiceAccount)
	})
	if err != nil {
		return nil, err
	}
	out := dto.ToServiceAccountResponse(user)
	return &out, nil
}

// RenewToken extends the current token's life to validityDays from now. The token
// itself is unchanged, so whatever holds it keeps working. An expired token can be
// renewed back into use.
func (s *ServiceAccountService) RenewToken(ctx context.Context, id string, req *dto.ServiceAccountTokenRequest) (*dto.ServiceAccountResponse, error) {
	if _, err := auth.RequirePermission(ctx, permission.ServiceAccountManage); err != nil {
		return nil, err
	}

	var user *model.User
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var err error
		if user, err = s.require(ctx, s.accounts.WithTx(tx), id); err != nil {
			return err
		}
		user.ServiceAccount.TokenExpiresAt = expiry(s.now(), dto.ValidityDaysOrDefault(req.ValidityDays))
		return s.accounts.WithTx(tx).Save(ctx, user.ServiceAccount)
	})
	if err != nil {
		return nil, err
	}
	out := dto.ToServiceAccountResponse(user)
	return &out, nil
}

// RegenerateToken replaces the token with a new one. The old token stops working in
// the same write; this is how a leaked token is revoked.
func (s *ServiceAccountService) RegenerateToken(ctx context.Context, id string, req *dto.ServiceAccountTokenRequest) (*dto.ServiceAccountTokenResponse, error) {
	if _, err := auth.RequirePermission(ctx, permission.ServiceAccountManage); err != nil {
		return nil, err
	}

	token, hash, err := auth.GenerateServiceAccountToken()
	if err != nil {
		return nil, err
	}

	var user *model.User
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var err error
		if user, err = s.require(ctx, s.accounts.WithTx(tx), id); err != nil {
			return err
		}
		now := s.now()
		user.ServiceAccount.TokenHash = hash
		user.ServiceAccount.TokenIssuedAt = now.UnixMilli()
		user.ServiceAccount.TokenExpiresAt = expiry(now, dto.ValidityDaysOrDefault(req.ValidityDays))
		return s.accounts.WithTx(tx).Save(ctx, user.ServiceAccount)
	})
	if err != nil {
		return nil, err
	}
	return &dto.ServiceAccountTokenResponse{
		ServiceAccount: dto.ToServiceAccountResponse(user),
		Token:          token,
	}, nil
}

// Delete removes a service account, its token, its group memberships and every share
// granted to it.
//
// An account that still owns records cannot be deleted — ownership is never
// transferred implicitly — and gets 409. Disabling it (status INACTIVE) revokes its
// access while keeping what it owns.
func (s *ServiceAccountService) Delete(ctx context.Context, id string) error {
	if _, err := auth.RequirePermission(ctx, permission.ServiceAccountManage); err != nil {
		return err
	}

	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		user, err := s.require(ctx, s.accounts.WithTx(tx), id)
		if err != nil {
			return err
		}
		if err := s.shares.WithTx(tx).DeleteByPrincipal(ctx, sharingmodel.PrincipalTypeUser, user.ID); err != nil {
			return err
		}
		// Memberships, roles and the token row cascade with the user.
		return tx.Delete(&model.User{}, "user_id = ?", user.ID).Error
	})
	if isForeignKeyViolation(s.db, err) {
		return httpx.Conflict("Service account %s still owns records and cannot be deleted; set its status to INACTIVE to revoke its access instead", id)
	}
	return err
}

func (s *ServiceAccountService) require(ctx context.Context, repo *repository.ServiceAccountRepository, id string) (*model.User, error) {
	user, err := repo.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "Service account not found with ID: %s", id)
	}
	return user, nil
}

// expiry returns the millisecond timestamp validityDays after now.
func expiry(now time.Time, validityDays int) int64 {
	return now.Add(time.Duration(validityDays) * 24 * time.Hour).UnixMilli()
}

// SQLite reports a referencing row as SQLITE_CONSTRAINT_FOREIGNKEY, or as
// SQLITE_CONSTRAINT_TRIGGER when the constraint is ON DELETE RESTRICT — which every
// owner column is. PostgreSQL reports SQLSTATE 23503 either way.
const (
	sqliteConstraintForeignKey = 787
	sqliteConstraintTrigger    = 1811
	postgresForeignKeyViolated = "23503"
)

// isForeignKeyViolation reports whether err is the database refusing a delete because
// a row still references the target.
//
// It inspects the driver error directly rather than enabling TranslateError, which
// would change the errors every other query returns — and the dialect translators
// miss both a wrapped error and SQLite's RESTRICT code.
func isForeignKeyViolation(db *gorm.DB, err error) bool {
	if err == nil {
		return false
	}
	if errors.Is(err, gorm.ErrForeignKeyViolated) {
		return true
	}
	var pg interface{ SQLState() string }
	if errors.As(err, &pg) && pg.SQLState() == postgresForeignKeyViolated {
		return true
	}
	var lite interface{ Code() int }
	if errors.As(err, &lite) {
		if c := lite.Code(); c == sqliteConstraintForeignKey || c == sqliteConstraintTrigger {
			return true
		}
	}
	if t, ok := db.Dialector.(gorm.ErrorTranslator); ok {
		return errors.Is(t.Translate(err), gorm.ErrForeignKeyViolated)
	}
	return false
}
