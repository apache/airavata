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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeInputResource extends AbstractResource {
	private static final Logger logger = LoggerFactory.getLogger(NodeInputResource.class);

    private WorkflowNodeDetailResource nodeDetailResource;
    private String inputKey;
    private String inputType;
    private String metadata;
    private String value;

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

    public String getInputType() {
        return inputType;
    }

    public void setInputType(String inputType) {
        this.inputType = inputType;
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

    @Override
    public Resource create(ResourceType type) {
        logger.error("Unsupported resource type for node input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for node input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for node input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Resource> get(ResourceType type) {
        logger.error("Unsupported resource type for node input data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    @Override
    public void save() {
        EntityManager em = ResourceUtils.getEntityManager();
        NodeInput existingInput = em.find(NodeInput.class, new NodeInput_PK(inputKey, nodeDetailResource.getNodeInstanceId()));
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        NodeInput nodeInput = new NodeInput();
        WorkflowNodeDetail nodeDetail = em.find(WorkflowNodeDetail.class, nodeDetailResource.getNodeInstanceId());
        nodeInput.setNodeDetails(nodeDetail);
        nodeInput.setNodeId(nodeDetail.getNodeId());
        nodeInput.setInputKey(inputKey);
        nodeInput.setInputKeyType(inputType);
        nodeInput.setValue(value);
        nodeInput.setMetadata(metadata);
        
        if (existingInput != null){
            existingInput.setNodeDetails(nodeDetail);
            existingInput.setNodeId(nodeDetail.getNodeId());
            existingInput.setInputKey(inputKey);
            existingInput.setInputKeyType(inputType);
            existingInput.setValue(value);
            existingInput.setMetadata(metadata);
            nodeInput = em.merge(existingInput);
        }else {
            em.persist(nodeInput);
        }
        em.getTransaction().commit();
        em.close();
    }
}
