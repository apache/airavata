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
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"mime/multipart"
	"net/http"
	"net/http/httptest"
	"os"
	"strings"
	"testing"

	datactl "github.com/apache/airavata/api/data/controller"
	datasvc "github.com/apache/airavata/api/data/service"
	"github.com/apache/airavata/internal/app"
)

// fakeTransport stands in for SSH. It records what it was asked to move and serves
// content from memory, so the rules above it — who may move a product's bytes, and
// which products can be moved at all — are testable without a host to log into.
type fakeTransport struct {
	// content is the remote filesystem, keyed by path.
	content map[string]string

	// downloads, uploads and dirs record what reached the transport, which is how a
	// test asserts a refusal happened before any connection was attempted.
	downloads []string
	uploads   []string
	dirs      []string

	endpoints []datasvc.Endpoint

	// err, when set, fails every operation, standing for an unreachable host.
	err error
}

func newFakeTransport() *fakeTransport {
	return &fakeTransport{content: map[string]string{}}
}

func (f *fakeTransport) Download(_ context.Context, ep datasvc.Endpoint, remotePath, localPath string) error {
	f.downloads = append(f.downloads, remotePath)
	f.endpoints = append(f.endpoints, ep)
	if f.err != nil {
		return f.err
	}
	body, ok := f.content[remotePath]
	if !ok {
		return errors.New("no such file: " + remotePath)
	}
	return os.WriteFile(localPath, []byte(body), 0o600)
}

func (f *fakeTransport) Upload(_ context.Context, ep datasvc.Endpoint, localPath, remotePath string) error {
	f.uploads = append(f.uploads, remotePath)
	f.endpoints = append(f.endpoints, ep)
	if f.err != nil {
		return f.err
	}
	body, err := os.ReadFile(localPath)
	if err != nil {
		return err
	}
	f.content[remotePath] = string(body)
	return nil
}

func (f *fakeTransport) MakeDirectory(_ context.Context, _ datasvc.Endpoint, remoteDir string) error {
	f.dirs = append(f.dirs, remoteDir)
	return f.err
}

// touched reports whether the transport was asked to do anything at all.
func (f *fakeTransport) touched() bool {
	return len(f.downloads)+len(f.uploads)+len(f.dirs) > 0
}

// withFakeTransport replaces the transfer service with one moving bytes through a
// fake, and rebuilds the routes over it.
func (h *harness) withFakeTransport() *fakeTransport {
	h.t.Helper()
	fake := newFakeTransport()
	repos := app.NewRepositories(h.db)
	h.svcs.DataTransfer = datasvc.NewDataTransferService(
		h.db, repos.DataProducts, repos.Shares, repos.SCPDataStorages, repos.GroupMembers, fake)
	h.rebuild()
	return fake
}

// upload issues a raw-body PUT, which is the shape `curl --data-binary` sends.
func (h *harness) upload(productID, token, body string) *httptest.ResponseRecorder {
	h.t.Helper()
	req := httptest.NewRequest(http.MethodPut,
		"/api/v1/data-products/"+productID+"/content", bytes.NewReader([]byte(body)))
	req.Header.Set("Content-Type", "application/octet-stream")
	if token != "" {
		req.Header.Set("Authorization", "Bearer "+token)
	}
	rec := httptest.NewRecorder()
	h.srv.ServeHTTP(rec, req)
	return rec
}

// download issues a GET for a product's content.
func (h *harness) download(productID, token string) *httptest.ResponseRecorder {
	h.t.Helper()
	req := httptest.NewRequest(http.MethodGet, "/api/v1/data-products/"+productID+"/content", nil)
	if token != "" {
		req.Header.Set("Authorization", "Bearer "+token)
	}
	rec := httptest.NewRecorder()
	h.srv.ServeHTTP(rec, req)
	return rec
}

// shareProductWithUser grants one user access to a product, as its owner, and returns
// the share's id so a test can widen it.
func (h *harness) shareProductWithUser(productID, userID, permission, ownerToken string) string {
	h.t.Helper()
	out := h.mustDo(http.MethodPost, "/api/v1/data-products/"+productID+"/shares", ownerToken,
		map[string]any{"principalType": "USER", "principalId": userID, "permission": permission},
		http.StatusCreated)
	return out["resourceSharingId"].(string)
}

// widenShare raises an existing share to permission.
func (h *harness) widenShare(productID, shareID, permission, ownerToken string) {
	h.t.Helper()
	h.mustDo(http.MethodPut, "/api/v1/data-products/"+productID+"/shares/"+shareID, ownerToken,
		map[string]any{"permission": permission}, http.StatusOK)
}

