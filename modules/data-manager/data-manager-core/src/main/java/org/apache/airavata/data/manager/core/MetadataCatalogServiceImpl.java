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

import org.apache.airavata.data.manager.core.db.dao.MetadataDao;
import org.apache.airavata.data.manager.cpi.DataManagerException;
import org.apache.airavata.data.manager.cpi.MetadataCatalogService;
import org.apache.airavata.model.data.metadata.MetadataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class MetadataCatalogServiceImpl implements MetadataCatalogService {
    private final static Logger logger = LoggerFactory.getLogger(MetadataCatalogServiceImpl.class);

    private MetadataDao metadataDao;

    public MetadataCatalogServiceImpl() throws IOException {
        this.metadataDao = new MetadataDao();
    }

    /**
     * Create new metadata model
     *
     * @param metadataModel
     * @return
     * @throws DataManagerException
     */
    @Override
    public String createMetadata(MetadataModel metadataModel) throws DataManagerException {
        try{
            return metadataDao.createMetadata(metadataModel);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * Update exisiting metadata model
     *
     * @param metadataModel
     * @throws DataManagerException
     */
    @Override
    public void updateMetadata(MetadataModel metadataModel) throws DataManagerException {
        try{
            metadataDao.updateMetadata(metadataModel);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * Delete existing metadata model
     *
     * @param metadataId
     * @throws DataManagerException
     */
    @Override
    public void deleteMetadata(String metadataId) throws DataManagerException {
        try{
            metadataDao.deleteMetadata(metadataId);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * Retrieve metadata model
     *
     * @param metadataId
     * @return
     * @throws DataManagerException
     */
    @Override
    public MetadataModel getMetadata(String metadataId) throws DataManagerException {
        try{
            return metadataDao.getMetadata(metadataId);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * Search Metadata
     *
     * @param username
     * @param gatewayId
     * @param searchText
     * @return
     * @throws DataManagerException
     */
    @Override
    public List<MetadataModel> searchMetadata(String username, String gatewayId, String searchText) throws DataManagerException {
        try{
            return metadataDao.searchMetaDataModels(username, gatewayId, searchText);
        }catch (Exception e){
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

//    public static void main(String[] args) throws IOException, DataManagerServiceException {
//        MetadataModel metadataModel = new MetadataModel();
//        metadataModel.setUsername("scnakandala");
//        metadataModel.setUserFriendlyName("Stress Free, 100km Tlayer");
//        metadataModel.setSize(103.67 * 1024);
//        metadataModel.setUserFriendlyDescription("Dynamo similar to C2-2, with a stress-free boundaries and a 100km thick" +
//                " thermal layer codBC.txt.in 2 0 0 0.129 0.0");
//        MetadataCatalogServiceImpl metadataCatalogService = new MetadataCatalogServiceImpl();
//        System.out.println(metadataCatalogService.searchMetadata("scnakandala", "default", "").size());
//    }
}