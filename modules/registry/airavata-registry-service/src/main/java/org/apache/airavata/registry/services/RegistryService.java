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
package org.apache.airavata.registry.services;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserResource;
import org.apache.airavata.persistance.registry.jpa.resources.Utils;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;
import org.apache.airavata.registry.api.exception.RegistrySettingsException;
import org.apache.airavata.registry.api.util.RegistrySettings;
import org.apache.airavata.registry.services.utils.DatabaseCreator;
import org.apache.airavata.registry.services.utils.JdbcStorage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistryService implements ServiceLifeCycle {
    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    public static final String PERSISTANT_DATA = "Configuration";
    public static final String REGISTRY_DEFAULT_GATEWAY_ID = "default.registry.gateway";
    public static final String REGISTRY_DEFAULT_USER = "default.registry.user";
    public static final String REGISTRY_DEFAULT_USER_PASSWORD = "default.registry.password";
    public static final String DERBY_SERVER_MODE_SYS_PROPERTY = "derby.drda.startNetworkServer";
    public static final String REGISTRY_JDBC_DRIVER = "registry.jdbc.driver";
    public static final String REGISTRY_JDBC_URL = "registry.jdbc.url";
    public static final String REGISTRY_JDBC_USER = "registry.jdbc.user";
    public static final String REGISTRY_JDBC_PASSWORD = "registry.jdbc.password";
    private JdbcStorage db;
    private NetworkServerControl server;

    private static volatile boolean serverStarted = false;

    @Override
    public void startUp(ConfigurationContext configurationContext, AxisService axisService) {
        //todo have to read these properties from some configuration
        initializeDB();
        serverStarted = true;
    }

    private void startDerbyInServerMode() {
        try {
            System.setProperty(DERBY_SERVER_MODE_SYS_PROPERTY, "true");
            server = new NetworkServerControl(InetAddress.getByName("0.0.0.0"),
                    Utils.getPort(),
                    Utils.getJDBCUser(), Utils.getJDBCPassword());
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

    private void stopDerbyServer() {
        try {
            server.shutdown();
            serverStarted = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeDB() {
    	System.setProperty("registry.initialize.state", "0");
        String jdbcUrl = null;
        String jdbcDriver = null;
        try{
            jdbcDriver = RegistrySettings.getSetting(REGISTRY_JDBC_DRIVER);
            jdbcUrl = RegistrySettings.getSetting(REGISTRY_JDBC_URL);
            String jdbcUser = RegistrySettings.getSetting(REGISTRY_JDBC_USER);
            String jdbcPassword = RegistrySettings.getSetting(REGISTRY_JDBC_PASSWORD);
            jdbcUrl = jdbcUrl + "?" + "user=" + jdbcUser + "&" + "password=" + jdbcPassword;
        } catch (RegistrySettingsException e) {
            logger.error("Unable to read properties" , e);
        }

        if (Utils.getDBType().equals("derby") && Utils.isDerbyStartEnabled()) {
            startDerbyInServerMode();
        }
        db = new JdbcStorage(10, 50, jdbcUrl, jdbcDriver, true);

        Connection conn = null;
        try {
            conn = db.connect();
            if (!DatabaseCreator.isDatabaseStructureCreated(PERSISTANT_DATA, conn)) {
                DatabaseCreator.createRegistryDatabase(conn);
                logger.info("New Database created for Registry");
            } else {
                logger.info("Database already created for Registry!");
            }
            try{
                GatewayResource gatewayResource = new GatewayResource();
                gatewayResource.setGatewayName(RegistrySettings.getSetting(REGISTRY_DEFAULT_GATEWAY_ID));
                gatewayResource.setOwner(RegistrySettings.getSetting(REGISTRY_DEFAULT_GATEWAY_ID));
                gatewayResource.save();
                UserResource userResource = (UserResource) gatewayResource.create(ResourceType.USER);
                userResource.setUserName(RegistrySettings.getSetting(REGISTRY_DEFAULT_USER));
                userResource.setPassword(RegistrySettings.getSetting(REGISTRY_DEFAULT_USER_PASSWORD));
                userResource.save();
                WorkerResource workerResource = (WorkerResource) gatewayResource.create(ResourceType.GATEWAY_WORKER);
                workerResource.setUser(userResource.getUserName());
                workerResource.save();
            } catch (RegistrySettingsException e) {
                logger.error("Unable to read properties", e);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Database failure");
        } finally {
            db.closeConnection(conn);
            try {
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
                conn.close();
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        System.setProperty("registry.initialize.state", "1");
    }

    public boolean isRegistryServiceStarted() {
        return serverStarted;
    }

    @Override
    public void shutDown(ConfigurationContext configurationContext, AxisService axisService) {

    }
}
