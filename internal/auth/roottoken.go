package auth

import (
	"crypto/subtle"
	"fmt"

	"github.com/google/uuid"

	"github.com/apache/airavata/internal/role"
)

// RootUsername is the principal name the bootstrap account authenticates as. Note
// that it is also the user id ownership checks compare against, so acting on owned
// resources as root requires a users row with this id.
const RootUsername = "root"

// RootTokenProvider holds the bootstrap Super Admin token.
//
// It exists so a fresh deployment has some way in before any real identity provider
// is wired up. It is meant for initial setup and testing; production deployments
// should disable it and authenticate through CILogon.
type RootTokenProvider struct {
	token string
}

// NewRootTokenProvider returns a provider using configured, or a fresh random token
// when configured is empty.
func NewRootTokenProvider(configured string) *RootTokenProvider {
	if configured == "" {
		configured = uuid.NewString()
	}
	return &RootTokenProvider{token: configured}
}

// Token returns the active root token.
func (p *RootTokenProvider) Token() string {
	if p == nil {
		return ""
	}
	return p.token
}

// Matches reports whether token is the root token, comparing in constant time so the
// token cannot be recovered by timing repeated guesses.
func (p *RootTokenProvider) Matches(token string) bool {
	if p == nil || p.token == "" || token == "" {
		return false
	}
	return subtle.ConstantTimeCompare([]byte(token), []byte(p.token)) == 1
}

// Principal returns the Super Admin principal the root token authenticates as.
func (p *RootTokenProvider) Principal() *Principal {
	return &Principal{
		Name:        RootUsername,
		Authorities: []string{string(role.SuperAdmin)},
		Attributes: map[string]any{
			"sub":      RootUsername,
			"username": RootUsername,
			"active":   true,
		},
	}
}

// Banner is the startup notice announcing the token, matching the Java service's
// behaviour of printing it where an operator will see it.
func (p *RootTokenProvider) Banner() string {
	return fmt.Sprintf(
		"\n========================================\nROOT ACCOUNT TOKEN (Super Admin):\n%s\n========================================\n",
		p.token)
}
