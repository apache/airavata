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

	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/permission"
)

// PermissionResolver turns a caller into the permissions they hold: what their
// authorities (role names) grant, or for a service account what has been granted to
// that account itself.
//
// An implementation must not fail open. When it cannot tell what a role grants it
// returns fewer permissions, never more, so a fault in the lookup denies rather than
// allows.
type PermissionResolver interface {
	Permissions(ctx context.Context, name string, authorities []string) permission.Set
}

// Option configures Middleware.
type Option func(*middlewareConfig)

type middlewareConfig struct {
	permissions PermissionResolver
}

// WithPermissions makes Middleware resolve each authenticated principal's permissions
// onto the principal, so the guards below can answer from it. Without it, no principal
// holds any permission.
func WithPermissions(r PermissionResolver) Option {
	return func(c *middlewareConfig) { c.permissions = r }
}

// Can reports whether the principal holds permission p. A nil principal, an anonymous
// caller, holds none.
func (p *Principal) Can(perm permission.Permission) bool {
	return p != nil && p.Permissions.Has(perm)
}

// RequirePermission is the guard for an action gated by a permission. Handlers and
// services use it instead of naming the roles that happen to grant the permission.
//
// An anonymous caller gets 401 and an authenticated one without the permission gets
// 403, as RequireAnyAuthority does.
func RequirePermission(ctx context.Context, perm permission.Permission) (*Principal, error) {
	p, err := RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	if !p.Can(perm) {
		return nil, httpx.Forbidden("Access denied")
	}
	return p, nil
}

// RequireSelfOrPermission allows the record's owner, or any caller holding perm. It is
// the imperative check the user service uses where an annotation cannot see the owner.
// The message is caller-supplied because each service phrases its own.
func RequireSelfOrPermission(ctx context.Context, ownerID string, perm permission.Permission, message string) (*Principal, error) {
	p, err := RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	if p.Name == ownerID || p.Can(perm) {
		return p, nil
	}
	return nil, httpx.Forbidden("%s", message)
}
