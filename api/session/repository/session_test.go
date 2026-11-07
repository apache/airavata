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

package repository_test

import (
	"context"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/db"

	model "github.com/apache/airavata/api/session/model"
	"github.com/apache/airavata/api/session/repository"
)

// A stale current run id changes nothing: that is what turns a lost race into a 409.
func TestStaleCurrentRunChangesNothing(t *testing.T) {
	gdb, err := gorm.Open(sqlite.Open("file::memory:"), &gorm.Config{Logger: logger.Default.LogMode(logger.Silent)})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	t.Cleanup(func() { sqlDB, _ := gdb.DB(); _ = sqlDB.Close() })
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatalf("automigrate: %v", err)
	}
	ctx, sessions := context.Background(), repository.NewSessionRepository(gdb)
	if err := sessions.Create(ctx, &model.Session{ID: "s", OwnerID: "alice", CreatedAt: 1, CurrentProcessID: "run-2"}); err != nil {
		t.Fatal(err)
	}

	if swapped, err := sessions.SwapCurrent(ctx, "s", "run-1", "run-3"); err != nil || swapped {
		t.Errorf("swap from a stale run = %v, %v; want false", swapped, err)
	}
	if deleted, err := sessions.Delete(ctx, &model.Session{ID: "s", CurrentProcessID: "run-1"}); err != nil || deleted {
		t.Errorf("delete at a stale run = %v, %v; want false", deleted, err)
	}
	if got, err := sessions.FindByID(ctx, "s"); err != nil || got.CurrentProcessID != "run-2" {
		t.Fatalf("session after stale writes = %+v, %v; want it unchanged at run-2", got, err)
	}

	if swapped, err := sessions.SwapCurrent(ctx, "s", "run-2", "run-3"); err != nil || !swapped {
		t.Errorf("swap from the current run = %v, %v; want true", swapped, err)
	}
	if deleted, err := sessions.Delete(ctx, &model.Session{ID: "s", CurrentProcessID: "run-3"}); err != nil || !deleted {
		t.Errorf("delete at the current run = %v, %v; want true", deleted, err)
	}
}
