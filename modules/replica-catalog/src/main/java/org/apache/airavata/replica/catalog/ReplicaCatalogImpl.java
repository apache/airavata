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
package org.apache.airavata.replica.catalog;

import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.apache.airavata.registry.cpi.DataCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReplicaCatalogImpl implements ReplicaCatalog {
    private final static Logger logger = LoggerFactory.getLogger(ReplicaCatalogImpl.class);

    private final DataCatalog dataCatalog;

    public ReplicaCatalogImpl() throws ReplicaCatalogException {
        try {
            this.dataCatalog = RegistryFactory.getDataCatalog();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        }
    }

    public ReplicaCatalogImpl(DataCatalog dataCatalog){
        this.dataCatalog = dataCatalog;
    }

    /**
     * To create a replica entry for an already existing file(s). This is how the system comes to know about already
     * existing resources
     * @param dataResourceModel
     * @return
     */
    @Override
    public String publishResource(DataResourceModel dataResourceModel) throws ReplicaCatalogException {
        try {
            String resourceId = dataCatalog.publishResource(dataResourceModel);
            return resourceId;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        }
    }

    /**
     * To remove a resource entry from the replica catalog
     * @param resourceId
     * @return
     */
    @Override
    public boolean removeResource(String resourceId) throws ReplicaCatalogException {
        try {
            boolean result = dataCatalog.removeResource(resourceId);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        }
    }

    /**
     * To update an existing data resource model
     * @param dataResourceModel
     * @return
     * @throws ReplicaCatalogException
     */
    @Override
    public boolean updateResource(DataResourceModel dataResourceModel) throws ReplicaCatalogException {
        try {
            boolean result = dataCatalog.updateResource(dataResourceModel);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        }
    }

    /**
     * To retrieve a resource object providing the resourceId
     * @param resourceId
     * @return
     */
    @Override
    public DataResourceModel getResource(String resourceId) throws ReplicaCatalogException {
        try {
            DataResourceModel dataResource = dataCatalog.getResource(resourceId);
            return dataResource;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
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
    public String publishReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException {
        try {
            String replicaId = dataCatalog.publishReplicaLocation(dataReplicaLocationModel);
            return replicaId;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        }
    }

    /**
     * To remove a replica entry from the replica catalog
     *
     * @param replicaId
     * @return
     */
    @Override
    public boolean removeReplicaLocation(String replicaId) throws ReplicaCatalogException {
        try {
            boolean result = dataCatalog.removeReplicaLocation(replicaId);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
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
    public boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws ReplicaCatalogException {
        try {
            boolean result = dataCatalog.updateReplicaLocation(dataReplicaLocationModel);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        }
    }

    /**
     * To retrieve a replica object providing the replicaId
     *
     * @param replicaId
     * @return
     */
    @Override
    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws ReplicaCatalogException {
        try {
            DataReplicaLocationModel dataReplicaLocationModel = dataCatalog.getReplicaLocation(replicaId);
            return dataReplicaLocationModel;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        }
    }

    /**
     * To retrieve all the replica entries for a given resource id
     *
     * @param resourceId
     * @return
     * @throws DataCatalogException
     */
    @Override
    public List<DataReplicaLocationModel> getAllReplicaLocations(String resourceId) throws ReplicaCatalogException {
        try {
            List<DataReplicaLocationModel> dataReplicaLocationModelList = dataCatalog.getAllReplicaLocations(resourceId);
            return dataReplicaLocationModelList;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new ReplicaCatalogException(e);
        }
    }
}