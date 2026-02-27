/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.gateway.service;

import java.util.List;
import org.apache.airavata.core.exception.CoreExceptions.AiravataException;
import org.apache.airavata.core.exception.RegistryExceptions.RegistryException;
import org.apache.airavata.gateway.model.Gateway;
import org.apache.airavata.gateway.model.GatewayGroups;

/**
 * Service for managing Gateway entities. PK = gatewayId (UUID), slug = gatewayName.
 * Use getGateway(nameOrId) for lookup by either slug or PK.
 */
public interface GatewayService {

    // -------------------------------------------------------------------------
    // Gateway CRUD
    // -------------------------------------------------------------------------

    boolean isGatewayExist(String nameOrId) throws RegistryException;

    Gateway getGateway(String gatewayNameOrId) throws RegistryException;

    List<Gateway> getAllGateways() throws RegistryException;

    void deleteGateway(String gatewayName) throws RegistryException;

    String createGateway(Gateway gateway) throws RegistryException;

    void updateGateway(String gatewayName, Gateway gateway) throws RegistryException;

    // -------------------------------------------------------------------------
    // Gateway groups (group IDs stored on the gateway entity)
    // -------------------------------------------------------------------------

    boolean isGatewayGroupsExists(String gatewayId) throws RegistryException;

    GatewayGroups getGatewayGroups(String gatewayId) throws RegistryException;

    GatewayGroups createGatewayGroups(GatewayGroups gatewayGroups) throws RegistryException;

    GatewayGroups updateGatewayGroups(GatewayGroups gatewayGroups) throws RegistryException;

    void deleteGatewayGroups(String gatewayId) throws RegistryException;

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    void init() throws AiravataException;

    void initializeAfterMigration() throws AiravataException;
}
