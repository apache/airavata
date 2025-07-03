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
package org.apache.airavata.credential.store.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 8/25/13
 * Time: 10:28 AM
 */
public class ConfigurationReaderTest {

    @BeforeEach
    public void setUp() throws Exception {}

    @Test
    public void testGetSuccessUrl() throws Exception {

        ConfigurationReader configurationReader = new ConfigurationReader();
        System.out.println(configurationReader.getSuccessUrl());
        assertEquals("/credential-store/success.jsp", configurationReader.getSuccessUrl());
    }

    @Test
    public void testGetErrorUrl() throws Exception {

        ConfigurationReader configurationReader = new ConfigurationReader();
        assertEquals("/credential-store/error.jsp", configurationReader.getErrorUrl());
    }

    @Test
    public void testRedirectUrl() throws Exception {

        ConfigurationReader configurationReader = new ConfigurationReader();
        assertEquals("/credential-store/show-redirect.jsp", configurationReader.getPortalRedirectUrl());
    }
}
