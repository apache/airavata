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

import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserResource;
import org.apache.airavata.persistance.registry.jpa.resources.Utils;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;
import org.apache.airavata.registry.services.utils.DatabaseCreator;
import org.apache.airavata.registry.services.utils.JdbcStorage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class RegistryService implements ServiceLifeCycle {
    private static final Logger logger = LoggerFactory.getLogger(RegistryService.class);

    public static final String PERSISTANT_DATA = "Configuration";
    public static final String GATEWAY_ID = "gateway.id";
    public static final String REGISTRY_USER = "registry.user";
    public static final String REGISTRY_PASSWORD = "registry.password";
    public static final String DERBY_SERVER_MODE_SYS_PROPERTY = "derby.drda.startNetworkServer";
    private JdbcStorage db;
    private NetworkServerControl server;

    @Override
    public void startUp(ConfigurationContext configurationContext, AxisService axisService) {
        //todo have to read these properties from some configuration
        initializeDB();
    }

    private void startDerbyInServerMode() {
        try {
            System.setProperty(DERBY_SERVER_MODE_SYS_PROPERTY, "true");
            server = new NetworkServerControl(InetAddress.getByName(Utils.getHost()),
                    Utils.getPort(),
                    Utils.getJDBCUser(), Utils.getJDBCUser());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeDB() {
        String jdbcUrl = null;
        String jdbcDriver = null;
        URL resource = this.getClass().getClassLoader().getResource("airavata-server.properties");
        Properties properties = new Properties();
        try {
            properties.load(resource.openStream());
        } catch (IOException e) {
            logger.error("Unable to read repository properties", e);
        }
        jdbcDriver = properties.getProperty("registry.jdbc.driver");
        jdbcUrl = properties.getProperty("registry.jdbc.url");
        String jdbcUser = properties.getProperty("registry.jdbc.user");
        String jdbcPassword = properties.getProperty("registry.jdbc.password");
        jdbcUrl = jdbcUrl + "?" + "user=" + jdbcUser + "&" + "password=" + jdbcPassword;

        if (Utils.getDBType().equals("derby")) {
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
        GatewayResource gatewayResource = new GatewayResource();
        gatewayResource.setGatewayName((String) properties.get(GATEWAY_ID));
        gatewayResource.setOwner((String) properties.get(GATEWAY_ID));
        gatewayResource.save();
        UserResource userResource = (UserResource) gatewayResource.create(ResourceType.USER);
        userResource.setUserName((String) properties.get(REGISTRY_USER));
        userResource.setPassword((String) properties.get(REGISTRY_PASSWORD));
        userResource.save();
        WorkerResource workerResource = (WorkerResource) gatewayResource.create(ResourceType.GATEWAY_WORKER);
        workerResource.setUser(userResource.getUserName());
        workerResource.save();
    }

    @Override
    public void shutDown(ConfigurationContext configurationContext, AxisService axisService) {
    }
}
