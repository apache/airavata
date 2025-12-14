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
package org.apache.airavata.common.model;

import java.util.Objects;

/**
 * Domain model: OutputDataObjectType
 */
public class OutputDataObjectType {
    private String name;
    private String value;
    private DataType type;
    private String applicationArgument;
    private boolean isRequired;
    private boolean requiredToAddedToCommandLine;
    private boolean dataMovement;
    private String location;
    private String searchQuery;
    private boolean outputStreaming;
    private String storageResourceId;
    private String metaData;

    public OutputDataObjectType() {}

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

    public boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public boolean getRequiredToAddedToCommandLine() {
        return requiredToAddedToCommandLine;
    }

    public void setRequiredToAddedToCommandLine(boolean requiredToAddedToCommandLine) {
        this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
    }

    public boolean getDataMovement() {
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

    public boolean getOutputStreaming() {
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

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutputDataObjectType that = (OutputDataObjectType) o;
        return Objects.equals(name, that.name)
                && Objects.equals(value, that.value)
                && Objects.equals(type, that.type)
                && Objects.equals(applicationArgument, that.applicationArgument)
                && Objects.equals(isRequired, that.isRequired)
                && Objects.equals(requiredToAddedToCommandLine, that.requiredToAddedToCommandLine)
                && Objects.equals(dataMovement, that.dataMovement)
                && Objects.equals(location, that.location)
                && Objects.equals(searchQuery, that.searchQuery)
                && Objects.equals(outputStreaming, that.outputStreaming)
                && Objects.equals(storageResourceId, that.storageResourceId)
                && Objects.equals(metaData, that.metaData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                value,
                type,
                applicationArgument,
                isRequired,
                requiredToAddedToCommandLine,
                dataMovement,
                location,
                searchQuery,
                outputStreaming,
                storageResourceId,
                metaData);
    }

    @Override
    public String toString() {
        return "OutputDataObjectType{" + "name=" + name + ", value=" + value + ", type=" + type
                + ", applicationArgument=" + applicationArgument + ", isRequired=" + isRequired
                + ", requiredToAddedToCommandLine=" + requiredToAddedToCommandLine + ", dataMovement=" + dataMovement
                + ", location=" + location + ", searchQuery=" + searchQuery + ", outputStreaming=" + outputStreaming
                + ", storageResourceId=" + storageResourceId + ", metaData=" + metaData + "}";
    }
}
