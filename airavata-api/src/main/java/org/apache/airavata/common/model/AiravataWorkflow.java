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

import java.util.List;
import java.util.Objects;

/**
 * Domain model: AiravataWorkflow
 */
public class AiravataWorkflow {
    private String id;
    private String experimentId;
    private String description;
    private List<WorkflowApplication> applications;
    private List<WorkflowHandler> handlers;
    private List<WorkflowConnection> connections;
    private List<WorkflowStatus> statuses;
    private List<ErrorModel> errors;
    private long createdAt;
    private long updatedAt;

    public AiravataWorkflow() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<WorkflowApplication> getApplications() {
        return applications;
    }

    public void setApplications(List<WorkflowApplication> applications) {
        this.applications = applications;
    }

    public List<WorkflowHandler> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<WorkflowHandler> handlers) {
        this.handlers = handlers;
    }

    public List<WorkflowConnection> getConnections() {
        return connections;
    }

    public void setConnections(List<WorkflowConnection> connections) {
        this.connections = connections;
    }

    public List<WorkflowStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<WorkflowStatus> statuses) {
        this.statuses = statuses;
    }

    public List<ErrorModel> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorModel> errors) {
        this.errors = errors;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AiravataWorkflow that = (AiravataWorkflow) o;
        return Objects.equals(id, that.id)
                && Objects.equals(experimentId, that.experimentId)
                && Objects.equals(description, that.description)
                && Objects.equals(applications, that.applications)
                && Objects.equals(handlers, that.handlers)
                && Objects.equals(connections, that.connections)
                && Objects.equals(statuses, that.statuses)
                && Objects.equals(errors, that.errors)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                experimentId,
                description,
                applications,
                handlers,
                connections,
                statuses,
                errors,
                createdAt,
                updatedAt);
    }

    @Override
    public String toString() {
        return "AiravataWorkflow{" + "id=" + id + ", experimentId=" + experimentId + ", description=" + description
                + ", applications=" + applications + ", handlers=" + handlers + ", connections=" + connections
                + ", statuses=" + statuses + ", errors=" + errors + ", createdAt=" + createdAt + ", updatedAt="
                + updatedAt + "}";
    }
}
