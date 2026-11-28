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

package service

import (
	"context"
	"errors"
	"fmt"
	"net/url"
	"os"
	"path/filepath"
	"strings"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/data/model"
	"github.com/apache/airavata/api/data/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// AiravataDataStorageService creates and deletes the per-user storage kept on the host
// Airavata runs on: a directory under the configured root, named after the user.
//
// It is internal. Nothing routes to it, so it does no authorisation of its own; the
// caller decides which user a storage is made or removed for.
type AiravataDataStorageService struct {
	db       *gorm.DB
	storages *repository.AiravataDataStorageRepository
	users    *iamrepo.UserRepository
	root     string
}

// NewAiravataDataStorageService returns a storage service that keeps every user's
// directory under root. root is resolved to an absolute path, so a relative setting
// does not move with the server's working directory after startup. An empty root is
// accepted here and refused by Create, so a server without one still starts.
func NewAiravataDataStorageService(
	db *gorm.DB,
	storages *repository.AiravataDataStorageRepository,
	users *iamrepo.UserRepository,
	root string,
) *AiravataDataStorageService {
	if strings.TrimSpace(root) != "" {
		if abs, err := filepath.Abs(root); err == nil {
			root = abs
		} else {
			root = filepath.Clean(root)
		}
	} else {
		root = ""
	}
	return &AiravataDataStorageService{db: db, storages: storages, users: users, root: root}
}

// Get returns the storage userID owns, or 404.
func (s *AiravataDataStorageService) Get(ctx context.Context, userID string) (*model.AiravataDataStorage, error) {
	storage, err := s.storages.FindByOwnerID(ctx, userID)
	if err != nil {
		return nil, notFoundAs(err, "Airavata data storage not found for user: %s", userID)
	}
	return storage, nil
}

// Create gives userID their storage: a row recording it and a directory for it under
// the configured root. A user has at most one, so a second call reports 409.
//
// The row is inserted before the directory is made, inside one transaction, so a
// failure on either side leaves neither behind. A directory that already exists — left
// by an earlier storage whose removal failed — is taken over rather than refused.
func (s *AiravataDataStorageService) Create(ctx context.Context, userID string) (*model.AiravataDataStorage, error) {
	dir, err := s.directoryFor(userID)
	if err != nil {
		return nil, err
	}

	var created *model.AiravataDataStorage
	var madeDir bool
	err = s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		storages := s.storages.WithTx(tx)

		if _, err := s.users.WithTx(tx).FindByID(ctx, userID); err != nil {
			return notFoundAs(err, "User not found: %s", userID)
		}
		if _, err := storages.FindByOwnerID(ctx, userID); err == nil {
			return httpx.Conflict("User %s already has an Airavata data storage", userID)
		} else if !errors.Is(err, gorm.ErrRecordNotFound) {
			return err
		}

		storage := &model.AiravataDataStorage{OwnerID: &userID, RootDirectory: &dir}
		if err := storages.Create(ctx, storage); err != nil {
			// The lookup above passed, so a failure here is most likely a concurrent
			// create winning the unique index on user_id.
			if _, findErr := storages.FindByOwnerID(ctx, userID); findErr == nil {
				return httpx.Conflict("User %s already has an Airavata data storage", userID)
			}
			return err
		}

		made, err := makeDirectory(dir)
		if err != nil {
			return err
		}
		madeDir = made
		created = storage
		return nil
	})
	if err != nil {
		// The transaction rolled back, so a directory made for it must go too.
		if madeDir {
			_ = os.Remove(dir)
		}
		return nil, err
	}
	return created, nil
}

// Delete removes userID's storage: its row and its directory with everything in it.
//
// The directory is removed inside the transaction that deletes the row, so a failed
// removal keeps the row and the caller can retry.
func (s *AiravataDataStorageService) Delete(ctx context.Context, userID string) error {
	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		storages := s.storages.WithTx(tx)

		storage, err := storages.FindByOwnerID(ctx, userID)
		if err != nil {
			return notFoundAs(err, "Airavata data storage not found for user: %s", userID)
		}
		if err := storages.Delete(ctx, storage); err != nil {
			return err
		}
		if storage.RootDirectory == nil || *storage.RootDirectory == "" {
			return nil
		}

		dir := *storage.RootDirectory
		if !s.contains(dir) {
			// Never recursively delete anything outside the configured root, whatever
			// the row says.
			return fmt.Errorf("airavata data storage %s points outside %s: %s", storage.ID, s.root, dir)
		}
		if err := os.RemoveAll(dir); err != nil {
			return fmt.Errorf("remove airavata data storage directory %s: %w", dir, err)
		}
		return nil
	})
}

// directoryFor is where userID's storage lives: the configured root joined with the
// user id.
//
// User ids come from outside — CILogon subjects may be URLs — so the id is
// path-escaped first. Escaping is reversible, which keeps two different users from
// ever landing on the same directory, and it turns every "/" into "%2F" so the result
// stays one level under the root.
func (s *AiravataDataStorageService) directoryFor(userID string) (string, error) {
	if s.root == "" {
		return "", errors.New("airavata data storage path is not configured")
	}
	name := url.PathEscape(userID)
	if name == "" || name == "." || name == ".." {
		return "", httpx.BadRequest("Invalid user id for an Airavata data storage: %q", userID)
	}
	dir := filepath.Join(s.root, name)
	if !s.contains(dir) {
		return "", httpx.BadRequest("Invalid user id for an Airavata data storage: %q", userID)
	}
	return dir, nil
}

// contains reports whether dir lies strictly beneath the configured root.
func (s *AiravataDataStorageService) contains(dir string) bool {
	if s.root == "" {
		return false
	}
	rel, err := filepath.Rel(s.root, filepath.Clean(dir))
	if err != nil {
		return false
	}
	return rel != "." && rel != ".." && !strings.HasPrefix(rel, ".."+string(filepath.Separator)) && !filepath.IsAbs(rel)
}

// makeDirectory creates dir, and the root above it if need be, readable only by the
// server's own account. It reports whether it made dir rather than finding it there.
func makeDirectory(dir string) (bool, error) {
	if err := os.MkdirAll(filepath.Dir(dir), 0o700); err != nil {
		return false, fmt.Errorf("create airavata data storage root %s: %w", filepath.Dir(dir), err)
	}
	err := os.Mkdir(dir, 0o700)
	switch {
	case err == nil:
		return true, nil
	case errors.Is(err, os.ErrExist):
		info, statErr := os.Stat(dir)
		if statErr != nil {
			return false, fmt.Errorf("inspect airavata data storage directory %s: %w", dir, statErr)
		}
		if !info.IsDir() {
			return false, fmt.Errorf("airavata data storage path %s exists and is not a directory", dir)
		}
		return false, nil
	default:
		return false, fmt.Errorf("create airavata data storage directory %s: %w", dir, err)
	}
}
