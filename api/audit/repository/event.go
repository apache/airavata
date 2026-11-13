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

// Package repository reads the audit log.
package repository

import (
	"context"

	"gorm.io/gorm"

	"github.com/apache/airavata/api/audit/dto"
	model "github.com/apache/airavata/api/audit/model"
)

// EventRepository reads audit events. There is deliberately no way to change or remove
// one here: the writes come from the audit plugin, and the log is append-only.
type EventRepository struct{ db *gorm.DB }

// NewEventRepository returns a repository backed by db.
func NewEventRepository(db *gorm.DB) *EventRepository { return &EventRepository{db: db} }

// Find returns up to q.Limit+1 events matching q, newest first. The extra row, if
// present, tells the caller there is another page without a second count query.
func (r *EventRepository) Find(ctx context.Context, q *dto.EventQuery) ([]model.Event, error) {
	tx := r.db.WithContext(ctx).Model(&model.Event{})
	if q.UserID != "" {
		tx = tx.Where("actor = ?", q.UserID)
	}
	if q.EntityType != "" {
		tx = tx.Where("entity_type = ?", q.EntityType)
	}
	if q.From != 0 {
		tx = tx.Where("occurred_at >= ?", q.From)
	}
	if q.To != 0 {
		tx = tx.Where("occurred_at < ?", q.To)
	}
	if q.Before != 0 {
		tx = tx.Where("id < ?", q.Before)
	}

	var out []model.Event
	err := tx.Order("id DESC").Limit(q.Limit + 1).Find(&out).Error
	return out, err
}

// EntityTypes returns every entity type that has an event, alphabetically.
func (r *EventRepository) EntityTypes(ctx context.Context) ([]string, error) {
	var out []string
	err := r.db.WithContext(ctx).Model(&model.Event{}).
		Distinct().Order("entity_type").Pluck("entity_type", &out).Error
	return out, err
}
