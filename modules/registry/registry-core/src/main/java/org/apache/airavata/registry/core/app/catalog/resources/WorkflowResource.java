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

package org.apache.airavata.registry.core.app.catalog.resources;

import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.registry.core.app.catalog.model.Workflow;
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

public class WorkflowResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowResource.class);
    private String wfName;
    private String createdUser;
    private String graph;
    private String wfTemplateId;
    private Timestamp createdTime;
    private Timestamp updatedTime;
    private String image;
    private String gatewayId;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
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
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW);
            generator.setParameter(WorkflowConstants.WF_TEMPLATE_ID, identifier);
            Query q = generator.selectQuery(em);
            Workflow workflow = (Workflow) q.getSingleResult();
            WorkflowResource workflowResource = (WorkflowResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.WORKFLOW, workflow);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
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
    public List<AppCatalogResource> get(String fieldName, Object value) throws AppCatalogException {
        List<AppCatalogResource> workflowResources = new ArrayList<AppCatalogResource>();
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
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for Workflow Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Workflow Resource.");
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
        return workflowResources;
    }

    @Override
    public List<AppCatalogResource> getAll() throws AppCatalogException {
        List<AppCatalogResource> workflows = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW);
            generator.setParameter(WorkflowConstants.GATEWAY_ID, gatewayId);
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
            generator.setParameter(WorkflowConstants.GATEWAY_ID, gatewayId);
            Query q = generator.selectQuery(em);
            List results = q.getResultList();
            if (results.size() != 0) {
                for (Object result : results) {
                    Workflow workflow = (Workflow) result;
                    workflowIds.add(workflow.getWfTemplateId());
                }
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
                if (em.isOpen()) {
                    if (em.getTransaction().isActive()){
                        em.getTransaction().rollback();
                    }
                    em.close();
                }
                logger.error("Unsupported field name for Workflow Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Workflow Resource.");
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
        return workflowResourceIDs;
    }

    @Override
    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            Workflow existingWorkflow = em.find(Workflow.class, wfTemplateId);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            Workflow workflow;
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingWorkflow == null) {
                workflow = new Workflow();
                workflow.setCreationTime(AiravataUtils.getCurrentTimestamp());
            } else {
                workflow = existingWorkflow;
                workflow.setUpdateTime(AiravataUtils.getCurrentTimestamp());
            }
            workflow.setWfName(getWfName());
            workflow.setCreatedUser(getCreatedUser());
            workflow.setGatewayId(gatewayId);
            if (getGraph() != null){
                workflow.setGraph(getGraph().toCharArray());
            }
            if (image != null){
                workflow.setImage(image.getBytes());
            }
            workflow.setWfTemplateId(getWfTemplateId());
            if (existingWorkflow == null) {
                em.persist(workflow);
            } else {
                em.merge(workflow);
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
            Workflow workflow = em.find(Workflow.class, identifier);
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
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
