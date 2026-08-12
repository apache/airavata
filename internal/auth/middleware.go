package auth

import (
	"errors"
	"net/http"
	"strings"

	"github.com/apache/airavata/internal/httpx"
)

// Middleware resolves the bearer token, if any, onto the request context.
//
// It deliberately does not reject anonymous requests. The Java filter chain permits
// every request and leaves enforcement to the method-level guards, so an endpoint
// with no authority requirement stays reachable without a token. What it does reject
// is a token that is present but unusable: a caller who supplies a bad token gets 401
// rather than being silently downgraded to anonymous, which would otherwise turn a
// typo'd token into a confusing 403 further down.
func Middleware(introspector Introspector) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			token := bearerToken(r)
			if token == "" {
				next.ServeHTTP(w, r)
				return
			}

			principal, err := introspector.Introspect(r.Context(), token)
			if err != nil {
				if errors.Is(err, ErrInvalidToken) {
					w.Header().Set("WWW-Authenticate", `Bearer error="invalid_token"`)
					httpx.WriteError(w, r, httpx.Unauthorized("Invalid bearer token"))
					return
				}
				// The identity provider is unreachable or misbehaving. That is not the
				// caller's fault, so it must not be reported as an auth failure.
				httpx.WriteError(w, r, &httpx.Error{
					Status:  http.StatusBadGateway,
					Message: "Unable to validate bearer token",
					Err:     err,
				})
				return
			}

			next.ServeHTTP(w, r.WithContext(WithPrincipal(r.Context(), principal)))
		})
	}
}

func bearerToken(r *http.Request) string {
	header := r.Header.Get("Authorization")
	if header == "" {
		return ""
	}
	scheme, token, found := strings.Cut(header, " ")
	if !found || !strings.EqualFold(scheme, "Bearer") {
		return ""
	}
	return strings.TrimSpace(token)
}
