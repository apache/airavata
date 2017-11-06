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

namespace java org.apache.airavata.model.scheduling
namespace php Airavata.Model.Scheduling
namespace cpp apache.airavata.model.scheduling
namespace py airavata.model.scheduling

/**
 * ComputationalResourceSchedulingModel:
 *
 *
*/
struct ComputationalResourceSchedulingModel {
    1: optional string resourceHostId,
    2: optional i32 totalCPUCount,
    3: optional i32 nodeCount,
    4: optional i32 numberOfThreads,
    5: optional string queueName,
    6: optional i32 wallTimeLimit,
    7: optional i32 totalPhysicalMemory,
    8: optional string chessisNumber,
    9: optional string staticWorkingDir,
    10: optional string overrideLoginUserName,
    11: optional string overrideScratchLocation,
    12: optional string overrideAllocationProjectNumber
}