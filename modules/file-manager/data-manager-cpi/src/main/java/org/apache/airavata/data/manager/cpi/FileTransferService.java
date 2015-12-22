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

import org.apache.airavata.model.file.FileNode;

public interface FileTransferService {

    void uploadFile(String fileName, byte[] fileData, String destinationResourceId, String destinationPath) throws DataManagerException;

    void importFile(String sourceUrl, String destinationResourceId, String destinationPath) throws DataManagerException;

    void transferFile(String sourceResourceId, String sourcePath, String destinationResourceId, String destinationPath) throws DataManagerException;

    FileNode getDirectoryListing(String storageResourceId, String directoryPath) throws DataManagerException;

    void moveFile(String storageResourceId, String sourcePath, String destinationPath) throws DataManagerException;

    void renameFile(String storageResourceId, String sourcePath, String newName) throws DataManagerException;

    void mkdir(String storageResourceId, String directoryPath) throws DataManagerException;
}