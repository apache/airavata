// Package httpx holds the HTTP plumbing shared by every handler: status-carrying
// errors, JSON encoding, and the error response shape.
package httpx

import (
	"errors"
	"fmt"
	"net/http"
)

// Error is a failure that already knows its HTTP status. It is the counterpart to
// Spring's ResponseStatusException: services raise it, and the handler layer turns it
// into a response without needing to know what went wrong.
type Error struct {
	Status  int
	Message string

	// Fields carries per-field validation failures. Empty for everything else.
	Fields []FieldError

	// Err is an optional wrapped cause, kept out of the response body.
	Err error
}

// FieldError is one failed validation constraint.
type FieldError struct {
	Field   string `json:"field"`
	Message string `json:"message"`
}

func (e *Error) Error() string {
	if e.Err != nil {
		return fmt.Sprintf("%d %s: %v", e.Status, e.Message, e.Err)
	}
	return fmt.Sprintf("%d %s", e.Status, e.Message)
}

func (e *Error) Unwrap() error { return e.Err }

// NotFound reports a missing resource. Message is phrased as the Java services
// phrase it, e.g. "Cluster not found: abc123".
func NotFound(format string, args ...any) *Error {
	return &Error{Status: http.StatusNotFound, Message: fmt.Sprintf(format, args...)}
}

// BadRequest reports a malformed or semantically invalid request.
func BadRequest(format string, args ...any) *Error {
	return &Error{Status: http.StatusBadRequest, Message: fmt.Sprintf(format, args...)}
}

// Conflict reports a request that collides with existing state — the two cases here
// are deleting an SSH key still in use and deleting a template that still has
// deployments.
func Conflict(format string, args ...any) *Error {
	return &Error{Status: http.StatusConflict, Message: fmt.Sprintf(format, args...)}
}

// Forbidden reports an authenticated caller lacking the required authority. It
// corresponds to Spring's AccessDeniedException for a non-anonymous principal.
func Forbidden(format string, args ...any) *Error {
	return &Error{Status: http.StatusForbidden, Message: fmt.Sprintf(format, args...)}
}

// Unauthorized reports a missing or unusable token. Spring returns this rather than
// 403 when the principal is anonymous, because the caller can fix it by presenting
// credentials.
func Unauthorized(format string, args ...any) *Error {
	return &Error{Status: http.StatusUnauthorized, Message: fmt.Sprintf(format, args...)}
}

// Invalid reports failed request-body validation.
func Invalid(fields []FieldError) *Error {
	return &Error{
		Status:  http.StatusBadRequest,
		Message: "Validation failed",
		Fields:  fields,
	}
}

// StatusOf returns the HTTP status err should be reported with, defaulting to 500 for
// anything that is not an *Error.
func StatusOf(err error) int {
	var e *Error
	if errors.As(err, &e) {
		return e.Status
	}
	return http.StatusInternalServerError
}