// Content goes out and comes back at the path the product names, and arriving moves
// the product to PROVISIONED — the dataset it was registered for is now where it says.
func TestDataProductContentRoundTrip(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()

	_, productID := h.seedProduct("reads", tokenAlice)

	out := h.mustDo(http.MethodGet, "/api/v1/data-products/"+productID, tokenAlice, nil, http.StatusOK)
	if out["provisionStatus"] != "REGISTERD" {
		t.Fatalf("new product status = %v, want REGISTERD", out["provisionStatus"])
	}

	rec := h.upload(productID, tokenAlice, "ACGT")
	if rec.Code != http.StatusOK {
		t.Fatalf("upload: status = %d, want 200\nbody: %s", rec.Code, rec.Body.String())
	}
	var uploaded map[string]any
	if err := json.Unmarshal(rec.Body.Bytes(), &uploaded); err != nil {
		t.Fatalf("decode upload response: %v", err)
	}
	if uploaded["provisionStatus"] != "PROVISIONED" {
		t.Errorf("status after upload = %v, want PROVISIONED", uploaded["provisionStatus"])
	}

	// The bytes went to the path the product names, under the storage's host and
	// account rather than anything the caller supplied.
	if got := fake.content["/scratch/reads"]; got != "ACGT" {
		t.Errorf("remote content = %q, want ACGT", got)
	}
	if len(fake.endpoints) == 0 || fake.endpoints[0].Host != "reads.example.edu" ||
		fake.endpoints[0].LoginUser != "runner" {
		t.Errorf("transfer endpoint = %+v, want the storage's host and login user", fake.endpoints)
	}
	// Port 22 is supplied when the storage names none, since a transfer needs one.
	if fake.endpoints[0].Port != 22 {
		t.Errorf("port = %d, want 22", fake.endpoints[0].Port)
	}
	// The parent directory is made first: a product may name a path no run has created.
	if len(fake.dirs) != 1 || fake.dirs[0] != "/scratch" {
		t.Errorf("directories created = %v, want [/scratch]", fake.dirs)
	}

	rec = h.download(productID, tokenAlice)
	if rec.Code != http.StatusOK {
		t.Fatalf("download: status = %d, want 200\nbody: %s", rec.Code, rec.Body.String())
	}
	if got := rec.Body.String(); got != "ACGT" {
		t.Errorf("downloaded %q, want ACGT", got)
	}
	if got := rec.Header().Get("Content-Length"); got != "4" {
		t.Errorf("Content-Length = %q, want 4", got)
	}
	if got := rec.Header().Get("Content-Disposition"); got != `attachment; filename=reads` {
		t.Errorf("Content-Disposition = %q, want the remote basename", got)
	}
}

// A browser sends a multipart form rather than a raw body, and the content is taken
// from the part named "file".
func TestDataProductUploadAcceptsMultipart(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()

	_, productID := h.seedProduct("reads", tokenAlice)

	var body bytes.Buffer
	form := multipart.NewWriter(&body)
	part, err := form.CreateFormFile("file", "reads.fasta")
	if err != nil {
		t.Fatalf("create form file: %v", err)
	}
	if _, err := part.Write([]byte("GATTACA")); err != nil {
		t.Fatalf("write form file: %v", err)
	}
	if err := form.Close(); err != nil {
		t.Fatalf("close form: %v", err)
	}

	req := httptest.NewRequest(http.MethodPut, "/api/v1/data-products/"+productID+"/content", &body)
	req.Header.Set("Content-Type", form.FormDataContentType())
	req.Header.Set("Authorization", "Bearer "+tokenAlice)
	rec := httptest.NewRecorder()
	h.srv.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("multipart upload: status = %d, want 200\nbody: %s", rec.Code, rec.Body.String())
	}
	// The remote name comes from the product, not from the part's filename: a product
	// names where its data lives, and an upload does not get to move it.
	if got := fake.content["/scratch/reads"]; got != "GATTACA" {
		t.Errorf("remote content = %q, want GATTACA", got)
	}
}

