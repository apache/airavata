package org.apache.airavata.helix.core.support;

import org.apache.airavata.agents.api.*;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;

import java.io.File;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class AdaptorSupportImpl implements AdaptorSupport {

    private static AdaptorSupportImpl INSTANCE;

    private final AgentStore agentStore = new AgentStore();

    private AdaptorSupportImpl() {}

    public synchronized static AdaptorSupportImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdaptorSupportImpl();
        }
        return INSTANCE;
    }

    public void initializeAdaptor() {
    }

    public CommandOutput executeCommand(String command, String workingDirectory, String computeResourceId, String protocol, String authToken) throws AgentException {
        return fetchAdaptor(computeResourceId, protocol, authToken).executeCommand(command, workingDirectory);
    }

    public void createDirectory(String path, String computeResourceId, String protocol, String authToken) throws AgentException {
        fetchAdaptor(computeResourceId, protocol, authToken).createDirectory(path);
    }

    public void copyFile(String sourceFile, String destinationFile, String computeResourceId, String protocol, String authToken) throws AgentException {
        fetchAdaptor(computeResourceId, protocol, authToken).copyFile(sourceFile, destinationFile);
    }

    public AgentAdaptor fetchAdaptor(String computeResource, String protocol, String authToken) throws AgentException {
         return agentStore.fetchAdaptor(computeResource, protocol, authToken);
    }
}
