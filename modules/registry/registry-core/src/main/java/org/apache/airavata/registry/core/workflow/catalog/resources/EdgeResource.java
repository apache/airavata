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
import org.apache.airavata.registry.core.workflow.catalog.model.Edge;
import org.apache.airavata.registry.core.workflow.catalog.model.Edge_PK;
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

public class EdgeResource extends WorkflowCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(EdgeResource.class);

    private String edgeId;
    private String name;
    private String statusId;
    private String templateId;
    private Timestamp createdTime;
    private String description;

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
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(EDGE);
            generator.setParameter(EdgeConstants.EDGE_ID, ids.get(EdgeConstants.EDGE_ID));
            generator.setParameter(EdgeConstants.TEMPLATE_ID, ids.get(EdgeConstants.TEMPLATE_ID));
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
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(EDGE);
            generator.setParameter(EdgeConstants.EDGE_ID, ids.get(EdgeConstants.EDGE_ID));
            generator.setParameter(EdgeConstants.TEMPLATE_ID, ids.get(EdgeConstants.TEMPLATE_ID));
            Query q = generator.selectQuery(em);
            Edge edge = (Edge) q.getSingleResult();
            EdgeResource edgeResource =
                    (EdgeResource) WorkflowCatalogJPAUtils.getResource(WorkflowCatalogResourceType.EDGE
                            , edge);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return edgeResource;
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
        List<WorkflowCatalogResource> edgeResources = new ArrayList<WorkflowCatalogResource>();
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(EDGE);
            List results;
            if (fieldName.equals(EdgeConstants.TEMPLATE_ID)) {
                generator.setParameter(EdgeConstants.TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Edge Edge = (Edge) result;
                        EdgeResource edgeResource =
                                (EdgeResource) WorkflowCatalogJPAUtils.getResource(
                                        WorkflowCatalogResourceType.EDGE, Edge);
                        edgeResources.add(edgeResource);
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
                logger.error("Unsupported field name for Edge Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Edge Resource.");
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
        return edgeResources;
    }

    public List<WorkflowCatalogResource> getAll() throws WorkflowCatalogException {
        return null;
    }

    public List<String> getAllIds() throws WorkflowCatalogException {
        return null;
    }

    public List<String> getIds(String fieldName, Object value) throws WorkflowCatalogException {
        List<String> edgeResourceIds = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(EDGE);
            List results;
            if (fieldName.equals(EdgeConstants.TEMPLATE_ID)) {
                generator.setParameter(EdgeConstants.TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Edge edge = (Edge) result;
                        edgeResourceIds.add(edge.getTemplateId());
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
                logger.error("Unsupported field name for Workflow Edge resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for Workflow Edge Resource.");
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
        return edgeResourceIds;
    }

    public void save() throws WorkflowCatalogException {
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            Edge existingEdge = em.find(Edge.class,new Edge_PK(templateId, edgeId));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingEdge != null) {
                existingEdge.setTemplateId(templateId);
                Workflow workflow = em.find(Workflow.class, templateId);
                existingEdge.setWorkflow(workflow);
                existingEdge.setComponentStatusId(statusId);
                existingEdge.setDescription(description);
                existingEdge.setName(name);
                existingEdge.setCreatedTime(createdTime);
                em.merge(existingEdge);
            } else {
                Edge edge = new Edge();
                edge.setTemplateId(templateId);
                Workflow workflow = em.find(Workflow.class, templateId);
                edge.setWorkflow(workflow);
                edge.setComponentStatusId(statusId);
                edge.setDescription(description);
                edge.setName(name);
                edge.setCreatedTime(AiravataUtils.getCurrentTimestamp());
                em.persist(edge);
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
            ids = (HashMap<String, String>) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new WorkflowCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            Edge edge = em.find(Edge.class, new Edge_PK(ids.get(EdgeConstants.TEMPLATE_ID), ids.get(EdgeConstants.EDGE_ID)));

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return edge != null;
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

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public String getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(String edgeId) {
        this.edgeId = edgeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
