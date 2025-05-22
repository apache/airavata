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
include "../app-catalog-models/application_io_models.thrift"

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
    2:  optional string processId,
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

struct DataBlock{
    1:  required string id,
    2:  optional string value,
    3:  optional application_io_models.DataType type,
    4:  optional i64 createdAt,
    5:  optional i64 updatedAt,
}

enum ComponentType {
    APPLICATION,
    HANDLER
}

struct WorkflowConnection {
    1:  required string id = airavata_commons.DEFAULT_ID,
    2:  optional DataBlock dataBlock,
    3:  required ComponentType fromType
    4:  required string fromId,
    5:  required string fromOutputName,
    6:  required ComponentType toType,
    7:  required string toId,
    8:  required string toInputName
    9:  optional i64 createdAt,
    10: optional i64 updatedAt
}

enum HandlerType {
    FLOW_STARTER,
    FLOW_TERMINATOR
}

enum HandlerState {
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

struct HandlerStatus {
    1:  optional string id,
    2:  required HandlerState state,
    3:  optional string description,
    4:  optional i64 updatedAt
}

struct WorkflowHandler {
    1:  required string id,
    2:  required HandlerType type,
    3:  optional list<application_io_models.InputDataObjectType> inputs,
    4:  optional list<application_io_models.OutputDataObjectType> outputs,
    5:  optional list<HandlerStatus> statuses,
    6:  optional list<airavata_commons.ErrorModel> errors,
    7:  optional i64 createdAt,
    8: optional i64 updatedAt
}

enum WorkflowState {
    CREATED,
    VALIDATED,
    SCHEDULED,
    LAUNCHED,
    EXECUTING,
    PAUSING,
    PAUSED,
    RESTARTING,
    CANCELING,
    CANCELED,
    COMPLETED,
    FAILED
}

struct WorkflowStatus {
    1:  optional string id,
    2:  required WorkflowState state,
    3:  optional string description,
    4:  optional i64 updatedAt
}

struct AiravataWorkflow {
    1:  required string id = airavata_commons.DEFAULT_ID,
    2:  required string experimentId,
    3:  optional string description,
    4:  optional list<WorkflowApplication> applications,
    5:  optional list<WorkflowHandler> handlers,
    6:  optional list<WorkflowConnection> connections,
    7:  optional list<WorkflowStatus> statuses,
    8:  optional list<airavata_commons.ErrorModel> errors,
    9:  optional i64 createdAt,
    10: optional i64 updatedAt
}
