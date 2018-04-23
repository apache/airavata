package org.apache.airavata.agents.api;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.client.CredentialStoreClientFactory;
import org.apache.airavata.credential.store.cpi.CredentialStoreService;
import org.apache.airavata.credential.store.exception.CredentialStoreException;
import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;

public class AgentUtils {

    // TODO this is inefficient. Try to use a connection pool
    public static RegistryService.Client getRegistryServiceClient() throws AgentException {
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
            final String serverHost = ServerSettings.getRegistryServerHost();
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException | ApplicationSettingsException e) {
            throw new AgentException("Unable to create registry client...", e);
        }
    }

    public static CredentialStoreService.Client getCredentialClient() throws AgentException {
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getCredentialStoreServerPort());
            final String serverHost =ServerSettings.getCredentialStoreServerHost();
            return CredentialStoreClientFactory.createAiravataCSClient(serverHost, serverPort);
        } catch (CredentialStoreException | ApplicationSettingsException e) {
            throw new AgentException("Unable to create credential client...", e);
        }
    }
}
