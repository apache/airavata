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
namespace py airavata.model.appcatalog.gatewayprofile

include "compute_resource_model.thrift"
include "data_movement_models.thrift"
include "account_provisioning_model.thrift"

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
 * resourceSpecificCredentialStoreToken:
 *  Resource specific credential store token. If this token is specified, then it is superceeded by the gateway's
 *   default credential store.
 *
*/
struct ComputeResourcePreference {
    1: required string computeResourceId,
    2: required bool overridebyAiravata = 1,
    3: optional string loginUserName,
    4: optional compute_resource_model.JobSubmissionProtocol preferredJobSubmissionProtocol,
    5: optional data_movement_models.DataMovementProtocol preferredDataMovementProtocol,
    6: optional string preferredBatchQueue,
    7: optional string scratchLocation,
    8: optional string allocationProjectNumber,
    9: optional string resourceSpecificCredentialStoreToken,
    10: optional string usageReportingGatewayId,
    11: optional string qualityOfService,
    12: optional string reservation,
    13: optional i64 reservationStartTime,
    14: optional i64 reservationEndTime,
    15: optional string sshAccountProvisioner,
    16: optional map<string, string> sshAccountProvisionerConfig,
    17: optional string sshAccountProvisionerAdditionalInfo
}

struct StoragePreference {
    1: required string storageResourceId,
    2: optional string loginUserName,
    3: optional string fileSystemRootLocation,
    4: optional string resourceSpecificCredentialStoreToken
}

/**
 * Gateway Resource Profile
 *
 * gatewayID:
 *  Unique identifier for the gateway assigned by Airavata. Corelate this to Airavata Admin API Gateway Registration.
 *
 * credentialStoreToken:
 *  Gateway's defualt credential store token.
 *
 * computeResourcePreferences:
 *  List of resource preferences for each of the registered compute resources.
 *
 *  identityServerTenant:
 *
 *  identityServerPwdCredToken:
 *
*/
struct GatewayResourceProfile {
    1: required string gatewayID,
    2: optional string credentialStoreToken,
    3: optional list<ComputeResourcePreference> computeResourcePreferences,
    4: optional list<StoragePreference> storagePreferences,
    5: optional string identityServerTenant,
    6: optional string identityServerPwdCredToken
}
