package httpx

import (
	"reflect"
	"strings"
)

// Constraints accumulates field violations, standing in for the jakarta.validation
// annotations on the Java DTOs. The messages are carried over verbatim so clients see
// the same text.
type Constraints struct {
	fields []FieldError
}

// NotBlank records a violation when value is empty or only whitespace, mirroring
// @NotBlank.
func (c *Constraints) NotBlank(field, message string, value string) {
	if strings.TrimSpace(value) == "" {
		c.Add(field, message)
	}
}

// NotBlankPtr is NotBlank for an optional field: a nil pointer is as much a violation
// as a blank string.
func (c *Constraints) NotBlankPtr(field, message string, value *string) {
	if value == nil || strings.TrimSpace(*value) == "" {
		c.Add(field, message)
	}
}

// NotNil records a violation when value is nil, mirroring @NotNull.
func (c *Constraints) NotNil(field, message string, value any) {
	if isNil(value) {
		c.Add(field, message)
	}
}

// Positive records a violation when value is nil or not greater than zero, mirroring
// @NotNull @Positive on wallTimeMinutes.
func (c *Constraints) Positive(field, message string, value *int64) {
	if value == nil || *value <= 0 {
		c.Add(field, message)
	}
}

// Email records a violation when value is present but not plausibly an address,
// mirroring @Email. Like Jakarta's default, a nil or empty value passes.
func (c *Constraints) Email(field, message string, value *string) {
	if value == nil || *value == "" {
		return
	}
	at := strings.IndexByte(*value, '@')
	if at <= 0 || at == len(*value)-1 || strings.ContainsAny(*value, " \t") {
		c.Add(field, message)
	}
}

// Add records an arbitrary violation.
func (c *Constraints) Add(field, message string) {
	c.fields = append(c.fields, FieldError{Field: field, Message: message})
}

// Nested folds the violations of a nested payload in under a prefix, mirroring @Valid
// on a nested DTO.
func (c *Constraints) Nested(prefix string, v Validator) {
	if v == nil || isNil(v) {
		return
	}
	for _, f := range v.Validate() {
		c.Add(prefix+"."+f.Field, f.Message)
	}
}

// Fields returns everything recorded so far.
func (c *Constraints) Fields() []FieldError { return c.fields }

// isNil reports whether v holds no value.
//
// Reflection rather than a type switch over the pointer types in use: a nil pointer
// wrapped in an interface is not equal to nil, so a switch silently passes every type
// it does not list — which for a DTO field like *model.ProcessType means a required
// enum with no value validates cleanly.
func isNil(v any) bool {
	if v == nil {
		return true
	}
	switch rv := reflect.ValueOf(v); rv.Kind() {
	case reflect.Ptr, reflect.Interface, reflect.Map, reflect.Slice, reflect.Func, reflect.Chan:
		return rv.IsNil()
	}
	return false
}
