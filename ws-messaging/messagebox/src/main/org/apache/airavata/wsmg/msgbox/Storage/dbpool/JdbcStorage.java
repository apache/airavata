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

package org.apache.airavata.wsmg.msgbox.Storage.dbpool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.airavata.wsmg.msgbox.ConfigurationManager;
import org.apache.airavata.wsmg.msgbox.util.ConfigKeys;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is used by DatabaseStorageImpl for database access to msgBoxes table, this is the core jdbc implementation for
 * msgBox service
 */
public class JdbcStorage {
    private static Log log = LogFactory.getLog(JdbcStorage.class);
    private String jdbcUrl = null;

    private String messagePreservationDays;

    private String messagePreservationHours;

    private String messagePreservationMinutes;

    private PreparedStatement stmt = null;

    private ResultSet rs = null;

    private ResultSetMetaData rsmd = null;

    // private Connection conn = null;
    private ConnectionPool connectionPool;

    private String jdbcDriver;

    public JdbcStorage(boolean enableTransactions, ConfigurationManager configs) {

        jdbcUrl = configs.getConfig(ConfigKeys.MSG_BOX_JDBC_URL);
        jdbcDriver = configs.getConfig(ConfigKeys.JDBC_DRIVER);
        this.messagePreservationDays = configs.getConfig(ConfigKeys.MSG_PRESV_DAYS);
        this.messagePreservationHours = configs.getConfig(ConfigKeys.MSG_PRESV_HRS);
        this.messagePreservationMinutes = configs.getConfig(ConfigKeys.MSG_PRESV_MINS);

        try {
            if (enableTransactions) {
                connectionPool = new ConnectionPool(jdbcDriver, jdbcUrl, 10, 50, true, false,
                        Connection.TRANSACTION_SERIALIZABLE);
            } else {
                connectionPool = new ConnectionPool(jdbcDriver, jdbcUrl, 10, 50, true);
            }
//            String value = System.getProperty("setup");
//            if(value != null){
                DatabaseCreator dbCreator = new DatabaseCreator(connectionPool);
                if(!dbCreator.isDatabaseStructureCreated("SELECT * from msgBoxes")){
                    dbCreator.createMsgBoxDatabase();
                }else{
                    log.error("Database already created !");
                }

//            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            throw new RuntimeException("Failed to create database connection poll.", e);
        }

    }

    public Connection connect() throws SQLException {

        Connection conn = connectionPool.getConnection();
        return conn;
    }

    public void closeConnection(Connection conn) throws java.sql.SQLException {

        connectionPool.free(conn);
    }

    public int update(String query) throws java.sql.SQLException {
        int result = 0;
        // connect();
        Connection conn = connectionPool.getConnection();
        stmt = conn.prepareStatement(query);
        result = stmt.executeUpdate();
        stmt.close();
        connectionPool.free(conn);

        return result;
    }

    /**
     * This method is provided so that yo can have better control over the statement. For example: You can use
     * stmt.setString to convert quotation mark automatically in an INSERT statement
     */
    public int insert(PreparedStatement stmt) throws java.sql.SQLException {
        int rows = 0;

        rows = stmt.executeUpdate();
        stmt.close();
        return rows;
    }

    public int insert(String query) throws java.sql.SQLException {
        int rows = 0;

        Connection conn = connectionPool.getConnection();
        stmt = conn.prepareStatement(query);
        rows = stmt.executeUpdate();
        stmt.close();
        connectionPool.free(conn);

        return rows;
    }

    public ResultSet query(String query) throws SQLException {
        Connection conn = connectionPool.getConnection();
        // Create a scrollable ResultSet so that I can use rs.last() to get
        // total number of rows without using another COUNT in SQL query
        Statement lstmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = lstmt.executeQuery(query);
        connectionPool.free(conn);
        return rs;
    }

    public int countRow(String tableName, String columnName) throws java.sql.SQLException {
        String query = new String("SELECT COUNT(" + columnName + ") FROM " + tableName);
        Connection conn = connectionPool.getConnection();
        stmt = conn.prepareStatement(query);
        rs = stmt.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        stmt.close();
        connectionPool.free(conn);
        return count;
    }

    public int getMessagePreservationDays() {
        return Integer.parseInt(messagePreservationDays);
    }

    public int getMessagePreservationHours() {
        return Integer.parseInt(messagePreservationHours);
    }

    public int getMessagePreservationMinutes() {
        return Integer.parseInt(messagePreservationMinutes);
    }

    public long getInterval() {
        long interval = this.getMessagePreservationDays() * 24;
        interval = (interval + this.getMessagePreservationHours()) * 60;
        interval = (interval + this.getMessagePreservationMinutes()) * 60;
        interval = interval * 1000;
        return interval;
    }

    public void closeAllConnections() {
        if (connectionPool != null)
            connectionPool.closeAllConnections();
    }
}
