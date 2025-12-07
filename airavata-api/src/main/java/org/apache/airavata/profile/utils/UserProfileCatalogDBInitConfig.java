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
package org.apache.airavata.profile.utils;

import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.config.AiravataServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserProfileCatalogDBInitConfig implements DBInitConfig {

    @Autowired
    private AiravataServerProperties properties;

    @Override
    public String getDriver() {
        return properties.getDatabase().getProfileService().getJdbcDriver();
    }

    @Override
    public String getUrl() {
        return properties.getDatabase().getProfileService().getJdbcUrl();
    }

    @Override
    public String getUser() {
        return properties.getDatabase().getProfileService().getJdbcUser();
    }

    @Override
    public String getPassword() {
        return properties.getDatabase().getProfileService().getJdbcPassword();
    }

    @Override
    public String getValidationQuery() {
        return properties.getDatabase().getProfileService().getValidationQuery();
    }

    @Override
    public String getDBInitScriptPrefix() {
        return "database_scripts/user-profile-catalog";
    }

    @Override
    public String getCheckTableName() {
        return "CONFIGURATION";
    }
}
