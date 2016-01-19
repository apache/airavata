/**
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
import org.apache.airavata.registry.core.app.catalog.model.Workflow;
import org.apache.airavata.registry.core.app.catalog.model.WorkflowOutput;
import org.apache.airavata.registry.core.app.catalog.model.WorkflowOutput_PK;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogJPAUtils;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogQueryGenerator;
import org.apache.airavata.registry.core.app.catalog.util.AppCatalogResourceType;
import org.apache.airavata.registry.cpi.AppCatalogException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowOutputResource extends AppCatAbstractResource {
    private final static Logger logger = LoggerFactory.getLogger(WorkflowOutputResource.class);

    private String wfTemplateId;
    private String outputKey;
    private String outputVal;
    private String dataType;
    private String validityType;
    private boolean dataMovement;
    private String dataNameLocation;

    private WorkflowResource workflowResource;

    public void remove(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW_OUTPUT);
            generator.setParameter(WFOutputConstants.WF_TEMPLATE_ID, ids.get(WFOutputConstants.WF_TEMPLATE_ID));
            generator.setParameter(WFOutputConstants.OUTPUT_KEY, ids.get(WFOutputConstants.OUTPUT_KEY));
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

    public AppCatalogResource get(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW_OUTPUT);
            generator.setParameter(WFOutputConstants.WF_TEMPLATE_ID, ids.get(WFOutputConstants.WF_TEMPLATE_ID));
            generator.setParameter(WFOutputConstants.OUTPUT_KEY, ids.get(WFOutputConstants.OUTPUT_KEY));
            Query q = generator.selectQuery(em);
            WorkflowOutput wfOutput = (WorkflowOutput) q.getSingleResult();
            WorkflowOutputResource workflowOutputResource =
                    (WorkflowOutputResource) AppCatalogJPAUtils.getResource(AppCatalogResourceType.WORKFLOW_OUTPUT
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

    public List<AppCatalogResource> get(String fieldName, Object value) throws AppCatalogException {
        List<AppCatalogResource> wfOutputResources = new ArrayList<AppCatalogResource>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW_OUTPUT);
            List results;
            if (fieldName.equals(WFOutputConstants.WF_TEMPLATE_ID)) {
                generator.setParameter(WFOutputConstants.WF_TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput wfOutput = (WorkflowOutput) result;
                        WorkflowOutputResource workflowOutputResource =
                                (WorkflowOutputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.WORKFLOW_OUTPUT, wfOutput);
                        wfOutputResources.add(workflowOutputResource);
                    }
                }
            } else if (fieldName.equals(WFOutputConstants.OUTPUT_KEY)) {
                generator.setParameter(WFOutputConstants.OUTPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput workflowOutput = (WorkflowOutput) result;
                        WorkflowOutputResource workflowOutputResource =
                                (WorkflowOutputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.WORKFLOW_OUTPUT, workflowOutput);
                        wfOutputResources.add(workflowOutputResource);
                    }
                }
            } else if (fieldName.equals(WFOutputConstants.DATA_TYPE)) {
                generator.setParameter(WFOutputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput workflowOutput = (WorkflowOutput) result;
                        WorkflowOutputResource workflowOutputResource =
                                (WorkflowOutputResource) AppCatalogJPAUtils.getResource(
                                        AppCatalogResourceType.WORKFLOW_OUTPUT, workflowOutput);
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
            throw new AppCatalogException(e);
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

    public List<AppCatalogResource> getAll() throws AppCatalogException {
        return null;
    }

    public List<String> getAllIds() throws AppCatalogException {
        return null;
    }

    public List<String> getIds(String fieldName, Object value) throws AppCatalogException {
        List<String> wfOutputResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            AppCatalogQueryGenerator generator = new AppCatalogQueryGenerator(WORKFLOW_OUTPUT);
            List results;
            if (fieldName.equals(WFOutputConstants.WF_TEMPLATE_ID)) {
                generator.setParameter(WFOutputConstants.WF_TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput workflowOutput = (WorkflowOutput) result;
                        wfOutputResourceIDs.add(workflowOutput.getWfTemplateId());
                    }
                }
            }
            if (fieldName.equals(WFOutputConstants.OUTPUT_KEY)) {
                generator.setParameter(WFOutputConstants.OUTPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput workflowOutput = (WorkflowOutput) result;
                        wfOutputResourceIDs.add(workflowOutput.getWfTemplateId());
                    }
                }
            } else if (fieldName.equals(WFOutputConstants.DATA_TYPE)) {
                generator.setParameter(WFOutputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowOutput workflowOutput = (WorkflowOutput) result;
                        wfOutputResourceIDs.add(workflowOutput.getWfTemplateId());
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
            throw new AppCatalogException(e);
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

    public void save() throws AppCatalogException {
        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            WorkflowOutput existingWorkflowOutput = em.find(WorkflowOutput.class,
                    new WorkflowOutput_PK(wfTemplateId, outputKey));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }

            em = AppCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingWorkflowOutput != null) {
                existingWorkflowOutput.setWfTemplateId(wfTemplateId);
                Workflow workflow = em.find(Workflow.class, wfTemplateId);
                existingWorkflowOutput.setWorkflow(workflow);
                existingWorkflowOutput.setDataType(dataType);
                existingWorkflowOutput.setOutputKey(outputKey);
                if (outputVal != null){
                    existingWorkflowOutput.setOutputVal(outputVal.toCharArray());
                }
                existingWorkflowOutput.setValidityType(validityType);
                existingWorkflowOutput.setDataMovement(dataMovement);
                existingWorkflowOutput.setDataNameLocation(dataNameLocation);
                em.merge(existingWorkflowOutput);
            } else {
                WorkflowOutput workflowOutput = new WorkflowOutput();
                workflowOutput.setWfTemplateId(wfTemplateId);
                Workflow workflow = em.find(Workflow.class, wfTemplateId);
                workflowOutput.setWorkflow(workflow);
                workflowOutput.setDataType(dataType);
                workflowOutput.setOutputKey(outputKey);
                if (outputVal != null){
                    workflowOutput.setOutputVal(outputVal.toCharArray());
                }
                workflowOutput.setValidityType(validityType);
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

    public boolean isExists(Object identifier) throws AppCatalogException {
        HashMap<String, String> ids;
        if (identifier instanceof Map) {
            ids = (HashMap) identifier;
        } else {
            logger.error("Identifier should be a map with the field name and it's value");
            throw new AppCatalogException("Identifier should be a map with the field name and it's value");
        }

        EntityManager em = null;
        try {
            em = AppCatalogJPAUtils.getEntityManager();
            WorkflowOutput workflowOutput = em.find(WorkflowOutput.class, new WorkflowOutput_PK(
                    ids.get(WFOutputConstants.WF_TEMPLATE_ID),
                    ids.get(WFOutputConstants.OUTPUT_KEY)));

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return workflowOutput != null;
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

    public String getValidityType() {
        return validityType;
    }

    public void setValidityType(String validityType) {
        this.validityType = validityType;
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
}
