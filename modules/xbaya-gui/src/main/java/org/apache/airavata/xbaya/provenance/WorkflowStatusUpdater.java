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

import java.sql.Timestamp;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.exception.AiravataAPIInvocationException;
import org.apache.airavata.registry.api.exception.RegistryException;
//import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowStatusUpdater {
    private static Logger logger = LoggerFactory.getLogger(WorkflowStatusUpdater.class);

    private AiravataAPI airavataAPI;

    public WorkflowStatusUpdater(AiravataAPI airavataAPI) {
        this.airavataAPI = airavataAPI;
    }

    public boolean workflowStarted(String experimentID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceStatus(experimentID, experimentID, WorkflowExecutionStatus.State.STARTED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowFailed(String experimentID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceStatus(experimentID, experimentID, WorkflowExecutionStatus.State.FAILED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowFinished(String experimentID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceStatus(experimentID, experimentID, WorkflowExecutionStatus.State.FINISHED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowRunning(String experimentID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceStatus(experimentID, experimentID, WorkflowExecutionStatus.State.RUNNING);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

     public boolean workflowPaused(String experimentID){
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceStatus(experimentID, experimentID, WorkflowExecutionStatus.State.PAUSED);
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean saveWorkflowData(String experimentID,String workflowInstanceID,String workflowTemplateID) {
        Timestamp currentTime = new Timestamp((new java.util.Date()).getTime());
        try {
            airavataAPI.getProvenanceManager().setWorkflowInstanceTemplateName(workflowInstanceID, workflowTemplateID);
            airavataAPI.getProvenanceManager().setWorkflowInstanceStatus(new WorkflowExecutionStatus(new WorkflowExecution(experimentID, workflowInstanceID), WorkflowExecutionStatus.State.STARTED,currentTime));
        } catch (AiravataAPIInvocationException e) {
            logger.error("Error saving Workflow Data !!");
        }
        return true;
    }
}
