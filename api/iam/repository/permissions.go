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

package repository

import (
	"context"
	"log/slog"
	"sync"
	"time"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/permission"
	"github.com/apache/airavata/internal/role"

	model "github.com/apache/airavata/api/iam/model"
)

// DefaultPermissionTTL is how long a role's permissions are remembered. It bounds how
// stale a grant can be on a server that did not itself make the change.
const DefaultPermissionTTL = time.Minute

// PermissionResolver resolves roles to permissions from the role_permissions table.
//
// The cache is keyed by role rather than by user. There are few roles and many users,
// so it stays small however many people are signed in, and a change to a role is one
// Invalidate rather than a hunt for every user holding it.
//
// A change made through this process calls Invalidate and takes effect at once. A
// change made by another server instance, or directly in the database, is picked up
// when the cached entry expires.
type PermissionResolver struct {
	db  *gorm.DB
	ttl time.Duration
	now func() time.Time

	mu    sync.RWMutex
	cache map[role.Role]cachedPermissions
}

type cachedPermissions struct {
	perms   permission.Set
	expires time.Time
}

// NewPermissionResolver returns a resolver over db. A ttl of zero or less means
// DefaultPermissionTTL.
func NewPermissionResolver(db *gorm.DB, ttl time.Duration) *PermissionResolver {
	if ttl <= 0 {
		ttl = DefaultPermissionTTL
	}
	return &PermissionResolver{
		db:    db,
		ttl:   ttl,
		now:   time.Now,
		cache: make(map[role.Role]cachedPermissions),
	}
}

// Permissions implements auth.PermissionResolver: the union of what every authority
// grants. An authority that is not a recognised role grants nothing.
//
// SUPER_ADMIN holds every permission without a lookup, so the bootstrap account keeps
// working whatever state the table is in.
func (r *PermissionResolver) Permissions(ctx context.Context, authorities []string) permission.Set {
	out := permission.Set{}
	for _, a := range authorities {
		ro := role.Role(a)
		if !ro.Valid() {
			continue
		}
		if ro == role.SuperAdmin {
			for _, p := range permission.All() {
				out[p] = struct{}{}
			}
			continue
		}
		for p := range r.forRole(ctx, ro) {
			out[p] = struct{}{}
		}
	}
	return out
}

// Invalidate forgets every cached role, so the next request reads the table again. Call
// it after changing role_permissions.
func (r *PermissionResolver) Invalidate() {
	r.mu.Lock()
	r.cache = make(map[role.Role]cachedPermissions)
	r.mu.Unlock()
}

func (r *PermissionResolver) forRole(ctx context.Context, ro role.Role) permission.Set {
	r.mu.RLock()
	hit, ok := r.cache[ro]
	r.mu.RUnlock()
	if ok && r.now().Before(hit.expires) {
		return hit.perms
	}

	var rows []model.RolePermission
	if err := r.db.WithContext(ctx).Where("role = ?", string(ro)).Find(&rows).Error; err != nil {
		// Fail closed, and do not cache the failure: the next request should try again
		// rather than deny for a whole TTL because of one bad moment.
		slog.Error("failed to look up role permissions; granting none",
			"role", ro, "error", err)
		return nil
	}
	perms := make(permission.Set, len(rows))
	for _, row := range rows {
		// A row naming a permission this build does not define (a downgrade, a manual
		// insert) cannot match any check, so it is dropped rather than carried.
		if row.Permission.Valid() {
			perms[row.Permission] = struct{}{}
		}
	}

	r.mu.Lock()
	r.cache[ro] = cachedPermissions{perms: perms, expires: r.now().Add(r.ttl)}
	r.mu.Unlock()
	return perms
}
