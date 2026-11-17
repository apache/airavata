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

package timing

import (
	"bufio"
	"context"
	"database/sql"
	"errors"
	"log/slog"
	"net"
	"net/http"
	"strings"
)

// Middleware puts a Recorder on every /api/ request and logs one "request timing" line
// when the request finishes: method, path, status, total duration, database totals and
// every span the request recorded. Other paths (the dashboard's static files, /health)
// pass through untouched.
//
// pool, when non-nil, adds the connection pool's state at the end of the request, which
// tells a request that waited for a connection, or had to dial a new one, from one that
// did not.
func Middleware(pool func() sql.DBStats) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			if !strings.HasPrefix(r.URL.Path, "/api/") {
				next.ServeHTTP(w, r)
				return
			}

			rec := New()
			sw := &statusWriter{ResponseWriter: w, status: http.StatusOK}
			next.ServeHTTP(sw, r.WithContext(WithRecorder(r.Context(), rec)))

			attrs := append([]slog.Attr{
				slog.String("method", r.Method),
				slog.String("path", r.URL.Path),
				slog.Int("status", sw.status),
			}, rec.attrs()...)
			if pool != nil {
				s := pool()
				attrs = append(attrs, slog.Group("pool",
					slog.Int("open", s.OpenConnections),
					slog.Int("in_use", s.InUse),
					slog.Int("idle", s.Idle),
					slog.Int64("wait_count", s.WaitCount),
					slog.Float64("wait_ms", ms(s.WaitDuration)),
					slog.Int64("max_idle_closed", s.MaxIdleClosed),
				))
			}
			slog.LogAttrs(context.Background(), slog.LevelInfo, "request timing", attrs...)
		})
	}
}

// statusWriter remembers the status code written. It passes Hijack and Unwrap through
// so websocket upgrades and http.ResponseController keep working behind it.
type statusWriter struct {
	http.ResponseWriter
	status      int
	wroteHeader bool
}

func (w *statusWriter) WriteHeader(code int) {
	if !w.wroteHeader {
		w.status = code
		w.wroteHeader = true
	}
	w.ResponseWriter.WriteHeader(code)
}

func (w *statusWriter) Write(b []byte) (int, error) {
	w.wroteHeader = true
	return w.ResponseWriter.Write(b)
}

func (w *statusWriter) Flush() {
	if f, ok := w.ResponseWriter.(http.Flusher); ok {
		f.Flush()
	}
}

func (w *statusWriter) Hijack() (net.Conn, *bufio.ReadWriter, error) {
	h, ok := w.ResponseWriter.(http.Hijacker)
	if !ok {
		return nil, nil, errors.New("response writer does not support hijacking")
	}
	w.status = http.StatusSwitchingProtocols
	return h.Hijack()
}

func (w *statusWriter) Unwrap() http.ResponseWriter { return w.ResponseWriter }
