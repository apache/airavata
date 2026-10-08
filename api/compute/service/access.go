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
	sharingsvc "github.com/apache/airavata/api/sharing/service"
)

// access is the shared resolver, aliased so the access structs in this package read
// the same as they did when compute owned a copy of it.
//
// That copy is gone. It had its own permission type, its own share struct and its own
// conversion at the boundary, all of which existed only because the sharing rows lived
// in compute's own tables. They live in one table now, so the rule is read from one
// place.
type access = sharingsvc.Access
