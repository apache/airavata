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
package org.apache.airavata.research.experiment.model;

import java.util.Objects;

/**
 * Domain model: ExperimentSummaryModel
 */
public class ExperimentSummaryModel {
    private String experimentId;
    private String projectId;
    private String gatewayId;
    private long creationTime;
    private String userName;
    private String name;
    private String description;
    private String executionId;
    private String resourceHostId;
    private String experimentStatus;
    private long statusUpdateTime;

    public ExperimentSummaryModel() {}

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getResourceHostId() {
        return resourceHostId;
    }

    public void setResourceHostId(String resourceHostId) {
        this.resourceHostId = resourceHostId;
    }

    public String getExperimentStatus() {
        return experimentStatus;
    }

    public void setExperimentStatus(String experimentStatus) {
        this.experimentStatus = experimentStatus;
    }

    public long getStatusUpdateTime() {
        return statusUpdateTime;
    }

    public void setStatusUpdateTime(long statusUpdateTime) {
        this.statusUpdateTime = statusUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentSummaryModel that = (ExperimentSummaryModel) o;
        return Objects.equals(experimentId, that.experimentId)
                && Objects.equals(projectId, that.projectId)
                && Objects.equals(gatewayId, that.gatewayId)
                && Objects.equals(creationTime, that.creationTime)
                && Objects.equals(userName, that.userName)
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description)
                && Objects.equals(executionId, that.executionId)
                && Objects.equals(resourceHostId, that.resourceHostId)
                && Objects.equals(experimentStatus, that.experimentStatus)
                && Objects.equals(statusUpdateTime, that.statusUpdateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                experimentId,
                projectId,
                gatewayId,
                creationTime,
                userName,
                name,
                description,
                executionId,
                resourceHostId,
                experimentStatus,
                statusUpdateTime);
    }

    @Override
    public String toString() {
        return "ExperimentSummaryModel{" + "experimentId=" + experimentId + ", projectId=" + projectId + ", gatewayId="
                + gatewayId + ", creationTime=" + creationTime + ", userName=" + userName + ", name=" + name
                + ", description=" + description + ", executionId=" + executionId + ", resourceHostId=" + resourceHostId
                + ", experimentStatus=" + experimentStatus + ", statusUpdateTime=" + statusUpdateTime + "}";
    }
}
