package auth

import (
	"context"
	"encoding/json"
	"fmt"
	"log/slog"
	"net/http"
	"net/url"
	"strings"
	"time"
)

// Introspector validates a bearer token and resolves it to a principal.
type Introspector interface {
	Introspect(ctx context.Context, token string) (*Principal, error)
}

// ErrInvalidToken reports a token that introspection rejected.
var ErrInvalidToken = fmt.Errorf("invalid bearer token")

// CILogonIntrospector validates tokens against CILogon.
//
// CILogon access tokens for standard OAuth clients are opaque strings rather than
// self-contained JWTs, so they cannot be verified locally against a JWK set — each
// one is checked by calling CILogon's RFC 7662 introspection endpoint. Introspection
// returns only a small fixed claim set, so profile attributes come from a second call
// to userinfo with the same token.
type CILogonIntrospector struct {
	HTTPClient       *http.Client
	IntrospectionURI string
	UserInfoURI      string
	ClientID         string
	ClientSecret     string
	Roles            RoleLookup

	// Root, when set, short-circuits introspection for the bootstrap token.
	Root *RootTokenProvider
}

// NewCILogonIntrospector builds an introspector with a bounded HTTP client, so a
// hanging identity provider cannot pin request goroutines indefinitely.
func NewCILogonIntrospector(introspectionURI, userInfoURI, clientID, clientSecret string, roles RoleLookup, root *RootTokenProvider) *CILogonIntrospector {
	return &CILogonIntrospector{
		HTTPClient:       &http.Client{Timeout: 10 * time.Second},
		IntrospectionURI: introspectionURI,
		UserInfoURI:      userInfoURI,
		ClientID:         clientID,
		ClientSecret:     clientSecret,
		Roles:            roles,
		Root:             root,
	}
}

// Introspect implements Introspector.
func (c *CILogonIntrospector) Introspect(ctx context.Context, token string) (*Principal, error) {
	if c.Root.Matches(token) {
		return c.Root.Principal(), nil
	}

	attributes, err := c.introspect(ctx, token)
	if err != nil {
		return nil, err
	}

	// Introspection claims win on conflict; userinfo only fills in gaps.
	for k, v := range c.userInfo(ctx, token) {
		if _, exists := attributes[k]; !exists {
			attributes[k] = v
		}
	}

	username := NormalizeUsername(firstString(attributes, "username", "preferred_username", "sub"))
	if username == "" {
		return nil, ErrInvalidToken
	}

	return &Principal{
		Name:        username,
		Authorities: c.Roles.Roles(ctx, username),
		Attributes:  attributes,
	}, nil
}

func (c *CILogonIntrospector) introspect(ctx context.Context, token string) (map[string]any, error) {
	form := url.Values{"token": {token}}
	req, err := http.NewRequestWithContext(ctx, http.MethodPost, c.IntrospectionURI, strings.NewReader(form.Encode()))
	if err != nil {
		return nil, fmt.Errorf("build introspection request: %w", err)
	}
	req.Header.Set("Content-Type", "application/x-www-form-urlencoded")
	req.Header.Set("Accept", "application/json")
	req.SetBasicAuth(c.ClientID, c.ClientSecret)

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("call introspection endpoint: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("%w: introspection returned %s", ErrInvalidToken, resp.Status)
	}

	var claims map[string]any
	if err := json.NewDecoder(resp.Body).Decode(&claims); err != nil {
		return nil, fmt.Errorf("decode introspection response: %w", err)
	}
	if active, _ := claims["active"].(bool); !active {
		return nil, ErrInvalidToken
	}
	return claims, nil
}

// userInfo fetches profile claims. A failure here is not fatal — the token is already
// known to be valid, and the profile only enriches the principal — so it degrades to
// no extra claims, as the Java client did.
func (c *CILogonIntrospector) userInfo(ctx context.Context, token string) map[string]any {
	req, err := http.NewRequestWithContext(ctx, http.MethodGet, c.UserInfoURI, nil)
	if err != nil {
		return nil
	}
	req.Header.Set("Authorization", "Bearer "+token)
	req.Header.Set("Accept", "application/json")

	resp, err := c.HTTPClient.Do(req)
	if err != nil {
		slog.Warn("failed to fetch CILogon userinfo; proceeding without profile claims", "error", err)
		return nil
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		slog.Warn("CILogon userinfo returned a non-OK status; proceeding without profile claims",
			"status", resp.Status)
		return nil
	}
	var claims map[string]any
	if err := json.NewDecoder(resp.Body).Decode(&claims); err != nil {
		slog.Warn("failed to decode CILogon userinfo; proceeding without profile claims", "error", err)
		return nil
	}
	return claims
}

// NormalizeUsername reduces a CILogon identifier to the form stored as iam/model.User.ID.
//
// CILogon sometimes reports usernames as full URIs like
// "http://cilogon.org/serverE/users/12345"; those collapse to "cilogon:12345" so that
// a user is looked up the same way no matter which claim carried the identity.
func NormalizeUsername(username string) string {
	if !strings.HasPrefix(username, "http://cilogon.org") {
		return username
	}
	if i := strings.LastIndexByte(username, '/'); i >= 0 && i < len(username)-1 {
		return "cilogon:" + username[i+1:]
	}
	return username
}

func firstString(m map[string]any, keys ...string) string {
	for _, k := range keys {
		if s, ok := m[k].(string); ok && s != "" {
			return s
		}
	}
	return ""
}
