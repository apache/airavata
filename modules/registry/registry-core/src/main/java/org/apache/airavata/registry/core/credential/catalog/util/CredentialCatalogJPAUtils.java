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

package org.apache.airavata.registry.core.credential.catalog.util;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

public class CredentialCatalogJPAUtils {
    private final static Logger logger = LoggerFactory.getLogger(CredentialCatalogJPAUtils.class);

    private static final String PERSISTENCE_UNIT_NAME = "credentialcatalog_data";
    private static final String CREDCATALOG_JDBC_DRIVER = "credential.store.jdbc.driver";
    private static final String CREDCATALOG_JDBC_URL = "credential.store.jdbc.url";
    private static final String CREDCATALOG_JDBC_USER = "credential.store.jdbc.user";
    private static final String CREDCACATALOG_JDBC_PWD = "credential.store.jdbc.password";
    private static final String CREDCACATALOG_VALIDATION_QUERY = "credential.store.jdbc.validationQuery";

    @PersistenceUnit(unitName="credentialcatalog_data")
    protected static EntityManagerFactory factory;

    @PersistenceContext(unitName="credentialcatalog_data")
    private static EntityManager credCatEntityManager;

    public static EntityManager getEntityManager() throws ApplicationSettingsException {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + readServerProperties(CREDCATALOG_JDBC_DRIVER) + "," +
                    "Url=" + readServerProperties(CREDCATALOG_JDBC_URL) + "?autoReconnect=true," +
                    "Username=" + readServerProperties(CREDCATALOG_JDBC_USER) + "," +
                    "Password=" + readServerProperties(CREDCACATALOG_JDBC_PWD) +
                    ",validationQuery=" + readServerProperties(CREDCACATALOG_VALIDATION_QUERY);
            System.out.println(connectionProperties);
            Map<String, String> properties = new HashMap<String, String>();
            properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
            properties.put("openjpa.ConnectionProperties", connectionProperties);
            properties.put("openjpa.DynamicEnhancementAgent", "true");
            properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");
            properties.put("openjpa.RemoteCommitProvider","sjvm");
            properties.put("openjpa.Log","DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
            properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
            properties.put("openjpa.jdbc.QuerySQLCache", "false");
            properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72," +
                    " PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);
        }
        credCatEntityManager = factory.createEntityManager();
        return credCatEntityManager;
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
