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

package org.apache.airavata.registry.core.data.catalog.impl;

import org.apache.airavata.model.data.resource.DataReplicaLocationModel;
import org.apache.airavata.model.data.resource.DataResourceModel;
import org.apache.airavata.registry.core.data.catalog.model.DataReplicaLocation;
import org.apache.airavata.registry.core.data.catalog.model.DataResource;
import org.apache.airavata.registry.core.data.catalog.utils.DataCatalogJPAUtils;
import org.apache.airavata.registry.core.data.catalog.utils.ThriftDataModelConversion;
import org.apache.airavata.registry.cpi.DataCatalog;
import org.apache.airavata.registry.cpi.DataCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DataCatalogImpl implements DataCatalog {

    private final static Logger logger = LoggerFactory.getLogger(DataCatalogImpl.class);

    @Override
    public String publishResource(DataResourceModel resourceModel) throws DataCatalogException {
        String resourceId = UUID.randomUUID().toString();
        resourceModel.setResourceId(resourceId);
        if(resourceModel.getDataReplicaLocations() != null){
            resourceModel.getDataReplicaLocations().stream().forEach(r-> {
                r.setResourceId(resourceId);
                r.setReplicaId(UUID.randomUUID().toString());
            });
        }
        resourceModel.setCreationTime(System.currentTimeMillis());
        resourceModel.setLastModifiedTime(System.currentTimeMillis());
        DataResource dataResource = ThriftDataModelConversion.getDataResource(resourceModel);
        EntityManager em = null;
        try {
            em = DataCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            em.persist(dataResource);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return resourceId;
    }

    @Override
    public boolean removeResource(String resourceId) throws DataCatalogException {
        EntityManager em = null;
        try {
            em = DataCatalogJPAUtils.getEntityManager();
            DataResource dataResource = em.find(DataResource.class, resourceId);
            if(dataResource == null)
                return false;
            em.getTransaction().begin();
            em.remove(dataResource);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return true;
    }

    @Override
    public boolean updateResource(DataResourceModel resourceModel) throws DataCatalogException {
        EntityManager em = null;
        try {
            em = DataCatalogJPAUtils.getEntityManager();
            DataResource dataResource = em.find(DataResource.class, resourceModel.getResourceId());
            if(dataResource == null)
                return false;
            em.getTransaction().begin();
            resourceModel.setCreationTime(dataResource.getCreationTime().getTime());
            resourceModel.setLastModifiedTime(System.currentTimeMillis());
            em.merge(ThriftDataModelConversion.getUpdatedDataResource(resourceModel, dataResource));
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return true;
    }

    @Override
    public DataResourceModel getResource(String resourceId) throws DataCatalogException  {
        EntityManager em = null;
        try {
            em = DataCatalogJPAUtils.getEntityManager();
            DataResource dataResource = em.find(DataResource.class, resourceId);
            return ThriftDataModelConversion.getDataResourceModel(dataResource);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public String publishReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataCatalogException {
        String replicaId = UUID.randomUUID().toString();
        dataReplicaLocationModel.setReplicaId(replicaId);
        dataReplicaLocationModel.setCreationTime(System.currentTimeMillis());
        dataReplicaLocationModel.setLastModifiedTime(System.currentTimeMillis());
        DataReplicaLocation replicaLocation = ThriftDataModelConversion.getDataReplicaLocation(dataReplicaLocationModel);
        EntityManager em = null;
        try {
            em = DataCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            em.persist(replicaLocation);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return replicaId;
    }

    @Override
    public boolean removeReplicaLocation(String replicaId) throws DataCatalogException {
        EntityManager em = null;
        try {
            em = DataCatalogJPAUtils.getEntityManager();
            DataReplicaLocation replicaLocation = em.find(DataReplicaLocation.class, replicaId);
            if(replicaLocation == null)
                return false;
            em.getTransaction().begin();
            em.remove(replicaLocation);
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return true;
    }

    @Override
    public boolean updateReplicaLocation(DataReplicaLocationModel dataReplicaLocationModel) throws DataCatalogException {
        EntityManager em = null;
        try {
            em = DataCatalogJPAUtils.getEntityManager();
            DataReplicaLocation dataReplicaLocation = em.find(DataReplicaLocation.class, dataReplicaLocationModel.getReplicaId());
            if(dataReplicaLocation == null)
                return false;
            em.getTransaction().begin();
            dataReplicaLocationModel.setCreationTime(dataReplicaLocation.getCreationTime().getTime());
            dataReplicaLocationModel.setLastModifiedTime(System.currentTimeMillis());
            em.merge(ThriftDataModelConversion.getUpdatedDataReplicaLocation(dataReplicaLocationModel, dataReplicaLocation));
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return true;
    }

    @Override
    public DataReplicaLocationModel getReplicaLocation(String replicaId) throws DataCatalogException {
        EntityManager em = null;
        try {
            em = DataCatalogJPAUtils.getEntityManager();
            DataReplicaLocation replicaLocation = em.find(DataReplicaLocation.class, replicaId);
            return ThriftDataModelConversion.getDataReplicaLocationModel(replicaLocation);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    @Override
    public List<DataReplicaLocationModel> getAllReplicaLocations(String resourceId) throws DataCatalogException {
        EntityManager em = null;
        try {
            em = DataCatalogJPAUtils.getEntityManager();
            DataResource dataResource = em.find(DataResource.class, resourceId);
            if(dataResource == null)
                return null;
            ArrayList<DataReplicaLocationModel> dataReplicaLocationModels = new ArrayList<>();
            dataResource.getDataReplicaLocations().stream().forEach(rl->dataReplicaLocationModels
                    .add(ThriftDataModelConversion.getDataReplicaLocationModel(rl)));
            return dataReplicaLocationModels;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new DataCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
}
