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
  namespace php Airavata.Model.job
  namespace cpp apache.airavata.model.job
  namespace py apache.airavata.model.job

struct JobModel {
    1: required string jobId,
    2: required string jobDescription,
    3: optional i64 creationTime,
    4: optional status_models.JobStatus jobStatus,
    7: optional string computeResourceConsumed,
    8: optional string jobName,
    9: optional string workingDir
}

