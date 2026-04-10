/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import org.apache.airavata.db.DBUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MariaDBContainer;

/**
 * Base class for database tests using a singleton Testcontainers MariaDB.
 * Provides a real MariaDB instance via Docker for integration testing.
 */
@Tag("integration")
@Execution(ExecutionMode.SAME_THREAD)
public class DatabaseTestCases {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseTestCases.class);

    protected static String driver = "org.mariadb.jdbc.Driver";

    private static MariaDBContainer<?> mariadb() {
        return SharedMariaDB.getInstance();
    }

    public static String getHostAddress() {
        return mariadb().getHost();
    }

    public static int getPort() {
        return mariadb().getFirstMappedPort();
    }

    public static String getUserName() {
        return mariadb().getUsername();
    }

    public static String getPassword() {
        return mariadb().getPassword();
    }

    public static String getDriver() {
        return driver;
    }

    public static String getJDBCUrl() {
        return mariadb().getJdbcUrl();
    }

    public static void waitTillServerStarts() {
        // Testcontainers waits for readiness automatically
    }

    public static void executeSQL(String sql) throws Exception {
        try (Connection conn = DriverManager.getConnection(getJDBCUrl(), getUserName(), getPassword());
                Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    public DBUtil getDbUtil() throws Exception {
        return new DBUtil(getJDBCUrl(), getUserName(), getPassword(), getDriver());
    }

    public Connection getConnection() throws Exception {
        DBUtil dbUtil = getDbUtil();
        Connection connection = dbUtil.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }
}
