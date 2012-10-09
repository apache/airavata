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

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceNode;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowNodeStatusUpdater {
       private static Logger logger = LoggerFactory.getLogger(WorkflowNodeStatusUpdater.class);

    private AiravataRegistry2 registry;

    public WorkflowNodeStatusUpdater(AiravataRegistry2 registry) {
        this.registry = registry;
    }

    public boolean workflowStarted(String workflowInstanceID,String nodeID,String inputs,String workflowID){
        try {
            //todo we currently save only service nodes
            WorkflowNodeType workflowNodeType = new WorkflowNodeType();
            workflowNodeType.setNodeType(WorkflowNodeType.WorkflowNode.SERVICENODE);
            WorkflowInstanceNode node = new WorkflowInstanceNode(new WorkflowInstance(workflowInstanceID,workflowInstanceID), nodeID);
			registry.updateWorkflowNodeInput(node,inputs);
            registry.updateWorkflowNodeType(node, workflowNodeType);
            registry.updateWorkflowNodeStatus(workflowInstanceID, nodeID, WorkflowInstanceStatus.ExecutionStatus.STARTED);
        } catch (RegistryException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowFailed(String workflowInstanceID,String nodeID){
        try {
            registry.updateWorkflowNodeStatus(workflowInstanceID, nodeID, WorkflowInstanceStatus.ExecutionStatus.FAILED);
            registry.updateWorkflowInstanceStatus(workflowInstanceID, WorkflowInstanceStatus.ExecutionStatus.FAILED);
        } catch (RegistryException e) {
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
            registry.updateWorkflowNodeOutput(node,inputs);
            registry.updateWorkflowNodeType(node,workflowNodeType);
            registry.updateWorkflowNodeStatus(workflowInstanceID, nodeID, WorkflowInstanceStatus.ExecutionStatus.FINISHED);
        } catch (RegistryException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowRunning(String workflowInstanceID,String nodeID){
        try {
            registry.updateWorkflowNodeStatus(workflowInstanceID, nodeID, WorkflowInstanceStatus.ExecutionStatus.RUNNING);
        } catch (RegistryException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

     public boolean workflowPaused(String workflowInstanceID,String nodeID){
        try {
            registry.updateWorkflowNodeStatus(workflowInstanceID, nodeID, WorkflowInstanceStatus.ExecutionStatus.PAUSED);
        } catch (RegistryException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }
}
