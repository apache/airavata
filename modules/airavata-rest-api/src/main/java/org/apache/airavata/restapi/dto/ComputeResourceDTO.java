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
package org.apache.airavata.restapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComputeResourceDTO {
    private String computeResourceId;
    private String hostName;
    private List<String> hostAliases;
    private List<String> ipAddresses;
    private String resourceDescription;
    private Boolean enabled;
    private List<BatchQueueDTO> batchQueues;
    private Map<String, String> fileSystems;
    private Integer maxMemoryPerNode;
    private Boolean gatewayUsageReporting;
    private String gatewayUsageModuleLoadCommand;
    private String gatewayUsageExecutable;
    private Integer cpusPerNode;
    private Integer defaultNodeCount;
    private Integer defaultCPUCount;
    private Integer defaultWalltime;

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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public List<BatchQueueDTO> getBatchQueues() {
        return batchQueues;
    }

    public void setBatchQueues(List<BatchQueueDTO> batchQueues) {
        this.batchQueues = batchQueues;
    }

    public Map<String, String> getFileSystems() {
        return fileSystems;
    }

    public void setFileSystems(Map<String, String> fileSystems) {
        this.fileSystems = fileSystems;
    }

    public Integer getMaxMemoryPerNode() {
        return maxMemoryPerNode;
    }

    public void setMaxMemoryPerNode(Integer maxMemoryPerNode) {
        this.maxMemoryPerNode = maxMemoryPerNode;
    }

    public Boolean getGatewayUsageReporting() {
        return gatewayUsageReporting;
    }

    public void setGatewayUsageReporting(Boolean gatewayUsageReporting) {
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

    public Integer getCpusPerNode() {
        return cpusPerNode;
    }

    public void setCpusPerNode(Integer cpusPerNode) {
        this.cpusPerNode = cpusPerNode;
    }

    public Integer getDefaultNodeCount() {
        return defaultNodeCount;
    }

    public void setDefaultNodeCount(Integer defaultNodeCount) {
        this.defaultNodeCount = defaultNodeCount;
    }

    public Integer getDefaultCPUCount() {
        return defaultCPUCount;
    }

    public void setDefaultCPUCount(Integer defaultCPUCount) {
        this.defaultCPUCount = defaultCPUCount;
    }

    public Integer getDefaultWalltime() {
        return defaultWalltime;
    }

    public void setDefaultWalltime(Integer defaultWalltime) {
        this.defaultWalltime = defaultWalltime;
    }
}



