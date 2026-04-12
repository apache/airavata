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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

import org.apache.airavata.interfaces.AgentException;
import org.apache.airavata.interfaces.CommandOutput;
import org.apache.airavata.interfaces.FileMetadata;
import org.apache.airavata.interfaces.StorageResourceAdaptor;
import org.apache.airavata.model.appcatalog.storageresource.proto.StorageResourceDescription;
import org.apache.airavata.model.credential.store.proto.SSHCredential;
import org.apache.airavata.model.data.movement.proto.DataMovementInterface;
import org.apache.airavata.model.data.movement.proto.DataMovementProtocol;
import org.apache.airavata.model.data.movement.proto.SCPDataMovement;
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
            log.info("Initializing SFTP adaptor: resource={}, gateway={}, user={}", storageResourceId, gatewayId, loginUser);

            StorageResourceDescription sr = AgentUtils.getRegistryServiceClient().getStorageResource(storageResourceId);

            Optional<DataMovementInterface> dmOp = sr.getDataMovementInterfacesList().stream()
                    .filter(iface -> iface.getDataMovementProtocol() == DataMovementProtocol.SCP)
                    .findFirst();

            DataMovementInterface dm = dmOp.orElseThrow(() ->
                    new AgentException("No SCP data movement interface for storage resource " + storageResourceId));

            SCPDataMovement scp = AgentUtils.getRegistryServiceClient().getSCPDataMovement(dm.getDataMovementInterfaceId());

            SSHCredential cred = AgentUtils.getCredentialClient().getSSHCredential(token, gatewayId);
            if (cred == null) throw new AgentException("No credential for token " + token);

            String altHost = scp.getAlternativeScpHostName();
            this.host = (altHost != null && !altHost.isEmpty()) ? altHost : sr.getHostName();
            this.port = scp.getSshPort() == 0 ? 22 : scp.getSshPort();
            this.username = loginUser;
            this.privateKey = cred.getPrivateKey();
            this.passphrase = cred.getPassphrase();

            log.info("SFTP adaptor ready: {}@{}:{}", username, host, port);

        } catch (Exception e) {
            log.error("Failed to init SFTP adaptor for " + storageResourceId, e);
            throw new AgentException("Failed to init SFTP adaptor for " + storageResourceId, e);
        }
    }

    private SFTPClient openSftp() throws Exception {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(host, port);

        // Write PEM key to temp file since SSHJ loadKeys expects a file path
        java.io.File keyFile = java.io.File.createTempFile("airavata-ssh-", ".pem");
        try {
            java.nio.file.Files.writeString(keyFile.toPath(), privateKey);
            ssh.authPublickey(username, ssh.loadKeys(keyFile.getAbsolutePath()));
        } finally {
            keyFile.delete();
        }

        return ssh.newSFTPClient();
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
            meta.setName(remoteFile.substring(remoteFile.lastIndexOf('/') + 1));
            meta.setSize(attrs.getSize());
            meta.setDirectory(attrs.getType() == FileMode.Type.DIRECTORY);
            return meta;
        } catch (Exception e) {
            throw new AgentException("Failed to get file metadata: " + remoteFile, e);
        }
    }

    @Override
    public org.apache.airavata.model.appcatalog.storageresource.proto.StorageDirectoryInfo getStorageDirectoryInfo(String location) throws AgentException {
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
                java.nio.file.Files.copy(localInStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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
    public void downloadFile(String remoteFile, OutputStream localOutStream, FileMetadata metadata) throws AgentException {
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
    public org.apache.airavata.model.appcatalog.storageresource.proto.StorageVolumeInfo getStorageVolumeInfo(String location) throws AgentException {
        return org.apache.airavata.model.appcatalog.storageresource.proto.StorageVolumeInfo.getDefaultInstance();
    }

    @Override
    public void destroy() {
        // No persistent connections to clean up
    }
}
