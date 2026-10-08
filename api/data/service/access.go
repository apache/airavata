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

// Package service holds the data vertical's business rules: registered datasets, the
// storages they live on and the virtual datasets assembled out of them, all reached
// through ownership and sharing rules rather than through platform roles.
//
// The rule itself lives in api/sharing/service, which every vertical shares. What is
// here is the part only this vertical can answer: which record an id names, who owns
// it, and which shares apply to it.
package service

import (
	"errors"

	"gorm.io/gorm"

	"github.com/apache/airavata/internal/httpx"

	sharingsvc "github.com/apache/airavata/api/sharing/service"
)

// notFoundAs converts a missing-row error into a 404 and leaves anything else alone.
func notFoundAs(err error, format string, args ...any) error {
	if errors.Is(err, gorm.ErrRecordNotFound) {
		return httpx.NotFound(format, args...)
	}
	return err
}

// access is the shared resolver, aliased so the access structs below read the same as
// they did when this vertical owned a copy of it.
type access = sharingsvc.Access
