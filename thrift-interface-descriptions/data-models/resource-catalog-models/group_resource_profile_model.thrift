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

namespace java org.apache.airavata.model.appcatalog.groupresourceprofile
namespace php Airavata.Model.AppCatalog.GroupResourceProfile
namespace cpp apache.airavata.model.appcatalog.groupresourceprofile
namespace py airavata.model.appcatalog.groupresourceprofile

include "../../airavata-apis/airavata_commons.thrift"
include "compute_resource_model.thrift"
include "data_movement_models.thrift"

struct GroupAccountSSHProvisionerConfig {
    1: required string resourceId,
    2: required string groupResourceProfileId = airavata_commons.DEFAULT_ID,
    3: required string configName,
    4: optional string configValue
}

struct ComputeResourceReservation {
    1: required string reservationId = airavata_commons.DEFAULT_ID,
    2: required string reservationName,
    3: required list<string> queueNames,
    4: required i64 startTime,
    5: required i64 endTime,
}

struct GroupComputeResourcePreference {
    1: required string computeResourceId,
    2: required string groupResourceProfileId = airavata_commons.DEFAULT_ID,
    3: required bool overridebyAiravata = 1,
    4: optional string loginUserName,
    5: optional compute_resource_model.JobSubmissionProtocol preferredJobSubmissionProtocol,
    6: optional data_movement_models.DataMovementProtocol preferredDataMovementProtocol,
    7: optional string preferredBatchQueue,
    8: optional string scratchLocation,
    9: optional string allocationProjectNumber,
    10: optional string resourceSpecificCredentialStoreToken,
    11: optional string usageReportingGatewayId,
    12: optional string qualityOfService,
    16: optional string sshAccountProvisioner,
    17: optional list<GroupAccountSSHProvisionerConfig> groupSSHAccountProvisionerConfigs,
    18: optional string sshAccountProvisionerAdditionalInfo,
    19: optional list<ComputeResourceReservation> reservations,
}

struct ComputeResourcePolicy {
    1: required string resourcePolicyId = airavata_commons.DEFAULT_ID,
    2: required string computeResourceId,
    3: required string groupResourceProfileId = airavata_commons.DEFAULT_ID,
    4: optional list<string> allowedBatchQueues
}

struct BatchQueueResourcePolicy {
    1: required string resourcePolicyId = airavata_commons.DEFAULT_ID,
    2: required string computeResourceId,
    3: required string groupResourceProfileId = airavata_commons.DEFAULT_ID,
    4: optional string queuename,
    5: optional i32 maxAllowedNodes,
    6: optional i32 maxAllowedCores,
    7: optional i32 maxAllowedWalltime
}

/**
 * Group Resource Profile
 *
 * gatewayID:
 *  Unique identifier for the gateway assigned by Airavata. Corelate this to Airavata Admin API Gateway Registration.
 *
 * groupResourceProfileId:
 *
 * computeResourcePreferences:
 *  List of resource preferences for each of the registered compute resources.
 *
 * computeResourcePolicies:
 *  List of enforced policies for each of the registered compute resources.
 *
 * batchQueueResourcePolicies:
 *  List of enforced policies on registered batch queues
 *
 * defaultCredentialStoreToken:
 *  The default credential store token to use for compute resources that don't specify a resource specific credential store token.
 *
*/

struct GroupResourceProfile {
    1: required string gatewayId,
    2: required string groupResourceProfileId = airavata_commons.DEFAULT_ID,
    3: optional string groupResourceProfileName,
    4: optional list<GroupComputeResourcePreference> computePreferences,
    5: optional list<ComputeResourcePolicy> computeResourcePolicies,
    6: optional list<BatchQueueResourcePolicy> batchQueueResourcePolicies
    7: optional i64 creationTime,
    8: optional i64 updatedTime,
    9: optional string defaultCredentialStoreToken
}
