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
package org.apache.airavata.registry.core.app.catalog.resources;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.core.app.catalog.model.WebDavDataMovement;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WebDavDataMovementResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(WebDavDataMovementResource.class);
    private String queueDescription;
    private String dataMovementInterfaceId;
    private String securityProtocol;
    private String webDavHostname;
    private int port;
    private Timestamp createdTime;
    private Timestamp updatedTime;

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public void remove(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WEBDAV_DATA_MOVEMENT);
            generator.setParameter(WebDavDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID, identifier);
            Query q = generator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
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
    public AppCatalogResource get(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WEBDAV_DATA_MOVEMENT);
            generator.setParameter(WebDavDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID, identifier);
            Query q = generator.selectQuery(em);
            WebDavDataMovement webDavDataMovement = (WebDavDataMovement) q.getSingleResult();
            WebDavDataMovementResource webDavDataMovementResource = (WebDavDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.WEBDAV_DATA_MOVEMENT, webDavDataMovement);

            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return webDavDataMovementResource;
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
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
    public List<AppCatalogResource> get(String fieldName, Object value) throws AppCatalogException {
        List<AppCatalogResource> webDavDataMovementResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WEBDAV_DATA_MOVEMENT);
            Query q;
            if ((fieldName.equals(WebDavDataMovementConstants.QUEUE_DESCRIPTION)) || (fieldName.equals(WebDavDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID)) || (fieldName.equals(WebDavDataMovementConstants.SECURITY_PROTOCOL)) || (fieldName.equals(WebDavDataMovementConstants.ALTERNATIVE_WEBDAV_HOSTNAME)) || (fieldName.equals(WebDavDataMovementConstants.SSH_PORT))) {
                generator.setParameter(fieldName, value);
                q = generator.selectQuery(em);
                List<?> results = q.getResultList();
                for (Object result : results) {
                    WebDavDataMovement webDavDataMovement = (WebDavDataMovement) result;
                    WebDavDataMovementResource webDavDataMovementResource = (WebDavDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.WEBDAV_DATA_MOVEMENT, webDavDataMovement);
                    webDavDataMovementResources.add(webDavDataMovementResource);
                }
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for WebDAV Data Movement Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for WebDAV Data Movement Resource.");
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return webDavDataMovementResources;
    }

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    @Override
    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> webDavDataMovementResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WEBDAV_DATA_MOVEMENT);
            Query q;
            if ((fieldName.equals(WebDavDataMovementConstants.QUEUE_DESCRIPTION)) || (fieldName.equals(WebDavDataMovementConstants.DATA_MOVEMENT_INTERFACE_ID)) || (fieldName.equals(WebDavDataMovementConstants.SECURITY_PROTOCOL)) || (fieldName.equals(WebDavDataMovementConstants.ALTERNATIVE_WEBDAV_HOSTNAME)) || (fieldName.equals(WebDavDataMovementConstants.SSH_PORT))) {
                generator.setParameter(fieldName, value);
                q = generator.selectQuery(em);
                List<?> results = q.getResultList();
                for (Object result : results) {
                    WebDavDataMovement webDavDataMovement = (WebDavDataMovement) result;
                    WebDavDataMovementResource webDavDataMovementResource = (WebDavDataMovementResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.WEBDAV_DATA_MOVEMENT, webDavDataMovement);
                    webDavDataMovementResourceIDs.add(webDavDataMovementResource.getDataMovementInterfaceId());
                }
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for WebDAV Data Movement Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for WebDAV Data Movement Resource.");
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return webDavDataMovementResourceIDs;
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            WebDavDataMovement existingWebDavDataMovement = em.find(WebDavDataMovement.class, dataMovementInterfaceId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            WebDavDataMovement webDavDataMovement;
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingWebDavDataMovement == null) {
                webDavDataMovement = new WebDavDataMovement();
                webDavDataMovement.setCreationTime(AiravataUtils.getCurrentTimestamp());
            } else {
                webDavDataMovement = existingWebDavDataMovement;
                webDavDataMovement.setUpdateTime(AiravataUtils.getCurrentTimestamp());
            }
            webDavDataMovement.setQueueDescription(getQueueDescription());
            webDavDataMovement.setDataMovementInterfaceId(getDataMovementInterfaceId());
            webDavDataMovement.setSecurityProtocol(getSecurityProtocol());
            webDavDataMovement.setWebDavHostname(getWebDavHostname());
            webDavDataMovement.setPort(getPort());
            if (existingWebDavDataMovement == null) {
                em.persist(webDavDataMovement);
            } else {
                em.merge(webDavDataMovement);
            }
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
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
    public boolean isExists(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            WebDavDataMovement webDavDataMovement = em.find(WebDavDataMovement.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return webDavDataMovement != null;
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new AppCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public String getQueueDescription() {
        return queueDescription;
    }

    public String getDataMovementInterfaceId() {
        return dataMovementInterfaceId;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public String getWebDavHostname() {
        return webDavHostname;
    }

    public int getPort() {
        return port;
    }

    public void setQueueDescription(String queueDescription) {
        this.queueDescription=queueDescription;
    }

    public void setDataMovementInterfaceId(String dataMovementInterfaceId) {
        this.dataMovementInterfaceId=dataMovementInterfaceId;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol=securityProtocol;
    }

    public void setWebDavHostname(String webDavHostname) {
        this.webDavHostname = webDavHostname;
    }

    public void setPort(int port) {
        this.port = port;
    }
}