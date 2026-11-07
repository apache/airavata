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

package audit

import (
	"context"
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"io"
	"log/slog"
)

// Logger writes one JSON line per write-tool call. Arguments are hashed, never logged.
type Logger struct {
	log *slog.Logger
}

func New(w io.Writer) *Logger {
	return &Logger{log: slog.New(slog.NewJSONHandler(w, nil))}
}

// Event is one audited call.
type Event struct {
	Principal    string
	Tool         string
	Arguments    json.RawMessage
	Confirmation string // "accepted", "declined", "cancelled", "skipped", "requested", "not_required"
	Result       string // "ok" or "error"
	ResourceIDs  []string
	Err          error
}

func (l *Logger) Record(ctx context.Context, e Event) {
	if l == nil {
		return
	}
	sum := sha256.Sum256(e.Arguments)
	attrs := []any{
		"principal", e.Principal,
		"tool", e.Tool,
		"arguments_sha256", hex.EncodeToString(sum[:]),
		"confirmation", e.Confirmation,
		"result", e.Result,
	}
	if len(e.ResourceIDs) > 0 {
		attrs = append(attrs, "resource_ids", e.ResourceIDs)
	}
	if e.Err != nil {
		attrs = append(attrs, "error", e.Err.Error())
	}
	l.log.InfoContext(ctx, "airavata-mcp audit", attrs...)
}
