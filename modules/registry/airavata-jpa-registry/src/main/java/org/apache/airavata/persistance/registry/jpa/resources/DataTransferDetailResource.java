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

package org.apache.airavata.persistance.registry.jpa.resources;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.DataTransferDetail;
import org.apache.airavata.persistance.registry.jpa.model.Status;
import org.apache.airavata.persistance.registry.jpa.model.TaskDetail;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DataTransferDetailResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(DataTransferDetailResource.class);
    private String transferId;
    private TaskDetailResource taskDetailResource;
    private Timestamp creationTime;
    private String transferDescription;

    public String getTransferId() {
        return transferId;
    }

    public void setTransferId(String transferId) {
        this.transferId = transferId;
    }

    public TaskDetailResource getTaskDetailResource() {
        return taskDetailResource;
    }

    public void setTaskDetailResource(TaskDetailResource taskDetailResource) {
        this.taskDetailResource = taskDetailResource;
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

    @Override
    public Resource create(ResourceType type) {
        switch (type){
            case STATUS:
                StatusResource statusResource = new StatusResource();
                statusResource.setDataTransferDetail(this);
                return statusResource;
            default:
                logger.error("Unsupported resource type for data transfer details data resource.", new UnsupportedOperationException());
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public void remove(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        switch (type){
            case STATUS:
                generator = new QueryGenerator(STATUS);
                generator.setParameter(StatusConstants.TRANSFER_ID, name);
                q = generator.deleteQuery(em);
                q.executeUpdate();
                break;
            default:
                logger.error("Unsupported resource type for data transfer details resource.", new IllegalArgumentException());
                break;
        }
        em.getTransaction().commit();
        em.close();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        QueryGenerator generator;
        Query q;
        switch (type) {
            case STATUS:
                generator = new QueryGenerator(STATUS);
                generator.setParameter(StatusConstants.TRANSFER_ID, name);
                q = generator.selectQuery(em);
                Status status = (Status)q.getSingleResult();
                StatusResource statusResource = (StatusResource)Utils.getResource(ResourceType.STATUS, status);
                em.getTransaction().commit();
                em.close();
                return statusResource;
            default:
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported resource type for data transfer details resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for data transfer details resource.");
        }
    }

    @Override
    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        List results;
        switch (type){
            case STATUS:
                generator = new QueryGenerator(STATUS);
                generator.setParameter(StatusConstants.TRANSFER_ID, transferId);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Status status = (Status) result;
                        StatusResource statusResource =
                                (StatusResource)Utils.getResource(ResourceType.STATUS, status);
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
        return resourceList;
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        DataTransferDetail existingDF = em.find(DataTransferDetail.class, transferId);
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        DataTransferDetail dataTransferDetail = new DataTransferDetail();
        TaskDetail taskDetail = em.find(TaskDetail.class, taskDetailResource.getTaskId());
        dataTransferDetail.setTransferId(transferId);
        dataTransferDetail.setTask(taskDetail);
        dataTransferDetail.setTaskId(taskDetailResource.getTaskId());
        dataTransferDetail.setCreationTime(creationTime);
        dataTransferDetail.setTransferDesc(transferDescription);
        if (existingDF != null){
            existingDF.setTransferId(transferId);
            existingDF.setTask(taskDetail);
            existingDF.setTaskId(taskDetailResource.getTaskId());
            existingDF.setCreationTime(creationTime);
            existingDF.setTransferDesc(transferDescription);
            dataTransferDetail = em.merge(existingDF);
        }else {
            em.merge(dataTransferDetail);
        }
        em.getTransaction().commit();
        em.close();
    }
}
