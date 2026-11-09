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

	"github.com/apache/airavata/internal/server"
)

func TestDocsServesSwaggerUI(t *testing.T) {
	h := newHarness(t)

	// Redirect from /docs to /docs/
	rec := h.do(http.MethodGet, "/docs", "", nil)
	if rec.Code != http.StatusFound && rec.Code != http.StatusMovedPermanently && rec.Code != http.StatusTemporaryRedirect {
		t.Fatalf("GET /docs = %d, want redirect", rec.Code)
	}
	if loc := rec.Header().Get("Location"); loc != "/docs/" {
		t.Errorf("GET /docs Location = %q, want /docs/", loc)
	}

	// GET /docs/ serves index.html with strict security headers
	rec = h.do(http.MethodGet, "/docs/", "", nil)
	if rec.Code != http.StatusOK {
		t.Fatalf("GET /docs/ = %d, want 200", rec.Code)
	}
	body := rec.Body.String()
	if !strings.Contains(body, "id=\"swagger-ui\"") {
		t.Errorf("GET /docs/ body missing #swagger-ui")
	}
	if !strings.Contains(body, "swagger-initializer.js") {
		t.Errorf("GET /docs/ body missing swagger-initializer.js")
	}

	// Security headers for HTML
	for header, wantSubstr := range map[string]string{
		"Content-Security-Policy": "default-src 'self'",
		"X-Frame-Options":         "DENY",
		"X-Content-Type-Options":  "nosniff",
		"Referrer-Policy":         "strict-origin-when-cross-origin",
		"Permissions-Policy":      "camera=()",
		"Cache-Control":           "no-cache",
	} {
		got := rec.Header().Get(header)
		if !strings.Contains(got, wantSubstr) {
			t.Errorf("header %s = %q, want containing %q", header, got, wantSubstr)
		}
	}
	if strings.Contains(rec.Header().Get("Content-Security-Policy"), "'unsafe-inline'") &&
		strings.Contains(rec.Header().Get("Content-Security-Policy"), "script-src 'self' 'unsafe-inline'") {
		t.Errorf("CSP script-src should not contain 'unsafe-inline'")
	}
}

func TestDocsServesOpenAPISpec(t *testing.T) {
	h := newHarness(t)

	rec := h.do(http.MethodGet, "/docs/openapi.yaml", "", nil)
	if rec.Code != http.StatusOK {
		t.Fatalf("GET /docs/openapi.yaml = %d, want 200", rec.Code)
	}
	if ct := rec.Header().Get("Content-Type"); !strings.Contains(ct, "yaml") {
		t.Errorf("Content-Type = %q, want yaml", ct)
	}
	if xcto := rec.Header().Get("X-Content-Type-Options"); xcto != "nosniff" {
		t.Errorf("X-Content-Type-Options = %q, want nosniff", xcto)
	}
	if xfo := rec.Header().Get("X-Frame-Options"); xfo != "DENY" {
		t.Errorf("X-Frame-Options = %q, want DENY", xfo)
	}
	if cc := rec.Header().Get("Cache-Control"); cc != "no-cache" {
		t.Errorf("Cache-Control = %q, want no-cache", cc)
	}

	body := rec.Body.String()
	if !strings.Contains(body, "openapi: 3.1.0") {
		t.Errorf("OpenAPI spec missing version header")
	}
	if !strings.Contains(body, "Apache Airavata HTTP API") {
		t.Errorf("OpenAPI spec missing API title")
	}
}

func TestDocsHardenedInitializer(t *testing.T) {
	h := newHarness(t)

	rec := h.do(http.MethodGet, "/docs/swagger-initializer.js", "", nil)
	if rec.Code != http.StatusOK {
		t.Fatalf("GET /docs/swagger-initializer.js = %d, want 200", rec.Code)
	}
	body := rec.Body.String()

	// Verify security hardening options
	for _, expected := range []string{
		"queryConfigEnabled: false",
		"validatorUrl: null",
		"layout: \"BaseLayout\"",
		"url: \"/docs/openapi.yaml\"",
		"persistAuthorization: true",
		"requestInterceptor:",
	} {
		if !strings.Contains(body, expected) {
			t.Errorf("swagger-initializer.js missing %q", expected)
		}
	}
}

func TestDocsStaticAssets(t *testing.T) {
	h := newHarness(t)

	for _, path := range []string{
		"/docs/swagger-ui.css",
		"/docs/swagger-ui-bundle.js",
		"/docs/swagger-ui-standalone-preset.js",
		"/docs/favicon-32x32.png",
	} {
		rec := h.do(http.MethodGet, path, "", nil)
		if rec.Code != http.StatusOK {
			t.Errorf("GET %s = %d, want 200", path, rec.Code)
		}
		if xcto := rec.Header().Get("X-Content-Type-Options"); xcto != "nosniff" {
			t.Errorf("GET %s X-Content-Type-Options = %q, want nosniff", path, xcto)
		}
		if cc := rec.Header().Get("Cache-Control"); !strings.Contains(cc, "max-age") {
			t.Errorf("GET %s Cache-Control = %q, want max-age", path, cc)
		}
	}
}

func TestDocsNotFoundAndNoDirectoryListing(t *testing.T) {
	h := newHarness(t)

	// Non-existent file
	rec := h.do(http.MethodGet, "/docs/nonexistent.js", "", nil)
	if rec.Code != http.StatusNotFound {
		t.Errorf("GET /docs/nonexistent.js = %d, want 404", rec.Code)
	}

	// Non-existent directory
	rec = h.do(http.MethodGet, "/docs/sub/dir/", "", nil)
	if rec.Code != http.StatusNotFound {
		t.Errorf("GET /docs/sub/dir/ = %d, want 404", rec.Code)
	}
}

func TestDocsMethodsRestricted(t *testing.T) {
	h := newHarness(t)

	// POST /docs/ should not be allowed
	rec := h.do(http.MethodPost, "/docs/", "", nil)
	if rec.Code != http.StatusMethodNotAllowed {
		t.Errorf("POST /docs/ = %d, want 405", rec.Code)
	}

	// HEAD /docs/ should succeed
	rec = h.do(http.MethodHead, "/docs/", "", nil)
	if rec.Code != http.StatusOK {
		t.Errorf("HEAD /docs/ = %d, want 200", rec.Code)
	}
}

func TestDocsDisabled(t *testing.T) {
	h := newHarness(t)
	h.cfg.DocsEnabled = false
	h.srv = server.New(h.cfg, h.svcs, h.introspector)

	rec := h.do(http.MethodGet, "/docs/", "", nil)
	if rec.Code != http.StatusNotFound {
		t.Errorf("GET /docs/ with DocsEnabled=false = %d, want 404", rec.Code)
	}

	rec = h.do(http.MethodGet, "/docs/openapi.yaml", "", nil)
	if rec.Code != http.StatusNotFound {
		t.Errorf("GET /docs/openapi.yaml with DocsEnabled=false = %d, want 404", rec.Code)
	}
}
