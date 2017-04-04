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

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "RESOURCE_JOB_MANAGER")
public class ResourceJobManager implements Serializable {
	
	@Id
	@Column(name = "RESOURCE_JOB_MANAGER_ID")
	private String resourceJobManagerId;
	
	@Column(name = "PUSH_MONITORING_ENDPOINT")
	private String pushMonitoringEndpoint;
	
	@Column(name = "JOB_MANAGER_BIN_PATH")
	private String jobManagerBinPath;
	
	@Column(name = "RESOURCE_JOB_MANAGER_TYPE")
	private String resourceJobManagerType;

    @Column(name = "CREATION_TIME")
    private Timestamp creationTime;

    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

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
	
	public String getResourceJobManagerId() {
		return resourceJobManagerId;
	}
	
	public String getPushMonitoringEndpoint() {
		return pushMonitoringEndpoint;
	}
	
	public String getJobManagerBinPath() {
		return jobManagerBinPath;
	}
	
	public String getResourceJobManagerType() {
		return resourceJobManagerType;
	}
	
	public void setResourceJobManagerId(String resourceJobManagerId) {
		this.resourceJobManagerId=resourceJobManagerId;
	}
	
	public void setPushMonitoringEndpoint(String pushMonitoringEndpoint) {
		this.pushMonitoringEndpoint=pushMonitoringEndpoint;
	}
	
	public void setJobManagerBinPath(String jobManagerBinPath) {
		this.jobManagerBinPath=jobManagerBinPath;
	}
	
	public void setResourceJobManagerType(String resourceJobManagerType) {
		this.resourceJobManagerType=resourceJobManagerType;
	}
}

