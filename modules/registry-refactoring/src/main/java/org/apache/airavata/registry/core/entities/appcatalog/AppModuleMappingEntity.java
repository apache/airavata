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


/**
 * The persistent class for the app_module_mapping database table.
 * 
 */
@Entity
@Table(name="APP_MODULE_MAPPING")
@IdClass(AppModuleMappingPK.class)
public class AppModuleMappingEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="INTERFACE_ID")
	private String interfaceId;

	@Id
	@Column(name="MODULE_ID")
	private String moduleId;

	@ManyToOne(targetEntity = ApplicationInterfaceEntity.class)
	@JoinColumn(name = "INTERFACE_ID")
	private ApplicationInterfaceEntity applicationInterface;

	@ManyToOne(targetEntity = ApplicationModuleEntity.class)
	@JoinColumn(name = "MODULE_ID")
	private ApplicationModuleEntity applicationModule;

	public AppModuleMappingEntity() {
	}

	public String getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public ApplicationInterfaceEntity getApplicationInterface() {
		return applicationInterface;
	}

	public void setApplicationInterface(ApplicationInterfaceEntity applicationInterface) {
		this.applicationInterface = applicationInterface;
	}

	public ApplicationModuleEntity getApplicationModule() {
		return applicationModule;
	}

	public void setApplicationModule(ApplicationModuleEntity applicationModule) {
		this.applicationModule = applicationModule;
	}

}