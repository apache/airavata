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
package org.apache.airavata.registry.core.entities.expcatalog;

import org.apache.airavata.model.application.io.DataType;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The primary key class for the process_input database table.
 */
@Entity
@Table(name = "PROCESS_INPUT")
@IdClass(ProcessInputPK.class)
public class ProcessInputEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PROCESS_ID")
    private String processId;

    @Id
    @Column(name = "INPUT_NAME")
    private String name;

    @Lob
    @Column(name = "INPUT_VALUE")
    private String value;

    @Column(name = "DATA_TYPE")
    @Enumerated(EnumType.STRING)
    private DataType type;

    @Column(name = "APPLICATION_ARGUMENT")
    private String applicationArgument;

    @Column(name = "STANDARD_INPUT")
    private boolean standardInput;

    @Lob
    @Column(name = "USER_FRIENDLY_DESCRIPTION")
    private String userFriendlyDescription;

    @Column(name = "METADATA", length = 4096)
    private String metaData;

    @Column(name = "INPUT_ORDER")
    private int inputOrder;

    @Column(name = "IS_REQUIRED")
    private boolean isRequired;

    @Column(name = "REQUIRED_TO_ADDED_TO_CMD")
    private boolean requiredToAddedToCommandLine;

    @Column(name = "DATA_STAGED")
    private boolean dataStaged;

    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;

    @Column(name = "IS_READ_ONLY")
    private boolean isReadOnly;

	@Column(name = "OVERRIDE_FILENAME")
	private String overrideFilename;

    @ManyToOne(targetEntity = ProcessEntity.class, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID", referencedColumnName = "PROCESS_ID")
    private ProcessEntity process;

    public ProcessInputEntity() {
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public String getApplicationArgument() {
        return applicationArgument;
    }

    public void setApplicationArgument(String applicationArgument) {
        this.applicationArgument = applicationArgument;
    }

    public boolean isStandardInput() {
        return standardInput;
    }

    public void setStandardInput(boolean standardInput) {
        this.standardInput = standardInput;
    }

    public String getUserFriendlyDescription() {
        return userFriendlyDescription;
    }

    public void setUserFriendlyDescription(String userFriendlyDescription) {
        this.userFriendlyDescription = userFriendlyDescription;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public int getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(int inputOrder) {
        this.inputOrder = inputOrder;
    }

    public boolean isIsRequired() {
        return isRequired;
    }

    public void setIsRequired(boolean required) {
        isRequired = required;
    }

    public boolean isRequiredToAddedToCommandLine() {
        return requiredToAddedToCommandLine;
    }

    public void setRequiredToAddedToCommandLine(boolean requiredToAddedToCommandLine) {
        this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
    }

    public boolean isDataStaged() {
        return dataStaged;
    }

    public void setDataStaged(boolean dataStaged) {
        this.dataStaged = dataStaged;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

	public String getOverrideFilename() {
		return overrideFilename;
	}

	public void setOverrideFilename(String overrideFilename) {
		this.overrideFilename = overrideFilename;
	}

    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity process) {
        this.process = process;
    }
}
