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

import java.lang.reflect.Method;
import org.apache.airavata.config.ServerSettings;
import org.apache.airavata.db.DBInitConfig;
import org.apache.airavata.db.JDBCConfig;
import org.apache.airavata.model.appcatalog.gatewayprofile.proto.GatewayResourceProfile;

public class AppCatalogDBInitConfig implements DBInitConfig {

    private static final String GWY_RESOURCE_PROFILE_REPO_CLASS =
            "org.apache.airavata.compute.repository.GwyResourceProfileRepository";

    private String dbInitScriptPrefix = "database_scripts/appcatalog";

    @Override
    public JDBCConfig getJDBCConfig() {
        return null;
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
            GatewayResourceProfile gatewayResourceProfile = GatewayResourceProfile.newBuilder()
                    .setGatewayId(ServerSettings.getDefaultUserGateway())
                    .build();

            Class<?> repoClass = Class.forName(GWY_RESOURCE_PROFILE_REPO_CLASS);
            Object repo = repoClass.getDeclaredConstructor().newInstance();
            Method existsMethod = repoClass.getMethod("isGatewayResourceProfileExists", String.class);
            boolean exists = (boolean) existsMethod.invoke(repo, gatewayResourceProfile.getGatewayId());
            if (!exists) {
                Method addMethod = repoClass.getMethod("addGatewayResourceProfile", GatewayResourceProfile.class);
                addMethod.invoke(repo, gatewayResourceProfile);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default gateway for app catalog", e);
        }
    }
}
