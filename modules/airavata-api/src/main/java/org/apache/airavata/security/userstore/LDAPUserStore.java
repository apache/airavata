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

import org.apache.airavata.security.UserStore;
import org.apache.airavata.security.UserStoreException;
import org.apache.airavata.security.util.PasswordDigester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.PasswordComparisonAuthenticator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A user store which talks to LDAP server. User credentials and user information are stored in a LDAP server.
 * Migrated from Apache Shiro to Spring Security LDAP.
 */
@Component
@Profile("!test")
public class LDAPUserStore implements UserStore {

    private LdapAuthenticationProvider ldapAuthenticationProvider;
    private LdapContextSource contextSource;

    protected static Logger log = LoggerFactory.getLogger(LDAPUserStore.class);

    private PasswordDigester passwordDigester;

    public boolean authenticate(String userName, Object credentials) throws UserStoreException {
        try {
            String password = passwordDigester.getPasswordHashValue((String) credentials);
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(userName, password);

            Authentication authentication = ldapAuthenticationProvider.authenticate(authRequest);
            return authentication != null && authentication.isAuthenticated();
        } catch (BadCredentialsException e) {
            log.warn("LDAP authentication failed for user: {}", userName, e);
            return false;
        } catch (Exception e) {
            log.error("Error during LDAP authentication", e);
            throw new UserStoreException("Error during LDAP authentication", e);
        }
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

    protected void initializeLDAP(String ldapUrl, String systemUser, String systemUserPassword, String userNameTemplate)
            throws UserStoreException {

        try {
            // Create LDAP context source
            contextSource = new LdapContextSource();
            contextSource.setUrl(ldapUrl);
            contextSource.setUserDn(systemUser);
            contextSource.setPassword(systemUserPassword);
            contextSource.afterPropertiesSet();
        } catch (Exception e) {
            throw new UserStoreException("Failed to initialize LDAP context", e);
        }

        // Create user search - convert Shiro template format to Spring LDAP format
        // Shiro: uid={0},ou=system -> Spring: (uid={0}) with base DN ou=system
        String searchBase = "";
        String searchFilter = userNameTemplate;
        if (userNameTemplate.contains(",")) {
            int commaIndex = userNameTemplate.indexOf(",");
            searchBase = userNameTemplate.substring(commaIndex + 1);
            searchFilter = userNameTemplate.substring(0, commaIndex);
            // Convert format: uid={0} -> (uid={0})
            if (!searchFilter.startsWith("(")) {
                searchFilter = "(" + searchFilter + ")";
            }
        }

        FilterBasedLdapUserSearch userSearch = new FilterBasedLdapUserSearch(searchBase, searchFilter, contextSource);

        // Create authenticator
        PasswordComparisonAuthenticator authenticator = new PasswordComparisonAuthenticator(contextSource);
        authenticator.setUserSearch(userSearch);
        authenticator.setPasswordEncoder(new PasswordDigesterEncoder(passwordDigester));

        // Create authorities populator (empty for now - can be extended)
        DefaultLdapAuthoritiesPopulator authoritiesPopulator = new DefaultLdapAuthoritiesPopulator(contextSource, "");

        // Create authentication provider
        ldapAuthenticationProvider = new LdapAuthenticationProvider(authenticator, authoritiesPopulator);
    }

    /**
     * Password encoder adapter for PasswordDigester
     */
    private static class PasswordDigesterEncoder
            implements org.springframework.security.crypto.password.PasswordEncoder {
        private final PasswordDigester passwordDigester;

        public PasswordDigesterEncoder(PasswordDigester passwordDigester) {
            this.passwordDigester = passwordDigester;
        }

        @Override
        public String encode(CharSequence rawPassword) {
            try {
                return passwordDigester.getPasswordHashValue(rawPassword.toString());
            } catch (UserStoreException e) {
                throw new RuntimeException("Failed to encode password", e);
            }
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            try {
                String hashed = passwordDigester.getPasswordHashValue(rawPassword.toString());
                return hashed.equals(encodedPassword);
            } catch (UserStoreException e) {
                throw new RuntimeException("Failed to match password", e);
            }
        }
    }
}
