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
package org.apache.airavata.data.manager.cpi;

import org.apache.airavata.model.data.transfer.FileTransferRequestModel;
import org.apache.airavata.model.data.transfer.LSEntryModel;
import org.apache.airavata.model.data.transfer.StorageResourceProtocol;

import java.util.List;

public interface FileTransferService {

    /**
     * Method to upload the give bytes to the destination storage system
     * @param gatewayId
     * @param username
     * @param fileData
     * @param destHostname
     * @param destLoginName
     * @param destPort
     * @param destProtocol
     * @param destinationPath
     * @param destHostCredToken
     * @return
     * @throws DataManagerException
     */
    String uploadFile(String gatewayId, String username, byte[] fileData, String destHostname, String destLoginName, int destPort,
                    StorageResourceProtocol destProtocol, String destinationPath, String destHostCredToken)
            throws DataManagerException;

    /**
     * Transfer file between two storage resources synchronously. Returns the file transfer request id
     * @param gatewayId
     * @param username
     * @param srcHostname
     * @param srcPort
     * @param srcLoginName
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
    String transferFile(String gatewayId, String username, String srcHostname, String srcLoginName, int srcPort,
                      StorageResourceProtocol srcProtocol, String srcPath, String srcHostCredToken,
                      String destHostname, String destLoginName, int destPort,
                      StorageResourceProtocol destProtocol, String destinationPath, String destHostCredToken)
            throws DataManagerException;

    /**
     * Transfer file between two storage resources asynchronously. Returns the file transfer request id
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
    String transferFileAsync(String gatewayId, String username, String srcHostname, String srcLoginName, int srcPort,
                           StorageResourceProtocol srcProtocol, String srcPath, String srcHostCredToken,
                           String destHostname, String destLoginName, int destPort,
                           StorageResourceProtocol destProtocol, String destinationPath, String destHostCredToken,
                           String[] callbackEmails)
            throws DataManagerException;

    /**
     * Get a directory listing of the specified source directory
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param path
     * @param hostCredential
     * @return
     * @throws DataManagerException
     */
    List<LSEntryModel> getDirectoryListing(String hostname, String loginName, int port,
                                       StorageResourceProtocol protocol, String path, String hostCredential)
            throws DataManagerException;

    /**
     * Move file from one place to another inside the same storage resource
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param sourcePath
     * @param destinationPath
     * @throws DataManagerException
     */
    void moveFile(String hostname, String loginName, int port,
                  StorageResourceProtocol protocol, String hostCredential, String sourcePath, String destinationPath)
            throws DataManagerException;

    /**
     * Rename a file
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param sourcePath
     * @param newName
     * @throws DataManagerException
     */
    void renameFile(String hostname, String loginName, int port,
                    StorageResourceProtocol protocol, String hostCredential, String sourcePath, String newName)
            throws DataManagerException;

    /**
     * Create new directory
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param dirPath
     * @throws DataManagerException
     */
    void mkdir(String hostname, String loginName, int port,
               StorageResourceProtocol protocol, String hostCredential, String dirPath)
            throws DataManagerException;

    /**
     * Delete File in storage resource
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param filePath
     * @throws DataManagerException
     */
    void deleteFile(String hostname, String loginName, int port,
                    StorageResourceProtocol protocol, String hostCredential, String filePath)
            throws DataManagerException;

    /**
     * Check whether the specified file exists
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param filePath
     * @return
     * @throws DataManagerException
     */
    boolean isExists(String hostname, String loginName, int port,
                    StorageResourceProtocol protocol, String hostCredential, String filePath)
            throws DataManagerException;

    /**
     * Check whether the path points to a directory
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param filePath
     * @return
     * @throws DataManagerException
     */
    boolean isDirectory(String hostname, String loginName, int port,
                     StorageResourceProtocol protocol, String hostCredential, String filePath)
            throws DataManagerException;

    /**
     * Method to retrieve file transfer status giving transfer id
     * @param transferId
     * @return
     * @throws DataManagerException
     */
    FileTransferRequestModel getFileTransferRequestStatus(String transferId)
            throws DataManagerException;
}