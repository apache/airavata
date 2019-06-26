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


/**
 * The persistent class for the application_interface database table.
 * 
 */
@Entity
@Table(name="APPLICATION_INTERFACE")
public class ApplicationInterfaceEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="INTERFACE_ID")
	private String applicationInterfaceId;

	@Column(name="APPLICATION_DESCRIPTION")
	private String applicationDescription;

	@Column(name="APPLICATION_NAME")
	private String applicationName;

	@Column(name="ARCHIVE_WORKING_DIRECTORY")
	private boolean archiveWorkingDirectory;

	@Column(name="CREATION_TIME", nullable = false, updatable = false)
	private Timestamp creationTime;

	@Column(name="GATEWAY_ID", nullable = false, updatable = false)
	private String gatewayId;

	@Column(name="UPDATE_TIME", nullable = false)
	private Timestamp updateTime;

	@Column(name="HAS_OPTIONAL_FILE_INPUTS")
	private boolean hasOptionalFileInputs;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="APP_MODULE_MAPPING", joinColumns = @JoinColumn(name="INTERFACE_ID"))
	@Column(name = "MODULE_ID")
	private List<String> applicationModules;

	@OneToMany(targetEntity = ApplicationInputEntity.class, cascade = CascadeType.ALL, orphanRemoval = true,
			mappedBy = "applicationInterface", fetch = FetchType.EAGER)
	private List<ApplicationInputEntity> applicationInputs;

	@OneToMany(targetEntity = ApplicationOutputEntity.class, cascade = CascadeType.ALL, orphanRemoval = true,
			mappedBy = "applicationInterface", fetch = FetchType.EAGER)
	private List<ApplicationOutputEntity> applicationOutputs;

	
	public ApplicationInterfaceEntity() {
	}

	public String getApplicationInterfaceId() {
		return applicationInterfaceId;
	}

	public void setApplicationInterfaceId(String applicationInterfaceId) {
		this.applicationInterfaceId = applicationInterfaceId;
	}

	public String getApplicationDescription() {
		return applicationDescription;
	}

	public void setApplicationDescription(String applicationDescription) {
		this.applicationDescription = applicationDescription;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public boolean getArchiveWorkingDirectory() {
		return archiveWorkingDirectory;
	}

	public void setArchiveWorkingDirectory(boolean archiveWorkingDirectory) {
		this.archiveWorkingDirectory = archiveWorkingDirectory;
	}

	public Timestamp getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(Timestamp creationTime) {
		this.creationTime = creationTime;
	}

	public String getGatewayId() {
		return gatewayId;
	}

	public void setGatewayId(String gatewayId) {
		this.gatewayId = gatewayId;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public boolean getHasOptionalFileInputs() {
		return hasOptionalFileInputs;
	}

	public void setHasOptionalFileInputs(boolean hasOptionalFileInputs) {
		this.hasOptionalFileInputs = hasOptionalFileInputs;
	}

	public List<String> getApplicationModules() { return applicationModules; }

	public void setApplicationModules(List<String> applicationModules) { this.applicationModules = applicationModules; }

	public List<ApplicationInputEntity> getApplicationInputs() {
		return applicationInputs;
	}

	public void setApplicationInputs(List<ApplicationInputEntity> applicationInputs) {
		this.applicationInputs = applicationInputs;
	}

	public List<ApplicationOutputEntity> getApplicationOutputs() {
		return applicationOutputs;
	}

	public void setApplicationOutputs(List<ApplicationOutputEntity> applicationOutputs) {
		this.applicationOutputs = applicationOutputs;
	}

}
