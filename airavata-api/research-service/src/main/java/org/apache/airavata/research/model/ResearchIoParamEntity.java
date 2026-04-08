/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.research.model;

import jakarta.persistence.*;
import java.io.Serializable;
import org.apache.airavata.model.application.io.proto.DataType;

/**
 * Unified persistent class for experiment input and output parameters.
 * The {@code direction} column discriminates between INPUT and OUTPUT rows.
 */
@Entity
@Table(name = "RESEARCH_IO_PARAM")
public class ResearchIoParamEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "PARAM_ID")
    private String paramId;

    @Column(name = "ENTITY_ID", insertable = false, updatable = false)
    private String entityId;

    @Column(name = "DIRECTION", nullable = false)
    private String direction; // INPUT or OUTPUT

    @Column(name = "PARAM_NAME")
    private String name;

    @Lob
    @Column(name = "PARAM_VALUE")
    private String value;

    @Column(name = "DATA_TYPE")
    @Enumerated(EnumType.STRING)
    private DataType type;

    @Column(name = "APPLICATION_ARGUMENT")
    private String applicationArgument;

    @Column(name = "IS_REQUIRED")
    private boolean isRequired;

    @Column(name = "REQUIRED_TO_ADDED_TO_CMD")
    private boolean requiredToAddedToCommandLine;

    @Column(name = "METADATA", length = 4096)
    private String metaData;

    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;

    // Input-specific fields
    @Column(name = "STANDARD_INPUT")
    private boolean standardInput;

    @Lob
    @Column(name = "USER_FRIENDLY_DESCRIPTION")
    private String userFriendlyDescription;

    @Column(name = "INPUT_ORDER")
    private int inputOrder;

    @Column(name = "DATA_STAGED")
    private boolean dataStaged;

    @Column(name = "IS_READ_ONLY")
    private boolean isReadOnly;

    @Column(name = "OVERRIDE_FILENAME")
    private String overrideFilename;

    // Output-specific fields
    @Column(name = "DATA_MOVEMENT")
    private boolean dataMovement;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "SEARCH_QUERY")
    private String searchQuery;

    @Column(name = "OUTPUT_STREAMING")
    private boolean outputStreaming;

    public ResearchIoParamEntity() {}

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
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

    public boolean isIsRequired() {
        return isRequired;
    }

    public void setIsRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public boolean isRequiredToAddedToCommandLine() {
        return requiredToAddedToCommandLine;
    }

    public void setRequiredToAddedToCommandLine(boolean requiredToAddedToCommandLine) {
        this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
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

    public int getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(int inputOrder) {
        this.inputOrder = inputOrder;
    }

    public boolean isDataStaged() {
        return dataStaged;
    }

    public void setDataStaged(boolean dataStaged) {
        this.dataStaged = dataStaged;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
    }

    public String getOverrideFilename() {
        return overrideFilename;
    }

    public void setOverrideFilename(String overrideFilename) {
        this.overrideFilename = overrideFilename;
    }

    public boolean isDataMovement() {
        return dataMovement;
    }

    public void setDataMovement(boolean dataMovement) {
        this.dataMovement = dataMovement;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public boolean isOutputStreaming() {
        return outputStreaming;
    }

    public void setOutputStreaming(boolean outputStreaming) {
        this.outputStreaming = outputStreaming;
    }
}
