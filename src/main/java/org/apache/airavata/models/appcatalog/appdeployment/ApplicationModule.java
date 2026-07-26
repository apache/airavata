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
package org.apache.airavata.models.appcatalog.appdeployment;

/**
 * Plain-POJO replacement for the generated
 * {@code org.apache.airavata.model.appcatalog.appdeployment.proto.ApplicationModule}.
 */
public record ApplicationModule(
        String appModuleId, String appModuleName, String appModuleVersion, String appModuleDescription) {

    public static Builder newBuilder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {
        private String appModuleId = "";
        private String appModuleName = "";
        private String appModuleVersion = "";
        private String appModuleDescription = "";

        private Builder() {}

        private Builder(ApplicationModule source) {
            this.appModuleId = source.appModuleId;
            this.appModuleName = source.appModuleName;
            this.appModuleVersion = source.appModuleVersion;
            this.appModuleDescription = source.appModuleDescription;
        }

        public Builder setAppModuleId(String appModuleId) {
            this.appModuleId = appModuleId;
            return this;
        }

        public Builder setAppModuleName(String appModuleName) {
            this.appModuleName = appModuleName;
            return this;
        }

        public Builder setAppModuleVersion(String appModuleVersion) {
            this.appModuleVersion = appModuleVersion;
            return this;
        }

        public Builder setAppModuleDescription(String appModuleDescription) {
            this.appModuleDescription = appModuleDescription;
            return this;
        }

        public ApplicationModule build() {
            return new ApplicationModule(appModuleId, appModuleName, appModuleVersion, appModuleDescription);
        }
    }
}
