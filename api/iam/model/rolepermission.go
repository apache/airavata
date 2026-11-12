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

package model

import (
	"gorm.io/gorm"

	"github.com/apache/airavata/internal/permission"
)

// RolePermission grants one permission to every holder of one role. The primary key is
// the (role, permission) pair, so a role holds each permission at most once.
//
// There is no foreign key to a roles table because roles are a fixed set (see
// internal/role), validated where a user is given one. There is none to a permissions
// table either: the catalogue is in code, since a handler can only ask for a permission
// the code knows about.
type RolePermission struct {
	Role       Role                  `gorm:"column:role;primaryKey;type:varchar(32)" json:"role"`
	Permission permission.Permission `gorm:"column:permission;primaryKey;type:varchar(64)" json:"permission"`
}

// TableName returns the table backing RolePermission.
func (RolePermission) TableName() string { return "role_permissions" }

// BeforeSave rejects an unrecognised role or permission, so a typo cannot be stored as a
// grant that silently never matches anything.
func (r *RolePermission) BeforeSave(*gorm.DB) error {
	if !r.Role.Valid() || !r.Permission.Valid() {
		return gorm.ErrInvalidValue
	}
	return nil
}
