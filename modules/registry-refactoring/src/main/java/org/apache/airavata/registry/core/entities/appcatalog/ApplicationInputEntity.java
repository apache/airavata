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
 * The persistent class for the application_input database table.
 * 
 */
@Entity
@Table(name = "APPLICATION_INPUT")
@IdClass(ApplicationInputPK.class)
public class ApplicationInputEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "INTERFACE_ID")
	private String interfaceId;

	@Id
	@Column(name = "INPUT_KEY")
	private String inputKey;

	@Column(name = "APP_ARGUMENT")
	private String appArgument;

	@Column(name = "DATA_STAGED")
	private boolean dataStaged;

	@Column(name = "DATA_TYPE")
	private String dataType;

	@Column(name = "INPUT_ORDER")
	private int inputOrder;

	@Column(name = "INPUT_VALUE")
	private String inputValue;

	@Column(name = "IS_REQUIRED")
	private boolean isRequired;

	@Column(name = "METADATA")
	private String metadata;

	@Column(name = "REQUIRED_TO_COMMANDLINE")
	private boolean requiredToCommandline;

	@Column(name = "STANDARD_INPUT")
	private boolean standardInput;

	@Column(name = "USER_FRIENDLY_DESC")
	private String userFriendlyDesc;

	@Column(name = "IS_READ_ONLY")
	private boolean isReadOnly;

	@ManyToOne(targetEntity = ApplicationInterfaceEntity.class, cascade = CascadeType.MERGE)
	@JoinColumn(name = "INTERFACE_ID")
	private ApplicationInterfaceEntity applicationInterface;

	public ApplicationInputEntity() {
	}

	public String getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}

	public String getInputKey() {
		return inputKey;
	}

	public void setInputKey(String inputKey) {
		this.inputKey = inputKey;
	}

	public String getAppArgument() {
		return appArgument;
	}

	public void setAppArgument(String appArgument) {
		this.appArgument = appArgument;
	}

	public boolean getDataStaged() {
		return dataStaged;
	}

	public void setDataStaged(boolean dataStaged) {
		this.dataStaged = dataStaged;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public int getInputOrder() {
		return inputOrder;
	}

	public void setInputOrder(int inputOrder) {
		this.inputOrder = inputOrder;
	}

	public String getInputValue() {
		return inputValue;
	}

	public void setInputValue(String inputValue) {
		this.inputValue = inputValue;
	}

	public boolean getIsRequired() {
		return isRequired;
	}

	public void setIsRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public boolean getRequiredToCommandline() {
		return requiredToCommandline;
	}

	public void setRequiredToCommandline(boolean requiredToCommandline) {
		this.requiredToCommandline = requiredToCommandline;
	}

	public boolean getStandardInput() {
		return standardInput;
	}

	public void setStandardInput(boolean standardInput) {
		this.standardInput = standardInput;
	}

	public String getUserFriendlyDesc() {
		return userFriendlyDesc;
	}

	public void setUserFriendlyDesc(String userFriendlyDesc) {
		this.userFriendlyDesc = userFriendlyDesc;
	}

	public boolean getIsReadOnly() {
		return isReadOnly;
	}

	public void setIsReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	public ApplicationInterfaceEntity getApplicationInterface() {
		return applicationInterface;
	}

	public void setApplicationInterface(ApplicationInterfaceEntity applicationInterface) {
		this.applicationInterface = applicationInterface;
	}

}