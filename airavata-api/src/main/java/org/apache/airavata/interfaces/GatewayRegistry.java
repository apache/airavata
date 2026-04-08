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
package org.apache.airavata.interfaces;

import java.util.List;
import org.apache.airavata.model.appcatalog.gatewaygroups.proto.GatewayGroups;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.model.workspace.proto.Gateway;

/**
 * Registry operations for gateways, gateway groups, and users.
 */
public interface GatewayRegistry {

    // --- Gateway operations ---
    String addGateway(Gateway gateway) throws Exception;

    Gateway getGateway(String gatewayId) throws Exception;

    boolean isGatewayExist(String gatewayId) throws Exception;

    boolean updateGateway(String gatewayId, Gateway updatedGateway) throws Exception;

    boolean deleteGateway(String gatewayId) throws Exception;

    List<Gateway> getAllGateways() throws Exception;

    // --- User operations ---
    boolean isUserExists(String gatewayId, String userName) throws Exception;

    List<String> getAllUsersInGateway(String gatewayId) throws Exception;

    String addUser(UserProfile userProfile) throws Exception;

    // --- Gateway groups operations ---
    boolean isGatewayGroupsExists(String gatewayId) throws Exception;

    GatewayGroups getGatewayGroups(String gatewayId) throws Exception;

    void createGatewayGroups(GatewayGroups gatewayGroups) throws Exception;

    void updateGatewayGroups(GatewayGroups gatewayGroups) throws Exception;
}
