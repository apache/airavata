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
package org.apache.airavata.orchestrator.client;

//import org.apache.airavata.client.AiravataAPIFactory;
//import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
//import org.apache.airavata.client.tools.DocumentCreator;
//import org.apache.airavata.client.tools.DocumentCreatorNew;

public class OrchestratorClientFactoryTest {
/*    private DocumentCreatorNew documentCreator;
    private OrchestratorService.Client orchestratorClient;
    private Registry registry;
    private int NUM_CONCURRENT_REQUESTS = 1;
    Initialize initialize;
    OrchestratorServer service;
    private static ServerCnxnFactory cnxnFactory;

    @Test
    public void setUp() {
    	AiravataUtils.setExecutionAsServer();
        initialize = new Initialize("registry-derby.sql");
        initialize.initializeDB();
        System.setProperty(Constants.ZOOKEEPER_SERVER_PORT,"2185");
        AiravataZKUtils.startEmbeddedZK(cnxnFactory);

        try {
            service = (new OrchestratorServer());
            service.start();
            registry = RegistryFactory.getDefaultExpCatalog();
            documentCreator = new DocumentCreatorNew(getAiravataClient());
            documentCreator.createLocalHostDocs();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        AiravataUtils.setExecutionAsServer();
        try {
            service.stop();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private Airavata.Client getAiravataClient() {
        Airavata.Client client = null;
            try {
                client = AiravataClientFactory.createAiravataClient("localhost", 8930);
            } catch (AiravataClientConnectException e) {
                e.printStackTrace();
            }
        return client;
    }

    private void storeDescriptors() {

    }*/
}
