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
package org.apache.airavata.registry.core.utils;

import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.model.workspace.Gateway;
import org.apache.airavata.model.workspace.GatewayApprovalStatus;
import org.apache.airavata.registry.core.repositories.expcatalog.GatewayRepository;
import org.apache.airavata.registry.core.repositories.expcatalog.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExpCatalogDBInitConfig implements DBInitConfig {

    @Autowired
    private AiravataServerProperties properties;

    private String dbInitScriptPrefix = "database_scripts/expcatalog";

    @Override
    public String getDriver() {
        return properties.getDatabase().getRegistry().getJdbcDriver();
    }

    @Override
    public String getUrl() {
        return properties.getDatabase().getRegistry().getJdbcUrl();
    }

    @Override
    public String getUser() {
        return properties.getDatabase().getRegistry().getJdbcUser();
    }

    @Override
    public String getPassword() {
        return properties.getDatabase().getRegistry().getJdbcPassword();
    }

    @Override
    public String getValidationQuery() {
        return properties.getDatabase().getValidationQuery();
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
            String defaultGatewayId = properties.getDefaultRegistry().getGateway();
            if (!gatewayRepository.isGatewayExist(defaultGatewayId)) {
                Gateway gateway = new Gateway();
                gateway.setGatewayId(defaultGatewayId);
                gateway.setGatewayApprovalStatus(GatewayApprovalStatus.APPROVED);
                gateway.setOauthClientId(properties.getDefaultRegistry().getOauthClientId());
                gateway.setOauthClientSecret(properties.getDefaultRegistry().getOauthClientSecret());
                gatewayRepository.addGateway(gateway);
            }

            UserRepository userRepository = new UserRepository();
            String defaultUsername = properties.getDefaultRegistry().getUser();
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
