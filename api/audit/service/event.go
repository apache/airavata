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

// Package service serves the audit log to administrators.
package service

import (
	"context"

	"github.com/apache/airavata/internal/auth"

	"github.com/apache/airavata/api/audit/dto"
	"github.com/apache/airavata/api/audit/repository"
)

// EventService reads the audit log. Every call needs ADMIN or SUPER_ADMIN: the log
// names what every user did.
type EventService struct{ events *repository.EventRepository }

// NewEventService returns a service over events.
func NewEventService(events *repository.EventRepository) *EventService {
	return &EventService{events: events}
}

// List returns one page of events matching q, newest first.
func (s *EventService) List(ctx context.Context, q *dto.EventQuery) (*dto.EventPage, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	found, err := s.events.Find(ctx, q)
	if err != nil {
		return nil, err
	}

	page := &dto.EventPage{Items: make([]dto.EventResponse, 0, len(found))}
	if len(found) > q.Limit {
		// The extra row only signals a next page; the cursor is the last row returned.
		found = found[:q.Limit]
		next := found[len(found)-1].ID
		page.NextCursor = &next
	}
	for i := range found {
		page.Items = append(page.Items, dto.NewEventResponse(&found[i]))
	}
	return page, nil
}

// EntityTypes lists the entity types the log holds events for.
func (s *EventService) EntityTypes(ctx context.Context) ([]string, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}
	out, err := s.events.EntityTypes(ctx)
	if out == nil {
		out = []string{}
	}
	return out, err
}
