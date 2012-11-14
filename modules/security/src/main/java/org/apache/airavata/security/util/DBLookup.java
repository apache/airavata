package org.apache.airavata.security.util;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Properties;

/**
 * Database lookup.
 */
public class DBLookup {

    private String jdbcUrl;
    private String databaseUserName;
    private String databasePassword;
    private String driverName;

    protected static Logger log = LoggerFactory.getLogger(DBLookup.class);

    private Properties properties;


    public DBLookup(String jdbcUrl, String userName, String password, String driver) {

        this.jdbcUrl = jdbcUrl;
        this.databaseUserName = userName;
        this.databasePassword = password;
        this.driverName = driver;
    }

    public void init() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        properties = new Properties();

        properties.put("user", databaseUserName);
        properties.put("password", databasePassword);
        properties.put("characterEncoding", "ISO-8859-1");
        properties.put("useUnicode", "true");

        loadDriver();
    }

    public String getMatchingColumnValue(String tableName, String selectColumn, String whereValue)
            throws SQLException {
        return getMatchingColumnValue(tableName, selectColumn, selectColumn, whereValue);
    }

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

    public DataSource getDataSource() {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(this.driverName);
        ds.setUsername(this.databaseUserName);
        ds.setPassword(this.databasePassword);
        ds.setUrl(this.jdbcUrl);

        return ds;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, properties);
    }

}
