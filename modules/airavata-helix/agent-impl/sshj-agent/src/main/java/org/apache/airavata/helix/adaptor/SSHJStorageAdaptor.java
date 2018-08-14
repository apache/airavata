/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.helix.adaptor;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;
import net.schmizz.sshj.sftp.SFTPClient;
import org.apache.airavata.agents.api.*;
import org.apache.airavata.model.appcatalog.storageresource.StorageResourceDescription;
import org.apache.airavata.model.credential.store.SSHCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SSHJStorageAdaptor extends SSHJAgentAdaptor implements StorageResourceAdaptor {

    private final static Logger logger = LoggerFactory.getLogger(SSHJAgentAdaptor.class);

    @Override
    public void init(String storageResourceId, String gatewayId, String loginUser, String token) throws AgentException {
        try {
            logger.info("Initializing Storage Resource Adaptor for storage resource : "+ storageResourceId + ", gateway : " +
                    gatewayId +", user " + loginUser + ", token : " + token);
            StorageResourceDescription storageResource = AgentUtils.getRegistryServiceClient().getStorageResource(storageResourceId);

            logger.info("Fetching credentials for cred store token " + token);

            SSHCredential sshCredential = AgentUtils.getCredentialClient().getSSHCredential(token, gatewayId);
            if (sshCredential == null) {
                throw new AgentException("Null credential for token " + token);
            }
            logger.info("Description for token : " + token + " : " + sshCredential.getDescription());

            createPoolingSSHJClient(loginUser, storageResource.getHostName(), sshCredential.getPublicKey(),
                    sshCredential.getPrivateKey(), sshCredential.getPassphrase());

        } catch (Exception e) {
            logger.error("Error while initializing ssh agent for storage resource " + storageResourceId + " to token " + token, e);
            throw new AgentException("Error while initializing ssh agent for storage resource " + storageResourceId + " to token " + token, e);
        }
    }

    @Override
    public void uploadFile(String sourceFile, String destFile) throws AgentException {
        super.copyFileTo(sourceFile, destFile);
    }

    @Override
    public void downloadFile(String sourceFile, String destFile) throws AgentException {
        super.copyFileFrom(sourceFile, destFile);
    }

    @Override
    public void deleteFile(String path) throws AgentException {
        try(SFTPClient sftpClient = sshjClient.newSFTPClientWrapper()) {
            sftpClient.rm(path);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @Override
    public void deleteDirectory(String path) throws AgentException {
        try(SFTPClient sftpClient = sshjClient.newSFTPClientWrapper()) {
            sftpClient.rmdir(path);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @Override
    public CommandOutput executeCommand(String command, String workingDirectory) throws AgentException {
        return super.executeCommand(command, workingDirectory);
    }

    @Override
    public FileInfo getFileInfo(String path) throws AgentException {
        try(SFTPClient sftpClient = sshjClient.newSFTPClientWrapper()) {
            FileAttributes existence = sftpClient.statExistence(path);
            FileInfo fileInfo = new FileInfo();
            if (existence == null) {
                fileInfo.setExist(false);
            } else {
                fileInfo.setExist(true);
                fileInfo.setCreatedDate(existence.getAtime());
                fileInfo.setModifiedDate(existence.getMtime());
                fileInfo.setFile(existence.getType() != FileMode.Type.DIRECTORY);
                fileInfo.setSize(existence.getSize());
            }
            return fileInfo;
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }

    @Override
    public void moveFile(String oldPath, String newPath) throws AgentException {
        try(SFTPClient sftpClient = sshjClient.newSFTPClientWrapper()) {
            sftpClient.rename(oldPath, newPath);
        } catch (Exception e) {
            throw new AgentException(e);
        }
    }
}
