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
 * The persistent class for the application_module database table.
 * 
 */
@Entity
@Table(name = "APPLICATION_MODULE")
public class ApplicationModuleEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "MODULE_ID")
	private String appModuleId;

	@Column(name = "CREATION_TIME", nullable = false, updatable = false)
	private Timestamp creationTime;

	@Column(name = "GATEWAY_ID", nullable = false, updatable = false)
	private String gatewayId;

	@Column(name = "MODULE_DESC")
	private String appModuleDescription;

	@Column(name = "MODULE_NAME")
	private String appModuleName;

	@Column(name = "MODULE_VERSION")
	private String appModuleVersion;

	@Column(name = "UPDATE_TIME", nullable = false)
	private Timestamp updateTime;

	public ApplicationModuleEntity() {
	}

	public String getAppModuleId() {
		return appModuleId;
	}

	public void setAppModuleId(String appModuleId) {
		this.appModuleId = appModuleId;
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

	public String getAppModuleDescription() {
		return appModuleDescription;
	}

	public void setAppModuleDescription(String appModuleDescription) {
		this.appModuleDescription = appModuleDescription;
	}

	public String getAppModuleName() {
		return appModuleName;
	}

	public void setAppModuleName(String appModuleName) {
		this.appModuleName = appModuleName;
	}

	public String getAppModuleVersion() {
		return appModuleVersion;
	}

	public void setAppModuleVersion(String appModuleVersion) {
		this.appModuleVersion = appModuleVersion;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
}
