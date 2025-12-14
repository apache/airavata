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

import java.util.Objects;

/**
 * Domain model: NodeModel
 */
public class NodeModel {
    private String nodeId;
    private String name;
    private String applicationId;
    private String applicationName;
    private ComponentStatus status;
    private String description;

    public NodeModel() {}

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public ComponentStatus getStatus() {
        return status;
    }

    public void setStatus(ComponentStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeModel that = (NodeModel) o;
        return Objects.equals(nodeId, that.nodeId)
                && Objects.equals(name, that.name)
                && Objects.equals(applicationId, that.applicationId)
                && Objects.equals(applicationName, that.applicationName)
                && Objects.equals(status, that.status)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, name, applicationId, applicationName, status, description);
    }

    @Override
    public String toString() {
        return "NodeModel{" + "nodeId=" + nodeId + ", name=" + name + ", applicationId=" + applicationId
                + ", applicationName=" + applicationName + ", status=" + status + ", description=" + description + "}";
    }
}
