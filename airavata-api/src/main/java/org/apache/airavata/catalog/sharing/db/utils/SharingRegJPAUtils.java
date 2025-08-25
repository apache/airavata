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
package org.apache.airavata.catalog.sharing.db.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.common.utils.JPAUtils;

public class SharingRegJPAUtils {
    private static final String PERSISTENCE_UNIT_NAME = "airavata-sharing-registry";
    private static final JDBCConfig JDBC_CONFIG = new SharingRegistryJDBCConfig();
    private static final EntityManagerFactory factory =
            JPAUtils.getEntityManagerFactory(PERSISTENCE_UNIT_NAME, JDBC_CONFIG);
    private static EntityManager entityManagerInstance = null;

    public static synchronized EntityManager getEntityManager() {
        if (entityManagerInstance == null || !entityManagerInstance.isOpen()) {
            entityManagerInstance = factory.createEntityManager();
        }
        return entityManagerInstance;
    }
}
