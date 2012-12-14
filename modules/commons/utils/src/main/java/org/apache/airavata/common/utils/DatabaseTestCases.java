package org.apache.airavata.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * An abstraction for database specific test classes. This will create a database and provides
 * methods to execute SQLs.
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
        return new StringBuilder().append("jdbc:derby://").append(getHostAddress()).append(":").append(getPort()).
                append("/persistent_data;create=true;user=").append(getUserName()).
                append(";password=").append(getPassword()).toString();
    }

    public static void waitTillServerStarts() {
        DBUtil dbUtil = new DBUtil(getJDBCUrl(), getUserName(), getPassword(), getDriver());

        Connection connection = null;
        try {
           connection = dbUtil.getConnection();
        } catch (SQLException e) {
            // ignore
        }

        while (connection == null) {
            try {
                Thread.sleep(1000);
                try {
                    connection = dbUtil.getConnection();
                } catch (SQLException e) {
                    // ignore
                }
            } catch (InterruptedException e) {
                //ignore
            }
        }


    }



    public static void executeSQL(String sql) throws Exception {
        DBUtil dbUtil = new DBUtil(getJDBCUrl(), getUserName(), getPassword(), getDriver());
        dbUtil.executeSQL(sql);
    }

}
