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

// Package repository reads and writes session rows. Runs are processes, read and
// written through the process repository.
package repository

import (
	"context"

	"gorm.io/gorm"

	model "github.com/apache/airavata/api/session/model"
)

type SessionRepository struct{ db *gorm.DB }

func NewSessionRepository(db *gorm.DB) *SessionRepository { return &SessionRepository{db: db} }

func (r *SessionRepository) WithTx(tx *gorm.DB) *SessionRepository { return &SessionRepository{db: tx} }

func (r *SessionRepository) FindByID(ctx context.Context, id string) (*model.Session, error) {
	var out model.Session
	if err := r.db.WithContext(ctx).First(&out, "session_id = ?", id).Error; err != nil {
		return nil, err
	}
	return &out, nil
}

func (r *SessionRepository) FindByOwnerID(ctx context.Context, userID string) ([]model.Session, error) {
	var out []model.Session
	err := r.db.WithContext(ctx).Where("user_id = ?", userID).Order("created_at DESC").Find(&out).Error
	return out, err
}

func (r *SessionRepository) Create(ctx context.Context, s *model.Session) error {
	return r.db.WithContext(ctx).Create(s).Error
}

// SwapCurrent points a session at its next run only while old is still current, and
// reports whether it did.
func (r *SessionRepository) SwapCurrent(ctx context.Context, id, old, next string) (bool, error) {
	res := r.db.WithContext(ctx).Model(&model.Session{}).
		Where("session_id = ? AND current_process_id = ?", id, old).
		Update("current_process_id", next)
	return res.RowsAffected == 1, res.Error
}

// Delete removes a session row only while its current run is still the one s read, and
// reports whether it did.
func (r *SessionRepository) Delete(ctx context.Context, s *model.Session) (bool, error) {
	res := r.db.WithContext(ctx).Where("current_process_id = ?", s.CurrentProcessID).Delete(s)
	return res.RowsAffected == 1, res.Error
}
