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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

public class ExpCatalogJPAUtils {
    private final static Logger logger = LoggerFactory.getLogger(ExpCatalogJPAUtils.class);

    private static final String PERSISTENCE_UNIT_NAME = "experiment_data_new";
    private static final String EXPCATALOG_JDBC_DRIVER = "registry.jdbc.driver";
    private static final String EXPCATALOG_JDBC_URL = "registry.jdbc.url";
    private static final String EXPCATALOG_JDBC_USER = "registry.jdbc.user";
    private static final String EXPCATALOG_JDBC_PASSWORD = "registry.jdbc.password";
    private static final String EXPCATALOG_VALIDATION_QUERY = "validationQuery";
    private static final String JPA_CACHE_SIZE = "jpa.cache.size";
    private static final String JPA_CACHE_ENABLED = "cache.enable";
    @PersistenceUnit(unitName=PERSISTENCE_UNIT_NAME)
    protected static EntityManagerFactory factory;
    @PersistenceContext(unitName=PERSISTENCE_UNIT_NAME)
    private static EntityManager expCatEntityManager;


    public static EntityManager getEntityManager() throws ApplicationSettingsException {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + readServerProperties(EXPCATALOG_JDBC_DRIVER) + "," +
                    "Url=" + readServerProperties(EXPCATALOG_JDBC_URL) + "?autoReconnect=true," +
                    "Username=" + readServerProperties(EXPCATALOG_JDBC_USER) + "," +
                    "Password=" + readServerProperties(EXPCATALOG_JDBC_PASSWORD) +
                    ",validationQuery=" + readServerProperties(EXPCATALOG_VALIDATION_QUERY);
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
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72, PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
//			properties.put("openjpa.jdbc.QuerySQLCache", "false");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }
        expCatEntityManager = factory.createEntityManager();
        return expCatEntityManager;
    }

    private static String readServerProperties(String propertyName) throws ApplicationSettingsException {
        try {
            return ServerSettings.getSetting(propertyName);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server.properties...", e);
            throw new ApplicationSettingsException("Unable to read airavata-server.properties...");
        }
    }

}
