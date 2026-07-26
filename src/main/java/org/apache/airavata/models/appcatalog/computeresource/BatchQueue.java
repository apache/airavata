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
package org.apache.airavata.models.appcatalog.computeresource;

/**
 * Batch Queue Information on SuperComputers.
 *
 * <p>Plain-POJO replacement for the generated {@code org.apache.airavata.model.appcatalog.computeresource.proto.BatchQueue}.
 *
 * @param maxRunTime Maximum allowed run time in hours.
 */
public record BatchQueue(
        String queueName,
        String queueDescription,
        int maxRunTime,
        int maxNodes,
        int maxProcessors,
        int maxJobsInQueue,
        int maxMemory,
        int cpuPerNode,
        int defaultNodeCount,
        int defaultCpuCount,
        int defaultWalltime,
        String queueSpecificMacros,
        boolean isDefaultQueue) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String queueName = "";
        private String queueDescription = "";
        private int maxRunTime;
        private int maxNodes;
        private int maxProcessors;
        private int maxJobsInQueue;
        private int maxMemory;
        private int cpuPerNode;
        private int defaultNodeCount;
        private int defaultCpuCount;
        private int defaultWalltime;
        private String queueSpecificMacros = "";
        private boolean isDefaultQueue;

        private Builder() {}

        private Builder(BatchQueue source) {
            this.queueName = source.queueName;
            this.queueDescription = source.queueDescription;
            this.maxRunTime = source.maxRunTime;
            this.maxNodes = source.maxNodes;
            this.maxProcessors = source.maxProcessors;
            this.maxJobsInQueue = source.maxJobsInQueue;
            this.maxMemory = source.maxMemory;
            this.cpuPerNode = source.cpuPerNode;
            this.defaultNodeCount = source.defaultNodeCount;
            this.defaultCpuCount = source.defaultCpuCount;
            this.defaultWalltime = source.defaultWalltime;
            this.queueSpecificMacros = source.queueSpecificMacros;
            this.isDefaultQueue = source.isDefaultQueue;
        }

        public Builder setQueueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        public Builder setQueueDescription(String queueDescription) {
            this.queueDescription = queueDescription;
            return this;
        }

        public Builder setMaxRunTime(int maxRunTime) {
            this.maxRunTime = maxRunTime;
            return this;
        }

        public Builder setMaxNodes(int maxNodes) {
            this.maxNodes = maxNodes;
            return this;
        }

        public Builder setMaxProcessors(int maxProcessors) {
            this.maxProcessors = maxProcessors;
            return this;
        }

        public Builder setMaxJobsInQueue(int maxJobsInQueue) {
            this.maxJobsInQueue = maxJobsInQueue;
            return this;
        }

        public Builder setMaxMemory(int maxMemory) {
            this.maxMemory = maxMemory;
            return this;
        }

        public Builder setCpuPerNode(int cpuPerNode) {
            this.cpuPerNode = cpuPerNode;
            return this;
        }

        public Builder setDefaultNodeCount(int defaultNodeCount) {
            this.defaultNodeCount = defaultNodeCount;
            return this;
        }

        public Builder setDefaultCpuCount(int defaultCpuCount) {
            this.defaultCpuCount = defaultCpuCount;
            return this;
        }

        public Builder setDefaultWalltime(int defaultWalltime) {
            this.defaultWalltime = defaultWalltime;
            return this;
        }

        public Builder setQueueSpecificMacros(String queueSpecificMacros) {
            this.queueSpecificMacros = queueSpecificMacros;
            return this;
        }

        public Builder setIsDefaultQueue(boolean isDefaultQueue) {
            this.isDefaultQueue = isDefaultQueue;
            return this;
        }

        public BatchQueue build() {
            return new BatchQueue(
                    queueName, queueDescription, maxRunTime, maxNodes, maxProcessors, maxJobsInQueue, maxMemory,
                    cpuPerNode, defaultNodeCount, defaultCpuCount, defaultWalltime, queueSpecificMacros,
                    isDefaultQueue);
        }
    }
}
