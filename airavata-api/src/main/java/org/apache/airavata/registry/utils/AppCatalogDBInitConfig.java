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
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.registry.services.GwyResourceProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppCatalogDBInitConfig implements DBInitConfig {

    @Autowired
    private AiravataServerProperties properties;

    @Autowired
    private GwyResourceProfileService gwyResourceProfileService;

    private String dbInitScriptPrefix = "database_scripts/appcatalog";

    @Override
    public String getDriver() {
        return properties.database.catalog.driver;
    }

    @Override
    public String getUrl() {
        return properties.database.catalog.url;
    }

    @Override
    public String getUser() {
        return properties.database.catalog.user;
    }

    @Override
    public String getPassword() {
        return properties.database.catalog.password;
    }

    @Override
    public String getValidationQuery() {
        return properties.database.catalog.validationQuery;
    }

    @Override
    public String getDBInitScriptPrefix() {
        return dbInitScriptPrefix;
    }

    public AppCatalogDBInitConfig setDbInitScriptPrefix(String dbInitScriptPrefix) {
        this.dbInitScriptPrefix = dbInitScriptPrefix;
        return this;
    }

    @Override
    public String getCheckTableName() {
        return "GATEWAY_PROFILE";
    }

    @Override
    public void postInit() {
        try {
            GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
            gatewayResourceProfile.setGatewayID(properties.services.default_.gateway);
            if (!gwyResourceProfileService.isGatewayResourceProfileExists(gatewayResourceProfile.getGatewayID())) {
                gwyResourceProfileService.addGatewayResourceProfile(gatewayResourceProfile);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default gateway for app catalog", e);
        }
    }
}
