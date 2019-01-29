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

import org.apache.airavata.common.utils.JDBCConfig;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

public class JPAUtils {

    public static final String PERSISTENCE_UNIT_NAME = "airavata-sharing-registry";
    private static final JDBCConfig JDBC_CONFIG = new SharingRegistryJDBCConfig();

    @PersistenceUnit(unitName = PERSISTENCE_UNIT_NAME)
    protected static EntityManagerFactory factory;
    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    private static EntityManager entityManager;

    public synchronized static EntityManager getEntityManager() throws SharingRegistryException {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + JDBC_CONFIG.getDriver() + "," +
                    "Url=" + JDBC_CONFIG.getURL() + "?autoReconnect=true," +
                    "Username=" + JDBC_CONFIG.getUser() + "," +
                    "Password=" + JDBC_CONFIG.getPassword() +
                    ",validationQuery=" + JDBC_CONFIG.getValidationQuery();

//            String connectionProperties = "DriverClassName=com.mysql.jdbc.Driver," +
//                    "Url=jdbc:mysql://localhost:3306/airavata_sharing_catalog?autoReconnect=true," +
//                    "Username=root," +
//                    "Password=," +
//                    ",validationQuery=SELECT 1 FROM CONFIGURATION";

            Map<String, String> properties = new HashMap<String, String>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");

//            properties.put("openjpa.DataCache", "" + readServerProperties(JPA_CACHE_ENABLED)
//                    + "(CacheSize=" + Integer.valueOf(readServerProperties(JPA_CACHE_SIZE)) + ", SoftReferenceSize=0)");
//            properties.put("openjpa.QueryCache", "" + readServerProperties(JPA_CACHE_ENABLED)
//                    + "(CacheSize=" + Integer.valueOf(readServerProperties(JPA_CACHE_SIZE)) + ", SoftReferenceSize=0)");

            properties.put("openjpa.RemoteCommitProvider", "sjvm");
            properties.put("openjpa.Log", "DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            properties.put("openjpa.jdbc.QuerySQLCache", "false");
//            properties.put("openjpa.Multithreaded", "true");
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72," +
                    " PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "warn");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }

        entityManager = factory.createEntityManager();
        return entityManager;
    }
}
