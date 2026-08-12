package httpx

import (
	"encoding/json"
	"errors"
	"log/slog"
	"net/http"
)

// errorBody is the JSON shape of every failure response.
type errorBody struct {
	Status  int          `json:"status"`
	Error   string       `json:"error"`
	Message string       `json:"message"`
	Fields  []FieldError `json:"fieldErrors,omitempty"`
}

// WriteJSON writes v as JSON with the given status. A nil v (or status 204) writes no
// body, which is what the delete endpoints need.
func WriteJSON(w http.ResponseWriter, status int, v any) {
	if v == nil || status == http.StatusNoContent {
		w.WriteHeader(status)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	if err := json.NewEncoder(w).Encode(v); err != nil {
		// The status line is already sent, so this can only be logged.
		slog.Error("write json response", "error", err)
	}
}

// WriteError renders err as a JSON failure. Anything that is not an *Error becomes a
// bare 500: the message of an unexpected error may name internal state, so it is
// logged rather than returned to the caller.
func WriteError(w http.ResponseWriter, r *http.Request, err error) {
	var e *Error
	if !errors.As(err, &e) {
		slog.Error("unhandled error",
			"method", r.Method, "path", r.URL.Path, "error", err)
		e = &Error{Status: http.StatusInternalServerError, Message: "Internal server error"}
	}
	if e.Status >= http.StatusInternalServerError {
		slog.Error("server error",
			"method", r.Method, "path", r.URL.Path, "error", err)
	}

	WriteJSON(w, e.Status, errorBody{
		Status:  e.Status,
		Error:   http.StatusText(e.Status),
		Message: e.Message,
		Fields:  e.Fields,
	})
}

// DecodeJSON reads the request body into dst.
//
// Unknown fields are ignored, matching Spring Boot's Jackson defaults
// (FAIL_ON_UNKNOWN_PROPERTIES is off), so a client sending an extra key gets the same
// acceptance it got from the Java service.
func DecodeJSON(r *http.Request, dst any) error {
	if r.Body == nil {
		return BadRequest("Request body is required")
	}
	if err := json.NewDecoder(r.Body).Decode(dst); err != nil {
		return BadRequest("Malformed JSON request body")
	}
	return nil
}

// Validator is implemented by request payloads that carry constraints.
type Validator interface {
	Validate() []FieldError
}

// Bind decodes the request body into dst and validates it when dst supports it.
func Bind(r *http.Request, dst any) error {
	if err := DecodeJSON(r, dst); err != nil {
		return err
	}
	if v, ok := dst.(Validator); ok {
		if fields := v.Validate(); len(fields) > 0 {
			return Invalid(fields)
		}
	}
	return nil
}
