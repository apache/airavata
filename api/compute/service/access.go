package service

import (
	"context"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/auth"

	model "github.com/apache/airavata/api/compute/model"
	iamrepo "github.com/apache/airavata/api/iam/repository"
)

// permission is the access level a sharing row grants.
//
// It mirrors model.ClusterPermission rather than being it, so the resolver below reads
// the same way as the data vertical's — each service converts at its boundary.
type permission string

const (
	permNone  permission = ""
	permRead  permission = "READ"
	permWrite permission = "WRITE"
)

// Allows reports whether holding p is enough to do something requiring want. WRITE
// implies READ; nothing implies WRITE.
func (p permission) Allows(want permission) bool {
	if p == permNone || want == permNone {
		return false
	}
	return p == permWrite || p == want
}

// share is one sharing row reduced to what an access decision needs: who it names and
// what it grants.
type share struct {
	subject string
	grants  permission
}

// newShare converts a stored (subject, permission) pair. A share naming nobody, or
// carrying a permission the model does not declare, grants nothing — so a column left
// at its zero value cannot be read as blanket access.
func newShare(subject string, grants model.ClusterPermission) share {
	s := share{subject: subject, grants: permission(grants)}
	if !grants.Valid() {
		s.grants = permNone
	}
	return s
}

// access resolves what the calling principal may do with a shared record.
//
// It is the same model the data storages use: strongest of ownership, a user share,
// and a group share reaching an active membership. Platform admins are treated as
// owners. "Control" — deleting a record and managing its shares — is not reachable
// through a share, because deciding who else gets access stays with the owner.
type access struct {
	members *iamrepo.GroupMemberRepository
}

// withTx binds the membership lookup to tx, for checks made from inside a transaction.
func (a access) withTx(tx *gorm.DB) access {
	return access{members: a.members.WithTx(tx)}
}

// permissionOf returns the caller's effective permission and whether they control the
// record. ownerID is nil for a record that has no owner at all, in which case only
// admins and shares reach it.
func (a access) permissionOf(ctx context.Context, ownerID *string, userShares, groupShares []share) (permission, bool, error) {
	principal, err := auth.RequireAuthenticated(ctx)
	if err != nil {
		return permNone, false, err
	}
	if principal.IsAdmin() || (ownerID != nil && *ownerID == principal.Name) {
		return permWrite, true, nil
	}

	best := permNone
	for _, s := range userShares {
		if s.subject == principal.Name {
			best = strongest(best, s.grants)
		}
	}

	// A group share reaches the caller only through an ACTIVE membership: a suspended
	// member keeps their place in the group without keeping access through it.
	if len(groupShares) > 0 {
		memberships, err := a.members.FindByUserID(ctx, principal.Name)
		if err != nil {
			return permNone, false, err
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

func strongest(have, candidate permission) permission {
	if candidate == permNone {
		return have
	}
	if candidate == permWrite || have == permNone {
		return candidate
	}
	return have
}
