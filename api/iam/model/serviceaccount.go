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

// ServiceAccount is the token side of a non-human user.
//
// The account itself is an ordinary users row (AuthMethod SERVICE_ACCOUNT), which is
// what lets it own resources, receive shares and join groups with no special casing
// anywhere else. This row holds only what a person does not have: the digest of the
// one active token, when it was issued and when it stops working.
//
// There is one token per account. Renewing moves its expiry; regenerating replaces
// it, which revokes the old one in the same write.
type ServiceAccount struct {
	UserID string `gorm:"column:user_id;primaryKey;type:varchar(255)" json:"userId"`

	Description *string `gorm:"column:description;type:varchar(1024)" json:"description,omitempty"`

	// CreatedBy is the admin who created the account. It is a plain record, not a
	// reference: the account outlives its creator's user row.
	CreatedBy string `gorm:"column:created_by;type:varchar(255);not null" json:"createdBy"`

	// TokenHash is the hex SHA-256 of the token. The token itself is never stored.
	TokenHash      string `gorm:"column:token_hash;type:varchar(64);not null;uniqueIndex:uk_service_account_token_hash" json:"-"`
	TokenIssuedAt  int64  `gorm:"column:token_issued_at;not null" json:"tokenIssuedAt"`
	TokenExpiresAt int64  `gorm:"column:token_expires_at;not null" json:"tokenExpiresAt"`

	// Permissions is what the account is allowed to do beyond acting on its own
	// records. Owned by the account: deleting it deletes the rows. Services write them
	// through the permission set operations, not by mutating this slice.
	Permissions []ServiceAccountPermission `gorm:"foreignKey:UserID;references:UserID;constraint:OnDelete:CASCADE,OnUpdate:CASCADE" json:"-"`
}

// TableName pins the table name.
func (ServiceAccount) TableName() string { return "service_accounts" }

// ExpiredAt reports whether the token has expired at nowMillis.
func (s *ServiceAccount) ExpiredAt(nowMillis int64) bool {
	return nowMillis >= s.TokenExpiresAt
}
