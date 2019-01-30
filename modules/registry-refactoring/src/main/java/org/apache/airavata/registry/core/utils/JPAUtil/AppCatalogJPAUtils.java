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

import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.registry.core.utils.AppCatalogJDBCConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.HashMap;
import java.util.Map;

public class AppCatalogJPAUtils {

    private final static Logger logger = LoggerFactory.getLogger(AppCatalogJPAUtils.class);
    // TODO: we can rename this back to appcatalog_data once we completely replace the other appcatalog_data persistence context in airavata-registry-core
    private static final String PERSISTENCE_UNIT_NAME = "appcatalog_data_new";
    private static final JDBCConfig JDBC_CONFIG = new AppCatalogJDBCConfig();
    @PersistenceUnit(unitName = PERSISTENCE_UNIT_NAME)
    protected static EntityManagerFactory factory;
    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    private static EntityManager appCatEntityManager;

    public static EntityManager getEntityManager() {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + JDBC_CONFIG.getDriver() + "," +
                    "Url=" + JDBC_CONFIG.getURL() + "?autoReconnect=true," +
                    "Username=" + JDBC_CONFIG.getUser() + "," +
                    "Password=" + JDBC_CONFIG.getPassword() +
                    ",validationQuery=" + JDBC_CONFIG.getValidationQuery();
            System.out.println(connectionProperties);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
            properties.put("openjpa.RemoteCommitProvider", "sjvm");
            properties.put("openjpa.Log", "DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
            properties.put("openjpa.jdbc.SynchronizeMappings", "validate");
            properties.put("openjpa.jdbc.QuerySQLCache", "false");
            properties.put("openjpa.DetachState", "all");
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72, PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }
        // clear cache at entitymangerfactory level
        if (factory.getCache() != null) {
            factory.getCache().evictAll();
        }
        appCatEntityManager = factory.createEntityManager();
        // clear the entitymanager cache
        if (appCatEntityManager != null) {
            appCatEntityManager.clear();
        }
        return appCatEntityManager;
    }
}
