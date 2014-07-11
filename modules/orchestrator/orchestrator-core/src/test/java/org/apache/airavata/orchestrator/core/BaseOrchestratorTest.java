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
package org.apache.airavata.orchestrator.core;


import org.apache.airavata.client.AiravataAPIFactory;
import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.client.tools.DocumentCreator;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.orchestrator.core.util.Initialize;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.resources.*;

public class BaseOrchestratorTest {

    private GatewayResource gatewayResource;
    private WorkerResource workerResource;
    private UserResource userResource;
    private Initialize initialize;
    private DocumentCreator documentCreator;

    public void setUp() throws Exception {
        initialize = new Initialize("registry-derby.sql");
        initialize.initializeDB();
        gatewayResource = (GatewayResource) ResourceUtils.getGateway(ServerSettings.getSystemUserGateway());
        workerResource = (WorkerResource) ResourceUtils.getWorker(gatewayResource.getGatewayName(), ServerSettings.getDefaultUser());
        userResource = new UserResource();
        userResource.setUserName(ServerSettings.getDefaultUser());
        userResource.setPassword(ServerSettings.getDefaultUser());

        documentCreator = new DocumentCreator(getAiravataAPI());
        documentCreator.createLocalHostDocs();
        documentCreator.createGramDocs();
        documentCreator.createPBSDocsForOGCE();
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

    private AiravataAPI getAiravataAPI() {
        AiravataAPI airavataAPI = null;
        try {
            String systemUserName = ServerSettings.getSystemUser();
            String gateway = ServerSettings.getSystemUserGateway();
            airavataAPI = AiravataAPIFactory.getAPI(gateway, systemUserName);
        } catch (ApplicationSettingsException e) {
            e.printStackTrace();
        } catch (AiravataAPIInvocationException e) {
            e.printStackTrace();
        }
        return airavataAPI;
    }

    public DocumentCreator getDocumentCreator() {
        return documentCreator;
    }

    public void setDocumentCreator(DocumentCreator documentCreator) {
        this.documentCreator = documentCreator;
    }

    private void settingServerProperties(){

    }
}
