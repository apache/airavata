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

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.apache.airavata.iam.util.PasswordDigester;
import org.apache.airavata.iam.util.UserStore;
import org.apache.airavata.iam.util.UserStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A user store which talks to LDAP server. User credentials and user information are stored in a LDAP server.
 * Uses standard JNDI to perform LDAP bind authentication.
 */
public class LDAPUserStore implements UserStore {

    protected static Logger log = LoggerFactory.getLogger(LDAPUserStore.class);

    private String ldapUrl;
    private String userDnTemplate;
    private PasswordDigester passwordDigester;

    @Override
    public boolean authenticate(String userName, Object credentials) throws UserStoreException {
        String hashedPassword = passwordDigester.getPasswordHashValue((String) credentials);
        String userDn = userDnTemplate.replace("{0}", userName);

        DirContext ctx = null;
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUrl);
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, userDn);
            env.put(Context.SECURITY_CREDENTIALS, hashedPassword);
            ctx = new InitialDirContext(env);
            return true;
        } catch (NamingException e) {
            log.warn("LDAP authentication failed for user {}: {}", userName, e.getMessage());
            return false;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                    log.debug("Error closing LDAP context", e);
                }
            }
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
                    } else if (element.getNodeName().equals("userDNTemplate")) {
                        userTemplate = element.getFirstChild().getNodeValue();
                    } else if (element.getNodeName().equals("passwordHashMethod")) {
                        passwordHashMethod = element.getFirstChild().getNodeValue();
                    }
                }
            }
        }

        passwordDigester = new PasswordDigester(passwordHashMethod);
        this.ldapUrl = url;
        this.userDnTemplate = userTemplate;
    }
}
