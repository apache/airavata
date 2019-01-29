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
import org.apache.airavata.registry.core.utils.ExpCatalogJDBCConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

public class ExpCatalogJPAUtils {
    private final static Logger logger = LoggerFactory.getLogger(ExpCatalogJPAUtils.class);

    private static final String PERSISTENCE_UNIT_NAME = "experiment_data_new";
    private static final JDBCConfig JDBC_CONFIG = new ExpCatalogJDBCConfig();
    @PersistenceUnit(unitName=PERSISTENCE_UNIT_NAME)
    protected static EntityManagerFactory factory;
    @PersistenceContext(unitName=PERSISTENCE_UNIT_NAME)
    private static EntityManager expCatEntityManager;


    public static EntityManager getEntityManager() {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + JDBC_CONFIG.getDriver() + "," +
                    "Url=" + JDBC_CONFIG.getURL() + "?autoReconnect=true," +
                    "Username=" + JDBC_CONFIG.getUser() + "," +
                    "Password=" + JDBC_CONFIG.getPassword() +
                    ",validationQuery=" + JDBC_CONFIG.getValidationQuery();
            System.out.println(connectionProperties);
            Map<String, String> properties = new HashMap<>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
//            properties.put("openjpa.DataCache","" + Utils.isCachingEnabled() + "(CacheSize=" + Utils.getJPACacheSize() + ", SoftReferenceSize=0)");
//            properties.put("openjpa.QueryCache","" + Utils.isCachingEnabled() + "(CacheSize=" + Utils.getJPACacheSize() + ", SoftReferenceSize=0)");
//            properties.put("javax.persistence.sharedCache.mode","ALL");
            properties.put("openjpa.RemoteCommitProvider", "sjvm");
            properties.put("openjpa.Log", "DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            properties.put("openjpa.DetachState", "all");
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72, PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
//			properties.put("openjpa.jdbc.QuerySQLCache", "false");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }
        expCatEntityManager = factory.createEntityManager();
        return expCatEntityManager;
    }
}
