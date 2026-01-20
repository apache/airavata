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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model: ApplicationInterfaceDescription
 */
public class ApplicationInterfaceDescription {
    private String applicationInterfaceId;
    private String applicationName;
    private String applicationDescription;
    private List<String> applicationModules = new ArrayList<>();
    private List<InputDataObjectType> applicationInputs = new ArrayList<>();
    private List<OutputDataObjectType> applicationOutputs = new ArrayList<>();
    private boolean archiveWorkingDirectory;
    private boolean hasOptionalFileInputs;
    private boolean cleanAfterStaged;

    public ApplicationInterfaceDescription() {}

    public String getApplicationInterfaceId() {
        return applicationInterfaceId;
    }

    public void setApplicationInterfaceId(String applicationInterfaceId) {
        this.applicationInterfaceId = applicationInterfaceId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationDescription() {
        return applicationDescription;
    }

    public void setApplicationDescription(String applicationDescription) {
        this.applicationDescription = applicationDescription;
    }

    public List<String> getApplicationModules() {
        return applicationModules;
    }

    public void setApplicationModules(List<String> applicationModules) {
        this.applicationModules = applicationModules;
    }

    public List<InputDataObjectType> getApplicationInputs() {
        return applicationInputs;
    }

    public void setApplicationInputs(List<InputDataObjectType> applicationInputs) {
        this.applicationInputs = applicationInputs;
    }

    public List<OutputDataObjectType> getApplicationOutputs() {
        return applicationOutputs;
    }

    public void setApplicationOutputs(List<OutputDataObjectType> applicationOutputs) {
        this.applicationOutputs = applicationOutputs;
    }

    public boolean getArchiveWorkingDirectory() {
        return archiveWorkingDirectory;
    }

    public void setArchiveWorkingDirectory(boolean archiveWorkingDirectory) {
        this.archiveWorkingDirectory = archiveWorkingDirectory;
    }

    public boolean getHasOptionalFileInputs() {
        return hasOptionalFileInputs;
    }

    public void setHasOptionalFileInputs(boolean hasOptionalFileInputs) {
        this.hasOptionalFileInputs = hasOptionalFileInputs;
    }

    public boolean getCleanAfterStaged() {
        return cleanAfterStaged;
    }

    public void setCleanAfterStaged(boolean cleanAfterStaged) {
        this.cleanAfterStaged = cleanAfterStaged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationInterfaceDescription that = (ApplicationInterfaceDescription) o;
        return Objects.equals(applicationInterfaceId, that.applicationInterfaceId)
                && Objects.equals(applicationName, that.applicationName)
                && Objects.equals(applicationDescription, that.applicationDescription)
                && Objects.equals(applicationModules, that.applicationModules)
                && Objects.equals(applicationInputs, that.applicationInputs)
                && Objects.equals(applicationOutputs, that.applicationOutputs)
                && Objects.equals(archiveWorkingDirectory, that.archiveWorkingDirectory)
                && Objects.equals(hasOptionalFileInputs, that.hasOptionalFileInputs)
                && Objects.equals(cleanAfterStaged, that.cleanAfterStaged);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                applicationInterfaceId,
                applicationName,
                applicationDescription,
                applicationModules,
                applicationInputs,
                applicationOutputs,
                archiveWorkingDirectory,
                hasOptionalFileInputs,
                cleanAfterStaged);
    }

    @Override
    public String toString() {
        return "ApplicationInterfaceDescription{" + "applicationInterfaceId=" + applicationInterfaceId
                + ", applicationName=" + applicationName + ", applicationDescription=" + applicationDescription
                + ", applicationModules=" + applicationModules + ", applicationInputs=" + applicationInputs
                + ", applicationOutputs=" + applicationOutputs + ", archiveWorkingDirectory=" + archiveWorkingDirectory
                + ", hasOptionalFileInputs=" + hasOptionalFileInputs + ", cleanAfterStaged=" + cleanAfterStaged + "}";
    }
}
