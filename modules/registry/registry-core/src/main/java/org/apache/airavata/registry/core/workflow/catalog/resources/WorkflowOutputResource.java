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
import org.apache.airavata.registry.core.workflow.catalog.model.Workflow;
import org.apache.airavata.registry.core.workflow.catalog.model.WorkflowOutput;
import org.apache.airavata.registry.core.workflow.catalog.model.WorkflowOutput_PK;
import org.apache.airavata.registry.core.workflow.catalog.utils.WorkflowCatalogJPAUtils;
import org.apache.airavata.registry.core.workflow.catalog.utils.WorkflowCatalogQueryGenerator;
import org.apache.airavata.registry.core.workflow.catalog.utils.WorkflowCatalogResourceType;
import org.apache.airavata.registry.cpi.WorkflowCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowOutputResource extends WorkflowCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowOutputResource.class);

    private String wfTemplateId;
    private String outputKey;
    private String outputVal;
    private String dataType;
    private boolean isRequired;
    private boolean dataMovement;
    private String dataNameLocation;
    private boolean requiredToCMD;
    private String searchQuery;
    private String appArgument;
    private boolean outputStreaming;

    private WorkflowResource workflowResource;

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
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_OUTPUT);
            generator.setParameter(WorkflowOutputConstants.WF_TEMPLATE_ID, ids.get(WorkflowOutputConstants.WF_TEMPLATE_ID));
            generator.setParameter(WorkflowOutputConstants.OUTPUT_KEY, ids.get(WorkflowOutputConstants.OUTPUT_KEY));
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
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new WorkflowCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_OUTPUT);
            generator.setParameter(WorkflowOutputConstants.WF_TEMPLATE_ID, ids.get(WorkflowOutputConstants.WF_TEMPLATE_ID));
            generator.setParameter(WorkflowOutputConstants.OUTPUT_KEY, ids.get(WorkflowOutputConstants.OUTPUT_KEY));
            Query q = generator.selectQuery(em);
            WorkflowOutput wfOutput = (WorkflowOutput) q.getSingleResult();
            WorkflowOutputResource workflowOutputResource =
                    (WorkflowOutputResource) WorkflowCatalogJPAUtils.getResource(WorkflowCatalogResourceType.WORKFLOW_OUTPUT
                            , wfOutput);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return workflowOutputResource;
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
        List<WorkflowCatalogResource> wfOutputResources = new ArrayList<WorkflowCatalogResource>();
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_OUTPUT);
            List results;
            if (fieldName.equals(WorkflowOutputConstants.WF_TEMPLATE_ID)) {
                generator.setParameter(WorkflowOutputConstants.WF_TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput wfOutput = (WorkflowOutput) result;
                        WorkflowOutputResource workflowOutputResource =
                                (WorkflowOutputResource) WorkflowCatalogJPAUtils.getResource(
                                        WorkflowCatalogResourceType.WORKFLOW_OUTPUT, wfOutput);
                        wfOutputResources.add(workflowOutputResource);
                    }
                }
            } else if (fieldName.equals(WorkflowOutputConstants.OUTPUT_KEY)) {
                generator.setParameter(WorkflowOutputConstants.OUTPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput workflowOutput = (WorkflowOutput) result;
                        WorkflowOutputResource workflowOutputResource =
                                (WorkflowOutputResource) WorkflowCatalogJPAUtils.getResource(
                                        WorkflowCatalogResourceType.WORKFLOW_OUTPUT, workflowOutput);
                        wfOutputResources.add(workflowOutputResource);
                    }
                }
            } else if (fieldName.equals(WorkflowOutputConstants.DATA_TYPE)) {
                generator.setParameter(WorkflowOutputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput workflowOutput = (WorkflowOutput) result;
                        WorkflowOutputResource workflowOutputResource =
                                (WorkflowOutputResource) WorkflowCatalogJPAUtils.getResource(
                                        WorkflowCatalogResourceType.WORKFLOW_OUTPUT, workflowOutput);
                        wfOutputResources.add(workflowOutputResource);
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
                logger.error("Unsupported field name for WF Output Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for WF Output Resource.");
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
        return wfOutputResources;
    }

    public List<WorkflowCatalogResource> getAll() throws WorkflowCatalogException {
        return null;
    }

    public List<String> getAllIds() throws WorkflowCatalogException {
        return null;
    }

    public List<String> getIds(String fieldName, Object value) throws WorkflowCatalogException {
        List<String> wfOutputResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_OUTPUT);
            List results;
            if (fieldName.equals(WorkflowOutputConstants.WF_TEMPLATE_ID)) {
                generator.setParameter(WorkflowOutputConstants.WF_TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput workflowOutput = (WorkflowOutput) result;
                        wfOutputResourceIDs.add(workflowOutput.getTemplateId());
                    }
                }
            }
            if (fieldName.equals(WorkflowOutputConstants.OUTPUT_KEY)) {
                generator.setParameter(WorkflowOutputConstants.OUTPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput workflowOutput = (WorkflowOutput) result;
                        wfOutputResourceIDs.add(workflowOutput.getTemplateId());
                    }
                }
            } else if (fieldName.equals(WorkflowOutputConstants.DATA_TYPE)) {
                generator.setParameter(WorkflowOutputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput workflowOutput = (WorkflowOutput) result;
                        wfOutputResourceIDs.add(workflowOutput.getTemplateId());
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
                logger.error("Unsupported field name for WF Output resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for WF Output Resource.");
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
        return wfOutputResourceIDs;
    }

    public void save() throws WorkflowCatalogException {
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            WorkflowOutput existingWorkflowOutput = em.find(WorkflowOutput.class,
                    new WorkflowOutput_PK(wfTemplateId, outputKey));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingWorkflowOutput != null) {
                existingWorkflowOutput.setTemplateId(wfTemplateId);
                Workflow workflow = em.find(Workflow.class, wfTemplateId);
                existingWorkflowOutput.setWorkflow(workflow);
                existingWorkflowOutput.setDataType(dataType);
                existingWorkflowOutput.setOutputKey(outputKey);
                if (outputVal != null){
                    existingWorkflowOutput.setOutputVal(outputVal.toCharArray());
                }
                existingWorkflowOutput.setDataMovement(dataMovement);
                existingWorkflowOutput.setDataNameLocation(dataNameLocation);
                em.merge(existingWorkflowOutput);
            } else {
                WorkflowOutput workflowOutput = new WorkflowOutput();
                workflowOutput.setTemplateId(wfTemplateId);
                Workflow workflow = em.find(Workflow.class, wfTemplateId);
                workflowOutput.setWorkflow(workflow);
                workflowOutput.setDataType(dataType);
                workflowOutput.setOutputKey(outputKey);
                if (outputVal != null){
                    workflowOutput.setOutputVal(outputVal.toCharArray());
                }
                workflowOutput.setDataMovement(dataMovement);
                workflowOutput.setDataNameLocation(dataNameLocation);
                em.persist(workflowOutput);
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
            WorkflowOutput workflowOutput = em.find(WorkflowOutput.class, new WorkflowOutput_PK(
                    ids.get(WorkflowOutputConstants.WF_TEMPLATE_ID),
                    ids.get(WorkflowOutputConstants.OUTPUT_KEY)));

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return workflowOutput != null;
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

    public String getWfTemplateId() {
        return wfTemplateId;
    }

    public void setWfTemplateId(String wfTemplateId) {
        this.wfTemplateId = wfTemplateId;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getOutputVal() {
        return outputVal;
    }

    public void setOutputVal(String outputVal) {
        this.outputVal = outputVal;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public WorkflowResource getWorkflowResource() {
        return workflowResource;
    }

    public void setWorkflowResource(WorkflowResource workflowResource) {
        this.workflowResource = workflowResource;
    }

    public boolean isDataMovement() {
        return dataMovement;
    }

    public void setDataMovement(boolean dataMovement) {
        this.dataMovement = dataMovement;
    }

    public String getDataNameLocation() {
        return dataNameLocation;
    }

    public void setDataNameLocation(String dataNameLocation) {
        this.dataNameLocation = dataNameLocation;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public boolean isRequiredToCMD() {
        return requiredToCMD;
    }

    public void setRequiredToCMD(boolean requiredToCMD) {
        this.requiredToCMD = requiredToCMD;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }

    public boolean isOutputStreaming() {
        return outputStreaming;
    }

    public void setOutputStreaming(boolean outputStreaming) {
        this.outputStreaming = outputStreaming;
    }
}
