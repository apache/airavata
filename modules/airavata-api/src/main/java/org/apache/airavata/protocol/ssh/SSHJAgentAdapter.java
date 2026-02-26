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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.sftp.FileMode.Type;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import net.schmizz.sshj.userauth.password.Resource;
import net.schmizz.sshj.xfer.FilePermission;
import net.schmizz.sshj.xfer.LocalDestFile;
import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;
import org.apache.airavata.iam.service.CredentialStoreService;
import org.apache.airavata.protocol.AgentAdapter;
import org.apache.airavata.protocol.AgentException;
import org.apache.airavata.protocol.CommandOutput;
import org.apache.airavata.protocol.FileMetadata;
import org.apache.airavata.protocol.ResourceLookup;
import org.apache.airavata.protocol.ssh.PoolingSSHJClient.SCPFileTransferResource;
import org.apache.airavata.protocol.ssh.PoolingSSHJClient.SFTPClientResource;
import org.apache.airavata.protocol.ssh.PoolingSSHJClient.SessionResource;
import org.apache.airavata.storage.resource.model.StorageDirectoryInfo;
import org.apache.airavata.storage.resource.model.StorageVolumeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"!test", "orchestrator-integration"})
public class SSHJAgentAdapter implements AgentAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SSHJAgentAdapter.class);

    protected final ResourceLookup resourceLookup;
    protected final CredentialStoreService credentialService;

    public SSHJAgentAdapter(ResourceLookup resourceLookup, CredentialStoreService credentialService) {
        this.resourceLookup = resourceLookup;
        this.credentialService = credentialService;
    }

    private PoolingSSHJClient sshjClient;

    protected void createPoolingSSHJClient(
            String user, String host, int port, String publicKey, String privateKey, String passphrase)
            throws IOException {
        var defaultConfig = new DefaultConfig();
        defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);

        // SSHJ 0.40+ removed ssh-rsa from default key algorithms.
        // Re-add RSA algorithms so RSA keys from the credential store work.
        var keyAlgos = new java.util.ArrayList<>(defaultConfig.getKeyAlgorithms());
        keyAlgos.add(KeyAlgorithms.RSASHA256());
        keyAlgos.add(KeyAlgorithms.RSASHA512());
        keyAlgos.add(KeyAlgorithms.SSHRSA());
        defaultConfig.setKeyAlgorithms(keyAlgos);

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

        var passwordFinder = passphrase != null ? PasswordUtils.createOneOff(passphrase.toCharArray()) : null;

        var keyProvider = sshjClient.loadKeys(privateKey, publicKey, passwordFinder);

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
            logger.info("Initializing Compute Resource SSH Adapter for compute resource : " + computeResource
                    + ", gateway : " + gatewayId + ", user " + userId + ", token : " + token);

            var resource = resourceLookup.getResource(computeResource);
            if (resource == null) {
                throw new AgentException("No resource found for id " + computeResource);
            }

            logger.info("Fetching credentials for cred store token " + token);

            var sshCredential = credentialService.getSSHCredential(token, gatewayId);

            if (sshCredential == null) {
                throw new AgentException("Null credential for token " + token);
            }
            logger.info("Description for token : " + token + " : " + sshCredential.getDescription());

            var selectedHostName = resource.getHostName();
            int selectedPort = resource.getPort() == 0 ? 22 : resource.getPort();

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
        SessionResource sessionResource = null;
        try {
            sessionResource = sshjClient.startSessionResource();
            var exec = sessionResource
                    .getSession()
                    .exec((workingDirectory != null ? "cd " + workingDirectory + "; " : "") + command);
            var standardOutReader = new StandardOutReader();

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
                Optional.ofNullable(sessionResource).ifPresent(SessionResource::markErrored);
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sessionResource).ifPresent(ss -> {
                try {
                    ss.close();
                } catch (Exception e) {
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
        SFTPClientResource sftpClientResource = null;
        try {
            sftpClientResource = sshjClient.newSFTPClientResource();
            if (recursive) {
                sftpClientResource.getSFTPClient().mkdirs(path);
            } else {
                sftpClientResource.getSFTPClient().mkdir(path);
            }
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(sftpClientResource).ifPresent(SFTPClientResource::markErrored);
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sftpClientResource).ifPresent(client -> {
                try {
                    client.close();
                } catch (Exception e) {
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
        SFTPClientResource sftpClientResource = null;
        try {
            sftpClientResource = sshjClient.newSFTPClientResource();
            sftpClientResource.getSFTPClient().rmdir(path);
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(sftpClientResource).ifPresent(SFTPClientResource::markErrored);
            }
            logger.error("Error while deleting directory {}", path, e);
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sftpClientResource).ifPresent(client -> {
                try {
                    client.close();
                } catch (Exception e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void uploadFile(String localFile, String remoteFile) throws AgentException {
        SCPFileTransferResource fileTransferResource = null;
        try {
            fileTransferResource = sshjClient.newSCPFileTransferResource();
            fileTransferResource.getFileTransfer().upload(localFile, remoteFile);

        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(fileTransferResource).ifPresent(SCPFileTransferResource::markErrored);
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(fileTransferResource).ifPresent(resource -> {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void uploadFile(InputStream localInStream, FileMetadata metadata, String remoteFile) throws AgentException {
        SCPFileTransferResource fileTransferResource = null;

        try {
            fileTransferResource = sshjClient.newSCPFileTransferResource();
            fileTransferResource
                    .getFileTransfer()
                    .upload(
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
                Optional.ofNullable(fileTransferResource).ifPresent(SCPFileTransferResource::markErrored);
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(fileTransferResource).ifPresent(resource -> {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void downloadFile(String remoteFile, String localFile) throws AgentException {
        SCPFileTransferResource fileTransferResource = null;
        try {
            fileTransferResource = sshjClient.newSCPFileTransferResource();
            fileTransferResource.getFileTransfer().download(remoteFile, localFile);

        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(fileTransferResource).ifPresent(SCPFileTransferResource::markErrored);
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(fileTransferResource).ifPresent(resource -> {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public void downloadFile(String remoteFile, OutputStream localOutStream, FileMetadata metadata)
            throws AgentException {
        SCPFileTransferResource fileTransferResource = null;
        try {
            fileTransferResource = sshjClient.newSCPFileTransferResource();
            fileTransferResource.getFileTransfer().download(remoteFile, new LocalDestFile() {
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
                Optional.ofNullable(fileTransferResource).ifPresent(SCPFileTransferResource::markErrored);
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(fileTransferResource).ifPresent(resource -> {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public List<String> listDirectory(String path) throws AgentException {
        SFTPClientResource sftpClientResource = null;
        try {
            sftpClientResource = sshjClient.newSFTPClientResource();
            var ls = sftpClientResource.getSFTPClient().ls(path);
            return ls.stream().map(RemoteResourceInfo::getName).collect(Collectors.toList());
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(sftpClientResource).ifPresent(SFTPClientResource::markErrored);
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sftpClientResource).ifPresent(client -> {
                try {
                    client.close();
                } catch (Exception e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public Boolean doesFileExist(String filePath) throws AgentException {
        SFTPClientResource sftpClientResource = null;
        try {
            sftpClientResource = sshjClient.newSFTPClientResource();
            return sftpClientResource.getSFTPClient().statExistence(filePath) != null;
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(sftpClientResource).ifPresent(SFTPClientResource::markErrored);
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sftpClientResource).ifPresent(client -> {
                try {
                    client.close();
                } catch (Exception e) {
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

        var commandOutput = executeCommand("ls " + fileName, parentPath); // This has a risk of returning folders also
        var filesTmp = commandOutput.getStdOut().split("\n");
        var files = new ArrayList<String>();
        for (var f : filesTmp) {
            if (!f.isEmpty()) {
                files.add(f);
            }
        }
        return files;
    }

    @Override
    public FileMetadata getFileMetadata(String remoteFile) throws AgentException {
        SFTPClientResource sftpClientResource = null;
        try {
            sftpClientResource = sshjClient.newSFTPClientResource();
            var stat = sftpClientResource.getSFTPClient().stat(remoteFile);
            var metadata = new FileMetadata();
            metadata.setName(new File(remoteFile).getName());
            metadata.setSize(stat.getSize());
            metadata.setPermissions(FilePermission.toMask(stat.getPermissions()));
            metadata.setDirectory(stat.getType() == Type.DIRECTORY);
            return metadata;
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                Optional.ofNullable(sftpClientResource).ifPresent(SFTPClientResource::markErrored);
            }
            throw new AgentException(e);

        } finally {
            Optional.ofNullable(sftpClientResource).ifPresent(resource -> {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Ignore
                }
            });
        }
    }

    @Override
    public StorageVolumeInfo getStorageVolumeInfo(String location) throws AgentException {
        return new RemoteStorageProber(this).getStorageVolumeInfo(location);
    }

    @Override
    public StorageDirectoryInfo getStorageDirectoryInfo(String location) throws AgentException {
        return new RemoteStorageProber(this).getStorageDirectoryInfo(location);
    }
}
