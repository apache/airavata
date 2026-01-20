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
package org.apache.airavata.agents.ssh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import org.apache.airavata.agents.api.AdaptorParams;
import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.CommandOutput;
import org.apache.airavata.agents.api.FileMetadata;
import org.apache.airavata.common.model.ComputeResourceDescription;
import org.apache.airavata.common.model.StorageDirectoryInfo;
import org.apache.airavata.common.model.StorageVolumeInfo;
import org.apache.airavata.credential.model.SSHCredential;
import org.apache.airavata.service.registry.RegistryService;
import org.apache.airavata.service.security.CredentialStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * SSH Agent Adaptor using SSHJ library.
 * Migrated from JSCH to SSHJ for better Java compatibility and maintenance.
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@Component
@Profile("!test")
public class SshAgentAdaptor implements AgentAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(SshAgentAdaptor.class);

    private SSHClient client = null;
    private String remoteHostname;
    private int remotePort;
    private String remoteUsername;

    protected final RegistryService registryService;
    protected final CredentialStoreService credentialService;

    public SshAgentAdaptor(RegistryService registryService, CredentialStoreService credentialService) {
        this.registryService = registryService;
        this.credentialService = credentialService;
    }

    public void init(AdaptorParams adaptorParams) throws AgentException {

        if (adaptorParams instanceof SshAdaptorParams params) {
            try {
                client = new SSHClient();

                // Configure host key verification
                if (params.isStrictHostKeyChecking() && params.getKnownHostsFilePath() != null) {
                    // Load known hosts file
                    try {
                        client.loadKnownHosts(new File(params.getKnownHostsFilePath()));
                    } catch (IOException e) {
                        logger.warn("Could not load known hosts file: {}", params.getKnownHostsFilePath(), e);
                        // Fall back to promiscuous verifier
                        client.addHostKeyVerifier(new PromiscuousVerifier());
                    }
                } else {
                    // Disable strict host key checking (equivalent to StrictHostKeyChecking=no)
                    client.addHostKeyVerifier(new PromiscuousVerifier());
                }

                client.connect(params.getHostName(), params.getPort());

                // Store connection info for logging
                this.remoteHostname = params.getHostName();
                this.remotePort = params.getPort();
                this.remoteUsername = params.getUserName();

                // Authenticate
                if (params.getPassword() != null) {
                    client.authPassword(params.getUserName(), params.getPassword());
                } else {
                    // Use key-based authentication
                    KeyProvider keyProvider = loadKeyProvider(params);
                    client.authPublickey(params.getUserName(), keyProvider);
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
                    registryService.getComputeResource(computeResourceId);

            logger.info("Fetching credentials for cred store token " + token);

            SSHCredential sshCredential = credentialService.getSSHCredential(token, gatewayId);
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
    public void destroy() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnect();
            } catch (IOException e) {
                logger.warn("Error disconnecting SSH client", e);
            }
        }
    }

    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        StandardOutReader commandOutput = new StandardOutReader();
        Session session = null;
        try {
            session = client.startSession();
            String fullCommand = (workingDirectory != null ? "cd " + workingDirectory + "; " : "") + command;
            Session.Command cmd = session.exec(fullCommand);

            // Read stdout
            try (InputStream out = cmd.getInputStream()) {
                commandOutput.readStdOutFromStream(out);
            }

            // Read stderr
            try (InputStream err = cmd.getErrorStream()) {
                commandOutput.readStdErrFromStream(err);
            }

            // Wait for command to complete
            cmd.join(30, TimeUnit.SECONDS);
            Integer exitStatus = cmd.getExitStatus();
            commandOutput.setExitCode(exitStatus != null ? exitStatus : -1);

            return commandOutput;
        } catch (IOException e) {
            logger.error("Failed to execute command {}", command, e);
            throw new AgentException("Failed to execute command " + command, e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (IOException e) {
                    logger.warn("Error closing session", e);
                }
            }
        }
    }

    public void createDirectory(String path) throws AgentException {
        createDirectory(path, false);
    }

    @Override
    public void createDirectory(String path, boolean recursive) throws AgentException {
        String command = (recursive ? "mkdir -p " : "mkdir ") + path;
        Session session = null;
        try {
            session = client.startSession();
            StandardOutReader stdOutReader = new StandardOutReader();

            Session.Command cmd = session.exec(command);

            try (InputStream out = cmd.getInputStream()) {
                stdOutReader.readStdOutFromStream(out);
            }

            try (InputStream err = cmd.getErrorStream()) {
                stdOutReader.readStdErrFromStream(err);
            }

            cmd.join(30, TimeUnit.SECONDS);

            if (stdOutReader.getStdError() != null && stdOutReader.getStdError().contains("mkdir:")) {
                throw new AgentException(stdOutReader.getStdError());
            }
        } catch (IOException e) {
            String msg = String.format(
                    "Unable to retrieve command output for command=%s on server=%s:%s connecting username=%s",
                    command, remoteHostname, remotePort, remoteUsername);
            logger.error(msg, e);
            throw new AgentException(msg, e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (IOException e) {
                    logger.warn("Error closing session", e);
                }
            }
        }
    }

    @Override
    public void deleteDirectory(String path) throws AgentException {
        if (path == null || path.trim().isEmpty()) {
            throw new AgentException("Directory path cannot be null or empty");
        }
        String escapedPath = path.replace("'", "'\"'\"'");
        String command = "rm -rf '" + escapedPath + "'";
        Session session = null;
        try {
            session = client.startSession();
            StandardOutReader stdOutReader = new StandardOutReader();

            Session.Command cmd = session.exec(command);

            try (InputStream out = cmd.getInputStream()) {
                stdOutReader.readStdOutFromStream(out);
            }

            try (InputStream err = cmd.getErrorStream()) {
                stdOutReader.readStdErrFromStream(err);
            }

            cmd.join(30, TimeUnit.SECONDS);

            if (stdOutReader.getStdError() != null && stdOutReader.getStdError().contains("rm:")) {
                throw new AgentException(stdOutReader.getStdError());
            }
        } catch (IOException e) {
            logger.error(
                    "Unable to retrieve command output. Command - {} on server - {}:{} connecting user name - {}",
                    command,
                    remoteHostname,
                    remotePort,
                    remoteUsername,
                    e);
            throw new AgentException(e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (IOException e) {
                    logger.warn("Error closing session", e);
                }
            }
        }
    }

    public void uploadFile(String localFile, String remoteFile) throws AgentException {
        try {
            SCPFileTransfer scp = client.newSCPFileTransfer();
            scp.upload(new net.schmizz.sshj.xfer.FileSystemFile(localFile), remoteFile);
        } catch (IOException e) {
            logger.error("Failed to transfer file from {} to remote location {}", localFile, remoteFile, e);
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
            SCPFileTransfer scp = client.newSCPFileTransfer();
            scp.download(remoteFile, new net.schmizz.sshj.xfer.FileSystemFile(localFile));
        } catch (FileNotFoundException e) {
            logger.error("Failed to find local file " + localFile, e);
            throw new AgentException("Failed to find local file " + localFile, e);
        } catch (IOException e) {
            logger.error("Error while handling streams", e);
            throw new AgentException("Error while handling streams", e);
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
        Session session = null;
        try {
            session = client.startSession();
            StandardOutReader stdOutReader = new StandardOutReader();

            Session.Command cmd = session.exec(command);

            try (InputStream out = cmd.getInputStream()) {
                stdOutReader.readStdOutFromStream(out);
            }

            try (InputStream err = cmd.getErrorStream()) {
                stdOutReader.readStdErrFromStream(err);
            }

            cmd.join(30, TimeUnit.SECONDS);

            if (stdOutReader.getStdError() != null && stdOutReader.getStdError().contains("ls:")) {
                throw new AgentException(stdOutReader.getStdError());
            }
            return Arrays.asList(stdOutReader.getStdOut().split("\n"));

        } catch (IOException e) {
            logger.error(
                    "Unable to retrieve command output. Command - " + command + " on server - "
                            + remoteHostname + ":" + remotePort + " connecting user name - "
                            + remoteUsername,
                    e);
            throw new AgentException(
                    "Unable to retrieve command output. Command - " + command + " on server - "
                            + remoteHostname + ":" + remotePort + " connecting user name - "
                            + remoteUsername,
                    e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (IOException e) {
                    logger.warn("Error closing session", e);
                }
            }
        }
    }

    @Override
    public Boolean doesFileExist(String filePath) throws AgentException {
        String command = "ls " + filePath;
        Session session = null;
        try {
            session = client.startSession();
            StandardOutReader stdOutReader = new StandardOutReader();

            Session.Command cmd = session.exec(command);

            try (InputStream out = cmd.getInputStream()) {
                stdOutReader.readStdOutFromStream(out);
            }

            try (InputStream err = cmd.getErrorStream()) {
                stdOutReader.readStdErrFromStream(err);
            }

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
                    "Unable to retrieve command output. Command - " + command + " on server - "
                            + remoteHostname + ":" + remotePort + " connecting user name - "
                            + remoteUsername,
                    e);
            throw new AgentException(
                    "Unable to retrieve command output. Command - " + command + " on server - "
                            + remoteHostname + ":" + remotePort + " connecting user name - "
                            + remoteUsername,
                    e);
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (IOException e) {
                    logger.warn("Error closing session", e);
                }
            }
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

    /**
     * Load KeyProvider from SshAdaptorParams.
     */
    private KeyProvider loadKeyProvider(SshAdaptorParams params) throws IOException {
        String privateKeyStr = new String(params.getPrivateKey(), StandardCharsets.UTF_8);
        String passphrase = params.getPassphrase();

        // Use SSHClient.loadKeys() to load key from string
        net.schmizz.sshj.userauth.password.PasswordFinder passwordFinder = null;
        if (passphrase != null && !passphrase.isEmpty()) {
            passwordFinder = net.schmizz.sshj.userauth.password.PasswordUtils.createOneOff(passphrase.toCharArray());
        }

        return client.loadKeys(privateKeyStr, null, passwordFinder);
    }
}
