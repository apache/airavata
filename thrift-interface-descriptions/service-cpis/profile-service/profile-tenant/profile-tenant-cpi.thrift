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

/*
 * Component Programming Interface definition for Apache Airavata Tenant profile Service.
 *
*/

include "../../../data-models/experiment-catalog-models/workspace_model.thrift"
include "profile_tenant_cpi_errors.thrift"

namespace java org.apache.airavata.service.profile.tenant.cpi

const string TENANT_PROFILE_CPI_VERSION = "0.17"
const string TENANT_PROFILE_CPI_NAME = "TenantProfileService"

service TenantProfileService {

    string getAPIVersion()
                        throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tenantProfileException)

    string addGateway (1: required workspace_model.Gateway gateway)
                        throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tenantProfileException)

    bool updateGateway(1: required workspace_model.Gateway updatedGateway)
                        throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tenantProfileException)

    workspace_model.Gateway getGateway (1: required string gatewayId)
                        throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tenantProfileException)

    bool deleteGateway(1: required string gatewayId)
                        throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tenantProfileException)

    list<workspace_model.Gateway> getAllGateways()
                        throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tenantProfileException)

    bool isGatewayExist(1: required string gatewayId)
                        throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tenantProfileException)
}