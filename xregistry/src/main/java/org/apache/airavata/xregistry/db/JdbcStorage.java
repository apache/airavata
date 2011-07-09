/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.airavata.xregistry.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.airavata.xregistry.XregistryException;


public class JdbcStorage {
   // private ConfigurationParamsReader config;

    // private String jdbcUrl=
    // "jdbc:mysql://156.56.104.175:3306/wsnt?user=root";
    private String jdbcUrl = null;;
    

    private PreparedStatement stmt = null;

    private ResultSet rs = null;

    private ResultSetMetaData rsmd = null;

    // private Connection conn = null;
   // private ConnectionPool connectionPool;

    private String jdbcDriver;
    
    

        
        public JdbcStorage(String fileName, boolean enableTransactions) {
            ConfigurationParamsReader config = new ConfigurationParamsReader(fileName);
            jdbcUrl = config.getProperty("jdbcUrl");
            jdbcDriver = config.getProperty("jdbcDriver");
            
        }

    
        
        public JdbcStorage(String jdbcUrl,String jdbcDriver) throws XregistryException {
//            try {
                this.jdbcDriver = jdbcDriver;
                this.jdbcUrl = jdbcUrl;
//                connectionPool = new ConnectionPool(jdbcDriver, jdbcUrl, 10,
//                            50, true);
//            } catch (SQLException e) {
//                throw new XregistryException(e);
//            }
        }

    public JdbcStorage(String fileName) {
        this(fileName, false);

    }

    public Connection connect() throws XregistryException {

        //Connection conn = connectionPool.getConnection();
        return makeNewConnection();
    }

    public void closeConnection(Connection conn) throws java.sql.SQLException {
        conn.setAutoCommit(true);
        //connectionPool.free(conn);
        conn.close();
    }

//    public int update(String query) throws java.sql.SQLException {
//        int result = 0;
//        // connect();
//        Connection conn = connectionPool.getConnection();
//        stmt = conn.prepareStatement(query);
//        result = stmt.executeUpdate();
//        stmt.close();
//        connectionPool.free(conn);
//
//        return result;
//    }
//
//    /**
//     * This method is provided so that yo can have better control over the
//     * statement. For example: You can use stmt.setString to convert quotation
//     * mark automatically in an INSERT statement
//     */
//    public int insert(PreparedStatement stmt) throws java.sql.SQLException {
//        int rows = 0;
//
//        rows = stmt.executeUpdate();
//        stmt.close();
//        return rows;
//    }
//
//    public int insert(String query) throws java.sql.SQLException {
//        int rows = 0;
//
//        Connection conn = connectionPool.getConnection();
//        stmt = conn.prepareStatement(query);
//        rows = stmt.executeUpdate();
//        stmt.close();
//        connectionPool.free(conn);
//
//        return rows;
//    }
//
//    public ResultSet query(String query) throws SQLException {
//        Connection conn = connectionPool.getConnection();
//        // Create a scrollable ResultSet so that I can use rs.last() to get
//        // total number of rows without using another COUNT in SQL query
//        Statement lstmt = conn.createStatement(
//                ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
//        ResultSet rs = lstmt.executeQuery(query);
//        connectionPool.free(conn);
//        return rs;
//    }
//
//    public int countRow(String tableName, String columnName)
//            throws java.sql.SQLException {
//        String query = new String("SELECT COUNT(" + columnName + ") FROM "
//                + tableName);
//        Connection conn = connectionPool.getConnection();
//        stmt = conn.prepareStatement(query);
//        rs = stmt.executeQuery();
//        rs.next();
//        int count = rs.getInt(1);
//        stmt.close();
//        connectionPool.free(conn);
//        return count;
//    }
    
    private Connection makeNewConnection() throws XregistryException  {
                try {
                    // Load database driver if not already loaded
                    Class.forName(jdbcDriver);
                    Connection connection;
                            connection = DriverManager.getConnection(jdbcUrl);
                    return connection;
                } catch (ClassNotFoundException e) {
                    throw new XregistryException(e);                    
                } catch (SQLException e) {
                    throw new XregistryException(e);
                }
}

}
