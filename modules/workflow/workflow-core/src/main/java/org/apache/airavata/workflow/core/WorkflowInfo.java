/**
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
 */
package org.apache.airavata.workflow.core;

import org.apache.airavata.workflow.core.dag.nodes.ApplicationNode;
import org.apache.airavata.workflow.core.dag.nodes.InputNode;
import org.apache.airavata.workflow.core.dag.nodes.OutputNode;
import org.apache.airavata.workflow.core.dag.nodes.WorkflowNode;

import java.util.List;

public class WorkflowInfo {
    private String name;
    private String id;
    private String description;
    private String version;
    private List<InputNode> inputs;
    private List<OutputNode> outputs;
    private List<ApplicationNode> applications;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<InputNode> getInputs() {
        return inputs;
    }

    public void setInputs(List<InputNode> inputs) {
        this.inputs = inputs;
    }

    public List<OutputNode> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<OutputNode> outputs) {
        this.outputs = outputs;
    }

    public List<ApplicationNode> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationNode> applications) {
        this.applications = applications;
    }
}
