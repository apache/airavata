/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.workflow.model;

import java.time.Instant;
import java.util.Map;

/**
 * Domain model: WorkflowRun
 *
 * An execution instance of a Workflow template. Each run records the aggregate
 * status of the execution and a per-step state map keyed by stepId. The step
 * states hold the backing experiment IDs so that individual experiment results
 * can be retrieved from the Airavata experiment registry.
 */
public class WorkflowRun {

    private String runId;
    private String workflowId;
    private String userName;
    private WorkflowRunStatus status;
    private Map<String, WorkflowRunStepState> stepStates;
    private Instant createdAt;
    private Instant updatedAt;

    public WorkflowRun() {}

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public WorkflowRunStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowRunStatus status) {
        this.status = status;
    }

    public Map<String, WorkflowRunStepState> getStepStates() {
        return stepStates;
    }

    public void setStepStates(Map<String, WorkflowRunStepState> stepStates) {
        this.stepStates = stepStates;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
