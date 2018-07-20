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
include "workflow_application_model.thrift"
include "workflow_handler_model.thrift"
include "workflow_connection_model.thrift"

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
    1:  required WorkflowState state,
    2:  optional string description,
    3:  optional i64 updatedAt
}

struct AiravataWorkflow {
    1:  required string id = airavata_commons.DEFAULT_ID,
    2:  required string name,
    3:  required string gatewayId,
    4:  required string storageResourceId,
    5:  optional string description,
    6:  optional bool enableEmailNotification,
    7:  optional list<string> notificationEmails,
    8:  optional list<workflow_application_model.WorkflowApplication> applications,
    9:  optional list<workflow_handler_model.WorkflowHandler> handlers,
    10: optional list<workflow_connection_model.WorkflowConnection> connections,
    11: optional list<WorkflowStatus> statuses,
    12: optional list<airavata_commons.ErrorModel> errors,
    13: optional i64 createdAt,
    14: optional i64 updatedAt
}
