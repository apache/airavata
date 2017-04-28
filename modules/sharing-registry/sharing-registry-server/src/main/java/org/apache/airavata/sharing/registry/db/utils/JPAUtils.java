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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.sharing.registry.models.SharingRegistryException;
import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class JPAUtils {
    private final static Logger logger = LoggerFactory.getLogger(JPAUtils.class);

    public static final String PERSISTENCE_UNIT_NAME = "airavata-sharing-registry";
    public static final String SHARING_REG_JDBC_DRIVER = "sharingcatalog.jdbc.driver";
    public static final String SHARING_REG_JDBC_URL = "sharingcatalog.jdbc.url";
    public static final String SHARING_REG_JDBC_USER = "sharingcatalog.jdbc.user";
    public static final String SHARING_REG_JDBC_PWD = "sharingcatalog.jdbc.password";
    public static final String SHARING_REG_VALIDATION_QUERY = "sharingcatalog.validationQuery";
    public static final String JPA_CACHE_SIZE = "jpa.cache.size";
    public static final String JPA_CACHE_ENABLED = "cache.enable";

    public static final String CONFIGURATION = "CONFIGURATION";
    public static final String START_DERBY_ENABLE = "start.derby.server.mode";
    public static final String DERBY_SERVER_MODE_SYS_PROPERTY = "derby.drda.startNetworkServer";
    private static NetworkServerControl server;
    private static JdbcStorage db;
    private static String jdbcURl;
    private static String jdbcDriver;
    private static String jdbcUser;
    private static String jdbcPassword;

    @PersistenceUnit(unitName = PERSISTENCE_UNIT_NAME)
    protected static EntityManagerFactory factory;
    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    private static EntityManager entityManager;

    public synchronized static EntityManager getEntityManager() throws SharingRegistryException {
        if (factory == null) {
            String connectionProperties = "DriverClassName=" + readServerProperties(SHARING_REG_JDBC_DRIVER) + "," +
                    "Url=" + readServerProperties(SHARING_REG_JDBC_URL) + "?autoReconnect=true," +
                    "Username=" + readServerProperties(SHARING_REG_JDBC_USER) + "," +
                    "Password=" + readServerProperties(SHARING_REG_JDBC_PWD) +
                    ",validationQuery=" + readServerProperties(SHARING_REG_VALIDATION_QUERY);

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

    public static void initializeDB() throws SharingRegistryException {
        jdbcDriver = readServerProperties(SHARING_REG_JDBC_DRIVER);
        jdbcURl = readServerProperties(SHARING_REG_JDBC_URL);
        jdbcUser = readServerProperties(SHARING_REG_JDBC_USER);
        jdbcPassword = readServerProperties(SHARING_REG_JDBC_PWD);
        jdbcURl = jdbcURl + "?" + "user=" + jdbcUser + "&" + "password=" + jdbcPassword;

        if (getDBType(jdbcURl).equals("derby") && isDerbyStartEnabled()) {
            startDerbyInServerMode();
        }
        db = new JdbcStorage(10, 50, jdbcURl, jdbcDriver, true);

        Connection conn = null;
        try {
            conn = db.connect();
            if (!DatabaseCreator.isDatabaseStructureCreated(CONFIGURATION, conn)) {
                DatabaseCreator.createRegistryDatabase("database_scripts/sharing-registry", conn);
                logger.info("New Database created for Sharing Catalog !!! ");
            } else {
                logger.info("Database already created for Sharing Catalog !!!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Database failure", e);
        } finally {
            db.closeConnection(conn);
            try {
                if(conn != null){
                    if (!conn.getAutoCommit()) {
                        conn.commit();
                    }
                    conn.close();
                }
            } catch (SQLException e) {
                logger.error("Error while closing database connection...", e.getMessage(), e);
            }
        }
    }

    public static String getDBType(String jdbcUrl){
        try{
            String cleanURI = jdbcUrl.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getScheme();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static boolean isDerbyStartEnabled(){
        try {
            String s = ServerSettings.getSetting(START_DERBY_ENABLE);
            if("true".equals(s)){
                return true;
            }
        }  catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata server properties", e.getMessage(), e);
            return false;
        }
        return false;
    }

    public static void startDerbyInServerMode() {
        try {
            System.setProperty(DERBY_SERVER_MODE_SYS_PROPERTY, "true");
            server = new NetworkServerControl(InetAddress.getByName("0.0.0.0"),
                    getPort(jdbcURl),
                    jdbcUser, jdbcPassword);
            java.io.PrintWriter consoleWriter = new java.io.PrintWriter(System.out, true);
            server.start(consoleWriter);
        } catch (IOException e) {
            logger.error("Unable to start Apache derby in the server mode! Check whether " +
                    "specified port is available");
        } catch (Exception e) {
            logger.error("Unable to start Apache derby in the server mode! Check whether " +
                    "specified port is available");
        }
    }

    public static void stopDerbyInServerMode() {
        System.setProperty(DERBY_SERVER_MODE_SYS_PROPERTY, "false");
        if (server!=null){
            try {
                server.shutdown();
            } catch (Exception e) {
                logger.error("Error when stopping the derby server : "+e.getLocalizedMessage());
            }
        }
    }

    public static int getPort(String jdbcURL){
        try{
            String cleanURI = jdbcURL.substring(5);
            URI uri = URI.create(cleanURI);
            return uri.getPort();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return -1;
        }
    }

    public static String readServerProperties(String propertyName) throws SharingRegistryException {
        try {
            return ServerSettings.getSetting(propertyName);
        } catch (ApplicationSettingsException e) {
            logger.error("Unable to read airavata-server.properties...", e);
            throw new SharingRegistryException("Unable to read airavata-server.properties...");
        }
    }
}