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
package org.apache.airavata.ide.integration;

import org.apache.airavata.api.server.AiravataAPIServer;
import org.apache.airavata.credential.store.server.CredentialStoreServer;
import org.apache.airavata.db.event.manager.DBEventManagerRunner;
import org.apache.airavata.orchestrator.server.OrchestratorServer;
import org.apache.airavata.registry.api.service.RegistryAPIServer;
import org.apache.airavata.service.profile.server.ProfileServiceServer;
import org.apache.airavata.sharing.registry.server.SharingRegistryServer;

public class APIServerStarter {

    public static void main(String args[]) throws Exception {
        DBEventManagerRunner dbEventManagerRunner = new DBEventManagerRunner();
        RegistryAPIServer registryAPIServer = new RegistryAPIServer();
        CredentialStoreServer credentialStoreServer = new CredentialStoreServer();
        SharingRegistryServer sharingRegistryServer = new SharingRegistryServer();
        AiravataAPIServer airavataAPIServer = new AiravataAPIServer();
        OrchestratorServer orchestratorServer = new OrchestratorServer();
        ProfileServiceServer profileServiceServer = new ProfileServiceServer();

        dbEventManagerRunner.start();
        registryAPIServer.start();
        credentialStoreServer.start();
        sharingRegistryServer.start();
        airavataAPIServer.start();
        orchestratorServer.start();
        profileServiceServer.start();
    }
}
