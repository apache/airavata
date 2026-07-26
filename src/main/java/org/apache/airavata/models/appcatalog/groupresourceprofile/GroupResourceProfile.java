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
package org.apache.airavata.models.appcatalog.groupresourceprofile;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.groupresourceprofile.proto.GroupResourceProfile}.
 */
public record GroupResourceProfile(
        String gatewayId,
        String groupResourceProfileId,
        String groupResourceProfileName,
        List<GroupComputeResourcePreference> computePreferences,
        List<ComputeResourcePolicy> computeResourcePolicies,
        List<BatchQueueResourcePolicy> batchQueueResourcePolicies,
        long creationTime,
        long updatedTime,
        String defaultCredentialStoreToken) {

    public GroupResourceProfile {
        computePreferences = computePreferences == null ? List.of() : List.copyOf(computePreferences);
        computeResourcePolicies = computeResourcePolicies == null ? List.of() : List.copyOf(computeResourcePolicies);
        batchQueueResourcePolicies =
                batchQueueResourcePolicies == null ? List.of() : List.copyOf(batchQueueResourcePolicies);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String gatewayId = "";
        private String groupResourceProfileId = "";
        private String groupResourceProfileName = "";
        private List<GroupComputeResourcePreference> computePreferences = new ArrayList<>();
        private List<ComputeResourcePolicy> computeResourcePolicies = new ArrayList<>();
        private List<BatchQueueResourcePolicy> batchQueueResourcePolicies = new ArrayList<>();
        private long creationTime;
        private long updatedTime;
        private String defaultCredentialStoreToken = "";

        private Builder() {}

        private Builder(GroupResourceProfile source) {
            this.gatewayId = source.gatewayId;
            this.groupResourceProfileId = source.groupResourceProfileId;
            this.groupResourceProfileName = source.groupResourceProfileName;
            this.computePreferences = new ArrayList<>(source.computePreferences);
            this.computeResourcePolicies = new ArrayList<>(source.computeResourcePolicies);
            this.batchQueueResourcePolicies = new ArrayList<>(source.batchQueueResourcePolicies);
            this.creationTime = source.creationTime;
            this.updatedTime = source.updatedTime;
            this.defaultCredentialStoreToken = source.defaultCredentialStoreToken;
        }

        public Builder setGatewayId(String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder setGroupResourceProfileId(String groupResourceProfileId) {
            this.groupResourceProfileId = groupResourceProfileId;
            return this;
        }

        public Builder setGroupResourceProfileName(String groupResourceProfileName) {
            this.groupResourceProfileName = groupResourceProfileName;
            return this;
        }

        public Builder addComputePreferences(GroupComputeResourcePreference value) {
            this.computePreferences.add(value);
            return this;
        }

        public Builder addAllComputePreferences(Iterable<GroupComputeResourcePreference> values) {
            values.forEach(this.computePreferences::add);
            return this;
        }

        public Builder clearComputePreferences() {
            this.computePreferences.clear();
            return this;
        }

        public Builder addComputeResourcePolicies(ComputeResourcePolicy value) {
            this.computeResourcePolicies.add(value);
            return this;
        }

        public Builder addAllComputeResourcePolicies(Iterable<ComputeResourcePolicy> values) {
            values.forEach(this.computeResourcePolicies::add);
            return this;
        }

        public Builder clearComputeResourcePolicies() {
            this.computeResourcePolicies.clear();
            return this;
        }

        public Builder addBatchQueueResourcePolicies(BatchQueueResourcePolicy value) {
            this.batchQueueResourcePolicies.add(value);
            return this;
        }

        public Builder addAllBatchQueueResourcePolicies(Iterable<BatchQueueResourcePolicy> values) {
            values.forEach(this.batchQueueResourcePolicies::add);
            return this;
        }

        public Builder clearBatchQueueResourcePolicies() {
            this.batchQueueResourcePolicies.clear();
            return this;
        }

        public Builder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder setUpdatedTime(long updatedTime) {
            this.updatedTime = updatedTime;
            return this;
        }

        public Builder setDefaultCredentialStoreToken(String defaultCredentialStoreToken) {
            this.defaultCredentialStoreToken = defaultCredentialStoreToken;
            return this;
        }

        public GroupResourceProfile build() {
            return new GroupResourceProfile(
                    gatewayId, groupResourceProfileId, groupResourceProfileName, computePreferences,
                    computeResourcePolicies, batchQueueResourcePolicies, creationTime, updatedTime,
                    defaultCredentialStoreToken);
        }
    }
}
