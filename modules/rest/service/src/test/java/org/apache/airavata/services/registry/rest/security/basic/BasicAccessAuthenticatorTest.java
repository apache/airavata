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

package org.apache.airavata.services.registry.rest.security.basic;

import org.apache.airavata.common.utils.Constants;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.security.configurations.AuthenticatorConfigurationReader;
import org.apache.airavata.services.registry.rest.security.AbstractAuthenticatorTest;
import org.apache.airavata.services.registry.rest.security.MyHttpServletRequest;
import org.apache.airavata.services.registry.rest.security.session.SessionAuthenticator;
import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for basic access authenticator.
 */
public class BasicAccessAuthenticatorTest extends AbstractAuthenticatorTest {

    private SessionAuthenticator sessionAuthenticator;

    private AuthenticatorConfigurationReader authenticatorConfigurationReader;

    public BasicAccessAuthenticatorTest() throws Exception {
        super("basicAccessAuthenticator");
    }

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();

        String createTable = "create table AIRAVATA_USER ( USERID varchar(255), PASSWORD varchar(255) )";
        executeSQL(createTable);

        String insertSQL = "INSERT INTO AIRAVATA_USER VALUES('amilaj', 'secret')";
        executeSQL(insertSQL);

    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    @Override
    public void testAuthenticateSuccess() throws Exception {

        Assert.assertTrue(authenticator.authenticate(getRequest("amilaj:secret")));
    }

    @Override
    public void testAuthenticateFail() throws Exception {
        Assert.assertFalse(authenticator.authenticate(getRequest("amilaj:secret1")));
    }

    @Test
    public void testAuthenticateFailUserName() throws Exception {
        Assert.assertFalse(authenticator.authenticate(getRequest("amila:secret1")));
    }

    @Override
    public void testCanProcess() throws Exception {

        Assert.assertTrue(authenticator.canProcess(getRequest("amilaj:secret")));
    }

    private MyHttpServletRequest getRequest(String userPassword) {
        MyHttpServletRequest myHttpServletRequest = new MyHttpServletRequest();

        String authHeader = "Basic " + new String(Base64.encodeBase64(userPassword.getBytes()));

        myHttpServletRequest.addHeader("Authorization", authHeader);
        myHttpServletRequest.addHeader(Constants.GATEWAY_NAME, "default");

        return myHttpServletRequest;

    }

    public void tearDown() throws Exception {

    }

    /*
     * public void testConfigure() throws Exception {
     * 
     * BasicAccessAuthenticator basicAccessAuthenticator = (BasicAccessAuthenticator)authenticator;
     * 
     * assertEquals("AIRAVATA_USER", basicAccessAuthenticator.getUserTable()); assertEquals("USERID",
     * basicAccessAuthenticator.getUserNameColumn()); assertEquals("PASSWORD",
     * basicAccessAuthenticator.getPasswordColumn()); }
     */

}
