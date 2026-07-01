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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.apache.airavata.interfaces.AgentException;
import org.apache.airavata.interfaces.CommandOutput;
import org.apache.airavata.interfaces.FileMetadata;
import org.apache.airavata.interfaces.StorageResourceAdaptor;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple SFTP-based storage adaptor using SSHJ directly.
 * No connection pooling, no abstract hierarchy — just SSH + SFTP.
 */
public class SSHJStorageAdaptor implements StorageResourceAdaptor {

    private static final Logger log = LoggerFactory.getLogger(SSHJStorageAdaptor.class);

    private String host;
    private int port;
    private String username;
    private String privateKey;
    private String passphrase;

    @Override
    public void init(String storageResourceId, String gatewayId, String loginUser, String token) throws AgentException {
        try {
            log.info(
                    "Initializing SFTP adaptor: resource={}, gateway={}, user={}",
                    storageResourceId,
                    gatewayId,
                    loginUser);

            StorageResourceDescription sr =
                    AgentUtils.getRegistryServiceClient().getStorageResource(storageResourceId);

            SSHCredential cred = AgentUtils.getCredentialClient().getSSHCredential(token, gatewayId);
            if (cred == null) throw new AgentException("No credential for token " + token);

            this.host = sr.getHostName();
            this.port = sr.getSftpPort() == 0 ? 22 : sr.getSftpPort();
            this.username = loginUser;
            this.privateKey = cred.getPrivateKey();
            this.passphrase = cred.getPassphrase();

            log.info("SFTP adaptor ready: {}@{}:{}", username, host, port);

        } catch (Exception e) {
            log.error("Failed to init SFTP adaptor for " + storageResourceId, e);
            throw new AgentException("Failed to init SFTP adaptor for " + storageResourceId, e);
        }
    }

    /**
     * SFTPClient that owns its underlying SSHClient and disconnects it on close.
     * Callers use {@code try (SFTPClient sftp = openSftp())}; closing the SFTP channel
     * alone would leak the SSHClient's socket and transport reader thread, so close()
     * tears down both.
     */
    private static final class OwnedSFTPClient extends SFTPClient {
        private final SSHClient ssh;

        OwnedSFTPClient(SSHClient ssh) throws Exception {
            super(ssh);
            this.ssh = ssh;
        }

        @Override
        public void close() throws java.io.IOException {
            try {
                super.close();
            } finally {
                ssh.disconnect();
            }
        }
    }

    private SFTPClient openSftp() throws Exception {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(host, port);

        try {
            // Write PEM key to temp file since SSHJ loadKeys expects a file path
            java.io.File keyFile = java.io.File.createTempFile("airavata-ssh-", ".pem");
            try {
                java.nio.file.Files.writeString(keyFile.toPath(), privateKey);
                ssh.authPublickey(username, ssh.loadKeys(keyFile.getAbsolutePath()));
            } finally {
                keyFile.delete();
            }
            // The returned client owns ssh and closes it when the caller closes the SFTPClient.
            return new OwnedSFTPClient(ssh);
        } catch (Exception e) {
            // Auth/SFTP-channel setup failed after connect; close ssh so we don't leak it.
            ssh.disconnect();
            throw e;
        }
    }

