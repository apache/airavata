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
 * Domain model: ApplicationModule
 */
public class ApplicationModule {
    private String appModuleId;
    private String appModuleName;
    private String appModuleVersion;
    private String appModuleDescription;

    public ApplicationModule() {}

    public String getAppModuleId() {
        return appModuleId;
    }

    public void setAppModuleId(String appModuleId) {
        this.appModuleId = appModuleId;
    }

    public String getAppModuleName() {
        return appModuleName;
    }

    public void setAppModuleName(String appModuleName) {
        this.appModuleName = appModuleName;
    }

    public String getAppModuleVersion() {
        return appModuleVersion;
    }

    public void setAppModuleVersion(String appModuleVersion) {
        this.appModuleVersion = appModuleVersion;
    }

    public String getAppModuleDescription() {
        return appModuleDescription;
    }

    public void setAppModuleDescription(String appModuleDescription) {
        this.appModuleDescription = appModuleDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationModule that = (ApplicationModule) o;
        return Objects.equals(appModuleId, that.appModuleId)
                && Objects.equals(appModuleName, that.appModuleName)
                && Objects.equals(appModuleVersion, that.appModuleVersion)
                && Objects.equals(appModuleDescription, that.appModuleDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(appModuleId, appModuleName, appModuleVersion, appModuleDescription);
    }

    @Override
    public String toString() {
        return "ApplicationModule{" + "appModuleId=" + appModuleId + ", appModuleName=" + appModuleName
                + ", appModuleVersion=" + appModuleVersion + ", appModuleDescription=" + appModuleDescription + "}";
    }
}
