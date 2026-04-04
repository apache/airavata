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
package org.apache.airavata.compute.util;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.airavata.interfaces.FileMetadata;
import org.apache.airavata.interfaces.SSHConnectionService;
import org.apache.airavata.interfaces.SSHConnectionService.*;
import org.apache.airavata.model.appcatalog.computeresource.proto.*;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageDirectoryInfo;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageVolumeInfo;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSH agent adaptor using SSHConnectionService.
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class SshAgentAdaptor implements AgentAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(SshAgentAdaptor.class);

    private SSHConnectionService sshConnectionService;
    private SSHConnection sshConnection;
    private String host;
    private int port;
    private String userName;

    public SshAgentAdaptor() {}

    public SshAgentAdaptor(SSHConnectionService sshConnectionService) {
        this.sshConnectionService = sshConnectionService;
    }

    public void setSshConnectionService(SSHConnectionService sshConnectionService) {
        this.sshConnectionService = sshConnectionService;
    }

    public void init(AdaptorParams adaptorParams) throws AgentException {

        if (adaptorParams instanceof SshAdaptorParams) {
            SshAdaptorParams params = SshAdaptorParams.class.cast(adaptorParams);
            try {
                this.host = params.getHostName();
                this.port = params.getPort();
                this.userName = params.getUserName();

                if (params.getPassword() != null) {
                    sshConnection = sshConnectionService.connectWithPassword(
                            params.getHostName(), params.getPort(), params.getUserName(), params.getPassword());
                } else {
                    sshConnection = sshConnectionService.connectSimple(
                            params.getHostName(),
                            params.getPort(),
                            params.getUserName(),
                            new String(params.getPublicKey()),
                            new String(params.getPrivateKey()),
                            params.getPassphrase());
                }

            } catch (IOException e) {
                throw new AgentException("Could not create ssh session for host " + params.getHostName(), e);
            }
        } else {
            throw new AgentException(
                    "Unknown parameter type to ssh initialize agent adaptor. Required SshAdaptorParams type");
        }
    }

    @Override
    public void init(String computeResourceId, String gatewayId, String userId, String token) throws AgentException {
        try {
            ComputeResourceDescription computeResourceDescription =
                    AgentUtils.getRegistryServiceClient().getComputeResource(computeResourceId);

            logger.info("Fetching credentials for cred store token " + token);

            SSHCredential sshCredential = AgentUtils.getCredentialClient().getSSHCredential(token, gatewayId);
            if (sshCredential == null) {
                throw new AgentException("Null credential for token " + token);
            }
            logger.info("Description for token : " + token + " : " + sshCredential.getDescription());

            SshAdaptorParams adaptorParams = new SshAdaptorParams();
            adaptorParams.setHostName(computeResourceDescription.getHostName());
            adaptorParams.setUserName(userId);
            adaptorParams.setPassphrase(sshCredential.getPassphrase());
            adaptorParams.setPrivateKey(sshCredential.getPrivateKey().getBytes());
            adaptorParams.setPublicKey(sshCredential.getPublicKey().getBytes());
            adaptorParams.setStrictHostKeyChecking(false);
            init(adaptorParams);

        } catch (Exception e) {
            logger.error(
                    "Error while initializing ssh agent for compute resource " + computeResourceId + " to token "
                            + token,
                    e);
            throw new AgentException(
                    "Error while initializing ssh agent for compute resource " + computeResourceId + " to token "
                            + token,
                    e);
        }
    }

    @Override
    public void destroy() {}

    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        StandardOutReader commandOutput = new StandardOutReader();
        try (SSHSession session = sshConnection.startSession()) {
            String fullCommand = (workingDirectory != null ? "cd " + workingDirectory + "; " : "") + command;
            SSHCommandResult cmd = session.exec(fullCommand);

            commandOutput.readStdOutFromStream(cmd.getInputStream());
            commandOutput.readStdErrFromStream(cmd.getErrorStream());
            cmd.join(30, TimeUnit.SECONDS);
            commandOutput.setExitCode(cmd.getExitStatus());
            return commandOutput;
        } catch (IOException e) {
            logger.error("Failed to execute command " + command, e);
            throw new AgentException("Failed to execute command " + command, e);
        }
    }

    public void createDirectory(String path) throws AgentException {
        createDirectory(path, false);
    }

    @Override
    public void createDirectory(String path, boolean recursive) throws AgentException {
        String command = (recursive ? "mkdir -p " : "mkdir ") + path;
        try (SSHSession session = sshConnection.startSession()) {
            SSHCommandResult cmd = session.exec(command);
            StandardOutReader stdOutReader = new StandardOutReader();

            stdOutReader.readStdOutFromStream(cmd.getInputStream());
            stdOutReader.readStdErrFromStream(cmd.getErrorStream());
            cmd.join(30, TimeUnit.SECONDS);

            if (stdOutReader.getStdError() != null && stdOutReader.getStdError().contains("mkdir:")) {
                throw new AgentException(stdOutReader.getStdError());
            }
        } catch (IOException e) {
            logger.error(
                    "Unable to retrieve command output. Command - " + command + " on server - " + host + ":" + port
                            + " connecting user name - " + userName,
                    e);
            throw new AgentException(e);
        }
    }

    @Override
    public void deleteDirectory(String path) throws AgentException {
        if (path == null || path.trim().isEmpty()) {
            throw new AgentException("Directory path cannot be null or empty");
        }
        String escapedPath = path.replace("'", "'\"'\"'");
        String command = "rm -rf '" + escapedPath + "'";
        try (SSHSession session = sshConnection.startSession()) {
            SSHCommandResult cmd = session.exec(command);
            StandardOutReader stdOutReader = new StandardOutReader();

            stdOutReader.readStdOutFromStream(cmd.getInputStream());
            stdOutReader.readStdErrFromStream(cmd.getErrorStream());
            cmd.join(30, TimeUnit.SECONDS);

            if (stdOutReader.getStdError() != null && stdOutReader.getStdError().contains("rm:")) {
                throw new AgentException(stdOutReader.getStdError());
            }
        } catch (IOException e) {
            logger.error(
                    "Unable to retrieve command output. Command - {} on server - {}:{} connecting user name - {}",
                    command,
                    host,
                    port,
                    userName,
                    e);
            throw new AgentException(e);
        }
    }

    public void uploadFile(String localFile, String remoteFile) throws AgentException {
        try {
            SCPSession scp = sshConnection.newSCPFileTransfer();
            scp.upload(localFile, remoteFile);
        } catch (IOException e) {
            logger.error("Failed to transfer file from " + localFile + " to remote location " + remoteFile, e);
            throw new AgentException(
                    "Failed to transfer file from " + localFile + " to remote location " + remoteFile, e);
        }
    }

    @Override
    public void uploadFile(InputStream localInStream, FileMetadata metadata, String remoteFile) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    public void downloadFile(String remoteFile, String localFile) throws AgentException {
        try {
            SCPSession scp = sshConnection.newSCPFileTransfer();
            scp.download(remoteFile, localFile);
        } catch (IOException e) {
            logger.error("Failed to transfer remote file from " + remoteFile + " to location " + localFile, e);
            throw new AgentException(
                    "Failed to transfer remote file from " + remoteFile + " to location " + localFile, e);
        }
    }

    @Override
    public void downloadFile(String remoteFile, OutputStream localOutStream, FileMetadata metadata)
            throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public List<String> listDirectory(String path) throws AgentException {
        String command = "ls " + path;
        try (SSHSession session = sshConnection.startSession()) {
            SSHCommandResult cmd = session.exec(command);
            StandardOutReader stdOutReader = new StandardOutReader();

            stdOutReader.readStdOutFromStream(cmd.getInputStream());
            stdOutReader.readStdErrFromStream(cmd.getErrorStream());
            cmd.join(30, TimeUnit.SECONDS);

            if (stdOutReader.getStdError() != null && stdOutReader.getStdError().contains("ls:")) {
                throw new AgentException(stdOutReader.getStdError());
            }
            return Arrays.asList(stdOutReader.getStdOut().split("\n"));

        } catch (IOException e) {
            logger.error(
                    "Unable to retrieve command output. Command - " + command + " on server - " + host + ":" + port
                            + " connecting user name - " + userName,
                    e);
            throw new AgentException(
                    "Unable to retrieve command output. Command - " + command + " on server - " + host + ":" + port
                            + " connecting user name - " + userName,
                    e);
        }
    }

    @Override
    public Boolean doesFileExist(String filePath) throws AgentException {
        String command = "ls " + filePath;
        try (SSHSession session = sshConnection.startSession()) {
            SSHCommandResult cmd = session.exec(command);
            StandardOutReader stdOutReader = new StandardOutReader();

            stdOutReader.readStdOutFromStream(cmd.getInputStream());
            stdOutReader.readStdErrFromStream(cmd.getErrorStream());
            cmd.join(30, TimeUnit.SECONDS);

            if (stdOutReader.getStdError() != null && stdOutReader.getStdError().contains("ls:")) {
                logger.info("Invalid file path " + filePath + ". stderr : " + stdOutReader.getStdError());
                return false;
            } else {
                String[] potentialFiles = stdOutReader.getStdOut().split("\n");
                if (potentialFiles.length > 1) {
                    logger.info("More than one file matching to given path " + filePath);
                    return false;
                } else if (potentialFiles.length == 0) {
                    logger.info("No file found for given path " + filePath);
                    return false;
                } else {
                    if (potentialFiles[0].trim().equals(filePath)) {
                        return true;
                    } else {
                        logger.info("Returned file name " + potentialFiles[0].trim()
                                + " does not match with given name " + filePath);
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            logger.error(
                    "Unable to retrieve command output. Command - " + command + " on server - " + host + ":" + port
                            + " connecting user name - " + userName,
                    e);
            throw new AgentException(
                    "Unable to retrieve command output. Command - " + command + " on server - " + host + ":" + port
                            + " connecting user name - " + userName,
                    e);
        }
    }

    @Override
    public List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public FileMetadata getFileMetadata(String remoteFile) throws AgentException {
        throw new AgentException("Operation not implemented");
    }

    @Override
    public StorageVolumeInfo getStorageVolumeInfo(String location) {
        throw new UnsupportedOperationException(
                "Operation not supported by SshAgentAdaptor. Use SSHJAgentAdaptor instead.");
    }

    @Override
    public StorageDirectoryInfo getStorageDirectoryInfo(String location) throws AgentException {
        throw new UnsupportedOperationException(
                "Operation not supported by SshAgentAdaptor. Use SSHJAgentAdaptor instead.");
    }
}
