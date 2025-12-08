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

import org.apache.airavata.config.AiravataServerProperties;
import org.apache.airavata.security.UserStore;
import org.apache.airavata.security.UserStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An abstract implementation of the UserStore. This will encapsulate JDBC configurations reading code.
 */
public abstract class AbstractJDBCUserStore implements UserStore {

    protected static Logger log = LoggerFactory.getLogger(JDBCUserStore.class);
    protected AiravataServerProperties properties;

    private String databaseURL = null;
    private String databaseDriver = null;
    private String databaseUserName = null;
    private String databasePassword = null;

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

    /**
     * Configures primary JDBC parameters. i.e
     *
     * @param node An XML configuration node.
     * @throws UserStoreException
     */
    public void configure(Node node) throws UserStoreException {

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

        if (databaseURL == null || databaseUserName == null || databasePassword == null) {
            // If database configurations are not specified in authenticators.xml we will read them from
            // server.properties file.
            if (properties != null) {
                var db = properties.database.vault;
                databaseDriver = db.driver;
                if (databaseDriver == null || databaseDriver.isEmpty()) {
                    databaseDriver = properties.database.registry.driver;
                }
                databaseURL = db.url;
                if (databaseURL == null || databaseURL.isEmpty()) {
                    databaseURL = properties.database.registry.url;
                }
                databaseUserName = db.user;
                if (databaseUserName == null || databaseUserName.isEmpty()) {
                    databaseUserName = properties.database.registry.user;
                }
                databasePassword = db.password;
                if (databasePassword == null || databasePassword.isEmpty()) {
                    databasePassword = properties.database.registry.password;
                }
            } else {
                log.warn("Database configurations not specified in XML and properties not available.");
            }

            StringBuilder stringBuilder = new StringBuilder("User store configurations - dbDriver - ");
            stringBuilder.append(databaseDriver);
            stringBuilder
                    .append(" URL - ")
                    .append(databaseURL)
                    .append(" DB user - ")
                    .append(databaseUserName);
            log.info(stringBuilder.toString());
        }
    }
}
