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
 * Domain model: InputDataObjectType
 */
public class InputDataObjectType {
    private String name;
    private String value;
    private DataType type;
    private String applicationArgument;
    private boolean standardInput;
    private String userFriendlyDescription;
    private String metaData;
    private int inputOrder;
    private boolean isRequired;
    private boolean requiredToAddedToCommandLine;
    private boolean dataStaged;
    private String storageResourceId;
    private boolean isReadOnly;
    private String overrideFilename;

    public InputDataObjectType() {}

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

    public boolean getStandardInput() {
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

    public boolean getDataStaged() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputDataObjectType that = (InputDataObjectType) o;
        return Objects.equals(name, that.name)
                && Objects.equals(value, that.value)
                && Objects.equals(type, that.type)
                && Objects.equals(applicationArgument, that.applicationArgument)
                && Objects.equals(standardInput, that.standardInput)
                && Objects.equals(userFriendlyDescription, that.userFriendlyDescription)
                && Objects.equals(metaData, that.metaData)
                && Objects.equals(inputOrder, that.inputOrder)
                && Objects.equals(isRequired, that.isRequired)
                && Objects.equals(requiredToAddedToCommandLine, that.requiredToAddedToCommandLine)
                && Objects.equals(dataStaged, that.dataStaged)
                && Objects.equals(storageResourceId, that.storageResourceId)
                && Objects.equals(isReadOnly, that.isReadOnly)
                && Objects.equals(overrideFilename, that.overrideFilename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                value,
                type,
                applicationArgument,
                standardInput,
                userFriendlyDescription,
                metaData,
                inputOrder,
                isRequired,
                requiredToAddedToCommandLine,
                dataStaged,
                storageResourceId,
                isReadOnly,
                overrideFilename);
    }

    @Override
    public String toString() {
        return "InputDataObjectType{" + "name=" + name + ", value=" + value + ", type=" + type
                + ", applicationArgument=" + applicationArgument + ", standardInput=" + standardInput
                + ", userFriendlyDescription=" + userFriendlyDescription + ", metaData=" + metaData + ", inputOrder="
                + inputOrder + ", isRequired=" + isRequired + ", requiredToAddedToCommandLine="
                + requiredToAddedToCommandLine + ", dataStaged=" + dataStaged + ", storageResourceId="
                + storageResourceId + ", isReadOnly=" + isReadOnly + ", overrideFilename=" + overrideFilename + "}";
    }
}
