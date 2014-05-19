/*
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
 *
 */

package org.apache.aiaravata.application.catalog.data.util;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class JPAUtils {
    private final static Logger logger = LoggerFactory.getLogger(JPAUtils.class);
    private static final String PERSISTENCE_UNIT_NAME = "appcatalog_data";
    private static final String APPCATALOG_JDBC_DRIVER = "appcatalog.jdbc.driver";
    private static final String APPCATALOG_JDBC_URL = "appcatalog.jdbc.url";
    private static final String APPCATALOG_JDBC_USER = "appcatalog.jdbc.user";
    private static final String APPCATALOG_JDBC_PWD = "appcatalog.jdbc.password";
    private static final String APPCATALOG_VALIDATION_QUERY = "appcatalog.validationQuery";
    private static final String JPA_CONNECTION_PROPERTIES = "jpa.connection.properties";
    protected static EntityManagerFactory factory;

    public static EntityManager getEntityManager() throws ApplicationSettingsException {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + readServerProperties(APPCATALOG_JDBC_DRIVER) + "," +
                    "Url=" + readServerProperties(APPCATALOG_JDBC_URL) + "," +
                    "Username=" + readServerProperties(APPCATALOG_JDBC_USER) + "," +
                    "Password=" + readServerProperties(APPCATALOG_JDBC_PWD) +
                    ",validationQuery=" + readServerProperties(APPCATALOG_VALIDATION_QUERY) + "," +
                    readServerProperties(JPA_CONNECTION_PROPERTIES);
            System.out.println(connectionProperties);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
            properties.put("openjpa.Log", "SQL=ERROR");
            properties.put("openjpa.Log","DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
            properties.put("openjpa.ReadLockLevel", "none");
            properties.put("openjpa.WriteLockLevel", "none");
            properties.put("openjpa.LockTimeout", "30000");
            properties.put("openjpa.LockManager", "none");
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72, PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=60000");
            properties.put("openjpa.jdbc.QuerySQLCache", "false");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }
        return factory.createEntityManager();
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
