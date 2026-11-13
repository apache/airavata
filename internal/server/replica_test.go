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
	"strings"
	"testing"
)

func s3Replica(storageID, key string) map[string]any {
	return map[string]any{"dataStorageId": storageID, "dataStorageType": "S3", "path": key}
}

// A product's own storage and path are its primary replica, from registration on, and
// the fields clients already read do not change.
func TestProductHasAPrimaryReplicaThatFollowsIt(t *testing.T) {
	h := newHarness(t)
	storageID, productID := h.seedProduct("primary", tokenAlice)
	base := "/api/v1/data-products/" + productID

	got := h.list(base+"/replicas", tokenAlice)
	if len(got) != 1 {
		t.Fatalf("replicas = %d, want the primary alone", len(got))
	}
	if got[0]["isPrimary"] != true || got[0]["dataStorageId"] != storageID ||
		got[0]["dataStorageType"] != "SCP" || got[0]["path"] != "/scratch/primary" {
		t.Errorf("primary replica = %v", got[0])
	}

	_, otherStorage := h.seedStorage("moved", tokenAlice)
	updated := h.mustDo(http.MethodPut, base, tokenAlice, map[string]any{
		"dataName": "primary", "isFile": true, "path": "/scratch/moved", "dataStorageId": otherStorage,
	}, http.StatusOK)
	if updated["dataStorageId"] != otherStorage || updated["path"] != "/scratch/moved" {
		t.Errorf("product location = %v %v, want the update applied", updated["dataStorageId"], updated["path"])
	}
	replicas := updated["replicas"].([]any)
	if len(replicas) != 1 {
		t.Fatalf("replicas after a move = %d, want the primary moved rather than copied", len(replicas))
	}
	if first := replicas[0].(map[string]any); first["dataStorageId"] != otherStorage || first["path"] != "/scratch/moved" {
		t.Errorf("primary after a move = %v", first)
	}
}

// An S3 location sits beside the SCP one and is reported by every read of the product,
// while the primary location stays where it was.
func TestProductCanHaveReplicasOnDifferentStorageKinds(t *testing.T) {
	h := newHarness(t)
	storageID, productID := h.seedProduct("multi", tokenAlice)
	_, s3StorageID := h.seedS3Storage("multi", tokenAlice)
	base := "/api/v1/data-products/" + productID

	added := h.mustDo(http.MethodPost, base+"/replicas", tokenAlice,
		s3Replica(s3StorageID, "runs/multi/out.dat"), http.StatusCreated)
	if added["isPrimary"] != false || added["dataProductId"] != productID {
		t.Errorf("added replica = %v, want a secondary of this product", added)
	}

	got := h.mustDo(http.MethodGet, base, tokenAlice, nil, http.StatusOK)
	if got["dataStorageId"] != storageID || got["dataStorageType"] != "SCP" {
		t.Errorf("primary location moved: %v %v", got["dataStorageId"], got["dataStorageType"])
	}
	replicas := got["replicas"].([]any)
	if len(replicas) != 2 {
		t.Fatalf("replicas = %d, want 2", len(replicas))
	}
	if first := replicas[0].(map[string]any); first["isPrimary"] != true {
		t.Errorf("first replica = %v, want the primary", first)
	}
	if second := replicas[1].(map[string]any); second["dataStorageType"] != "S3" || second["path"] != "runs/multi/out.dat" {
		t.Errorf("second replica = %v, want the S3 one", second)
	}

	mine := h.list("/api/v1/data-products/me", tokenAlice)
	if len(mine) != 1 || len(mine[0]["replicas"].([]any)) != 2 {
		t.Errorf("/me = %v, want the product with both replicas", mine)
	}

	// An update that does not mention replicas leaves the secondary alone.
	updated := h.mustDo(http.MethodPut, base, tokenAlice, map[string]any{
		"dataName": "renamed", "isFile": true, "path": "/scratch/multi", "dataStorageId": storageID,
	}, http.StatusOK)
	if len(updated["replicas"].([]any)) != 2 {
		t.Errorf("replicas after an update = %v, want both kept", updated["replicas"])
	}
}

