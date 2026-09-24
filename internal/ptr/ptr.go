/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
 */

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
