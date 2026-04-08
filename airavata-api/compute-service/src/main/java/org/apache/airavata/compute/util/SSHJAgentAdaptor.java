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
import java.util.*;
import java.util.stream.Collectors;
import org.apache.airavata.interfaces.AgentAdaptor;
import org.apache.airavata.interfaces.AgentException;
import org.apache.airavata.interfaces.CommandOutput;
import org.apache.airavata.interfaces.FileMetadata;
import org.apache.airavata.interfaces.SSHConnectionService;
import org.apache.airavata.interfaces.SSHConnectionService.*;
import org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionInterface;
import org.apache.airavata.model.appcatalog.computeresource.proto.JobSubmissionProtocol;
import org.apache.airavata.model.appcatalog.computeresource.proto.SSHJobSubmission;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageDirectoryInfo;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageVolumeInfo;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHJAgentAdaptor implements AgentAdaptor {

    private static final Logger logger = LoggerFactory.getLogger(SSHJAgentAdaptor.class);

    private SSHConnectionService sshConnectionService;
    private SSHConnection sshConnection;

    public SSHJAgentAdaptor() {}

    public SSHJAgentAdaptor(SSHConnectionService sshConnectionService) {
        this.sshConnectionService = sshConnectionService;
    }

    public void setSshConnectionService(SSHConnectionService sshConnectionService) {
        this.sshConnectionService = sshConnectionService;
    }

    public void init(String user, String host, int port, String publicKey, String privateKey, String passphrase)
            throws AgentException {
        try {
            sshConnection = sshConnectionService.connect(host, port, user, publicKey, privateKey, passphrase);
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

            ComputeResourceDescription computeResourceDescription =
                    AgentUtils.getRegistryServiceClient().getComputeResource(computeResource);

            logger.info("Fetching job submission interfaces for compute resource " + computeResource);

            Optional<JobSubmissionInterface> jobSubmissionInterfaceOp =
                    computeResourceDescription.getJobSubmissionInterfacesList().stream()
                            .filter(iface -> iface.getJobSubmissionProtocol() == JobSubmissionProtocol.SSH)
                            .findFirst();

            JobSubmissionInterface sshInterface = jobSubmissionInterfaceOp.orElseThrow(
                    () -> new AgentException("Could not find a SSH interface for compute resource " + computeResource));

            SSHJobSubmission sshJobSubmission = AgentUtils.getRegistryServiceClient()
                    .getSSHJobSubmission(sshInterface.getJobSubmissionInterfaceId());

            logger.info("Fetching credentials for cred store token " + token);

            SSHCredential sshCredential = AgentUtils.getCredentialClient().getSSHCredential(token, gatewayId);

            if (sshCredential == null) {
                throw new AgentException("Null credential for token " + token);
            }
            logger.info("Description for token : " + token + " : " + sshCredential.getDescription());

            String alternateHostName = sshJobSubmission.getAlternativeSshHostName();
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

            sshConnection = sshConnectionService.connect(
                    selectedHostName,
                    selectedPort,
                    userId,
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
            if (sshConnection != null) {
                sshConnection.close();
            }
        } catch (IOException e) {
            logger.warn("Failed to stop ssh connection due to : " + e.getMessage());
            // ignore
        }
    }

    @Override
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        SSHSession session = null;
        try {
            session = sshConnection.startSession();
            SSHCommandResult exec =
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
            if (session != null && isConnectionException(e)) {
                session.setErrored(true);
            }
            throw new AgentException(e);

        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void createDirectory(String path) throws AgentException {
        createDirectory(path, false);
    }

    @Override
    public void createDirectory(String path, boolean recursive) throws AgentException {
        SFTPSession sftpClient = null;
        try {
            sftpClient = sshConnection.newSFTPClient();
            if (recursive) {
                sftpClient.mkdirs(path);
            } else {
                sftpClient.mkdir(path);
            }
        } catch (Exception e) {
            if (sftpClient != null && isConnectionException(e)) {
                sftpClient.setErrored(true);
            }
            throw new AgentException(e);

        } finally {
            if (sftpClient != null) {
                try {
                    sftpClient.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void deleteDirectoryRecursively(SFTPSession sftpClient, String path) throws IOException {
        RemoteFileAttributes lstat = sftpClient.lstat(path);
        if (lstat.isDirectory()) {
            List<RemoteFileInfo> ls = sftpClient.ls(path);
            if (ls == null || ls.isEmpty()) {
                sftpClient.rmdir(path);
            } else {
                for (RemoteFileInfo r : ls) {
                    deleteDirectoryRecursively(sftpClient, path + "/" + r.getName());
                }
                sftpClient.rmdir(path);
            }
        } else {
            sftpClient.rm(path);
        }
    }

    @Override
    public void deleteDirectory(String path) throws AgentException {
        if (path == null || path.trim().isEmpty()) {
            throw new AgentException("Directory path cannot be null or empty");
        }
        SFTPSession sftpClient = null;
        try {
            sftpClient = sshConnection.newSFTPClient();
            deleteDirectoryRecursively(sftpClient, path);
        } catch (Exception e) {
            if (sftpClient != null && isConnectionException(e)) {
                sftpClient.setErrored(true);
            }
            logger.error("Error while deleting directory {}", path, e);
            throw new AgentException(e);

        } finally {
            if (sftpClient != null) {
                try {
                    sftpClient.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void uploadFile(String localFile, String remoteFile) throws AgentException {
        SCPSession fileTransfer = null;
        try {
            fileTransfer = sshConnection.newSCPFileTransfer();
            fileTransfer.upload(localFile, remoteFile);

        } catch (Exception e) {
            if (fileTransfer != null && isConnectionException(e)) {
                fileTransfer.setErrored(true);
            }
            throw new AgentException(e);

        } finally {
            if (fileTransfer != null) {
                try {
                    fileTransfer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void uploadFile(InputStream localInStream, FileMetadata metadata, String remoteFile) throws AgentException {
        SCPSession fileTransfer = null;

        try {
            fileTransfer = sshConnection.newSCPFileTransfer();
            fileTransfer.upload(
                    new SSHLocalSourceFile() {
                        @Override
                        public String getName() {
                            return metadata.getName();
                        }

                        @Override
                        public long getLength() {
                            return metadata.getSize();
                        }

                        @Override
                        public InputStream getInputStream() {
                            return localInStream;
                        }

                        @Override
                        public int getPermissions() {
                            return 420;
                        }

                        @Override
                        public boolean isFile() {
                            return true;
                        }

                        @Override
                        public boolean isDirectory() {
                            return false;
                        }
                    },
                    remoteFile);
        } catch (Exception e) {
            if (fileTransfer != null && isConnectionException(e)) {
                fileTransfer.setErrored(true);
            }
            throw new AgentException(e);

        } finally {
            if (fileTransfer != null) {
                try {
                    fileTransfer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void downloadFile(String remoteFile, String localFile) throws AgentException {
        SCPSession fileTransfer = null;
        try {
            fileTransfer = sshConnection.newSCPFileTransfer();
            fileTransfer.download(remoteFile, localFile);

        } catch (Exception e) {
            if (fileTransfer != null && isConnectionException(e)) {
                fileTransfer.setErrored(true);
            }
            throw new AgentException(e);

        } finally {
            if (fileTransfer != null) {
                try {
                    fileTransfer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void downloadFile(String remoteFile, OutputStream localOutStream, FileMetadata metadata)
            throws AgentException {
        SCPSession fileTransfer = null;
        try {
            fileTransfer = sshConnection.newSCPFileTransfer();
            fileTransfer.download(remoteFile, new SSHLocalDestFile() {
                @Override
                public long getLength() {
                    return metadata.getSize();
                }

                @Override
                public OutputStream getOutputStream() {
                    return localOutStream;
                }

                @Override
                public OutputStream getOutputStream(boolean append) {
                    return localOutStream;
                }

                @Override
                public SSHLocalDestFile getTargetFile(String filename) {
                    return this;
                }
            });
        } catch (Exception e) {
            if (fileTransfer != null && isConnectionException(e)) {
                fileTransfer.setErrored(true);
            }
            throw new AgentException(e);

        } finally {
            if (fileTransfer != null) {
                try {
                    fileTransfer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public List<String> listDirectory(String path) throws AgentException {
        SFTPSession sftpClient = null;
        try {
            sftpClient = sshConnection.newSFTPClient();
            List<RemoteFileInfo> ls = sftpClient.ls(path);
            return ls.stream().map(RemoteFileInfo::getName).collect(Collectors.toList());
        } catch (Exception e) {
            if (sftpClient != null && isConnectionException(e)) {
                sftpClient.setErrored(true);
            }
            throw new AgentException(e);

        } finally {
            if (sftpClient != null) {
                try {
                    sftpClient.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public Boolean doesFileExist(String filePath) throws AgentException {
        SFTPSession sftpClient = null;
        try {
            sftpClient = sshConnection.newSFTPClient();
            return sftpClient.statExistence(filePath) != null;
        } catch (Exception e) {
            if (sftpClient != null && isConnectionException(e)) {
                sftpClient.setErrored(true);
            }
            throw new AgentException(e);

        } finally {
            if (sftpClient != null) {
                try {
                    sftpClient.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException {
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
        SFTPSession sftpClient = null;
        try {
            sftpClient = sshConnection.newSFTPClient();
            RemoteFileAttributes stat = sftpClient.stat(remoteFile);
            FileMetadata metadata = new FileMetadata();
            metadata.setName(new File(remoteFile).getName());
            metadata.setSize(stat.getSize());
            metadata.setPermissions(stat.getPermissions());
            metadata.setDirectory(stat.isDirectory());
            return metadata;
        } catch (Exception e) {
            if (sftpClient != null && isConnectionException(e)) {
                sftpClient.setErrored(true);
            }
            throw new AgentException(e);

        } finally {
            if (sftpClient != null) {
                try {
                    sftpClient.close();
                } catch (IOException ignored) {
                }
            }
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

            StorageDirectoryInfo storageDirectoryInfo = StorageDirectoryInfo.newBuilder()
                    .setTotalSizeByteCount(numberOfKBytes * 1024)
                    .setTotalSize(numberOfKBytes + "kb")
                    .build();
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

            StorageVolumeInfo volumeInfo = StorageVolumeInfo.newBuilder()
                    .setTotalSize(totalSizeHuman)
                    .setUsedSize(usedSizeHuman)
                    .setAvailableSize(availableSizeHuman)
                    .setTotalSizeByteCount(totalSizeBytes)
                    .setUsedSizeByteCount(usedSizeBytes)
                    .setAvailableSizeByteCount(availableSizeBytes)
                    .setPercentageUsed(percentageUsed)
                    .setMountPoint(mountPoint)
                    .setFilesystemType(filesystemType)
                    .build();

            return volumeInfo;

        } catch (Exception e) {
            logger.error("Error parsing df output: {} for location {}", e.getMessage(), targetLocation, e);
            throw new AgentException(
                    "Error parsing df output: " + e.getMessage() + " for location " + targetLocation, e);
        }
    }

    /**
     * Check if the root cause is a connection exception (for pool error tracking).
     */
    private static boolean isConnectionException(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause.getClass().getSimpleName().equals("ConnectionException")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
