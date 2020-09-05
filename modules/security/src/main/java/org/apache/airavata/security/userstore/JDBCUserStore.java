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
package org.apache.airavata.security.userstore;

import org.apache.airavata.security.UserStoreException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.security.util.PasswordDigester;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.jdbc.JdbcRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.sql.DataSource;

/**
 * The JDBC user store implementation.
 */
public class JDBCUserStore extends AbstractJDBCUserStore {

    protected static Logger log = LoggerFactory.getLogger(JDBCUserStore.class);

    private JdbcRealm jdbcRealm;

    private PasswordDigester passwordDigester;

    public JDBCUserStore() {
        jdbcRealm = new JdbcRealm();
    }

    @Override
    public boolean authenticate(String userName, Object credentials) throws UserStoreException {
        AuthenticationToken authenticationToken = new UsernamePasswordToken(userName,
                passwordDigester.getPasswordHashValue((String) credentials));

        AuthenticationInfo authenticationInfo;
        try {

            authenticationInfo = jdbcRealm.getAuthenticationInfo(authenticationToken);
            return authenticationInfo != null;

        } catch (AuthenticationException e) {
            log.debug(e.getLocalizedMessage(), e);
            return false;
        }
    }

    @Override
    public boolean authenticate(Object credentials) throws UserStoreException {
        log.error("JDBC user store only supports user name, password based authentication.");
        throw new UnsupportedOperationException();
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

        StringBuilder stringBuilder = new StringBuilder(
                "Configuring DB parameters for authenticator with User name Table - ");
        stringBuilder.append(userTable).append(" User name column - ").append(userNameColumn)
                .append(" Password column - ").append(passwordColumn);

        log.debug(stringBuilder.toString());
    }

    protected void initializeDatabaseLookup(String passwordColumn, String userTable, String userNameColumn) throws IllegalAccessException, ClassNotFoundException, InstantiationException {

        DBUtil dbUtil = new DBUtil(getDatabaseURL(), getDatabaseUserName(), getDatabasePassword(), getDatabaseDriver());
        DataSource dataSource = dbUtil.getDataSource();
        jdbcRealm.setDataSource(dataSource);

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ").append(passwordColumn).append(" FROM ").append(userTable).append(" WHERE ")
                .append(userNameColumn).append(" = ?");

        jdbcRealm.setAuthenticationQuery(stringBuilder.toString());
    }

    public PasswordDigester getPasswordDigester() {
        return passwordDigester;
    }
}
