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

include "../../airavata-apis/airavata_commons.thrift"

namespace java org.apache.airavata.model.workflow
namespace php Airavata.Model.Workflow
namespace cpp apache.airavata.model.workflow
namespace py airavata.model.workflow

enum ApplicationState {
    CREATED,
    VALIDATED,
    SCHEDULED,
    LAUNCHED,
    EXECUTING,
    CANCELING,
    CANCELED,
    COMPLETED,
    FAILED
}

struct ApplicationStatus {
    1:  required ApplicationState state,
    2:  optional string description,
    3:  optional i64 updatedAt
}

struct ApplicationConfig {
    1:  optional string computeResourceId,
    2:  optional string queue_name,
    3:  optional i32 nodeCount,
    4:  optional i32 coreCount,
    5:  optional i32 wallTimeLimit,
    6:  optional i32 physicalMemory
}

struct WorkflowApplication {
    1:  required string componentId,
    2:  required bool belongsToMainWorkflow,
    3:  optional string applicationInterfaceId,
    4:  optional ApplicationConfig config,
    5:  optional list<ApplicationStatus> statuses,
    6:  optional list<airavata_commons.ErrorModel> errors
    7:  optional i64 createdAt,
    8:  optional i64 updatedAt
}
