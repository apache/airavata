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

import org.apache.airavata.common.utils.AiravataUtils;
import org.apache.airavata.model.data.movement.SecurityProtocol;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;


/**
 * The persistent class for the ssh_job_submission database table.
 * 
 */
@Entity
@Table(name="SSH_JOB_SUBMISSION")
public class SshJobSubmissionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="JOB_SUBMISSION_INTERFACE_ID")
	private String jobSubmissionInterfaceId;

	@ManyToOne(cascade= CascadeType.MERGE)
	@JoinColumn(name = "RESOURCE_JOB_MANAGER_ID", nullable = false, updatable = false)
	private ResourceJobManagerEntity resourceJobManager;

	@Column(name="ALTERNATIVE_SSH_HOSTNAME")
	private String alternativeSshHostname;

	@Column(name="CREATION_TIME", nullable = false, updatable = false)
	private Timestamp creationTime = AiravataUtils.getCurrentTimestamp();

	@Column(name="MONITOR_MODE")
	private String monitorMode;

	@Column(name="SECURITY_PROTOCOL")
	@Enumerated(EnumType.STRING)
	private SecurityProtocol securityProtocol;

	@Column(name="SSH_PORT")
	private int sshPort;

	@Column(name="UPDATE_TIME")
	private Timestamp updateTime;

	public SshJobSubmissionEntity() {
	}

	public String getJobSubmissionInterfaceId() {
		return jobSubmissionInterfaceId;
	}

	public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
		this.jobSubmissionInterfaceId = jobSubmissionInterfaceId;
	}

	public String getAlternativeSshHostname() {
		return alternativeSshHostname;
	}

	public void setAlternativeSshHostname(String alternativeSshHostname) {
		this.alternativeSshHostname = alternativeSshHostname;
	}

	public Timestamp getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Timestamp creationTime) {
		this.creationTime = creationTime;
	}

	public String getMonitorMode() {
		return monitorMode;
	}

	public void setMonitorMode(String monitorMode) {
		this.monitorMode = monitorMode;
	}

	public SecurityProtocol getSecurityProtocol() {
		return securityProtocol;
	}

	public void setSecurityProtocol(SecurityProtocol securityProtocol) {
		this.securityProtocol = securityProtocol;
	}

	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

    public ResourceJobManagerEntity getResourceJobManager() {
        return resourceJobManager;
    }

    public void setResourceJobManager(ResourceJobManagerEntity resourceJobManager) {
        this.resourceJobManager = resourceJobManager;
    }
}
