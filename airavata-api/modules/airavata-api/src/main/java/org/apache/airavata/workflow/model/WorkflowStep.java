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
import org.apache.airavata.research.application.model.ApplicationInput;

/**
 * Domain model: WorkflowStep
 *
 * A single node in the workflow DAG representing one application execution.
 * Canvas layout coordinates (x, y) are stored alongside the application
 * reference and its pre-configured inputs so that the UI can restore the
 * visual editor state without additional queries.
 */
public class WorkflowStep {

    private String stepId;
    private String applicationId;
    private String label;
    private List<ApplicationInput> inputs;
    private int x;
    private int y;

    public WorkflowStep() {}

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<ApplicationInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<ApplicationInput> inputs) {
        this.inputs = inputs;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
