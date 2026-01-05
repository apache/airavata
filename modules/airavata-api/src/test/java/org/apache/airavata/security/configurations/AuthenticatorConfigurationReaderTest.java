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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URLDecoder;
import java.util.List;
import org.apache.airavata.security.Authenticator;
import org.apache.airavata.security.userstore.JDBCUserStore;
import org.apache.airavata.security.userstore.LDAPUserStore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A test class for authenticator configuration reader. Reads the authenticators.xml in resources directory.
 *
 * @deprecated This test is disabled because AuthenticatorConfigurationReader uses reflection which has been removed.
 * Use AuthenticatorRegistry instead, which collects authenticator beans from Spring context.
 */
@Deprecated
@Disabled(
        "AuthenticatorConfigurationReader uses reflection which has been removed. Use AuthenticatorRegistry for testing authenticators.")
public class AuthenticatorConfigurationReaderTest {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticatorConfigurationReaderTest.class);
    private String configurationFile = URLDecoder.decode(
            this.getClass().getClassLoader().getResource("authenticators.xml").getFile());

    @Test
    public void testInit() throws Exception {
        File f = new File(".");
        logger.info("File absolute path: {}", f.getAbsolutePath());
        File file = new File(configurationFile);
        if (!file.exists() && !file.canRead()) {
            throw new Exception("Error reading configuration file " + configurationFile);
        }
        AuthenticatorConfigurationReader authenticatorConfigurationReader = new AuthenticatorConfigurationReader();
        authenticatorConfigurationReader.init(configurationFile);
        assertTrue(AuthenticatorConfigurationReader.isAuthenticationEnabled());
        List<Authenticator> authenticators = authenticatorConfigurationReader.getAuthenticatorList();
        assertEquals(3, authenticators.size());
        for (Authenticator authenticator : authenticators) {
            if (authenticator instanceof TestDBAuthenticator1) {
                assertEquals("dbAuthenticator1", authenticator.getAuthenticatorName());
                assertEquals(6, authenticator.getPriority());
                assertTrue(authenticator.isEnabled());
                assertEquals(
                        "jdbc:h2:mem:testdb1;DB_CLOSE_DELAY=-1",
                        ((TestDBAuthenticator1) authenticator).getDatabaseURL());
                assertEquals("org.h2.Driver", ((TestDBAuthenticator1) authenticator).getDatabaseDriver());
                assertEquals("sa", ((TestDBAuthenticator1) authenticator).getDatabaseUserName());
                assertEquals("secret1", ((TestDBAuthenticator1) authenticator).getDatabasePassword());
                assertNotNull(authenticator.getUserStore());
                assertTrue(authenticator.getUserStore() instanceof JDBCUserStore);
                JDBCUserStore jdbcUserStore = (JDBCUserStore) authenticator.getUserStore();
                assertEquals("MD5", jdbcUserStore.getPasswordDigester().getHashMethod());
            } else if (authenticator instanceof TestDBAuthenticator2) {
                assertEquals("dbAuthenticator2", authenticator.getAuthenticatorName());
                assertEquals(7, authenticator.getPriority());
                assertTrue(authenticator.isEnabled());
                assertTrue(authenticator.getUserStore() instanceof LDAPUserStore);
            } else if (authenticator instanceof TestDBAuthenticator3) {
                assertEquals("dbAuthenticator3", authenticator.getAuthenticatorName());
                assertEquals(8, authenticator.getPriority());
                assertTrue(authenticator.isEnabled());
                assertEquals(
                        "jdbc:h2:mem:testdb3;DB_CLOSE_DELAY=-1",
                        ((TestDBAuthenticator3) authenticator).getDatabaseURL());
                assertEquals("org.h2.Driver", ((TestDBAuthenticator3) authenticator).getDatabaseDriver());
                assertEquals("sa", ((TestDBAuthenticator3) authenticator).getDatabaseUserName());
                assertEquals("secret3", ((TestDBAuthenticator3) authenticator).getDatabasePassword());
                assertNotNull(authenticator.getUserStore());
                assertTrue(authenticator.getUserStore() instanceof JDBCUserStore);
            }
        }
        assertEquals(8, authenticators.get(0).getPriority());
        assertEquals(7, authenticators.get(1).getPriority());
        assertEquals(6, authenticators.get(2).getPriority());
    }

    @Test
    public void testDisabledAuthenticator() throws Exception {
        String disabledConfiguration = URLDecoder.decode(this.getClass()
                .getClassLoader()
                .getResource("disabled-authenticator.xml")
                .getFile());
        AuthenticatorConfigurationReader authenticatorConfigurationReader = new AuthenticatorConfigurationReader();
        authenticatorConfigurationReader.init(disabledConfiguration);
        assertFalse(AuthenticatorConfigurationReader.isAuthenticationEnabled());
    }
}
