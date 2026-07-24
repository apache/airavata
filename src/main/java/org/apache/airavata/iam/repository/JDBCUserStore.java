/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.iam.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.airavata.db.DBUtil;
import org.apache.airavata.iam.util.PasswordDigester;
import org.apache.airavata.iam.util.UserStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The JDBC user store implementation. Uses direct JDBC queries for authentication
 * instead of Apache Shiro.
 */
public class JDBCUserStore extends AbstractJDBCUserStore {

    protected static Logger log = LoggerFactory.getLogger(JDBCUserStore.class);

    private DataSource dataSource;
    private String authenticationQuery;
    private PasswordDigester passwordDigester;

    @Override
    public boolean authenticate(String userName, Object credentials) throws UserStoreException {
        String hashedPassword = passwordDigester.getPasswordHashValue((String) credentials);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(authenticationQuery)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString(1);
                    return hashedPassword.equals(storedPassword);
                }
                return false;
            }
        } catch (SQLException e) {
            log.debug("JDBC authentication failed for user {}: {}", userName, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void configure(Node node) throws UserStoreException {

        super.configure(node);

        /**
         * <specificConfigurations> <database> <jdbcUrl></jdbcUrl> <databaseDriver></databaseDriver>
         * <userName></userName> <password></password> <passwordHashMethod>MD5</passwordHashMethod>
         * <userTableName></userTableName> <userNameColumnName></userNameColumnName>
         * <passwordColumnName></passwordColumnName> </database> </specificConfigurations>
         */
        NodeList databaseNodeList = node.getChildNodes();

        Node databaseNode = null;

        for (int k = 0; k < databaseNodeList.getLength(); ++k) {

            Node n = databaseNodeList.item(k);

            if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
                databaseNode = n;
            }
        }

        String userTable = null;
        String userNameColumn = null;
        String passwordColumn = null;
        String passwordHashMethod = null;

        if (databaseNode != null) {
            NodeList nodeList = databaseNode.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node n = nodeList.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) n;

                    if (element.getNodeName().equals("userTableName")) {
                        userTable = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("userNameColumnName")) {
                        userNameColumn = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("passwordColumnName")) {
                        passwordColumn = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("passwordHashMethod")) {
                        passwordHashMethod = element.getFirstChild().getNodeValue();
                    }
                }
            }
        }

        passwordDigester = new PasswordDigester(passwordHashMethod);

        try {
            initializeDatabaseLookup(passwordColumn, userTable, userNameColumn);
        } catch (Exception e) {
            log.error("Error while initializing database configurations.", e);
            throw new UserStoreException("Error while initializing database configurations.", e);
        }

        StringBuilder stringBuilder =
                new StringBuilder("Configuring DB parameters for authenticator with User name Table - ");
        stringBuilder
                .append(userTable)
                .append(" User name column - ")
                .append(userNameColumn)
                .append(" Password column - ")
                .append(passwordColumn);

        log.debug(stringBuilder.toString());
    }

    protected void initializeDatabaseLookup(String passwordColumn, String userTable, String userNameColumn)
            throws IllegalAccessException, ClassNotFoundException, InstantiationException {

        DBUtil dbUtil = new DBUtil(getDatabaseURL(), getDatabaseUserName(), getDatabasePassword(), getDatabaseDriver());
        dataSource = dbUtil.getDataSource();

        authenticationQuery = "SELECT " + passwordColumn + " FROM " + userTable + " WHERE " + userNameColumn + " = ?";
    }

    public PasswordDigester getPasswordDigester() {
        return passwordDigester;
    }
}
