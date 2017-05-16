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
import org.apache.airavata.registry.core.workflow.catalog.model.Workflow;
import org.apache.airavata.registry.core.workflow.catalog.model.WorkflowStatus;
import org.apache.airavata.registry.core.workflow.catalog.model.WorkflowStatus_PK;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowStatusResource extends WorkflowCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowStatusResource.class);

    private String statusId;
    private String state;
    private String reason;
    private String templateId;
    private Timestamp updatedTime;

    public void remove(Object identifier) throws WorkflowCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new WorkflowCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_STATUS);
            generator.setParameter(WorkflowStatusConstants.STATUS_ID, ids.get(WorkflowStatusConstants.STATUS_ID));
            generator.setParameter(WorkflowStatusConstants.TEMPLATE_ID, ids.get(WorkflowStatusConstants.TEMPLATE_ID));
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
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap<String, String>) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new WorkflowCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_STATUS);
            generator.setParameter(WorkflowStatusConstants.STATUS_ID, ids.get(WorkflowStatusConstants.STATUS_ID));
            generator.setParameter(WorkflowStatusConstants.TEMPLATE_ID, ids.get(WorkflowStatusConstants.TEMPLATE_ID));
            Query q = generator.selectQuery(em);
            WorkflowStatus status = (WorkflowStatus) q.getSingleResult();
            WorkflowStatusResource statusResource =
                    (WorkflowStatusResource) WorkflowCatalogJPAUtils.getResource(WorkflowCatalogResourceType.WORKFLOW_STATUS
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
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_STATUS);
            List results;
            if (fieldName.equals(WorkflowStatusConstants.TEMPLATE_ID)) {
                generator.setParameter(WorkflowStatusConstants.TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowStatus WorkflowStatus = (WorkflowStatus) result;
                        WorkflowStatusResource statusResource =
                                (WorkflowStatusResource) WorkflowCatalogJPAUtils.getResource(
                                        WorkflowCatalogResourceType.WORKFLOW_STATUS, WorkflowStatus);
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
                logger.error("Unsupported field name for Workflow status Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Workflow status Resource.");
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
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_STATUS);
            List results;
            if (fieldName.equals(WorkflowStatusConstants.TEMPLATE_ID)) {
                generator.setParameter(WorkflowStatusConstants.TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowStatus WorkflowStatus = (WorkflowStatus) result;
                        statusResourceIds.add(WorkflowStatus.getTemplateId());
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
                logger.error("Unsupported field name for Workflow Status resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Workflow Status Resource.");
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
            WorkflowStatus existingStatus = em.find(WorkflowStatus.class,new WorkflowStatus_PK(templateId, statusId));
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
                WorkflowStatus status = new WorkflowStatus();
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
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new WorkflowCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            WorkflowStatus status = em.find(WorkflowStatus.class, new WorkflowStatus_PK(ids.get(WorkflowStatusConstants.TEMPLATE_ID),ids.get(WorkflowStatusConstants.STATUS_ID)));

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
