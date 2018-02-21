package org.apache.airavata.agents.api;

import java.io.File;
import java.util.List;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface AgentAdaptor {

    public void init(String computeResource, String gatewayId, String userId, String token) throws AgentException;

    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException;

    public void createDirectory(String path) throws AgentException;

    public void copyFile(String sourceFile, String destinationFile) throws AgentException;

    public List<String> listDirectory(String path) throws AgentException;
}
