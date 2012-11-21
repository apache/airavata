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
package org.apache.airavata.xbaya.provenance;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
//import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowNodeStatusUpdater {
       private static Logger logger = LoggerFactory.getLogger(WorkflowNodeStatusUpdater.class);

    private AiravataAPI airavataAPI;

    public WorkflowNodeStatusUpdater(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }

    public boolean workflowStarted(String workflowInstanceID,String nodeID,String inputs,String workflowID){
        try {
            //todo we currently save only service nodes
            WorkflowNodeType workflowNodeType = new WorkflowNodeType();
            workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.SERVICENODE);
            WorkflowInstanceNode node = new WorkflowInstanceNode(new WorkflowInstance(workflowInstanceID,workflowInstanceID), nodeID);
			airavataAPI.getProvenanceManager().setWorkflowInstanceNodeInput(node, inputs);
            airavataAPI.getProvenanceManager().setWorkflowNodeType(node, workflowNodeType);
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowInstanceStatus.ExecutionStatus.STARTED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowFailed(String workflowInstanceID,String nodeID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowInstanceStatus.ExecutionStatus.FAILED);
            airavataAPI.getProvenanceManager().setWorkflowInstanceStatus(workflowInstanceID, workflowInstanceID, WorkflowInstanceStatus.ExecutionStatus.FAILED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowFinished(String workflowInstanceID,String nodeID,String inputs,String workflowID){
        try {
        	WorkflowNodeType workflowNodeType = new WorkflowNodeType();
            workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.SERVICENODE);
            WorkflowInstanceNode node = new WorkflowInstanceNode(new WorkflowInstance(workflowInstanceID,workflowInstanceID), nodeID);
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeOutput(node, inputs);
            airavataAPI.getProvenanceManager().setWorkflowNodeType(node,workflowNodeType);
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowInstanceStatus.ExecutionStatus.FINISHED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowRunning(String workflowInstanceID,String nodeID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowInstanceStatus.ExecutionStatus.RUNNING);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

     public boolean workflowPaused(String workflowInstanceID,String nodeID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowInstanceStatus.ExecutionStatus.PAUSED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }
}
