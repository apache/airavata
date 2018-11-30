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

namespace java org.apache.airavata.model.status
namespace php Airavata.Model.Status
namespace cpp apache.airavata.model.status
namespace py airavata.model.status

enum ExperimentState {
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

enum TaskState {
    CREATED,
    EXECUTING,
    COMPLETED,
    FAILED,
    CANCELED
}

enum ProcessState {
    CREATED,
    VALIDATED,
    STARTED,
    PRE_PROCESSING,
    CONFIGURING_WORKSPACE,
    INPUT_DATA_STAGING,
    EXECUTING,
    MONITORING,
    OUTPUT_DATA_STAGING,
    POST_PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLING,
    CANCELED
}

enum JobState {
    SUBMITTED,
    QUEUED,
    ACTIVE,
    COMPLETE,
    CANCELED,
    FAILED,
    SUSPENDED,
    UNKNOWN,
    NON_CRITICAL_FAIL
}

/**
 * Status: A generic status object.
 *
 * state:
 *   State .
 *
 * timeOfStateChange:
 *   time the status was last updated.
 *
 * reason:
 *   User friendly reason on how the state is inferred.
 *
*/
struct ExperimentStatus {
    1: required ExperimentState state,
    2: optional i64 timeOfStateChange,
    3: optional string reason,
    4: optional string statusId
}

struct ProcessStatus {
    1: required ProcessState state,
    2: optional i64 timeOfStateChange,
    3: optional string reason,
    4: optional string statusId
}

struct TaskStatus {
    1: required TaskState state,
    2: optional i64 timeOfStateChange,
    3: optional string reason,
    4: optional string statusId
}

struct JobStatus {
    1: required JobState jobState,
    2: optional i64 timeOfStateChange,
    3: optional string reason,
    4: optional string statusId
}

struct QueueStatusModel {
    1: required string hostName;
    2: required string queueName;
    3: required bool queueUp;
    4: required i32 runningJobs;
    5: required i32 queuedJobs;
    6: required i64 time;
}
