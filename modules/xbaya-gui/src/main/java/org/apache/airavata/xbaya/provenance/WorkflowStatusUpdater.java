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

import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowStatusUpdater {
    private static Logger logger = LoggerFactory.getLogger(WorkflowStatusUpdater.class);

    private AiravataRegistry2 registry;

    public WorkflowStatusUpdater(AiravataRegistry2 registry) {
        this.registry = registry;
    }

    public boolean workflowStarted(String experimentID){
        try {
            registry.updateWorkflowInstanceStatus(experimentID, WorkflowInstanceStatus.ExecutionStatus.STARTED);
        } catch (RegistryException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowFailed(String experimentID){
        try {
            registry.updateWorkflowInstanceStatus(experimentID, WorkflowInstanceStatus.ExecutionStatus.FAILED);
        } catch (RegistryException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowFinished(String experimentID){
        try {
            registry.updateWorkflowInstanceStatus(experimentID, WorkflowInstanceStatus.ExecutionStatus.FINISHED);
        } catch (RegistryException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean workflowRunning(String experimentID){
        try {
            registry.updateWorkflowInstanceStatus(experimentID, WorkflowInstanceStatus.ExecutionStatus.RUNNING);
        } catch (RegistryException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

     public boolean workflowPaused(String experimentID){
        try {
            registry.updateWorkflowInstanceStatus(experimentID, WorkflowInstanceStatus.ExecutionStatus.PAUSED);
        } catch (RegistryException e) {
            logger.error("Error updating Wokflow Node status !!");
            return false;
        }
        return true;
    }

    public boolean saveWorkflowData(String experimentID,String workflowInstanceID,String workflowTemplateID) {
        Timestamp currentTime = new Timestamp((new java.util.Date()).getTime());
        try {
            registry.setWorkflowInstanceTemplateName(workflowInstanceID, workflowTemplateID);
            registry.updateWorkflowInstanceStatus(new WorkflowInstanceStatus(new WorkflowInstance(experimentID, workflowInstanceID), WorkflowInstanceStatus.ExecutionStatus.STARTED,currentTime));
        } catch (RegistryException e) {
            logger.error("Error saving Workflow Data !!");
        }
        return true;
    }
}
