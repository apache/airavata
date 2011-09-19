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

package org.apache.airavata.wsmg.commons.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcStorage {
    private static Logger log = LoggerFactory.getLogger(JdbcStorage.class);

    private PreparedStatement stmt = null;

    private ResultSet rs = null;

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

    public void closeConnection(Connection conn) {
        connectionPool.free(conn);
    }

    public int update(String query) throws SQLException {
        int result = 0;
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(query);
            result = stmt.executeUpdate();
        } finally {
            if (conn != null) {
                connectionPool.free(conn);
            }

            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        }
        return result;
    }

    /**
     * This method is provided so that you can have better control over the
     * statement. For example: You can use stmt.setString to convert quotation
     * mark automatically in an UPDATE statement
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

    public ResultSet query(String query) throws SQLException {
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            conn.setAutoCommit(false);
        } finally {
            if (conn != null) {
                connectionPool.free(conn);
            }
        }
        return rs;
    }

    public void close() throws SQLException {
        if (stmt != null && !stmt.isClosed()) {
            stmt.close();
        }
    }

    public int countRow(String tableName, String columnName) throws SQLException {
        String query = new String("SELECT COUNT(" + columnName + ") FROM " + tableName);
        int count = -1;
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            rs.next();
            count = rs.getInt(1);
        } finally {
            if (conn != null) {
                connectionPool.free(conn);
            }
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        }
        return count;
    }

    /**
     * @param query
     * @return
     * @throws SQLException
     */
    public int insert(String query) throws SQLException {
        int rows = 0;
        Connection conn = null;
        try {
            conn = connectionPool.getConnection();
            stmt = conn.prepareStatement(query);
            rows = stmt.executeUpdate();
        } finally {
            if (conn != null) {
                connectionPool.free(conn);
            }
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
        }
        return rows;
    }

    /**
     * This method is provided so that yo can have better control over the
     * statement. For example: You can use stmt.setString to convert quotation
     * mark automatically in an INSERT statement
     * 
     * NOTE: Statement is closed after execution
     */
    public int insertAndClose(PreparedStatement stmt) throws SQLException {
        int rows = 0;
        try {
            rows = stmt.executeUpdate();
        } finally {
            stmt.close();
        }
        return rows;
    }

    public void closeAllConnections() {
        if (connectionPool != null)
            connectionPool.dispose();
    }
}
