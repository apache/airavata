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

namespace java org.apache.airavata.model.tenant
namespace php Airavata.Model.Tenant
namespace cpp apache.airavata.model.tenant
namespace py airavata.model.tenant

enum TenantApprovalStatus {
    REQUESTED,
    APPROVED,
    ACTIVE,
    DEACTIVATED,
    CANCELLED,
    DENIED,
    CREATED,
    DEPLOYED
}

struct TenantPreferences {
    10: optional string tenantAdminFirstName,
    11: optional string tenantAdminLastName,
    12: optional string tenantAdminEmail,
}

struct TenantConfig {
    16: optional string oauthClientId,
    17: optional string oauthClientSecret,
        13: optional string identityServerUserName,
        14: optional string identityServerPasswordToken,
}

struct Tenant {
    1: required string tenantId,
    2: required TenantApprovalStatus tenantApprovalStatus,
    3: optional string tenantName,
    4: optional string domain,
    5: optional string emailAddress
    6: optional string tenantAcronym,
    7: optional string tenantURL,
    8: optional string tenantPublicAbstract,
    9: optional string reviewProposalDescription,
    15: optional string declinedReason,
    18: optional i64 requestCreationTime,
    19: optional string requesterUsername
}