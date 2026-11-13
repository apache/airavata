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

package repository

import (
	"context"
	"errors"
	"fmt"
	"time"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/permission"

	model "github.com/apache/airavata/api/iam/model"
)

// ServiceAccountRepository reads and writes service account token rows.
type ServiceAccountRepository struct{ db *gorm.DB }

// NewServiceAccountRepository returns a repository backed by db.
func NewServiceAccountRepository(db *gorm.DB) *ServiceAccountRepository {
	return &ServiceAccountRepository{db: db}
}

// WithTx returns a repository bound to tx.
func (r *ServiceAccountRepository) WithTx(tx *gorm.DB) *ServiceAccountRepository {
	return &ServiceAccountRepository{db: tx}
}

// FindAll returns every service account with its user row.
func (r *ServiceAccountRepository) FindAll(ctx context.Context) ([]model.User, error) {
	var out []model.User
	err := r.db.WithContext(ctx).
		Preload("ServiceAccount").
		Preload("ServiceAccount.Permissions").
		Where("auth_method = ?", model.AuthMethodServiceAccount).
		Order("user_id").
		Find(&out).Error
	return out, err
}

// FindByID returns one service account with its user row, or gorm.ErrRecordNotFound
// when id names no service account — including when it names a person.
func (r *ServiceAccountRepository) FindByID(ctx context.Context, id string) (*model.User, error) {
	var out model.User
	err := r.db.WithContext(ctx).
		Preload("ServiceAccount").
		Preload("ServiceAccount.Permissions").
		Where("auth_method = ?", model.AuthMethodServiceAccount).
		First(&out, "user_id = ?", id).Error
	if err != nil {
		return nil, err
	}
	if out.ServiceAccount == nil {
		return nil, gorm.ErrRecordNotFound
	}
	return &out, nil
}

// Save inserts or updates a service account row. Its permissions are written only
// through ReplacePermissions, never as a side effect of saving the account.
func (r *ServiceAccountRepository) Save(ctx context.Context, sa *model.ServiceAccount) error {
	return r.db.WithContext(ctx).Omit(clause.Associations).Save(sa).Error
}

// ReplacePermissions makes want the account's complete set of grants: rows not in want
// are deleted and missing ones inserted, and rows already present are left alone, so an
// unchanged permission leaves no trace in the audit log.
func (r *ServiceAccountRepository) ReplacePermissions(ctx context.Context, id string, want []permission.Permission) error {
	db := r.db.WithContext(ctx)

	var have []model.ServiceAccountPermission
	if err := db.Where("user_id = ?", id).Find(&have).Error; err != nil {
		return err
	}
	keep := make(map[permission.Permission]bool, len(want))
	for _, p := range want {
		keep[p] = true
	}
	present := make(map[permission.Permission]bool, len(have))
	for _, row := range have {
		present[row.Permission] = true
		if !keep[row.Permission] {
			if err := db.Delete(&row).Error; err != nil {
				return err
			}
		}
	}
	for _, p := range want {
		if present[p] {
			continue
		}
		if err := db.Create(&model.ServiceAccountPermission{UserID: id, Permission: p}).Error; err != nil {
			return err
		}
	}
	return nil
}

// ServiceAccountTokenLookup resolves service account tokens against the database. It
// implements auth.ServiceAccountResolver.
type ServiceAccountTokenLookup struct {
	DB *gorm.DB

	// Now is the clock, replaceable in tests. Nil means time.Now.
	Now func() time.Time
}

// ResolveServiceAccount implements auth.ServiceAccountResolver.
//
// A token is accepted only if its digest matches an account's current token, the
// token has not expired, and the account's user row is ACTIVE. Every one of those
// failures is the same ErrInvalidToken, so a caller cannot tell a revoked token from
// one that never existed.
//
// The principal's authorities are fixed to USER by auth.ServiceAccountPrincipal;
// user_roles is deliberately not consulted.
func (l ServiceAccountTokenLookup) ResolveServiceAccount(ctx context.Context, token string) (*auth.Principal, error) {
	var sa model.ServiceAccount
	err := l.DB.WithContext(ctx).
		Where("token_hash = ?", auth.HashServiceAccountToken(token)).
		First(&sa).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, auth.ErrInvalidToken
		}
		return nil, fmt.Errorf("look up service account token: %w", err)
	}

	now := time.Now
	if l.Now != nil {
		now = l.Now
	}
	if sa.ExpiredAt(now().UnixMilli()) {
		return nil, auth.ErrInvalidToken
	}

	var user model.User
	if err := l.DB.WithContext(ctx).First(&user, "user_id = ?", sa.UserID).Error; err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, auth.ErrInvalidToken
		}
		return nil, fmt.Errorf("look up service account user: %w", err)
	}
	if !user.IsServiceAccount() || user.Status == nil || *user.Status != model.UserStatusActive {
		return nil, auth.ErrInvalidToken
	}

	return auth.ServiceAccountPrincipal(user.ID), nil
}
