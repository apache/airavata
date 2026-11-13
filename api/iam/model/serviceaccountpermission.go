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

// ServiceAccountPermission grants one permission to one service account. The primary
// key is the (user_id, permission) pair, so an account holds each permission at most
// once.
//
// A service account's permissions are exactly these rows. It holds no role that grants
// any, so granting a permission to a role never reaches a service account, and each
// account can differ from the next. The rows belong to the account and go with it.
type ServiceAccountPermission struct {
	UserID     string                `gorm:"column:user_id;primaryKey;type:varchar(255)" json:"userId"`
	Permission permission.Permission `gorm:"column:permission;primaryKey;type:varchar(64)" json:"permission"`
}

// TableName returns the table backing ServiceAccountPermission.
func (ServiceAccountPermission) TableName() string { return "service_account_permissions" }

// BeforeSave rejects a permission that does not exist or that no service account may
// hold, so neither can be stored by any path, including one that skips the service.
func (p *ServiceAccountPermission) BeforeSave(*gorm.DB) error {
	if !p.Permission.GrantableToServiceAccount() {
		return gorm.ErrInvalidValue
	}
	return nil
}
