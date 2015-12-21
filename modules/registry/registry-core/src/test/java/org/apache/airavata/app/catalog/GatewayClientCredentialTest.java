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

package org.apache.airavata.app.catalog;

import junit.framework.Assert;
import org.apache.airavata.app.catalog.util.Initialize;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class GatewayClientCredentialTest {
    private static Initialize initialize;
    private static AppCatalog appcatalog;
    private static final Logger logger = LoggerFactory.getLogger(GatewayClientCredentialTest.class);

    @Before
    public void setUp() {
        try {
            initialize = new Initialize("appcatalog-derby.sql");
            initialize.initializeDB();
            appcatalog = RegistryFactory.getAppCatalog();
        } catch (AppCatalogException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("********** TEAR DOWN ************");
        initialize.stopDerbyServer();
    }

    @Test
    public void gatewayProfileTest() throws Exception {
        GwyClientCredential gatewayClientCredential = appcatalog.getGatewayClientCredential();
        Map.Entry<String, String> cred = gatewayClientCredential.generateNewGatewayClientCredential("default");
        Assert.assertNotNull(cred.getKey());
        gatewayClientCredential.removeGatewayClientCredential(cred.getKey());
        Assert.assertNull(gatewayClientCredential.getAllGatewayClientCredentials("default"));
        appcatalog.getGatewayClientCredential().generateNewGatewayClientCredential("default");
        Assert.assertNotNull(gatewayClientCredential.getAllGatewayClientCredentials("default"));
    }

}
