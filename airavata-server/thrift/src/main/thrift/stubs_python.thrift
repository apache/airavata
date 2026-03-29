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

include "base-api/base_api.thrift"

include "data-models/credential_store_models.thrift"
include "data-models/status_models.thrift"
include "data-models/job_model.thrift"
include "data-models/experiment_model.thrift"
include "data-models/workspace_model.thrift"
include "data-models/scheduling_model.thrift"
include "data-models/application_deployment_model.thrift"
include "data-models/application_interface_model.thrift"
include "data-models/application_io_models.thrift"
include "data-models/parser_model.thrift"
include "data-models/account_provisioning_model.thrift"
include "data-models/compute_resource_model.thrift"
include "data-models/storage_resource_model.thrift"
include "data-models/gateway_resource_profile_model.thrift"
include "data-models/group_resource_profile_model.thrift"
include "data-models/user_resource_profile_model.thrift"
include "data-models/data_movement_models.thrift"
include "data-models/gateway_groups_model.thrift"
include "data-models/replica_catalog_models.thrift"
include "data-models/group_manager_model.thrift"
include "data-models/tenant_profile_model.thrift"
include "data-models/user_profile_model.thrift"
include "data-models/workflow_data_model.thrift"
include "data-models/sharing_models.thrift"

include "airavata-apis/airavata_api.thrift"
include "airavata-apis/airavata_commons.thrift"
include "airavata-apis/airavata_errors.thrift"
include "airavata-apis/db_event_model.thrift"
include "airavata-apis/messaging_events.thrift"
include "airavata-apis/security_model.thrift"
include "airavata-apis/workflow_api.thrift"

include "service-cpis/credential-store-cpi.thrift"
include "service-cpis/registry-api.thrift"
include "service-cpis/orchestrator-cpi.thrift"
include "service-cpis/sharing_cpi.thrift"
include "service-cpis/profile-service-cpi.thrift"
