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

namespace java org.apache.airavata.model.appcatalog.userresourceprofile
namespace php Airavata.Model.AppCatalog.UserResourceProfile
namespace cpp apache.airavata.model.appcatalog.userresourceprofile
namespace py airavata.model.appcatalog.userresourceprofile

include "compute_resource_model.thrift"
include "data_movement_models.thrift"
include "../user-tenant-group-models/user_profile_model.thrift"

/**
 * User specific preferences for a Computer Resource
 *
 * computeResourceId:
 *   Corelate the preference to a compute resource.
 *
 *
 * loginUserName:
 *   If turned true, Airavata will override the preferences of better alternatives exist.
 *
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
 * validated:
 *  If true the the configuration has been validated in the sense that the username and credential can be used to
 *  login to the remote host and the scratchLocation is a valid location that the user has permission to write to.
 *  Should be treated as read-only and only mutated by Airavata middleware.
 *
*/
struct UserComputeResourcePreference {
    1: required string computeResourceId,
    2: optional string loginUserName,
    3: optional string preferredBatchQueue,
    4: optional string scratchLocation,
    5: optional string allocationProjectNumber,
    6: optional string resourceSpecificCredentialStoreToken,
    7: optional string qualityOfService,
    8: optional string reservation,
    9: optional i64 reservationStartTime,
    10: optional i64 reservationEndTime,
    11: optional bool validated = false
}

struct UserStoragePreference {
    1: required string storageResourceId,
    2: optional string loginUserName,
    3: optional string fileSystemRootLocation,
    4: optional string resourceSpecificCredentialStoreToken
}

/**
 * User Resource Profile
 *
 * userId:
 * Unique identifier used to link user to corresponding user data model
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
struct UserResourceProfile {
    1: required string userId,
    2: required string gatewayID,
    3: optional string credentialStoreToken,
    4: optional list<UserComputeResourcePreference> userComputeResourcePreferences,
    5: optional list<UserStoragePreference> userStoragePreferences,
    6: optional string identityServerTenant,
    7: optional string identityServerPwdCredToken,
}