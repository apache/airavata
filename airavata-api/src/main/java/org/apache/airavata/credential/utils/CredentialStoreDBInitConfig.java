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
package org.apache.airavata.credential.utils;

import org.apache.airavata.common.utils.DBInitConfig;
import org.apache.airavata.config.AiravataServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CredentialStoreDBInitConfig implements DBInitConfig {

    @Autowired
    private AiravataServerProperties properties;

    @Override
    public String getDriver() {
        String driver = properties.database.vault.driver;
        if (driver == null || driver.isEmpty()) {
            driver = properties.database.registry.driver;
        }
        return driver;
    }

    @Override
    public String getUrl() {
        String url = properties.database.vault.url;
        String driver = properties.database.vault.driver;
        org.slf4j.LoggerFactory.getLogger(CredentialStoreDBInitConfig.class)
                .debug("CredentialStore - URL: {}, Driver: {}", url, driver);
        if (url == null || url.isEmpty()) {
            url = properties.database.registry.url;
            org.slf4j.LoggerFactory.getLogger(CredentialStoreDBInitConfig.class)
                    .debug("Using registry URL as fallback: {}", url);
        }
        if (url == null || url.isEmpty()) {
            throw new IllegalStateException("JDBC URL is not configured for credential store or registry database");
        }
        if (!url.startsWith("jdbc:")) {
            throw new IllegalStateException("Invalid JDBC URL format. Expected URL starting with 'jdbc:', got: " + url
                    + ". Driver is: " + driver);
        }
        return url;
    }

    @Override
    public String getUser() {
        String user = properties.database.vault.user;
        if (user == null || user.isEmpty()) {
            user = properties.database.registry.user;
        }
        return user;
    }

    @Override
    public String getPassword() {
        String password = properties.database.vault.password;
        if (password == null || password.isEmpty()) {
            password = properties.database.registry.password;
        }
        return password;
    }

    @Override
    public String getValidationQuery() {
        return properties.database.vault.validationQuery;
    }

    @Override
    public String getDBInitScriptPrefix() {
        return "database_scripts/credstore";
    }

    @Override
    public String getCheckTableName() {
        return "CREDENTIALS";
    }
}
