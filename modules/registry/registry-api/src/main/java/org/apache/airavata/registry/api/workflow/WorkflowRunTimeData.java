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
package org.apache.airavata.registry.api.workflow;

import java.sql.Timestamp;

public class WorkflowRunTimeData {
    String experimentID;
    String workflowInstanceID;
    String templateID;
    Timestamp startTime;
    WorkflowExecutionStatus.State workflowStatus;
    Timestamp lastUpdateTime;

    public WorkflowRunTimeData(String experimentID, String workflowInstanceID, String templateID,
                               Timestamp startTime, WorkflowExecutionStatus.State workflowStatus, Timestamp lastUpdateTime) {
        this.experimentID = experimentID;
        this.workflowInstanceID = workflowInstanceID;
        this.templateID = templateID;
        this.startTime = startTime;
        this.workflowStatus = workflowStatus;
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getExperimentID() {
        return experimentID;
    }

    public String getWorkflowInstanceID() {
        return workflowInstanceID;
    }

    public String getTemplateID() {
        return templateID;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public WorkflowExecutionStatus.State getWorkflowStatus() {
        return workflowStatus;
    }

    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setExperimentID(String experimentID) {
        this.experimentID = experimentID;
    }

    public void setWorkflowInstanceID(String workflowInstanceID) {
        this.workflowInstanceID = workflowInstanceID;
    }

    public void setTemplateID(String templateID) {
        this.templateID = templateID;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setWorkflowStatus(WorkflowExecutionStatus.State workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
