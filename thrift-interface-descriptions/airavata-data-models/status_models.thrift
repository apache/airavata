/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

namespace java org.apache.airavata.model.status
namespace php Airavata.Model.Status
namespace cpp apache.airavata.model.status
namespace py apache.airavata.model.status

enum State {
    CREATED,
    VALIDATED,
    SCHEDULED,
    LAUNCHED,
    EXECUTING,
    CANCELING,
    CANCELED,
    SUSPENDED,
    COMPLETED,
    FAILED
}

/**
 * Status: A generic status object.
 *
 * state:
 *   State .
 *
 * timeOfStateChange:
 *   time the status was last updated.
 *
 * reason:
 *   User friendly reason on how the state is inferred.
 *
*/
struct Status {
    1: required State state,
    2: optional i64 timeOfStateChange,
    3: string reason
}
