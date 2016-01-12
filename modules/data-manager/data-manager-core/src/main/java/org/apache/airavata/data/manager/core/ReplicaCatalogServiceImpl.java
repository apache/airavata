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

import org.apache.airavata.data.manager.core.db.dao.FileCollectionDao;
import org.apache.airavata.data.manager.core.db.dao.FileDao;
import org.apache.airavata.data.manager.cpi.DataManagerException;
import org.apache.airavata.data.manager.cpi.ReplicaCatalogService;
import org.apache.airavata.model.data.replica.FileCollectionModel;
import org.apache.airavata.model.data.replica.FileModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ReplicaCatalogServiceImpl implements ReplicaCatalogService {
    private final static Logger logger = LoggerFactory.getLogger(ReplicaCatalogServiceImpl.class);

    private FileDao fileDao;
    private FileCollectionDao fileCollectionDao;

    public ReplicaCatalogServiceImpl() throws IOException {
        this.fileDao = new FileDao();
        this.fileCollectionDao = new FileCollectionDao();
    }

    /**
     * Creates a new file entry in the replica catalog
     *
     * @param fileModel
     * @return
     */
    @Override
    public String registerFileDetails(FileModel fileModel) throws DataManagerException {
        try{
            return fileDao.createFile(fileModel);
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new DataManagerException(ex);
        }
    }

    /**
     * Updates an existing file information
     *
     * @param fileModel
     */
    @Override
    public void updateFileDetails(FileModel fileModel) throws DataManagerException {
        try{
            fileDao.updateFile(fileModel);
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new DataManagerException(ex);
        }
    }

    /**
     * Deletes the specified file details entry
     *
     * @param fileId
     */
    @Override
    public void deleteFileDetails(String fileId) throws DataManagerException {
        try{
            fileDao.deleteFile(fileId);
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new DataManagerException(ex);
        }
    }

    /**
     * Retrieves file details for the specified file id
     *
     * @param fileId
     * @return
     */
    @Override
    public FileModel getFileDetails(String fileId) throws DataManagerException {
        try{
            return fileDao.getFile(fileId);
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new DataManagerException(ex);
        }
    }

    /**
     * Create new file collection entry
     *
     * @param fileCollectionModel
     * @return
     * @throws DataManagerException
     */
    @Override
    public String registerFileCollection(FileCollectionModel fileCollectionModel) throws DataManagerException {
        try{
            return fileCollectionDao.createFileCollection(fileCollectionModel);
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new DataManagerException(ex);
        }
    }

    /**
     * Update existing file collection
     *
     * @param fileCollectionModel
     * @throws DataManagerException
     */
    @Override
    public void updateFileCollection(FileCollectionModel fileCollectionModel) throws DataManagerException {
        try{
            fileCollectionDao.updateFileCollection(fileCollectionModel);
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new DataManagerException(ex);
        }
    }

    /**
     * Delete exisiting file collection
     *
     * @param collectionId
     * @throws DataManagerException
     */
    @Override
    public void deleteFileCollection(String collectionId) throws DataManagerException {
        try{
            fileCollectionDao.deleteFileCollection(collectionId);
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new DataManagerException(ex);
        }
    }

    /**
     * Retrieve file collection specifying the collection id
     *
     * @param collectionId
     * @return
     * @throws DataManagerException
     */
    @Override
    public FileCollectionModel getFileCollection(String collectionId) throws DataManagerException {
        try{
            return fileCollectionDao.getFileCollection(collectionId);
        }catch (Exception ex){
            logger.error(ex.getMessage(), ex);
            throw new DataManagerException(ex);
        }
    }
}