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
import java.sql.Timestamp;


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
	private String interfaceId;

	@Column(name="APPLICATION_DESCRIPTION")
	private String applicationDescription;

	@Column(name="APPLICATION_NAME")
	private String applicationName;

	@Column(name="ARCHIVE_WORKING_DIRECTORY")
	private short archiveWorkingDirectory;

	@Column(name="CREATION_TIME")
	private Timestamp creationTime;

	@Column(name="GATEWAY_ID")
	private String gatewayId;

	@Column(name="UPDATE_TIME")
	private Timestamp updateTime;

	@Column(name="HAS_OPTIONAL_FILE_INPUTS")
	private short hasOptionalFileInputs;

	
	public ApplicationInterfaceEntity() {
	}

	public String getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
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

	public short getArchiveWorkingDirectory() {
		return archiveWorkingDirectory;
	}

	public void setArchiveWorkingDirectory(short archiveWorkingDirectory) {
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

	public short getHasOptionalFileInputs() {
		return hasOptionalFileInputs;
	}

	public void setHasOptionalFileInputs(short hasOptionalFileInputs) {
		this.hasOptionalFileInputs = hasOptionalFileInputs;
	}
}