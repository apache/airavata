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

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.groupresourceprofile.proto.BatchQueueResourcePolicy}.
 */
public record BatchQueueResourcePolicy(
        String resourcePolicyId,
        String computeResourceId,
        String groupResourceProfileId,
        String queuename,
        int maxAllowedNodes,
        int maxAllowedCores,
        int maxAllowedWalltime) {

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
        private String queuename = "";
        private int maxAllowedNodes;
        private int maxAllowedCores;
        private int maxAllowedWalltime;

        private Builder() {}

        private Builder(BatchQueueResourcePolicy source) {
            this.resourcePolicyId = source.resourcePolicyId;
            this.computeResourceId = source.computeResourceId;
            this.groupResourceProfileId = source.groupResourceProfileId;
            this.queuename = source.queuename;
            this.maxAllowedNodes = source.maxAllowedNodes;
            this.maxAllowedCores = source.maxAllowedCores;
            this.maxAllowedWalltime = source.maxAllowedWalltime;
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

        public Builder setQueuename(String queuename) {
            this.queuename = queuename;
            return this;
        }

        public Builder setMaxAllowedNodes(int maxAllowedNodes) {
            this.maxAllowedNodes = maxAllowedNodes;
            return this;
        }

        public Builder setMaxAllowedCores(int maxAllowedCores) {
            this.maxAllowedCores = maxAllowedCores;
            return this;
        }

        public Builder setMaxAllowedWalltime(int maxAllowedWalltime) {
            this.maxAllowedWalltime = maxAllowedWalltime;
            return this;
        }

        public BatchQueueResourcePolicy build() {
            return new BatchQueueResourcePolicy(
                    resourcePolicyId, computeResourceId, groupResourceProfileId, queuename, maxAllowedNodes,
                    maxAllowedCores, maxAllowedWalltime);
        }
    }
}
