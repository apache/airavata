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
package org.apache.airavata.models.application.io;

/**
 * Application Inputs. The parameters describe how inputs are passed to the application.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.application.io.proto.InputDataObjectType}.
 *
 * @param name Name of the parameter.
 * @param value Value of the parameter. A default value could be set during registration.
 * @param type Data type of the parameter.
 * @param applicationArgument The argument flag sent to the application. Such as -p pressure.
 * @param standardInput When this value is set, the parameter is sent as standard input rather
 *     than a parameter. Typically this is passed using redirection operator ">".
 * @param userFriendlyDescription Description to be displayed at the user interface.
 * @param metaData Any metadata. This is typically ignored by Airavata and is used by gateways for
 *     application configuration.
 * @param overrideFilename Rename input file to given value when staging to compute resource.
 */
public record InputDataObjectType(
        String name,
        String value,
        DataType type,
        String applicationArgument,
        boolean standardInput,
        String userFriendlyDescription,
        String metaData,
        int inputOrder,
        boolean isRequired,
        boolean requiredToAddedToCommandLine,
        boolean dataStaged,
        String storageResourceId,
        boolean isReadOnly,
        String overrideFilename) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String name = "";
        private String value = "";
        private DataType type = DataType.DATA_TYPE_UNKNOWN;
        private String applicationArgument = "";
        private boolean standardInput;
        private String userFriendlyDescription = "";
        private String metaData = "";
        private int inputOrder;
        private boolean isRequired;
        private boolean requiredToAddedToCommandLine;
        private boolean dataStaged;
        private String storageResourceId = "";
        private boolean isReadOnly;
        private String overrideFilename = "";

        private Builder() {}

        private Builder(InputDataObjectType source) {
            this.name = source.name;
            this.value = source.value;
            this.type = source.type;
            this.applicationArgument = source.applicationArgument;
            this.standardInput = source.standardInput;
            this.userFriendlyDescription = source.userFriendlyDescription;
            this.metaData = source.metaData;
            this.inputOrder = source.inputOrder;
            this.isRequired = source.isRequired;
            this.requiredToAddedToCommandLine = source.requiredToAddedToCommandLine;
            this.dataStaged = source.dataStaged;
            this.storageResourceId = source.storageResourceId;
            this.isReadOnly = source.isReadOnly;
            this.overrideFilename = source.overrideFilename;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setType(DataType type) {
            this.type = type;
            return this;
        }

        public Builder setApplicationArgument(String applicationArgument) {
            this.applicationArgument = applicationArgument;
            return this;
        }

        public Builder setStandardInput(boolean standardInput) {
            this.standardInput = standardInput;
            return this;
        }

        public Builder setUserFriendlyDescription(String userFriendlyDescription) {
            this.userFriendlyDescription = userFriendlyDescription;
            return this;
        }

        public Builder setMetaData(String metaData) {
            this.metaData = metaData;
            return this;
        }

        public Builder setInputOrder(int inputOrder) {
            this.inputOrder = inputOrder;
            return this;
        }

        public Builder setIsRequired(boolean isRequired) {
            this.isRequired = isRequired;
            return this;
        }

        public Builder setRequiredToAddedToCommandLine(boolean requiredToAddedToCommandLine) {
            this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
            return this;
        }

        public Builder setDataStaged(boolean dataStaged) {
            this.dataStaged = dataStaged;
            return this;
        }

        public Builder setStorageResourceId(String storageResourceId) {
            this.storageResourceId = storageResourceId;
            return this;
        }

        public Builder setIsReadOnly(boolean isReadOnly) {
            this.isReadOnly = isReadOnly;
            return this;
        }

        public Builder setOverrideFilename(String overrideFilename) {
            this.overrideFilename = overrideFilename;
            return this;
        }

        public InputDataObjectType build() {
            return new InputDataObjectType(
                    name, value, type, applicationArgument, standardInput, userFriendlyDescription,
                    metaData, inputOrder, isRequired, requiredToAddedToCommandLine, dataStaged,
                    storageResourceId, isReadOnly, overrideFilename);
        }
    }
}
