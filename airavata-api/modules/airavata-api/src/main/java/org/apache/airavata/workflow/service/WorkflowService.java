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
package org.apache.airavata.workflow.service;

import java.util.List;
import org.apache.airavata.workflow.model.Workflow;
import org.apache.airavata.workflow.model.WorkflowRun;
import org.apache.airavata.workflow.model.WorkflowRunStatus;

/**
 * Service contract for managing workflows and their execution runs.
 */
public interface WorkflowService {

    Workflow createWorkflow(Workflow workflow);

    Workflow getWorkflow(String workflowId);

    List<Workflow> getWorkflowsByProject(String projectId, String gatewayId);

    List<Workflow> getWorkflowsByUser(String userName, String gatewayId);

    Workflow updateWorkflow(String workflowId, Workflow workflow);

    void deleteWorkflow(String workflowId);

    WorkflowRun createRun(String workflowId, String userName);

    WorkflowRun getRun(String runId);

    List<WorkflowRun> getRunsByWorkflow(String workflowId);

    WorkflowRun updateRunStepState(String runId, String stepId, String experimentId, WorkflowRunStatus status);

    WorkflowRun updateRunStatus(String runId, WorkflowRunStatus status);
}
