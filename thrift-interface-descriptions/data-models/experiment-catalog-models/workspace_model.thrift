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

include "../../airavata-apis/airavata_commons.thrift"

namespace java org.apache.airavata.model.workspace
namespace php Airavata.Model.Workspace
namespace cpp apache.airavata.model.workspace
namespace py airavata.model.workspace

/*
 * This file describes the definitions of the Airavata Workspace. The workspace is a container for all user data
 *   organized as Projects and Experiment within them.
 *
 * The Experiment data model is divided into 6 categories: experiment metadata, experiment configuration
 *   data, experiment generated data, experiment monitoring data, provenance data and error handling data.
 *
 *
*/

struct Group {
    1: required string groupName,
    2: optional string description
}

struct Project {
    1: required string projectID = airavata_commons.DEFAULT_ID,
    2: required string owner,
    3: required string gatewayId,
    4: required string name,
    5: optional string description
    6: optional i64 creationTime
    7: optional list<string> sharedUsers,
    8: optional list<string> sharedGroups
}

struct User {
    1: required string airavataInternalUserId = airavata_commons.DEFAULT_ID,
    2: optional string userName,
    3: required string gatewayId,
    4: optional string firstName,
    5: optional string lastName,
    6: optional string email
}

enum GatewayApprovalStatus {
    REQUESTED,
    APPROVED,
    ACTIVE,
    DEACTIVATED,
    CANCELLED,
    DENIED,
    CREATED,
    DEPLOYED
}

struct Gateway {
    1: optional string airavataInternalGatewayId,
    2: required string gatewayId,
    3: required GatewayApprovalStatus gatewayApprovalStatus,
    4: optional string gatewayName,
    5: optional string domain,
    6: optional string emailAddress
    7: optional string gatewayAcronym,
    8: optional string gatewayURL,
    9: optional string gatewayPublicAbstract,
    10: optional string reviewProposalDescription,
    11: optional string gatewayAdminFirstName,
    12: optional string gatewayAdminLastName,
    13: optional string gatewayAdminEmail,
    14: optional string identityServerUserName,
    15: optional string identityServerPasswordToken,
    16: optional string declinedReason,
    17: optional string oauthClientId,
    18: optional string oauthClientSecret,
    19: optional i64 requestCreationTime,
    20: optional string requesterUsername
}

struct GatewayUsageReportingCommand {
    1: required string gatewayId,
    2: required string computeResourceId,
    3: required string command
}

enum NotificationPriority {
    LOW,
    NORMAL,
    HIGH
}

struct Notification {
    1: optional string notificationId,
    2: required string gatewayId,
    3: required string title,
    4: required string notificationMessage,
    5: optional i64 creationTime,
    6: optional i64 publishedTime,
    7: optional i64 expirationTime,
    8: optional NotificationPriority priority
}