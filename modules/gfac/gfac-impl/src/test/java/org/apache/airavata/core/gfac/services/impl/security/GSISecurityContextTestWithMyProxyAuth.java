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
///*
// *
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements.  See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership.  The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License.  You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// *
// */
//
//package org.apache.airavata.core.gfac.services.impl.security;
//
//import junit.framework.Assert;
//import org.apache.airavata.common.utils.DatabaseTestCases;
//import org.apache.airavata.common.utils.DerbyUtil;
//import org.apache.airavata.common.utils.ServerSettings;
//import org.apache.airavata.credential.store.store.CredentialReader;
//import org.apache.airavata.credential.store.store.CredentialReaderFactory;
//import org.apache.airavata.gfac.core.RequestData;
//import org.apache.airavata.gfac.gsissh.security.TokenizedMyProxyAuthInfo;
//import org.apache.log4j.Logger;
//import org.ietf.jgss.GSSCredential;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//
//public class GSISecurityContextTestWithMyProxyAuth extends DatabaseTestCases {
//
//    private static String userName;
//    private static String password;
//
//    private static final Logger log = Logger.getLogger(GSISecurityContextTestWithMyProxyAuth.class);
//
//    @BeforeClass
//    public static void setUpClass() throws Exception {
//
////        System.setProperty("myproxy.username", "ogce");
////        System.setProperty("myproxy.password", "");
//        userName = System.getProperty("myproxy.username");
//        password = System.getProperty("myproxy.password");
//        System.setProperty("myproxy.server", "myproxy.teragrid.org");
//        System.setProperty("myproxy.life", "3600");
//        System.setProperty("credential.store.keystore.url", "../configuration/server/src/main/resources/airavata.jks");
//        System.setProperty("credential.store.keystore.alias", "airavata");
//        System.setProperty("credential.store.keystore.password", "airavata");
//
//        if (userName == null || password == null || userName.trim().equals("") || password.trim().equals("")) {
//            log.error("===== Please set myproxy.username and myproxy.password system properties. =======");
//            Assert.fail("Please set myproxy.user and myproxy.password system properties.");
//        }
//
//        log.info("Using my proxy user name - " + userName);
//
//        setUpDatabase();
//
//    }
//
//    public static void setUpDatabase() throws Exception {
//        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());
//
//        waitTillServerStarts();
//
//
//        String createTable = "CREATE TABLE CREDENTIALS\n" + "(\n"
//                + "        GATEWAY_ID VARCHAR(256) NOT NULL,\n"
//                + "        TOKEN_ID VARCHAR(256) NOT NULL,\n"
//                + // Actual token used to identify the credential
//                "        CREDENTIAL BLOB NOT NULL,\n" + "        PORTAL_USER_ID VARCHAR(256) NOT NULL,\n"
//                + "        TIME_PERSISTED TIMESTAMP DEFAULT CURRENT_TIMESTAMP,\n"
//                + "        PRIMARY KEY (GATEWAY_ID, TOKEN_ID)\n" + ")";
//
//        String dropTable = "drop table CREDENTIALS";
//
//        try {
//            executeSQL(dropTable);
//        } catch (Exception e) {
//        }
//
//        executeSQL(createTable);
//
//    }
//
//    @AfterClass
//    public static void shutDownDatabase() throws Exception {
//        DerbyUtil.stopDerbyServer();
//    }
//
//    private GSSCredential getGSSCredentials() throws Exception {
//
//        TokenizedMyProxyAuthInfo gsiTokenizedMyProxyAuthInfo = getGSISecurityContext();
//        return gsiTokenizedMyProxyAuthInfo.getCredentials();
//    }
//
//    private TokenizedMyProxyAuthInfo getGSISecurityContext() throws Exception {
//
//        RequestData requestData = new RequestData();
//
//        requestData.setMyProxyUserName(userName);
//        requestData.setMyProxyPassword(password);
//        requestData.setMyProxyServerUrl(ServerSettings.getMyProxyServer());
//        requestData.setMyProxyLifeTime(ServerSettings.getMyProxyLifetime());
//        CredentialReader credentialReader = CredentialReaderFactory.createCredentialStoreReader(getDbUtil());
//
//        return new TokenizedMyProxyAuthInfo(requestData);
//    }
//
//    @Test
//    public void testGetGssCredentials() throws Exception {
//
//        Assert.assertNotNull(getGSSCredentials());
//    }
//    /*
//    @Test
//    public void testRenewCredentials() throws Exception {
//        GSISecurityContext gsiSecurityContext = getGSISecurityContext();
//        gsiSecurityContext.getGssCredentials();
//        Assert.assertNotNull(gsiSecurityContext.renewCredentials());
//
//    }
//
//    @Test
//    public void testGetCredentialsFromStore() throws Exception {
//        GSISecurityContext gsiSecurityContext = getGSISecurityContext();
//        Assert.assertNotNull(gsiSecurityContext.getCredentialsFromStore());
//
//    } */
//
//    @Test
//    public void testGetDefaultCredentials() throws Exception {
//        TokenizedMyProxyAuthInfo gsiSecurityContext = getGSISecurityContext();
//        Assert.assertNotNull(gsiSecurityContext.getDefaultCredentials());
//
//    }
//
//    @Test
//    public void testGetProxyCredentials() throws Exception {
//        TokenizedMyProxyAuthInfo gsiSecurityContext = getGSISecurityContext();
//        Assert.assertNotNull(gsiSecurityContext.getProxyCredentials());
//
//    }
//    /*
//    @Test
//    public void testRenewCredentialsAsATrustedHost() throws Exception {
//        GSISecurityContext gsiSecurityContext = getGSISecurityContext();
//        gsiSecurityContext.getGssCredentials();
//        Assert.assertNotNull(gsiSecurityContext.renewCredentialsAsATrustedHost());
//    } */
//
//}
