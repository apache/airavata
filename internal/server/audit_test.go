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

package server_test

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"strings"
	"testing"

	auditmodel "github.com/apache/airavata/api/audit/model"
	iammodel "github.com/apache/airavata/api/iam/model"
	"github.com/apache/airavata/internal/auth"
	"gorm.io/gorm"
)

func (h *harness) auditEvents(entityType string) []auditmodel.Event {
	h.t.Helper()
	var evs []auditmodel.Event
	if err := h.db.Where("entity_type = ?", entityType).Order("id").Find(&evs).Error; err != nil {
		h.t.Fatalf("read audit events: %v", err)
	}
	return evs
}

func TestAuditRecordsCreateUpdateDeleteWithActor(t *testing.T) {
	h := newHarness(t)

	groupID := h.seedGroup("g1", tokenAlice)
	h.mustDo("PUT", "/api/v1/groups/"+groupID, tokenAlice, map[string]any{"groupName": "g2"}, http.StatusOK)
	h.mustDo("DELETE", "/api/v1/groups/"+groupID, tokenAlice, nil, http.StatusNoContent)

	var got []string
	for _, e := range h.auditEvents("groups") {
		if e.Actor != "alice" || e.EntityID != groupID || e.OccurredAt == 0 {
			t.Errorf("unexpected event: %+v", e)
		}
		got = append(got, string(e.Action))
	}
	want := []string{"CREATE", "UPDATE", "DELETE"}
	if len(got) != len(want) {
		t.Fatalf("group actions = %v, want %v", got, want)
	}
	for i := range want {
		if got[i] != want[i] {
			t.Fatalf("group actions = %v, want %v", got, want)
		}
	}
}

func TestAuditIgnoresReadsAndRejectedRequests(t *testing.T) {
	h := newHarness(t)
	h.do("GET", "/api/v1/groups", tokenAlice, nil)
	h.do("DELETE", "/api/v1/groups/does-not-exist", tokenAlice, nil)
	h.do("POST", "/api/v1/groups", tokenAlice, map[string]any{"groupName": ""})

	var n int64
	h.db.Model(&auditmodel.Event{}).Where("entity_type = ?", "groups").Count(&n)
	if n != 0 {
		t.Errorf("got %d group events, want none", n)
	}
}

func TestAuditAttributesContextlessWritesToSystem(t *testing.T) {
	h := newHarness(t)
	if err := h.db.Create(&iammodel.User{ID: "sys-made", CreatedAt: 1}).Error; err != nil {
		t.Fatal(err)
	}
	evs := h.auditEvents("users")
	last := evs[len(evs)-1]
	if last.Actor != auditmodel.SystemActor || last.EntityID != "sys-made" || last.Action != auditmodel.ActionCreate {
		t.Errorf("unexpected event: %+v", last)
	}

	ctx := auth.WithPrincipal(context.Background(), &auth.Principal{Name: "carol"})
	if err := h.db.WithContext(ctx).Model(&iammodel.User{ID: "sys-made"}).Update("created_at", 2).Error; err != nil {
		t.Fatal(err)
	}
	evs = h.auditEvents("users")
	last = evs[len(evs)-1]
	if last.Actor != "carol" || last.Action != auditmodel.ActionUpdate || last.ChangedFields != "created_at" {
		t.Errorf("unexpected update event: %+v", last)
	}
}

func TestAuditRollsBackWithTheChange(t *testing.T) {
	h := newHarness(t)
	boom := errors.New("boom")
	err := h.db.Transaction(func(tx *gorm.DB) error {
		if err := tx.Create(&iammodel.User{ID: "rolled-back", CreatedAt: 1}).Error; err != nil {
			return err
		}
		return boom
	})
	if !errors.Is(err, boom) {
		t.Fatalf("err = %v", err)
	}
	var n int64
	h.db.Model(&auditmodel.Event{}).Where("entity_id = ?", "rolled-back").Count(&n)
	if n != 0 {
		t.Errorf("a rolled-back change left %d audit rows", n)
	}
}

type auditPage struct {
	Items []struct {
		ID         int64  `json:"id"`
		OccurredAt int64  `json:"occurredAt"`
		Actor      string `json:"actor"`
		Action     string `json:"action"`
		EntityType string `json:"entityType"`
		EntityID   string `json:"entityId"`
	} `json:"items"`
	NextCursor *int64 `json:"nextCursor"`
}

