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

package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.DataTransferDetail;
import org.apache.airavata.registry.core.experiment.catalog.model.Status;
import org.apache.airavata.registry.core.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.apache.airavata.registry.cpi.utils.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DataTransferDetailExperimentCatResource extends AbstractExperimentCatResource {
    private static final Logger logger = LoggerFactory.getLogger(DataTransferDetailExperimentCatResource.class);
    private String transferId;
    private String taskId;
    private Timestamp creationTime;
    private String transferDescription;
    private StatusExperimentCatResource datatransferStatus;

    public StatusExperimentCatResource getDatatransferStatus() {
        return datatransferStatus;
    }

    public void setDatatransferStatus(StatusExperimentCatResource datatransferStatus) {
        this.datatransferStatus = datatransferStatus;
    }

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public String getTransferDescription() {
        return transferDescription;
    }

    public void setTransferDescription(String transferDescription) {
        this.transferDescription = transferDescription;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        switch (type){
            case STATUS:
                StatusExperimentCatResource statusResource = new StatusExperimentCatResource();
                statusResource.setTransferId(transferId);
                return statusResource;
            default:
                logger.error("Unsupported resource type for data transfer details data resource.", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException {
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            switch (type) {
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.TRANSFER_ID, name);
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.DATA_TRANSFER);
                    q = generator.deleteQuery(em);
                    q.executeUpdate();
                    break;
                default:
                    logger.error("Unsupported resource type for data transfer details resource.", new IllegalArgumentException());
                    break;
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator;
            Query q;
            switch (type) {
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.TRANSFER_ID, name);
                    generator.setParameter(StatusConstants.STATUS_TYPE, StatusType.DATA_TRANSFER);
                    q = generator.selectQuery(em);
                    Status status = (Status) q.getSingleResult();
                    StatusExperimentCatResource statusResource = (StatusExperimentCatResource) Utils.getResource(ResourceType.STATUS, status);
                    em.getTransaction().commit();
                    em.close();
                    return statusResource;
                default:
                    em.getTransaction().commit();
                    em.close();
                    logger.error("Unsupported resource type for data transfer details resource.", new IllegalArgumentException());
                    throw new IllegalArgumentException("Unsupported resource type for data transfer details resource.");
            }
        } catch (Exception e) {
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException{
        List<ExperimentCatResource> resourceList = new ArrayList<ExperimentCatResource>();
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            QueryGenerator generator;
            List results;
            switch (type) {
                case STATUS:
                    generator = new QueryGenerator(STATUS);
                    generator.setParameter(StatusConstants.TRANSFER_ID, transferId);
                    q = generator.selectQuery(em);
                    results = q.getResultList();
                    if (results.size() != 0) {
                        for (Object result : results) {
                            Status status = (Status) result;
                            StatusExperimentCatResource statusResource =
                                    (StatusExperimentCatResource) Utils.getResource(ResourceType.STATUS, status);
                            resourceList.add(statusResource);
                        }
                    }
                    break;
                default:
                    em.getTransaction().commit();
                    em.close();
                    logger.error("Unsupported resource type for workflow node details resource.", new UnsupportedOperationException());
                    throw new UnsupportedOperationException();
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return resourceList;
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            DataTransferDetail existingDF = em.find(DataTransferDetail.class, transferId);
            em.close();

            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            DataTransferDetail dataTransferDetail = new DataTransferDetail();
            dataTransferDetail.setTransferId(transferId);
            dataTransferDetail.setTaskId(taskId);
            dataTransferDetail.setCreationTime(creationTime);
            if (transferDescription != null) {
                dataTransferDetail.setTransferDesc(transferDescription.toCharArray());
            }
            if (existingDF != null) {
                existingDF.setTransferId(transferId);
                existingDF.setTaskId(taskId);
                existingDF.setCreationTime(creationTime);
                if (transferDescription != null) {
                    existingDF.setTransferDesc(transferDescription.toCharArray());
                }
                dataTransferDetail = em.merge(existingDF);
            } else {
                em.persist(dataTransferDetail);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public StatusExperimentCatResource getDataTransferStatus () throws RegistryException{
        List<ExperimentCatResource> resources = get(ResourceType.STATUS);
        for (ExperimentCatResource resource : resources) {
            StatusExperimentCatResource dataTransferStatus = (StatusExperimentCatResource) resource;
            if(dataTransferStatus.getStatusType().equals(StatusType.DATA_TRANSFER.toString())){
                if (dataTransferStatus.getState() == null || dataTransferStatus.getState().equals("") ){
                    dataTransferStatus.setState("UNKNOWN");
                }
                return dataTransferStatus;
            }
        }
        return null;
    }
}