// Reading a product's bytes takes READ and writing them takes WRITE, which is the same
// standing the record itself is read and edited under.
func TestDataProductContentRespectsShares(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()

	_, productID := h.seedProduct("reads", tokenAlice)
	fake.content["/scratch/reads"] = "ACGT"

	// A stranger reaches neither, and nothing is attempted against the host.
	if rec := h.download(productID, tokenBob); rec.Code != http.StatusForbidden {
		t.Errorf("stranger download: status = %d, want 403", rec.Code)
	}
	if rec := h.upload(productID, tokenBob, "nope"); rec.Code != http.StatusForbidden {
		t.Errorf("stranger upload: status = %d, want 403", rec.Code)
	}
	if fake.touched() {
		t.Errorf("a refused request still reached the host: %+v", fake)
	}

	// READ opens the download and leaves the upload shut.
	shareID := h.shareProductWithUser(productID, "bob", "READ", tokenAlice)
	if rec := h.download(productID, tokenBob); rec.Code != http.StatusOK {
		t.Errorf("READ download: status = %d, want 200", rec.Code)
	}
	if rec := h.upload(productID, tokenBob, "nope"); rec.Code != http.StatusForbidden {
		t.Errorf("READ upload: status = %d, want 403", rec.Code)
	}
	if got := fake.content["/scratch/reads"]; got != "ACGT" {
		t.Errorf("a refused upload changed the remote file: %q", got)
	}

	// WRITE opens both, since WRITE implies READ.
	h.widenShare(productID, shareID, "WRITE", tokenAlice)
	if rec := h.upload(productID, tokenBob, "TTTT"); rec.Code != http.StatusOK {
		t.Errorf("WRITE upload: status = %d, want 200", rec.Code)
	}
	if got := fake.content["/scratch/reads"]; got != "TTTT" {
		t.Errorf("remote content = %q, want TTTT", got)
	}
}

// A share on the product is enough on its own. The storage it lives on is never shared
// here, and it is the storage that holds the credential: requiring a share on that too
// would make sharing a product useless, and granting one would hand over every dataset
// on the host rather than the one meant.
func TestDataProductContentNeedsNoStorageShare(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()

	storageID, productID := h.seedProduct("reads", tokenAlice)
	fake.content["/scratch/reads"] = "ACGT"
	h.shareProductWithUser(productID, "bob", "READ", tokenAlice)

	// Bob cannot so much as read the storage record.
	h.mustDo(http.MethodGet, "/api/v1/scp-data-storages/"+storageID, tokenBob, nil, http.StatusForbidden)

	if rec := h.download(productID, tokenBob); rec.Code != http.StatusOK {
		t.Fatalf("download under a product share: status = %d, want 200\nbody: %s", rec.Code, rec.Body.String())
	}
}

// An anonymous caller reaches nothing: these routes carry data, and none of the data
// vertical is readable without a token.
func TestDataProductContentRefusesAnonymous(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()

	_, productID := h.seedProduct("reads", tokenAlice)

	if rec := h.download(productID, ""); rec.Code != http.StatusUnauthorized {
		t.Errorf("anonymous download: status = %d, want 401", rec.Code)
	}
	if rec := h.upload(productID, "", "nope"); rec.Code != http.StatusUnauthorized {
		t.Errorf("anonymous upload: status = %d, want 401", rec.Code)
	}
	if fake.touched() {
		t.Errorf("an anonymous request reached the host: %+v", fake)
	}
}

// A product that names no id at all is a 404, and the body of a write to one is never
// read: the caller is told before they are asked for bytes.
func TestDataProductContentUnknownProduct(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()

	if rec := h.download("no-such-product", tokenAlice); rec.Code != http.StatusNotFound {
		t.Errorf("download of a missing product: status = %d, want 404", rec.Code)
	}
	if rec := h.upload("no-such-product", tokenAlice, "body"); rec.Code != http.StatusNotFound {
		t.Errorf("upload to a missing product: status = %d, want 404", rec.Code)
	}
	if fake.touched() {
		t.Errorf("a request for a missing product reached the host: %+v", fake)
	}
}

// A directory product moves as a tree, which single-file SCP does not do. Refusing it
// beats transferring the one file a recursive copy would have started with.
func TestDataProductContentRefusesDirectoryProducts(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()

	_, storageID := h.seedStorage("outputs", tokenAlice)
	out := h.mustDo(http.MethodPost, "/api/v1/data-products", tokenAlice, map[string]any{
		"dataName": "outputs", "isFile": false, "path": "/scratch/outputs", "dataStorageId": storageID,
	}, http.StatusCreated)
	productID := out["dataId"].(string)

	if rec := h.download(productID, tokenAlice); rec.Code != http.StatusBadRequest {
		t.Errorf("download of a directory product: status = %d, want 400", rec.Code)
	}
	if rec := h.upload(productID, tokenAlice, "body"); rec.Code != http.StatusBadRequest {
		t.Errorf("upload to a directory product: status = %d, want 400", rec.Code)
	}
	if fake.touched() {
		t.Errorf("a directory product reached the host: %+v", fake)
	}
}

