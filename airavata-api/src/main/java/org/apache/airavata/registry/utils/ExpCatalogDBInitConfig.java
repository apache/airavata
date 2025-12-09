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
package org.apache.airavata.registry.utils;

import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.registry.exceptions.RegistryException;
import org.apache.airavata.registry.services.GatewayService;
import org.apache.airavata.registry.services.UserService;
import org.springframework.stereotype.Component;

@Component
public class ExpCatalogDBInitConfig implements DBInitConfig {

    private final AiravataServerProperties properties;
    private final GatewayService gatewayService;
    private final UserService userService;

    public ExpCatalogDBInitConfig(
            AiravataServerProperties properties, GatewayService gatewayService, UserService userService) {
        this.properties = properties;
        this.gatewayService = gatewayService;
        this.userService = userService;
    }

    private String dbInitScriptPrefix = "database_scripts/expcatalog";

    @Override
    public String getDriver() {
        return properties.database.registry.driver;
    }

    @Override
    public String getUrl() {
        return properties.database.registry.url;
    }

    @Override
    public String getUser() {
        return properties.database.registry.user;
    }

    @Override
    public String getPassword() {
        return properties.database.registry.password;
    }

    @Override
    public String getValidationQuery() {
        return properties.database.registry.validationQuery;
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
            String defaultGatewayId = properties.services.default_.gateway;
            if (!gatewayService.isGatewayExist(defaultGatewayId)) {
                Gateway gateway = new Gateway();
                gateway.setGatewayId(defaultGatewayId);
                gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
                gateway.setOauthClientId(properties.security.iam.oauthClientId);
                gateway.setOauthClientSecret(properties.security.iam.oauthClientSecret);
                gatewayService.addGateway(gateway);
            }

            String defaultUsername = properties.services.default_.user;
            if (!userService.isUserExists(defaultGatewayId, defaultUsername)) {
                UserProfile defaultUser = new UserProfile();
                defaultUser.setUserId(defaultUsername);
                defaultUser.setGatewayId(defaultGatewayId);
                userService.addUser(defaultUser);
            }

        } catch (RegistryException e) {
            throw new RuntimeException("Failed to post-initialize the expcatalog database", e);
        }
    }
}
