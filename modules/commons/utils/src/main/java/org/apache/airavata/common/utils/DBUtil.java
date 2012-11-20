package org.apache.airavata.common.utils;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
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

    public DBUtil(String jdbcUrl, String userName, String password, String driver) {

        this.jdbcUrl = jdbcUrl;
        this.databaseUserName = userName;
        this.databasePassword = password;
        this.driverName = driver;
    }

    /**
     * Initializes and load driver. Must be called this before calling anyother method.
     * @throws ClassNotFoundException If DB driver is not found.
     * @throws InstantiationException If unable to create driver class.
     * @throws IllegalAccessException If security does not allow users to instantiate driver object.
     */
    public void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        properties = new Properties();

        properties.put("user", databaseUserName);
        properties.put("password", databasePassword);
        properties.put("characterEncoding", "ISO-8859-1");
        properties.put("useUnicode", "true");

        loadDriver();
    }

    /**
     * Generic method to query values in the database.
     * @param tableName Table name to query
     * @param selectColumn The column selecting
     * @param whereValue The condition query
     * @return The value appropriate to the query.
     * @throws SQLException If an error occurred while querying
     */
    public String getMatchingColumnValue(String tableName, String selectColumn, String whereValue)
            throws SQLException {
        return getMatchingColumnValue(tableName, selectColumn, selectColumn, whereValue);
    }

    /**
     * Generic method to query values in the database.
     * @param tableName Table name to query
     * @param selectColumn The column selecting
     * @param whereColumn The column which condition should apply
     * @param whereValue The condition query
     * @return The value appropriate to the query.
     * @throws SQLException If an error occurred while querying
     */
    public String getMatchingColumnValue(String tableName, String selectColumn, String whereColumn, String whereValue)
            throws SQLException {

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ").append(selectColumn).append(" FROM ").append(tableName)
                .append(" WHERE ").append(whereColumn).append(" = ?");

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

    private void loadDriver() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class.forName(driverName).newInstance();
    }

    /**
     * Gets a new DBCP data source.
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
     * @return A new DB connection.
     * @throws SQLException If an error occurred while creating the connection.
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, properties);
    }

    /**
     * Utility method to close statements and connections.
     * @param preparedStatement The prepared statement to close.
     * @param connection The connection to close.
     */
    public void cleanup(PreparedStatement preparedStatement, Connection connection) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Error closing prepared statement.", e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Error closing database connection.", e);
            }
        }
    }

    /**
     * Mainly useful for tests.
     * @param tableName The table name.
     * @param connection The connection to be used.
     */
    public static void truncate(String tableName, Connection connection) throws SQLException {

        String sql = "delete from " + tableName;

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.executeUpdate();

        connection.commit();

    }

    /**
     * Creates a DBUtil object based on servlet context configurations.
     * @param servletContext The servlet context.
     * @return DBUtil object.
     * @throws Exception If an error occurred while reading configurations or while creating
     * database object.
     */
    public static DBUtil getDBUtil(ServletContext servletContext) throws Exception{

        String jdbcUrl = servletContext.getInitParameter("credential-store-jdbc-url");
        String userName = servletContext.getInitParameter("credential-store-db-user");
        String password = servletContext.getInitParameter("credential-store-db-password");
        String driverName = servletContext.getInitParameter("credential-store-db-driver");

        StringBuilder stringBuilder = new StringBuilder("Starting credential store, connecting to database - ");
        stringBuilder.append(jdbcUrl).append(" DB user - ").append(userName).
                append(" driver name - ").append(driverName);

        log.info(stringBuilder.toString());

        DBUtil dbUtil = new DBUtil(jdbcUrl, userName, password, driverName);
        try {
            dbUtil.init();
        } catch (Exception e) {
            log.error("Error initializing database operations.", e);
            throw e;
        }

        return dbUtil;
    }



}
