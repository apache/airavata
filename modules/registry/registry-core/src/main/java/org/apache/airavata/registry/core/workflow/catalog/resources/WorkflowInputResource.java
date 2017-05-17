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
import org.apache.airavata.registry.core.workflow.catalog.model.WorkflowInput;
import org.apache.airavata.registry.core.workflow.catalog.model.WorkflowInput_PK;
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

public class WorkflowInputResource extends WorkflowCatAbstractResource {

    private final static Logger logger = LoggerFactory.getLogger(WorkflowInputResource.class);

    private String wfTemplateId;
    private String inputKey;
    private String dataType;
    private String inputVal;
    private String metadata;
    private String appArgument;
    private String userFriendlyDesc;
    private boolean standardInput;
    private int inputOrder;
    private boolean isRequired;
    private boolean requiredToCMD;
    private boolean dataStaged;

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
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_INPUT);
            generator.setParameter(WorkflowInputConstants.WF_TEMPLATE_ID, ids.get(WorkflowInputConstants.WF_TEMPLATE_ID));
            generator.setParameter(WorkflowInputConstants.INPUT_KEY, ids.get(WorkflowInputConstants.INPUT_KEY));
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
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_INPUT);
            generator.setParameter(WorkflowInputConstants.WF_TEMPLATE_ID, ids.get(WorkflowInputConstants.WF_TEMPLATE_ID));
            generator.setParameter(WorkflowInputConstants.INPUT_KEY, ids.get(WorkflowInputConstants.INPUT_KEY));
            Query q = generator.selectQuery(em);
            WorkflowInput workflowInput = (WorkflowInput) q.getSingleResult();
            WorkflowInputResource workflowInputResource =
                    (WorkflowInputResource) WorkflowCatalogJPAUtils.getResource(WorkflowCatalogResourceType.WORKFLOW_INPUT
                            , workflowInput);
            em.getTransaction().commit();
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return workflowInputResource;
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
        List<WorkflowCatalogResource> wfInputResources = new ArrayList<WorkflowCatalogResource>();
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_INPUT);
            List results;
            if (fieldName.equals(WorkflowInputConstants.WF_TEMPLATE_ID)) {
                generator.setParameter(WorkflowInputConstants.WF_TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowInput workflowInput = (WorkflowInput) result;
                        WorkflowInputResource workflowInputResource =
                                (WorkflowInputResource) WorkflowCatalogJPAUtils.getResource(
                                        WorkflowCatalogResourceType.WORKFLOW_INPUT, workflowInput);
                        wfInputResources.add(workflowInputResource);
                    }
                }
            } else if (fieldName.equals(WorkflowInputConstants.INPUT_KEY)) {
                generator.setParameter(WorkflowInputConstants.INPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowInput workflowInput = (WorkflowInput) result;
                        WorkflowInputResource workflowInputResource =
                                (WorkflowInputResource) WorkflowCatalogJPAUtils.getResource(
                                        WorkflowCatalogResourceType.WORKFLOW_INPUT, workflowInput);
                        wfInputResources.add(workflowInputResource);
                    }
                }
            } else if (fieldName.equals(WorkflowInputConstants.DATA_TYPE)) {
                generator.setParameter(WorkflowInputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowInput workflowInput = (WorkflowInput) result;
                        WorkflowInputResource workflowInputResource =
                                (WorkflowInputResource) WorkflowCatalogJPAUtils.getResource(
                                        WorkflowCatalogResourceType.WORKFLOW_INPUT, workflowInput);
                        wfInputResources.add(workflowInputResource);
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
                logger.error("Unsupported field name for WFInput Resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for WFInput Resource.");
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
        return wfInputResources;
    }

    public List<WorkflowCatalogResource> getAll() throws WorkflowCatalogException {
        return null;
    }

    public List<String> getAllIds() throws WorkflowCatalogException {
        return null;
    }

    public List<String> getIds(String fieldName, Object value) throws WorkflowCatalogException {
        List<String> wfInputResourceIDs = new ArrayList<String>();
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            Query q;
            WorkflowCatalogQueryGenerator generator = new WorkflowCatalogQueryGenerator(WORKFLOW_INPUT);
            List results;
            if (fieldName.equals(WorkflowInputConstants.WF_TEMPLATE_ID)) {
                generator.setParameter(WorkflowInputConstants.WF_TEMPLATE_ID, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowInput workflowInput = (WorkflowInput) result;
                        wfInputResourceIDs.add(workflowInput.getTemplateID());
                    }
                }
            } else if (fieldName.equals(WorkflowInputConstants.INPUT_KEY)) {
                generator.setParameter(WorkflowInputConstants.INPUT_KEY, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowInput workflowInput = (WorkflowInput) result;
                        wfInputResourceIDs.add(workflowInput.getTemplateID());
                    }
                }
            } else if (fieldName.equals(WorkflowInputConstants.DATA_TYPE)) {
                generator.setParameter(WorkflowInputConstants.DATA_TYPE, value);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        WorkflowInput workflowInput = (WorkflowInput) result;
                        wfInputResourceIDs.add(workflowInput.getTemplateID());
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
                logger.error("Unsupported field name for WFInput resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported field name for WFInput Resource.");
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
        return wfInputResourceIDs;
    }

    public void save() throws WorkflowCatalogException {
        EntityManager em = null;
        try {
            em = WorkflowCatalogJPAUtils.getEntityManager();
            WorkflowInput existingWFInput = em.find(WorkflowInput.class, new WorkflowInput_PK(wfTemplateId, inputKey));
            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            WorkflowInput workflowInput;
            em = WorkflowCatalogJPAUtils.getEntityManager();
            em.getTransaction().begin();
            if (existingWFInput == null) {
                workflowInput = new WorkflowInput();
            } else {
            	workflowInput=existingWFInput;
            }
            workflowInput.setTemplateID(wfTemplateId);
            Workflow workflow = em.find(Workflow.class, wfTemplateId);
            workflowInput.setWorkflow(workflow);
            workflowInput.setDataType(dataType);
            workflowInput.setInputKey(inputKey);
            if (inputVal != null){
                workflowInput.setInputVal(inputVal.toCharArray());
            }
            workflowInput.setMetadata(metadata);
            workflowInput.setAppArgument(appArgument);
            workflowInput.setUserFriendlyDesc(userFriendlyDesc);
            workflowInput.setStandardInput(standardInput);
            workflowInput.setRequiredToCMD(requiredToCMD);
            workflowInput.setRequired(isRequired);
            workflowInput.setDataStaged(dataStaged);
            if (existingWFInput == null) {
                em.persist(workflowInput);
            } else {
                em.merge(workflowInput);
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
            WorkflowInput workflowInput = em.find(WorkflowInput.class, new WorkflowInput_PK(
                    ids.get(WorkflowInputConstants.WF_TEMPLATE_ID),
                    ids.get(WorkflowInputConstants.INPUT_KEY)));

            if (em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
            return workflowInput != null;
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

    public String getInputKey() {
        return inputKey;
    }

    public void setInputKey(String inputKey) {
        this.inputKey = inputKey;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getInputVal() {
        return inputVal;
    }

    public void setInputVal(String inputVal) {
        this.inputVal = inputVal;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }

    public String getUserFriendlyDesc() {
        return userFriendlyDesc;
    }

    public void setUserFriendlyDesc(String userFriendlyDesc) {
        this.userFriendlyDesc = userFriendlyDesc;
    }

    public WorkflowResource getWorkflowResource() {
        return workflowResource;
    }

    public void setWorkflowResource(WorkflowResource workflowResource) {
        this.workflowResource = workflowResource;
    }

    public boolean isStandardInput() {
        return standardInput;
    }

    public void setStandardInput(boolean standardInput) {
        this.standardInput = standardInput;
    }

    public int getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(int inputOrder) {
        this.inputOrder = inputOrder;
    }

    public boolean getRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        this.isRequired = required;
    }

    public boolean getRequiredToCMD() {
        return requiredToCMD;
    }

    public void setRequiredToCMD(boolean requiredToCMD) {
        this.requiredToCMD = requiredToCMD;
    }

    public boolean isDataStaged() {
        return dataStaged;
    }

    public void setDataStaged(boolean dataStaged) {
        this.dataStaged = dataStaged;
    }
}
