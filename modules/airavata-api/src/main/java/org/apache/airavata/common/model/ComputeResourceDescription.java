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
import java.util.Map;
import java.util.Objects;

/**
 * Domain model: ComputeResourceDescription
 */
public class ComputeResourceDescription {
    private String computeResourceId;
    private String hostName;
    private List<String> hostAliases;
    private List<String> ipAddresses;
    private String resourceDescription;
    private boolean enabled;
    private List<BatchQueue> batchQueues;
    private Map<FileSystems, String> fileSystems;
    private List<JobSubmissionInterface> jobSubmissionInterfaces;
    private List<DataMovementInterface> dataMovementInterfaces;
    private int maxMemoryPerNode;
    private boolean gatewayUsageReporting;
    private String gatewayUsageModuleLoadCommand;
    private String gatewayUsageExecutable;
    private int cpusPerNode;
    private int defaultNodeCount;
    private int defaultCPUCount;
    private int defaultWalltime;

    public ComputeResourceDescription() {}

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public List<String> getHostAliases() {
        return hostAliases;
    }

    public void setHostAliases(List<String> hostAliases) {
        this.hostAliases = hostAliases;
    }

    public List<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(List<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<BatchQueue> getBatchQueues() {
        return batchQueues;
    }

    public void setBatchQueues(List<BatchQueue> batchQueues) {
        this.batchQueues = batchQueues;
    }

    public Map<FileSystems, String> getFileSystems() {
        return fileSystems;
    }

    public void setFileSystems(Map<FileSystems, String> fileSystems) {
        this.fileSystems = fileSystems;
    }

    public List<JobSubmissionInterface> getJobSubmissionInterfaces() {
        return jobSubmissionInterfaces;
    }

    public void setJobSubmissionInterfaces(List<JobSubmissionInterface> jobSubmissionInterfaces) {
        this.jobSubmissionInterfaces = jobSubmissionInterfaces;
    }

    public List<DataMovementInterface> getDataMovementInterfaces() {
        return dataMovementInterfaces;
    }

    public void setDataMovementInterfaces(List<DataMovementInterface> dataMovementInterfaces) {
        this.dataMovementInterfaces = dataMovementInterfaces;
    }

    public int getMaxMemoryPerNode() {
        return maxMemoryPerNode;
    }

    public void setMaxMemoryPerNode(int maxMemoryPerNode) {
        this.maxMemoryPerNode = maxMemoryPerNode;
    }

    public boolean getGatewayUsageReporting() {
        return gatewayUsageReporting;
    }

    public void setGatewayUsageReporting(boolean gatewayUsageReporting) {
        this.gatewayUsageReporting = gatewayUsageReporting;
    }

    public String getGatewayUsageModuleLoadCommand() {
        return gatewayUsageModuleLoadCommand;
    }

    public void setGatewayUsageModuleLoadCommand(String gatewayUsageModuleLoadCommand) {
        this.gatewayUsageModuleLoadCommand = gatewayUsageModuleLoadCommand;
    }

    public String getGatewayUsageExecutable() {
        return gatewayUsageExecutable;
    }

    public void setGatewayUsageExecutable(String gatewayUsageExecutable) {
        this.gatewayUsageExecutable = gatewayUsageExecutable;
    }

    public int getCpusPerNode() {
        return cpusPerNode;
    }

    public void setCpusPerNode(int cpusPerNode) {
        this.cpusPerNode = cpusPerNode;
    }

    public int getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public void setDefaultNodeCount(int defaultNodeCount) {
        this.defaultNodeCount = defaultNodeCount;
    }

    public int getDefaultCPUCount() {
        return defaultCPUCount;
    }

    public void setDefaultCPUCount(int defaultCPUCount) {
        this.defaultCPUCount = defaultCPUCount;
    }

    public int getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(int defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComputeResourceDescription that = (ComputeResourceDescription) o;
        return Objects.equals(computeResourceId, that.computeResourceId)
                && Objects.equals(hostName, that.hostName)
                && Objects.equals(hostAliases, that.hostAliases)
                && Objects.equals(ipAddresses, that.ipAddresses)
                && Objects.equals(resourceDescription, that.resourceDescription)
                && Objects.equals(enabled, that.enabled)
                && Objects.equals(batchQueues, that.batchQueues)
                && Objects.equals(fileSystems, that.fileSystems)
                && Objects.equals(jobSubmissionInterfaces, that.jobSubmissionInterfaces)
                && Objects.equals(dataMovementInterfaces, that.dataMovementInterfaces)
                && Objects.equals(maxMemoryPerNode, that.maxMemoryPerNode)
                && Objects.equals(gatewayUsageReporting, that.gatewayUsageReporting)
                && Objects.equals(gatewayUsageModuleLoadCommand, that.gatewayUsageModuleLoadCommand)
                && Objects.equals(gatewayUsageExecutable, that.gatewayUsageExecutable)
                && Objects.equals(cpusPerNode, that.cpusPerNode)
                && Objects.equals(defaultNodeCount, that.defaultNodeCount)
                && Objects.equals(defaultCPUCount, that.defaultCPUCount)
                && Objects.equals(defaultWalltime, that.defaultWalltime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                computeResourceId,
                hostName,
                hostAliases,
                ipAddresses,
                resourceDescription,
                enabled,
                batchQueues,
                fileSystems,
                jobSubmissionInterfaces,
                dataMovementInterfaces,
                maxMemoryPerNode,
                gatewayUsageReporting,
                gatewayUsageModuleLoadCommand,
                gatewayUsageExecutable,
                cpusPerNode,
                defaultNodeCount,
                defaultCPUCount,
                defaultWalltime);
    }

    @Override
    public String toString() {
        return "ComputeResourceDescription{" + "computeResourceId=" + computeResourceId + ", hostName=" + hostName
                + ", hostAliases=" + hostAliases + ", ipAddresses=" + ipAddresses + ", resourceDescription="
                + resourceDescription + ", enabled=" + enabled + ", batchQueues=" + batchQueues + ", fileSystems="
                + fileSystems + ", jobSubmissionInterfaces=" + jobSubmissionInterfaces + ", dataMovementInterfaces="
                + dataMovementInterfaces + ", maxMemoryPerNode=" + maxMemoryPerNode + ", gatewayUsageReporting="
                + gatewayUsageReporting + ", gatewayUsageModuleLoadCommand=" + gatewayUsageModuleLoadCommand
                + ", gatewayUsageExecutable=" + gatewayUsageExecutable + ", cpusPerNode=" + cpusPerNode
                + ", defaultNodeCount=" + defaultNodeCount + ", defaultCPUCount=" + defaultCPUCount
                + ", defaultWalltime=" + defaultWalltime + "}";
    }
}
