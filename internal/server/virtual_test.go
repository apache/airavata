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

// seedDirectoryProduct registers a directory-valued product, which seedProduct does
// not: a directory node stands for one, and a file node must be refused one.
func (h *harness) seedDirectoryProduct(name, token string) string {
	h.t.Helper()
	_, storageID := h.seedStorage(name, token)
	out := h.mustDo(http.MethodPost, "/api/v1/data-products", token, map[string]any{
		"dataName": name, "isFile": false, "path": "/scratch/" + name, "dataStorageId": storageID,
	}, http.StatusCreated)
	return out["dataId"].(string)
}

// seedDataset creates a root directory owned by the caller of token.
func (h *harness) seedDataset(name, token string) string {
	h.t.Helper()
	out := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", token, map[string]any{
		"directoryName": name,
	}, http.StatusCreated)
	return out["virtualDataDirectoryId"].(string)
}

// A dataset is a tree of references: building one places registered products at names
// of the caller's choosing, and reading it back walks down a level at a time.
func TestVirtualDatasetRoundTrip(t *testing.T) {
	h := newHarness(t)

	_, productID := h.seedProduct("run1", tokenAlice)
	root := h.seedDataset("experiment", tokenAlice)

	sub := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "outputs", "parentDirectoryId": root,
	}, http.StatusCreated)
	subID := sub["virtualDataDirectoryId"].(string)

	file := h.mustDo(http.MethodPost, "/api/v1/virtual-data-files", tokenAlice, map[string]any{
		"fileName": "energy.log", "parentDirectoryId": subID, "dataProductId": productID,
	}, http.StatusCreated)

	if file["dataProductId"] != productID {
		t.Errorf("file dataProductId = %v, want %s", file["dataProductId"], productID)
	}

	// The root lists the subdirectory and no files.
	contents := h.mustDo(http.MethodGet, "/api/v1/virtual-data-directories/"+root+"/contents",
		tokenAlice, nil, http.StatusOK)
	dirs, _ := contents["virtualDataDirectories"].([]any)
	if len(dirs) != 1 {
		t.Fatalf("root holds %d directories, want 1", len(dirs))
	}
	if _, ok := contents["virtualDataFiles"]; ok {
		t.Error("root reported files it does not have")
	}

	// The subdirectory lists the file.
	contents = h.mustDo(http.MethodGet, "/api/v1/virtual-data-directories/"+subID+"/contents",
		tokenAlice, nil, http.StatusOK)
	files, _ := contents["virtualDataFiles"].([]any)
	if len(files) != 1 {
		t.Fatalf("subdirectory holds %d files, want 1", len(files))
	}
	if got := files[0].(map[string]any)["fileName"]; got != "energy.log" {
		t.Errorf("fileName = %v, want energy.log", got)
	}

	// The owner's listing names the root, not every node under it.
	mine := h.list("/api/v1/virtual-data-directories/me", tokenAlice)
	if len(mine) != 1 || mine[0]["virtualDataDirectoryId"] != root {
		t.Errorf("own datasets = %v, want just the root", mine)
	}
}

// One directory listing holds files and directories together, so a name is taken for
// both kinds at once.
func TestVirtualDatasetRejectsDuplicateNames(t *testing.T) {
	h := newHarness(t)

	_, productID := h.seedProduct("dup", tokenAlice)
	root := h.seedDataset("experiment", tokenAlice)

	h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "outputs", "parentDirectoryId": root,
	}, http.StatusCreated)

	h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "outputs", "parentDirectoryId": root,
	}, http.StatusConflict)

	// A file cannot take the name a sibling directory already holds.
	h.mustDo(http.MethodPost, "/api/v1/virtual-data-files", tokenAlice, map[string]any{
		"fileName": "outputs", "parentDirectoryId": root, "dataProductId": productID,
	}, http.StatusConflict)

	// The same name under a different parent is an ordinary tree, not a collision.
	other := h.seedDataset("other", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "outputs", "parentDirectoryId": other,
	}, http.StatusCreated)
}

// A node names a product of the matching kind, and a product-backed directory stands
// for what is on disk rather than holding entries of its own.
func TestVirtualDatasetProductKinds(t *testing.T) {
	h := newHarness(t)

	_, fileProduct := h.seedProduct("afile", tokenAlice)
	dirProduct := h.seedDirectoryProduct("adir", tokenAlice)
	root := h.seedDataset("experiment", tokenAlice)

	// A file cannot stand for a directory product, nor a directory for a file product.
	h.mustDo(http.MethodPost, "/api/v1/virtual-data-files", tokenAlice, map[string]any{
		"fileName": "x", "parentDirectoryId": root, "dataProductId": dirProduct,
	}, http.StatusBadRequest)
	h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "x", "parentDirectoryId": root, "dataProductId": fileProduct,
	}, http.StatusBadRequest)

	grafted := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "staged", "parentDirectoryId": root, "dataProductId": dirProduct,
	}, http.StatusCreated)
	graftedID := grafted["virtualDataDirectoryId"].(string)

	// Nothing can be placed inside it: its contents are whatever is on disk.
	h.mustDo(http.MethodPost, "/api/v1/virtual-data-files", tokenAlice, map[string]any{
		"fileName": "y", "parentDirectoryId": graftedID, "dataProductId": fileProduct,
	}, http.StatusConflict)

	contents := h.mustDo(http.MethodGet, "/api/v1/virtual-data-directories/"+graftedID+"/contents",
		tokenAlice, nil, http.StatusOK)
	if _, ok := contents["virtualDataFiles"]; ok {
		t.Error("a product-backed directory reported entries of its own")
	}
}

