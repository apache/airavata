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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An abstraction for database specific test classes. This will create a database and provides methods to execute SQLs.
 */
public class DatabaseTestCases {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseTestCases.class);

    protected static String hostAddress = "localhost";
    protected static int port = 20000;
    protected static String userName = "admin";
    protected static String password = "admin";
    protected static String driver = "org.apache.derby.jdbc.ClientDriver";

    public static String getHostAddress() {
        return hostAddress;
    }

    public static int getPort() {
        return port;
    }

    public static String getUserName() {
        return userName;
    }

    public static String getPassword() {
        return password;
    }

    public static String getDriver() {
        return driver;
    }

    public static String getJDBCUrl() {
        return new StringBuilder().append("jdbc:derby://").append(getHostAddress()).append(":").append(getPort())
                .append("/experiment_catalog;create=true;user=").append(getUserName()).append(";password=")
                .append(getPassword()).toString();
    }

    public static void waitTillServerStarts() {
        DBUtil dbUtil = null;

        try {
            dbUtil = new DBUtil(getJDBCUrl(), getUserName(), getPassword(), getDriver());
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

    public static void executeSQL(String sql) throws Exception {
        DBUtil dbUtil = new DBUtil(getJDBCUrl(), getUserName(), getPassword(), getDriver());
        dbUtil.executeSQL(sql);
    }

    public DBUtil getDbUtil () throws Exception {
        return new DBUtil(getJDBCUrl(), getUserName(), getPassword(), getDriver());

    }

    public Connection getConnection() throws Exception {

        DBUtil dbUtil =  getDbUtil ();
        Connection connection = dbUtil.getConnection();
        connection.setAutoCommit(true);
        return connection;

    }

}
