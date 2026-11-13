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

// seedS3Storage registers an S3 data storage owned by the caller of token, returning
// the credential it presents along with its id.
func (h *harness) seedS3Storage(name, token string) (credentialID, storageID string) {
	h.t.Helper()
	credentialID = h.seedS3Credential(name, token)
	out := h.mustDo(http.MethodPost, "/api/v1/s3-data-storages", token, map[string]any{
		"dataName": name, "endpointUrl": "https://minio.example.edu:9000",
		"bucketName": name + "-bucket", "pathStyle": true, "s3CredentialId": credentialID,
	}, http.StatusCreated)
	return credentialID, out["dataId"].(string)
}

func (h *harness) shareS3StorageWithUser(storageID, userID, permission, ownerToken string) string {
	h.t.Helper()
	out := h.mustDo(http.MethodPost, "/api/v1/s3-data-storages/"+storageID+"/shares", ownerToken,
		map[string]any{"principalType": "USER", "principalId": userID, "permission": permission}, http.StatusCreated)
	return out["resourceSharingId"].(string)
}

// An S3 storage spells out where the bucket is and inlines the credential's safe
// summary; an omitted region and path-style flag take their defaults.
func TestS3StorageRegistrationDefaultsAndShape(t *testing.T) {
	h := newHarness(t)
	credID := h.seedS3Credential("shape", tokenAlice)

	created := h.mustDo(http.MethodPost, "/api/v1/s3-data-storages", tokenAlice, map[string]any{
		"dataName": "results", "endpointUrl": "https://minio.example.edu:9000/",
		"bucketName": "results", "s3CredentialId": credID,
	}, http.StatusCreated)

	if created["ownerId"] != "alice" {
		t.Errorf("ownerId = %v, want it taken from the token", created["ownerId"])
	}
	if created["region"] != "us-east-1" {
		t.Errorf("region = %v, want the us-east-1 default", created["region"])
	}
	if created["pathStyle"] != false {
		t.Errorf("pathStyle = %v, want false by default", created["pathStyle"])
	}
	if created["endpointUrl"] != "https://minio.example.edu:9000" {
		t.Errorf("endpointUrl = %v, want the trailing slash dropped", created["endpointUrl"])
	}
	if created["permission"] != "WRITE" {
		t.Errorf("permission = %v, want WRITE for the owner", created["permission"])
	}
	cred, ok := created["s3Credential"].(map[string]any)
	if !ok {
		t.Fatalf("storage response did not inline its credential: %v", created)
	}
	if cred["accessKeyId"] != "AKIA-shape" {
		t.Errorf("inlined credential = %v, want its access key id", cred)
	}

	// Without an endpoint it is AWS: the field comes back null rather than empty.
	aws := h.mustDo(http.MethodPost, "/api/v1/s3-data-storages", tokenAlice, map[string]any{
		"dataName": "aws", "region": "eu-west-2", "bucketName": "aws-bucket", "s3CredentialId": credID,
	}, http.StatusCreated)
	if aws["endpointUrl"] != nil || aws["region"] != "eu-west-2" {
		t.Errorf("AWS storage = endpoint %v region %v, want null and eu-west-2", aws["endpointUrl"], aws["region"])
	}
}

// The credential's secret is never reachable through a storage, by its owner or by a
// grantee.
func TestS3StorageNeverLeaksTheCredentialSecret(t *testing.T) {
	h := newHarness(t)
	_, storageID := h.seedS3Storage("leak", tokenAlice)
	h.shareS3StorageWithUser(storageID, "bob", "READ", tokenAlice)

	for _, req := range []struct{ path, token string }{
		{"/api/v1/s3-data-storages/" + storageID, tokenAlice},
		{"/api/v1/s3-data-storages/" + storageID, tokenBob},
		{"/api/v1/s3-data-storages/me", tokenAlice},
		{"/api/v1/s3-data-storages/shared-with-me", tokenBob},
		{"/api/v1/s3-data-storages", tokenAdmin},
	} {
		rec := h.do(http.MethodGet, req.path, req.token, nil)
		if rec.Code != http.StatusOK {
			t.Errorf("GET %s: status = %d, want 200", req.path, rec.Code)
			continue
		}
		if body := rec.Body.String(); strings.Contains(body, "secret-leak") {
			t.Errorf("GET %s leaked the secret: %s", req.path, body)
		}
	}
}

