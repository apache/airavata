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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WorkflowCatalogDBInitConfig implements DBInitConfig {

    @Autowired
    private AiravataServerProperties properties;

    private String dbInitScriptPrefix = "database_scripts/airavataworkflowcatalog";

    @Override
    public String getDriver() {
        return properties.getDatabase().getWorkflowCatalog().getJdbcDriver();
    }

    @Override
    public String getUrl() {
        return properties.getDatabase().getWorkflowCatalog().getJdbcUrl();
    }

    @Override
    public String getUser() {
        return properties.getDatabase().getWorkflowCatalog().getJdbcUser();
    }

    @Override
    public String getPassword() {
        return properties.getDatabase().getWorkflowCatalog().getJdbcPassword();
    }

    @Override
    public String getValidationQuery() {
        return properties.getDatabase().getWorkflowCatalog().getValidationQuery();
    }

    @Override
    public String getDBInitScriptPrefix() {
        return dbInitScriptPrefix;
    }

    public WorkflowCatalogDBInitConfig setDbInitScriptPrefix(String dbInitScriptPrefix) {
        this.dbInitScriptPrefix = dbInitScriptPrefix;
        return this;
    }

    @Override
    public String getCheckTableName() {
        return "CONFIGURATION";
    }
}
