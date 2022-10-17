package org.apache.airavata.metascheduler.core.utils;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.registry.api.client.RegistryServiceClientFactory;
import org.apache.airavata.registry.api.exception.RegistryServiceException;

import org.apache.airavata.registry.api.RegistryService;
import org.apache.airavata.registry.api.RegistryService.Client;

/**
 * This class contains all utility methods across scheduler sub projects
 */
public class Utils {
    /**
     * Provides registry client to access databases
     * @return RegistryService.Client
     */
    public static RegistryService.Client getRegistryServiceClient() {
        try {
            final int serverPort = Integer.parseInt(ServerSettings.getRegistryServerPort());
            final String serverHost = ServerSettings.getRegistryServerHost();
            return RegistryServiceClientFactory.createRegistryClient(serverHost, serverPort);
        } catch (RegistryServiceException | ApplicationSettingsException e) {
            throw new RuntimeException("Unable to create registry client...", e);
        }
    }
}
