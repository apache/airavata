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
package org.apache.airavata.security;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An abstract authenticator class which reads database configurations.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AbstractDatabaseAuthenticator extends AbstractAuthenticator {

    private String databaseURL;

    private String databaseDriver;

    private String databaseUserName;

    private String databasePassword;

    public AbstractDatabaseAuthenticator() {
        super();
    }

    public AbstractDatabaseAuthenticator(String name) {
        super(name);
    }

    /**
     * We are reading database parameters in this case.
     * 
     * @param node
     *            An XML configuration node.
     */
    public void configure(Node node) {

        /**
         * <specificConfigurations> <database> <jdbcUrl></jdbcUrl> <databaseDriver></databaseDriver>
         * <userName></userName> <password></password> </database> </specificConfigurations>
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

                    if (element.getNodeName().equals("jdbcUrl")) {
                        databaseURL = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("databaseDriver")) {
                        databaseDriver = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("userName")) {
                        databaseUserName = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("password")) {
                        databasePassword = element.getFirstChild().getNodeValue();
                    }
                }
            }
        }

        StringBuilder stringBuilder = new StringBuilder("Configuring DB parameters for authenticator with JDBC URL - ");
        stringBuilder.append(databaseURL).append(" DB driver - ").append(" DB user - ").append(databaseUserName)
                .append(" DB password - xxxxxx");

        log.debug(stringBuilder.toString());

        try {
            getUserStore().configure(node);
        } catch (UserStoreException e) {
            String msg = "Error configuring user store associated with authenticator.";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    public String getDatabaseURL() {
        return databaseURL;
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public String getDatabaseUserName() {
        return databaseUserName;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }
}
