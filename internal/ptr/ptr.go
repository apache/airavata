// Package ptr provides helpers for the pointer fields used throughout the entity
// model. Nullable database columns are modelled as pointers so that "absent" stays
// distinguishable from the zero value — a distinction the Java entities got for free
// from boxed types (Integer, Long, Boolean) and nullable Strings.
package ptr

// To returns a pointer to v. Useful for literals: ptr.To(42), ptr.To("gpu").
func To[T any](v T) *T {
	return &v
}

// From dereferences p, returning the zero value of T when p is nil.
func From[T any](p *T) T {
	if p == nil {
		var zero T
		return zero
	}
	return *p
}

// FromOr dereferences p, returning def when p is nil.
func FromOr[T any](p *T, def T) T {
	if p == nil {
		return def
	}
	return *p
}

// NonBlank returns nil when s points at an empty string, and s otherwise. The Java
// services normalise blank form fields to null before mapping so that an empty field
// means "leave unchanged" rather than "erase the stored value"; this is that rule.
func NonBlank(s *string) *string {
	if s == nil || *s == "" {
		return nil
	}
	return s
}
