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
package org.apache.airavata.models.workflow;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.models.commons.ErrorModel;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.workflow.proto.AiravataWorkflow}.
 */
public record AiravataWorkflow(
        String id,
        String experimentId,
        String description,
        List<WorkflowApplication> applications,
        List<WorkflowHandler> handlers,
        List<WorkflowConnection> connections,
        List<WorkflowStatus> statuses,
        List<ErrorModel> errors,
        long createdAt,
        long updatedAt) {

    public AiravataWorkflow {
        applications = applications == null ? List.of() : List.copyOf(applications);
        handlers = handlers == null ? List.of() : List.copyOf(handlers);
        connections = connections == null ? List.of() : List.copyOf(connections);
        statuses = statuses == null ? List.of() : List.copyOf(statuses);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String id = "";
        private String experimentId = "";
        private String description = "";
        private List<WorkflowApplication> applications = new ArrayList<>();
        private List<WorkflowHandler> handlers = new ArrayList<>();
        private List<WorkflowConnection> connections = new ArrayList<>();
        private List<WorkflowStatus> statuses = new ArrayList<>();
        private List<ErrorModel> errors = new ArrayList<>();
        private long createdAt;
        private long updatedAt;

        private Builder() {}

        private Builder(AiravataWorkflow source) {
            this.id = source.id;
            this.experimentId = source.experimentId;
            this.description = source.description;
            this.applications = new ArrayList<>(source.applications);
            this.handlers = new ArrayList<>(source.handlers);
            this.connections = new ArrayList<>(source.connections);
            this.statuses = new ArrayList<>(source.statuses);
            this.errors = new ArrayList<>(source.errors);
            this.createdAt = source.createdAt;
            this.updatedAt = source.updatedAt;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setExperimentId(String experimentId) {
            this.experimentId = experimentId;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder addApplications(WorkflowApplication value) {
            this.applications.add(value);
            return this;
        }

        public Builder addAllApplications(Iterable<WorkflowApplication> values) {
            values.forEach(this.applications::add);
            return this;
        }

        public Builder clearApplications() {
            this.applications.clear();
            return this;
        }

        public Builder addHandlers(WorkflowHandler value) {
            this.handlers.add(value);
            return this;
        }

        public Builder addAllHandlers(Iterable<WorkflowHandler> values) {
            values.forEach(this.handlers::add);
            return this;
        }

        public Builder clearHandlers() {
            this.handlers.clear();
            return this;
        }

        public Builder addConnections(WorkflowConnection value) {
            this.connections.add(value);
            return this;
        }

        public Builder addAllConnections(Iterable<WorkflowConnection> values) {
            values.forEach(this.connections::add);
            return this;
        }

        public Builder clearConnections() {
            this.connections.clear();
            return this;
        }

        public Builder addStatuses(WorkflowStatus value) {
            this.statuses.add(value);
            return this;
        }

        public Builder addAllStatuses(Iterable<WorkflowStatus> values) {
            values.forEach(this.statuses::add);
            return this;
        }

        public Builder clearStatuses() {
            this.statuses.clear();
            return this;
        }

        public Builder addErrors(ErrorModel value) {
            this.errors.add(value);
            return this;
        }

        public Builder addAllErrors(Iterable<ErrorModel> values) {
            values.forEach(this.errors::add);
            return this;
        }

        public Builder clearErrors() {
            this.errors.clear();
            return this;
        }

        public Builder setCreatedAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setUpdatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AiravataWorkflow build() {
            return new AiravataWorkflow(
                    id, experimentId, description, applications, handlers, connections, statuses, errors,
                    createdAt, updatedAt);
        }
    }
}
