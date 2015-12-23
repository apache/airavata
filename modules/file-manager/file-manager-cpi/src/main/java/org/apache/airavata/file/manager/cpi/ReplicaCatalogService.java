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

import org.apache.airavata.model.file.replica.FileCollectionModel;
import org.apache.airavata.model.file.replica.FileModel;

public interface ReplicaCatalogService {

    /**
     * Creates a new file entry in the replica catalog
     * @param fileModel
     * @return
     */
    String registerFileDetails(FileModel fileModel) throws FileManagerException;

    /**
     * Updates an existing file information
     * @param fileModel
     */
    void updateFileDetails(FileModel fileModel) throws FileManagerException;


    /**
     * Deletes the specified file details entry
     * @param fileId
     */
    void deleteFileDetails(String fileId) throws FileManagerException;


    /**
     * Retrieves file details for the specified file id
     * @param fileId
     * @return
     */
    FileModel getFileDetails(String fileId) throws FileManagerException;


    /**
     * Create new file collection entry
     * @param fileCollectionModel
     * @return
     * @throws FileManagerException
     */
    String registerFileCollection(FileCollectionModel fileCollectionModel) throws FileManagerException;

    /**
     * Update existing file collection
     * @param fileCollectionModel
     * @throws FileManagerException
     */
    void updateFileCollection(FileCollectionModel fileCollectionModel) throws FileManagerException;

    /**
     * Delete exisiting file collection
     * @param collectionId
     * @throws FileManagerException
     */
    void deleteFileCollection(String collectionId) throws FileManagerException;

    /**
     * Retrieve file collection specifying the collection id
     * @param collectionId
     * @return
     * @throws FileManagerException
     */
    FileCollectionModel getFileCollection(String collectionId) throws FileManagerException;
}