// A replica is a statement about a product and a place, so it takes standing on both.
func TestReplicaPermissions(t *testing.T) {
	h := newHarness(t)
	_, productID := h.seedProduct("guarded", tokenAlice)
	_, aliceS3 := h.seedS3Storage("alices", tokenAlice)
	_, bobS3 := h.seedS3Storage("bobs", tokenBob)
	base := "/api/v1/data-products/" + productID + "/replicas"

	if rec := h.do(http.MethodGet, base, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("list with no standing: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodGet, base, "", nil); rec.Code != http.StatusUnauthorized {
		t.Errorf("anonymous list: status = %d, want 401", rec.Code)
	}

	shareID := h.shareProductWithUser(productID, "bob", "READ", tokenAlice)
	if got := h.list(base, tokenBob); len(got) != 1 {
		t.Errorf("list with READ = %d replicas, want 1", len(got))
	}
	if rec := h.do(http.MethodPost, base, tokenBob, s3Replica(bobS3, "a/key")); rec.Code != http.StatusForbidden {
		t.Errorf("add with READ: status = %d, want 403", rec.Code)
	}

	// Alice's own product, but a bucket nobody shared with her.
	if rec := h.do(http.MethodPost, base, tokenAlice, s3Replica(bobS3, "a/key")); rec.Code != http.StatusForbidden {
		t.Errorf("add on an unreachable storage: status = %d, want 403", rec.Code)
	}

	// With WRITE on the product, bob can add a replica on a bucket of his own, but not
	// on alice's.
	h.widenShare(productID, shareID, "WRITE", tokenAlice)
	if rec := h.do(http.MethodPost, base, tokenBob, s3Replica(aliceS3, "a/key")); rec.Code != http.StatusForbidden {
		t.Errorf("grantee adding on a storage they cannot read: status = %d, want 403", rec.Code)
	}
	added := h.mustDo(http.MethodPost, base, tokenBob, s3Replica(bobS3, "a/key"), http.StatusCreated)
	h.mustDo(http.MethodDelete, base+"/"+added["replicaId"].(string), tokenBob, nil, http.StatusNoContent)
}

func TestReplicaValidation(t *testing.T) {
	h := newHarness(t)
	storageID, productID := h.seedProduct("valid", tokenAlice)
	_, s3StorageID := h.seedS3Storage("valid", tokenAlice)
	base := "/api/v1/data-products/" + productID + "/replicas"

	for name, tc := range map[string]struct {
		body  map[string]any
		field string
	}{
		"blank storage":   {map[string]any{"dataStorageId": " ", "path": "/a"}, "dataStorageId"},
		"blank path":      {map[string]any{"dataStorageId": storageID, "path": ""}, "path"},
		"HPC type":        {map[string]any{"dataStorageId": storageID, "dataStorageType": "HPC", "path": "/a"}, "dataStorageType"},
		"unknown type":    {map[string]any{"dataStorageId": storageID, "dataStorageType": "FTP", "path": "/a"}, "dataStorageType"},
		"long SCP path":   {map[string]any{"dataStorageId": storageID, "path": "/" + strings.Repeat("a", 2048)}, "path"},
		"leading slash":   {s3Replica(s3StorageID, "/a/key"), "path"},
		"dot-dot segment": {s3Replica(s3StorageID, "a/../key"), "path"},
		"dot segment":     {s3Replica(s3StorageID, "a/./key"), "path"},
		"control char":    {s3Replica(s3StorageID, "a\nkey"), "path"},
		"long object key": {s3Replica(s3StorageID, strings.Repeat("a", 1025)), "path"},
	} {
		rec := h.do(http.MethodPost, base, tokenAlice, tc.body)
		if rec.Code != http.StatusBadRequest {
			t.Errorf("%s: status = %d, want 400", name, rec.Code)
			continue
		}
		if got := firstFieldError(t, rec); got != tc.field {
			t.Errorf("%s: field error = %q, want %s", name, got, tc.field)
		}
	}

	// An id of the wrong kind resolves to nothing rather than to the other table.
	if rec := h.do(http.MethodPost, base, tokenAlice, s3Replica(storageID, "a/key")); rec.Code != http.StatusNotFound {
		t.Errorf("SCP id named as S3: status = %d, want 404", rec.Code)
	}
	if rec := h.do(http.MethodPost, base, tokenAlice, s3Replica("nope", "a/key")); rec.Code != http.StatusNotFound {
		t.Errorf("unknown storage: status = %d, want 404", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/data-products/nope/replicas", tokenAlice, s3Replica(s3StorageID, "a/key")); rec.Code != http.StatusNotFound {
		t.Errorf("unknown product: status = %d, want 404", rec.Code)
	}

	// A second replica on the same SCP storage, at another path, is a location too.
	h.mustDo(http.MethodPost, base, tokenAlice,
		map[string]any{"dataStorageId": storageID, "path": "/backup/valid"}, http.StatusCreated)
}

func TestReplicaConflictsAndRemoval(t *testing.T) {
	h := newHarness(t)
	storageID, productID := h.seedProduct("conflict", tokenAlice)
	_, otherProduct := h.seedProduct("other", tokenAlice)
	_, s3StorageID := h.seedS3Storage("conflict", tokenAlice)
	base := "/api/v1/data-products/" + productID + "/replicas"

	if rec := h.do(http.MethodPost, base, tokenAlice,
		map[string]any{"dataStorageId": storageID, "path": "/scratch/conflict"}); rec.Code != http.StatusConflict {
		t.Errorf("replica at the primary location: status = %d, want 409", rec.Code)
	}
	added := h.mustDo(http.MethodPost, base, tokenAlice, s3Replica(s3StorageID, "a/key"), http.StatusCreated)
	if rec := h.do(http.MethodPost, base, tokenAlice, s3Replica(s3StorageID, "a/key")); rec.Code != http.StatusConflict {
		t.Errorf("duplicate replica: status = %d, want 409", rec.Code)
	}

	replicaID := added["replicaId"].(string)
	if rec := h.do(http.MethodDelete, "/api/v1/data-products/"+otherProduct+"/replicas/"+replicaID, tokenAlice, nil); rec.Code != http.StatusNotFound {
		t.Errorf("replica removed through another product: status = %d, want 404", rec.Code)
	}
	if rec := h.do(http.MethodDelete, base+"/"+productID, tokenAlice, nil); rec.Code != http.StatusConflict {
		t.Errorf("removing the primary: status = %d, want 409", rec.Code)
	}
	h.mustDo(http.MethodDelete, base+"/"+replicaID, tokenAlice, nil, http.StatusNoContent)
	if got := h.list(base, tokenAlice); len(got) != 1 {
		t.Errorf("replicas after removal = %d, want 1", len(got))
	}
	if rec := h.do(http.MethodDelete, base+"/"+replicaID, tokenAlice, nil); rec.Code != http.StatusNotFound {
		t.Errorf("removing it twice: status = %d, want 404", rec.Code)
	}
}

// Moving the primary onto a location a secondary already holds must not list it twice.
func TestMovingThePrimaryOntoASecondaryMergesThem(t *testing.T) {
	h := newHarness(t)
	storageID, productID := h.seedProduct("merge", tokenAlice)
	base := "/api/v1/data-products/" + productID
	h.mustDo(http.MethodPost, base+"/replicas", tokenAlice,
		map[string]any{"dataStorageId": storageID, "path": "/backup/merge"}, http.StatusCreated)

	updated := h.mustDo(http.MethodPut, base, tokenAlice, map[string]any{
		"dataName": "merge", "isFile": true, "path": "/backup/merge", "dataStorageId": storageID,
	}, http.StatusOK)
	replicas := updated["replicas"].([]any)
	if len(replicas) != 1 || replicas[0].(map[string]any)["isPrimary"] != true {
		t.Errorf("replicas = %v, want the one primary", replicas)
	}
}

// A storage holding only a secondary replica is still in use.
func TestStorageHoldingAReplicaCannotBeDeleted(t *testing.T) {
	h := newHarness(t)
	_, productID := h.seedProduct("held", tokenAlice)
	_, s3StorageID := h.seedS3Storage("held-s3", tokenAlice)
	_, scpStorageID := h.seedStorage("held-scp", tokenAlice)
	base := "/api/v1/data-products/" + productID + "/replicas"

	s3 := h.mustDo(http.MethodPost, base, tokenAlice, s3Replica(s3StorageID, "a/key"), http.StatusCreated)
	scp := h.mustDo(http.MethodPost, base, tokenAlice,
		map[string]any{"dataStorageId": scpStorageID, "path": "/backup/held"}, http.StatusCreated)

	if rec := h.do(http.MethodDelete, "/api/v1/s3-data-storages/"+s3StorageID, tokenAlice, nil); rec.Code != http.StatusConflict {
		t.Errorf("delete of an S3 storage with a replica: status = %d, want 409", rec.Code)
	}
	if rec := h.do(http.MethodDelete, "/api/v1/scp-data-storages/"+scpStorageID, tokenAlice, nil); rec.Code != http.StatusConflict {
		t.Errorf("delete of an SCP storage with a replica: status = %d, want 409", rec.Code)
	}

	h.mustDo(http.MethodDelete, base+"/"+s3["replicaId"].(string), tokenAlice, nil, http.StatusNoContent)
	h.mustDo(http.MethodDelete, base+"/"+scp["replicaId"].(string), tokenAlice, nil, http.StatusNoContent)
	h.mustDo(http.MethodDelete, "/api/v1/s3-data-storages/"+s3StorageID, tokenAlice, nil, http.StatusNoContent)
	h.mustDo(http.MethodDelete, "/api/v1/scp-data-storages/"+scpStorageID, tokenAlice, nil, http.StatusNoContent)
}

func TestDeletingAProductDeletesItsReplicas(t *testing.T) {
	h := newHarness(t)
	_, productID := h.seedProduct("gone", tokenAlice)
	_, s3StorageID := h.seedS3Storage("gone", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/data-products/"+productID+"/replicas", tokenAlice,
		s3Replica(s3StorageID, "a/key"), http.StatusCreated)

	h.mustDo(http.MethodDelete, "/api/v1/data-products/"+productID, tokenAlice, nil, http.StatusNoContent)

	var left int64
	h.db.Table("data_product_replicas").Where("data_product_id = ?", productID).Count(&left)
	if left != 0 {
		t.Errorf("%d replicas survived the product, want 0", left)
	}
	h.mustDo(http.MethodDelete, "/api/v1/s3-data-storages/"+s3StorageID, tokenAlice, nil, http.StatusNoContent)
}

// The primary location is what launches and the content endpoint read, and neither can
// act on S3 yet, so S3 can only be a secondary location for now.
func TestS3CannotBeAProductsPrimaryLocation(t *testing.T) {
	h := newHarness(t)
	_, s3StorageID := h.seedS3Storage("primary-s3", tokenAlice)
	rec := h.do(http.MethodPost, "/api/v1/data-products", tokenAlice, map[string]any{
		"dataName": "run", "isFile": true, "path": "runs/run", "dataStorageId": s3StorageID, "dataStorageType": "S3",
	})
	if rec.Code != http.StatusBadRequest {
		t.Errorf("product with an S3 primary: status = %d, want 400", rec.Code)
	}
}

// A staging task naming S3 would do nothing and report success until copyData handles it.
func TestS3IsNotYetAStagingStorageType(t *testing.T) {
	h := newHarness(t)
	processID := h.seedProcess("no-s3-staging", tokenAlice)
	rec := h.do(http.MethodPost, "/api/v1/processes/"+processID+"/data-staging-tasks", tokenAlice,
		map[string]any{"sourcePath": "/in", "destinationPath": "/out", "sourceDataStorageType": "S3"})
	if rec.Code != http.StatusBadRequest {
		t.Errorf("staging task naming S3: status = %d, want 400", rec.Code)
	}
}
