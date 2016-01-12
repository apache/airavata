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
 *
*/
package org.apache.airavata.data.manager.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.data.manager.core.db.dao.FileTransferRequestDao;
import org.apache.airavata.data.manager.core.remote.client.RemoteStorageClient;
import org.apache.airavata.data.manager.core.remote.client.http.HTTPStorageClient;
import org.apache.airavata.data.manager.core.remote.client.scp.SCPStorageClient;
import org.apache.airavata.data.manager.core.remote.client.sftp.SFTPStorageClient;
import org.apache.airavata.data.manager.cpi.DataManagerException;
import org.apache.airavata.data.manager.cpi.FileTransferService;
import org.apache.airavata.model.data.transfer.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

public class FileTransferServiceImpl implements FileTransferService {

    private final static Logger logger = LoggerFactory.getLogger(FileTransferServiceImpl.class);

    private FileTransferRequestDao fileTransferRequestDao;

    public FileTransferServiceImpl() throws IOException {
        this.fileTransferRequestDao = new FileTransferRequestDao();
    }

    /**
     * Method to upload the give bytes to the destination storage system
     *
     * @param gatewayId
     * @param username
     * @param fileData
     * @param destHostName
     * @param destLoginName
     * @param destPort
     * @param destProtocol
     * @param destinationPath
     * @param destHostCredToken
     * @return
     * @throws DataManagerException
     */
    @Override
    public String uploadFile(String gatewayId, String username, byte[] fileData, String destHostName, String destLoginName, int destPort,
                             StorageResourceProtocol destProtocol,
                             String destinationPath, String destHostCredToken) throws DataManagerException {
        long transferTime = System.currentTimeMillis();
        if(destProtocol == StorageResourceProtocol.SCP || destProtocol == StorageResourceProtocol.SFTP) {
            Object credential = getCredential(destHostCredToken);
            SSHCredential sshCredential;
            if (credential instanceof SSHCredential) {
                sshCredential = (SSHCredential) credential;
                File srcFile = null;
                FileWriter fileWriter = null;
                FileTransferRequestModel fileTransferRequestModel = null;
                try {
                    String srcFilePath = System.getProperty("java.io.tmpdir")+File.separator+ UUID.randomUUID().toString();
                    srcFile = new File(srcFilePath);
                    fileWriter = new FileWriter(srcFile);
                    fileWriter.write(new String(fileData));
                    fileWriter.close();
                    RemoteStorageClient remoteStorageClient;
                    if(destProtocol == StorageResourceProtocol.SCP)
                        remoteStorageClient = new SCPStorageClient(destHostName, destPort, destLoginName,
                            sshCredential.getPrivateKey(),
                            sshCredential.getPublicKey(), sshCredential.getPassphrase().getBytes());
                    else
                        remoteStorageClient = new SFTPStorageClient(destHostName, destPort, destLoginName,
                                sshCredential.getPrivateKey(),
                                sshCredential.getPublicKey(), sshCredential.getPassphrase().getBytes());

                    fileTransferRequestModel = new FileTransferRequestModel();
                    fileTransferRequestModel.setGatewayId(gatewayId);
                    fileTransferRequestModel.setUsername(username);
                    fileTransferRequestModel.setSrcHostname(InetAddress.getLocalHost().getHostName());
                    fileTransferRequestModel.setSrcProtocol(StorageResourceProtocol.LOCAL);
                    fileTransferRequestModel.setDestHostname(destHostName);
                    fileTransferRequestModel.setDestLoginName(destLoginName);
                    fileTransferRequestModel.setDestPort(destPort);
                    fileTransferRequestModel.setDestProtocol(destProtocol);
                    fileTransferRequestModel.setDestFilePath(destinationPath);
                    fileTransferRequestModel.setDestHostCredToken(destHostCredToken);
                    fileTransferRequestModel.setFileTransferMode(FileTransferMode.SYNC);
                    remoteStorageClient.writeFile(srcFile, destinationPath);
                    transferTime = System.currentTimeMillis() - transferTime;
                    fileTransferRequestModel.setTransferTime(transferTime);
                    fileTransferRequestModel.setTransferStatus(FileTransferStatus.COMPLETED);
                    fileTransferRequestModel.setCreatedTime(System.currentTimeMillis());
                    fileTransferRequestModel.setLastModifiedType(fileTransferRequestModel.getCreatedTime());
                    fileTransferRequestModel.setFileSize(srcFile.length());
                    String transferId = fileTransferRequestDao.createFileTransferRequest(fileTransferRequestModel);
                    return transferId;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    if(fileTransferRequestModel != null) {
                        fileTransferRequestModel.setTransferStatus(FileTransferStatus.FAILED);
                        try {
                            fileTransferRequestDao.createFileTransferRequest(fileTransferRequestModel);
                        } catch (JsonProcessingException e1) {
                            logger.error(e.getMessage(), e);
                            throw new DataManagerException(e);
                        }
                    }
                    throw new DataManagerException(e.getMessage());
                } finally {
                    if(srcFile != null)
                        srcFile.delete();
                    if(fileWriter != null)
                        try {
                            fileWriter.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                            throw new DataManagerException(e);
                        }
                }
            } else {
                throw new DataManagerException("Only SSHCredential type is supported");
            }
        }else{
            throw new DataManagerException(destProtocol.toString() + " protocol is not supported for this method");
        }
    }

