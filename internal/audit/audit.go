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

// Package audit records every change made to a persistent record.
//
// It hooks the data layer rather than the HTTP layer, so a change is recorded whichever
// way it was made — an API call, the execution engine, a bootstrap step — and a request
// that changes nothing leaves nothing behind.
package audit

import (
	"fmt"
	"reflect"
	"sort"
	"strings"
	"time"
	"unicode/utf8"

	"gorm.io/gorm"
	"gorm.io/gorm/clause"

	"github.com/apache/airavata/api/audit/model"
	"github.com/apache/airavata/internal/auth"
)

const pluginName = "airavata:audit"

// Register makes gdb record an Event for every row its repositories create, update or
// delete. Call it once per connection, before the first write.
//
// The Event is inserted on the same connection as the change, so inside a transaction
// the two commit or roll back together: no change goes unrecorded, and none is recorded
// that did not happen. The corollary is that a failure to write the Event fails the
// change itself. An audit trail that can silently lose entries is not one.
//
// The caller is read from the context the repository was given. A repository call made
// without one — the execution engine's, for instance — is recorded as SystemActor.
//
// Two things are not seen. Rows removed by a foreign key's ON DELETE CASCADE are removed
// by the database, so only the record that was deleted is recorded. And a write made
// through raw SQL bypasses the model entirely.
func Register(gdb *gorm.DB) error {
	return gdb.Use(plugin{})
}

type plugin struct{}

func (plugin) Name() string { return pluginName }

func (plugin) Initialize(gdb *gorm.DB) error {
	cb := gdb.Callback()
	if err := cb.Create().After("gorm:create").Register(pluginName+":create", record(model.ActionCreate)); err != nil {
		return err
	}
	if err := cb.Update().After("gorm:update").Register(pluginName+":update", record(model.ActionUpdate)); err != nil {
		return err
	}
	return cb.Delete().After("gorm:delete").Register(pluginName+":delete", record(model.ActionDelete))
}

func record(action model.Action) func(*gorm.DB) {
	return func(tx *gorm.DB) {
		stmt := tx.Statement
		switch {
		case tx.Error != nil, tx.DryRun, stmt.Schema == nil:
			return
		case stmt.Schema.Table == (model.Event{}).TableName():
			// The log is not itself audited.
			return
		case tx.RowsAffected == 0:
			// A write that changed nothing — an update that matched no row, an
			// insert that lost to ON CONFLICT DO NOTHING — is not a change.
			return
		}

		act := action
		if _, ok := stmt.Clauses[clause.OnConflict{}.Name()]; ok && action == model.ActionCreate {
			act = model.ActionUpsert
		}

		actor := model.SystemActor
		if p := auth.FromContext(stmt.Context); p != nil && p.Name != "" {
			actor = truncate(p.Name, 255)
		}

		now := time.Now().UnixMilli()
		changed := changedFields(stmt)
		ids := entityIDs(tx)

		events := make([]model.Event, 0, len(ids))
		for _, id := range ids {
			events = append(events, model.Event{
				OccurredAt:    now,
				Actor:         actor,
				Action:        act,
				EntityType:    truncate(stmt.Schema.Table, 64),
				EntityID:      truncate(id, 255),
				ChangedFields: changed,
			})
		}
		if len(events) == 0 {
			return
		}

		// A fresh statement on the same connection and context, so the insert joins the
		// caller's transaction without inheriting its conditions.
		if err := tx.Session(&gorm.Session{NewDB: true}).Create(&events).Error; err != nil {
			_ = tx.AddError(err)
		}
	}
}

// entityIDs returns the primary key of each row the statement wrote. A statement that
// addressed rows by a condition and carries no key yields one empty id, so the change is
// still recorded.
func entityIDs(tx *gorm.DB) []string {
	stmt := tx.Statement
	rv := stmt.ReflectValue
	if !rv.IsValid() {
		return []string{""}
	}

	if rv.Kind() == reflect.Slice || rv.Kind() == reflect.Array {
		out := make([]string, 0, rv.Len())
		for i := 0; i < rv.Len(); i++ {
			out = append(out, keyOf(stmt, reflect.Indirect(rv.Index(i))))
		}
		return out
	}
	return []string{keyOf(stmt, rv)}
}

// keyOf joins a row's non-zero primary key columns with ":".
func keyOf(stmt *gorm.Statement, row reflect.Value) string {
	if !row.IsValid() || row.Kind() != reflect.Struct {
		return ""
	}
	parts := make([]string, 0, len(stmt.Schema.PrimaryFields))
	for _, f := range stmt.Schema.PrimaryFields {
		v, zero := f.ValueOf(stmt.Context, row)
		if zero {
			continue
		}
		parts = append(parts, fmt.Sprint(v))
	}
	return strings.Join(parts, ":")
}

// changedFields names the columns an update set, when the write said so: Updates with a
// map, or a Select. Values are never read.
func changedFields(stmt *gorm.Statement) string {
	var cols []string
	if m, ok := stmt.Dest.(map[string]any); ok {
		for k := range m {
			cols = append(cols, k)
		}
		sort.Strings(cols)
	} else {
		cols = append(cols, stmt.Selects...)
	}
	return truncate(strings.Join(cols, ","), 1024)
}

// truncate cuts s to at most n bytes without splitting a character.
func truncate(s string, n int) string {
	if len(s) <= n {
		return s
	}
	s = s[:n]
	for !utf8.ValidString(s) {
		s = s[:len(s)-1]
	}
	return s
}
