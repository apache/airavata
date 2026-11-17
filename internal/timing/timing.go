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

// Package timing breaks one request's latency down into where it went: named spans
// (auth, service calls, individual repository calls) and the database time spent
// inside each.
//
// A Recorder rides on the request context. Code that wants a span calls
// Start(ctx, name) and invokes the returned stop function; code running without a
// recorder (workers, tests, the CLI) pays a context lookup and nothing else.
package timing

import (
	"context"
	"log/slog"
	"sync"
	"time"
)

type ctxKey struct{}

// Recorder collects the spans and database time of one request. It is safe for
// concurrent use, though spans recorded from concurrent goroutines attribute database
// time to whichever spans were open at the time.
type Recorder struct {
	start time.Time

	mu      sync.Mutex
	spans   []span
	index   map[string]int
	notes   []slog.Attr
	dbCount int
	dbTime  time.Duration
}

type span struct {
	name    string
	calls   int
	dur     time.Duration
	dbCount int
	dbTime  time.Duration
}

// New returns a recorder whose clock starts now.
func New() *Recorder {
	return &Recorder{start: time.Now(), index: make(map[string]int)}
}

// WithRecorder returns ctx carrying r.
func WithRecorder(ctx context.Context, r *Recorder) context.Context {
	return context.WithValue(ctx, ctxKey{}, r)
}

// FromContext returns the recorder on ctx, or nil.
func FromContext(ctx context.Context) *Recorder {
	if ctx == nil {
		return nil
	}
	r, _ := ctx.Value(ctxKey{}).(*Recorder)
	return r
}

// Start opens a span named name on ctx's recorder and returns the function that closes
// it. A span opened more than once under the same name accumulates: its duration and
// database time are summed and its call count reported. Without a recorder on ctx the
// returned function does nothing.
func Start(ctx context.Context, name string) func() {
	r := FromContext(ctx)
	if r == nil {
		return func() {}
	}
	r.mu.Lock()
	dbCount, dbTime := r.dbCount, r.dbTime
	r.mu.Unlock()
	began := time.Now()

	return func() {
		dur := time.Since(began)
		r.mu.Lock()
		defer r.mu.Unlock()
		i, ok := r.index[name]
		if !ok {
			i = len(r.spans)
			r.index[name] = i
			r.spans = append(r.spans, span{name: name})
		}
		s := &r.spans[i]
		s.calls++
		s.dur += dur
		s.dbCount += r.dbCount - dbCount
		s.dbTime += r.dbTime - dbTime
	}
}

// Note attaches a key/value to the request's timing log line, for facts that are not
// durations, such as whether a cache hit.
func Note(ctx context.Context, key string, value any) {
	r := FromContext(ctx)
	if r == nil {
		return
	}
	r.mu.Lock()
	r.notes = append(r.notes, slog.Any(key, value))
	r.mu.Unlock()
}

// AddDB records one database round trip of duration d.
func (r *Recorder) AddDB(d time.Duration) {
	r.mu.Lock()
	r.dbCount++
	r.dbTime += d
	r.mu.Unlock()
}

// attrs renders the recorder as log attributes: the total, the database totals, each
// span as a group, then the notes.
func (r *Recorder) attrs() []slog.Attr {
	total := time.Since(r.start)
	r.mu.Lock()
	defer r.mu.Unlock()

	out := []slog.Attr{
		slog.Float64("total_ms", ms(total)),
		slog.Int("db_queries", r.dbCount),
		slog.Float64("db_ms", ms(r.dbTime)),
	}
	for _, s := range r.spans {
		group := []any{slog.Float64("ms", ms(s.dur)), slog.Int("db_queries", s.dbCount), slog.Float64("db_ms", ms(s.dbTime))}
		if s.calls > 1 {
			group = append(group, slog.Int("calls", s.calls))
		}
		out = append(out, slog.Group(s.name, group...))
	}
	return append(out, r.notes...)
}

// ms reports d in milliseconds to two decimal places.
func ms(d time.Duration) float64 {
	return float64(d.Microseconds()/10) / 100
}
