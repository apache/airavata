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
include "workflow_application_model.thrift"
include "workflow_connection_model.thrift"

namespace java org.apache.airavata.model.workflow
namespace php Airavata.Model.Workflow
namespace cpp apache.airavata.model.workflow
namespace py airavata.model.workflow

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

struct WorkflowHandler {
    1:  required string id,
    2:  required bool belongsToMainWorkflow,
    3:  required HandlerType type,
    4:  optional list<application_io_models.InputDataObjectType> inputs,
    5:  optional list<application_io_models.OutputDataObjectType> outputs,
    6:  optional list<workflow_application_model.WorkflowApplication> applications,
    7:  optional list<WorkflowHandler> handlers,
    8:  optional list<workflow_connection_model.WorkflowConnection> connections,
    9:  optional list<HandlerStatus> statuses,
    10: optional list<airavata_commons.ErrorModel> errors,
    11: optional i64 createdAt,
    12: optional i64 updatedAt
}
