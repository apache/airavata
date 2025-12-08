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
package org.apache.airavata.helix.adaptor;

import java.io.*;
import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.*;
import net.schmizz.sshj.sftp.FileMode.Type;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import net.schmizz.sshj.userauth.password.Resource;
import net.schmizz.sshj.xfer.FilePermission;
import net.schmizz.sshj.xfer.LocalDestFile;
import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;
import org.apache.airavata.agents.api.*;
import org.apache.airavata.helix.adaptor.wrapper.SCPFileTransferWrapper;
import org.apache.airavata.helix.adaptor.wrapper.SFTPClientWrapper;
import org.apache.airavata.helix.adaptor.wrapper.SessionWrapper;
import org.apache.airavata.helix.agent.ssh.StandardOutReader;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.storageresource.StorageDirectoryInfo;
import org.apache.airavata.model.appcatalog.storageresource.StorageVolumeInfo;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.apache.airavata.service.CredentialStoreService;
import org.apache.airavata.service.RegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SSHJAgentAdaptor implements AgentAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(SSHJAgentAdaptor.class);

    @Autowired
    protected RegistryService registryService;

    @Autowired
    protected CredentialStoreService credentialService;

    private PoolingSSHJClient sshjClient;

    protected void createPoolingSSHJClient(
            String user, String host, int port, String publicKey, String privateKey, String passphrase)
            throws IOException {
        DefaultConfig defaultConfig = new DefaultConfig();
        defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);

        sshjClient = new PoolingSSHJClient(defaultConfig, host, port == 0 ? 22 : port);
        sshjClient.addHostKeyVerifier(new HostKeyVerifier() {

            @Override
            public boolean verify(String hostname, int port, PublicKey key) {
                return true;
            }

            @Override
            public List<String> findExistingAlgorithms(String hostname, int port) {
                return Collections.emptyList();
            }
        });

        sshjClient.setMaxSessionsForConnection(1);

        PasswordFinder passwordFinder =
                passphrase != null ? PasswordUtils.createOneOff(passphrase.toCharArray()) : null;

        KeyProvider keyProvider = sshjClient.loadKeys(privateKey, publicKey, passwordFinder);

        final List<AuthMethod> am = new LinkedList<>();
        am.add(new AuthPublickey(keyProvider));

        am.add(new AuthKeyboardInteractive(new ChallengeResponseProvider() {
            @Override
            public List<String> getSubmethods() {
                return new ArrayList<>();
            }

            @Override
            public void init(Resource resource, String name, String instruction) {}

            @Override
            public char[] getResponse(String prompt, boolean echo) {
                return new char[0];
            }

            @Override
            public boolean shouldRetry() {
                return false;
            }
        }));

        sshjClient.auth(user, am);
    }

    public void init(String user, String host, int port, String publicKey, String privateKey, String passphrase)
            throws AgentException {
        try {
            createPoolingSSHJClient(user, host, port, publicKey, privateKey, passphrase);
        } catch (IOException e) {
            logger.error(
                    "Error while initializing sshj agent for user " + user + " host " + host + " for key starting with "
                            + publicKey.substring(0, 10),
                    e);
            throw new AgentException(
                    "Error while initializing sshj agent for user " + user + " host " + host + " for key starting with "
                            + publicKey.substring(0, 10),
                    e);
        }
    }

    @Override
    public void init(String computeResource, String gatewayId, String userId, String token) throws AgentException {
        try {
            logger.info("Initializing Compute Resource SSH Adaptor for compute resource : " + computeResource
                    + ", gateway : " + gatewayId + ", user " + userId + ", token : " + token);

            ComputeResourceDescription computeResourceDescription = registryService.getComputeResource(computeResource);

            logger.info("Fetching job submission interfaces for compute resource " + computeResource);

            Optional<JobSubmissionInterface> jobSubmissionInterfaceOp =
                    computeResourceDescription.getJobSubmissionInterfaces().stream()
                            .filter(iface -> iface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH)
                            .findFirst();

            JobSubmissionInterface sshInterface = jobSubmissionInterfaceOp.orElseThrow(
                    () -> new AgentException("Could not find a SSH interface for compute resource " + computeResource));

            SSHJobSubmission sshJobSubmission =
                    registryService.getSSHJobSubmission(sshInterface.getJobSubmissionInterfaceId());

            logger.info("Fetching credentials for cred store token " + token);

            SSHCredential sshCredential = credentialService.getSSHCredential(token, gatewayId);

            if (sshCredential == null) {
                throw new AgentException("Null credential for token " + token);
            }
            logger.info("Description for token : " + token + " : " + sshCredential.getDescription());

            String alternateHostName = sshJobSubmission.getAlternativeSSHHostName();
            String selectedHostName = (alternateHostName == null || "".equals(alternateHostName))
                    ? computeResourceDescription.getHostName()
                    : alternateHostName;

            int selectedPort = sshJobSubmission.getSshPort() == 0 ? 22 : sshJobSubmission.getSshPort();

            logger.info(
                    "Using user {}, Host {}, Port {} to create ssh client for compute resource {}",
                    userId,
                    selectedHostName,
                    selectedPort,
                    computeResource);

            createPoolingSSHJClient(
                    userId,
                    selectedHostName,
                    selectedPort,
                    sshCredential.getPublicKey(),
                    sshCredential.getPrivateKey(),
                    sshCredential.getPassphrase());

        } catch (Exception e) {
            logger.error(
                    "Error while initializing ssh agent for compute resource " + computeResource + " to token " + token,
                    e);
            throw new AgentException(
                    "Error while initializing ssh agent for compute resource " + computeResource + " to token " + token,
                    e);
        }
    }

    @Override
    public void destroy() {
        try {
            if (sshjClient != null) {
                sshjClient.disconnect();
                sshjClient.close();
            }
        } catch (IOException e) {
            logger.warn("Failed to stop sshj client for host " + sshjClient.getHost() + " and user "
                    + sshjClient.getUsername() + " due to : " + e.getMessage());
            // ignore
        }
    }

    @Override
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        SessionWrapper session = null;
        try {
            session = sshjClient.startSessionWrapper();
            Session.Command exec =
                    session.exec((workingDirectory != null ? "cd " + workingDirectory + "; " : "") + command);
            StandardOutReader standardOutReader = new StandardOutReader();

            try {
                standardOutReader.readStdOutFromStream(exec.getInputStream());
                standardOutReader.readStdErrFromStream(exec.getErrorStream());
            } finally {
                exec.close(); // closing the channel before getting the exit status
                standardOutReader.setExitCode(Optional.ofNullable(exec.getExitStatus())
                        .orElseThrow(() -> new Exception("Exit status received as null")));
            }
            return standardOutReader;

        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(session).ifPresent(ft -> ft.setErrored(true));
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(session).ifPresent(ss -> {
                try {
                    ss.close();
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void createDirectory(String path) throws AgentException {
        createDirectory(path, false);
    }

    @Override
    public void createDirectory(String path, boolean recursive) throws AgentException {
        SFTPClientWrapper sftpClient = null;
        try {
            sftpClient = sshjClient.newSFTPClientWrapper();
            if (recursive) {
                sftpClient.mkdirs(path);
            } else {
                sftpClient.mkdir(path);
            }
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(sftpClient).ifPresent(ft -> ft.setErrored(true));
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sftpClient).ifPresent(client -> {
                try {
                    client.close();
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void deleteDirectory(String path) throws AgentException {
        if (path == null || path.trim().isEmpty()) {
            throw new AgentException("Directory path cannot be null or empty");
        }
        SFTPClientWrapper sftpClient = null;
        try {
            sftpClient = sshjClient.newSFTPClientWrapper();
            sftpClient.rmdir(path);
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(sftpClient).ifPresent(ft -> ft.setErrored(true));
            }
            logger.error("Error while deleting directory {}", path, e);
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sftpClient).ifPresent(client -> {
                try {
                    client.close();
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void uploadFile(String localFile, String remoteFile) throws AgentException {
        SCPFileTransferWrapper fileTransfer = null;
        try {
            fileTransfer = sshjClient.newSCPFileTransferWrapper();
            fileTransfer.upload(localFile, remoteFile);

        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(fileTransfer).ifPresent(ft -> ft.setErrored(true));
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(fileTransfer).ifPresent(scpFileTransferWrapper -> {
                try {
                    scpFileTransferWrapper.close();
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void uploadFile(InputStream localInStream, FileMetadata metadata, String remoteFile) throws AgentException {
        SCPFileTransferWrapper fileTransfer = null;

        try {
            fileTransfer = sshjClient.newSCPFileTransferWrapper();
            fileTransfer.upload(
                    new LocalSourceFile() {
                        @Override
                        public String getName() {
                            return metadata.getName();
                        }

                        @Override
                        public long getLength() {
                            return metadata.getSize();
                        }

                        @Override
                        public InputStream getInputStream() throws IOException {
                            return localInStream;
                        }

                        @Override
                        public int getPermissions() throws IOException {
                            return 420; // metadata.getPermissions();
                        }

                        @Override
                        public boolean isFile() {
                            return true;
                        }

                        @Override
                        public boolean isDirectory() {
                            return false;
                        }

                        @Override
                        public Iterable<? extends LocalSourceFile> getChildren(LocalFileFilter filter)
                                throws IOException {
                            return null;
                        }

                        @Override
                        public boolean providesAtimeMtime() {
                            return false;
                        }

                        @Override
                        public long getLastAccessTime() throws IOException {
                            return 0;
                        }

                        @Override
                        public long getLastModifiedTime() throws IOException {
                            return 0;
                        }
                    },
                    remoteFile);
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(fileTransfer).ifPresent(ft -> ft.setErrored(true));
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(fileTransfer).ifPresent(scpFileTransferWrapper -> {
                try {
                    scpFileTransferWrapper.close();
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void downloadFile(String remoteFile, String localFile) throws AgentException {
        SCPFileTransferWrapper fileTransfer = null;
        try {
            fileTransfer = sshjClient.newSCPFileTransferWrapper();
            fileTransfer.download(remoteFile, localFile);

        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(fileTransfer).ifPresent(ft -> ft.setErrored(true));
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(fileTransfer).ifPresent(scpFileTransferWrapper -> {
                try {
                    scpFileTransferWrapper.close();
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void downloadFile(String remoteFile, OutputStream localOutStream, FileMetadata metadata)
            throws AgentException {
        SCPFileTransferWrapper fileTransfer = null;
        try {
            fileTransfer = sshjClient.newSCPFileTransferWrapper();
            fileTransfer.download(remoteFile, new LocalDestFile() {
                @Override
                public long getLength() {
                    return metadata.getSize();
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    return localOutStream;
                }

                @Override
                public OutputStream getOutputStream(boolean append) {
                    return localOutStream;
                }

                @Override
                public LocalDestFile getChild(String name) {
                    return null;
                }

                @Override
                public LocalDestFile getTargetFile(String filename) throws IOException {
                    return this;
                }

                @Override
                public LocalDestFile getTargetDirectory(String dirname) throws IOException {
                    return null;
                }

                @Override
                public void setPermissions(int perms) throws IOException {}

                @Override
                public void setLastAccessedTime(long t) throws IOException {}

                @Override
                public void setLastModifiedTime(long t) throws IOException {}
            });
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(fileTransfer).ifPresent(ft -> ft.setErrored(true));
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(fileTransfer).ifPresent(scpFileTransferWrapper -> {
                try {
                    scpFileTransferWrapper.close();
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public List<String> listDirectory(String path) throws AgentException {
        SFTPClientWrapper sftpClient = null;
        try {
            sftpClient = sshjClient.newSFTPClientWrapper();
            List<RemoteResourceInfo> ls = sftpClient.ls(path);
            return ls.stream().map(RemoteResourceInfo::getName).collect(Collectors.toList());
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(sftpClient).ifPresent(ft -> ft.setErrored(true));
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sftpClient).ifPresent(client -> {
                try {
                    client.close();
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public Boolean doesFileExist(String filePath) throws AgentException {
        SFTPClientWrapper sftpClient = null;
        try {
            sftpClient = sshjClient.newSFTPClientWrapper();
            return sftpClient.statExistence(filePath) != null;
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(sftpClient).ifPresent(ft -> ft.setErrored(true));
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sftpClient).ifPresent(client -> {
                try {
                    client.close();
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException {

        /*try (SFTPClient sftpClient = sshjClient.newSFTPClientWrapper()) {
            List<RemoteResourceInfo> ls = sftpClient.ls(parentPath, resource -> isMatch(resource.getName(), fileName));
            return ls.stream().map(RemoteResourceInfo::getPath).collect(Collectors.toList());
        } catch (Exception e) {
            throw new AgentException(e);
        }*/
        /*
        if (fileName.endsWith("*")) {
            throw new AgentException("Wildcards that ends with * does not support for security reasons. Specify an extension");
        }
        */

        CommandOutput commandOutput =
                executeCommand("ls " + fileName, parentPath); // This has a risk of returning folders also
        String[] filesTmp = commandOutput.getStdOut().split("\n");
        List<String> files = new ArrayList<>();
        for (String f : filesTmp) {
            if (!f.isEmpty()) {
                files.add(f);
            }
        }
        return files;
    }

    @Override
    public FileMetadata getFileMetadata(String remoteFile) throws AgentException {
        SFTPClientWrapper sftpClient = null;
        try {
            sftpClient = sshjClient.newSFTPClientWrapper();
            FileAttributes stat = sftpClient.stat(remoteFile);
            FileMetadata metadata = new FileMetadata();
            metadata.setName(new File(remoteFile).getName());
            metadata.setSize(stat.getSize());
            metadata.setPermissions(FilePermission.toMask(stat.getPermissions()));
            metadata.setDirectory(stat.getType() == Type.DIRECTORY);
            return metadata;
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(sftpClient).ifPresent(ft -> ft.setErrored(true));
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sftpClient).ifPresent(scpFileTransferWrapper -> {
                try {
                    scpFileTransferWrapper.close();
                } catch (IOException e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public StorageVolumeInfo getStorageVolumeInfo(String location) throws AgentException {
        try {
            String targetLocation = location;
            if (targetLocation == null || targetLocation.trim().isEmpty()) {
                CommandOutput homeOutput = executeCommand("echo $HOME", null);

                if (homeOutput.getExitCode() != 0
                        || homeOutput.getStdOut() == null
                        || homeOutput.getStdOut().trim().isEmpty()) {
                    logger.error("Failed to determine user's home directory: {}", homeOutput.getStdError());
                    throw new AgentException("Failed to determine user's home directory: " + homeOutput.getStdError());
                }
                targetLocation = homeOutput.getStdOut().trim();
            }

            // Escape location to prevent command injection and handle spaces
            String escapedLocation = targetLocation.replace("'", "'\"'\"'");
            String dfCommand = "df -P -T -h '" + escapedLocation + "'";
            String dfBytesCommand = "df -P -T '" + escapedLocation + "'";

            CommandOutput dfHumanOutput = executeCommand(dfCommand, null);
            CommandOutput dfBytesOutput = executeCommand(dfBytesCommand, null);

            if (dfHumanOutput.getExitCode() != 0) {
                logger.error(
                        "Failed to execute df command for location {}: {}",
                        targetLocation,
                        dfHumanOutput.getStdError());
                throw new AgentException("Failed to execute df command for location " + targetLocation + ": "
                        + dfHumanOutput.getStdError());
            }

            if (dfBytesOutput.getExitCode() != 0) {
                logger.error(
                        "Failed to execute df command for location {}: {}",
                        targetLocation,
                        dfBytesOutput.getStdError());
                throw new AgentException("Failed to execute df command for location " + targetLocation + ": "
                        + dfBytesOutput.getStdError());
            }

            return parseDfOutput(dfHumanOutput.getStdOut(), dfBytesOutput.getStdOut(), targetLocation);

        } catch (Exception e) {
            logger.error("Error while retrieving storage volume info for location " + location, e);
            throw new AgentException("Error while retrieving storage volume info for location " + location, e);
        }
    }

    @Override
    public StorageDirectoryInfo getStorageDirectoryInfo(String location) throws AgentException {
        try {
            String targetLocation = location;
            if (targetLocation == null || targetLocation.trim().isEmpty()) {
                CommandOutput homeOutput = executeCommand("echo $HOME", null);

                if (homeOutput.getExitCode() != 0
                        || homeOutput.getStdOut() == null
                        || homeOutput.getStdOut().trim().isEmpty()) {
                    logger.error("Failed to determine user's home directory: {}", homeOutput.getStdError());
                    throw new AgentException("Failed to determine user's home directory: " + homeOutput.getStdError());
                }
                targetLocation = homeOutput.getStdOut().trim();
            }

            // Escape location to prevent command injection and handle spaces
            String escapedLocation = targetLocation.replace("'", "'\"'\"'");
            String duKBytesCommand = "du -sk '" + escapedLocation + "'";

            CommandOutput duKBytesOutput = executeCommand(duKBytesCommand, null);

            if (duKBytesOutput.getExitCode() != 0) {
                logger.error(
                        "Failed to execute du -sk command for location {}: {}",
                        targetLocation,
                        duKBytesOutput.getStdError());
                throw new AgentException("Failed to execute du -sk command for location " + targetLocation + ": "
                        + duKBytesOutput.getStdError());
            }

            String outputKbStr = duKBytesOutput.getStdOut().trim();
            logger.info("OutputKbStr: for du -ku {} is {}", location, outputKbStr);
            String numberOfKBytesStr = outputKbStr.split(" ")[0];

            long numberOfKBytes = Long.parseLong(numberOfKBytesStr);

            StorageDirectoryInfo storageDirectoryInfo = new StorageDirectoryInfo();
            storageDirectoryInfo.setTotalSizeBytes(numberOfKBytes * 1024);
            storageDirectoryInfo.setTotalSize(numberOfKBytes + "kb");
            return storageDirectoryInfo;

        } catch (Exception e) {
            logger.error("Error while retrieving storage directory info for location " + location, e);
            throw new AgentException("Error while retrieving storage directory info for location " + location, e);
        }
    }

    private StorageVolumeInfo parseDfOutput(String dfHumanOutput, String dfBytesOutput, String targetLocation)
            throws AgentException {
        try {
            // Parse df -P -T -h output (POSIX format with filesystem type)
            String[] humanLines = dfHumanOutput.split("\n");
            String[] bytesLines = dfBytesOutput.split("\n");

            if (humanLines.length < 2 || bytesLines.length < 2) {
                logger.error(
                        "Unexpected df output format while parsing storage volume info for location {}",
                        targetLocation);
                throw new AgentException(
                        "Unexpected df output format while parsing storage volume info for location " + targetLocation);
            }

            // Skip the header line and get the data line
            String humanDataLine = humanLines[1].trim();
            String bytesDataLine = bytesLines[1].trim();

            // Split by whitespace. POSIX format uses fixed width columns separated by spaces
            String[] humanFields = humanDataLine.split("\\s+");
            String[] bytesFields = bytesDataLine.split("\\s+");

            if (humanFields.length < 7 || bytesFields.length < 7) {
                logger.error(
                        "Unexpected df output format - insufficient fields while parsing storage volume info for location {}",
                        targetLocation);
                throw new AgentException(
                        "Unexpected df output format - insufficient fields while parsing storage volume info for location "
                                + targetLocation);
            }

            String filesystemType = humanFields[1]; // ext4, xfs, etc.
            String totalSizeHuman = humanFields[2];
            String usedSizeHuman = humanFields[3];
            String availableSizeHuman = humanFields[4];
            String capacityStr = humanFields[5].replace("%", "");

            // If Mount point contains spaces
            StringBuilder mountPointBuilder = new StringBuilder();
            for (int i = 6; i < humanFields.length; i++) {
                if (i > 6) {
                    mountPointBuilder.append(" ");
                }
                mountPointBuilder.append(humanFields[i]);
            }
            String mountPoint = mountPointBuilder.toString();

            // Parse bytes output. Same format but in 1024-byte blocks
            long totalSizeBlocks = Long.parseLong(bytesFields[2]);
            long usedSizeBlocks = Long.parseLong(bytesFields[3]);
            long availableSizeBlocks = Long.parseLong(bytesFields[4]);

            // Convert 1024-byte blocks to bytes
            long totalSizeBytes = totalSizeBlocks * 1024L;
            long usedSizeBytes = usedSizeBlocks * 1024L;
            long availableSizeBytes = availableSizeBlocks * 1024L;

            double percentageUsed = Double.parseDouble(capacityStr);

            StorageVolumeInfo volumeInfo = new StorageVolumeInfo();
            volumeInfo.setTotalSize(totalSizeHuman);
            volumeInfo.setUsedSize(usedSizeHuman);
            volumeInfo.setAvailableSize(availableSizeHuman);
            volumeInfo.setTotalSizeBytes(totalSizeBytes);
            volumeInfo.setUsedSizeBytes(usedSizeBytes);
            volumeInfo.setAvailableSizeBytes(availableSizeBytes);
            volumeInfo.setPercentageUsed(percentageUsed);
            volumeInfo.setMountPoint(mountPoint);
            volumeInfo.setFilesystemType(filesystemType);

            return volumeInfo;

        } catch (Exception e) {
            logger.error("Error parsing df output: {} for location {}", e.getMessage(), targetLocation, e);
            throw new AgentException(
                    "Error parsing df output: " + e.getMessage() + " for location " + targetLocation, e);
        }
    }
}
