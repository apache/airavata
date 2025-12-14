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
package org.apache.airavata.common.model;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

/**
 * Domain model: WorkflowModel
 */
public class WorkflowModel {
    private String templateId;
    private String name;
    private String graph;
    private String gatewayId;
    private String createdUser;
    private ByteBuffer image;
    private List<InputDataObjectType> workflowInputs;
    private List<OutputDataObjectType> workflowOutputs;
    private long creationTime;

    public WorkflowModel() {}

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGraph() {
        return graph;
    }

    public void setGraph(String graph) {
        this.graph = graph;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public ByteBuffer getImage() {
        return image;
    }

    public void setImage(ByteBuffer image) {
        this.image = image;
    }

    public List<InputDataObjectType> getWorkflowInputs() {
        return workflowInputs;
    }

    public void setWorkflowInputs(List<InputDataObjectType> workflowInputs) {
        this.workflowInputs = workflowInputs;
    }

    public List<OutputDataObjectType> getWorkflowOutputs() {
        return workflowOutputs;
    }

    public void setWorkflowOutputs(List<OutputDataObjectType> workflowOutputs) {
        this.workflowOutputs = workflowOutputs;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkflowModel that = (WorkflowModel) o;
        return Objects.equals(templateId, that.templateId)
                && Objects.equals(name, that.name)
                && Objects.equals(graph, that.graph)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(createdUser, that.createdUser)
                && Objects.equals(image, that.image)
                && Objects.equals(workflowInputs, that.workflowInputs)
                && Objects.equals(workflowOutputs, that.workflowOutputs)
                && Objects.equals(creationTime, that.creationTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                templateId, name, graph, gatewayId, createdUser, image, workflowInputs, workflowOutputs, creationTime);
    }

    @Override
    public String toString() {
        return "WorkflowModel{" + "templateId=" + templateId + ", name=" + name + ", graph=" + graph + ", gatewayId="
                + gatewayId + ", createdUser=" + createdUser + ", image=" + image + ", workflowInputs=" + workflowInputs
                + ", workflowOutputs=" + workflowOutputs + ", creationTime=" + creationTime + "}";
    }
}
