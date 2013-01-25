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

package org.apache.airavata.services.registry.rest.security.session;

import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.services.registry.rest.security.AbstractAuthenticatorTest;
import org.apache.airavata.services.registry.rest.security.MyHttpServletRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * Session authenticator test.
 */
public class SessionAuthenticatorTest extends AbstractAuthenticatorTest {

    public SessionAuthenticatorTest() throws Exception {
        super("sessionAuthenticator");
    }

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        DerbyUtil.startDerbyInServerMode(getHostAddress(), getPort(), getUserName(), getPassword());

        waitTillServerStarts();

        String createSessionTable = "create table Persons ( sessionId varchar(255) )";
        executeSQL(createSessionTable);

        String insertSessionSQL = "INSERT INTO Persons VALUES('1234')";
        executeSQL(insertSessionSQL);
    }

    @AfterClass
    public static void shutDownDatabase() throws Exception {
        DerbyUtil.stopDerbyServer();
    }

    public void testAuthenticateSuccess() throws Exception {

        MyHttpServletRequest servletRequestRequest = new MyHttpServletRequest();
        servletRequestRequest.addHeader("sessionTicket", "1234");

        Assert.assertTrue(authenticator.authenticate(servletRequestRequest));

    }

    public void testAuthenticateFail() throws Exception {

        MyHttpServletRequest servletRequestRequest = new MyHttpServletRequest();
        servletRequestRequest.addHeader("sessionTicket", "12345");

        Assert.assertFalse(authenticator.authenticate(servletRequestRequest));

    }

    public void testCanProcess() throws Exception {

        MyHttpServletRequest servletRequestRequest = new MyHttpServletRequest();
        servletRequestRequest.addHeader("sessionTicket", "12345");

        Assert.assertTrue(authenticator.canProcess(servletRequestRequest));

    }
}
