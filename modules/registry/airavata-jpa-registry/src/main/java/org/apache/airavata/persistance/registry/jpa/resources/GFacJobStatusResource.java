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
import org.apache.airavata.persistance.registry.jpa.model.Experiment_Data;
import org.apache.airavata.persistance.registry.jpa.model.GFac_Job_Data;
import org.apache.airavata.persistance.registry.jpa.model.GFac_Job_Status;
import org.apache.airavata.persistance.registry.jpa.model.Workflow_Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.List;

public class GFacJobStatusResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(GFacJobStatusResource.class);
    private GFacJobDataResource gFacJobDataResource;
    private String localJobID;
    private Timestamp statusUpdateTime;
    private String status;

    public String getLocalJobID() {
        return localJobID;
    }

    public Timestamp getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setLocalJobID(String localJobID) {
        this.localJobID = localJobID;
    }

    public void setStatusUpdateTime(Timestamp statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public GFacJobDataResource getgFacJobDataResource() {
        return gFacJobDataResource;
    }

    public void setgFacJobDataResource(GFacJobDataResource gFacJobDataResource) {
        this.gFacJobDataResource = gFacJobDataResource;
    }

    @Override
    public Resource create(ResourceType type) {
        logger.error("Unsupported resource type for GFac Job status resource" ,new UnsupportedOperationException() );
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for GFac Job status resource" ,new UnsupportedOperationException() );
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for GFac Job status resource" ,new UnsupportedOperationException() );
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Resource> get(ResourceType type) {
        logger.error("Unsupported resource type for GFac Job status resource" ,new UnsupportedOperationException() );
        throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        GFac_Job_Status gFacJobStatus = new GFac_Job_Status();
        GFac_Job_Data gFacJobData = em.find(GFac_Job_Data.class, localJobID);
        gFacJobStatus.setgFac_job_data(gFacJobData);
        gFacJobStatus.setLocal_Job_ID(localJobID);
        gFacJobStatus.setStatus_update_time(statusUpdateTime);
        gFacJobStatus.setStatus(status);
        em.persist(gFacJobStatus);
        em.getTransaction().commit();
        em.close();
    }
}
