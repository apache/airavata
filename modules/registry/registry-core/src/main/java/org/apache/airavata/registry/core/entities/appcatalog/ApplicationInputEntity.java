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

import org.apache.airavata.model.application.io.DataType;
import org.apache.openjpa.persistence.jdbc.ForeignKey;
import org.apache.openjpa.persistence.jdbc.ForeignKeyAction;

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
	private String name;

	@Column(name = "APP_ARGUMENT")
	private String applicationArgument;

	@Column(name = "DATA_STAGED")
	private boolean dataStaged;

	@Column(name = "DATA_TYPE")
	@Enumerated(EnumType.STRING)
	private DataType type;

	@Column(name = "INPUT_ORDER")
	private int inputOrder;

	@Column(name = "INPUT_VALUE")
	private String value;

	@Column(name = "IS_REQUIRED")
	private boolean isRequired;

	@Column(name = "METADATA", length = 4096)
	private String metaData;

	@Column(name = "REQUIRED_TO_COMMANDLINE")
	private boolean requiredToAddedToCommandLine;

	@Column(name = "STANDARD_INPUT")
	private boolean standardInput;

	@Lob
	@Column(name = "USER_FRIENDLY_DESC")
	private String userFriendlyDescription;

	@Column(name = "IS_READ_ONLY")
	private boolean isReadOnly;

	@Column(name = "OVERRIDE_FILENAME")
	private String overrideFilename;

	@ManyToOne(targetEntity = ApplicationInterfaceEntity.class)
	@JoinColumn(name = "INTERFACE_ID", nullable = false, updatable = false)
	@ForeignKey(deleteAction = ForeignKeyAction.CASCADE)
	private ApplicationInterfaceEntity applicationInterface;

	public ApplicationInputEntity() {
	}

	public String getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(String interfaceId) {
		this.interfaceId = interfaceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApplicationArgument() {
		return applicationArgument;
	}

	public void setApplicationArgument(String applicationArgument) {
		this.applicationArgument = applicationArgument;
	}

	public boolean getDataStaged() {
		return dataStaged;
	}

	public void setDataStaged(boolean dataStaged) {
		this.dataStaged = dataStaged;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public int getInputOrder() {
		return inputOrder;
	}

	public void setInputOrder(int inputOrder) {
		this.inputOrder = inputOrder;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean getIsRequired() {
		return isRequired;
	}

	public void setIsRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public boolean getRequiredToAddedToCommandLine() {
		return requiredToAddedToCommandLine;
	}

	public void setRequiredToAddedToCommandLine(boolean requiredToAddedToCommandLine) {
		this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
	}

	public boolean getStandardInput() {
		return standardInput;
	}

	public void setStandardInput(boolean standardInput) {
		this.standardInput = standardInput;
	}

	public String getUserFriendlyDescription() {
		return userFriendlyDescription;
	}

	public ApplicationInputEntity setUserFriendlyDescription(String userFriendlyDescription) {
		this.userFriendlyDescription = userFriendlyDescription;
		return this;
	}

	public boolean getIsReadOnly() {
		return isReadOnly;
	}

	public void setIsReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	public String getOverrideFilename() {
		return overrideFilename;
	}

	public void setOverrideFilename(String overrideFilename) {
		this.overrideFilename = overrideFilename;
	}

	public ApplicationInterfaceEntity getApplicationInterface() {
		return applicationInterface;
	}

	public void setApplicationInterface(ApplicationInterfaceEntity applicationInterface) {
		this.applicationInterface = applicationInterface;
	}

}
