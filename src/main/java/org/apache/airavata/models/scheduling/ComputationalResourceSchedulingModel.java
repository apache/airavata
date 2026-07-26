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
package org.apache.airavata.models.scheduling;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.scheduling.proto.ComputationalResourceSchedulingModel}.
 */
public record ComputationalResourceSchedulingModel(
        String resourceHostId,
        int totalCpuCount,
        int nodeCount,
        int numberOfThreads,
        String queueName,
        int wallTimeLimit,
        int totalPhysicalMemory,
        String chessisNumber,
        String staticWorkingDir,
        String overrideLoginUserName,
        String overrideScratchLocation,
        String overrideAllocationProjectNumber,
        int mGroupCount) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String resourceHostId = "";
        private int totalCpuCount;
        private int nodeCount;
        private int numberOfThreads;
        private String queueName = "";
        private int wallTimeLimit;
        private int totalPhysicalMemory;
        private String chessisNumber = "";
        private String staticWorkingDir = "";
        private String overrideLoginUserName = "";
        private String overrideScratchLocation = "";
        private String overrideAllocationProjectNumber = "";
        private int mGroupCount;

        private Builder() {}

        private Builder(ComputationalResourceSchedulingModel source) {
            this.resourceHostId = source.resourceHostId;
            this.totalCpuCount = source.totalCpuCount;
            this.nodeCount = source.nodeCount;
            this.numberOfThreads = source.numberOfThreads;
            this.queueName = source.queueName;
            this.wallTimeLimit = source.wallTimeLimit;
            this.totalPhysicalMemory = source.totalPhysicalMemory;
            this.chessisNumber = source.chessisNumber;
            this.staticWorkingDir = source.staticWorkingDir;
            this.overrideLoginUserName = source.overrideLoginUserName;
            this.overrideScratchLocation = source.overrideScratchLocation;
            this.overrideAllocationProjectNumber = source.overrideAllocationProjectNumber;
            this.mGroupCount = source.mGroupCount;
        }

        public Builder setResourceHostId(String resourceHostId) {
            this.resourceHostId = resourceHostId;
            return this;
        }

        public Builder setTotalCpuCount(int totalCpuCount) {
            this.totalCpuCount = totalCpuCount;
            return this;
        }

        public Builder setNodeCount(int nodeCount) {
            this.nodeCount = nodeCount;
            return this;
        }

        public Builder setNumberOfThreads(int numberOfThreads) {
            this.numberOfThreads = numberOfThreads;
            return this;
        }

        public Builder setQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public Builder setWallTimeLimit(int wallTimeLimit) {
            this.wallTimeLimit = wallTimeLimit;
            return this;
        }

        public Builder setTotalPhysicalMemory(int totalPhysicalMemory) {
            this.totalPhysicalMemory = totalPhysicalMemory;
            return this;
        }

        public Builder setChessisNumber(String chessisNumber) {
            this.chessisNumber = chessisNumber;
            return this;
        }

        public Builder setStaticWorkingDir(String staticWorkingDir) {
            this.staticWorkingDir = staticWorkingDir;
            return this;
        }

        public Builder setOverrideLoginUserName(String overrideLoginUserName) {
            this.overrideLoginUserName = overrideLoginUserName;
            return this;
        }

        public Builder setOverrideScratchLocation(String overrideScratchLocation) {
            this.overrideScratchLocation = overrideScratchLocation;
            return this;
        }

        public Builder setOverrideAllocationProjectNumber(String overrideAllocationProjectNumber) {
            this.overrideAllocationProjectNumber = overrideAllocationProjectNumber;
            return this;
        }

        public Builder setMGroupCount(int mGroupCount) {
            this.mGroupCount = mGroupCount;
            return this;
        }

        public ComputationalResourceSchedulingModel build() {
            return new ComputationalResourceSchedulingModel(
                    resourceHostId, totalCpuCount, nodeCount, numberOfThreads, queueName, wallTimeLimit,
                    totalPhysicalMemory, chessisNumber, staticWorkingDir, overrideLoginUserName,
                    overrideScratchLocation, overrideAllocationProjectNumber, mGroupCount);
        }
    }
}
