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

package org.apache.airavata.sample.gateway;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.sample.gateway.userstore.GatewayUserStore;
import org.apache.airavata.sample.gateway.userstore.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 7/31/13
 * Time: 9:12 PM
 */

public class SampleGateway {

    private DBUtil dbUtil;

    private GatewayUserStore gatewayUserStore;

    public static final String GATEWAY_SESSION = "Gateway";

    private static boolean databaseStarted = false;

    protected static Logger log = LoggerFactory.getLogger(GatewayUserStore.class);

    public SampleGateway(ServletContext servletContext) throws Exception {

        int port = Integer.parseInt(servletContext.getInitParameter("jdbc.port"));

        String userName = servletContext.getInitParameter("jdbc.user");
        String password = servletContext.getInitParameter("jdbc.password");
        String host = servletContext.getInitParameter("jdbc.host");
        String driver = servletContext.getInitParameter("jdbc.driver");

        if (!databaseStarted) {
            // Start the database
            DerbyUtil.startDerbyInServerMode(host,
                    port, userName,
                    password);
        }


        String jdbcUrl = getJDBCUrl(host, port, userName, password);

        // Create the dbutil class
        dbUtil = new DBUtil(jdbcUrl,
                userName,
                password,
                driver);

        if (!databaseStarted) {
            GatewayUserStore.initializeData(dbUtil);
            databaseStarted = true;
        }

        gatewayUserStore = new GatewayUserStore(dbUtil);

    }

    public List<User> getAllUsers() {
        return gatewayUserStore.getUsers();
    }

    public static String getJDBCUrl(String host, int port, String user, String password) {
        return new StringBuilder().append("jdbc:derby://").append(host).append(":").append(port)
                .append("/persistent_data;create=true;user=").append(user).append(";password=")
                .append(password).toString();
    }

    private static void waitTillServerStarts(String jdbcUrl, String userName, String password, String driver) {
        DBUtil dbUtil = null;

        try {
            dbUtil = new DBUtil(jdbcUrl, userName, password, driver);
        } catch (Exception e) {
            // ignore
        }

        Connection connection = null;
        try {
            if (dbUtil != null) {
                connection = dbUtil.getConnection();
            }
        } catch (Throwable e) {
            // ignore
        }

        while (connection == null) {
            try {
                Thread.sleep(1000);
                try {
                    if (dbUtil != null) {
                        connection = dbUtil.getConnection();
                    }
                } catch (SQLException e) {
                    // ignore
                }
            } catch (InterruptedException e) {
                // ignore
            }
        }

    }

    public static boolean isAdmin(String user) {
        return user.equals("admin");
    }

    public void updateTokenId(String tokenId) {
        this.gatewayUserStore.updateTokens(tokenId);
    }

    public String getTokenIdForUser(String user) {
        return this.gatewayUserStore.getUserToken(user);
    }


    public boolean authenticate(String userName, String password) {

        Connection connection = null;
        try {
            connection = dbUtil.getConnection();
            String storedPassword = this.gatewayUserStore.getPassword(userName, connection);

            if (password.equals(storedPassword)) {
                return true;
            }


        } catch (SQLException e) {
            log.error("An error occurred while authentication", e);
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error while closing the connection", e);
                }
            }
        }

        return false;

    }


}
