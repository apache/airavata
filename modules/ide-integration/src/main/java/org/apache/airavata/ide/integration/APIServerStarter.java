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

import org.apache.airavata.api.thrift.server.AiravataServiceServer;
import org.apache.airavata.api.thrift.server.CredentialServiceServer;
import org.apache.airavata.api.thrift.server.OrchestratorServiceServer;
import org.apache.airavata.api.thrift.server.ProfileServiceServer;
import org.apache.airavata.api.thrift.server.RegistryServiceServer;
import org.apache.airavata.api.thrift.server.SharingRegistryServer;
import org.apache.airavata.manager.dbevent.DBEventManagerRunner;

public class APIServerStarter {

    public static void main(String[] args) throws Exception {
        DBEventManagerRunner dbEventManagerRunner = new DBEventManagerRunner();
        RegistryServiceServer registryAPIServer = new RegistryServiceServer();
        CredentialServiceServer credentialStoreServer = new CredentialServiceServer();
        SharingRegistryServer sharingRegistryServer = new SharingRegistryServer();
        AiravataServiceServer airavataAPIServer = new AiravataServiceServer();
        OrchestratorServiceServer orchestratorServer = new OrchestratorServiceServer();
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
