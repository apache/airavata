/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.entities.appcatalog;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * The persistent class for the compute_resource database table.
 */
@Entity
@Table(name = "COMPUTE_RESOURCE")
public class ComputeResourceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_ID")
    private String computeResourceId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "ENABLED")
    private short enabled;

    @Column(name = "GATEWAY_USAGE_EXECUTABLE")
    private String gatewayUsageExecutable;

    @Column(name = "GATEWAY_USAGE_MODULE_LOAD_CMD")
    private String gatewayUsageModuleLoadCommand;

    @Column(name = "GATEWAY_USAGE_REPORTING")
    private boolean gatewayUsageReporting;

    @Column(name = "HOST_NAME")
    private String hostName;

    @Column(name = "MAX_MEMORY_NODE")
    private int maxMemoryPerNode;

    @Column(name = "RESOURCE_DESCRIPTION")
    private String resourceDescription;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @Column(name = "CPUS_PER_NODE")
    private Integer cpusPerNode;

    @Column(name = "DEFAULT_NODE_COUNT")
    private Integer defaultNodeCount;

    @Column(name = "DEFAULT_CPU_COUNT")
    private Integer defaultCPUCount;

    @Column(name = "DEFAULT_WALLTIME")
    private Integer defaultWalltime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="HOST_ALIAS", joinColumns = @JoinColumn(name="RESOURCE_ID"))
    @Column(name = "ALIAS")
    private List<String> hostAliases;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="HOST_IPADDRESS", joinColumns = @JoinColumn(name="RESOURCE_ID"))
    @Column(name = "IP_ADDRESS")
    private List<String> ipAddresses;

    @OneToMany(targetEntity = BatchQueueEntity.class, cascade = CascadeType.ALL,
            mappedBy = "computeResource", fetch = FetchType.EAGER)
    private List<BatchQueueEntity> batchQueues;

    @OneToMany(targetEntity = JobSubmissionInterfaceEntity.class, cascade = CascadeType.ALL,
            mappedBy = "computeResource", fetch = FetchType.EAGER)
    private List<JobSubmissionInterfaceEntity> jobSubmissionInterfaces;

    @OneToMany(targetEntity = DataMovementInterfaceEntity.class, cascade = CascadeType.ALL,
            mappedBy = "computeResource", fetch = FetchType.EAGER)
    private List<DataMovementInterfaceEntity> dataMovementInterfaces;

    public ComputeResourceEntity() {
    }

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public short getEnabled() {
        return enabled;
    }

    public void setEnabled(short enabled) {
        this.enabled = enabled;
    }

    public String getGatewayUsageExecutable() {
        return gatewayUsageExecutable;
    }

    public void setGatewayUsageExecutable(String gatewayUsageExecutable) {
        this.gatewayUsageExecutable = gatewayUsageExecutable;
    }

    public boolean isGatewayUsageReporting() {
        return gatewayUsageReporting;
    }

    public void setGatewayUsageReporting(boolean gatewayUsageReporting) {
        this.gatewayUsageReporting = gatewayUsageReporting;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
    }

    public String getGatewayUsageModuleLoadCommand() {
        return gatewayUsageModuleLoadCommand;
    }

    public void setGatewayUsageModuleLoadCommand(String gatewayUsageModuleLoadCommand) {
        this.gatewayUsageModuleLoadCommand = gatewayUsageModuleLoadCommand;
    }

    public int getMaxMemoryPerNode() {
        return maxMemoryPerNode;
    }

    public void setMaxMemoryPerNode(int maxMemoryPerNode) {
        this.maxMemoryPerNode = maxMemoryPerNode;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getResourceDescription() {
        return resourceDescription;
    }

    public void setResourceDescription(String resourceDescription) {
        this.resourceDescription = resourceDescription;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
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

    public List<BatchQueueEntity> getBatchQueues() {
        return batchQueues;
    }

    public void setBatchQueues(List<BatchQueueEntity> batchQueues) {
        this.batchQueues = batchQueues;
    }

    public List<JobSubmissionInterfaceEntity> getJobSubmissionInterfaces() {
        return jobSubmissionInterfaces;
    }

    public void setJobSubmissionInterfaces(List<JobSubmissionInterfaceEntity> jobSubmissionInterfaces) {
        this.jobSubmissionInterfaces = jobSubmissionInterfaces;
    }

    public List<DataMovementInterfaceEntity> getDataMovementInterfaces() {
        return dataMovementInterfaces;
    }

    public void setDataMovementInterfaces(List<DataMovementInterfaceEntity> dataMovementInterfaces) {
        this.dataMovementInterfaces = dataMovementInterfaces;
    }
}