    @Override
    public List<String> listDirectory(String path) throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            List<RemoteResourceInfo> entries = sftp.ls(path);
            List<String> names = new ArrayList<>();
            for (RemoteResourceInfo entry : entries) {
                names.add(entry.getName());
            }
            return names;
        } catch (Exception e) {
            log.error("Failed to list directory: " + path, e);
            throw new AgentException("Failed to list directory: " + path, e);
        }
    }

    @Override
    public Boolean doesFileExist(String filePath) throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            sftp.stat(filePath);
            return true;
        } catch (net.schmizz.sshj.sftp.SFTPException e) {
            if (e.getMessage().contains("No such file")) return false;
            log.error("SFTP error checking file: " + filePath, e);
            throw new AgentException("Failed to check file existence: " + filePath, e);
        } catch (Exception e) {
            log.error("Failed to check file existence: " + filePath, e);
            throw new AgentException("Failed to check file existence: " + filePath, e);
        }
    }

    @Override
    public FileMetadata getFileMetadata(String remoteFile) throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            FileAttributes attrs = sftp.stat(remoteFile);
            FileMetadata meta = new FileMetadata();
            String name = remoteFile.substring(remoteFile.lastIndexOf('/') + 1);
            meta.setName(name);
            meta.setSize(attrs.getSize());
            meta.setDirectory(attrs.getType() == FileMode.Type.DIRECTORY);
            // SFTP mtime is seconds since the epoch; expose it as epoch millis.
            meta.setModifiedTime(attrs.getMtime() * 1000L);
            meta.setContentType(java.net.URLConnection.guessContentTypeFromName(name));
            return meta;
        } catch (Exception e) {
            throw new AgentException("Failed to get file metadata: " + remoteFile, e);
        }
    }

    @Override
    public org.apache.airavata.model.appcatalog.storageresource.proto.StorageDirectoryInfo getStorageDirectoryInfo(
            String location) throws AgentException {
        return org.apache.airavata.model.appcatalog.storageresource.proto.StorageDirectoryInfo.getDefaultInstance();
    }

    @Override
    public void createDirectory(String path) throws AgentException {
        createDirectory(path, false);
    }

    @Override
    public void createDirectory(String path, boolean recursive) throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            if (recursive) {
                sftp.mkdirs(path);
            } else {
                sftp.mkdir(path);
            }
        } catch (Exception e) {
            throw new AgentException("Failed to create directory: " + path, e);
        }
    }

    @Override
    public void deleteDirectory(String path) throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            sftp.rmdir(path);
        } catch (Exception e) {
            throw new AgentException("Failed to delete directory: " + path, e);
        }
    }

    @Override
    public void deleteFile(String path) throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            sftp.rm(path);
        } catch (Exception e) {
            throw new AgentException("Failed to delete file: " + path, e);
        }
    }

    @Override
    public void moveFile(String sourcePath, String destinationPath) throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            sftp.rename(sourcePath, destinationPath);
        } catch (Exception e) {
            throw new AgentException("Failed to move file: " + sourcePath + " -> " + destinationPath, e);
        }
    }

    @Override
    public void copyFile(String sourcePath, String destinationPath) throws AgentException {
        // SFTP has no server-side copy primitive, so stage the source through a local temp
        // file and re-upload to the destination (same get/put mechanism used by download/upload).
        // Unlike moveFile, the source is left in place.
        try (SFTPClient sftp = openSftp()) {
            java.io.File tempFile = java.io.File.createTempFile("airavata-copy-", ".tmp");
            try {
                sftp.get(sourcePath, tempFile.getAbsolutePath());
                sftp.put(tempFile.getAbsolutePath(), destinationPath);
            } finally {
                tempFile.delete();
            }
        } catch (Exception e) {
            throw new AgentException("Failed to copy file: " + sourcePath + " -> " + destinationPath, e);
        }
    }

    @Override
    public void createSymlink(String targetPath, String linkPath) throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            sftp.symlink(linkPath, targetPath);
        } catch (Exception e) {
            throw new AgentException("Failed to create symlink: " + linkPath + " -> " + targetPath, e);
        }
    }

    @Override
    public void uploadFile(String localFile, String remoteFile) throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            sftp.put(localFile, remoteFile);
        } catch (Exception e) {
            throw new AgentException("Failed to upload file: " + localFile + " -> " + remoteFile, e);
        }
    }

    @Override
    public void uploadFile(InputStream localInStream, FileMetadata metadata, String remoteFile) throws AgentException {
        // Write stream to temp file then upload
        try (SFTPClient sftp = openSftp()) {
            java.io.File tempFile = java.io.File.createTempFile("airavata-upload-", ".tmp");
            try {
                java.nio.file.Files.copy(
                        localInStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                sftp.put(tempFile.getAbsolutePath(), remoteFile);
            } finally {
                tempFile.delete();
            }
        } catch (Exception e) {
            throw new AgentException("Failed to upload stream to: " + remoteFile, e);
        }
    }

    @Override
    public void downloadFile(String remoteFile, String localFile) throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            sftp.get(remoteFile, localFile);
        } catch (Exception e) {
            throw new AgentException("Failed to download file: " + remoteFile, e);
        }
    }

    @Override
    public void downloadFile(String remoteFile, OutputStream localOutStream, FileMetadata metadata)
            throws AgentException {
        try (SFTPClient sftp = openSftp()) {
            java.io.File tempFile = java.io.File.createTempFile("airavata-download-", ".tmp");
            try {
                sftp.get(remoteFile, tempFile.getAbsolutePath());
                java.nio.file.Files.copy(tempFile.toPath(), localOutStream);
            } finally {
                tempFile.delete();
            }
        } catch (Exception e) {
            throw new AgentException("Failed to download file: " + remoteFile, e);
        }
    }

    @Override
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        throw new AgentException("Command execution not supported on storage resources");
    }

    @Override
    public List<String> getFileNameFromExtension(String fileName, String parentPath) throws AgentException {
        List<String> result = new ArrayList<>();
        try (SFTPClient sftp = openSftp()) {
            for (RemoteResourceInfo entry : sftp.ls(parentPath)) {
                if (entry.getName().endsWith(fileName)) {
                    result.add(entry.getName());
                }
            }
        } catch (Exception e) {
            throw new AgentException("Failed to search files by extension: " + fileName, e);
        }
        return result;
    }

    @Override
    public org.apache.airavata.model.appcatalog.storageresource.proto.StorageVolumeInfo getStorageVolumeInfo(
            String location) throws AgentException {
        return org.apache.airavata.model.appcatalog.storageresource.proto.StorageVolumeInfo.getDefaultInstance();
    }

    @Override
    public void destroy() {
        // Each operation opens and closes its own SSH+SFTP connection (see openSftp /
        // OwnedSFTPClient), so there is no pooled or long-lived connection to tear down here.
    }
}
