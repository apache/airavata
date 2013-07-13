/*
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
 *
 */

package org.apache.airavata.gfac.context.security;

import junit.framework.Assert;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.CredentialReaderFactory;
import org.apache.airavata.gfac.RequestData;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 7/12/13
 * Time: 12:58 PM
 */

@Test(enabled=false)
public class GSISecurityContextTest extends DatabaseTestCases {

    private static String userName;
    private static String password;

    private static final Logger log = Logger.getLogger(GSISecurityContextTest.class);

    @BeforeClass
    public static void setUpClass() throws Exception {
        AiravataUtils.setExecutionAsServer();

        userName = System.getProperty("myproxy.user");
        password = System.getProperty("myproxy.password");

        if (userName == null || password == null || userName.trim().equals("") || password.trim().equals("")) {
            log.error("===== Please set myproxy.user and myproxy.password system properties. =======");
            Assert.fail("Please set myproxy.user and myproxy.password system properties.");
        }

        log.info("Using my proxy user name - " + userName);

        setUpDatabase();

    }

    public static void setUpDatabase() throws Exception {
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();

        /*
         * String createTable = "CREATE TABLE CREDENTIALS\n" + "(\n" + "        GATEWAY_NAME VARCHAR(256) NOT NULL,\n" +
         * "        COMMUNITY_USER_NAME VARCHAR(256) NOT NULL,\n" + "        CREDENTIAL BLOB NOT NULL,\n" +
         * "        PRIVATE_KEY BLOB NOT NULL,\n" + "        NOT_BEFORE VARCHAR(256) NOT NULL,\n" +
         * "        NOT_AFTER VARCHAR(256) NOT NULL,\n" + "        LIFETIME INTEGER NOT NULL,\n" +
         * "        REQUESTING_PORTAL_USER_NAME VARCHAR(256) NOT NULL,\n" +
         * "        REQUESTED_TIME TIMESTAMP DEFAULT '0000-00-00 00:00:00',\n" +
         * "        PRIMARY KEY (GATEWAY_NAME, COMMUNITY_USER_NAME)\n" + ")";
         */

        String createTable = "CREATE TABLE CREDENTIALS\n" + "(\n"
                + "        GATEWAY_ID VARCHAR(256) NOT NULL,\n"
                + "        TOKEN_ID VARCHAR(256) NOT NULL,\n"
                + // Actual token used to identify the credential
                "        CREDENTIAL BLOB NOT NULL,\n" + "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n"
                + "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
                + "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n" + ")";

        String dropTable = "drop table CREDENTIALS";

        try {
            executeSQL(dropTable);
        } catch (Exception e) {
        }

        executeSQL(createTable);

    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @Test
    public void testGetTrustedCertificatePath() throws Exception {

        File f = new File(GSISecurityContext.getTrustedCertificatePath());
        Assert.assertTrue(f.exists());
    }

    @Test
    public void testGetGssCredentials() throws Exception {

        RequestData requestData = new RequestData();

        requestData.setMyProxyUserName(userName);
        requestData.setMyProxyPassword(password);

        CredentialReader credentialReader = CredentialReaderFactory.createCredentialStoreReader(getDbUtil());

        GSISecurityContext gsiSecurityContext = new GSISecurityContext(credentialReader, requestData);

        Assert.assertNotNull(gsiSecurityContext.getGssCredentials());


    }

    @Test
    public void testRenewCredentials() throws Exception {

    }

    @Test
    public void testGetCredentialsFromStore() throws Exception {

    }

    @Test
    public void testGetDefaultCredentials() throws Exception {

    }

    @Test
    public void testGetProxyCredentials() throws Exception {

    }

    @Test
    public void testRenewCredentialsAsATrustedHost() throws Exception {

    }
}
