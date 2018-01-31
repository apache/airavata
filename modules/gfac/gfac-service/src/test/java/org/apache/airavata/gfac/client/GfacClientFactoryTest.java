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
package org.apache.airavata.gfac.client;

import org.apache.airavata.gfac.client.util.Initialize;
import org.apache.airavata.gfac.cpi.GfacService;
import org.apache.airavata.gfac.server.GfacServer;
import org.apache.zookeeper.server.ServerCnxnFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GfacClientFactoryTest {
    private final static Logger logger = LoggerFactory.getLogger(GfacClientFactoryTest.class);
//    private DocumentCreator documentCreator;
    private GfacService.Client gfacClient;
    private int NUM_CONCURRENT_REQUESTS = 1;
    Initialize initialize;
    GfacServer service;
    private static ServerCnxnFactory cnxnFactory;
/*
    @Test
    public void setUp() {
    	AiravataUtils.setExecutionAsServer();
        initialize = new Initialize("registry-derby.sql");
        initialize.initializeDB();
        AiravataZKUtils.startEmbeddedZK(cnxnFactory);
        try {
            service = (new GfacServer());
            service.start();
            registry = RegistryFactory.getDefaultExpCatalog();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        AiravataUtils.setExecutionAsServer();
        documentCreator = new DocumentCreator(getAiravataAPI());
        documentCreator.createLocalHostDocs();

        try {
            service.stop();
            cnxnFactory.shutdown();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private AiravataAPI getAiravataAPI() {
        AiravataAPI airavataAPI = null;
            try {
                String systemUserName = ServerSettings.getDefaultUser();
                String gateway = ServerSettings.getDefaultUserGateway();
                airavataAPI = AiravataAPIFactory.getAPI(gateway, systemUserName);
            } catch (ApplicationSettingsException e) {
                e.printStackTrace();
            } catch (AiravataAPIInvocationException e) {
                e.printStackTrace();
            }
        return airavataAPI;
    }
*/

    private void storeDescriptors() {

    }


}
