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


namespace java org.apache.airavata.model
namespace php Airavata.Model
namespace py airavata.model.workflow

include "../app-catalog-models/application_io_models.thrift"
include "../../airavata-apis/airavata_commons.thrift"

/*
 * This file describes the definitions of the Airavata Execution Data Structures. Each of the
 *   language specific Airavata Client SDK's will translate this neutral data model into an
 *   appropriate form for passing to the Airavata Server Execution API Calls.
*/


struct WorkflowModel {
    1: required string templateId = airavata_commons.DEFAULT_ID,
    2: required string name,
    3: required string graph,
    4: required string gatewayId,
    5: required string createdUser,
    6: optional binary image,
    7: optional list<application_io_models.InputDataObjectType> workflowInputs,
    8: optional list<application_io_models.OutputDataObjectType> workflowOutputs,
    9: optional i64 creationTime,
}

enum WorkflowState {
    CREATED,
    STARTED,
    EXECUTING,
    COMPLETED,
    FAILED,
    CANCELLING,
    CANCELED
}

enum ComponentState {
    CREATED,
    WAITING,
    READY,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELED
}

struct ComponentStatus {
    1: required ComponentState state = ComponentState.CREATED,
    2: optional string reason,
    3: optional i64 timeofStateChange
}

struct WorkflowStatus {
    1: required WorkflowState state,
    2: optional i64 timeOfStateChange,
    3: optional string reason
}

struct EdgeModel {
    1: required string edgeId = airavata_commons.DEFAULT_ID,
    2: optional string name,
    3: optional ComponentStatus status,
    4: optional string description
}

struct PortModel {
    1: required string portId = airavata_commons.DEFAULT_ID,
    2: optional string name,
    3: optional ComponentStatus status,
    4: optional string value,
    5: optional string description

}

struct NodeModel {
    1: required string nodeId= airavata_commons.DEFAULT_ID,
    2: optional string name,
    3: optional string applicationId,
    4: optional string applicationName,
    5: optional ComponentStatus status,
    6: optional string description
}
