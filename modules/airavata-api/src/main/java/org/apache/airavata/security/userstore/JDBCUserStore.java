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

import javax.sql.DataSource;
import org.apache.airavata.common.exception.CoreExceptions.ApplicationSettingsException;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.security.UserStoreException;
import org.apache.airavata.security.util.PasswordDigester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The JDBC user store implementation.
 * Migrated from Apache Shiro to Spring Security JDBC authentication.
 */
@Component
@Profile("!test")
public class JDBCUserStore extends AbstractJDBCUserStore {

    protected static Logger log = LoggerFactory.getLogger(JDBCUserStore.class);

    private DaoAuthenticationProvider authenticationProvider;
    private PasswordDigester passwordDigester;
    private DataSource dataSource;

    public JDBCUserStore() {
        // Constructor
    }

    @Override
    public boolean authenticate(String userName, Object credentials) throws UserStoreException {
        try {
            String password = passwordDigester.getPasswordHashValue((String) credentials);
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(userName, password);

            Authentication authentication = authenticationProvider.authenticate(authRequest);
            return authentication != null && authentication.isAuthenticated();
        } catch (BadCredentialsException e) {
            log.debug("JDBC authentication failed for user: {}", userName, e);
            return false;
        } catch (Exception e) {
            log.error("Error during JDBC authentication", e);
            throw new UserStoreException("Error during JDBC authentication", e);
        }
    }

    /**
     * Authenticate using credentials object (session token).
     *
     * <p>JDBCUserStore only supports username/password authentication, not session token
     * authentication. Use {@link #authenticate(String, Object)} with username and password instead.
     *
     * @param credentials the credentials (not used)
     * @return authentication result
     * @throws UserStoreException if authentication fails
     * @throws UnsupportedOperationException always - JDBC user store only supports username/password auth
     */
    @Override
    public boolean authenticate(Object credentials) throws UserStoreException {
        log.error("JDBC user store only supports user name, password based authentication.");
        throw new UnsupportedOperationException(
                "JDBCUserStore only supports username/password authentication - use authenticate(String, Object) instead");
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

        var message = String.format(
                "Configuring DB parameters for authenticator with User name Table - %s User name column - %s Password column - %s",
                userTable, userNameColumn, passwordColumn);
        log.debug(message);
    }

    protected void initializeDatabaseLookup(String passwordColumn, String userTable, String userNameColumn)
            throws ApplicationSettingsException, UserStoreException {
        try {
            DBUtil dbUtil =
                    new DBUtil(getDatabaseURL(), getDatabaseUserName(), getDatabasePassword(), getDatabaseDriver());
            dataSource = dbUtil.getDataSource();

            // Create user details service
            UserDetailsService userDetailsService =
                    new JdbcUserDetailsService(dataSource, userTable, userNameColumn, passwordColumn);

            // Create password encoder adapter
            PasswordEncoder passwordEncoder = new PasswordDigesterEncoder(passwordDigester);

            // Create authentication provider using modern constructor-based API
            // Spring Security 6.5+ deprecates no-arg constructor and setUserDetailsService()
            // Modern approach: use constructor with UserDetailsService, then set PasswordEncoder
            authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
            authenticationProvider.setPasswordEncoder(passwordEncoder);
        } catch (Exception e) {
            throw new UserStoreException("Failed to initialize JDBC authentication", e);
        }
    }

    public PasswordDigester getPasswordDigester() {
        return passwordDigester;
    }

    /**
     * UserDetailsService implementation for JDBC authentication
     */
    private static class JdbcUserDetailsService implements UserDetailsService {
        private final JdbcTemplate jdbcTemplate;
        private final String userTable;
        private final String userNameColumn;
        private final String passwordColumn;

        public JdbcUserDetailsService(
                DataSource dataSource, String userTable, String userNameColumn, String passwordColumn) {
            this.jdbcTemplate = new JdbcTemplate(dataSource);
            this.userTable = userTable;
            this.userNameColumn = userNameColumn;
            this.passwordColumn = passwordColumn;
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            String sql = String.format("SELECT %s FROM %s WHERE %s = ?", passwordColumn, userTable, userNameColumn);

            try {
                String password = jdbcTemplate.queryForObject(sql, String.class, username);
                if (password == null) {
                    throw new UsernameNotFoundException("User not found: " + username);
                }
                return User.withUsername(username)
                        .password(password)
                        .authorities("ROLE_USER")
                        .build();
            } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                throw new UsernameNotFoundException("User not found: " + username, e);
            }
        }
    }

    /**
     * Password encoder adapter for PasswordDigester
     */
    private static class PasswordDigesterEncoder implements PasswordEncoder {
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
