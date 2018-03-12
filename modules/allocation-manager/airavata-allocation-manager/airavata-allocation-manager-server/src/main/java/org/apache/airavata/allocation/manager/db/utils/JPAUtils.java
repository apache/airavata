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
package org.apache.airavata.allocation.manager.db.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.allocation.manager.models.AllocationManagerException;
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
    public static final String PERSISTENCE_UNIT_NAME = "airavata-allocation-manager-server";
    
     @PersistenceUnit(unitName = PERSISTENCE_UNIT_NAME)
    protected static EntityManagerFactory factory;
    @PersistenceContext(unitName = PERSISTENCE_UNIT_NAME)
    private static EntityManager entityManager;
    
    public synchronized static EntityManager getEntityManager() throws Exception {
       if(factory==null){
    	   String connectionProperties = "DriverClassName = com.mysql.jdbc.Driver" + "," +
                   "Url=jdbc:mysql://localhost:3306/resource"+ "?autoReconnect=true," +
                   "Username=root" + "," + "Password=root";

    	Map<String, String> properties = new HashMap<String, String>();
        properties.put("openjpa.ConnectionDriverName", "org.apache.commons.dbcp.BasicDataSource");
        properties.put("openjpa.ConnectionProperties", connectionProperties);
        properties.put("openjpa.DynamicEnhancementAgent", "true");
        properties.put("openjpa.RuntimeUnenhancedClasses", "unsupported");

//        properties.put("openjpa.DataCache", "" + readServerProperties(JPA_CACHE_ENABLED)
//                + "(CacheSize=" + Integer.valueOf(readServerProperties(JPA_CACHE_SIZE)) + ", SoftReferenceSize=0)");
//        properties.put("openjpa.QueryCache", "" + readServerProperties(JPA_CACHE_ENABLED)
//                + "(CacheSize=" + Integer.valueOf(readServerProperties(JPA_CACHE_SIZE)) + ", SoftReferenceSize=0)");

      //  properties.put("openjpa.RemoteCommitProvider", "sjvm");
        properties.put("openjpa.Log", "DefaultLevel=INFO, Runtime=INFO, Tool=INFO, SQL=INFO");
        properties.put("openjpa.jdbc.SynchronizeMappings", "buildSchema(ForeignKeys=true)");
        properties.put("openjpa.jdbc.QuerySQLCache", "false");
//        properties.put("openjpa.Multithreaded", "true");
        properties.put("openjpa.ConnectionFactoryProperties", "PrettyPrint=true, PrettyPrintLineLength=72," +
                " PrintParameters=true, MaxActive=10, MaxIdle=5, MinIdle=2, MaxWait=31536000,  autoReconnect=true");
        properties.put("openjpa.RuntimeUnenhancedClasses", "warn");
        	factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
         entityManager = factory.createEntityManager();
        return entityManager;
    }

    public static void initializeDB() throws Exception {
    		String jdbcDriver = "com.mysql.jdbc.Driver";
        String jdbcURl = "jdbc:mysql://localhost:3306/resource";
        String jdbcUser = "root";
        String jdbcPassword = "root";
        jdbcURl = jdbcURl + "?" + "user=" + jdbcUser + "&" + "password=" + jdbcPassword;

       
        JdbcStorage db = new JdbcStorage(10, 50, jdbcURl, jdbcDriver, true);

        Connection conn = null;
        try {
            conn = db.connect();
            if (!DatabaseCreator.isDatabaseStructureCreated("USER_DETAILS", conn)) {
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
}
