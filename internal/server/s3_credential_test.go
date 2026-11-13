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
	"fmt"
	"net/http"
	"strings"
	"testing"

	auditmodel "github.com/apache/airavata/api/audit/model"
)

func (h *harness) seedS3Credential(name, token string) string {
	h.t.Helper()
	cred := h.mustDo(http.MethodPost, "/api/v1/s3-credentials", token, map[string]any{
		"name": name, "accessKeyId": "AKIA-" + name, "secretAccessKey": "secret-" + name,
	}, http.StatusCreated)
	return cred["s3CredentialId"].(string)
}

type storedS3Credential struct {
	Name            string
	AccessKeyID     string
	SecretAccessKey string
	SessionToken    *string
}

func (h *harness) storedS3Credential(id string) storedS3Credential {
	h.t.Helper()
	var stored storedS3Credential
	h.db.Table("s3_credentials").
		Select("name, access_key_id, secret_access_key, session_token").
		Where("s3_credential_id = ?", id).Scan(&stored)
	return stored
}

// The secret access key and session token must never appear in a response, and
// creation must insist on a secret.
func TestS3CredentialSecretsAreNeverReturnedAndRequiredOnCreate(t *testing.T) {
	h := newHarness(t)

	for _, secret := range []any{nil, "", "   "} {
		rec := h.do(http.MethodPost, "/api/v1/s3-credentials", tokenAlice, map[string]any{
			"name": "c", "accessKeyId": "AKIAEXAMPLE", "secretAccessKey": secret,
		})
		if rec.Code != http.StatusBadRequest {
			t.Errorf("create with secret %q: status = %d, want 400", secret, rec.Code)
		}
	}

	created := h.mustDo(http.MethodPost, "/api/v1/s3-credentials", tokenAlice, map[string]any{
		"name": "c", "accessKeyId": "AKIAEXAMPLE", "secretAccessKey": "SUPER-SECRET",
		"sessionToken": "SESSION-TOKEN",
	}, http.StatusCreated)
	if created["accessKeyId"] != "AKIAEXAMPLE" {
		t.Errorf("accessKeyId = %v, want it returned", created["accessKeyId"])
	}
	if created["ownerId"] != "alice" {
		t.Errorf("ownerId = %v, want it taken from the token", created["ownerId"])
	}

	id := created["s3CredentialId"].(string)
	for _, path := range []string{"/api/v1/s3-credentials", "/api/v1/s3-credentials/" + id} {
		body := h.do(http.MethodGet, path, tokenAlice, nil).Body.String()
		if strings.Contains(body, "SUPER-SECRET") || strings.Contains(body, "SESSION-TOKEN") {
			t.Errorf("GET %s leaked a secret: %s", path, body)
		}
	}
	for _, field := range []string{"secretAccessKey", "sessionToken"} {
		if _, ok := created[field]; ok {
			t.Errorf("create response has a %q field", field)
		}
	}
}

// A client round-tripping a response has no secret to send back, so a blank one must
// mean "leave it alone" rather than "erase it".
func TestUpdatingS3CredentialWithoutSecretsPreservesThem(t *testing.T) {
	h := newHarness(t)
	created := h.mustDo(http.MethodPost, "/api/v1/s3-credentials", tokenAlice, map[string]any{
		"name": "c", "accessKeyId": "AKIAEXAMPLE", "secretAccessKey": "ORIGINAL",
		"sessionToken": "ORIGINAL-TOKEN",
	}, http.StatusCreated)
	id := created["s3CredentialId"].(string)

	h.mustDo(http.MethodPut, "/api/v1/s3-credentials/"+id, tokenAlice, map[string]any{
		"name": "renamed", "accessKeyId": "AKIAEXAMPLE", "secretAccessKey": "",
	}, http.StatusOK)

	stored := h.storedS3Credential(id)
	if stored.SecretAccessKey != "ORIGINAL" {
		t.Errorf("secret = %q, want it preserved across an update that omitted it", stored.SecretAccessKey)
	}
	if stored.SessionToken == nil || *stored.SessionToken != "ORIGINAL-TOKEN" {
		t.Errorf("session token = %v, want it preserved", stored.SessionToken)
	}
	if stored.Name != "renamed" {
		t.Errorf("name = %q, want the update applied", stored.Name)
	}
}

// The access key ID, secret and session token are one set, so an update cannot leave
// a mismatched pair behind.
func TestS3CredentialKeyRotationKeepsTheSetConsistent(t *testing.T) {
	h := newHarness(t)
	created := h.mustDo(http.MethodPost, "/api/v1/s3-credentials", tokenAlice, map[string]any{
		"name": "c", "accessKeyId": "OLD-ID", "secretAccessKey": "OLD-SECRET",
		"sessionToken": "OLD-TOKEN",
	}, http.StatusCreated)
	base := "/api/v1/s3-credentials/" + created["s3CredentialId"].(string)

	// A new ID with the old secret could never sign a request.
	if rec := h.do(http.MethodPut, base, tokenAlice, map[string]any{
		"name": "c", "accessKeyId": "NEW-ID",
	}); rec.Code != http.StatusBadRequest {
		t.Errorf("new access key ID without a secret: status = %d, want 400", rec.Code)
	}

	// A new secret without a token drops the old token: it belonged to the old secret.
	h.mustDo(http.MethodPut, base, tokenAlice, map[string]any{
		"name": "c", "accessKeyId": "NEW-ID", "secretAccessKey": "NEW-SECRET",
	}, http.StatusOK)
	stored := h.storedS3Credential(created["s3CredentialId"].(string))
	if stored.AccessKeyID != "NEW-ID" || stored.SecretAccessKey != "NEW-SECRET" {
		t.Errorf("stored = %+v, want the new ID and secret", stored)
	}
	if stored.SessionToken != nil {
		t.Errorf("session token = %q, want it cleared with the old secret", *stored.SessionToken)
	}

	// A token on its own may be refreshed without resending the secret.
	h.mustDo(http.MethodPut, base, tokenAlice, map[string]any{
		"name": "c", "accessKeyId": "NEW-ID", "sessionToken": "NEW-TOKEN",
	}, http.StatusOK)
	stored = h.storedS3Credential(created["s3CredentialId"].(string))
	if stored.SessionToken == nil || *stored.SessionToken != "NEW-TOKEN" {
		t.Errorf("session token = %v, want NEW-TOKEN", stored.SessionToken)
	}
	if stored.SecretAccessKey != "NEW-SECRET" {
		t.Errorf("secret = %q, want it kept", stored.SecretAccessKey)
	}
}

