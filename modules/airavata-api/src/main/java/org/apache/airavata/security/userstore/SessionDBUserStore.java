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
package org.apache.airavata.security.userstore;

import java.sql.SQLException;
import org.apache.airavata.common.exception.CoreExceptions.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.security.UserStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * User store which works on sessions. Will talk to database to check whether session ids are stored in the database.
 */
@Component
@Profile("!test")
public class SessionDBUserStore extends AbstractJDBCUserStore {

    private String sessionTable;
    private String sessionColumn;
    private String comparingColumn;

    protected DBUtil dbUtil;

    protected static Logger log = LoggerFactory.getLogger(SessionDBUserStore.class);

    /**
     * Authenticate a user with username and credentials.
     *
     * <p>SessionDBUserStore only supports session token authentication, not username/password
     * authentication. Use {@link #authenticate(Object)} with a session token instead.
     *
     * @param userName the username (not used)
     * @param credentials the credentials (not used)
     * @return authentication result
     * @throws UserStoreException if authentication fails
     * @throws UnsupportedOperationException always - this user store only supports session tokens
     */
    @Override
    public boolean authenticate(String userName, Object credentials) throws UserStoreException {
        throw new UnsupportedOperationException(
                "SessionDBUserStore only supports session token authentication - use authenticate(Object token) instead");
    }

    @Override
    public boolean authenticate(Object credentials) throws UserStoreException {

        String sessionTicket = (String) credentials;

        try {
            String sessionString = dbUtil.getMatchingColumnValue(sessionTable, sessionColumn, sessionTicket);
            return (sessionString != null);
        } catch (SQLException e) {
            throw new UserStoreException("Error querying database for session information.", e);
        }
    }

    @Override
    public void configure(Node node) throws UserStoreException {

        super.configure(node);
        /**
         * <specificConfigurations> <sessionTable> </sessionTable> <sessionColumn></sessionColumn>
         * <comparingColumn></comparingColumn> </specificConfigurations>
         */
        NodeList databaseNodeList = node.getChildNodes();

        Node databaseNode = null;

        for (int k = 0; k < databaseNodeList.getLength(); ++k) {

            Node n = databaseNodeList.item(k);

            if (n != null && n.getNodeType() == Node.ELEMENT_NODE) {
                databaseNode = n;
            }
        }

        if (databaseNode != null) {
            NodeList nodeList = databaseNode.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node n = nodeList.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) n;

                    if (element.getNodeName().equals("sessionTable")) {
                        sessionTable = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("sessionColumn")) {
                        sessionColumn = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("comparingColumn")) {
                        comparingColumn = element.getFirstChild().getNodeValue();
                    }
                }
            }
        }

        initializeDatabaseLookup();

        var message = String.format(
                "Configuring DB parameters for authenticator with Session Table - %s Session column - %s Comparing column - %s",
                sessionTable, sessionColumn, comparingColumn);
        log.debug(message);
    }

    private void initializeDatabaseLookup() throws UserStoreException {

        try {
            this.dbUtil =
                    new DBUtil(getDatabaseURL(), getDatabaseUserName(), getDatabasePassword(), getDatabaseDriver());
        } catch (ApplicationSettingsException e) {
            log.error("Error while initializing database lookup", e.getMessage());
            throw new UserStoreException("Error while initializing database lookup", e);
        }
    }
}
