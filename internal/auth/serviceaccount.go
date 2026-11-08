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

package auth

import (
	"context"
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"encoding/hex"
	"fmt"
	"strings"

	"github.com/apache/airavata/internal/role"
)

// ServiceAccountIDPrefix namespaces service account user ids. Every service account
// is a users row whose id is this prefix followed by the account's name, so a
// service account can own resources, be shared with and join groups exactly as a
// person does, and its id can never collide with a CILogon subject.
const ServiceAccountIDPrefix = "sa:"

// ServiceAccountTokenPrefix marks a bearer token as a service account token.
//
// The prefix is what lets the introspector route the token to the local database
// instead of CILogon: an Airavata-issued secret must never be sent to a third party's
// introspection endpoint. It also makes a leaked token recognisable to secret
// scanners.
const ServiceAccountTokenPrefix = "airavata_sa_"

// serviceAccountTokenBytes is the entropy of a generated token. 32 random bytes put
// guessing out of reach, which is also why a plain SHA-256 digest is a sufficient
// way to store it: a slow password hash only matters for low-entropy secrets.
const serviceAccountTokenBytes = 32

// ServiceAccountAuthority is the only authority a service account ever holds.
//
// It is fixed here rather than looked up, so no row in user_roles — however it got
// there — can make a service account an administrator.
const ServiceAccountAuthority = role.User

// ServiceAccountResolver resolves a service account token to its principal.
//
// It returns ErrInvalidToken for a token that is unknown, expired, or belongs to an
// account that is not active, and any other error when the lookup itself failed.
type ServiceAccountResolver interface {
	ResolveServiceAccount(ctx context.Context, token string) (*Principal, error)
}

// IsServiceAccountToken reports whether token is shaped like a service account token.
func IsServiceAccountToken(token string) bool {
	return strings.HasPrefix(token, ServiceAccountTokenPrefix)
}

// IsServiceAccountID reports whether a user id names a service account.
func IsServiceAccountID(userID string) bool {
	return strings.HasPrefix(userID, ServiceAccountIDPrefix)
}

// GenerateServiceAccountToken returns a new random token and the digest to store for
// it. The token itself is never persisted; it is shown to the caller once.
func GenerateServiceAccountToken() (token, hash string, err error) {
	buf := make([]byte, serviceAccountTokenBytes)
	if _, err := rand.Read(buf); err != nil {
		return "", "", fmt.Errorf("generate service account token: %w", err)
	}
	token = ServiceAccountTokenPrefix + base64.RawURLEncoding.EncodeToString(buf)
	return token, HashServiceAccountToken(token), nil
}

// HashServiceAccountToken returns the hex SHA-256 digest a token is stored and looked
// up by.
func HashServiceAccountToken(token string) string {
	sum := sha256.Sum256([]byte(token))
	return hex.EncodeToString(sum[:])
}

// ServiceAccountPrincipal builds the principal a service account authenticates as.
// Its authorities are always exactly USER.
func ServiceAccountPrincipal(userID string) *Principal {
	return &Principal{
		Name:        userID,
		Authorities: []string{string(ServiceAccountAuthority)},
		Attributes: map[string]any{
			"sub":             userID,
			"username":        userID,
			"active":          true,
			"service_account": true,
		},
	}
}

// IsServiceAccount reports whether the principal is a service account.
func (p *Principal) IsServiceAccount() bool {
	return p != nil && IsServiceAccountID(p.Name)
}
