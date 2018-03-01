package org.apache.airavata.helix.agent.local;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.CommandOutput;

import java.util.List;

public class LocalAgentAdaptor implements AgentAdaptor {



    public void init(Object agentPams) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public void init(String computeResource, String gatewayId, String userId, String token) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public void createDirectory(String path) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public void copyFileTo(String localFile, String remoteFile) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public void copyFileFrom(String remoteFile, String localFile) throws AgentException {
        throw new AgentException("Operation not implemented");
    }


    @Override
    public List<String> listDirectory(String path) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException {
        throw new AgentException("Operation not implemented");
    }
}
