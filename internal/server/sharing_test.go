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

	sharingmodel "github.com/apache/airavata/api/sharing/model"
)

// One endpoint, one body shape, every kind of record. The same request that grants to
// a user grants to a group, and the same request works whichever resource it is sent
// to — which is the whole point of collapsing the eight tables into one.
func TestSharingIsUniformAcrossResources(t *testing.T) {
	h := newHarness(t)

	_, storageID := h.seedStorage("uniform", tokenAlice)
	productOut := h.mustDo(http.MethodPost, "/api/v1/data-products", tokenAlice, map[string]any{
		"dataName": "uniform", "isFile": true, "path": "/scratch/uniform", "dataStorageId": storageID,
	}, http.StatusCreated)
	productID := productOut["dataId"].(string)
	directoryID := h.seedDataset("uniform", tokenAlice)
	configID, _ := h.seedClusterConfig(tokenAlice, "uniform")

	groupID := h.seedGroup("uniform-team", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob"}, http.StatusCreated)

	for _, res := range []struct {
		base     string
		wantType sharingmodel.ResourceType
		id       string
	}{
		{"/api/v1/scp-data-storages/" + storageID, sharingmodel.ResourceTypeSCPDataStorage, storageID},
		{"/api/v1/data-products/" + productID, sharingmodel.ResourceTypeDataProduct, productID},
		{"/api/v1/virtual-data-directories/" + directoryID, sharingmodel.ResourceTypeVirtualDataDirectory, directoryID},
		{"/api/v1/slurm-cluster-configs/" + configID, sharingmodel.ResourceTypeSlurmClusterConfig, configID},
	} {
		userShare := h.mustDo(http.MethodPost, res.base+"/shares", tokenAlice,
			map[string]any{"principalType": "USER", "principalId": "bob", "permission": "READ"},
			http.StatusCreated)
		if userShare["resourceType"] != string(res.wantType) || userShare["resourceId"] != res.id {
			t.Errorf("%s: share names %v/%v, want %s/%s",
				res.base, userShare["resourceType"], userShare["resourceId"], res.wantType, res.id)
		}

		h.mustDo(http.MethodPost, res.base+"/shares", tokenAlice,
			map[string]any{"principalType": "GROUP", "principalId": groupID, "permission": "WRITE"},
			http.StatusCreated)

		// Both kinds come back from one listing.
		shares := h.list(res.base+"/shares", tokenAlice)
		if len(shares) != 2 {
			t.Fatalf("%s: listed %d shares, want 2", res.base, len(shares))
		}

		// The strongest grant reaching the caller wins, across principal kinds.
		got := h.mustDo(http.MethodGet, res.base, tokenBob, nil, http.StatusOK)
		if got["permission"] != "WRITE" {
			t.Errorf("%s: permission = %v, want WRITE from the stronger group share",
				res.base, got["permission"])
		}

		// Widening and revoking read the same on every resource.
		id := userShare["resourceSharingId"].(string)
		h.mustDo(http.MethodPut, res.base+"/shares/"+id, tokenAlice,
			map[string]any{"permission": "WRITE"}, http.StatusOK)
		h.mustDo(http.MethodDelete, res.base+"/shares/"+id, tokenAlice, nil, http.StatusNoContent)
		if remaining := h.list(res.base+"/shares", tokenAlice); len(remaining) != 1 {
			t.Errorf("%s: %d shares after revoke, want 1", res.base, len(remaining))
		}
	}
}

// An id is unique within its own kind, not across them, so a share must be addressed
// by both. A sharing id belonging to a product is not reachable through a storage.
func TestSharingIdsAreScopedToTheirResourceKind(t *testing.T) {
	h := newHarness(t)

	storageID, productID := h.seedProduct("scoped", tokenAlice)
	share := h.mustDo(http.MethodPost, "/api/v1/data-products/"+productID+"/shares", tokenAlice,
		map[string]any{"principalType": "USER", "principalId": "bob"}, http.StatusCreated)
	id := share["resourceSharingId"].(string)

	if rec := h.do(http.MethodDelete,
		"/api/v1/scp-data-storages/"+storageID+"/shares/"+id, tokenAlice, nil); rec.Code != http.StatusNotFound {
		t.Errorf("product share reached through a storage: status = %d, want 404", rec.Code)
	}
	if rec := h.do(http.MethodPut,
		"/api/v1/scp-data-storages/"+storageID+"/shares/"+id, tokenAlice,
		map[string]any{"permission": "WRITE"}); rec.Code != http.StatusNotFound {
		t.Errorf("product share updated through a storage: status = %d, want 404", rec.Code)
	}
}

// The principal type is what used to be the choice between two endpoints, so an
// unrecognised one is a bad request rather than a route that does not exist.
func TestSharingRejectsBadPrincipal(t *testing.T) {
	h := newHarness(t)
	_, productID := h.seedProduct("bad-principal", tokenAlice)
	base := "/api/v1/data-products/" + productID + "/shares"

	for _, body := range []map[string]any{
		{"principalType": "ROBOT", "principalId": "bob"},
		{"principalId": "bob"},
		{"principalType": "USER"},
		{"principalType": "USER", "principalId": "  "},
	} {
		rec := h.do(http.MethodPost, base, tokenAlice, body)
		if rec.Code != http.StatusBadRequest {
			t.Errorf("body %v: status = %d, want 400", body, rec.Code)
		}
	}
}

// No foreign key points from a share at the group it names, so deleting a group has to
// withdraw what it was granted rather than leaving rows naming a group that is gone.
func TestDeletingGroupWithdrawsItsShares(t *testing.T) {
	h := newHarness(t)

	_, productID := h.seedProduct("group-doomed", tokenAlice)
	groupID := h.seedGroup("doomed", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/groups/"+groupID+"/members", tokenAlice,
		map[string]any{"userId": "bob"}, http.StatusCreated)

	base := "/api/v1/data-products/" + productID
	h.mustDo(http.MethodPost, base+"/shares", tokenAlice,
		map[string]any{"principalType": "GROUP", "principalId": groupID, "permission": "READ"},
		http.StatusCreated)
	h.mustDo(http.MethodGet, base, tokenBob, nil, http.StatusOK)

	h.mustDo(http.MethodDelete, "/api/v1/groups/"+groupID, tokenAlice, nil, http.StatusNoContent)

	if shares := h.list(base+"/shares", tokenAlice); len(shares) != 0 {
		t.Errorf("%d shares survived the group delete, want 0", len(shares))
	}
	if rec := h.do(http.MethodGet, base, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("access through a deleted group: status = %d, want 403", rec.Code)
	}
}

// Deleting a directory cascades the subtree through the self-referencing foreign key,
// but the shares hanging off those nodes point at nothing and are not cascaded with
// them — the service deletes them by id.
func TestDeletingDirectorySubtreeWithdrawsNestedShares(t *testing.T) {
	h := newHarness(t)

	root := h.seedDataset("nested-shares", tokenAlice)
	sub := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "outputs", "parentDirectoryId": root,
	}, http.StatusCreated)
	subID := sub["virtualDataDirectoryId"].(string)

	h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories/"+subID+"/shares", tokenAlice,
		map[string]any{"principalType": "USER", "principalId": "bob", "permission": "READ"},
		http.StatusCreated)

	h.mustDo(http.MethodDelete, "/api/v1/virtual-data-directories/"+root, tokenAlice, nil, http.StatusNoContent)

	var remaining int64
	h.db.Model(&sharingmodel.Sharing{}).
		Where("resource_type = ?", sharingmodel.ResourceTypeVirtualDataDirectory).
		Count(&remaining)
	if remaining != 0 {
		t.Errorf("%d shares survived the subtree delete, want 0", remaining)
	}
}

