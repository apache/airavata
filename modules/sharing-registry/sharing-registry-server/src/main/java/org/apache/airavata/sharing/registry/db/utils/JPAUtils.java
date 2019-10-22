/**
 *
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
 */
package org.apache.airavata.sharing.registry.db.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.airavata.common.utils.JDBCConfig;

public class JPAUtils {
    public static final String PERSISTENCE_UNIT_NAME = "airavata-sharing-registry";
    private static final JDBCConfig JDBC_CONFIG = new SharingRegistryJDBCConfig();
    private static final EntityManagerFactory factory = org.apache.airavata.common.utils.JPAUtils.getEntityManagerFactory(PERSISTENCE_UNIT_NAME, JDBC_CONFIG);

    public static EntityManager getEntityManager() {
        return factory.createEntityManager();
    }
}
