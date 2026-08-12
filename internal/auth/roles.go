package auth

import (
	"context"

	"github.com/apache/airavata/internal/role"
)

// RoleLookup resolves the authorities granted to an authenticated username.
//
// Authorities are looked up here rather than read out of the token, so a token cannot
// assert its own privileges.
type RoleLookup interface {
	Roles(ctx context.Context, username string) []string
}

// MockRoleLookup is the hardcoded stand-in carried over from the Java service: two
// fixed usernames are admins and everyone else is a plain user.
//
// It is a placeholder. iam.DBRoleLookup reads the user_roles table the schema already
// provides and should replace this once roles are actually administered.
type MockRoleLookup struct{}

// Roles implements RoleLookup.
func (MockRoleLookup) Roles(_ context.Context, username string) []string {
	switch username {
	case "admin", "default-admin":
		return []string{string(role.Admin)}
	default:
		return []string{string(role.User)}
	}
}
