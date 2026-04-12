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
import org.apache.airavata.interfaces.SharingFacade;
import org.apache.airavata.model.user.proto.UserProfile;
import org.apache.airavata.model.workspace.proto.Gateway;
import org.apache.airavata.model.workspace.proto.GatewayApprovalStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExpCatalogDBInitConfig implements DBInitConfig {

    private static final Logger logger = LoggerFactory.getLogger(ExpCatalogDBInitConfig.class);

    private String dbInitScriptPrefix = "database_scripts/expcatalog";

    @Autowired
    private GatewayRegistry gatewayRegistry;

    @Autowired
    private SharingFacade sharingFacade;

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

    private void initializeSharingForGateway(String gatewayId) {
        // Domain
        tryCreate("sharing domain", () ->
                sharingFacade.createDomain(gatewayId, "Gateway " + gatewayId, "Sharing domain for " + gatewayId));

        // Entity types
        String[] entityTypes = {"PROJECT", "EXPERIMENT", "FILE", "APPLICATION_DEPLOYMENT", "GROUP_RESOURCE_PROFILE", "CREDENTIAL_TOKEN"};
        for (String et : entityTypes) {
            tryCreate("entity type " + et, () ->
                    sharingFacade.createEntityType(gatewayId + ":" + et, gatewayId, et, et + " entity type"));
        }

        // Permission types
        String[] permTypes = {"READ", "WRITE", "MANAGE_SHARING"};
        for (String pt : permTypes) {
            tryCreate("permission type " + pt, () ->
                    sharingFacade.createPermissionType(gatewayId + ":" + pt, gatewayId, pt, pt + " permission type"));
        }

        logger.info("Sharing initialized for gateway: {}", gatewayId);
    }

    private void tryCreate(String label, ThrowingRunnable action) {
        try {
            action.run();
            logger.info("Created {}", label);
        } catch (Exception e) {
            logger.debug("{} already exists: {}", label, e.getMessage());
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    @Override
    @jakarta.annotation.PostConstruct
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
                logger.info("Created default gateway: {}", defaultGatewayId);
            }

            // Initialize sharing domain, entity types, and permission types (idempotent)
            initializeSharingForGateway(defaultGatewayId);

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
