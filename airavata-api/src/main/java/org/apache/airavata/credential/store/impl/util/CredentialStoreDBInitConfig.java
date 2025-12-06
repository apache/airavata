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
package org.apache.airavata.credential.store.impl.util;

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
        String driver = properties.getDatabase().getCredentialStore().getJdbcDriver();
        if (driver == null || driver.isEmpty()) {
            driver = properties.getDatabase().getRegistry().getJdbcDriver();
        }
        return driver;
    }

    @Override
    public String getUrl() {
        String url = properties.getDatabase().getCredentialStore().getJdbcUrl();
        if (url == null || url.isEmpty()) {
            url = properties.getDatabase().getRegistry().getJdbcUrl();
        }
        return url;
    }

    @Override
    public String getUser() {
        String user = properties.getDatabase().getCredentialStore().getJdbcUser();
        if (user == null || user.isEmpty()) {
            user = properties.getDatabase().getRegistry().getJdbcUser();
        }
        return user;
    }

    @Override
    public String getPassword() {
        String password = properties.getDatabase().getCredentialStore().getJdbcPassword();
        if (password == null || password.isEmpty()) {
            password = properties.getDatabase().getRegistry().getJdbcPassword();
        }
        return password;
    }

    @Override
    public String getValidationQuery() {
        return properties.getDatabase().getCredentialStore().getJdbcValidationQuery();
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
