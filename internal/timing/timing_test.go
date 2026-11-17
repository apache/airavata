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
	"bytes"
	"context"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

type parent struct {
	ID       uint
	Children []child
}

type child struct {
	ID       uint
	ParentID uint
}

func openDB(t *testing.T) *gorm.DB {
	t.Helper()
	gdb, err := gorm.Open(sqlite.Open("file::memory:"), &gorm.Config{Logger: logger.Default.LogMode(logger.Silent)})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	if err := gdb.AutoMigrate(&parent{}, &child{}); err != nil {
		t.Fatalf("migrate: %v", err)
	}
	if err := RegisterGorm(gdb); err != nil {
		t.Fatalf("register: %v", err)
	}
	return gdb
}

func TestGormCountsEachStatementIncludingPreloads(t *testing.T) {
	gdb := openDB(t)
	if err := gdb.Create(&parent{ID: 1, Children: []child{{ID: 1}, {ID: 2}}}).Error; err != nil {
		t.Fatalf("seed: %v", err)
	}

	rec := New()
	ctx := WithRecorder(context.Background(), rec)

	stop := Start(ctx, "find")
	var p parent
	if err := gdb.WithContext(ctx).Preload("Children").First(&p, 1).Error; err != nil {
		t.Fatalf("find: %v", err)
	}
	stop()

	// The parent and its preload are separate round trips.
	if rec.dbCount != 2 {
		t.Fatalf("db queries = %d, want 2", rec.dbCount)
	}
	if s := rec.spans[rec.index["find"]]; s.dbCount != 2 || s.calls != 1 {
		t.Fatalf("span = %+v, want 2 queries in 1 call", s)
	}
}

func TestGormIgnoresStatementsWithoutRecorder(t *testing.T) {
	gdb := openDB(t)
	var ps []parent
	if err := gdb.Find(&ps).Error; err != nil {
		t.Fatalf("find: %v", err)
	}
}

func TestStartAccumulatesRepeatedSpans(t *testing.T) {
	rec := New()
	ctx := WithRecorder(context.Background(), rec)
	Start(ctx, "a")()
	Start(ctx, "a")()
	if got := rec.spans[rec.index["a"]].calls; got != 2 {
		t.Fatalf("calls = %d, want 2", got)
	}
	// No recorder: a no-op, not a panic.
	Start(context.Background(), "a")()
	Note(context.Background(), "k", "v")
}

func TestMiddlewareLogsAPIRequests(t *testing.T) {
	var buf bytes.Buffer
	prev := slog.Default()
	slog.SetDefault(slog.New(slog.NewTextHandler(&buf, nil)))
	t.Cleanup(func() { slog.SetDefault(prev) })

	h := Middleware(nil)(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		if strings.HasPrefix(r.URL.Path, "/api/") && FromContext(r.Context()) == nil {
			t.Error("no recorder on an /api/ request")
		}
		Start(r.Context(), "auth.introspect")()
		Note(r.Context(), "auth.claims_cache", "hit")
		w.WriteHeader(http.StatusTeapot)
	}))

	h.ServeHTTP(httptest.NewRecorder(), httptest.NewRequest(http.MethodGet, "/api/v1/things", nil))
	line := buf.String()
	for _, want := range []string{"request timing", "path=/api/v1/things", "status=418", "auth.introspect.ms=", "auth.claims_cache=hit", "total_ms="} {
		if !strings.Contains(line, want) {
			t.Errorf("log line %q lacks %q", line, want)
		}
	}

	buf.Reset()
	h.ServeHTTP(httptest.NewRecorder(), httptest.NewRequest(http.MethodGet, "/health", nil))
	if strings.Contains(buf.String(), "request timing") {
		t.Errorf("non-API request was logged: %q", buf.String())
	}
}
