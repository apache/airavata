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

/**
 * Domain model: WorkflowRunStepState
 *
 * Captures the runtime state of a single WorkflowStep within a WorkflowRun.
 * The experimentId links back to the Airavata experiment that was created to
 * execute this step, allowing callers to query detailed experiment status
 * without duplicating that information here.
 */
public class WorkflowRunStepState {

    private String experimentId;
    private WorkflowRunStatus status;

    public WorkflowRunStepState() {}

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public WorkflowRunStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowRunStatus status) {
        this.status = status;
    }
}
