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

package org.apache.airavata.services.registry.rest.security;

import junit.framework.TestCase;
import org.apache.airavata.common.utils.DatabaseTestCases;
import org.apache.airavata.common.utils.DerbyUtil;
import org.apache.airavata.security.Authenticator;
import org.apache.airavata.security.configurations.AuthenticatorConfigurationReader;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * An abstract class to implement test cases for authenticators.
 */
public abstract class AbstractAuthenticatorTest extends DatabaseTestCases {

    private String authenticatorName;

    protected Authenticator authenticator = null;

    public AbstractAuthenticatorTest(String name) throws Exception {
        authenticatorName = name;
    }

    protected AuthenticatorConfigurationReader authenticatorConfigurationReader;

    @Before
    public void setUp() throws Exception {

        authenticatorConfigurationReader = new AuthenticatorConfigurationReader();
        authenticatorConfigurationReader.init(this.getClass().getClassLoader()
                .getResourceAsStream("authenticators.xml"));

        List<Authenticator> listAuthenticators = authenticatorConfigurationReader.getAuthenticatorList();

        if (listAuthenticators == null) {
            throw new Exception("No authenticators found !");
        }

        for (Authenticator a : listAuthenticators) {
            if (a.getAuthenticatorName().equals(authenticatorName)) {
                authenticator = a;
            }
        }

        if (authenticator == null) {
            throw new Exception("Could not find an authenticator with name " + authenticatorName);
        }

    }

    @Test
    public abstract void testAuthenticateSuccess() throws Exception;

    @Test
    public abstract void testAuthenticateFail() throws Exception;

    @Test
    public abstract void testCanProcess() throws Exception;
}
