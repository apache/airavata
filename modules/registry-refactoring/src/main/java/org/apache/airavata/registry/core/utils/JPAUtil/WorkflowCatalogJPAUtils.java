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

public class WorkflowCatalogJPAUtils {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowCatalogJPAUtils.class);

    private static final String PERSISTENCE_UNIT_NAME = "workflowcatalog_data";
    private static final String WFCATALOG_JDBC_DRIVER = "workflowcatalog.jdbc.driver";
    private static final String WFCATALOG_JDBC_URL = "workflowcatalog.jdbc.url";
    private static final String WFCATALOG_JDBC_USER = "workflowcatalog.jdbc.user";
    private static final String WFCATALOG_JDBC_PASSWORD = "workflowcatalog.jdbc.password";
    private static final String WFCATALOG_VALIDATION_QUERY = "workflowcatalog.validationQuery";
    private static final String JPA_CACHE_SIZE = "jpa.cache.size";
    private static final String JPA_CACHE_ENABLED = "cache.enable";
    @PersistenceUnit(unitName=PERSISTENCE_UNIT_NAME)
    protected static EntityManagerFactory factory;
    @PersistenceContext(unitName=PERSISTENCE_UNIT_NAME)
    private static EntityManager wfCatEntityManager;

    public static EntityManager getEntityManager() throws ApplicationSettingsException {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + readServerProperties(WFCATALOG_JDBC_DRIVER) + "," +
                    "Url=" + readServerProperties(WFCATALOG_JDBC_URL) + "?autoReconnect=true," +
                    "Username=" + readServerProperties(WFCATALOG_JDBC_USER) + "," +
                    "Password=" + readServerProperties(WFCATALOG_JDBC_PASSWORD) +
                    ",validationQuery=" + readServerProperties(WFCATALOG_VALIDATION_QUERY);
            System.out.println(connectionProperties);
            Map<String, String> properties = new HashMap<>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
            properties.put("openjpa.RemoteCommitProvider","sjvm");
            properties.put("openjpa.Log","DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            properties.put("openjpa.jdbc.QuerySQLCache", "false");
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72, PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }
        wfCatEntityManager = factory.createEntityManager();
        return wfCatEntityManager;
    }

    private static String readServerProperties (String propertyName) throws ApplicationSettingsException {
        try {
            return ServerSettings.getSetting(propertyName);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server.properties...", e);
            throw new ApplicationSettingsException("Unable to read airavata-server.properties...");
        }
    }

}
