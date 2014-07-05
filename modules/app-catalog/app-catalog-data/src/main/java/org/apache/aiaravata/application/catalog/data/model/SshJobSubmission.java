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

package org.apache.aiaravata.application.catalog.data.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.openjpa.persistence.DataCache;

@DataCache
@Entity
@Table(name = "SSH_JOB_SUBMISSION")
public class SshJobSubmission implements Serializable {
	
	@Column(name = "RESOURCE_JOB_MANAGER_ID")
	private String resourceJobManagerId;
	
	@ManyToOne(cascade= CascadeType.MERGE)
	@JoinColumn(name = "RESOURCE_JOB_MANAGER_ID")
	private ResourceJobManager resourceJobManager;
	
	@Id
	@Column(name = "JOB_SUBMISSION_INTERFACE_ID")
	private String jobSubmissionInterfaceId;
	
	@Column(name = "ALTERNATIVE_SSH_HOSTNAME")
	private String alternativeSshHostname;
	
	@Column(name = "SECURITY_PROTOCOL")
	private String securityProtocol;
	
	@Column(name = "SSH_PORT")
	private int sshPort;
	
	public String getResourceJobManagerId() {
		return resourceJobManagerId;
	}
	
	public ResourceJobManager getResourceJobManager() {
		return resourceJobManager;
	}
	
	public String getJobSubmissionInterfaceId() {
		return jobSubmissionInterfaceId;
	}
	
	public String getAlternativeSshHostname() {
		return alternativeSshHostname;
	}
	
	public String getSecurityProtocol() {
		return securityProtocol;
	}
	
	public int getSshPort() {
		return sshPort;
	}
	
	public void setResourceJobManagerId(String resourceJobManagerId) {
		this.resourceJobManagerId=resourceJobManagerId;
	}
	
	public void setResourceJobManager(ResourceJobManager resourceJobManager) {
		this.resourceJobManager=resourceJobManager;
	}
	
	public void setJobSubmissionInterfaceId(String jobSubmissionInterfaceId) {
		this.jobSubmissionInterfaceId=jobSubmissionInterfaceId;
	}
	
	public void setAlternativeSshHostname(String alternativeSshHostname) {
		this.alternativeSshHostname=alternativeSshHostname;
	}
	
	public void setSecurityProtocol(String securityProtocol) {
		this.securityProtocol=securityProtocol;
	}
	
	public void setSshPort(int sshPort) {
		this.sshPort=sshPort;
	}
}
