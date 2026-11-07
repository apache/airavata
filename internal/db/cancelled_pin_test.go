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

package db_test

import (
	"os"
	"strings"
	"testing"
	"time"

	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	model "github.com/apache/airavata/api/process/model"
	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/ptr"
)

// pinPool connects to a fresh schema, every pooled connection searching it.
func pinPool(t *testing.T, schema string) *gorm.DB {
	openPostgres(t, schema)
	dsn := os.Getenv("AIRAVATA_TEST_POSTGRES_DSN")
	sep := "?"
	if strings.Contains(dsn, "?") {
		sep = "&"
	}
	pool, err := db.Open(db.DefaultConfig(dsn+sep+"search_path="+schema), &gorm.Config{Logger: logger.Default.LogMode(logger.Silent)})
	if err != nil {
		t.Fatal(err)
	}
	t.Cleanup(func() {
		if sqlDB, err := pool.DB(); err == nil {
			sqlDB.Close()
		}
	})
	if err := db.AutoMigrate(pool); err != nil {
		t.Fatal(err)
	}
	return pool
}

// late writes a status on a connection of its own and settles once that write waits on a lock or ends.
func late(t *testing.T, pool *gorm.DB, runID string, status model.ProcessStatusType) (blocked bool, done <-chan error) {
	pids, written := make(chan int, 1), make(chan error, 1)
	go func() {
		written <- pool.Connection(func(conn *gorm.DB) error {
			var pid int
			err := conn.Raw("SELECT pg_backend_pid()").Scan(&pid).Error
			pids <- pid
			if err != nil {
				return err
			}
			return conn.Create(&model.ProcessStatus{ProcessID: &runID, Status: ptr.To(status)}).Error
		})
		close(pids)
	}()
	pid, connected := <-pids
	if !connected {
		t.Fatalf("the late status got no connection: %v", <-written)
	}
	for deadline := time.Now().Add(5 * time.Second); time.Now().Before(deadline); time.Sleep(5 * time.Millisecond) {
		select {
		case err := <-written:
			replay := make(chan error, 1)
			replay <- err
			return false, replay
		default:
		}
		if err := pool.Raw("SELECT cardinality(pg_blocking_pids(?)) > 0", pid).Scan(&blocked).Error; err != nil {
			t.Fatal(err)
		}
		if blocked {
			return true, written
		}
	}
	t.Fatal("the late status neither waited nor ended")
	return false, nil
}

// A status written while a Stop holds the run's row must not repoint it past CANCELLED once the
// Stop commits under READ COMMITTED. Like the other PostgreSQL tests it skips without a DSN.
func TestCancelledPinHoldsAgainstAWaitingStatus(t *testing.T) {
	pool := pinPool(t, "airavata_pin")
	run := &model.Process{}
	if err := pool.Create(run).Error; err != nil {
		t.Fatal(err)
	}
	stop := pool.Begin()
	cancelled := &model.ProcessStatus{ProcessID: &run.ID, Status: ptr.To(model.ProcessStatusTypeCancelled)}
	if err := stop.Create(cancelled).Error; err != nil {
		t.Fatal(err)
	}
	blocked, done := late(t, pool, run.ID, model.ProcessStatusTypeFailed)
	if !blocked {
		t.Fatal("the late status did not wait on the Stop")
	}
	if err := stop.Commit().Error; err != nil {
		t.Fatal(err)
	}
	if err := <-done; err != nil {
		t.Fatal(err)
	}

	var got model.Process
	if err := pool.First(&got, "process_id = ?", run.ID).Error; err != nil {
		t.Fatal(err)
	}
	if ptr.From(got.LastStatusID) != cancelled.ID {
		t.Errorf("last status = %v, want the CANCELLED %s", ptr.From(got.LastStatusID), cancelled.ID)
	}
}

// Two status writes for one run hold its key-share lock from their inserts, so neither may then
// ask for a lock that conflicts with it.
func TestConcurrentStatusesDoNotDeadlock(t *testing.T) {
	pool := pinPool(t, "airavata_deadlock")
	run := &model.Process{}
	if err := pool.Create(run).Error; err != nil {
		t.Fatal(err)
	}
	first := pool.Begin()
	if err := first.Exec(`INSERT INTO process_statuses (process_status_id, process_id, status) VALUES ('inserted', ?, 'RUNNING')`, run.ID).Error; err != nil {
		t.Fatal(err)
	}
	_, done := late(t, pool, run.ID, model.ProcessStatusTypeFailed)
	if err := first.Create(&model.ProcessStatus{ProcessID: &run.ID, Status: ptr.To(model.ProcessStatusTypeCancelled)}).Error; err != nil {
		t.Errorf("the first write: %v", err)
	}
	first.Commit()
	if err := <-done; err != nil {
		t.Errorf("the second write: %v", err)
	}
}
