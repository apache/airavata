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

package service_test

import (
	"context"
	"net/http"
	"os"
	"path/filepath"
	"testing"

	"github.com/glebarez/sqlite"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"

	"github.com/apache/airavata/internal/db"
	"github.com/apache/airavata/internal/httpx"

	datarepo "github.com/apache/airavata/api/data/repository"
	service "github.com/apache/airavata/api/data/service"
	iammodel "github.com/apache/airavata/api/iam/model"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

func newAiravataStorageService(t *testing.T) (*service.AiravataDataStorageService, *gorm.DB, string) {
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
	root := filepath.Join(t.TempDir(), "storage")
	svc := service.NewAiravataDataStorageService(gdb,
		datarepo.NewAiravataDataStorageRepository(gdb), iamrepo.NewUserRepository(gdb), root)
	return svc, gdb, root
}

func addUser(t *testing.T, gdb *gorm.DB, id string) {
	t.Helper()
	if err := gdb.Create(&iammodel.User{ID: id, CreatedAt: 1}).Error; err != nil {
		t.Fatalf("create user: %v", err)
	}
}

func TestAiravataDataStorageCreateAndDelete(t *testing.T) {
	svc, gdb, root := newAiravataStorageService(t)
	ctx := context.Background()
	userID := "http://cilogon.org/serverA/users/42"
	addUser(t, gdb, userID)

	storage, err := svc.Create(ctx, userID)
	if err != nil {
		t.Fatalf("create: %v", err)
	}
	want := filepath.Join(root, "http:%2F%2Fcilogon.org%2FserverA%2Fusers%2F42")
	if storage.RootDirectory == nil || *storage.RootDirectory != want {
		t.Fatalf("RootDirectory = %v, want %s", storage.RootDirectory, want)
	}
	if info, err := os.Stat(want); err != nil || !info.IsDir() {
		t.Fatalf("directory not created: %v", err)
	}

	if _, err := svc.Create(ctx, userID); httpx.StatusOf(err) != http.StatusConflict {
		t.Fatalf("second create: got %v, want 409", err)
	}

	if err := os.WriteFile(filepath.Join(want, "f.txt"), []byte("x"), 0o600); err != nil {
		t.Fatal(err)
	}
	if err := svc.Delete(ctx, userID); err != nil {
		t.Fatalf("delete: %v", err)
	}
	if _, err := os.Stat(want); !os.IsNotExist(err) {
		t.Fatalf("directory survived delete: %v", err)
	}
	if _, err := svc.Get(ctx, userID); httpx.StatusOf(err) != http.StatusNotFound {
		t.Fatalf("get after delete: got %v, want 404", err)
	}
	if err := svc.Delete(ctx, userID); httpx.StatusOf(err) != http.StatusNotFound {
		t.Fatalf("second delete: got %v, want 404", err)
	}

	// The storage can be recreated once deleted.
	if _, err := svc.Create(ctx, userID); err != nil {
		t.Fatalf("recreate: %v", err)
	}
}

func TestAiravataDataStorageCreateUnknownUser(t *testing.T) {
	svc, _, root := newAiravataStorageService(t)
	if _, err := svc.Create(context.Background(), "nobody"); httpx.StatusOf(err) != http.StatusNotFound {
		t.Fatalf("got %v, want 404", err)
	}
	if _, err := os.Stat(filepath.Join(root, "nobody")); !os.IsNotExist(err) {
		t.Fatalf("directory created for unknown user: %v", err)
	}
}

func TestAiravataDataStorageRejectsTraversal(t *testing.T) {
	svc, gdb, _ := newAiravataStorageService(t)
	addUser(t, gdb, "..")
	if _, err := svc.Create(context.Background(), ".."); httpx.StatusOf(err) != http.StatusBadRequest {
		t.Fatalf("got %v, want 400", err)
	}
}
