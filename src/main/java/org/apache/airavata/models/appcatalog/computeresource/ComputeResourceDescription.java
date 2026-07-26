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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Computational Resource Description.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.computeresource.proto.ComputeResourceDescription}.
 *
 * @param computeResourceId Airavata Internal Unique Identifier to distinguish Compute Resource.
 * @param hostName Fully Qualified Host Name.
 * @param hostAliases Aliases if any.
 * @param ipAddresses IP Addresses of the Resource.
 * @param resourceDescription A user friendly description of the resource.
 * @param fileSystems Map of file systems type and the path.
 * @param sshPort SSH port for remote job-submission commands on the host.
 * @param resourceJobManager Resource job manager configuration (SLURM provisioning); {@code null}
 *     if unset, matching the generated {@code hasResourceJobManager()} presence check.
 */
public record ComputeResourceDescription(
        String computeResourceId,
        String hostName,
        List<String> hostAliases,
        List<String> ipAddresses,
        String resourceDescription,
        boolean enabled,
        List<BatchQueue> batchQueues,
        Map<Integer, String> fileSystems,
        int maxMemoryPerNode,
        boolean gatewayUsageReporting,
        String gatewayUsageModuleLoadCommand,
        String gatewayUsageExecutable,
        int cpusPerNode,
        int defaultNodeCount,
        int defaultCpuCount,
        int defaultWalltime,
        int sshPort,
        ResourceJobManager resourceJobManager) {

    public ComputeResourceDescription {
        hostAliases = hostAliases == null ? List.of() : List.copyOf(hostAliases);
        ipAddresses = ipAddresses == null ? List.of() : List.copyOf(ipAddresses);
        batchQueues = batchQueues == null ? List.of() : List.copyOf(batchQueues);
        fileSystems = fileSystems == null ? Map.of() : Map.copyOf(fileSystems);
    }

    public boolean hasResourceJobManager() {
        return resourceJobManager != null;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String computeResourceId = "";
        private String hostName = "";
        private List<String> hostAliases = new ArrayList<>();
        private List<String> ipAddresses = new ArrayList<>();
        private String resourceDescription = "";
        private boolean enabled;
        private List<BatchQueue> batchQueues = new ArrayList<>();
        private Map<Integer, String> fileSystems = new LinkedHashMap<>();
        private int maxMemoryPerNode;
        private boolean gatewayUsageReporting;
        private String gatewayUsageModuleLoadCommand = "";
        private String gatewayUsageExecutable = "";
        private int cpusPerNode;
        private int defaultNodeCount;
        private int defaultCpuCount;
        private int defaultWalltime;
        private int sshPort;
        private ResourceJobManager resourceJobManager;

        private Builder() {}

        private Builder(ComputeResourceDescription source) {
            this.computeResourceId = source.computeResourceId;
            this.hostName = source.hostName;
            this.hostAliases = new ArrayList<>(source.hostAliases);
            this.ipAddresses = new ArrayList<>(source.ipAddresses);
            this.resourceDescription = source.resourceDescription;
            this.enabled = source.enabled;
            this.batchQueues = new ArrayList<>(source.batchQueues);
            this.fileSystems = new LinkedHashMap<>(source.fileSystems);
            this.maxMemoryPerNode = source.maxMemoryPerNode;
            this.gatewayUsageReporting = source.gatewayUsageReporting;
            this.gatewayUsageModuleLoadCommand = source.gatewayUsageModuleLoadCommand;
            this.gatewayUsageExecutable = source.gatewayUsageExecutable;
            this.cpusPerNode = source.cpusPerNode;
            this.defaultNodeCount = source.defaultNodeCount;
            this.defaultCpuCount = source.defaultCpuCount;
            this.defaultWalltime = source.defaultWalltime;
            this.sshPort = source.sshPort;
            this.resourceJobManager = source.resourceJobManager;
        }

        public Builder setComputeResourceId(String computeResourceId) {
            this.computeResourceId = computeResourceId;
            return this;
        }

        public Builder setHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public Builder addHostAliases(String value) {
            this.hostAliases.add(value);
            return this;
        }

        public Builder addAllHostAliases(Iterable<String> values) {
            values.forEach(this.hostAliases::add);
            return this;
        }

        public Builder clearHostAliases() {
            this.hostAliases.clear();
            return this;
        }

        public Builder addIpAddresses(String value) {
            this.ipAddresses.add(value);
            return this;
        }

        public Builder addAllIpAddresses(Iterable<String> values) {
            values.forEach(this.ipAddresses::add);
            return this;
        }

        public Builder clearIpAddresses() {
            this.ipAddresses.clear();
            return this;
        }

        public Builder setResourceDescription(String resourceDescription) {
            this.resourceDescription = resourceDescription;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder addBatchQueues(BatchQueue value) {
            this.batchQueues.add(value);
            return this;
        }

        public Builder addAllBatchQueues(Iterable<BatchQueue> values) {
            values.forEach(this.batchQueues::add);
            return this;
        }

        public Builder clearBatchQueues() {
            this.batchQueues.clear();
            return this;
        }

        public Builder putFileSystems(Integer key, String value) {
            this.fileSystems.put(key, value);
            return this;
        }

        public Builder putAllFileSystems(Map<Integer, String> values) {
            this.fileSystems.putAll(values);
            return this;
        }

        public Builder clearFileSystems() {
            this.fileSystems.clear();
            return this;
        }

        public Builder setMaxMemoryPerNode(int maxMemoryPerNode) {
            this.maxMemoryPerNode = maxMemoryPerNode;
            return this;
        }

        public Builder setGatewayUsageReporting(boolean gatewayUsageReporting) {
            this.gatewayUsageReporting = gatewayUsageReporting;
            return this;
        }

        public Builder setGatewayUsageModuleLoadCommand(String gatewayUsageModuleLoadCommand) {
            this.gatewayUsageModuleLoadCommand = gatewayUsageModuleLoadCommand;
            return this;
        }

        public Builder setGatewayUsageExecutable(String gatewayUsageExecutable) {
            this.gatewayUsageExecutable = gatewayUsageExecutable;
            return this;
        }

        public Builder setCpusPerNode(int cpusPerNode) {
            this.cpusPerNode = cpusPerNode;
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

        public Builder setSshPort(int sshPort) {
            this.sshPort = sshPort;
            return this;
        }

        public Builder setResourceJobManager(ResourceJobManager resourceJobManager) {
            this.resourceJobManager = resourceJobManager;
            return this;
        }

        public ComputeResourceDescription build() {
            return new ComputeResourceDescription(
                    computeResourceId, hostName, hostAliases, ipAddresses, resourceDescription, enabled,
                    batchQueues, fileSystems, maxMemoryPerNode, gatewayUsageReporting,
                    gatewayUsageModuleLoadCommand, gatewayUsageExecutable, cpusPerNode, defaultNodeCount,
                    defaultCpuCount, defaultWalltime, sshPort, resourceJobManager);
        }
    }
}
