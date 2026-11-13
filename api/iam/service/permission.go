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

package service

import (
	"context"

	"github.com/apache/airavata/internal/auth"
	"github.com/apache/airavata/internal/permission"

	"github.com/apache/airavata/api/iam/dto"
)

// PermissionService lists the permissions that exist.
//
// The catalogue is the same for everyone and names nothing about any user, so any
// signed-in caller may read it; what a caller may grant from it is decided where the
// grant is made.
type PermissionService struct{}

// NewPermissionService returns a permission service.
func NewPermissionService() *PermissionService { return &PermissionService{} }

// List returns every permission with its description, sorted.
func (s *PermissionService) List(ctx context.Context) ([]dto.PermissionResponse, error) {
	if _, err := auth.RequireAuthenticated(ctx); err != nil {
		return nil, err
	}
	catalog := permission.Catalog()
	out := make([]dto.PermissionResponse, 0, len(catalog))
	for _, info := range catalog {
		out = append(out, dto.PermissionResponse{
			Permission:                string(info.Permission),
			Description:               info.Description,
			GrantableToServiceAccount: info.GrantableToServiceAccount,
		})
	}
	return out, nil
}
