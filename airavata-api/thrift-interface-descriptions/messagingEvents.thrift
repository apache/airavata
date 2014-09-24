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

include "experimentModel.thrift"

namespace java org.apache.airavata.model.messaging.event
namespace php Airavata.Model.Messaging.Event
namespace cpp apache.airavata.model.messaging.event

const string DEFAULT_ID = "DO_NOT_SET_AT_CLIENTS"

enum MessageLevel {
    INFO,
    DEBUG,
    ERROR,
    ACK
}

enum MessageType {
    EXPERIMENT,
    TASK,
    WORKFLOWNODE,
    JOB
}

struct ExperimentStatusChangeEvent {
    1: required experimentModel.ExperimentState state;
    2: required string experimentId;
}

struct WorkflowIdentity {
    1: required string workflowNodeId;
    2: required string experimentId;
}

struct WorkflowNodeStatusChangeEvent {
    1: required experimentModel.WorkflowNodeState state;
    2: required WorkflowIdentity workflowNodeIdentity;
}

struct TaskIdentity {
    1: required string taskId;
    2: required string workflowNodeId;
    3: required string experimentId;
}

struct TaskStatusChangeEvent {
    1: required experimentModel.TaskState state;
    2: required TaskIdentity taskIdentity;
}

struct TaskOutputChangeEvent {
    1: required list<experimentModel.DataObjectType> output;
    2: required TaskIdentity taskIdentity;
}

struct JobIdentity {
    1: required string jobId;
    2: required string taskId;
    3: required string workflowNodeId;
    4: required string experimentId;
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

struct JobStatusChangeEvent {
    1: required experimentModel.JobState state;
    2: required JobIdentity jobIdentity;
//    3: required JobMonitor jobMonitor;
}

struct Message {
    1: required binary event;
    2: required string messageId = DEFAULT_ID;
    3: required MessageType messageType;
    4: optional i64 updatedTime;
    5: optional MessageLevel messageLevel;
}







