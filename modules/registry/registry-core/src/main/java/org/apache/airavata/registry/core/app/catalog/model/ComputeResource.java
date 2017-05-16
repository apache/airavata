/**
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
 */
package org.apache.airavata.registry.core.app.catalog.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "COMPUTE_RESOURCE")
public class ComputeResource implements Serializable {
	
	@Column(name = "RESOURCE_DESCRIPTION")
	private String resourceDescription;
	
	@Id
	@Column(name = "RESOURCE_ID")
	private String resourceId;
	
	@Column(name = "HOST_NAME")
	private String hostName;

    @Column(name = "MAX_MEMORY_NODE")
    private int maxMemoryPerNode;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    @Column(name = "ENABLED")
    private boolean enabled;

    @Column(name = "GATEWAY_USAGE_REPORTING")
    private boolean gatewayUsageReporting;

    @Column(name = "GATEWAY_USAGE_MODULE_LOAD_CMD")
    private String gatewayUsageModLoadCMD;

    @Column(name = "GATEWAY_USAGE_EXECUTABLE")
    private String gatewayUsageExec;

    @Column(name = "CPUS_PER_NODE")
    private Integer cpusPerNode;

    @Column(name = "DEFAULT_NODE_COUNT")
    private Integer defaultNodeCount;

    @Column(name = "DEFAULT_CPU_COUNT")
    private Integer defaultCPUCount;

    @Column(name = "DEFAULT_WALLTIME")
    private Integer defaultWalltime;

    public Timestamp getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public String getResourceDescription() {
		return resourceDescription;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	
	public String getHostName() {
		return hostName;
	}

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
	
	public void setResourceDescription(String resourceDescription) {
		this.resourceDescription=resourceDescription;
	}
	
	public void setResourceId(String resourceId) {
		this.resourceId=resourceId;
	}
	
	public void setHostName(String hostName) {
		this.hostName=hostName;
	}

    public int getMaxMemoryPerNode() {
        return maxMemoryPerNode;
    }

    public void setMaxMemoryPerNode(int maxMemoryPerNode) {
        this.maxMemoryPerNode = maxMemoryPerNode;
    }

    public boolean isGatewayUsageReporting() {
        return gatewayUsageReporting;
    }

    public void setGatewayUsageReporting(boolean gatewayUsageReporting) {
        this.gatewayUsageReporting = gatewayUsageReporting;
    }

    public String getGatewayUsageModLoadCMD() {
        return gatewayUsageModLoadCMD;
    }

    public void setGatewayUsageModLoadCMD(String gatewayUsageModLoadCMD) {
        this.gatewayUsageModLoadCMD = gatewayUsageModLoadCMD;
    }

    public String getGatewayUsageExec() {
        return gatewayUsageExec;
    }

    public void setGatewayUsageExec(String gatewayUsageExec) {
        this.gatewayUsageExec = gatewayUsageExec;
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