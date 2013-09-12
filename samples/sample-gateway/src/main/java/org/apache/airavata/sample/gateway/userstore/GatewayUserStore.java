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

package org.apache.airavata.sample.gateway.userstore;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.DerbyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User store to maintain internal DB database.
 */
public class GatewayUserStore {

    protected static Logger log = LoggerFactory.getLogger(GatewayUserStore.class);

    private DBUtil dbUtil;

    private static boolean dbInitialized = false;


    public GatewayUserStore(DBUtil dbUtil) throws Exception {

        this.dbUtil = dbUtil;

    }

    public static void initializeData(DBUtil dbUtil) throws Exception {

        if (dbInitialized) {
            return;
        }

        String dropTableSQL = "drop table Users";

        try {
            dbUtil.executeSQL(dropTableSQL);
        } catch (Exception e) {}

        String createTableSQL = "CREATE TABLE Users\n" +
                "(\n" +
                "        user_name VARCHAR(256) NOT NULL,\n" +
                "        password VARCHAR(256) NOT NULL,\n" +
                "        email VARCHAR(256) NOT NULL,\n" +
                "        token_id VARCHAR(256) DEFAULT NULL,\n" +
                "        PRIMARY KEY (USER_NAME)\n" +
                ")";

        dbUtil.executeSQL(createTableSQL);

        System.out.println("Table created ....");

        GatewayUserStore gatewayUserStore = new GatewayUserStore(dbUtil);
        gatewayUserStore.addUser("admin", "admin", "admin@samplegateway.org");
        gatewayUserStore.addUser("kermit", "kermit", "kermit@samplegateway.org");
        gatewayUserStore.addUser("taz", "taz", "taz@samplegateway.org");
        gatewayUserStore.addUser("coyote", "coyote", "coyote@samplegateway.org");

        System.out.println("Users added ...");


    }


    public void addUser(String userName, String password, String email) {

        String sql = "insert into Users values (?, ?, ?, ?)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, email);
            preparedStatement.setString(4, null);

            preparedStatement.executeUpdate();

            connection.commit();

            log.debug("User " + userName + " successfully added.");

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error persisting user information.");
            stringBuilder.append(" user - ").append(userName);

            log.error(stringBuilder.toString(), e);

            throw new RuntimeException(stringBuilder.toString(), e);
        } finally {

            dbUtil.cleanup(preparedStatement, connection);
        }

    }

    public void updateTokens (String token) {

        String sql = "update Users set token_id = ?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, token);

            preparedStatement.executeUpdate();

            connection.commit();

            log.debug("Users successfully updated to use token - " + token);

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating token data.");
            stringBuilder.append(" token - ").append(token);

            log.error(stringBuilder.toString(), e);

            throw new RuntimeException(stringBuilder.toString(), e);
        } finally {

            dbUtil.cleanup(preparedStatement, connection);
        }

    }

    public String getPassword(String userName, Connection connection) {

        String sql = "select password from Users where user_name = ?";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, userName);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("password");
            }

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error retrieving credentials for user.");
            stringBuilder.append("name - ").append(userName);

            log.error(stringBuilder.toString(), e);

            throw new RuntimeException(stringBuilder.toString(), e);
        } finally {

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("Error closing result set", e);
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    log.error("Error closing prepared statement", e);
                }
            }
        }

        return null;
    }

    public void changePassword(String userName, String oldPassword, String newPassword) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();

            String storedPassword = getPassword(userName, connection);

            String oldDigestedPassword = oldPassword;

            if (storedPassword != null) {
                if (!storedPassword.equals(oldDigestedPassword)) {
                    throw new RuntimeException("Previous password did not match correctly. Please specify old password"
                            + " correctly.");
                }
            }

            String sql = "update Users set password = ? where user_name = ?";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, userName);

            preparedStatement.executeUpdate();

            connection.commit();

            log.debug("Password changed for user " + userName);

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials.");
            stringBuilder.append(" user - ").append(userName);

            log.error(stringBuilder.toString(), e);

            throw new RuntimeException(stringBuilder.toString(), e);
        } finally {

            dbUtil.cleanup(preparedStatement, connection);
        }

    }

    public void changePasswordByAdmin(String userName, String newPassword) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();

            String sql = "update Users set password = ? where user_name = ?";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, newPassword);
            preparedStatement.setString(2, userName);

            preparedStatement.executeUpdate();

            connection.commit();

            log.debug("Admin changed password of user " + userName);

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials.");
            stringBuilder.append(" user - ").append(userName);

            log.error(stringBuilder.toString(), e);

            throw new RuntimeException(stringBuilder.toString(), e);
        } finally {

            dbUtil.cleanup(preparedStatement, connection);
        }

    }

    public void deleteUser(String userName) {

        String sql = "delete from Users where user_name=?";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, userName);

            preparedStatement.executeUpdate();

            connection.commit();

            log.debug("User " + userName + " deleted.");

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error deleting user.");
            stringBuilder.append("user - ").append(userName);

            log.error(stringBuilder.toString(), e);

            throw new RuntimeException(stringBuilder.toString(), e);
        } finally {
            dbUtil.cleanup(preparedStatement, connection);
        }

    }

    public List<User> getUsers() {

        List<User> userList = new ArrayList<User>();

        String sql = "select * from Users";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;

        try {

            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                User user = new User();
                user.setUserName(resultSet.getString("user_name"));
                user.setPassword(resultSet.getString("password"));
                user.setEmail(resultSet.getString("email"));
                user.setToken(resultSet.getString("token_id"));

                userList.add(user);
            }

        } catch (SQLException e) {
            String errorString = "Error retrieving Users.";
            log.error(errorString, e);

            throw new RuntimeException(errorString, e);
        } finally {

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("Error closing result set", e);
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    log.error("Error closing prepared statement", e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error closing connection", e);
                }
            }
        }

        return userList;

    }



    public String getUserToken(String userName) {

        String sql = "select token_id from Users where user_name=?";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;

        try {

            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, userName);

            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("token_id");
            }

        } catch (SQLException e) {
            String errorString = "Error retrieving token for user " + userName;
            log.error(errorString, e);

            throw new RuntimeException(errorString, e);
        } finally {

            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    log.error("Error closing result set", e);
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    log.error("Error closing prepared statement", e);
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    log.error("Error closing connection", e);
                }
            }
        }

        return null;

    }

    public static String getPasswordRegularExpression() {
        return "'^[a-zA-Z0-9_-]{6,15}$'";
    }
}

