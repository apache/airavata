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

// Package service holds the data vertical's business rules: registered datasets and
// the storages they live on, both reached through ownership and sharing rules rather
// than through platform roles.
package service

import (
	"context"
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/httpx"

	model "github.com/apache/airavata/api/data/model"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// notFoundAs converts a missing-row error into a 404 and leaves anything else alone.
func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}

// share is one sharing row reduced to what an access decision needs: who it names and
// what it grants.
type share struct {
	subject string
	grants  model.AccessPermission
}

// newShare converts a stored (subject, permission) pair. A share with no subject or no
// permission grants nothing, so an unset column cannot be read as blanket access.
func newShare(subject *string, grants *string) share {
	s := share{}
	if subject != nil {
		s.subject = *subject
	}
	if grants != nil {
		s.grants = model.AccessPermission(*grants)
	}
	if s.grants != model.AccessPermissionRead && s.grants != model.AccessPermissionWrite {
		s.grants = model.AccessPermissionNone
	}
	return s
}

// access resolves what the calling principal may do with a shared record.
//
// It is the same model the cluster configs use: strongest of ownership, a
// user share, and a group share reaching an active membership. Platform admins are
// treated as owners. "Control" — deleting a record and managing its shares — is not
// reachable through a share, because deciding who else gets access stays with the
// owner.
type access struct {
	members *iamrepo.GroupMemberRepository
}

// withTx binds the membership lookup to tx, for checks made from inside a transaction.
func (a access) withTx(tx *gorm.DB) access {
	return access{members: a.members.WithTx(tx)}
}

// permissionOf returns the caller's effective permission and whether they control the
// record. ownerID is nil for records that have no owner at all — a storage — in which
// case only admins and shares reach it.
// Returns the effective access permission for the caller, whether they control the record, and any error encountered
func (a access) permissionOf(ctx context.Context, ownerID *string, userShares, groupShares []share) (model.AccessPermission, bool, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return model.AccessPermissionNone, false, err
	}
	if principal.IsAdmin() || (ownerID != nil && *ownerID == principal.Name) {
		return model.AccessPermissionWrite, true, nil
	}

	best := model.AccessPermissionNone
	for _, s := range userShares {
		if s.subject == principal.Name {
			best = strongest(best, s.grants)
		}
	}

	if len(groupShares) > 0 {
		memberships, err := a.members.FindByUserID(ctx, principal.Name)
		if err != nil {
			return model.AccessPermissionNone, false, err
		}
		active := make(map[string]bool, len(memberships))
		for i := range memberships {
			if memberships[i].IsActive() {
				active[memberships[i].GroupID] = true
			}
		}
		for _, s := range groupShares {
			if active[s.subject] {
				best = strongest(best, s.grants)
			}
		}
	}

	return best, false, nil
}

func strongest(have, candidate model.AccessPermission) model.AccessPermission {
	if candidate == model.AccessPermissionNone {
		return have
	}
	if candidate == model.AccessPermissionWrite || have == model.AccessPermissionNone {
		return candidate
	}
	return have
}
