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
package org.apache.airavata.research.application.model;

import java.util.List;

/**
 * Domain model for application interface description including inputs, outputs, and module references.
 */
public class ApplicationInterfaceDescription {

    private String applicationInterfaceId;
    private String applicationName;
    private String applicationDescription;
    private List<String> applicationModules;
    private List<ApplicationInput> applicationInputs;
    private List<ApplicationOutput> applicationOutputs;
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

    public List<ApplicationInput> getApplicationInputs() {
        return applicationInputs;
    }

    public void setApplicationInputs(List<ApplicationInput> applicationInputs) {
        this.applicationInputs = applicationInputs;
    }

    public List<ApplicationOutput> getApplicationOutputs() {
        return applicationOutputs;
    }

    public void setApplicationOutputs(List<ApplicationOutput> applicationOutputs) {
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
}
