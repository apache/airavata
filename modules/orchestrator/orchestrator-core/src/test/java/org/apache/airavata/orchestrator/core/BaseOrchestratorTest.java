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
package org.apache.airavata.orchestrator.core;


//import org.apache.airavata.client.tools.DocumentCreatorNew;

public class BaseOrchestratorTest {

/*    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private UserResource userResource;
    private Initialize initialize;
    private DocumentCreatorNew documentCreator;

    public void setUp() throws Exception {
        initialize = new Initialize("registry-derby.sql");
        initialize.initializeDB();
        gatewayResource = (GatewayResource) ResourceUtils.getGateway(ServerSettings.getSystemUserGateway());
        workerResource = (WorkerResource) ResourceUtils.getWorker(gatewayResource.getGatewayName(), ServerSettings.getDefaultUser());
        userResource = new UserResource();
        userResource.setUserName(ServerSettings.getDefaultUser());
        userResource.setPassword(ServerSettings.getDefaultUser());

        documentCreator = new DocumentCreatorNew(getAiravataClient());
        documentCreator.createLocalHostDocs();
        documentCreator.createPBSDocsForOGCE_Echo();
    }

    public void tearDown() throws Exception {
        initialize.stopDerbyServer();
    }

    public GatewayResource getGatewayResource() {
        return gatewayResource;
    }

    public WorkerResource getWorkerResource() {
        return workerResource;
    }

    public UserResource getUserResource() {
        return userResource;
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

    public DocumentCreatorNew getDocumentCreator() {
        return documentCreator;
    }

    public void setDocumentCreator(DocumentCreatorNew documentCreator) {
        this.documentCreator = documentCreator;
    }

    private void settingServerProperties(){

    }*/
}
