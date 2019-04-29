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
include "../resource-catalog-models/compute_resource_model.thrift"
include "../resource-catalog-models/data_movement_models.thrift"
include "../app-catalog-models/application_io_models.thrift"
include "status_models.thrift"
include "job_model.thrift"

namespace java org.apache.airavata.model.task
namespace php Airavata.Model.Task
namespace cpp apache.airavata.model.task
namespace py airavata.model.task

/**
 * TaskTypes: An enumerated list of TaskTypes. Task being generic, the task type will provide the concrete interpretation.
 *
*/
enum TaskTypes {
    ENV_SETUP,
    DATA_STAGING,
    JOB_SUBMISSION,
    ENV_CLEANUP,
    MONITORING,
    OUTPUT_FETCHING
}

/**
 * TaskModel: A structure holding the generic task details.
 *
 * taskDetail:
 *   A friendly description of the task, usally used to communicate information to users.
 *
 * subTaskModel:
 *   A generic byte object for the Task developer to store internal serialized data into registry catalogs.
*/
struct TaskModel {
    1: required string taskId = airavata_commons.DEFAULT_ID,
    2: required TaskTypes taskType,
    3: required string parentProcessId,
    4: required i64 creationTime,
    5: required i64 lastUpdateTime,
    6: required list<status_models.TaskStatus> taskStatuses,
    7: optional string taskDetail,
    8: optional binary subTaskModel,
    9: optional list<airavata_commons.ErrorModel> taskErrors,
    10: optional list<job_model.JobModel> jobs,
    11: optional i32 maxRetry,
    12: optional i32 currentRetry,
}

/**
 * DataStagingTaskModel: A structure holding the data staging task details.
 *
 * Source and Destination locations includes standard representation of protocol, host, port and path
 *   A friendly description of the task, usally used to communicate information to users.
 *
*/

enum DataStageType {
	INPUT,
	OUPUT,
	ARCHIVE_OUTPUT

}
struct DataStagingTaskModel {
    1: required string source,
    2: required string destination,
    3: required DataStageType type,
    4: optional i64 transferStartTime,
    5: optional i64 transferEndTime,
    6: optional string transferRate,
    7: optional  application_io_models.InputDataObjectType processInput,
    8: optional application_io_models.OutputDataObjectType processOutput
}

/**
* EnvironmentSetupTaskModel: A structure holding the environment creation task details
**/
struct EnvironmentSetupTaskModel {
	1: required string location,
	2: required data_movement_models.SecurityProtocol protocol
}


struct JobSubmissionTaskModel {
	1: required compute_resource_model.JobSubmissionProtocol jobSubmissionProtocol,
	2: required compute_resource_model.MonitorMode monitorMode,
	3: optional i32 wallTime
}

struct MonitorTaskModel {
	1: required compute_resource_model.MonitorMode monitorMode
}