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
import org.apache.airavata.registry.core.experiment.catalog.model.*;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

public class NotificationEmailExperimentCatResource extends AbstractExperimentCatResource {
    private static final Logger logger = LoggerFactory.getLogger(NotificationEmailExperimentCatResource.class);

    private int emailId = 0;
    private String experimentId;
    private String taskId;
    private String emailAddress;


    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for experiment input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Notification_Email notification_email;
            if (emailId != 0 ){
                notification_email  = em.find(Notification_Email.class, emailId);
                notification_email.setEmailId(emailId);
            }else {
                notification_email = new Notification_Email();
            }
            notification_email.setExperiment_id(experimentId);
            notification_email.setTaskId(taskId);
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
