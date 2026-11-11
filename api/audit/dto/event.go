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

// Package dto holds the audit log's request and response shapes.
package dto

import (
	"net/url"
	"strconv"

	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/audit/model"
)

// Page-size bounds for listing events.
const (
	DefaultLimit = 100
	MaxLimit     = 500
)

// EventQuery selects events. Every filter is optional and they combine with AND.
type EventQuery struct {
	// UserID matches the actor exactly.
	UserID string
	// EntityType matches the record's table exactly, such as "processes".
	EntityType string
	// From is inclusive and To exclusive, both in milliseconds since the epoch; zero
	// leaves that end open.
	From, To int64
	// Before returns only events with a smaller id than this, which is how a caller
	// reads the next page. Zero starts from the newest.
	Before int64
	Limit  int
}

// ParseEventQuery reads and validates the query string of a list request.
func ParseEventQuery(q url.Values) (*EventQuery, error) {
	out := &EventQuery{
		UserID:     q.Get("userId"),
		EntityType: q.Get("entityType"),
		Limit:      DefaultLimit,
	}

	var c httpx.Constraints
	num := func(name string) int64 {
		raw := q.Get(name)
		if raw == "" {
			return 0
		}
		n, err := strconv.ParseInt(raw, 10, 64)
		if err != nil || n < 0 {
			c.Add(name, name+" must be a non-negative integer")
			return 0
		}
		return n
	}
	out.From, out.To, out.Before = num("from"), num("to"), num("before")
	if out.To != 0 && out.From >= out.To {
		c.Add("to", "to must be after from")
	}
	if raw := q.Get("limit"); raw != "" {
		n, err := strconv.Atoi(raw)
		if err != nil || n < 1 || n > MaxLimit {
			c.Add("limit", "limit must be between 1 and "+strconv.Itoa(MaxLimit))
		} else {
			out.Limit = n
		}
	}

	if fields := c.Fields(); len(fields) > 0 {
		return nil, &httpx.Error{Status: 400, Message: "Validation failed", Fields: fields}
	}
	return out, nil
}

// EventResponse is one audit event.
type EventResponse struct {
	ID            int64  `json:"id"`
	OccurredAt    int64  `json:"occurredAt"`
	Actor         string `json:"actor"`
	Action        string `json:"action"`
	EntityType    string `json:"entityType"`
	EntityID      string `json:"entityId"`
	ChangedFields string `json:"changedFields"`
}

// NewEventResponse maps an event to its response.
func NewEventResponse(e *model.Event) EventResponse {
	return EventResponse{
		ID:            e.ID,
		OccurredAt:    e.OccurredAt,
		Actor:         e.Actor,
		Action:        string(e.Action),
		EntityType:    e.EntityType,
		EntityID:      e.EntityID,
		ChangedFields: e.ChangedFields,
	}
}

// EventPage is one page of events, newest first.
type EventPage struct {
	Items []EventResponse `json:"items"`
	// NextCursor is the "before" value that fetches the following page, or null when
	// this was the last.
	NextCursor *int64 `json:"nextCursor"`
}
