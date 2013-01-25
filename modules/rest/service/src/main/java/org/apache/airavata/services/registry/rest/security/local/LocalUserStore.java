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

package org.apache.airavata.services.registry.rest.security.local;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.SecurityUtil;
import org.apache.airavata.registry.api.util.RegistrySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User store to maintain internal DB database.
 */
public class LocalUserStore {

    protected static Logger log = LoggerFactory.getLogger(LocalUserStore.class);

    private DBUtil dbUtil;

    private String hashMethod;

    public LocalUserStore(ServletContext servletContext) throws Exception {
        // Properties properties = WebAppUtil.getAiravataProperties(servletContext);

        hashMethod = RegistrySettings.getSetting("default.registry.password.hash.method");

        dbUtil = new DBUtil(RegistrySettings.getSetting("registry.jdbc.url"),
                RegistrySettings.getSetting("registry.jdbc.user"),
                RegistrySettings.getSetting("registry.jdbc.password"),
                RegistrySettings.getSetting("registry.jdbc.driver"));

        dbUtil.init();
    }

    public LocalUserStore(DBUtil db) {
        dbUtil = db;
    }

    public void addUser(String userName, String password) {

        String sql = "insert into users values (?, ?)";

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, userName);
            preparedStatement.setString(2, SecurityUtil.digestString(password, hashMethod));

            preparedStatement.executeUpdate();

            connection.commit();

            log.debug("User " + userName + " successfully added.");

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error persisting user information.");
            stringBuilder.append(" user - ").append(userName);

            log.error(stringBuilder.toString(), e);

            throw new RuntimeException(stringBuilder.toString(), e);
        } catch (NoSuchAlgorithmException e) {
            String stringBuilder = "Error creating hash value for password.";
            log.error(stringBuilder, e);

            throw new RuntimeException(stringBuilder, e);
        } finally {

            dbUtil.cleanup(preparedStatement, connection);
        }

    }

    protected String getPassword(String userName, Connection connection) {

        String sql = "select password from users where user_name = ?";

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

            String oldDigestedPassword = SecurityUtil.digestString(oldPassword, hashMethod);

            if (storedPassword != null) {
                if (!storedPassword.equals(oldDigestedPassword)) {
                    throw new RuntimeException("Previous password did not match correctly. Please specify old password"
                            + " correctly.");
                }
            }

            String sql = "update users set password = ? where user_name = ?";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, SecurityUtil.digestString(newPassword, hashMethod));
            preparedStatement.setString(2, userName);

            preparedStatement.executeUpdate();

            connection.commit();

            log.debug("Password changed for user " + userName);

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials.");
            stringBuilder.append(" user - ").append(userName);

            log.error(stringBuilder.toString(), e);

            throw new RuntimeException(stringBuilder.toString(), e);
        } catch (NoSuchAlgorithmException e) {
            String stringBuilder = "Error creating hash value for password.";
            log.error(stringBuilder, e);

            throw new RuntimeException(stringBuilder, e);
        } finally {

            dbUtil.cleanup(preparedStatement, connection);
        }

    }

    public void changePasswordByAdmin(String userName, String newPassword) {

        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = dbUtil.getConnection();

            String sql = "update users set password = ? where user_name = ?";

            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, SecurityUtil.digestString(newPassword, hashMethod));
            preparedStatement.setString(2, userName);

            preparedStatement.executeUpdate();

            connection.commit();

            log.debug("Admin changed password of user " + userName);

        } catch (SQLException e) {
            StringBuilder stringBuilder = new StringBuilder("Error updating credentials.");
            stringBuilder.append(" user - ").append(userName);

            log.error(stringBuilder.toString(), e);

            throw new RuntimeException(stringBuilder.toString(), e);
        } catch (NoSuchAlgorithmException e) {
            String stringBuilder = "Error creating hash value for password.";
            log.error(stringBuilder, e);

            throw new RuntimeException(stringBuilder, e);
        } finally {

            dbUtil.cleanup(preparedStatement, connection);
        }

    }

    public void deleteUser(String userName) {

        String sql = "delete from users where user_name=?";

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

    public List<String> getUsers() {

        List<String> userList = new ArrayList<String>();

        String sql = "select user_name from users";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Connection connection = null;

        try {

            connection = dbUtil.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                userList.add(resultSet.getString("user_name"));
            }

        } catch (SQLException e) {
            String errorString = "Error retrieving users.";
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

        Collections.sort(userList);

        return userList;

    }

    public static String getPasswordRegularExpression() {
        return "'^[a-zA-Z0-9_-]{6,15}$'";
    }
}
