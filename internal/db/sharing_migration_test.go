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

	"github.com/apache/airavata/internal/db"
)

// The 0002 migration carries existing grants into the unified table. Running it on an
// empty database proves only that the SQL parses; this seeds each of the eight old
// tables and checks every row lands with the right resource type, principal type and
// id, under its original sharing id.
//
// Like the other PostgreSQL tests it skips unless AIRAVATA_TEST_POSTGRES_DSN is set.
func TestUnifiedSharingMigrationCarriesRowsOver(t *testing.T) {
	gdb := openPostgres(t, "airavata_datamig")
	ctx := context.Background()

	all := db.Migrations()
	if len(all) < 2 {
		t.Fatalf("expected at least 2 migrations, got %d", len(all))
	}
	if _, err := db.NewMigrator(gdb, all[:1]).Up(ctx); err != nil {
		t.Fatalf("baseline: %v", err)
	}

	seed := []string{
		`INSERT INTO users (user_id, created_at) VALUES ('alice', 1), ('bob', 1)`,
		`INSERT INTO groups (group_id, created_at) VALUES ('g1', 1)`,
		// The old tables carry real foreign keys to the records they open up.
		`INSERT INTO scp_data_storages (data_id) VALUES ('stor1')`,
		`INSERT INTO data_products (data_id, is_file, created_at) VALUES ('prod1', true, 1)`,
		`INSERT INTO virtual_data_directories (virtual_data_directory_id, created_at) VALUES ('dir1', 1)`,
		`INSERT INTO scp_data_storage_group_sharings VALUES ('s-g','stor1','g1','READ')`,
		`INSERT INTO scp_data_storage_user_sharings VALUES ('s-u','stor1','bob','WRITE')`,
		`INSERT INTO data_product_group_sharings VALUES ('p-g','prod1','g1','WRITE')`,
		`INSERT INTO data_product_user_sharings VALUES ('p-u','prod1','bob','READ')`,
		`INSERT INTO virtual_data_directory_group_sharings VALUES ('v-g','dir1','g1','READ')`,
		`INSERT INTO virtual_data_directory_user_sharings VALUES ('v-u','dir1','bob','WRITE')`,
		`INSERT INTO slurm_clusters (slurm_cluster_id, cluster_name, headnode_host, headnode_port) VALUES ('c1','c',  'h', 22)`,
		`INSERT INTO slurm_cluster_configs (slurm_cluster_config_id, slurm_cluster_id, login_user, work_root) VALUES ('cfg1','c1','runner','/scratch')`,
		`INSERT INTO slurm_cluster_config_group_sharings VALUES ('c-g','cfg1','g1','READ')`,
		`INSERT INTO slurm_cluster_config_user_sharings VALUES ('c-u','cfg1','bob','WRITE')`,
		// Rows that granted nothing under the old nullable columns must be left behind.
		`INSERT INTO data_product_user_sharings VALUES ('p-null','prod1',NULL,'READ')`,
		`INSERT INTO data_product_user_sharings VALUES ('p-noperm','prod1','alice',NULL)`,
	}
	for _, s := range seed {
		if err := gdb.Exec(s).Error; err != nil {
			t.Fatalf("seed %q: %v", s, err)
		}
	}

	if _, err := db.NewMigrator(gdb, all).Up(ctx); err != nil {
		t.Fatalf("0002: %v", err)
	}

	type row struct {
		ID            string `gorm:"column:resource_sharing_id"`
		ResourceType  string `gorm:"column:resource_type"`
		ResourceID    string `gorm:"column:resource_id"`
		PrincipalType string `gorm:"column:principal_type"`
		PrincipalID   string `gorm:"column:principal_id"`
		Permission    string `gorm:"column:permission"`
	}
	var got []row
	if err := gdb.Raw(`SELECT * FROM resource_sharings ORDER BY resource_sharing_id`).Scan(&got).Error; err != nil {
		t.Fatalf("read back: %v", err)
	}

	want := map[string]row{
		"s-g": {"s-g", "SCP_DATA_STORAGE", "stor1", "GROUP", "g1", "READ"},
		"s-u": {"s-u", "SCP_DATA_STORAGE", "stor1", "USER", "bob", "WRITE"},
		"p-g": {"p-g", "DATA_PRODUCT", "prod1", "GROUP", "g1", "WRITE"},
		"p-u": {"p-u", "DATA_PRODUCT", "prod1", "USER", "bob", "READ"},
		"v-g": {"v-g", "VIRTUAL_DATA_DIRECTORY", "dir1", "GROUP", "g1", "READ"},
		"v-u": {"v-u", "VIRTUAL_DATA_DIRECTORY", "dir1", "USER", "bob", "WRITE"},
		"c-g": {"c-g", "SLURM_CLUSTER_CONFIG", "cfg1", "GROUP", "g1", "READ"},
		"c-u": {"c-u", "SLURM_CLUSTER_CONFIG", "cfg1", "USER", "bob", "WRITE"},
	}
	if len(got) != len(want) {
		t.Fatalf("carried %d rows, want %d: %+v", len(got), len(want), got)
	}
	for _, g := range got {
		w, ok := want[g.ID]
		if !ok {
			t.Errorf("unexpected row %+v", g)
			continue
		}
		if g != w {
			t.Errorf("row %s = %+v, want %+v", g.ID, g, w)
		}
	}

	for _, tbl := range []string{
		"scp_data_storage_group_sharings", "scp_data_storage_user_sharings",
		"data_product_group_sharings", "data_product_user_sharings",
		"virtual_data_directory_group_sharings", "virtual_data_directory_user_sharings",
		"slurm_cluster_config_group_sharings", "slurm_cluster_config_user_sharings",
	} {
		if gdb.Migrator().HasTable(tbl) {
			t.Errorf("table %q survived the migration", tbl)
		}
	}
}
