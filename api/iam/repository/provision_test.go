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
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/db"

	model "github.com/apache/airavata/api/iam/model"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

func openTestDB(t *testing.T) *gorm.DB {
	t.Helper()
	gdb, err := gorm.Open(sqlite.Open("file::memory:?_pragma=foreign_keys(1)"), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Silent),
	})
	if err != nil {
		t.Fatalf("open sqlite: %v", err)
	}
	if err := db.AutoMigrate(gdb); err != nil {
		t.Fatalf("automigrate: %v", err)
	}
	return gdb
}

// fakeCILogon answers introspection and userinfo for any token as one CILogon user.
func fakeCILogon(t *testing.T) *httptest.Server {
	t.Helper()
	mux := http.NewServeMux()
	mux.HandleFunc("POST /introspect", func(w http.ResponseWriter, _ *http.Request) {
		_ = json.NewEncoder(w).Encode(map[string]any{
			"active": true, "sub": "http://cilogon.org/serverA/users/194529",
		})
	})
	mux.HandleFunc("GET /userinfo", func(w http.ResponseWriter, _ *http.Request) {
		_ = json.NewEncoder(w).Encode(map[string]any{
			"given_name": "Ada", "family_name": "Lovelace", "email": "ada@example.edu",
		})
	})
	srv := httptest.NewServer(mux)
	t.Cleanup(srv.Close)
	return srv
}

func TestCILogonSignInCreatesTheUserRecord(t *testing.T) {
	gdb := openTestDB(t)
	ctx := context.Background()
	idp := fakeCILogon(t)

	introspector := auth.NewCILogonIntrospector(idp.URL+"/introspect", idp.URL+"/userinfo",
		"client", "secret", iamrepo.DBRoleLookup{DB: gdb}, nil)
	introspector.Users = &iamrepo.CILogonUserProvisioner{DB: gdb}

	for range 2 {
		principal, err := introspector.Introspect(ctx, "token")
		if err != nil {
			t.Fatalf("Introspect: %v", err)
		}
		if principal.Name != "cilogon:194529" {
			t.Fatalf("Name = %q, want cilogon:194529", principal.Name)
		}
		if principal.Status != "SUSPENDED" || principal.Active() {
			t.Fatalf("Status = %q, want a new user to start SUSPENDED", principal.Status)
		}
	}

	user, err := iamrepo.NewUserRepository(gdb).FindByID(ctx, "cilogon:194529")
	if err != nil {
		t.Fatalf("FindByID: %v", err)
	}
	if user.AuthMethod == nil || *user.AuthMethod != model.AuthMethodCILogon {
		t.Errorf("AuthMethod = %v, want CILOGON", user.AuthMethod)
	}
	if user.Status == nil || *user.Status != model.UserStatusSuspended {
		t.Errorf("Status = %v, want SUSPENDED", user.Status)
	}
	if user.Email == nil || *user.Email != "ada@example.edu" ||
		user.FirstName == nil || *user.FirstName != "Ada" ||
		user.LastName == nil || *user.LastName != "Lovelace" {
		t.Errorf("profile = %+v, want it filled from CILogon", user)
	}
}

func TestCILogonSignInLeavesAnExistingRecordAlone(t *testing.T) {
	gdb := openTestDB(t)
	ctx := context.Background()

	repo := iamrepo.NewUserRepository(gdb)
	email := "ada@university.example.edu"
	status := model.UserStatusInactive
	if err := repo.Save(ctx, &model.User{ID: "cilogon:194529", Email: &email, Status: &status, CreatedAt: 1}); err != nil {
		t.Fatalf("Save: %v", err)
	}

	provisioner := &iamrepo.CILogonUserProvisioner{DB: gdb}
	got, err := provisioner.ProvisionUser(ctx, &auth.Principal{
		Name:       "cilogon:194529",
		Attributes: map[string]any{"email": "ada@example.edu"},
	})
	if err != nil {
		t.Fatalf("ProvisionUser: %v", err)
	}
	if got != "INACTIVE" {
		t.Errorf("status = %q, want the existing record's INACTIVE", got)
	}

	// Activating the record takes effect on the next request.
	active := model.UserStatusActive
	if err := gdb.Model(&model.User{}).Where("user_id = ?", "cilogon:194529").Update("status", active).Error; err != nil {
		t.Fatalf("activate: %v", err)
	}
	if got, _ := provisioner.ProvisionUser(ctx, &auth.Principal{Name: "cilogon:194529"}); got != "ACTIVE" {
		t.Errorf("status after activation = %q, want ACTIVE", got)
	}
	// Restore what the rest of the test checks.
	if err := gdb.Model(&model.User{}).Where("user_id = ?", "cilogon:194529").Update("status", status).Error; err != nil {
		t.Fatalf("restore: %v", err)
	}

	user, err := repo.FindByID(ctx, "cilogon:194529")
	if err != nil {
		t.Fatalf("FindByID: %v", err)
	}
	if *user.Email != email || *user.Status != status || user.CreatedAt != 1 {
		t.Errorf("user = %+v, want the existing record unchanged", user)
	}
}