// Placing a product in a tree needs READ on it: a caller who could cite a product they
// cannot read would learn its name and shape from the dataset built around it.
func TestVirtualDatasetRefusesUnreadableProduct(t *testing.T) {
	h := newHarness(t)

	_, aliceProduct := h.seedProduct("private", tokenAlice)
	bobRoot := h.seedDataset("bobs", tokenBob)

	h.mustDo(http.MethodPost, "/api/v1/virtual-data-files", tokenBob, map[string]any{
		"fileName": "stolen", "parentDirectoryId": bobRoot, "dataProductId": aliceProduct,
	}, http.StatusForbidden)

	// Once Alice shares the product, Bob may cite it.
	h.mustDo(http.MethodPost, "/api/v1/data-products/"+aliceProduct+"/user-shares", tokenAlice,
		map[string]any{"userId": "bob", "permission": "READ"}, http.StatusCreated)
	h.mustDo(http.MethodPost, "/api/v1/virtual-data-files", tokenBob, map[string]any{
		"fileName": "shared", "parentDirectoryId": bobRoot, "dataProductId": aliceProduct,
	}, http.StatusCreated)
}

// A share names a directory and opens the subtree under it, so access granted at the
// root reaches a node nested below without a share of its own.
func TestVirtualDatasetShareIsInherited(t *testing.T) {
	h := newHarness(t)

	_, productID := h.seedProduct("inherit", tokenAlice)
	root := h.seedDataset("experiment", tokenAlice)
	sub := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "outputs", "parentDirectoryId": root,
	}, http.StatusCreated)
	subID := sub["virtualDataDirectoryId"].(string)

	// Before any share, Bob reaches nothing.
	h.mustDo(http.MethodGet, "/api/v1/virtual-data-directories/"+subID, tokenBob, nil, http.StatusForbidden)

	h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories/"+root+"/user-shares", tokenAlice,
		map[string]any{"userId": "bob", "permission": "READ"}, http.StatusCreated)

	// READ at the root reaches the nested node.
	got := h.mustDo(http.MethodGet, "/api/v1/virtual-data-directories/"+subID, tokenBob, nil, http.StatusOK)
	if got["permission"] != "READ" {
		t.Errorf("permission = %v, want READ", got["permission"])
	}

	// READ is not WRITE: Bob cannot add to the dataset.
	h.mustDo(http.MethodPost, "/api/v1/virtual-data-files", tokenBob, map[string]any{
		"fileName": "bobs.log", "parentDirectoryId": subID, "dataProductId": productID,
	}, http.StatusForbidden)

	// The share names the root, which is what Bob's shared-with-me listing reports.
	shared := h.list("/api/v1/virtual-data-directories/shared-with-me", tokenBob)
	if len(shared) != 1 || shared[0]["virtualDataDirectoryId"] != root {
		t.Fatalf("shared-with-me = %v, want just the shared root", shared)
	}
}

// WRITE lets a grantee build inside someone else's dataset, but deleting a directory
// stays with the owner: the subtree can hold nodes other people were granted access to.
func TestVirtualDatasetWriteShareCannotDeleteDirectory(t *testing.T) {
	h := newHarness(t)

	_, productID := h.seedProduct("write", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/data-products/"+productID+"/user-shares", tokenAlice,
		map[string]any{"userId": "bob", "permission": "READ"}, http.StatusCreated)

	root := h.seedDataset("experiment", tokenAlice)
	h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories/"+root+"/user-shares", tokenAlice,
		map[string]any{"userId": "bob", "permission": "WRITE"}, http.StatusCreated)

	// Bob may add a directory and a file.
	sub := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenBob, map[string]any{
		"directoryName": "bobs-work", "parentDirectoryId": root,
	}, http.StatusCreated)
	subID := sub["virtualDataDirectoryId"].(string)
	file := h.mustDo(http.MethodPost, "/api/v1/virtual-data-files", tokenBob, map[string]any{
		"fileName": "bobs.log", "parentDirectoryId": subID, "dataProductId": productID,
	}, http.StatusCreated)

	// What he added belongs to the dataset's owner, not to him.
	if sub["ownerId"] != "alice" {
		t.Errorf("ownerId = %v, want the dataset owner alice", sub["ownerId"])
	}

	// He may remove a file, which withdraws nobody's access.
	h.mustDo(http.MethodDelete, "/api/v1/virtual-data-files/"+file["virtualDataFileId"].(string),
		tokenBob, nil, http.StatusNoContent)

	// He may not delete a directory, nor manage the share list.
	h.mustDo(http.MethodDelete, "/api/v1/virtual-data-directories/"+subID, tokenBob, nil, http.StatusForbidden)
	h.mustDo(http.MethodGet, "/api/v1/virtual-data-directories/"+root+"/user-shares", tokenBob, nil, http.StatusForbidden)

	// The owner can.
	h.mustDo(http.MethodDelete, "/api/v1/virtual-data-directories/"+subID, tokenAlice, nil, http.StatusNoContent)
}