// A share opens a subtree, and ownership spans a lineage, so sharing a nested node with
// someone who owns the dataset above it would grant nothing they do not already have.
func TestSharingDirectoryWithLineageOwnerIsRefused(t *testing.T) {
	h := newHarness(t)

	root := h.seedDataset("lineage", tokenAlice)
	sub := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "outputs", "parentDirectoryId": root,
	}, http.StatusCreated)
	subID := sub["virtualDataDirectoryId"].(string)

	if rec := h.do(http.MethodPost, "/api/v1/virtual-data-directories/"+subID+"/shares", tokenAlice,
		map[string]any{"principalType": "USER", "principalId": "alice"}); rec.Code != http.StatusConflict {
		t.Errorf("sharing with the owner of an ancestor: status = %d, want 409", rec.Code)
	}
}

// A grant made further up the tree is what the caller holds further down, even when a
// weaker one names the nested node directly: the strongest grant reaching them wins.
func TestStrongestGrantInLineageWins(t *testing.T) {
	h := newHarness(t)

	root := h.seedDataset("strongest-lineage", tokenAlice)
	sub := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "outputs", "parentDirectoryId": root,
	}, http.StatusCreated)
	subID := sub["virtualDataDirectoryId"].(string)

	h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories/"+root+"/shares", tokenAlice,
		map[string]any{"principalType": "USER", "principalId": "bob", "permission": "WRITE"},
		http.StatusCreated)
	h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories/"+subID+"/shares", tokenAlice,
		map[string]any{"principalType": "USER", "principalId": "bob", "permission": "READ"},
		http.StatusCreated)

	got := h.mustDo(http.MethodGet, "/api/v1/virtual-data-directories/"+subID, tokenBob, nil, http.StatusOK)
	if got["permission"] != "WRITE" {
		t.Errorf("permission = %v, want WRITE from the stronger grant at the root", got["permission"])
	}
}
