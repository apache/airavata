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

package client

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"reflect"
	"strings"
	"time"

	openapi_types "github.com/oapi-codegen/runtime/types"
)

// TokenFunc returns the bearer token for the next request.
type TokenFunc func(ctx context.Context) (string, error)

// APIError is a non-2xx answer from airavata-server, carrying its error envelope.
type APIError struct {
	Status      int
	Message     string
	FieldErrors []FieldError
}

func (e *APIError) Error() string {
	msg := fmt.Sprintf("airavata-server returned %d: %s", e.Status, e.Message)
	for _, f := range e.FieldErrors {
		msg += fmt.Sprintf("; %s: %s", f.Field, f.Message)
	}
	return msg
}

// IsStatus reports whether err is an APIError with the given HTTP status.
func IsStatus(err error, status int) bool {
	var apiErr *APIError
	return errors.As(err, &apiErr) && apiErr.Status == status
}

// New builds a client for baseURL that authenticates every request with token.
func New(baseURL string, token TokenFunc, httpClient *http.Client) (*ClientWithResponses, error) {
	if httpClient == nil {
		httpClient = &http.Client{Timeout: 60 * time.Second}
	}
	return NewClientWithResponses(strings.TrimRight(baseURL, "/"),
		WithHTTPClient(httpClient),
		WithRequestEditorFn(func(ctx context.Context, req *http.Request) error {
			if token == nil {
				return nil
			}
			t, err := token(ctx)
			if err != nil {
				return err
			}
			req.Header.Set("Authorization", "Bearer "+t)
			return nil
		}))
}

type response interface {
	StatusCode() int
}

// JSON returns the decoded 2xx body of type T (the JSON200/JSON201 field) or an *APIError.
func JSON[T any](r response, err error) (*T, error) {
	if err != nil {
		return nil, err
	}
	status := r.StatusCode()
	if status < 200 || status > 299 {
		return nil, apiError(status, bodyOf(r))
	}
	v := reflect.ValueOf(r)
	if v.Kind() == reflect.Pointer {
		v = v.Elem()
	}
	if f := v.FieldByName(fmt.Sprintf("JSON%d", status)); f.IsValid() && !f.IsNil() {
		if t, ok := f.Interface().(*T); ok {
			return t, nil
		}
	}
	return nil, fmt.Errorf("airavata-server returned %d with an unexpected body", status)
}

// NoContent returns nil for a 2xx response and an *APIError otherwise.
func NoContent(r response, err error) error {
	if err != nil {
		return err
	}
	if status := r.StatusCode(); status < 200 || status > 299 {
		return apiError(status, bodyOf(r))
	}
	return nil
}

func apiError(status int, body []byte) error {
	var env Error
	if json.Unmarshal(body, &env) == nil && env.Message != "" {
		e := &APIError{Status: status, Message: env.Message}
		if env.FieldErrors != nil {
			e.FieldErrors = *env.FieldErrors
		}
		return e
	}
	return &APIError{Status: status, Message: http.StatusText(status)}
}

// The generated responses all carry the raw body in a Body field.
func bodyOf(r any) []byte {
	v := reflect.ValueOf(r)
	if v.Kind() == reflect.Pointer {
		v = v.Elem()
	}
	if v.Kind() != reflect.Struct {
		return nil
	}
	f := v.FieldByName("Body")
	if !f.IsValid() || f.Kind() != reflect.Slice {
		return nil
	}
	b, _ := f.Interface().([]byte)
	return b
}

// ParseUUID parses s as an API id, naming field in the error.
func ParseUUID(field, s string) (openapi_types.UUID, error) {
	var id openapi_types.UUID
	if err := id.UnmarshalText([]byte(strings.TrimSpace(s))); err != nil {
		return id, fmt.Errorf("%s %q is not a valid id; take ids from the list tools", field, s)
	}
	return id, nil
}
