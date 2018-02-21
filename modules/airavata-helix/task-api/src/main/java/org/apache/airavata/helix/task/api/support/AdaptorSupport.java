package org.apache.airavata.helix.task.api.support;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.agents.api.JobSubmissionOutput;

import java.io.File;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface AdaptorSupport {
    public void initializeAdaptor();

    public AgentAdaptor fetchAdaptor(String computeResource, String protocol, String authToken) throws Exception;


    /**
     *
     * @param command
     * @param workingDirectory
     * @param computeResourceId
     * @param protocol
     * @param authToken
     * @throws Exception
     */
    public CommandOutput executeCommand(String command, String workingDirectory, String computeResourceId, String protocol, String authToken) throws Exception;

    /**
     *
     * @param path
     * @param computeResourceId
     * @param protocol
     * @param authToken
     * @throws Exception
     */
    public void createDirectory(String path, String computeResourceId, String protocol, String authToken) throws Exception;

    /**
     *
     * @param sourceFile
     * @param destinationFile
     * @param computeResourceId
     * @param protocol
     * @param authToken
     * @throws Exception
     */
    public void copyFile(String sourceFile, String destinationFile, String computeResourceId, String protocol, String authToken) throws Exception;
}
