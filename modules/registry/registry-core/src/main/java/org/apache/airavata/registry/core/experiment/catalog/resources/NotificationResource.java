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
import org.apache.airavata.registry.core.experiment.catalog.model.Notification;
import org.apache.airavata.registry.core.experiment.catalog.utils.QueryGenerator;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationResource extends AbstractExpCatResource {

    private final static Logger logger = LoggerFactory.getLogger(NotificationResource.class);

    private String notificationId;
    private String gatewayId;
    private String title;
    private String notificationMessage;
    private String priority;
    private Timestamp creationTime;
    private Timestamp publishedTime;
    private Timestamp expirationTime;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public Timestamp getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(Timestamp publishedTime) {
        this.publishedTime = publishedTime;
    }

    public Timestamp getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Timestamp expirationTime) {
        this.expirationTime = expirationTime;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @param type child resource type
     * @return child resource
     */
    public ExperimentCatResource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for user resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     * @param type child resource type
     * @param name child resource name
     */
    public void remove(ResourceType type, Object name) throws RegistryException {
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator = new QueryGenerator(NOTIFICATION);
            generator.setParameter(NotificationConstants.NOTIFICATION_ID, name);
            Query q = generator.deleteQuery(em);
            q.executeUpdate();
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
    }

    /**
     * @param type child resource type
     * @param name child resource name
     * @return UnsupportedOperationException
     */
    public ExperimentCatResource get(ResourceType type, Object notificationId) throws RegistryException {
        EntityManager em = null;
        try {
            if(!type.equals(ResourceType.NOTIFICATION)){
                logger.error("Unsupported resource type for Notification resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for Notification resource.");
            }
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator = new QueryGenerator(NOTIFICATION);
            generator.setParameter(NotificationConstants.NOTIFICATION_ID, notificationId);
            Query q = generator.selectQuery(em);
            Notification notification = (Notification)q.getSingleResult();
            em.getTransaction().commit();
            em.close();
            if(notification != null)
                return Utils.getResource(ResourceType.NOTIFICATION, notification);
            else
                return null;
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
    }

    /**
     * @param type child resource type
     * @return UnsupportedOperationException
     */
    public List<ExperimentCatResource> get(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for user resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    /**
     * save user to the database
     */
    public void save() throws RegistryException {
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            Notification existingNotification = em.find(Notification.class, notificationId);
            em.close();
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingNotification != null) {
                existingNotification.setNotificationId(notificationId);
                existingNotification.setGatewayId(gatewayId);
                existingNotification.setTitle(title);
                existingNotification.setNotificationMessage(notificationMessage);
                existingNotification.setPublishedDate(publishedTime);
                existingNotification.setExpirationDate(expirationTime);
                existingNotification.setCreationDate(creationTime);
                existingNotification.setPriority(priority);
                em.merge(existingNotification);
            } else {
                Notification notification = new Notification();
                notification.setNotificationId(notificationId);
                notification.setGatewayId(gatewayId);
                notification.setTitle(title);
                notification.setNotificationMessage(notificationMessage);
                notification.setPublishedDate(publishedTime);
                notification.setExpirationDate(expirationTime);
                notification.setCreationDate(creationTime);
                notification.setPriority(priority);
                em.persist(notification);
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
    }

    /**
     * @param type child resource type
     * @param name child resource name
     * @return UnsupportedOperationException
     */
    public boolean isExists(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for user resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public List<ExperimentCatResource> getAllNotifications(String gatewayId) throws RegistryException{
        List<ExperimentCatResource> resourceList = new ArrayList<ExperimentCatResource>();
        EntityManager em = null;
        try {
            em = ExpCatResourceUtils.getEntityManager();
            em.getTransaction().begin();
            QueryGenerator generator = new QueryGenerator(NOTIFICATION);
            generator.setParameter(NotificationConstants.GATEWAY_ID, gatewayId);
            Query q = generator.selectQuery(em);
            List<?> results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Notification notification = (Notification) result;
                    NotificationResource notificationResource = (NotificationResource)
                            Utils.getResource(ResourceType.NOTIFICATION, notification);
                    resourceList.add(notificationResource);
                }
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
        return resourceList;
    }
}
