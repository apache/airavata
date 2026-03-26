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

import java.util.List;

/**
 * Domain model: WorkflowEdge
 *
 * A directed edge in the workflow DAG connecting two WorkflowSteps. Each edge
 * carries zero or more output-to-input mappings that describe how data produced
 * by the source step is forwarded to the destination step.
 */
public class WorkflowEdge {

    private String fromStepId;
    private String toStepId;
    private List<WorkflowEdgeMapping> mappings;

    public WorkflowEdge() {}

    public String getFromStepId() {
        return fromStepId;
    }

    public void setFromStepId(String fromStepId) {
        this.fromStepId = fromStepId;
    }

    public String getToStepId() {
        return toStepId;
    }

    public void setToStepId(String toStepId) {
        this.toStepId = toStepId;
    }

    public List<WorkflowEdgeMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<WorkflowEdgeMapping> mappings) {
        this.mappings = mappings;
    }
}
