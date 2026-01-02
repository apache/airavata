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
package org.apache.airavata.security.configurations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.airavata.security.Authenticator;
import org.apache.airavata.security.UserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class will read authenticators.xml and load all configurations related to authenticators.
 *
 * @deprecated This class uses reflection for dependency resolution. Use {@link org.apache.airavata.security.AuthenticatorRegistry}
 *             instead, which collects authenticator beans from Spring context. This class is kept for backward
 *             compatibility and test purposes only.
 */
@Deprecated
public class AuthenticatorConfigurationReader extends AbstractConfigurationReader {

    private List<Authenticator> authenticatorList = new ArrayList<Authenticator>();

    protected static Logger log = LoggerFactory.getLogger(AuthenticatorConfigurationReader.class);

    protected static boolean authenticationEnabled = true;

    public AuthenticatorConfigurationReader() {}

    public void init(InputStream inputStream) throws IOException, ParserConfigurationException, SAXException {

        authenticationEnabled = true;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();

        NodeList rootNodeList = doc.getElementsByTagName("authenticators");

        if (rootNodeList == null || rootNodeList.getLength() == 0) {
            throw new ParserConfigurationException("authenticators.xml should have authenticators root element.");
        }

        Node authenticatorsNode = rootNodeList.item(0);
        NamedNodeMap rootAttributes = authenticatorsNode.getAttributes();

        if (rootAttributes != null && rootAttributes.getNamedItem("enabled") != null) {

            String enabledAttribute = rootAttributes.getNamedItem("enabled").getNodeValue();
            if (enabledAttribute != null) {

                if (enabledAttribute.equals("false")) {
                    authenticationEnabled = false;
                }
            }
        }

        NodeList authenticators = doc.getElementsByTagName("authenticator");

        for (int i = 0; i < authenticators.getLength(); ++i) {
            Node node = authenticators.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {

                NamedNodeMap namedNodeMap = node.getAttributes();

                String name = namedNodeMap.getNamedItem("name").getNodeValue();
                String className = namedNodeMap.getNamedItem("class").getNodeValue();
                String enabled = namedNodeMap.getNamedItem("enabled").getNodeValue();
                String priority = namedNodeMap.getNamedItem("priority").getNodeValue();
                String userStoreClass = namedNodeMap.getNamedItem("userstore").getNodeValue();

                if (className == null) {
                    reportError("class");
                }

                if (userStoreClass == null) {
                    reportError("userstore");
                }

                Authenticator authenticator = createAuthenticator(name, className, enabled, priority, userStoreClass);

                NodeList configurationNodes = node.getChildNodes();

                for (int j = 0; j < configurationNodes.getLength(); ++j) {

                    Node configurationNode = configurationNodes.item(j);

                    if (configurationNode.getNodeType() == Node.ELEMENT_NODE) {

                        if (configurationNode.getNodeName().equals("specificConfigurations")) {
                            authenticator.configure(configurationNode);
                        }
                    }
                }

                if (authenticator.isEnabled()) {
                    authenticatorList.add(authenticator);
                }

                Collections.sort(authenticatorList, new AuthenticatorComparator());

                var message = String.format(
                        "Successfully initialized authenticator %s with class %s enabled? %s priority = %s",
                        name, className, enabled, priority);

                log.debug(message);
            }
        }
    }

    private void reportError(String element) throws ParserConfigurationException {
        throw new ParserConfigurationException("Error in configuration. Missing mandatory element " + element);
    }

    protected Authenticator createAuthenticator(
            String name, String className, String enabled, String priority, String userStoreClassName) {

        // Reflection removed - use AuthenticatorRegistry instead
        throw new UnsupportedOperationException(
                "AuthenticatorConfigurationReader.createAuthenticator() uses reflection and is no longer supported. "
                        + "Use AuthenticatorRegistry to get authenticator beans from Spring context. "
                        + "Authenticators should be Spring beans with @Component and @ConditionalOnProperty annotations.");
    }

    protected UserStore createUserStore(String userStoreClassName) {
        // Reflection removed - use Spring DI instead
        throw new UnsupportedOperationException(
                "AuthenticatorConfigurationReader.createUserStore() uses reflection and is no longer supported. "
                        + "UserStore implementations should be Spring beans with @Component and @ConditionalOnProperty annotations. "
                        + "Inject them via constructor injection in authenticator beans.");
    }

    public List<Authenticator> getAuthenticatorList() {
        return Collections.unmodifiableList(authenticatorList);
    }

    /**
     * We can specify whether authentication is enabled in the system for all request or not. This we can state in the
     * configuration. AuthenticatorConfigurationReader will read that information and will populate that to static
     * boolean authenticationEnabled. This method will say whether authentication is enabled in the system or disabled
     * in the system.
     *
     * @return <code>true</code> if authentication is enabled. Else <code>false</code>.
     */
    public static boolean isAuthenticationEnabled() {
        return authenticationEnabled;
    }

    /**
     * Comparator to sort authenticators based on authenticator priority.
     */
    public class AuthenticatorComparator implements Comparator<Authenticator> {

        @Override
        public int compare(Authenticator o1, Authenticator o2) {
            return (o1.getPriority() > o2.getPriority() ? -1 : (o1.getPriority() == o2.getPriority() ? 0 : 1));
        }
    }
}
