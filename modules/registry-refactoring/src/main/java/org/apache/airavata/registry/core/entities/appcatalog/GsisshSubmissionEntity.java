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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * The persistent class for the gsissh_submission database table.
 * 
 */
@Entity
@Table(name = "GSISSH_SUBMISSION")
public class GsisshSubmissionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "SUBMISSION_ID")
	private String submissionId;

	@Column(name = "INSTALLED_PATH")
	private String installedPath;

	@Column(name = "MONITOR_MODE")
	private String monitorMode;

	@Column(name = "RESOURCE_JOB_MANAGER")
	private String resourceJobManager;

	@Column(name = "SSH_PORT")
	private int sshPort;

	public GsisshSubmissionEntity() {
	}

	public String getSubmissionId() {
		return submissionId;
	}

	public void setSubmissionId(String submissionId) {
		this.submissionId = submissionId;
	}

	public String getInstalledPath() {
		return installedPath;
	}

	public void setInstalledPath(String installedPath) {
		this.installedPath = installedPath;
	}

	public String getMonitorMode() {
		return monitorMode;
	}

	public void setMonitorMode(String monitorMode) {
		this.monitorMode = monitorMode;
	}

	public String getResourceJobManager() {
		return resourceJobManager;
	}

	public void setResourceJobManager(String resourceJobManager) {
		this.resourceJobManager = resourceJobManager;
	}

	public int getSshPort() {
		return sshPort;
	}

	public void setSshPort(int sshPort) {
		this.sshPort = sshPort;
	}
}