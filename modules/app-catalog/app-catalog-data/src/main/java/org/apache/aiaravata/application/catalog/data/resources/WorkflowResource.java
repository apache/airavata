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

package org.apache.aiaravata.application.catalog.data.resources;

import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.model.Workflow;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogJPAUtils;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogQueryGenerator;
import org.apache.aiaravata.application.catalog.data.util.AppCatalogResourceType;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class WorkflowResource extends AbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowResource.class);
    private String wfName;
    private String createdUser;
    private String graph;
    private String wfTemplateId;
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW);
            generator.setParameter(WorkflowConstants.WF_TEMPLATE_ID, identifier);
            Query q = generator.deleteQuery(em);
            q.executeUpdate();
            em.getTransaction().commit();
            em.close();
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
    public Resource get(Object identifier) throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW);
            generator.setParameter(WorkflowConstants.WF_TEMPLATE_ID, identifier);
            Query q = generator.selectQuery(em);
            Workflow workflow = (Workflow) q.getSingleResult();
            WorkflowResource workflowResource = (WorkflowResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.WORKFLOW, workflow);
            em.getTransaction().commit();
            em.close();
            return workflowResource;
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
    public List<Resource> get(String fieldName, Object value) throws AppCatalogException {
        List<Resource> workflowResources = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW);
            Query q;
            if ((fieldName.equals(WorkflowConstants.WF_NAME)) || (fieldName.equals(WorkflowConstants.CREATED_USER)) || (fieldName.equals(WorkflowConstants.GRAPH)) || (fieldName.equals(WorkflowConstants.WF_TEMPLATE_ID))) {
                generator.setParameter(fieldName, value);
                q = generator.selectQuery(em);
                List<?> results = q.getResultList();
                for (Object result : results) {
                    Workflow workflow = (Workflow) result;
                    WorkflowResource workflowResource = (WorkflowResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.WORKFLOW, workflow);
                    workflowResources.add(workflowResource);
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Workflow Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Workflow Resource.");
            }
            em.getTransaction().commit();
            em.close();
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
        return workflowResources;
    }

    @Override
    public List<Resource> getAll() throws AppCatalogException {
        List<Resource> workflows = new ArrayList<Resource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW);
            Query q = generator.selectQuery(em);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Workflow workflow = (Workflow) result;
                    WorkflowResource wfResource =
                            (WorkflowResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.WORKFLOW, workflow);
                    workflows.add(wfResource);
                }
            }
            em.getTransaction().commit();
            em.close();
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
        return workflows;
    }

    @Override
    public List<String> getAllIds() throws AppCatalogException {
        List<String> workflowIds = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW);
            Query q = generator.selectQuery(em);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Workflow workflow = (Workflow) result;
                    workflowIds.add(workflow.getWfTemplateId());
                }
            }
            em.getTransaction().commit();
            em.close();
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
        return workflowIds;
    }

    @Override
    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> workflowResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW);
            Query q;
            if ((fieldName.equals(WorkflowConstants.WF_NAME)) || (fieldName.equals(WorkflowConstants.CREATED_USER)) || (fieldName.equals(WorkflowConstants.GRAPH)) || (fieldName.equals(WorkflowConstants.WF_TEMPLATE_ID))) {
                generator.setParameter(fieldName, value);
                q = generator.selectQuery(em);
                List<?> results = q.getResultList();
                for (Object result : results) {
                    Workflow workflow = (Workflow) result;
                    WorkflowResource workflowResource = (WorkflowResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.WORKFLOW, workflow);
                    workflowResourceIDs.add(workflowResource.getWfTemplateId());
                }
            } else {
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported field name for Workflow Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Workflow Resource.");
            }
            em.getTransaction().commit();
            em.close();
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
        return workflowResourceIDs;
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            Workflow existingWorkflow = em.find(Workflow.class, wfTemplateId);
            em.close();
            Workflow workflow;
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingWorkflow == null) {
                workflow = new Workflow();
                workflow.setCreatedUser(createdUser);
            } else {
                workflow = existingWorkflow;
                workflow.setUpdateTime(updatedTime);
            }
            workflow.setWfName(getWfName());
            workflow.setCreatedUser(getCreatedUser());
            if (getGraph() != null){
                workflow.setGraph(getGraph().toCharArray());
            }
            workflow.setWfTemplateId(getWfTemplateId());
            if (existingWorkflow == null) {
                em.persist(workflow);
            } else {
                em.merge(workflow);
            }
            em.getTransaction().commit();
            em.close();
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
            Workflow workflow = em.find(Workflow.class, identifier);
            em.close();
            return workflow != null;
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

    public String getWfName() {
        return wfName;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public String getGraph() {
        return graph;
    }

    public String getWfTemplateId() {
        return wfTemplateId;
    }

    public void setWfName(String wfName) {
        this.wfName=wfName;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser=createdUser;
    }

    public void setGraph(String graph) {
        this.graph=graph;
    }

    public void setWfTemplateId(String wfTemplateId) {
        this.wfTemplateId=wfTemplateId;
    }
}
