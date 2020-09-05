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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.sql.SQLException;

/**
 * User store which works on sessions. Will talk to database to check whether session ids are stored in the database.
 */
public class SessionDBUserStore extends AbstractJDBCUserStore {

    private String sessionTable;
    private String sessionColumn;
    private String comparingColumn;

    protected DBUtil dbUtil;

    protected static Logger log = LoggerFactory.getLogger(SessionDBUserStore.class);

    @Override
    public boolean authenticate(String userName, Object credentials) throws UserStoreException {
        // This user store only supports session tokens.
        throw new UnsupportedOperationException();
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

        StringBuilder stringBuilder = new StringBuilder(
                "Configuring DB parameters for authenticator with Session Table - ");
        stringBuilder.append(sessionTable).append(" Session column - ").append(sessionColumn)
                .append(" Comparing column - ").append(comparingColumn);

        log.debug(stringBuilder.toString());
    }

    private void initializeDatabaseLookup() throws RuntimeException {

        try {
            this.dbUtil = new DBUtil(getDatabaseURL(), getDatabaseUserName(), getDatabasePassword(), getDatabaseDriver());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading database driver. Driver class not found.", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Error loading database driver. Error instantiating driver object.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error loading database driver. Illegal access to driver object.", e);
        }
    }
}
