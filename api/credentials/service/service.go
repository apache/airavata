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

// Package service holds the credential vertical's business rules: the write-only
// handling that keeps a stored private key from ever being read back, and the
// ownership that keeps a key to the person who registered it.
package service

import (
	"context"
	"errors"
	"strings"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/ptr"

	dto "github.com/apache/airavata/api/credentials/dto"
	model "github.com/apache/airavata/api/credentials/model"
	"github.com/apache/airavata/api/credentials/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	iamsvc "github.com/apache/airavata/api/iam/service"
)

// KeyReferrer counts the records outside this package that still present a key.
//
// A key is registered here but used elsewhere — a cluster config logs in with one, a
// data storage stages under one — and this package must not import those verticals to
// find out. They satisfy this interface instead, and are handed to the service in
// internal/app, which is the one place that knows the whole graph.
type KeyReferrer interface {
	CountByKeyID(ctx context.Context, keyID string) (int64, error)
}

// KeyAccess answers "may this caller present this key?" for services outside the
// credentials package.
//
// A cluster config and a data storage both name a key, and both ask this before
// pointing at one. Exposing the question rather than the table is what keeps a single
// definition of who may present a key — its owner, nobody else.
type KeyAccess struct{ keys *repository.SSHKeyRepository }

// NewKeyAccess returns a checker over the key table.
func NewKeyAccess(keys *repository.SSHKeyRepository) *KeyAccess {
	return &KeyAccess{keys: keys}
}

// WithTx returns a checker bound to tx, for checks made from inside a transaction.
func (a *KeyAccess) WithTx(tx *gorm.DB) *KeyAccess {
	return &KeyAccess{keys: a.keys.WithTx(tx)}
}

// Find loads a key without asking who owns it. It is for a record re-presenting the
// key it already holds: keeping a key is not assigning one, so someone editing a
// config shared with them does not need to own the key already on it.
func (a *KeyAccess) Find(ctx context.Context, id string) (*model.SSHKey, error) {
	key, err := a.keys.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "SSH key not found: %s", id)
	}
	return key, nil
}

// RequireOwned loads a key the caller may assign: 404 when there is no such key, 403
// when it belongs to somebody else. Assigning a key is presenting its private material
// under a name of your choosing, which is the owner's to allow — and they allow it by
// sharing what holds the key, never the key.
func (a *KeyAccess) RequireOwned(ctx context.Context, id string) (*model.SSHKey, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	key, err := a.Find(ctx, id)
	if err != nil {
		return nil, err
	}
	if !key.OwnedBy(principal.Name) {
		return nil, httpx.Forbidden("Access denied: SSH key %s belongs to another user", id)
	}
	return key, nil
}

// SSHKeyService manages registered SSH keypairs.
//
// Registering one is self-service — any authenticated caller may register a key, and it
// belongs to them — but nothing about it is reachable by anyone else. Reads, updates
// and deletes are the owner's alone, and there is no share to open one up: a key is the
// credential itself, so lending it out is what sharing a cluster config is for.
//
// Platform admins are deliberately not treated as owners here, unlike everywhere else
// in this API. An admin has no business reading or repointing someone's key material.
type SSHKeyService struct {
	keys   *repository.SSHKeyRepository
	users  *iamrepo.UserRepository
	usedBy []KeyReferrer
}

// NewSSHKeyService returns an SSH key service. usedBy is every kind of record that can
// hold a key; a key none of them names is free to delete.
func NewSSHKeyService(keys *repository.SSHKeyRepository, users *iamrepo.UserRepository, usedBy ...KeyReferrer) *SSHKeyService {
	return &SSHKeyService{keys: keys, users: users, usedBy: usedBy}
}

