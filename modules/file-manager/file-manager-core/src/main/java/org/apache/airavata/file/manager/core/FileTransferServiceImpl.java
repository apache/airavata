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
package org.apache.airavata.file.manager.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.airavata.credential.store.credential.impl.ssh.SSHCredential;
import org.apache.airavata.file.manager.core.db.dao.FileTransferRequestDao;
import org.apache.airavata.file.manager.core.remote.client.RemoteStorageClient;
import org.apache.airavata.file.manager.core.remote.client.http.HTTPStorageClient;
import org.apache.airavata.file.manager.core.remote.client.scp.SCPStorageClient;
import org.apache.airavata.file.manager.core.remote.client.sftp.SFTPStorageClient;
import org.apache.airavata.file.manager.cpi.FileManagerException;
import org.apache.airavata.file.manager.cpi.FileTransferService;
import org.apache.airavata.model.file.*;
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
     * @param fileData
     * @param destHostName
     * @param destLoginName
     * @param destPort
     * @param destProtocol
     * @param destinationPath
     * @param destHostCredToken
     * @return
     * @throws FileManagerException
     */
    @Override
    public String uploadFile(byte[] fileData, String destHostName, String destLoginName, int destPort,
                             StorageResourceProtocol destProtocol,
                             String destinationPath, String destHostCredToken) throws FileManagerException {
        long transferTime = System.currentTimeMillis();
        if(destProtocol == StorageResourceProtocol.SCP || destProtocol == StorageResourceProtocol.SFTP) {
            Object credential = getCredential(destHostCredToken);
            SSHCredential sshCredential;
            if (credential instanceof SSHCredential) {
                sshCredential = (SSHCredential) credential;
                File srcFile = null;
                FileWriter fileWriter = null;
                FileTransferRequest fileTransferRequest = null;
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

                    fileTransferRequest = new FileTransferRequest();
                    fileTransferRequest.setSrcHostname(InetAddress.getLocalHost().getHostName());
                    fileTransferRequest.setSrcProtocol(StorageResourceProtocol.LOCAL);
                    fileTransferRequest.setDestHostname(destHostName);
                    fileTransferRequest.setDestLoginName(destLoginName);
                    fileTransferRequest.setDestPort(destPort);
                    fileTransferRequest.setDestProtocol(destProtocol);
                    fileTransferRequest.setDestFilePath(destinationPath);
                    fileTransferRequest.setDestHostCredToken(destHostCredToken);
                    fileTransferRequest.setFileTransferMode(FileTransferMode.SYNC);
                    remoteStorageClient.writeFile(srcFile, destinationPath);
                    transferTime = System.currentTimeMillis() - transferTime;
                    fileTransferRequest.setTransferTime(transferTime);
                    fileTransferRequest.setTransferStatus(FileTransferStatus.COMPLETED);
                    fileTransferRequest.setCreatedTime(System.currentTimeMillis());
                    fileTransferRequest.setLastModifiedType(fileTransferRequest.getCreatedTime());
                    fileTransferRequest.setFileSize(srcFile.length());
                    String transferId = fileTransferRequestDao.createFileTransferRequest(fileTransferRequest);
                    return transferId;
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    if(fileTransferRequest != null) {
                        fileTransferRequest.setTransferStatus(FileTransferStatus.FAILED);
                        try {
                            fileTransferRequestDao.createFileTransferRequest(fileTransferRequest);
                        } catch (JsonProcessingException e1) {
                            logger.error(e.getMessage(), e);
                            throw new FileManagerException(e);
                        }
                    }
                    throw new FileManagerException(e.getMessage());
                } finally {
                    if(srcFile != null)
                        srcFile.delete();
                    if(fileWriter != null)
                        try {
                            fileWriter.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                            throw new FileManagerException(e);
                        }
                }
            } else {
                throw new FileManagerException("Only SSHCredential type is supported");
            }
        }else{
            throw new FileManagerException(destProtocol.toString() + " protocol is not supported for this method");
        }
    }

    /**
     * Transfer file between two storage resources synchronously. Returns the file transfer request id
     *
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
     * @throws FileManagerException
     */
    @Override
    public String transferFile(String srcHostname, String srcLoginName, int srcPort, StorageResourceProtocol srcProtocol,
                               String srcPath, String srcHostCredToken, String destHostname, String destLoginName, int destPort,
                               StorageResourceProtocol destProtocol, String destinationPath, String destHostCredToken)
            throws FileManagerException {
        long transferTime = System.currentTimeMillis();
        File srcFile = null;
        FileTransferRequest fileTransferRequest = null;
        try{
            fileTransferRequest = new FileTransferRequest();
            fileTransferRequest.setSrcHostname(srcHostname);
            fileTransferRequest.setSrcPort(srcPort);
            fileTransferRequest.setSrcLoginName(srcLoginName);
            fileTransferRequest.setSrcFilePath(srcPath);
            fileTransferRequest.setSrcProtocol(srcProtocol);
            fileTransferRequest.setSrcHostCredToken(srcHostCredToken);
            fileTransferRequest.setDestHostname(destHostname);
            fileTransferRequest.setDestPort(destPort);
            fileTransferRequest.setDestLoginName(destLoginName);
            fileTransferRequest.setDestFilePath(destinationPath);
            fileTransferRequest.setDestProtocol(destProtocol);
            fileTransferRequest.setDestHostCredToken(destHostCredToken);
            fileTransferRequest.setCreatedTime(System.currentTimeMillis());
            fileTransferRequest.setLastModifiedType(fileTransferRequest.getCreatedTime());
            fileTransferRequest.setFileTransferMode(FileTransferMode.SYNC);

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
                        throw new FileManagerException("Only support SSHCredentials for SCP host");
                    }
                }else{
                    Object credential = getCredential(srcHostCredToken);
                    if(credential instanceof SSHCredential){
                        SSHCredential sshCredential = (SSHCredential) credential;
                        srcClient = new SFTPStorageClient(srcHostname, srcPort, srcLoginName, sshCredential.getPrivateKey(),
                                sshCredential.getPublicKey(), sshCredential.getPassphrase().getBytes());
                    }else{
                        throw new FileManagerException("Only support SSHCredentials for SFTP host");
                    }
                }
                fileTransferRequest.setTransferStatus(FileTransferStatus.RUNNING);
                srcFile = srcClient.readFile(srcPath);
            }else{
                throw new FileManagerException("Unsupported src protocol " + srcProtocol);
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
                        throw new FileManagerException("Only support SSHCredentials for SCP host");
                    }
                }else{
                    Object credential = getCredential(srcHostCredToken);
                    if(credential instanceof SSHCredential){
                        SSHCredential sshCredential = (SSHCredential) credential;
                        destClient = new SFTPStorageClient(srcHostname, srcPort, srcLoginName, sshCredential.getPrivateKey(),
                                sshCredential.getPublicKey(), sshCredential.getPassphrase().getBytes());
                    }else{
                        throw new FileManagerException("Only support SSHCredentials for SFTP host");
                    }
                }
                destClient.writeFile(srcFile, destinationPath);
                transferTime = System.currentTimeMillis() - transferTime;
                fileTransferRequest.setTransferTime(transferTime);
                fileTransferRequest.setFileSize(srcFile.length());
                fileTransferRequest.setTransferStatus(FileTransferStatus.COMPLETED);
                String transferId = fileTransferRequestDao.createFileTransferRequest(fileTransferRequest);
                return transferId;
            }else{
                throw new FileManagerException("Unsupported src protocol " + srcProtocol);
            }
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            if(fileTransferRequest != null) {
                fileTransferRequest.setTransferStatus(FileTransferStatus.FAILED);
                try {
                    fileTransferRequestDao.createFileTransferRequest(fileTransferRequest);
                } catch (JsonProcessingException ex) {
                    logger.error(ex.getMessage(), ex);
                    throw new FileManagerException(ex);
                }
            }
            throw new FileManagerException(e);
        }finally {
            if(srcFile != null)
                srcFile.delete();
        }
    }

    /**
     * Transfer file between two storage resources asynchronously. Returns the file transfer request id
     *
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
     * @throws FileManagerException
     */
    @Override
    public String transferFileAsync(String srcHostname, String srcLoginName, int srcPort, StorageResourceProtocol srcProtocol,
                                    String srcPath, String srcHostCredToken, String destHostname, String destLoginName,
                                    int destPort, StorageResourceProtocol destProtocol, String destinationPath,
                                    String destHostCredToken, String[] callbackEmails) throws FileManagerException {
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
     * @throws FileManagerException
     */
    @Override
    public List<FileNode> getDirectoryListing(String hostname, String loginName, int port, StorageResourceProtocol protocol,
                                              String path, String hostCredential) throws FileManagerException {
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
     * @throws FileManagerException
     */
    @Override
    public void moveFile(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                         String sourcePath, String destinationPath) throws FileManagerException {

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
     * @throws FileManagerException
     */
    @Override
    public void renameFile(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                           String sourcePath, String newName) throws FileManagerException {

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
     * @throws FileManagerException
     */
    @Override
    public void mkdir(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                      String dirPath) throws FileManagerException {

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
     * @throws FileManagerException
     */
    @Override
    public void deleteFile(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                           String filePath) throws FileManagerException {

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
     * @throws FileManagerException
     */
    @Override
    public boolean isExists(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                            String filePath) throws FileManagerException {
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
     * @throws FileManagerException
     */
    @Override
    public boolean isDirectory(String hostname, String loginName, int port, StorageResourceProtocol protocol, String hostCredential,
                               String filePath) throws FileManagerException {
        return false;
    }

    /**
     * Method to retrieve file transfer status giving transfer id
     *
     * @param transferId
     * @return
     * @throws FileManagerException
     */
    @Override
    public FileTransferRequest getFileTransferRequestStatus(String transferId) throws FileManagerException {
        try{
            return fileTransferRequestDao.getFileTransferRequest(transferId);
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new FileManagerException(ex);
        }
    }


    //TODO API Call to Credential Store
    private SSHCredential getCredential(String credentialStoreToken) throws FileManagerException{
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
            throw new FileManagerException(ex);
        }
    }

    public static void main(String[] args) throws IOException, FileManagerException {
        FileTransferServiceImpl fileTransferService = new FileTransferServiceImpl();
        String sourceFile = "fsgsdgsdgsdgsdg";
        String transferId = fileTransferService.uploadFile(sourceFile.getBytes(), "gw75.iu.xsede.org", "pga", 22,
                StorageResourceProtocol.SCP, "/var/www/portals/test.file", null);
        FileTransferRequest fileTransferRequest = fileTransferService.fileTransferRequestDao.getFileTransferRequest(transferId);
        System.out.println("file transfer id:" + fileTransferRequest.getTransferId());

        transferId = fileTransferService.transferFile("gw75.iu.xsede.org", "pga", 22, StorageResourceProtocol.SCP,
                "/var/www/portals/test.file", null, "gw75.iu.xsede.org", "pga", 22, StorageResourceProtocol.SCP,
                "/var/www/portals/test2.file", null);
        fileTransferRequest = fileTransferService.fileTransferRequestDao.getFileTransferRequest(transferId);
        System.out.println("file transfer id:" + fileTransferRequest.getTransferId());
    }
}