func (h *harness) auditPage(query string) auditPage {
	h.t.Helper()
	rec := h.do("GET", "/api/v1/audit-events"+query, tokenAdmin, nil)
	if rec.Code != http.StatusOK {
		h.t.Fatalf("GET audit-events%s: status = %d\nbody: %s", query, rec.Code, rec.Body.String())
	}
	var out auditPage
	if err := json.Unmarshal(rec.Body.Bytes(), &out); err != nil {
		h.t.Fatal(err)
	}
	return out
}

func TestAuditEndpointIsAdminOnly(t *testing.T) {
	h := newHarness(t)
	for _, path := range []string{"/api/v1/audit-events", "/api/v1/audit-events/entity-types"} {
		if rec := h.do("GET", path, tokenAlice, nil); rec.Code != http.StatusForbidden {
			t.Errorf("%s as a user: status = %d, want 403", path, rec.Code)
		}
		if rec := h.do("GET", path, "", nil); rec.Code != http.StatusUnauthorized {
			t.Errorf("%s anonymous: status = %d, want 401", path, rec.Code)
		}
		if rec := h.do("GET", path, tokenAdmin, nil); rec.Code != http.StatusOK {
			t.Errorf("%s as an admin: status = %d, want 200", path, rec.Code)
		}
	}
}

func TestAuditEndpointFilters(t *testing.T) {
	h := newHarness(t)
	h.seedGroup("a-group", tokenAlice)
	h.seedGroup("b-group", tokenBob)
	h.seedGroup("b-group-2", tokenBob)

	if got := h.auditPage("?userId=bob&entityType=groups").Items; len(got) != 2 {
		t.Errorf("bob's group events = %d, want 2", len(got))
	}
	for _, e := range h.auditPage("?userId=alice").Items {
		if e.Actor != "alice" {
			t.Errorf("userId filter returned %+v", e)
		}
	}
	// A second type, so the entity-type filter has something to exclude.
	if n := len(h.auditPage("?entityType=users").Items); n == 0 {
		t.Fatal("seeded users should have events")
	}
	for _, e := range h.auditPage("?entityType=users").Items {
		if e.EntityType != "users" {
			t.Errorf("entityType filter returned %+v", e)
		}
	}

	all := h.auditPage("")
	if all.Items[0].ID < all.Items[len(all.Items)-1].ID {
		t.Error("events should be newest first")
	}

	// Dates: from is inclusive, to exclusive.
	t0 := all.Items[len(all.Items)-1].OccurredAt
	t1 := all.Items[0].OccurredAt
	if n := len(h.auditPage(fmt.Sprintf("?from=%d", t1+1)).Items); n != 0 {
		t.Errorf("from after every event returned %d", n)
	}
	if n := len(h.auditPage(fmt.Sprintf("?to=%d", t0)).Items); n != 0 {
		t.Errorf("to at the first event returned %d, want none (exclusive)", n)
	}
	if n := len(h.auditPage(fmt.Sprintf("?from=%d&to=%d", t0, t1+1)).Items); n != len(all.Items) {
		t.Errorf("full range returned %d, want %d", n, len(all.Items))
	}

	types := h.do("GET", "/api/v1/audit-events/entity-types", tokenAdmin, nil).Body.String()
	if !strings.Contains(types, `"groups"`) || !strings.Contains(types, `"users"`) {
		t.Errorf("entity types = %s", types)
	}
}

func TestAuditEndpointPagesWithCursor(t *testing.T) {
	h := newHarness(t)
	for i := 0; i < 5; i++ {
		h.seedGroup(fmt.Sprintf("g%d", i), tokenAlice)
	}

	seen := map[int64]bool{}
	query := "?entityType=groups&limit=2"
	for pages := 0; ; pages++ {
		if pages > 5 {
			t.Fatal("paging did not terminate")
		}
		p := h.auditPage(query)
		for _, e := range p.Items {
			if seen[e.ID] {
				t.Fatalf("event %d returned twice", e.ID)
			}
			seen[e.ID] = true
		}
		if p.NextCursor == nil {
			break
		}
		query = fmt.Sprintf("?entityType=groups&limit=2&before=%d", *p.NextCursor)
	}
	if len(seen) != 5 {
		t.Errorf("paged through %d events, want 5", len(seen))
	}
}

func TestAuditEndpointRejectsBadQuery(t *testing.T) {
	h := newHarness(t)
	for _, q := range []string{"?limit=0", "?limit=100000", "?from=abc", "?from=10&to=5"} {
		if rec := h.do("GET", "/api/v1/audit-events"+q, tokenAdmin, nil); rec.Code != http.StatusBadRequest {
			t.Errorf("%s: status = %d, want 400", q, rec.Code)
		}
	}
}
