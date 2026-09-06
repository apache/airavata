// Package service holds the credential vertical's business rules: chiefly the
// write-only handling that keeps a stored private key from ever being read back.
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

// SSHKeyService manages registered SSH keypairs.
//
// Reads are open to any caller — responses carry only the public half — while every
// write is administrative.
type SSHKeyService struct {
	keys   *repository.SSHKeyRepository
	usedBy []KeyReferrer
}

// NewSSHKeyService returns an SSH key service. usedBy is every kind of record that can
// hold a key; a key none of them names is free to delete.
func NewSSHKeyService(keys *repository.SSHKeyRepository, usedBy ...KeyReferrer) *SSHKeyService {
	return &SSHKeyService{keys: keys, usedBy: usedBy}
}

// List returns every key.
func (s *SSHKeyService) List(ctx context.Context) ([]dto.SSHKeyResponse, error) {
	keys, err := s.keys.FindAll(ctx)
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
	key, err := s.requireKey(ctx, id)
	if err != nil {
		return nil, err
	}
	out := dto.ToSSHKeyResponse(key)
	return &out, nil
}

// Create registers a key.
//
// The private key is required here even though the payload makes it optional: the
// same payload is reused for updates, where omitting it means "keep what is stored".
func (s *SSHKeyService) Create(ctx context.Context, req *dto.SSHKeyRequest) (*dto.SSHKeyResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
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
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	key, err := s.requireKey(ctx, id)
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
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}
	key, err := s.keys.FindByID(ctx, id)
	if err != nil {
		return notFoundAs(err, "SSH key not found: %s", id)
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

func (s *SSHKeyService) requireKey(ctx context.Context, id string) (*model.SSHKey, error) {
	key, err := s.keys.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "SSH key not found: %s", id)
	}
	return key, nil
}

func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}
