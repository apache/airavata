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
	"net/http"
	"testing"
)

// Walks the exact sequence docs/sharing.md prints, asserting the status codes and
// permissions it claims.
func TestDocsSharingWorkedExample(t *testing.T) {
	h := newHarness(t)

	_, product := h.seedProduct("doc-example", tokenAlice)
	team := h.seedGroup("doc-team", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/groups/"+team+"/members", tokenAlice,
		map[string]any{"userId": "bob"}, http.StatusCreated)

	base := "/api/v1/data-products/" + product

	// 1. Owner holds WRITE.
	got := h.mustDo(http.MethodGet, base, tokenAlice, nil, http.StatusOK)
	if got["permission"] != "WRITE" {
		t.Errorf("step 1: owner permission = %v, want WRITE", got["permission"])
	}

	// 2. Bob sees nothing: 403, not 404.
	if rec := h.do(http.MethodGet, base, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("step 2: status = %d, want 403", rec.Code)
	}

	// 3-4. Group share, WRITE, reaches Bob through an active membership.
	h.mustDo(http.MethodPost, base+"/shares", tokenAlice,
		map[string]any{"principalType": "GROUP", "principalId": team, "permission": "WRITE"},
		http.StatusCreated)
	got = h.mustDo(http.MethodGet, base, tokenBob, nil, http.StatusOK)
	if got["permission"] != "WRITE" {
		t.Errorf("step 4: permission = %v, want WRITE", got["permission"])
	}

	// 5. A direct READ grant does not cap the group's WRITE.
	h.mustDo(http.MethodPost, base+"/shares", tokenAlice,
		map[string]any{"principalType": "USER", "principalId": "bob", "permission": "READ"},
		http.StatusCreated)
	got = h.mustDo(http.MethodGet, base, tokenBob, nil, http.StatusOK)
	if got["permission"] != "WRITE" {
		t.Errorf("step 5: permission = %v, want WRITE (strongest still applies)", got["permission"])
	}

	// 6. Narrowing the group grant is what actually narrows Bob.
	shares := h.list(base+"/shares", tokenAlice)
	if len(shares) != 2 {
		t.Fatalf("step 6: %d shares listed, want 2", len(shares))
	}
	var groupShare string
	for _, s := range shares {
		if s["principalType"] == "GROUP" {
			groupShare = s["resourceSharingId"].(string)
		}
		// The documented response shape.
		for _, k := range []string{"resourceSharingId", "resourceType", "resourceId", "principalType", "principalId", "permission"} {
			if _, ok := s[k]; !ok {
				t.Errorf("share response is missing documented field %q: %v", k, s)
			}
		}
		if s["resourceType"] != "DATA_PRODUCT" || s["resourceId"] != product {
			t.Errorf("share names %v/%v, want DATA_PRODUCT/%s", s["resourceType"], s["resourceId"], product)
		}
	}
	h.mustDo(http.MethodPut, base+"/shares/"+groupShare, tokenAlice,
		map[string]any{"permission": "READ"}, http.StatusOK)
	got = h.mustDo(http.MethodGet, base, tokenBob, nil, http.StatusOK)
	if got["permission"] != "READ" {
		t.Errorf("step 6: permission = %v, want READ", got["permission"])
	}

	// 7. A grantee cannot read the share list, nor delete the record.
	if rec := h.do(http.MethodGet, base+"/shares", tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("step 7: share list status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodDelete, base, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("step 7: delete status = %d, want 403", rec.Code)
	}

	// Revoke returns 204, as documented.
	h.mustDo(http.MethodDelete, base+"/shares/"+groupShare, tokenAlice, nil, http.StatusNoContent)

	// PUT requires permission; it does not default.
	userShare := h.list(base+"/shares", tokenAlice)[0]["resourceSharingId"].(string)
	if rec := h.do(http.MethodPut, base+"/shares/"+userShare, tokenAlice, map[string]any{}); rec.Code != http.StatusBadRequest {
		t.Errorf("PUT without permission: status = %d, want 400", rec.Code)
	}

	// Owned records never appear in /shared-with-me.
	for _, row := range h.list("/api/v1/data-products/shared-with-me", tokenAlice) {
		if row["dataId"] == product {
			t.Error("an owned product appeared in the owner's shared-with-me listing")
		}
	}
}
