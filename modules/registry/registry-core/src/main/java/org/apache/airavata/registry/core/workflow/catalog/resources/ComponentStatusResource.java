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
package org.apache.airavata.registry.core.workflow.catalog.resources;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.core.workflow.catalog.model.ComponentStatus;
import org.apache.airavata.registry.core.workflow.catalog.model.Workflow;
import org.apache.airavata.registry.core.workflow.catalog.utils.WorkflowCatalogJPAUtils;
import org.apache.airavata.registry.core.workflow.catalog.utils.WorkflowCatalogQueryGenerator;
import org.apache.airavata.registry.core.workflow.catalog.utils.WorkflowCatalogResourceType;
import org.apache.airavata.registry.cpi.WorkflowCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ComponentStatusResource extends WorkflowCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(ComponentStatusResource.class);

    private String statusId;
    private String state;
    private String reason;
    private String templateId;
    private Timestamp updatedTime;

    public void remove(Object identifier) throws WorkflowCatalogException {
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(COMPONENT_STATUS);
            generator.setParameter(ComponentStatusConstants.STATUS_ID, identifier.toString());
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
            throw new WorkflowCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public WorkflowCatalogResource get(Object identifier) throws WorkflowCatalogException {
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(COMPONENT_STATUS);
            generator.setParameter(ComponentStatusConstants.STATUS_ID, identifier.toString());
            Query q = generator.selectQuery(em);
            ComponentStatus status = (ComponentStatus) q.getSingleResult();
            ComponentStatusResource statusResource =
                    (ComponentStatusResource) WorkflowCatalogJPAUtils.getResource(WorkflowCatalogResourceType.COMPONENT_STATUS
                            , status);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return statusResource;
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new WorkflowCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public List<WorkflowCatalogResource> get(String fieldName, Object value) throws WorkflowCatalogException {
        List<WorkflowCatalogResource> statusResources = new ArrayList<WorkflowCatalogResource>();
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(COMPONENT_STATUS);
            List results;
            if (fieldName.equals(ComponentStatusConstants.TEMPLATE_ID)) {
                generator.setParameter(ComponentStatusConstants.TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ComponentStatus componentStatus = (ComponentStatus) result;
                        ComponentStatusResource statusResource =
                                (ComponentStatusResource) WorkflowCatalogJPAUtils.getResource(
                                        WorkflowCatalogResourceType.COMPONENT_STATUS, componentStatus);
                        statusResources.add(statusResource);
                    }
                }
            }else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for Component status Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Component status Resource.");
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
            throw new WorkflowCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return statusResources;
    }

    public List<WorkflowCatalogResource> getAll() throws WorkflowCatalogException {
        return null;
    }

    public List<String> getAllIds() throws WorkflowCatalogException {
        return null;
    }

    public List<String> getIds(String fieldName, Object value) throws WorkflowCatalogException {
        List<String> statusResourceIds = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(COMPONENT_STATUS);
            List results;
            if (fieldName.equals(ComponentStatusConstants.TEMPLATE_ID)) {
                generator.setParameter(ComponentStatusConstants.TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        ComponentStatus componentStatus = (ComponentStatus) result;
                        statusResourceIds.add(componentStatus.getTemplateId());
                    }
                }
            } else {
                em.getTransaction().commit();
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for Component Status resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Component Status Resource.");
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
            throw new WorkflowCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
        return statusResourceIds;
    }

    public void save() throws WorkflowCatalogException {
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            ComponentStatus existingStatus = em.find(ComponentStatus.class,statusId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingStatus != null) {
                existingStatus.setTemplateId(templateId);
                Workflow workflow = em.find(Workflow.class, templateId);
                existingStatus.setWorkflow(workflow);
                existingStatus.setReason(reason);
                existingStatus.setState(state);
                existingStatus.setUpdateTime(AiravataUtils.getCurrentTimestamp());
                em.merge(existingStatus);
            } else {
                ComponentStatus status = new ComponentStatus();
                status.setTemplateId(templateId);
                Workflow workflow = em.find(Workflow.class, templateId);
                status.setWorkflow(workflow);
                status.setReason(reason);
                status.setState(state);
                status.setUpdateTime(AiravataUtils.getCurrentTimestamp());
                em.persist(status);
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
            throw new WorkflowCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public boolean isExists(Object identifier) throws WorkflowCatalogException {
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            ComponentStatus status = em.find(ComponentStatus.class, identifier.toString());

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return status != null;
        } catch (ApplicationSettingsException e) {
            logger.error(e.getMessage(), e);
            throw new WorkflowCatalogException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()) {
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Timestamp getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Timestamp updatedTime) {
        this.updatedTime = updatedTime;
    }
}
