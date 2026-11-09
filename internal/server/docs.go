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

package server

import (
	"embed"
	"io/fs"
	"log/slog"
	"net/http"
	"path"
	"strings"

	"github.com/apache/airavata/docs"
)

// docsPrefix is where the API documentation and Swagger UI are mounted.
const docsPrefix = "/docs"

//go:embed swaggerui/*
var swaggerUIFS embed.FS

const docsCSP = "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self' data:; connect-src 'self'; frame-ancestors 'none'; form-action 'self'; base-uri 'self'; object-src 'none'"

// registerDocs mounts Swagger UI and the OpenAPI spec at /docs.
func registerDocs(mux *http.ServeMux, enabled bool) {
	if !enabled {
		return
	}

	staticFS, err := fs.Sub(swaggerUIFS, "swaggerui")
	if err != nil {
		slog.Error("failed to load swagger ui assets", "error", err)
		return
	}

	mux.HandleFunc("GET "+docsPrefix, func(w http.ResponseWriter, r *http.Request) {
		http.Redirect(w, r, docsPrefix+"/", http.StatusFound)
	})
	mux.HandleFunc("GET "+docsPrefix+"/openapi.yaml", serveOpenAPISpec)
	mux.Handle("GET "+docsPrefix+"/", http.StripPrefix(docsPrefix, docsHandler(staticFS)))
}

func serveOpenAPISpec(w http.ResponseWriter, _ *http.Request) {
	w.Header().Set("Content-Type", "application/yaml; charset=utf-8")
	w.Header().Set("X-Content-Type-Options", "nosniff")
	w.Header().Set("X-Frame-Options", "DENY")
	w.Header().Set("Referrer-Policy", "strict-origin-when-cross-origin")
	w.Header().Set("Permissions-Policy", "camera=(), microphone=(), geolocation=()")
	w.Header().Set("Cache-Control", "no-cache")
	_, _ = w.Write(docs.OpenAPI)
}

func docsHandler(root fs.FS) http.Handler {
	files := http.FileServerFS(root)
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		name := strings.TrimPrefix(path.Clean(r.URL.Path), "/")
		isIndex := name == "" || name == "." || name == "index.html"

		w.Header().Set("X-Content-Type-Options", "nosniff")
		w.Header().Set("X-Frame-Options", "DENY")
		w.Header().Set("Referrer-Policy", "strict-origin-when-cross-origin")
		w.Header().Set("Permissions-Policy", "camera=(), microphone=(), geolocation=()")

		if isIndex {
			w.Header().Set("Content-Security-Policy", docsCSP)
			w.Header().Set("Cache-Control", "no-cache")
		} else {
			info, err := fs.Stat(root, name)
			if err != nil || info.IsDir() {
				http.NotFound(w, r)
				return
			}
			w.Header().Set("Cache-Control", "public, max-age=86400")
		}

		files.ServeHTTP(w, r)
	})
}
