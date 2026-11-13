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
	"strings"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/ptr"

	dto "github.com/apache/airavata/api/credentials/dto"
	model "github.com/apache/airavata/api/credentials/model"
	"github.com/apache/airavata/api/credentials/repository"
	iamrepo "github.com/apache/airavata/api/iam/repository"
	iamsvc "github.com/apache/airavata/api/iam/service"
)

// S3CredentialReferrer counts the records outside this package that still present an
// S3 credential. It plays the part KeyReferrer plays for SSH credentials: the records
// that use one live in other verticals, which satisfy this interface instead of being
// imported here.
type S3CredentialReferrer interface {
	CountByS3CredentialID(ctx context.Context, credentialID string) (int64, error)
}

// S3CredentialService manages registered S3 access keys.
//
// The rules are the SSH credential rules: registering one is self-service, and reading,
// updating and deleting it are the owner's alone. Platform admins are not owners, and
// there is no share to open a credential up.
type S3CredentialService struct {
	creds  *repository.S3CredentialRepository
	users  *iamrepo.UserRepository
	usedBy []S3CredentialReferrer
}

// NewS3CredentialService returns an S3 credential service. usedBy is every kind of
// record that can hold a credential; one none of them names is free to delete.
func NewS3CredentialService(creds *repository.S3CredentialRepository, users *iamrepo.UserRepository, usedBy ...S3CredentialReferrer) *S3CredentialService {
	return &S3CredentialService{creds: creds, users: users, usedBy: usedBy}
}

// List returns the caller's own credentials.
func (s *S3CredentialService) List(ctx context.Context) ([]dto.S3CredentialResponse, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	creds, err := s.creds.FindByOwnerID(ctx, principal.Name)
	if err != nil {
		return nil, err
	}
	out := make([]dto.S3CredentialResponse, 0, len(creds))
	for i := range creds {
		out = append(out, dto.ToS3CredentialResponse(&creds[i]))
	}
	return out, nil
}

// Get returns one credential.
func (s *S3CredentialService) Get(ctx context.Context, id string) (*dto.S3CredentialResponse, error) {
	cred, err := s.requireOwned(ctx, id)
	if err != nil {
		return nil, err
	}
	out := dto.ToS3CredentialResponse(cred)
	return &out, nil
}

// Create registers a credential owned by the calling user. The owner is taken from the
// token, and the secret access key is required.
func (s *S3CredentialService) Create(ctx context.Context, req *dto.S3CredentialRequest) (*dto.S3CredentialResponse, error) {
	owner, err := iamsvc.RequireCurrentUser(ctx, s.users)
	if err != nil {
		return nil, err
	}
	if req.SecretAccessKey == nil || strings.TrimSpace(*req.SecretAccessKey) == "" {
		return nil, httpx.BadRequest("Secret access key is required when creating an S3 credential")
	}

	cred := &model.S3Credential{
		Name:            req.Name,
		AccessKeyID:     req.AccessKeyID,
		SecretAccessKey: *req.SecretAccessKey,
		SessionToken:    ptr.NonBlank(req.SessionToken),
		OwnerID:         owner.ID,
	}
	if err := s.creds.Save(ctx, cred); err != nil {
		return nil, err
	}
	out := dto.ToS3CredentialResponse(cred)
	return &out, nil
}

// Update changes a credential.
//
// A blank or absent secret access key means "keep the stored one", as for SSH
// credentials. Two rules follow from the access key ID, secret and session token being
// one set:
//
//   - Changing the access key ID requires a new secret. Keeping the old secret under a
//     new ID would leave a credential that can never sign a request.
//   - A new secret replaces the session token too: the token is set to whatever the
//     request sends, and cleared when it sends none. A token belongs to the secret it
//     was issued with, so carrying it over would pair it with the wrong one.
//
// When no new secret is sent, a non-blank session token on its own replaces the stored
// one and a blank one keeps it.
func (s *S3CredentialService) Update(ctx context.Context, id string, req *dto.S3CredentialRequest) (*dto.S3CredentialResponse, error) {
	cred, err := s.requireOwned(ctx, id)
	if err != nil {
		return nil, err
	}

	secret := ptr.NonBlank(req.SecretAccessKey)
	if secret == nil && req.AccessKeyID != cred.AccessKeyID {
		return nil, httpx.BadRequest("Secret access key is required when changing the access key ID")
	}

	cred.Name = req.Name
	cred.AccessKeyID = req.AccessKeyID
	if secret != nil {
		cred.SecretAccessKey = *secret
		cred.SessionToken = ptr.NonBlank(req.SessionToken)
	} else if token := ptr.NonBlank(req.SessionToken); token != nil {
		cred.SessionToken = token
	}

	if err := s.creds.Save(ctx, cred); err != nil {
		return nil, err
	}
	out := dto.ToS3CredentialResponse(cred)
	return &out, nil
}

// Delete removes a credential, refusing with 409 while anything still presents it.
func (s *S3CredentialService) Delete(ctx context.Context, id string) error {
	cred, err := s.requireOwned(ctx, id)
	if err != nil {
		return err
	}

	held := int64(0)
	for _, referrer := range s.usedBy {
		n, err := referrer.CountByS3CredentialID(ctx, id)
		if err != nil {
			return err
		}
		held += n
	}
	if held > 0 {
		return httpx.Conflict("S3 credential %s is still presented by %d record(s) and cannot be deleted", id, held)
	}
	return s.creds.Delete(ctx, cred)
}

func (s *S3CredentialService) requireOwned(ctx context.Context, id string) (*model.S3Credential, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	cred, err := s.creds.FindByID(ctx, id)
	if err != nil {
		return nil, notFoundAs(err, "S3 credential not found: %s", id)
	}
	if !cred.OwnedBy(principal.Name) {
		return nil, httpx.Forbidden("Access denied: S3 credential %s belongs to another user", id)
	}
	return cred, nil
}