// List returns the caller's own keys. There is no listing across owners: a key is
// private to whoever registered it, and that holds for admins too.
func (s *SSHKeyService) List(ctx context.Context) ([]dto.SSHKeyResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	keys, err := s.keys.FindByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	out := make([]dto.SSHKeyResponse, 0, len(keys))
	for i := range keys {
		out = append(out, dto.ToSSHKeyResponse(&keys[i]))
	}
	return out, nil
}

// Get returns one key.
func (s *SSHKeyService) Get(ctx context.Context, id string) (*dto.SSHKeyResponse, error) {
	key, err := s.requireOwnedKey(ctx, id)
	if err != nil {
		return nil, err
	}
	out := dto.ToSSHKeyResponse(key)
	return &out, nil
}

// Create registers a key owned by the calling user.
//
// The owner is taken from the token, so there is no way to register a key on someone
// else's behalf. The private key is required here even though the payload makes it
// optional: the same payload is reused for updates, where omitting it means "keep what
// is stored".
func (s *SSHKeyService) Create(ctx context.Context, req *dto.SSHKeyRequest) (*dto.SSHKeyResponse, error) {
	owner, err := iamsvc.RequireCurrentUser(ctx, s.users)
	if err != nil {
		return nil, err
	}
	if req.PrivateKey == nil || strings.TrimSpace(*req.PrivateKey) == "" {
		return nil, httpx.BadRequest("Private key is required when creating a key")
	}

	key := &model.SSHKey{
		SSHKeyName: req.SSHKeyName,
		PublicKey:  req.PublicKey,
		PrivateKey: *req.PrivateKey,
		Passphrase: ptr.NonBlank(req.Passphrase),
		OwnerID:    owner.ID,
	}
	if err := s.keys.Save(ctx, key); err != nil {
		return nil, err
	}
	out := dto.ToSSHKeyResponse(key)
	return &out, nil
}

// Update changes a key.
//
// A blank or absent private key or passphrase means "leave the stored secret alone",
// not "erase it". Without that rule, a client round-tripping a response — which never
// contains the secrets — would silently wipe them.
func (s *SSHKeyService) Update(ctx context.Context, id string, req *dto.SSHKeyRequest) (*dto.SSHKeyResponse, error) {
	key, err := s.requireOwnedKey(ctx, id)
	if err != nil {
		return nil, err
	}

	key.SSHKeyName = req.SSHKeyName
	key.PublicKey = req.PublicKey
	if v := ptr.NonBlank(req.PrivateKey); v != nil {
		key.PrivateKey = *v
	}
	if v := ptr.NonBlank(req.Passphrase); v != nil {
		key.Passphrase = v
	}

	if err := s.keys.Save(ctx, key); err != nil {
		return nil, err
	}
	out := dto.ToSSHKeyResponse(key)
	return &out, nil
}

// Delete removes a key, refusing while anything still presents it.
//
// Every foreign key pointing at a key is RESTRICT, so the database would refuse this
// anyway; asking first turns an opaque constraint violation into a 409 that says how
// many records still hold the key. The count runs before the delete rather than in a
// transaction with it — as the data storage service checks its products — and the
// constraint remains the backstop for anything registered in between.
func (s *SSHKeyService) Delete(ctx context.Context, id string) error {
	key, err := s.requireOwnedKey(ctx, id)
	if err != nil {
		return err
	}

	held := int64(0)
	for _, referrer := range s.usedBy {
		n, err := referrer.CountByKeyID(ctx, id)
		if err != nil {
			return err
		}
		held += n
	}
	if held > 0 {
		return httpx.Conflict("SSH key %s is still presented by %d record(s) and cannot be deleted", id, held)
	}
	return s.keys.Delete(ctx, key)
}

func (s *SSHKeyService) requireOwnedKey(ctx context.Context, id string) (*model.SSHKey, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	key, err := s.keys.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "SSH key not found: %s", id)
	}
	if !key.OwnedBy(principal.Name) {
		return nil, httpx.Forbidden("Access denied: SSH key %s belongs to another user", id)
	}
	return key, nil
}

func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}
