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
package org.apache.airavata.file.manager.cpi;

import org.apache.airavata.model.file.FileNode;
import org.apache.airavata.model.file.FileTransferRequest;
import org.apache.airavata.model.file.StorageResourceProtocol;

import java.util.List;

public interface FileTransferService {

    /**
     * Method to upload the give bytes to the destination storage system
     * @param fileData
     * @param destHostname
     * @param destLoginName
     * @param destPort
     * @param destProtocol
     * @param destinationPath
     * @param destHostCredToken
     * @return
     * @throws FileManagerException
     */
    String uploadFile(byte[] fileData, String destHostname, String destLoginName, int destPort,
                    StorageResourceProtocol destProtocol, String destinationPath, String destHostCredToken)
            throws FileManagerException;

    /**
     * Transfer file between two storage resources synchronously. Returns the file transfer request id
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
     * @throws FileManagerException
     */
    String transferFile(String srcHostname, String srcLoginName, int srcPort,
                      StorageResourceProtocol srcProtocol, String srcPath, String srcHostCredToken,
                      String destHostname, String destLoginName, int destPort,
                      StorageResourceProtocol destProtocol, String destinationPath, String destHostCredToken)
            throws FileManagerException;

    /**
     * Transfer file between two storage resources asynchronously. Returns the file transfer request id
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
    String transferFileAsync(String srcHostname, String srcLoginName, int srcPort,
                           StorageResourceProtocol srcProtocol, String srcPath, String srcHostCredToken,
                           String destHostname, String destLoginName, int destPort,
                           StorageResourceProtocol destProtocol, String destinationPath, String destHostCredToken,
                           String[] callbackEmails)
            throws FileManagerException;

    /**
     * Get a directory listing of the specified source directory
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param path
     * @param hostCredential
     * @return
     * @throws FileManagerException
     */
    List<FileNode> getDirectoryListing(String hostname, String loginName, int port,
                                       StorageResourceProtocol protocol, String path, String hostCredential)
            throws FileManagerException;

    /**
     * Move file from one place to another inside the same storage resource
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param sourcePath
     * @param destinationPath
     * @throws FileManagerException
     */
    void moveFile(String hostname, String loginName, int port,
                  StorageResourceProtocol protocol, String hostCredential, String sourcePath, String destinationPath)
            throws FileManagerException;

    /**
     * Rename a file
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param sourcePath
     * @param newName
     * @throws FileManagerException
     */
    void renameFile(String hostname, String loginName, int port,
                    StorageResourceProtocol protocol, String hostCredential, String sourcePath, String newName)
            throws FileManagerException;

    /**
     * Create new directory
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param dirPath
     * @throws FileManagerException
     */
    void mkdir(String hostname, String loginName, int port,
               StorageResourceProtocol protocol, String hostCredential, String dirPath)
            throws FileManagerException;

    /**
     * Delete File in storage resource
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param filePath
     * @throws FileManagerException
     */
    void deleteFile(String hostname, String loginName, int port,
                    StorageResourceProtocol protocol, String hostCredential, String filePath)
            throws FileManagerException;

    /**
     * Check whether the specified file exists
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param filePath
     * @return
     * @throws FileManagerException
     */
    boolean isExists(String hostname, String loginName, int port,
                    StorageResourceProtocol protocol, String hostCredential, String filePath)
            throws FileManagerException;

    /**
     * Check whether the path points to a directory
     * @param hostname
     * @param loginName
     * @param port
     * @param protocol
     * @param hostCredential
     * @param filePath
     * @return
     * @throws FileManagerException
     */
    boolean isDirectory(String hostname, String loginName, int port,
                     StorageResourceProtocol protocol, String hostCredential, String filePath)
            throws FileManagerException;

    /**
     * Method to retrieve file transfer status giving transfer id
     * @param transferId
     * @return
     * @throws FileManagerException
     */
    FileTransferRequest getFileTransferRequestStatus(String transferId)
            throws FileManagerException;
}