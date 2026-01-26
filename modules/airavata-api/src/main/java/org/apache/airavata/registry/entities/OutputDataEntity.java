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
package org.apache.airavata.registry.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import org.apache.airavata.common.model.DataObjectParentType;
import org.apache.airavata.common.model.DataType;

/**
 * Unified OutputDataEntity that consolidates output data records from experiments, processes,
 * applications, and handlers.
 *
 * <p>This entity replaces the following separate entities:
 * <ul>
 *   <li>{@code ExperimentOutputEntity}</li>
 *   <li>{@code ProcessOutputEntity}</li>
 *   <li>{@code ApplicationOutputEntity}</li>
 *   <li>{@code HandlerOutputEntity}</li>
 * </ul>
 *
 * <p>The {@code parentType} field discriminates between different output sources,
 * while {@code parentId} stores the ID of the parent entity (experiment ID, process ID, etc.).
 */
@Entity(name = "OutputDataEntity")
@Table(
        name = "OUTPUT_DATA",
        indexes = {
            @Index(name = "idx_output_parent", columnList = "PARENT_ID, PARENT_TYPE"),
            @Index(name = "idx_output_name", columnList = "OUTPUT_NAME")
        })
@IdClass(OutputDataEntityPK.class)
public class OutputDataEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PARENT_ID", nullable = false)
    private String parentId;

    @Id
    @Column(name = "PARENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private DataObjectParentType parentType;

    @Id
    @Column(name = "OUTPUT_NAME", nullable = false)
    private String name;

    @Lob
    @Column(name = "OUTPUT_VALUE")
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

    @Column(name = "DATA_MOVEMENT")
    private boolean dataMovement;

    @Column(name = "LOCATION")
    private String location;

    @Column(name = "SEARCH_QUERY")
    private String searchQuery;

    @Column(name = "OUTPUT_STREAMING")
    private boolean outputStreaming;

    @Column(name = "STORAGE_RESOURCE_ID")
    private String storageResourceId;

    @Lob
    @Column(name = "USER_FRIENDLY_DESCRIPTION")
    private String userFriendlyDescription;

    @Column(name = "METADATA", length = 4096)
    private String metaData;

    @Column(name = "OUTPUT_ORDER")
    private int outputOrder;

    public OutputDataEntity() {}

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public DataObjectParentType getParentType() {
        return parentType;
    }

    public void setParentType(DataObjectParentType parentType) {
        this.parentType = parentType;
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

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public boolean isRequiredToAddedToCommandLine() {
        return requiredToAddedToCommandLine;
    }

    public void setRequiredToAddedToCommandLine(boolean requiredToAddedToCommandLine) {
        this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
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

    public String getStorageResourceId() {
        return storageResourceId;
    }

    public void setStorageResourceId(String storageResourceId) {
        this.storageResourceId = storageResourceId;
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

    public int getOutputOrder() {
        return outputOrder;
    }

    public void setOutputOrder(int outputOrder) {
        this.outputOrder = outputOrder;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        OutputDataEntity that = (OutputDataEntity) obj;
        return Objects.equals(parentId, that.parentId)
                && parentType == that.parentType
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, parentType, name);
    }

    @Override
    public String toString() {
        return "OutputDataEntity{"
                + "parentId='"
                + parentId
                + '\''
                + ", parentType="
                + parentType
                + ", name='"
                + name
                + '\''
                + ", type="
                + type
                + ", outputOrder="
                + outputOrder
                + ", isRequired="
                + isRequired
                + '}';
    }
}
