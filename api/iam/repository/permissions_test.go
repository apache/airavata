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
	"time"

	"github.com/apache/airavata/internal/permission"
	"github.com/apache/airavata/internal/role"

	model "github.com/apache/airavata/api/iam/model"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

func TestPermissionResolver(t *testing.T) {
	gdb := openTestDB(t) // seeded with the defaults: ADMIN holds audit:read
	r := iamrepo.NewPermissionResolver(gdb, time.Hour)
	ctx := context.Background()

	for _, tc := range []struct {
		name        string
		authorities []string
		want        bool
	}{
		{"admin by default", []string{"ADMIN"}, true},
		{"user by default", []string{"USER"}, false},
		{"super admin holds everything", []string{"SUPER_ADMIN"}, true},
		{"union across roles", []string{"USER", "ADMIN"}, true},
		{"unknown authority", []string{"WIZARD"}, false},
		{"no authorities", nil, false},
	} {
		if got := r.Permissions(ctx, tc.authorities).Has(permission.AuditRead); got != tc.want {
			t.Errorf("%s: has audit:read = %v, want %v", tc.name, got, tc.want)
		}
	}
}

func TestPermissionResolverCachesUntilInvalidatedOrExpired(t *testing.T) {
	gdb := openTestDB(t)
	r := iamrepo.NewPermissionResolver(gdb, time.Hour)
	ctx := context.Background()
	user := []string{"USER"}

	if r.Permissions(ctx, user).Has(permission.AuditRead) {
		t.Fatal("USER holds audit:read before any grant")
	}
	if err := gdb.Create(&model.RolePermission{Role: role.User, Permission: permission.AuditRead}).Error; err != nil {
		t.Fatal(err)
	}
	if r.Permissions(ctx, user).Has(permission.AuditRead) {
		t.Error("a change was visible before Invalidate or expiry; the cache is not caching")
	}
	r.Invalidate()
	if !r.Permissions(ctx, user).Has(permission.AuditRead) {
		t.Error("the grant is not visible after Invalidate")
	}

	// A fresh resolver with a tiny TTL picks changes up on its own.
	short := iamrepo.NewPermissionResolver(gdb, time.Millisecond)
	short.Permissions(ctx, user)
	if err := gdb.Delete(&model.RolePermission{}, "role = ? AND permission = ?", "USER", "audit:read").Error; err != nil {
		t.Fatal(err)
	}
	time.Sleep(5 * time.Millisecond)
	if short.Permissions(ctx, user).Has(permission.AuditRead) {
		t.Error("the revoked grant outlived its TTL")
	}
}

// A failed lookup denies, and is not remembered.
func TestPermissionResolverFailsClosed(t *testing.T) {
	gdb := openTestDB(t)
	r := iamrepo.NewPermissionResolver(gdb, time.Hour)
	if err := gdb.Migrator().DropTable(&model.RolePermission{}); err != nil {
		t.Fatal(err)
	}
	if r.Permissions(context.Background(), []string{"ADMIN"}).Has(permission.AuditRead) {
		t.Error("a failed lookup granted a permission")
	}
	// SUPER_ADMIN needs no lookup, so it survives a broken table.
	if !r.Permissions(context.Background(), []string{"SUPER_ADMIN"}).Has(permission.AuditRead) {
		t.Error("SUPER_ADMIN was locked out by a broken role_permissions table")
	}
	if err := gdb.AutoMigrate(&model.RolePermission{}); err != nil {
		t.Fatal(err)
	}
	if err := gdb.Create(&model.RolePermission{Role: role.Admin, Permission: permission.AuditRead}).Error; err != nil {
		t.Fatal(err)
	}
	if !r.Permissions(context.Background(), []string{"ADMIN"}).Has(permission.AuditRead) {
		t.Error("the failure was cached")
	}
}

func TestRolePermissionRejectsUnknownValues(t *testing.T) {
	gdb := openTestDB(t)
	for _, rp := range []model.RolePermission{
		{Role: "WIZARD", Permission: permission.AuditRead},
		{Role: role.User, Permission: "audit:typo"},
	} {
		if err := gdb.Create(&rp).Error; err == nil {
			t.Errorf("stored %+v", rp)
		}
	}
}
