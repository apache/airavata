/**
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
 */
package org.apache.airavata.registry.core.experiment.catalog.resources;

import org.apache.airavata.registry.core.experiment.catalog.ExpCatResourceUtils;
import org.apache.airavata.registry.core.experiment.catalog.ExperimentCatResource;
import org.apache.airavata.registry.core.experiment.catalog.ResourceType;
import org.apache.airavata.registry.core.experiment.catalog.model.QueueStatus;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class QueueStatusResource extends AbstractExpCatResource {
    private final static Logger logger = LoggerFactory.getLogger(QueueStatusResource.class);
    private String hostName;
    private String queueName;
    private Long time;
    private Boolean queueUp;
    private Integer runningJobs;
    private Integer queuedJobs;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Boolean getQueueUp() {
        return queueUp;
    }

    public void setQueueUp(Boolean queueUp) {
        this.queueUp = queueUp;
    }

    public Integer getRunningJobs() {
        return runningJobs;
    }

    public void setRunningJobs(Integer runningJobs) {
        this.runningJobs = runningJobs;
    }

    public Integer getQueuedJobs() {
        return queuedJobs;
    }

    public void setQueuedJobs(Integer queuedJobs) {
        this.queuedJobs = queuedJobs;
    }


    /**
     * This method will create associate resource objects for the given resource type.
     *
     * @param type child resource type
     * @return associate child resource
     */
    @Override
    public ExperimentCatResource create(ResourceType type) throws  RegistryException {
        throw new RegistryException("Method not supported...!!!");
    }

    /**
     * This method will remove the given child resource from the database
     *
     * @param type child resource type
     * @param name child resource name
     */
    @Override
    public void remove(ResourceType type, Object name) throws RegistryException {
        throw new RegistryException("Method not supported...!!!");
    }

    /**
     * This method will return the given child resource from the database
     *
     * @param type child resource type
     * @param name child resource name
     * @return associate child resource
     */
    @Override
    public ExperimentCatResource get(ResourceType type, Object name) throws RegistryException {
        throw new RegistryException("Method not supported...!!!");
    }

    /**
     * This method will list all the child resources for the given resource type
     *
     * @param type child resource type
     * @return list of child resources of the given child resource type
     */
    @Override
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException {
        List<ExperimentCatResource> result = new ArrayList<>();
        EntityManager em = null;
        try {
            String query = "SELECT q from QueueStatus q WHERE q.time IN (SELECT MAX(q2.time) FROM QueueStatus q2 GROUP BY q2.hostName, q2.queueName)";
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            Query q = em.createQuery(query);
            List resultList = q.getResultList();
            for (Object o : resultList) {
                QueueStatus queueStatus = (QueueStatus) o;
                QueueStatusResource queueStatusResource = new QueueStatusResource();
                queueStatusResource.setHostName(queueStatus.getHostName());
                queueStatusResource.setQueueName(queueStatus.getQueueName());
                queueStatusResource.setTime(queueStatus.getTime());
                queueStatusResource.setQueueUp(queueStatus.getQueueUp());
                queueStatusResource.setQueuedJobs(queueStatus.getQueuedJobs());
                queueStatusResource.setRunningJobs(queueStatus.getRunningJobs());
                result.add(queueStatusResource);
            }
            em.getTransaction().commit();
            em.close();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return result;
    }

    /**
     * This method will save the resource to the database.
     */
    @Override
    public void save() throws RegistryException {
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            QueueStatus queueStatus = new QueueStatus();
            queueStatus.setHostName(hostName);
            queueStatus.setQueueName(queueName);
            queueStatus.setTime(time);
            queueStatus.setQueueUp(queueUp);
            queueStatus.setRunningJobs(runningJobs);
            queueStatus.setQueuedJobs(queuedJobs);
            em.getTransaction().begin();
            em.persist(queueStatus);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
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
