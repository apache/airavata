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

include "../../../airavata-apis/airavata_errors.thrift"
include "../../../airavata-apis/security_model.thrift"
include "../../../data-models/experiment-catalog-models/workspace_model.thrift"
include "profile_tenant_cpi_errors.thrift"
include "../../../base-api/base_api.thrift"

namespace java org.apache.airavata.service.profile.tenant.cpi
namespace php Airavata.Service.Profile.Tenant.CPI
namespace py airavata.service.profile.tenant.cpi

const string TENANT_PROFILE_CPI_VERSION = "0.18.0"
const string TENANT_PROFILE_CPI_NAME = "TenantProfileService"

service TenantProfileService extends base_api.BaseAPI {

    /**
     * Return the airavataInternalGatewayId assigned to given gateway.
     */
    string addGateway (1: required security_model.AuthzToken authzToken,
                       2: required workspace_model.Gateway gateway)
                    throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tpe,
                            2: airavata_errors.AuthorizationException ae)

    bool updateGateway (1: required security_model.AuthzToken authzToken,
                        2: required workspace_model.Gateway updatedGateway)
                     throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tpe,
                             2: airavata_errors.AuthorizationException ae)

    workspace_model.Gateway getGateway (1: required security_model.AuthzToken authzToken,
                                        2: required string airavataInternalGatewayId)
                                     throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tpe,
                                             2: airavata_errors.AuthorizationException ae)

    bool deleteGateway (1: required security_model.AuthzToken authzToken,
                        2: required string airavataInternalGatewayId,
                        3: required string gatewayId)
                     throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tpe,
                             2: airavata_errors.AuthorizationException ae)

    list<workspace_model.Gateway> getAllGateways (1: required security_model.AuthzToken authzToken)
                                               throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tpe,
                                                       2: airavata_errors.AuthorizationException ae)

    bool isGatewayExist (1: required security_model.AuthzToken authzToken,
                         2: required string gatewayId)
                      throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tpe,
                              2: airavata_errors.AuthorizationException ae)

    list<workspace_model.Gateway> getAllGatewaysForUser (1: required security_model.AuthzToken authzToken,
                                                         2: required string requesterUsername)
                                               throws (1: profile_tenant_cpi_errors.TenantProfileServiceException tpe,
                                                       2: airavata_errors.AuthorizationException ae)
}
