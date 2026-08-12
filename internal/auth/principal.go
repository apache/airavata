// Package auth resolves bearer tokens into principals and enforces the authority
// checks that Spring expressed as @PreAuthorize.
package auth

import (
	"context"

	"github.com/apache/airavata/internal/httpx"
	"github.com/apache/airavata/internal/role"
)

// Principal is the authenticated caller.
//
// Name is the identity every service compares ownership against, and it is expected
// to equal iam.User.ID: CILogon subjects normalised to "cilogon:12345", or "root" for
// the bootstrap account.
//
// Authorities are bare role names — "ADMIN", "SUPER_ADMIN", "USER" — with no "ROLE_"
// prefix, matching the Java expressions exactly.
type Principal struct {
	Name        string
	Authorities []string
	Attributes  map[string]any
}

// HasAnyAuthority reports whether the principal holds at least one of want.
func (p *Principal) HasAnyAuthority(want ...string) bool {
	if p == nil {
		return false
	}
	for _, have := range p.Authorities {
		for _, w := range want {
			if have == w {
				return true
			}
		}
	}
	return false
}

// IsAdmin reports whether the principal holds ADMIN or SUPER_ADMIN, the pairing every
// administrative check in the Java services uses.
func (p *Principal) IsAdmin() bool {
	return p.HasAnyAuthority(string(role.Admin), string(role.SuperAdmin))
}

type principalKey struct{}

// WithPrincipal returns a context carrying p.
func WithPrincipal(ctx context.Context, p *Principal) context.Context {
	return context.WithValue(ctx, principalKey{}, p)
}

// FromContext returns the principal on ctx, or nil when the caller is anonymous.
func FromContext(ctx context.Context) *Principal {
	p, _ := ctx.Value(principalKey{}).(*Principal)
	return p
}

// RequireAuthenticated is the @PreAuthorize("isAuthenticated()") guard.
//
// An anonymous caller gets 401 rather than 403: Spring's ExceptionTranslationFilter
// sends anonymous principals to the authentication entry point, because presenting a
// token is something the caller can actually do about it.
func RequireAuthenticated(ctx context.Context) (*Principal, error) {
	p := FromContext(ctx)
	if p == nil {
		return nil, httpx.Unauthorized("Full authentication is required to access this resource")
	}
	return p, nil
}

// RequireAnyAuthority is the @PreAuthorize("hasAnyAuthority(...)") guard. An
// authenticated caller holding none of want gets 403.
func RequireAnyAuthority(ctx context.Context, want ...string) (*Principal, error) {
	p, err := RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	if !p.HasAnyAuthority(want...) {
		return nil, httpx.Forbidden("Access denied")
	}
	return p, nil
}

// RequireAdmin is shorthand for the ADMIN/SUPER_ADMIN pairing.
func RequireAdmin(ctx context.Context) (*Principal, error) {
	return RequireAnyAuthority(ctx, string(role.Admin), string(role.SuperAdmin))
}

// RequireSuperAdmin gates the user-registration path.
func RequireSuperAdmin(ctx context.Context) (*Principal, error) {
	return RequireAnyAuthority(ctx, string(role.SuperAdmin))
}

// RequireSelfOrAdmin allows the owner or any admin, and is the imperative check the
// credential, data and user services use in place of an annotation. The message is
// caller-supplied because each service phrases its own.
func RequireSelfOrAdmin(ctx context.Context, ownerID string, message string) (*Principal, error) {
	p, err := RequireAuthenticated(ctx)
	if err != nil {
		return nil, err
	}
	if p.Name == ownerID || p.IsAdmin() {
		return p, nil
	}
	return nil, httpx.Forbidden("%s", message)
}