    /**
     * Transfer file between two storage resources synchronously. Returns the file transfer request id
     *
     * @param gatewayId
     * @param username
     * @param srcHostname
     * @param srcLoginName
     * @param srcPort
     * @param srcProtocol
     * @param srcPath
     * @param srcHostCredToken
     * @param destHostname
     * @param destLoginName
     * @param destPort
     * @param destProtocol
     * @param destinationPath
     * @param destHostCredToken
     * @return
     * @throws DataManagerException
     */
    @Override
    public String transferFile(String gatewayId, String username, String srcHostname, String srcLoginName, int srcPort, StorageResourceProtocol srcProtocol,
                               String srcPath, String srcHostCredToken, String destHostname, String destLoginName, int destPort,
                               StorageResourceProtocol destProtocol, String destinationPath, String destHostCredToken)
            throws DataManagerException {
        long transferTime = System.currentTimeMillis();
        File srcFile = null;
        FileTransferRequestModel fileTransferRequestModel = null;
        try{
            fileTransferRequestModel = new FileTransferRequestModel();
            fileTransferRequestModel.setGatewayId(gatewayId);
            fileTransferRequestModel.setUsername(username);
            fileTransferRequestModel.setSrcHostname(srcHostname);
            fileTransferRequestModel.setSrcPort(srcPort);
            fileTransferRequestModel.setSrcLoginName(srcLoginName);
            fileTransferRequestModel.setSrcFilePath(srcPath);
            fileTransferRequestModel.setSrcProtocol(srcProtocol);
            fileTransferRequestModel.setSrcHostCredToken(srcHostCredToken);
            fileTransferRequestModel.setDestHostname(destHostname);
            fileTransferRequestModel.setDestPort(destPort);
            fileTransferRequestModel.setDestLoginName(destLoginName);
            fileTransferRequestModel.setDestFilePath(destinationPath);
            fileTransferRequestModel.setDestProtocol(destProtocol);
            fileTransferRequestModel.setDestHostCredToken(destHostCredToken);
            fileTransferRequestModel.setCreatedTime(System.currentTimeMillis());
            fileTransferRequestModel.setLastModifiedType(fileTransferRequestModel.getCreatedTime());
            fileTransferRequestModel.setFileTransferMode(FileTransferMode.SYNC);

            if(srcProtocol == StorageResourceProtocol.HTTP || srcProtocol == StorageResourceProtocol.HTTPS ||
                    srcProtocol == StorageResourceProtocol.SCP || srcProtocol == StorageResourceProtocol.SFTP){
                RemoteStorageClient srcClient = null;
                if(srcProtocol == StorageResourceProtocol.HTTP){
                    srcClient = new HTTPStorageClient(HTTPStorageClient.Protocol.HTTP, srcHostname, srcPort);
                }else if(srcProtocol == StorageResourceProtocol.HTTPS){
                    srcClient = new HTTPStorageClient(HTTPStorageClient.Protocol.HTTPS, srcHostname, srcPort);
                }else if(srcProtocol == StorageResourceProtocol.SCP){
                    Object credential = getCredential(srcHostCredToken);
                    if(credential instanceof SSHCredential){
                        SSHCredential sshCredential = (SSHCredential) credential;
                        srcClient = new SCPStorageClient(srcHostname, srcPort, srcLoginName, sshCredential.getPrivateKey(),
                                sshCredential.getPublicKey(), sshCredential.getPassphrase().getBytes());
                    }else{
                        throw new DataManagerException("Only support SSHCredentials for SCP host");
                    }
                }else{
                    Object credential = getCredential(srcHostCredToken);
                    if(credential instanceof SSHCredential){
                        SSHCredential sshCredential = (SSHCredential) credential;
                        srcClient = new SFTPStorageClient(srcHostname, srcPort, srcLoginName, sshCredential.getPrivateKey(),
                                sshCredential.getPublicKey(), sshCredential.getPassphrase().getBytes());
                    }else{
                        throw new DataManagerException("Only support SSHCredentials for SFTP host");
                    }
                }
                fileTransferRequestModel.setTransferStatus(FileTransferStatus.RUNNING);
                srcFile = srcClient.readFile(srcPath);
            }else{
                throw new DataManagerException("Unsupported src protocol " + srcProtocol);
            }

            if(destProtocol == StorageResourceProtocol.SCP || destProtocol == StorageResourceProtocol.SFTP){
                RemoteStorageClient destClient = null;
                if(destProtocol == StorageResourceProtocol.SCP){
                    Object credential = getCredential(srcHostCredToken);
                    if(credential instanceof SSHCredential){
                        SSHCredential sshCredential = (SSHCredential) credential;
                        destClient = new SCPStorageClient(srcHostname, srcPort, srcLoginName, sshCredential.getPrivateKey(),
                                sshCredential.getPublicKey(), sshCredential.getPassphrase().getBytes());
                    }else{
                        throw new DataManagerException("Only support SSHCredentials for SCP host");
                    }
                }else{
                    Object credential = getCredential(srcHostCredToken);
                    if(credential instanceof SSHCredential){
                        SSHCredential sshCredential = (SSHCredential) credential;
                        destClient = new SFTPStorageClient(srcHostname, srcPort, srcLoginName, sshCredential.getPrivateKey(),
                                sshCredential.getPublicKey(), sshCredential.getPassphrase().getBytes());
                    }else{
                        throw new DataManagerException("Only support SSHCredentials for SFTP host");
                    }
                }
                destClient.writeFile(srcFile, destinationPath);
                transferTime = System.currentTimeMillis() - transferTime;
                fileTransferRequestModel.setTransferTime(transferTime);
                fileTransferRequestModel.setFileSize(srcFile.length());
                fileTransferRequestModel.setTransferStatus(FileTransferStatus.COMPLETED);
                String transferId = fileTransferRequestDao.createFileTransferRequest(fileTransferRequestModel);
                return transferId;
            }else{
                throw new DataManagerException("Unsupported src protocol " + srcProtocol);
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            if(fileTransferRequestModel != null) {
                fileTransferRequestModel.setTransferStatus(FileTransferStatus.FAILED);
                try {
                    fileTransferRequestDao.createFileTransferRequest(fileTransferRequestModel);
                } catch (JsonProcessingException ex) {
                    logger.error(ex.getMessage(), ex);
                    throw new DataManagerException(ex);
                }
            }
            throw new DataManagerException(e);
        }finally {
            if(srcFile != null)
                srcFile.delete();
        }
    }

    /**
     * Transfer file between two storage resources asynchronously. Returns the file transfer request id
     *
     * @param gatewayId
     * @param username
     * @param srcHostname
     * @param srcLoginName
     * @param srcPort
     * @param srcProtocol
     * @param srcPath
     * @param srcHostCredToken
     * @param destHostname
     * @param destLoginName
     * @param destPort
     * @param destProtocol
     * @param destinationPath
     * @param destHostCredToken
     * @param callbackEmails
     * @return
     * @throws DataManagerException
     */
    @Override
    public String transferFileAsync(String gatewayId, String username, String srcHostname, String srcLoginName, int srcPort, StorageResourceProtocol srcProtocol,
                                    String srcPath, String srcHostCredToken, String destHostname, String destLoginName,
                                    int destPort, StorageResourceProtocol destProtocol, String destinationPath,
                                    String destHostCredToken, String[] callbackEmails) throws DataManagerException {
        return null;
    }

    /**
     * Get a directory listing of the specified source directory
     *
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param path
     * @param hostCredential
     * @return
     * @throws DataManagerException
     */
    @Override
    public List<LSEntryModel> getDirectoryListing(String hostname, String loginName, int port, StorageResourceProtocol protocol,
                                              String path, String hostCredential) throws DataManagerException {
        return null;
    }

    /**
     * Move file from one place to another inside the same storage resource
     *
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param sourcePath
     * @param destinationPath
     * @throws DataManagerException
     */
    @Override
    public void moveFile(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                         String sourcePath, String destinationPath) throws DataManagerException {

    }

    /**
     * Rename a file
     *
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param sourcePath
     * @param newName
     * @throws DataManagerException
     */
    @Override
    public void renameFile(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                           String sourcePath, String newName) throws DataManagerException {

    }

    /**
     * Create new directory
     *
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param dirPath
     * @throws DataManagerException
     */
    @Override
    public void mkdir(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                      String dirPath) throws DataManagerException {

    }

    /**
     * Delete File in storage resource
     *
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param filePath
     * @throws DataManagerException
     */
    @Override
    public void deleteFile(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                           String filePath) throws DataManagerException {

    }

    /**
     * Check whether the specified file exists
     *
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param filePath
     * @return
     * @throws DataManagerException
     */
    @Override
    public boolean isExists(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                            String filePath) throws DataManagerException {
        return false;
    }

    /**
     * Check whether the path points to a directory
     *
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param filePath
     * @return
     * @throws DataManagerException
     */
    @Override
    public boolean isDirectory(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                               String filePath) throws DataManagerException {
        return false;
    }

    /**
     * Method to retrieve file transfer status giving transfer id
     *
     * @param transferId
     * @return
     * @throws DataManagerException
     */
    @Override
    public FileTransferRequestModel getFileTransferRequestStatus(String transferId) throws DataManagerException {
        try{
            return fileTransferRequestDao.getFileTransferRequest(transferId);
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new DataManagerException(ex);
        }
    }


    //TODO API Call to Credential Store
    private SSHCredential getCredential(String credentialStoreToken) throws DataManagerException {
        try{
            SSHCredential sshCredential = new SSHCredential();
            File privateKey = new File("/Users/supun/.ssh/id_rsa");
            byte[] privateKeyBytes = IOUtils.toByteArray(new FileInputStream(privateKey));
            File publicKey = new File("/Users/supun/.ssh/id_rsa.pub");
            byte[] publicKeyBytes = IOUtils.toByteArray(new FileInputStream(publicKey));
            String passPhrase = "cecilia@1990";
            sshCredential.setPrivateKey(privateKeyBytes);
            sshCredential.setPublicKey(publicKeyBytes);
            sshCredential.setPassphrase(passPhrase);
            return sshCredential;
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new DataManagerException(ex);
        }
    }

    public static void main(String[] args) throws IOException, DataManagerException {
        FileTransferServiceImpl fileTransferService = new FileTransferServiceImpl();
        String sourceFile = "fsgsdgsdgsdgsdg";
        String transferId = fileTransferService.uploadFile("default", "supun", sourceFile.getBytes(), "gw75.iu.xsede.org",
                "pga", 22, StorageResourceProtocol.SCP, "/var/www/portals/test.file", null);
        FileTransferRequestModel fileTransferRequestModel = fileTransferService.fileTransferRequestDao.getFileTransferRequest(transferId);
        System.out.println("file transfer id:" + fileTransferRequestModel.getTransferId());

        transferId = fileTransferService.transferFile("default", "supun", "gw75.iu.xsede.org", "pga", 22, StorageResourceProtocol.SCP,
                "/var/www/portals/test.file", null, "gw75.iu.xsede.org", "pga", 22, StorageResourceProtocol.SCP,
                "/var/www/portals/test2.file", null);
        fileTransferRequestModel = fileTransferService.fileTransferRequestDao.getFileTransferRequest(transferId);
        System.out.println("file transfer id:" + fileTransferRequestModel.getTransferId());
    }
}