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
	"context"
	"testing"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/db"
)

const seedLegacyProducts = `INSERT INTO data_products (data_id, is_file, path, data_storage_id, data_storage_type, created_at) VALUES
	('located', true, '/scratch/a', 'stor1', 'SCP', 5),
	('untyped', true, '/scratch/b', 'stor1', '', 6),
	('no-path', true, NULL, 'stor1', 'SCP', 7),
	('blank-path', true, '', 'stor1', 'SCP', 8),
	('no-storage', true, '/scratch/c', NULL, 'SCP', 9)`

type backfilledReplica struct {
	ID            string `gorm:"column:replica_id"`
	ProductID     string `gorm:"column:data_product_id"`
	StorageID     string `gorm:"column:data_storage_id"`
	StorageType   string `gorm:"column:data_storage_type"`
	Path          string `gorm:"column:path"`
	IsPrimary     bool   `gorm:"column:is_primary"`
	CreatedAtTime int64  `gorm:"column:created_at"`
}

// checkBackfill asserts each located product became one primary replica and that a
// product naming no location got none.
func checkBackfill(t *testing.T, gdb *gorm.DB) {
	t.Helper()

	var got []backfilledReplica
	if err := gdb.Raw(`SELECT * FROM data_product_replicas ORDER BY replica_id`).Scan(&got).Error; err != nil {
		t.Fatalf("read back: %v", err)
	}
	want := []backfilledReplica{
		{"located", "located", "stor1", "SCP", "/scratch/a", true, 5},
		{"untyped", "untyped", "stor1", "SCP", "/scratch/b", true, 6},
	}
	if len(got) != len(want) {
		t.Fatalf("backfilled %d replicas, want %d: %+v", len(got), len(want), got)
	}
	for i := range want {
		if got[i] != want[i] {
			t.Errorf("replica %d = %+v, want %+v", i, got[i], want[i])
		}
	}
}

// AutoMigrate only creates the table, so it has to backfill too, and it runs at every
// start, so doing so twice must change nothing.
func TestAutoMigrateBackfillsProductReplicas(t *testing.T) {
	gdb := newTestDB(t)
	if err := gdb.Exec(seedLegacyProducts).Error; err != nil {
		t.Fatalf("seed: %v", err)
	}

	for run := 1; run <= 2; run++ {
		if err := db.AutoMigrate(gdb); err != nil {
			t.Fatalf("automigrate run %d: %v", run, err)
		}
		checkBackfill(t, gdb)
	}
}

// Migration 0022 on a database that already holds products, on PostgreSQL. It skips
// unless AIRAVATA_TEST_POSTGRES_DSN is set, like the other PostgreSQL tests.
func TestReplicaMigrationCarriesProductsOver(t *testing.T) {
	gdb := openPostgres(t, "airavata_replicamig")
	ctx := context.Background()

	all := db.Migrations()
	var before []db.Migration
	for _, m := range all {
		if m.Version < 22 {
			before = append(before, m)
		}
	}
	if len(before) == len(all) {
		t.Fatal("migration 0022 is missing")
	}
	if _, err := db.NewMigrator(gdb, before).Up(ctx); err != nil {
		t.Fatalf("migrations before 0022: %v", err)
	}
	if err := gdb.Exec(seedLegacyProducts).Error; err != nil {
		t.Fatalf("seed: %v", err)
	}

	if _, err := db.NewMigrator(gdb, all).Up(ctx); err != nil {
		t.Fatalf("0022: %v", err)
	}
	checkBackfill(t, gdb)
}
