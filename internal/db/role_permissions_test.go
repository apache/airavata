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
	"regexp"
	"sort"
	"strings"
	"testing"

	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/permission"

	iammodel "github.com/apache/airavata/api/iam/model"
)

func grants(t *testing.T, rows []iammodel.RolePermission) []string {
	t.Helper()
	out := make([]string, 0, len(rows))
	for _, r := range rows {
		out = append(out, string(r.Role)+" "+string(r.Permission))
	}
	sort.Strings(out)
	return out
}

func TestAutoMigrateSeedsDefaultsOnce(t *testing.T) {
	gdb := newTestDB(t)

	var seeded []iammodel.RolePermission
	if err := gdb.Find(&seeded).Error; err != nil {
		t.Fatal(err)
	}
	var want []string
	for r, perms := range permission.Defaults() {
		for _, p := range perms {
			want = append(want, string(r)+" "+string(p))
		}
	}
	sort.Strings(want)
	if got := grants(t, seeded); strings.Join(got, ",") != strings.Join(want, ",") {
		t.Fatalf("seeded %v, want %v", got, want)
	}

	// An administrator's revocation must survive the next start.
	if err := gdb.Where("1 = 1").Delete(&iammodel.RolePermission{}).Error; err != nil {
		t.Fatal(err)
	}
	if err := gdb.Create(&iammodel.RolePermission{Role: "USER", Permission: permission.AuditRead}).Error; err != nil {
		t.Fatal(err)
	}
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatal(err)
	}
	var after []iammodel.RolePermission
	if err := gdb.Find(&after).Error; err != nil {
		t.Fatal(err)
	}
	if got := grants(t, after); len(got) != 1 || got[0] != "USER audit:read" {
		t.Errorf("AutoMigrate re-seeded over an edited table: %v", got)
	}
}

// The versioned migrations and the in-code defaults seed the same grants.
func TestDefaultsMatchMigrations(t *testing.T) {
	row := regexp.MustCompile(`\('([A-Z_]+)','([a-z_:]+)'\)`)
	var got []string
	for _, m := range db.Migrations() {
		if !strings.Contains(m.SQL, `INSERT INTO "role_permissions"`) {
			continue
		}
		for _, r := range row.FindAllStringSubmatch(m.SQL, -1) {
			got = append(got, r[1]+" "+r[2])
		}
	}
	sort.Strings(got)

	var want []string
	for r, perms := range permission.Defaults() {
		for _, p := range perms {
			want = append(want, string(r)+" "+string(p))
		}
	}
	sort.Strings(want)
	if strings.Join(got, ",") != strings.Join(want, ",") {
		t.Errorf("migrations seed\n  %v\nbut permission.Defaults has\n  %v", got, want)
	}
}
