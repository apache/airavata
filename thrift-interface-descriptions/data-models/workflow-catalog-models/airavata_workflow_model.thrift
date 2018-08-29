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

struct NotificationEmail {
    1:  required string email,
}

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

struct WorkflowConnection {
    1:  required string id = airavata_commons.DEFAULT_ID,
    2:  required bool belongsToMainWorkflow,
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
    FLOW_TERMINATOR,
    DOWHILE_LOOP,
    FOREACH_LOOP
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

struct WorkflowHandler {
    1:  required string id,
    2:  required bool belongsToMainWorkflow,
    3:  required HandlerType type,
    4:  optional list<application_io_models.InputDataObjectType> inputs,
    5:  optional list<application_io_models.OutputDataObjectType> outputs,
    6:  optional list<WorkflowApplication> applications,
    7:  optional list<WorkflowConnection> connections,
    8:  optional list<HandlerStatus> statuses,
    9: optional list<airavata_commons.ErrorModel> errors,
    10: optional i64 createdAt,
    11: optional i64 updatedAt
}

struct AiravataWorkflow {
    1:  required string id = airavata_commons.DEFAULT_ID,
    2:  required string name,
    3:  required string gatewayId,
    4:  required string userName,
    5:  required string storageResourceId,
    6:  optional string description,
    7:  optional bool enableEmailNotification,
    8:  optional list<NotificationEmail> notificationEmails,
    9:  optional list<WorkflowApplication> applications,
    10: optional list<WorkflowHandler> handlers,
    11: optional list<WorkflowConnection> connections,
    12: optional list<WorkflowStatus> statuses,
    13: optional list<airavata_commons.ErrorModel> errors,
    14: optional i64 createdAt,
    15: optional i64 updatedAt
}