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
package org.apache.airavata.common.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.DriverManager;
import org.apache.derby.drda.NetworkServerControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * This class includes methods to start stop Derby database. Mainly used for tests.
 */
public class DerbyUtil {

    private static NetworkServerControl server;

    private static final Logger logger = LoggerFactory.getLogger(DerbyUtil.class);

    public static final String DERBY_SERVER_MODE_SYS_PROPERTY = "derby.drda.startNetworkServer";

    /**
     * Starts new derby server instance with given configurations.
     * 
     * @param hostAddress
     *            The host address start the server.
     * @param port
     *            The port number which server is starting.
     * @param user
     *            JDBC user name.
     * @param password
     *            JDBC password.
     * @throws Exception
     *             If an error occurred while starting the server.
     */
    public static void startDerbyInServerMode(String hostAddress, int port, String user, String password)
            throws Exception {
        PrintWriter consoleWriter = null;

        try {
            System.setProperty(DERBY_SERVER_MODE_SYS_PROPERTY, "true");
            server = new NetworkServerControl(InetAddress.getByName(hostAddress), port, user, password);
            consoleWriter = new PrintWriter(System.out, true);
            server.start(consoleWriter);

        } catch (IOException e) {
            logger.error("Unable to start Apache derby in the server mode! Check whether "
                    + "specified port is available", e);
            throw e;
        } catch (Exception e) {
            logger.error("Unable to start Apache derby in the server mode! Check whether "
                    + "specified port is available", e);
            throw e;
        } finally {

            if (consoleWriter != null) {
                consoleWriter.close();
            }

        }

    }

    /**
     * Starts derby server in embedded mode.
     * 
     * @throws ClassNotFoundException
     *             If specified driver not found in the class path.
     * @throws SQLException
     *             If an error occurred while creat
     */
    public static void startDerbyInEmbeddedMode() throws ClassNotFoundException, SQLException {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        DriverManager.getConnection("jdbc:derby:memory:unit-testing-jpa;create=true").close();
    }

    /**
     * Shuts down the server.
     * 
     * @throws Exception
     *             If an error occurred while shutting down.
     */
    public static void stopDerbyServer() throws Exception {
        try {
            server.shutdown();
        } catch (Exception e) {
            logger.error("Error shutting down derby server.", e);
            throw e;
        }
    }
}
