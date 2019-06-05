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
import org.apache.airavata.model.appcatalog.gatewayprofile.GatewayResourceProfile;
import org.apache.airavata.model.appcatalog.userresourceprofile.UserResourceProfile;
import org.apache.airavata.registry.core.repositories.appcatalog.GwyResourceProfileRepository;
import org.apache.airavata.registry.core.repositories.appcatalog.UserResourceProfileRepository;

public class AppCatalogDBInitConfig implements DBInitConfig {

    private String dbInitScriptPrefix = "database_scripts/appcatalog";

    @Override
    public JDBCConfig getJDBCConfig() {
        return new AppCatalogJDBCConfig();
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

        GwyResourceProfileRepository gwyResourceProfileRepository = new GwyResourceProfileRepository();
        try {
            GatewayResourceProfile gatewayResourceProfile = new GatewayResourceProfile();
            gatewayResourceProfile.setGatewayID(ServerSettings.getDefaultUserGateway());
            if (!gwyResourceProfileRepository.isGatewayResourceProfileExists(gatewayResourceProfile.getGatewayID())) {
                gwyResourceProfileRepository.addGatewayResourceProfile(gatewayResourceProfile);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default gateway for app catalog", e);
        }
    }

}
