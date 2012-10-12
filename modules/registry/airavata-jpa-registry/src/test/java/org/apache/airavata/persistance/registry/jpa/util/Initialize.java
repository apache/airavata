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

package org.apache.airavata.persistance.registry.jpa.util;

import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.resources.GatewayResource;
import org.apache.airavata.persistance.registry.jpa.resources.UserResource;
import org.apache.airavata.persistance.registry.jpa.resources.Utils;
import org.apache.airavata.persistance.registry.jpa.resources.WorkerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.derby.drda.NetworkServerControl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.sql.*;
import java.util.Properties;
import java.util.StringTokenizer;

public class Initialize {
    private static final Logger logger = LoggerFactory.getLogger(Initialize.class);
    public static final String GATEWAY_ID = "gateway.id";
    public static final String REGISTRY_USER = "registry.user";
    public static final String REGISTRY_PASSWORD = "registry.password";
    public static final String DERBY_SERVER_MODE_SYS_PROPERTY = "derby.drda.startNetworkServer";
    private NetworkServerControl server;
    private static final String delimiter = ";";

    public static boolean checkStringBufferEndsWith(StringBuffer buffer, String suffix) {
        if (suffix.length() > buffer.length()) {
            return false;
        }
        // this loop is done on purpose to avoid memory allocation performance
        // problems on various JDKs
        // StringBuffer.lastIndexOf() was introduced in jdk 1.4 and
        // implementation is ok though does allocation/copying
        // StringBuffer.toString().endsWith() does massive memory
        // allocation/copying on JDK 1.5
        // See http://issues.apache.org/bugzilla/show_bug.cgi?id=37169
        int endIndex = suffix.length() - 1;
        int bufferIndex = buffer.length() - 1;
        while (endIndex >= 0) {
            if (buffer.charAt(bufferIndex) != suffix.charAt(endIndex)) {
                return false;
            }
            bufferIndex--;
            endIndex--;
        }
        return true;
    }

    public void initializeDB() {
        String jdbcUrl = null;
        String jdbcDriver = null;
        URL resource = this.getClass().getClassLoader().getResource("repository.properties");
        Properties properties = new Properties();
        try {
            properties.load(resource.openStream());
        } catch (IOException e) {
            System.out.println("Unable to read repository properties");
        }
        jdbcDriver = properties.getProperty("registry.jdbc.driver");
        jdbcUrl = properties.getProperty("registry.jdbc.url");
        String jdbcUser = properties.getProperty("registry.jdbc.user");
        String jdbcPassword = properties.getProperty("registry.jdbc.password");
        jdbcUrl = jdbcUrl + "?" + "user=" + jdbcUser + "&" + "password=" + jdbcPassword;


        if (Utils.getDBType().equals("derby")) {
            startDerbyInServerMode();
//            startDerbyInEmbeddedMode();
        }


        Connection conn = null;
        try {
            Class.forName(Utils.getJDBCDriver()).newInstance();
            conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
            executeSQLScript(conn);
            System.out.println("New Database created for Registry");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("Database failure");
        } finally {
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

    private void executeSQLScript(Connection conn) throws Exception {
        StringBuffer sql = new StringBuffer();
        BufferedReader reader = null;
        try{

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("data-derby.sql");
        reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("//")) {
                continue;
            }
            if (line.startsWith("--")) {
                continue;
            }
            StringTokenizer st = new StringTokenizer(line);
            if (st.hasMoreTokens()) {
                String token = st.nextToken();
                if ("REM".equalsIgnoreCase(token)) {
                    continue;
                }
            }
            sql.append(" ").append(line);

            // SQL defines "--" as a comment to EOL
            // and in Oracle it may contain a hint
            // so we cannot just remove it, instead we must end it
            if (line.indexOf("--") >= 0) {
                sql.append("\n");
            }
            if ((checkStringBufferEndsWith(sql, delimiter))) {
                executeSQL(sql.substring(0, sql.length() - delimiter.length()), conn);
                sql.replace(0, sql.length(), "");
            }
        }
        // Catch any statements not followed by ;
        if (sql.length() > 0) {
            executeSQL(sql.toString(), conn);
        }
        }catch (IOException e){
            logger.error("Error occurred while executing SQL script for creating Airavata database", e);
            throw new Exception("Error occurred while executing SQL script for creating Airavata database", e);
        }finally {
            if (reader != null) {
                reader.close();
            }

        }

    }

    private static void executeSQL(String sql, Connection conn) throws Exception {
        // Check and ignore empty statements
        if ("".equals(sql.trim())) {
            return;
        }

        Statement statement = null;
        try {
            logger.debug("SQL : " + sql);

            boolean ret;
            int updateCount = 0, updateCountTotal = 0;
            statement = conn.createStatement();
            ret = statement.execute(sql);
            updateCount = statement.getUpdateCount();
            do {
                if (!ret) {
                    if (updateCount != -1) {
                        updateCountTotal += updateCount;
                    }
                }
                ret = statement.getMoreResults();
                if (ret) {
                    updateCount = statement.getUpdateCount();
                }
            } while (ret);

            logger.debug(sql + " : " + updateCountTotal + " rows affected");

            SQLWarning warning = conn.getWarnings();
            while (warning != null) {
                logger.info(warning + " sql warning");
                warning = warning.getNextWarning();
            }
            conn.clearWarnings();
        } catch (SQLException e) {
            if (e.getSQLState().equals("X0Y32")) {
                // eliminating the table already exception for the derby
                // database
                logger.info("Table Already Exists", e);
            } else {
                throw new Exception("Error occurred while executing : " + sql, e);
            }
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    logger.error("Error occurred while closing result set.", e);
                }
            }
        }
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

    private void startDerbyInEmbeddedMode(){
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            DriverManager.getConnection("jdbc:derby:memory:unit-testing-jpa;create=true").close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
