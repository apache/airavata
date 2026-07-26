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
package org.apache.airavata.models.experiment;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.experiment.proto.ExperimentSummaryModel}.
 */
public record ExperimentSummaryModel(
        String experimentId,
        String projectId,
        String gatewayId,
        long creationTime,
        String userName,
        String name,
        String description,
        String executionId,
        String resourceHostId,
        String experimentStatus,
        long statusUpdateTime) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String experimentId = "";
        private String projectId = "";
        private String gatewayId = "";
        private long creationTime;
        private String userName = "";
        private String name = "";
        private String description = "";
        private String executionId = "";
        private String resourceHostId = "";
        private String experimentStatus = "";
        private long statusUpdateTime;

        private Builder() {}

        private Builder(ExperimentSummaryModel source) {
            this.experimentId = source.experimentId;
            this.projectId = source.projectId;
            this.gatewayId = source.gatewayId;
            this.creationTime = source.creationTime;
            this.userName = source.userName;
            this.name = source.name;
            this.description = source.description;
            this.executionId = source.executionId;
            this.resourceHostId = source.resourceHostId;
            this.experimentStatus = source.experimentStatus;
            this.statusUpdateTime = source.statusUpdateTime;
        }

        public Builder setExperimentId(String experimentId) {
            this.experimentId = experimentId;
            return this;
        }

        public Builder setProjectId(String projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setExecutionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder setResourceHostId(String resourceHostId) {
            this.resourceHostId = resourceHostId;
            return this;
        }

        public Builder setExperimentStatus(String experimentStatus) {
            this.experimentStatus = experimentStatus;
            return this;
        }

        public Builder setStatusUpdateTime(long statusUpdateTime) {
            this.statusUpdateTime = statusUpdateTime;
            return this;
        }

        public ExperimentSummaryModel build() {
            return new ExperimentSummaryModel(
                    experimentId, projectId, gatewayId, creationTime, userName, name, description, executionId,
                    resourceHostId, experimentStatus, statusUpdateTime);
        }
    }
}