// Moving a directory into its own subtree would detach it from every root, so it is
// refused rather than stored.
func TestVirtualDatasetRefusesMoveIntoOwnSubtree(t *testing.T) {
	h := newHarness(t)

	root := h.seedDataset("experiment", tokenAlice)
	mid := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "mid", "parentDirectoryId": root,
	}, http.StatusCreated)
	midID := mid["virtualDataDirectoryId"].(string)
	leaf := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "leaf", "parentDirectoryId": midID,
	}, http.StatusCreated)
	leafID := leaf["virtualDataDirectoryId"].(string)

	h.mustDo(http.MethodPut, "/api/v1/virtual-data-directories/"+midID, tokenAlice, map[string]any{
		"directoryName": "mid", "parentDirectoryId": leafID,
	}, http.StatusConflict)

	// A node cannot be its own parent either.
	h.mustDo(http.MethodPut, "/api/v1/virtual-data-directories/"+midID, tokenAlice, map[string]any{
		"directoryName": "mid", "parentDirectoryId": midID,
	}, http.StatusConflict)

	// A legitimate move succeeds.
	h.mustDo(http.MethodPut, "/api/v1/virtual-data-directories/"+leafID, tokenAlice, map[string]any{
		"directoryName": "leaf", "parentDirectoryId": root,
	}, http.StatusOK)
}

// Deleting a directory takes the subtree with it and leaves the products it cited
// registered: a dataset is a set of references, not the data.
func TestVirtualDatasetDeleteCascadesButKeepsProducts(t *testing.T) {
	h := newHarness(t)

	_, productID := h.seedProduct("keep", tokenAlice)
	root := h.seedDataset("experiment", tokenAlice)
	sub := h.mustDo(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
		"directoryName": "outputs", "parentDirectoryId": root,
	}, http.StatusCreated)
	subID := sub["virtualDataDirectoryId"].(string)
	file := h.mustDo(http.MethodPost, "/api/v1/virtual-data-files", tokenAlice, map[string]any{
		"fileName": "energy.log", "parentDirectoryId": subID, "dataProductId": productID,
	}, http.StatusCreated)
	fileID := file["virtualDataFileId"].(string)

	h.mustDo(http.MethodDelete, "/api/v1/virtual-data-directories/"+root, tokenAlice, nil, http.StatusNoContent)

	h.mustDo(http.MethodGet, "/api/v1/virtual-data-directories/"+subID, tokenAlice, nil, http.StatusNotFound)
	h.mustDo(http.MethodGet, "/api/v1/virtual-data-files/"+fileID, tokenAlice, nil, http.StatusNotFound)

	// The product outlives the dataset that cited it.
	h.mustDo(http.MethodGet, "/api/v1/data-products/"+productID, tokenAlice, nil, http.StatusOK)
}

// A node name is one path segment, so a separator in it would let a tree describe a
// location it does not have.
func TestVirtualDatasetValidation(t *testing.T) {
	h := newHarness(t)

	root := h.seedDataset("experiment", tokenAlice)

	for _, name := range []any{"", "  ", "a/b", "..", nil} {
		rec := h.do(http.MethodPost, "/api/v1/virtual-data-directories", tokenAlice, map[string]any{
			"directoryName": name, "parentDirectoryId": root,
		})
		if rec.Code != http.StatusBadRequest {
			t.Errorf("directoryName %v: status = %d, want 400", name, rec.Code)
			continue
		}
		if got := firstFieldError(t, rec); got != "directoryName" {
			t.Errorf("directoryName %v: field error on %q, want directoryName", name, got)
		}
	}

	// A file needs both halves: it is a product placed under a directory.
	rec := h.do(http.MethodPost, "/api/v1/virtual-data-files", tokenAlice, map[string]any{
		"fileName": "x",
	})
	if rec.Code != http.StatusBadRequest {
		t.Fatalf("status = %d, want 400", rec.Code)
	}
}

// Listing every dataset names who holds what data, so it is admin only.
func TestVirtualDatasetListIsAdminOnly(t *testing.T) {
	h := newHarness(t)

	h.seedDataset("experiment", tokenAlice)

	h.mustDo(http.MethodGet, "/api/v1/virtual-data-directories", tokenAlice, nil, http.StatusForbidden)
	all := h.list("/api/v1/virtual-data-directories", tokenAdmin)
	if len(all) != 1 {
		t.Errorf("admin listing has %d directories, want 1", len(all))
	}
	h.mustDo(http.MethodGet, "/api/v1/virtual-data-directories", tokenBogus, nil, http.StatusUnauthorized)
}
