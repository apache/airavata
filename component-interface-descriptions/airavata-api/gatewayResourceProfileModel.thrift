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

namespace java org.apache.airavata.model.appcatalog.gatewayprofile
namespace php Airavata.Model.AppCatalog.GatewayProfile
namespace cpp apache.airavata.model.appcatalog.gatewayprofile
namespace py apache.airavata.model.appcatalog.gatewayprofile

include "computeResourceModel.thrift"

/**
 * Gateway specific preferences for a Computer Resource
 *
 * computeResourceId:
 *   Corelate the preference to a compute resource.
 *
 * overridebyAiravata:
 *   If turned true, Airavata will override the preferences of better alternatives exist.
 *
 * loginUserName:
 *   If turned true, Airavata will override the preferences of better alternatives exist.
 *
 * preferredJobSubmissionProtocol:
 *   For resources with multiple job submission protocols, the gateway can pick a preferred option.
 *
 * preferredDataMovementProtocol:
 *   For resources with multiple data movement protocols, the gateway can pick a preferred option.
 *
 * preferredBatchQueue:
 *  Gateways can choose a defualt batch queue based on average job dimention, reservations or other metrics.
 *
 * scratchLocation:
 *  Path to the local scratch space on a HPC cluster. Typically used to create working directory for job execution.
 *
 * allocationProjectNumber:
 *  Typically used on HPC machines to charge computing usage to a account number. For instance, on XSEDE once an
 *    allocation is approved, an allocation number is assigned. Before passing this number with job submittions, the
 *    account to be used has to be added to the allocation.
 *
*/
struct ComputeResourcePreference {
    1: required string computeResourceId,
    2: required bool overridebyAiravata = 1,
    3: optional string loginUserName,
    4: optional computeResourceModel.JobSubmissionProtocol preferredJobSubmissionProtocol,
    5: optional computeResourceModel.DataMovementProtocol preferredDataMovementProtocol,
    6: optional string preferredBatchQueue,
    7: optional string scratchLocation,
    8: optional string allocationProjectNumber
}

/**
 * Gateway Resource Profile
 *
 * gatewayID:
 *   Unique identifier for the gateway assigned by Airavata. Corelate this to Airavata Admin API Gateway Registration.
 *
 * computeResourcePreferences:
 *  List of resource preferences for each of the registered compute resources.
 *
 *
*/
struct GatewayResourceProfile {
    1: required string gatewayID,
    2: optional list<ComputeResourcePreference> computeResourcePreferences
}
