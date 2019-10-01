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

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Properties;

/**
 * Database lookup. Abstracts out JDBC operations.
 */
public class DBUtil {

    private String jdbcUrl;
    private String databaseUserName;
    private String databasePassword;
    private String driverName;

    protected static Logger log = LoggerFactory.getLogger(DBUtil.class);

    private Properties properties;

    public DBUtil(String jdbcUrl, String userName, String password, String driver) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException {

        this.jdbcUrl = jdbcUrl;
        this.databaseUserName = userName;
        this.databasePassword = password;
        this.driverName = driver;

        init();
    }

    public DBUtil(JDBCConfig jdbcConfig) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        this(jdbcConfig.getURL(), jdbcConfig.getUser(), jdbcConfig.getPassword(), jdbcConfig.getDriver());
    }

    /**
     * Initializes and load driver. Must be called this before calling anyother method.
     * 
     * @throws ClassNotFoundException
     *             If DB driver is not found.
     * @throws InstantiationException
     *             If unable to create driver class.
     * @throws IllegalAccessException
     *             If security does not allow users to instantiate driver object.
     */
    private void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        properties = new Properties();

        properties.put("user", databaseUserName);
        properties.put("password", databasePassword);
        properties.put("characterEncoding", "ISO-8859-1");
        properties.put("useUnicode", "true");

        loadDriver();
    }

    /**
     * Generic method to query values in the database.
     * 
     * @param tableName
     *            Table name to query
     * @param selectColumn
     *            The column selecting
     * @param whereValue
     *            The condition query
     * @return The value appropriate to the query.
     * @throws SQLException
     *             If an error occurred while querying
     */
    public String getMatchingColumnValue(String tableName, String selectColumn, String whereValue) throws SQLException {
        return getMatchingColumnValue(tableName, selectColumn, selectColumn, whereValue);
    }

    /**
     * Generic method to query values in the database.
     * 
     * @param tableName
     *            Table name to query
     * @param selectColumn
     *            The column selecting
     * @param whereColumn
     *            The column which condition should apply
     * @param whereValue
     *            The condition query
     * @return The value appropriate to the query.
     * @throws SQLException
     *             If an error occurred while querying
     */
    public String getMatchingColumnValue(String tableName, String selectColumn, String whereColumn, String whereValue)
            throws SQLException {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ").append(selectColumn).append(" FROM ").append(tableName).append(" WHERE ")
                .append(whereColumn).append(" = ?");

        String sql = stringBuilder.toString();

        Connection connection = getConnection();

        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = null;

        try {
            ps.setString(1, whereValue);
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            }

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }

                ps.close();
                connection.close();

            } catch (Exception ignore) {
                log.error("An error occurred while closing database connections ", ignore);
            }
        }

        return null;
    }

    /**
     * Create table utility method.
     * 
     * @param sql
     *            SQL to be executed.
     * @throws SQLException
     *             If an error occurred while creating the table.
     */
    public void executeSQL(String sql) throws SQLException {

        Connection connection = getConnection();

        PreparedStatement ps = connection.prepareStatement(sql);

        try {
            ps.executeUpdate();
            connection.commit();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                connection.close();

            } catch (Exception ignore) {
                log.error("An error occurred while closing database connections ", ignore);
            }
        }

    }

    private void loadDriver() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class.forName(driverName).newInstance();
    }

    /**
     * Gets a new DBCP data source.
     * 
     * @return A new data source.
     */
    public DataSource getDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(this.driverName);
        ds.setUsername(this.databaseUserName);
        ds.setPassword(this.databasePassword);
        ds.setUrl(this.jdbcUrl);

        return ds;
    }

    /**
     * Creates a new JDBC connections based on provided DBCP properties.
     * 
     * @return A new DB connection.
     * @throws SQLException
     *             If an error occurred while creating the connection.
     */
    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl, properties);
        connection.setAutoCommit(false);
        return connection;
    }

    /**
     * Utility method to close statements and connections.
     * 
     * @param preparedStatement
     *            The prepared statement to close.
     * @param connection
     *            The connection to close.
     */
    public static void cleanup(PreparedStatement preparedStatement, Connection connection) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Error closing prepared statement.", e);
            }
        }
        cleanup(connection);
    }

    /**
     * Utility method to close statements and connections.
     *
     * @param preparedStatement
     *            The prepared statement to close.
     */
    public static void cleanup(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Error closing prepared statement.", e);
            }
        }
    }

    /**
     * Utility method to close statements and connections.
     *
     * @param preparedStatement
     *            The prepared statement to close.
     */
    public static void cleanup(PreparedStatement preparedStatement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("Error closing prepared statement.", e);
            }
        }

        cleanup(preparedStatement);
    }

    /**
     * Cleanup the connection.
     * @param connection The connection to close.
     */
    public static void cleanup(Connection connection) {
        if (connection != null) {
            try {
                if (connection.isClosed()) {
                    return;
                }
                if (!connection.getAutoCommit()) {
                    connection.rollback();
                }
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Error closing connection", e);
            }
        }
    }

    /**
     * Mainly useful for tests.
     * 
     * @param tableName
     *            The table name.
     * @param connection
     *            The connection to be used.
     */
    public static void truncate(String tableName, Connection connection) throws SQLException {

        String sql = "delete from " + tableName;

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.executeUpdate();

        connection.commit();

    }

    /**
     * Creates a DBUtil object based on servlet context configurations.
     *
     * @return DBUtil object.
     * @throws Exception
     * If an error occurred while reading configurations or while creating database object.
     */
    public static DBUtil getCredentialStoreDBUtil() throws ApplicationSettingsException, IllegalAccessException,
            ClassNotFoundException, InstantiationException {
        String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
        String userName = ServerSettings.getCredentialStoreDBUser();
        String password = ServerSettings.getCredentialStoreDBPassword();
        String driverName = ServerSettings.getCredentialStoreDBDriver();

        StringBuilder stringBuilder = new StringBuilder("Starting credential store, connecting to database - ");
        stringBuilder.append(jdbcUrl).append(" DB user - ").append(userName).append(" driver name - ")
                .append(driverName);

        log.debug(stringBuilder.toString());

        DBUtil dbUtil = new DBUtil(jdbcUrl, userName, password, driverName);
        dbUtil.init();

        return dbUtil;
    }

}
