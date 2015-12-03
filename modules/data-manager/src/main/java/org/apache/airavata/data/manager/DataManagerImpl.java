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

import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.registry.core.experiment.catalog.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.apache.airavata.registry.cpi.DataCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataManagerImpl implements DataManager{
    private final static Logger logger = LoggerFactory.getLogger(DataManagerImpl.class);

    private DataCatalog dataCatalog;

    public DataManagerImpl() throws DataManagerException {
        try {
            this.dataCatalog = RegistryFactory.getDataCatalog();
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    @Override
    public String publishDataResource(DataResourceModel resourceModel) throws DataManagerException{
        try {
            String resourceId = dataCatalog.publishResource(resourceModel);
            return resourceId;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    @Override
    public boolean removeDataResource(String resourceId) throws DataManagerException {
        try {
            boolean result = dataCatalog.removeResource(resourceId);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    @Override
    public boolean updateDataResource(DataResourceModel dataResourceModel) throws DataManagerException {
        try {
            boolean result = dataCatalog.updateResource(dataResourceModel);
            return result;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    @Override
    public DataResourceModel getDataResource(String resourceId) throws DataManagerException {
        try {
            DataResourceModel dataResource = dataCatalog.getResource(resourceId);
            return dataResource;
        } catch (DataCatalogException e) {
            logger.error(e.getMessage(), e);
            throw new DataManagerException(e);
        }
    }

    @Override
    public boolean copyDataResource(String resourceId, String destLocation) throws DataManagerException{
        return false;
    }
}