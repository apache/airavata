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
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/apache/airavata/internal/server"
)

// newDashboardHarness serves a fake static export: an index, one nested page, a 404
// page and a fingerprinted asset.
func newDashboardHarness(t *testing.T, files map[string]string) *harness {
	t.Helper()
	h := newHarness(t)
	dir := t.TempDir()
	for name, body := range files {
		p := filepath.Join(dir, name)
		if err := os.MkdirAll(filepath.Dir(p), 0o755); err != nil {
			t.Fatal(err)
		}
		if err := os.WriteFile(p, []byte(body), 0o644); err != nil {
			t.Fatal(err)
		}
	}
	h.cfg.DashboardDir = dir
	h.srv = server.New(h.cfg, h.svcs, h.introspector)
	return h
}

func TestDashboardServesStaticExport(t *testing.T) {
	h := newDashboardHarness(t, map[string]string{
		"index.html":               "home",
		"processes/index.html":     "processes",
		"404.html":                 "missing",
		"_next/static/app-1a2b.js": "js",
	})

	for _, tc := range []struct {
		path       string
		wantStatus int
		wantBody   string
	}{
		{"/dashboard/", http.StatusOK, "home"},
		{"/dashboard/processes/", http.StatusOK, "processes"},
		{"/dashboard/_next/static/app-1a2b.js", http.StatusOK, "js"},
		{"/dashboard/no-such-page/", http.StatusNotFound, "missing"},
		// A directory with no page of its own is not listed.
		{"/dashboard/_next/", http.StatusNotFound, "missing"},
	} {
		rec := h.do(http.MethodGet, tc.path, "", nil)
		if rec.Code != tc.wantStatus || !strings.Contains(rec.Body.String(), tc.wantBody) {
			t.Errorf("GET %s = %d %q, want %d containing %q", tc.path, rec.Code, rec.Body.String(), tc.wantStatus, tc.wantBody)
		}
	}

	rec := h.do(http.MethodGet, "/dashboard/_next/static/app-1a2b.js", "", nil)
	if got := rec.Header().Get("Cache-Control"); !strings.Contains(got, "immutable") {
		t.Errorf("fingerprinted asset Cache-Control = %q, want immutable", got)
	}
}

func TestRootRedirectsToDashboard(t *testing.T) {
	h := newDashboardHarness(t, map[string]string{"index.html": "home"})

	// Without the trailing slash, the mux redirects into the dashboard.
	if rec := h.do(http.MethodGet, "/dashboard", "", nil); rec.Code != http.StatusMovedPermanently || rec.Header().Get("Location") != "/dashboard/" {
		t.Errorf("GET /dashboard = %d to %q, want 301 to /dashboard/", rec.Code, rec.Header().Get("Location"))
	}

	rec := h.do(http.MethodGet, "/", "", nil)
	if rec.Code != http.StatusFound || rec.Header().Get("Location") != "/dashboard/" {
		t.Errorf("GET / = %d to %q, want 302 to /dashboard/", rec.Code, rec.Header().Get("Location"))
	}
	// The API is untouched by the catch-all.
	if rec := h.do(http.MethodGet, "/health", "", nil); rec.Code != http.StatusOK {
		t.Errorf("GET /health = %d, want 200", rec.Code)
	}
}

func TestUnbuiltDashboardExplainsItself(t *testing.T) {
	h := newDashboardHarness(t, nil)

	rec := h.do(http.MethodGet, "/dashboard/", "", nil)
	if rec.Code != http.StatusServiceUnavailable || !strings.Contains(rec.Body.String(), "npm --prefix dashboard") {
		t.Errorf("GET /dashboard/ = %d %q, want 503 with build instructions", rec.Code, rec.Body.String())
	}
}
