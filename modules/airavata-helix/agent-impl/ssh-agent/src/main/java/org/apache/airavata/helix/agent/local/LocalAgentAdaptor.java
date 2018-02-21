package org.apache.airavata.helix.agent.local;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.agents.api.JobSubmissionOutput;

import java.io.File;
import java.util.List;

public class LocalAgentAdaptor implements AgentAdaptor {



    public void init(Object agentPams) throws AgentException {

    }

    @Override
    public void init(String computeResource, String gatewayId, String userId, String token) throws AgentException {

    }

    @Override
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        return null;
    }

    @Override
    public void createDirectory(String path) throws AgentException {

    }

    @Override
    public void copyFile(String sourceFile, String destinationFile) throws AgentException {

    }

    @Override
    public List<String> listDirectory(String path) throws AgentException {
        return null;
    }
}
