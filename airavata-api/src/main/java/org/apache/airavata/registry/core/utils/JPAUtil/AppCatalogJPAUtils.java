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
package org.apache.airavata.registry.core.utils.JPAUtil;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.airavata.common.utils.JPAUtils;
import org.apache.airavata.config.AiravataServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AppCatalogJPAUtils {

    // TODO: we can rename this back to appcatalog_data once we completely replace
    // the other appcatalog_data persistence context in airavata-registry-core
    public static final String PERSISTENCE_UNIT_NAME = "appcatalog_data_new";

    private static AppCatalogJPAUtils instance;
    private EntityManagerFactory factory;

    @Autowired
    private AiravataServerProperties properties;

    @PostConstruct
    public void init() {
        instance = this;
        var db = properties.getDatabase().getAppCatalog();
        factory = JPAUtils.getEntityManagerFactory(
                PERSISTENCE_UNIT_NAME,
                db.getJdbcDriver(),
                db.getJdbcUrl(),
                db.getJdbcUser(),
                db.getJdbcPassword(),
                db.getValidationQuery());
    }

    public static EntityManager getEntityManager() {
        if (instance == null || instance.factory == null) {
            throw new IllegalStateException("AppCatalogJPAUtils not initialized. Make sure it's a Spring bean.");
        }
        return instance.factory.createEntityManager();
    }
}
