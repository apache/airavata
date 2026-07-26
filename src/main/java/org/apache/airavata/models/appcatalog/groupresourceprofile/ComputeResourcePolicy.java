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
 * {@code org.apache.airavata.model.appcatalog.groupresourceprofile.proto.ComputeResourcePolicy}.
 */
public record ComputeResourcePolicy(
        String resourcePolicyId, String computeResourceId, String groupResourceProfileId,
        List<String> allowedBatchQueues) {

    public ComputeResourcePolicy {
        allowedBatchQueues = allowedBatchQueues == null ? List.of() : List.copyOf(allowedBatchQueues);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String resourcePolicyId = "";
        private String computeResourceId = "";
        private String groupResourceProfileId = "";
        private List<String> allowedBatchQueues = new ArrayList<>();

        private Builder() {}

        private Builder(ComputeResourcePolicy source) {
            this.resourcePolicyId = source.resourcePolicyId;
            this.computeResourceId = source.computeResourceId;
            this.groupResourceProfileId = source.groupResourceProfileId;
            this.allowedBatchQueues = new ArrayList<>(source.allowedBatchQueues);
        }

        public Builder setResourcePolicyId(String resourcePolicyId) {
            this.resourcePolicyId = resourcePolicyId;
            return this;
        }

        public Builder setComputeResourceId(String computeResourceId) {
            this.computeResourceId = computeResourceId;
            return this;
        }

        public Builder setGroupResourceProfileId(String groupResourceProfileId) {
            this.groupResourceProfileId = groupResourceProfileId;
            return this;
        }

        public Builder addAllowedBatchQueues(String value) {
            this.allowedBatchQueues.add(value);
            return this;
        }

        public Builder addAllAllowedBatchQueues(Iterable<String> values) {
            values.forEach(this.allowedBatchQueues::add);
            return this;
        }

        public Builder clearAllowedBatchQueues() {
            this.allowedBatchQueues.clear();
            return this;
        }

        public ComputeResourcePolicy build() {
            return new ComputeResourcePolicy(
                    resourcePolicyId, computeResourceId, groupResourceProfileId, allowedBatchQueues);
        }
    }
}
