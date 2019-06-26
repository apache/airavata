/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.airavata.registry.core.utils;

import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.registry.core.repositories.expcatalog.GatewayRepository;
import org.apache.airavata.registry.core.repositories.expcatalog.UserRepository;

public class ExpCatalogDBInitConfig implements DBInitConfig {

    private String dbInitScriptPrefix = "database_scripts/expcatalog";

    @Override
    public JDBCConfig getJDBCConfig() {
        return new ExpCatalogJDBCConfig();
    }

    @Override
    public String getDBInitScriptPrefix() {
        return this.dbInitScriptPrefix;
    }

    public ExpCatalogDBInitConfig setDbInitScriptPrefix(String dbInitScriptPrefix) {
        this.dbInitScriptPrefix = dbInitScriptPrefix;
        return this;
    }

    @Override
    public String getCheckTableName() {
        return "CONFIGURATION";
    }

    @Override
    public void postInit() {

        try {
            // Create default gateway and default user if not already created
            GatewayRepository gatewayRepository = new GatewayRepository();
            String defaultGatewayId = ServerSettings.getDefaultUserGateway();
            if (!gatewayRepository.isGatewayExist(defaultGatewayId)) {
                Gateway gateway = new Gateway();
                gateway.setGatewayId(defaultGatewayId);
                gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
                gateway.setOauthClientId(ServerSettings.getSetting("default.registry.oauth.client.id"));
                gateway.setOauthClientSecret(ServerSettings.getSetting("default.registry.oauth.client.secret"));
                gatewayRepository.addGateway(gateway);
            }

            UserRepository userRepository = new UserRepository();
            String defaultUsername = ServerSettings.getDefaultUser();
            if (!userRepository.isUserExists(defaultGatewayId, defaultUsername)) {
                UserProfile defaultUser = new UserProfile();
                defaultUser.setUserId(defaultUsername);
                defaultUser.setGatewayId(defaultGatewayId);
                userRepository.addUser(defaultUser);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to post-initialize the expcatalog database", e);
        }
    }
}
