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
