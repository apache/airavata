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
package org.apache.airavata.research.service.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Local entity that mirrors airavata-api ComputeResourceEntity structure
 * Used for local development without external airavata-api dependencies
 */
@Entity
@Table(name = "LOCAL_COMPUTE_RESOURCE")
public class LocalComputeResourceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "RESOURCE_ID")
    private String computeResourceId;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "ENABLED")
    private short enabled;

    @Column(name = "HOST_NAME")
    private String hostName;

    @Column(name = "MAX_MEMORY_NODE")
    private int maxMemoryPerNode;

    @Column(name = "RESOURCE_DESCRIPTION", length = 4000)
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

    // Default constructor
    public LocalComputeResourceEntity() {}

    // Constructor matching airavata-api pattern
    public LocalComputeResourceEntity(String computeResourceId, String hostName, String description) {
        this.computeResourceId = computeResourceId;
        this.hostName = hostName;
        this.resourceDescription = description;
        this.enabled = 1;
        this.creationTime = new Timestamp(System.currentTimeMillis());
        this.updateTime = new Timestamp(System.currentTimeMillis());
    }

    // Getters and setters (matching airavata-api naming)
    public String getComputeResourceId() {
        return computeResourceId;
    }

    public void setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
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

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getMaxMemoryPerNode() {
        return maxMemoryPerNode;
    }

    public void setMaxMemoryPerNode(int maxMemoryPerNode) {
        this.maxMemoryPerNode = maxMemoryPerNode;
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

    @Override
    public String toString() {
        return "LocalComputeResourceEntity{" +
                "computeResourceId='" + computeResourceId + '\'' +
                ", hostName='" + hostName + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}