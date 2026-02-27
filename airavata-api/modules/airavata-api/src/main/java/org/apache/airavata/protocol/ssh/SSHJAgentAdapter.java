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
package org.apache.airavata.protocol.ssh;

import com.hierynomus.sshj.key.KeyAlgorithms;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.sftp.FileMode.Type;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import net.schmizz.sshj.userauth.password.Resource;
import net.schmizz.sshj.xfer.FilePermission;
import net.schmizz.sshj.xfer.LocalDestFile;
import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.protocol.AgentAdapter;
import org.apache.airavata.protocol.AgentAdapter.AgentException;
import org.apache.airavata.protocol.AgentAdapter.CommandOutput;
import org.apache.airavata.protocol.AgentAdapter.FileMetadata;
import org.apache.airavata.protocol.ResourceLookup;
import org.apache.airavata.protocol.ssh.PoolingSSHJClient.SCPFileTransferResource;
import org.apache.airavata.protocol.ssh.PoolingSSHJClient.SFTPClientResource;
import org.apache.airavata.protocol.ssh.PoolingSSHJClient.SessionResource;
import org.apache.airavata.storage.resource.model.StorageDirectoryInfo;
import org.apache.airavata.storage.resource.model.StorageVolumeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHJAgentAdapter implements AgentAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SSHJAgentAdapter.class);

    private final ResourceLookup resourceLookup;
    private final CredentialStoreService credentialService;
    private PoolingSSHJClient sshjClient;

    public SSHJAgentAdapter(ResourceLookup resourceLookup, CredentialStoreService credentialService) {
        this.resourceLookup = resourceLookup;
        this.credentialService = credentialService;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    public void init(String user, String host, int port, String publicKey, String privateKey, String passphrase)
            throws AgentException {
        try {
            createPoolingSSHJClient(user, host, port, publicKey, privateKey, passphrase);
        } catch (IOException e) {
            throw new AgentException("Error initializing sshj agent for user " + user + " host " + host, e);
        }
    }

    @Override
    public void init(String resourceId, String gatewayId, String userId, String token) throws AgentException {
        try {
            logger.info("Initializing SSH adapter for resource {}, gateway {}, user {}", resourceId, gatewayId, userId);

            var resource = resourceLookup.getResource(resourceId);
            if (resource == null) {
                throw new AgentException("No resource found for id " + resourceId);
            }

            var sshCredential = credentialService.getSSHCredential(token, gatewayId);
            if (sshCredential == null) {
                throw new AgentException("Null credential for token " + token);
            }

            createPoolingSSHJClient(
                    userId,
                    resource.getHostName(),
                    resource.getPort() == 0 ? 22 : resource.getPort(),
                    sshCredential.getPublicKey(),
                    sshCredential.getPrivateKey(),
                    sshCredential.getPassphrase());

        } catch (Exception e) {
            throw new AgentException("Error initializing ssh agent for resource " + resourceId, e);
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
            logger.warn(
                    "Failed to stop sshj client for host {} and user {}: {}",
                    sshjClient.getHost(),
                    sshjClient.getUsername(),
                    e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Command execution
    // -------------------------------------------------------------------------

    @Override
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        return withSession(session -> {
            var exec = session.getSession()
                    .exec((workingDirectory != null ? "cd " + workingDirectory + "; " : "") + command);

            String stdOut;
            String stdErr;
            int exitCode;
            try {
                stdOut = readStream(exec.getInputStream());
                stdErr = readStream(exec.getErrorStream());
            } finally {
                exec.close();
                exitCode = Optional.ofNullable(exec.getExitStatus())
                        .orElseThrow(() -> new Exception("Exit status received as null"));
            }
            return new CommandOutput(stdOut, stdErr, exitCode);
        });
    }

    // -------------------------------------------------------------------------
    // Directory operations
    // -------------------------------------------------------------------------

    @Override
    public void createDirectory(String path) throws AgentException {
        createDirectory(path, false);
    }

    @Override
    public void createDirectory(String path, boolean recursive) throws AgentException {
        withSftp(sftp -> {
            if (recursive) {
                sftp.mkdirs(path);
            } else {
                sftp.mkdir(path);
            }
            return null;
        });
    }

    @Override
    public void deleteDirectory(String path) throws AgentException {
        if (path == null || path.trim().isEmpty()) {
            throw new AgentException("Directory path cannot be null or empty");
        }
        withSftp(sftp -> {
            sftp.rmdir(path);
            return null;
        });
    }

    @Override
    public List<String> listDirectory(String path) throws AgentException {
        return withSftp(
                sftp -> sftp.ls(path).stream().map(RemoteResourceInfo::getName).toList());
    }

    // -------------------------------------------------------------------------
    // File operations
    // -------------------------------------------------------------------------

    @Override
    public void uploadFile(String localFile, String remoteFile) throws AgentException {
        withScp(scp -> {
            scp.upload(localFile, remoteFile);
            return null;
        });
    }

    @Override
    public void uploadFile(InputStream localInStream, FileMetadata metadata, String remoteFile) throws AgentException {
        withScp(scp -> {
            scp.upload(toLocalSourceFile(localInStream, metadata), remoteFile);
            return null;
        });
    }

    @Override
    public void downloadFile(String remoteFile, String localFile) throws AgentException {
        withScp(scp -> {
            scp.download(remoteFile, localFile);
            return null;
        });
    }

    @Override
    public void downloadFile(String remoteFile, OutputStream localOutStream, FileMetadata metadata)
            throws AgentException {
        withScp(scp -> {
            scp.download(remoteFile, toLocalDestFile(localOutStream, metadata));
            return null;
        });
    }

    @Override
    public Boolean doesFileExist(String filePath) throws AgentException {
        return withSftp(sftp -> sftp.statExistence(filePath) != null);
    }

    @Override
    public List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException {
        var commandOutput = executeCommand("ls " + fileName, parentPath);
        var files = new ArrayList<String>();
        for (var f : commandOutput.getStdOut().split("\n")) {
            if (!f.isEmpty()) {
                files.add(f);
            }
        }
        return files;
    }

    @Override
    public FileMetadata getFileMetadata(String remoteFile) throws AgentException {
        return withSftp(sftp -> {
            var stat = sftp.stat(remoteFile);
            return new FileMetadata(
                    new File(remoteFile).getName(),
                    stat.getSize(),
                    FilePermission.toMask(stat.getPermissions()),
                    stat.getType() == Type.DIRECTORY);
        });
    }

    // -------------------------------------------------------------------------
    // Storage probing
    // -------------------------------------------------------------------------

    @Override
    public StorageVolumeInfo getStorageVolumeInfo(String location) throws AgentException {
        try {
            var targetLocation = resolveLocation(location);

            var escapedLocation = targetLocation.replace("'", "'\"'\"'");
            var dfCommand = "df -P -T -h '" + escapedLocation + "'";
            var dfBytesCommand = "df -P -T '" + escapedLocation + "'";

            var dfHumanOutput = executeCommand(dfCommand, null);
            var dfBytesOutput = executeCommand(dfBytesCommand, null);

            if (dfHumanOutput.getExitCode() != 0) {
                throw new AgentException("Failed to execute df command for location " + targetLocation + ": "
                        + dfHumanOutput.getStdError());
            }

            if (dfBytesOutput.getExitCode() != 0) {
                throw new AgentException("Failed to execute df command for location " + targetLocation + ": "
                        + dfBytesOutput.getStdError());
            }

            return parseDfOutput(dfHumanOutput.getStdOut(), dfBytesOutput.getStdOut(), targetLocation);

        } catch (AgentException e) {
            throw e;
        } catch (Exception e) {
            throw new AgentException("Error while retrieving storage volume info for location " + location, e);
        }
    }

    @Override
    public StorageDirectoryInfo getStorageDirectoryInfo(String location) throws AgentException {
        try {
            var targetLocation = resolveLocation(location);

            var escapedLocation = targetLocation.replace("'", "'\"'\"'");
            var duKBytesCommand = "du -sk '" + escapedLocation + "'";

            var duKBytesOutput = executeCommand(duKBytesCommand, null);

            if (duKBytesOutput.getExitCode() != 0) {
                throw new AgentException("Failed to execute du -sk command for location " + targetLocation + ": "
                        + duKBytesOutput.getStdError());
            }

            var outputKbStr = duKBytesOutput.getStdOut().trim();
            logger.info("OutputKbStr: for du -ku {} is {}", location, outputKbStr);
            var numberOfKBytesStr = outputKbStr.split(" ")[0];

            long numberOfKBytes = Long.parseLong(numberOfKBytesStr);

            var storageDirectoryInfo = new StorageDirectoryInfo();
            storageDirectoryInfo.setTotalSizeBytes(numberOfKBytes * 1024);
            storageDirectoryInfo.setTotalSize(numberOfKBytes + "kb");
            return storageDirectoryInfo;

        } catch (AgentException e) {
            throw e;
        } catch (Exception e) {
            throw new AgentException("Error while retrieving storage directory info for location " + location, e);
        }
    }

    private String resolveLocation(String location) throws AgentException {
        if (location != null && !location.trim().isEmpty()) {
            return location;
        }
        var homeOutput = executeCommand("echo $HOME", null);
        if (homeOutput.getExitCode() != 0
                || homeOutput.getStdOut() == null
                || homeOutput.getStdOut().trim().isEmpty()) {
            throw new AgentException("Failed to determine user's home directory: " + homeOutput.getStdError());
        }
        return homeOutput.getStdOut().trim();
    }

    private StorageVolumeInfo parseDfOutput(String dfHumanOutput, String dfBytesOutput, String targetLocation)
            throws AgentException {
        try {
            var humanLines = dfHumanOutput.split("\n");
            var bytesLines = dfBytesOutput.split("\n");

            if (humanLines.length < 2 || bytesLines.length < 2) {
                throw new AgentException(
                        "Unexpected df output format while parsing storage volume info for location " + targetLocation);
            }

            var humanDataLine = humanLines[1].trim();
            var bytesDataLine = bytesLines[1].trim();

            var humanFields = humanDataLine.split("\\s+");
            var bytesFields = bytesDataLine.split("\\s+");

            if (humanFields.length < 7 || bytesFields.length < 7) {
                throw new AgentException(
                        "Unexpected df output format - insufficient fields while parsing storage volume info for location "
                                + targetLocation);
            }

            var filesystemType = humanFields[1];
            var totalSizeHuman = humanFields[2];
            var usedSizeHuman = humanFields[3];
            var availableSizeHuman = humanFields[4];
            var capacityStr = humanFields[5].replace("%", "");

            var mountPointBuilder = new StringBuilder();
            for (int i = 6; i < humanFields.length; i++) {
                if (i > 6) {
                    mountPointBuilder.append(" ");
                }
                mountPointBuilder.append(humanFields[i]);
            }
            var mountPoint = mountPointBuilder.toString();

            var totalSizeBlocks = Long.parseLong(bytesFields[2]);
            var usedSizeBlocks = Long.parseLong(bytesFields[3]);
            var availableSizeBlocks = Long.parseLong(bytesFields[4]);

            long totalSizeBytes = totalSizeBlocks * 1024L;
            long usedSizeBytes = usedSizeBlocks * 1024L;
            long availableSizeBytes = availableSizeBlocks * 1024L;

            double percentageUsed = Double.parseDouble(capacityStr);

            var volumeInfo = new StorageVolumeInfo();
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

        } catch (AgentException e) {
            throw e;
        } catch (Exception e) {
            throw new AgentException(
                    "Error parsing df output: " + e.getMessage() + " for location " + targetLocation, e);
        }
    }

    // -------------------------------------------------------------------------
    // Resource management helpers
    // -------------------------------------------------------------------------

    @FunctionalInterface
    private interface SftpAction<T> {
        T execute(SFTPClient sftp) throws Exception;
    }

    @FunctionalInterface
    private interface ScpAction<T> {
        T execute(SCPFileTransfer scp) throws Exception;
    }

    @FunctionalInterface
    private interface SessionAction<T> {
        T execute(SessionResource session) throws Exception;
    }

    private <T> T withSftp(SftpAction<T> action) throws AgentException {
        SFTPClientResource resource = null;
        try {
            resource = sshjClient.newSFTPClientResource();
            return action.execute(resource.getSFTPClient());
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(resource).ifPresent(SFTPClientResource::markErrored);
            }
            throw new AgentException(e);
        } finally {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private <T> T withScp(ScpAction<T> action) throws AgentException {
        SCPFileTransferResource resource = null;
        try {
            resource = sshjClient.newSCPFileTransferResource();
            return action.execute(resource.getFileTransfer());
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(resource).ifPresent(SCPFileTransferResource::markErrored);
            }
            throw new AgentException(e);
        } finally {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    private <T> T withSession(SessionAction<T> action) throws AgentException {
        SessionResource resource = null;
        try {
            resource = sshjClient.startSessionResource();
            return action.execute(resource);
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(resource).ifPresent(SessionResource::markErrored);
            }
            throw new AgentException(e);
        } finally {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // SSH client setup
    // -------------------------------------------------------------------------

    private void createPoolingSSHJClient(
            String user, String host, int port, String publicKey, String privateKey, String passphrase)
            throws IOException {
        var defaultConfig = new DefaultConfig();
        defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);

        // SSHJ 0.40+ removed ssh-rsa from default key algorithms.
        // Re-add RSA algorithms so RSA keys from the credential store work.
        var keyAlgos = new ArrayList<>(defaultConfig.getKeyAlgorithms());
        keyAlgos.add(KeyAlgorithms.RSASHA256());
        keyAlgos.add(KeyAlgorithms.RSASHA512());
        keyAlgos.add(KeyAlgorithms.SSHRSA());
        defaultConfig.setKeyAlgorithms(keyAlgos);

        sshjClient = new PoolingSSHJClient(defaultConfig, host, port == 0 ? 22 : port);
        sshjClient.addHostKeyVerifier(new PromiscuousVerifier());
        sshjClient.setMaxSessionsForConnection(1);

        var passwordFinder = passphrase != null ? PasswordUtils.createOneOff(passphrase.toCharArray()) : null;
        var keyProvider = sshjClient.loadKeys(privateKey, publicKey, passwordFinder);

        sshjClient.auth(
                user,
                List.of(
                        new AuthPublickey(keyProvider),
                        new AuthKeyboardInteractive(new NoOpChallengeResponseProvider())));
    }

    // -------------------------------------------------------------------------
    // SCP stream adapters
    // -------------------------------------------------------------------------

    private static class NoOpChallengeResponseProvider implements ChallengeResponseProvider {
        @Override
        public List<String> getSubmethods() {
            return List.of();
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
    }

    private static LocalSourceFile toLocalSourceFile(InputStream inputStream, FileMetadata metadata) {
        return new LocalSourceFile() {
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
                return inputStream;
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

            @Override
            public Iterable<? extends LocalSourceFile> getChildren(LocalFileFilter filter) {
                return null;
            }

            @Override
            public boolean providesAtimeMtime() {
                return false;
            }

            @Override
            public long getLastAccessTime() {
                return 0;
            }

            @Override
            public long getLastModifiedTime() {
                return 0;
            }
        };
    }

    private static LocalDestFile toLocalDestFile(OutputStream outputStream, FileMetadata metadata) {
        return new LocalDestFile() {
            @Override
            public long getLength() {
                return metadata.getSize();
            }

            @Override
            public OutputStream getOutputStream() {
                return outputStream;
            }

            @Override
            public OutputStream getOutputStream(boolean append) {
                return outputStream;
            }

            @Override
            public LocalDestFile getChild(String name) {
                return null;
            }

            @Override
            public LocalDestFile getTargetFile(String filename) {
                return this;
            }

            @Override
            public LocalDestFile getTargetDirectory(String dirname) {
                return null;
            }

            @Override
            public void setPermissions(int perms) {}

            @Override
            public void setLastAccessedTime(long t) {}

            @Override
            public void setLastModifiedTime(long t) {}
        };
    }

    private static String readStream(InputStream is) throws IOException {
        var writer = new StringWriter();
        try (var reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        }
        return writer.toString();
    }
}
