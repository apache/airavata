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

/**
 * Unified Airavata Service Interface
 * This service combines all Airavata thrift services into a single multiplexed service
 * for simplified deployment and client interaction.
 */

// Include all existing data models
include "../airavata-apis/airavata_errors.thrift"
include "../airavata-apis/airavata_commons.thrift"
include "../airavata-apis/security_model.thrift"
include "../airavata-apis/messaging_events.thrift"
include "../airavata-apis/db_event_model.thrift"

include "../base-api/base_api.thrift"

// Data models
include "../data-models/credential_store_models.thrift"
include "../data-models/status_models.thrift"
include "../data-models/job_model.thrift"
include "../data-models/experiment_model.thrift"
include "../data-models/workspace_model.thrift"
include "../data-models/scheduling_model.thrift"
include "../data-models/application_io_models.thrift"
include "../data-models/application_deployment_model.thrift"
include "../data-models/application_interface_model.thrift"
include "../data-models/parser_model.thrift"
include "../data-models/account_provisioning_model.thrift"
include "../data-models/compute_resource_model.thrift"
include "../data-models/storage_resource_model.thrift"
include "../data-models/gateway_resource_profile_model.thrift"
include "../data-models/group_resource_profile_model.thrift"
include "../data-models/user_resource_profile_model.thrift"
include "../data-models/data_movement_models.thrift"
include "../data-models/gateway_groups_model.thrift"
include "../data-models/replica_catalog_models.thrift"
include "../data-models/group_manager_model.thrift"
include "../data-models/user_profile_model.thrift"
include "../data-models/tenant_profile_model.thrift"
include "../data-models/sharing_models.thrift"
include "../data-models/workflow_data_model.thrift"
include "../data-models/workflow_model.thrift"
include "../data-models/process_model.thrift"
include "../data-models/task_model.thrift"

// Service definitions
include "../airavata-apis/airavata_api.thrift"
include "../airavata-apis/workflow_api.thrift"
include "../service-cpis/registry-api.thrift"
include "../service-cpis/credential-store-cpi.thrift"
include "../service-cpis/sharing_cpi.thrift"
include "../service-cpis/orchestrator-cpi.thrift"
include "../service-cpis/profile-service/profile-user/profile-user-cpi.thrift"
include "../service-cpis/profile-service/profile-tenant/profile-tenant-cpi.thrift"
include "../service-cpis/profile-service/iam-admin-services/iam-admin-services-cpi.thrift"
include "../service-cpis/profile-service/group-manager/group-manager-cpi.thrift"

namespace java org.apache.airavata.service.airavata
namespace php Airavata.Service.Airavata
namespace cpp apache.airavata.service.airavata
namespace perl ApacheAiravataService
namespace py airavata.service.airavata
namespace js ApacheAiravataService

/**
 * Airavata Service Interface Versions depend upon this Thrift Interface File. When making changes, please edit the
 *  Version Constants according to Semantic Versioning Specification (SemVer) http://semver.org.
 *
 * Note: The Airavata Service version may be different from the Airavata software release versions.
 *
 * The Airavata Service version is composed as a dot delimited string with major, minor, and patch level components.
 *
 *  - Major: Incremented for backward incompatible changes. An example would be changes to interfaces.
 *  - Minor: Incremented for backward compatible changes. An example would be the addition of a new optional methods.
 *  - Patch: Incremented for bug fixes. The patch level should be increased for every edit that doesn't result
 *              in a change to major/minor version numbers.
 *
*/
const string AIRAVATA_SERVICE_VERSION = "0.18.0"

// Service name constants for TMultiplexedProcessor
const string AIRAVATA_SERVICE_NAME = "Airavata"
const string REGISTRY_SERVICE_NAME = "RegistryService"
const string CREDENTIAL_STORE_SERVICE_NAME = "CredentialStoreService"
const string SHARING_REGISTRY_SERVICE_NAME = "SharingRegistryService"
const string ORCHESTRATOR_SERVICE_NAME = "OrchestratorService"
const string WORKFLOW_SERVICE_NAME = "Workflow"
const string USER_PROFILE_SERVICE_NAME = "UserProfileService"
const string TENANT_PROFILE_SERVICE_NAME = "TenantProfileService"
const string IAM_ADMIN_SERVICES_NAME = "IamAdminServices"
const string GROUP_MANAGER_SERVICE_NAME = "GroupManagerService"

/**
 * AiravataService is a multiplexed service that combines all Airavata services.
 * This service definition is used for documentation and client generation.
 * The actual implementation uses TMultiplexedProcessor to route calls to the appropriate service handlers.
 */
service AiravataService extends base_api.BaseAPI {
    /**
     * This service combines all Airavata services:
     * - Airavata API
     * - Registry Service
     * - Credential Store Service
     * - Sharing Registry Service
     * - Orchestrator Service
     * - Workflow Service
     * - User Profile Service
     * - Tenant Profile Service
     * - IAM Admin Services
     * - Group Manager Service
     * 
     * Clients should use TMultiplexedProtocol with the appropriate service name
     * to access specific service methods.
     */
    string getServiceVersion()
}
