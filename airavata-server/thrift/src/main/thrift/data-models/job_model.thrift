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

 include "status_models.thrift"

  namespace java org.apache.airavata.model.job
  namespace php Airavata.Model.Job
  namespace cpp apache.airavata.model.job
  namespace py airavata.model.job

struct JobModel {
    1: required string jobId,
    2: required string taskId,
    3: required string processId,
    4: required string jobDescription,
    5: optional i64 creationTime,
    6: optional list<status_models.JobStatus> jobStatuses,
    7: optional string computeResourceConsumed,
    8: optional string jobName,
    9: optional string workingDir,
    10: optional string stdOut,
    11: optional string stdErr,
    12: optional i32 exitCode
}

