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

package auth

import (
	"context"

	"github.com/apache/airavata/internal/role"
)

// RoleLookup resolves the authorities granted to an authenticated username.
//
// Authorities are looked up here rather than read out of the token, so a token cannot
// assert its own privileges.
type RoleLookup interface {
	Roles(ctx context.Context, username string) []string
}

// MockRoleLookup is the hardcoded stand-in carried over from the Java service: two
// fixed usernames are admins and everyone else is a plain user.
//
// It is a placeholder. iam/repository.DBRoleLookup reads the user_roles table the schema already
// provides and should replace this once roles are actually administered.
type MockRoleLookup struct{}

// Roles implements RoleLookup.
func (MockRoleLookup) Roles(_ context.Context, username string) []string {
	switch username {
	case "admin", "default-admin":
		return []string{string(role.Admin)}
	default:
		return []string{string(role.User)}
	}
}
