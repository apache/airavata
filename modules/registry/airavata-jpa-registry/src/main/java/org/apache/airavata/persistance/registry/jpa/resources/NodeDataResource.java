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


import org.apache.airavata.persistance.registry.jpa.Resource;
import org.apache.airavata.persistance.registry.jpa.ResourceType;
import org.apache.airavata.persistance.registry.jpa.ResourceUtils;
import org.apache.airavata.persistance.registry.jpa.model.Node_Data;
import org.apache.airavata.persistance.registry.jpa.model.Node_DataPK;
import org.apache.airavata.persistance.registry.jpa.model.Node_Error;
import org.apache.airavata.persistance.registry.jpa.model.Workflow_Data;
import org.apache.airavata.persistance.registry.jpa.utils.QueryGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NodeDataResource extends AbstractResource{
    private final static Logger logger = LoggerFactory.getLogger(NodeDataResource.class);
    private WorkflowDataResource workflowDataResource;
    private String nodeID;
    private String nodeType;
    private String inputs;
    private String outputs;
    private String status;
    private Timestamp startTime;
    private Timestamp lastUpdateTime;
    private int executionIndex;

    public WorkflowDataResource getWorkflowDataResource() {
        return workflowDataResource;
    }

    public String getNodeID() {
        return nodeID;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getInputs() {
        return inputs;
    }

    public String getOutputs() {
        return outputs;
    }

    public String getStatus() {
        return status;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setWorkflowDataResource(WorkflowDataResource workflowDataResource) {
        this.workflowDataResource = workflowDataResource;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public void setInputs(String inputs) {
        this.inputs = inputs;
    }

    public void setOutputs(String outputs) {
        this.outputs = outputs;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Resource create(ResourceType type) {
        logger.error("Unsupported resource type for node data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public void remove(ResourceType type, Object name) {
        logger.error("Unsupported resource type for node data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public Resource get(ResourceType type, Object name) {
        logger.error("Unsupported resource type for node data resource.", new UnsupportedOperationException());
        throw new UnsupportedOperationException();
    }

    public List<Resource> get(ResourceType type) {
        List<Resource> resourceList = new ArrayList<Resource>();
        EntityManager em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Query q;
        QueryGenerator generator;
        List<?> results;
        switch (type){
            case  NODE_ERROR:
                generator = new QueryGenerator(NODE_ERROR);
                generator.setParameter(NodeErrorConstants.NODE_ID, nodeID);
                q = generator.selectQuery(em);
                results = q.getResultList();
                if (results.size() != 0) {
                    for (Object result : results) {
                        Node_Error nodeError = (Node_Error)result;
                        NodeErrorResource nodeErrorResource = (NodeErrorResource)Utils.getResource(ResourceType.NODE_ERROR, nodeError);
                        resourceList.add(nodeErrorResource);
                    }
                }
                break;
            default:
                em.getTransaction().commit();
                em.close();
                logger.error("Unsupported resource type for node data resource.", new IllegalArgumentException());
                throw new IllegalArgumentException("Unsupported resource type for node data resource.");
        }
        em.getTransaction().commit();
        em.close();
        return resourceList;
    }

    public void save() {
        if(lastUpdateTime == null){
            java.util.Date date= new java.util.Date();
            lastUpdateTime = new Timestamp(date.getTime());
        }
        EntityManager em = ResourceUtils.getEntityManager();
        Node_Data existingNodeData = em.find(Node_Data.class, new Node_DataPK(workflowDataResource.getWorkflowInstanceID(), nodeID, executionIndex));
        em.close();

        em = ResourceUtils.getEntityManager();
        em.getTransaction().begin();
        Node_Data nodeData = new Node_Data();
        nodeData.setNode_id(nodeID);
        Workflow_Data workflow_data = em.find(Workflow_Data.class, workflowDataResource.getWorkflowInstanceID());
        nodeData.setWorkflow_Data(workflow_data);
        byte[] inputsByte = null;
        if (inputs!=null) {
			inputsByte = inputs.getBytes();
			nodeData.setInputs(inputsByte);
		}
		byte[] outputsByte = null;
        if (outputs!=null) {
			outputsByte = outputs.getBytes();
			nodeData.setOutputs(outputsByte);
		}
		nodeData.setNode_type(nodeType);
        nodeData.setLast_update_time(lastUpdateTime);
        nodeData.setStart_time(startTime);
        nodeData.setStatus(status);
        nodeData.setExecution_index(executionIndex);
        if(existingNodeData != null){
            existingNodeData.setInputs(inputsByte);
            existingNodeData.setOutputs(outputsByte);
            existingNodeData.setLast_update_time(lastUpdateTime);
            existingNodeData.setNode_type(nodeType);
            existingNodeData.setStart_time(startTime);
            existingNodeData.setStatus(status);
            existingNodeData.setExecution_index(executionIndex);
            nodeData = em.merge(existingNodeData);
        }  else {
            em.persist(nodeData);
        }
        em.getTransaction().commit();
        em.close();
    }

    public int getExecutionIndex() {
        return executionIndex;
    }

    public void setExecutionIndex(int executionIndex) {
        this.executionIndex = executionIndex;
    }
}