// A credential belongs to whoever registered it. There is no share, and platform admins
// get no standing on one either.
func TestS3CredentialIsPrivateToItsOwner(t *testing.T) {
	h := newHarness(t)
	id := h.seedS3Credential("alices", tokenAlice)
	base := "/api/v1/s3-credentials/" + id

	update := map[string]any{"name": "renamed", "accessKeyId": "AKIA-alices"}
	for _, token := range []string{tokenBob, tokenAdmin} {
		if rec := h.do(http.MethodGet, base, token, nil); rec.Code != http.StatusForbidden {
			t.Errorf("read by %s: status = %d, want 403", token, rec.Code)
		}
		if rec := h.do(http.MethodPut, base, token, update); rec.Code != http.StatusForbidden {
			t.Errorf("update by %s: status = %d, want 403", token, rec.Code)
		}
		if rec := h.do(http.MethodDelete, base, token, nil); rec.Code != http.StatusForbidden {
			t.Errorf("delete by %s: status = %d, want 403", token, rec.Code)
		}
	}
	h.mustDo(http.MethodPut, base, tokenAlice, update, http.StatusOK)

	if mine := h.list("/api/v1/s3-credentials", tokenAlice); len(mine) != 1 {
		t.Errorf("alice's credentials = %d, want 1", len(mine))
	}
	for _, token := range []string{tokenBob, tokenAdmin} {
		if theirs := h.list("/api/v1/s3-credentials", token); len(theirs) != 0 {
			t.Errorf("%s sees %d of alice's credentials, want 0", token, len(theirs))
		}
	}
	for _, rec := range []int{
		h.do(http.MethodGet, "/api/v1/s3-credentials", "", nil).Code,
		h.do(http.MethodGet, base, "", nil).Code,
		h.do(http.MethodPost, "/api/v1/s3-credentials", "", map[string]any{
			"name": "anon", "accessKeyId": "AKIA", "secretAccessKey": "s",
		}).Code,
	} {
		if rec != http.StatusUnauthorized {
			t.Errorf("anonymous request: status = %d, want 401", rec)
		}
	}

	h.mustDo(http.MethodDelete, base, tokenAlice, nil, http.StatusNoContent)
	if rec := h.do(http.MethodGet, base, tokenAlice, nil); rec.Code != http.StatusNotFound {
		t.Errorf("read after delete: status = %d, want 404", rec.Code)
	}
}

func TestS3CredentialValidation(t *testing.T) {
	h := newHarness(t)
	for name, body := range map[string]map[string]any{
		"blank name":          {"name": " ", "accessKeyId": "AKIA", "secretAccessKey": "s"},
		"blank access key ID": {"name": "c", "accessKeyId": "", "secretAccessKey": "s"},
	} {
		if rec := h.do(http.MethodPost, "/api/v1/s3-credentials", tokenAlice, body); rec.Code != http.StatusBadRequest {
			t.Errorf("%s: status = %d, want 400", name, rec.Code)
		}
	}
}

// Every change to a credential is audited like any other record, and the audit log
// names the columns that changed but never their values, so no secret reaches it.
func TestS3CredentialChangesAreAuditedWithoutSecrets(t *testing.T) {
	h := newHarness(t)
	created := h.mustDo(http.MethodPost, "/api/v1/s3-credentials", tokenAlice, map[string]any{
		"name": "c", "accessKeyId": "AKIA-AUDIT", "secretAccessKey": "AUDIT-SECRET",
		"sessionToken": "AUDIT-TOKEN",
	}, http.StatusCreated)
	id := created["s3CredentialId"].(string)
	h.mustDo(http.MethodPut, "/api/v1/s3-credentials/"+id, tokenAlice, map[string]any{
		"name": "c", "accessKeyId": "AKIA-AUDIT", "secretAccessKey": "ROTATED-SECRET",
	}, http.StatusOK)
	h.mustDo(http.MethodDelete, "/api/v1/s3-credentials/"+id, tokenAlice, nil, http.StatusNoContent)

	var actions []string
	for _, e := range h.auditEvents("s3_credentials") {
		if e.Actor != "alice" || e.EntityID != id {
			t.Errorf("unexpected event: %+v", e)
		}
		actions = append(actions, string(e.Action))
	}
	if got := strings.Join(actions, ","); got != "CREATE,UPDATE,DELETE" {
		t.Errorf("audited actions = %s, want CREATE,UPDATE,DELETE", got)
	}

	var all []auditmodel.Event
	h.db.Find(&all)
	for _, e := range all {
		row := fmt.Sprintf("%+v", e)
		for _, secret := range []string{"AUDIT-SECRET", "AUDIT-TOKEN", "ROTATED-SECRET"} {
			if strings.Contains(row, secret) {
				t.Errorf("audit event holds a secret: %s", row)
			}
		}
	}
}
