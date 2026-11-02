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
	"errors"
	"io/fs"
	"log/slog"
	"net/http"
	"os"
	"path"
	"strings"
)

// dashboardPrefix is where the dashboard is mounted. It must match basePath in
// dashboard/next.config.mjs, which every asset URL in the export is built against.
const dashboardPrefix = "/dashboard"

// registerDashboard serves the Next.js dashboard's static export from dir.
//
// The dashboard is a static export rather than a Node server of its own so it starts
// and stops with this process, on the same origin as the API: no second port, no CORS,
// and nothing to run beyond the binary. Every page is a client of the API below it.
func registerDashboard(mux *http.ServeMux, dir string) {
	if dir == "" {
		return
	}
	if _, err := os.Stat(path.Join(dir, "index.html")); err != nil {
		slog.Warn("dashboard not built; serving a placeholder at "+dashboardPrefix+"/",
			"dir", dir, "hint", "npm --prefix dashboard ci && npm --prefix dashboard run build")
		mux.HandleFunc("GET "+dashboardPrefix+"/", func(w http.ResponseWriter, _ *http.Request) {
			http.Error(w, "The Airavata dashboard has not been built. Run:\n\n"+
				"  npm --prefix dashboard ci && npm --prefix dashboard run build\n\n"+
				"then restart the server (or set AIRAVATA_DASHBOARD_DIR to an existing build).",
				http.StatusServiceUnavailable)
		})
		return
	}

	slog.Info("serving dashboard", "path", dashboardPrefix+"/", "dir", dir)
	mux.Handle("GET "+dashboardPrefix+"/", http.StripPrefix(dashboardPrefix, dashboardHandler(os.DirFS(dir))))
	mux.HandleFunc("GET /{$}", func(w http.ResponseWriter, r *http.Request) {
		http.Redirect(w, r, dashboardPrefix+"/", http.StatusFound)
	})
}

// dashboardHandler serves files from the export, answering a path the export has no
// file for with its 404 page rather than a directory listing or a bare "not found".
func dashboardHandler(root fs.FS) http.Handler {
	files := http.FileServerFS(root)
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		name := strings.TrimPrefix(path.Clean(r.URL.Path), "/")
		if name == "" {
			name = "."
		}
		info, err := fs.Stat(root, name)
		if err == nil && info.IsDir() {
			_, err = fs.Stat(root, path.Join(name, "index.html"))
		}
		if errors.Is(err, fs.ErrNotExist) {
			notFound(w, root)
			return
		}

		// Next fingerprints everything under _next/static, so it never changes in place.
		if strings.HasPrefix(name, "_next/static/") {
			w.Header().Set("Cache-Control", "public, max-age=31536000, immutable")
		}
		files.ServeHTTP(w, r)
	})
}

func notFound(w http.ResponseWriter, root fs.FS) {
	page, err := fs.ReadFile(root, "404.html")
	if err != nil {
		http.NotFound(w, nil)
		return
	}
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.WriteHeader(http.StatusNotFound)
	_, _ = w.Write(page)
}
