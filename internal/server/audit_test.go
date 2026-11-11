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
	"errors"
	"net/http"
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
