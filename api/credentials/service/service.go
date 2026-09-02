// Package service holds the credential vertical's business rules: the write-only
// handling that keeps a stored private key from ever being read back, and the sharing
// model that decides who may use an SSH endpoint credential.
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

// SSHKeyService manages registered SSH keypairs.
//
// Reads are open to any caller — responses carry only the public half — while every
// write is administrative.
type SSHKeyService struct {
	db     *gorm.DB
	keys   *repository.SSHKeyRepository
	usedBy *repository.SSHUserCredentialRepository
}

// NewSSHKeyService returns an SSH key service.
func NewSSHKeyService(db *gorm.DB, keys *repository.SSHKeyRepository, usedBy *repository.SSHUserCredentialRepository) *SSHKeyService {
	return &SSHKeyService{db: db, keys: keys, usedBy: usedBy}
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

// Delete removes a key, refusing while any credential still uses it.
//
// This is application-level referential integrity: the schema has no cascade and no
// restricting path from key to credential, so without this check the credentials
// would be left pointing at a key that no longer exists.
func (s *SSHKeyService) Delete(ctx context.Context, id string) error {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}

	return s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		keys, usedBy := s.keys.WithTx(tx), s.usedBy.WithTx(tx)

		key, err := keys.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "SSH key not found: %s", id)
		}
		inUse, err := usedBy.ExistsByKeyID(ctx, id)
		if err != nil {
			return err
		}
		if inUse {
			return httpx.Conflict("Key is in use by a credential and cannot be deleted: %s", id)
		}
		return keys.Delete(ctx, key)
	})
}

func (s *SSHKeyService) requireKey(ctx context.Context, id string) (*model.SSHKey, error) {
	key, err := s.keys.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "SSH key not found: %s", id)
	}
	return key, nil
}

// SSHUserCredentialService manages username-to-key credentials.
type SSHUserCredentialService struct {
	db          *gorm.DB
	credentials *repository.SSHUserCredentialRepository
	keys        *repository.SSHKeyRepository
}

// NewSSHUserCredentialService returns a credential service.
func NewSSHUserCredentialService(db *gorm.DB, creds *repository.SSHUserCredentialRepository, keys *repository.SSHKeyRepository) *SSHUserCredentialService {
	return &SSHUserCredentialService{db: db, credentials: creds, keys: keys}
}

// List returns every credential, or only those using keyID when it is non-empty.
func (s *SSHUserCredentialService) List(ctx context.Context, keyID string) ([]dto.SSHUserCredentialResponse, error) {
	var (
		creds []model.SSHUserCredential
		err   error
	)
	if keyID == "" {
		creds, err = s.credentials.FindAll(ctx)
	} else {
		creds, err = s.credentials.FindByKeyID(ctx, keyID)
	}
	if err != nil {
		return nil, err
	}
	out := make([]dto.SSHUserCredentialResponse, 0, len(creds))
	for i := range creds {
		out = append(out, dto.ToSSHUserCredentialResponse(&creds[i]))
	}
	return out, nil
}

// Get returns one credential.
func (s *SSHUserCredentialService) Get(ctx context.Context, id string) (*dto.SSHUserCredentialResponse, error) {
	cred, err := s.credentials.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "SSH credential not found: %s", id)
	}
	out := dto.ToSSHUserCredentialResponse(cred)
	return &out, nil
}

// Create registers a credential against an existing key.
func (s *SSHUserCredentialService) Create(ctx context.Context, req *dto.SSHUserCredentialRequest) (*dto.SSHUserCredentialResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	var out dto.SSHUserCredentialResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		creds, keys := s.credentials.WithTx(tx), s.keys.WithTx(tx)

		key, err := keys.FindByID(ctx, req.SSHKeyID)
		if err != nil {
			return notFoundAs(err, "SSH key not found: %s", req.SSHKeyID)
		}
		cred := &model.SSHUserCredential{Username: req.Username, SSHKeyID: &key.ID, SSHKey: key}
		if err := creds.Save(ctx, cred); err != nil {
			return err
		}
		out = dto.ToSSHUserCredentialResponse(cred)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Update changes a credential's username or backing key.
func (s *SSHUserCredentialService) Update(ctx context.Context, id string, req *dto.SSHUserCredentialRequest) (*dto.SSHUserCredentialResponse, error) {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return nil, err
	}

	var out dto.SSHUserCredentialResponse
	err := s.db.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		creds, keys := s.credentials.WithTx(tx), s.keys.WithTx(tx)

		cred, err := creds.FindByID(ctx, id)
		if err != nil {
			return notFoundAs(err, "SSH credential not found: %s", id)
		}
		key, err := keys.FindByID(ctx, req.SSHKeyID)
		if err != nil {
			return notFoundAs(err, "SSH key not found: %s", req.SSHKeyID)
		}

		cred.Username = req.Username
		cred.SSHKeyID = &key.ID
		cred.SSHKey = key

		if err := creds.Save(ctx, cred); err != nil {
			return err
		}
		out = dto.ToSSHUserCredentialResponse(cred)
		return nil
	})
	if err != nil {
		return nil, err
	}
	return &out, nil
}

// Delete removes a credential.
func (s *SSHUserCredentialService) Delete(ctx context.Context, id string) error {
	if _, err := auth.RequireAdmin(ctx); err != nil {
		return err
	}
	cred, err := s.credentials.FindByID(ctx, id)
	if err != nil {
		return notFoundAs(err, "SSH credential not found: %s", id)
	}
	return s.credentials.Delete(ctx, cred)
}

func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}
