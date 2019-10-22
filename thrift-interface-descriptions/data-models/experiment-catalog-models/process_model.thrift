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
include "status_models.thrift"
include "task_model.thrift"
include "scheduling_model.thrift"

namespace java org.apache.airavata.model.process
namespace php Airavata.Model.Process
namespace cpp apache.airavata.model.process
namespace py airavata.model.process


struct ProcessWorkflow {
    1: required string processId;
    2: required string workflowId;
    3: optional i64 creationTime;
    4: optional string type;
}

/**
 * ProcessModel: A structure holding the process details. The infromation is derived based on user provided
 *          configuration data or system inferred information from scheduling and QoS parameters.
 *
 * processDetail:
 *   A friendly description of the process, usally used to communicate information to users.
 *
 *
*/
struct ProcessModel {
    1: required string processId = airavata_commons.DEFAULT_ID,
    2: required string experimentId,
    3: optional i64 creationTime,
    4: optional i64 lastUpdateTime,
    5: optional list<status_models.ProcessStatus> processStatuses,
    6: optional string processDetail,
    7: optional string applicationInterfaceId,
    8: optional string applicationDeploymentId,
    9: optional string computeResourceId,
    10: optional list<application_io_models.InputDataObjectType> processInputs,
    11: optional list<application_io_models.OutputDataObjectType> processOutputs,
    12: optional scheduling_model.ComputationalResourceSchedulingModel processResourceSchedule,
    13: optional list<task_model.TaskModel> tasks,
    14: optional string taskDag,
    15: optional list<airavata_commons.ErrorModel> processErrors,
    16: optional string gatewayExecutionId,
    17: optional bool enableEmailNotification,
    18: optional list<string> emailAddresses,
    19: optional string storageResourceId,
    20: optional string userDn,
    21: optional bool generateCert = 0,
    22: optional string experimentDataDir,
    23: optional string userName,
    24: optional bool useUserCRPref,
    25: optional string groupResourceProfileId;
    26: optional list<ProcessWorkflow> processWorkflows;
}
