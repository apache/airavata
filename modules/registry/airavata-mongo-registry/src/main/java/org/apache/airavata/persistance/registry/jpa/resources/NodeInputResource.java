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

package org.apache.airavata.persistance.registry.jpa.resources;

import java.util.List;

import javax.persistence.EntityManager;

import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.NodeInput;
import org.apache.airavata.persistance.registry.jpa.model.NodeInput_PK;
import org.apache.airavata.persistance.registry.jpa.model.WorkflowNodeDetail;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeInputResource extends AbstractResource {
	private static final Logger logger = LoggerFactory.getLogger(NodeInputResource.class);

    private WorkflowNodeDetailResource nodeDetailResource;
    private String inputKey;
    private String dataType;
    private String metadata;
    private String value;
    private String appArgument;
    private boolean standardInput;
    private String userFriendlyDesc;
    private int inputOrder;
    private boolean isRequired;
    private boolean requiredToCMD;
    private boolean dataStaged;

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

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }

    public boolean isStandardInput() {
        return standardInput;
    }

    public void setStandardInput(boolean standardInput) {
        this.standardInput = standardInput;
    }

    public String getUserFriendlyDesc() {
        return userFriendlyDesc;
    }

    public void setUserFriendlyDesc(String userFriendlyDesc) {
        this.userFriendlyDesc = userFriendlyDesc;
    }

    public WorkflowNodeDetailResource getNodeDetailResource() {
        return nodeDetailResource;
    }

    public void setNodeDetailResource(WorkflowNodeDetailResource nodeDetailResource) {
        this.nodeDetailResource = nodeDetailResource;
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

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(int inputOrder) {
        this.inputOrder = inputOrder;
    }

    public Resource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for node input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for node input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public Resource get(ResourceType type, Object name) throws RegistryException {
        logger.error("Unsupported resource type for node input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<Resource> get(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for node input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            NodeInput existingInput = em.find(NodeInput.class, new NodeInput_PK(inputKey, nodeDetailResource.getNodeInstanceId()));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            NodeInput nodeInput = new NodeInput();
            WorkflowNodeDetail nodeDetail = em.find(WorkflowNodeDetail.class, nodeDetailResource.getNodeInstanceId());
            nodeInput.setNodeDetails(nodeDetail);
            nodeInput.setNodeId(nodeDetail.getNodeId());
            nodeInput.setInputKey(inputKey);
            nodeInput.setDataType(dataType);
            nodeInput.setValue(value);
            nodeInput.setMetadata(metadata);
            nodeInput.setAppArgument(appArgument);
            nodeInput.setStandardInput(standardInput);
            nodeInput.setUserFriendlyDesc(userFriendlyDesc);
            nodeInput.setInputOrder(inputOrder);
            nodeInput.setRequiredToCMD(requiredToCMD);
            nodeInput.setIsRequired(isRequired);
            nodeInput.setDataStaged(dataStaged);

            if (existingInput != null){
                existingInput.setNodeDetails(nodeDetail);
                existingInput.setNodeId(nodeDetail.getNodeId());
                existingInput.setInputKey(inputKey);
                existingInput.setDataType(dataType);
                existingInput.setValue(value);
                existingInput.setMetadata(metadata);
                existingInput.setAppArgument(appArgument);
                existingInput.setStandardInput(standardInput);
                existingInput.setUserFriendlyDesc(userFriendlyDesc);
                existingInput.setInputOrder(inputOrder);
                existingInput.setRequiredToCMD(requiredToCMD);
                existingInput.setIsRequired(isRequired);
                existingInput.setDataStaged(dataStaged);
                nodeInput = em.merge(existingInput);
            }else {
                em.persist(nodeInput);
            }
            em.getTransaction().commit();
            em.close();
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        }finally {
            if (em != null && em.isOpen()){
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
}
