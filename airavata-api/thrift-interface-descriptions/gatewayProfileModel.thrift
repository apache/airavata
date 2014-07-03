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

const string DEFAULT_ID = "DO_NOT_SET_AT_CLIENTS"

/**
 * Resource Preferences for each of the gateway
 *
 * computeResourceId:
 *   Corelate the preference for compute resource to
 *
 * gatewayName:
 *   Name of the Gateway.
 *
 * ipAddress:
 *   IP Addresse of the Hostname.
 *
 * resourceDescription:
 *  A user friendly description of the hostname.
 *
 * JobSubmissionProtocols:
 *  A computational resources may have one or more ways of submitting Jobs. This structure
 *  will hold all available mechanisms to interact with the resource.
 *
 * DataMovementProtocol:
 *  Option to specify a prefered data movement mechanism of the available options.
 *
*/
struct ComputeResourcePreference {
    1: required string computeResourceId,
    2: required bool overridebyAiravata = 1,
    3: optional string preferredJobSubmissionProtocol,
    4: optional string preferredDataMovementProtocol,
    5: optional string preferredBatchQueue,
    6: optional string scratchLocation,
    7: optional string allocationProjectNumber
}

/**
 * Gateway Profile
 *
 * gatewayID:
 *   Unique identifier for the gateway assigned by Airavata
 *
 * gatewayName:
 *   Name of the Gateway.
 *
 * ipAddress:
 *   IP Addresse of the Hostname.
 *
 * resourceDescription:
 *  A user friendly description of the hostname.
 *
 * JobSubmissionProtocols:
 *  A computational resources may have one or more ways of submitting Jobs. This structure
 *  will hold all available mechanisms to interact with the resource.
 *
 * DataMovementProtocol:
 *  Option to specify a prefered data movement mechanism of the available options.
 *
*/
struct GatewayProfile {
    1: required string gatewayID = DEFAULT_ID,
    2: required string gatewayName,
    3: optional string gatewayDescription,
    4: optional list<ComputeResourcePreference> computeResourcePreferences
}
