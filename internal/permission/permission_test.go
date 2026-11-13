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

package permission_test

import (
	"testing"

	"github.com/apache/airavata/internal/permission"
)

// An administrator picks permissions from their descriptions, so none may be missing.
func TestEveryPermissionIsDescribed(t *testing.T) {
	for _, info := range permission.Catalog() {
		if info.Description == "" {
			t.Errorf("%s has no description", info.Permission)
		}
	}
	if got, want := len(permission.Catalog()), len(permission.All()); got != want {
		t.Errorf("catalogue has %d entries, want %d", got, want)
	}
	if permission.Permission("bogus:x").Description() != "" {
		t.Error("an undefined permission has a description")
	}
}

func TestServiceAccountGrantability(t *testing.T) {
	for _, p := range []permission.Permission{permission.AdminManage, permission.UserUpdateAny, permission.ServiceAccountManage, "bogus:x"} {
		if p.GrantableToServiceAccount() {
			t.Errorf("%s is grantable to a service account", p)
		}
	}
	for _, p := range []permission.Permission{permission.ProcessList, permission.UserCreate, permission.UserSetStatus} {
		if !p.GrantableToServiceAccount() {
			t.Errorf("%s is not grantable to a service account", p)
		}
	}
}
