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
 * Component Programming Interface definition for Apache Airavata Gateway profile Service.
 *
*/

include "../../../data-models/experiment-catalog-models/workspace_model.thrift"
include "profile_gateway_cpi_errors.thrift"

namespace java org.apache.airavata.service.profile.gateway.cpi

const string GATEWAY_PROFILE_CPI_VERSION = "0.17"
const string GATEWAY_PROFILE_CPI_NAME = "GatewayProfileService"

service GatewayProfileService {

    string getAPIVersion()
                        throws (1: profile_gateway_cpi_errors.GatewayProfileServiceException gatewayProfileException)

    string addGateway (1: required workspace_model.Gateway gateway)
                        throws (1: profile_gateway_cpi_errors.GatewayProfileServiceException gatewayProfileException)

    bool updateGateway(1: required string gatewayId, 2: required workspace_model.Gateway updatedGateway)
                        throws (1: profile_gateway_cpi_errors.GatewayProfileServiceException gatewayProfileException)

    workspace_model.Gateway getGateway (1: required string gatewayId)
                        throws (1: profile_gateway_cpi_errors.GatewayProfileServiceException gatewayProfileException)

    bool deleteGateway(1: required string gatewayId)
                        throws (1: profile_gateway_cpi_errors.GatewayProfileServiceException gatewayProfileException)

    list<workspace_model.Gateway> getAllGateways()
                        throws (1: profile_gateway_cpi_errors.GatewayProfileServiceException gatewayProfileException)

    bool isGatewayExist(1: required string gatewayId)
                        throws (1: profile_gateway_cpi_errors.GatewayProfileServiceException gatewayProfileException)
}