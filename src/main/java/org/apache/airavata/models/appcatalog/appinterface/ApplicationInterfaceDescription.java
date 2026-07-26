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
package org.apache.airavata.models.appcatalog.appinterface;

import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.models.application.io.InputDataObjectType;
import org.apache.airavata.models.application.io.OutputDataObjectType;

/**
 * Application Interface Description.
 *
 * <p>Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.appinterface.proto.ApplicationInterfaceDescription}.
 *
 * @param applicationInterfaceId Airavata Internal Unique ID. This is set by the registry.
 * @param applicationName Name of the application interface.
 * @param applicationDescription Optional description.
 * @param applicationModules Associate all application modules with versions which interface is
 *     applicable to.
 * @param applicationInputs Inputs to be passed to the application.
 * @param applicationOutputs Outputs generated from the application.
 * @param archiveWorkingDirectory Whether to archive the working directory.
 * @param hasOptionalFileInputs Whether the application has optional file inputs.
 * @param cleanAfterStaged Whether to clean up after staging.
 */
public record ApplicationInterfaceDescription(
        String applicationInterfaceId,
        String applicationName,
        String applicationDescription,
        List<String> applicationModules,
        List<InputDataObjectType> applicationInputs,
        List<OutputDataObjectType> applicationOutputs,
        boolean archiveWorkingDirectory,
        boolean hasOptionalFileInputs,
        boolean cleanAfterStaged) {

    public ApplicationInterfaceDescription {
        applicationModules = applicationModules == null ? List.of() : List.copyOf(applicationModules);
        applicationInputs = applicationInputs == null ? List.of() : List.copyOf(applicationInputs);
        applicationOutputs = applicationOutputs == null ? List.of() : List.copyOf(applicationOutputs);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String applicationInterfaceId = "";
        private String applicationName = "";
        private String applicationDescription = "";
        private List<String> applicationModules = new ArrayList<>();
        private List<InputDataObjectType> applicationInputs = new ArrayList<>();
        private List<OutputDataObjectType> applicationOutputs = new ArrayList<>();
        private boolean archiveWorkingDirectory;
        private boolean hasOptionalFileInputs;
        private boolean cleanAfterStaged;

        private Builder() {}

        private Builder(ApplicationInterfaceDescription source) {
            this.applicationInterfaceId = source.applicationInterfaceId;
            this.applicationName = source.applicationName;
            this.applicationDescription = source.applicationDescription;
            this.applicationModules = new ArrayList<>(source.applicationModules);
            this.applicationInputs = new ArrayList<>(source.applicationInputs);
            this.applicationOutputs = new ArrayList<>(source.applicationOutputs);
            this.archiveWorkingDirectory = source.archiveWorkingDirectory;
            this.hasOptionalFileInputs = source.hasOptionalFileInputs;
            this.cleanAfterStaged = source.cleanAfterStaged;
        }

        public Builder setApplicationInterfaceId(String applicationInterfaceId) {
            this.applicationInterfaceId = applicationInterfaceId;
            return this;
        }

        public Builder setApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder setApplicationDescription(String applicationDescription) {
            this.applicationDescription = applicationDescription;
            return this;
        }

        public Builder addApplicationModules(String value) {
            this.applicationModules.add(value);
            return this;
        }

        public Builder addAllApplicationModules(Iterable<String> values) {
            values.forEach(this.applicationModules::add);
            return this;
        }

        public Builder clearApplicationModules() {
            this.applicationModules.clear();
            return this;
        }

        public Builder addApplicationInputs(InputDataObjectType value) {
            this.applicationInputs.add(value);
            return this;
        }

        public Builder addAllApplicationInputs(Iterable<InputDataObjectType> values) {
            values.forEach(this.applicationInputs::add);
            return this;
        }

        public Builder clearApplicationInputs() {
            this.applicationInputs.clear();
            return this;
        }

        public Builder addApplicationOutputs(OutputDataObjectType value) {
            this.applicationOutputs.add(value);
            return this;
        }

        public Builder addAllApplicationOutputs(Iterable<OutputDataObjectType> values) {
            values.forEach(this.applicationOutputs::add);
            return this;
        }

        public Builder clearApplicationOutputs() {
            this.applicationOutputs.clear();
            return this;
        }

        public Builder setArchiveWorkingDirectory(boolean archiveWorkingDirectory) {
            this.archiveWorkingDirectory = archiveWorkingDirectory;
            return this;
        }

        public Builder setHasOptionalFileInputs(boolean hasOptionalFileInputs) {
            this.hasOptionalFileInputs = hasOptionalFileInputs;
            return this;
        }

        public Builder setCleanAfterStaged(boolean cleanAfterStaged) {
            this.cleanAfterStaged = cleanAfterStaged;
            return this;
        }

        public ApplicationInterfaceDescription build() {
            return new ApplicationInterfaceDescription(
                    applicationInterfaceId, applicationName, applicationDescription, applicationModules,
                    applicationInputs, applicationOutputs, archiveWorkingDirectory, hasOptionalFileInputs,
                    cleanAfterStaged);
        }
    }
}
