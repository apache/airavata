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
import org.apache.airavata.persistance.registry.jpa.model.*;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class NotificationEmailResource extends AbstractResource {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailResource.class);

    private int emailId = 0;
    private ExperimentResource experimentResource;
    private TaskDetailResource taskDetailResource;
    private String emailAddress;


    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public ExperimentResource getExperimentResource() {
        return experimentResource;
    }

    public void setExperimentResource(ExperimentResource experimentResource) {
        this.experimentResource = experimentResource;
    }

    public TaskDetailResource getTaskDetailResource() {
        return taskDetailResource;
    }

    public void setTaskDetailResource(TaskDetailResource taskDetailResource) {
        this.taskDetailResource = taskDetailResource;
    }

    public Resource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public List<Resource> get(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Notification_Email notification_email;
            if (emailId != 0 ){
                notification_email  = em.find(Notification_Email.class, emailId);
                notification_email.setEmailId(emailId);
            }else {
                notification_email = new Notification_Email();
            }
            Experiment experiment = em.find(Experiment.class, experimentResource.getExpID());
            notification_email.setExperiment(experiment);
            notification_email.setExperiment_id(experiment.getExpId());
            if (taskDetailResource != null){
                TaskDetail taskDetail = em.find(TaskDetail.class, taskDetailResource.getTaskId());
                notification_email.setTaskDetail(taskDetail);
                notification_email.setTaskId(taskDetail.getTaskId());
            }
            notification_email.setEmailAddress(emailAddress);
            em.persist(notification_email);
            emailId = notification_email.getEmailId();
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
}
