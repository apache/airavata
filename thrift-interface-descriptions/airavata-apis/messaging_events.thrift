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

include "../data-models/experiment-catalog-models/status_models.thrift"
include "../data-models/app-catalog-models/application_io_models.thrift"
include "airavata_commons.thrift"

namespace java org.apache.airavata.model.messaging.event
namespace php Airavata.Model.Messaging.Event
namespace cpp apache.airavata.model.messaging.event
namespace py airavata.model.messaging.event

enum MessageLevel {
    INFO,
    DEBUG,
    ERROR,
    ACK
}

enum MessageType {
    EXPERIMENT,
    EXPERIMENT_CANCEL,
    TASK,
    PROCESS,
    JOB,
    LAUNCHPROCESS,
    TERMINATEPROCESS,
    PROCESSOUTPUT,
    DB_EVENT,
    INTERMEDIATE_OUTPUTS,
}

struct ExperimentStatusChangeEvent {
    1: required status_models.ExperimentState state;
    2: required string experimentId;
    3: required string gatewayId;
}

struct ProcessIdentifier {
    1: required string processId;
    2: required string experimentId;
    3: required string gatewayId;
}

struct TaskIdentifier {
    1: required string taskId;
    2: required string processId;
    3: required string experimentId;
    4: required string gatewayId;
}

struct TaskStatusChangeEvent {
    1: required status_models.TaskState state;
    2: required TaskIdentifier  taskIdentity;
}

struct TaskStatusChangeRequestEvent {
    1: required status_models.TaskState state;
    2: required TaskIdentifier taskIdentity;
}

struct ProcessStatusChangeEvent {
    1: required status_models.ProcessState state;
    2: required ProcessIdentifier processIdentity;
}

struct ProcessStatusChangeRequestEvent {
    1: required status_models.ProcessState state;
    2: required ProcessIdentifier processIdentity;
}

struct TaskOutputChangeEvent {
    1: required list<application_io_models.OutputDataObjectType> output;
    2: required TaskIdentifier taskIdentity;
}

struct JobIdentifier {
    1: required string jobId;
    2: required string taskId;
    3: required string processId;
    4: required string experimentId;
    5: required string gatewayId;
}

//struct JobMonitor {
//    1: optional string username;
//    2: optional i64 jobStartedTime;
//    3: optional i64 lastMonitoredTime;
//    4: optional string hostId;
//    5: optional map<string, string> parameters;
//    6: optional string jobName;
//    7: optional i32 failedCount = 0;
//    // FIXME - Job execution context
//    //8:
// }

struct ExperimentSubmitEvent{
    1: required string experimentId,
    2: required string gatewayId,
}

struct ProcessSubmitEvent{
    1: required string processId,
    2: required string gatewayId,
    3: required string experimentId,
    4: required string tokenId
}

struct ProcessTerminateEvent{
    1: required string processId,
    2: required string gatewayId,
    3: required string tokenId
}

struct JobStatusChangeEvent {
    1: required status_models.JobState state;
    2: required JobIdentifier jobIdentity;
}

struct JobStatusChangeRequestEvent {
    1: required status_models.JobState state;
    2: required JobIdentifier jobIdentity;
}

struct ExperimentIntermediateOutputsEvent {
    1: required string experimentId;
    2: required string gatewayId;
    3: required list<string> outputNames;
}

struct Message {
    1: required binary event;
    2: required string messageId = airavata_commons.DEFAULT_ID,
    3: required MessageType messageType;
    4: optional i64 updatedTime;
    5: optional MessageLevel messageLevel;
}