// A storage belongs to whoever registered it; everyone else needs a sharing rule, and
// no share confers control.
func TestS3StorageIsReachableOnlyByOwnerAndGrantees(t *testing.T) {
	h := newHarness(t)
	credID, storageID := h.seedS3Storage("staging", tokenAlice)
	base := "/api/v1/s3-data-storages/" + storageID

	if rec := h.do(http.MethodGet, base, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("read before sharing: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodGet, "/api/v1/s3-data-storages", tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("listing by a non-admin: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodGet, base, "", nil); rec.Code != http.StatusUnauthorized {
		t.Errorf("anonymous read: status = %d, want 401", rec.Code)
	}
	if mine := h.list("/api/v1/s3-data-storages/me", tokenAlice); len(mine) != 1 {
		t.Errorf("alice's own storages = %d, want 1", len(mine))
	}
	if shared := h.list("/api/v1/s3-data-storages/shared-with-me", tokenBob); len(shared) != 0 {
		t.Errorf("shared-with-me before sharing = %d, want 0", len(shared))
	}

	sharingID := h.shareS3StorageWithUser(storageID, "bob", "READ", tokenAlice)

	got := h.mustDo(http.MethodGet, base, tokenBob, nil, http.StatusOK)
	if got["permission"] != "READ" {
		t.Errorf("permission = %v, want READ", got["permission"])
	}
	shared := h.list("/api/v1/s3-data-storages/shared-with-me", tokenBob)
	if len(shared) != 1 || shared[0]["permission"] != "READ" {
		t.Errorf("shared-with-me = %v, want the one storage with READ", shared)
	}
	if mine := h.list("/api/v1/s3-data-storages/me", tokenBob); len(mine) != 0 {
		t.Errorf("bob's own storages = %d, want 0", len(mine))
	}

	repoint := map[string]any{
		"dataName": "renamed", "bucketName": "staging-bucket", "s3CredentialId": credID,
	}
	if rec := h.do(http.MethodPut, base, tokenBob, repoint); rec.Code != http.StatusForbidden {
		t.Errorf("update with READ: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodGet, base+"/shares", tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("grantee listing the shares: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodDelete, base, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("grantee deleting the storage: status = %d, want 403", rec.Code)
	}

	// Widened to WRITE the update goes through, but control still does not.
	h.mustDo(http.MethodPut, base+"/shares/"+sharingID, tokenAlice,
		map[string]any{"permission": "WRITE"}, http.StatusOK)
	updated := h.mustDo(http.MethodPut, base, tokenBob, repoint, http.StatusOK)
	if updated["ownerId"] != "alice" || updated["dataName"] != "renamed" {
		t.Errorf("after a grantee update = owner %v name %v, want alice and renamed",
			updated["ownerId"], updated["dataName"])
	}
	if rec := h.do(http.MethodDelete, base, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("grantee deleting with WRITE: status = %d, want 403", rec.Code)
	}

	// Revoked, the access goes with it.
	h.mustDo(http.MethodDelete, base+"/shares/"+sharingID, tokenAlice, nil, http.StatusNoContent)
	if rec := h.do(http.MethodGet, base, tokenBob, nil); rec.Code != http.StatusForbidden {
		t.Errorf("read after revoke: status = %d, want 403", rec.Code)
	}
}

// Putting a credential on a storage is presenting its secret under a bucket of the
// caller's choosing, so it is refused unless they own the credential. Keeping the one
// a shared storage already has is not an assignment.
func TestAssigningAnS3CredentialRequiresOwningIt(t *testing.T) {
	h := newHarness(t)
	aliceCred := h.seedS3Credential("alices", tokenAlice)

	storage := map[string]any{"dataName": "bobs", "bucketName": "bobs-bucket", "s3CredentialId": aliceCred}
	if rec := h.do(http.MethodPost, "/api/v1/s3-data-storages", tokenBob, storage); rec.Code != http.StatusForbidden {
		t.Errorf("storage on another user's credential: status = %d, want 403", rec.Code)
	}
	if rec := h.do(http.MethodPost, "/api/v1/s3-data-storages", tokenAdmin, storage); rec.Code != http.StatusForbidden {
		t.Errorf("storage registered by an admin on alice's credential: status = %d, want 403", rec.Code)
	}

	_, storageID := h.seedS3Storage("shared", tokenAlice)
	base := "/api/v1/s3-data-storages/" + storageID
	h.shareS3StorageWithUser(storageID, "bob", "WRITE", tokenAlice)
	current := h.mustDo(http.MethodGet, base, tokenBob, nil, http.StatusOK)["s3CredentialId"].(string)

	body := func(credID string) map[string]any {
		return map[string]any{"dataName": "renamed", "bucketName": "shared-bucket", "s3CredentialId": credID}
	}
	h.mustDo(http.MethodPut, base, tokenBob, body(current), http.StatusOK)

	adminCred := h.seedS3Credential("admins", tokenAdmin)
	if rec := h.do(http.MethodPut, base, tokenBob, body(adminCred)); rec.Code != http.StatusForbidden {
		t.Errorf("grantee assigning a third party's credential: status = %d, want 403", rec.Code)
	}

	bobCred := h.seedS3Credential("bobs", tokenBob)
	updated := h.mustDo(http.MethodPut, base, tokenBob, body(bobCred), http.StatusOK)
	if updated["s3CredentialId"] != bobCred {
		t.Errorf("s3CredentialId = %v, want bob's credential %s", updated["s3CredentialId"], bobCred)
	}
}

func TestS3StorageValidation(t *testing.T) {
	h := newHarness(t)
	credID := h.seedS3Credential("validation", tokenAlice)
	body := func(over map[string]any) map[string]any {
		out := map[string]any{"dataName": "scratch", "bucketName": "scratch", "s3CredentialId": credID}
		for k, v := range over {
			out[k] = v
		}
		return out
	}

	for name, tc := range map[string]struct {
		over  map[string]any
		field string
	}{
		"bucket too short":       {map[string]any{"bucketName": "ab"}, "bucketName"},
		"bucket upper case":      {map[string]any{"bucketName": "Scratch"}, "bucketName"},
		"bucket with underscore": {map[string]any{"bucketName": "my_bucket"}, "bucketName"},
		"bucket double dot":      {map[string]any{"bucketName": "my..bucket"}, "bucketName"},
		"bucket trailing hyphen": {map[string]any{"bucketName": "bucket-"}, "bucketName"},
		"endpoint ftp scheme":    {map[string]any{"endpointUrl": "ftp://minio.example.edu"}, "endpointUrl"},
		"endpoint no scheme":     {map[string]any{"endpointUrl": "minio.example.edu"}, "endpointUrl"},
		"endpoint credentials":   {map[string]any{"endpointUrl": "https://key:secret@minio.example.edu"}, "endpointUrl"},
		"endpoint with path":     {map[string]any{"endpointUrl": "https://minio.example.edu/bucket"}, "endpointUrl"},
		"endpoint with query":    {map[string]any{"endpointUrl": "https://minio.example.edu?x=1"}, "endpointUrl"},
		"region with slash":      {map[string]any{"region": "us/east"}, "region"},
		"blank credential id":    {map[string]any{"s3CredentialId": " "}, "s3CredentialId"},
		"blank name":             {map[string]any{"dataName": ""}, "dataName"},
	} {
		rec := h.do(http.MethodPost, "/api/v1/s3-data-storages", tokenAlice, body(tc.over))
		if rec.Code != http.StatusBadRequest {
			t.Errorf("%s: status = %d, want 400", name, rec.Code)
			continue
		}
		if got := firstFieldError(t, rec); got != tc.field {
			t.Errorf("%s: field error = %q, want %s", name, got, tc.field)
		}
	}

	if rec := h.do(http.MethodPost, "/api/v1/s3-data-storages", tokenAlice,
		body(map[string]any{"s3CredentialId": "nope"})); rec.Code != http.StatusNotFound {
		t.Errorf("unknown credential: status = %d, want 404", rec.Code)
	}

	// The common accepted shapes.
	for _, over := range []map[string]any{
		{"endpointUrl": "http://localhost:9000", "pathStyle": true},
		{"endpointUrl": "https://s3.example.edu"},
		{"endpointUrl": "", "region": ""},
		{"bucketName": "my.dotted-bucket.2024"},
	} {
		h.mustDo(http.MethodPost, "/api/v1/s3-data-storages", tokenAlice, body(over), http.StatusCreated)
	}
}

// A credential a storage presents cannot be deleted, and deleting a storage takes its
// shares with it.
func TestS3StorageAndCredentialDeletion(t *testing.T) {
	h := newHarness(t)
	credID, storageID := h.seedS3Storage("in-use", tokenAlice)
	h.shareS3StorageWithUser(storageID, "bob", "READ", tokenAlice)

	if rec := h.do(http.MethodDelete, "/api/v1/s3-credentials/"+credID, tokenAlice, nil); rec.Code != http.StatusConflict {
		t.Errorf("delete of a credential a storage presents: status = %d, want 409", rec.Code)
	}

	h.mustDo(http.MethodDelete, "/api/v1/s3-data-storages/"+storageID, tokenAlice, nil, http.StatusNoContent)
	var shares int64
	h.db.Table("resource_sharings").Where("resource_id = ?", storageID).Count(&shares)
	if shares != 0 {
		t.Errorf("%d shares survived the storage delete, want 0", shares)
	}
	if rec := h.do(http.MethodGet, "/api/v1/s3-data-storages/"+storageID, tokenAlice, nil); rec.Code != http.StatusNotFound {
		t.Errorf("read after delete: status = %d, want 404", rec.Code)
	}

	h.mustDo(http.MethodDelete, "/api/v1/s3-credentials/"+credID, tokenAlice, nil, http.StatusNoContent)
}

// A product's primary location is still an SCP storage. One that names no storage type
// is on SCP, so an S3 storage id does not resolve as its location, and S3 is not a type
// a product's primary location accepts. S3 data is attached to a product as a replica.
func TestS3StorageIsNotYetAPrimaryProductLocation(t *testing.T) {
	h := newHarness(t)
	_, storageID := h.seedS3Storage("not-yet", tokenAlice)
	product := map[string]any{
		"dataName": "run-1", "isFile": true, "path": "runs/run-1", "dataStorageId": storageID,
	}
	if rec := h.do(http.MethodPost, "/api/v1/data-products", tokenAlice, product); rec.Code != http.StatusNotFound {
		t.Errorf("product on an S3 storage id: status = %d, want 404", rec.Code)
	}
	product["dataStorageType"] = "S3"
	if rec := h.do(http.MethodPost, "/api/v1/data-products", tokenAlice, product); rec.Code != http.StatusBadRequest {
		t.Errorf("product with dataStorageType S3: status = %d, want 400", rec.Code)
	}
}
