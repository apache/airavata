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

import org.apache.airavata.file.manager.cpi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileManagerImpl implements FileManager {
    private final static Logger logger = LoggerFactory.getLogger(FileManagerImpl.class);

    /**
     * Return file transfer service instance
     *
     * @return
     */
    @Override
    public FileTransferService getFileTransferService()  throws FileManagerException {
        try{
            return new FileTransferServiceImpl();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new FileManagerException(e);
        }
    }

    /**
     * Return replica catalog service instance
     *
     * @return
     */
    @Override
    public ReplicaCatalogService getReplicaCatalogService()  throws FileManagerException{
        try{
            return new ReplicaCatalogServiceImpl();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new FileManagerException(e);
        }
    }

    /**
     * Return metadata catalog service
     *
     * @return
     */
    @Override
    public MetadataCatalogService getMetadataCatalogService()  throws FileManagerException{
        try{
            return new MetadataCatalogServiceImpl();
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new FileManagerException(e);
        }
    }
}