// A host that cannot be reached is the upstream's failure, not the request's, and the
// product's status is left where it was rather than recorded as provisioned.
func TestDataProductContentReportsUnreachableHost(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()
	fake.err = errors.New("connection refused")

	_, productID := h.seedProduct("reads", tokenAlice)

	if rec := h.download(productID, tokenAlice); rec.Code != http.StatusBadGateway {
		t.Errorf("download from an unreachable host: status = %d, want 502", rec.Code)
	}
	if rec := h.upload(productID, tokenAlice, "ACGT"); rec.Code != http.StatusBadGateway {
		t.Errorf("upload to an unreachable host: status = %d, want 502", rec.Code)
	}

	out := h.mustDo(http.MethodGet, "/api/v1/data-products/"+productID, tokenAlice, nil, http.StatusOK)
	if out["provisionStatus"] != "REGISTERD" {
		t.Errorf("status after a failed upload = %v, want REGISTERD", out["provisionStatus"])
	}
}

// The staging file a download is served from is removed once the response is written:
// a server that kept one copy per request would fill its disk with other people's data.
func TestDataProductDownloadCleansUpStagingFiles(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()

	_, productID := h.seedProduct("reads", tokenAlice)
	fake.content["/scratch/reads"] = "ACGT"

	before := stagingFileCount(t)
	for range 3 {
		if rec := h.download(productID, tokenAlice); rec.Code != http.StatusOK {
			t.Fatalf("download: status = %d, want 200", rec.Code)
		}
	}
	if after := stagingFileCount(t); after != before {
		t.Errorf("staging entries went from %d to %d; downloads left copies behind", before, after)
	}
}

// stagingFileCount counts the temporary directories a download stages content in.
func stagingFileCount(t *testing.T) int {
	t.Helper()
	entries, err := os.ReadDir(os.TempDir())
	if err != nil {
		t.Fatalf("read temp dir: %v", err)
	}
	n := 0
	for _, e := range entries {
		if strings.HasPrefix(e.Name(), "airavata-download-") {
			n++
		}
	}
	return n
}

// An upload past the cap is reported as too large rather than as malformed, and
// nothing reaches the host: the body is refused while it is still being staged.
func TestDataProductUploadRefusesOversizedBody(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()

	original := datactl.MaxUploadBytes
	datactl.MaxUploadBytes = 8
	t.Cleanup(func() { datactl.MaxUploadBytes = original })

	_, productID := h.seedProduct("reads", tokenAlice)

	if rec := h.upload(productID, tokenAlice, "ACGTACGTACGT"); rec.Code != http.StatusRequestEntityTooLarge {
		t.Errorf("oversized upload: status = %d, want 413\nbody: %s", rec.Code, rec.Body.String())
	}
	if len(fake.uploads) != 0 {
		t.Errorf("an oversized upload still reached the host: %v", fake.uploads)
	}

	// A body inside the cap is unaffected.
	if rec := h.upload(productID, tokenAlice, "ACGT"); rec.Code != http.StatusOK {
		t.Errorf("upload within the cap: status = %d, want 200\nbody: %s", rec.Code, rec.Body.String())
	}
}

// A multipart body carrying no part named "file" has no content to write, and says so
// rather than writing an empty file over the dataset.
func TestDataProductUploadRejectsMultipartWithoutFilePart(t *testing.T) {
	h := newHarness(t)
	fake := h.withFakeTransport()

	_, productID := h.seedProduct("reads", tokenAlice)
	fake.content["/scratch/reads"] = "ACGT"

	var body bytes.Buffer
	form := multipart.NewWriter(&body)
	if err := form.WriteField("notes", "wrong field name"); err != nil {
		t.Fatalf("write field: %v", err)
	}
	if err := form.Close(); err != nil {
		t.Fatalf("close form: %v", err)
	}

	req := httptest.NewRequest(http.MethodPut, "/api/v1/data-products/"+productID+"/content", &body)
	req.Header.Set("Content-Type", form.FormDataContentType())
	req.Header.Set("Authorization", "Bearer "+tokenAlice)
	rec := httptest.NewRecorder()
	h.srv.ServeHTTP(rec, req)

	if rec.Code != http.StatusBadRequest {
		t.Errorf("multipart without a file part: status = %d, want 400\nbody: %s", rec.Code, rec.Body.String())
	}
	if got := fake.content["/scratch/reads"]; got != "ACGT" {
		t.Errorf("remote content = %q, want it untouched", got)
	}
}
