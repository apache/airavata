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
	"strings"
	"time"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"

	"github.com/apache/airavata/internal/auth"

	model "github.com/apache/airavata/api/iam/model"
)

// CILogonUserProvisioner resolves a CILogon principal to its users row, creating the
// row the first time the principal authenticates. A new row is SUSPENDED, with the
// profile filled from the CILogon claims, until an administrator activates it. An
// existing row is never changed here.
//
// It runs on every CILogon request, so a status change takes effect on the user's
// next request.
type CILogonUserProvisioner struct {
	DB *gorm.DB
}

// ProvisionUser implements auth.UserProvisioner.
func (p *CILogonUserProvisioner) ProvisionUser(ctx context.Context, principal *auth.Principal) (string, error) {
	db := p.DB.WithContext(ctx)
	var existing model.User
	err := db.Select("user_id", "status").First(&existing, "user_id = ?", principal.Name).Error
	if err == nil {
		return statusOf(&existing), nil
	}
	if !errors.Is(err, gorm.ErrRecordNotFound) {
		return "", err
	}

	authMethod := model.AuthMethodCILogon
	status := model.UserStatusSuspended
	user := &model.User{
		ID:         principal.Name,
		AuthMethod: &authMethod,
		Status:     &status,
		Email:      claim(principal.Attributes, "email"),
		FirstName:  claim(principal.Attributes, "given_name"),
		LastName:   claim(principal.Attributes, "family_name"),
		CreatedAt:  time.Now().UnixMilli(),
	}
	// Two first requests can race. The loser's insert does nothing, and the row it
	// reads back is the winner's.
	if err := db.Clauses(clause.OnConflict{DoNothing: true}).Create(user).Error; err != nil {
		return "", err
	}
	if err := db.Select("user_id", "status").First(&existing, "user_id = ?", principal.Name).Error; err != nil {
		return "", err
	}
	return statusOf(&existing), nil
}

// statusOf reads a row's status. A row with none predates statuses and counts as
// active, as every registration has always set ACTIVE.
func statusOf(u *model.User) string {
	if u.Status == nil {
		return string(model.UserStatusActive)
	}
	return string(*u.Status)
}

func claim(attributes map[string]any, key string) *string {
	s, _ := attributes[key].(string)
	if s = strings.TrimSpace(s); s == "" {
		return nil
	}
	return &s
}
