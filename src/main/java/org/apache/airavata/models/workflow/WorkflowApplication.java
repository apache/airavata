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
 * {@code org.apache.airavata.model.workflow.proto.WorkflowApplication}.
 */
public record WorkflowApplication(
        String id,
        String processId,
        String applicationInterfaceId,
        String computeResourceId,
        String queueName,
        int nodeCount,
        int coreCount,
        int wallTimeLimit,
        int physicalMemory,
        List<ApplicationStatus> statuses,
        List<ErrorModel> errors,
        long createdAt,
        long updatedAt) {

    public WorkflowApplication {
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
        private String processId = "";
        private String applicationInterfaceId = "";
        private String computeResourceId = "";
        private String queueName = "";
        private int nodeCount;
        private int coreCount;
        private int wallTimeLimit;
        private int physicalMemory;
        private List<ApplicationStatus> statuses = new ArrayList<>();
        private List<ErrorModel> errors = new ArrayList<>();
        private long createdAt;
        private long updatedAt;

        private Builder() {}

        private Builder(WorkflowApplication source) {
            this.id = source.id;
            this.processId = source.processId;
            this.applicationInterfaceId = source.applicationInterfaceId;
            this.computeResourceId = source.computeResourceId;
            this.queueName = source.queueName;
            this.nodeCount = source.nodeCount;
            this.coreCount = source.coreCount;
            this.wallTimeLimit = source.wallTimeLimit;
            this.physicalMemory = source.physicalMemory;
            this.statuses = new ArrayList<>(source.statuses);
            this.errors = new ArrayList<>(source.errors);
            this.createdAt = source.createdAt;
            this.updatedAt = source.updatedAt;
        }

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setProcessId(String processId) {
            this.processId = processId;
            return this;
        }

        public Builder setApplicationInterfaceId(String applicationInterfaceId) {
            this.applicationInterfaceId = applicationInterfaceId;
            return this;
        }

        public Builder setComputeResourceId(String computeResourceId) {
            this.computeResourceId = computeResourceId;
            return this;
        }

        public Builder setQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public Builder setNodeCount(int nodeCount) {
            this.nodeCount = nodeCount;
            return this;
        }

        public Builder setCoreCount(int coreCount) {
            this.coreCount = coreCount;
            return this;
        }

        public Builder setWallTimeLimit(int wallTimeLimit) {
            this.wallTimeLimit = wallTimeLimit;
            return this;
        }

        public Builder setPhysicalMemory(int physicalMemory) {
            this.physicalMemory = physicalMemory;
            return this;
        }

        public Builder addStatuses(ApplicationStatus value) {
            this.statuses.add(value);
            return this;
        }

        public Builder addAllStatuses(Iterable<ApplicationStatus> values) {
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

        public WorkflowApplication build() {
            return new WorkflowApplication(
                    id, processId, applicationInterfaceId, computeResourceId, queueName, nodeCount, coreCount,
                    wallTimeLimit, physicalMemory, statuses, errors, createdAt, updatedAt);
        }
    }
}
