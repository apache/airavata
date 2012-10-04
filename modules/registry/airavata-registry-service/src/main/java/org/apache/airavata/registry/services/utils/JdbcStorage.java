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
package org.apache.airavata.registry.services.utils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcStorage {
    private static Logger log = LoggerFactory.getLogger(JdbcStorage.class);

    private ConnectionPool connectionPool;

    public JdbcStorage(String jdbcUrl, String jdbcDriver) {
        // default init connection and max connection
        this(3, 50, jdbcUrl, jdbcDriver, true);
    }

    public JdbcStorage(int initCon, int maxCon, String url, String driver, boolean enableTransactions) {
        try {
            if (enableTransactions) {
                connectionPool = new ConnectionPool(driver, url, initCon, maxCon, true, false,
                        Connection.TRANSACTION_SERIALIZABLE);
            } else {
                connectionPool = new ConnectionPool(driver, url, initCon, maxCon, true);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create database connection pool.", e);
        }
    }

    /**
     * Check if this connection pool is auto commit or not
     *
     * @return
     */
    public boolean isAutoCommit() {
        return connectionPool.isAutoCommit();
    }

    public void commit(Connection conn) {
        try {
            if (conn != null && !conn.getAutoCommit()) {
                conn.commit();
            }
        } catch (SQLException sqle) {
            log.error("Cannot commit data", sqle);
        }
    }

    public void commitAndFree(Connection conn) {
        commit(conn);
        closeConnection(conn);
    }

    public void rollback(Connection conn) {
        try {
            if (conn != null && !conn.getAutoCommit()) {
                conn.rollback();
            }
        } catch (SQLException sqle) {
            log.error("Cannot Rollback data", sqle);
        }
    }

    public void rollbackAndFree(Connection conn) {
        rollback(conn);
        closeConnection(conn);
    }

    public Connection connect() {

        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            log.error(e.getMessage(), e);
        }
        return conn;
    }

    /**
     * This method is provided so that you can have better control over the statement. For example: You can use
     * stmt.setString to convert quotation mark automatically in an UPDATE statement
     *
     * NOTE: Statement is closed after execution
     */
    public int executeUpdateAndClose(PreparedStatement stmt) throws SQLException {
        int rows = 0;
        try {
            rows = stmt.executeUpdate();
            if (rows == 0) {
                log.info("Problem: 0 rows affected by insert/update/delete statement.");
            }
        } finally {
            stmt.close();
        }
        return rows;
    }

    public int countRow(String tableName, String columnName) throws SQLException {
        String query = new String("SELECT COUNT(" + columnName + ") FROM " + tableName);
        int count = -1;
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            count = rs.getInt(1);
            commit(conn);
        } catch (SQLException sql) {
            rollback(conn);
            throw sql;
        } finally {
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }
            } finally {
                closeConnection(conn);
            }
        }
        return count;
    }

    public void quietlyClose(Connection conn, Statement... stmts) {
        if (stmts != null) {
            for (Statement stmt : stmts) {
                try {
                    if (stmt != null && !stmt.isClosed()) {
                        stmt.close();
                    }
                } catch (SQLException sql) {
                    log.error(sql.getMessage(), sql);
                }
            }
        }
        closeConnection(conn);
    }

    public void closeConnection(Connection conn) {
        if (conn != null) {
            connectionPool.free(conn);
        }
    }

    public void closeAllConnections() {
        if (connectionPool != null)
            connectionPool.dispose();
    }

    public void shutdown() {
        connectionPool.shutdown();
    }
}
