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
    1:  optional string id,
    2:  required ApplicationState state,
    3:  optional string description,
    4:  optional i64 updatedAt
}

struct WorkflowApplication {
    1:  required string id,
    2:  required bool belongsToMainWorkflow,
    3:  optional string applicationInterfaceId,
    4:  optional string computeResourceId,
    5:  optional string queueName,
    6:  optional i32 nodeCount,
    7:  optional i32 coreCount,
    8:  optional i32 wallTimeLimit,
    9:  optional i32 physicalMemory
    10: optional list<ApplicationStatus> statuses,
    11: optional list<airavata_commons.ErrorModel> errors
    12: optional i64 createdAt,
    13: optional i64 updatedAt
}
