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
package org.apache.airavata.registry.core.utils.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.common.utils.JPAUtils;
import org.apache.airavata.registry.core.utils.ReplicaCatalogJDBCConfig;

public class RepCatalogJPAUtils {

    // TODO: we can rename this back to replicacatalog_data once we completely replace the other replicacatalog_data persistence context in airavata-registry-core
    public static final String PERSISTENCE_UNIT_NAME = "replicacatalog_data_new";
    private static final JDBCConfig JDBC_CONFIG = new ReplicaCatalogJDBCConfig();
    private static final EntityManagerFactory factory = JPAUtils.getEntityManagerFactory(PERSISTENCE_UNIT_NAME, JDBC_CONFIG);

    public static EntityManager getEntityManager() {
        return factory.createEntityManager();
    }
}
