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
package org.apache.airavata.data.manager;

import org.apache.airavata.data.manager.utils.ssh.SSHAuthenticationUtils;
import org.apache.airavata.data.manager.utils.DataTransferUtils;
import org.apache.airavata.data.manager.utils.ssh.SSHKeyAuthentication;
import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.apache.airavata.registry.cpi.DataCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

public class DataManagerImpl implements DataManager{
    private final static Logger logger = LoggerFactory.getLogger(DataManagerImpl.class);

    private final DataCatalog dataCatalog;
    private final SSHKeyAuthentication sshKeyAuthentication;

    public DataManagerImpl() throws DataManagerException {
        try {
            this.dataCatalog = RegistryFactory.getDataCatalog();
            this.sshKeyAuthentication = SSHAuthenticationUtils.getSSHKeyAuthentication();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    public DataManagerImpl(DataCatalog dataCatalog, SSHKeyAuthentication sshKeyAuthentication){
        this.dataCatalog = dataCatalog;
        this.sshKeyAuthentication = sshKeyAuthentication;
    }

    /**
     * To create a replica entry for an already existing file(s). This is how the system comes to know about already
     * existing resources
     * @param dataResourceModel
     * @return
     */
    @Override
    public String publishResource(DataResourceModel dataResourceModel) throws DataManagerException{
        try {
            String resourceId = dataCatalog.publishResource(dataResourceModel);
            return resourceId;
        } catch (DataCatalogException e) {
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
            boolean result = dataCatalog.removeResource(resourceId);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To update an existing data resource model
     * @param dataResourceModel
     * @return
     * @throws DataManagerException
     */
    @Override
    public boolean updateResource(DataResourceModel dataResourceModel) throws DataManagerException {
        try {
            boolean result = dataCatalog.updateResource(dataResourceModel);
            return result;
        } catch (DataCatalogException e) {
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
            DataResourceModel dataResource = dataCatalog.getResource(resourceId);
            return dataResource;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To copy an already existing resource to a specified location. After successful copying the new location will be
     * added to the available replica locations of the resource. The replica to copy will be selected automatically based
     * on performance and availability metrics.
     * @param resourceId
     * @param destLocation
     * @return
     */
    @Override
    public boolean copyResource(String resourceId, String destLocation) throws DataManagerException{
        return false;
    }

    /**
     * To copy an already existing resource from the specified replica location to a specified location. After successful
     * copying the new location will be added to the available replica locations of the resource
     *
     * @param resourceId
     * @param replicaId
     * @param destLocation
     * @return
     * @throws DataManagerException
     */
    @Override
    public boolean copyResource(String resourceId, String replicaId, String destLocation)
            throws DataManagerException {
        try {
            //FIXME Bellow implementation is a skeleton code only to support the current airavata usecase of one to one copy
            DataResourceModel resourceModel = getResource(resourceId);
            DataReplicaLocationModel replicaLocationModel = getReplicaLocation(replicaId);
            if(resourceModel == null)
                throw new DataManagerException("Non existent resource id:"+resourceId);
            if(replicaLocationModel == null)
                throw new DataManagerException("Non existent replica id:"+replicaId);
            URI sourceUri = new URI(replicaLocationModel.getDataLocations().get(0));
            URI destinationUri = new URI(destLocation);
            DataTransferUtils dataTransferUtils = new DataTransferUtils(sshKeyAuthentication);
            boolean result = dataTransferUtils.copyData(sourceUri, destinationUri);
            if(result){

            }
            return result;
        } catch (Exception e) {
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
    public String publishReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataManagerException {
        try {
            String replicaId = dataCatalog.publishReplicaLocation(dataReplicaLocationModel);
            return replicaId;
        } catch (DataCatalogException e) {
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
            boolean result = dataCatalog.removeReplicaLocation(replicaId);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    /**
     * To update an existing data replica model
     *
     * @param dataReplicaLocationModel
     * @return
     * @throws DataManagerException
     */
    @Override
    public boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataManagerException {
        try {
            boolean result = dataCatalog.updateReplicaLocation(dataReplicaLocationModel);
            return result;
        } catch (DataCatalogException e) {
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
            DataReplicaLocationModel dataReplicaLocationModel = dataCatalog.getReplicaLocation(replicaId);
            return dataReplicaLocationModel;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
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
    public List<DataReplicaLocationModel> getAllReplicaLocations(String resourceId) throws DataManagerException {
        try {
            List<DataReplicaLocationModel> dataReplicaLocationModelList = dataCatalog.getAllReplicaLocations(resourceId);
            return dataReplicaLocationModelList;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }
}