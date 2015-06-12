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

include "airavata_commons.thrift"
include "status_models.thrift"
include "task_model.thrift"
include "application_io_models.thrift"
include "scheduling_model.thrift"

namespace java org.apache.airavata.model.process
namespace php Airavata.Model.Process
namespace cpp apache.airavata.model.process
namespace py apache.airavata.model.process


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
    2: optional i64 creationTime,
    3: optional i64 lastUpdateTime,
    4: optional status_models.Status processStatus,
    5: optional string processDetail,
    6: optional list<application_io_models.InputDataObjectType> processInputs,
    7: optional list<application_io_models.OutputDataObjectType> processOutputs,
    8: optional scheduling_model.ComputationalResourceSchedulingModel resourceSchedule,
    9: optional list<task_model.TaskModel> tasks,
    10: optional string taskDag
    11: optional airavata_commons.ErrorDetails processErrorDetails
}