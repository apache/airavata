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
package org.apache.airavata.replica.catalog.core;

import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ReplicaCatalog;
import org.apache.airavata.registry.cpi.ReplicaCatalogException;

import org.apache.airavata.replica.catalog.cpi.DataManager;
import org.apache.airavata.replica.catalog.cpi.DataManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataManagerImpl implements DataManager {
    private final static Logger logger = LoggerFactory.getLogger(DataManagerImpl.class);

    private final ReplicaCatalog replicaCatalog;

    public DataManagerImpl() throws DataManagerException {
        try {
            this.replicaCatalog = RegistryFactory.getReplicaCatalog();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    public DataManagerImpl(ReplicaCatalog replicaCatalog){
        this.replicaCatalog = replicaCatalog;
    }

    /**
     * To create a replica entry for an already existing file(s). This is how the system comes to know about already
     * existing resources
     * @param dataResourceModel
     * @return
     */
    @Override
    public String registerResource(DataResourceModel dataResourceModel) throws DataManagerException {
        try {
            String resourceId = replicaCatalog.publishResource(dataResourceModel);
            return resourceId;
        } catch (ReplicaCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To remove a resource entry from the replica catalog
     * @param resourceId
     * @return
     */
    @Override
    public boolean removeResource(String resourceId) throws DataManagerException {
        try {
            boolean result = replicaCatalog.removeResource(resourceId);
            return result;
        } catch (ReplicaCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To update an existing data resource model
     * @param dataResourceModel
     * @return
     * @throws ReplicaCatalogException
     */
    @Override
    public boolean updateResource(DataResourceModel dataResourceModel) throws DataManagerException {
        try {
            boolean result = replicaCatalog.updateResource(dataResourceModel);
            return result;
        } catch (ReplicaCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To retrieve a resource object providing the resourceId
     * @param resourceId
     * @return
     */
    @Override
    public DataResourceModel getResource(String resourceId) throws DataManagerException {
        try {
            DataResourceModel dataResource = replicaCatalog.getResource(resourceId);
            return dataResource;
        } catch (ReplicaCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To create a new data replica location. This is how the system comes to know about already
     * existing resources
     *
     * @param dataReplicaLocationModel
     * @return
     */
    @Override
    public String registerReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataManagerException {
        try {
            String replicaId = replicaCatalog.publishReplicaLocation(dataReplicaLocationModel);
            return replicaId;
        } catch (ReplicaCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To remove a replica entry from the replica catalog
     *
     * @param replicaId
     * @return
     */
    @Override
    public boolean removeReplicaLocation(String replicaId) throws DataManagerException {
        try {
            boolean result = replicaCatalog.removeReplicaLocation(replicaId);
            return result;
        } catch (ReplicaCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To update an existing data replica model
     *
     * @param dataReplicaLocationModel
     * @return
     * @throws ReplicaCatalogException
     */
    @Override
    public boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataManagerException {
        try {
            boolean result = replicaCatalog.updateReplicaLocation(dataReplicaLocationModel);
            return result;
        } catch (ReplicaCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To retrieve a replica object providing the replicaId
     *
     * @param replicaId
     * @return
     */
    @Override
    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws DataManagerException {
        try {
            DataReplicaLocationModel dataReplicaLocationModel = replicaCatalog.getReplicaLocation(replicaId);
            return dataReplicaLocationModel;
        } catch (ReplicaCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To retrieve all the replica entries for a given resource id
     *
     * @param resourceId
     * @return
     * @throws ReplicaCatalogException
     */
    @Override
    public List<DataReplicaLocationModel> getAllReplicaLocations(String resourceId) throws DataManagerException {
        try {
            List<DataReplicaLocationModel> dataReplicaLocationModelList = replicaCatalog.getAllReplicaLocations(resourceId);
            return dataReplicaLocationModelList;
        } catch (ReplicaCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }
}