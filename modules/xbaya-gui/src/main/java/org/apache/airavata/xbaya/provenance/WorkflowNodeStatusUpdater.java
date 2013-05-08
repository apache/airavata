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
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowNodeStatusUpdater {
       private static Logger logger = LoggerFactory.getLogger(WorkflowNodeStatusUpdater.class);

    private AiravataAPI airavataAPI;

    public WorkflowNodeStatusUpdater(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }

    public boolean workflowNodeStarted(String workflowInstanceID,String nodeID,String inputs,String workflowID){
        try {
            //todo we currently save only service nodes
            WorkflowNodeType workflowNodeType = new WorkflowNodeType();
            workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.SERVICENODE);
            WorkflowInstanceNode node = new WorkflowInstanceNode(new WorkflowExecution(workflowInstanceID,workflowInstanceID), nodeID);
			airavataAPI.getProvenanceManager().setWorkflowInstanceNodeInput(node, inputs);
            airavataAPI.getProvenanceManager().setWorkflowNodeType(node, workflowNodeType);
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowExecutionStatus.State.STARTED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowNodeFailed(String workflowInstanceID,String nodeID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowExecutionStatus.State.FAILED);
            airavataAPI.getProvenanceManager().setWorkflowInstanceStatus(workflowInstanceID, workflowInstanceID, WorkflowExecutionStatus.State.FAILED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowNodeFinished(String workflowInstanceID,String nodeID,String inputs,String workflowID){
        try {
        	WorkflowNodeType workflowNodeType = new WorkflowNodeType();
            workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.SERVICENODE);
            WorkflowInstanceNode node = new WorkflowInstanceNode(new WorkflowExecution(workflowInstanceID,workflowInstanceID), nodeID);
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeOutput(node, inputs);
            airavataAPI.getProvenanceManager().setWorkflowNodeType(node,workflowNodeType);
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowExecutionStatus.State.FINISHED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowNodeRunning(String workflowInstanceID,String nodeID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowExecutionStatus.State.RUNNING);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

     public boolean workflowNodePaused(String workflowInstanceID,String nodeID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowExecutionStatus.State.PAUSED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowNodeStatusPending(String workflowInstanceID,String nodeID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowExecutionStatus.State.PENDING);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

       public boolean workflowNodeStatusActive(String workflowInstanceID,String nodeID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowExecutionStatus.State.ACTIVE);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

       public boolean workflowNodeStatusDone(String workflowInstanceID,String nodeID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceNodeStatus(workflowInstanceID, workflowInstanceID, nodeID, WorkflowExecutionStatus.State.DONE);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }


}
