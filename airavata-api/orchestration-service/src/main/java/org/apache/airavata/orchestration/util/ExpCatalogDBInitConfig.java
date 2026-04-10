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
package org.apache.airavata.orchestration.util;

import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.db.DBInitConfig;
import org.apache.airavata.db.JDBCConfig;
import org.apache.airavata.interfaces.GatewayRegistry;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.GatewayApprovalStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExpCatalogDBInitConfig implements DBInitConfig {

    private String dbInitScriptPrefix = "database_scripts/expcatalog";

    @Autowired
    private GatewayRegistry gatewayRegistry;

    @Override
    public JDBCConfig getJDBCConfig() {
        return null;
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
            String defaultGatewayId = ServerSettings.getDefaultUserGateway();
            if (!gatewayRegistry.isGatewayExist(defaultGatewayId)) {
                Gateway gateway = Gateway.newBuilder()
                        .setGatewayId(defaultGatewayId)
                        .setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED)
                        .setOauthClientId(ServerSettings.getSetting("default.registry.oauth.client.id"))
                        .setOauthClientSecret(ServerSettings.getSetting("default.registry.oauth.client.secret"))
                        .build();
                gatewayRegistry.addGateway(gateway);
            }

            String defaultUsername = ServerSettings.getDefaultUser();
            if (!gatewayRegistry.isUserExists(defaultGatewayId, defaultUsername)) {
                UserProfile defaultUser = UserProfile.newBuilder()
                        .setUserId(defaultUsername)
                        .setGatewayId(defaultGatewayId)
                        .build();
                gatewayRegistry.addUser(defaultUser);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to post-initialize the expcatalog database", e);
        }
    }
}
