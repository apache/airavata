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
import org.apache.airavata.persistance.registry.jpa.model.NodeOutput;
import org.apache.airavata.persistance.registry.jpa.model.NodeOutput_PK;
import org.apache.airavata.persistance.registry.jpa.model.WorkflowNodeDetail;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeOutputResource extends AbstractResource {
	private static final Logger logger = LoggerFactory.getLogger(NodeOutputResource.class);
	
    private WorkflowNodeDetailResource nodeDetailResource;
    private String outputKey;
    private String dataType;
    private String value;
    private boolean isRequired;
    private boolean dataMovement;
    private String dataNameLocation;
    private boolean requiredToCMD;
    private String searchQuery;
    private String appArgument;

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


    public boolean getRequiredToCMD() {
        return requiredToCMD;
    }

    public void setRequiredToCMD(boolean requiredToCMD) {
        this.requiredToCMD = requiredToCMD;
    }

    public boolean getRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        this.isRequired = required;
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

    public WorkflowNodeDetailResource getNodeDetailResource() {
        return nodeDetailResource;
    }

    public void setNodeDetailResource(WorkflowNodeDetailResource nodeDetailResource) {
        this.nodeDetailResource = nodeDetailResource;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public void setOutputKey(String outputKey) {
        this.outputKey = outputKey;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    
    public Resource create(ResourceType type) throws RegistryException {
        logger.error("Unsupported resource type for node output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void remove(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for node output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public Resource get(ResourceType type, Object name) throws RegistryException{
        logger.error("Unsupported resource type for node output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public List<Resource> get(ResourceType type) throws RegistryException{
        logger.error("Unsupported resource type for node output data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    
    public void save() throws RegistryException{
        EntityManager em = null;
        try {
            em = ResourceUtils.getEntityManager();
            NodeOutput existingOutput = em.find(NodeOutput.class, new NodeOutput_PK(outputKey, nodeDetailResource.getNodeInstanceId()));
            em.close();

            em = ResourceUtils.getEntityManager();
            em.getTransaction().begin();
            NodeOutput nodeOutput = new NodeOutput();
            WorkflowNodeDetail nodeDetail = em.find(WorkflowNodeDetail.class, nodeDetailResource.getNodeInstanceId());
            nodeOutput.setNode(nodeDetail);
            nodeOutput.setNodeId(nodeDetail.getNodeId());
            nodeOutput.setOutputKey(outputKey);
            nodeOutput.setDataType(dataType);
            nodeOutput.setValue(value);
            nodeOutput.setRequired(isRequired);
            nodeOutput.setRequiredToCMD(requiredToCMD);
            nodeOutput.setDataMovement(dataMovement);
            nodeOutput.setDataNameLocation(dataNameLocation);
            nodeOutput.setApplicationArgument(appArgument);
            nodeOutput.setSearchQuery(searchQuery);

            if (existingOutput != null) {
                existingOutput.setNode(nodeDetail);
                existingOutput.setNodeId(nodeDetail.getNodeId());
                existingOutput.setOutputKey(outputKey);
                existingOutput.setDataType(dataType);
                existingOutput.setValue(value);
                existingOutput.setRequired(isRequired);
                existingOutput.setRequiredToCMD(requiredToCMD);
                existingOutput.setDataMovement(dataMovement);
                existingOutput.setDataNameLocation(dataNameLocation);
                existingOutput.setApplicationArgument(appArgument);
                existingOutput.setSearchQuery(searchQuery);
                nodeOutput = em.merge(existingOutput);
            } else {
                em.persist(nodeOutput);
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RegistryException(e);
        } finally {
            if (em != null && em.isOpen()) {
                if (em.getTransaction().isActive()){
                    em.getTransaction().rollback();
                }
                em.close();
            }
        }
    }
}
