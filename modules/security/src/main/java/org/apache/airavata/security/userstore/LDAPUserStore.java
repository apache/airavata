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

import org.apache.airavata.security.UserStore;
import org.apache.airavata.security.UserStoreException;
import org.apache.airavata.security.util.PasswordDigester;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.ldap.JndiLdapContextFactory;
import org.apache.shiro.realm.ldap.JndiLdapRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A user store which talks to LDAP server. User credentials and user information are stored in a LDAP server.
 */
public class LDAPUserStore implements UserStore {

    private JndiLdapRealm ldapRealm;

    protected static Logger log = LoggerFactory.getLogger(LDAPUserStore.class);

    private PasswordDigester passwordDigester;

    public boolean authenticate(String userName, Object credentials) throws UserStoreException {

        AuthenticationToken authenticationToken = new UsernamePasswordToken(userName,
                passwordDigester.getPasswordHashValue((String) credentials));

        AuthenticationInfo authenticationInfo;
        try {
            authenticationInfo = ldapRealm.getAuthenticationInfo(authenticationToken);
        } catch (AuthenticationException e) {
            log.warn(e.getLocalizedMessage(), e);
            return false;
        }

        return authenticationInfo != null;

    }

    @Override
    public boolean authenticate(Object credentials) throws UserStoreException {
        log.error("LDAP user store only supports authenticating with user name and password.");
        throw new UnsupportedOperationException();
    }

    public void configure(Node specificConfigurationNode) throws UserStoreException {

        /**
         * <specificConfiguration> <ldap> <url>ldap://localhost:10389</url> <systemUser>admin</systemUser>
         * <systemUserPassword>secret</systemUserPassword> <userDNTemplate>uid={0},ou=system</userDNTemplate> </ldap>
         * </specificConfiguration>
         */

        Node configurationNode = null;
        if (specificConfigurationNode != null) {
            NodeList nodeList = specificConfigurationNode.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node n = nodeList.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    configurationNode = n;
                }
            }
        }

        String url = null;
        String systemUser = null;
        String systemUserPassword = null;
        String userTemplate = null;
        String passwordHashMethod = null;

        if (configurationNode != null) {
            NodeList nodeList = configurationNode.getChildNodes();

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node n = nodeList.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) n;

                    if (element.getNodeName().equals("url")) {
                        url = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("systemUser")) {
                        systemUser = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("systemUserPassword")) {
                        systemUserPassword = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("userDNTemplate")) {
                        userTemplate = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("passwordHashMethod")) {
                        passwordHashMethod = element.getFirstChild().getNodeValue();
                    }
                }
            }
        }

        passwordDigester = new PasswordDigester(passwordHashMethod);

        initializeLDAP(url, systemUser, systemUserPassword, userTemplate);

    }

    protected void initializeLDAP(String ldapUrl, String systemUser, String systemUserPassword, String userNameTemplate) {

        JndiLdapContextFactory jndiLdapContextFactory = new JndiLdapContextFactory();

        jndiLdapContextFactory.setUrl(ldapUrl);
        jndiLdapContextFactory.setSystemUsername(systemUser);
        jndiLdapContextFactory.setSystemPassword(systemUserPassword);

        ldapRealm = new JndiLdapRealm();

        ldapRealm.setContextFactory(jndiLdapContextFactory);
        ldapRealm.setUserDnTemplate(userNameTemplate);

        ldapRealm.init();

    }
}
